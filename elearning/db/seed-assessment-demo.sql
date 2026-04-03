-- =============================================================================
-- Dữ liệu mẫu bổ sung cho Assessment, Grading, Progress Tracking
-- Chạy sau:
--   1) schema-standard.sql
--   2) seed-data.sql
-- Mục tiêu:
--   - Có assignment còn hạn để demo làm bài / nộp bài
--   - Có assignment cho phép nộp trễ
--   - Có bài quiz auto-grade
--   - Có bài tự luận manual-grade
--   - Có resubmission history
--   - Có file metadata để demo download / lịch sử bài nộp
-- =============================================================================

SET NAMES utf8mb4;

-- Mật khẩu demo: Demo@2024
SET @pwd := '$2b$10$IgwunfhzoNX76OVOOWU1.eKSB7SmLMjNAdTvjYpuY79/U0ebJmcpy';

INSERT INTO `users`
(`id`, `username`, `email`, `password`, `full_name`, `role`, `phone`, `active`, `locked`, `created_at`, `department_id`, `student_code`)
VALUES
(8, 'admin.cntt', 'admin@univ.edu.vn', @pwd, 'Quản trị hệ thống', 'ADMIN', '0900000000', 1, 0, '2024-08-10', 1, NULL)
ON DUPLICATE KEY UPDATE
`password` = VALUES(`password`),
`full_name` = VALUES(`full_name`),
`role` = VALUES(`role`),
`active` = VALUES(`active`),
`locked` = VALUES(`locked`);

UPDATE `assignments`
SET `due_date` = '2027-12-15 23:59:00',
    `allow_late_submission` = b'0',
    `max_attempts` = 1
WHERE `id` = 1;

UPDATE `assignments`
SET `due_date` = '2027-12-20 23:59:00',
    `allow_late_submission` = b'0',
    `max_attempts` = 2
WHERE `id` = 2;

UPDATE `assignments`
SET `due_date` = '2027-12-18 23:59:00',
    `allow_late_submission` = b'0',
    `max_attempts` = 1
WHERE `id` = 3;

UPDATE `assignments`
SET `due_date` = '2027-12-22 23:59:00',
    `allow_late_submission` = b'0',
    `max_attempts` = 1
WHERE `id` = 4;

INSERT INTO `assignments`
(`id`, `course_id`, `title`, `description`, `type`, `due_date`, `max_score`, `allow_late_submission`, `max_attempts`)
VALUES
(5, 1, 'Bài tập bù: Báo cáo ngắn về cây BST',
 'Viết báo cáo 1-2 trang phân tích ứng dụng BST và trường hợp suy biến. Bài này cho phép nộp trễ và tối đa 2 lần nộp để minh họa chức năng resubmission.',
 'HOMEWORK', '2026-03-15 23:59:00', 10.0, b'1', 2)
ON DUPLICATE KEY UPDATE
`title` = VALUES(`title`),
`description` = VALUES(`description`),
`type` = VALUES(`type`),
`due_date` = VALUES(`due_date`),
`max_score` = VALUES(`max_score`),
`allow_late_submission` = VALUES(`allow_late_submission`),
`max_attempts` = VALUES(`max_attempts`);

DELETE FROM `submissions`
WHERE `assignment_id` IN (1, 2, 3, 5);

INSERT INTO `submissions`
(`id`, `assignment_id`, `student_id`, `content`, `file_url`, `original_file_name`, `file_size`, `score`, `feedback`, `status`, `attempt_number`, `late_submission`, `auto_graded`, `submitted_at`, `graded_at`)
VALUES
(1, 1, 3, 'Q1:B;Q2:B;Q3:B;Q4:B;Q5:C;', NULL, NULL, NULL, 10.0, 'Hệ thống chấm tự động: đúng 5/5 câu.', 'GRADED', 1, b'0', b'1', '2026-11-10 20:00:00', '2026-11-10 20:00:00'),
(2, 1, 4, 'Q1:B;Q2:B;Q3:B;Q4:A;Q5:C;', NULL, NULL, NULL, 8.0, 'Hệ thống chấm tự động: đúng 4/5 câu.', 'GRADED', 1, b'0', b'1', '2026-11-11 18:30:00', '2026-11-11 18:30:00'),
(3, 2, 3,
 'Phân tích đoạn 1: vòng lặp đơn O(n). Đoạn 2: hai vòng lồng O(n²). Giải thích: số lần lặp lồng tỷ lệ n(n-1)/2 ~ Θ(n²).',
 'uploads/assessment/assignment-2-student-3-demo1.pdf', 'phan-tich-do-phuc-tap.pdf', 245760, 9.0, 'Lập luận chặt chẽ, trình bày rõ ràng.', 'GRADED', 1, b'0', b'0', '2026-11-12 09:00:00', '2026-11-13 08:15:00'),
