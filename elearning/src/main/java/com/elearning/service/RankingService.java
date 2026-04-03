package com.elearning.service;

import com.elearning.model.dto.ranking.LeaderboardEntryDto;
import com.elearning.model.entity.Enrollment;
import com.elearning.model.entity.User;
import com.elearning.repository.RankingEnrollmentRepository;
import com.elearning.repository.RankingLessonProgressRepository;
import com.elearning.repository.RankingSubmissionRepository;
import com.elearning.repository.UserRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingEnrollmentRepository rankingEnrollmentRepository;
    private final RankingLessonProgressRepository rankingLessonProgressRepository;
    private final RankingSubmissionRepository rankingSubmissionRepository;
    private final UserRepository userRepository;

    public List<LeaderboardEntryDto> getLeaderboard() {
        List<Enrollment> allEnrollments = rankingEnrollmentRepository.findAll();
        Map<Long, StudentRankingSnapshot> snapshotMap = new HashMap<Long, StudentRankingSnapshot>();

        for (Enrollment enrollment : allEnrollments) {
            if (enrollment.getStudent() == null || enrollment.getStudent().getRole() != User.Role.STUDENT) {
                continue;
            }
            Long studentId = enrollment.getStudent().getId();
            StudentRankingSnapshot snapshot = snapshotMap.get(studentId);
            if (snapshot == null) {
                snapshot = buildStudentRankingSnapshot(enrollment.getStudent());
                snapshotMap.put(studentId, snapshot);
            }
            snapshot.totalXp += enrollment.getTotalXp();
            snapshot.totalPoints += enrollment.getTotalXp();
        }

        List<User> students = userRepository.findByRole(User.Role.STUDENT);
        List<StudentRankingSnapshot> snapshots = new ArrayList<StudentRankingSnapshot>();
        for (User student : students) {
            StudentRankingSnapshot snapshot = snapshotMap.get(student.getId());
            if (snapshot == null) {
                snapshot = buildStudentRankingSnapshot(student);
            }
            snapshots.add(snapshot);
        }

        snapshots.sort(Comparator
                .comparingInt(StudentRankingSnapshot::getTotalPoints).reversed()
                .thenComparing(Comparator.comparingLong(StudentRankingSnapshot::getCompletedLessons).reversed())
                .thenComparing(Comparator.comparingLong(StudentRankingSnapshot::getGradedSubmissions).reversed())
                .thenComparing(StudentRankingSnapshot::getFullName, String.CASE_INSENSITIVE_ORDER)
                .thenComparingLong(StudentRankingSnapshot::getUserId));

        List<LeaderboardEntryDto> leaderboard = new ArrayList<LeaderboardEntryDto>();
        for (int i = 0; i < snapshots.size(); i++) {
            StudentRankingSnapshot snapshot = snapshots.get(i);
            leaderboard.add(LeaderboardEntryDto.builder()
                    .rank(i + 1)
                    .userId(snapshot.userId)
                    .fullName(snapshot.fullName)
                    .totalPoints(snapshot.totalPoints)
                    .totalXp(snapshot.totalXp)
                    .level(snapshot.totalXp / 100 + 1)
                    .xpInLevel(snapshot.totalXp % 100)
                    .completedLessons(snapshot.completedLessons)
                    .gradedSubmissions(snapshot.gradedSubmissions)
                    .build());
        }
        return leaderboard;
    }

    public LeaderboardEntryDto getMyRank(Long userId) {
        for (LeaderboardEntryDto entry : getLeaderboard()) {
            if (entry.getUserId().equals(userId)) {
                return entry;
            }
        }
        return null;
    }

    private StudentRankingSnapshot buildStudentRankingSnapshot(User student) {
        long completedLessons = rankingLessonProgressRepository.countByStudentIdAndCompletedTrue(student.getId());
        long gradedSubmissions = rankingSubmissionRepository.countGradedSubmissions(student.getId());

        return StudentRankingSnapshot.builder()
                .userId(student.getId())
                .fullName(normalizeFullName(student.getFullName()))
                .completedLessons(completedLessons)
                .gradedSubmissions(gradedSubmissions)
                .build();
    }

    private String normalizeFullName(String fullName) {
        return fullName == null ? "" : fullName.trim();
    }

    @Getter
    @Builder
    private static class StudentRankingSnapshot {
        private final Long userId;
        private final String fullName;
        private final long completedLessons;
        private final long gradedSubmissions;
        private int totalXp;
        private int totalPoints;
    }
}
