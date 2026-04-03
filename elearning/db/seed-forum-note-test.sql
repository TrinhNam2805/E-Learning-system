-- Self-contained demo data for testing Forum + Note features.
-- Creates a dedicated teacher, two students, one course, three lessons,
-- lesson-linked forum posts, comments, lesson notes, standalone notes and note links.
-- Recommended order:
--   1. migration-forum-note-compat.sql
--   2. seed-forum-note-test.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM `comments` WHERE `id` BETWEEN 9101 AND 9199;
DELETE FROM `note_links` WHERE `id` BETWEEN 9101 AND 9199 OR `from_note_id` BETWEEN 9101 AND 9199 OR `to_note_id` BETWEEN 9101 AND 9199;
DELETE FROM `notes` WHERE `id` BETWEEN 9101 AND 9199;
DELETE FROM `lesson_progress` WHERE `id` BETWEEN 9101 AND 9199;
DELETE FROM `forum_posts` WHERE `id` BETWEEN 9101 AND 9199;
DELETE FROM `enrollments` WHERE `id` BETWEEN 9101 AND 9199;
DELETE FROM `lessons` WHERE `id` BETWEEN 9101 AND 9199;
DELETE FROM `courses` WHERE `id` = 9101;
DELETE FROM `users` WHERE `id` BETWEEN 9101 AND 9103;
DELETE FROM `departments` WHERE `id` = 9101;

SET FOREIGN_KEY_CHECKS = 1;

-- Password for all demo accounts below: Demo@2024
SET @pwd := '$2b$10$IgwunfhzoNX76OVOOWU1.eKSB7SmLMjNAdTvjYpuY79/U0ebJmcpy';

INSERT INTO `departments` (`id`, `code`, `name`) VALUES
(9101, 'TEST', 'Forum and Note Test Department');

INSERT INTO `users`
(`id`, `username`, `email`, `password`, `full_name`, `role`, `phone`, `active`, `locked`, `created_at`, `department_id`, `student_code`) VALUES
(9101, 'teacher.forum.note', 'teacher.forum.note@test.local', @pwd, 'Forum Note Teacher', 'TEACHER', '0900009101', 1, 0, '2026-01-10', 9101, NULL),
(9102, 'student.alpha', 'student.alpha@test.local', @pwd, 'Student Alpha', 'STUDENT', '0900009102', 1, 0, '2026-01-10', 9101, 'TEST9102'),
(9103, 'student.beta', 'student.beta@test.local', @pwd, 'Student Beta', 'STUDENT', '0900009103', 1, 0, '2026-01-10', 9101, 'TEST9103');

INSERT INTO `courses`
(`id`, `course_code`, `course_name`, `description`, `teacher_id`, `enroll_password`, `semester`, `academic_year`, `status`, `max_students`, `thumbnail`, `department_id`) VALUES
(9101, 'FORUMNOTE101', 'Forum and Note Integration Testing', 'Dedicated sample course for testing lesson-linked forum posts and advanced note flows.', 9101, 'TEST9101', 'HK2', '2025-2026', 'PUBLISHED', 30, '/images/e-learning.jpg', 9101);

INSERT INTO `lessons`
(`id`, `course_id`, `lesson_order`, `lesson_title`, `lesson_content`, `video_url`, `duration_minutes`, `published`) VALUES
(9101, 9101, 1, 'Lesson 1 - Forum linkage basics',
'<h2>Overview</h2><p>This lesson is used to test creating a forum topic linked to a specific lesson.</p><p>Highlighted excerpt: Forum topics can reference a lesson for context.</p>',
NULL, 35, 1),
(9102, 9101, 2, 'Lesson 2 - Note excerpts and tags',
'<h2>Overview</h2><p>This lesson is used to test note excerpts, note tags and opening a note back in its lesson.</p><p>Highlighted excerpt: Good notes keep the key quote close to the original lesson context.</p>',
NULL, 40, 1),
(9103, 9101, 3, 'Lesson 3 - Cross-linking notes',
'<h2>Overview</h2><p>This lesson is used to test note-to-note links and lesson-aware discussions.</p><p>Highlighted excerpt: Linking notes helps connect prerequisites and follow-up concepts.</p>',
NULL, 45, 1);

INSERT INTO `enrollments`
(`id`, `student_id`, `course_id`, `enrolled_at`, `status`, `progress_percentage`, `activity_xp`, `total_xp`) VALUES
(9101, 9102, 9101, '2026-02-01', 'ACTIVE', 67, 20, 60),
(9102, 9103, 9101, '2026-02-01', 'ACTIVE', 34, 0, 20);

INSERT INTO `lesson_progress`
(`id`, `student_id`, `lesson_id`, `completed`, `completed_at`) VALUES
(9101, 9102, 9101, 1, '2026-02-03'),
(9102, 9102, 9102, 1, '2026-02-05'),
(9103, 9103, 9101, 1, '2026-02-04');

