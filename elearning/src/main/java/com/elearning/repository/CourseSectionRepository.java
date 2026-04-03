package com.elearning.repository;

import com.elearning.model.entity.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {
    List<CourseSection> findByCourse_IdOrderBySectionOrderAsc(Long courseId);

    long countByCourse_Id(Long courseId);

    Optional<CourseSection> findByCourse_IdAndId(Long courseId, Long sectionId);
}
