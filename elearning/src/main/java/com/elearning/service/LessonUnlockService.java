package com.elearning.service;

import com.elearning.model.dto.lesson.LessonAccessDto;
import com.elearning.model.entity.Assignment;
import com.elearning.model.entity.Lesson;
import com.elearning.model.entity.Submission;
import com.elearning.repository.AssignmentRepository;
import com.elearning.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LessonUnlockService {

    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;

    public Map<Long, LessonAccessDto> buildCourseLessonAccess(Long courseId, Long studentId) {
        List<Lesson> lessons = lessonService.findPublishedByCourseId(courseId);
        Map<Long, LessonAccessDto> accessMap = new LinkedHashMap<Long, LessonAccessDto>();

        for (int i = 0; i < lessons.size(); i++) {
            Lesson lesson = lessons.get(i);
            boolean completed = enrollmentService.isLessonCompleted(studentId, lesson.getId());

            if (i == 0) {
                accessMap.put(lesson.getId(), LessonAccessDto.builder()
                        .lessonId(lesson.getId())
                        .accessible(true)
                        .completed(completed)
                        .build());
                continue;
            }

            Lesson previousLesson = lessons.get(i - 1);
            if (!enrollmentService.isLessonCompleted(studentId, previousLesson.getId())) {
                accessMap.put(lesson.getId(), LessonAccessDto.builder()
                        .lessonId(lesson.getId())
                        .accessible(false)
                        .completed(completed)
                        .lockedReason("You need to complete the previous lesson: " + previousLesson.getLessonTitle() + ".")
                        .prerequisiteLessonTitle(previousLesson.getLessonTitle())
                        .build());
                continue;
            }

            LessonAccessDto blockingAccess = findBlockingAssignment(courseId, previousLesson, lesson.getId(), studentId, completed);
            if (blockingAccess != null) {
                accessMap.put(lesson.getId(), blockingAccess);
                continue;
            }

            accessMap.put(lesson.getId(), LessonAccessDto.builder()
                    .lessonId(lesson.getId())
                    .accessible(true)
                    .completed(completed)
                    .prerequisiteLessonTitle(previousLesson.getLessonTitle())
                    .build());
        }

        return accessMap;
    }

    private LessonAccessDto findBlockingAssignment(Long courseId,
                                                   Lesson previousLesson,
                                                   Long currentLessonId,
                                                   Long studentId,
                                                   boolean completed) {
        List<Assignment> linkedAssignments = assignmentRepository
                .findByCourseIdAndLessonIdOrderByDueDateAsc(courseId, previousLesson.getId());

        for (Assignment assignment : linkedAssignments) {
            if (assignment.getType() != Assignment.AssignmentType.QUIZ) {
                continue;
            }
            Double requiredScore = assignment.getMinimumPassingScore();
            if (requiredScore == null) {
                continue;
            }

            List<Submission> submissionHistory = submissionRepository
                    .findHistoryByAssignmentIdAndStudentId(assignment.getId(), studentId);

            boolean hasPassingScore = false;
            boolean hasSubmission = !submissionHistory.isEmpty();
            boolean waitingForGrading = false;
            double bestScore = -1;

            for (Submission submission : submissionHistory) {
                if (submission.getScore() == null) {
                    waitingForGrading = true;
                    continue;
                }
                bestScore = Math.max(bestScore, submission.getScore());
                if (submission.getScore() >= requiredScore) {
                    hasPassingScore = true;
                    break;
                }
            }

            if (hasPassingScore) {
                continue;
            }

            String lockedReason;
            if (!hasSubmission) {
                lockedReason = "You need to complete assignment \"" + assignment.getTitle()
                        + "\" from the previous lesson and score at least " + formatScore(requiredScore) + " points.";
            } else if (waitingForGrading && bestScore < 0) {
                lockedReason = "Assignment \"" + assignment.getTitle()
                        + "\" is awaiting grading. You need at least " + formatScore(requiredScore) + " points to unlock the next lesson.";
            } else {
                lockedReason = "Assignment \"" + assignment.getTitle()
                        + "\" does not meet the requirement. Minimum required: " + formatScore(requiredScore) + " points.";
            }

            return LessonAccessDto.builder()
                    .lessonId(currentLessonId)
                    .accessible(false)
                    .completed(completed)
                    .lockedReason(lockedReason)
                    .prerequisiteLessonTitle(previousLesson.getLessonTitle())
                    .blockingAssignmentTitle(assignment.getTitle())
                    .requiredScore(requiredScore)
                    .build();
        }

        return null;
    }

    private String formatScore(Double score) {
        if (score == null) {
            return "0";
        }
        if (score == Math.floor(score)) {
            return String.valueOf(score.intValue());
        }
        return String.format(java.util.Locale.US, "%.1f", score);
    }
}
