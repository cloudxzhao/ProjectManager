# 数据迁移方案

## 概述

本文档描述如何将单体数据库拆分到多个微服务数据库，并提供迁移脚本、校验脚本和回滚方案。

## 迁移目标

### 源数据库（单体）

```
projectdb (PostgreSQL)
├── users
├── projects
├── project_members
├── tasks
├── comments
├── epics
├── user_stories
├── issues
├── wiki_spaces
├── wiki_pages
├── notifications
└── ...
```

### 目标数据库（微服务）

```
auth_db         ├── users (部分字段)
                └── refresh_tokens

user_db         ├── users (完整字段)
                └── user_profiles

project_db      ├── projects
                └── project_members

task_db         ├── tasks
                └── comments

story_db        ├── epics
                └── user_stories

issue_db        └── issues

wiki_db         ├── wiki_spaces
                └── wiki_pages

notify_db       └── notifications
```

## 迁移策略

### 阶段性迁移

```
┌─────────────────────────────────────────────────────────────┐
│  Phase 1: 双写模式                                           │
│  - 旧数据库：读写                                             │
│  - 新数据库：只写（数据同步）                                  │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  Phase 2: 数据校验                                           │
│  - 比对旧数据库和新数据库的数据                                │
│  - 修复数据不一致问题                                         │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  Phase 3: 读切换                                             │
│  - 旧数据库：只写                                             │
│  - 新数据库：读 + 写                                          │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  Phase 4: 关闭双写                                           │
│  - 旧数据库：只读（备份）                                      │
│  - 新数据库：读 + 写                                          │
└─────────────────────────────────────────────────────────────┘
```

## 迁移脚本

### 1. 数据库初始化脚本

每个微服务数据库的初始化脚本已创建：

```
Builder/microservices/init-scripts/
├── auth-db-init.sql
├── user-db-init.sql
├── project-db-init.sql
├── task-db-init.sql
├── story-db-init.sql
├── issue-db-init.sql
├── wiki-db-init.sql
└── notify-db-init.sql
```

### 2. 数据迁移脚本（Python）

