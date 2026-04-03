-- =============================================================================
-- DỮ LIỆU MẪU BÀI TẬP NỘP FILE CHO CÁC HỌC PHẦN MẪU
-- Chạy sau:
--   1) schema-standard.sql
--   2) seed-data.sql
--   3) migration-assessment-workflow.sql (nếu DB cũ chưa có cột assessment mới)
--
-- Mục tiêu:
--   - Tạo assignment loại HOMEWORK để form tải file hiển thị trong UI
--   - Giữ due_date ở tương lai để sinh viên có thể nộp bài ngay
--   - Có ít nhất một bài cho phép nộp trễ và một bài cho phép nộp lại
--   - Có thể chạy lại an toàn
--
-- Cách chạy:
--   mysql -u root -p --default-character-set=utf8mb4 e-learning < elearning/db/seed-file-upload-assignment-sample.sql
-- =============================================================================

SET NAMES utf8mb4;

-- Xóa dữ liệu bài test nộp file cũ nếu đã tồn tại
DELETE s
FROM `submissions` s
JOIN `assignments` a ON a.id = s.assignment_id
WHERE a.title LIKE '[TEST-UPLOAD] %';

DELETE FROM `assignments`
WHERE `title` LIKE '[TEST-UPLOAD] %';

-- Lấy id khóa học mẫu
SET @course_cs201 := (SELECT id FROM `courses` WHERE `course_code` = 'CS201' LIMIT 1);
SET @course_web101 := (SELECT id FROM `courses` WHERE `course_code` = 'WEB101' LIMIT 1);
SET @course_java101 := (SELECT id FROM `courses` WHERE `course_code` = 'JAVA101' LIMIT 1);

-- Bảo đảm tài khoản demo chính có thể test cả 3 học phần nếu cần
INSERT INTO `enrollments` (`student_id`, `course_id`, `enrolled_at`, `status`, `progress_percentage`, `activity_xp`, `total_xp`)
SELECT 3, @course_java101, CURDATE(), 'ACTIVE', 0, 0, 0
WHERE @course_java101 IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM `enrollments`
      WHERE `student_id` = 3
        AND `course_id` = @course_java101
  );

-- =============================================================================
-- CS201 - bài tập yêu cầu nộp PDF/DOCX
-- =============================================================================

INSERT INTO `assignments`
(`course_id`, `title`, `description`, `type`, `due_date`, `max_score`, `allow_late_submission`, `max_attempts`)
VALUES
(@course_cs201,
 '[TEST-UPLOAD] CS201 - Báo cáo phân tích độ phức tạp',
 'Bài tập tự luận yêu cầu nộp file PDF hoặc DOCX. Hãy phân tích Big-O cho ba đoạn giả mã trong chương 1 và nêu rõ cách suy luận. Bạn có thể nhập ghi chú ngắn vào ô nội dung, nhưng cần tải lên tệp bài làm để test chức năng upload.',
 'HOMEWORK',
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 10 DAY), '23:59:00'),
 10.0,
 b'0',
 2);

-- =============================================================================
-- WEB101 - bài tập yêu cầu nộp file ZIP/PDF
-- =============================================================================

INSERT INTO `assignments`
(`course_id`, `title`, `description`, `type`, `due_date`, `max_score`, `allow_late_submission`, `max_attempts`)
VALUES
(@course_web101,
 '[TEST-UPLOAD] WEB101 - Nộp file giao diện landing page',
 'Bài tập thực hành yêu cầu nộp file ZIP chứa mã nguồn HTML/CSS hoặc file PDF mô tả giao diện. Mục tiêu là giúp test chức năng nộp bài có tệp đính kèm. Bài này cho phép nộp lại tối đa 2 lần để bạn kiểm thử resubmission.',
 'HOMEWORK',
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 12 DAY), '23:59:00'),
 10.0,
 b'0',
 2);

-- =============================================================================
-- JAVA101 - bài tập yêu cầu nộp file, có cho phép nộp trễ
-- =============================================================================

INSERT INTO `assignments`
(`course_id`, `title`, `description`, `type`, `due_date`, `max_score`, `allow_late_submission`, `max_attempts`)
VALUES
(@course_java101,
 '[TEST-UPLOAD] JAVA101 - Nộp file chương trình quản lý sinh viên',
 'Bài tập yêu cầu nộp file ZIP chứa project Java hoặc file PDF mô tả thiết kế lớp. Bài này bật cho phép nộp trễ để bạn test cả upload file và xác nhận nộp muộn khi cần.',
 'HOMEWORK',
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 14 DAY), '23:59:00'),
 10.0,
 b'1',
 2);

ALTER TABLE `assignments` AUTO_INCREMENT = 2000;
