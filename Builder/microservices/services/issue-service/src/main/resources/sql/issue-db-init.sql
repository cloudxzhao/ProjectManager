-- Issue Service Database Initialization Script
-- Database: issue_db

CREATE DATABASE IF NOT EXISTS issue_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE issue_db;

-- Issues table
CREATE TABLE IF NOT EXISTS issues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    issue_key VARCHAR(50) NOT NULL COMMENT '问题Key',
    title VARCHAR(255) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    steps_to_reproduce TEXT COMMENT '重现步骤',
    environment VARCHAR(500) COMMENT '环境信息',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    epic_id BIGINT COMMENT '史诗ID',
    task_id BIGINT COMMENT '任务ID',
    assignee_id BIGINT COMMENT '负责人ID',
    reporter_id VARCHAR(50) COMMENT '报告人ID',
    status VARCHAR(50) DEFAULT 'OPEN' COMMENT '状态: OPEN, IN_PROGRESS, IN_REVIEW, RESOLVED, CLOSED, REOPENED',
    priority VARCHAR(50) DEFAULT 'MEDIUM' COMMENT '优先级: LOW, MEDIUM, HIGH, CRITICAL',
    severity VARCHAR(50) DEFAULT 'NORMAL' COMMENT '严重程度: TRIVIAL, MINOR, NORMAL, MAJOR, CRITICAL',
    type VARCHAR(50) DEFAULT 'BUG' COMMENT '类型: BUG, TASK, IMPROVEMENT, STORY, SUB_TASK',
    story_points INT COMMENT '故事点数',
    parent_issue_id BIGINT COMMENT '父问题ID',
    resolved_at DATETIME COMMENT '解决时间',
    due_date DATETIME COMMENT '截止日期',
    creator_id BIGINT COMMENT '创建人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    UNIQUE KEY uk_issue_key (issue_key),
    INDEX idx_project_id (project_id),
    INDEX idx_task_id (task_id),
    INDEX idx_assignee_id (assignee_id),
    INDEX idx_status (status),
    INDEX idx_type (type),
    INDEX idx_parent_issue_id (parent_issue_id),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题表';

-- Insert sample data
INSERT INTO issues (issue_key, title, description, steps_to_reproduce, environment, project_id, status, priority, severity, type, assignee_id, reporter_id, creator_id) VALUES
('ISSUE-1-AAA11111', '用户登录页面无法显示', '登录页面加载失败，浏览器控制台报错', '1. 打开浏览器\n2. 访问登录页面\n3. 页面空白', 'Chrome 120 / Windows 11', 1, 'OPEN', 'HIGH', 'MAJOR', 'BUG', 1, 'system', 1),
('ISSUE-1-BBB22222', '项目列表加载缓慢', '项目列表页面加载超过10秒', '1. 登录系统\n2. 进入项目列表页面\n3. 等待加载', 'Chrome 120 / Windows 11', 1, 'IN_PROGRESS', 'MEDIUM', 'NORMAL', 'TASK', 1, 'system', 1),
('ISSUE-1-CCC33333', '添加任务按钮无响应', '点击添加任务按钮没有反应', '1. 进入项目详情页\n2. 点击添加任务按钮\n3. 无响应', 'Chrome 120 / Windows 11', 1, 'RESOLVED', 'HIGH', 'CRITICAL', 'BUG', 1, 'system', 1);