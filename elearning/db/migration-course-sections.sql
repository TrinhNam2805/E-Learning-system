-- =============================================================================
-- Migration: curriculum theo phần (Udemy-style) — chạy trên DB đã có bảng lessons
-- =============================================================================
-- Bắt buộc nếu gặp: Unknown column 'lesson_._section_id' (JPA map Lesson.section)
-- mysql -u root -p --default-character-set=utf8mb4 `e-learning` < elearning/db/migration-course-sections.sql
-- =============================================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `course_sections` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_id` BIGINT NOT NULL,
  `section_order` INT NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_course_sections_course_id` (`course_id`),
  CONSTRAINT `fk_course_sections_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thêm cột + index + FK chỉ khi chưa có (idempotent — chạy lại không lỗi)
SET @db = DATABASE();
SET @has_col = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'lessons' AND COLUMN_NAME = 'section_id'
);
SET @sql = IF(@has_col = 0,
  'ALTER TABLE `lessons` ADD COLUMN `section_id` BIGINT NULL DEFAULT NULL AFTER `course_id`, ADD KEY `idx_lessons_section_id` (`section_id`), ADD CONSTRAINT `fk_lessons_section` FOREIGN KEY (`section_id`) REFERENCES `course_sections` (`id`) ON DELETE SET NULL ON UPDATE CASCADE',
  'SELECT 1 AS `migration_course_sections_lessons_ok`'
);
PREPARE _mcs FROM @sql;
EXECUTE _mcs;
DEALLOCATE PREPARE _mcs;

-- Một phần mặc định cho mỗi khóa học đang có bài học
INSERT INTO `course_sections` (`course_id`, `section_order`, `title`)
SELECT c.id, 1, 'Phần 1 — Nội dung khóa học'
FROM `courses` c
WHERE EXISTS (SELECT 1 FROM `lessons` l WHERE l.course_id = c.id)
  AND NOT EXISTS (SELECT 1 FROM `course_sections` cs WHERE cs.course_id = c.id);

UPDATE `lessons` l
JOIN `course_sections` cs ON cs.course_id = l.course_id AND cs.section_order = 1
SET l.section_id = cs.id
WHERE l.section_id IS NULL;
