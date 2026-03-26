#!/usr/bin/env python3
"""
数据回滚脚本 - 在迁移失败时恢复到原始状态

使用方法:
    python rollback_data.py --source postgresql://user:pass@localhost/old_db \
                           --target-auth postgresql://user:pass@localhost/auth_db \
                           ...
                           --backup-path /path/to/backup
"""

import argparse
import psycopg2
from psycopg2.extras import RealDictCursor
import logging
import os
import shutil
from datetime import datetime

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class DataRollback:
    """数据回滚器"""

    def __init__(self, source_conn_str: str, target_conn_strs: dict, backup_path: str):
        self.source_conn_str = source_conn_str
        self.target_conn_strs = target_conn_strs
        self.backup_path = backup_path
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

    def backup_target_databases(self):
        """备份当前目标数据库（回滚前再次备份）"""
        logger.info("备份目标数据库...")
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        backup_dir = os.path.join(self.backup_path, f"pre_rollback_{timestamp}")
        os.makedirs(backup_dir, exist_ok=True)

        for db_name, conn_str in self.target_conn_strs.items():
            logger.info(f"备份 {db_name}...")
            # 使用 pg_dump 备份
            dump_file = os.path.join(backup_dir, f"{db_name}_backup.sql")
            conn_info = psycopg2.extensions.parse_dsn(conn_str)
            cmd = f"pg_dump -h {conn_info.host} -U {conn_info.user} -d {conn_info.dbname} -f {dump_file}"
            if 'password' in conn_info:
                cmd = f"PGPASSWORD={conn_info.password} {cmd}"
            os.system(cmd)
            logger.info(f"已备份到：{dump_file}")

        return backup_dir

    def clear_target_databases(self):
        """清空目标数据库"""
        logger.info("清空目标数据库...")

        # 定义各数据库的清空顺序（先清空从表，再清空主表）
        table_order = {
            'auth': ['refresh_tokens', 'users'],
            'user': ['user_profiles', 'users'],
            'project': ['project_members', 'projects'],
            'task': ['comments', 'tasks'],
            'story': ['user_stories', 'epics'],
            'issue': ['issues'],
            'wiki': ['wiki_pages', 'wiki_spaces'],
            'notify': ['notifications'],
        }

        for db_name, tables in table_order.items():
            if db_name not in self.target_conns:
                continue

            logger.info(f"清空 {db_name} 的数据...")
            with self.target_conns[db_name].cursor() as cur:
                for table in tables:
                    try:
                        # 先禁用外键检查
                        cur.execute("SET CONSTRAINTS ALL DEFERRED")
                        # 截断表
                        cur.execute(f"TRUNCATE TABLE {table} CASCADE")
                        logger.info(f"  已截断表：{table}")
                    except Exception as e:
                        logger.warning(f"截断表 {table} 失败：{e}")

            self.target_conns[db_name].commit()

        logger.info("目标数据库已清空")

    def restore_from_backup(self, backup_file: str, db_name: str):
        """从备份文件恢复数据库"""
        logger.info(f"从备份文件恢复 {db_name}: {backup_file}")

        if not os.path.exists(backup_file):
            logger.error(f"备份文件不存在：{backup_file}")
            return False

        conn_str = self.target_conn_strs[db_name]
        conn_info = psycopg2.extensions.parse_dsn(conn_str)

        # 使用 psql 恢复
        cmd = f"psql -h {conn_info.host} -U {conn_info.user} -d {conn_info.dbname} -f {backup_file}"
        if 'password' in conn_info:
            cmd = f"PGPASSWORD={conn_info.password} {cmd}"

        result = os.system(cmd)
        if result == 0:
            logger.info(f"{db_name} 恢复成功")
            return True
        else:
            logger.error(f"{db_name} 恢复失败")
            return False

    def rollback_to_monolith(self):
        """回滚到单体数据库"""
        logger.info("=" * 50)
        logger.info("开始回滚到单体架构...")
        logger.info("=" * 50)

        try:
            self.connect()

            # 步骤 1: 备份当前目标数据库
            backup_dir = self.backup_target_databases()
            logger.info(f"已备份目标数据库到：{backup_dir}")

            # 步骤 2: 清空目标数据库
            self.clear_target_databases()

            # 步骤 3: 如果有从单体数据库的备份，恢复
            monolith_backup = os.path.join(self.backup_path, "monolith_backup.sql")
            if os.path.exists(monolith_backup):
                logger.info("发现单体数据库备份，开始恢复...")
                self.restore_from_backup(monolith_backup, 'source')
            else:
                logger.warning("未找到单体数据库备份文件，跳过恢复")
                logger.warning("请手动从备份恢复单体数据库")

            logger.info("=" * 50)
            logger.info("回滚完成!")
            logger.info("=" * 50)
            logger.info("后续步骤:")
            logger.info("1. 将前端 API_URL 改回单体服务地址")
            logger.info("2. 重启单体服务")
            logger.info("3. 验证功能正常")

            return True

        except Exception as e:
            logger.error(f"回滚失败：{e}")
            return False

        finally:
            self.close()


def main():
    parser = argparse.ArgumentParser(description='数据回滚脚本')
    parser.add_argument('--source', required=True, help='源数据库连接字符串')
    parser.add_argument('--target-auth', required=True, help='Auth 数据库连接字符串')
    parser.add_argument('--target-user', required=True, help='User 数据库连接字符串')
    parser.add_argument('--target-project', required=True, help='Project 数据库连接字符串')
    parser.add_argument('--target-task', required=True, help='Task 数据库连接字符串')
    parser.add_argument('--target-story', required=True, help='Story 数据库连接字符串')
    parser.add_argument('--target-issue', required=True, help='Issue 数据库连接字符串')
    parser.add_argument('--target-wiki', required=True, help='Wiki 数据库连接字符串')
    parser.add_argument('--target-notify', required=True, help='Notify 数据库连接字符串')
    parser.add_argument('--backup-path', required=True, help='备份文件路径')

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

    rollback = DataRollback(args.source, target_conn_strs, args.backup_path)
    success = rollback.rollback_to_monolith()

    exit(0 if success else 1)


if __name__ == '__main__':
    main()
