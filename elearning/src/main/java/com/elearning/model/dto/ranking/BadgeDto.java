package com.elearning.model.dto.ranking;

import com.elearning.model.entity.UserBadge;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BadgeDto {

    private final Long id;
    private final String code;
    private final String name;
    private final String description;
    private final String icon;
    private final String criterionType;
    private final int thresholdValue;
    private final LocalDateTime earnedAt;

    public static BadgeDto fromEntity(UserBadge userBadge) {
        return BadgeDto.builder()
                .id(userBadge.getBadgeDefinition().getId())
                .code(userBadge.getBadgeDefinition().getCode())
                .name(userBadge.getBadgeDefinition().getName())
                .description(userBadge.getBadgeDefinition().getDescription())
                .icon(userBadge.getBadgeDefinition().getIcon())
                .criterionType(userBadge.getBadgeDefinition().getCriterionType().name())
                .thresholdValue(userBadge.getBadgeDefinition().getThresholdValue())
                .earnedAt(userBadge.getEarnedAt())
                .build();
    }
}
