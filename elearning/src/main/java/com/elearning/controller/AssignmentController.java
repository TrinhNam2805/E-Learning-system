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
import java.time.LocalDateTime;
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
    private final GamificationService gamificationService;

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

        boolean pastDue = isPastDue(assignment);

        model.addAttribute("assignment", assignment);
        model.addAttribute("mySubmission", mySubmission);
        model.addAttribute("questions", questions);
        model.addAttribute("pastDue", pastDue);
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
        if (user.getRole() != User.Role.STUDENT) return "redirect:/access-denied";
        boolean enrolled = enrollmentService.isEnrolled(user.getId(), assignment.getCourse().getId());
        if (!enrolled) return "redirect:/courses/" + assignment.getCourse().getId();

        if (assignmentService.findSubmission(id, user.getId()).isPresent()) {
            ra.addFlashAttribute("error", "You have already submitted this assignment.");
            return "redirect:/assignments/" + id;
        }

        boolean pastDue = isPastDue(assignment);
        boolean confirmLate = "true".equalsIgnoreCase(allParams.get("confirmLate"));
        if (pastDue && !confirmLate) {
            ra.addFlashAttribute("error",
                    "This assignment is past the due date. Check \"Late submission\" to confirm you still want to submit.");
            return "redirect:/assignments/" + id;
        }

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

            Submission.SubmissionStatus quizStatus = pastDue
                    ? Submission.SubmissionStatus.LATE
                    : Submission.SubmissionStatus.GRADED;

            Submission sub = Submission.builder()
                    .assignment(assignment).student(user)
                    .content(submitContent).score(score)
                    .status(quizStatus).build();
            assignmentService.submit(sub);
            int xpGained = gamificationService.awardQuizXp(user.getId(), assignment.getCourse().getId(), score, assignment.getMaxScore());
            notificationService.send(user, "Quiz Result",
                    "You scored " + String.format("%.1f", score) + "/" + assignment.getMaxScore()
                            + " in quiz: " + assignment.getTitle() + ". +" + xpGained + " XP.",
                    Notification.NotifType.GRADE);
            String lateNote = pastDue ? " (late submission)" : "";
            ra.addFlashAttribute("success", "Quiz submitted! Score: " + String.format("%.1f", score)
                    + lateNote + ". +" + xpGained + " XP.");
        } else {
            Submission.SubmissionStatus hwStatus = pastDue
                    ? Submission.SubmissionStatus.LATE
                    : Submission.SubmissionStatus.SUBMITTED;
            Submission sub = Submission.builder()
                    .assignment(assignment).student(user)
                    .content(submitContent)
                    .status(hwStatus)
                    .build();
            assignmentService.submit(sub);
            String lateNote = pastDue ? " (marked as late)" : "";
            ra.addFlashAttribute("success", "Assignment submitted! Awaiting grading." + lateNote);
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

    private static boolean isPastDue(Assignment assignment) {
        LocalDateTime due = assignment.getDueDate();
        if (due == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(due);
    }
}
