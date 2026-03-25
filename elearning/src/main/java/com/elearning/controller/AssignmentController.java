package com.elearning.controller;

import com.elearning.model.entity.*;
import com.elearning.repository.UserRepository;
import com.elearning.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EnrollmentService enrollmentService;

    @GetMapping("/{id}")
    public String view(@PathVariable Long id,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        Assignment assignment = assignmentService.findById(id).orElse(null);
        if (assignment == null) return "redirect:/courses";

        boolean enrolled = enrollmentService.isEnrolled(user.getId(), assignment.getCourse().getId());
        if (!enrolled && user.getRole() == User.Role.STUDENT) return "redirect:/courses/" + assignment.getCourse().getId();

        Submission mySubmission = assignmentService.findSubmission(id, user.getId()).orElse(null);
        List<QuizQuestion> questions = assignmentService.findQuestions(id);

        model.addAttribute("assignment", assignment);
        model.addAttribute("mySubmission", mySubmission);
        model.addAttribute("questions", questions);
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "assignment/view";
    }

    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id,
                         @RequestParam(required = false) String content,
                         @RequestParam Map<String, String> allParams,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes ra) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        Assignment assignment = assignmentService.findById(id).orElse(null);
        if (assignment == null) return "redirect:/courses";

        String submitContent = content;

        // For QUIZ: auto-grade
        if (assignment.getType() == Assignment.AssignmentType.QUIZ) {
            List<QuizQuestion> questions = assignmentService.findQuestions(id);
            int correct = 0;
            StringBuilder answers = new StringBuilder();
            for (QuizQuestion q : questions) {
                String ans = allParams.get("q_" + q.getId());
                answers.append("Q").append(q.getId()).append(":").append(ans != null ? ans : "").append(";");
                if (q.getCorrectAnswer().equals(ans)) correct++;
            }
            submitContent = answers.toString();
            double score = questions.isEmpty() ? 0 : (correct * assignment.getMaxScore() / questions.size());

            Submission sub = Submission.builder()
                    .assignment(assignment).student(user)
                    .content(submitContent).score(score)
                    .status(Submission.SubmissionStatus.GRADED).build();
            assignmentService.submit(sub);
            notificationService.send(user, "Quiz Result",
                    "You scored " + String.format("%.1f", score) + "/" + assignment.getMaxScore() + " in quiz: " + assignment.getTitle(),
                    Notification.NotifType.GRADE);
            ra.addFlashAttribute("success", "Quiz submitted! Score: " + String.format("%.1f", score));
        } else {
            Submission sub = Submission.builder()
                    .assignment(assignment).student(user)
                    .content(submitContent).build();
            assignmentService.submit(sub);
            ra.addFlashAttribute("success", "Assignment submitted! Awaiting grading.");
        }

        return "redirect:/assignments/" + id;
    }

    // Teacher: view submissions
    @GetMapping("/{id}/submissions")
    public String submissions(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null || user.getRole() == User.Role.STUDENT) return "redirect:/dashboard";

        Assignment assignment = assignmentService.findById(id).orElse(null);
        if (assignment == null) return "redirect:/teacher/dashboard";

        List<Submission> submissions = assignmentService.findSubmissionsByAssignment(id);
        model.addAttribute("assignment", assignment);
        model.addAttribute("submissions", submissions);
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "assignment/submissions";
    }

    @PostMapping("/grade/{submissionId}")
    public String grade(@PathVariable Long submissionId,
                        @RequestParam double score,
                        @RequestParam(required = false) String feedback,
                        @AuthenticationPrincipal UserDetails userDetails,
                        RedirectAttributes ra) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null || user.getRole() == User.Role.STUDENT) return "redirect:/dashboard";

        Submission sub = assignmentService.grade(submissionId, score, feedback);
        notificationService.send(sub.getStudent(), "Assignment Graded",
                "Your submission has been graded: " + String.format("%.1f", score) + " points.",
                Notification.NotifType.GRADE);
        ra.addFlashAttribute("success", "Graded successfully!");
        return "redirect:/assignments/" + sub.getAssignment().getId() + "/submissions";
    }
}
