package com.elearning.controller;

import com.elearning.exception.AssessmentException;
import com.elearning.model.dto.assessment.AssessmentAssignmentDto;
import com.elearning.model.dto.assessment.AssessmentProgressDto;
import com.elearning.model.dto.assessment.AssessmentSubmissionDto;
import com.elearning.model.dto.assessment.AssignmentSubmissionForm;
import com.elearning.model.dto.assessment.SubmissionGradeForm;
import com.elearning.model.entity.Assignment;
import com.elearning.model.entity.Submission;
import com.elearning.model.entity.User;
import com.elearning.repository.UserRepository;
import com.elearning.service.AssessmentFileStorageService;
import com.elearning.service.AssessmentResultTrackingService;
import com.elearning.service.AssignmentService;
import com.elearning.service.EnrollmentService;
import com.elearning.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssessmentResultTrackingService assessmentResultTrackingService;
    private final AssessmentFileStorageService assessmentFileStorageService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EnrollmentService enrollmentService;

    @GetMapping("/{id}")
    public String view(@PathVariable Long id,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model) {
        User user = getCurrentUser(userDetails);
        if (user == null) {
            return "redirect:/login";
        }

        Assignment assignment = assignmentService.findById(id).orElse(null);
        if (assignment == null) {
            return "redirect:/courses";
        }
        if (!canAccessAssignment(user, assignment)) {
            return user.getRole() == User.Role.STUDENT
                    ? "redirect:/courses/" + assignment.getCourse().getId()
                    : "redirect:/dashboard";
        }

        AssessmentAssignmentDto assignmentView = assessmentResultTrackingService.getAssignmentDetail(id, user.getId());
        List<AssessmentSubmissionDto> submissionHistory = assessmentResultTrackingService.getAssignmentHistory(id, user.getId());
        AssessmentProgressDto progress = user.getRole() == User.Role.STUDENT
                ? assessmentResultTrackingService.getCourseProgress(assignment.getCourse().getId(), user.getId())
                : null;

        model.addAttribute("assignment", assignmentView);
        model.addAttribute("submissionHistory", submissionHistory);
        model.addAttribute("progress", progress);
        model.addAttribute("submissionForm", new AssignmentSubmissionForm());
        model.addAttribute("allowedExtensions", assessmentFileStorageService.getAllowedExtensions());
        model.addAttribute("maxFileSizeBytes", assessmentFileStorageService.getMaxFileSizeBytes());
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "assignment/detail";
    }

    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id,
                         @Valid @ModelAttribute("submissionForm") AssignmentSubmissionForm form,
                         BindingResult bindingResult,
                         @RequestParam Map<String, String> allParams,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        if (user == null) {
            return "redirect:/login";
        }

        Assignment assignment = assignmentService.findById(id).orElse(null);
        if (assignment == null) {
            return "redirect:/courses";
        }
        if (!canAccessAssignment(user, assignment) || user.getRole() != User.Role.STUDENT) {
            return "redirect:/access-denied";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/assignments/" + id;
        }

        try {
            AssignmentService.SubmissionResult result = assignmentService.submit(id, user, form, allParams);
            Submission submission = result.getSubmission();

            if (assignment.getType() == Assignment.AssignmentType.QUIZ) {
                StringBuilder success = new StringBuilder();
                success.append("Quiz submitted successfully. Current score: ")
                        .append(String.format(java.util.Locale.US, "%.1f", submission.getScore()))
                        .append("/").append(String.format(java.util.Locale.US, "%.1f", assignment.getMaxScore())).append(".");
                if (submission.isLateSubmission()) {
                    success.append(" The submission was recorded as late.");
                }
                if (result.getAwardedXp() > 0) {
                    success.append(" +").append(result.getAwardedXp()).append(" XP.");
                }
                redirectAttributes.addFlashAttribute("success", success.toString());
            } else {
                StringBuilder success = new StringBuilder("Assignment submitted successfully. Your work is awaiting instructor grading.");
                if (submission.isLateSubmission()) {
                    success.append(" The submission was recorded as late.");
                }
                redirectAttributes.addFlashAttribute("success", success.toString());
            }
        } catch (AssessmentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/assignments/" + id;
    }

    @GetMapping("/{id}/submissions")
    public String submissions(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        User user = getCurrentUser(userDetails);
        if (user == null) {
            return "redirect:/login";
        }
        if (user.getRole() == User.Role.STUDENT) {
            return "redirect:/dashboard";
        }

        Assignment assignment = assignmentService.findById(id).orElse(null);
        if (assignment == null) {
            return "redirect:/teacher/dashboard";
        }
        if (!canManageAssignment(user, assignment)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("assignment", assessmentResultTrackingService.getAssignmentDetail(id, user.getId()));
        model.addAttribute("submissions", assessmentResultTrackingService.getAssignmentSubmissionsForTeacher(id));
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "assignment/submission-list";
    }

    @PostMapping("/grade/{submissionId}")
    public String grade(@PathVariable Long submissionId,
                        @Valid @ModelAttribute("gradeForm") SubmissionGradeForm form,
                        BindingResult bindingResult,
                        @AuthenticationPrincipal UserDetails userDetails,
                        RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        if (user == null) {
            return "redirect:/login";
        }
        if (user.getRole() == User.Role.STUDENT) {
            return "redirect:/dashboard";
        }

        Submission submission;
        try {
            submission = assignmentService.getDetailedSubmissionOrThrow(submissionId);
        } catch (AssessmentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/dashboard";
        }
        if (!canManageAssignment(user, submission.getAssignment())) {
            return "redirect:/dashboard";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/assignments/" + submission.getAssignment().getId() + "/submissions";
        }

        try {
            AssignmentService.GradeResult result = assignmentService.grade(submissionId, form);
            StringBuilder success = new StringBuilder("Submission graded successfully.");
            if (result.getAwardedXp() > 0) {
                success.append(" Awarded ").append(result.getAwardedXp()).append(" XP to the student.");
            }
            redirectAttributes.addFlashAttribute("success", success.toString());
        } catch (AssessmentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/assignments/" + submission.getAssignment().getId() + "/submissions";
    }

    @GetMapping("/results")
    public String results(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestParam(required = false) Long courseId,
                          Model model) {
        User user = getCurrentUser(userDetails);
        if (user == null) {
            return "redirect:/login";
        }
        if (user.getRole() != User.Role.STUDENT) {
            return "redirect:/dashboard";
        }

        List<AssessmentProgressDto> progressCards = assessmentResultTrackingService.getStudentCourseProgress(user.getId());
        List<AssessmentSubmissionDto> history = assessmentResultTrackingService.getStudentSubmissionHistory(user.getId());
        List<AssessmentSubmissionDto> filteredHistory = new ArrayList<AssessmentSubmissionDto>();
        for (AssessmentSubmissionDto item : history) {
            if (courseId == null || courseId.equals(item.getCourseId())) {
                filteredHistory.add(item);
            }
        }

        model.addAttribute("progressCards", progressCards);
        model.addAttribute("submissionHistory", filteredHistory);
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "assignment/results";
    }

    @GetMapping("/submissions/{submissionId}/file")
    public ResponseEntity<Resource> downloadSubmissionFile(@PathVariable Long submissionId,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Submission submission;
        try {
            submission = assignmentService.getDetailedSubmissionOrThrow(submissionId);
        } catch (AssessmentException ex) {
            return ResponseEntity.notFound().build();
        }
        if (!canDownloadSubmission(user, submission)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (submission.getFileUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        Resource resource;
        try {
            resource = assessmentFileStorageService.loadAsResource(submission.getFileUrl());
        } catch (AssessmentException ex) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition(submission.getOriginalFileName()))
                .body(resource);
    }

    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userRepository.findByEmail(userDetails.getUsername()).orElse(null);
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

    private boolean canDownloadSubmission(User user, Submission submission) {
        if (user.getRole() == User.Role.ADMIN) {
            return true;
        }
        if (user.getRole() == User.Role.TEACHER) {
            return canManageAssignment(user, submission.getAssignment());
        }
        return user.getRole() == User.Role.STUDENT
                && submission.getStudent() != null
                && user.getId().equals(submission.getStudent().getId());
    }

    private String buildContentDisposition(String fileName) {
        String safeName = fileName == null ? "submission" : fileName;
        try {
            return "attachment; filename*=UTF-8''" + URLEncoder.encode(safeName, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException ex) {
            return "attachment; filename=\"submission\"";
        }
    }
}
