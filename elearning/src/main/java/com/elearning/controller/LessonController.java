package com.elearning.controller;

import com.elearning.model.dto.CurriculumSectionDto;
import com.elearning.model.dto.lesson.LessonAccessDto;
import com.elearning.model.entity.Assignment;
import com.elearning.model.entity.Course;
import com.elearning.model.entity.Lesson;
import com.elearning.model.entity.Note;
import com.elearning.model.entity.Notification;
import com.elearning.model.entity.User;
import com.elearning.repository.UserRepository;
import com.elearning.service.CourseService;
import com.elearning.service.EnrollmentService;
import com.elearning.service.AssignmentService;
import com.elearning.service.LessonService;
import com.elearning.service.LessonUnlockService;
import com.elearning.service.NoteService;
import com.elearning.service.NotificationService;
import com.elearning.util.LessonContentHtmlSanitizer;
import com.elearning.util.VideoEmbedUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final NoteService noteService;
    private final LessonUnlockService lessonUnlockService;
    private final AssignmentService assignmentService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @GetMapping("/{lessonId}")
    public String view(@PathVariable Long lessonId,
                       @RequestParam Long courseId,
                       @RequestParam(required = false) Long noteId,
                       @AuthenticationPrincipal UserDetails userDetails,
                       RedirectAttributes ra,
                       Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Course course = courseService.findById(courseId).orElse(null);
        Lesson lesson = lessonService.findById(lessonId).orElse(null);
        if (course == null || lesson == null) {
            return "redirect:/courses";
        }

        if (user.getRole() == User.Role.STUDENT && !enrollmentService.isEnrolled(user.getId(), courseId)) {
            return "redirect:/courses/" + courseId;
        }

        List<Lesson> allLessons = lessonService.findPublishedByCourseId(courseId);
        List<CurriculumSectionDto> curriculumSections = lessonService.buildCurriculumSections(allLessons);
        Map<Long, LessonAccessDto> lessonAccessMap = user.getRole() == User.Role.STUDENT
                ? lessonUnlockService.buildCourseLessonAccess(courseId, user.getId())
                : new LinkedHashMap<>();

        if (user.getRole() == User.Role.STUDENT) {
            LessonAccessDto currentLessonAccess = lessonAccessMap.get(lessonId);
            if (currentLessonAccess != null && !currentLessonAccess.isAccessible()) {
                ra.addFlashAttribute("error", currentLessonAccess.getLockedReason());
                return "redirect:/courses/" + courseId;
            }
        }

        int idx = -1;
        for (int i = 0; i < allLessons.size(); i++) {
            if (allLessons.get(i).getId().equals(lessonId)) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            return "redirect:/courses/" + courseId;
        }

        Lesson prev = idx > 0 ? allLessons.get(idx - 1) : null;
        Lesson next = idx < allLessons.size() - 1 ? allLessons.get(idx + 1) : null;
        int lessonIndexOneBased = idx + 1;
        String videoEmbed = VideoEmbedUtil.embedUrl(lesson.getVideoUrl());

        boolean isCompleted = enrollmentService.isLessonCompleted(user.getId(), lessonId);
        List<Assignment> lessonAssignments = assignmentService.findLessonWorkflowAssignments(courseId, lessonId);
        List<Note> lessonNotes = noteService.findByStudentAndLesson(user.getId(), lessonId);

        model.addAttribute("course", course);
        model.addAttribute("lesson", lesson);
        model.addAttribute("renderedLessonContent", LessonContentHtmlSanitizer.sanitizeForRender(lesson.getLessonContent()));
        model.addAttribute("allLessons", allLessons);
        model.addAttribute("curriculumSections", curriculumSections);
        model.addAttribute("lessonIndexOneBased", lessonIndexOneBased);
        model.addAttribute("totalLessonsCount", allLessons.size());
        model.addAttribute("videoEmbedUrl", videoEmbed);
        model.addAttribute("videoIsDirectFile", videoEmbed != null && VideoEmbedUtil.isDirectVideoFile(videoEmbed));
        model.addAttribute("hideStudentSidebar", true);
        model.addAttribute("prev", prev);
        model.addAttribute("next", next);
        model.addAttribute("isCompleted", isCompleted);
        model.addAttribute("lessonAssignments", lessonAssignments);
        model.addAttribute("lessonNotes", lessonNotes);
        model.addAttribute("lessonAccessMap", lessonAccessMap);
        model.addAttribute("nextLessonAccess", next != null ? lessonAccessMap.get(next.getId()) : null);
        model.addAttribute("canMarkComplete", user.getRole() == User.Role.STUDENT);
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));

        List<Long> completedLessonIds = user.getRole() == User.Role.STUDENT
                ? allLessons.stream()
                .filter(l -> lessonAccessMap.containsKey(l.getId())
                        ? lessonAccessMap.get(l.getId()).isCompleted()
                        : enrollmentService.isLessonCompleted(user.getId(), l.getId()))
                .map(Lesson::getId)
                .collect(Collectors.toList())
                : java.util.Collections.emptyList();
        model.addAttribute("completedLessonIds", completedLessonIds);

        Long scrollNote = null;
        String focusSourceExcerpt = null;
        if (noteId != null && noteService.isNoteOnLesson(user.getId(), lessonId, noteId)) {
            scrollNote = noteId;
            focusSourceExcerpt = noteService.findByIdAndStudentId(noteId, user.getId())
                    .map(Note::getSourceExcerpt)
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .orElse(null);
        }
        model.addAttribute("scrollToNoteId", scrollNote);
        model.addAttribute("focusSourceExcerpt", focusSourceExcerpt);

        return "lesson/view";
    }

    @PostMapping("/{lessonId}/complete")
    public String markComplete(@PathVariable Long lessonId,
                               @RequestParam Long courseId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes ra) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Lesson lesson = lessonService.findById(lessonId).orElse(null);
        if (lesson == null) {
            return "redirect:/courses/" + courseId;
        }
        if (user.getRole() != User.Role.STUDENT) {
            return "redirect:/access-denied";
        }
        if (!enrollmentService.isEnrolled(user.getId(), courseId)) {
            return "redirect:/courses/" + courseId;
        }

        LessonAccessDto lessonAccess = lessonUnlockService.buildCourseLessonAccess(courseId, user.getId()).get(lessonId);
        if (lessonAccess != null && !lessonAccess.isAccessible()) {
            ra.addFlashAttribute("error", lessonAccess.getLockedReason());
            return "redirect:/courses/" + courseId;
        }

        enrollmentService.markLessonComplete(user, lesson);
        notificationService.send(
                user,
                "Lesson completed",
                "You completed the lesson: " + lesson.getLessonTitle() + " (+20 XP).",
                Notification.NotifType.BADGE
        );
        return assignmentService.findFirstLessonWorkflowAssignment(courseId, lessonId)
                .map(nextAssignment -> {
                    ra.addFlashAttribute("success", "Lesson marked as complete. Continue with the quiz and homework for this lesson.");
                    return "redirect:/assignments/" + nextAssignment.getId();
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("success", "Lesson marked as complete. +20 XP");
                    return "redirect:/lessons/" + lessonId + "?courseId=" + courseId;
                });
    }
}
