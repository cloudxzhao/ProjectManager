## ADDED Requirements

### Requirement: 服务间认证
服务间调用 SHALL 通过 JWT Token 进行身份验证。

#### Scenario: 内部服务 Token 传递
- **WHEN** 服务 A 调用服务 B
- **THEN** 请求携带 Gateway 注入的 JWT Token

#### Scenario: 服务身份验证
- **WHEN** 服务收到内部调用请求
- **THEN** 系统验证 Token 有效性并提取调用者身份

#### Scenario: 过期 Token 处理
- **WHEN** 服务间 Token 过期
- **THEN** 系统返回 401 错误，调用方刷新 Token 后重试

### Requirement: 内部服务白名单
系统 SHALL 支持配置内部服务访问白名单。

#### Scenario: 白名单配置
- **WHEN** 管理员配置服务白名单
- **THEN** 只有白名单内的服务可以相互调用

#### Scenario: 非白名单调用拒绝
- **WHEN** 非白名单服务尝试调用内部接口
- **THEN** 系统拒绝请求并记录安全日志

#### Scenario: 动态白名单更新
- **WHEN** 白名单配置变更
- **THEN** 系统实时生效，无需重启服务

### Requirement: 敏感接口保护
系统 SHALL 对敏感接口实施额外的访问控制。

#### Scenario: 敏感接口标记
- **WHEN** 接口标记为敏感（如删除用户、修改权限）
- **THEN** 系统要求额外的权限验证

#### Scenario: 操作审计日志
- **WHEN** 调用敏感接口
- **THEN** 系统记录操作者、时间、IP、参数等审计信息

#### Scenario: 敏感操作二次确认
- **WHEN** 执行高风险操作（如批量删除）
- **THEN** 系统要求额外的确认 Token

### Requirement: API 网关边界安全
API Gateway SHALL 作为安全边界保护内部服务。

#### Scenario: 外部请求必须经过 Gateway
- **WHEN** 外部请求直接访问服务端口
- **THEN** 系统拒绝请求（服务只监听内网）

#### Scenario: 内网隔离
- **WHEN** 部署服务时
- **THEN** 业务服务只暴露内网 IP，仅 Gateway 暴露公网

#### Scenario: DDoS 防护
- **WHEN** Gateway 检测到异常流量
- **THEN** 系统触发限流并通知运维

### Requirement: 服务间通信加密
服务间敏感数据传输 SHALL 支持加密。

#### Scenario: 敏感字段加密
- **WHEN** 传输包含敏感数据（如密码、身份证）
- **THEN** 系统对敏感字段加密传输

#### Scenario: 加密算法配置
- **WHEN** 配置加密策略
- **THEN** 系统支持 AES-256 等标准加密算法

#### Scenario: 密钥轮换
- **WHEN** 达到密钥轮换周期
- **THEN** 系统支持平滑切换新密钥

### Requirement: 访问令牌管理
系统 SHALL 管理服务间访问令牌的生命周期。

#### Scenario: 令牌颁发
- **WHEN** 服务启动时
- **THEN** 系统从 Auth Service 获取服务访问令牌

#### Scenario: 令牌刷新
- **WHEN** 令牌即将过期（剩余时间 < 1/3 有效期）
- **THEN** 系统自动刷新令牌

#### Scenario: 令牌撤销
- **WHEN** 检测到安全事件
- **THEN** 管理员可撤销指定服务的令牌

### Requirement: 安全审计
系统 SHALL 记录服务间调用的安全审计日志。

#### Scenario: 调用链记录
- **WHEN** 服务间发生调用
- **THEN** 系统记录调用方、被调用方、接口、时间、结果

#### Scenario: 异常调用告警
- **WHEN** 检测到异常调用模式（如频繁调用敏感接口）
- **THEN** 系统触发安全告警

#### Scenario: 审计日志查询
- **WHEN** 安全审计需要
- **THEN** 系统支持按时间、服务、接口查询审计日志