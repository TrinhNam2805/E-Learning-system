package com.elearning.model.dto.assessment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssessmentProgressDto {

    private final Long courseId;
    private final String courseCode;
    private final String courseName;
    private final int totalAssignments;
    private final int submittedAssignments;
    private final int gradedAssignments;
    private final int pendingGradeAssignments;
    private final int overdueAssignments;
    private final int lateSubmissions;
    private final int completionPercentage;
    private final Double averageScore;
    private final int activityXp;
    private final int totalXp;
}
