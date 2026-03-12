-- ProjectHub 数据库初始数据 - V2
-- Flyway 版本：V2

-- ============================================
-- 初始数据
-- ============================================

-- 初始化角色
INSERT INTO sys_role (name, code, description) VALUES
    ('超级管理员', 'ADMIN', '系统超级管理员，拥有全部权限'),
    ('企业管理员', 'ENTERPRISE_ADMIN', '企业/组织管理员'),
    ('项目经理', 'PROJECT_MANAGER', '项目负责人'),
    ('团队成员', 'MEMBER', '普通成员'),
    ('访客', 'GUEST', '只读用户');

-- 初始化权限
INSERT INTO sys_permission (name, code, description) VALUES
    -- 项目权限
    ('查看项目', 'PROJECT_VIEW', '查看项目信息'),
    ('创建项目', 'PROJECT_CREATE', '创建新项目'),
    ('编辑项目', 'PROJECT_EDIT', '编辑项目信息'),
    ('删除项目', 'PROJECT_DELETE', '删除项目'),
    ('管理项目成员', 'PROJECT_MEMBER_MANAGE', '添加/移除项目成员'),

    -- 任务权限
    ('查看任务', 'TASK_VIEW', '查看任务'),
    ('创建任务', 'TASK_CREATE', '创建新任务'),
    ('编辑任务', 'TASK_EDIT', '编辑任务'),
    ('删除任务', 'TASK_DELETE', '删除任务'),
    ('分配任务', 'TASK_ASSIGN', '分配任务给他人'),
    ('移动任务', 'TASK_MOVE', '移动任务状态'),

    -- 用户故事权限
    ('查看用户故事', 'STORY_VIEW', '查看用户故事'),
    ('创建用户故事', 'STORY_CREATE', '创建用户故事'),
    ('编辑用户故事', 'STORY_EDIT', '编辑用户故事'),
    ('删除用户故事', 'STORY_DELETE', '删除用户故事'),

    -- 问题权限
    ('查看问题', 'ISSUE_VIEW', '查看问题'),
    ('创建问题', 'ISSUE_CREATE', '创建问题'),
    ('编辑问题', 'ISSUE_EDIT', '编辑问题'),
    ('删除问题', 'ISSUE_DELETE', '删除问题'),

    -- Wiki 权限
    ('查看 Wiki', 'WIKI_VIEW', '查看 Wiki 文档'),
    ('编辑 Wiki', 'WIKI_EDIT', '编辑 Wiki 文档'),
    ('删除 Wiki', 'WIKI_DELETE', '删除 Wiki 文档'),

    -- 报表权限
    ('查看报表', 'REPORT_VIEW', '查看数据报表'),

    -- 系统权限
    ('访问管理后台', 'ADMIN_ACCESS', '访问系统管理后台'),
    ('用户管理', 'USER_MANAGE', '管理用户账号'),
    ('系统配置', 'SYSTEM_CONFIG', '配置系统参数');

-- 分配管理员权限 (角色 ID=1)
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- 默认管理员用户 (密码：Admin123 - bcrypt 加密, strength=10)
INSERT INTO sys_user (username, email, password, status) VALUES
    ('admin', 'admin@projecthub.com', '$2a$10$49QV.C712rCF9d6DhN7WSOLnoWxJh0vjFXzXbdEp8eT9PDTJw4Eoe', 'ACTIVE');

-- 分配管理员角色给用户 ID=1
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
