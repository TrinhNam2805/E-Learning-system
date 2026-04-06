CREATE DATABASE IF NOT EXISTS `e-learning`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE `e-learning`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `comments`;
DROP TABLE IF EXISTS `forum_posts`;
DROP TABLE IF EXISTS `user_badges`;
DROP TABLE IF EXISTS `badge_definitions`;
DROP TABLE IF EXISTS `quiz_questions`;
DROP TABLE IF EXISTS `submissions`;
DROP TABLE IF EXISTS `assignments`;
DROP TABLE IF EXISTS `lesson_progress`;
DROP TABLE IF EXISTS `note_links`;
DROP TABLE IF EXISTS `notes`;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `enrollments`;
DROP TABLE IF EXISTS `lessons`;
DROP TABLE IF EXISTS `course_sections`;
DROP TABLE IF EXISTS `course_prerequisites`;
DROP TABLE IF EXISTS `courses`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `departments`;

SET FOREIGN_KEY_CHECKS = 1;


CREATE TABLE `departments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(20) NOT NULL COMMENT 'MÃ£ khoa, vÃ­ dá»¥ CNTT',
  `name` VARCHAR(200) NOT NULL COMMENT 'TÃªn Ä‘áº§y Ä‘á»§',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_departments_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='ÄÆ¡n vá»‹ quáº£n lÃ½ há»c pháº§n vÃ  cÃ¡n bá»™';


CREATE TABLE `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `full_name` VARCHAR(100) NOT NULL,
  `role` VARCHAR(20) NOT NULL COMMENT 'Current scope: STUDENT',
  `phone` VARCHAR(20) DEFAULT NULL,
  `active` BIT(1) NOT NULL DEFAULT b'1',
  `locked` BIT(1) NOT NULL DEFAULT b'0',
  `created_at` DATE DEFAULT NULL,
  `department_id` BIGINT DEFAULT NULL COMMENT 'Khoa / Ä‘Æ¡n vá»‹ trá»±c thuá»™c',
  `student_code` VARCHAR(20) DEFAULT NULL COMMENT 'MÃ£ sá»‘ sinh viÃªn',
  `password_reset_token` VARCHAR(100) DEFAULT NULL,
  `password_reset_expires` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_username` (`username`),
  UNIQUE KEY `uk_users_email` (`email`),
  UNIQUE KEY `uk_users_student_code` (`student_code`),
  UNIQUE KEY `uk_users_password_reset_token` (`password_reset_token`),
  KEY `idx_users_department_id` (`department_id`),
  KEY `idx_users_role` (`role`),
  CONSTRAINT `fk_users_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='TÃ i khoáº£n há»‡ thá»‘ng';

CREATE TABLE `badge_definitions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL,
  `name` VARCHAR(120) NOT NULL,
  `description` VARCHAR(255) NOT NULL,
  `icon` VARCHAR(80) NOT NULL,
  `criterion_type` VARCHAR(40) NOT NULL COMMENT 'TOTAL_XP | COMPLETED_LESSONS | GRADED_SUBMISSIONS | PERFECT_QUIZZES | COMPLETED_COURSES',
  `threshold_value` INT NOT NULL,
  `active` BIT(1) NOT NULL DEFAULT b'1',
  `display_order` INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_badge_definitions_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Danh má»¥c badge vÃ  rule trao badge';

CREATE TABLE `user_badges` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `badge_definition_id` BIGINT NOT NULL,
  `earned_at` DATETIME(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_badges_user_badge` (`user_id`, `badge_definition_id`),
  KEY `idx_user_badges_badge_definition_id` (`badge_definition_id`),
  CONSTRAINT `fk_user_badges_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_user_badges_badge_definition` FOREIGN KEY (`badge_definition_id`) REFERENCES `badge_definitions` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Badge mÃ  ngÆ°á»i dÃ¹ng Ä‘Ã£ Ä‘áº¡t Ä‘Æ°á»£c';


CREATE TABLE `courses` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_code` VARCHAR(20) NOT NULL COMMENT 'MÃ£ há»c pháº§n (theo CTÄT)',
  `course_name` VARCHAR(200) NOT NULL,
  `description` TEXT,
  `enroll_password` VARCHAR(100) DEFAULT NULL,
  `semester` VARCHAR(20) DEFAULT NULL COMMENT 'VÃ­ dá»¥ HK1, HK2',
  `academic_year` VARCHAR(10) DEFAULT NULL COMMENT 'VÃ­ dá»¥ 2024-2025',
  `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT | PUBLISHED | ARCHIVED',
  `max_students` INT NOT NULL DEFAULT 50,
  `thumbnail` VARCHAR(255) DEFAULT NULL,
  `department_id` BIGINT DEFAULT NULL,
  `credits` INT NOT NULL DEFAULT 3 COMMENT 'Sá»‘ tÃ­n chá»‰',
  `theory_hours` INT NOT NULL DEFAULT 30 COMMENT 'Giá» lÃ½ thuyáº¿t (Ä‘á» cÆ°Æ¡ng)',
  `practice_hours` INT NOT NULL DEFAULT 15 COMMENT 'Giá» thá»±c hÃ nh / bÃ i táº­p',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_courses_offering` (`course_code`, `semester`, `academic_year`),
  KEY `idx_courses_department_id` (`department_id`),
  KEY `idx_courses_status` (`status`),
  CONSTRAINT `fk_courses_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Lá»›p há»c pháº§n (instance theo há»c ká»³)';


CREATE TABLE `course_prerequisites` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_id` BIGINT NOT NULL,
  `prerequisite_course_code` VARCHAR(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_prereq` (`course_id`, `prerequisite_course_code`),
  KEY `idx_prereq_course_id` (`course_id`),
  CONSTRAINT `fk_prereq_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='MÃ´n tiÃªn quyáº¿t theo mÃ£ há»c pháº§n';


CREATE TABLE `course_sections` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_id` BIGINT NOT NULL,
  `section_order` INT NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_course_sections_course_id` (`course_id`),
  CONSTRAINT `fk_course_sections_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Pháº§n ná»™i dung trong khÃ³a há»c';

CREATE TABLE `lessons` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_id` BIGINT NOT NULL,
  `section_id` BIGINT DEFAULT NULL,
  `lesson_order` INT NOT NULL,
  `lesson_title` VARCHAR(200) NOT NULL,
  `lesson_content` LONGTEXT,
  `video_url` VARCHAR(500) DEFAULT NULL,
  `duration_minutes` INT NOT NULL DEFAULT 45,
  `published` BIT(1) NOT NULL DEFAULT b'1',
  PRIMARY KEY (`id`),
  KEY `idx_lessons_course_id` (`course_id`),
  KEY `idx_lessons_section_id` (`section_id`),
  CONSTRAINT `fk_lessons_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_lessons_section` FOREIGN KEY (`section_id`) REFERENCES `course_sections` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `enrollments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT NOT NULL,
  `course_id` BIGINT NOT NULL,
  `enrolled_at` DATE DEFAULT NULL,
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE | COMPLETED | DROPPED',
  `progress_percentage` INT NOT NULL DEFAULT 0,
  `activity_xp` INT NOT NULL DEFAULT 0 COMMENT 'XP tá»« quiz, bÃ i Ä‘Æ°á»£c cháº¥m',
  `total_xp` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_enrollment_student_course` (`student_id`, `course_id`),
  KEY `idx_enrollments_course_id` (`course_id`),
  CONSTRAINT `fk_enrollments_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_enrollments_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='ÄÄƒng kÃ½ lá»›p há»c pháº§n';

