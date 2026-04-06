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
    private final Long lessonId;
    private final Integer lessonOrder;
    private final String lessonTitle;
    private final String title;
    private final String description;
    private final String type;
    private final LocalDateTime dueDate;
    private final double maxScore;
    private final Double minimumPassingScore;
    private final boolean allowLateSubmission;
    private final int maxAttempts;
    private final boolean unlimitedAttempts;
    private final boolean editableSubmission;
    private final int submissionCount;
    private final int remainingAttempts;
    private final boolean pastDue;
    private final boolean canSubmit;
    private final AssessmentSubmissionDto latestSubmission;
    private final List<AssessmentQuestionDto> questions;
    private final List<AssessmentQuizResponseDto> latestQuizResponses;

    public static AssessmentAssignmentDto fromEntity(Assignment assignment,
                                                     AssessmentSubmissionDto latestSubmission,
                                                     List<AssessmentQuestionDto> questions,
                                                     List<AssessmentQuizResponseDto> latestQuizResponses,
                                                     int submissionCount,
                                                     boolean pastDue) {
        boolean editableSubmission = assignment.getType() == Assignment.AssignmentType.HOMEWORK
                && latestSubmission != null
                && !pastDue;
        boolean unlimitedAttempts = assignment.getType() == Assignment.AssignmentType.QUIZ;
        int maxAttempts = assignment.getType() == Assignment.AssignmentType.HOMEWORK
                ? 1
                : Math.max(assignment.getMaxAttempts(), 1);
        int remainingAttempts;
        if (unlimitedAttempts) {
            remainingAttempts = Integer.MAX_VALUE;
        } else if (assignment.getType() == Assignment.AssignmentType.HOMEWORK) {
            remainingAttempts = submissionCount > 0 ? 0 : 1;
        } else {
            remainingAttempts = Math.max(maxAttempts - submissionCount, 0);
        }
        boolean canSubmit = editableSubmission
                || ((unlimitedAttempts || remainingAttempts > 0) && (!pastDue || assignment.isAllowLateSubmission()));

        return AssessmentAssignmentDto.builder()
                .id(assignment.getId())
                .courseId(assignment.getCourse() != null ? assignment.getCourse().getId() : null)
                .courseCode(assignment.getCourse() != null ? assignment.getCourse().getCourseCode() : null)
                .courseName(assignment.getCourse() != null ? assignment.getCourse().getCourseName() : null)
                .lessonId(assignment.getLesson() != null ? assignment.getLesson().getId() : null)
                .lessonOrder(assignment.getLesson() != null ? assignment.getLesson().getLessonOrder() : null)
                .lessonTitle(assignment.getLesson() != null ? assignment.getLesson().getLessonTitle() : null)
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .type(assignment.getType() != null ? assignment.getType().name() : null)
                .dueDate(assignment.getDueDate())
                .maxScore(assignment.getMaxScore())
                .minimumPassingScore(assignment.getMinimumPassingScore())
                .allowLateSubmission(assignment.isAllowLateSubmission())
                .maxAttempts(maxAttempts)
                .unlimitedAttempts(unlimitedAttempts)
                .editableSubmission(editableSubmission)
                .submissionCount(submissionCount)
                .remainingAttempts(remainingAttempts)
                .pastDue(pastDue)
                .canSubmit(canSubmit)
                .latestSubmission(latestSubmission)
                .questions(questions)
                .latestQuizResponses(latestQuizResponses)
                .build();
    }
}
