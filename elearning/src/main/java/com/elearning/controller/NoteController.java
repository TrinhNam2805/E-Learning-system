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
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final CourseService courseService;
    private final LessonService lessonService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        List<Note> allNotes = noteService.findByStudentId(user.getId());
        List<Note> standaloneNotes = noteService.findStandaloneByStudent(user.getId());

        model.addAttribute("allNotes", allNotes);
        model.addAttribute("standaloneNotes", standaloneNotes);
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        model.addAttribute("courses", courseService.findAllPublished());
        return "note/list";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long lessonId,
                       @RequestParam(required = false) Long courseId,
                       @RequestParam String content,
                       @RequestParam(defaultValue = "#FFFF00") String highlightColor,
                       @RequestParam(required = false) String title,
                       @RequestParam(defaultValue = "LESSON") String noteType,
                       @AuthenticationPrincipal UserDetails userDetails,
                       RedirectAttributes ra) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        Note.NoteType type = Note.NoteType.valueOf(noteType);
        Note.NoteBuilder builder = Note.builder()
                .student(user)
                .content(content)
                .highlightColor(highlightColor)
                .noteType(type)
                .title(title);

        if (lessonId != null) {
            lessonService.findById(lessonId).ifPresent(builder::lesson);
        }
        if (courseId != null) {
            courseService.findById(courseId).ifPresent(builder::course);
        }

        noteService.save(builder.build());
        ra.addFlashAttribute("success", "Note saved!");

        if (lessonId != null && courseId != null) {
            return "redirect:/lessons/" + lessonId + "?courseId=" + courseId;
        }
        return "redirect:/notes";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes ra) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        noteService.findById(id).ifPresent(note -> {
            if (note.getStudent().getId().equals(user.getId())) {
                noteService.delete(id);
            }
        });
        ra.addFlashAttribute("success", "Note deleted.");
        return "redirect:/notes";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @RequestParam String content,
                         @RequestParam(defaultValue = "#FFFF00") String highlightColor,
                         @RequestParam(required = false) String title,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes ra) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        noteService.findById(id).ifPresent(note -> {
            if (note.getStudent().getId().equals(user.getId())) {
                note.setContent(content);
                note.setHighlightColor(highlightColor);
                if (title != null) note.setTitle(title);
                noteService.save(note);
            }
        });
        ra.addFlashAttribute("success", "Note updated.");
        return "redirect:/notes";
    }
}
