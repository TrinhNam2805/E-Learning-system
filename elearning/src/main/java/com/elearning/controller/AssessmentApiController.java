package com.elearning.controller;

import com.elearning.exception.AssessmentAccessException;
import com.elearning.exception.AssessmentValidationException;
import com.elearning.model.dto.assessment.AssessmentAssignmentDto;
import com.elearning.model.dto.assessment.AssessmentProgressDto;
import com.elearning.model.dto.assessment.AssessmentSubmissionDto;
import com.elearning.model.dto.assessment.AssignmentSubmissionForm;
import com.elearning.model.dto.assessment.SubmissionGradeForm;
import com.elearning.model.entity.Assignment;
import com.elearning.model.entity.User;
import com.elearning.repository.UserRepository;
import com.elearning.service.AssessmentResultTrackingService;
import com.elearning.service.AssignmentService;
import com.elearning.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class AssessmentApiController {

    private final AssignmentService assignmentService;
    private final AssessmentResultTrackingService assessmentResultTrackingService;
    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;

    @GetMapping("/assignments/{id}")
    public AssessmentAssignmentDto getAssignment(@PathVariable Long id,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        User user = requireCurrentUser(userDetails);
        Assignment assignment = assignmentService.getDetailedAssignmentOrThrow(id);
        if (!canAccessAssignment(user, assignment)) {
            throw new AssessmentAccessException("You do not have access to this assignment.");
        }
        return assessmentResultTrackingService.getAssignmentDetail(id, user.getId());
    }

    @GetMapping("/assignments/{id}/submissions/me")
    public List<AssessmentSubmissionDto> getMySubmissionHistory(@PathVariable Long id,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        User user = requireCurrentUser(userDetails);
        Assignment assignment = assignmentService.getDetailedAssignmentOrThrow(id);
        if (!canAccessAssignment(user, assignment)) {
            throw new AssessmentAccessException("You do not have permission to view this assignment's submission history.");
        }
        return assessmentResultTrackingService.getAssignmentHistory(id, user.getId());
    }

    @PostMapping(value = "/assignments/{id}/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AssessmentSubmissionDto submit(@PathVariable Long id,
                                          @Valid @ModelAttribute AssignmentSubmissionForm form,
                                          BindingResult bindingResult,
                                          @RequestParam Map<String, String> allParams,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        User user = requireCurrentUser(userDetails);
        if (user.getRole() != User.Role.STUDENT) {
            throw new AssessmentAccessException("Only students can submit assignments.");
        }
        if (bindingResult.hasErrors()) {
            throw new AssessmentValidationException(bindingResult.getFieldError().getDefaultMessage());
        }
        return AssessmentSubmissionDto.fromEntity(assignmentService.submit(id, user, form, allParams).getSubmission());
    }

    @PostMapping("/submissions/{submissionId}/grade")
    public AssessmentSubmissionDto grade(@PathVariable Long submissionId,
                                         @Valid @ModelAttribute SubmissionGradeForm form,
                                         BindingResult bindingResult,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        User user = requireCurrentUser(userDetails);
        if (user.getRole() == User.Role.STUDENT) {
            throw new AssessmentAccessException("You do not have permission to grade submissions.");
        }
        Assignment assignment = assignmentService.getDetailedSubmissionOrThrow(submissionId).getAssignment();
        if (!canManageAssignment(user, assignment)) {
            throw new AssessmentAccessException("You do not have permission to grade assignments for this course.");
        }
        if (bindingResult.hasErrors()) {
            throw new AssessmentValidationException(bindingResult.getFieldError().getDefaultMessage());
        }
        return AssessmentSubmissionDto.fromEntity(assignmentService.grade(submissionId, form).getSubmission());
    }

    @GetMapping("/courses/{courseId}/progress")
    public AssessmentProgressDto getCourseProgress(@PathVariable Long courseId,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        User user = requireCurrentUser(userDetails);
        if (user.getRole() != User.Role.STUDENT) {
            throw new AssessmentAccessException("Only students have personal progress data.");
        }
        if (!enrollmentService.isEnrolled(user.getId(), courseId)) {
            throw new AssessmentAccessException("You are not enrolled in this course.");
        }
        return assessmentResultTrackingService.getCourseProgress(courseId, user.getId());
    }

    private User requireCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new AssessmentAccessException("Invalid login session.");
        }
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            throw new AssessmentAccessException("The signed-in user could not be found.");
        }
        return user;
    }

    private boolean canAccessAssignment(User user, Assignment assignment) {
        if (user.getRole() == User.Role.STUDENT) {
            return enrollmentService.isEnrolled(user.getId(), assignment.getCourse().getId());
        }
        return canManageAssignment(user, assignment);
    }

    private boolean canManageAssignment(User user, Assignment assignment) {
        if (user.getRole() == User.Role.ADMIN) {
            return true;
        }
        return user.getRole() == User.Role.TEACHER
                && assignment.getCourse() != null
                && assignment.getCourse().getTeacher() != null
                && user.getId().equals(assignment.getCourse().getTeacher().getId());
    }
}
