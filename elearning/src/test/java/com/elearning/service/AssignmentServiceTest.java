package com.elearning.service;

import com.elearning.exception.AssessmentDeadlineException;
import com.elearning.exception.AssessmentStorageException;
import com.elearning.exception.AssessmentValidationException;
import com.elearning.model.dto.assessment.AssignmentSubmissionForm;
import com.elearning.model.dto.assessment.SubmissionGradeForm;
import com.elearning.model.entity.Assignment;
import com.elearning.model.entity.Course;
import com.elearning.model.entity.Notification;
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
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
                .fullName("Sinh vien A")
                .build();

        User teacher = User.builder()
                .id(20L)
                .role(User.Role.TEACHER)
                .fullName("Giang vien B")
                .build();

        course = Course.builder()
                .id(30L)
                .courseCode("SE330")
                .courseName("Software Engineering")
                .teacher(teacher)
                .build();

        homework = Assignment.builder()
                .id(40L)
                .course(course)
                .title("Bai tap tuan 1")
                .type(Assignment.AssignmentType.HOMEWORK)
                .dueDate(LocalDateTime.now().plusDays(1))
                .maxScore(10.0)
                .allowLateSubmission(false)
                .maxAttempts(2)
                .build();
    }

    @Test
    void submitOnTimeShouldSaveSubmission() {
        AssignmentSubmissionForm form = new AssignmentSubmissionForm();
        form.setContent("Noi dung bai lam");

        when(assignmentRepository.findDetailedById(homework.getId())).thenReturn(Optional.of(homework));
        when(enrollmentService.isEnrolled(student.getId(), course.getId())).thenReturn(true);
        when(submissionRepository.countByAssignmentIdAndStudentId(homework.getId(), student.getId())).thenReturn(0L);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssignmentService.SubmissionResult result = assignmentService.submit(
                homework.getId(), student, form, new HashMap<String, String>());

        assertNotNull(result.getSubmission());
        assertEquals("Noi dung bai lam", result.getSubmission().getContent());
        assertEquals(Submission.SubmissionStatus.SUBMITTED, result.getSubmission().getStatus());
        assertFalse(result.getSubmission().isLateSubmission());
        assertEquals(1, result.getSubmission().getAttemptNumber());
        verify(submissionRepository).save(any(Submission.class));
    }

    @Test
    void submitLateShouldRejectWhenLateSubmissionDisabled() {
        homework.setDueDate(LocalDateTime.now().minusHours(3));

        AssignmentSubmissionForm form = new AssignmentSubmissionForm();
        form.setContent("Noi dung tre han");

        when(assignmentRepository.findDetailedById(homework.getId())).thenReturn(Optional.of(homework));
        when(enrollmentService.isEnrolled(student.getId(), course.getId())).thenReturn(true);
        when(submissionRepository.countByAssignmentIdAndStudentId(homework.getId(), student.getId())).thenReturn(0L);

        AssessmentDeadlineException ex = assertThrows(AssessmentDeadlineException.class,
                () -> assignmentService.submit(homework.getId(), student, form, new HashMap<String, String>()));

        assertEquals("Bài tập đã quá hạn và không cho phép nộp trễ.", ex.getMessage());
        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void submitShouldRejectInvalidFileFormat() {
        AssignmentSubmissionForm form = new AssignmentSubmissionForm();
        form.setAttachment(new MockMultipartFile("attachment", "malware.exe",
                "application/octet-stream", "virus".getBytes()));

        when(assignmentRepository.findDetailedById(homework.getId())).thenReturn(Optional.of(homework));
        when(enrollmentService.isEnrolled(student.getId(), course.getId())).thenReturn(true);
        when(submissionRepository.countByAssignmentIdAndStudentId(homework.getId(), student.getId())).thenReturn(0L);
        when(assessmentFileStorageService.storeSubmissionFile(eq(homework.getId()), eq(student.getId()), any(MockMultipartFile.class)))
                .thenThrow(new AssessmentValidationException("Định dạng tệp không được hỗ trợ."));

        AssessmentValidationException ex = assertThrows(AssessmentValidationException.class,
                () -> assignmentService.submit(homework.getId(), student, form, new HashMap<String, String>()));

        assertEquals("Định dạng tệp không được hỗ trợ.", ex.getMessage());
    }

    @Test
    void submitShouldRejectOversizedFile() {
        AssignmentSubmissionForm form = new AssignmentSubmissionForm();
        form.setAttachment(new MockMultipartFile("attachment", "big.pdf",
                "application/pdf", new byte[8]));

        when(assignmentRepository.findDetailedById(homework.getId())).thenReturn(Optional.of(homework));
        when(enrollmentService.isEnrolled(student.getId(), course.getId())).thenReturn(true);
        when(submissionRepository.countByAssignmentIdAndStudentId(homework.getId(), student.getId())).thenReturn(0L);
        when(assessmentFileStorageService.storeSubmissionFile(eq(homework.getId()), eq(student.getId()), any(MockMultipartFile.class)))
                .thenThrow(new AssessmentValidationException("Dung lượng tệp vượt quá giới hạn 5 MB."));

        AssessmentValidationException ex = assertThrows(AssessmentValidationException.class,
                () -> assignmentService.submit(homework.getId(), student, form, new HashMap<String, String>()));

        assertEquals("Dung lượng tệp vượt quá giới hạn 5 MB.", ex.getMessage());
    }

    @Test
    void submitShouldSurfaceStorageFailure() {
        AssignmentSubmissionForm form = new AssignmentSubmissionForm();
        form.setAttachment(new MockMultipartFile("attachment", "essay.pdf",
                "application/pdf", "du lieu".getBytes()));

        when(assignmentRepository.findDetailedById(homework.getId())).thenReturn(Optional.of(homework));
        when(enrollmentService.isEnrolled(student.getId(), course.getId())).thenReturn(true);
        when(submissionRepository.countByAssignmentIdAndStudentId(homework.getId(), student.getId())).thenReturn(0L);
        when(assessmentFileStorageService.storeSubmissionFile(eq(homework.getId()), eq(student.getId()), any(MockMultipartFile.class)))
                .thenThrow(new AssessmentStorageException("Không thể lưu tệp bài nộp lúc này.", new RuntimeException("network")));

        AssessmentStorageException ex = assertThrows(AssessmentStorageException.class,
                () -> assignmentService.submit(homework.getId(), student, form, new HashMap<String, String>()));

        assertEquals("Không thể lưu tệp bài nộp lúc này.", ex.getMessage());
    }

    @Test
    void lecturerShouldGradeSuccessfully() {
        Submission submission = Submission.builder()
                .id(90L)
                .assignment(homework)
                .student(student)
                .content("Noi dung bai tap")
                .status(Submission.SubmissionStatus.SUBMITTED)
                .attemptNumber(1)
                .build();

        SubmissionGradeForm form = new SubmissionGradeForm();
        form.setScore(8.5);
        form.setFeedback("Bai lam tot");

        when(submissionRepository.findDetailedById(submission.getId())).thenReturn(Optional.of(submission));
        when(submissionRepository.existsByAssignmentIdAndStudentIdAndScoreIsNotNull(homework.getId(), student.getId())).thenReturn(false);
        when(enrollmentService.isEnrolled(student.getId(), course.getId())).thenReturn(true);
        when(gamificationService.awardGradedAssignmentXp(student.getId(), course.getId(), 8.5, homework.getMaxScore(), homework.getType()))
                .thenReturn(43);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssignmentService.GradeResult result = assignmentService.grade(submission.getId(), form);

        assertEquals(8.5, result.getSubmission().getScore());
        assertEquals("Bai lam tot", result.getSubmission().getFeedback());
        assertEquals(Submission.SubmissionStatus.GRADED, result.getSubmission().getStatus());
        assertEquals(43, result.getAwardedXp());
        verify(notificationService).send(eq(student), eq("Đã có kết quả bài tập"), any(String.class), eq(Notification.NotifType.GRADE));
    }

    @Test
    void viewScoreWhenNotGradedShouldStayPending() {
        Submission submission = Submission.builder()
                .assignment(homework)
                .student(student)
                .status(Submission.SubmissionStatus.SUBMITTED)
                .attemptNumber(1)
                .content("Dang cho cham")
                .build();

        assertFalse(com.elearning.model.dto.assessment.AssessmentSubmissionDto.fromEntity(submission).isGraded());
        assertEquals(null, com.elearning.model.dto.assessment.AssessmentSubmissionDto.fromEntity(submission).getScore());
    }
}
