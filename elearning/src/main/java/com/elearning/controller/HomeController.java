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
public class HomeController {

    private final CourseService courseService;
    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;
    private final NotificationService notificationService;
    private final NoteService noteService;
    private final RankingService rankingService;

    @GetMapping({"/", "/home"})
    public String home(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<Course> featured = courseService.findTopPublishedByEnrollmentCount(5);
        model.addAttribute("featuredCourses", featured);

        if (userDetails != null) {
            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            if (user != null) {
                if (user.getRole() != User.Role.STUDENT) {
                    return "redirect:/access-denied";
                }
                model.addAttribute("currentUser", user);
                List<Enrollment> enrollments = enrollmentService.findByStudentId(user.getId());
                model.addAttribute("enrollments", enrollments);
                model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
                model.addAttribute("recentNotes", noteService.findByStudentId(user.getId()).stream().limit(3).collect(Collectors.toList()));
                model.addAttribute("rankings", rankingService.getLeaderboard().stream().limit(5).collect(Collectors.toList()));
                model.addAttribute("myRank", rankingService.getMyRank(user.getId()));
            }
        }
        return "home";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }
}
