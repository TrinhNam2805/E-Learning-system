-- =============================================================================
-- Dữ liệu DEMO: 3 khóa DSA, HTML/CSS, Java cơ bản — không sinh viên mẫu.
-- Chạy sau: schema-standard.sql + migration-password-reset.sql + migration-activity-xp-notes.sql
-- Mật khẩu demo: Demo@2024
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM `comments`;
DELETE FROM `quiz_questions`;
DELETE FROM `submissions`;
DELETE FROM `note_links`;
DELETE FROM `notes`;
DELETE FROM `notifications`;
DELETE FROM `lesson_progress`;
DELETE FROM `enrollments`;
DELETE FROM `forum_posts`;
DELETE FROM `assignments`;
DELETE FROM `lessons`;
DELETE FROM `course_sections`;
DELETE FROM `course_prerequisites`;
DELETE FROM `courses`;
DELETE FROM `users`;
DELETE FROM `departments`;

SET FOREIGN_KEY_CHECKS = 1;

SET @pwd := '$2b$10$IgwunfhzoNX76OVOOWU1.eKSB7SmLMjNAdTvjYpuY79/U0ebJmcpy';

INSERT INTO `departments` (`id`, `code`, `name`) VALUES
(1, 'CNTT', 'Khoa Công nghệ Thông tin');

INSERT INTO `users` (`id`, `username`, `email`, `password`, `full_name`, `role`, `phone`, `active`, `locked`, `created_at`, `department_id`, `student_code`) VALUES
(1, 'gv.nguyenvanan', 'teacher@cntt.edu.vn', @pwd, 'TS. Nguyễn Văn An', 'TEACHER', '0901234567', 1, 0, '2024-08-15', 1, NULL),
(2, 'admin.cntt', 'admin@cntt.edu.vn', @pwd, 'Quản trị hệ thống', 'ADMIN', NULL, 1, 0, '2024-08-15', 1, NULL);

INSERT INTO `courses` (`id`, `course_code`, `course_name`, `description`, `teacher_id`, `enroll_password`, `semester`, `academic_year`, `status`, `max_students`, `thumbnail`, `department_id`, `credits`, `theory_hours`, `practice_hours`) VALUES
(1, 'CS201', 'Cấu trúc dữ liệu và giải thuật (DSA)',
'DSA: Big-O, stack, queue, BST, sắp xếp và duyệt đồ thị cơ bản.',
1, 'CS201HK1', 'HK1', '2024-2025', 'PUBLISHED', 60,
'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&q=80', 1, 4, 45, 30),
(2, 'WEB101', 'HTML & CSS — Thiết kế trang web',
'HTML5 ngữ nghĩa, CSS box model, Flexbox, responsive. Nội dung hiển thị trên LMS; có thể bổ sung PDF/DOCX riêng.',
1, 'WEB101HK1', 'HK1', '2024-2025', 'PUBLISHED', 50,
'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800&q=80', 1, 3, 30, 45),
(3, 'JAVA101', 'Lập trình Java cơ bản',
'Cú pháp Java, OOP, ngoại lệ, Collection giới thiệu.',
1, 'JAVA101HK1', 'HK1', '2024-2025', 'PUBLISHED', 55,
'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&q=80', 1, 3, 30, 45);

INSERT INTO `course_prerequisites` (`id`, `course_id`, `prerequisite_course_code`) VALUES
(1, 2, 'CS201'),
(2, 3, 'CS201');

INSERT INTO `course_sections` (`id`, `course_id`, `section_order`, `title`) VALUES
(1, 1, 1, 'Phần 1 — DSA'),
(2, 2, 1, 'Phần 1 — HTML & CSS'),
(3, 3, 1, 'Phần 1 — Java');

