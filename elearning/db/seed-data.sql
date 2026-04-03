-- =============================================================================
-- Dữ liệu mẫu học thuật cho hệ thống E-Learning (MySQL 8, utf8mb4)
-- =============================================================================
-- Chạy sau khi đã áp dụng schema:  schema-standard.sql  (khuyến nghị) hoặc DDL tương đương.
-- ddl-auto=none trong application.properties.
-- Mật khẩu đăng nhập demo (tất cả tài khoản dưới đây): Demo@2024
-- Hash BCrypt (strength 10), tương thích BCryptPasswordEncoder của Spring Security.
--
-- Cách chạy (ví dụ):
--   mysql -u root -p --default-character-set=utf8mb4 e-learning < elearning/db/seed-data.sql
--
-- Lưu ý MySQL: TRUNCATE bảng cha thường lỗi #1701 dù tắt FK checks. Dùng DELETE theo thứ tự con → cha.
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
DELETE FROM `course_prerequisites`;
DELETE FROM `courses`;
DELETE FROM `users`;
DELETE FROM `departments`;

SET FOREIGN_KEY_CHECKS = 1;

-- Mật khẩu: Demo@2024
SET @pwd := '$2b$10$IgwunfhzoNX76OVOOWU1.eKSB7SmLMjNAdTvjYpuY79/U0ebJmcpy';

INSERT INTO `departments` (`id`, `code`, `name`) VALUES
(1, 'CNTT', 'Khoa Công nghệ Thông tin');

INSERT INTO `users` (`id`, `username`, `email`, `password`, `full_name`, `role`, `phone`, `active`, `locked`, `created_at`, `department_id`, `student_code`) VALUES
(1, 'gv.nguyenvanan', 'nguyen.van.an@univ.edu.vn', @pwd, 'TS. Nguyễn Văn An', 'TEACHER', '0901234567', 1, 0, '2024-08-15', 1, NULL),
(2, 'gv.tranthib', 'tran.thi.b@univ.edu.vn', @pwd, 'ThS. Trần Thị Bích', 'TEACHER', '0902345678', 1, 0, '2024-08-15', 1, NULL),
(3, 'sv.phamminhc', 'pham.minh.c@student.univ.edu.vn', @pwd, 'Phạm Minh Châu', 'STUDENT', '0913456789', 1, 0, '2024-09-01', 1, 'B2201001'),
(4, 'sv.lethid', 'le.thi.d@student.univ.edu.vn', @pwd, 'Lê Thị Diệu', 'STUDENT', '0914567890', 1, 0, '2024-09-01', 1, 'B2201002'),
(5, 'sv.hoangvane', 'hoang.van.e@student.univ.edu.vn', @pwd, 'Hoàng Văn Em', 'STUDENT', '0915678901', 1, 0, '2024-09-01', 1, 'B2201003'),
(6, 'sv.vothif', 'vo.thi.f@student.univ.edu.vn', @pwd, 'Võ Thị Phương', 'STUDENT', '0916789012', 1, 0, '2024-09-02', 1, 'B2201004'),
(7, 'sv.dangvang', 'dang.van.g@student.univ.edu.vn', @pwd, 'Đặng Văn Giang', 'STUDENT', '0917890123', 1, 0, '2024-09-02', 1, 'B2201005');

INSERT INTO `courses` (`id`, `course_code`, `course_name`, `description`, `teacher_id`, `enroll_password`, `semester`, `academic_year`, `status`, `max_students`, `thumbnail`, `department_id`, `credits`, `theory_hours`, `practice_hours`) VALUES
(1, 'CS201', 'Cấu trúc dữ liệu và giải thuật (DSA)',
'Môn nền tảng: phân tích độ phức tạp (Big-O), danh sách, stack, queue, cây (BST), sắp xếp, tìm kiếm và duyệt đồ thị cơ bản. Phù hợp CTĐT CNTT.',
1, 'CS201HK1', 'HK1', '2024-2025', 'PUBLISHED', 60,
'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&q=80', 1, 4, 45, 30),

