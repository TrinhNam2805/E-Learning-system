package com.elearning.model.dto.assessment;

import com.elearning.model.entity.Assignment;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
public class AssignmentCreateForm {

    @NotBlank(message = "Assignment title must not be blank.")
    @Size(max = 200, message = "Assignment title must not exceed 200 characters.")
    private String title;

    @Size(max = 10000, message = "Assignment description must not exceed 10000 characters.")
    private String description;

    @NotNull(message = "Assignment type is required.")
    private Assignment.AssignmentType type = Assignment.AssignmentType.HOMEWORK;

    @NotNull(message = "Due date is required.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dueDate;

    @NotNull(message = "Maximum score is required.")
    @DecimalMin(value = "1.0", message = "Maximum score must be greater than or equal to 1.")
    private Double maxScore = 10.0;

    private Long lessonId;

    @DecimalMin(value = "0.0", message = "Minimum passing score cannot be less than 0.")
    private Double minimumPassingScore;

    private boolean allowLateSubmission;

    @NotNull(message = "Maximum attempts is required.")
    @Min(value = 1, message = "Maximum attempts must be at least 1.")
    @Max(value = 10, message = "Maximum attempts cannot exceed 10.")
    private Integer maxAttempts = 1;
}
