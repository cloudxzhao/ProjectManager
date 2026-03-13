-- V4: 添加用户昵称字段
-- 为 sys_user 表添加 nickname 字段

ALTER TABLE sys_user ADD COLUMN nickname VARCHAR(50);

-- 创建索引
CREATE INDEX idx_user_nickname ON sys_user(nickname);