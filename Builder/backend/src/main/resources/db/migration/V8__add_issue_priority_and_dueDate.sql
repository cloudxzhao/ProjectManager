-- 添加 issue 表的 priority 字段
ALTER TABLE issue ADD COLUMN priority VARCHAR(20);

-- 添加 issue 表的 due_date 字段
ALTER TABLE issue ADD COLUMN due_date DATE;

-- 添加索引
CREATE INDEX idx_issue_priority ON issue(priority);
