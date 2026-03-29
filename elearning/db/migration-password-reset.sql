-- Chạy trên DB hiện có (ddl-auto=none) nếu bảng users chưa có cột reset mật khẩu.
-- mysql -u root -p e-learning < elearning/db/migration-password-reset.sql

ALTER TABLE `users`
  ADD COLUMN `password_reset_token` VARCHAR(100) DEFAULT NULL AFTER `student_code`,
  ADD COLUMN `password_reset_expires` DATETIME(6) DEFAULT NULL AFTER `password_reset_token`;

ALTER TABLE `users`
  ADD UNIQUE KEY `uk_users_password_reset_token` (`password_reset_token`);
