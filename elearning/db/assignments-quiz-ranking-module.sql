-- =============================================================================
-- MODULE: Bài tập, Quiz, Nộp bài, điểm số, XP và Ranking
-- =============================================================================
-- Đồng bộ với code: AssignmentController, AssignmentService, GamificationService,
--                   EnrollmentService, RankingService (Spring Boot).
--
-- CÁCH DÙNG
--   • Khởi tạo DB đầy đủ: chạy schema-standard.sql (hoặc DDL tương đương), rồi seed.
--   • File này dùng để: tra cứu logic, review DDL riêng cụm chức năng, hoặc so sánh
--     khi merge/migration — KHÔNG thay thế toàn bộ schema nếu đã có bảng khác.
--   • Cần sẵn bảng: `users`, `courses` (FK). Cột `enrollments.activity_xp` / `total_xp`
--     nằm trong bảng enrollments (xem migration-activity-xp-notes.sql nếu DB cũ).
--
-- mysql -u root -p --default-character-set=utf8mb4 e-learning < elearning/db/assignments-quiz-ranking-module.sql
-- =============================================================================


-- =============================================================================
-- PHẦN A — LOGIC NGHIỆP VỤ (khớp code Java, không thực thi)
-- =============================================================================
--
-- A1) Bảng dữ liệu liên quan
--   • assignments      : bài giao (HOMEWORK | QUIZ | EXAM), due_date, max_score
--   • quiz_questions   : câu hỏi trắc nghiệm gắn assignment type QUIZ
--   • submissions      : lần nộp của sinh viên (mỗi SV 1 bản ghi / assignment — app kiểm tra)
--   • enrollments      : activity_xp (XP từ quiz + bài được chấm), total_xp (tổng trong khóa)
--
-- A2) Nộp bài tự luận (HOMEWORK / EXAM)
--   • POST lưu submissions: content = văn bản nộp; score = NULL; status = SUBMITTED hoặc LATE
--   • Giảng viên chấm: score, feedback, status = GRADED
--   • Điểm thang điểm: 0 … max_score (do GV nhập khi chấm)
--   • XP (lần chấm ĐẦU TIÊN, và chỉ khi type ≠ QUIZ):
--       xp_hw = round( min(1, score / max_score) * 50 ), giới hạn tối đa 50 XP/lần
--       (GamificationService.MAX_XP_ASSIGNMENT = 50)
--       Cộng vào enrollments.activity_xp của đúng (student, course), rồi recalc total_xp.
--
-- A3) Nộp quiz (type = QUIZ)
--   • Mỗi câu đúng nếu đáp án gửi lên (q_<questionId>) == quiz_questions.correct_answer (A|B|C|D)
--   • Điểm (ứng dụng — KHÔNG dùng cột quiz_questions.points khi chấm):
--       score = (số câu đúng / số câu) * max_score
--       Nếu không có câu hỏi: score = 0
--   • submissions: score đã set ngay; status = GRADED (đúng hạn) hoặc LATE (nộp trễ có xác nhận)
--   • XP ngay sau khi nộp:
--       xp_q = round( min(1, score / max_score) * 40 ), tối đa 40 XP/lần quiz
--       (GamificationService.MAX_XP_QUIZ = 40)
--   • Bài QUIZ đã chấm tự động: không gọi thêm awardGradedAssignmentXp khi GV “chấm lại”.
--
-- A4) Tổng XP trong một enrollment (khóa học)
--   • total_xp = lesson_xp + activity_xp
--   • lesson_xp = (số bài học đã hoàn thành trong khóa đó) * 20
--   • activity_xp = cộng dồn các lần +XP từ quiz và từ chấm bài tự luận (theo quy tắc trên)
--
-- A5) Ranking (bảng xếp hạng toàn hệ thống)
--   • Với mỗi sinh viên: totalXp_rank = SUM(enrollments.total_xp) trên TẤT CẢ các khóa đã ghi danh
--   • Sắp xếp giảm dần theo totalXp_rank
--   • Level (hiển thị): level = floor(totalXp_rank / 100) + 1
--   • XP trong level hiện tại: totalXp_rank % 100
--
-- =============================================================================
-- PHẦN B — DDL (chỉ chạy khi cần tạo lại riêng các bảng; đã có courses, users)
-- =============================================================================

SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `submissions`;
DROP TABLE IF EXISTS `quiz_questions`;
DROP TABLE IF EXISTS `assignments`;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `assignments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_id` BIGINT NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `description` TEXT,
  `type` VARCHAR(20) DEFAULT 'HOMEWORK' COMMENT 'HOMEWORK | QUIZ | EXAM',
  `due_date` DATETIME(6) DEFAULT NULL,
  `max_score` DOUBLE NOT NULL DEFAULT 10,
  PRIMARY KEY (`id`),
  KEY `idx_assignments_course_id` (`course_id`),
  CONSTRAINT `fk_assignments_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bài tập / Quiz / Kiểm tra theo khóa học';

CREATE TABLE `quiz_questions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `assignment_id` BIGINT NOT NULL,
  `question_text` TEXT NOT NULL,
  `option_a` VARCHAR(500) DEFAULT NULL,
  `option_b` VARCHAR(500) DEFAULT NULL,
  `option_c` VARCHAR(500) DEFAULT NULL,
  `option_d` VARCHAR(500) DEFAULT NULL,
  `correct_answer` VARCHAR(1) NOT NULL COMMENT 'A | B | C | D',
  `question_order` INT NOT NULL DEFAULT 1,
  `points` DOUBLE NOT NULL DEFAULT 1 COMMENT 'App hiện tại chưa dùng khi tính điểm — điểm chia đều theo số câu',
  PRIMARY KEY (`id`),
  KEY `idx_quiz_questions_assignment_id` (`assignment_id`),
  CONSTRAINT `fk_quiz_questions_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Câu hỏi trắc nghiệm (assignment type = QUIZ)';

CREATE TABLE `submissions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `assignment_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `content` TEXT COMMENT 'Tự luận: nội dung nộp; Quiz: chuỗi đáp án Qid:value;',
  `file_url` VARCHAR(500) DEFAULT NULL COMMENT 'App chưa dùng trong luồng nộp hiện tại',
  `score` DOUBLE DEFAULT NULL,
  `feedback` TEXT,
  `status` VARCHAR(20) DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED | GRADED | LATE',
  `submitted_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_submissions_assignment_id` (`assignment_id`),
  KEY `idx_submissions_student_id` (`student_id`),
  CONSTRAINT `fk_submissions_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_submissions_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Nộp bài; 1 SV / assignment (ràng buộc ở tầng ứng dụng)';

-- =============================================================================
-- PHẦN C — enrollments: cột phục vụ XP & ranking (thường đã có trong schema-standard)
-- =============================================================================
-- Nếu DB cũ chưa có activity_xp, chạy migration-activity-xp-notes.sql
--
--   `progress_percentage`  INT ...
--   `activity_xp`          INT NOT NULL DEFAULT 0  -- XP từ quiz + chấm bài (cộng dồn)
--   `total_xp`             INT NOT NULL DEFAULT 0  -- lesson_xp + activity_xp (mỗi lần recalc)

-- =============================================================================
-- PHẦN D — Gợi ý seed (comment): gán quiz_questions sau khi INSERT assignments
-- =============================================================================
-- INSERT INTO assignments (course_id, title, description, type, due_date, max_score)
-- VALUES (1, N'Quiz mẫu', N'...', 'QUIZ', '2026-12-31 23:59:00', 10);
-- SET @aid = LAST_INSERT_ID();
-- INSERT INTO quiz_questions (assignment_id, question_text, option_a, option_b, option_c, option_d, correct_answer, question_order, points)
-- VALUES (@aid, N'Câu 1?', 'A1','B1','C1','D1','A',1,1);
