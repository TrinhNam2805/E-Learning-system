-- Diễn đàn: bài viết toàn kênh (course_id NULL) + gắn tùy chọn bài học (lesson_id).
-- Chạy sau schema-standard.sql (hoặc bản đã triển khai có forum_posts).

SET NAMES utf8mb4;

ALTER TABLE `forum_posts`
  MODIFY `course_id` BIGINT NULL,
  ADD COLUMN `lesson_id` BIGINT NULL DEFAULT NULL AFTER `course_id`,
  ADD KEY `idx_forum_posts_lesson_id` (`lesson_id`),
  ADD CONSTRAINT `fk_forum_posts_lesson` FOREIGN KEY (`lesson_id`) REFERENCES `lessons` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE;