(2, 'WEB101', 'HTML &amp; CSS — Thiết kế trang web',
'Ngữ nghĩa HTML5, biểu mẫu, CSS (box model, màu sắc, typography), bố cục Flexbox và responsive cơ bản. Nội dung hiển thị trực tiếp trên LMS; tài liệu PDF/DOC có thể bổ sung sau qua mục tài liệu hoặc liên kết ngoài.',
2, 'WEB101HK1', 'HK1', '2024-2025', 'PUBLISHED', 50,
'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800&q=80', 1, 3, 30, 45),

(3, 'JAVA101', 'Lập trình Java cơ bản',
'Cú pháp Java, kiểu dữ liệu, điều khiển, mảng, lớp và đối tượng, kế thừa, đa hình, interface, xử lý ngoại lệ, generics giới thiệu và Collection cơ bản.',
1, 'JAVA101HK1', 'HK1', '2024-2025', 'PUBLISHED', 55,
'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&q=80', 1, 3, 30, 45);

-- Tiên quyết (mã CTĐT): WEB101 và JAVA101 gợi ý đã qua CS201
INSERT INTO `course_prerequisites` (`id`, `course_id`, `prerequisite_course_code`) VALUES
(1, 2, 'CS201'),
(2, 3, 'CS201');

INSERT INTO `lessons` (`id`, `course_id`, `lesson_order`, `lesson_title`, `lesson_content`, `video_url`, `duration_minutes`, `published`) VALUES
(1, 1, 1, 'Phân tích độ phức tạp và ký hiệu asymptotic',
CONCAT(
'<h2>Mục tiêu học tập</h2><ul>',
'<li>Phân biệt thời gian chạy thực nghiệm và phân tích tiệm cận.</li>',
'<li>Sử dụng ký hiệu O, Omega, Theta để mô tả tốc độ tăng trưởng.</li></ul>',
'<h2>Nội dung cốt lõi</h2>',
'<p>Khi đánh giá thuật toán, ta quan tâm hành vi khi kích thước đầu vào <em>n</em> đủ lớn. Ký hiệu <strong>O(f(n))</strong> mô tả cận trên (worst-case thường dùng trong giảng dạy); Omega mô tả cận dưới; Theta khi cận trên và dưới cùng bậc.</p>',
'<p>Ví dụ: nếu số phép gán trong hai vòng lặp lồng nhau tỷ lệ n(n-1)/2 thì độ phức tạp thời gian là Theta(n²), không phải Theta(n³).</p>',
'<h2>Ghi chú</h2><p>Phân tích asymptotic mô tả xu hướng khi n lớn; không thay thế benchmark trên dữ liệu thực.</p>',
'<div class="material-hint"><strong>Về PDF/DOCX:</strong> Nội dung bài học trên LMS là HTML. Giảng viên có thể đính kèm thêm file PDF hoặc DOCX qua kho tài liệu khóa học hoặc liên kết ngoài — sinh viên tải về để đọc sâu.</div>'
),
NULL, 90, 1),

(2, 1, 2, 'Danh sách liên kết, stack và queue',
CONCAT(
'<h2>Mục tiêu</h2><ul>',
'<li>So sánh mảng với danh sách liên kết đơn/kép.</li>',
'<li>Hiểu stack (LIFO) và queue (FIFO).</li></ul>',
'<h2>Stack</h2><p>Thao tác push/pop ở một đầu. Ứng dụng: duyệt DFS, kiểm tra ngoặc, undo.</p>',
'<h2>Queue</h2><p>Enqueue ở đuôi, dequeue ở đầu. Ứng dụng: BFS theo lớp, xử lý hàng đợi tác vụ.</p>',
'<h2>Độ phức tạp</h2><p>Với cài đặt chuẩn, push/pop/enqueue/dequeue đều O(1) trong mô hình tính toán thông thường.</p>'
),
NULL, 90, 1),

