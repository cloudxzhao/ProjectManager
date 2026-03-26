## ADDED Requirements

### Requirement: 服务注册
每个微服务 SHALL 在启动时向注册中心注册自身信息。

#### Scenario: 服务启动注册
- **WHEN** 微服务启动完成
- **THEN** 系统将服务名称、IP、端口注册到 Nacos

#### Scenario: 注册信息包含元数据
- **WHEN** 服务注册时
- **THEN** 注册信息包含服务名、IP、端口、版本、权重

#### Scenario: 服务停止注销
- **WHEN** 微服务正常停止
- **THEN** 系统从注册中心移除服务实例

### Requirement: 服务发现
微服务 SHALL 能够通过服务名发现其他服务的实例。

#### Scenario: 根据服务名获取实例
- **WHEN** 服务 A 需要调用服务 B
- **THEN** 系统根据服务名 B 返回可用的实例列表

#### Scenario: 实例列表更新
- **WHEN** 目标服务实例发生变化
- **THEN** 系统在 10 秒内更新本地实例缓存

#### Scenario: 无可用实例处理
- **WHEN** 目标服务没有可用实例
- **THEN** 系统返回服务不可用错误

### Requirement: 健康检查
注册中心 SHALL 定期检查服务实例的健康状态。

#### Scenario: 主动健康检查
- **WHEN** 服务实例超过 15 秒未发送心跳
- **THEN** 系统标记该实例为不健康

#### Scenario: 实例自动摘除
- **WHEN** 服务实例超过 30 秒未发送心跳
- **THEN** 系统从服务列表中移除该实例

#### Scenario: 实例恢复
- **WHEN** 被摘除的实例恢复并发送心跳
- **THEN** 系统重新将其加入服务列表

### Requirement: 负载均衡
服务调用 SHALL 支持多种负载均衡策略。

#### Scenario: 轮询负载均衡
- **WHEN** 默认情况下调用服务
- **THEN** 系统按轮询方式选择实例

#### Scenario: 加权负载均衡
- **WHEN** 服务实例配置了不同权重
- **THEN** 系统按权重比例分配流量

#### Scenario: 一致性哈希
- **WHEN** 配置使用一致性哈希策略
- **THEN** 相同 key 的请求路由到同一实例

### Requirement: 注册中心高可用
注册中心 SHALL 支持集群部署，保证高可用。

#### Scenario: 集群节点同步
- **WHEN** 任一节点收到服务注册信息
- **THEN** 信息同步到所有集群节点

#### Scenario: 节点故障转移
- **WHEN** 某个注册中心节点宕机
- **THEN** 服务自动连接其他健康节点

#### Scenario: 数据持久化
- **WHEN** 注册中心重启
- **THEN** 服务注册信息从持久化存储恢复