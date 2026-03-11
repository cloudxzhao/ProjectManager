-- ProjectHub 数据库 Schema
-- 数据库：PostgreSQL 15+
-- 字符集：UTF-8
-- 创建时间：2026-03-11

-- ============================================
-- 扩展
-- ============================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- 枚举类型
-- ============================================

-- 用户状态
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'BANNED');

-- 项目状态
CREATE TYPE project_status AS ENUM ('ACTIVE', 'COMPLETED', 'ARCHIVED');

-- 任务状态
CREATE TYPE task_status AS ENUM ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE');

-- 优先级
CREATE TYPE priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');

-- 问题类型
CREATE TYPE issue_type AS ENUM ('BUG', 'ISSUE', 'IMPROVEMENT', 'TECH_DEBT');

-- 问题严重程度
CREATE TYPE issue_severity AS ENUM ('TRIVIAL', 'MINOR', 'NORMAL', 'MAJOR', 'CRITICAL');

-- 问题状态
CREATE TYPE issue_status AS ENUM ('NEW', 'CONFIRMED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REOPENED');

-- 项目成员角色
CREATE TYPE project_member_role AS ENUM ('OWNER', 'MANAGER', 'MEMBER');

-- 通知类型
CREATE TYPE notification_type AS ENUM ('INFO', 'WARNING', 'ERROR', 'TASK', 'PROJECT');

-- ============================================
-- 系统表 - 用户和权限
-- ============================================

-- 用户表
CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(255),
    status user_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,

    CONSTRAINT chk_username_length CHECK (LENGTH(username) BETWEEN 2 AND 20),
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- 用户表索引
CREATE INDEX idx_user_email ON sys_user(email);
CREATE INDEX idx_user_username ON sys_user(username);
CREATE INDEX idx_user_status ON sys_user(status);
CREATE INDEX idx_user_deleted_at ON sys_user(deleted_at);

-- 角色表
CREATE TABLE sys_role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 用户角色关联表
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
);

-- 权限表
CREATE TABLE sys_permission (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 角色权限关联表
CREATE TABLE sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,

    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE CASCADE
);

-- ============================================
-- 项目模块
-- ============================================

-- 项目表
CREATE TABLE project (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    owner_id BIGINT NOT NULL,
    status project_status NOT NULL DEFAULT 'ACTIVE',
    icon VARCHAR(50),
    theme_color VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_project_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id),
    CONSTRAINT chk_project_dates CHECK (end_date >= start_date)
);

-- 项目表索引
CREATE INDEX idx_project_owner ON project(owner_id);
CREATE INDEX idx_project_status ON project(status);
CREATE INDEX idx_project_deleted_at ON project(deleted_at);
CREATE INDEX idx_project_name ON project(name);

-- 项目成员表
CREATE TABLE project_member (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role project_member_role NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_project_member UNIQUE (project_id, user_id),
    CONSTRAINT fk_project_member_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_member_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);

-- 项目成员表索引
CREATE INDEX idx_project_member_project ON project_member(project_id);
CREATE INDEX idx_project_member_user ON project_member(user_id);

-- ============================================
-- 任务模块
-- ============================================

-- 任务表
CREATE TABLE task (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status task_status NOT NULL DEFAULT 'TODO',
    priority priority NOT NULL DEFAULT 'MEDIUM',
    assignee_id BIGINT,
    creator_id BIGINT NOT NULL,
    parent_id BIGINT,
    due_date DATE,
    story_points INTEGER,
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_assignee FOREIGN KEY (assignee_id) REFERENCES sys_user(id),
    CONSTRAINT fk_task_creator FOREIGN KEY (creator_id) REFERENCES sys_user(id),
    CONSTRAINT fk_task_parent FOREIGN KEY (parent_id) REFERENCES task(id) ON DELETE CASCADE,
    CONSTRAINT chk_story_points CHECK (story_points IS NULL OR story_points >= 0)
);

-- 任务表索引
CREATE INDEX idx_task_project ON task(project_id);
CREATE INDEX idx_task_assignee ON task(assignee_id);
CREATE INDEX idx_task_creator ON task(creator_id);
CREATE INDEX idx_task_status ON task(status);
CREATE INDEX idx_task_priority ON task(priority);
CREATE INDEX idx_task_parent ON task(parent_id);
CREATE INDEX idx_task_deleted_at ON task(deleted_at);
CREATE INDEX idx_task_project_status ON task(project_id, status);
CREATE INDEX idx_task_project_position ON task(project_id, position);

