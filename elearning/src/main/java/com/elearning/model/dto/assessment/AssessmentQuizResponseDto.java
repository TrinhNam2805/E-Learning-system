package com.elearning.model.dto.assessment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssessmentQuizResponseDto {

    private final Integer questionOrder;
    private final String questionText;
    private final String selectedOption;
    private final String selectedAnswerText;
    private final String correctOption;
    private final String correctAnswerText;
    private final boolean correct;
}
