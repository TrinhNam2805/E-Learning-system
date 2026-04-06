package com.elearning.service;

import com.elearning.exception.AssessmentDeadlineException;
import com.elearning.exception.AssessmentStorageException;
import com.elearning.exception.AssessmentValidationException;
import com.elearning.model.dto.assessment.AssignmentSubmissionForm;
import com.elearning.model.entity.Assignment;
import com.elearning.model.entity.Course;
import com.elearning.model.entity.QuizQuestion;
import com.elearning.model.entity.Submission;
import com.elearning.model.entity.User;
import com.elearning.repository.AssignmentRepository;
import com.elearning.repository.QuizQuestionRepository;
import com.elearning.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private QuizQuestionRepository quizQuestionRepository;
    @Mock
    private GamificationService gamificationService;
    @Mock
    private EnrollmentService enrollmentService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AssessmentFileStorageService assessmentFileStorageService;

    @InjectMocks
    private AssignmentService assignmentService;

    private User student;
    private Course course;
    private Assignment homework;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id(10L)
                .role(User.Role.STUDENT)
                .email("student@elearning.test")
                .fullName("Student A")
                .build();

        course = Course.builder()
                .id(30L)
                .courseCode("SE330")
                .courseName("Software Engineering")
                .build();

        homework = Assignment.builder()
                .id(40L)
                .course(course)
                .title("Week 1 Homework")
                .type(Assignment.AssignmentType.HOMEWORK)
                .dueDate(LocalDateTime.now().plusDays(1))
                .maxScore(10.0)
                .allowLateSubmission(false)
                .maxAttempts(1)
                .build();
    }

    @Test
    void submitOnTimeShouldSaveSubmission() {
        AssignmentSubmissionForm form = new AssignmentSubmissionForm();
        form.setContent("Homework content");

        when(assignmentRepository.findDetailedById(homework.getId())).thenReturn(Optional.of(homework));
        when(enrollmentService.isEnrolled(student.getId(), course.getId())).thenReturn(true);
        when(submissionRepository.countByAssignmentIdAndStudentId(homework.getId(), student.getId())).thenReturn(0L);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssignmentService.SubmissionResult result = assignmentService.submit(
                homework.getId(), student, form, new HashMap<String, String>());

        assertNotNull(result.getSubmission());
        assertEquals("Homework content", result.getSubmission().getContent());
        assertEquals(Submission.SubmissionStatus.SUBMITTED, result.getSubmission().getStatus());
        assertFalse(result.getSubmission().isLateSubmission());
        assertEquals(1, result.getSubmission().getAttemptNumber());
        assertFalse(result.isUpdatedExisting());
        verify(submissionRepository).save(any(Submission.class));
    }

    @Test
    void submitLateShouldRejectWhenLateSubmissionDisabled() {
        homework.setDueDate(LocalDateTime.now().minusHours(3));

        AssignmentSubmissionForm form = new AssignmentSubmissionForm();
        form.setContent("Late homework");

        when(assignmentRepository.findDetailedById(homework.getId())).thenReturn(Optional.of(homework));
        when(enrollmentService.isEnrolled(student.getId(), course.getId())).thenReturn(true);
        when(submissionRepository.countByAssignmentIdAndStudentId(homework.getId(), student.getId())).thenReturn(0L);

        AssessmentDeadlineException ex = assertThrows(
                AssessmentDeadlineException.class,
                () -> assignmentService.submit(homework.getId(), student, form, new HashMap<String, String>()));

        assertEquals("This assignment is past due and late submission is not allowed.", ex.getMessage());
        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void submitShouldRejectInvalidFileFormat() {
        AssignmentSubmissionForm form = new AssignmentSubmissionForm();
        form.setAttachment(new MockMultipartFile(
                "attachment", "malware.exe", "application/octet-stream", "virus".getBytes()));

        when(assignmentRepository.findDetailedById(homework.getId())).thenReturn(Optional.of(homework));
        when(enrollmentService.isEnrolled(student.getId(), course.getId())).thenReturn(true);
        when(submissionRepository.countByAssignmentIdAndStudentId(homework.getId(), student.getId())).thenReturn(0L);
        when(assessmentFileStorageService.storeSubmissionFile(eq(homework.getId()), eq(student.getId()), any(MockMultipartFile.class)))
                .thenThrow(new AssessmentValidationException("Unsupported file format."));

        AssessmentValidationException ex = assertThrows(
                AssessmentValidationException.class,
                () -> assignmentService.submit(homework.getId(), student, form, new HashMap<String, String>()));

        assertEquals("Unsupported file format.", ex.getMessage());
    }

    @Test
    void submitShouldRejectOversizedFile() {
        AssignmentSubmissionForm form = new AssignmentSubmissionForm();
        form.setAttachment(new MockMultipartFile("attachment", "big.pdf", "application/pdf", new byte[8]));

        when(assignmentRepository.findDetailedById(homework.getId())).thenReturn(Optional.of(homework));
        when(enrollmentService.isEnrolled(student.getId(), course.getId())).thenReturn(true);
        when(submissionRepository.countByAssignmentIdAndStudentId(homework.getId(), student.getId())).thenReturn(0L);
        when(assessmentFileStorageService.storeSubmissionFile(eq(homework.getId()), eq(student.getId()), any(MockMultipartFile.class)))
                .thenThrow(new AssessmentValidationException("File size exceeds the 5 MB limit."));

        AssessmentValidationException ex = assertThrows(
                AssessmentValidationException.class,
                () -> assignmentService.submit(homework.getId(), student, form, new HashMap<String, String>()));

        assertEquals("File size exceeds the 5 MB limit.", ex.getMessage());
    }

    @Test
    void submitShouldSurfaceStorageFailure() {
        AssignmentSubmissionForm form = new AssignmentSubmissionForm();
        form.setAttachment(new MockMultipartFile("attachment", "essay.pdf", "application/pdf", "data".getBytes()));

        when(assignmentRepository.findDetailedById(homework.getId())).thenReturn(Optional.of(homework));
        when(enrollmentService.isEnrolled(student.getId(), course.getId())).thenReturn(true);
        when(submissionRepository.countByAssignmentIdAndStudentId(homework.getId(), student.getId())).thenReturn(0L);
        when(assessmentFileStorageService.storeSubmissionFile(eq(homework.getId()), eq(student.getId()), any(MockMultipartFile.class)))
                .thenThrow(new AssessmentStorageException("Cannot store submission file right now.", new RuntimeException("network")));

        AssessmentStorageException ex = assertThrows(
                AssessmentStorageException.class,
                () -> assignmentService.submit(homework.getId(), student, form, new HashMap<String, String>()));

        assertEquals("Cannot store submission file right now.", ex.getMessage());
    }

    @Test
    void homeworkResubmissionShouldUpdateExistingSubmissionBeforeDueDate() {
        Submission existingSubmission = Submission.builder()
                .id(90L)
                .assignment(homework)
                .student(student)
                .content("Old content")
                .score(7.5)
                .feedback("Previous feedback")
                .status(Submission.SubmissionStatus.GRADED)
                .attemptNumber(1)
                .build();

        AssignmentSubmissionForm form = new AssignmentSubmissionForm();
        form.setContent("Updated content");

        when(assignmentRepository.findDetailedById(homework.getId())).thenReturn(Optional.of(homework));
        when(enrollmentService.isEnrolled(student.getId(), course.getId())).thenReturn(true);
        when(submissionRepository.findHistoryByAssignmentIdAndStudentId(homework.getId(), student.getId()))
                .thenReturn(List.of(existingSubmission));
        when(submissionRepository.countByAssignmentIdAndStudentId(homework.getId(), student.getId())).thenReturn(1L);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssignmentService.SubmissionResult result = assignmentService.submit(
                homework.getId(), student, form, new HashMap<String, String>());

        assertTrue(result.isUpdatedExisting());
        assertEquals(existingSubmission.getId(), result.getSubmission().getId());
        assertEquals("Updated content", result.getSubmission().getContent());
        assertEquals(Submission.SubmissionStatus.SUBMITTED, result.getSubmission().getStatus());
        assertEquals(null, result.getSubmission().getScore());
        assertEquals(null, result.getSubmission().getFeedback());
        assertEquals(1, result.getSubmission().getAttemptNumber());
        assertEquals(0, result.getAwardedXp());
        verify(submissionRepository).save(existingSubmission);
    }

    @Test
    void homeworkResubmissionWithNewFileShouldReplaceAndDeleteOldAttachment() {
        Submission existingSubmission = Submission.builder()
                .id(93L)
                .assignment(homework)
                .student(student)
                .content("Old content")
                .fileUrl("assignment-40-student-10-old.pdf")
                .originalFileName("old-homework.pdf")
                .fileSize(1024L)
                .status(Submission.SubmissionStatus.SUBMITTED)
                .attemptNumber(1)
                .build();

        AssignmentSubmissionForm form = new AssignmentSubmissionForm();
        form.setContent("Updated content");
        form.setAttachment(new MockMultipartFile(
                "attachment",
                "new-homework.pdf",
                "application/pdf",
                "new file".getBytes()));

        when(assignmentRepository.findDetailedById(homework.getId())).thenReturn(Optional.of(homework));
        when(enrollmentService.isEnrolled(student.getId(), course.getId())).thenReturn(true);
        when(submissionRepository.findHistoryByAssignmentIdAndStudentId(homework.getId(), student.getId()))
                .thenReturn(List.of(existingSubmission));
        when(submissionRepository.countByAssignmentIdAndStudentId(homework.getId(), student.getId())).thenReturn(1L);
        when(assessmentFileStorageService.storeSubmissionFile(eq(homework.getId()), eq(student.getId()), any(MockMultipartFile.class)))
                .thenReturn(new AssessmentFileStorageService.StoredFile(
                        "assignment-40-student-10-new.pdf",
                        "new-homework.pdf",
                        2048L));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssignmentService.SubmissionResult result = assignmentService.submit(
                homework.getId(), student, form, new HashMap<String, String>());

        assertTrue(result.isUpdatedExisting());
        assertEquals("assignment-40-student-10-new.pdf", result.getSubmission().getFileUrl());
        assertEquals("new-homework.pdf", result.getSubmission().getOriginalFileName());
        assertEquals(2048L, result.getSubmission().getFileSize());
        verify(assessmentFileStorageService).deleteSubmissionFile("assignment-40-student-10-old.pdf");
        verify(submissionRepository).save(existingSubmission);
    }

    @Test
    void removeHomeworkSubmissionShouldDeleteExistingSubmissionBeforeDueDate() {
        Submission existingSubmission = Submission.builder()
                .id(92L)
                .assignment(homework)
                .student(student)
                .content("Uploaded homework")
                .fileUrl("assignment-40-student-10.pdf")
                .originalFileName("week1.pdf")
                .attemptNumber(1)
                .build();

        when(assignmentRepository.findDetailedById(homework.getId())).thenReturn(Optional.of(homework));
        when(enrollmentService.isEnrolled(student.getId(), course.getId())).thenReturn(true);
        when(submissionRepository.findHistoryByAssignmentIdAndStudentId(homework.getId(), student.getId()))
                .thenReturn(List.of(existingSubmission));

        assignmentService.removeHomeworkSubmission(homework.getId(), student);

        verify(assessmentFileStorageService).deleteSubmissionFile("assignment-40-student-10.pdf");
        verify(submissionRepository).delete(existingSubmission);
    }

    @Test
    void quizResubmissionShouldCreateAnotherAttemptWithoutAwardingMoreXp() {
        Assignment quiz = Assignment.builder()
                .id(41L)
                .course(course)
                .title("Week 1 Quiz")
                .type(Assignment.AssignmentType.QUIZ)
                .dueDate(LocalDateTime.now().plusDays(1))
                .maxScore(10.0)
                .minimumPassingScore(6.0)
                .allowLateSubmission(false)
                .maxAttempts(1)
                .build();

        Submission existingAttempt = Submission.builder()
                .id(91L)
                .assignment(quiz)
                .student(student)
                .attemptNumber(1)
                .score(8.0)
                .status(Submission.SubmissionStatus.GRADED)
                .build();

        QuizQuestion question1 = QuizQuestion.builder()
                .id(501L)
                .assignment(quiz)
                .questionOrder(1)
                .correctAnswer("A")
                .build();
        QuizQuestion question2 = QuizQuestion.builder()
                .id(502L)
                .assignment(quiz)
                .questionOrder(2)
                .correctAnswer("B")
                .build();

        AssignmentSubmissionForm form = new AssignmentSubmissionForm();
        HashMap<String, String> answers = new HashMap<String, String>();
        answers.put("q_501", "A");
        answers.put("q_502", "B");

        when(assignmentRepository.findDetailedById(quiz.getId())).thenReturn(Optional.of(quiz));
        when(enrollmentService.isEnrolled(student.getId(), course.getId())).thenReturn(true);
        when(submissionRepository.findHistoryByAssignmentIdAndStudentId(quiz.getId(), student.getId()))
                .thenReturn(List.of(existingAttempt));
        when(submissionRepository.countByAssignmentIdAndStudentId(quiz.getId(), student.getId())).thenReturn(1L);
        when(quizQuestionRepository.findByAssignmentIdOrderByQuestionOrderAsc(quiz.getId()))
                .thenReturn(Arrays.asList(question1, question2));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssignmentService.SubmissionResult result = assignmentService.submit(quiz.getId(), student, form, answers);

        assertEquals(2, result.getSubmission().getAttemptNumber());
        assertEquals(10.0, result.getSubmission().getScore());
        assertEquals(0, result.getAwardedXp());
        verify(gamificationService, never()).awardQuizXp(any(Long.class), any(Long.class), anyDouble(), anyDouble());
    }

    @Test
    void viewScoreWhenNotGradedShouldStayPending() {
        Submission submission = Submission.builder()
                .assignment(homework)
                .student(student)
                .status(Submission.SubmissionStatus.SUBMITTED)
                .attemptNumber(1)
                .content("Pending review")
                .build();

        assertFalse(com.elearning.model.dto.assessment.AssessmentSubmissionDto.fromEntity(submission).isGraded());
        assertEquals(null, com.elearning.model.dto.assessment.AssessmentSubmissionDto.fromEntity(submission).getScore());
    }
}
