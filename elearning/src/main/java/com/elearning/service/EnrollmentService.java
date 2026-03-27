package com.elearning.service;

import com.elearning.model.entity.*;
import com.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository progressRepository;

    public boolean isEnrolled(Long studentId, Long courseId) {
        return enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    public Optional<Enrollment> findByStudentAndCourse(Long studentId, Long courseId) {
        return enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);
    }

    public List<Enrollment> findByStudentId(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    public List<Enrollment> findByCourseId(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    @Transactional
    public Enrollment enroll(User student, Course course) {
        if (isEnrolled(student.getId(), course.getId())) {
            throw new IllegalStateException("You are already enrolled in this course.");
        }
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .build();
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void markLessonComplete(User student, Lesson lesson) {
        LessonProgress progress = progressRepository
                .findByStudentIdAndLessonId(student.getId(), lesson.getId())
                .orElse(LessonProgress.builder().student(student).lesson(lesson).build());

        if (!progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(java.time.LocalDate.now());
            progressRepository.save(progress);
            recalcProgress(student.getId(), lesson.getCourse().getId());
        }
    }

    @Transactional
    public void recalcProgress(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndCourseId(studentId, courseId).orElse(null);
        if (enrollment == null) return;

        long total = lessonRepository.countByCourseId(courseId);
        long completed = progressRepository.countCompletedByStudentAndCourse(studentId, courseId);

        int pct = total > 0 ? (int) Math.round((completed * 100.0) / total) : 0;
        enrollment.setProgressPercentage(pct);
        enrollment.setTotalXp((int) completed * 20);
        enrollmentRepository.save(enrollment);
    }

    public boolean isLessonCompleted(Long studentId, Long lessonId) {
        return progressRepository.existsByStudentIdAndLessonIdAndCompletedTrue(studentId, lessonId);
    }

    public long countEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.countByCourseId(courseId);
    }

    public long countAll() {
        return enrollmentRepository.count();
    }
}
