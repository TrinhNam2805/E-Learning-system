package com.elearning.service;

import com.elearning.model.dto.assessment.AssessmentAssignmentDto;
import com.elearning.model.dto.assessment.AssessmentProgressDto;
import com.elearning.model.dto.assessment.AssessmentQuestionDto;
import com.elearning.model.dto.assessment.AssessmentQuizResponseDto;
import com.elearning.model.dto.assessment.AssessmentSubmissionDto;
import com.elearning.model.entity.Assignment;
import com.elearning.model.entity.Enrollment;
import com.elearning.model.entity.QuizQuestion;
import com.elearning.model.entity.Submission;
import com.elearning.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssessmentResultTrackingService {

    private final AssignmentService assignmentService;
    private final SubmissionRepository submissionRepository;
    private final EnrollmentService enrollmentService;

    public AssessmentAssignmentDto getAssignmentDetail(Long assignmentId, Long studentId) {
        Assignment assignment = assignmentService.getDetailedAssignmentOrThrow(assignmentId);
        List<Submission> submissionHistory = submissionRepository.findHistoryByAssignmentIdAndStudentId(assignmentId, studentId);
        List<QuizQuestion> questions = assignmentService.findQuestions(assignmentId);

        AssessmentSubmissionDto latestSubmission = submissionHistory.isEmpty()
                ? null
                : AssessmentSubmissionDto.fromEntity(submissionHistory.get(0));

        List<AssessmentQuestionDto> questionDtos = new ArrayList<AssessmentQuestionDto>();
        for (QuizQuestion question : questions) {
            questionDtos.add(AssessmentQuestionDto.fromEntity(question));
        }
        List<AssessmentQuizResponseDto> latestQuizResponses = buildLatestQuizResponses(
                submissionHistory.isEmpty() ? null : submissionHistory.get(0),
                questions);

        return AssessmentAssignmentDto.fromEntity(
                assignment,
                latestSubmission,
                questionDtos,
                latestQuizResponses,
                submissionHistory.size(),
                assignmentService.isPastDue(assignment));
    }

    public List<AssessmentSubmissionDto> getAssignmentHistory(Long assignmentId, Long studentId) {
        List<Submission> submissions = submissionRepository.findHistoryByAssignmentIdAndStudentId(assignmentId, studentId);
        return mapSubmissions(submissions);
    }

    public List<AssessmentSubmissionDto> getStudentSubmissionHistory(Long studentId) {
        return mapSubmissions(submissionRepository.findDetailedByStudentId(studentId));
    }

    public List<AssessmentProgressDto> getStudentCourseProgress(Long studentId) {
        List<Enrollment> enrollments = enrollmentService.findByStudentId(studentId);
        List<AssessmentProgressDto> progress = new ArrayList<AssessmentProgressDto>();
        for (Enrollment enrollment : enrollments) {
            progress.add(getCourseProgress(enrollment.getCourse().getId(), studentId));
        }
        return progress;
    }

    public AssessmentProgressDto getCourseProgress(Long courseId, Long studentId) {
        List<Assignment> assignments = assignmentService.findByCourseId(courseId);
        List<Submission> submissions = submissionRepository.findDetailedByStudentIdAndCourseId(studentId, courseId);
        Map<Long, Submission> latestByAssignment = latestByAssignment(submissions);

        int gradedAssignments = 0;
        int pendingGradeAssignments = 0;
        int lateSubmissions = 0;
        int overdueAssignments = 0;
        double totalScore = 0;

        for (Assignment assignment : assignments) {
            Submission latest = latestByAssignment.get(assignment.getId());
            if (latest == null) {
                if (assignmentService.isPastDue(assignment)) {
                    overdueAssignments++;
                }
                continue;
            }

            if (latest.isLateSubmission() || latest.getStatus() == Submission.SubmissionStatus.LATE) {
                lateSubmissions++;
            }

            if (latest.getScore() != null) {
                gradedAssignments++;
                totalScore += latest.getScore();
            } else {
                pendingGradeAssignments++;
            }
        }

        int totalAssignments = assignments.size();
        int submittedAssignments = latestByAssignment.size();
        int completionPercentage = totalAssignments == 0
                ? 0
                : (int) Math.round((submittedAssignments * 100.0) / totalAssignments);
        Double averageScore = gradedAssignments == 0
                ? null
                : Math.round((totalScore / gradedAssignments) * 100.0) / 100.0;

        Enrollment enrollment = enrollmentService.findByStudentAndCourse(studentId, courseId).orElse(null);
        Assignment firstAssignment = assignments.isEmpty() ? null : assignments.get(0);

        return AssessmentProgressDto.builder()
                .courseId(courseId)
                .courseCode(firstAssignment != null && firstAssignment.getCourse() != null
                        ? firstAssignment.getCourse().getCourseCode()
                        : (enrollment != null && enrollment.getCourse() != null ? enrollment.getCourse().getCourseCode() : null))
                .courseName(firstAssignment != null && firstAssignment.getCourse() != null
                        ? firstAssignment.getCourse().getCourseName()
                        : (enrollment != null && enrollment.getCourse() != null ? enrollment.getCourse().getCourseName() : null))
                .totalAssignments(totalAssignments)
                .submittedAssignments(submittedAssignments)
                .gradedAssignments(gradedAssignments)
                .pendingGradeAssignments(pendingGradeAssignments)
                .overdueAssignments(overdueAssignments)
                .lateSubmissions(lateSubmissions)
                .completionPercentage(completionPercentage)
                .averageScore(averageScore)
                .activityXp(enrollment != null ? enrollment.getActivityXp() : 0)
                .totalXp(enrollment != null ? enrollment.getTotalXp() : 0)
                .build();
    }

    private List<AssessmentSubmissionDto> mapSubmissions(List<Submission> submissions) {
        List<AssessmentSubmissionDto> items = new ArrayList<AssessmentSubmissionDto>();
        for (Submission submission : submissions) {
            items.add(AssessmentSubmissionDto.fromEntity(submission));
        }
        return items;
    }

    private Map<Long, Submission> latestByAssignment(List<Submission> submissions) {
        Map<Long, Submission> result = new LinkedHashMap<Long, Submission>();
        for (Submission submission : submissions) {
            Long assignmentId = submission.getAssignment().getId();
            if (!result.containsKey(assignmentId)) {
                result.put(assignmentId, submission);
            }
        }
        return result;
    }

    private List<AssessmentQuizResponseDto> buildLatestQuizResponses(Submission latestSubmission,
                                                                     List<QuizQuestion> questions) {
        List<AssessmentQuizResponseDto> items = new ArrayList<AssessmentQuizResponseDto>();
        if (latestSubmission == null || questions == null || questions.isEmpty()) {
            return items;
        }
        if (latestSubmission.getAssignment() == null
                || latestSubmission.getAssignment().getType() != Assignment.AssignmentType.QUIZ) {
            return items;
        }

        Map<Long, String> answersByQuestionId = parseQuizAnswers(latestSubmission.getContent());
        for (QuizQuestion question : questions) {
            String selectedOption = normalizeOption(answersByQuestionId.get(question.getId()));
            String correctOption = normalizeOption(question.getCorrectAnswer());
            items.add(AssessmentQuizResponseDto.builder()
                    .questionOrder(question.getQuestionOrder())
                    .questionText(question.getQuestionText())
                    .selectedOption(selectedOption)
                    .selectedAnswerText(resolveOptionText(question, selectedOption))
                    .correctOption(correctOption)
                    .correctAnswerText(resolveOptionText(question, correctOption))
                    .correct(selectedOption != null && selectedOption.equals(correctOption))
                    .build());
        }
        return items;
    }

    private Map<Long, String> parseQuizAnswers(String content) {
        Map<Long, String> answers = new LinkedHashMap<Long, String>();
        if (content == null || content.trim().isEmpty()) {
            return answers;
        }

        String[] entries = content.split(";");
        for (String entry : entries) {
            if (entry == null || entry.trim().isEmpty() || !entry.startsWith("Q")) {
                continue;
            }
            int colonIndex = entry.indexOf(':');
            if (colonIndex <= 1 || colonIndex >= entry.length() - 1) {
                continue;
            }
            try {
                Long questionId = Long.valueOf(entry.substring(1, colonIndex));
                String selectedOption = normalizeOption(entry.substring(colonIndex + 1));
                if (selectedOption != null) {
                    answers.put(questionId, selectedOption);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return answers;
    }

    private String normalizeOption(String option) {
        if (option == null) {
            return null;
        }
        String normalized = option.trim().toUpperCase();
        return normalized.isEmpty() ? null : normalized;
    }

    private String resolveOptionText(QuizQuestion question, String option) {
        if (question == null || option == null) {
            return null;
        }
        switch (option) {
            case "A":
                return question.getOptionA();
            case "B":
                return question.getOptionB();
            case "C":
                return question.getOptionC();
            case "D":
                return question.getOptionD();
            default:
                return null;
        }
    }
}
