package com.elearning.model.entity;

import javax.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(
        name = "courses",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_courses_offering",
                    columnNames = {"course_code", "semester", "academic_year"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã học phần theo CTĐT; duy nhất trong cặp (học kỳ, năm học) */
    @Column(name = "course_code", nullable = false, length = 20)
    private String courseCode;

    @Column(nullable = false, length = 200)
    private String courseName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String enrollPassword;

    @Column(name = "semester", length = 20)
    private String semester;

    @Column(name = "academic_year", length = 10)
    private String academicYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.DRAFT;

    @Builder.Default
    private int maxStudents = 50;

    @Column(length = 255)
    private String thumbnail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false)
    @Builder.Default
    private int credits = 3;

    @Column(nullable = false)
    @Builder.Default
    private int theoryHours = 30;

    @Column(nullable = false)
    @Builder.Default
    private int practiceHours = 15;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("lessonOrder ASC")
    private List<Lesson> lessons;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sectionOrder ASC")
    private List<CourseSection> courseSections;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Enrollment> enrollments;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ForumPost> forumPosts;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Assignment> assignments;

    public enum Status {
        DRAFT, PUBLISHED, ARCHIVED
    }
}
