package com.elearning.model.entity;

import javax.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "lessons")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private int lessonOrder;

    @Column(nullable = false, length = 200)
    private String lessonTitle;

    @Column(columnDefinition = "LONGTEXT")
    private String lessonContent;

    @Column(length = 500)
    private String videoUrl;

    @Builder.Default
    private int durationMinutes = 45;

    @Builder.Default
    private boolean published = true;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LessonProgress> progressList;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Note> notes;
}
