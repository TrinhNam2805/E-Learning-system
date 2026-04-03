package com.elearning.service;

import com.elearning.model.dto.ranking.LeaderboardEntryDto;
import com.elearning.model.entity.Course;
import com.elearning.model.entity.Enrollment;
import com.elearning.model.entity.User;
import com.elearning.repository.RankingEnrollmentRepository;
import com.elearning.repository.RankingLessonProgressRepository;
import com.elearning.repository.RankingSubmissionRepository;
import com.elearning.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private RankingEnrollmentRepository rankingEnrollmentRepository;
    @Mock
    private RankingLessonProgressRepository rankingLessonProgressRepository;
    @Mock
    private RankingSubmissionRepository rankingSubmissionRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RankingService rankingService;

    private User studentA;
    private User studentB;
    private Enrollment enrollmentA;
    private Enrollment enrollmentB;

    @BeforeEach
    void setUp() {
        studentA = User.builder()
                .id(1L)
                .fullName("Nguyễn Văn A")
                .role(User.Role.STUDENT)
                .build();

        studentB = User.builder()
                .id(2L)
                .fullName("Trần Thị B")
                .role(User.Role.STUDENT)
                .build();

        Course course = Course.builder()
                .id(10L)
                .courseCode("CS201")
                .build();

        enrollmentA = Enrollment.builder()
                .id(100L)
                .student(studentA)
                .course(course)
                .totalXp(180)
                .build();

        enrollmentB = Enrollment.builder()
                .id(101L)
                .student(studentB)
                .course(course)
                .totalXp(120)
                .build();
    }

    @Test
    void leaderboardShouldReturnRankedData() {
        when(rankingEnrollmentRepository.findAll()).thenReturn(Arrays.asList(enrollmentA, enrollmentB));
        when(userRepository.findByRole(User.Role.STUDENT)).thenReturn(Arrays.asList(studentA, studentB));
        when(rankingLessonProgressRepository.countByStudentIdAndCompletedTrue(1L)).thenReturn(5L);
        when(rankingLessonProgressRepository.countByStudentIdAndCompletedTrue(2L)).thenReturn(3L);
        when(rankingSubmissionRepository.countGradedSubmissions(1L)).thenReturn(4L);
        when(rankingSubmissionRepository.countGradedSubmissions(2L)).thenReturn(2L);

        List<LeaderboardEntryDto> leaderboard = rankingService.getLeaderboard();

        assertEquals(2, leaderboard.size());
        assertEquals(1, leaderboard.get(0).getRank());
        assertEquals(studentA.getId(), leaderboard.get(0).getUserId());
        assertEquals(180, leaderboard.get(0).getTotalPoints());
        assertEquals(2, leaderboard.get(1).getRank());
    }

    @Test
    void leaderboardShouldBeEmptyWhenNoStudentData() {
        when(rankingEnrollmentRepository.findAll()).thenReturn(Collections.<Enrollment>emptyList());
        when(userRepository.findByRole(User.Role.STUDENT)).thenReturn(Collections.<User>emptyList());

        List<LeaderboardEntryDto> leaderboard = rankingService.getLeaderboard();

        assertTrue(leaderboard.isEmpty());
    }

    @Test
    void shouldReturnCurrentUserRank() {
        when(rankingEnrollmentRepository.findAll()).thenReturn(Arrays.asList(enrollmentA, enrollmentB));
        when(userRepository.findByRole(User.Role.STUDENT)).thenReturn(Arrays.asList(studentA, studentB));
        when(rankingLessonProgressRepository.countByStudentIdAndCompletedTrue(1L)).thenReturn(5L);
        when(rankingLessonProgressRepository.countByStudentIdAndCompletedTrue(2L)).thenReturn(3L);
        when(rankingSubmissionRepository.countGradedSubmissions(1L)).thenReturn(4L);
        when(rankingSubmissionRepository.countGradedSubmissions(2L)).thenReturn(2L);

        LeaderboardEntryDto currentRank = rankingService.getMyRank(2L);

        assertNotNull(currentRank);
        assertEquals(2, currentRank.getRank());
        assertEquals(120, currentRank.getTotalXp());
    }

    @Test
    void leaderboardShouldReflectUpdatedPointsAfterSubmission() {
        Enrollment updatedEnrollmentB = Enrollment.builder()
                .id(101L)
                .student(studentB)
                .course(enrollmentB.getCourse())
                .totalXp(260)
                .build();

        when(rankingEnrollmentRepository.findAll())
                .thenReturn(Arrays.asList(enrollmentA, enrollmentB))
                .thenReturn(Arrays.asList(enrollmentA, updatedEnrollmentB));
        when(userRepository.findByRole(User.Role.STUDENT)).thenReturn(Arrays.asList(studentA, studentB));
        when(rankingLessonProgressRepository.countByStudentIdAndCompletedTrue(1L)).thenReturn(5L);
        when(rankingLessonProgressRepository.countByStudentIdAndCompletedTrue(2L)).thenReturn(3L);
        when(rankingSubmissionRepository.countGradedSubmissions(1L)).thenReturn(4L);
        when(rankingSubmissionRepository.countGradedSubmissions(2L)).thenReturn(2L);

        List<LeaderboardEntryDto> beforeUpdate = rankingService.getLeaderboard();
        List<LeaderboardEntryDto> afterUpdate = rankingService.getLeaderboard();

        assertEquals(studentA.getId(), beforeUpdate.get(0).getUserId());
        assertEquals(studentB.getId(), afterUpdate.get(0).getUserId());
        assertEquals(260, afterUpdate.get(0).getTotalPoints());
    }
}