(3, 1, 3, 'Cây BST và duyệt cây',
CONCAT(
'<h2>Định nghĩa BST</h2>',
'<p>Cây nhị phân tìm kiếm: với mỗi nút v, mọi khóa cây con trái nhỏ hơn khóa(v), mọi khóa cây con phải lớn hơn (giả sử không trùng khóa).</p>',
'<h2>Trường hợp xấu nhất</h2>',
'<p>Chèn theo thứ tự đã sắp có thể làm BST suy biến thành danh sách; chiều cao O(n). Cây cân bằng (AVL, Red-Black) giữ chiều cao O(log n).</p>',
'<h2>Duyệt cây</h2><p>Preorder, inorder, postorder. Duyệt inorder trên BST cho dãy khóa tăng dần.</p>'
),
NULL, 90, 1),

(4, 2, 1, 'HTML5: cấu trúc và ngữ nghĩa',
CONCAT(
'<h2>Giới thiệu</h2>',
'<p>HTML mô tả cấu trúc tài liệu web: tiêu đề, đoạn, danh sách, liên kết, biểu mẫu. HTML5 chuẩn hóa các thẻ ngữ nghĩa như <code>header</code>, <code>nav</code>, <code>main</code>, <code>article</code>, <code>section</code>, <code>footer</code>.</p>',
'<h2>Ví dụ khung trang</h2>',
'<pre>&lt;!DOCTYPE html&gt;\n&lt;html lang="vi"&gt;\n&lt;head&gt;&lt;meta charset="UTF-8"&gt;&lt;title&gt;Trang mẫu&lt;/title&gt;&lt;/head&gt;\n&lt;body&gt;&lt;h1&gt;Tiêu đề&lt;/h1&gt;&lt;p&gt;Đoạn văn.&lt;/p&gt;&lt;/body&gt;\n&lt;/html&gt;</pre>',
'<h2>Form cơ bản</h2>',
'<p>Dùng <code>label</code> gắn với <code>input</code>, <code>type</code> phù hợp (text, email, password), và <code>button type="submit"</code> để gửi dữ liệu.</p>'
),
NULL, 60, 1),

(5, 2, 2, 'CSS: box model, màu và typography',
CONCAT(
'<h2>Box model</h2>',
'<p>Mỗi phần tử có <strong>content</strong>, <strong>padding</strong>, <strong>border</strong>, <strong>margin</strong>. Thuộc tính <code>box-sizing: border-box</code> giúp tính kích thước trực quan hơn.</p>',
'<h2>Màu và font</h2>',
'<p>Dùng mã hex, rgb/rgba, hoặc tên màu. Khai báo font-family với danh sách dự phòng; <code>line-height</code> cải thiện khả năng đọc.</p>',
'<h2>Ví dụ</h2>',
'<pre>.card {\n  max-width: 480px;\n  padding: 1rem;\n  border: 1px solid #e0e7ef;\n  border-radius: 8px;\n}</pre>'
),
NULL, 60, 1),

(6, 2, 3, 'Flexbox và responsive cơ bản',
CONCAT(
'<h2>Flexbox</h2>',
'<p>Đặt <code>display: flex</code> trên container; dùng <code>justify-content</code> (trục chính) và <code>align-items</code> (trục phụ) để căn chỉnh hàng/cột.</p>',
'<h2>Responsive</h2>',
'<p>Thiết kế mobile-first: bắt đầu từ layout hẹp, sau đó dùng <code>@media (min-width: ...)</code> để mở rộng. Đơn vị <code>rem</code>/<code>%</code> giúp co giãn chữ và khối.</p>',
'<h2>Gợi ý</h2>',
'<p>Kết hợp Flexbox với <code>gap</code> để tạo khoảng cách đều giữa các mục mà không cần margin thủ công.</p>'
),
NULL, 60, 1),

(7, 3, 1, 'Java: JVM, Hello World và kiểu dữ liệu',
CONCAT(
'<h2>Nền tảng Java</h2>',
'<p>Mã nguồn <code>.java</code> biên dịch thành bytecode chạy trên JVM — đa nền tảng. Mỗi ứng dụng có hàm <code>public static void main(String[] args)</code> làm điểm vào.</p>',
'<h2>Hello World</h2>',
'<pre>public class Hello {\n  public static void main(String[] args) {\n    System.out.println("Xin chao");\n  }\n}</pre>',
'<h2>Kiểu dữ liệu</h2>',
'<p>Kiểu nguyên thủy: <code>int</code>, <code>long</code>, <code>double</code>, <code>boolean</code>, <code>char</code>. Kiểu tham chiếu: chuỗi, mảng, đối tượng. Chuỗi bất biến dùng lớp <code>String</code>.</p>'
),
NULL, 75, 1),

