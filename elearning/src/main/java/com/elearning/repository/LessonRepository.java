package com.elearning.repository;

import com.elearning.model.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseIdOrderByLessonOrderAsc(Long courseId);

    @Query("SELECT DISTINCT l FROM Lesson l LEFT JOIN FETCH l.section s WHERE l.course.id = :courseId ORDER BY COALESCE(s.sectionOrder, 999999), l.lessonOrder")
    List<Lesson> findByCourseIdOrderByCurriculum(@Param("courseId") Long courseId);

    List<Lesson> findByCourseIdAndPublishedTrueOrderByLessonOrderAsc(Long courseId);

    @Query("SELECT DISTINCT l FROM Lesson l LEFT JOIN FETCH l.section s WHERE l.course.id = :courseId AND l.published = true ORDER BY COALESCE(s.sectionOrder, 999999), l.lessonOrder")
    List<Lesson> findPublishedByCourseIdOrderByCurriculum(@Param("courseId") Long courseId);

    long countByCourseId(Long courseId);
}
