package com.elearning.service;

import com.elearning.exception.AssessmentDeadlineException;
import com.elearning.exception.AssessmentNotFoundException;
import com.elearning.exception.AssessmentValidationException;
import com.elearning.model.dto.assessment.AssignmentSubmissionForm;
import com.elearning.model.entity.Assignment;
import com.elearning.model.entity.Notification;
import com.elearning.model.entity.QuizQuestion;
import com.elearning.model.entity.Submission;
import com.elearning.model.entity.User;
import com.elearning.repository.AssignmentRepository;
import com.elearning.repository.QuizQuestionRepository;
import com.elearning.repository.SubmissionRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

    private static final Set<String> QUIZ_OPTIONS =
            new HashSet<String>(Arrays.asList("A", "B", "C", "D"));

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final GamificationService gamificationService;
    private final EnrollmentService enrollmentService;
    private final NotificationService notificationService;
    private final AssessmentFileStorageService assessmentFileStorageService;

    public List<Assignment> findByCourseId(Long courseId) {
        return assignmentRepository.findByCourseIdOrderByDueDateAsc(courseId);
    }

    public List<Assignment> findVisibleForStudent(Long courseId, Long studentId) {
        List<Assignment> assignments = findByCourseId(courseId);
        assignments.removeIf(assignment -> !isVisibleToStudent(assignment, studentId));
        return assignments;
    }

    public Optional<Assignment> findById(Long id) {
        return assignmentRepository.findDetailedById(id);
    }

    public Assignment getDetailedAssignmentOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new AssessmentNotFoundException("Assignment not found."));
    }

    public Submission getDetailedSubmissionOrThrow(Long submissionId) {
        return submissionRepository.findDetailedById(submissionId)
                .orElseThrow(() -> new AssessmentNotFoundException("Submission not found."));
    }

    public List<Assignment> findUpcomingForStudent(Long studentId) {
        return assignmentRepository.findUpcomingForStudent(studentId, LocalDateTime.now());
    }

    @Transactional
    public Assignment save(Assignment assignment) {
        if (assignment.getType() == Assignment.AssignmentType.HOMEWORK) {
            assignment.setMaxAttempts(1);
        } else if (assignment.getMaxAttempts() <= 0) {
            assignment.setMaxAttempts(1);
        }
        if (assignment.getMaxScore() <= 0) {
            assignment.setMaxScore(10.0);
        }
        if (assignment.getLesson() == null) {
            assignment.setMinimumPassingScore(null);
        } else if (assignment.getMinimumPassingScore() != null) {
            if (assignment.getMinimumPassingScore() < 0) {
                throw new AssessmentValidationException("The minimum passing score cannot be less than 0.");
            }
            if (assignment.getMinimumPassingScore() > assignment.getMaxScore()) {
                throw new AssessmentValidationException("The minimum passing score cannot be greater than the maximum score.");
            }
        }
        return assignmentRepository.save(assignment);
    }

    @Transactional
    public void delete(Long id) {
        assignmentRepository.deleteById(id);
    }

    public Optional<Submission> findLatestSubmission(Long assignmentId, Long studentId) {
        return submissionRepository.findTopByAssignmentIdAndStudentIdOrderByAttemptNumberDescSubmittedAtDesc(
                assignmentId, studentId);
    }

    public List<Assignment> findLessonWorkflowAssignments(Long courseId, Long lessonId) {
        List<Assignment> assignments = assignmentRepository.findByCourseIdAndLessonIdOrderByDueDateAsc(courseId, lessonId);
        assignments.sort((left, right) -> {
            int typeCompare = Integer.compare(workflowPriority(left), workflowPriority(right));
            if (typeCompare != 0) {
                return typeCompare;
            }
            if (left.getDueDate() == null && right.getDueDate() == null) {
                return left.getId().compareTo(right.getId());
            }
            if (left.getDueDate() == null) {
                return 1;
            }
            if (right.getDueDate() == null) {
                return -1;
            }
            int dueDateCompare = left.getDueDate().compareTo(right.getDueDate());
            return dueDateCompare != 0 ? dueDateCompare : left.getId().compareTo(right.getId());
        });
        return assignments;
    }

    public Optional<Assignment> findFirstLessonWorkflowAssignment(Long courseId, Long lessonId) {
        List<Assignment> workflowAssignments = findLessonWorkflowAssignments(courseId, lessonId);
        return workflowAssignments.isEmpty() ? Optional.empty() : Optional.of(workflowAssignments.get(0));
    }

    public Optional<Assignment> findNextLessonWorkflowAssignment(Assignment currentAssignment) {
        if (currentAssignment == null || currentAssignment.getCourse() == null || currentAssignment.getLesson() == null) {
            return Optional.empty();
        }

        List<Assignment> workflowAssignments = findLessonWorkflowAssignments(
                currentAssignment.getCourse().getId(),
                currentAssignment.getLesson().getId());

        for (int i = 0; i < workflowAssignments.size(); i++) {
            if (workflowAssignments.get(i).getId().equals(currentAssignment.getId())) {
                if (i + 1 < workflowAssignments.size()) {
                    return Optional.of(workflowAssignments.get(i + 1));
                }
                break;
            }
        }

        return Optional.empty();
    }

    public boolean isVisibleToStudent(Assignment assignment, Long studentId) {
        if (assignment == null || studentId == null || assignment.getCourse() == null) {
            return false;
        }
        if (!enrollmentService.isEnrolled(studentId, assignment.getCourse().getId())) {
            return false;
        }
        if (assignment.getLesson() == null) {
            return true;
        }
        return enrollmentService.isLessonCompleted(studentId, assignment.getLesson().getId());
    }

    public List<Submission> findSubmissionHistory(Long assignmentId, Long studentId) {
        return submissionRepository.findHistoryByAssignmentIdAndStudentId(assignmentId, studentId);
    }

    public List<Submission> findSubmissionsByAssignment(Long assignmentId) {
        return submissionRepository.findDetailedByAssignmentId(assignmentId);
    }

    public List<Submission> findSubmissionsByStudent(Long studentId) {
        return submissionRepository.findDetailedByStudentId(studentId);
    }

    @Transactional
    public SubmissionResult submit(Long assignmentId,
                                   User student,
                                   AssignmentSubmissionForm form,
                                   Map<String, String> allParams) {
        Assignment assignment = getDetailedAssignmentOrThrow(assignmentId);
        validateStudentMaySubmit(assignment, student);

        List<Submission> submissionHistory = submissionRepository.findHistoryByAssignmentIdAndStudentId(assignmentId, student.getId());
        long existingAttempts = submissionRepository.countByAssignmentIdAndStudentId(assignmentId, student.getId());
        boolean editableSingleSubmission = isEditableSingleSubmissionAssignment(assignment);
        if (!hasUnlimitedAttempts(assignment)
                && !editableSingleSubmission
                && existingAttempts >= Math.max(assignment.getMaxAttempts(), 1)) {
            throw new AssessmentValidationException("You have used all available submissions for this assignment.");
        }

        boolean pastDue = isPastDue(assignment);
        validateDeadline(assignment, pastDue, form.isConfirmLate());

        if (editableSingleSubmission && !submissionHistory.isEmpty()) {
            return updateExistingSubmission(assignment, submissionHistory.get(0), form, pastDue);
        }

        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .attemptNumber((int) existingAttempts + 1)
                .lateSubmission(pastDue)
                .status(Submission.SubmissionStatus.SUBMITTED)
                .content(normalize(form.getContent()))
                .build();

        if (assignment.getType() == Assignment.AssignmentType.QUIZ) {
            return submitQuiz(assignment, student, submission, form.getAttachment(), allParams);
        }

        attachFile(submission, form.getAttachment(), assignmentId, student.getId());
        validateOpenSubmission(submission);

        Submission saved = submissionRepository.save(submission);
        return new SubmissionResult(saved, 0, false);
    }

    public boolean isPastDue(Assignment assignment) {
        LocalDateTime dueDate = assignment.getDueDate();
        return dueDate != null && LocalDateTime.now().isAfter(dueDate);
    }

    public List<QuizQuestion> findQuestions(Long assignmentId) {
        return quizQuestionRepository.findByAssignmentIdOrderByQuestionOrderAsc(assignmentId);
    }

    @Transactional
    public QuizQuestion saveQuestion(QuizQuestion question) {
        return quizQuestionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        quizQuestionRepository.deleteById(id);
    }

    private SubmissionResult submitQuiz(Assignment assignment,
                                        User student,
                                        Submission submission,
                                        MultipartFile attachment,
                                        Map<String, String> allParams) {
        if (attachment != null && !attachment.isEmpty()) {
            throw new AssessmentValidationException("Quiz submissions do not support file attachments.");
        }

        List<QuizQuestion> questions = findQuestions(assignment.getId());
        if (questions.isEmpty()) {
            throw new AssessmentValidationException("This quiz does not have any questions yet.");
        }

        StringBuilder answerBuilder = new StringBuilder();
        int correctAnswers = 0;
        for (QuizQuestion question : questions) {
            String answer = normalizeQuizAnswer(allParams.get("q_" + question.getId()));
            if (!QUIZ_OPTIONS.contains(answer)) {
                throw new AssessmentValidationException("Please answer every question before submitting.");
            }
            answerBuilder.append("Q").append(question.getId()).append(":").append(answer).append(";");
            if (answer.equalsIgnoreCase(question.getCorrectAnswer())) {
                correctAnswers++;
            }
        }

        double score = (correctAnswers * assignment.getMaxScore()) / questions.size();

        submission.setContent(answerBuilder.toString());
        submission.setScore(score);
        submission.setAutoGraded(true);
        submission.setStatus(Submission.SubmissionStatus.GRADED);
        submission.setFeedback("Auto-graded by the system: " + correctAnswers + "/" + questions.size() + " correct answers.");
        submission.setGradedAt(LocalDateTime.now());

        Submission saved = submissionRepository.save(submission);
        int awardedXp = 0;
        if (submission.getAttemptNumber() == 1
                && enrollmentService.isEnrolled(student.getId(), assignment.getCourse().getId())) {
            awardedXp = gamificationService.awardQuizXp(
                    student.getId(), assignment.getCourse().getId(), score, assignment.getMaxScore());
        }

        StringBuilder message = new StringBuilder();
        message.append("Quiz \"").append(assignment.getTitle()).append("\" was auto-graded: ")
                .append(String.format(Locale.US, "%.1f", score))
                .append("/").append(String.format(Locale.US, "%.1f", assignment.getMaxScore())).append(".");
        if (submission.isLateSubmission()) {
            message.append(" The submission was recorded as late.");
        }
        if (awardedXp > 0) {
            message.append(" +").append(awardedXp).append(" XP.");
        }
        notificationService.send(student,
                "Quiz result",
                message.toString(),
                Notification.NotifType.GRADE);

        return new SubmissionResult(saved, awardedXp, false);
    }

    private boolean hasUnlimitedAttempts(Assignment assignment) {
        return assignment != null && assignment.getType() == Assignment.AssignmentType.QUIZ;
    }

    private boolean isEditableSingleSubmissionAssignment(Assignment assignment) {
        return assignment != null && assignment.getType() == Assignment.AssignmentType.HOMEWORK;
    }

    private int workflowPriority(Assignment assignment) {
        if (assignment == null || assignment.getType() == null) {
            return 99;
        }
        switch (assignment.getType()) {
            case QUIZ:
                return 0;
            case HOMEWORK:
                return 1;
            case EXAM:
                return 2;
            default:
                return 99;
        }
    }

    private void validateStudentMaySubmit(Assignment assignment, User student) {
        if (student == null || student.getRole() != User.Role.STUDENT) {
            throw new AssessmentValidationException("Only students can submit assignments.");
        }
        if (!enrollmentService.isEnrolled(student.getId(), assignment.getCourse().getId())) {
            throw new AssessmentValidationException("You are not enrolled in this course.");
        }
        if (!isVisibleToStudent(assignment, student.getId())) {
            throw new AssessmentValidationException("Complete the lesson first to unlock this quiz or homework.");
        }
    }

    private void validateDeadline(Assignment assignment, boolean pastDue, boolean confirmedLate) {
        if (!pastDue) {
            return;
        }
        if (!assignment.isAllowLateSubmission()) {
            throw new AssessmentDeadlineException("This assignment is past due and late submission is not allowed.");
        }
        if (!confirmedLate) {
            throw new AssessmentDeadlineException("This assignment is past due. Please confirm late submission to continue.");
        }
    }

    private void attachFile(Submission submission, MultipartFile attachment, Long assignmentId, Long studentId) {
        AssessmentFileStorageService.StoredFile storedFile = assessmentFileStorageService
                .storeSubmissionFile(assignmentId, studentId, attachment);
        if (storedFile == null) {
            return;
        }
        submission.setFileUrl(storedFile.getStoredFileName());
        submission.setOriginalFileName(storedFile.getOriginalFileName());
        submission.setFileSize(storedFile.getSize());
    }

    private SubmissionResult updateExistingSubmission(Assignment assignment,
                                                      Submission existingSubmission,
                                                      AssignmentSubmissionForm form,
                                                      boolean pastDue) {
        if (pastDue) {
            throw new AssessmentValidationException("This homework can no longer be edited because the submission window has closed.");
        }

        existingSubmission.setContent(normalize(form.getContent()));
        existingSubmission.setLateSubmission(false);
        existingSubmission.setStatus(Submission.SubmissionStatus.SUBMITTED);
        existingSubmission.setScore(null);
        existingSubmission.setFeedback(null);
        existingSubmission.setAutoGraded(false);
        existingSubmission.setGradedAt(null);
        existingSubmission.setSubmittedAt(LocalDateTime.now());
        attachFile(existingSubmission, form.getAttachment(), assignment.getId(), existingSubmission.getStudent().getId());
        validateOpenSubmission(existingSubmission);

        Submission saved = submissionRepository.save(existingSubmission);
        return new SubmissionResult(saved, 0, true);
    }

    private void validateOpenSubmission(Submission submission) {
        boolean hasContent = StringUtils.hasText(submission.getContent());
        boolean hasFile = StringUtils.hasText(submission.getFileUrl());
        if (!hasContent && !hasFile) {
            throw new AssessmentValidationException("Please enter submission content or upload a valid file.");
        }
    }
    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeQuizAnswer(String answer) {
        return answer == null ? null : answer.trim().toUpperCase(Locale.ROOT);
    }

    @Getter
    public static class SubmissionResult {
        private final Submission submission;
        private final int awardedXp;
        private final boolean updatedExisting;

        public SubmissionResult(Submission submission, int awardedXp) {
            this(submission, awardedXp, false);
        }

        public SubmissionResult(Submission submission, int awardedXp, boolean updatedExisting) {
            this.submission = submission;
            this.awardedXp = awardedXp;
            this.updatedExisting = updatedExisting;
        }
    }
}
