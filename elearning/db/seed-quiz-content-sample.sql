-- =============================================================================
-- DỮ LIỆU QUIZ BỔ SUNG CHO CÁC HỌC PHẦN MẪU
-- Chạy sau:
--   1) schema-standard.sql
--   2) seed-data.sql
--   3) seed-assessment-demo.sql (khuyến nghị, nhưng không bắt buộc)
--
-- Mục tiêu:
--   - Tạo thêm nhiều quiz để test chức năng hiển thị quiz, làm bài, auto-grade,
--     lịch sử nộp bài, deadline và theo dõi kết quả.
--   - Script có tính idempotent: chạy lại sẽ xóa bộ quiz test cũ rồi tạo lại.
--
-- Cách chạy:
--   mysql -u root -p --default-character-set=utf8mb4 e-learning < elearning/db/seed-quiz-content-sample.sql
-- =============================================================================

SET NAMES utf8mb4;

-- Xóa dữ liệu quiz test cũ nếu đã tồn tại
DELETE s
FROM `submissions` s
JOIN `assignments` a ON a.id = s.assignment_id
WHERE a.title LIKE '[TEST-QUIZ] %';

DELETE q
FROM `quiz_questions` q
JOIN `assignments` a ON a.id = q.assignment_id
WHERE a.title LIKE '[TEST-QUIZ] %';

DELETE FROM `assignments`
WHERE `title` LIKE '[TEST-QUIZ] %';

-- Lấy id khóa học mẫu
SET @course_cs201 := (SELECT id FROM `courses` WHERE `course_code` = 'CS201' LIMIT 1);
SET @course_web101 := (SELECT id FROM `courses` WHERE `course_code` = 'WEB101' LIMIT 1);
SET @course_java101 := (SELECT id FROM `courses` WHERE `course_code` = 'JAVA101' LIMIT 1);
SET @lesson_cs201_1 := (SELECT id FROM `lessons` WHERE `course_id` = @course_cs201 AND `lesson_order` = 1 LIMIT 1);
SET @lesson_cs201_2 := (SELECT id FROM `lessons` WHERE `course_id` = @course_cs201 AND `lesson_order` = 2 LIMIT 1);
SET @lesson_cs201_3 := (SELECT id FROM `lessons` WHERE `course_id` = @course_cs201 AND `lesson_order` = 3 LIMIT 1);
SET @lesson_web101_1 := (SELECT id FROM `lessons` WHERE `course_id` = @course_web101 AND `lesson_order` = 1 LIMIT 1);
SET @lesson_web101_2 := (SELECT id FROM `lessons` WHERE `course_id` = @course_web101 AND `lesson_order` = 2 LIMIT 1);
SET @lesson_web101_3 := (SELECT id FROM `lessons` WHERE `course_id` = @course_web101 AND `lesson_order` = 3 LIMIT 1);
SET @lesson_java101_1 := (SELECT id FROM `lessons` WHERE `course_id` = @course_java101 AND `lesson_order` = 1 LIMIT 1);
SET @lesson_java101_2 := (SELECT id FROM `lessons` WHERE `course_id` = @course_java101 AND `lesson_order` = 2 LIMIT 1);
SET @lesson_java101_3 := (SELECT id FROM `lessons` WHERE `course_id` = @course_java101 AND `lesson_order` = 3 LIMIT 1);

-- =============================================================================
-- CS201 - DSA
-- =============================================================================

INSERT INTO `assignments`
(`course_id`, `title`, `description`, `type`, `due_date`, `max_score`, `allow_late_submission`, `max_attempts`)
VALUES
(@course_cs201,
 '[TEST-QUIZ] CS201 - Chương 1: Big-O và Asymptotic',
 'Quiz chương 1 môn Cấu trúc dữ liệu và giải thuật. Dùng để test auto-grade cho các câu hỏi về Big-O, Omega, Theta.',
 'QUIZ',
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 7 DAY), '23:59:00'),
 10.0,
 b'0',
 1);
SET @quiz_cs201_1 := LAST_INSERT_ID();

