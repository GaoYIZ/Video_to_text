-- 创建数据库
CREATE DATABASE IF NOT EXISTS video_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE video_db;

-- 创建视频信息表
CREATE TABLE IF NOT EXISTS video_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    video_path VARCHAR(500) NOT NULL COMMENT '视频文件路径',
    audio_path VARCHAR(500) DEFAULT NULL COMMENT '音频文件路径',
    transcript TEXT COMMENT '转录文本',
    summary TEXT COMMENT '总结内容',
    status VARCHAR(50) DEFAULT 'UPLOADED' COMMENT '状态: UPLOADED, CONVERTED, TRANSCRIBED, SUMMARIZED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频信息表';

