package com.elearning.repository;

import com.elearning.model.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<Note> findByStudentIdAndLessonIdOrderByCreatedAtDesc(Long studentId, Long lessonId);

    List<Note> findByStudentIdAndCourseIdOrderByCreatedAtDesc(Long studentId, Long courseId);

    List<Note> findByStudentIdAndNoteTypeOrderByCreatedAtDesc(Long studentId, Note.NoteType noteType);

    List<Note> findByStudentIdAndTagsContainingIgnoreCaseOrderByCreatedAtDesc(Long studentId, String tagFragment);
}
