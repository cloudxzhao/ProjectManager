-- Wiki 模块增强迁移脚本
-- 添加 HTML 渲染、层级结构、版本管理等功能

-- 1. 添加新字段到 wiki_document 表
-- 添加 HTML 内容字段
ALTER TABLE wiki_document ADD COLUMN IF NOT EXISTS content_html TEXT;

-- 添加父路径字段（用于层级查询和排序）
ALTER TABLE wiki_document ADD COLUMN IF NOT EXISTS parent_path VARCHAR(1000);

-- 添加层级字段
ALTER TABLE wiki_document ADD COLUMN IF NOT EXISTS level INTEGER DEFAULT 0;

-- 添加浏览次数字段
ALTER TABLE wiki_document ADD COLUMN IF NOT EXISTS view_count INTEGER DEFAULT 0;

-- 将 position 重命名为 order_num
ALTER TABLE wiki_document ADD COLUMN IF NOT EXISTS order_num INTEGER DEFAULT 0;
UPDATE wiki_document SET order_num = position WHERE order_num IS NULL OR order_num = 0;
ALTER TABLE wiki_document DROP COLUMN IF EXISTS position;

-- 添加状态字段替代 is_published
ALTER TABLE wiki_document ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PUBLISHED';
-- 将 is_published 转换为 status
UPDATE wiki_document SET status = 'PUBLISHED' WHERE is_published = true;
UPDATE wiki_document SET status = 'DRAFT' WHERE is_published = false;
ALTER TABLE wiki_document DROP COLUMN IF EXISTS is_published;

-- 2. 添加新字段到 wiki_history 表
ALTER TABLE wiki_history ADD COLUMN IF NOT EXISTS content_html TEXT;
ALTER TABLE wiki_history ADD COLUMN IF NOT EXISTS change_log VARCHAR(500);
ALTER TABLE wiki_history ADD COLUMN IF NOT EXISTS change_type VARCHAR(20) DEFAULT 'UPDATE';

-- 3. 创建全文搜索索引和触发器
-- 添加 tsvector 字段用于全文搜索
ALTER TABLE wiki_document ADD COLUMN IF NOT EXISTS search_vector tsvector;

-- 创建 GIN 索引用于全文搜索
CREATE INDEX IF NOT EXISTS idx_wiki_document_search_vector ON wiki_document USING gin(search_vector);

-- 创建更新 search_vector 的函数
CREATE OR REPLACE FUNCTION wiki_document_search_vector_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('simple', COALESCE(NEW.title, '')), 'A') ||
        setweight(to_tsvector('simple', COALESCE(NEW.content, '')), 'B');
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- 创建触发器
DROP TRIGGER IF EXISTS wiki_document_search_vector_trigger ON wiki_document;
CREATE TRIGGER wiki_document_search_vector_trigger
    BEFORE INSERT OR UPDATE OF title, content
    ON wiki_document
    FOR EACH ROW
    EXECUTE FUNCTION wiki_document_search_vector_update();

-- 为现有数据初始化 search_vector
UPDATE wiki_document SET search_vector =
    setweight(to_tsvector('simple', COALESCE(title, '')), 'A') ||
    setweight(to_tsvector('simple', COALESCE(content, '')), 'B')
WHERE search_vector IS NULL;

-- 4. 创建 wiki_attachment 表用于文档附件
CREATE TABLE IF NOT EXISTS wiki_attachment (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100),
    uploader_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_wiki_attachment_document FOREIGN KEY (document_id)
        REFERENCES wiki_document(id) ON DELETE CASCADE
);

-- 创建附件索引
CREATE INDEX IF NOT EXISTS idx_wiki_attachment_document_id ON wiki_attachment(document_id);
CREATE INDEX IF NOT EXISTS idx_wiki_attachment_uploader_id ON wiki_attachment(uploader_id);

-- 5. 创建索引优化查询性能
CREATE INDEX IF NOT EXISTS idx_wiki_document_project_parent ON wiki_document(project_id, parent_id);
CREATE INDEX IF NOT EXISTS idx_wiki_document_parent_path ON wiki_document(parent_path);
CREATE INDEX IF NOT EXISTS idx_wiki_document_level ON wiki_document(level);
CREATE INDEX IF NOT EXISTS idx_wiki_document_order_num ON wiki_document(order_num);
CREATE INDEX IF NOT EXISTS idx_wiki_document_status ON wiki_document(status);
CREATE INDEX IF NOT EXISTS idx_wiki_history_document_version ON wiki_history(document_id, version DESC);