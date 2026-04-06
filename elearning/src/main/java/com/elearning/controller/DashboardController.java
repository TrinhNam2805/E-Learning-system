package com.elearning.controller;

import com.elearning.model.dto.ranking.BadgeCollectionDto;
import com.elearning.model.dto.ranking.LeaderboardEntryDto;
import com.elearning.model.entity.Assignment;
import com.elearning.model.entity.Course;
import com.elearning.model.entity.Enrollment;
import com.elearning.model.entity.Notification;
import com.elearning.model.entity.User;
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
    private final BadgeService badgeService;
    private final CourseService courseService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";
        switch (user.getRole()) {
            case STUDENT:
                return "redirect:/student/dashboard";
            case TEACHER:
                return "redirect:/teacher/dashboard";
            case ADMIN:
                return "redirect:/admin/dashboard";
            default:
                return "redirect:/access-denied";
        }
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
        LeaderboardEntryDto myRank = rankingService.getMyRank(user.getId());
        BadgeCollectionDto earnedBadges = badgeService.getEarnedBadges(user.getId());

        model.addAttribute("currentUser", user);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("completedLessons", completedLessons);
        model.addAttribute("totalXp", totalXp);
        model.addAttribute("notifications", notifications.stream().limit(4).collect(Collectors.toList()));
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        model.addAttribute("upcomingAssignments", upcoming.stream().limit(5).collect(Collectors.toList()));
        model.addAttribute("myRank", myRank);
        model.addAttribute("earnedBadges", earnedBadges.getBadges().stream().limit(4).collect(Collectors.toList()));
        model.addAttribute("earnedBadgeCount", earnedBadges.getEarnedBadgeCount());
        return "dashboard/student";
    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";
        if (user.getRole() != User.Role.TEACHER && user.getRole() != User.Role.ADMIN) {
            return "redirect:/dashboard";
        }

        List<Course> myCourses = user.getRole() == User.Role.ADMIN
                ? courseService.findAll()
                : courseService.findByTeacher(user);

        int totalStudents = myCourses.stream()
                .mapToInt(c -> (int) enrollmentService.countEnrollmentsByCourse(c.getId()))
                .sum();
        int totalLessons = myCourses.stream()
                .mapToInt(c -> (int) lessonService.countByCourseId(c.getId()))
                .sum();

        model.addAttribute("currentUser", user);
        model.addAttribute("myCourses", myCourses);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalLessons", totalLessons);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "dashboard/teacher";
    }
}
