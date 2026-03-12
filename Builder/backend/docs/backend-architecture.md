# ProjectHub 后端架构设计文档

## 1. 技术选型

| 技术 | 选型 | 版本 | 说明 |
|------|------|------|------|
| JDK | OpenJDK | 21 (LTS) | Java 21 长期支持版 |
| 框架 | Spring Boot | 3.2.x | 最新 LTS 版本 |
| 构建工具 | Maven | 3.9+ | 依赖管理和构建 |
| 数据库 | PostgreSQL | 16.x | 关系型数据库 |
| 缓存 | Redis | latest | 内存数据库 |
| ORM | Spring Data JPA | latest | 数据访问层 |
| 认证 | Spring Security + JWT | latest | 安全认证 |
| API 文档 | SpringDoc OpenAPI | 2.x | Swagger UI |
| 连接池 | HikariCP | 5.x | 高性能连接池 |
| 数据库迁移 | Flyway | 9.x | 版本控制 |

## 2. 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                      Client (Frontend)                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     API Gateway / Nginx                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Spring Boot Application                    │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Controller Layer (API 接口)                             │ │
│  │  - AuthController    - ProjectController                │ │
│  │  - UserController    - TaskController                   │ │
│  └─────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Service Layer (业务逻辑)                                │ │
│  │  - AuthService       - ProjectService                   │ │
│  │  - UserService       - TaskService                      │ │
│  └─────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Repository Layer (数据访问)                             │ │
│  │  - UserRepository    - ProjectRepository                │ │
│  │  - TaskRepository    - ...                              │ │
│  └─────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Security Filter Chain                                   │ │
│  │  - JwtAuthenticationFilter                               │ │
│  │  - SecurityContextHolderFilter                           │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
           │                                    │
           ▼                                    ▼
┌─────────────────────┐            ┌─────────────────────┐
│    PostgreSQL       │            │       Redis         │
│   (主数据存储)       │            │   (缓存/Session)    │
└─────────────────────┘            └─────────────────────┘
```

## 3. 项目目录结构

```
src/main/java/com/projecthub/
├── ProjectHubApplication.java        # 启动类
├── common/                           # 公共模块
│   ├── config/                       # 配置类
│   │   ├── SecurityConfig.java       # Spring Security 配置
│   │   ├── RedisConfig.java          # Redis 配置
│   │   └── SwaggerConfig.java        # OpenAPI 配置
│   ├── constant/                     # 常量定义
│   │   └── ErrorCode.java            # 错误码枚举
│   ├── exception/                    # 异常处理
│   │   ├── BusinessException.java    # 业务异常
│   │   └── GlobalExceptionHandler.java
│   ├── response/                     # 统一响应
│   │   └── Result.java               # 统一返回格式
│   ├── util/                         # 工具类
│   │   ├── JwtUtil.java              # JWT 工具
│   │   └── PasswordUtil.java         # 密码加密工具
│   └── aspect/                       # AOP 切面
│       └── LogAspect.java            # 操作日志切面
├── module/                           # 业务模块
│   ├── auth/                         # 认证模块
│   │   ├── controller/
│   │   ├── service/
│   │   └── dto/
│   ├── user/                         # 用户模块
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   ├── project/                      # 项目模块
│   ├── task/                         # 任务模块
│   ├── story/                        # 用户故事模块
│   ├── issue/                        # 问题追踪模块
│   ├── wiki/                         # Wiki 模块
│   ├── report/                       # 报表模块
│   └── notification/                 # 通知模块
├── security/                         # 安全相关
│   ├── JwtAuthenticationFilter.java  # JWT 过滤器
│   ├── UserDetailsImpl.java          # UserDetailsService 实现
│   └── WebSecurityConfig.java        # Web 安全配置
└── infrastructure/                   # 基础设施
    ├── cache/                        # 缓存服务
    └── storage/                      # 对象存储

src/main/resources/
├── application.yml                   # 主配置文件
├── application-dev.yml               # 开发环境配置
├── application-prod.yml              # 生产环境配置
└── db/migration/                     # Flyway 迁移脚本
    ├── V1__init_schema.sql           # 初始化表结构
    └── V2__initial_data.sql          # 初始数据
