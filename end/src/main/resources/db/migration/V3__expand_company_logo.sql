-- ==============================================================================
-- Flyway Database Migration: V3 Expand Company Logo Capacity
-- 将 tb_company 表的 logo 字段扩容为 MEDIUMTEXT，全面支持长 URL、SVG 和 Base64 图片
-- ==============================================================================

ALTER TABLE `tb_company` MODIFY COLUMN `logo` MEDIUMTEXT NULL COMMENT '企业Logo (代码/Base64/URL)';
