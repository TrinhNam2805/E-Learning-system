package com.elearning.model.dto.assessment;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Size;

@Getter
@Setter
public class AssignmentSubmissionForm {

    @Size(max = 10000, message = "Submission content must not exceed 10000 characters.")
    private String content;

    private MultipartFile attachment;

    private boolean confirmLate;
}