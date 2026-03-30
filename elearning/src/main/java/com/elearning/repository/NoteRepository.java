package com.elearning.repository;

import com.elearning.model.entity.Note;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    @EntityGraph("Note.withLessonAndCourse")
    List<Note> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    @EntityGraph("Note.withLessonAndCourse")
    List<Note> findByStudentIdAndLessonIdOrderByCreatedAtDesc(Long studentId, Long lessonId);

    long countByStudentId(Long studentId);

    boolean existsByIdAndStudent_IdAndLesson_Id(Long id, Long studentId, Long lessonId);

    @EntityGraph("Note.withLessonAndCourse")
    Optional<Note> findByIdAndStudent_Id(Long id, Long studentId);

    List<Note> findByStudentIdAndCourseIdOrderByCreatedAtDesc(Long studentId, Long courseId);

    List<Note> findByStudentIdAndNoteTypeOrderByCreatedAtDesc(Long studentId, Note.NoteType noteType);

    @EntityGraph("Note.withLessonAndCourse")
    List<Note> findByStudentIdAndTagsContainingIgnoreCaseOrderByCreatedAtDesc(Long studentId, String tagFragment);
}
