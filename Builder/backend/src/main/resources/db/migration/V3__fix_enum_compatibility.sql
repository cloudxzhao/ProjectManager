-- V3: 修复枚举类型兼容性问题
-- 将 PostgreSQL 枚举类型改为 VARCHAR，以兼容 Hibernate JPA
-- Flyway V3

-- ============================================
-- 第一步：删除依赖这些字段的视图
-- ============================================
DROP VIEW IF EXISTS v_project_stats;
DROP VIEW IF EXISTS v_task_detail;

-- ============================================
-- 第二步：修改列类型从枚举到 VARCHAR
-- ============================================

-- 1. project 表的 status 字段
ALTER TABLE project
    ALTER COLUMN status TYPE VARCHAR(20) USING status::TEXT;
ALTER TABLE project ALTER COLUMN status SET DEFAULT 'ACTIVE';

-- 2. sys_user 表的 status 字段
ALTER TABLE sys_user
    ALTER COLUMN status TYPE VARCHAR(20) USING status::TEXT;
ALTER TABLE sys_user ALTER COLUMN status SET DEFAULT 'ACTIVE';

-- 3. task 表的 status 和 priority 字段
ALTER TABLE task
    ALTER COLUMN status TYPE VARCHAR(20) USING status::TEXT,
    ALTER COLUMN priority TYPE VARCHAR(20) USING priority::TEXT;
ALTER TABLE task ALTER COLUMN status SET DEFAULT 'TODO';
ALTER TABLE task ALTER COLUMN priority SET DEFAULT 'MEDIUM';

-- 4. project_member 表的 role 字段
ALTER TABLE project_member
    ALTER COLUMN role TYPE VARCHAR(20) USING role::TEXT;
ALTER TABLE project_member ALTER COLUMN role SET DEFAULT 'MEMBER';

-- 5. user_story 表的 priority 和 status 字段
ALTER TABLE user_story
    ALTER COLUMN priority TYPE VARCHAR(20) USING priority::TEXT,
    ALTER COLUMN status TYPE VARCHAR(20) USING status::TEXT;
ALTER TABLE user_story ALTER COLUMN priority SET DEFAULT 'MEDIUM';
ALTER TABLE user_story ALTER COLUMN status SET DEFAULT 'TODO';

-- 6. issue 表的 type, severity, status 字段
ALTER TABLE issue
    ALTER COLUMN type TYPE VARCHAR(20) USING type::TEXT,
    ALTER COLUMN severity TYPE VARCHAR(20) USING severity::TEXT,
    ALTER COLUMN status TYPE VARCHAR(20) USING status::TEXT;
ALTER TABLE issue ALTER COLUMN type SET DEFAULT 'BUG';
ALTER TABLE issue ALTER COLUMN severity SET DEFAULT 'NORMAL';
ALTER TABLE issue ALTER COLUMN status SET DEFAULT 'NEW';

-- 7. notification 表的 type 字段
ALTER TABLE notification
    ALTER COLUMN type TYPE VARCHAR(20) USING type::TEXT;
ALTER TABLE notification ALTER COLUMN type SET DEFAULT 'INFO';

-- ============================================
-- 第三步：重建视图
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
    (SELECT COUNT(*) FROM task_comment c WHERE c.task_id = t.id AND c.deleted_at IS NULL) AS comment_count
FROM task t
LEFT JOIN project p ON t.project_id = p.id
LEFT JOIN sys_user au ON t.assignee_id = au.id
LEFT JOIN sys_user cu ON t.creator_id = cu.id
WHERE t.deleted_at IS NULL;
