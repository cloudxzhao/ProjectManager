# ProjectHub 项目管理系统

> 让团队协作更高效 · 让项目交付更可控 · 让知识沉淀更有序

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.x-61dafb.svg)](https://react.dev)
[![Next.js](https://img.shields.io/badge/Next.js-14.x-black.svg)](https://nextjs.org)

---

## 📖 项目简介

ProjectHub 是一款**现代化、高颜值、全功能**的项目管理系统，融合敏捷开发理念与现代 UI 设计美学，打造**高效、智能、可控**的协作体验。

### 核心特性

- 📋 **任务看板** - 可视化 Kanban 看板，支持拖拽操作
- 📁 **项目管理** - 完整的项目 CRUD 和成员管理
- ✅ **任务管理** - 任务创建、分配、状态流转
- ✏️ **用户故事** - Epic/Story 敏捷需求管理
- 🐛 **问题追踪** - Bug/Issue 全流程追踪
- 📄 **Wiki 知识库** - Markdown 文档协作
- 📊 **数据报表** - 燃尽图、累积流图、速度图
- 🔔 **消息通知** - 实时站内信和邮件通知

---

## 🏗️ 技术架构

### 前端技术栈

| 技术 | 选型 | 版本 |
|------|------|------|
| 框架 | React | 18.x |
| 元框架 | Next.js | 14.x (App Router) |
| 语言 | TypeScript | 5.x |
| UI 库 | Ant Design | 6.x |
| 状态管理 | Zustand + TanStack Query | 最新 |
| 样式 | Tailwind CSS | 3.x |
| HTTP 客户端 | Axios | 1.x |
| 表单处理 | React Hook Form | 7.x |
| 数据验证 | Zod | 3.x |
| 拖拽库 | @dnd-kit | 6.x |
| 图表库 | Recharts | 3.x |

### 后端技术栈

| 技术 | 选型 | 版本 |
|------|------|------|
| JDK | OpenJDK | 21 (LTS) |
| 框架 | Spring Boot | 3.2.x |
| 构建工具 | Maven | 3.9+ |
| 数据库 | PostgreSQL | 15.x |
| 缓存 | Redis | 7.x |
| ORM | Spring Data JPA + MyBatis Plus | 最新 |
| 认证 | Spring Security + JWT | 最新 |
| API 文档 | SpringDoc OpenAPI | 2.x |
| 连接池 | HikariCP | 5.x |
| 映射 | MapStruct | 1.5.x |

---

## 📁 项目结构

```
ProjectManagerStudy/
├── Builder/
│   ├── backend/                 # 后端 Spring Boot 项目
│   │   ├── src/main/java/
│   │   │   └── com/projecthub/
│   │   │       ├── common/      # 公共模块
│   │   │       ├── module/      # 业务模块
│   │   │       ├── security/    # 安全认证
│   │   │       └── infrastructure/
│   │   └── src/main/resources/
│   │       └── application*.yml
│   │
│   └── frontend/                # 前端 Next.js 项目
│       ├── src/
│       │   ├── app/             # Next.js 路由
│       │   ├── components/      # React 组件
│       │   ├── lib/             # 工具库
│       │   ├── stores/          # 状态管理
│       │   ├── types/           # TS 类型定义
│       │   └── config/          # 配置文件
│       └── public/
│
├── docs/                        # 架构设计文档
│   ├── frontend-architecture.md
│   ├── backend-architecture.md
│   └── api/
│       └── openapi.yaml
│
├── Requirements/                # 产品需求文档
│   ├── Requirements.md
│   └── CLAUDE.md
│
└── Pages/                       # 页面原型
    ├── login.html
    ├── dashboard.html
    ├── projects.html
    └── project-detail.html
```

---

## 🚀 快速开始

### 环境要求

```bash
# 后端
- JDK 21+
- Maven 3.9+
- PostgreSQL 15+
- Redis 7+

# 前端
- Node.js 18+
- npm/yarn
```

### 使用 Docker 启动数据库

```bash
# 启动 PostgreSQL
docker run -d \
  --name projecthub-postgres \
  -e POSTGRES_PASSWORD=Admin_2026 \
  -p 5432:5432 \
  postgres:16

# 启动 Redis
docker run -d \
  --name projecthub-redis \
  -p 6379:6379 \
  redis:latest
```

### 后端启动

```bash
cd Builder/backend

# 创建数据库
docker exec projecthub-postgres createdb -U postgres projecthub_dev

# 编译项目
mvn clean install

# 启动应用 (开发环境)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 访问 Swagger UI
# http://localhost:9527/swagger-ui.html
```

### 前端启动

```bash
cd Builder/frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 访问应用
# http://localhost:3000
```

---

## 📋 功能模块

### 认证模块
- 邮箱密码登录 / 注册
- JWT Token 认证
- 密码找回

### 项目管理
- 项目 CRUD 操作
- 项目成员管理
- 项目权限控制
- 项目归档

### 任务管理
- 任务看板 (Kanban)
- 任务 CRUD 操作
- 任务拖拽状态流转
- 子任务管理
- 任务评论

### 敏捷开发
- 史诗 (Epic) 管理
- 用户故事 (User Story)
- 问题追踪 (Bug/Issue)

### 知识库
- Wiki 文档管理
- Markdown 编辑
- 版本历史

### 数据报表
- 燃尽图
- 累积流图
- 速度图
- 任务分布图

---

## 🔌 API 接口

### 主要端点

| 端点 | 说明 |
|------|------|
| `POST /api/v1/auth/login` | 用户登录 |
| `POST /api/v1/auth/register` | 用户注册 |
| `GET /api/v1/projects` | 项目列表 |
| `POST /api/v1/projects` | 创建项目 |
| `GET /api/v1/projects/{id}` | 项目详情 |
| `PUT /api/v1/projects/{id}` | 更新项目 |
| `DELETE /api/v1/projects/{id}` | 删除项目 |
| `GET /api/v1/projects/{id}/members` | 项目成员 |
| `GET /api/v1/tasks` | 任务列表 |
| `POST /api/v1/tasks` | 创建任务 |
| `POST /api/v1/tasks/{id}/move` | 移动任务 |

完整 API 文档请启动后端后访问：
```
http://localhost:9527/swagger-ui.html
```

---

## 👥 用户角色

| 角色 | 权限 |
|------|------|
| 系统管理员 | 平台全部权限 |
| 企业管理员 | 企业内全部权限 |
| 项目经理 | 项目管理、成员管理 |
| 团队成员 | 查看参与项目、创建任务 |
| 访客 | 只读权限 |

---

## 📸 系统截图

### 仪表盘
![Dashboard](./img/dashboard.png)

### 项目列表
![Projects](./img/projects.png)

### 任务看板
![Kanban](./img/kanban.png)

### 项目详情
![Project Detail](./img/project-detail.png)

---

## 🧪 测试

### 后端测试

```bash
cd Builder/backend

# 运行单元测试
mvn test

# 运行测试并生成覆盖率报告
mvn clean test jacoco:report

# 代码格式化检查
mvn spotless:check

# 代码格式化
mvn spotless:apply
```

### 前端测试

```bash
cd Builder/frontend

# 运行单元测试
npm test

# 运行 E2E 测试
npm run test:e2e

# 代码检查
npm run lint

# 代码格式化
npm run format
```

---

## 📦 部署

### Docker 部署

```bash
# 后端构建
cd Builder/backend
mvn clean package -DskipTests
docker build -t projecthub-backend:latest .

# 前端构建
cd Builder/frontend
npm run build
docker build -t projecthub-frontend:latest .

# 使用 docker-compose 启动
docker-compose up -d
```

### 环境变量配置

```bash
# .env 文件
DB_HOST=localhost
DB_PORT=5432
DB_NAME=projecthub
DB_USER=postgres
DB_PASSWORD=your_password

REDIS_HOST=localhost
REDIS_PORT=6379

JWT_SECRET=your-secret-key
JWT_EXPIRATION=7200000
```

---

## 📚 文档

| 文档 | 说明 |
|------|------|
| [Requirements.md](Requirements/Requirements.md) | 产品需求文档 |
| [frontend-architecture.md](docs/frontend-architecture.md) | 前端架构设计 |
| [backend-architecture.md](docs/backend-architecture.md) | 后端架构设计 |
| [openapi.yaml](docs/api/openapi.yaml) | API 接口文档 |

---

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范

- 后端：遵循 Google Java Style，使用 `mvn spotless:check` 检查
- 前端：遵循 ESLint + Prettier 规范，使用 `npm run lint` 检查

---

## 📄 开源协议

MIT License

---

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [React](https://react.dev)
- [Next.js](https://nextjs.org)
- [Ant Design](https://ant.design)
- [PostgreSQL](https://www.postgresql.org)
- [Redis](https://redis.io)

---

## 📬 联系方式

- 项目主页：[ProjectHub](#)
- 问题反馈：[Issues](../../issues)

---

<div align="center">

**让团队协作更高效 · 让项目交付更可控 · 让知识沉淀更有序**

Made with ❤️ by ProjectHub Team

</div>
