## Why

当前 ProjectHub 采用单体架构（Spring Boot + Next.js），随着业务增长和团队规模扩大，面临以下挑战：

1. **部署效率低** - 任何模块改动都需要重新部署整个后端服务
2. **扩展受限** - 任务和 Wiki 等高负载模块无法独立水平扩展
3. **故障隔离差** - 单个模块故障可能影响整个系统可用性
4. **技术演进困难** - 不同模块适合不同技术栈（如 Wiki 适合 Node.js，报表适合 Python）

迁移到微服务架构可以解决这些问题，提升系统的可维护性、可扩展性和团队开发效率。

## What Changes

### 架构变更
- **BREAKING**: 将单体 Spring Boot 应用拆分为多个独立微服务
- 引入 API Gateway 作为统一入口
- 引入服务注册与发现（Nacos）
- 引入分布式配置中心（Nacos）
- 引入消息队列实现异步通信（RabbitMQ）

### 服务拆分
- 拆分为 8 个核心微服务：
  - `auth-service` - 认证授权服务
  - `user-service` - 用户管理服务
  - `project-service` - 项目管理服务
  - `task-service` - 任务管理服务
  - `story-service` - 用户故事服务
  - `issue-service` - 问题追踪服务
  - `wiki-service` - 知识库服务
  - `notification-service` - 通知服务

### 基础设施变更
- **BREAKING**: 数据库拆分（每个服务独立数据库）
- 引入分布式事务解决方案（Seata AT 模式）
- 引入链路追踪（SkyWalking 8.x）
- 引入集中式日志（ELK Stack）

## Capabilities

### New Capabilities

- `api-gateway`: 统一 API 网关，负责路由、限流、认证、日志、灰度路由等
- `service-discovery`: 服务注册与发现机制
- `distributed-config`: 分布式配置管理
- `distributed-tracing`: 分布式链路追踪
- `event-bus`: 事件驱动通信机制
- `circuit-breaker`: 熔断降级机制
- `database-migration`: 数据库拆分与迁移
- `service-security`: 服务间安全通信
- `distributed-transaction`: 分布式事务处理

### Modified Capabilities

- `auth-module`: 认证模块需要改造为独立服务，支持 OAuth2.0 和 JWT 跨服务验证
- `project-module`: 项目模块需要拆分核心业务，与任务服务解耦
- `task-module`: 任务模块需要支持事件驱动，异步处理通知
- `notification-module`: 通知模块需要改为消息队列消费者模式

## Impact

### 代码影响
| 区域 | 影响程度 | 说明 |
|------|----------|------|
| Builder/backend | **重大** | 完整重构，按服务拆分 |
| Builder/frontend | 中等 | API 调整，需适配 Gateway |
| 数据库 | **重大** | 拆分为多数据库 |
| 运维脚本 | **重大** | Docker Compose/K8s 配置重写 |

### 依赖变更
- 新增：Spring Cloud Alibaba
- 新增：Nacos（注册中心+配置中心）
- 新增：Seata（分布式事务）
- 新增：RabbitMQ（消息队列）
- 新增：Redis（分布式缓存、分布式锁）
- 新增：SkyWalking（链路追踪）

### 团队影响
- 开发团队需要学习微服务架构模式
- 运维团队需要掌握容器化部署
- 测试团队需要适应服务间集成测试

### 迁移风险
- 数据迁移复杂度高（需双写双读过渡）
- 分布式事务处理难度增加
- 运维复杂度提升
- 初期开发效率可能下降（学习曲线）

## 数据迁移方案

### 迁移策略
采用**双写双读**渐进式迁移策略，确保业务连续性：

```
阶段1: 双写阶段              阶段2: 验证阶段              阶段3: 切换阶段
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│    Application  │         │    Application  │         │    Application  │
│        │        │         │        │        │         │        │        │
│   ┌────┴────┐   │         │   ┌────┴────┐   │         │        │        │
│   ▼         ▼   │         │   ▼         ▼   │         │        ▼        │
│ 旧DB      新DB  │ ──────▶ │ 旧DB      新DB  │ ──────▶ │       新DB      │
│ (主)      (备)  │         │ (备)      (主)  │         │       (主)      │
└─────────────────┘         └─────────────────┘         └─────────────────┘
     写入两边                 读新DB，校验差异             只读写新DB
```

### 拆表规则
| 服务 | 源表 | 目标数据库 | 拆分说明 |
|------|------|------------|----------|
| auth-service | users, tokens | auth_db | 用户认证相关表 |
| user-service | users, profiles | user_db | 用户信息表（与 auth 共享 ID） |
| project-service | projects, project_members | project_db | 项目和成员关系表 |
| task-service | tasks, comments | task_db | 任务和评论表 |
| story-service | epics, user_stories | story_db | 史诗和故事表 |
| issue-service | issues | issue_db | 问题追踪表 |
| wiki-service | wiki_documents, wiki_history | wiki_db | 文档和版本历史表 |
| notification-service | notifications | notify_db | 通知表 |

### 数据同步延迟目标
- 用户信息变更：1 秒内同步到缓存
- 事件消息投递：100ms 内到达消费者

