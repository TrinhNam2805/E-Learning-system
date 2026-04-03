-- =============================================================================
-- MIGRATION: khóa lesson theo thứ tự bằng lesson-linked assignments
-- Chạy cho DB cũ đã có schema trước đó
--
-- Thêm:
--   - assignments.lesson_id
--   - assignments.minimum_passing_score
--
-- Mục tiêu:
--   - Cho phép gắn assignment với một lesson cụ thể
--   - Bài học kế tiếp chỉ mở khi lesson trước đã hoàn thành và assignment liên kết đạt điểm tối thiểu
-- =============================================================================

SET NAMES utf8mb4;

ALTER TABLE `assignments`
    ADD COLUMN `lesson_id` BIGINT NULL AFTER `course_id`,
    ADD COLUMN `minimum_passing_score` DOUBLE NULL AFTER `max_score`;

ALTER TABLE `assignments`
    ADD KEY `idx_assignments_lesson_id` (`lesson_id`);

ALTER TABLE `assignments`
    ADD CONSTRAINT `fk_assignments_lesson`
        FOREIGN KEY (`lesson_id`) REFERENCES `lessons` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE;

-- Dữ liệu mẫu mặc định theo seed-data.sql
UPDATE `assignments`
SET `lesson_id` = 1,
    `minimum_passing_score` = 6.0
WHERE `id` = 1;

UPDATE `assignments`
SET `lesson_id` = 2,
    `minimum_passing_score` = 6.0
WHERE `id` = 2;

UPDATE `assignments`
SET `lesson_id` = 4,
    `minimum_passing_score` = 5.0
WHERE `id` = 3;

UPDATE `assignments`
SET `lesson_id` = 7,
    `minimum_passing_score` = 5.0
WHERE `id` = 4;
