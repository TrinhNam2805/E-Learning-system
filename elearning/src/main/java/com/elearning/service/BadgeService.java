package com.elearning.service;

import com.elearning.model.dto.ranking.BadgeCollectionDto;
import com.elearning.model.dto.ranking.BadgeDto;
import com.elearning.model.entity.BadgeDefinition;
import com.elearning.model.entity.User;
import com.elearning.model.entity.UserBadge;
import com.elearning.repository.BadgeDefinitionRepository;
import com.elearning.repository.RankingEnrollmentRepository;
import com.elearning.repository.RankingLessonProgressRepository;
import com.elearning.repository.RankingSubmissionRepository;
import com.elearning.repository.UserBadgeRepository;
import com.elearning.repository.UserRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeDefinitionRepository badgeDefinitionRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final RankingEnrollmentRepository rankingEnrollmentRepository;
    private final RankingLessonProgressRepository rankingLessonProgressRepository;
    private final RankingSubmissionRepository rankingSubmissionRepository;
    private final UserRepository userRepository;

    @Transactional
    public BadgeCollectionDto getEarnedBadges(Long userId) {
        synchronizeBadges(userId);
        List<UserBadge> earnedBadges = userBadgeRepository.findByUserIdOrderByEarnedAtAsc(userId);
        List<BadgeDto> badges = new ArrayList<BadgeDto>();
        for (UserBadge earnedBadge : earnedBadges) {
            badges.add(BadgeDto.fromEntity(earnedBadge));
        }
        return BadgeCollectionDto.builder()
                .userId(userId)
                .earnedBadgeCount(badges.size())
                .badges(badges)
                .build();
    }

    @Transactional
    public void synchronizeBadges(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRole() != User.Role.STUDENT) {
            return;
        }

        BadgeMetrics metrics = computeMetrics(userId);
        List<BadgeDefinition> definitions = badgeDefinitionRepository.findByActiveTrueOrderByDisplayOrderAscIdAsc();
        for (BadgeDefinition definition : definitions) {
            if (userBadgeRepository.existsByUserIdAndBadgeDefinitionId(userId, definition.getId())) {
                continue;
            }
            if (!qualifies(definition, metrics)) {
                continue;
            }
            userBadgeRepository.save(UserBadge.builder()
                    .user(user)
                    .badgeDefinition(definition)
                    .build());
        }
    }

    private boolean qualifies(BadgeDefinition definition, BadgeMetrics metrics) {
        switch (definition.getCriterionType()) {
            case TOTAL_XP:
                return metrics.totalXp >= definition.getThresholdValue();
            case COMPLETED_LESSONS:
                return metrics.completedLessons >= definition.getThresholdValue();
            case GRADED_SUBMISSIONS:
                return metrics.gradedSubmissions >= definition.getThresholdValue();
            case PERFECT_QUIZZES:
                return metrics.perfectQuizzes >= definition.getThresholdValue();
            case COMPLETED_COURSES:
                return metrics.completedCourses >= definition.getThresholdValue();
            default:
                return false;
        }
    }

    private BadgeMetrics computeMetrics(Long userId) {
        Integer totalXpValue = rankingEnrollmentRepository.sumTotalXpByStudentId(userId);
        int totalXp = totalXpValue == null ? 0 : totalXpValue;
        long completedLessons = rankingLessonProgressRepository.countByStudentIdAndCompletedTrue(userId);
        long gradedSubmissions = rankingSubmissionRepository.countGradedSubmissions(userId);
        long perfectQuizzes = rankingSubmissionRepository.countPerfectQuizzes(userId);
        long completedCourses = rankingEnrollmentRepository.countCompletedCourses(userId);

        return BadgeMetrics.builder()
                .totalXp(totalXp)
                .completedLessons(completedLessons)
                .gradedSubmissions(gradedSubmissions)
                .perfectQuizzes(perfectQuizzes)
                .completedCourses(completedCourses)
                .build();
    }

    @Getter
    @Builder
    private static class BadgeMetrics {
        private final int totalXp;
        private final long completedLessons;
        private final long gradedSubmissions;
        private final long perfectQuizzes;
        private final long completedCourses;
    }
}
