## ADDED Requirements

### Requirement: API Gateway 路由转发
系统 SHALL 通过 API Gateway 将客户端请求路由到对应的后端微服务。

#### Scenario: 路由到认证服务
- **WHEN** 客户端发送请求到 `/api/v1/auth/**`
- **THEN** 系统将请求路由到 auth-service

#### Scenario: 路由到用户服务
- **WHEN** 客户端发送请求到 `/api/v1/users/**`
- **THEN** 系统将请求路由到 user-service

#### Scenario: 路由到项目服务
- **WHEN** 客户端发送请求到 `/api/v1/projects/**`
- **THEN** 系统将请求路由到 project-service

### Requirement: 统一认证入口
API Gateway SHALL 验证所有受保护接口的 JWT Token，并注入用户信息到请求头。

#### Scenario: 有效 Token 验证
- **WHEN** 请求携带有效的 JWT Token
- **THEN** 系统解析 Token 并将用户 ID、角色、用户名注入请求头

#### Scenario: 无效 Token 拒绝
- **WHEN** 请求携带无效或过期的 JWT Token
- **THEN** 系统返回 401 Unauthorized 错误

#### Scenario: 公开路径放行
- **WHEN** 请求路径为 `/api/v1/auth/login` 或 `/api/v1/auth/register`
- **THEN** 系统跳过认证，直接转发请求

### Requirement: 请求限流
API Gateway SHALL 支持基于 IP 和用户的请求限流。

#### Scenario: IP 限流触发
- **WHEN** 同一 IP 在 1 秒内发送超过 100 个请求
- **THEN** 系统返回 429 Too Many Requests 错误

#### Scenario: 用户限流触发
- **WHEN** 同一用户在 1 分钟内发送超过 1000 个请求
- **THEN** 系统返回 429 Too Many Requests 错误

### Requirement: 请求日志
API Gateway SHALL 记录所有请求的访问日志，包含请求 ID、路径、耗时、状态码。

#### Scenario: 记录成功请求
- **WHEN** 请求处理完成
- **THEN** 系统记录请求 ID、路径、方法、耗时、状态码

#### Scenario: 请求 ID 传递
- **WHEN** 请求包含 X-Request-Id 头
- **THEN** 系统使用该 ID 并传递给下游服务

#### Scenario: 自动生成请求 ID
- **WHEN** 请求不包含 X-Request-Id 头
- **THEN** 系统自动生成唯一请求 ID

### Requirement: 熔断降级
API Gateway SHALL 在下游服务不可用时返回友好的降级响应。

#### Scenario: 服务熔断
- **WHEN** 下游服务连续失败 5 次
- **THEN** 系统触发熔断，后续请求直接返回降级响应

#### Scenario: 熔断恢复
- **WHEN** 熔断服务在 30 秒后恢复正常
- **THEN** 系统逐步放行请求进行探测

### Requirement: CORS 配置
API Gateway SHALL 支持跨域请求配置。

#### Scenario: 预检请求处理
- **WHEN** 收到 OPTIONS 预检请求
- **THEN** 系统返回允许的源、方法、头信息

#### Scenario: 跨域请求放行
- **WHEN** 请求来源在允许列表中
- **THEN** 系统添加 CORS 头并放行请求

### Requirement: 灰度路由
API Gateway SHALL 支持基于请求特征的灰度路由。

#### Scenario: 基于 Header 灰度
- **WHEN** 请求包含 `X-Gray-Version: v2` Header
- **THEN** 系统将请求路由到灰度版本服务实例

#### Scenario: 基于用户 ID 灰度
- **WHEN** 配置灰度用户白名单
- **THEN** 白名单用户请求路由到灰度服务

#### Scenario: 灰度比例控制
- **WHEN** 配置灰度流量比例（如 10%）
- **THEN** 系统按比例将流量分发到灰度实例

#### Scenario: 灰度回滚
- **WHEN** 灰度服务出现异常
- **THEN** 系统自动将流量切回稳定版本

### Requirement: 请求超时配置
API Gateway SHALL 支持不同接口的超时配置。

#### Scenario: 默认超时设置
- **WHEN** 未配置特定接口超时
- **THEN** 系统使用默认超时时间 30 秒

#### Scenario: 接口级超时配置
- **WHEN** 配置特定接口超时时间（如报表接口 60 秒）
- **THEN** 系统按配置时间等待响应

#### Scenario: 超时响应处理
- **WHEN** 请求超时
- **THEN** 系统返回 504 Gateway Timeout 错误并记录日志

### Requirement: 响应压缩
API Gateway SHALL 支持响应数据压缩。

#### Scenario: Gzip 压缩
- **WHEN** 响应体大于 1KB 且客户端支持 Gzip
- **THEN** 系统压缩响应数据

#### Scenario: 压缩类型配置
- **WHEN** 配置压缩的 MIME 类型
- **THEN** 系统对指定类型（application/json、text/html）启用压缩

#### Scenario: 压缩级别配置
- **WHEN** 配置压缩级别（1-9）
- **THEN** 系统按配置级别平衡压缩率和性能