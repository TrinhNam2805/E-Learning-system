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
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final NoteService noteService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @GetMapping("/{lessonId}")
    public String view(@PathVariable Long lessonId,
                       @RequestParam Long courseId,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        Course course = courseService.findById(courseId).orElse(null);
        Lesson lesson = lessonService.findById(lessonId).orElse(null);
        if (course == null || lesson == null) return "redirect:/courses";

        // Guard: student must be enrolled
        if (user.getRole() == User.Role.STUDENT && !enrollmentService.isEnrolled(user.getId(), courseId)) {
            return "redirect:/courses/" + courseId;
        }

        List<Lesson> allLessons = lessonService.findPublishedByCourseId(courseId);
        int idx = allLessons.indexOf(lesson);
        Lesson prev = idx > 0 ? allLessons.get(idx - 1) : null;
        Lesson next = idx < allLessons.size() - 1 ? allLessons.get(idx + 1) : null;

        boolean isCompleted = enrollmentService.isLessonCompleted(user.getId(), lessonId);
        List<Note> lessonNotes = noteService.findByStudentAndLesson(user.getId(), lessonId);
        List<Note> noteLinkCandidates = noteService.findByStudentId(user.getId());

        model.addAttribute("course", course);
        model.addAttribute("lesson", lesson);
        model.addAttribute("allLessons", allLessons);
        model.addAttribute("prev", prev);
        model.addAttribute("next", next);
        model.addAttribute("isCompleted", isCompleted);
        model.addAttribute("lessonNotes", lessonNotes);
        model.addAttribute("noteLinkCandidates", noteLinkCandidates);
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));

        // completed lesson IDs for sidebar checkmarks
        List<Long> completedLessonIds = allLessons.stream()
                .filter(l -> enrollmentService.isLessonCompleted(user.getId(), l.getId()))
                .map(Lesson::getId)
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("completedLessonIds", completedLessonIds);

        return "lesson/view";
    }

    @PostMapping("/{lessonId}/complete")
    public String markComplete(@PathVariable Long lessonId,
                               @RequestParam Long courseId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes ra) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        Lesson lesson = lessonService.findById(lessonId).orElse(null);
        if (lesson == null) return "redirect:/courses/" + courseId;
        if (user.getRole() != User.Role.STUDENT) return "redirect:/access-denied";
        if (!enrollmentService.isEnrolled(user.getId(), courseId)) return "redirect:/courses/" + courseId;

        enrollmentService.markLessonComplete(user, lesson);
        notificationService.send(user, "Lesson Completed",
                "Congratulations! You completed: " + lesson.getLessonTitle() + " (+20 XP)",
                Notification.NotifType.BADGE);
        ra.addFlashAttribute("success", "Lesson marked complete! +20 XP");
        return "redirect:/lessons/" + lessonId + "?courseId=" + courseId;
    }
}
