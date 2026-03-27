package com.elearning.service;

import com.elearning.model.entity.Note;
import com.elearning.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;

    public List<Note> findByStudentId(Long studentId) {
        return noteRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
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

    @Transactional
    public Note save(Note note) {
        return noteRepository.save(note);
    }

    @Transactional
    public void delete(Long id) {
        noteRepository.deleteById(id);
    }
}
