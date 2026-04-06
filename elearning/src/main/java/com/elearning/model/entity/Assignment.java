package com.elearning.model.entity;

import javax.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private AssignmentType type = AssignmentType.HOMEWORK;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "max_score")
    @Builder.Default
    private double maxScore = 10.0;

    @Column(name = "minimum_passing_score")
    private Double minimumPassingScore;

    @Column(name = "allow_late_submission", nullable = false)
    @Builder.Default
    private boolean allowLateSubmission = false;

    @Column(name = "max_attempts", nullable = false)
    @Builder.Default
    private int maxAttempts = 1;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Submission> submissions;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuizQuestion> questions;

    public enum AssignmentType {
        HOMEWORK, QUIZ, EXAM
    }
}
