package com.elearning.repository;

import com.elearning.model.entity.Submission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    @Query("SELECT s FROM Submission s " +
           "JOIN FETCH s.assignment a " +
           "JOIN FETCH a.course c " +
           "JOIN FETCH s.student st " +
           "WHERE a.id = :assignmentId AND st.id = :studentId " +
           "ORDER BY s.attemptNumber DESC, s.submittedAt DESC")
    List<Submission> findHistoryByAssignmentIdAndStudentId(@Param("assignmentId") Long assignmentId,
                                                           @Param("studentId") Long studentId);

    @Query("SELECT s FROM Submission s " +
           "JOIN FETCH s.assignment a " +
           "JOIN FETCH a.course c " +
           "JOIN FETCH s.student st " +
           "WHERE a.id = :assignmentId " +
           "ORDER BY st.fullName ASC, s.attemptNumber DESC, s.submittedAt DESC")
    List<Submission> findDetailedByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Query("SELECT s FROM Submission s " +
           "JOIN FETCH s.assignment a " +
           "JOIN FETCH a.course c " +
           "JOIN FETCH s.student st " +
           "WHERE st.id = :studentId " +
           "ORDER BY s.submittedAt DESC, s.attemptNumber DESC")
    List<Submission> findDetailedByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT s FROM Submission s " +
           "JOIN FETCH s.assignment a " +
           "JOIN FETCH a.course c " +
           "JOIN FETCH s.student st " +
           "WHERE st.id = :studentId AND c.id = :courseId " +
           "ORDER BY s.submittedAt DESC, s.attemptNumber DESC")
    List<Submission> findDetailedByStudentIdAndCourseId(@Param("studentId") Long studentId,
                                                        @Param("courseId") Long courseId);

    @Query("SELECT s FROM Submission s " +
           "JOIN FETCH s.assignment a " +
           "JOIN FETCH a.course c " +
           "JOIN FETCH s.student st " +
           "WHERE s.id = :submissionId")
    Optional<Submission> findDetailedById(@Param("submissionId") Long submissionId);

    Optional<Submission> findTopByAssignmentIdAndStudentIdOrderByAttemptNumberDescSubmittedAtDesc(Long assignmentId,
                                                                                                   Long studentId);

    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    boolean existsByAssignmentIdAndStudentIdAndScoreIsNotNull(Long assignmentId, Long studentId);
    long countByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
}
