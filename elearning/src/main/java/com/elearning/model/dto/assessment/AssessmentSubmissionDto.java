package com.elearning.model.dto.assessment;

import com.elearning.model.entity.Submission;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AssessmentSubmissionDto {

    private final Long id;
    private final Long assignmentId;
    private final String assignmentTitle;
    private final Long courseId;
    private final String courseCode;
    private final Long lessonId;
    private final Integer lessonOrder;
    private final String lessonTitle;
    private final Long studentId;
    private final String studentName;
    private final String studentEmail;
    private final Integer attemptNumber;
    private final String content;
    private final String fileName;
    private final Long fileSize;
    private final Double score;
    private final String feedback;
    private final String status;
    private final boolean lateSubmission;
    private final boolean autoGraded;
    private final boolean graded;
    private final LocalDateTime submittedAt;
    private final LocalDateTime gradedAt;
    private final List<AssessmentQuizResponseDto> quizResponses;

    public static AssessmentSubmissionDto fromEntity(Submission submission) {
        return fromEntity(submission, null);
    }

    public static AssessmentSubmissionDto fromEntity(Submission submission,
                                                     List<AssessmentQuizResponseDto> quizResponses) {
        Submission.SubmissionStatus status = submission.getStatus();
        boolean graded = submission.getScore() != null
                || status == Submission.SubmissionStatus.GRADED
                || submission.isAutoGraded();

        return AssessmentSubmissionDto.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignment() != null ? submission.getAssignment().getId() : null)
                .assignmentTitle(submission.getAssignment() != null ? submission.getAssignment().getTitle() : null)
                .courseId(submission.getAssignment() != null && submission.getAssignment().getCourse() != null
                        ? submission.getAssignment().getCourse().getId() : null)
                .courseCode(submission.getAssignment() != null && submission.getAssignment().getCourse() != null
                        ? submission.getAssignment().getCourse().getCourseCode() : null)
                .lessonId(submission.getAssignment() != null && submission.getAssignment().getLesson() != null
                        ? submission.getAssignment().getLesson().getId() : null)
                .lessonOrder(submission.getAssignment() != null && submission.getAssignment().getLesson() != null
                        ? submission.getAssignment().getLesson().getLessonOrder() : null)
                .lessonTitle(submission.getAssignment() != null && submission.getAssignment().getLesson() != null
                        ? submission.getAssignment().getLesson().getLessonTitle() : null)
                .studentId(submission.getStudent() != null ? submission.getStudent().getId() : null)
                .studentName(submission.getStudent() != null ? submission.getStudent().getFullName() : null)
                .studentEmail(submission.getStudent() != null ? submission.getStudent().getEmail() : null)
                .attemptNumber(submission.getAttemptNumber())
                .content(submission.getContent())
                .fileName(submission.getOriginalFileName())
                .fileSize(submission.getFileSize())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .status(status != null ? status.name() : null)
                .lateSubmission(submission.isLateSubmission() || status == Submission.SubmissionStatus.LATE)
                .autoGraded(submission.isAutoGraded())
                .graded(graded)
                .submittedAt(submission.getSubmittedAt())
                .gradedAt(submission.getGradedAt())
                .quizResponses(quizResponses)
                .build();
    }
}
