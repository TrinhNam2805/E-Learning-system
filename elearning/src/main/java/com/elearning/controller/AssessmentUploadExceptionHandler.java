package com.elearning.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@ControllerAdvice(assignableTypes = AssignmentController.class)
@Controller
public class AssessmentUploadExceptionHandler {

    @ExceptionHandler({MultipartException.class, MaxUploadSizeExceededException.class})
    public String handleUploadError(HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request)
                .put("error", "The uploaded file exceeds the allowed size or the submitted data is invalid.");
        return "redirect:" + resolveRedirectPath(request);
    }

    private String resolveRedirectPath(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.trim().isEmpty()) {
            return "/assignments/results";
        }
        try {
            URI uri = URI.create(referer);
            StringBuilder path = new StringBuilder(uri.getPath());
            if (uri.getQuery() != null && !uri.getQuery().trim().isEmpty()) {
                path.append("?").append(uri.getQuery());
            }
            return path.toString();
        } catch (IllegalArgumentException ex) {
            return "/assignments/results";
        }
    }
}