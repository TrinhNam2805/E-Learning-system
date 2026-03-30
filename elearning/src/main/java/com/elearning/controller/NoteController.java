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
    public String list(@RequestParam(required = false) String tag,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        List<Note> allNotes = noteService.findByStudentIdAndTag(user.getId(), tag);
        List<Note> standaloneNotes = allNotes.stream()
                .filter(n -> n.getNoteType() == Note.NoteType.STANDALONE)
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("allNotes", allNotes);
        model.addAttribute("standaloneNotes", standaloneNotes);
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        model.addAttribute("courses", courseService.findAllPublished());
        model.addAttribute("filterTag", tag);
        return "note/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        Note note = noteService.findById(id).orElse(null);
        if (note == null || !note.getStudent().getId().equals(user.getId())) {
            return "redirect:/notes";
        }

        model.addAttribute("note", note);
        model.addAttribute("outgoing", noteService.findOutgoingLinks(id));
        model.addAttribute("incoming", noteService.findIncomingLinks(id));
        model.addAttribute("linkCandidates", noteService.listOtherNotesForLinking(user.getId(), id));
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "note/detail";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long lessonId,
                       @RequestParam(required = false) Long courseId,
                       @RequestParam String content,
                       @RequestParam(defaultValue = "#FFFF00") String highlightColor,
                       @RequestParam(required = false) String title,
                       @RequestParam(required = false) String sourceExcerpt,
                       @RequestParam(required = false) String tags,
                       @RequestParam(required = false) Long linkToNoteId,
                       @RequestParam(required = false) String linkLabel,
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
                .title(title)
                .sourceExcerpt(sourceExcerpt != null && !sourceExcerpt.trim().isEmpty() ? sourceExcerpt.trim() : null)
                .tags(normalizeTags(tags));

        if (lessonId != null) {
            lessonService.findById(lessonId).ifPresent(builder::lesson);
        }
        if (courseId != null) {
            courseService.findById(courseId).ifPresent(builder::course);
        }

        Note saved = noteService.save(builder.build());

        if (linkToNoteId != null) {
            try {
                noteService.linkNotes(saved.getId(), linkToNoteId, user, linkLabel);
            } catch (Exception ex) {
                ra.addFlashAttribute("error", "Note saved, but link failed: " + ex.getMessage());
                return redirectAfterSave(lessonId, courseId);
            }
        }

        ra.addFlashAttribute("success", "Note saved!");
        return redirectAfterSave(lessonId, courseId);
    }

    private static String redirectAfterSave(Long lessonId, Long courseId) {
        if (lessonId != null && courseId != null) {
            return "redirect:/lessons/" + lessonId + "?courseId=" + courseId;
        }
        return "redirect:/notes";
    }

    private static String normalizeTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return null;
        }
        return tags.trim().replaceAll("\\s*,\\s*", ",");
    }

    @PostMapping("/{id}/link")
    public String addLink(@PathVariable Long id,
                          @RequestParam Long toNoteId,
                          @RequestParam(required = false) String linkLabel,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes ra) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";
        try {
            noteService.linkNotes(id, toNoteId, user, linkLabel);
            ra.addFlashAttribute("success", "Link created.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/notes/" + id;
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
                         @RequestParam(required = false) String sourceExcerpt,
                         @RequestParam(required = false) String tags,
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
                note.setSourceExcerpt(sourceExcerpt != null && !sourceExcerpt.trim().isEmpty() ? sourceExcerpt.trim() : null);
                note.setTags(normalizeTags(tags));
                noteService.save(note);
            }
        });
        ra.addFlashAttribute("success", "Note updated.");
        return "redirect:/notes/" + id;
    }
}
