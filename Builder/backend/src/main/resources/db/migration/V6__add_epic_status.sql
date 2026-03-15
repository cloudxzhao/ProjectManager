-- ProjectHub 数据库迁移 - V6
-- 为 epic 表添加 status 字段

-- ============================================
-- 枚举类型
-- ============================================

-- 史诗状态
CREATE TYPE epic_status AS ENUM ('ACTIVE', 'INACTIVE');

-- ============================================
-- 修改 epic 表
-- ============================================

-- 添加 status 字段，默认值为 ACTIVE
ALTER TABLE epic ADD COLUMN status epic_status NOT NULL DEFAULT 'ACTIVE';

-- 添加注释
COMMENT ON COLUMN epic.status IS '史诗状态：ACTIVE/INACTIVE';

-- 添加索引
CREATE INDEX idx_epic_status ON epic(status);
