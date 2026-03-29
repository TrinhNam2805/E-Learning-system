package com.elearning.service;

import com.elearning.model.entity.*;
import com.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final GamificationService gamificationService;
    private final EnrollmentService enrollmentService;

    public List<Assignment> findByCourseId(Long courseId) {
        return assignmentRepository.findByCourseIdOrderByDueDateAsc(courseId);
    }

    public Optional<Assignment> findById(Long id) {
        return assignmentRepository.findById(id);
    }

    public List<Assignment> findUpcomingForStudent(Long studentId) {
        return assignmentRepository.findUpcomingForStudent(studentId, LocalDateTime.now());
    }

    @Transactional
    public Assignment save(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    @Transactional
    public void delete(Long id) {
        assignmentRepository.deleteById(id);
    }

    // Submissions
    public Optional<Submission> findSubmission(Long assignmentId, Long studentId) {
        return submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
    }

    public List<Submission> findSubmissionsByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    @Transactional
    public Submission submit(Submission submission) {
        return submissionRepository.save(submission);
    }

    @Transactional
    public Submission grade(Long submissionId, double score, String feedback) {
        Submission sub = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found."));
        boolean firstGrade = sub.getScore() == null;
        Assignment a = sub.getAssignment();
        sub.setScore(score);
        sub.setFeedback(feedback);
        sub.setStatus(Submission.SubmissionStatus.GRADED);
        Submission saved = submissionRepository.save(sub);

        if (firstGrade
                && enrollmentService.isEnrolled(sub.getStudent().getId(), a.getCourse().getId())
                && a.getType() != Assignment.AssignmentType.QUIZ) {
            gamificationService.awardGradedAssignmentXp(
                    sub.getStudent().getId(),
                    a.getCourse().getId(),
                    score,
                    a.getMaxScore(),
                    a.getType());
        }
        return saved;
    }

    // Quiz questions
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
}
