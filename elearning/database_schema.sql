-- =====================================================
-- DATABASE SCHEMA CHO HỆ THỐNG E-LEARNING
-- Khoa Công nghệ Thông tin
-- =====================================================
-- Tài liệu tham khảo: Group_15_SRS.docx
-- Ngày tạo: 2024
-- =====================================================

-- Xóa database cũ nếu tồn tại (cẩn thận khi sử dụng)
-- DROP DATABASE IF EXISTS elearning_db;
-- CREATE DATABASE elearning_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE elearning_db;

-- =====================================================
-- 1. BẢNG NGƯỜI DÙNG (USERS)
-- =====================================================
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('STUDENT', 'TEACHER', 'ADMIN') NOT NULL DEFAULT 'STUDENT',
    avatar_url VARCHAR(255) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    date_of_birth DATE DEFAULT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    is_locked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL DEFAULT NULL,
    INDEX idx_email (email),
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 2. BẢNG KHÓA HỌC (COURSES)
-- =====================================================
CREATE TABLE courses (
    course_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    course_name VARCHAR(200) NOT NULL,
    description TEXT,
    teacher_id BIGINT NOT NULL,
    password VARCHAR(100) DEFAULT NULL COMMENT 'Mật khẩu đăng ký khóa học',
    semester VARCHAR(20) DEFAULT NULL COMMENT 'Học kỳ (VD: 2024-1)',
    academic_year VARCHAR(10) DEFAULT NULL,
    thumbnail_url VARCHAR(255) DEFAULT NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT',
    max_students INT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    INDEX idx_teacher (teacher_id),
    INDEX idx_course_code (course_code),
    INDEX idx_semester (semester)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 3. BẢNG BÀI HỌC (LESSONS)
-- =====================================================
CREATE TABLE lessons (
    lesson_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    lesson_title VARCHAR(200) NOT NULL,
    lesson_content TEXT,
    lesson_order INT NOT NULL DEFAULT 0,
    video_url VARCHAR(255) DEFAULT NULL,
    document_url VARCHAR(255) DEFAULT NULL,
    duration_minutes INT DEFAULT NULL,
    is_published BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    INDEX idx_course (course_id),
    INDEX idx_order (course_id, lesson_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. BẢNG ĐĂNG KÝ KHÓA HỌC (ENROLLMENTS)
-- =====================================================
CREATE TABLE enrollments (
    enrollment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'COMPLETED', 'DROPPED') DEFAULT 'ACTIVE',
    progress_percentage DECIMAL(5,2) DEFAULT 0.00,
    total_xp INT DEFAULT 0 COMMENT 'Điểm kinh nghiệm tích lũy',
    UNIQUE KEY unique_enrollment (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    INDEX idx_student (student_id),
    INDEX idx_course (course_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 5. BẢNG THEO DÕI TIẾN ĐỘ BÀI HỌC (LESSON_PROGRESS)
-- =====================================================
CREATE TABLE lesson_progress (
    progress_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,
    watched_duration_seconds INT DEFAULT 0,
    last_position_seconds INT DEFAULT 0 COMMENT 'Vị trí dừng lại khi xem video',
    completed_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_progress (student_id, lesson_id),
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE,
    INDEX idx_student_lesson (student_id, lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 6. BẢNG BÀI TẬP/BÀI KIỂM TRA (ASSIGNMENTS)
-- =====================================================
CREATE TABLE assignments (
    assignment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    lesson_id BIGINT DEFAULT NULL COMMENT 'Bài tập gắn với bài học cụ thể (nếu có)',
    title VARCHAR(200) NOT NULL,
    description TEXT,
    assignment_type ENUM('QUIZ', 'HOMEWORK', 'EXAM') NOT NULL,
    max_score DECIMAL(5,2) NOT NULL DEFAULT 100.00,
    time_limit_minutes INT DEFAULT NULL COMMENT 'Thời gian làm bài (phút)',
    due_date DATETIME DEFAULT NULL,
    allow_late_submission BOOLEAN DEFAULT FALSE,
    is_published BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE SET NULL,
    INDEX idx_course (course_id),
    INDEX idx_lesson (lesson_id),
    INDEX idx_type (assignment_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 7. BẢNG CÂU HỎI TRẮC NGHIỆM (QUIZ_QUESTIONS)
-- =====================================================
CREATE TABLE quiz_questions (
    question_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    question_type ENUM('MULTIPLE_CHOICE', 'TRUE_FALSE', 'SHORT_ANSWER') DEFAULT 'MULTIPLE_CHOICE',
    points DECIMAL(5,2) DEFAULT 1.00,
    question_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id) ON DELETE CASCADE,
    INDEX idx_assignment (assignment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 8. BẢNG ĐÁP ÁN (QUESTION_OPTIONS)
-- =====================================================
CREATE TABLE question_options (
    option_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    option_order INT DEFAULT 0,
    FOREIGN KEY (question_id) REFERENCES quiz_questions(question_id) ON DELETE CASCADE,
    INDEX idx_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 9. BẢNG BÀI NỘP (SUBMISSIONS)
-- =====================================================
CREATE TABLE submissions (
    submission_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    assignment_id BIGINT NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    submitted_file_url VARCHAR(255) DEFAULT NULL COMMENT 'Đường dẫn file nộp bài (nếu là homework)',
    submission_text TEXT DEFAULT NULL COMMENT 'Nội dung bài nộp dạng text',
    score DECIMAL(5,2) DEFAULT NULL,
    is_graded BOOLEAN DEFAULT FALSE,
    is_late BOOLEAN DEFAULT FALSE,
    feedback TEXT DEFAULT NULL COMMENT 'Nhận xét từ giảng viên',
    graded_at TIMESTAMP NULL DEFAULT NULL,
    graded_by BIGINT DEFAULT NULL COMMENT 'Giảng viên chấm bài',
    status ENUM('DRAFT', 'SUBMITTED', 'GRADED', 'RETURNED') DEFAULT 'DRAFT',
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id) ON DELETE CASCADE,
    FOREIGN KEY (graded_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_student (student_id),
    INDEX idx_assignment (assignment_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 10. BẢNG CÂU TRẢ LỜI TRẮC NGHIỆM (QUIZ_ANSWERS)
-- =====================================================
CREATE TABLE quiz_answers (
    answer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option_id BIGINT DEFAULT NULL COMMENT 'Đáp án đã chọn (cho multiple choice)',
    answer_text TEXT DEFAULT NULL COMMENT 'Câu trả lời tự luận',
    is_correct BOOLEAN DEFAULT NULL,
    points_earned DECIMAL(5,2) DEFAULT 0.00,
    FOREIGN KEY (submission_id) REFERENCES submissions(submission_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES quiz_questions(question_id) ON DELETE CASCADE,
    FOREIGN KEY (selected_option_id) REFERENCES question_options(option_id) ON DELETE SET NULL,
    INDEX idx_submission (submission_id),
    INDEX idx_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 11. BẢNG GHI CHÚ (NOTES)
-- =====================================================
CREATE TABLE notes (
    note_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    highlighted_text TEXT DEFAULT NULL COMMENT 'Đoạn văn bản được highlight',
    note_content TEXT NOT NULL,
    highlight_color VARCHAR(20) DEFAULT '#FFFF00' COMMENT 'Màu highlight',
    text_position_start INT DEFAULT NULL COMMENT 'Vị trí bắt đầu trong văn bản',
    text_position_end INT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE,
    INDEX idx_student (student_id),
    INDEX idx_lesson (lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 12. BẢNG DIỄN ĐÀN/BÀI VIẾT (FORUM_POSTS)
-- =====================================================
CREATE TABLE forum_posts (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    post_type ENUM('QUESTION', 'DISCUSSION', 'ANNOUNCEMENT') DEFAULT 'DISCUSSION',
    is_pinned BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    view_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_course (course_id),
    INDEX idx_author (author_id),
    INDEX idx_type (post_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 13. BẢNG BÌNH LUẬN (COMMENTS)
-- =====================================================
CREATE TABLE comments (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    parent_comment_id BIGINT DEFAULT NULL COMMENT 'Bình luận cha (để hỗ trợ reply)',
    content TEXT NOT NULL,
    is_edited BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES forum_posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE,
    INDEX idx_post (post_id),
    INDEX idx_author (author_id),
    INDEX idx_parent (parent_comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 14. BẢNG HUY HIỆU (BADGES)
-- =====================================================
CREATE TABLE badges (
    badge_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    badge_name VARCHAR(100) NOT NULL,
    badge_description TEXT,
    badge_icon_url VARCHAR(255) DEFAULT NULL,
    badge_type ENUM('COURSE_COMPLETION', 'HIGH_SCORE', 'CONSISTENCY', 'PARTICIPATION', 'CUSTOM') DEFAULT 'CUSTOM',
    criteria_xp INT DEFAULT NULL COMMENT 'Điểm XP cần đạt',
    criteria_score DECIMAL(5,2) DEFAULT NULL COMMENT 'Điểm số cần đạt',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_badge_name (badge_name),
    INDEX idx_type (badge_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 15. BẢNG HUY HIỆU CỦA NGƯỜI DÙNG (USER_BADGES)
-- =====================================================
CREATE TABLE user_badges (
    user_badge_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL,
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    course_id BIGINT DEFAULT NULL COMMENT 'Huy hiệu gắn với khóa học cụ thể (nếu có)',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (badge_id) REFERENCES badges(badge_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE SET NULL,
    UNIQUE KEY unique_user_badge (user_id, badge_id, course_id),
    INDEX idx_user (user_id),
    INDEX idx_badge (badge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 16. BẢNG XẾP HẠNG (RANKINGS)
-- =====================================================
CREATE TABLE rankings (
    ranking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT DEFAULT NULL COMMENT 'NULL = xếp hạng tổng, có giá trị = xếp hạng theo khóa học',
    total_xp INT DEFAULT 0,
    total_score DECIMAL(10,2) DEFAULT 0.00,
    rank_position INT DEFAULT NULL COMMENT 'Thứ hạng',
    period_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME') DEFAULT 'ALL_TIME',
    period_start DATE DEFAULT NULL,
    period_end DATE DEFAULT NULL,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_course (course_id),
    INDEX idx_period (period_type, period_start, period_end),
    INDEX idx_rank (course_id, period_type, rank_position)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 17. BẢNG THÔNG BÁO (NOTIFICATIONS)
-- =====================================================
CREATE TABLE notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    notification_type ENUM('GRADE', 'ASSIGNMENT', 'BADGE', 'COMMENT', 'ANNOUNCEMENT', 'SYSTEM') DEFAULT 'SYSTEM',
    related_id BIGINT DEFAULT NULL COMMENT 'ID của đối tượng liên quan (submission_id, badge_id, etc.)',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_read (user_id, is_read),
    INDEX idx_type (notification_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 18. BẢNG LỊCH SỬ HOẠT ĐỘNG (ACTIVITY_LOGS)
-- =====================================================
CREATE TABLE activity_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) DEFAULT NULL COMMENT 'Loại đối tượng (course, lesson, assignment, etc.)',
    entity_id BIGINT DEFAULT NULL,
    ip_address VARCHAR(45) DEFAULT NULL,
    user_agent TEXT DEFAULT NULL,
    details JSON DEFAULT NULL COMMENT 'Chi tiết bổ sung dạng JSON',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_user (user_id),
    INDEX idx_action (action),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 19. BẢNG TÀI LIỆU ĐÍNH KÈM (ATTACHMENTS)
-- =====================================================
CREATE TABLE attachments (
    attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type ENUM('LESSON', 'ASSIGNMENT', 'POST', 'COMMENT') NOT NULL,
    entity_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(255) NOT NULL,
    file_size BIGINT DEFAULT NULL COMMENT 'Kích thước file (bytes)',
    file_type VARCHAR(50) DEFAULT NULL COMMENT 'MIME type',
    uploaded_by BIGINT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (uploaded_by) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_uploader (uploaded_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 20. BẢNG CẤU HÌNH HỆ THỐNG (SYSTEM_SETTINGS)
-- =====================================================
CREATE TABLE system_settings (
    setting_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT,
    setting_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') DEFAULT 'STRING',
    description TEXT,
    updated_by BIGINT DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (updated_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- INSERT DỮ LIỆU MẪU (OPTIONAL)
-- =====================================================

-- Tạo tài khoản Admin mặc định
-- Mật khẩu: admin123 (cần hash bằng BCrypt trong ứng dụng)
INSERT INTO users (username, email, password_hash, full_name, role) VALUES
('admin', 'admin@elearning.edu.vn', '$2a$10$placeholder_hash_here', 'Quản trị viên hệ thống', 'ADMIN');

-- Tạo một số huy hiệu mặc định
INSERT INTO badges (badge_name, badge_description, badge_type) VALUES
('Khởi đầu', 'Hoàn thành bài học đầu tiên', 'COURSE_COMPLETION'),
('Xuất sắc', 'Đạt điểm tối đa trong bài kiểm tra', 'HIGH_SCORE'),
('Chăm chỉ', 'Học tập đều đặn 7 ngày liên tiếp', 'CONSISTENCY'),
('Tích cực', 'Tham gia 10 cuộc thảo luận', 'PARTICIPATION');

-- Cấu hình hệ thống mặc định
INSERT INTO system_settings (setting_key, setting_value, setting_type, description) VALUES
('site_name', 'Hệ thống E-Learning Khoa CNTT', 'STRING', 'Tên hệ thống'),
('max_file_size', '10485760', 'NUMBER', 'Kích thước file tối đa (bytes) - 10MB'),
('allow_registration', 'true', 'BOOLEAN', 'Cho phép đăng ký tài khoản mới'),
('xp_per_lesson', '10', 'NUMBER', 'Điểm XP nhận được khi hoàn thành 1 bài học');

-- =====================================================
-- TẠO VIEWS HỮU ÍCH
-- =====================================================

-- View: Danh sách sinh viên và khóa học đã đăng ký
CREATE OR REPLACE VIEW v_student_courses AS
SELECT 
    u.user_id,
    u.full_name AS student_name,
    u.email,
    c.course_id,
    c.course_code,
    c.course_name,
    e.enrolled_at,
    e.progress_percentage,
    e.total_xp,
    e.status AS enrollment_status
FROM users u
INNER JOIN enrollments e ON u.user_id = e.student_id
INNER JOIN courses c ON e.course_id = c.course_id
WHERE u.role = 'STUDENT';

-- View: Bảng xếp hạng tổng
CREATE OR REPLACE VIEW v_leaderboard AS
SELECT 
    u.user_id,
    u.full_name,
    u.avatar_url,
    COALESCE(SUM(e.total_xp), 0) AS total_xp,
    COALESCE(COUNT(DISTINCT e.course_id), 0) AS courses_enrolled,
    COALESCE(COUNT(DISTINCT ub.badge_id), 0) AS badges_count
FROM users u
LEFT JOIN enrollments e ON u.user_id = e.student_id
LEFT JOIN user_badges ub ON u.user_id = ub.user_id
WHERE u.role = 'STUDENT'
GROUP BY u.user_id, u.full_name, u.avatar_url
ORDER BY total_xp DESC;

-- View: Tiến độ học tập của sinh viên
CREATE OR REPLACE VIEW v_student_progress AS
SELECT 
    u.user_id,
    u.full_name AS student_name,
    c.course_id,
    c.course_name,
    COUNT(DISTINCT l.lesson_id) AS total_lessons,
    COUNT(DISTINCT lp.lesson_id) AS completed_lessons,
    ROUND(COUNT(DISTINCT lp.lesson_id) * 100.0 / NULLIF(COUNT(DISTINCT l.lesson_id), 0), 2) AS completion_percentage
FROM users u
INNER JOIN enrollments e ON u.user_id = e.student_id
INNER JOIN courses c ON e.course_id = c.course_id
LEFT JOIN lessons l ON c.course_id = l.course_id AND l.is_published = TRUE
LEFT JOIN lesson_progress lp ON u.user_id = lp.student_id 
    AND l.lesson_id = lp.lesson_id 
    AND lp.is_completed = TRUE
WHERE u.role = 'STUDENT'
GROUP BY u.user_id, u.full_name, c.course_id, c.course_name;

-- =====================================================
-- STORED PROCEDURES (OPTIONAL)
-- =====================================================

DELIMITER //

-- Procedure: Tính toán và cập nhật xếp hạng
CREATE PROCEDURE sp_update_rankings(
    IN p_course_id BIGINT,
    IN p_period_type VARCHAR(20)
)
BEGIN
    DECLARE v_period_start DATE;
    DECLARE v_period_end DATE;
    
    -- Xác định khoảng thời gian
    CASE p_period_type
        WHEN 'DAILY' THEN
            SET v_period_start = CURDATE();
            SET v_period_end = CURDATE();
        WHEN 'WEEKLY' THEN
            SET v_period_start = DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY);
            SET v_period_end = DATE_ADD(v_period_start, INTERVAL 6 DAY);
        WHEN 'MONTHLY' THEN
            SET v_period_start = DATE_FORMAT(CURDATE(), '%Y-%m-01');
            SET v_period_end = LAST_DAY(CURDATE());
        ELSE
            SET v_period_start = NULL;
            SET v_period_end = NULL;
    END CASE;
    
    -- Xóa dữ liệu cũ
    DELETE FROM rankings 
    WHERE course_id = IFNULL(p_course_id, course_id)
      AND period_type = p_period_type
      AND period_start = v_period_start;
    
    -- Tính toán và chèn dữ liệu mới
    INSERT INTO rankings (user_id, course_id, total_xp, total_score, period_type, period_start, period_end)
    SELECT 
        e.student_id,
        IFNULL(p_course_id, e.course_id),
        SUM(e.total_xp) AS total_xp,
        COALESCE(SUM(s.score), 0) AS total_score,
        p_period_type,
        v_period_start,
        v_period_end
    FROM enrollments e
    LEFT JOIN submissions s ON e.student_id = s.student_id 
        AND e.course_id = (SELECT course_id FROM assignments WHERE assignment_id = s.assignment_id)
        AND s.is_graded = TRUE
    WHERE (p_course_id IS NULL OR e.course_id = p_course_id)
    GROUP BY e.student_id, e.course_id;
    
    -- Cập nhật thứ hạng
    SET @rank = 0;
    UPDATE rankings r
    INNER JOIN (
        SELECT ranking_id, 
               @rank := @rank + 1 AS new_rank
        FROM rankings
        WHERE course_id = IFNULL(p_course_id, course_id)
          AND period_type = p_period_type
          AND period_start = v_period_start
        ORDER BY total_xp DESC, total_score DESC
    ) ranked ON r.ranking_id = ranked.ranking_id
    SET r.rank_position = ranked.new_rank;
END //

DELIMITER ;

-- =====================================================
-- TRIGGERS (OPTIONAL)
-- =====================================================

DELIMITER //

-- Trigger: Tự động cập nhật progress_percentage khi hoàn thành bài học
CREATE TRIGGER trg_update_enrollment_progress
AFTER UPDATE ON lesson_progress
FOR EACH ROW
BEGIN
    IF NEW.is_completed = TRUE AND (OLD.is_completed IS NULL OR OLD.is_completed = FALSE) THEN
        UPDATE enrollments e
        INNER JOIN lessons l ON NEW.lesson_id = l.lesson_id
        SET e.progress_percentage = (
            SELECT ROUND(COUNT(DISTINCT lp.lesson_id) * 100.0 / NULLIF(COUNT(DISTINCT l2.lesson_id), 0), 2)
            FROM lessons l2
            LEFT JOIN lesson_progress lp ON l2.lesson_id = lp.lesson_id 
                AND lp.student_id = NEW.student_id 
                AND lp.is_completed = TRUE
            WHERE l2.course_id = l.course_id AND l2.is_published = TRUE
        )
        WHERE e.student_id = NEW.student_id
          AND e.course_id = l.course_id;
    END IF;
END //

-- Trigger: Tự động cấp huy hiệu khi đạt điều kiện
CREATE TRIGGER trg_check_badge_eligibility
AFTER UPDATE ON enrollments
FOR EACH ROW
BEGIN
    -- Kiểm tra huy hiệu hoàn thành khóa học
    IF NEW.progress_percentage >= 100 AND (OLD.progress_percentage IS NULL OR OLD.progress_percentage < 100) THEN
        INSERT IGNORE INTO user_badges (user_id, badge_id, course_id)
        SELECT NEW.student_id, badge_id, NEW.course_id
        FROM badges
        WHERE badge_type = 'COURSE_COMPLETION'
          AND is_active = TRUE
        LIMIT 1;
    END IF;
END //

DELIMITER ;

-- =====================================================
-- KẾT THÚC
-- =====================================================
