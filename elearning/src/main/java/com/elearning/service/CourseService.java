package com.elearning.service;

import com.elearning.model.entity.Course;
import com.elearning.model.entity.User;
import com.elearning.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public List<Course> findAllPublished() {
        return courseRepository.findByStatusOrderByIdDesc(Course.Status.PUBLISHED);
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    public List<Course> findByTeacher(User teacher) {
        return courseRepository.findByTeacher(teacher);
    }

    public List<Course> search(String q) {
        if (q == null || q.trim().isEmpty()) return findAllPublished();
        return courseRepository.searchPublished(q);
    }

    @Transactional
    public Course save(Course course) {
        return courseRepository.save(course);
    }

    @Transactional
    public void delete(Long id) {
        courseRepository.deleteById(id);
    }

    public long countAll() {
        return courseRepository.count();
    }

    public long countPublished() {
        return courseRepository.findByStatus(Course.Status.PUBLISHED).size();
    }
}