INSERT INTO `forum_posts`
(`id`, `course_id`, `lesson_id`, `author_id`, `title`, `content`, `post_type`, `view_count`, `created_at`) VALUES
(9101, NULL, NULL, 9101, 'Global announcement for testing', 'This global post is used to verify the forum still supports channel-wide announcements.', 'ANNOUNCEMENT', 12, '2026-02-06 08:00:00'),
(9102, 9101, NULL, 9101, 'Course-wide reminder before lesson discussion', 'Use this course forum to ask follow-up questions, and link the topic to a lesson when the discussion is lesson-specific.', 'ANNOUNCEMENT', 9, '2026-02-06 08:30:00'),
(9103, 9101, 9101, 9102, 'Question about lesson 1 forum linkage', 'I created this topic to test that a forum post can be attached directly to Lesson 1 and shown back in the forum UI.', 'QUESTION', 17, '2026-02-06 09:00:00'),
(9104, 9101, 9102, 9103, 'Discussion on note excerpts from lesson 2', 'This post is linked to Lesson 2 so we can verify the lesson badge, the post detail page and the related lesson shortcut.', 'DISCUSSION', 14, '2026-02-06 09:20:00'),
(9105, 9101, 9103, 9101, 'Lesson 3 note-linking examples', 'Use this topic to collect examples of how one lesson note can reference another note.', 'DISCUSSION', 7, '2026-02-06 10:00:00');

INSERT INTO `comments`
(`id`, `post_id`, `author_id`, `content`, `created_at`) VALUES
(9101, 9103, 9101, 'Yes. If the topic is about one lesson only, keep the lesson link so students can jump back to the right material.', '2026-02-06 09:10:00'),
(9102, 9104, 9102, 'The excerpt preview is useful because it preserves the exact quote I selected in the lesson.', '2026-02-06 09:40:00'),
(9103, 9105, 9103, 'I linked my summary note to the note that contains the original explanation.', '2026-02-06 10:15:00');

INSERT INTO `notes`
(`id`, `student_id`, `lesson_id`, `course_id`, `content`, `highlight_color`, `title`, `note_type`, `source_excerpt`, `tags`, `created_at`, `updated_at`) VALUES
(9101, 9102, 9101, 9101, 'Key takeaway: lesson-linked forum topics give context and make discussions easier to find later.', '#FFFF00', 'Lesson 1 forum note', 'LESSON', 'Forum topics can reference a lesson for context.', 'forum,lesson-link,context', '2026-02-03 09:00:00', '2026-02-03 09:00:00'),
(9102, 9102, 9102, 9101, 'Key takeaway: save the exact quote so the note remains anchored to the original explanation.', '#87CEEB', 'Lesson 2 excerpt note', 'LESSON', 'Good notes keep the key quote close to the original lesson context.', 'notes,excerpt,tags', '2026-02-05 11:00:00', '2026-02-05 11:00:00'),
(9103, 9102, NULL, 9101, 'Standalone summary: lesson 1 introduces the forum link; lesson 2 explains excerpt-based notes; lesson 3 connects notes together.', '#90EE90', 'Standalone review note', 'STANDALONE', NULL, 'review,standalone,summary', '2026-02-05 18:00:00', '2026-02-05 18:00:00'),
(9104, 9103, 9101, 9101, 'Student Beta note: remember to open the related lesson directly from the forum post detail page.', '#FFB6C1', 'Student Beta lesson note', 'LESSON', 'Forum topics can reference a lesson for context.', 'forum,open-lesson', '2026-02-04 10:30:00', '2026-02-04 10:30:00');

INSERT INTO `note_links`
(`id`, `from_note_id`, `to_note_id`, `relation_label`, `created_at`) VALUES
(9101, 9103, 9101, 'relates-to', '2026-02-05 18:05:00'),
(9102, 9102, 9101, 'prerequisite', '2026-02-05 11:05:00');

ALTER TABLE `departments` AUTO_INCREMENT = 9200;
ALTER TABLE `users` AUTO_INCREMENT = 9200;
ALTER TABLE `courses` AUTO_INCREMENT = 9200;
ALTER TABLE `lessons` AUTO_INCREMENT = 9200;
ALTER TABLE `enrollments` AUTO_INCREMENT = 9200;
ALTER TABLE `forum_posts` AUTO_INCREMENT = 9200;
ALTER TABLE `comments` AUTO_INCREMENT = 9200;
ALTER TABLE `lesson_progress` AUTO_INCREMENT = 9200;
ALTER TABLE `notes` AUTO_INCREMENT = 9200;
ALTER TABLE `note_links` AUTO_INCREMENT = 9200;
