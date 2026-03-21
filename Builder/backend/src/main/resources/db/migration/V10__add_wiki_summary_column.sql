-- 添加 summary 字段到 wiki_document 表
-- 用于存储文档摘要信息
-- Flyway 版本：V10

ALTER TABLE wiki_document
ADD COLUMN IF NOT EXISTS summary VARCHAR(500);

COMMENT ON COLUMN wiki_document.summary IS '文档摘要';
