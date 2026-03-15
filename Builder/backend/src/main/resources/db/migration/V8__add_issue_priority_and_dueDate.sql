-- 添加 issue 表的 priority 字段 (如果不存在)
ALTER TABLE issue ADD COLUMN IF NOT EXISTS priority VARCHAR(20);

-- 添加 issue 表的 due_date 字段 (如果不存在)
ALTER TABLE issue ADD COLUMN IF NOT EXISTS due_date DATE;

-- 添加索引 (如果不存在)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_issue_priority') THEN
        CREATE INDEX idx_issue_priority ON issue(priority);
    END IF;
END $$;
