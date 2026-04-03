package com.elearning.repository;

import com.elearning.model.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RankingSubmissionRepository extends JpaRepository<Submission, Long> {

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.student.id = :userId AND s.score IS NOT NULL")
    long countGradedSubmissions(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(*) FROM submissions s " +
                   "JOIN assignments a ON a.id = s.assignment_id " +
                   "WHERE s.student_id = :userId " +
                   "AND a.type = 'QUIZ' " +
                   "AND s.score IS NOT NULL " +
                   "AND s.score >= a.max_score",
           nativeQuery = true)
    long countPerfectQuizzes(@Param("userId") Long userId);
}
