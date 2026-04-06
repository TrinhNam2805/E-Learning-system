package com.elearning.model.dto.ranking;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BadgeCollectionDto {

    private final Long userId;
    private final int earnedBadgeCount;
    private final List<BadgeDto> badges;
}
