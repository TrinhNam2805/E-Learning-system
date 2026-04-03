package com.elearning.model.dto;

import com.elearning.model.entity.Lesson;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/** Nhóm bài học theo section (kiểu curriculum Udemy). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurriculumSectionDto {
    private Long sectionId;
    private String title;
    private int sectionOrder;
    @Builder.Default
    private List<Lesson> lessons = new ArrayList<>();
    private int sectionMinutesTotal;
}
