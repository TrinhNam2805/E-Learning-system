package com.elearning.service;

import com.elearning.model.entity.Lesson;
import com.elearning.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    public List<Lesson> findByCourseId(Long courseId) {
        return lessonRepository.findByCourseIdOrderByLessonOrderAsc(courseId);
    }

    public List<Lesson> findPublishedByCourseId(Long courseId) {
        return lessonRepository.findByCourseIdAndPublishedTrueOrderByLessonOrderAsc(courseId);
    }

    public Optional<Lesson> findById(Long id) {
        return lessonRepository.findById(id);
    }

    @Transactional
    public Lesson save(Lesson lesson) {
        return lessonRepository.save(lesson);
    }

    @Transactional
    public void delete(Long id) {
        lessonRepository.deleteById(id);
    }

    public long countByCourseId(Long courseId) {
        return lessonRepository.countByCourseId(courseId);
    }
}
