package com.elearning.model.dto.ranking;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LeaderboardEntryDto {

    private final int rank;
    private final Long userId;
    private final String fullName;
    private final int totalPoints;
    private final int totalXp;
    private final int level;
    private final int xpInLevel;
    private final long completedLessons;
    private final long gradedSubmissions;
}
