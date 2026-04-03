package com.elearning.controller;

import com.elearning.model.entity.*;
import com.elearning.model.dto.lesson.LessonAccessDto;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;
    private final ForumService forumService;
    private final AssignmentService assignmentService;
    private final LessonUnlockService lessonUnlockService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model) {
        List<Course> courses = q != null && !q.trim().isEmpty()
                ? courseService.search(q)
                : courseService.findAllPublished();
        model.addAttribute("courses", courses);
        model.addAttribute("q", q);
        addUserToModel(userDetails, model);
        return "course/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        Course course = courseService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found."));
        List<Lesson> lessons = lessonService.findPublishedByCourseId(id);
        List<ForumPost> recentPosts = forumService.findByCourseId(id).stream().limit(3).collect(Collectors.toList());
        List<Assignment> assignments = assignmentService.findByCourseId(id);

        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        model.addAttribute("recentPosts", recentPosts);
        model.addAttribute("assignments", assignments);

        User user = addUserToModel(userDetails, model);
        model.addAttribute("enrolled", false);
        if (user != null) {
            boolean enrolled = enrollmentService.isEnrolled(user.getId(), id);
            model.addAttribute("enrolled", enrolled);
            if (enrolled) {
                java.util.Map<Long, LessonAccessDto> lessonAccessMap = lessonUnlockService.buildCourseLessonAccess(id, user.getId());
                List<Long> completedLessonIds = lessonAccessMap.values().stream()
                        .filter(LessonAccessDto::isCompleted)
                        .map(LessonAccessDto::getLessonId)
                        .collect(Collectors.toList());
                model.addAttribute("lessonAccessMap", lessonAccessMap);
                model.addAttribute("completedLessonIds", completedLessonIds);
            }
        }
        return "course/detail";
    }

    @PostMapping("/{id}/enroll")
    public String enroll(@PathVariable Long id,
                         @RequestParam(required = false) String enrollPassword,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes ra) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        Course course = courseService.findById(id).orElse(null);
        if (course == null) { ra.addFlashAttribute("error", "Course not found."); return "redirect:/courses"; }

        if (course.getEnrollPassword() != null && !course.getEnrollPassword().trim().isEmpty()) {
            if (!course.getEnrollPassword().equals(enrollPassword)) {
                ra.addFlashAttribute("error", "Incorrect enrollment password.");
                return "redirect:/courses/" + id;
            }
        }

        try {
            enrollmentService.enroll(user, course);
            notificationService.send(user, "Enrollment Successful",
                    "You have successfully enrolled in: " + course.getCourseName(),
                    Notification.NotifType.SYSTEM);
            ra.addFlashAttribute("success", "Enrolled successfully!");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/courses/" + id;
    }

    private User addUserToModel(UserDetails userDetails, Model model) {
        if (userDetails == null) return null;
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user != null) {
            model.addAttribute("currentUser", user);
            model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        }
        return user;
    }
}
