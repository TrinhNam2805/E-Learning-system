package com.elearning.model.dto.assessment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AssessmentApiErrorDto {

    private final String message;
    private final LocalDateTime timestamp;
}
