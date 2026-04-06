package com.elearning.model.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Liên kết có hướng giữa hai ghi chú (đồ thị tri thức cá nhân — dễ mở rộng sau).
 */
@Entity
@Table(
        name = "note_links",
        uniqueConstraints = @UniqueConstraint(columnNames = {"from_note_id", "to_note_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_note_id", nullable = false)
    private Note fromNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_note_id", nullable = false)
    private Note toNote;

    @Column(length = 200)
    private String relationLabel;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