INSERT INTO `quiz_questions`
(`assignment_id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `question_order`, `points`)
VALUES
(@quiz_cs201_1, 'Ký hiệu O(f(n)) thường dùng để mô tả điều gì?', 'Cận dưới của độ phức tạp', 'Cận trên của độ phức tạp', 'Độ sâu của đệ quy', 'Số lượng biến trong chương trình', 'B', 1, 1.0),
(@quiz_cs201_1, 'Thuật toán duyệt qua toàn bộ mảng n phần tử đúng một lần có độ phức tạp nào?', 'O(log n)', 'O(n)', 'O(n log n)', 'O(n²)', 'B', 2, 1.0),
(@quiz_cs201_1, 'Ký hiệu Θ(f(n)) có nghĩa là gì?', 'Chỉ có cận dưới', 'Chỉ có cận trên', 'Cùng bậc tăng trưởng ở cả cận trên và cận dưới', 'Thuật toán chạy đúng f(n) mili giây', 'C', 3, 1.0),
(@quiz_cs201_1, 'Nếu một vòng lặp lồng trong một vòng lặp khác, mỗi vòng lặp chạy n lần, độ phức tạp xấp xỉ là?', 'O(n)', 'O(log n)', 'O(n²)', 'O(2n)', 'C', 4, 1.0),
(@quiz_cs201_1, 'Trường hợp nào sau đây thường được dùng mặc định khi báo cáo độ phức tạp?', 'Best-case', 'Worst-case', 'Random-case', 'Compile-time', 'B', 5, 1.0);

INSERT INTO `assignments`
(`course_id`, `title`, `description`, `type`, `due_date`, `max_score`, `allow_late_submission`, `max_attempts`)
VALUES
(@course_cs201,
 '[TEST-QUIZ] CS201 - Chương 2: Stack và Queue',
 'Quiz chương 2 môn Cấu trúc dữ liệu và giải thuật. Dùng để test câu hỏi về LIFO/FIFO, ứng dụng stack và queue.',
 'QUIZ',
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 9 DAY), '23:59:00'),
 10.0,
 b'0',
 1);
SET @quiz_cs201_2 := LAST_INSERT_ID();

INSERT INTO `quiz_questions`
(`assignment_id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `question_order`, `points`)
VALUES
(@quiz_cs201_2, 'Cấu trúc nào hoạt động theo nguyên tắc LIFO?', 'Queue', 'Stack', 'Binary Tree', 'Hash Table', 'B', 1, 1.0),
(@quiz_cs201_2, 'Thao tác thêm phần tử vào stack gọi là gì?', 'enqueue', 'append', 'push', 'insertRear', 'C', 2, 1.0),
(@quiz_cs201_2, 'Ứng dụng điển hình của queue là gì?', 'Undo thao tác', 'Duyệt BFS', 'Tính toán hash', 'Cân bằng cây AVL', 'B', 3, 1.0),
(@quiz_cs201_2, 'Nếu cài đặt hợp lý, push/pop trên stack thường có độ phức tạp?', 'O(1)', 'O(log n)', 'O(n)', 'O(n²)', 'A', 4, 1.0),
(@quiz_cs201_2, 'Queue tuân theo nguyên tắc nào?', 'Vào sau ra trước', 'Vào trước ra trước', 'Luôn sắp xếp tăng dần', 'Chỉ chứa số nguyên', 'B', 5, 1.0);

INSERT INTO `assignments`
(`course_id`, `title`, `description`, `type`, `due_date`, `max_score`, `allow_late_submission`, `max_attempts`)
VALUES
(@course_cs201,
 '[TEST-QUIZ] CS201 - Chương 3: BST và Duyệt cây',
 'Quiz chương 3 môn Cấu trúc dữ liệu và giải thuật. Dùng để test câu hỏi về BST, inorder, và trường hợp suy biến.',
 'QUIZ',
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 11 DAY), '23:59:00'),
 10.0,
 b'0',
 1);
SET @quiz_cs201_3 := LAST_INSERT_ID();

