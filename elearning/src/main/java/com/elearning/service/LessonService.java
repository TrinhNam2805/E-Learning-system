package com.elearning.service;

import com.elearning.model.dto.CurriculumSectionDto;
import com.elearning.model.entity.Lesson;
import com.elearning.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.elearning.util.LessonContentOutlineExtractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Short preview lines per lesson (from HTML headings, bullets, or first paragraph).
     */
    public Map<Long, List<String>> buildLessonOutlinePreviews(List<Lesson> lessons) {
        Map<Long, List<String>> map = new LinkedHashMap<>();
        if (lessons == null) {
            return map;
        }
        for (Lesson lesson : lessons) {
            map.put(lesson.getId(), LessonContentOutlineExtractor.extractLines(lesson.getLessonContent()));
        }
        return map;
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
