CREATE DATABASE IF NOT EXISTS `videoai` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `videoai`;

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_no` VARCHAR(32) NOT NULL COMMENT '用户编号',
  `username` VARCHAR(64) NOT NULL COMMENT '用户名',
  `password` VARCHAR(128) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(64) NOT NULL COMMENT '昵称',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `quota_limit` INT NOT NULL DEFAULT 10000 COMMENT '用户 AI token 配额',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_no` (`user_no`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `media_file` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `file_no` VARCHAR(32) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `file_name` VARCHAR(255) NOT NULL,
  `file_md5` VARCHAR(64) NOT NULL COMMENT '内容级去重 md5',
  `file_size` BIGINT NOT NULL,
  `file_ext` VARCHAR(32) DEFAULT NULL,
  `mime_type` VARCHAR(128) DEFAULT NULL,
  `bucket_name` VARCHAR(128) DEFAULT NULL,
  `object_name` VARCHAR(255) NOT NULL,
  `storage_type` VARCHAR(32) NOT NULL COMMENT 'MINIO/LOCAL',
  `upload_status` TINYINT NOT NULL DEFAULT 2,
  `ref_count` INT NOT NULL DEFAULT 1 COMMENT '引用次数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_md5` (`file_md5`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='媒体文件表';

CREATE TABLE IF NOT EXISTS `upload_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `upload_id` VARCHAR(64) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `file_name` VARCHAR(255) NOT NULL,
  `file_md5` VARCHAR(64) NOT NULL,
  `file_size` BIGINT NOT NULL,
  `chunk_size` INT NOT NULL,
  `total_chunks` INT NOT NULL,
  `uploaded_chunks_json` JSON DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-init 1-uploading 2-merged 3-fast_hit',
  `media_file_id` BIGINT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_upload_id` (`upload_id`),
  KEY `idx_user_file_md5` (`user_id`, `file_md5`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上传记录表';

CREATE TABLE IF NOT EXISTS `video_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_no` VARCHAR(32) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `file_id` BIGINT NOT NULL,
  `file_md5` VARCHAR(64) NOT NULL,
  `video_title` VARCHAR(255) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT 'PENDING/UPLOADED/QUEUED/PROCESSING_AUDIO/TRANSCRIBING/SUMMARIZING/INDEXING/SUCCESS/FAILED',
  `current_step` VARCHAR(64) DEFAULT NULL,
  `progress_percent` INT NOT NULL DEFAULT 0,
  `retry_count` INT NOT NULL DEFAULT 0,
  `fail_reason` VARCHAR(1024) DEFAULT NULL,
  `session_id` VARCHAR(64) DEFAULT NULL,
  `started_at` DATETIME DEFAULT NULL,
  `finished_at` DATETIME DEFAULT NULL,
  `cost_time_ms` BIGINT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_file_id` (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频解析任务表';

CREATE TABLE IF NOT EXISTS `video_transcript` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `file_id` BIGINT NOT NULL,
  `language` VARCHAR(32) DEFAULT 'zh-CN',
  `duration_ms` BIGINT DEFAULT NULL,
  `word_count` INT NOT NULL DEFAULT 0,
  `transcript_text` LONGTEXT NOT NULL,
  `segment_json` LONGTEXT DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频转写表';

CREATE TABLE IF NOT EXISTS `video_transcript_segment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` BIGINT NOT NULL,
  `transcript_id` BIGINT NOT NULL,
  `segment_index` INT NOT NULL,
  `start_time_ms` BIGINT DEFAULT NULL,
  `end_time_ms` BIGINT DEFAULT NULL,
  `content` TEXT NOT NULL,
  `keywords` VARCHAR(512) DEFAULT NULL COMMENT '关键词召回字段',
  `token_count` INT DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_task_segment` (`task_id`, `segment_index`),
  FULLTEXT KEY `ft_content_keywords` (`content`, `keywords`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频转写分段表，用于 RAG 检索';

CREATE TABLE IF NOT EXISTS `video_summary` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` BIGINT NOT NULL,
  `file_id` BIGINT NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `summary` TEXT NOT NULL,
  `outline_json` JSON DEFAULT NULL,
  `keywords_json` JSON DEFAULT NULL,
  `highlights_json` JSON DEFAULT NULL,
  `qa_suggestions_json` JSON DEFAULT NULL,
  `model_name` VARCHAR(64) DEFAULT NULL,
  `prompt_tokens` INT DEFAULT 0,
  `completion_tokens` INT DEFAULT 0,
  `total_tokens` INT DEFAULT 0,
  `cost_time_ms` BIGINT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_summary_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频摘要表';

CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `session_id` VARCHAR(64) NOT NULL,
  `role` VARCHAR(32) NOT NULL,
  `message_type` VARCHAR(32) NOT NULL COMMENT 'QA/AGENT_QA',
  `content` TEXT NOT NULL,
  `cited_segments_json` JSON DEFAULT NULL,
  `model_name` VARCHAR(64) DEFAULT NULL,
  `total_tokens` INT DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_task_session` (`task_id`, `session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答消息表';

CREATE TABLE IF NOT EXISTS `ai_usage_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `task_id` BIGINT DEFAULT NULL,
  `biz_type` VARCHAR(32) NOT NULL COMMENT 'SUMMARY/QA/AGENT_QA/ASR',
  `request_hash` VARCHAR(64) DEFAULT NULL,
  `model_name` VARCHAR(64) DEFAULT NULL,
  `prompt_tokens` INT DEFAULT 0,
  `completion_tokens` INT DEFAULT 0,
  `total_tokens` INT DEFAULT 0,
  `duration_ms` BIGINT DEFAULT NULL,
  `hit_cache` TINYINT NOT NULL DEFAULT 0,
  `success` TINYINT NOT NULL DEFAULT 1,
  `error_message` VARCHAR(1024) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_biz_type` (`user_id`, `biz_type`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 使用记录表';

CREATE TABLE IF NOT EXISTS `task_event_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` BIGINT NOT NULL,
  `task_no` VARCHAR(32) NOT NULL,
  `from_status` VARCHAR(64) DEFAULT NULL,
  `to_status` VARCHAR(64) DEFAULT NULL,
  `step` VARCHAR(64) DEFAULT NULL,
  `event_type` VARCHAR(64) DEFAULT NULL,
  `success` TINYINT NOT NULL DEFAULT 1,
  `detail` VARCHAR(1024) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_task_create_time` (`task_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务事件日志表';

INSERT INTO `user` (`user_no`, `username`, `password`, `nickname`, `status`, `quota_limit`)
VALUES ('U202604170001', 'demo_user', '$2a$10$lUKUzWgudVPHiZo2E6lKEOczdqRqcAfIIVLWfJcR2yMU1Mt6DHPbi', '演示账号', 1, 10000)
ON DUPLICATE KEY UPDATE `nickname` = VALUES(`nickname`);