INSERT INTO `quiz_questions`
(`assignment_id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `question_order`, `points`)
VALUES
(@quiz_cs201_3, 'Trong BST, mọi khóa ở cây con trái của một nút phải như thế nào?', 'Lớn hơn khóa của nút', 'Nhỏ hơn khóa của nút', 'Bằng khóa của nút', 'Không có quy tắc', 'B', 1, 1.0),
(@quiz_cs201_3, 'Duyệt inorder trên BST cho kết quả gì?', 'Dãy khóa tăng dần', 'Dãy khóa giảm dần', 'Dãy ngẫu nhiên', 'Không xác định', 'A', 2, 1.0),
(@quiz_cs201_3, 'Nếu chèn các khóa đã sắp xếp tăng dần vào BST thường, cây có thể bị?', 'Cân bằng hoàn hảo', 'Suy biến thành dạng gần danh sách', 'Chuyển thành heap', 'Tự động xoay cân bằng', 'B', 3, 1.0),
(@quiz_cs201_3, 'Thuật toán duyệt theo chiều rộng trên cây thường dùng cấu trúc nào?', 'Stack', 'Queue', 'Set', 'Map', 'B', 4, 1.0),
(@quiz_cs201_3, 'Mục tiêu của cây cân bằng như AVL/Red-Black là gì?', 'Giảm bộ nhớ về 0', 'Giữ chiều cao gần O(log n)', 'Không cần phép so sánh', 'Loại bỏ hoàn toàn đệ quy', 'B', 5, 1.0);

-- =============================================================================
-- WEB101 - HTML/CSS
-- =============================================================================

INSERT INTO `assignments`
(`course_id`, `title`, `description`, `type`, `due_date`, `max_score`, `allow_late_submission`, `max_attempts`)
VALUES
(@course_web101,
 '[TEST-QUIZ] WEB101 - Chương 1: HTML5 Semantic',
 'Quiz chương 1 môn HTML & CSS. Dùng để test câu hỏi về semantic HTML, form và cấu trúc tài liệu.',
 'QUIZ',
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 8 DAY), '23:59:00'),
 10.0,
 b'0',
 1);
SET @quiz_web101_1 := LAST_INSERT_ID();

INSERT INTO `quiz_questions`
(`assignment_id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `question_order`, `points`)
VALUES
(@quiz_web101_1, 'Thẻ nào phù hợp nhất cho nội dung chính của trang?', '<div>', '<main>', '<span>', '<meta>', 'B', 1, 1.0),
(@quiz_web101_1, 'Thẻ nào nên dùng cho vùng điều hướng chính?', '<nav>', '<mark>', '<em>', '<aside>' ,'A', 2, 1.0),
(@quiz_web101_1, 'Thuộc tính nào liên kết label với input?', 'for', 'href', 'src', 'action', 'A', 3, 1.0),
(@quiz_web101_1, 'Thẻ nào dùng để tạo biểu mẫu nhập dữ liệu?', '<table>', '<article>', '<form>', '<section>', 'C', 4, 1.0),
(@quiz_web101_1, 'Khai báo nào giúp trình duyệt hiểu đây là tài liệu HTML5?', '<html5>', '<!DOCTYPE html>', '<meta html5>', '<document html>', 'B', 5, 1.0);

INSERT INTO `assignments`
(`course_id`, `title`, `description`, `type`, `due_date`, `max_score`, `allow_late_submission`, `max_attempts`)
VALUES
(@course_web101,
 '[TEST-QUIZ] WEB101 - Chương 2: CSS Box Model',
 'Quiz chương 2 môn HTML & CSS. Dùng để test câu hỏi về content, padding, border, margin và typography.',
 'QUIZ',
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 10 DAY), '23:59:00'),
 10.0,
 b'0',
 1);
SET @quiz_web101_2 := LAST_INSERT_ID();

