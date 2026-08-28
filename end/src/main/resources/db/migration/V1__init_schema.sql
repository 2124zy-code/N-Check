-- ==============================================================================
-- Flyway Database Migration: V1 Initial Schema Definition for N-Check
-- ==============================================================================

-- 1. 用户体系表 (SysUser)
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户主键ID',
    `username` VARCHAR(64) NOT NULL UNIQUE COMMENT '登录账号名',
    `password_hash` VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密哈希密码',
    `nickname` VARCHAR(64) NOT NULL COMMENT '用户昵称/显示名',
    `avatar_color` VARCHAR(32) NOT NULL DEFAULT '#4f46e5' COMMENT '头像主题色',
    `role` VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN, USER',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 2. 目标企业档案表 (TbCompany)
CREATE TABLE IF NOT EXISTS `tb_company` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '企业主键ID',
    `user_id` BIGINT NOT NULL COMMENT '所属租户用户ID',
    `company_code` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '企业代号标识 (如 bytedance, tencent)',
    `name` VARCHAR(128) NOT NULL COMMENT '企业名称 (如 字节跳动)',
    `logo` MEDIUMTEXT NULL COMMENT '企业Logo (代码/Base64/URL)',
    `industry` VARCHAR(128) NOT NULL DEFAULT '互联网' COMMENT '所属行业',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建档时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_company_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='目标企业档案表';

-- 3. 面试题库与题解表 (TbEntry)
CREATE TABLE IF NOT EXISTS `tb_entry` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '题目主键ID',
    `user_id` BIGINT NOT NULL COMMENT '所属租户用户ID',
    `company_id` BIGINT NOT NULL COMMENT '关联企业ID',
    `type` VARCHAR(32) NOT NULL COMMENT '内容类型: 八股文 / 算法题',
    `title` VARCHAR(255) NOT NULL COMMENT '题目标题 / 核心考点',
    `difficulty` VARCHAR(32) NOT NULL DEFAULT '中等' COMMENT '难度: 简单 / 中等 / 困难',
    `status` VARCHAR(32) NOT NULL DEFAULT '未掌握' COMMENT '掌握熟练度: 未掌握 / 学习中 / 已掌握',
    `is_starred` TINYINT NOT NULL DEFAULT 0 COMMENT '是否星标压轴: 0-否, 1-是',
    `tags` JSON NULL COMMENT '标签分类体系 (JSON 数组)',
    `content` LONGTEXT NULL COMMENT 'Markdown题解 / 算法代码正文',
    `last_reviewed_at` DATETIME NULL COMMENT '最近复习时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_entry_user_company` (`user_id`, `company_id`),
    INDEX `idx_entry_user_status` (`user_id`, `status`),
    INDEX `idx_entry_user_starred` (`user_id`, `is_starred`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面经题库与题解表';

-- 4. 每日复盘随笔表 (TbDailyNote)
CREATE TABLE IF NOT EXISTS `tb_daily_note` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `review_date` VARCHAR(10) NOT NULL COMMENT '复盘日期 (格式: YYYY-MM-DD)',
    `note_content` LONGTEXT NULL COMMENT '复盘随笔与心得',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_date` (`user_id`, `review_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日复盘随笔表';
