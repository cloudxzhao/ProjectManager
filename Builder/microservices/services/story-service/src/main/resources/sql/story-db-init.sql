-- Story Service Database Initialization Script
-- Database: story_db

CREATE DATABASE IF NOT EXISTS story_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE story_db;

-- Epics table
CREATE TABLE IF NOT EXISTS epics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT '史诗名称',
    description TEXT COMMENT '描述',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    status VARCHAR(50) DEFAULT 'OPEN' COMMENT '状态: OPEN, IN_PROGRESS, COMPLETED, ARCHIVED',
    priority INT DEFAULT 0 COMMENT '优先级: 0-4 (LOW, MEDIUM, HIGH, CRITICAL)',
    start_date DATETIME COMMENT '开始日期',
    end_date DATETIME COMMENT '结束日期',
    creator_id BIGINT COMMENT '创建人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_project_id (project_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='史诗表';

-- User Stories table
CREATE TABLE IF NOT EXISTS user_stories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    story_key VARCHAR(50) NOT NULL COMMENT '故事Key',
    title VARCHAR(255) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    acceptance_criteria TEXT COMMENT '验收标准',
    epic_id BIGINT COMMENT '史诗ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    assignee_id BIGINT COMMENT '负责人ID',
    status VARCHAR(50) DEFAULT 'OPEN' COMMENT '状态: OPEN, IN_PROGRESS, IN_REVIEW, COMPLETED, ARCHIVED',
    priority VARCHAR(50) DEFAULT 'MEDIUM' COMMENT '优先级: LOW, MEDIUM, HIGH, CRITICAL',
    story_points INT COMMENT '故事点数',
    creator_id BIGINT COMMENT '创建人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    UNIQUE KEY uk_story_key (story_key),
    INDEX idx_project_id (project_id),
    INDEX idx_epic_id (epic_id),
    INDEX idx_assignee_id (assignee_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户故事表';

-- Insert sample data
INSERT INTO epics (name, description, project_id, status, priority, creator_id) VALUES
('用户认证模块', '实现用户登录、注册、权限管理等功能', 1, 'OPEN', 2, 1),
('项目管理模块', '实现项目的创建、编辑、删除等功能', 1, 'IN_PROGRESS', 3, 1);

INSERT INTO user_stories (story_key, title, description, acceptance_criteria, epic_id, project_id, status, priority, story_points, creator_id) VALUES
('STORY-1-AAA11111', '用户登录功能', '实现用户通过用户名密码登录系统', '1. 输入正确的用户名密码能够登录成功\n2. 输入错误的用户名密码提示登录失败\n3. 登录成功后跳转到首页', 1, 1, 'COMPLETED', 'HIGH', 5, 1),
('STORY-1-BBB22222', '用户注册功能', '实现新用户注册功能', '1. 输入邮箱和密码可以注册\n2. 邮箱已存在提示错误\n3. 注册成功跳转到登录页', 1, 1, 'IN_PROGRESS', 'HIGH', 3, 1),
('STORY-1-CCC33333', '项目创建功能', '实现创建新项目功能', '1. 填写项目名称和描述可以创建项目\n2. 创建成功显示在项目列表\n3. 项目名称不能为空', 2, 1, 'OPEN', 'HIGH', 5, 1),
('STORY-1-DDD44444', '项目成员管理', '添加和移除项目成员', '1. 项目创建者可以添加成员\n2. 可以移除项目成员\n3. 成员可以查看项目', 2, 1, 'OPEN', 'MEDIUM', 3, 1);