(4, 2, 4,
 'Em gửi lần 1. Phần 2 còn trình bày ngắn, chưa làm rõ số phép toán trong hai vòng lặp.',
 'uploads/assessment/assignment-2-student-4-attempt1.docx', 'phan-tich-do-phuc-tap-lan1.docx', 98304, 6.5, 'Bạn đã hiểu ý chính, nhưng cần bổ sung ví dụ minh họa. Bạn có thể nộp lại 1 lần.', 'GRADED', 1, b'0', b'0', '2026-11-13 10:30:00', '2026-11-13 16:00:00'),
(5, 2, 4,
 'Em đã viết lại phần giải thích cho hai đoạn giả mã, có bổ sung phần chứng minh Big-O và bảng tổng hợp số bước.',
 'uploads/assessment/assignment-2-student-4-attempt2.pdf', 'phan-tich-do-phuc-tap-lan2.pdf', 157286, NULL, NULL, 'SUBMITTED', 2, b'0', b'0', '2026-11-14 09:20:00', NULL),
(6, 3, 3, 'Q6:B;Q7:B;Q8:A;Q9:B;', NULL, NULL, NULL, 7.5, 'Hệ thống chấm tự động: đúng 3/4 câu.', 'GRADED', 1, b'0', b'1', '2026-11-15 20:10:00', '2026-11-15 20:10:00'),
(7, 5, 5,
 'Bài báo cáo muộn: em mô tả cách duyệt inorder, preorder và minh họa cây bị suy biến khi dữ liệu đầu vào đã sắp xếp.',
 'uploads/assessment/assignment-5-student-5-late.pdf', 'bao-cao-bst-late.pdf', 204800, NULL, NULL, 'SUBMITTED', 1, b'1', b'0', '2026-03-18 21:45:00', NULL);

UPDATE `enrollments`
SET `progress_percentage` = 67,
    `activity_xp` = 85,
    `total_xp` = 125
WHERE `student_id` = 3 AND `course_id` = 1;

UPDATE `enrollments`
SET `progress_percentage` = 33,
    `activity_xp` = 65,
    `total_xp` = 85
WHERE `student_id` = 4 AND `course_id` = 1;

UPDATE `enrollments`
SET `progress_percentage` = 0,
    `activity_xp` = 0,
    `total_xp` = 0
WHERE `student_id` = 5 AND `course_id` = 1;

UPDATE `enrollments`
SET `progress_percentage` = 0,
    `activity_xp` = 30,
    `total_xp` = 30
WHERE `student_id` = 3 AND `course_id` = 2;

DELETE FROM `notifications`
WHERE `id` IN (4, 5, 6, 7);

INSERT INTO `notifications` (`id`, `user_id`, `title`, `message`, `type`, `is_read`, `created_at`) VALUES
(4, 4, 'Bài tập đã được mở lại', 'Bạn còn 1 lần nộp lại cho bài "Bài tập: Phân tích độ phức tạp...".', 'ASSIGNMENT', b'0', '2026-11-13 16:05:00'),
(5, 5, 'Ghi nhận bài nộp trễ', 'Hệ thống đã ghi nhận bài nộp trễ cho "Bài tập bù: Báo cáo ngắn về cây BST".', 'ASSIGNMENT', b'0', '2026-03-18 21:50:00'),
(6, 3, 'Kết quả quiz HTML/CSS', 'Quiz HTML/CSS đã được chấm tự động. Điểm hiện tại: 7.5/10.', 'GRADE', b'0', '2026-11-15 20:12:00'),
(7, 4, 'Bài nộp đang chờ chấm', 'Lần nộp thứ 2 cho bài "Phân tích độ phức tạp" đã được ghi nhận và đang chờ giảng viên chấm.', 'ASSIGNMENT', b'0', '2026-11-14 09:25:00');

ALTER TABLE `users` AUTO_INCREMENT = 100;
ALTER TABLE `assignments` AUTO_INCREMENT = 100;
ALTER TABLE `submissions` AUTO_INCREMENT = 100;
ALTER TABLE `notifications` AUTO_INCREMENT = 100;
