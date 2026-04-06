package com.elearning.controller;

import com.elearning.exception.AssessmentException;
import com.elearning.model.dto.assessment.AssessmentAssignmentDto;
import com.elearning.model.dto.assessment.AssessmentProgressDto;
import com.elearning.model.dto.assessment.AssessmentSubmissionDto;
import com.elearning.model.dto.assessment.AssignmentSubmissionForm;
import com.elearning.model.entity.Assignment;
import com.elearning.model.entity.Lesson;
import com.elearning.model.entity.Submission;
import com.elearning.model.entity.User;
import com.elearning.repository.UserRepository;
import com.elearning.service.AssessmentFileStorageService;
import com.elearning.service.AssessmentResultTrackingService;
import com.elearning.service.AssignmentService;
import com.elearning.service.LessonService;
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
import org.springframework.util.StringUtils;
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
import java.util.Optional;

@Controller
@RequestMapping("/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssessmentResultTrackingService assessmentResultTrackingService;
    private final AssessmentFileStorageService assessmentFileStorageService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final LessonService lessonService;

    @GetMapping("/{id}")
    public String view(@PathVariable Long id,
                       @RequestParam(required = false) String mode,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model) {
        User user = getCurrentUser(userDetails);
        if (user == null) {
            return "redirect:/login";
        }
        if (user.getRole() != User.Role.STUDENT) {
            return "redirect:/access-denied";
        }

        Assignment assignment = assignmentService.findById(id).orElse(null);
        if (assignment == null) {
            return "redirect:/courses";
        }
        if (!canAccessAssignment(user, assignment)) {
            return "redirect:/courses/" + assignment.getCourse().getId();
        }

        AssessmentAssignmentDto assignmentView = assessmentResultTrackingService.getAssignmentDetail(id, user.getId());
        List<AssessmentSubmissionDto> submissionHistory = assessmentResultTrackingService.getAssignmentHistory(id, user.getId());
        AssessmentProgressDto progress = assessmentResultTrackingService.getCourseProgress(assignment.getCourse().getId(), user.getId());
        AssignmentSubmissionForm submissionForm = new AssignmentSubmissionForm();
        if (assignmentView.isEditableSubmission() && assignmentView.getLatestSubmission() != null) {
            submissionForm.setContent(assignmentView.getLatestSubmission().getContent());
        }
        boolean homeworkEditMode = "edit".equalsIgnoreCase(mode)
                && "HOMEWORK".equals(assignmentView.getType())
                && assignmentView.isEditableSubmission()
                && assignmentView.getLatestSubmission() != null;
        boolean quizRetakeMode = "retake".equalsIgnoreCase(mode)
                && "QUIZ".equals(assignmentView.getType())
                && assignmentView.getLatestSubmission() != null;

        populateQuizFollowUp(model, assignment, submissionHistory);
        populateHomeworkContext(model, assignmentView);

        model.addAttribute("assignment", assignmentView);
        model.addAttribute("submissionHistory", submissionHistory);
        model.addAttribute("progress", progress);
        model.addAttribute("submissionForm", submissionForm);
        model.addAttribute("homeworkEditMode", homeworkEditMode);
        model.addAttribute("showHomeworkForm",
                "HOMEWORK".equals(assignmentView.getType())
                        && (assignmentView.getLatestSubmission() == null || homeworkEditMode));
        model.addAttribute("showQuizAttemptPanel",
                "QUIZ".equals(assignmentView.getType())
                        && assignmentView.isCanSubmit()
                        && assignmentView.getQuestions() != null
                        && !assignmentView.getQuestions().isEmpty()
                        && (assignmentView.getLatestSubmission() == null || quizRetakeMode));
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
        boolean editMode = "edit".equalsIgnoreCase(allParams.get("mode"));
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
            return buildAssignmentRedirect(id, editMode);
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
                if (isPassingQuiz(assignment, submission)) {
                    success.append(" You passed this quiz. Use the next-step button below to continue.");
                } else {
                    success.append(" Review the linked lesson, then try again to unlock the next step.");
                }
                redirectAttributes.addFlashAttribute("success", success.toString());
                return buildAssignmentRedirect(id, false);
            } else {
                String assignmentLabel = assignment.getType() == Assignment.AssignmentType.HOMEWORK ? "Homework" : "Assignment";
                StringBuilder success = new StringBuilder(result.isUpdatedExisting()
                        ? assignmentLabel + " updated successfully. Your latest version is awaiting instructor grading."
                        : assignmentLabel + " submitted successfully. Your work is awaiting instructor grading.");
                if (submission.isLateSubmission()) {
                    success.append(" The submission was recorded as late.");
                }
                redirectAttributes.addFlashAttribute("success", success.toString());
                return buildAssignmentRedirect(id, false);
            }
        } catch (AssessmentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return buildAssignmentRedirect(id, editMode);
    }

    @PostMapping("/{id}/remove")
    public String removeHomeworkSubmission(@PathVariable Long id,
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

        try {
            assignmentService.removeHomeworkSubmission(id, user);
            redirectAttributes.addFlashAttribute("success", "Homework submission removed. You can submit a new file before the deadline.");
        } catch (AssessmentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return buildAssignmentRedirect(id, false);
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
        return user.getRole() == User.Role.STUDENT
                && assignmentService.isVisibleToStudent(assignment, user.getId());
    }

    private boolean canDownloadSubmission(User user, Submission submission) {
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

    private void populateQuizFollowUp(Model model,
                                      Assignment assignment,
                                      List<AssessmentSubmissionDto> submissionHistory) {
        if (assignment == null || assignment.getType() != Assignment.AssignmentType.QUIZ) {
            return;
        }

        double requiredScore = assignment.getMinimumPassingScore() != null
                ? assignment.getMinimumPassingScore()
                : 0.0;
        model.addAttribute("quizRequiredScore", formatScore(requiredScore));
        if (submissionHistory == null || submissionHistory.isEmpty()) {
            model.addAttribute("quizPassed", false);
            return;
        }

        AssessmentSubmissionDto latestSubmission = submissionHistory.get(0);
        AssessmentSubmissionDto passingAttempt = findPassingSubmission(submissionHistory, requiredScore);
        boolean quizPassed = passingAttempt != null;
        boolean latestAttemptPassed = latestSubmission.getScore() != null
                && latestSubmission.getScore() >= requiredScore;

        model.addAttribute("quizPassed", quizPassed);
        model.addAttribute("quizLatestAttemptPassed", latestAttemptPassed);
        model.addAttribute("quizPassingAttemptNumber",
                passingAttempt != null ? passingAttempt.getAttemptNumber() : null);

        if (quizPassed) {
            Optional<Lesson> nextLesson = lessonService.findNextPublishedLesson(
                    assignment.getCourse().getId(),
                    assignment.getLesson() != null ? assignment.getLesson().getId() : null);
            if (nextLesson.isPresent()) {
                Lesson lesson = nextLesson.get();
                model.addAttribute("quizFollowUpUrl", "/lessons/" + lesson.getId() + "?courseId=" + assignment.getCourse().getId());
                model.addAttribute("quizFollowUpLabel", "Open next lesson");
                if (latestAttemptPassed) {
                    model.addAttribute("quizFollowUpHint",
                            "You have met the passing score. The next lesson is unlocked and ready to open.");
                } else {
                    model.addAttribute("quizFollowUpHint",
                            "A previous passing attempt already unlocked the next lesson. You can continue with the course now.");
                }
            } else if (assignment.getLesson() != null && assignment.getCourse() != null) {
                model.addAttribute("quizFollowUpUrl", "/lessons/" + assignment.getLesson().getId() + "?courseId=" + assignment.getCourse().getId());
                model.addAttribute("quizFollowUpLabel", "Back to lesson");
                model.addAttribute("quizFollowUpHint", "This quiz is already passed. Return to the lesson to continue your review.");
            }
        } else if (assignment.getLesson() != null && assignment.getCourse() != null) {
            model.addAttribute("quizFollowUpUrl", "/lessons/" + assignment.getLesson().getId() + "?courseId=" + assignment.getCourse().getId());
            model.addAttribute("quizFollowUpLabel", "Back to lesson");
            model.addAttribute("quizFollowUpHint", "Review the lesson, then keep retaking the quiz until one attempt reaches the required score.");
        }
    }

    private void populateHomeworkContext(Model model,
                                         AssessmentAssignmentDto assignmentView) {
        if (assignmentView == null || "QUIZ".equals(assignmentView.getType())) {
            return;
        }

        String homeworkBrief = assignmentView.getDescription();
        if (!StringUtils.hasText(homeworkBrief)) {
            if (assignmentView.getLessonTitle() != null && assignmentView.getCourseName() != null) {
                homeworkBrief = "Complete the homework for lesson \""
                        + assignmentView.getLessonTitle()
                        + "\" in "
                        + assignmentView.getCourseName()
                        + ". Submit a written response and attach the required deliverable file for review.";
            } else if (assignmentView.getCourseName() != null) {
                homeworkBrief = "Complete the current homework in "
                        + assignmentView.getCourseName()
                        + " and upload the requested deliverable for instructor review.";
            } else {
                homeworkBrief = "Complete the current homework and upload the required deliverable for review.";
            }
        }

        model.addAttribute("homeworkBriefText", homeworkBrief);
    }

    private boolean isPassingQuiz(Assignment assignment, Submission submission) {
        if (assignment == null || submission == null || submission.getScore() == null) {
            return false;
        }
        if (assignment.getType() != Assignment.AssignmentType.QUIZ) {
            return false;
        }
        if (assignment.getMinimumPassingScore() == null) {
            return true;
        }
        return submission.getScore() >= assignment.getMinimumPassingScore();
    }

    private String formatScore(double score) {
        if (score == Math.floor(score)) {
            return String.valueOf((int) score);
        }
        return String.format(java.util.Locale.US, "%.1f", score);
    }

    private AssessmentSubmissionDto findPassingSubmission(List<AssessmentSubmissionDto> submissionHistory,
                                                          double requiredScore) {
        if (submissionHistory == null) {
            return null;
        }
        for (AssessmentSubmissionDto submission : submissionHistory) {
            if (submission.getScore() != null && submission.getScore() >= requiredScore) {
                return submission;
            }
        }
        return null;
    }

    private String buildAssignmentRedirect(Long assignmentId, boolean editMode) {
        String baseUrl = "redirect:/assignments/" + assignmentId;
        return editMode ? baseUrl + "?mode=edit" : baseUrl;
    }
}
