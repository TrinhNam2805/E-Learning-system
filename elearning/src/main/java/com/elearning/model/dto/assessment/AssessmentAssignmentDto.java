package com.elearning.model.dto.assessment;

import com.elearning.model.entity.Assignment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AssessmentAssignmentDto {

    private final Long id;
    private final Long courseId;
    private final String courseCode;
    private final String courseName;
    private final String title;
    private final String description;
    private final String type;
    private final LocalDateTime dueDate;
    private final double maxScore;
    private final boolean allowLateSubmission;
    private final int maxAttempts;
    private final int submissionCount;
    private final int remainingAttempts;
    private final boolean pastDue;
    private final boolean canSubmit;
    private final AssessmentSubmissionDto latestSubmission;
    private final List<AssessmentQuestionDto> questions;

    public static AssessmentAssignmentDto fromEntity(Assignment assignment,
                                                     AssessmentSubmissionDto latestSubmission,
                                                     List<AssessmentQuestionDto> questions,
                                                     int submissionCount,
                                                     boolean pastDue) {
        int maxAttempts = Math.max(assignment.getMaxAttempts(), 1);
        int remainingAttempts = Math.max(maxAttempts - submissionCount, 0);
        boolean canSubmit = remainingAttempts > 0 && (!pastDue || assignment.isAllowLateSubmission());

        return AssessmentAssignmentDto.builder()
                .id(assignment.getId())
                .courseId(assignment.getCourse() != null ? assignment.getCourse().getId() : null)
                .courseCode(assignment.getCourse() != null ? assignment.getCourse().getCourseCode() : null)
                .courseName(assignment.getCourse() != null ? assignment.getCourse().getCourseName() : null)
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .type(assignment.getType() != null ? assignment.getType().name() : null)
                .dueDate(assignment.getDueDate())
                .maxScore(assignment.getMaxScore())
                .allowLateSubmission(assignment.isAllowLateSubmission())
                .maxAttempts(maxAttempts)
                .submissionCount(submissionCount)
                .remainingAttempts(remainingAttempts)
                .pastDue(pastDue)
                .canSubmit(canSubmit)
                .latestSubmission(latestSubmission)
                .questions(questions)
                .build();
    }
}
