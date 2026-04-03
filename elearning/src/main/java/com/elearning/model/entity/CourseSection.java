package com.elearning.model.entity;

import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "section_order", nullable = false)
    private int sectionOrder;

    @Column(nullable = false, length = 200)
    private String title;
}
