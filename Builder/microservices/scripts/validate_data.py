#!/usr/bin/env python3
"""
数据校验脚本 - 验证迁移后数据的一致性

使用方法:
    python validate_data.py --source postgresql://user:pass@localhost/old_db \
                           --target-auth postgresql://user:pass@localhost/auth_db \
                           ...
"""

import argparse
import psycopg2
from psycopg2.extras import RealDictCursor
import logging
import sys
from typing import Dict, List, Tuple

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class DataValidator:
    """数据校验器"""

    def __init__(self, source_conn_str: str, target_conn_strs: Dict[str, str]):
        self.source_conn_str = source_conn_str
        self.target_conn_strs = target_conn_strs
        self.source_conn = None
        self.target_conns = {}
        self.errors = []
        self.warnings = []

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

    def validate_user_count(self) -> bool:
        """验证用户数量"""
        logger.info("校验用户数量...")

        with self.source_conn.cursor(cursor_factory=RealDictCursor) as source_cur:
            source_cur.execute("SELECT COUNT(*) as count FROM users WHERE deleted = false")
            source_count = source_cur.fetchone()['count']

        with self.target_conns['auth'].cursor() as auth_cur:
            auth_cur.execute("SELECT COUNT(*) as count FROM users")
            auth_count = auth_cur.fetchone()['count']

        with self.target_conns['user'].cursor() as user_cur:
            user_cur.execute("SELECT COUNT(*) as count FROM users")
            user_count = user_cur.fetchone()['count']

        logger.info(f"源数据库用户数：{source_count}")
        logger.info(f"auth_db 用户数：{auth_count}")
        logger.info(f"user_db 用户数：{user_count}")

        if source_count != auth_count:
            self.errors.append(f"用户数量不匹配：源={source_count}, auth_db={auth_count}")
            return False

        if source_count != user_count:
            self.errors.append(f"用户数量不匹配：源={source_count}, user_db={user_count}")
            return False

        logger.info("用户数量校验通过 ✓")
        return True

    def validate_project_count(self) -> bool:
        """验证项目数量"""
        logger.info("校验项目数量...")

        with self.source_conn.cursor(cursor_factory=RealDictCursor) as source_cur:
            source_cur.execute("SELECT COUNT(*) as count FROM projects WHERE deleted = false")
            source_count = source_cur.fetchone()['count']

        with self.target_conns['project'].cursor() as project_cur:
            project_cur.execute("SELECT COUNT(*) as count FROM projects")
            project_count = project_cur.fetchone()['count']

        logger.info(f"源数据库项目数：{source_count}")
        logger.info(f"project_db 项目数：{project_count}")

        if source_count != project_count:
            self.errors.append(f"项目数量不匹配：源={source_count}, project_db={project_count}")
            return False

        logger.info("项目数量校验通过 ✓")
        return True

    def validate_task_count(self) -> bool:
        """验证任务数量"""
        logger.info("校验任务数量...")

        with self.source_conn.cursor(cursor_factory=RealDictCursor) as source_cur:
            source_cur.execute("SELECT COUNT(*) as count FROM tasks WHERE deleted = false")
            source_count = source_cur.fetchone()['count']

        with self.target_conns['task'].cursor() as task_cur:
            task_cur.execute("SELECT COUNT(*) as count FROM tasks")
            task_count = task_cur.fetchone()['count']

        logger.info(f"源数据库任务数：{source_count}")
        logger.info(f"task_db 任务数：{task_count}")

        if source_count != task_count:
            self.errors.append(f"任务数量不匹配：源={source_count}, task_db={task_count}")
            return False

        logger.info("任务数量校验通过 ✓")
        return True

    def validate_comment_count(self) -> bool:
        """验证评论数量"""
        logger.info("校验评论数量...")

        with self.source_conn.cursor(cursor_factory=RealDictCursor) as source_cur:
            source_cur.execute("SELECT COUNT(*) as count FROM comments")
            source_count = source_cur.fetchone()['count']

        with self.target_conns['task'].cursor() as task_cur:
            task_cur.execute("SELECT COUNT(*) as count FROM comments")
            task_count = task_cur.fetchone()['count']

        logger.info(f"源数据库评论数：{source_count}")
        logger.info(f"task_db 评论数：{task_count}")

        if source_count != task_count:
            self.errors.append(f"评论数量不匹配：源={source_count}, task_db={task_count}")
            return False

        logger.info("评论数量校验通过 ✓")
        return True

    def validate_project_members(self) -> bool:
        """验证项目成员数据"""
        logger.info("校验项目成员数据...")

        with self.source_conn.cursor(cursor_factory=RealDictCursor) as source_cur:
            source_cur.execute("""
                SELECT pm.id, pm.project_id, pm.user_id, pm.role,
                       p.name as project_name, u.username
                FROM project_members pm
                JOIN projects p ON pm.project_id = p.id
                JOIN users u ON pm.user_id = u.id
                WHERE p.deleted = false AND u.deleted = false
            """)
            source_members = source_cur.fetchall()

        with self.target_conns['project'].cursor() as project_cur:
            project_cur.execute("""
                SELECT pm.id, pm.project_id, pm.user_id, pm.role,
                       p.name as project_name, u.username
                FROM project_members pm
                JOIN projects p ON pm.project_id = p.id
                JOIN users u ON pm.user_id = u.id
            """)
            target_members = project_cur.fetchall()

        logger.info(f"源数据库项目成员数：{len(source_members)}")
        logger.info(f"project_db 项目成员数：{len(target_members)}")

        if len(source_members) != len(target_members):
            self.errors.append(f"项目成员数量不匹配：源={len(source_members)}, target={len(target_members)}")
            return False

        # 验证每个成员的详情
        source_set = {(m['id'], m['project_id'], m['user_id']) for m in source_members}
        target_set = {(m['id'], m['project_id'], m['user_id']) for m in target_members}

        missing_in_target = source_set - target_set
        extra_in_target = target_set - source_set

        if missing_in_target:
            self.errors.append(f"目标数据库缺少的成员：{missing_in_target}")
            return False

        if extra_in_target:
            self.warnings.append(f"目标数据库多出的成员：{extra_in_target}")

        logger.info("项目成员数据校验通过 ✓")
        return True

    def validate_foreign_keys(self) -> bool:
        """验证外键引用完整性"""
        logger.info("校验外键引用完整性...")

        issues = []

        # 验证任务的项目引用
        with self.target_conns['task'].cursor() as task_cur:
            task_cur.execute("""
                SELECT t.id, t.project_id
                FROM tasks t
                LEFT JOIN projects p ON t.project_id = p.id
                WHERE p.id IS NULL
            """)
            orphan_tasks = task_cur.fetchall()

            if orphan_tasks:
                issues.append(f"找到 {len(orphan_tasks)} 个孤立任务（项目不存在）")

        # 验证评论的任务引用
        with self.target_conns['task'].cursor() as task_cur:
            task_cur.execute("""
                SELECT c.id, c.task_id
                FROM comments c
                LEFT JOIN tasks t ON c.task_id = t.id
                WHERE t.id IS NULL
            """)
            orphan_comments = task_cur.fetchall()

            if orphan_comments:
                issues.append(f"找到 {len(orphan_comments)} 条孤立评论（任务不存在）")

        if issues:
            for issue in issues:
                self.warnings.append(f"外键引用问题：{issue}")
            logger.warning(f"发现 {len(issues)} 个外键引用问题")
            return False

        logger.info("外键引用完整性校验通过 ✓")
        return True

    def run_all_validations(self) -> bool:
        """运行所有校验"""
        logger.info("=" * 50)
        logger.info("开始数据校验...")
        logger.info("=" * 50)

        try:
            self.connect()

            results = []
            results.append(self.validate_user_count())
            results.append(self.validate_project_count())
            results.append(self.validate_task_count())
            results.append(self.validate_comment_count())
            results.append(self.validate_project_members())
            results.append(self.validate_foreign_keys())

            logger.info("=" * 50)
            logger.info("校验完成")
            logger.info("=" * 50)

            if self.errors:
                logger.error(f"发现 {len(self.errors)} 个错误:")
                for error in self.errors:
                    logger.error(f"  - {error}")

            if self.warnings:
                logger.warning(f"发现 {len(self.warnings)} 个警告:")
                for warning in self.warnings:
                    logger.warning(f"  - {warning}")

            if not results:
                logger.info("所有校验项均通过 ✓")
                return True
            elif all(results):
                logger.info("所有校验项均通过 ✓")
                return True
            else:
                logger.error("校验失败 ✗")
                return False

        except Exception as e:
            logger.error(f"校验过程出错：{e}")
            return False
        finally:
            self.close()


def main():
    parser = argparse.ArgumentParser(description='数据校验脚本')
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

    validator = DataValidator(args.source, target_conn_strs)
    success = validator.run_all_validations()

    sys.exit(0 if success else 1)


if __name__ == '__main__':
    main()
