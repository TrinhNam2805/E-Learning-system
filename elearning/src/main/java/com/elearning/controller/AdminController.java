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

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final LessonService lessonService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null || user.getRole() != User.Role.ADMIN) return "redirect:/dashboard";

        List<User> users = userService.findAll();
        List<Course> courses = courseService.findAll();

        model.addAttribute("currentUser", user);
        model.addAttribute("users", users);
        model.addAttribute("courses", courses);
        model.addAttribute("totalUsers", userService.countAll());
        model.addAttribute("totalStudents", users.stream().filter(u -> u.getRole() == User.Role.STUDENT).count());
        model.addAttribute("totalTeachers", users.stream().filter(u -> u.getRole() == User.Role.TEACHER).count());
        model.addAttribute("totalCourses", courseService.countAll());
        model.addAttribute("totalPublished", courseService.countPublished());
        model.addAttribute("totalEnrollments", enrollmentService.countAll());
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "admin/dashboard";
    }

    @PostMapping("/users/{id}/toggle-lock")
    public String toggleLock(@PathVariable Long id, RedirectAttributes ra) {
        userService.findById(id).ifPresent(u -> {
            userService.toggleLock(id);
            ra.addFlashAttribute("success", (u.isLocked() ? "Unlocked" : "Locked") + " account: " + u.getFullName());
        });
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/courses/create")
    public String createCoursePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";
        model.addAttribute("currentUser", user);
        model.addAttribute("teachers", userService.findAllTeachers());
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "admin/course-form";
    }

    @PostMapping("/courses/create")
    public String createCourse(@RequestParam String courseCode,
                               @RequestParam String courseName,
                               @RequestParam String description,
                               @RequestParam Long teacherId,
                               @RequestParam(required = false) String enrollPassword,
                               @RequestParam String semester,
                               @RequestParam String academicYear,
                               @RequestParam String status,
                               @RequestParam int maxStudents,
                               @RequestParam(required = false) String thumbnail,
                               RedirectAttributes ra) {
        User teacher = userRepository.findById(teacherId).orElse(null);
        if (teacher == null) { ra.addFlashAttribute("error", "Teacher not found."); return "redirect:/admin/courses/create"; }

        Course course = Course.builder()
                .courseCode(courseCode).courseName(courseName).description(description)
                .teacher(teacher).enrollPassword(enrollPassword).semester(semester)
                .academicYear(academicYear).status(Course.Status.valueOf(status))
                .maxStudents(maxStudents).thumbnail(thumbnail != null && !thumbnail.trim().isEmpty() ? thumbnail : "/images/e-learning.jpg")
                .build();
        courseService.save(course);
        ra.addFlashAttribute("success", "Course created successfully!");
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/courses/{id}/lessons")
    public String manageLessons(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        Course course = courseService.findById(id).orElse(null);
        if (course == null) return "redirect:/admin/dashboard";

        model.addAttribute("course", course);
        model.addAttribute("lessons", lessonService.findByCourseId(id));
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "admin/lessons";
    }

    @PostMapping("/courses/{courseId}/lessons/add")
    public String addLesson(@PathVariable Long courseId,
                            @RequestParam String lessonTitle,
                            @RequestParam String lessonContent,
                            @RequestParam(required = false) String videoUrl,
                            @RequestParam int durationMinutes,
                            RedirectAttributes ra) {
        Course course = courseService.findById(courseId).orElse(null);
        if (course == null) return "redirect:/admin/dashboard";

        long count = lessonService.countByCourseId(courseId);
        Lesson lesson = Lesson.builder()
                .course(course).lessonTitle(lessonTitle).lessonContent(lessonContent)
                .videoUrl(videoUrl).durationMinutes(durationMinutes)
                .lessonOrder((int) count + 1).build();
        lessonService.save(lesson);
        ra.addFlashAttribute("success", "Lesson added successfully!");
        return "redirect:/admin/courses/" + courseId + "/lessons";
    }
}
