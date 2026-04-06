package com.elearning.controller;

import com.elearning.exception.AssessmentAccessException;
import com.elearning.exception.AssessmentDeadlineException;
import com.elearning.exception.AssessmentException;
import com.elearning.exception.AssessmentNotFoundException;
import com.elearning.exception.AssessmentStorageException;
import com.elearning.model.dto.assessment.AssessmentApiErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.time.LocalDateTime;

@RestControllerAdvice(assignableTypes = AssessmentApiController.class)
public class AssessmentApiExceptionHandler {

    @ExceptionHandler(AssessmentNotFoundException.class)
    public ResponseEntity<AssessmentApiErrorDto> handleNotFound(AssessmentNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AssessmentAccessException.class)
    public ResponseEntity<AssessmentApiErrorDto> handleAccess(AssessmentAccessException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler({AssessmentDeadlineException.class, MultipartException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<AssessmentApiErrorDto> handleBadRequest(Exception ex) {
        String message = ex instanceof AssessmentDeadlineException
                ? ex.getMessage()
                : "The uploaded file exceeds the allowed size.";
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(AssessmentStorageException.class)
    public ResponseEntity<AssessmentApiErrorDto> handleStorage(AssessmentStorageException ex) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(AssessmentException.class)
    public ResponseEntity<AssessmentApiErrorDto> handleGenericAssessment(AssessmentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<AssessmentApiErrorDto> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(AssessmentApiErrorDto.builder()
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}