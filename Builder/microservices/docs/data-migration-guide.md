# 数据迁移完整指南

## 目录

1. [迁移前准备](#迁移前准备)
2. [迁移步骤](#迁移步骤)
3. [迁移脚本使用说明](#迁移脚本使用说明)
4. [数据校验](#数据校验)
5. [回滚方案](#回滚方案)
6. [常见问题](#常见问题)

---

## 迁移前准备

### 1. 环境检查

确保以下服务正常运行：

```bash
# 检查 Docker 服务
docker-compose ps

# 预期输出：
# NAME                    STATUS          PORTS
# nacos                   Up              0.0.0.0:8848->8848/tcp
# postgres                Up              0.0.0.0:5432->5432/tcp
# rabbitmq                Up              0.0.0.0:5672->5672/tcp
```

### 2. 数据备份

**重要**: 在迁移前务必备份生产数据！

```bash
# 备份单体数据库
pg_dump -h localhost -U postgres -d projectdb -f backup/monolith_backup.sql

# 验证备份文件
ls -lh backup/monolith_backup.sql
```

### 3. 准备目标数据库

创建微服务数据库：

```bash
# 使用 Docker Compose 启动所有数据库
docker-compose -f docker-compose.db.yml up -d

# 或使用 init 脚本初始化
psql -h localhost -U postgres -f init-scripts/auth-db-init.sql
psql -h localhost -U postgres -f init-scripts/user-db-init.sql
psql -h localhost -U postgres -f init-scripts/project-db-init.sql
# ... 其他数据库
```

### 4. 安装依赖

```bash
# 安装 Python 依赖
pip install psycopg2-binary argparse
```

---

## 迁移步骤

### 阶段 1: 双写模式（1-2 周）

在此阶段，新旧系统同时运行，数据双向同步：

```
┌─────────────────────────────────────────────────────────────┐
│  应用层                                                      │
│  ┌─────────────┐     ┌─────────────┐                       │
│  │  写操作     │────▶│  旧数据库   │                       │
│  │             │     │  (主)       │                       │
│  └─────────────┘     └──────┬──────┘                       │
│                             │                               │
│                             │ 同步                          │
│                             ▼                               │
│                      ┌─────────────┐                       │
│                      │  新数据库   │                       │
│                      │  (从)       │                       │
│                      └─────────────┘                       │
└─────────────────────────────────────────────────────────────┘
```

### 阶段 2: 数据迁移

运行迁移脚本：

```bash
cd Builder/microservices/scripts

# 执行迁移
python migrate_data.py \
  --source "postgresql://postgres:postgres@localhost:5432/projectdb" \
  --target-auth "postgresql://postgres:postgres@localhost:5432/auth_db" \
  --target-user "postgresql://postgres:postgres@localhost:5432/user_db" \
  --target-project "postgresql://postgres:postgres@localhost:5432/project_db" \
  --target-task "postgresql://postgres:postgres@localhost:5432/task_db" \
  --target-story "postgresql://postgres:postgres@localhost:5432/story_db" \
  --target-issue "postgresql://postgres:postgres@localhost:5432/issue_db" \
  --target-wiki "postgresql://postgres:postgres@localhost:5432/wiki_db" \
  --target-notify "postgresql://postgres:postgres@localhost:5432/notify_db"
```

### 阶段 3: 数据校验

```bash
# 执行校验
python validate_data.py \
  --source "postgresql://postgres:postgres@localhost:5432/projectdb" \
  --target-auth "postgresql://postgres:postgres@localhost:5432/auth_db" \
  --target-user "postgresql://postgres:postgres@localhost:5432/user_db" \
  --target-project "postgresql://postgres:postgres@localhost:5432/project_db" \
  --target-task "postgresql://postgres:postgres@localhost:5432/task_db" \
  --target-story "postgresql://postgres:postgres@localhost:5432/story_db" \
  --target-issue "postgresql://postgres:postgres@localhost:5432/issue_db" \
  --target-wiki "postgresql://postgres:postgres@localhost:5432/wiki_db" \
  --target-notify "postgresql://postgres:postgres@localhost:5432/notify_db"
```

### 阶段 4: 流量切换

1. **切换读流量**: 修改配置，从新数据库读取数据
2. **切换写流量**: 关闭双写，只写新数据库
3. **验证功能**: 确保所有功能正常

---

## 迁移脚本使用说明

### migrate_data.py - 数据迁移脚本

**功能**: 将数据从单体数据库迁移到微服务数据库

**参数**:
| 参数 | 必填 | 说明 |
|------|------|------|
| --source | 是 | 源数据库连接字符串 |
| --target-auth | 是 | Auth 数据库连接字符串 |
| --target-user | 是 | User 数据库连接字符串 |
| --target-project | 是 | Project 数据库连接字符串 |
| --target-task | 是 | Task 数据库连接字符串 |
| --target-story | 是 | Story 数据库连接字符串 |
| --target-issue | 是 | Issue 数据库连接字符串 |
| --target-wiki | 是 | Wiki 数据库连接字符串 |
| --target-notify | 是 | Notify 数据库连接字符串 |

**示例**:
```bash
python migrate_data.py \
  --source "postgresql://user:pass@localhost/old_db" \
  --target-auth "postgresql://user:pass@localhost/auth_db" \
  ...
```

### validate_data.py - 数据校验脚本

**功能**: 验证迁移后数据的一致性和完整性

**校验项**:
- 用户数量校验
- 项目数量校验
- 任务数量校验
- 评论数量校验
- 项目成员数据校验
- 外键引用完整性校验

**返回值**:
- 0: 所有校验通过
- 1: 校验失败

**示例**:
```bash
python validate_data.py \
  --source "postgresql://user:pass@localhost/old_db" \
  --target-auth "postgresql://user:pass@localhost/auth_db" \
  ...
```

### rollback_data.py - 回滚脚本

**功能**: 在迁移失败时恢复到原始状态

**参数**:
| 参数 | 必填 | 说明 |
|------|------|------|
| --source | 是 | 源数据库连接字符串 |
| --target-* | 是 | 各微服务数据库连接字符串 |
| --backup-path | 是 | 备份文件路径 |

**示例**:
```bash
python rollback_data.py \
  --source "postgresql://user:pass@localhost/old_db" \
  --target-auth "postgresql://user:pass@localhost/auth_db" \
  ... \
  --backup-path /path/to/backups
```

---

## 数据校验

### 自动校验

使用 `validate_data.py` 脚本进行自动校验。

### 手动校验

```sql
-- 1. 校验用户数据
SELECT 'source' as db, COUNT(*) FROM projectdb.users WHERE deleted = false
UNION ALL
SELECT 'auth_db', COUNT(*) FROM auth_db.users
UNION ALL
SELECT 'user_db', COUNT(*) FROM user_db.users;

-- 2. 校验项目数据
SELECT 'source' as db, COUNT(*) FROM projectdb.projects WHERE deleted = false
UNION ALL
SELECT 'project_db', COUNT(*) FROM project_db.projects;

-- 3. 校验任务数据
SELECT 'source' as db, COUNT(*) FROM projectdb.tasks WHERE deleted = false
UNION ALL
SELECT 'task_db', COUNT(*) FROM task_db.tasks;

-- 4. 随机抽样校验
SELECT * FROM projectdb.users ORDER BY RANDOM() LIMIT 5;
SELECT * FROM auth_db.users ORDER BY RANDOM() LIMIT 5;
SELECT * FROM user_db.users ORDER BY RANDOM() LIMIT 5;
```

---

## 回滚方案

### 回滚触发条件

满足以下任一条件时执行回滚：

1. 数据校验失败且无法修复
2. 关键功能不可用超过 30 分钟
3. 性能下降超过 50%
4. 数据丢失或不一致

### 回滚步骤

```bash
# 1. 停止微服务
docker-compose stop auth-service user-service project-service ...

# 2. 执行回滚脚本
python rollback_data.py \
  --source "postgresql://user:pass@localhost/old_db" \
  --target-auth "postgresql://user:pass@localhost/auth_db" \
  ... \
  --backup-path /path/to/backups

# 3. 重启单体服务
docker-compose start monolith-service

# 4. 切换前端配置
# 将 NEXT_PUBLIC_API_URL 改回单体服务地址

# 5. 验证功能
```

### 回滚验证清单

- [ ] 单体服务启动成功
- [ ] 前端配置已切换
- [ ] 登录功能正常
- [ ] 项目列表可访问
- [ ] 任务管理功能正常
- [ ] 数据完整性确认

---

## 常见问题

### Q1: 迁移脚本报错 "connection refused"

**A**: 检查数据库是否启动且连接字符串正确

```bash
# 检查数据库端口
netstat -an | grep 5432

# 测试连接
psql -h localhost -U postgres -d projectdb -c "SELECT 1"
```

### Q2: 迁移速度太慢

**A**: 可以调整批量处理大小

```python
# 在 migrate_data.py 中调整
BATCH_SIZE = 1000  # 默认值，可以调大到 5000 或 10000
```

### Q3: 外键约束导致迁移失败

**A**: 在迁移前禁用外键检查

```sql
-- 在目标数据库执行
ALTER TABLE table_name DISABLE TRIGGER ALL;
-- 迁移完成后启用
ALTER TABLE table_name ENABLE TRIGGER ALL;
```

### Q4: 迁移后自增 ID 不连续

**A**: 重置序列

```sql
-- 对每个表的主键序列执行
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
```

### Q5: 字符集不一致导致乱码

**A**: 确保源和目标数据库字符集一致

```sql
-- 检查字符集
SHOW SERVER_ENCODING;

-- 创建数据库时指定字符集
CREATE DATABASE db_name WITH ENCODING 'UTF8';
```

---

## 迁移时间表

| 阶段 | 时间 | 任务 | 负责人 |
|------|------|------|--------|
| 准备 | Week 1 | 环境搭建、备份 | DBA |
| 演练 | Week 2 | 测试环境迁移 | 开发团队 |
| 迁移 | Week 3 | 生产环境迁移 | 全体 |
| 验证 | Week 4 | 数据校验、功能验证 | QA |

---

## 联系方式

迁移过程中遇到问题，请联系:

- 技术负责人：[姓名] [联系方式]
- DBA: [姓名] [联系方式]
- 开发团队：[联系方式]

---

**重要提示**:
- 迁移前务必在测试环境充分演练
- 生产迁移选择低峰时段（如凌晨）
- 保持备份文件至少 30 天
- 记录迁移过程中的所有操作和异常
