## ADDED Requirements

### Requirement: 消息发布
服务 SHALL 能够将事件发布到消息队列。

#### Scenario: 事件发布成功
- **WHEN** 服务发布事件到指定交换机
- **THEN** 系统将事件路由到绑定该交换机的队列

#### Scenario: 事件持久化
- **WHEN** 事件发布时标记为持久化
- **THEN** 系统将事件写入磁盘保证可靠性

#### Scenario: 发布确认
- **WHEN** 事件发布成功
- **THEN** 系统返回确认消息给发布者

### Requirement: 消息消费
服务 SHALL 能够从消息队列订阅和消费事件。

#### Scenario: 事件订阅
- **WHEN** 服务订阅某个队列
- **THEN** 系统将队列中的事件推送给消费者

#### Scenario: 消费确认
- **WHEN** 消费者成功处理事件
- **THEN** 系统从队列中移除该事件

#### Scenario: 消费失败重试
- **WHEN** 消费者处理事件失败
- **THEN** 系统将事件重新入队或转入死信队列

### Requirement: 事件类型定义
系统 SHALL 定义标准的事件格式和类型。

#### Scenario: 事件结构规范
- **WHEN** 服务发布事件
- **THEN** 事件包含 eventId、eventType、timestamp、source、data 字段

#### Scenario: 事件类型注册
- **WHEN** 新事件类型定义后
- **THEN** 系统在事件注册中心登记事件元数据

#### Scenario: 事件版本管理
- **WHEN** 事件结构变更
- **THEN** 系统支持多版本事件共存

### Requirement: 消息可靠性保证
系统 SHALL 保证消息的可靠传输。

#### Scenario: 生产者确认
- **WHEN** 生产者开启确认模式
- **THEN** 系统在消息持久化后返回确认

#### Scenario: 消费者手动确认
- **WHEN** 消费者开启手动确认模式
- **THEN** 系统在收到确认后才移除消息

#### Scenario: 死信队列处理
- **WHEN** 消息重试次数超过阈值
- **THEN** 系统将消息转入死信队列等待人工处理

### Requirement: 延迟消息
系统 SHALL 支持延迟消息投递。

#### Scenario: 定时投递
- **WHEN** 发布消息时指定延迟时间
- **THEN** 系统在指定时间后投递消息

#### Scenario: 延迟队列实现
- **WHEN** 消息需要延迟处理
- **THEN** 系统使用 RabbitMQ 延迟插件或 TTL + 死信队列实现

#### Scenario: 任务超时提醒
- **WHEN** 任务创建时设置截止日期
- **THEN** 系统在截止日期前发送提醒通知

### Requirement: 事件幂等性
系统 SHALL 保证事件处理的幂等性，防止重复消费导致数据不一致。

#### Scenario: 基于 eventId 去重
- **WHEN** 消费者收到事件
- **THEN** 系统检查 eventId 是否已处理，已处理则跳过

#### Scenario: 幂等性存储
- **WHEN** 事件处理成功
- **THEN** 系统记录 eventId 到幂等性表（设置 TTL 过期时间）

#### Scenario: 并发事件处理
- **WHEN** 同一 eventId 被并发消费
- **THEN** 系统通过分布式锁保证只有一个消费者处理成功

### Requirement: 事件溯源
系统 SHALL 支持关键业务事件的事件溯源能力。

#### Scenario: 事件持久化存储
- **WHEN** 关键业务事件发布
- **THEN** 系统将事件持久化到事件存储库

#### Scenario: 事件回放
- **WHEN** 需要重建数据状态
- **THEN** 系统按时间顺序回放历史事件

#### Scenario: 事件快照
- **WHEN** 事件数量达到阈值
- **THEN** 系统创建状态快照，加速回放

#### Scenario: 事件查询
- **WHEN** 按条件查询历史事件
- **THEN** 系统支持按聚合 ID、事件类型、时间范围查询