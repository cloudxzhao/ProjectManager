# ProjectHub 本地开发环境配置指南

## 环境概述

本地开发环境使用 Docker 容器运行基础设施服务：

| 服务 | 容器名 | 端口映射 | 账号/密码 |
|------|--------|----------|-----------|
| PostgreSQL | project-manager-postgres | 15432 -> 5432 | postgres / Admin_2026 |
| Redis | project-manager-redis | 16379 -> 6379 | - / Admin_2026 |

## 数据库配置

### PostgreSQL 连接信息

```yaml
host: localhost
port: 15432
database: projecthub_dev
username: postgres
password: Admin_2026
```

### Redis 连接信息

```yaml
host: localhost
port: 16379
password: Admin_2026
```

## 微服务配置

### 环境变量文件

在 `Builder/microservices/` 目录下创建 `.env.local` 文件：

```bash
# PostgreSQL 配置（使用本地 Docker 容器，端口 15432）
DB_HOST=localhost
DB_PORT=15432
DB_NAME=projecthub_dev
DB_USER=postgres
DB_PASSWORD=Admin_2026

# Redis 配置（使用本地 Docker 容器，端口 16379）
REDIS_HOST=localhost
REDIS_PORT=16379
REDIS_PASSWORD=Admin_2026

# Nacos 配置
NACOS_SERVER=localhost:8848

# JWT 配置
JWT_SECRET=projecthub-secret-key-must-be-at-least-256-bits-long-for-hs256
```

### 微服务端口

| 服务 | 端口 |
|------|------|
| API Gateway | 8080 |
| Auth Service | 9521 |
| User Service | 9522 |
| Project Service | 9523 |
| Task Service | 9524 |
| Story Service | 9525 |
| Issue Service | 9526 |
| Wiki Service | 9527 |
| Notification Service | 9528 |

## 启动步骤

### 1. 确认 Docker 容器运行

```bash
docker ps | grep project-manager
```

应该看到：
- `project-manager-postgres`
- `project-manager-redis`

### 2. 启动微服务（Windows）

```bash
cd Builder/microservices
start-dev.bat
```

### 3. 启动前端

```bash
cd Builder/frontend
npm run dev
```

访问 http://localhost:3000

## 数据库表结构

当前 `projecthub_dev` 数据库包含以下表：

| 表名 | 说明 |
|------|------|
| sys_user | 系统用户表 |
| sys_role | 系统角色表 |
| sys_permission | 系统权限表 |
| sys_role_permission | 角色权限关联表 |
| sys_user_role | 用户角色关联表 |
| project | 项目表 |
| project_member | 项目成员表 |
| task | 任务表 |
| task_comment | 任务评论表 |
| sub_task | 子任务表 |
| epic | 史诗表 |
| user_story | 用户故事表 |
| issue | 问题追踪表 |
| notification | 通知表 |
| wiki_document | Wiki 文档表 |
| wiki_attachment | Wiki 附件表 |
| wiki_history | Wiki 历史表 |
| operation_log | 操作日志表 |
| sys_config | 系统配置表 |
| flyway_schema_history | Flyway 迁移历史表 |

## 默认管理员账号

```
邮箱：admin@projecthub.com
状态：ACTIVE
```

## 验证连接

### 测试 PostgreSQL 连接

```bash
docker exec -it project-manager-postgres psql -U postgres -d projecthub_dev -c "SELECT count(*) FROM project;"
```

### 测试 Redis 连接

```bash
docker exec -it project-manager-redis redis-cli -a "Admin_2026" ping
```

### 测试 API 健康检查

```bash
curl http://localhost:8080/actuator/health
```

## 常见问题

### Q: 端口冲突怎么办？

A: 修改 Docker 容器端口映射，并同步更新 `.env.local` 文件：
```bash
# 例如修改 PostgreSQL 端口为 15433
docker stop project-manager-postgres
docker rm project-manager-postgres
docker run -d -p 15433:5432 --name project-manager-postgres ...
```

### Q: 微服务无法连接数据库？

A: 检查以下几点：
1. 确认 Docker 容器正在运行
2. 确认 `.env.local` 文件配置正确
3. 确认环境变量已导出

### Q: 前端无法连接后端？

A: 检查：
1. API Gateway 是否启动在 8080 端口
2. `next.config.js` 中的代理配置是否正确
3. 浏览器控制台查看具体错误信息
