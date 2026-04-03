package com.elearning.repository;

import com.elearning.model.entity.UserBadge;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    @EntityGraph(attributePaths = {"badgeDefinition"})
    List<UserBadge> findByUserIdOrderByEarnedAtAsc(Long userId);

    boolean existsByUserIdAndBadgeDefinitionId(Long userId, Long badgeDefinitionId);

    long countByUserId(Long userId);
}