```python
#!/usr/bin/env python3
"""
数据库迁移脚本 - 将单体数据库数据拆分到微服务数据库

使用方法:
    python migrate_data.py --source postgresql://user:pass@localhost/old_db \
                          --target-auth postgresql://user:pass@localhost/auth_db \
                          --target-user postgresql://user:pass@localhost/user_db \
                          ...
"""

import argparse
import psycopg2
from psycopg2.extras import RealDictCursor
import logging
from datetime import datetime

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class DataMigrator:
    """数据迁移器"""

    def __init__(self, source_conn_str, target_conn_strs):
        """
        初始化迁移器

        Args:
            source_conn_str: 源数据库连接字符串
            target_conn_strs: 目标数据库连接字符串字典
        """
        self.source_conn_str = source_conn_str
        self.target_conn_strs = target_conn_strs
        self.source_conn = None
        self.target_conns = {}

    def connect(self):
        """建立数据库连接"""
        logger.info("连接到源数据库...")
        self.source_conn = psycopg2.connect(self.source_conn_str)

        for db_name, conn_str in self.target_conn_strs.items():
            logger.info(f"连接到目标数据库：{db_name}")
            self.target_conns[db_name] = psycopg2.connect(conn_str)

    def close(self):
        """关闭数据库连接"""
        if self.source_conn:
            self.source_conn.close()

        for conn in self.target_conns.values():
            conn.close()

    def migrate_users(self):
        """迁移用户数据"""
        logger.info("开始迁移用户数据...")

        with self.source_conn.cursor(cursor_factory=RealDictCursor) as source_cur:
            # 从源数据库读取用户数据
            source_cur.execute("""
                SELECT id, email, username, password_hash, role,
                       created_at, updated_at, deleted
                FROM users
                WHERE deleted = false
            """)
            users = source_cur.fetchall()

            logger.info(f"找到 {len(users)} 个用户")

            # 迁移到 auth_db（认证相关字段）
            logger.info("迁移用户数据到 auth_db...")
            with self.target_conns['auth'].cursor() as auth_cur:
                for user in users:
                    auth_cur.execute("""
                        INSERT INTO users (id, email, username, password_hash, role,
                                          created_at, updated_at)
                        VALUES (%s, %s, %s, %s, %s, %s, %s)
                        ON CONFLICT (id) DO UPDATE SET
                            email = EXCLUDED.email,
                            username = EXCLUDED.username,
                            role = EXCLUDED.role
                    """, (user['id'], user['email'], user['username'],
                          user['password_hash'], user['role'],
                          user['created_at'], user['updated_at']))

                self.target_conns['auth'].commit()

            # 迁移到 user_db（完整用户信息）
            logger.info("迁移用户数据到 user_db...")
            with self.target_conns['user'].cursor() as user_cur:
                for user in users:
                    user_cur.execute("""
                        INSERT INTO users (id, email, username, password_hash, role,
                                          created_at, updated_at)
                        VALUES (%s, %s, %s, %s, %s, %s, %s)
                        ON CONFLICT (id) DO UPDATE SET
                            email = EXCLUDED.email,
                            username = EXCLUDED.username,
                            role = EXCLUDED.role
                    """, (user['id'], user['email'], user['username'],
                          user['password_hash'], user['role'],
                          user['created_at'], user['updated_at']))

                self.target_conns['user'].commit()

        logger.info("用户数据迁移完成")

    def migrate_projects(self):
        """迁移项目数据"""
        logger.info("开始迁移项目数据...")

        with self.source_conn.cursor(cursor_factory=RealDictCursor) as source_cur:
            # 迁移项目数据
            source_cur.execute("""
                SELECT id, name, description, owner_id, status,
                       created_at, updated_at, deleted
                FROM projects
                WHERE deleted = false
            """)
            projects = source_cur.fetchall()

            logger.info(f"找到 {len(projects)} 个项目")

            with self.target_conns['project'].cursor() as project_cur:
                for project in projects:
                    project_cur.execute("""
                        INSERT INTO projects (id, name, description, owner_id, status,
                                             created_at, updated_at)
                        VALUES (%s, %s, %s, %s, %s, %s, %s)
                        ON CONFLICT (id) DO UPDATE SET
                            name = EXCLUDED.name,
                            description = EXCLUDED.description,
                            status = EXCLUDED.status
                    """, (project['id'], project['name'], project['description'],
                          project['owner_id'], project['status'],
                          project['created_at'], project['updated_at']))

                self.target_conns['project'].commit()

            # 迁移项目成员数据
            source_cur.execute("""
                SELECT id, project_id, user_id, role, joined_at
                FROM project_members
            """)
            members = source_cur.fetchall()

            logger.info(f"找到 {len(members)} 个项目成员")

            with self.target_conns['project'].cursor() as project_cur:
                for member in members:
                    project_cur.execute("""
                        INSERT INTO project_members (id, project_id, user_id, role, joined_at)
                        VALUES (%s, %s, %s, %s, %s)
                        ON CONFLICT (id) DO UPDATE SET
                            role = EXCLUDED.role
                    """, (member['id'], member['project_id'], member['user_id'],
                          member['role'], member['joined_at']))

                self.target_conns['project'].commit()

        logger.info("项目数据迁移完成")

    def migrate_tasks(self):
        """迁移任务数据"""
        logger.info("开始迁移任务数据...")

        with self.source_conn.cursor(cursor_factory=RealDictCursor) as source_cur:
            # 迁移任务数据
            source_cur.execute("""
                SELECT id, title, description, project_id, assignee_id,
                       status, priority, column_id, position,
                       created_at, updated_at, deleted
                FROM tasks
                WHERE deleted = false
            """)
            tasks = source_cur.fetchall()

            logger.info(f"找到 {len(tasks)} 个任务")

            with self.target_conns['task'].cursor() as task_cur:
                for task in tasks:
                    task_cur.execute("""
                        INSERT INTO tasks (id, title, description, project_id, assignee_id,
                                          status, priority, column_id, position,
                                          created_at, updated_at)
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                        ON CONFLICT (id) DO UPDATE SET
                            title = EXCLUDED.title,
                            status = EXCLUDED.status,
                            priority = EXCLUDED.priority
                    """, (task['id'], task['title'], task['description'],
                          task['project_id'], task['assignee_id'],
                          task['status'], task['priority'],
                          task['column_id'], task['position'],
                          task['created_at'], task['updated_at']))

                self.target_conns['task'].commit()

            # 迁移评论数据
            source_cur.execute("""
                SELECT id, task_id, user_id, content, created_at, updated_at
                FROM comments
            """)
            comments = source_cur.fetchall()

            logger.info(f"找到 {len(comments)} 条评论")

            with self.target_conns['task'].cursor() as task_cur:
                for comment in comments:
                    task_cur.execute("""
                        INSERT INTO comments (id, task_id, user_id, content, created_at, updated_at)
                        VALUES (%s, %s, %s, %s, %s, %s)
                        ON CONFLICT (id) DO UPDATE SET
                            content = EXCLUDED.content
                    """, (comment['id'], comment['task_id'], comment['user_id'],
                          comment['content'], comment['created_at'],
                          comment['updated_at']))

                self.target_conns['task'].commit()

        logger.info("任务数据迁移完成")

    def run(self):
        """执行完整迁移"""
        try:
            self.connect()
            self.migrate_users()
            self.migrate_projects()
            self.migrate_tasks()
            # ... 迁移其他数据
            logger.info("数据迁移完成！")
        except Exception as e:
            logger.error(f"迁移失败：{e}")
            raise
        finally:
            self.close()


def main():
    parser = argparse.ArgumentParser(description='数据库迁移脚本')
    parser.add_argument('--source', required=True, help='源数据库连接字符串')
    parser.add_argument('--target-auth', required=True, help='Auth 数据库连接字符串')
    parser.add_argument('--target-user', required=True, help='User 数据库连接字符串')
    parser.add_argument('--target-project', required=True, help='Project 数据库连接字符串')
    parser.add_argument('--target-task', required=True, help='Task 数据库连接字符串')
    parser.add_argument('--target-story', required=True, help='Story 数据库连接字符串')
    parser.add_argument('--target-issue', required=True, help='Issue 数据库连接字符串')
    parser.add_argument('--target-wiki', required=True, help='Wiki 数据库连接字符串')
    parser.add_argument('--target-notify', required=True, help='Notify 数据库连接字符串')

    args = parser.parse_args()

    target_conn_strs = {
        'auth': args.target_auth,
        'user': args.target_user,
        'project': args.target_project,
        'task': args.target_task,
        'story': args.target_story,
        'issue': args.target_issue,
        'wiki': args.target_wiki,
        'notify': args.target_notify,
    }

    migrator = DataMigrator(args.source, target_conn_strs)
    migrator.run()


if __name__ == '__main__':
    main()