(8, 3, 2, 'Lớp, đối tượng và constructor',
CONCAT(
'<h2>Lớp và đối tượng</h2>',
'<p>Lớp là khuôn mẫu; đối tượng là thể hiện. Thuộc tính lưu trạng thái, phương thức định nghĩa hành vi. Từ khóa <code>this</code> tham chiếu thể hiện hiện tại.</p>',
'<h2>Constructor</h2>',
'<p>Hàm khởi tạo trùng tên lớp, không kiểu trả về. Có thể nạp chồng constructor. Khối khởi tạo chạy trước thân constructor.</p>',
'<h2>Ví dụ ngắn</h2>',
'<pre>public class Point {\n  private final int x, y;\n  public Point(int x, int y) { this.x = x; this.y = y; }\n  public int sum() { return x + y; }\n}</pre>'
),
NULL, 75, 1),

(9, 3, 3, 'Kế thừa, interface và xử lý ngoại lệ',
CONCAT(
'<h2>Kế thừa</h2>',
'<p>Dùng <code>extends</code> cho một lớp cha. Ghi đè phương thức với <code>@Override</code>; đa hình cho phép tham chiếu kiểu cha trỏ tới thể hiện lớp con.</p>',
'<h2>Interface</h2>',
'<p>Định nghĩa hợp đồng hành vi; lớp <code>implements</code> một hoặc nhiều interface. Từ Java 8 có phương thức <code>default</code> trên interface.</p>',
'<h2>Ngoại lệ</h2>',
'<p>Dùng <code>try / catch / finally</code>; ném ngoại lệ với <code>throw</code>. Ưu tiên bắt ngoại lệ cụ thể; tránh bắt <code>Exception</code> quá rộng nếu không cần.</p>'
),
NULL, 75, 1);

INSERT INTO `enrollments` (`id`, `student_id`, `course_id`, `enrolled_at`, `status`, `progress_percentage`, `activity_xp`, `total_xp`) VALUES
(1, 3, 1, '2024-09-05', 'ACTIVE', 35, 40, 120),
(2, 4, 1, '2024-09-06', 'ACTIVE', 28, 15, 95),
(3, 5, 1, '2024-09-07', 'ACTIVE', 15, 0, 40),
(4, 3, 2, '2024-09-08', 'ACTIVE', 20, 20, 60),
(5, 6, 2, '2024-09-08', 'ACTIVE', 10, 10, 30),
(6, 7, 3, '2024-09-09', 'ACTIVE', 12, 5, 35);

INSERT INTO `assignments` (`id`, `course_id`, `title`, `description`, `type`, `due_date`, `max_score`) VALUES
(1, 1, 'Kiểm tra nhanh: Độ phức tạp và cấu trúc dữ liệu cơ bản',
'Làm trắc nghiệm. Thời gian gợi ý: 25 phút. Đọc kỹ đề trước khi chọn đáp án.',
'QUIZ', '2025-12-15 23:59:00', 10.0),

(2, 1, 'Bài tập: Phân tích độ phức tạp của hai đoạn giả mã',
'Cho hai đoạn giả mã trong tài liệu bài 1. Yêu cầu: xác định Big-O theo n, giải thích từng bước lập luận (số vòng lặp, chi phối số hạng). Nộp file PDF hoặc văn bản trong khung nộp bài.',
'HOMEWORK', '2025-12-20 23:59:00', 10.0),

(3, 2, 'Quiz: HTML5 và CSS cơ bản',
'Trắc nghiệm về thẻ ngữ nghĩa, form, box model và Flexbox.',
'QUIZ', '2025-12-18 23:59:00', 10.0),

