package com.elearning.service;

import com.elearning.model.entity.Enrollment;
import com.elearning.model.entity.User;
import com.elearning.repository.EnrollmentRepository;
import com.elearning.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Data
    public static class RankEntry {
        private int rank;
        private Long userId;
        private String fullName;
        private int totalXp;
        private int level;
        private int xpInLevel;
    }

    public List<RankEntry> getLeaderboard() {
        List<Enrollment> all = enrollmentRepository.findAll();
        Map<Long, Integer> xpMap = new HashMap<>();
        for (Enrollment e : all) {
            xpMap.merge(e.getStudent().getId(), e.getTotalXp(), Integer::sum);
        }

        List<User> students = userRepository.findByRole(User.Role.STUDENT);
        List<RankEntry> entries = new ArrayList<>();
        for (User u : students) {
            int xp = xpMap.getOrDefault(u.getId(), 0);
            RankEntry entry = new RankEntry();
            entry.setUserId(u.getId());
            entry.setFullName(u.getFullName());
            entry.setTotalXp(xp);
            entry.setLevel(xp / 100 + 1);
            entry.setXpInLevel(xp % 100);
            entries.add(entry);
        }

        entries.sort(Comparator.comparingInt(RankEntry::getTotalXp).reversed());
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }
        return entries;
    }

    public RankEntry getMyRank(Long userId) {
        return getLeaderboard().stream()
                .filter(r -> r.getUserId().equals(userId))
                .findFirst().orElse(null);
    }
}
