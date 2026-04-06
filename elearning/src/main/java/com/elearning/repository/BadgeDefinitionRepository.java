package com.elearning.repository;

import com.elearning.model.entity.BadgeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BadgeDefinitionRepository extends JpaRepository<BadgeDefinition, Long> {
    List<BadgeDefinition> findByActiveTrueOrderByDisplayOrderAscIdAsc();
    Optional<BadgeDefinition> findByCode(String code);
}
