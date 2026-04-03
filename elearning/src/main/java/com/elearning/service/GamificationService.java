package com.elearning.service;

import com.elearning.model.entity.Assignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Quy tắc XP thống nhất: bài học (trong EnrollmentService.recalcProgress) + hoạt động ở đây.
 */
@Service
@RequiredArgsConstructor
public class GamificationService {

    /** Tối đa XP một lần cho quiz (theo % điểm). */
    public static final int MAX_XP_QUIZ = 40;
    /** Tối đa XP một lần cho bài tập/kiểm tra tự luận sau khi chấm. */
    public static final int MAX_XP_ASSIGNMENT = 50;

    private final EnrollmentService enrollmentService;

    /** @return XP đã cộng (để hiển thị thông báo) */
    public int awardQuizXp(Long studentId, Long courseId, double score, double maxScore) {
        int xp = scaleXp(score, maxScore, MAX_XP_QUIZ);
        enrollmentService.addActivityXp(studentId, courseId, xp);
        return xp;
    }

    public int awardGradedAssignmentXp(Long studentId, Long courseId, double score, double maxScore,
                                       Assignment.AssignmentType type) {
        if (type == Assignment.AssignmentType.QUIZ) {
            return 0;
        }
        int xp = scaleXp(score, maxScore, MAX_XP_ASSIGNMENT);
        enrollmentService.addActivityXp(studentId, courseId, xp);
        return xp;
    }

    private static int scaleXp(double score, double maxScore, int cap) {
        if (maxScore <= 0 || score < 0) {
            return 0;
        }
        double ratio = Math.min(1.0, score / maxScore);
        int xp = (int) Math.round(ratio * cap);
        return Math.min(cap, Math.max(0, xp));
    }
}
