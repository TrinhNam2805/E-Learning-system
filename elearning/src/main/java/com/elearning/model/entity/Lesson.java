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

    @Column(name = "lesson_order", nullable = false)
    private int lessonOrder;

    @Column(name = "lesson_title", nullable = false, length = 200)
    private String lessonTitle;

    @Column(name = "lesson_content", columnDefinition = "LONGTEXT")
    private String lessonContent;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "duration_minutes")
    @Builder.Default
    private int durationMinutes = 45;

    @Builder.Default
    private boolean published = true;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LessonProgress> progressList;

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY)
    private List<Assignment> assignments;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Note> notes;
}
