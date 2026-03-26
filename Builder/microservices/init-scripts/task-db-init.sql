-- Task Service 数据库初始化脚本
-- 数据库: task_db

-- 创建任务表
CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    task_key VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    project_id BIGINT NOT NULL,
    assignee_id BIGINT,
    creator_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'TODO',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    story_points INTEGER,
    due_date DATE,
    column_id BIGINT,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 创建评论表
CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id),
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    parent_id BIGINT REFERENCES comments(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_tasks_project ON tasks(project_id);
CREATE INDEX IF NOT EXISTS idx_tasks_assignee ON tasks(assignee_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_task_key ON tasks(task_key);
CREATE INDEX IF NOT EXISTS idx_comments_task ON comments(task_id);

COMMENT ON TABLE tasks IS '任务表';
COMMENT ON TABLE comments IS '评论表';
COMMENT ON COLUMN tasks.id IS '任务ID';
COMMENT ON COLUMN tasks.task_key IS '任务编号(如TASK-001)';
COMMENT ON COLUMN tasks.status IS '状态: TODO, IN_PROGRESS, IN_REVIEW, DONE';
COMMENT ON COLUMN tasks.priority IS '优先级: LOW, MEDIUM, HIGH, URGENT';
COMMENT ON COLUMN comments.parent_id IS '父评论ID(用于回复)';