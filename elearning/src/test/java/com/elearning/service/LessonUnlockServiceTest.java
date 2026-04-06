package com.elearning.service;

import com.elearning.model.dto.lesson.LessonAccessDto;
import com.elearning.model.entity.Assignment;
import com.elearning.model.entity.Course;
import com.elearning.model.entity.Lesson;
import com.elearning.model.entity.Submission;
import com.elearning.repository.AssignmentRepository;
import com.elearning.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonUnlockServiceTest {

    @Mock
    private LessonService lessonService;
    @Mock
    private EnrollmentService enrollmentService;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private LessonUnlockService lessonUnlockService;

    private Course course;
    private Lesson lesson1;
    private Lesson lesson2;
    private Assignment lesson1Quiz;

    @BeforeEach
    void setUp() {
        course = Course.builder()
                .id(100L)
                .courseCode("CS201")
                .courseName("DSA")
                .build();

        lesson1 = Lesson.builder()
                .id(11L)
                .course(course)
                .lessonOrder(1)
                .lessonTitle("Big-O")
                .build();

        lesson2 = Lesson.builder()
                .id(12L)
                .course(course)
                .lessonOrder(2)
                .lessonTitle("Stack and Queue")
                .build();

        lesson1Quiz = Assignment.builder()
                .id(21L)
                .course(course)
                .lesson(lesson1)
                .title("Quiz Big-O")
                .type(Assignment.AssignmentType.QUIZ)
                .maxScore(10.0)
                .minimumPassingScore(6.0)
                .build();
    }

    @Test
    void firstLessonShouldAlwaysBeAccessible() {
        when(lessonService.findPublishedByCourseId(course.getId())).thenReturn(Arrays.asList(lesson1, lesson2));
        when(enrollmentService.isLessonCompleted(3L, lesson1.getId())).thenReturn(false);
        when(enrollmentService.isLessonCompleted(3L, lesson2.getId())).thenReturn(false);

        Map<Long, LessonAccessDto> accessMap = lessonUnlockService.buildCourseLessonAccess(course.getId(), 3L);

        assertTrue(accessMap.get(lesson1.getId()).isAccessible());
        assertFalse(accessMap.get(lesson2.getId()).isAccessible());
    }

    @Test
    void secondLessonShouldLockWhenPreviousLessonIncomplete() {
        when(lessonService.findPublishedByCourseId(course.getId())).thenReturn(Arrays.asList(lesson1, lesson2));
        when(enrollmentService.isLessonCompleted(3L, lesson1.getId())).thenReturn(false);
        when(enrollmentService.isLessonCompleted(3L, lesson2.getId())).thenReturn(false);

        Map<Long, LessonAccessDto> accessMap = lessonUnlockService.buildCourseLessonAccess(course.getId(), 3L);

        assertFalse(accessMap.get(lesson2.getId()).isAccessible());
        assertTrue(accessMap.get(lesson2.getId()).getLockedReason().contains("You need to complete the previous lesson"));
    }

    @Test
    void secondLessonShouldLockWhenAssignmentScoreBelowThreshold() {
        Submission weakSubmission = Submission.builder()
                .id(31L)
                .assignment(lesson1Quiz)
                .score(5.0)
                .build();

        when(lessonService.findPublishedByCourseId(course.getId())).thenReturn(Arrays.asList(lesson1, lesson2));
        when(enrollmentService.isLessonCompleted(3L, lesson1.getId())).thenReturn(true);
        when(enrollmentService.isLessonCompleted(3L, lesson2.getId())).thenReturn(false);
        when(assignmentRepository.findByCourseIdAndLessonIdOrderByDueDateAsc(course.getId(), lesson1.getId()))
                .thenReturn(Collections.singletonList(lesson1Quiz));
        when(submissionRepository.findHistoryByAssignmentIdAndStudentId(lesson1Quiz.getId(), 3L))
                .thenReturn(Collections.singletonList(weakSubmission));

        Map<Long, LessonAccessDto> accessMap = lessonUnlockService.buildCourseLessonAccess(course.getId(), 3L);

        assertFalse(accessMap.get(lesson2.getId()).isAccessible());
        assertTrue(accessMap.get(lesson2.getId()).getLockedReason().contains("does not meet the requirement"));
    }

    @Test
    void secondLessonShouldUnlockWhenAssignmentPassed() {
        Submission passedSubmission = Submission.builder()
                .id(32L)
                .assignment(lesson1Quiz)
                .score(8.0)
                .build();

        when(lessonService.findPublishedByCourseId(course.getId())).thenReturn(Arrays.asList(lesson1, lesson2));
        when(enrollmentService.isLessonCompleted(3L, lesson1.getId())).thenReturn(true);
        when(enrollmentService.isLessonCompleted(3L, lesson2.getId())).thenReturn(false);
        when(assignmentRepository.findByCourseIdAndLessonIdOrderByDueDateAsc(course.getId(), lesson1.getId()))
                .thenReturn(Collections.singletonList(lesson1Quiz));
        when(submissionRepository.findHistoryByAssignmentIdAndStudentId(lesson1Quiz.getId(), 3L))
                .thenReturn(Collections.singletonList(passedSubmission));

        Map<Long, LessonAccessDto> accessMap = lessonUnlockService.buildCourseLessonAccess(course.getId(), 3L);

        assertTrue(accessMap.get(lesson2.getId()).isAccessible());
    }
}