INSERT INTO `quiz_questions`
(`assignment_id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `question_order`, `points`)
VALUES
(@quiz_web101_2, 'Box model theo thứ tự từ trong ra ngoài là gì?', 'content, padding, border, margin', 'margin, border, padding, content', 'content, margin, padding, border', 'padding, content, border, margin', 'A', 1, 1.0),
(@quiz_web101_2, 'Thuộc tính nào thường dùng để làm cách tính kích thước dễ kiểm soát hơn?', 'display: block', 'box-sizing: border-box', 'position: absolute', 'overflow: hidden', 'B', 2, 1.0),
(@quiz_web101_2, 'Khoảng cách ngoài cùng của một phần tử là gì?', 'padding', 'margin', 'border', 'font-size', 'B', 3, 1.0),
(@quiz_web101_2, 'Thuộc tính nào điều khiển họ font chữ?', 'font-family', 'font-style-only', 'text-box', 'letter-box', 'A', 4, 1.0),
(@quiz_web101_2, 'line-height thường dùng để cải thiện điều gì?', 'Tốc độ tải trang', 'Khả năng đọc của đoạn văn', 'Kích thước ảnh', 'Số cột trong grid', 'B', 5, 1.0);

INSERT INTO `assignments`
(`course_id`, `title`, `description`, `type`, `due_date`, `max_score`, `allow_late_submission`, `max_attempts`)
VALUES
(@course_web101,
 '[TEST-QUIZ] WEB101 - Chương 3: Flexbox và Responsive',
 'Quiz chương 3 môn HTML & CSS. Dùng để test câu hỏi về flexbox, justify-content, align-items và media query.',
 'QUIZ',
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 12 DAY), '23:59:00'),
 10.0,
 b'0',
 1);
SET @quiz_web101_3 := LAST_INSERT_ID();

INSERT INTO `quiz_questions`
(`assignment_id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `question_order`, `points`)
VALUES
(@quiz_web101_3, 'Thuộc tính nào căn phần tử theo trục chính trong Flexbox?', 'align-items', 'justify-content', 'flex-grow', 'z-index', 'B', 1, 1.0),
(@quiz_web101_3, 'Để kích hoạt flex layout, container cần có gì?', 'position: flex', 'display: flex', 'float: flex', 'layout: flex', 'B', 2, 1.0),
(@quiz_web101_3, 'Media query dùng để làm gì?', 'Ẩn JavaScript', 'Áp CSS theo điều kiện thiết bị/kích thước', 'Nén font chữ', 'Tạo API', 'B', 3, 1.0),
(@quiz_web101_3, 'Thiết kế mobile-first nghĩa là gì?', 'Thiết kế cho desktop trước', 'Thiết kế cho mobile trước rồi mở rộng dần', 'Chỉ hỗ trợ điện thoại', 'Luôn dùng fixed width', 'B', 4, 1.0),
(@quiz_web101_3, 'Thuộc tính nào thường dùng để tạo khoảng cách đều giữa item flex mà không cần margin thủ công?', 'gap', 'filter', 'opacity', 'clip-path', 'A', 5, 1.0);

-- =============================================================================
-- JAVA101 - Java cơ bản
-- =============================================================================

INSERT INTO `assignments`
(`course_id`, `title`, `description`, `type`, `due_date`, `max_score`, `allow_late_submission`, `max_attempts`)
VALUES
(@course_java101,
 '[TEST-QUIZ] JAVA101 - Chương 1: Java cơ bản và kiểu dữ liệu',
 'Quiz chương 1 môn Java cơ bản. Dùng để test câu hỏi về JVM, main method và kiểu dữ liệu.',
 'QUIZ',
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 8 DAY), '23:59:00'),
 10.0,
 b'0',
 1);
SET @quiz_java101_1 := LAST_INSERT_ID();

INSERT INTO `quiz_questions`
(`assignment_id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `question_order`, `points`)
VALUES
(@quiz_java101_1, 'Điểm vào chuẩn của chương trình Java là gì?', 'start()', 'run()', 'public static void main(String[] args)', 'init()', 'C', 1, 1.0),
(@quiz_java101_1, 'Kiểu nguyên thủy dùng để lưu true/false là?', 'String', 'int', 'boolean', 'char', 'C', 2, 1.0),
(@quiz_java101_1, 'Mã Java sau khi biên dịch thường chạy trên đâu?', 'Trực tiếp trên BIOS', 'JVM', 'Chỉ trên Linux kernel', 'SQL engine', 'B', 3, 1.0),
(@quiz_java101_1, 'Kiểu nào sau đây là kiểu tham chiếu?', 'int', 'double', 'boolean', 'String', 'D', 4, 1.0),
(@quiz_java101_1, 'Lớp String trong Java có đặc điểm nổi bật nào?', 'Luôn mutable', 'Bất biến', 'Chỉ chứa số', 'Không thể so sánh', 'B', 5, 1.0);

INSERT INTO `assignments`
(`course_id`, `title`, `description`, `type`, `due_date`, `max_score`, `allow_late_submission`, `max_attempts`)
VALUES
(@course_java101,
 '[TEST-QUIZ] JAVA101 - Chương 2: Lớp và Đối tượng',
 'Quiz chương 2 môn Java cơ bản. Dùng để test câu hỏi về class, object, constructor và từ khóa this.',
 'QUIZ',
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 10 DAY), '23:59:00'),
 10.0,
 b'0',
 1);
SET @quiz_java101_2 := LAST_INSERT_ID();

INSERT INTO `quiz_questions`
(`assignment_id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `question_order`, `points`)
VALUES
(@quiz_java101_2, 'Trong OOP, lớp (class) là gì?', 'Một giá trị boolean', 'Một khuôn mẫu tạo đối tượng', 'Một câu lệnh lặp', 'Một kiểu SQL', 'B', 1, 1.0),
(@quiz_java101_2, 'Hàm khởi tạo trong Java có đặc điểm nào?', 'Có kiểu trả về bắt buộc là void', 'Trùng tên lớp và không có kiểu trả về', 'Luôn là static', 'Không được có tham số', 'B', 2, 1.0),
(@quiz_java101_2, 'Từ khóa this thường dùng để làm gì?', 'Truy cập file hệ thống', 'Tham chiếu tới đối tượng hiện tại', 'Tạo thread mới', 'Đánh dấu biến static', 'B', 3, 1.0),
(@quiz_java101_2, 'Thuộc tính của đối tượng dùng để biểu diễn gì?', 'Trạng thái', 'Màu cú pháp', 'Tên package', 'Phiên bản JDK', 'A', 4, 1.0),
(@quiz_java101_2, 'Phương thức trong lớp biểu diễn gì?', 'Hành vi', 'Khóa ngoại', 'Chỉ số mảng', 'Mức cache CPU', 'A', 5, 1.0);

