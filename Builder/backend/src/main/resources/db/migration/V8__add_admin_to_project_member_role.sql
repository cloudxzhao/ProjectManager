-- ProjectHub 数据库迁移 - V8
-- Flyway 版本：V8
-- 说明：添加 ADMIN 角色到项目成员枚举

-- 添加 ADMIN 角色到项目成员枚举
ALTER TYPE project_member_role ADD VALUE IF NOT EXISTS 'ADMIN' AFTER 'OWNER';
