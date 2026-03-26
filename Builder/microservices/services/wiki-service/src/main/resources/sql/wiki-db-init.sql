-- Wiki Service Database Initialization Script
-- Database: wiki_db

CREATE DATABASE IF NOT EXISTS wiki_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wiki_db;

-- Wiki Spaces table
CREATE TABLE IF NOT EXISTS wiki_spaces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT '空间名称',
    description TEXT COMMENT '描述',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    icon VARCHAR(50) COMMENT '图标',
    owner_id VARCHAR(50) COMMENT '所有者ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_project_id (project_id),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识空间表';

-- Wiki Pages table
CREATE TABLE IF NOT EXISTS wiki_pages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    space_id BIGINT NOT NULL COMMENT '空间ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父页面ID',
    title VARCHAR(255) NOT NULL COMMENT '页面标题',
    content LONGTEXT COMMENT '页面内容（Markdown格式）',
    slug VARCHAR(255) COMMENT 'URL别名',
    order_num INT DEFAULT 0 COMMENT '排序号',
    level INT DEFAULT 1 COMMENT '层级',
    creator_id BIGINT COMMENT '创建人ID',
    last_editor_id BIGINT COMMENT '最后编辑人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_space_id (space_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_slug (slug),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识页面表';

-- Insert sample data
INSERT INTO wiki_spaces (name, description, project_id, icon, owner_id) VALUES
('项目文档', '项目相关文档和资料', 1, 'folder', '1'),
('技术文档', '技术架构和开发文档', 1, 'book', '1');

INSERT INTO wiki_pages (space_id, parent_id, title, content, slug, order_num, level, creator_id) VALUES
(1, 0, '项目概述', '# 项目概述\n\n这是项目的概述文档。', 'overview', 1, 1, 1),
(1, 1, '功能清单', '# 功能清单\n\n## 功能1\n\n## 功能2', 'features', 1, 2, 1),
(1, 0, '开发指南', '# 开发指南\n\n## 环境要求\n\n## 安装步骤', 'guide', 2, 1, 1);