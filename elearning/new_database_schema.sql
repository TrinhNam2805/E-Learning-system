-- =====================================================
-- NEW DATABASE SCHEMA SYNCED WITH JPA ENTITIES
-- =====================================================

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    phone VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    course_name VARCHAR(200) NOT NULL,
    description TEXT,
    teacher_id BIGINT,
    enroll_password VARCHAR(100),
    semester VARCHAR(20),
    academic_year VARCHAR(10),
    status VARCHAR(20) NOT NULL,
    max_students INT DEFAULT 50,
    thumbnail VARCHAR(255),
    FOREIGN KEY (teacher_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lessons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    lesson_order INT NOT NULL,
    lesson_title VARCHAR(200) NOT NULL,
    lesson_content LONGTEXT,
    video_url VARCHAR(500),
    duration_minutes INT DEFAULT 45,
    published BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (course_id) REFERENCES courses(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    type VARCHAR(20),
    due_date DATETIME,
    max_score DOUBLE DEFAULT 10.0,
    FOREIGN KEY (course_id) REFERENCES courses(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE enrollments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrolled_at DATE,
    status VARCHAR(20),
    progress_percentage INT DEFAULT 0,
    total_xp INT DEFAULT 0,
    UNIQUE KEY unique_enrollment (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lesson_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    completed_at DATE,
    UNIQUE KEY unique_lesson_progress (student_id, lesson_id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (lesson_id) REFERENCES lessons(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE forum_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    title VARCHAR(300) NOT NULL,
    content TEXT NOT NULL,
    post_type VARCHAR(20),
    view_count INT DEFAULT 0,
    created_at DATETIME,
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (author_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME,
    FOREIGN KEY (post_id) REFERENCES forum_posts(id),
    FOREIGN KEY (author_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quiz_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    option_a VARCHAR(500),
    option_b VARCHAR(500),
    option_c VARCHAR(500),
    option_d VARCHAR(500),
    correct_answer VARCHAR(1) NOT NULL,
    question_order INT DEFAULT 1,
    points DOUBLE DEFAULT 1.0,
    FOREIGN KEY (assignment_id) REFERENCES assignments(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    content TEXT,
    file_url VARCHAR(500),
    score DOUBLE,
    feedback TEXT,
    status VARCHAR(20),
    submitted_at DATETIME,
    FOREIGN KEY (assignment_id) REFERENCES assignments(id),
    FOREIGN KEY (student_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    lesson_id BIGINT,
    course_id BIGINT,
    content TEXT NOT NULL,
    highlight_color VARCHAR(10) NOT NULL DEFAULT '#FFFF00',
    title VARCHAR(200),
    note_type VARCHAR(20),
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (lesson_id) REFERENCES lessons(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30),
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- MOCK DATA INSERTIONS
-- =====================================================

-- 1. Users (password is 'password123' - placeholder)
INSERT INTO users (username, email, password, full_name, role, phone, active, locked, created_at) VALUES
('admin', 'admin@elearning.com', 'password123', 'System Administrator', 'ADMIN', '0123456789', TRUE, FALSE, '2024-01-01'),
('jdoe', 'john.doe@university.edu', 'password123', 'John Doe', 'TEACHER', '0987654321', TRUE, FALSE, '2024-01-05'),
('asmith', 'alice.smith@student.com', 'password123', 'Alice Smith', 'STUDENT', '0555444333', TRUE, FALSE, '2024-01-10');

-- 2. Courses
INSERT INTO courses (course_code, course_name, description, teacher_id, enroll_password, semester, academic_year, status, max_students, thumbnail) VALUES
('CS101', 'Introduction to Java', 'Learn the basics of Java programming language.', 2, 'java2024', 'Semester 1', '2024', 'PUBLISHED', 100, 'https://example.com/java.png'),
('SWE301', 'Software Engineering', 'Advanced software design and architecture.', 2, NULL, 'Semester 1', '2024', 'PUBLISHED', 50, 'https://example.com/swe.png');

-- 3. Lessons
INSERT INTO lessons (course_id, lesson_order, lesson_title, lesson_content, video_url, duration_minutes, published) VALUES
(1, 1, 'Variables and Data Types', 'In this lesson, we cover primitive types and variables...', 'https://youtube.com/watch?v=v1', 45, TRUE),
(1, 2, 'Control Flow Statements', 'Learn about IF, ELSE, and SWITCH cases...', 'https://youtube.com/watch?v=v2', 60, TRUE),
(2, 1, 'SDLC Overview', 'Introduction to Software Development Life Cycle models.', 'https://youtube.com/watch?v=v3', 30, TRUE);

-- 4. Assignments
INSERT INTO assignments (course_id, title, description, type, due_date, max_score) VALUES
(1, 'Java Basics Quiz', 'A short quiz on variables and types.', 'QUIZ', '2024-12-31 23:59:59', 10.0),
(1, 'First Program', 'Upload your first Hello World program.', 'HOMEWORK', '2024-12-15 23:59:59', 10.0);

-- 5. Enrollments
INSERT INTO enrollments (student_id, course_id, enrolled_at, status, progress_percentage, total_xp) VALUES
(3, 1, '2024-02-01', 'ACTIVE', 50, 150);

-- 6. Lesson Progress
INSERT INTO lesson_progress (student_id, lesson_id, completed, completed_at) VALUES
(3, 1, TRUE, '2024-02-05');

-- 7. Forum Posts
INSERT INTO forum_posts (course_id, author_id, title, content, post_type, view_count, created_at) VALUES
(1, 3, 'Question about Loops', 'How do I use a for-each loop in Java?', 'QUESTION', 12, '2024-02-10 10:00:00');

-- 8. Comments
INSERT INTO comments (post_id, author_id, content, created_at) VALUES
(1, 2, 'You can use it like this: for (Type item : collection) { ... }', '2024-02-10 11:30:00');

-- 9. Quiz Questions
INSERT INTO quiz_questions (assignment_id, question_text, option_a, option_b, option_c, option_d, correct_answer, question_order, points) VALUES
(1, 'What is the size of an int in Java?', '16 bits', '32 bits', '64 bits', '8 bits', 'B', 1, 2.0),
(1, 'Which keyword is used to define a class?', 'class', 'struct', 'def', 'object', 'A', 2, 2.0),
(1, 'Is Java case-sensitive?', 'Yes', 'No', 'Depends on OS', 'Only for strings', 'A', 3, 2.0);

-- 10. Submissions
INSERT INTO submissions (assignment_id, student_id, content, file_url, score, feedback, status, submitted_at) VALUES
(1, 3, '{"q1":"B", "q2":"A", "q3":"A"}', NULL, 6.0, 'Good job on the basics!', 'GRADED', '2024-02-15 14:20:00');

-- 11. Notes
INSERT INTO notes (student_id, lesson_id, course_id, content, highlight_color, title, note_type, created_at, updated_at) VALUES
(3, 1, 1, 'Important to remember about final variables.', '#FFFF00', 'Java constants', 'LESSON', '2024-02-16 09:00:00', '2024-02-16 09:00:00');

-- 12. Notifications
INSERT INTO notifications (user_id, title, message, type, is_read, created_at) VALUES
(3, 'Welcome!', 'Welcome to the Introduction to Java course.', 'SYSTEM', FALSE, '2024-02-01 08:00:00');

