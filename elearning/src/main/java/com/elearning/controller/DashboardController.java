package com.elearning.controller;

import com.elearning.model.entity.*;
import com.elearning.repository.UserRepository;
import com.elearning.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;
    private final LessonService lessonService;
    private final NotificationService notificationService;
    private final AssignmentService assignmentService;
    private final RankingService rankingService;
    private final CourseService courseService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";
        if (user.getRole() == User.Role.STUDENT) return "redirect:/student/dashboard";
        return "redirect:/access-denied";
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";
        if (user.getRole() != User.Role.STUDENT) return "redirect:/dashboard";

        List<Enrollment> enrollments = enrollmentService.findByStudentId(user.getId());
        long completedLessons = enrollments.stream()
                .mapToLong(e -> lessonService.findPublishedByCourseId(e.getCourse().getId()).stream()
                        .filter(l -> enrollmentService.isLessonCompleted(user.getId(), l.getId())).count())
                .sum();
        int totalXp = enrollments.stream().mapToInt(Enrollment::getTotalXp).sum();
        List<Notification> notifications = notificationService.findByUserId(user.getId());
        List<Assignment> upcoming = assignmentService.findUpcomingForStudent(user.getId());
        RankingService.RankEntry myRank = rankingService.getMyRank(user.getId());

        model.addAttribute("currentUser", user);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("completedLessons", completedLessons);
        model.addAttribute("totalXp", totalXp);
        model.addAttribute("notifications", notifications.stream().limit(4).collect(Collectors.toList()));
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        model.addAttribute("upcomingAssignments", upcoming.stream().limit(5).collect(Collectors.toList()));
        model.addAttribute("myRank", myRank);
        return "dashboard/student";
    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";
        return "redirect:/access-denied";
    }
}
