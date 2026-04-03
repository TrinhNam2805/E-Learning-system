package com.elearning.model.dto.assessment;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class SubmissionGradeForm {

    @NotNull(message = "Score is required.")
    @DecimalMin(value = "0.0", message = "Score must be greater than or equal to 0.")
    private Double score;

    @Size(max = 5000, message = "Feedback must not exceed 5000 characters.")
    private String feedback;
}
