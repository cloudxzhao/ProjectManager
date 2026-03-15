-- ProjectHub 数据库迁移 - V6
-- 功能：添加业务服务模块（Business Service）
-- 数据库：PostgreSQL 15+
-- Flyway 版本：V6
-- 创建日期：2026-03-15

-- ============================================
-- 1. 创建业务服务表
-- ============================================

CREATE TABLE IF NOT EXISTS business_service (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    code VARCHAR(50) NOT NULL,
    owner_id BIGINT,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    position INTEGER DEFAULT 0 NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- 添加表注释
COMMENT ON TABLE business_service IS '业务服务表';
COMMENT ON COLUMN business_service.id IS '主键 ID';
COMMENT ON COLUMN business_service.project_id IS '所属项目 ID';
COMMENT ON COLUMN business_service.name IS '服务名称';
COMMENT ON COLUMN business_service.description IS '服务描述';
COMMENT ON COLUMN business_service.code IS '服务代码标识（如 USER_SERVICE）';
COMMENT ON COLUMN business_service.owner_id IS '服务负责人 ID';
COMMENT ON COLUMN business_service.status IS '状态：ACTIVE/INACTIVE/ARCHIVED';
COMMENT ON COLUMN business_service.position IS '排序位置';
COMMENT ON COLUMN business_service.created_at IS '创建时间';
COMMENT ON COLUMN business_service.updated_at IS '更新时间';
COMMENT ON COLUMN business_service.deleted_at IS '删除时间（软删除）';

-- ============================================
-- 2. 创建索引
-- ============================================

-- 项目查询索引
CREATE INDEX idx_service_project ON business_service(project_id, deleted_at);

-- 状态查询索引
CREATE INDEX idx_service_status ON business_service(status);

-- 唯一约束：同一项目下服务代码唯一
CREATE UNIQUE INDEX uk_project_code ON business_service(project_id, code, deleted_at);

-- ============================================
-- 3. 添加外键约束
-- ============================================

ALTER TABLE business_service
ADD CONSTRAINT fk_service_project
    FOREIGN KEY (project_id)
    REFERENCES project(id)
    ON DELETE CASCADE;

ALTER TABLE business_service
ADD CONSTRAINT fk_service_owner
    FOREIGN KEY (owner_id)
    REFERENCES sys_user(id)
    ON DELETE SET NULL;

-- ============================================
-- 4. 修改用户故事表，新增服务关联字段
-- ============================================

-- 添加 service_id 字段
ALTER TABLE user_story
ADD COLUMN IF NOT EXISTS service_id BIGINT;

-- 添加字段注释
COMMENT ON COLUMN user_story.service_id IS '所属业务服务 ID';

-- 添加外键约束
ALTER TABLE user_story
ADD CONSTRAINT fk_story_service
    FOREIGN KEY (service_id)
    REFERENCES business_service(id)
    ON DELETE SET NULL;

-- 创建服务索引
CREATE INDEX IF NOT EXISTS idx_story_service ON user_story(service_id, deleted_at);

-- 创建项目 + 服务联合查询索引
CREATE INDEX IF NOT EXISTS idx_story_project_service ON user_story(project_id, service_id, deleted_at);

-- ============================================
-- 5. 更新用户故事视图，包含服务信息
-- ============================================

-- 删除旧视图
DROP VIEW IF EXISTS v_user_story_detail;

-- 重新创建视图，添加服务信息
CREATE VIEW v_user_story_detail AS
SELECT
    us.id AS user_story_id,
    us.title AS user_story_title,
    us.status AS user_story_status,
    us.priority AS user_story_priority,
    us.story_points,
    us.position,
    us.created_at,
    us.updated_at,
    p.id AS project_id,
    p.name AS project_name,
    e.id AS epic_id,
    e.title AS epic_title,
    bs.id AS service_id,
    bs.name AS service_name,
    bs.code AS service_code,
    au.username AS assignee_username,
    au.email AS assignee_email
FROM user_story us
LEFT JOIN project p ON us.project_id = p.id
LEFT JOIN epic e ON us.epic_id = e.id
LEFT JOIN business_service bs ON us.service_id = bs.id
LEFT JOIN sys_user au ON us.assignee_id = au.id
WHERE us.deleted_at IS NULL;
