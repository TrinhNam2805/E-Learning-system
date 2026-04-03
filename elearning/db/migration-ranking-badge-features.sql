-- =============================================================================
-- MIGRATION: Ranking and Badge Features
-- Chạy cho DB cũ trước khi dùng runtime với module ranking/badge mới
-- =============================================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `badge_definitions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL,
  `name` VARCHAR(120) NOT NULL,
  `description` VARCHAR(255) NOT NULL,
  `icon` VARCHAR(80) NOT NULL,
  `criterion_type` VARCHAR(40) NOT NULL COMMENT 'TOTAL_XP | COMPLETED_LESSONS | GRADED_SUBMISSIONS | PERFECT_QUIZZES | COMPLETED_COURSES',
  `threshold_value` INT NOT NULL,
  `active` BIT(1) NOT NULL DEFAULT b'1',
  `display_order` INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_badge_definitions_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_badges` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `badge_definition_id` BIGINT NOT NULL,
  `earned_at` DATETIME(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_badges_user_badge` (`user_id`, `badge_definition_id`),
  KEY `idx_user_badges_badge_definition_id` (`badge_definition_id`),
  CONSTRAINT `fk_user_badges_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_user_badges_badge_definition` FOREIGN KEY (`badge_definition_id`) REFERENCES `badge_definitions` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `badge_definitions`
(`id`, `code`, `name`, `description`, `icon`, `criterion_type`, `threshold_value`, `active`, `display_order`) VALUES
(1, 'FIRST_STEP', 'Bước khởi đầu', 'Hoàn thành 1 bài học đầu tiên trong hệ thống.', 'rocket_launch', 'COMPLETED_LESSONS', 1, b'1', 1),
(2, 'LEARNING_STREAK', 'Tiến bộ bền bỉ', 'Hoàn thành ít nhất 5 bài học.', 'local_fire_department', 'COMPLETED_LESSONS', 5, b'1', 2),
(3, 'FIRST_GRADE', 'Có điểm đầu tiên', 'Có ít nhất 1 bài nộp đã được chấm điểm.', 'fact_check', 'GRADED_SUBMISSIONS', 1, b'1', 3),
(4, 'QUIZ_ACE', 'Chuyên gia quiz', 'Đạt điểm tuyệt đối ở ít nhất 1 bài quiz.', 'psychology', 'PERFECT_QUIZZES', 1, b'1', 4),
(5, 'XP_BRONZE', 'XP Đồng', 'Đạt ít nhất 100 XP tích lũy.', 'workspace_premium', 'TOTAL_XP', 100, b'1', 5),
(6, 'XP_SILVER', 'XP Bạc', 'Đạt ít nhất 300 XP tích lũy.', 'workspace_premium', 'TOTAL_XP', 300, b'1', 6),
(7, 'COURSE_FINISHER', 'Hoàn tất học phần', 'Hoàn thành ít nhất 1 khóa học với tiến độ 100%.', 'school', 'COMPLETED_COURSES', 1, b'1', 7)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`description` = VALUES(`description`),
`icon` = VALUES(`icon`),
`criterion_type` = VALUES(`criterion_type`),
`threshold_value` = VALUES(`threshold_value`),
`active` = VALUES(`active`),
`display_order` = VALUES(`display_order`);

ALTER TABLE `badge_definitions` AUTO_INCREMENT = 100;
ALTER TABLE `user_badges` AUTO_INCREMENT = 100;
