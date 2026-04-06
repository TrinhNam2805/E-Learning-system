package com.elearning.model.dto.assessment;

import com.elearning.model.entity.QuizQuestion;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssessmentQuestionDto {

    private final Long id;
    private final String questionText;
    private final String optionA;
    private final String optionB;
    private final String optionC;
    private final String optionD;
    private final int questionOrder;
    private final double points;

    public static AssessmentQuestionDto fromEntity(QuizQuestion question) {
        return AssessmentQuestionDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .optionA(question.getOptionA())
                .optionB(question.getOptionB())
                .optionC(question.getOptionC())
                .optionD(question.getOptionD())
                .questionOrder(question.getQuestionOrder())
                .points(question.getPoints())
                .build();
    }
}
