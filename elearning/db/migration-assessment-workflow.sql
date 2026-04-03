-- Migration cho module Assessment, Grading, and Progress Tracking
-- Chạy trên DB hiện có nếu schema đang dùng bản cũ chưa có rule nộp trễ / nhiều lần nộp / metadata file.

ALTER TABLE `assignments`
  ADD COLUMN `allow_late_submission` BIT(1) NOT NULL DEFAULT b'0' AFTER `max_score`,
  ADD COLUMN `max_attempts` INT NOT NULL DEFAULT 1 AFTER `allow_late_submission`;

ALTER TABLE `submissions`
  ADD COLUMN `original_file_name` VARCHAR(255) NULL AFTER `file_url`,
  ADD COLUMN `file_size` BIGINT NULL AFTER `original_file_name`,
  ADD COLUMN `attempt_number` INT NOT NULL DEFAULT 1 AFTER `status`,
  ADD COLUMN `late_submission` BIT(1) NOT NULL DEFAULT b'0' AFTER `attempt_number`,
  ADD COLUMN `auto_graded` BIT(1) NOT NULL DEFAULT b'0' AFTER `late_submission`,
  ADD COLUMN `graded_at` DATETIME(6) NULL AFTER `submitted_at`;

UPDATE `submissions`
SET `late_submission` = b'1'
WHERE `status` = 'LATE';