(4, 3, 'Quiz: Java cơ bản',
'Trắc nghiệm về kiểu dữ liệu, lớp, kế thừa và xử lý ngoại lệ.',
'QUIZ', '2025-12-22 23:59:00', 10.0);

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

INSERT INTO `quiz_questions` (`id`, `assignment_id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `question_order`, `points`) VALUES
(1, 1,
'Cho đoạn mã duyệt một mảng một chiều n phần tử đúng một lần, mỗi phần tử thực hiện O(1) phép toán. Độ phức tạp thời gian tiệm cận (worst-case) là gì?',
'O(n²)', 'O(n)', 'O(log n)', 'O(1)', 'B', 1, 2.0),

(2, 1,
'Cấu trúc dữ liệu nào phù hợp nhất với nguyên tắc "vào sau ra trước" (LIFO)?',
'Hàng đợi (queue)', 'Ngăn xếp (stack)', 'Danh sách liên kết đơn (chỉ duyệt từ đầu)', 'Bảng băm (hash table)', 'B', 2, 2.0),

(3, 1,
'Trong BST không cân bằng, trường hợp xấu nhất khi chèn n khóa đã sắp xếp có thể dẫn đến chiều cao cây xấp xỉ:',
'log n', 'n', '√n', '1', 'B', 3, 2.0),

(4, 1,
'Thuật toán tìm kiếm nhị phân trên mảng đã sắp có độ phức tạp thời gian (worst-case) là:',
'O(n)', 'O(log n)', 'O(n log n)', 'O(1)', 'B', 4, 2.0),

(5, 1,
'Ký hiệu Θ(f(n)) nghĩa là:',
'Chỉ cận trên', 'Chỉ cận dưới', 'Cận trên và cận dưới cùng bậc (cùng tốc độ tăng)', 'Luôn tương đương O(n²)', 'C', 5, 2.0),

(6, 3,
'Thẻ HTML5 nào thích hợp cho khối nội dung chính của trang?',
'&lt;div&gt;', '&lt;main&gt;', '&lt;span&gt;', '&lt;meta&gt;', 'B', 1, 2.5),

(7, 3,
'Thuộc tính CSS nào thường dùng cùng Flexbox để căn các phần tử theo trục chính?',
'align-items', 'justify-content', 'float', 'z-index', 'B', 2, 2.5),

(8, 3,
'Box model gồm các phần nào (từ trong ra ngoài)?',
'content, padding, border, margin', 'margin, border, padding, content', 'chỉ content và margin', 'chỉ padding', 'A', 3, 2.5),

(9, 3,
'Media query trong CSS dùng để:',
'Chỉ đổi màu nền', 'Áp dụng style theo kích thước màn hình hoặc điều kiện thiết bị', 'Tắt JavaScript', 'Nén ảnh tự động', 'B', 4, 2.5),

(10, 4,
'Kiểu nguyên thủy nào trong Java dùng để lưu true/false?',
'int', 'boolean', 'String', 'double', 'B', 1, 2.5),

(11, 4,
'Từ khóa nào dùng để lớp con kế thừa lớp cha?',
'implements', 'extends', 'inherits', 'superclass', 'B', 2, 2.5),

(12, 4,
'Khối nào bắt buộc có khi xử lý checked exception trong Java?',
'if', 'try-catch hoặc khai báo throws', 'switch', 'for', 'B', 3, 2.5),

(13, 4,
'Phương thức khởi tạo trong Java có kiểu trả về là gì?',
'void', 'int', 'không có kiểu trả về (trùng tên lớp)', 'Object', 'C', 4, 2.5);

INSERT INTO `forum_posts` (`id`, `course_id`, `author_id`, `title`, `content`, `post_type`, `view_count`, `created_at`) VALUES
(1, 1, 3, 'Thắc mắc về phân biệt O và Θ',
'Thầy/cô và các bạn cho em hỏi: nếu thuật toán có worst-case O(n²) nhưng trung bình O(n log n) thì khi báo cáo độ phức tạp nên nêu rõ ngữ cảnh worst-case hay average-case? Em cảm ơn.',
'QUESTION', 42, '2024-10-01 09:15:00'),

(2, 1, 2, 'Tài liệu tham khảo: Introduction to Algorithms (Cormen et al.)',
'Nhắc nhóm mình có thể đối chiếu định nghĩa asymptotic trong sách Cormen, chương 3. Thư viện trường có bản mượn.',
'ANNOUNCEMENT', 128, '2024-10-03 14:00:00'),

(3, 2, 4, 'Flexbox: justify-content vs align-items',
'Mình hay nhầm hai thuộc tính này khi đổi trục row/column. Có mẹo gốc nhớ nhanh không các bạn?',
'DISCUSSION', 33, '2024-10-05 11:20:00'),

(4, 3, 5, 'Checked exception và throws',
'Khi nào nên khai báo throws trên main thay vì bọc try-catch? Em muốn hiểu convention trong dự án thực tế.',
'QUESTION', 19, '2024-10-08 16:45:00');

INSERT INTO `comments` (`id`, `post_id`, `author_id`, `content`, `created_at`) VALUES
(1, 1, 1, 'Em nên nêu rõ worst-case, average-case và best-case nếu có dữ liệu; trong báo cáo khoa học thường ghi worst-case cho giới hạn trên và trung bình nếu phân phối đầu vào được giả định.', '2024-10-01 10:05:00'),
(2, 1, 4, 'Mình hay ghi: "Worst-case time O(...); expected time O(...)" nếu có randomized algorithm.', '2024-10-01 11:30:00'),
(3, 3, 2, 'Với Flexbox: nhớ trục chính phụ thuộc flex-direction; justify-content căn theo trục chính, align-items theo trục phụ (khi row thì ngang/dọc đổi vai khi chuyển column).', '2024-10-05 15:00:00');

INSERT INTO `submissions` (`id`, `assignment_id`, `student_id`, `content`, `file_url`, `score`, `feedback`, `status`, `submitted_at`) VALUES
(1, 1, 3, '{"answers":["B","B","B","B","C"]}', NULL, 10.0, 'Đáp án đúng cả 5 câu. Rất tốt.', 'GRADED', '2024-11-10 20:00:00'),
(2, 1, 4, '{"answers":["B","B","B","B","C"]}', NULL, 8.0, 'Đúng 4/5; câu 4 cần ôn lại tìm kiếm nhị phân.', 'GRADED', '2024-11-11 18:30:00'),
(3, 2, 3,
'Phân tích đoạn 1: vòng lặp đơn O(n). Đoạn 2: hai vòng lồng O(n²). Giải thích: số lần lặp lồng tỷ lệ n(n-1)/2 ~ Θ(n²).',
NULL, 9.0, 'Lập luận chặt chẽ, trình bày rõ ràng.', 'GRADED', '2024-11-12 09:00:00');

INSERT INTO `lesson_progress` (`id`, `student_id`, `lesson_id`, `completed`, `completed_at`) VALUES
(1, 3, 1, 1, '2024-09-20'),
(2, 3, 2, 1, '2024-09-22'),
(3, 4, 1, 1, '2024-09-21'),
(4, 6, 4, 1, '2024-10-10');

INSERT INTO `notes` (`id`, `student_id`, `lesson_id`, `course_id`, `content`, `highlight_color`, `title`, `note_type`, `source_excerpt`, `tags`, `created_at`, `updated_at`) VALUES
(1, 3, 1, 1, 'Nhớ phân biệt worst-case và average-case khi báo cáo.', '#FFF59D', 'Ghi chú bài 1', 'LESSON',
'Ký hiệu O(f(n)) mô tả cận trên (worst-case thường dùng trong giảng dạy); Omega mô tả cận dưới.',
'big-o,asymptotic,exam',
'2024-09-20 10:00:00', '2024-09-20 10:00:00'),
(2, 4, NULL, 1, 'Ôn lại định nghĩa BST và trường hợp suy biến.', '#FFCCBC', 'Ôn tập giữa kỳ', 'STANDALONE', NULL,
'bst,tree,review',
'2024-10-01 08:00:00', '2024-10-01 08:00:00');

INSERT INTO `note_links` (`id`, `from_note_id`, `to_note_id`, `relation_label`, `created_at`) VALUES
(1, 2, 1, 'relates-to', '2024-10-01 09:00:00');

INSERT INTO `notifications` (`id`, `user_id`, `title`, `message`, `type`, `is_read`, `created_at`) VALUES
(1, 3, 'Điểm bài kiểm tra đã có', 'Bài "Kiểm tra nhanh: Độ phức tạp..." đã được chấm. Điểm: 10/10.', 'GRADE', 1, '2024-11-11 08:00:00'),
(2, 4, 'Điểm bài kiểm tra đã có', 'Bài kiểm tra đã được chấm. Xem phản hồi trong mục nộp bài.', 'GRADE', 0, '2024-11-11 08:05:00'),
(3, 3, 'Thông báo khóa học', 'Diễn đàn CS201 có thêm công bố tài liệu tham khảo.', 'ANNOUNCEMENT', 0, '2024-10-03 14:30:00');

INSERT INTO `badge_definitions`
(`id`, `code`, `name`, `description`, `icon`, `criterion_type`, `threshold_value`, `active`, `display_order`) VALUES
(1, 'FIRST_STEP', 'Bước khởi đầu', 'Hoàn thành 1 bài học đầu tiên trong hệ thống.', 'rocket_launch', 'COMPLETED_LESSONS', 1, 1, 1),
(2, 'LEARNING_STREAK', 'Tiến bộ bền bỉ', 'Hoàn thành ít nhất 5 bài học.', 'local_fire_department', 'COMPLETED_LESSONS', 5, 1, 2),
(3, 'FIRST_GRADE', 'Có điểm đầu tiên', 'Có ít nhất 1 bài nộp đã được chấm điểm.', 'fact_check', 'GRADED_SUBMISSIONS', 1, 1, 3),
(4, 'QUIZ_ACE', 'Chuyên gia quiz', 'Đạt điểm tuyệt đối ở ít nhất 1 bài quiz.', 'psychology', 'PERFECT_QUIZZES', 1, 1, 4),
(5, 'XP_BRONZE', 'XP Đồng', 'Đạt ít nhất 100 XP tích lũy.', 'workspace_premium', 'TOTAL_XP', 100, 1, 5),
(6, 'XP_SILVER', 'XP Bạc', 'Đạt ít nhất 300 XP tích lũy.', 'workspace_premium', 'TOTAL_XP', 300, 1, 6),
(7, 'COURSE_FINISHER', 'Hoàn tất học phần', 'Hoàn thành ít nhất 1 khóa học với tiến độ 100%.', 'school', 'COMPLETED_COURSES', 1, 1, 7);

-- Reset AUTO_INCREMENT để id không chồng lấn khi thêm bản ghi mới sau này
ALTER TABLE `departments` AUTO_INCREMENT = 100;
ALTER TABLE `users` AUTO_INCREMENT = 100;
ALTER TABLE `badge_definitions` AUTO_INCREMENT = 100;
ALTER TABLE `user_badges` AUTO_INCREMENT = 100;
ALTER TABLE `courses` AUTO_INCREMENT = 100;
ALTER TABLE `course_prerequisites` AUTO_INCREMENT = 100;
ALTER TABLE `lessons` AUTO_INCREMENT = 100;
ALTER TABLE `enrollments` AUTO_INCREMENT = 100;
ALTER TABLE `assignments` AUTO_INCREMENT = 100;
ALTER TABLE `quiz_questions` AUTO_INCREMENT = 100;
ALTER TABLE `forum_posts` AUTO_INCREMENT = 100;
ALTER TABLE `comments` AUTO_INCREMENT = 100;
ALTER TABLE `submissions` AUTO_INCREMENT = 100;
ALTER TABLE `lesson_progress` AUTO_INCREMENT = 100;
ALTER TABLE `notes` AUTO_INCREMENT = 100;
ALTER TABLE `note_links` AUTO_INCREMENT = 100;
ALTER TABLE `notifications` AUTO_INCREMENT = 100;