```

## 4. DDD 领域驱动设计

### 分层架构

```
┌─────────────────────────────────────┐
│         Interface Layer             │  ← Controller, DTO
├─────────────────────────────────────┤
│         Application Layer           │  ← Service, Application Service
├─────────────────────────────────────┤
│          Domain Layer               │  ← Entity, Value Object, Domain Service
├─────────────────────────────────────┤
│      Infrastructure Layer           │  ← Repository Impl, DB, External Service
└─────────────────────────────────────┘
```

### 领域模型

| 聚合根 | 实体 | 值对象 |
|--------|------|--------|
| User | - | Email, Password |
| Project | ProjectMember | ProjectRole |
| Task | SubTask, Comment | TaskStatus, Priority |
| Epic | UserStory | StoryStatus |
| Issue | - | IssueType, Severity |
| WikiDocument | WikiHistory | - |

## 5. API 设计原则

### RESTful 规范

| HTTP 方法 | 路径 | 说明 |
|----------|------|------|
| GET | /api/v1/projects | 获取项目列表 |
| POST | /api/v1/projects | 创建项目 |
| GET | /api/v1/projects/{id} | 获取项目详情 |
| PUT | /api/v1/projects/{id} | 更新项目 |
| DELETE | /api/v1/projects/{id} | 删除项目 |

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1234567890
}
```

### 错误码规范

| 范围 | 模块 |
|------|------|
| 1000-1999 | 通用错误 |
| 2000-2999 | 认证模块 |
| 3000-3999 | 用户模块 |
| 4000-4999 | 项目模块 |
| 5000-5999 | 任务模块 |
| 6000-6999 | 用户故事模块 |
| 7000-7999 | 问题追踪模块 |
| 8000-8999 | Wiki 模块 |

## 6. 安全设计

### 认证流程

```
1. 用户登录 (POST /api/v1/auth/login)
   ↓
2. 验证用户名密码
   ↓
3. 生成 JWT Token
   ↓
4. 返回 Token 给客户端
   ↓
5. 客户端携带 Token 访问 API (Authorization: Bearer <token>)
   ↓
6. JwtAuthenticationFilter 验证 Token
   ↓
7. 访问受保护的资源
```

### JWT Token 结构

| 字段 | 说明 | 有效期 |
|------|------|--------|
| Access Token | 访问令牌 | 2 小时 |
| Refresh Token | 刷新令牌 | 7 天 |

### 密码安全

- 使用 BCrypt 加密算法
- Salt rounds: 10
- 密码长度要求：8-20 位

## 7. 缓存策略

### Redis 缓存设计

| 缓存 Key | 数据类型 | 过期时间 | 说明 |
|----------|----------|----------|------|
| user:{id} | Hash | 30min | 用户信息缓存 |
| project:{id} | Hash | 30min | 项目信息缓存 |
| task:{id} | Hash | 30min | 任务信息缓存 |
| session:{token} | String | 2h | Session 缓存 |

### 缓存更新策略

- **Cache-Aside**: 读取时先查缓存，未命中则查数据库并回写缓存
- **主动失效**: 数据更新时主动删除缓存

## 8. 数据库设计

详见：[数据库设计文档](database/schema.md)

### 核心设计原则

1. 所有表都有 `created_at` 和 `updated_at` 字段
2. 核心业务表支持软删除 (`deleted_at`)
3. 外键约束保证数据一致性
4. 合理索引优化查询性能

## 9. 日志规范

### 日志级别

| 级别 | 说明 | 使用场景 |
|------|------|----------|
| ERROR | 错误 | 系统异常、业务异常 |
| WARN | 警告 | 可恢复的异常 |
| INFO | 信息 | 关键业务流程 |
| DEBUG | 调试 | 开发调试信息 |

### 日志格式

```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

## 10. 测试策略

| 测试类型 | 工具 | 覆盖率要求 |
|----------|------|------------|
| 单元测试 | JUnit 5 + Mockito | ≥80% |
| 集成测试 | Testcontainers | 核心流程 |
| API 测试 | MockMvc | 所有接口 |

## 11. 部署架构

### Docker 部署

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=postgres
      - REDIS_HOST=redis
    depends_on:
      - postgres
      - redis

  postgres:
    image: postgres:16
    environment:
      - POSTGRES_PASSWORD=xxx

  redis:
    image: redis:latest
```

### 健康检查

- `/actuator/health` - Spring Boot Actuator 健康检查
- `/actuator/health/liveness` - 存活探针
- `/actuator/health/readiness` - 就绪探针

## 12. 性能优化

### 数据库优化

1. 合理使用索引
2. 避免 N+1 查询
3. 使用连接池 (HikariCP)
4. 分页查询

### 缓存优化

1. 热点数据缓存
2. 多级缓存策略
3. 缓存预热

### API 优化

1. 分页响应
2. 字段过滤
3. 批量接口
