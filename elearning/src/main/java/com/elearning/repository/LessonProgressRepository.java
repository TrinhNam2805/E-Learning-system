package com.elearning.repository;

import com.elearning.model.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByStudentIdAndLessonId(Long studentId, Long lessonId);
    boolean existsByStudentIdAndLessonIdAndCompletedTrue(Long studentId, Long lessonId);
    List<LessonProgress> findByStudentId(Long studentId);

    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.student.id = :studentId AND lp.completed = true")
    long countCompletedByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.student.id = :studentId " +
           "AND lp.lesson.course.id = :courseId AND lp.completed = true")
    long countCompletedByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
