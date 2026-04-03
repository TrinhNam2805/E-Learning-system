package com.elearning.model.entity;

import javax.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
@NamedEntityGraph(
        name = "Note.withLessonAndCourse",
        attributeNodes = {
                @NamedAttributeNode("course"),
                @NamedAttributeNode(value = "lesson", subgraph = "lesson-with-course")
        },
        subgraphs = @NamedSubgraph(
                name = "lesson-with-course",
                attributeNodes = @NamedAttributeNode("course")
        )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // nullable: standalone note không gắn bài giảng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    // nullable: standalone note không gắn khóa học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String highlightColor = "#FFFF00";

    // Tiêu đề cho standalone note
    @Column(length = 200)
    private String title;

    // Loại note: LESSON (gắn bài giảng) hoặc STANDALONE (tự do)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private NoteType noteType = NoteType.LESSON;

    /** Đoạn trích từ bài giảng (truy vết nguồn học thuật). */
    @Column(columnDefinition = "TEXT")
    private String sourceExcerpt;

    /** Nhãn phân loại, phân tách bằng dấu phẩy — ví dụ: chuẩn-hóa, ôn-tập, lab */
    @Column(length = 500)
    private String tags;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum NoteType {
        LESSON, STANDALONE
    }
}