INSERT INTO `lessons` (`id`, `course_id`, `section_id`, `lesson_order`, `lesson_title`, `lesson_content`, `video_url`, `duration_minutes`, `published`) VALUES
(1, 1, 1, 1, 'Big-O và phân tích asymptotic',
CONCAT('<h2>Nội dung</h2><p>Phân tích độ phức tạp: O, Omega, Theta. Ký hiệu <strong>O(f(n))</strong> mô tả cận trên (worst-case thường dùng).</p>',
'<div class="material-hint">PDF/DOCX: nếu GV có slide riêng, đăng từ kho tài liệu khóa học.</div>'),
NULL, 90, 1),
(2, 1, 1, 2, 'Stack và Queue',
'<h2>Stack</h2><p>LIFO — push/pop O(1).</p><h2>Queue</h2><p>FIFO — BFS, hàng đợi tác vụ.</p>',
NULL, 90, 1),
(3, 1, 1, 3, 'Cây BST',
'<h2>BST</h2><p>Khóa trái nhỏ hơn nút, khóa phải lớn hơn. Duyệt inorder cho sắp tăng.</p>',
NULL, 90, 1),
(4, 2, 2, 1, 'HTML5 cấu trúc',
'<h2>Semantic</h2><p>Dùng <code>header</code>, <code>main</code>, <code>article</code>, <code>footer</code> thay vì chỉ <code>div</code>.</p>',
NULL, 60, 1),
(5, 2, 2, 2, 'CSS box model',
'<p>Content, padding, border, margin. <code>box-sizing: border-box</code> giúp tính kích thước ổn định.</p>',
NULL, 60, 1),
(6, 2, 2, 3, 'Flexbox',
'<p><code>display: flex</code>; <code>justify-content</code> và <code>align-items</code> căn chỉnh hàng/cột.</p>',
NULL, 60, 1),
(7, 3, 3, 1, 'Java: Hello World',
'<pre>public class Hello {\n  public static void main(String[] args) {\n    System.out.println("Hi");\n  }\n}</pre>',
NULL, 75, 1),
(8, 3, 3, 2, 'Lớp và đối tượng',
'<p><code>class</code>, thuộc tính, phương thức, constructor. <code>this</code> tham chiếu thể hiện hiện tại.</p>',
NULL, 75, 1),
(9, 3, 3, 3, 'Kế thừa và ngoại lệ',
'<p><code>extends</code>, <code>implements</code>, <code>try/catch/finally</code>.</p>',
NULL, 75, 1);

INSERT INTO `assignments` (`id`, `course_id`, `title`, `description`, `type`, `due_date`, `max_score`) VALUES
(1, 1, 'Quiz: DSA cơ bản', 'Trắc nghiệm.', 'QUIZ', '2026-12-15 23:59:00', 10.0),
(2, 1, 'Bài tập: Phân tích độ phức tạp', 'Nộp văn bản.', 'HOMEWORK', '2026-12-20 23:59:00', 10.0);

INSERT INTO `quiz_questions` (`id`, `assignment_id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `question_order`, `points`) VALUES
(1, 1, 'Độ phức tạp duyệt mảng n phần tử một lần?', 'O(n²)', 'O(n)', 'O(log n)', 'O(1)', 'B', 1, 5.0),
(2, 1, 'Cấu trúc LIFO?', 'Queue', 'Stack', 'List', 'Heap', 'B', 2, 5.0);

INSERT INTO `forum_posts` (`id`, `course_id`, `author_id`, `title`, `content`, `post_type`, `view_count`, `created_at`) VALUES
(1, 1, 1, 'Thông báo: tài liệu ôn tập', 'Cormen ch.3 — asymptotic.', 'ANNOUNCEMENT', 5, '2024-10-03 14:00:00');

INSERT INTO `comments` (`id`, `post_id`, `author_id`, `content`, `created_at`) VALUES
(1, 1, 1, 'Các em xem thêm bài tập trong LMS.', '2024-10-03 15:00:00');

ALTER TABLE `departments` AUTO_INCREMENT = 100;
ALTER TABLE `users` AUTO_INCREMENT = 100;
ALTER TABLE `courses` AUTO_INCREMENT = 100;
ALTER TABLE `course_prerequisites` AUTO_INCREMENT = 100;
ALTER TABLE `course_sections` AUTO_INCREMENT = 100;
ALTER TABLE `lessons` AUTO_INCREMENT = 100;
ALTER TABLE `assignments` AUTO_INCREMENT = 100;
ALTER TABLE `quiz_questions` AUTO_INCREMENT = 100;
ALTER TABLE `forum_posts` AUTO_INCREMENT = 100;
ALTER TABLE `comments` AUTO_INCREMENT = 100;
