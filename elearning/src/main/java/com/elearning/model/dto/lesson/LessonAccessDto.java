package com.elearning.model.dto.lesson;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LessonAccessDto {

    private final Long lessonId;
    private final boolean accessible;
    private final boolean completed;
    private final String lockedReason;
    private final String prerequisiteLessonTitle;
    private final String blockingAssignmentTitle;
    private final Double requiredScore;
}
