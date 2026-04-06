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

    /** Giới hạn theo cột TEXT / UX; mỗi user một bộ ghi chú (student_id trong DB). */
    private static final int MAX_NOTE_CONTENT_LENGTH = 50_000;
    private static final int MAX_SOURCE_EXCERPT_LENGTH = 20_000;
    private static final int MAX_NOTE_TITLE_LENGTH = 200;
    private static final int MAX_TAGS_LENGTH = 500;
    /** Số ghi chú tối đa mỗi tài khoản (tránh lạm dụng). */
    private static final long MAX_NOTES_PER_USER = 10_000L;

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

        Note note = noteService.findByIdAndStudentId(id, user.getId()).orElse(null);
        if (note == null) {
            return "redirect:/notes";
        }

        model.addAttribute("note", note);
        model.addAttribute("outgoing", noteService.findOutgoingLinks(id));
        model.addAttribute("incoming", noteService.findIncomingLinks(id));
        model.addAttribute("linkCandidates", noteService.listOtherNotesForLinking(user.getId(), id));
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "note/detail-fixed";
    }

    /**
     * Mở đúng bài học chứa ghi chú (tránh lỗi URL/ghi chú thiếu course trên client).
     * Nếu không gắn lesson → về trang chi tiết ghi chú.
     */
    @GetMapping("/{id}/open-lesson")
    public String openLesson(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        Note note = noteService.findByIdAndStudentId(id, user.getId()).orElse(null);
        if (note == null) return "redirect:/notes";
        if (note.getLesson() == null) {
            return "redirect:/notes/" + id;
        }
        Long courseId = note.getCourse() != null
                ? note.getCourse().getId()
                : note.getLesson().getCourse().getId();
        return "redirect:/lessons/" + note.getLesson().getId() + "?courseId=" + courseId + "&noteId=" + id;
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

        if (content.length() > MAX_NOTE_CONTENT_LENGTH) {
            ra.addFlashAttribute("error", "Note content is too long (max " + MAX_NOTE_CONTENT_LENGTH + " characters).");
            return redirectAfterSave(lessonId, courseId, null);
        }
        if (title != null && title.length() > MAX_NOTE_TITLE_LENGTH) {
            ra.addFlashAttribute("error", "Title is too long (max " + MAX_NOTE_TITLE_LENGTH + " characters).");
            return redirectAfterSave(lessonId, courseId, null);
        }
        if (sourceExcerpt != null && sourceExcerpt.length() > MAX_SOURCE_EXCERPT_LENGTH) {
            ra.addFlashAttribute("error", "Source excerpt is too long (max " + MAX_SOURCE_EXCERPT_LENGTH + " characters).");
            return redirectAfterSave(lessonId, courseId, null);
        }
        String tagsNorm = normalizeTags(tags);
        if (tagsNorm != null && tagsNorm.length() > MAX_TAGS_LENGTH) {
            ra.addFlashAttribute("error", "Tags are too long (max " + MAX_TAGS_LENGTH + " characters).");
            return redirectAfterSave(lessonId, courseId, null);
        }
        if (noteService.countByStudentId(user.getId()) >= MAX_NOTES_PER_USER) {
            ra.addFlashAttribute("error", "Storage limit reached: maximum " + MAX_NOTES_PER_USER + " notes per account.");
            return redirectAfterSave(lessonId, courseId, null);
        }

        Note.NoteType type = Note.NoteType.valueOf(noteType);
        Note.NoteBuilder builder = Note.builder()
                .student(user)
                .content(content)
                .highlightColor(highlightColor)
                .noteType(type)
                .title(title)
                .sourceExcerpt(sourceExcerpt != null && !sourceExcerpt.trim().isEmpty() ? sourceExcerpt.trim() : null)
                .tags(tagsNorm);

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
                return redirectAfterSave(lessonId, courseId, saved.getId());
            }
        }

        ra.addFlashAttribute("success", "Note saved!");
        return redirectAfterSave(lessonId, courseId, saved.getId());
    }

    private static String redirectAfterSave(Long lessonId, Long courseId, Long savedNoteId) {
        if (lessonId != null && courseId != null) {
            StringBuilder sb = new StringBuilder("redirect:/lessons/")
                    .append(lessonId)
                    .append("?courseId=")
                    .append(courseId);
            if (savedNoteId != null) {
                sb.append("&noteId=").append(savedNoteId);
            }
            return sb.toString();
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

        noteService.findByIdAndStudentId(id, user.getId()).ifPresent(note -> noteService.delete(id));
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

        if (content.length() > MAX_NOTE_CONTENT_LENGTH) {
            ra.addFlashAttribute("error", "Note content is too long (max " + MAX_NOTE_CONTENT_LENGTH + " characters).");
            return "redirect:/notes/" + id;
        }
        if (title != null && title.length() > MAX_NOTE_TITLE_LENGTH) {
            ra.addFlashAttribute("error", "Title is too long (max " + MAX_NOTE_TITLE_LENGTH + " characters).");
            return "redirect:/notes/" + id;
        }
        if (sourceExcerpt != null && sourceExcerpt.length() > MAX_SOURCE_EXCERPT_LENGTH) {
            ra.addFlashAttribute("error", "Source excerpt is too long (max " + MAX_SOURCE_EXCERPT_LENGTH + " characters).");
            return "redirect:/notes/" + id;
        }
        String tagsNorm = normalizeTags(tags);
        if (tagsNorm != null && tagsNorm.length() > MAX_TAGS_LENGTH) {
            ra.addFlashAttribute("error", "Tags are too long (max " + MAX_TAGS_LENGTH + " characters).");
            return "redirect:/notes/" + id;
        }

        noteService.findByIdAndStudentId(id, user.getId()).ifPresent(note -> {
            note.setContent(content);
            note.setHighlightColor(highlightColor);
            if (title != null) note.setTitle(title);
            note.setSourceExcerpt(sourceExcerpt != null && !sourceExcerpt.trim().isEmpty() ? sourceExcerpt.trim() : null);
            note.setTags(tagsNorm);
            noteService.save(note);
        });
        ra.addFlashAttribute("success", "Note updated.");
        return "redirect:/notes/" + id;
    }
}
