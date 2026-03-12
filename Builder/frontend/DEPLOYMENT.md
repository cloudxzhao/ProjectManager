# ProjectHub 前端部署文档

## 部署概述

本文档说明如何将 ProjectHub 前端应用部署到 Nginx 服务器。

## 部署架构

```
用户请求 → Nginx (端口 80) → Next.js 应用 (端口 3000)
                        → 后端 API (端口 8080)
```

## 已完成的部署步骤

### 1. 前端打包

```bash
cd /data/project/ProjectManager/Builder/frontend
npm run build
```

构建产物位于 `.next/` 目录。

### 2. Nginx 配置文件

**配置文件位置**: `/etc/nginx/sites-available/projecthub.conf`
**启用链接**: `/etc/nginx/sites-enabled/projecthub.conf`

### 3. Systemd 服务

**服务文件位置**: `/etc/systemd/system/projecthub-frontend.service`
**服务名称**: `projecthub-frontend`

---

## 服务状态

### 运行中的服务

| 服务名称 | 状态 | 端口 | 说明 |
|---------|------|------|------|
| Nginx | ✅ 运行中 | 80 | Web 服务器/反向代理 |
| projecthub-frontend | ✅ 运行中 | 3000 | Next.js 应用 |
| projecthub-backend | ❌ 未启动 | 8080 | Spring Boot 后端 |

### 启动后端服务

```bash
# 启动后端服务
echo "926926" | sudo -S systemctl start projecthub-backend

# 查看状态
echo "926926" | sudo -S systemctl status projecthub-backend

# 设置开机自启
echo "926926" | sudo -S systemctl enable projecthub-backend
```

---

## 管理命令

### 前端服务管理

```bash
# 启动服务
sudo systemctl start projecthub-frontend

# 停止服务
sudo systemctl stop projecthub-frontend

# 重启服务
sudo systemctl restart projecthub-frontend

# 查看状态
sudo systemctl status projecthub-frontend

# 查看日志
sudo journalctl -u projecthub-frontend -f

# 开机自启
sudo systemctl enable projecthub-frontend
```

### Nginx 管理

```bash
# 重启 Nginx
sudo systemctl restart nginx

# 重载配置 (不中断服务)
sudo systemctl reload nginx

# 测试配置
sudo nginx -t

# 查看状态
sudo systemctl status nginx
```

---

## 访问地址

| 功能 | URL | 说明 |
|------|-----|------|
| 前端应用 | http://localhost/ | ProjectHub 主应用 |
| 登录页面 | http://localhost/login | 用户登录 |
| 注册页面 | http://localhost/register | 用户注册 |
| 仪表盘 | http://localhost/dashboard | 项目仪表盘 |
| 项目管理 | http://localhost/projects | 项目列表 |
| API 接口 | http://localhost/api/v1/ | REST API |
| Swagger 文档 | http://localhost/swagger-ui.html | API 文档 |
| 健康检查 | http://localhost/health | 服务健康检查 |

---

## 日志文件位置

| 组件 | 日志路径 |
|------|----------|
| Nginx 访问日志 | /var/log/nginx/projecthub_access.log |
| Nginx 错误日志 | /var/log/nginx/projecthub_error.log |
| Next.js 服务日志 | journalctl -u projecthub-frontend |

---

## 配置文件说明

### Nginx 配置 (`/etc/nginx/sites-available/projecthub.conf`)

```nginx
# 反向代理配置说明：
# 1. /api/* → http://127.0.0.1:8080/api/* (后端 API)
# 2. /*    → http://127.0.0.1:3000/* (Next.js 应用)
# 3. /_next/static → 静态资源缓存 1 年
# 4. 支持 WebSocket 连接
# 5. Gzip 压缩优化传输
# 6. 客户端最大请求体 50MB
```

### Systemd 服务配置 (`/etc/systemd/system/projecthub-frontend.service`)

```ini
# 关键配置说明：
# - WorkingDirectory: /data/project/ProjectManager/Builder/frontend
# - PORT=3000
# - NODE_ENV=production
# - NEXT_PUBLIC_API_URL=/api/v1 (通过 Nginx 反向代理)
# - 自动重启 (Restart=on-failure)
```

---

## 故障排查

### 1. 前端无法访问

```bash
# 检查 Next.js 服务状态
sudo systemctl status projecthub-frontend

# 查看 Next.js 日志
sudo journalctl -u projecthub-frontend -n 50

# 检查端口是否监听
sudo netstat -tlnp | grep 3000

# 测试直接访问 Next.js
curl http://localhost:3000/
```

### 2. API 返回 502 错误

```bash
# 检查后端服务状态
sudo systemctl status projecthub-backend

# 检查后端端口
sudo netstat -tlnp | grep 8080

# 测试后端 API 直接访问
curl http://localhost:8080/api/v1/health
```

### 3. Nginx 配置问题

```bash
# 测试 Nginx 配置
sudo nginx -t

# 查看 Nginx 错误日志
sudo tail -f /var/log/nginx/projecthub_error.log

# 重载 Nginx 配置
sudo systemctl reload nginx
```

---

## 环境变量

应用使用以下环境变量：

| 变量名 | 值 | 说明 |
|--------|-----|------|
| NODE_ENV | production | 生产环境模式 |
| PORT | 3000 | Next.js 服务端口 |
| NEXT_PUBLIC_API_URL | /api/v1 | API 基础路径 |
| NEXT_PUBLIC_APP_NAME | ProjectHub | 应用名称 |
| NEXT_PUBLIC_APP_URL | http://localhost | 应用访问 URL |

---

## 重新部署流程

### 完整重新部署

```bash
# 1. 停止服务
sudo systemctl stop projecthub-frontend

# 2. 进入项目目录
cd /data/project/ProjectManager/Builder/frontend

# 3. 拉取最新代码 (如适用)
git pull

# 4. 重新构建
npm run build

# 5. 启动服务
sudo systemctl start projecthub-frontend

# 6. 验证状态
sudo systemctl status projecthub-frontend
```

### 热更新 (仅配置变更)

```bash
# 1. 测试 Nginx 配置
sudo nginx -t

# 2. 重载 Nginx
sudo systemctl reload nginx
```

---

## 安全建议

1. **启用 HTTPS**: 使用 Let's Encrypt 配置 SSL 证书
2. **配置防火墙**: 仅开放必要的端口 (80, 443)
3. **限制请求频率**: 配置 Nginx 限流模块
4. **隐藏版本信息**: 配置 `server_tokens off`
5. **配置 CSP**: 添加内容安全策略头

---

## 性能优化建议

1. **启用 HTTP/2**: 升级 Nginx 支持 HTTP/2
2. **配置缓存**: 优化浏览器缓存策略
3. **启用 Brotli**: 使用 Brotli 替代 Gzip 压缩
4. **配置 CDN**: 静态资源使用 CDN 分发
5. **调整 worker 数量**: 根据服务器配置优化 Nginx worker_processes

---

**部署日期**: 2026-03-12
**Next.js 版本**: 14.2.35
**Nginx 版本**: 1.24.0
**Node.js 版本**: 22.13.1