INSERT INTO `assignments`
(`course_id`, `title`, `description`, `type`, `due_date`, `max_score`, `allow_late_submission`, `max_attempts`)
VALUES
(@course_java101,
 '[TEST-QUIZ] JAVA101 - Chương 3: Kế thừa, Interface, Ngoại lệ',
 'Quiz chương 3 môn Java cơ bản. Dùng để test câu hỏi về extends, implements, try/catch và throws.',
 'QUIZ',
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 12 DAY), '23:59:00'),
 10.0,
 b'0',
 1);
SET @quiz_java101_3 := LAST_INSERT_ID();

INSERT INTO `quiz_questions`
(`assignment_id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `question_order`, `points`)
VALUES
(@quiz_java101_3, 'Từ khóa nào dùng để một lớp kế thừa lớp cha?', 'implements', 'inherits', 'extends', 'super', 'C', 1, 1.0),
(@quiz_java101_3, 'Một lớp triển khai interface bằng từ khóa nào?', 'extends', 'implements', 'throws', 'instanceof', 'B', 2, 1.0),
(@quiz_java101_3, 'Khối nào thường dùng để bắt và xử lý ngoại lệ?', 'if/else', 'switch/case', 'try/catch', 'for/while', 'C', 3, 1.0),
(@quiz_java101_3, 'Khai báo throws thường dùng khi nào?', 'Muốn báo phương thức có thể phát sinh ngoại lệ', 'Muốn tăng tốc chương trình', 'Muốn tạo constructor', 'Muốn đổi package', 'A', 4, 1.0),
(@quiz_java101_3, 'Mục đích của đa hình trong Java là gì?', 'Giảm số lượng file xuống 1', 'Cho phép tham chiếu kiểu cha trỏ tới đối tượng lớp con', 'Loại bỏ hoàn toàn kế thừa', 'Bắt buộc mọi lớp đều final', 'B', 5, 1.0);

UPDATE `assignments` SET `lesson_id` = @lesson_cs201_1, `minimum_passing_score` = 6.0 WHERE `id` = @quiz_cs201_1;
UPDATE `assignments` SET `lesson_id` = @lesson_cs201_2, `minimum_passing_score` = 6.0 WHERE `id` = @quiz_cs201_2;
UPDATE `assignments` SET `lesson_id` = @lesson_cs201_3, `minimum_passing_score` = 6.0 WHERE `id` = @quiz_cs201_3;
UPDATE `assignments` SET `lesson_id` = @lesson_web101_1, `minimum_passing_score` = 5.0 WHERE `id` = @quiz_web101_1;
UPDATE `assignments` SET `lesson_id` = @lesson_web101_2, `minimum_passing_score` = 5.0 WHERE `id` = @quiz_web101_2;
UPDATE `assignments` SET `lesson_id` = @lesson_web101_3, `minimum_passing_score` = 5.0 WHERE `id` = @quiz_web101_3;
UPDATE `assignments` SET `lesson_id` = @lesson_java101_1, `minimum_passing_score` = 5.0 WHERE `id` = @quiz_java101_1;
UPDATE `assignments` SET `lesson_id` = @lesson_java101_2, `minimum_passing_score` = 5.0 WHERE `id` = @quiz_java101_2;
UPDATE `assignments` SET `lesson_id` = @lesson_java101_3, `minimum_passing_score` = 5.0 WHERE `id` = @quiz_java101_3;

ALTER TABLE `assignments` AUTO_INCREMENT = 1000;
ALTER TABLE `quiz_questions` AUTO_INCREMENT = 5000;
