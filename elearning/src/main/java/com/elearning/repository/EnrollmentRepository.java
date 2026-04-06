package com.elearning.repository;

import com.elearning.model.entity.Enrollment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    /** Load course eagerly for student-facing views while open-in-view is disabled. */
    @EntityGraph(attributePaths = {"course"})
    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findByCourseId(Long courseId);
    long countByCourseId(Long courseId);

    @Query("SELECT SUM(e.totalXp) FROM Enrollment e WHERE e.student.id = :studentId")
    Integer sumXpByStudentId(@Param("studentId") Long studentId);
}

