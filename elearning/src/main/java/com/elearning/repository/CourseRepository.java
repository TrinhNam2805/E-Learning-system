package com.elearning.repository;

import com.elearning.model.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByStatus(Course.Status status);

    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND " +
           "(LOWER(c.courseName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(c.courseCode) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%',:q,'%')))")
    List<Course> searchPublished(@Param("q") String q);

    List<Course> findByStatusOrderByIdDesc(Course.Status status);
}
