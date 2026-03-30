package com.elearning.service;

import com.elearning.model.entity.Note;
import com.elearning.model.entity.NoteLink;
import com.elearning.model.entity.User;
import com.elearning.repository.NoteLinkRepository;
import com.elearning.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteLinkRepository noteLinkRepository;

    public List<Note> findByStudentId(Long studentId) {
        return noteRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    public List<Note> findByStudentIdAndTag(Long studentId, String tagFragment) {
        if (tagFragment == null || tagFragment.trim().isEmpty()) {
            return findByStudentId(studentId);
        }
        return noteRepository.findByStudentIdAndTagsContainingIgnoreCaseOrderByCreatedAtDesc(
                studentId, tagFragment.trim());
    }

    public List<Note> findByStudentAndLesson(Long studentId, Long lessonId) {
        return noteRepository.findByStudentIdAndLessonIdOrderByCreatedAtDesc(studentId, lessonId);
    }

    public List<Note> findByStudentAndCourse(Long studentId, Long courseId) {
        return noteRepository.findByStudentIdAndCourseIdOrderByCreatedAtDesc(studentId, courseId);
    }

    public List<Note> findStandaloneByStudent(Long studentId) {
        return noteRepository.findByStudentIdAndNoteTypeOrderByCreatedAtDesc(studentId, Note.NoteType.STANDALONE);
    }

    public Optional<Note> findById(Long id) {
        return noteRepository.findById(id);
    }

    public List<NoteLink> findOutgoingLinks(Long noteId) {
        return noteLinkRepository.findByFromNoteId(noteId);
    }

    public List<NoteLink> findIncomingLinks(Long noteId) {
        return noteLinkRepository.findByToNoteId(noteId);
    }

    @Transactional
    public Note save(Note note) {
        return noteRepository.save(note);
    }

    /**
     * Liên kết hai ghi chú của cùng một người (đồ thị tri thức).
     */
    @Transactional
    public void linkNotes(Long fromNoteId, Long toNoteId, User student, String relationLabel) {
        if (fromNoteId.equals(toNoteId)) {
            throw new IllegalArgumentException("Cannot link a note to itself.");
        }
        Note from = noteRepository.findById(fromNoteId).orElseThrow(() -> new IllegalArgumentException("Note not found."));
        Note to = noteRepository.findById(toNoteId).orElseThrow(() -> new IllegalArgumentException("Target note not found."));
        if (!from.getStudent().getId().equals(student.getId()) || !to.getStudent().getId().equals(student.getId())) {
            throw new IllegalStateException("You can only link your own notes.");
        }
        if (noteLinkRepository.existsByFromNoteIdAndToNoteId(fromNoteId, toNoteId)) {
            return;
        }
        NoteLink link = NoteLink.builder()
                .fromNote(from)
                .toNote(to)
                .relationLabel(relationLabel != null && !relationLabel.isEmpty() ? relationLabel.trim() : "related")
                .build();
        noteLinkRepository.save(link);
    }

    @Transactional
    public void delete(Long id) {
        noteLinkRepository.deleteAllByNoteInvolved(id);
        noteRepository.deleteById(id);
    }

    /** Các ghi chú khác của cùng sinh viên (để chọn liên kết). */
    public List<Note> listOtherNotesForLinking(Long studentId, Long excludeNoteId) {
        return noteRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .filter(n -> !n.getId().equals(excludeNoteId))
                .collect(Collectors.toList());
    }
}
