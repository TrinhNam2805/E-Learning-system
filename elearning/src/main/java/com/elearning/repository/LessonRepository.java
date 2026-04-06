package com.elearning.repository;

import com.elearning.model.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseIdOrderByLessonOrderAsc(Long courseId);

    List<Lesson> findByCourseIdAndPublishedTrueOrderByLessonOrderAsc(Long courseId);

    long countByCourseId(Long courseId);
}
