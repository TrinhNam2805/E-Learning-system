package com.elearning.repository;

import com.elearning.model.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByAssignmentIdOrderByQuestionOrderAsc(Long assignmentId);
    long countByAssignmentId(Long assignmentId);
}
