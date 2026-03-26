# API Gateway 配置说明

## 概述

API Gateway 基于 Spring Cloud Gateway 构建，作为所有微服务的统一入口，提供以下核心功能：

- **路由转发**：将请求路由到对应的后端微服务
- **JWT 认证**：验证请求 Token 并注入用户信息
- **限流熔断**：基于 Sentinel 实现限流和熔断降级
- **请求日志**：记录所有请求的详细信息
- **灰度路由**：支持基于 Header 和用户 ID 的灰度发布
- **CORS 跨域**：全局跨域请求支持
- **响应压缩**：Gzip 压缩响应数据

## 配置说明

### 1. 路由配置 (application.yaml)

所有服务路由在 `application.yaml` 中配置：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/v1/auth/**
```

**路由列表：**

| 服务 | 路径 | 说明 |
|------|------|------|
| auth-service | /api/v1/auth/** | 认证服务 |
| user-service | /api/v1/users/** | 用户服务 |
| project-service | /api/v1/projects/** | 项目服务 |
| task-service | /api/v1/tasks/** | 任务服务 |
| story-service | /api/v1/stories/**, /api/v1/epics/** | 故事服务 |
| issue-service | /api/v1/issues/** | 问题服务 |
| wiki-service | /api/v1/wiki/** | Wiki 服务 |
| notification-service | /api/v1/notifications/**, /api/v1/messages/** | 通知服务 |

### 2. JWT 认证配置

公开路径（无需认证）：

```yaml
security:
  ignore:
    urls:
      - /api/v1/auth/login
      - /api/v1/auth/register
      - /api/v1/auth/refresh
      - /api/v1/auth/forgot-password
      - /api/v1/auth/reset-password
      - /actuator/**
      - /swagger-ui/**
      - /v3/api-docs/**
```

认证过滤器会自动：
- 验证 JWT Token 有效性
- 解析用户 ID、用户名、角色
- 注入到请求头：`X-User-Id`, `X-User-Name`, `X-User-Role`

### 3. Sentinel 限流熔断配置

#### 3.1 默认限流规则

```yaml
sentinel:
  default-flow-count: 100      # 每秒 100 个请求
  default-interval-sec: 1      # 统计窗口 1 秒
  default-burst: 200           # 突发流量 200
  default-control-behavior: 0  # 快速失败
```

#### 3.2 熔断降级规则

```yaml
sentinel:
  circuit-breaker:
    min-request-amount: 10     # 最小请求数
    slow-ratio-threshold: 0.8  # 慢调用比例 80%
    slow-rt-ms: 5000           # 慢调用 RT 5 秒
    error-ratio-threshold: 0.5 # 异常比例 50%
    error-amount: 10           # 异常数 10
    sleep-window-ms: 30000     # 熔断时长 30 秒
```

#### 3.3 Nacos 动态配置

限流规则可通过 Nacos 配置中心动态调整：

- **Data ID**: `gateway-service-sentinel.yaml`
- **Group**: `DEFAULT_GROUP`

### 4. 超时配置

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 5000  # 连接超时 5 秒
        response-timeout: 30s  # 响应超时 30 秒
```

### 5. 响应压缩配置

```yaml
server:
  compression:
    enabled: true
    min-response-size: 1KB
    mime-types: application/json,text/html,text/plain,text/css,application/javascript,text/xml,application/xml
```

### 6. 灰度路由

灰度路由支持三种方式：

#### 6.1 Header 灰度

请求携带 Header：
```
X-Gray-Version: v2
```

#### 6.2 用户 ID 灰度

配置白名单用户，自动路由到灰度版本。

#### 6.3 比例灰度

通过 Nacos 配置流量比例分发。

## 过滤器执行顺序

| 顺序 | 过滤器 | Order |
|------|--------|-------|
| 1 | RequestLogFilter | -200 |
| 2 | JwtAuthenticationFilter | -100 |
| 3 | GrayRouteFilter | -50 |
| 4 | SentinelGatewayFilter | -1 |

## 响应格式

### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 认证失败 (401)

```json
{
  "code": 401,
  "message": "Token 无效或已过期",
  "data": null
}
```

### 限流响应 (429)

```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后再试",
  "data": null
}
```

### 熔断降级 (503)

```json
{
  "code": 503,
  "message": "服务暂时不可用，请稍后再试",
  "data": null
}
```

## 日志格式

请求日志：
```
[RequestId] >>> METHOD /path from 192.168.1.1 userId=123
```

响应日志：
```
[RequestId] <<< METHOD /path - 200 (150ms)
```

## 监控端点

- **健康检查**: `GET /actuator/health`
- **网关信息**: `GET /actuator/gateway`
- **Gateway Routes**: `GET /actuator/gateway/routes`

## 部署配置

Gateway 服务注册到 Nacos，服务名为 `gateway-service`，默认端口 `8080`。

### Docker 部署

```bash
docker run -d \
  -p 8080:8080 \
  -e NACOS_SERVER=nacos:8848 \
  -e JWT_SECRET=your-secret-key \
  --name gateway \
  gateway-service:1.0.0
```

## 相关文档

- [OpenSpec API Gateway 需求规格](../../../openspec/changes/microservices-migration/specs/api-gateway/spec.md)
- [微服务架构设计](../../../openspec/changes/microservices-migration/design.md)
