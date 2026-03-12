# ProjectHub 数据库设计文档

## 概述

| 项目 | 说明 |
|------|------|
| 数据库 | PostgreSQL 16 |
| Flyway 版本 | V2 |
| 字符编码 | UTF8 |
| 时区 | UTC |

## 数据库表概览

### 系统模块（sys_）

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| `sys_user` | 用户表 | id, username, email, password, status, deleted_at |
| `sys_role` | 角色表 | id, name, code, description |
| `sys_user_role` | 用户角色关联表 | user_id, role_id |
| `sys_permission` | 权限表 | id, name, code, description |
| `sys_role_permission` | 角色权限关联表 | role_id, permission_id |
| `sys_config` | 系统配置表 | config_key, config_value, description |

### 项目模块

| 表名 | 说明 | 主要字段 |
|------|------|------|
| `project` | 项目表 | id, name, description, start_date, end_date, owner_id, status, deleted_at |
| `project_member` | 项目成员表 | id, project_id, user_id, role, joined_at |

### 任务模块

| 表名 | 说明 | 主要字段 |
|------|------|------|
| `task` | 任务表 | id, project_id, title, description, status, priority, assignee_id, parent_id, deleted_at |
| `sub_task` | 子任务表 | id, task_id, title, completed, position |
| `task_comment` | 任务评论表 | id, task_id, user_id, content, parent_id, deleted_at |

### 用户故事模块

| 表名 | 说明 | 主要字段 |
|------|------|------|
| `epic` | 史诗表 | id, project_id, title, description, color, deleted_at |
| `user_story` | 用户故事表 | id, epic_id, project_id, title, acceptance_criteria, priority, story_points, deleted_at |

### 问题追踪模块

| 表名 | 说明 | 主要字段 |
|------|------|------|
| `issue` | 问题表 | id, project_id, title, description, type, severity, status, reporter_id, deleted_at |

### Wiki 模块

| 表名 | 说明 | 主要字段 |
|------|------|------|
| `wiki_document` | Wiki 文档表 | id, project_id, parent_id, title, content, author_id, version, is_published, deleted_at |
| `wiki_history` | Wiki 历史表 | id, document_id, user_id, version, content, created_at |

### 通知模块

| 表名 | 说明 | 主要字段 |
|------|------|------|
| `notification` | 通知表 | id, user_id, title, content, type, is_read, related_id, deleted_at |

### 日志模块

| 表名 | 说明 | 主要字段 |
|------|------|------|
| `operation_log` | 操作日志表 | id, user_id, username, module, operation, method, params, result, ip_address, duration, created_at |

## 枚举类型

### 用户状态 (user_status)
- `ACTIVE` - 活跃
- `INACTIVE` - 非活跃
- `BANNED` - 已禁用

### 项目状态 (project_status)
- `ACTIVE` - 进行中
- `COMPLETED` - 已完成
- `ARCHIVED` - 已归档

### 任务状态 (task_status)
- `TODO` - 待办
- `IN_PROGRESS` - 进行中
- `IN_REVIEW` - 审查中
- `DONE` - 已完成

### 优先级 (priority)
- `LOW` - 低
- `MEDIUM` - 中
- `HIGH` - 高
- `URGENT` - 紧急

### 问题类型 (issue_type)
- `BUG` - Bug
- `ISSUE` - 问题
- `IMPROVEMENT` - 改进
- `TECH_DEBT` - 技术债务

### 问题严重程度 (issue_severity)
- `TRIVIAL` - 琐碎
- `MINOR` - 轻微
- `NORMAL` - 普通
- `MAJOR` - 严重
- `CRITICAL` - 危急

### 问题状态 (issue_status)
- `NEW` - 新建
- `CONFIRMED` - 已确认
- `IN_PROGRESS` - 处理中
- `RESOLVED` - 已解决
- `CLOSED` - 已关闭
- `REOPENED` - 重新打开

### 项目成员角色 (project_member_role)
- `OWNER` - 所有者
- `MANAGER` - 管理员
- `MEMBER` - 成员

### 通知类型 (notification_type)
- `INFO` - 信息
- `WARNING` - 警告
- `ERROR` - 错误
- `TASK` - 任务
- `PROJECT` - 项目

## 表关系图（简化）

```
sys_user (1) ──────< project (N)
    │                    │
    │                    └─────< project_member (N) ─────> sys_user (1)
    │                           │
    │                    └─────< task (N)
    │                           │
    │                           └─────< sub_task (N)
    │                           │
    │                           └─────< task_comment (N)
    │
    └─────< sys_user_role (N) ─────> sys_role (1)
                                        │
                                        └─────< sys_role_permission (N) ─────> sys_permission (1)

project (1) ──────< epic (N) ──────< user_story (N)
         │
         └─────< issue (N)
         │
         └─────< wiki_document (N) ──────< wiki_history (N)
```

## 软删除设计

以下表使用 `deleted_at` 字段实现软删除：

- `sys_user`
- `project`
- `task`
- `task_comment`
- `epic`
- `user_story`
- `issue`
- `wiki_document`
- `notification`

软删除的 SQL 更新语句由 Hibernate 自动处理：
```sql
UPDATE {table} SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?
```

查询时自动添加 `deleted_at IS NULL` 条件。

## 索引设计

### 通用索引规则

1. 所有外键字段都有索引
2. 所有状态字段都有索引
3. 所有时间戳字段都有索引
4. 常用组合查询有复合索引

### 重要复合索引

| 表 | 索引字段 | 用途 |
|----|---------|------|
| `task` | (project_id, status) | 项目任务状态筛选 |
| `task` | (project_id, position) | 项目任务排序 |
| `project_member` | (project_id, user_id) | 唯一性约束 + 快速查询 |

## 视图

### v_project_stats

项目统计视图，提供项目任务完成情况统计。

### v_task_detail

任务详情视图，关联任务、项目、用户信息。

## 触发器

### update_updated_at_column

自动更新 `updated_at` 字段的触发器，应用于：
- `sys_user`
- `project`
- `task`
- `user_story`
- `wiki_document`

## Flyway 迁移历史

| 版本 | 描述 | 执行时间 |
|------|------|----------|
| V1 | init schema | 初始化所有表结构 |
| V2 | initial data | 插入初始数据 |