CREATE TABLE `assignments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_id` BIGINT NOT NULL,
  `lesson_id` BIGINT DEFAULT NULL,
  `title` VARCHAR(200) NOT NULL,
  `description` TEXT,
  `type` VARCHAR(20) DEFAULT 'HOMEWORK' COMMENT 'HOMEWORK | QUIZ | EXAM',
  `due_date` DATETIME(6) DEFAULT NULL,
  `max_score` DOUBLE NOT NULL DEFAULT 10,
  `minimum_passing_score` DOUBLE DEFAULT NULL,
  `allow_late_submission` BIT(1) NOT NULL DEFAULT b'0',
  `max_attempts` INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `idx_assignments_course_id` (`course_id`),
  KEY `idx_assignments_lesson_id` (`lesson_id`),
  CONSTRAINT `fk_assignments_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_assignments_lesson` FOREIGN KEY (`lesson_id`) REFERENCES `lessons` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `quiz_questions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `assignment_id` BIGINT NOT NULL,
  `question_text` TEXT NOT NULL,
  `option_a` VARCHAR(500) DEFAULT NULL,
  `option_b` VARCHAR(500) DEFAULT NULL,
  `option_c` VARCHAR(500) DEFAULT NULL,
  `option_d` VARCHAR(500) DEFAULT NULL,
  `correct_answer` VARCHAR(1) NOT NULL,
  `question_order` INT NOT NULL DEFAULT 1,
  `points` DOUBLE NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `idx_quiz_questions_assignment_id` (`assignment_id`),
  CONSTRAINT `fk_quiz_questions_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `submissions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `assignment_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `content` TEXT,
  `file_url` VARCHAR(500) DEFAULT NULL,
  `original_file_name` VARCHAR(255) DEFAULT NULL,
  `file_size` BIGINT DEFAULT NULL,
  `score` DOUBLE DEFAULT NULL,
  `feedback` TEXT,
  `status` VARCHAR(20) DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED | GRADED | LATE',
  `attempt_number` INT NOT NULL DEFAULT 1,
  `late_submission` BIT(1) NOT NULL DEFAULT b'0',
  `auto_graded` BIT(1) NOT NULL DEFAULT b'0',
  `submitted_at` DATETIME(6) DEFAULT NULL,
  `graded_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_submissions_assignment_id` (`assignment_id`),
  KEY `idx_submissions_student_id` (`student_id`),
  CONSTRAINT `fk_submissions_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_submissions_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `forum_posts` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_id` BIGINT DEFAULT NULL COMMENT 'NULL = diá»…n Ä‘Ã n chung toÃ n kÃªnh',
  `lesson_id` BIGINT DEFAULT NULL COMMENT 'Tuá»³ chá»n: tháº£o luáº­n gáº¯n má»™t bÃ i há»c',
  `author_id` BIGINT NOT NULL,
  `title` VARCHAR(300) NOT NULL,
  `content` TEXT NOT NULL,
  `post_type` VARCHAR(20) DEFAULT 'DISCUSSION',
  `view_count` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_forum_posts_course_id` (`course_id`),
  KEY `idx_forum_posts_lesson_id` (`lesson_id`),
  KEY `idx_forum_posts_author_id` (`author_id`),
  CONSTRAINT `fk_forum_posts_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_forum_posts_lesson` FOREIGN KEY (`lesson_id`) REFERENCES `lessons` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_forum_posts_author` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `comments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL,
  `author_id` BIGINT NOT NULL,
  `content` TEXT NOT NULL,
  `created_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_comments_post_id` (`post_id`),
  KEY `idx_comments_author_id` (`author_id`),
  CONSTRAINT `fk_comments_post` FOREIGN KEY (`post_id`) REFERENCES `forum_posts` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_comments_author` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `lesson_progress` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT NOT NULL,
  `lesson_id` BIGINT NOT NULL,
  `completed` BIT(1) NOT NULL DEFAULT b'0',
  `completed_at` DATE DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lesson_progress_student_lesson` (`student_id`, `lesson_id`),
  KEY `idx_lesson_progress_lesson_id` (`lesson_id`),
  CONSTRAINT `fk_lesson_progress_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_lesson_progress_lesson` FOREIGN KEY (`lesson_id`) REFERENCES `lessons` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT NOT NULL,
  `lesson_id` BIGINT DEFAULT NULL,
  `course_id` BIGINT DEFAULT NULL,
  `content` TEXT NOT NULL,
  `highlight_color` VARCHAR(10) NOT NULL DEFAULT '#FFFF00',
  `title` VARCHAR(200) DEFAULT NULL,
  `note_type` VARCHAR(20) DEFAULT 'LESSON',
  `source_excerpt` TEXT COMMENT 'TrÃ­ch dáº«n tá»« bÃ i giáº£ng (truy váº¿t)',
  `tags` VARCHAR(500) DEFAULT NULL COMMENT 'NhÃ£n phÃ¢n tÃ¡ch báº±ng dáº¥u pháº©y',
  `created_at` DATETIME(6) DEFAULT NULL,
  `updated_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notes_student_id` (`student_id`),
  KEY `idx_notes_lesson_id` (`lesson_id`),
  KEY `idx_notes_course_id` (`course_id`),
  CONSTRAINT `fk_notes_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_notes_lesson` FOREIGN KEY (`lesson_id`) REFERENCES `lessons` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_notes_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `note_links` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `from_note_id` BIGINT NOT NULL,
  `to_note_id` BIGINT NOT NULL,
  `relation_label` VARCHAR(200) DEFAULT NULL,
  `created_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_note_link` (`from_note_id`, `to_note_id`),
  KEY `idx_note_links_to` (`to_note_id`),
  CONSTRAINT `fk_note_links_from` FOREIGN KEY (`from_note_id`) REFERENCES `notes` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_note_links_to` FOREIGN KEY (`to_note_id`) REFERENCES `notes` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='LiÃªn káº¿t ghi chÃº (Ä‘á»“ thá»‹ tri thá»©c cÃ¡ nhÃ¢n)';

CREATE TABLE `notifications` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `message` TEXT NOT NULL,
  `type` VARCHAR(30) DEFAULT 'SYSTEM',
  `is_read` BIT(1) NOT NULL DEFAULT b'0',
  `created_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notifications_user_id` (`user_id`),
  CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



-- =============================================================================
-- Full demo seed for the E-Learning system
-- 8 progressive IT courses
-- 12 lessons per course
-- Each lesson has a quiz and a homework assignment
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM `comments`;
DELETE FROM `forum_posts`;
DELETE FROM `quiz_questions`;
DELETE FROM `submissions`;
DELETE FROM `assignments`;
DELETE FROM `lesson_progress`;
DELETE FROM `note_links`;
DELETE FROM `notes`;
DELETE FROM `notifications`;
DELETE FROM `enrollments`;
DELETE FROM `course_sections`;
DELETE FROM `course_prerequisites`;
DELETE FROM `courses`;
DELETE FROM `user_badges`;
DELETE FROM `badge_definitions`;
DELETE FROM `users`;
DELETE FROM `departments`;

