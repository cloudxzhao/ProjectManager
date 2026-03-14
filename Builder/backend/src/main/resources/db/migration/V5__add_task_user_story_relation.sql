-- ProjectHub 数据库迁移 - V5
-- 功能：添加任务与用户故事的关联关系
-- 数据库：PostgreSQL 15+
-- Flyway 版本：V5

-- ============================================
-- 任务表添加用户故事 ID 字段
-- ============================================

-- 添加 user_story_id 字段
ALTER TABLE task
ADD COLUMN user_story_id BIGINT,
ADD CONSTRAINT fk_task_user_story
    FOREIGN KEY (user_story_id)
    REFERENCES user_story(id)
    ON DELETE SET NULL;

-- 添加索引以提高查询性能
CREATE INDEX idx_task_user_story ON task(user_story_id);

-- 添加联合索引，支持按项目和用户故事筛选任务
CREATE INDEX idx_task_project_user_story ON task(project_id, user_story_id);

-- ============================================
-- 更新任务详情视图，包含用户故事信息
-- ============================================

-- 删除旧视图
DROP VIEW IF EXISTS v_task_detail;

-- 重新创建视图，保持原有列顺序
CREATE VIEW v_task_detail AS
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
