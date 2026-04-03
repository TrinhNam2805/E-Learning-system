package com.elearning.service;

import com.elearning.model.dto.CurriculumSectionDto;
import com.elearning.model.entity.Course;
import com.elearning.model.entity.CourseSection;
import com.elearning.model.entity.Lesson;
import com.elearning.repository.CourseSectionRepository;
import com.elearning.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseSectionRepository courseSectionRepository;

    public List<Lesson> findByCourseId(Long courseId) {
        return lessonRepository.findByCourseIdOrderByCurriculum(courseId);
    }

    public List<Lesson> findPublishedByCourseId(Long courseId) {
        return lessonRepository.findPublishedByCourseIdOrderByCurriculum(courseId);
    }

    /** Gom danh sách bài (đã sắp theo section) thành các nhóm hiển thị kiểu Udemy. */
    public List<CurriculumSectionDto> buildCurriculumSections(List<Lesson> orderedLessons) {
        if (orderedLessons == null || orderedLessons.isEmpty()) {
            return Collections.emptyList();
        }
        List<CurriculumSectionDto> out = new ArrayList<>();
        CurriculumSectionDto current = null;
        for (Lesson l : orderedLessons) {
            Long sid = l.getSection() != null ? l.getSection().getId() : null;
            String title = l.getSection() != null ? l.getSection().getTitle() : "Nội dung khóa học";
            int order = l.getSection() != null ? l.getSection().getSectionOrder() : 0;
            if (current == null || !Objects.equals(current.getSectionId(), sid)) {
                current = CurriculumSectionDto.builder()
                        .sectionId(sid)
                        .title(title)
                        .sectionOrder(order)
                        .lessons(new ArrayList<>())
                        .sectionMinutesTotal(0)
                        .build();
                out.add(current);
            }
            current.getLessons().add(l);
            current.setSectionMinutesTotal(current.getSectionMinutesTotal() + l.getDurationMinutes());
        }
        return out;
    }

    /** Gán section cho bài mới: theo sectionId hợp lệ, hoặc phần đầu tiên, hoặc tạo phần mặc định. */
    @Transactional
    public CourseSection resolveSectionForLesson(Long courseId, Course course, Long sectionId) {
        if (sectionId != null) {
            Optional<CourseSection> cs = courseSectionRepository.findByCourse_IdAndId(courseId, sectionId);
            if (cs.isPresent()) {
                return cs.get();
            }
        }
        List<CourseSection> list = courseSectionRepository.findByCourse_IdOrderBySectionOrderAsc(courseId);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        CourseSection created = CourseSection.builder()
                .course(course)
                .sectionOrder(1)
                .title("Phần 1 — Nội dung khóa học")
                .build();
        return courseSectionRepository.save(created);
    }

    @Transactional
    public CourseSection addSection(Long courseId, Course course, String title) {
        int n = (int) courseSectionRepository.countByCourse_Id(courseId);
        CourseSection s = CourseSection.builder()
                .course(course)
                .sectionOrder(n + 1)
                .title(title != null && !title.trim().isEmpty() ? title.trim() : "Phần " + (n + 1))
                .build();
        return courseSectionRepository.save(s);
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