## 服务间安全方案

### 安全架构
```
┌──────────────────────────────────────────────────────────────┐
│                        外部网络                               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                    API Gateway                          │ │
│  │           - JWT 验证                                     │ │
│  │           - 用户信息注入                                 │ │
│  │           - 限流防护                                     │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────────┬───────────────────────────────┘
                               │ 内部信任网络
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
        ▼                      ▼                      ▼
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│ Auth Service  │     │ User Service  │     │Project Service│
│               │     │               │     │               │
│ - 服务Token   │     │ - 服务Token   │     │ - 服务Token   │
└───────────────┘     └───────────────┘     └───────────────┘
```

### 安全措施
| 措施 | 说明 |
|------|------|
| 边界防护 | 仅 Gateway 暴露公网，业务服务仅监听内网 |
| JWT 传递 | Gateway 验证 JWT，用户信息注入 Header 传递给下游 |
| 服务白名单 | 配置服务间调用白名单，非白名单服务不可互相调用 |
| 敏感接口审计 | 删除、权限变更等敏感操作记录审计日志 |
| 敏感字段加密 | 密码、身份证等敏感数据传输时加密 |

## 容量规划

### 初始资源配置
| 组件 | CPU | 内存 | 实例数 | 说明 |
|------|-----|------|--------|------|
| API Gateway | 4 核 | 8 GB | 2 | 接入层，流量入口 |
| Nacos | 2 核 | 4 GB | 3 | 集群模式 |
| RabbitMQ | 2 核 | 4 GB | 2 | 镜像队列模式 |
| Redis | 2 核 | 4 GB | 2 | 主从模式 |
| SkyWalking OAP | 2 核 | 4 GB | 1 | 链路追踪 |
| Auth Service | 2 核 | 4 GB | 2 | 认证服务 |
| User Service | 2 核 | 4 GB | 2 | 用户服务 |
| Project Service | 2 核 | 4 GB | 2 | 项目服务 |
| Task Service | 4 核 | 8 GB | 2 | 任务服务（高负载） |
| Story Service | 2 核 | 4 GB | 2 | 故事服务 |
| Issue Service | 2 核 | 4 GB | 2 | 问题服务 |
| Wiki Service | 2 核 | 4 GB | 2 | 知识库服务 |
| Notification Service | 2 核 | 4 GB | 2 | 通知服务 |
| PostgreSQL | 4 核 | 16 GB | 8 | 每服务一个实例 |

**总计**：约 50 核 CPU、100 GB 内存

### 扩容策略
- Task Service、Wiki Service 支持水平扩展（无状态）
- 数据库支持读写分离和分库分表（后期）

## 分布式事务场景

### 使用 Seata AT 模式的场景
| 场景 | 参与服务 | 说明 |
|------|----------|------|
| 项目删除 | project-service, task-service, story-service, issue-service | 级联删除项目下所有数据 |
| 任务转移 | task-service, project-service | 更新任务归属和项目统计 |
| 用户注销 | auth-service, user-service, project-service | 清理用户所有关联数据 |

### 最终一致性场景
| 场景 | 实现方式 | 说明 |
|------|----------|------|
| 任务分配通知 | RabbitMQ 事件 | 任务分配后异步发送通知 |
| 项目成员变更 | RabbitMQ 事件 | 成员变更后更新缓存 |
| 文档更新通知 | RabbitMQ 事件 | 文档更新后通知相关人员 |

## 监控 SLI/SLO

### 服务水平指标（SLI）
| 指标 | 目标值（SLO） | 测量方式 |
|------|---------------|----------|
| 可用性 | ≥ 99.9% | 成功请求 / 总请求 |
| P95 响应时间 | < 500ms | SkyWalking 统计 |
| P99 响应时间 | < 1000ms | SkyWalking 统计 |
| 错误率 | < 0.1% | 错误请求 / 总请求 |
| 事件投递延迟 | < 100ms | RabbitMQ 监控 |

### 告警规则
| 规则 | 阈值 | 级别 |
|------|------|------|
| 服务不可用 | 可用性 < 99% | P0 |
| 响应时间超标 | P95 > 1000ms 持续 5 分钟 | P1 |
| 错误率过高 | 错误率 > 1% 持续 3 分钟 | P1 |
| 消息积压 | 队列消息 > 10000 | P2 |

## Timeline Estimate

| 阶段 | 内容 | 预估周期 |
|------|------|----------|
| Phase 1 | 基础设施搭建（Gateway、Nacos、消息队列） | 2 周 |
| Phase 2 | 核心服务拆分（auth、user、project） | 3 周 |
| Phase 3 | 业务服务拆分（task、story、issue、wiki） | 3 周 |
| Phase 4 | 通知服务、监控、链路追踪 | 2 周 |
| Phase 5 | 测试、优化、文档 | 2 周 |

**总计：约 12 周**

## 第三期规划（本期不涉及）

- Kubernetes 容器编排
- Service Mesh（Istio/Linkerd）
- 多数据中心部署
- GitOps 持续部署