SET FOREIGN_KEY_CHECKS = 1;

SET @pwd := '$2b$10$IgwunfhzoNX76OVOOWU1.eKSB7SmLMjNAdTvjYpuY79/U0ebJmcpy';

INSERT INTO `departments` (`id`, `code`, `name`) VALUES
(1, 'CNTT', 'Faculty of Information Technology');

INSERT INTO `users`
(`id`, `username`, `email`, `password`, `full_name`, `role`, `phone`, `active`, `locked`, `created_at`, `department_id`, `student_code`) VALUES
(5, 'sv.phamminhchau', 'minh.chau@student.cntt.edu.vn', @pwd, 'Pham Minh Chau', 'STUDENT', '0913456789', 1, 0, '2025-09-01', 1, 'B2201001'),
(6, 'sv.lethudung', 'thu.dung@student.cntt.edu.vn', @pwd, 'Le Thu Dung', 'STUDENT', '0914567890', 1, 0, '2025-09-01', 1, 'B2201002'),
(7, 'sv.dangvangiang', 'van.giang@student.cntt.edu.vn', @pwd, 'Dang Van Giang', 'STUDENT', '0915678901', 1, 0, '2025-09-01', 1, 'B2201003'),
(8, 'sv.nguyenhoangnam', 'hoang.nam@student.cntt.edu.vn', @pwd, 'Nguyen Hoang Nam', 'STUDENT', '0916789012', 1, 0, '2025-09-02', 1, 'B2201004'),
(9, 'sv.vongocmai', 'ngoc.mai@student.cntt.edu.vn', @pwd, 'Vo Ngoc Mai', 'STUDENT', '0917890123', 1, 0, '2025-09-02', 1, 'B2201005'),
(10, 'sv.tranquocbao', 'quoc.bao@student.cntt.edu.vn', @pwd, 'Tran Quoc Bao', 'STUDENT', '0918901234', 1, 0, '2025-09-02', 1, 'B2201006');

