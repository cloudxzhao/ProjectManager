-- 添加 task 表的 epic_id 字段
ALTER TABLE task ADD COLUMN epic_id BIGINT;

-- 添加外键约束
ALTER TABLE task
    ADD CONSTRAINT fk_task_epic
    FOREIGN KEY (epic_id)
    REFERENCES epic(id)
    ON DELETE SET NULL;

-- 添加索引
CREATE INDEX idx_task_epic_id ON task(epic_id);