package com.elearning.service;

import com.elearning.model.entity.BadgeDefinition;
import com.elearning.model.entity.User;
import com.elearning.repository.BadgeDefinitionRepository;
import com.elearning.repository.RankingEnrollmentRepository;
import com.elearning.repository.RankingLessonProgressRepository;
import com.elearning.repository.RankingSubmissionRepository;
import com.elearning.repository.UserBadgeRepository;
import com.elearning.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock
    private BadgeDefinitionRepository badgeDefinitionRepository;
    @Mock
    private UserBadgeRepository userBadgeRepository;
    @Mock
    private RankingEnrollmentRepository rankingEnrollmentRepository;
    @Mock
    private RankingLessonProgressRepository rankingLessonProgressRepository;
    @Mock
    private RankingSubmissionRepository rankingSubmissionRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BadgeService badgeService;

    @Test
    void shouldAwardBadgeWhenUserMeetsCriteria() {
        User student = User.builder()
                .id(1L)
                .role(User.Role.STUDENT)
                .fullName("Nguyễn Văn A")
                .build();

        BadgeDefinition badge = BadgeDefinition.builder()
                .id(10L)
                .code("XP_BRONZE")
                .name("XP Đồng")
                .criterionType(BadgeDefinition.CriterionType.TOTAL_XP)
                .thresholdValue(100)
                .active(true)
                .displayOrder(1)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(badgeDefinitionRepository.findByActiveTrueOrderByDisplayOrderAscIdAsc()).thenReturn(Collections.singletonList(badge));
        when(userBadgeRepository.existsByUserIdAndBadgeDefinitionId(1L, 10L)).thenReturn(false);
        when(rankingEnrollmentRepository.sumTotalXpByStudentId(1L)).thenReturn(150);
        when(rankingLessonProgressRepository.countByStudentIdAndCompletedTrue(1L)).thenReturn(0L);
        when(rankingSubmissionRepository.countGradedSubmissions(1L)).thenReturn(0L);
        when(rankingSubmissionRepository.countPerfectQuizzes(1L)).thenReturn(0L);
        when(rankingEnrollmentRepository.countCompletedCourses(1L)).thenReturn(0L);

        badgeService.synchronizeBadges(1L);

        verify(userBadgeRepository).save(any());
    }

    @Test
    void shouldNotAwardBadgeWhenUserDoesNotMeetCriteria() {
        User student = User.builder()
                .id(1L)
                .role(User.Role.STUDENT)
                .fullName("Nguyễn Văn A")
                .build();

        BadgeDefinition badge = BadgeDefinition.builder()
                .id(11L)
                .code("COURSE_FINISHER")
                .name("Hoàn tất học phần")
                .criterionType(BadgeDefinition.CriterionType.COMPLETED_COURSES)
                .thresholdValue(1)
                .active(true)
                .displayOrder(1)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(badgeDefinitionRepository.findByActiveTrueOrderByDisplayOrderAscIdAsc()).thenReturn(Collections.singletonList(badge));
        when(userBadgeRepository.existsByUserIdAndBadgeDefinitionId(1L, 11L)).thenReturn(false);
        when(rankingEnrollmentRepository.sumTotalXpByStudentId(1L)).thenReturn(80);
        when(rankingLessonProgressRepository.countByStudentIdAndCompletedTrue(1L)).thenReturn(2L);
        when(rankingSubmissionRepository.countGradedSubmissions(1L)).thenReturn(1L);
        when(rankingSubmissionRepository.countPerfectQuizzes(1L)).thenReturn(0L);
        when(rankingEnrollmentRepository.countCompletedCourses(1L)).thenReturn(0L);

        badgeService.synchronizeBadges(1L);

        verify(userBadgeRepository, never()).save(any());
    }
}