CREATE TEMPORARY TABLE `tmp_course_seed` (
  `sort_order` INT NOT NULL,
  `course_code` VARCHAR(20) NOT NULL,
  `course_name` VARCHAR(200) NOT NULL,
  `description` TEXT NOT NULL,
  `enroll_password` VARCHAR(100) NOT NULL,
  `thumbnail` VARCHAR(255) NOT NULL,
  `credits` INT NOT NULL,
  `theory_hours` INT NOT NULL,
  `practice_hours` INT NOT NULL,
  `deliverable` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`sort_order`),
  UNIQUE KEY `uk_tmp_course_seed_code` (`course_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `tmp_course_seed`
(`sort_order`, `course_code`, `course_name`, `description`, `enroll_password`, `thumbnail`, `credits`, `theory_hours`, `practice_hours`, `deliverable`) VALUES
(1, 'ITF101', 'Introduction to IT and Digital Skills', 'Start the IT learning journey with computer hardware, operating systems, the internet, problem-solving thinking, and effective technical study habits.', 'ITF101-START', 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=1200&q=80', 3, 30, 30, 'PDF checklist or learning roadmap'),
(2, 'PRG101', 'Programming Fundamentals with Python', 'Build programming thinking in Python from variables, loops, and functions to file handling and a mini project.', 'PRG101-PY', 'https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=1200&q=80', 3, 30, 45, 'Python file package with a short README'),
(3, 'DBI201', 'Databases and Practical SQL', 'Study relational data models, SQL data manipulation, schema design, and application-to-database integration.', 'DBI201-SQL', 'https://images.unsplash.com/photo-1555949963-aa79dcee981c?w=1200&q=80', 3, 30, 45, 'SQL script plus result screenshots'),
(4, 'WEB201', 'Web Frontend Development with HTML, CSS, and JavaScript', 'Move from semantic HTML and CSS layout to responsive design, JavaScript DOM work, and a complete frontend mini project.', 'WEB201-FE', 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=1200&q=80', 3, 30, 45, 'ZIP file containing the HTML, CSS, and JavaScript project'),
(5, 'OOP201', 'Object-Oriented Programming with Java', 'Practice object-oriented programming in Java through classes, interfaces, exceptions, collections, testing, and application modeling.', 'OOP201-JAVA', 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1200&q=80', 4, 45, 30, 'Java project ZIP with a model description'),
(6, 'DSA201', 'Data Structures and Algorithms', 'Develop skill in Big-O analysis, recursion, data structures, and algorithms needed for more advanced programming.', 'DSA201-ALG', 'https://images.unsplash.com/photo-1516116216624-53e697fedbea?w=1200&q=80', 4, 45, 30, 'PDF report or source-code demonstration'),
(7, 'API301', 'Backend Development with Spring Boot', 'Design a client-server backend with Spring Boot, REST APIs, JPA, authentication, file upload, testing, and release workflow.', 'API301-BE', 'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=1200&q=80', 4, 45, 30, 'Spring Boot project ZIP with API collection'),
(8, 'DEV301', 'DevOps and Application Deployment', 'Bring together Docker, CI/CD, monitoring, security, and the steps required to deploy an application to a real environment.', 'DEV301-OPS', 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1200&q=80', 3, 30, 45, 'Docker Compose setup, deployment checklist, and PDF report');

CREATE TEMPORARY TABLE `tmp_prereq_seed` (
  `course_code` VARCHAR(20) NOT NULL,
  `prerequisite_course_code` VARCHAR(20) NOT NULL,
  PRIMARY KEY (`course_code`, `prerequisite_course_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `tmp_prereq_seed` (`course_code`, `prerequisite_course_code`) VALUES
('PRG101', 'ITF101'),
('DBI201', 'PRG101'),
('WEB201', 'ITF101'),
('OOP201', 'PRG101'),
('DSA201', 'PRG101'),
('API301', 'DBI201'),
('API301', 'WEB201'),
('API301', 'OOP201'),
('DEV301', 'API301');

CREATE TEMPORARY TABLE `tmp_lesson_seed` (
  `course_code` VARCHAR(20) NOT NULL,
  `section_order` INT NOT NULL,
  `lesson_order` INT NOT NULL,
  `lesson_title` VARCHAR(200) NOT NULL,
  `duration_minutes` INT NOT NULL,
  PRIMARY KEY (`course_code`, `lesson_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `tmp_lesson_seed`
(`course_code`, `section_order`, `lesson_order`, `lesson_title`, `duration_minutes`) VALUES
('ITF101', 1, 1, 'The Role of Computer Systems and the Internet', 60),
('ITF101', 1, 2, 'Hardware, Operating Systems, and Files', 75),
('ITF101', 1, 3, 'Core Tools: Terminal, Editor, and Browser', 75),
('ITF101', 1, 4, 'Account Security and Safe Online Work', 75),
('ITF101', 2, 5, 'Problem-Solving Thinking and Flowcharts', 75),
('ITF101', 2, 6, 'Data Representation: Bits, Bytes, Text, and Images', 75),
('ITF101', 2, 7, 'Getting Started with the Command Line', 75),
('ITF101', 2, 8, 'GitHub, Issues, and Submission Workflow', 75),
('ITF101', 3, 9, 'Finding Technical Documentation Effectively', 75),
('ITF101', 3, 10, 'Teamwork and Technical Communication', 75),
('ITF101', 3, 11, 'Reading Requirements and Writing Study Reports', 75),
('ITF101', 3, 12, 'Wrap-up: Building a Long-Term IT Learning Plan', 60),
('PRG101', 1, 1, 'Installing Python and Running Your First Script', 60),
('PRG101', 1, 2, 'Variables, Data Types, and Operators', 75),
('PRG101', 1, 3, 'Branching with if-elif-else', 75),
('PRG101', 1, 4, 'Loops with for, while, and range', 75),
('PRG101', 2, 5, 'Functions, Parameters, and Return Values', 75),
('PRG101', 2, 6, 'Lists, Tuples, and Dictionaries', 75),
('PRG101', 2, 7, 'String Processing and File I/O', 75),
('PRG101', 2, 8, 'Basic Debugging and Reading Tracebacks', 75),
('PRG101', 3, 9, 'Breaking Problems into Small Functions', 75),
('PRG101', 3, 10, 'Working with Simple Modules and Packages', 75),
('PRG101', 3, 11, 'Reading and Writing JSON and Tabular Data', 75),
('PRG101', 3, 12, 'Mini Project: Task Manager Script', 60),
('DBI201', 1, 1, 'Data Modeling and Relational Tables', 60),
('DBI201', 1, 2, 'Primary Keys, Foreign Keys, and Constraints', 75),
('DBI201', 1, 3, 'Basic SELECT Queries', 75),
('DBI201', 1, 4, 'WHERE, ORDER BY, and Aggregate Functions', 75),
('DBI201', 2, 5, 'JOINs and Combining Data from Multiple Tables', 75),
('DBI201', 2, 6, 'GROUP BY, HAVING, and Reporting', 75),
('DBI201', 2, 7, 'Safe INSERT, UPDATE, and DELETE', 75),
('DBI201', 2, 8, 'Normalization up to 3NF', 75),
('DBI201', 3, 9, 'Designing a Schema for a Learning Platform', 75),
('DBI201', 3, 10, 'Indexes and Basic Query Optimization', 75),
('DBI201', 3, 11, 'Connecting Python/Java to a Database', 75),
('DBI201', 3, 12, 'Mini Project: LMS Database Design', 60),
('WEB201', 1, 1, 'Semantic HTML Structure', 60),
('WEB201', 1, 2, 'Forms and Basic Validation', 75),
('WEB201', 1, 3, 'CSS Box Model and Typography', 75),
('WEB201', 1, 4, 'Flexbox and Grid Layout', 75),
('WEB201', 2, 5, 'Mobile-First Responsive Design', 75),
('WEB201', 2, 6, 'JavaScript Basics for the DOM', 75),
('WEB201', 2, 7, 'Event Handling and Form Data', 75),
('WEB201', 2, 8, 'Fetch API and Backend Calls', 75),
('WEB201', 3, 9, 'Organizing Static Files and Assets', 75),
('WEB201', 3, 10, 'Accessibility and Semantic UI', 75),
('WEB201', 3, 11, 'Basic Frontend Performance Optimization', 75),
('WEB201', 3, 12, 'Mini Project: Course Landing Page', 60),
('OOP201', 1, 1, 'JDK, JVM, and the Classpath', 60),
('OOP201', 1, 2, 'Classes, Objects, and Constructors', 75),
('OOP201', 1, 3, 'Encapsulation and Access Modifiers', 75),
('OOP201', 1, 4, 'Inheritance and Polymorphism', 75),
('OOP201', 2, 5, 'Interfaces and Abstract Classes', 75),
('OOP201', 2, 6, 'Exceptions and Error Handling', 75),
('OOP201', 2, 7, 'Collection Framework', 75),
('OOP201', 2, 8, 'Basics of Generics', 75),
('OOP201', 3, 9, 'File I/O and Simple Serialization', 75),
('OOP201', 3, 10, 'Unit Testing with JUnit Basics', 75),
('OOP201', 3, 11, 'Designing the Model Layer for a Management App', 75),
('OOP201', 3, 12, 'Mini Project: Student Management Application', 60),
('DSA201', 1, 1, 'Time Complexity and Big-O', 60),
('DSA201', 1, 2, 'Recursion and the Call Stack', 75),
('DSA201', 1, 3, 'Arrays, Linked Lists, and Dynamic Arrays', 75),
('DSA201', 1, 4, 'Stacks and Queues', 75),
('DSA201', 2, 5, 'Hash Maps and Sets', 75),
('DSA201', 2, 6, 'Trees and Binary Search Trees', 75),
('DSA201', 2, 7, 'Heaps and Priority Queues', 75),
('DSA201', 2, 8, 'Core Sorting Algorithms', 75),
('DSA201', 3, 9, 'Binary Search and Two Pointers', 75),
('DSA201', 3, 10, 'Graphs, BFS, and DFS', 75),
('DSA201', 3, 11, 'Dynamic Programming Basics', 75),
('DSA201', 3, 12, 'Mini Project: Algorithm Utility Toolkit', 60),
('API301', 1, 1, 'Client-Server Architecture and HTTP', 60),
('API301', 1, 2, 'Bootstrapping a Spring Boot Project', 75),
('API301', 1, 3, 'Controllers, Services, and Repositories', 75),
('API301', 1, 4, 'REST APIs and JSON', 75),
('API301', 2, 5, 'Validation and Exception Handling', 75),
('API301', 2, 6, 'JPA Entities and Relationship Mapping', 75),
('API301', 2, 7, 'Basic Authentication and Authorization', 75),
('API301', 2, 8, 'File Upload and Storage Handling', 75),
('API301', 3, 9, 'Logging and Environment Configuration', 75),
('API301', 3, 10, 'Writing API Tests', 75),
('API301', 3, 11, 'API Documentation and Release Workflow', 75),
('API301', 3, 12, 'Mini Project: LMS Backend', 60),
('DEV301', 1, 1, 'Linux Commands and Processes', 60),
('DEV301', 1, 2, 'Environment Variables and Secrets', 75),
('DEV301', 1, 3, 'Docker Images and Containers', 75),
('DEV301', 1, 4, 'Docker Compose for Multi-Service Systems', 75),
('DEV301', 2, 5, 'CI/CD Pipeline Basics', 75),
('DEV301', 2, 6, 'Reverse Proxies and Domains', 75),
('DEV301', 2, 7, 'Logging and Basic Monitoring', 75),
('DEV301', 2, 8, 'Backup, Rollback, and Versioning', 75),
('DEV301', 3, 9, 'Performance, Caching, and Horizontal Scaling', 75),
('DEV301', 3, 10, 'Deployment Security Essentials', 75),
('DEV301', 3, 11, 'Basic Cloud Deployment', 75),
('DEV301', 3, 12, 'Mini Project: Deploying the LMS Demo', 60);

CREATE TEMPORARY TABLE `tmp_enrollment_seed` (
  `student_id` BIGINT NOT NULL,
  `course_code` VARCHAR(20) NOT NULL,
  `enrolled_at` DATE NOT NULL,
  `completed_lessons` INT NOT NULL,
  `activity_xp` INT NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  PRIMARY KEY (`student_id`, `course_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `tmp_enrollment_seed`
(`student_id`, `course_code`, `enrolled_at`, `completed_lessons`, `activity_xp`, `status`) VALUES
(5, 'ITF101', '2026-01-05', 12, 180, 'COMPLETED'),
(5, 'PRG101', '2026-01-20', 8, 110, 'ACTIVE'),
(5, 'DBI201', '2026-02-10', 5, 60, 'ACTIVE'),
(5, 'WEB201', '2026-02-18', 6, 70, 'ACTIVE'),
(5, 'OOP201', '2026-03-01', 3, 30, 'ACTIVE'),
(6, 'ITF101', '2026-01-07', 10, 120, 'ACTIVE'),
(6, 'PRG101', '2026-01-25', 6, 60, 'ACTIVE'),
(6, 'DBI201', '2026-02-14', 2, 20, 'ACTIVE'),
(6, 'WEB201', '2026-02-22', 4, 40, 'ACTIVE'),
(7, 'ITF101', '2026-01-03', 12, 170, 'COMPLETED'),
(7, 'PRG101', '2026-01-17', 12, 190, 'COMPLETED'),
(7, 'DBI201', '2026-02-05', 9, 120, 'ACTIVE'),
(7, 'WEB201', '2026-02-12', 8, 100, 'ACTIVE'),
(7, 'OOP201', '2026-02-25', 7, 90, 'ACTIVE'),
(7, 'DSA201', '2026-03-02', 6, 75, 'ACTIVE'),
(7, 'API301', '2026-03-10', 3, 40, 'ACTIVE'),
(8, 'ITF101', '2026-01-15', 7, 50, 'ACTIVE'),
(8, 'PRG101', '2026-02-01', 5, 45, 'ACTIVE'),
(8, 'OOP201', '2026-03-05', 2, 15, 'ACTIVE'),
(8, 'DSA201', '2026-03-12', 2, 20, 'ACTIVE'),
(9, 'ITF101', '2026-01-04', 12, 160, 'COMPLETED'),
(9, 'DBI201', '2026-02-08', 8, 100, 'ACTIVE'),
(9, 'WEB201', '2026-02-16', 10, 130, 'ACTIVE'),
(9, 'API301', '2026-03-18', 1, 10, 'ACTIVE'),
(10, 'ITF101', '2026-01-22', 4, 20, 'ACTIVE'),
(10, 'PRG101', '2026-02-06', 1, 5, 'ACTIVE');

INSERT INTO `courses`
(`course_code`, `course_name`, `description`, `enroll_password`, `semester`, `academic_year`, `status`, `max_students`, `thumbnail`, `department_id`, `credits`, `theory_hours`, `practice_hours`)
SELECT
  tcs.course_code,
  tcs.course_name,
  tcs.description,
  tcs.enroll_password,
  'SEM1',
  '2026-2027',
  'PUBLISHED',
  60,
  tcs.thumbnail,
  1,
  tcs.credits,
  tcs.theory_hours,
  tcs.practice_hours
FROM `tmp_course_seed` tcs
ORDER BY tcs.sort_order;

INSERT INTO `course_prerequisites` (`course_id`, `prerequisite_course_code`)
SELECT c.id, tps.prerequisite_course_code
FROM `tmp_prereq_seed` tps
JOIN `courses` c ON c.course_code = tps.course_code
ORDER BY c.id, tps.prerequisite_course_code;

INSERT INTO `course_sections` (`course_id`, `section_order`, `title`)
SELECT
  c.id,
  s.section_order,
  CASE s.section_order
    WHEN 1 THEN CONCAT('Section 1 - Foundations of ', c.course_code)
    WHEN 2 THEN CONCAT('Section 2 - Core Practice of ', c.course_code)
    ELSE CONCAT('Section 3 - Integration and Mini Project of ', c.course_code)
  END
FROM `courses` c
JOIN (
  SELECT 1 AS section_order
  UNION ALL SELECT 2
  UNION ALL SELECT 3
) s
ORDER BY c.id, s.section_order;

INSERT INTO `lessons`
(`course_id`, `section_id`, `lesson_order`, `lesson_title`, `lesson_content`, `video_url`, `duration_minutes`, `published`)
SELECT
  c.id,
  cs.id,
  tls.lesson_order,
  tls.lesson_title,
  CONCAT(
    '<h2>Objective</h2><p>This lesson helps students master <strong>', tls.lesson_title,
    '</strong> in the course <strong>', c.course_name, '</strong>.</p>',
    '<h2>Core content</h2><p>', c.course_name,
    ' is designed as a progressive path from fundamentals to more advanced work. This lesson focuses on "', tls.lesson_title,
    '" and connects directly to the next lesson in the 12-lesson sequence.</p>',
    '<p><strong>Key idea:</strong> Lesson "', tls.lesson_title,
    '" is a required checkpoint that lets students take the quiz, submit the file-based homework, and unlock the next lesson.</p>',
    '<div class="material-hint"><strong>Outside work:</strong> After finishing the lesson, students must complete the external assignment and upload a file so the system can record the result.</div>'
  ),
  NULL,
  tls.duration_minutes,
  b'1'
FROM `tmp_lesson_seed` tls
JOIN `courses` c ON c.course_code = tls.course_code
JOIN `course_sections` cs ON cs.course_id = c.id AND cs.section_order = tls.section_order
ORDER BY c.id, tls.lesson_order;

INSERT INTO `enrollments`
(`student_id`, `course_id`, `enrolled_at`, `status`, `progress_percentage`, `activity_xp`, `total_xp`)
SELECT
  tes.student_id,
  c.id,
  tes.enrolled_at,
  tes.status,
  ROUND(tes.completed_lessons * 100.0 / 12),
  tes.activity_xp,
  tes.completed_lessons * 20 + tes.activity_xp
FROM `tmp_enrollment_seed` tes
JOIN `courses` c ON c.course_code = tes.course_code
ORDER BY tes.student_id, c.id;

INSERT INTO `assignments`
(`course_id`, `lesson_id`, `title`, `description`, `type`, `due_date`, `max_score`, `minimum_passing_score`, `allow_late_submission`, `max_attempts`)
SELECT
  c.id,
  l.id,
  CONCAT('[QUIZ] ', c.course_code, ' - Lesson ', LPAD(l.lesson_order, 2, '0'), ': ', l.lesson_title),
  CONCAT('Three-question quiz for lesson "', l.lesson_title, '". Score at least 6/10 to unlock the next lesson.'),
  'QUIZ',
  TIMESTAMP(DATE_ADD('2026-09-01', INTERVAL ((tcs.sort_order - 1) * 18 + l.lesson_order) DAY), '23:59:00'),
  10.0,
  6.0,
  b'0',
  1
FROM `lessons` l
JOIN `courses` c ON c.id = l.course_id
JOIN `tmp_course_seed` tcs ON tcs.course_code = c.course_code
ORDER BY c.id, l.lesson_order;

INSERT INTO `assignments`
(`course_id`, `lesson_id`, `title`, `description`, `type`, `due_date`, `max_score`, `minimum_passing_score`, `allow_late_submission`, `max_attempts`)
SELECT
  c.id,
  l.id,
  CONCAT('[HOMEWORK] ', c.course_code, ' - Lesson ', LPAD(l.lesson_order, 2, '0'), ': ', l.lesson_title),
  CONCAT('Complete the outside-work assignment for lesson "', l.lesson_title, '" and submit ', tcs.deliverable, '.'),
  'HOMEWORK',
  TIMESTAMP(DATE_ADD('2026-09-03', INTERVAL ((tcs.sort_order - 1) * 18 + l.lesson_order) DAY), '23:59:00'),
  10.0,
  NULL,
  CASE WHEN MOD(l.lesson_order, 4) = 0 THEN b'1' ELSE b'0' END,
  1
FROM `lessons` l
JOIN `courses` c ON c.id = l.course_id
JOIN `tmp_course_seed` tcs ON tcs.course_code = c.course_code
ORDER BY c.id, l.lesson_order;

INSERT INTO `quiz_questions`
(`assignment_id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `question_order`, `points`)
SELECT
  a.id,
  CONCAT('Lesson "', l.lesson_title, '" belongs to which course?'),
  c.course_name,
  'Advanced Information Security',
  'Computer Graphics',
  'Game Design',
  'A',
  1,
  1.0
FROM `assignments` a
JOIN `lessons` l ON l.id = a.lesson_id
JOIN `courses` c ON c.id = a.course_id
WHERE a.type = 'QUIZ'
ORDER BY a.id;

INSERT INTO `quiz_questions`
(`assignment_id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `question_order`, `points`)
SELECT
  a.id,
  CONCAT('What is the main focus of lesson "', l.lesson_title, '"?'),
  l.lesson_title,
  'Waterfall planning',
  'Shader pipeline',
  'Quantum circuit',
  'A',
  2,
  1.0
FROM `assignments` a
JOIN `lessons` l ON l.id = a.lesson_id
WHERE a.type = 'QUIZ'
ORDER BY a.id;

INSERT INTO `quiz_questions`
(`assignment_id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `question_order`, `points`)
SELECT
  a.id,
  CONCAT('What must you submit for the outside-work assignment of lesson "', l.lesson_title, '"?'),
  tcs.deliverable,
  'Only forum participation',
  'No deliverable is required',
  'Only mark the lesson as complete',
  'A',
  3,
  1.0
FROM `assignments` a
JOIN `lessons` l ON l.id = a.lesson_id
JOIN `courses` c ON c.id = a.course_id
JOIN `tmp_course_seed` tcs ON tcs.course_code = c.course_code
WHERE a.type = 'QUIZ'
ORDER BY a.id;

INSERT INTO `lesson_progress`
(`student_id`, `lesson_id`, `completed`, `completed_at`)
SELECT
  tes.student_id,
  l.id,
  b'1',
  DATE_ADD(tes.enrolled_at, INTERVAL l.lesson_order DAY)
FROM `tmp_enrollment_seed` tes
JOIN `courses` c ON c.course_code = tes.course_code
JOIN `lessons` l ON l.course_id = c.id
WHERE l.lesson_order <= tes.completed_lessons
ORDER BY tes.student_id, l.id;

INSERT INTO `forum_posts`
(`course_id`, `lesson_id`, `author_id`, `title`, `content`, `post_type`, `view_count`, `created_at`) VALUES
(NULL, NULL, 5, 'Welcome to the progressive IT learning path', 'This demo dataset now contains 8 linked IT courses, each with 12 lessons, quizzes, homework, notes and forum topics so the LMS feels like a live learning environment.', 'DISCUSSION', 45, '2026-03-01 08:00:00'),
(NULL, NULL, 6, 'How to use notes, quiz and file submission together', 'Complete the current lesson, pass the lesson quiz, save notes while studying and upload your outside-work file in the homework assignment for each lesson.', 'DISCUSSION', 39, '2026-03-01 09:00:00');

INSERT INTO `forum_posts`
(`course_id`, `lesson_id`, `author_id`, `title`, `content`, `post_type`, `view_count`, `created_at`)
SELECT
  c.id,
  NULL,
  5 + MOD(tcs.sort_order - 1, 6),
  CONCAT('Study guide for ', c.course_code),
  CONCAT('Course ', c.course_name, ' has 12 lessons, and each lesson includes a quiz and a file-submission homework assignment. Use notes inside the lesson and ask questions in the forum whenever you get stuck.'),
  'DISCUSSION',
  20 + tcs.sort_order * 3,
  TIMESTAMP(DATE_ADD('2026-03-02', INTERVAL tcs.sort_order DAY), '08:30:00')
FROM `courses` c
JOIN `tmp_course_seed` tcs ON tcs.course_code = c.course_code
ORDER BY tcs.sort_order;

INSERT INTO `forum_posts`
(`course_id`, `lesson_id`, `author_id`, `title`, `content`, `post_type`, `view_count`, `created_at`)
SELECT
  c.id,
  l.id,
  5 + MOD(tcs.sort_order - 1, 6),
  CONCAT('Lesson 04 Q&A - ', l.lesson_title),
  CONCAT('I am currently studying lesson "', l.lesson_title, '" in course ', c.course_code, '. Could anyone share a fast study approach and tips for completing the file-submission homework for this lesson?'),
  'QUESTION',
  12 + tcs.sort_order * 2,
  TIMESTAMP(DATE_ADD('2026-03-05', INTERVAL tcs.sort_order DAY), '10:15:00')
FROM `courses` c
JOIN `tmp_course_seed` tcs ON tcs.course_code = c.course_code
JOIN `lessons` l ON l.course_id = c.id AND l.lesson_order = 4
ORDER BY tcs.sort_order;

INSERT INTO `forum_posts`
(`course_id`, `lesson_id`, `author_id`, `title`, `content`, `post_type`, `view_count`, `created_at`)
SELECT
  c.id,
  l.id,
  5 + MOD(tcs.sort_order + 1, 6),
  CONCAT('Mini project wrap-up - ', c.course_code),
  CONCAT('Lesson 12 "', l.lesson_title, '" requires submitting ', tcs.deliverable, '. Our group is collecting experience and a checklist before submission.'),
  'DISCUSSION',
  16 + tcs.sort_order * 2,
  TIMESTAMP(DATE_ADD('2026-03-07', INTERVAL tcs.sort_order DAY), '15:45:00')
FROM `courses` c
JOIN `tmp_course_seed` tcs ON tcs.course_code = c.course_code
JOIN `lessons` l ON l.course_id = c.id AND l.lesson_order = 12
ORDER BY tcs.sort_order;

INSERT INTO `comments`
(`post_id`, `author_id`, `content`, `created_at`)
SELECT
  fp.id,
  5 + MOD(tcs.sort_order, 6),
  CONCAT('For lesson "', l.lesson_title, '", break the task into input, process, and output. Finish the quiz first, then package the results into a file for homework submission.'),
  DATE_ADD(fp.created_at, INTERVAL 45 MINUTE)
FROM `forum_posts` fp
JOIN `courses` c ON c.id = fp.course_id
JOIN `tmp_course_seed` tcs ON tcs.course_code = c.course_code
JOIN `lessons` l ON l.id = fp.lesson_id
WHERE fp.title = CONCAT('Lesson 04 Q&A - ', l.lesson_title)
ORDER BY fp.id;

INSERT INTO `comments`
(`post_id`, `author_id`, `content`, `created_at`)
SELECT
  fp.id,
  5 + MOD(tcs.sort_order + 2, 6),
  CONCAT('Our group will submit ', tcs.deliverable, ' and include summary notes from lessons 1 to 11 to review everything before the mini project.'),
  DATE_ADD(fp.created_at, INTERVAL 90 MINUTE)
FROM `forum_posts` fp
JOIN `courses` c ON c.id = fp.course_id
JOIN `tmp_course_seed` tcs ON tcs.course_code = c.course_code
WHERE fp.title = CONCAT('Mini project wrap-up - ', c.course_code)
ORDER BY fp.id;

INSERT INTO `notes`
(`student_id`, `lesson_id`, `course_id`, `content`, `highlight_color`, `title`, `note_type`, `source_excerpt`, `tags`, `created_at`, `updated_at`)
SELECT
  5 + MOD(tcs.sort_order - 1, 6),
  NULL,
  c.id,
  CONCAT('Course overview for ', c.course_code, ': follow the 12 lessons in order, pass the quiz to unlock the next lesson, and submit ', tcs.deliverable, ' for each homework assignment.'),
  '#FDE68A',
  CONCAT('Overview note - ', c.course_code),
  'STANDALONE',
  NULL,
  CONCAT(LOWER(c.course_code), ',roadmap,course-note'),
  TIMESTAMP(DATE_ADD('2026-03-10', INTERVAL tcs.sort_order DAY), '07:30:00'),
  TIMESTAMP(DATE_ADD('2026-03-10', INTERVAL tcs.sort_order DAY), '07:30:00')
FROM `courses` c
JOIN `tmp_course_seed` tcs ON tcs.course_code = c.course_code
ORDER BY tcs.sort_order;

INSERT INTO `notes`
(`student_id`, `lesson_id`, `course_id`, `content`, `highlight_color`, `title`, `note_type`, `source_excerpt`, `tags`, `created_at`, `updated_at`)
SELECT
  5 + MOD(tcs.sort_order + l.lesson_order - 2, 6),
  l.id,
  c.id,
  CONCAT('Remember that lesson "', l.lesson_title, '" is a mandatory checkpoint in the ', c.course_code, ' learning path. After this lesson, complete the quiz and submit the homework in the required format.'),
  CASE MOD(l.lesson_order, 4)
    WHEN 0 THEN '#BFDBFE'
    WHEN 1 THEN '#FDE68A'
    WHEN 2 THEN '#C7F9CC'
    ELSE '#FBCFE8'
  END,
  CONCAT('Note - ', c.course_code, ' - Lesson ', LPAD(l.lesson_order, 2, '0')),
  'LESSON',
  CONCAT('Lesson "', l.lesson_title, '" is a required checkpoint so students can take the quiz, submit the file-based homework, and unlock the next lesson.'),
  CONCAT(LOWER(c.course_code), ',lesson-', LPAD(l.lesson_order, 2, '0'), ',study'),
  TIMESTAMP(DATE_ADD('2026-03-12', INTERVAL (tcs.sort_order * 2 + l.lesson_order) DAY), '20:00:00'),
  TIMESTAMP(DATE_ADD('2026-03-12', INTERVAL (tcs.sort_order * 2 + l.lesson_order) DAY), '20:00:00')
FROM `lessons` l
JOIN `courses` c ON c.id = l.course_id
JOIN `tmp_course_seed` tcs ON tcs.course_code = c.course_code
ORDER BY c.id, l.lesson_order;

INSERT INTO `note_links`
(`from_note_id`, `to_note_id`, `relation_label`, `created_at`)
SELECT
  cn.id,
  ln.id,
  'course-roadmap',
  DATE_ADD(cn.created_at, INTERVAL 1 HOUR)
FROM `notes` cn
JOIN `courses` c ON c.id = cn.course_id
JOIN `lessons` l ON l.course_id = c.id AND l.lesson_order = 1
JOIN `notes` ln ON ln.lesson_id = l.id
WHERE cn.lesson_id IS NULL
ORDER BY c.id;

INSERT INTO `note_links`
(`from_note_id`, `to_note_id`, `relation_label`, `created_at`)
SELECT
  n4.id,
  n12.id,
  'project-prep',
  DATE_ADD(n4.created_at, INTERVAL 2 HOUR)
FROM `courses` c
JOIN `lessons` l4 ON l4.course_id = c.id AND l4.lesson_order = 4
JOIN `lessons` l12 ON l12.course_id = c.id AND l12.lesson_order = 12
JOIN `notes` n4 ON n4.lesson_id = l4.id
JOIN `notes` n12 ON n12.lesson_id = l12.id
ORDER BY c.id;

INSERT INTO `submissions`
(`assignment_id`, `student_id`, `content`, `file_url`, `original_file_name`, `file_size`, `score`, `feedback`, `status`, `attempt_number`, `late_submission`, `auto_graded`, `submitted_at`, `graded_at`)
SELECT
  a.id,
  tes.student_id,
  CONCAT('Seed quiz answers for ', c.course_code, ' lesson ', l.lesson_order),
  NULL,
  NULL,
  NULL,
  ROUND(6 + MOD(tes.student_id + c.id, 5), 1),
  CONCAT('Auto-graded seed result for ', c.course_code, ' lesson ', l.lesson_order, '.'),
  'GRADED',
  1,
  b'0',
  b'1',
  TIMESTAMP(DATE_ADD(tes.enrolled_at, INTERVAL l.lesson_order + 10 DAY), '20:00:00'),
  TIMESTAMP(DATE_ADD(tes.enrolled_at, INTERVAL l.lesson_order + 10 DAY), '20:00:00')
FROM `tmp_enrollment_seed` tes
JOIN `courses` c ON c.course_code = tes.course_code
JOIN `lessons` l ON l.course_id = c.id AND l.lesson_order = 1
JOIN `assignments` a ON a.course_id = c.id AND a.lesson_id = l.id AND a.type = 'QUIZ'
WHERE tes.completed_lessons >= 1
ORDER BY tes.student_id, c.id;

INSERT INTO `submissions`
(`assignment_id`, `student_id`, `content`, `file_url`, `original_file_name`, `file_size`, `score`, `feedback`, `status`, `attempt_number`, `late_submission`, `auto_graded`, `submitted_at`, `graded_at`)
SELECT
  a.id,
  tes.student_id,
  CONCAT('Seed homework submission for ', c.course_code, ' lesson ', l.lesson_order, '.'),
  CONCAT('uploads/seed/', LOWER(c.course_code), '-lesson-', LPAD(l.lesson_order, 2, '0'), '-student-', tes.student_id, '.pdf'),
  CONCAT(LOWER(c.course_code), '-lesson-', LPAD(l.lesson_order, 2, '0'), '-student-', tes.student_id, '.pdf'),
  180000 + tes.student_id * 1000 + l.lesson_order * 500,
  CASE WHEN tes.completed_lessons >= 2 THEN ROUND(7 + MOD(tes.student_id + c.id, 4) * 0.75, 1) ELSE NULL END,
  CASE
    WHEN tes.completed_lessons >= 2 THEN CONCAT('Graded homework sample for ', c.course_code, ' lesson ', l.lesson_order, '.')
    ELSE NULL
  END,
  CASE
    WHEN tes.completed_lessons >= 2 THEN 'GRADED'
    ELSE 'SUBMITTED'
  END,
  1,
  b'0',
  b'0',
  TIMESTAMP(DATE_ADD(tes.enrolled_at, INTERVAL l.lesson_order + 12 DAY), '21:00:00'),
  CASE
    WHEN tes.completed_lessons >= 2 THEN TIMESTAMP(DATE_ADD(tes.enrolled_at, INTERVAL l.lesson_order + 13 DAY), '09:00:00')
    ELSE NULL
  END
FROM `tmp_enrollment_seed` tes
JOIN `courses` c ON c.course_code = tes.course_code
JOIN `lessons` l ON l.course_id = c.id AND l.lesson_order = 1
JOIN `assignments` a ON a.course_id = c.id AND a.lesson_id = l.id AND a.type = 'HOMEWORK'
WHERE tes.completed_lessons >= 1
ORDER BY tes.student_id, c.id;

INSERT INTO `submissions`
(`assignment_id`, `student_id`, `content`, `file_url`, `original_file_name`, `file_size`, `score`, `feedback`, `status`, `attempt_number`, `late_submission`, `auto_graded`, `submitted_at`, `graded_at`)
SELECT
  a.id,
  tes.student_id,
  CONCAT('Seed quiz answers for ', c.course_code, ' lesson ', l.lesson_order),
  NULL,
  NULL,
  NULL,
  ROUND(6 + MOD(tes.student_id + c.id + 1, 5), 1),
  CONCAT('Auto-graded seed result for ', c.course_code, ' lesson ', l.lesson_order, '.'),
  'GRADED',
  1,
  b'0',
  b'1',
  TIMESTAMP(DATE_ADD(tes.enrolled_at, INTERVAL l.lesson_order + 15 DAY), '20:15:00'),
  TIMESTAMP(DATE_ADD(tes.enrolled_at, INTERVAL l.lesson_order + 15 DAY), '20:15:00')
FROM `tmp_enrollment_seed` tes
JOIN `courses` c ON c.course_code = tes.course_code
JOIN `lessons` l ON l.course_id = c.id AND l.lesson_order = 2
JOIN `assignments` a ON a.course_id = c.id AND a.lesson_id = l.id AND a.type = 'QUIZ'
WHERE tes.completed_lessons >= 2
ORDER BY tes.student_id, c.id;

INSERT INTO `notifications`
(`id`, `user_id`, `title`, `message`, `type`, `is_read`, `created_at`) VALUES
(1, 5, 'Completed course ITF101', 'You have completed ITF101 and unlocked the next stage of the IT learning path.', 'BADGE', b'0', '2026-03-28 08:15:00'),
(2, 5, 'Homework graded', 'Your homework in PRG101 has been graded. Open assignment history to read the feedback.', 'GRADE', b'0', '2026-03-29 09:30:00'),
(3, 6, 'Next lesson unlocked', 'You passed the latest quiz and the next lesson in WEB201 is now unlocked.', 'ASSIGNMENT', b'0', '2026-03-29 10:00:00'),
(4, 7, 'Mini project reminder', 'API301 mini project is open. Use your lesson notes and forum checklist before submitting.', 'ASSIGNMENT', b'0', '2026-03-30 14:20:00'),
(5, 8, 'Quiz result available', 'Your quiz in DSA201 has been auto-graded. Review the result and continue to the next lesson.', 'GRADE', b'1', '2026-03-30 19:15:00'),
(6, 9, 'Forum discussion updated', 'There is a new reply in the WEB201 mini project discussion topic.', 'FORUM', b'0', '2026-03-31 07:45:00'),
(7, 10, 'Study note saved', 'Your note in PRG101 lesson 1 was saved successfully.', 'SYSTEM', b'1', '2026-03-31 08:10:00'),
(8, 7, 'Course progress milestone', 'You have passed 5 or more lessons in several courses. Keep your streak going.', 'BADGE', b'0', '2026-03-31 12:30:00');

INSERT INTO `badge_definitions`
(`id`, `code`, `name`, `description`, `icon`, `criterion_type`, `threshold_value`, `active`, `display_order`) VALUES
(1, 'FIRST_STEP', 'First Step', 'Complete the first lesson in the system.', 'rocket_launch', 'COMPLETED_LESSONS', 1, b'1', 1),
(2, 'LEARNING_STREAK', 'Steady Progress', 'Complete at least 5 lessons.', 'local_fire_department', 'COMPLETED_LESSONS', 5, b'1', 2),
(3, 'FIRST_GRADE', 'First Grade', 'Receive a grade for at least 1 submission.', 'fact_check', 'GRADED_SUBMISSIONS', 1, b'1', 3),
(4, 'QUIZ_ACE', 'Quiz Specialist', 'Earn a perfect score on at least 1 quiz.', 'psychology', 'PERFECT_QUIZZES', 1, b'1', 4),
(5, 'XP_BRONZE', 'XP Bronze', 'Reach at least 100 accumulated XP.', 'workspace_premium', 'TOTAL_XP', 100, b'1', 5),
(6, 'XP_SILVER', 'XP Silver', 'Reach at least 300 accumulated XP.', 'workspace_premium', 'TOTAL_XP', 300, b'1', 6),
(7, 'COURSE_FINISHER', 'Course Finisher', 'Complete at least 1 course with 100% progress.', 'school', 'COMPLETED_COURSES', 1, b'1', 7);

INSERT INTO `user_badges`
(`id`, `user_id`, `badge_definition_id`, `earned_at`) VALUES
(1, 5, 1, '2026-01-12 10:00:00'),
(2, 5, 3, '2026-02-05 18:15:00'),
(3, 5, 5, '2026-03-10 09:00:00'),
(4, 5, 7, '2026-03-28 08:10:00'),
(5, 7, 1, '2026-01-10 09:00:00'),
(6, 7, 2, '2026-02-20 17:00:00'),
(7, 7, 4, '2026-03-05 20:15:00'),
(8, 7, 5, '2026-03-05 20:16:00'),
(9, 9, 1, '2026-01-14 11:30:00');

DROP TEMPORARY TABLE IF EXISTS `tmp_enrollment_seed`;
DROP TEMPORARY TABLE IF EXISTS `tmp_lesson_seed`;
DROP TEMPORARY TABLE IF EXISTS `tmp_prereq_seed`;
DROP TEMPORARY TABLE IF EXISTS `tmp_course_seed`;

ALTER TABLE `departments` AUTO_INCREMENT = 100;
ALTER TABLE `users` AUTO_INCREMENT = 100;
ALTER TABLE `badge_definitions` AUTO_INCREMENT = 100;
ALTER TABLE `user_badges` AUTO_INCREMENT = 100;
ALTER TABLE `courses` AUTO_INCREMENT = 100;
ALTER TABLE `course_prerequisites` AUTO_INCREMENT = 100;
ALTER TABLE `course_sections` AUTO_INCREMENT = 100;
ALTER TABLE `lessons` AUTO_INCREMENT = 100;
ALTER TABLE `enrollments` AUTO_INCREMENT = 100;
ALTER TABLE `assignments` AUTO_INCREMENT = 1000;
ALTER TABLE `quiz_questions` AUTO_INCREMENT = 5000;
ALTER TABLE `forum_posts` AUTO_INCREMENT = 100;
ALTER TABLE `comments` AUTO_INCREMENT = 100;
ALTER TABLE `submissions` AUTO_INCREMENT = 100;
ALTER TABLE `lesson_progress` AUTO_INCREMENT = 100;
ALTER TABLE `notes` AUTO_INCREMENT = 100;
ALTER TABLE `note_links` AUTO_INCREMENT = 100;
ALTER TABLE `notifications` AUTO_INCREMENT = 100;


