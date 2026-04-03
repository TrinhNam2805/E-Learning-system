package com.elearning.controller;

import com.elearning.exception.AssessmentException;
import com.elearning.model.dto.assessment.AssignmentCreateForm;
import com.elearning.model.entity.*;
import com.elearning.repository.UserRepository;
import com.elearning.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final CourseService courseService;
    private final LessonService lessonService;
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
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "teacher/lessons";
    }

    @PostMapping("/courses/{courseId}/lessons/add")
    public String addLesson(@PathVariable Long courseId,
                            @RequestParam String lessonTitle,
                            @RequestParam String lessonContent,
                            @RequestParam(required = false) String videoUrl,
                            @RequestParam int durationMinutes,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes ra) {
        User user = getTeacher(userDetails);
        if (user == null) return "redirect:/login";

        Course course = courseService.findById(courseId).orElse(null);
        if (course == null) return "redirect:/teacher/dashboard";

        long count = lessonService.countByCourseId(courseId);
        Lesson lesson = Lesson.builder()
                .course(course).lessonTitle(lessonTitle).lessonContent(lessonContent)
                .videoUrl(videoUrl).durationMinutes(durationMinutes)
                .lessonOrder((int) count + 1).build();
        lessonService.save(lesson);
        ra.addFlashAttribute("success", "Lesson added successfully!");
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
        model.addAttribute("lessons", lessonService.findByCourseId(courseId));
        model.addAttribute("assignmentForm", new AssignmentCreateForm());
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "teacher/assessment-assignments";
    }

    @PostMapping("/courses/{courseId}/assignments/add")
    public String addAssignment(@PathVariable Long courseId,
                                @Valid @ModelAttribute("assignmentForm") AssignmentCreateForm form,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes ra) {
        User user = getTeacher(userDetails);
        if (user == null) return "redirect:/login";

        Course course = courseService.findById(courseId).orElse(null);
        if (course == null) return "redirect:/teacher/dashboard";

        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/teacher/courses/" + courseId + "/assignments";
        }

        Lesson linkedLesson = null;
        if (form.getLessonId() != null) {
            linkedLesson = lessonService.findById(form.getLessonId()).orElse(null);
            if (linkedLesson == null || linkedLesson.getCourse() == null || !courseId.equals(linkedLesson.getCourse().getId())) {
                ra.addFlashAttribute("error", "The selected lesson is invalid.");
                return "redirect:/teacher/courses/" + courseId + "/assignments";
            }
            if (form.getMinimumPassingScore() == null) {
                ra.addFlashAttribute("error", "Please enter the minimum passing score required to unlock the next lesson.");
                return "redirect:/teacher/courses/" + courseId + "/assignments";
            }
            if (form.getMinimumPassingScore() > form.getMaxScore()) {
                ra.addFlashAttribute("error", "The minimum passing score cannot be greater than the maximum score.");
                return "redirect:/teacher/courses/" + courseId + "/assignments";
            }
        } else if (form.getMinimumPassingScore() != null) {
            ra.addFlashAttribute("error", "Only configure a minimum passing score when a linked lesson is selected.");
            return "redirect:/teacher/courses/" + courseId + "/assignments";
        }

        Assignment assignment = Assignment.builder()
                .course(course)
                .lesson(linkedLesson)
                .title(form.getTitle())
                .description(form.getDescription())
                .type(form.getType())
                .dueDate(form.getDueDate())
                .maxScore(form.getMaxScore())
                .minimumPassingScore(form.getMinimumPassingScore())
                .allowLateSubmission(form.isAllowLateSubmission())
                .maxAttempts(form.getMaxAttempts())
                .build();
        try {
            assignmentService.save(assignment);
        } catch (AssessmentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/teacher/courses/" + courseId + "/assignments";
        }

        // Notify enrolled students
        List<Enrollment> enrollments = enrollmentService.findByCourseId(courseId);
        for (Enrollment e : enrollments) {
            notificationService.send(e.getStudent(), "New assignment: " + form.getTitle(),
                    "Course " + course.getCourseCode() + " has a new assignment. Due date: " + form.getDueDate() + ".",
                    Notification.NotifType.ASSIGNMENT);
        }

        ra.addFlashAttribute("success", "Assignment created successfully.");
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
