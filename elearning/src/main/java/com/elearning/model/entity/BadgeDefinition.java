package com.elearning.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "badge_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, length = 80)
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(name = "criterion_type", nullable = false, length = 40)
    private CriterionType criterionType;

    @Column(name = "threshold_value", nullable = false)
    private int thresholdValue;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 1;

    public enum CriterionType {
        TOTAL_XP,
        COMPLETED_LESSONS,
        GRADED_SUBMISSIONS,
        PERFECT_QUIZZES,
        COMPLETED_COURSES
    }
}
