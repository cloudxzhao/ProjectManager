-- ProjectHub 数据库迁移 - V9
-- Flyway 版本：V9
-- 说明：为非管理员角色分配权限

-- ============================================
-- 为企业管理员分配权限 (角色 ID=2)
-- ============================================
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission WHERE code IN (
    'PROJECT_CREATE', 'PROJECT_VIEW', 'PROJECT_EDIT', 'PROJECT_DELETE', 'PROJECT_MEMBER_MANAGE',
    'TASK_CREATE', 'TASK_EDIT', 'TASK_DELETE', 'TASK_MOVE', 'TASK_ASSIGN', 'TASK_VIEW', 'TASK_COMMENT',
    'STORY_CREATE', 'STORY_EDIT', 'STORY_DELETE', 'STORY_VIEW',
    'EPIC_CREATE', 'EPIC_EDIT', 'EPIC_DELETE', 'EPIC_VIEW',
    'ISSUE_CREATE', 'ISSUE_EDIT', 'ISSUE_DELETE', 'ISSUE_VIEW',
    'WIKI_CREATE', 'WIKI_EDIT', 'WIKI_DELETE', 'WIKI_VIEW',
    'REPORT_VIEW'
);

-- ============================================
-- 为项目经理分配权限 (角色 ID=3)
-- ============================================
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 3, id FROM sys_permission WHERE code IN (
    'PROJECT_CREATE', 'PROJECT_VIEW', 'PROJECT_EDIT', 'PROJECT_MEMBER_MANAGE',
    'TASK_CREATE', 'TASK_EDIT', 'TASK_MOVE', 'TASK_ASSIGN', 'TASK_VIEW', 'TASK_COMMENT',
    'STORY_CREATE', 'STORY_EDIT', 'STORY_VIEW',
    'EPIC_CREATE', 'EPIC_EDIT', 'EPIC_VIEW',
    'ISSUE_CREATE', 'ISSUE_EDIT', 'ISSUE_VIEW',
    'WIKI_CREATE', 'WIKI_EDIT', 'WIKI_VIEW',
    'REPORT_VIEW'
);

-- ============================================
-- 为团队成员分配权限 (角色 ID=4)
-- ============================================
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 4, id FROM sys_permission WHERE code IN (
    'PROJECT_CREATE', 'PROJECT_VIEW',
    'TASK_CREATE', 'TASK_EDIT', 'TASK_VIEW', 'TASK_COMMENT',
    'STORY_VIEW', 'EPIC_VIEW', 'ISSUE_VIEW', 'WIKI_VIEW',
    'REPORT_VIEW'
);

-- ============================================
-- 为访客只分配查看权限 (角色 ID=5)
-- ============================================
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 5, id FROM sys_permission WHERE code IN (
    'PROJECT_VIEW',
    'TASK_VIEW', 'TASK_COMMENT',
    'STORY_VIEW', 'EPIC_VIEW', 'ISSUE_VIEW', 'WIKI_VIEW',
    'REPORT_VIEW'
);
