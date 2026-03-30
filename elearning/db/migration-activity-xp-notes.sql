-- Chạy trên DB đã tồn tại (bổ sung activity_xp, ghi chú nâng cao, note_links).
-- mysql -u root -p e-learning < elearning/db/migration-activity-xp-notes.sql

ALTER TABLE `enrollments`
  ADD COLUMN `activity_xp` INT NOT NULL DEFAULT 0 COMMENT 'XP từ quiz, bài được chấm' AFTER `progress_percentage`;

UPDATE `enrollments` SET `activity_xp` = 0 WHERE `activity_xp` IS NULL;

-- Đồng bộ total_xp = lesson portion + activity (lesson = progress từ bài đã học — xấp xỉ từ total_xp cũ nếu chưa có activity)
-- Giữ nguyên total_xp hiện có nếu đã có dữ liệu; hoặc chạy script tùy chỉnh.

ALTER TABLE `notes`
  ADD COLUMN `source_excerpt` TEXT NULL COMMENT 'Trích dẫn từ bài giảng' AFTER `note_type`,
  ADD COLUMN `tags` VARCHAR(500) NULL COMMENT 'Nhãn, phân tách bằng dấu phẩy' AFTER `source_excerpt`;

CREATE TABLE IF NOT EXISTS `note_links` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `from_note_id` BIGINT NOT NULL,
  `to_note_id` BIGINT NOT NULL,
  `relation_label` VARCHAR(200) DEFAULT NULL,
  `created_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_note_link` (`from_note_id`, `to_note_id`),
  KEY `idx_note_links_to` (`to_note_id`),
  CONSTRAINT `fk_note_links_from` FOREIGN KEY (`from_note_id`) REFERENCES `notes` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_note_links_to` FOREIGN KEY (`to_note_id`) REFERENCES `notes` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
