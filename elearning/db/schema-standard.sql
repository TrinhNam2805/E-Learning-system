-- =============================================================================
-- LÆ°á»£c Ä‘á»“ CSDL chuáº©n hÃ³a cho há»‡ E-Learning há»c thuáº­t (Khoa CNTT / Ä‘áº¡i há»c)
-- MySQL 8.x, InnoDB, utf8mb4_unicode_ci
-- =============================================================================
-- NguyÃªn táº¯c thiáº¿t káº¿ (tham chiáº¿u giÃ¡o trÃ¬nh CSDL / quy trÃ¬nh Ä‘Ã o táº¡o):
--   1) KhÃ³a chÃ­nh surrogate (BIGINT AUTO_INCREMENT), khÃ³a nghiá»‡p vá»¥ (mÃ£ mÃ´n, MSSV) UNIQUE.
--   2) 3NF: mÃ´ táº£ khoa tÃ¡ch báº£ng departments; khÃ´ng lá»“ng giÃ¡ trá»‹ Ä‘a trá»‹ trong má»™t cá»™t.
--   3) ToÃ n váº¹n tham chiáº¿u: FK ON DELETE RESTRICT (hoáº·c CASCADE cÃ³ chá»§ Ä‘Ã­ch) â€” trÃ¡nh má»“ cÃ´i dá»¯ liá»‡u.
--   4) ÄÄƒng kÃ½ há»c pháº§n: enrollments = quan há»‡ N:M sinh viÃªn â†” lá»›p há»c pháº§n (courses = má»™t láº§n má»Ÿ lá»›p theo ká»³).
--   5) TiÃªn quyáº¿t há»c pháº§n: báº£ng course_prerequisites (mÃ£ mÃ´n tiÃªn quyáº¿t theo mÃ£ CTÄT â€” lÆ°u mÃ£ chuá»—i Ä‘á»ƒ linh hoáº¡t nhiá»u ká»³ má»Ÿ lá»›p).
--   6) Chá»‰ má»¥c trÃªn FK vÃ  cÃ¡c cá»™t lá»c thÆ°á»ng dÃ¹ng (status, role, semester).
--
-- CÃ¡ch dÃ¹ng: táº¡o database rá»“i cháº¡y file nÃ y, sau Ä‘Ã³ cháº¡y seed-data.sql (ddl-auto=none).
-- DB Ä‘Ã£ táº¡o tá»« báº£n cÅ© (thiáº¿u course_sections / lessons.section_id): cháº¡y thÃªm db/migration-course-sections.sql.
--   mysql -u root -p --default-character-set=utf8mb4 -e "CREATE DATABASE IF NOT EXISTS \`e-learning\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
--   mysql -u root -p --default-character-set=utf8mb4 e-learning < elearning/db/schema-standard.sql
-- =============================================================================

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

-- -----------------------------------------------------------------------------
-- Khoa / Ä‘Æ¡n vá»‹ Ä‘Ã o táº¡o (vÃ­ dá»¥: Khoa CÃ´ng nghá»‡ ThÃ´ng tin)
-- -----------------------------------------------------------------------------
CREATE TABLE `departments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(20) NOT NULL COMMENT 'MÃ£ khoa, vÃ­ dá»¥ CNTT',
  `name` VARCHAR(200) NOT NULL COMMENT 'TÃªn Ä‘áº§y Ä‘á»§',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_departments_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='ÄÆ¡n vá»‹ quáº£n lÃ½ há»c pháº§n vÃ  cÃ¡n bá»™';

-- -----------------------------------------------------------------------------
-- NgÆ°á»i dÃ¹ng: sinh viÃªn / giáº£ng viÃªn / quáº£n trá»‹
-- student_code: MSSV
-- -----------------------------------------------------------------------------
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

-- -----------------------------------------------------------------------------
-- Há»c pháº§n theo ká»³ (lá»›p há»c pháº§n): má»™t hÃ ng = má»™t láº§n má»Ÿ lá»›p (HK + nÄƒm há»c)
-- credits / theory_hours / practice_hours: khá»‘i lÆ°á»£ng theo Ä‘á» cÆ°Æ¡ng CTÄT
-- -----------------------------------------------------------------------------
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

-- -----------------------------------------------------------------------------
-- TiÃªn quyáº¿t: há»c pháº§n (course_id) yÃªu cáº§u Ä‘Ã£ qua mÃ´n cÃ³ mÃ£ prerequisite_course_code
-- (Chuáº©n CTÄT: quan há»‡ giá»¯a cÃ¡c mÃ£ há»c pháº§n; khÃ´ng rÃ ng buá»™c FK tá»›i courses Ä‘á»ƒ trÃ¹ng mÃ£ nhiá»u ká»³)
-- -----------------------------------------------------------------------------
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

-- Pháº§n/chÆ°Æ¡ng trong khÃ³a (curriculum kiá»ƒu Udemy)
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