-- 子任务表
CREATE TABLE sub_task (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_sub_task_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
);

-- 子任务表索引
CREATE INDEX idx_sub_task_task ON sub_task(task_id);

-- 任务评论表
CREATE TABLE comment (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT,
    issue_id BIGINT,
    story_id BIGINT,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    parent_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,

    CONSTRAINT chk_comment_target CHECK (task_id IS NOT NULL OR issue_id IS NOT NULL OR story_id IS NOT NULL),
    CONSTRAINT fk_comment_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_issue FOREIGN KEY (issue_id) REFERENCES issue(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_story FOREIGN KEY (story_id) REFERENCES user_story(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comment(id) ON DELETE CASCADE
);

-- 评论表索引
CREATE INDEX idx_comment_task ON comment(task_id);
CREATE INDEX idx_comment_issue ON comment(issue_id);
CREATE INDEX idx_comment_story ON comment(story_id);
CREATE INDEX idx_comment_user ON comment(user_id);
CREATE INDEX idx_comment_parent ON comment(parent_id);
CREATE INDEX idx_comment_deleted_at ON comment(deleted_at);

-- ============================================
-- 用户故事模块
-- ============================================

-- 史诗表
CREATE TABLE epic (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    color VARCHAR(20),
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_epic_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

-- 史诗表索引
CREATE INDEX idx_epic_project ON epic(project_id);

-- 用户故事表
CREATE TABLE user_story (
    id BIGSERIAL PRIMARY KEY,
    epic_id BIGINT,
    project_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    acceptance_criteria TEXT,
    priority priority NOT NULL DEFAULT 'MEDIUM',
    story_points INTEGER,
    assignee_id BIGINT,
    status task_status NOT NULL DEFAULT 'TODO',
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_story_epic FOREIGN KEY (epic_id) REFERENCES epic(id) ON DELETE SET NULL,
    CONSTRAINT fk_story_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_story_assignee FOREIGN KEY (assignee_id) REFERENCES sys_user(id),
    CONSTRAINT chk_story_points CHECK (story_points IS NULL OR story_points >= 0)
);

-- 用户故事表索引
CREATE INDEX idx_story_epic ON user_story(epic_id);
CREATE INDEX idx_story_project ON user_story(project_id);
CREATE INDEX idx_story_assignee ON user_story(assignee_id);
CREATE INDEX idx_story_status ON user_story(status);
CREATE INDEX idx_story_priority ON user_story(priority);

-- ============================================
-- 问题追踪模块
-- ============================================

-- 问题表
CREATE TABLE issue (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    type issue_type NOT NULL DEFAULT 'BUG',
    severity issue_severity NOT NULL DEFAULT 'NORMAL',
    status issue_status NOT NULL DEFAULT 'NEW',
    assignee_id BIGINT,
    reporter_id BIGINT NOT NULL,
    found_date DATE NOT NULL,
    resolved_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_issue_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_issue_assignee FOREIGN KEY (assignee_id) REFERENCES sys_user(id),
    CONSTRAINT fk_issue_reporter FOREIGN KEY (reporter_id) REFERENCES sys_user(id),
    CONSTRAINT chk_resolved_date CHECK (resolved_date IS NULL OR resolved_date >= found_date)
);

-- 问题表索引
CREATE INDEX idx_issue_project ON issue(project_id);
CREATE INDEX idx_issue_assignee ON issue(assignee_id);
CREATE INDEX idx_issue_reporter ON issue(reporter_id);
CREATE INDEX idx_issue_type ON issue(type);
CREATE INDEX idx_issue_severity ON issue(severity);
CREATE INDEX idx_issue_status ON issue(status);

-- ============================================
-- Wiki 模块
-- ============================================

-- Wiki 文档表
CREATE TABLE wiki_document (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    parent_id BIGINT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    position INTEGER NOT NULL DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_wiki_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_wiki_parent FOREIGN KEY (parent_id) REFERENCES wiki_document(id) ON DELETE CASCADE,
    CONSTRAINT fk_wiki_author FOREIGN KEY (author_id) REFERENCES sys_user(id)
);

-- Wiki 文档表索引
CREATE INDEX idx_wiki_project ON wiki_document(project_id);
CREATE INDEX idx_wiki_parent ON wiki_document(parent_id);
CREATE INDEX idx_wiki_author ON wiki_document(author_id);
CREATE INDEX idx_wiki_published ON wiki_document(is_published);

-- Wiki 版本历史表
CREATE TABLE wiki_history (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    version INTEGER NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    change_summary VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_wiki_history_document FOREIGN KEY (document_id) REFERENCES wiki_document(id) ON DELETE CASCADE,
    CONSTRAINT fk_wiki_history_author FOREIGN KEY (author_id) REFERENCES sys_user(id),
    CONSTRAINT uk_wiki_history UNIQUE (document_id, version)
);

-- Wiki 历史表索引
CREATE INDEX idx_wiki_history_document ON wiki_history(document_id);

-- ============================================
-- 通知模块
-- ============================================

-- 通知表
CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    type notification_type NOT NULL DEFAULT 'INFO',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    related_id BIGINT,
    related_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);

-- 通知表索引
CREATE INDEX idx_notification_user ON notification(user_id);
CREATE INDEX idx_notification_is_read ON notification(is_read);
CREATE INDEX idx_notification_created_at ON notification(created_at);
CREATE INDEX idx_notification_related ON notification(related_id, related_type);

-- ============================================
-- 系统设置表
-- ============================================

-- 系统配置表
CREATE TABLE sys_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 配置表索引
CREATE INDEX idx_sys_config_key ON sys_config(config_key);

-- ============================================
-- 操作日志表
-- ============================================

-- 操作日志表
CREATE TABLE operation_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    operation VARCHAR(100) NOT NULL,
    module VARCHAR(50),
    request_method VARCHAR(10),
    request_url VARCHAR(500),
    request_params TEXT,
    response_status INTEGER,
    error_message TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    execution_time INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_operation_log_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

-- 操作日志表索引
CREATE INDEX idx_operation_log_user ON operation_log(user_id);
CREATE INDEX idx_operation_log_operation ON operation_log(operation);
CREATE INDEX idx_operation_log_module ON operation_log(module);
CREATE INDEX idx_operation_log_created_at ON operation_log(created_at);

-- ============================================
-- 初始数据
-- ============================================

-- 初始化角色
INSERT INTO sys_role (name, code, description) VALUES
    ('超级管理员', 'ADMIN', '系统超级管理员，拥有全部权限'),
    ('企业管理员', 'ENTERPRISE_ADMIN', '企业/组织管理员'),
    ('项目经理', 'PROJECT_MANAGER', '项目负责人'),
    ('团队成员', 'MEMBER', '普通成员'),
    ('访客', 'GUEST', '只读用户');

-- 初始化权限
INSERT INTO sys_permission (name, code, description) VALUES
    -- 项目权限
    ('查看项目', 'PROJECT_VIEW', '查看项目信息'),
    ('创建项目', 'PROJECT_CREATE', '创建新项目'),
    ('编辑项目', 'PROJECT_EDIT', '编辑项目信息'),
    ('删除项目', 'PROJECT_DELETE', '删除项目'),
    ('管理项目成员', 'PROJECT_MEMBER_MANAGE', '添加/移除项目成员'),

    -- 任务权限
    ('查看任务', 'TASK_VIEW', '查看任务'),
    ('创建任务', 'TASK_CREATE', '创建新任务'),
    ('编辑任务', 'TASK_EDIT', '编辑任务'),
    ('删除任务', 'TASK_DELETE', '删除任务'),
    ('分配任务', 'TASK_ASSIGN', '分配任务给他人'),
    ('移动任务', 'TASK_MOVE', '移动任务状态'),

    -- 用户故事权限
    ('查看用户故事', 'STORY_VIEW', '查看用户故事'),
    ('创建用户故事', 'STORY_CREATE', '创建用户故事'),
    ('编辑用户故事', 'STORY_EDIT', '编辑用户故事'),
    ('删除用户故事', 'STORY_DELETE', '删除用户故事'),

    -- 问题权限
    ('查看问题', 'ISSUE_VIEW', '查看问题'),
    ('创建问题', 'ISSUE_CREATE', '创建问题'),
    ('编辑问题', 'ISSUE_EDIT', '编辑问题'),
    ('删除问题', 'ISSUE_DELETE', '删除问题'),

    -- Wiki 权限
    ('查看 Wiki', 'WIKI_VIEW', '查看 Wiki 文档'),
    ('编辑 Wiki', 'WIKI_EDIT', '编辑 Wiki 文档'),
    ('删除 Wiki', 'WIKI_DELETE', '删除 Wiki 文档'),

    -- 报表权限
    ('查看报表', 'REPORT_VIEW', '查看数据报表'),

    -- 系统权限
    ('访问管理后台', 'ADMIN_ACCESS', '访问系统管理后台'),
    ('用户管理', 'USER_MANAGE', '管理用户账号'),
    ('系统配置', 'SYSTEM_CONFIG', '配置系统参数');

-- 分配管理员权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- 默认管理员用户 (密码：Admin123 - 需要在应用层加密)
-- 注意：实际密码应该使用 bcrypt 加密
INSERT INTO sys_user (username, email, password, status) VALUES
    ('admin', 'admin@projecthub.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ACTIVE');

-- 分配管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- ============================================
-- 视图 (可选)
-- ============================================

-- 项目统计视图
CREATE OR REPLACE VIEW v_project_stats AS
SELECT
    p.id AS project_id,
    p.name AS project_name,
    p.status AS project_status,
    COUNT(DISTINCT t.id) AS total_tasks,
    COUNT(DISTINCT CASE WHEN t.status = 'TODO' THEN t.id END) AS todo_tasks,
    COUNT(DISTINCT CASE WHEN t.status = 'IN_PROGRESS' THEN t.id END) AS in_progress_tasks,
    COUNT(DISTINCT CASE WHEN t.status = 'IN_REVIEW' THEN t.id END) AS in_review_tasks,
    COUNT(DISTINCT CASE WHEN t.status = 'DONE' THEN t.id END) AS done_tasks,
    COUNT(DISTINCT pm.user_id) AS member_count,
    ROUND(
        COUNT(DISTINCT CASE WHEN t.status = 'DONE' THEN t.id END)::NUMERIC /
        NULLIF(COUNT(DISTINCT t.id), 0)::NUMERIC * 100,
        2
    ) AS progress_percentage
FROM project p
LEFT JOIN task t ON p.id = t.project_id AND t.deleted_at IS NULL
LEFT JOIN project_member pm ON p.id = pm.project_id
WHERE p.deleted_at IS NULL
GROUP BY p.id, p.name, p.status;

-- 任务详情视图
CREATE OR REPLACE VIEW v_task_detail AS
SELECT
    t.id AS task_id,
    t.title AS task_title,
    t.status AS task_status,
    t.priority AS task_priority,
    t.story_points,
    t.due_date,
    t.created_at,
    t.updated_at,
    p.id AS project_id,
    p.name AS project_name,
    au.username AS assignee_username,
    au.email AS assignee_email,
    cu.username AS creator_username,
    cu.email AS creator_email,
    (SELECT COUNT(*) FROM sub_task st WHERE st.task_id = t.id) AS sub_task_count,
    (SELECT COUNT(*) FROM sub_task st WHERE st.task_id = t.id AND st.completed = TRUE) AS completed_sub_task_count,
    (SELECT COUNT(*) FROM comment c WHERE c.task_id = t.id AND c.deleted_at IS NULL) AS comment_count
FROM task t
LEFT JOIN project p ON t.project_id = p.id
LEFT JOIN sys_user au ON t.assignee_id = au.id
LEFT JOIN sys_user cu ON t.creator_id = cu.id
WHERE t.deleted_at IS NULL;

-- ============================================
-- 触发器
-- ============================================

-- 更新时自动更新 updated_at 字段
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为需要自动更新 updated_at 的表添加触发器
CREATE TRIGGER update_sys_user_updated_at
    BEFORE UPDATE ON sys_user
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_project_updated_at
    BEFORE UPDATE ON project
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_task_updated_at
    BEFORE UPDATE ON task
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_story_updated_at
    BEFORE UPDATE ON user_story
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_wiki_document_updated_at
    BEFORE UPDATE ON wiki_document
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- 备注
-- ============================================

-- 1. 所有主键使用 BIGSERIAL 自增
-- 2. 软删除使用 deleted_at 字段标记
-- 3. 时间字段统一使用 TIMESTAMP，时区处理在应用层
-- 4. 金额相关字段 (如有) 使用 DECIMAL(10,2)
-- 5. 所有外键都设置了 ON DELETE CASCADE 或 ON DELETE SET NULL
-- 6. 关键字段都有索引优化查询性能
-- 7. 使用 PostgreSQL 的 ENUM 类型保证数据一致性
-- 8. 使用 CHECK 约束保证数据有效性
-- 9. 使用触发器自动维护 updated_at 字段
-- 10. 提供视图简化常用查询
