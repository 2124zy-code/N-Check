-- ==============================================================================
-- Flyway Database Migration: V2 Default Seed Data for N-Check
-- 仅预置系统内置默认账号 (admin / 123456)，不预置任何企业与题目数据
-- ==============================================================================

-- 预置默认管理员用户 (admin / 123456)
INSERT INTO `sys_user` (`id`, `username`, `password_hash`, `nickname`, `avatar_color`, `role`, `create_time`, `update_time`)
VALUES (1, 'admin', '$2a$10$GXCnSznMoOUuMtVzsSWeKefKcdu6e0un7kCNR4fCUMHxPTL2W2zuK', '管理员', '#4f46e5', 'ADMIN', NOW(), NOW())
ON DUPLICATE KEY UPDATE `password_hash`='$2a$10$GXCnSznMoOUuMtVzsSWeKefKcdu6e0un7kCNR4fCUMHxPTL2W2zuK';
