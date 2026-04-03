package com.elearning.controller;

import com.elearning.model.entity.*;
import com.elearning.repository.CourseSectionRepository;
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

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final CourseService courseService;
    private final LessonService lessonService;
    private final CourseSectionRepository courseSectionRepository;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private User getTeacher(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByEmail(userDetails.getUsername()).orElse(null);
    }

    @GetMapping("/courses/{courseId}/lessons")
    public String manageLessons(@PathVariable Long courseId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        User user = getTeacher(userDetails);
        if (user == null) return "redirect:/login";

        Course course = courseService.findById(courseId).orElse(null);
        if (course == null) return "redirect:/teacher/dashboard";

        model.addAttribute("course", course);
        model.addAttribute("lessons", lessonService.findByCourseId(courseId));
        model.addAttribute("sections", courseSectionRepository.findByCourse_IdOrderBySectionOrderAsc(courseId));
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "teacher/lessons";
    }

    @PostMapping("/courses/{courseId}/lessons/add")
    public String addLesson(@PathVariable Long courseId,
                            @RequestParam String lessonTitle,
                            @RequestParam String lessonContent,
                            @RequestParam(required = false) String videoUrl,
                            @RequestParam(required = false) Long sectionId,
                            @RequestParam int durationMinutes,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes ra) {
        User user = getTeacher(userDetails);
        if (user == null) return "redirect:/login";

        Course course = courseService.findById(courseId).orElse(null);
        if (course == null) return "redirect:/teacher/dashboard";

        CourseSection section = lessonService.resolveSectionForLesson(courseId, course, sectionId);
        long count = lessonService.countByCourseId(courseId);
        Lesson lesson = Lesson.builder()
                .course(course).section(section).lessonTitle(lessonTitle).lessonContent(lessonContent)
                .videoUrl(videoUrl).durationMinutes(durationMinutes)
                .lessonOrder((int) count + 1).build();
        lessonService.save(lesson);
        ra.addFlashAttribute("success", "Lesson added successfully!");
        return "redirect:/teacher/courses/" + courseId + "/lessons";
    }

    @PostMapping("/courses/{courseId}/sections/add")
    public String addSection(@PathVariable Long courseId,
                             @RequestParam String sectionTitle,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes ra) {
        User user = getTeacher(userDetails);
        if (user == null) return "redirect:/login";
        Course course = courseService.findById(courseId).orElse(null);
        if (course == null) return "redirect:/teacher/dashboard";
        lessonService.addSection(courseId, course, sectionTitle);
        ra.addFlashAttribute("success", "Section added.");
        return "redirect:/teacher/courses/" + courseId + "/lessons";
    }

    @PostMapping("/lessons/{lessonId}/delete")
    public String deleteLesson(@PathVariable Long lessonId,
                               @RequestParam Long courseId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes ra) {
        User user = getTeacher(userDetails);
        if (user == null) return "redirect:/login";
        lessonService.delete(lessonId);
        ra.addFlashAttribute("success", "Lesson deleted.");
        return "redirect:/teacher/courses/" + courseId + "/lessons";
    }

    @GetMapping("/courses/{courseId}/assignments")
    public String manageAssignments(@PathVariable Long courseId,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) {
        User user = getTeacher(userDetails);
        if (user == null) return "redirect:/login";

        Course course = courseService.findById(courseId).orElse(null);
        if (course == null) return "redirect:/teacher/dashboard";

        model.addAttribute("course", course);
        model.addAttribute("assignments", assignmentService.findByCourseId(courseId));
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "teacher/assignments";
    }

    @PostMapping("/courses/{courseId}/assignments/add")
    public String addAssignment(@PathVariable Long courseId,
                                @RequestParam String title,
                                @RequestParam(required = false) String description,
                                @RequestParam String type,
                                @RequestParam String dueDate,
                                @RequestParam double maxScore,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes ra) {
        User user = getTeacher(userDetails);
        if (user == null) return "redirect:/login";

        Course course = courseService.findById(courseId).orElse(null);
        if (course == null) return "redirect:/teacher/dashboard";

        Assignment assignment = Assignment.builder()
                .course(course).title(title).description(description)
                .type(Assignment.AssignmentType.valueOf(type))
                .dueDate(LocalDateTime.parse(dueDate + "T23:59:00"))
                .maxScore(maxScore).build();
        assignmentService.save(assignment);

        // Notify enrolled students
        List<Enrollment> enrollments = enrollmentService.findByCourseId(courseId);
        for (Enrollment e : enrollments) {
            notificationService.send(e.getStudent(), "New Assignment: " + title,
                    "Course " + course.getCourseCode() + " has a new assignment. Due: " + dueDate,
                    Notification.NotifType.ASSIGNMENT);
        }

        ra.addFlashAttribute("success", "Assignment added successfully!");
        return "redirect:/teacher/courses/" + courseId + "/assignments";
    }

    @GetMapping("/courses/{courseId}/students")
    public String viewStudents(@PathVariable Long courseId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User user = getTeacher(userDetails);
        if (user == null) return "redirect:/login";

        Course course = courseService.findById(courseId).orElse(null);
        if (course == null) return "redirect:/teacher/dashboard";

        List<Enrollment> enrollments = enrollmentService.findByCourseId(courseId);
        model.addAttribute("course", course);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "teacher/students";
    }
}
