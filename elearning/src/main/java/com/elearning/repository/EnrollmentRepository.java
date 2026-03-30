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
    /** course + teacher: view needs teacher.fullName; open-in-view=false → must fetch in graph */
    @EntityGraph(attributePaths = {"course", "course.teacher"})
    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findByCourseId(Long courseId);
    long countByCourseId(Long courseId);

    @Query("SELECT e FROM Enrollment e WHERE e.course.teacher.id = :teacherId")
    List<Enrollment> findByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT SUM(e.totalXp) FROM Enrollment e WHERE e.student.id = :studentId")
    Integer sumXpByStudentId(@Param("studentId") Long studentId);
}
