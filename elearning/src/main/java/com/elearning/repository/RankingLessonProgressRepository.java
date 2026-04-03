package com.elearning.repository;

import com.elearning.model.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankingLessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    long countByStudentIdAndCompletedTrue(Long studentId);
}
