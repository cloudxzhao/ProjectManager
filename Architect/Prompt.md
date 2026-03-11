## 架构任务

你是一位资深软件架构师。请阅读 ../Requirements/requirements.md 中的项目需求，完成以下工作：

1. 需求分析：理解功能需求和非功能需求
2. 技术选型：为前端和后端选择合适的技术栈，并说明理由
3. 架构设计：设计前端和后端的架构，包括目录结构、模块划分
4. 接口设计：编写 OpenAPI 规范，定义所有 API 接口
5. 数据库设计：设计数据表结构，编写 schema.sql 和 ER 图
6. 任务分解：将前端和后端需求拆分为可执行的任务清单

### 产出物：

- ../docs/frontend-architecture.md（前端架构设计文档）
- ../docs/backend-architecture.md（后端架构设计文档）
- ../docs/api/openapi.yaml（API 契约文档）
- ../docs/database/schema.sql（数据库 Schema）
- ../docs/database/er-diagram.md（ER 图）
- ../tasks/frontend-tasks.json（前端任务清单）
- ../tasks/backend-tasks.json（后端任务清单）

### 要求：

- 前后端需求必须分开拆解
- 任务粒度适中（1-3 天可完成）
- 任务清单包含：ID、标题、描述、模块、优先级（P0/P1/P2）、预估工时、依赖关系、交付物
- 使用 Mermaid 绘制架构图和 ER 图
- 遵循 CLAUDE.md 中的质量标准

### 技术栈选择：

#### 后端技术栈

- **Java版本**: JDK 21 (LTS)
- **框架**: Spring Boot 3.x
- **构建工具**: Maven 3.9+ 
- **数据库**: PostgreSQL
- **缓存**: Redis 7.x
- **设计模式**：DDD领域驱动 

#### 前端技术栈

- **框架**: React 18.x 
- **元框架**: Next.js 14.x (App Router)
- **语言**: TypeScript 5.x
- **状态管理**: Zustand 或 React Query
- **UI组件库**: Ant Design 5.x 或 MUI 
- **构建工具**: Vite (Next.js内置)