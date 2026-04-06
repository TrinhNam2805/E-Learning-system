package com.elearning.repository;

import com.elearning.model.entity.Assignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    @EntityGraph(attributePaths = {"course", "lesson"})
    List<Assignment> findByCourseIdOrderByDueDateAsc(Long courseId);

    @EntityGraph(attributePaths = {"course", "lesson"})
    List<Assignment> findByCourseIdAndLessonIdOrderByDueDateAsc(Long courseId, Long lessonId);

    @EntityGraph(attributePaths = {"course", "lesson"})
    @Query("SELECT a FROM Assignment a WHERE a.id = :id")
    Optional<Assignment> findDetailedById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"course", "lesson"})
    @Query("SELECT a FROM Assignment a WHERE a.course.id IN " +
           "(SELECT e.course.id FROM Enrollment e WHERE e.student.id = :studentId) " +
           "AND a.dueDate >= :now ORDER BY a.dueDate ASC")
    List<Assignment> findUpcomingForStudent(@Param("studentId") Long studentId, @Param("now") LocalDateTime now);
}
