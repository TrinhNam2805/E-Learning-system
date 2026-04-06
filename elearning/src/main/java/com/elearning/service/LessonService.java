package com.elearning.service;

import com.elearning.model.dto.CurriculumSectionDto;
import com.elearning.model.entity.Lesson;
import com.elearning.repository.LessonRepository;
import com.elearning.util.LessonContentHtmlSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
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

    public List<CurriculumSectionDto> buildCurriculumSections(List<Lesson> orderedLessons) {
        if (orderedLessons == null || orderedLessons.isEmpty()) {
            return Collections.emptyList();
        }

        List<Lesson> lessons = new ArrayList<>(orderedLessons);
        CurriculumSectionDto section = CurriculumSectionDto.builder()
                .sectionId(null)
                .title("Course content")
                .sectionOrder(1)
                .lessons(lessons)
                .sectionMinutesTotal(lessons.stream().mapToInt(Lesson::getDurationMinutes).sum())
                .build();
        return Collections.singletonList(section);
    }

    public Optional<Lesson> findById(Long id) {
        return lessonRepository.findById(id);
    }

    public Optional<Lesson> findNextPublishedLesson(Long courseId, Long currentLessonId) {
        if (courseId == null || currentLessonId == null) {
            return Optional.empty();
        }

        List<Lesson> orderedLessons = findPublishedByCourseId(courseId);
        for (int i = 0; i < orderedLessons.size(); i++) {
            Lesson lesson = orderedLessons.get(i);
            if (!currentLessonId.equals(lesson.getId())) {
                continue;
            }
            if (i + 1 < orderedLessons.size()) {
                return Optional.of(orderedLessons.get(i + 1));
            }
            break;
        }

        return Optional.empty();
    }

    @Transactional
    public Lesson save(Lesson lesson) {
        if (lesson != null) {
            lesson.setLessonContent(LessonContentHtmlSanitizer.sanitizeForStorage(lesson.getLessonContent()));
        }
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
