-- Compatibility migration for legacy databases used to test Forum + Note features.
-- Safe to run on a database that may already contain some or all of these changes.
-- Recommended order:
--   1. schema-standard.sql (or your current schema)
--   2. migration-forum-note-compat.sql
--   3. seed-forum-note-test.sql

SET NAMES utf8mb4;

-- Global forum requires course_id to be nullable.
ALTER TABLE `forum_posts`
    MODIFY COLUMN `course_id` BIGINT NULL;

-- forum_posts.lesson_id
SET @ddl := IF(
    (SELECT COUNT(*)
     FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'forum_posts'
       AND COLUMN_NAME = 'lesson_id') = 0,
    'ALTER TABLE `forum_posts` ADD COLUMN `lesson_id` BIGINT NULL DEFAULT NULL AFTER `course_id`',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := IF(
    (SELECT COUNT(*)
     FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'forum_posts'
       AND INDEX_NAME = 'idx_forum_posts_lesson_id') = 0,
    'ALTER TABLE `forum_posts` ADD KEY `idx_forum_posts_lesson_id` (`lesson_id`)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := IF(
    (SELECT COUNT(*)
     FROM information_schema.REFERENTIAL_CONSTRAINTS
     WHERE CONSTRAINT_SCHEMA = DATABASE()
       AND CONSTRAINT_NAME = 'fk_forum_posts_lesson') = 0,
    'ALTER TABLE `forum_posts` ADD CONSTRAINT `fk_forum_posts_lesson` FOREIGN KEY (`lesson_id`) REFERENCES `lessons` (`id`) ON DELETE SET NULL ON UPDATE CASCADE',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- notes.source_excerpt
SET @ddl := IF(
    (SELECT COUNT(*)
     FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'notes'
       AND COLUMN_NAME = 'source_excerpt') = 0,
    'ALTER TABLE `notes` ADD COLUMN `source_excerpt` TEXT NULL COMMENT ''Quote captured from lesson'' AFTER `note_type`',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- notes.tags
SET @ddl := IF(
    (SELECT COUNT(*)
     FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'notes'
       AND COLUMN_NAME = 'tags') = 0,
    'ALTER TABLE `notes` ADD COLUMN `tags` VARCHAR(500) NULL COMMENT ''Comma-separated tags'' AFTER `source_excerpt`',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- note_links graph table
CREATE TABLE IF NOT EXISTS `note_links` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
