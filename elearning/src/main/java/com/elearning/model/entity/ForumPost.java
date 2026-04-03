package com.elearning.model.entity;

import javax.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "forum_posts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ForumPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Null = diễn đàn chung toàn hệ thống (kênh). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    /** Tuỳ chọn: bài đăng gắn một bài học cụ thể (thường kèm course). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private PostType postType = PostType.DISCUSSION;

    @Builder.Default
    private int viewCount = 0;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<Comment> comments;

    public enum PostType {
        QUESTION, DISCUSSION, ANNOUNCEMENT
    }
}
