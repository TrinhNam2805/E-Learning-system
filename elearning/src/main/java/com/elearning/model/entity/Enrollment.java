package com.elearning.model.entity;

import javax.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "course_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private LocalDate enrolledAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Builder.Default
    private int progressPercentage = 0;

    /** XP từ hoạt động: quiz, bài tập được chấm (không ghi đè khi tính lại tiến độ bài học) */
    @Column(nullable = false)
    @Builder.Default
    private int activityXp = 0;

    @Builder.Default
    private int totalXp = 0;

    @PrePersist
    protected void onCreate() {
        if (enrolledAt == null) enrolledAt = LocalDate.now();
    }

    public enum Status {
        ACTIVE, COMPLETED, DROPPED
    }
}
