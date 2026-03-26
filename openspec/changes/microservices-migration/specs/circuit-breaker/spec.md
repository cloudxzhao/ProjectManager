## ADDED Requirements

### Requirement: 熔断保护
系统 SHALL 在下游服务故障时触发熔断，防止故障蔓延。

#### Scenario: 熔断触发条件
- **WHEN** 下游服务错误率超过 50%（最近 10 次调用）
- **THEN** 系统触发熔断，后续请求直接返回降级响应

#### Scenario: 熔断状态切换
- **WHEN** 熔断器状态变为 OPEN
- **THEN** 系统在指定时间窗口后进入 HALF-OPEN 状态进行探测

#### Scenario: 熔断恢复
- **WHEN** HALF-OPEN 状态下探测请求成功
- **THEN** 熔断器状态变为 CLOSED，恢复正常调用

#### Scenario: 熔断器强制打开
- **WHEN** 管理员手动强制打开熔断器
- **THEN** 系统拒绝所有请求直到手动关闭

### Requirement: 降级处理
系统 SHALL 在服务不可用时提供降级响应。

#### Scenario: 返回默认值
- **WHEN** 服务调用失败触发降级
- **THEN** 系统返回预定义的默认值或空数据

#### Scenario: 返回缓存数据
- **WHEN** 服务不可用但存在缓存数据
- **THEN** 系统返回缓存的旧数据

#### Scenario: 友好提示
- **WHEN** 用户请求被降级
- **THEN** 系统返回友好的提示信息

### Requirement: 限流保护
系统 SHALL 支持多种限流策略保护服务。

#### Scenario: QPS 限流
- **WHEN** 请求 QPS 超过配置阈值
- **THEN** 系统拒绝超出的请求

#### Scenario: 并发线程数限流
- **WHEN** 并发处理线程数超过阈值
- **THEN** 系统拒绝新请求

#### Scenario: 关联限流
- **WHEN** 关联资源达到阈值
- **THEN** 系统限制当前资源的访问

### Requirement: 热点参数限流
系统 SHALL 支持基于热点参数的限流。

#### Scenario: 参数级限流
- **WHEN** 某个参数值（如商品 ID）的访问频率过高
- **THEN** 系统限制该参数值的访问

#### Scenario: 热点参数统计
- **WHEN** 系统运行时
- **THEN** 系统统计访问频率最高的参数值

#### Scenario: 参数例外项
- **WHEN** 配置参数例外项
- **THEN** 特定参数值不受限流影响

### Requirement: 系统自适应保护
系统 SHALL 根据整体负载自动调节流量。

#### Scenario: 系统负载过高保护
- **WHEN** 系统 CPU 使用率超过阈值
- **THEN** 系统自动限制入口流量

#### Scenario: 平均响应时间保护
- **WHEN** 平均响应时间超过阈值
- **THEN** 系统限制并发请求

#### Scenario: 入口流量控制
- **WHEN** 系统接近过载
- **THEN** 系统按优先级拒绝低优先级请求