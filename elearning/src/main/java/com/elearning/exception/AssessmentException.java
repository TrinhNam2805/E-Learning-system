package com.elearning.exception;

public class AssessmentException extends RuntimeException {

    public AssessmentException(String message) {
        super(message);
    }

    public AssessmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
