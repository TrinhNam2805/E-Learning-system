package com.elearning.repository;

import com.elearning.model.entity.Enrollment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RankingEnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @EntityGraph(attributePaths = {"student"})
    List<Enrollment> findAll();

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :userId AND e.progressPercentage >= 100")
    long countCompletedCourses(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(e.totalXp), 0) FROM Enrollment e WHERE e.student.id = :userId")
    Integer sumTotalXpByStudentId(@Param("userId") Long userId);
}
