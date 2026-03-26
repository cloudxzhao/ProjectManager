# SkyWalking 链路追踪配置指南

## 概述

SkyWalking 是一个分布式链路追踪系统，用于微服务架构中的性能监控和故障排查。

## 架构

```
┌─────────────────────────────────────────────────────────────┐
│                    SkyWalking OAP Server                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │  接收 Agent 数据  │  │  分析处理数据    │  │  存储数据    │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │
                              │ gRPC
                              │
         ┌────────────────────┼────────────────────┐
         │                    │                    │
    ┌────┴────┐          ┌────┴────┐         ┌────┴────┐
    │Service A│          │Service B│         │Service C│
    │ Agent   │          │ Agent   │         │ Agent   │
    └─────────┘          └─────────┘         └─────────┘
```

## 部署 SkyWalking OAP Server

### Docker 部署

```yaml
# docker-compose.yml
version: '3.8'
services:
  oap:
    image: apache/skywalking-oap-server:9.2.0
    container_name: skywalking-oap
    ports:
      - "11800:11800"  # gRPC 端口
      - "12800:12800"  # HTTP 端口
    environment:
      - SW_STORAGE=elasticsearch
      - SW_STORAGE_ES_CLUSTER_NODES=elasticsearch:9200
      - SW_CORE_RECORD_DATA_TTL=3
      - SW_CORE_METRICS_DATA_TTL=3
    depends_on:
      - elasticsearch

  elasticsearch:
    image: elasticsearch:8.8.0
    container_name: elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"

  ui:
    image: apache/skywalking-ui:9.2.0
    container_name: skywalking-ui
    ports:
      - "8080:8080"
    environment:
      - SW_OAP_ADDRESS=http://oap:12800
    depends_on:
      - oap
```

## 服务集成

### 方式 1: JVM Agent（推荐）

在启动脚本中添加 JVM 参数：

```bash
java -javaagent:/path/to/skywalking-agent.jar \
     -Dskywalking.agent.service_name=auth-service \
     -Dskywalking.agent.authentication=your-token \
     -Dskywalking.collector.backend_service=oap:11800 \
     -jar your-service.jar
```

### 方式 2: 环境变量（Docker 部署）

```yaml
services:
  auth-service:
    image: auth-service:latest
    environment:
      - JAVA_TOOL_OPTIONS=-javaagent:/skywalking/agent/skywalking-agent.jar
      - SW_AGENT_NAME=auth-service
      - SW_AGENT_AUTHENTICATION=your-token
      - SW_AGENT_COLLECTOR_BACKEND_SERVICES=oap:11800
    volumes:
      - ./skywalking-agent:/skywalking/agent
```

### 方式 3: Maven 依赖（代码级集成）

```xml
<dependency>
    <groupId>org.apache.skywalking</groupId>
    <artifactId>apm-toolkit-trace</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 配置文件说明

### application.yaml 配置

```yaml
spring:
  application:
    name: auth-service

# SkyWalking Agent 配置（仅在非容器环境使用）
skywalking:
  agent:
    service_name: ${SW_AGENT_NAME:auth-service}
    authentication: ${SW_AGENT_AUTHENTICATION:your-token}
    namespace: ${SW_NAMESPACE:ProjectHub}
  collector:
    backend_service: ${SW_AGENT_COLLECTOR_BACKEND_SERVICES:oap:11800}
  logging:
    level: info
    output: console
```

## 日志集成（包含 TraceId）

### Logback 配置

```xml
<!-- logback-spring.xml -->
<configuration>
    <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId},%X{spanId}] %-5level %logger{36} - %msg%n"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

### 日志格式说明

```
2024-01-15 10:30:45.123 [http-nio-9521-exec-1] [T123456789,S0] INFO  c.p.auth.controller.AuthController - 用户登录成功
```

- `T123456789`: Trace ID（全链路唯一标识）
- `S0`: Span ID（当前链路中的操作序号）

## 监控指标

### 健康检查端点

```yaml
# application.yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    tags:
      application: ${spring.application.name}
      service: ${spring.application.name}
```

访问 `http://localhost:9521/actuator/health` 查看服务健康状态

### Prometheus 集成

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

访问 `http://localhost:9521/actuator/prometheus` 查看 Prometheus 格式的指标数据

## 告警规则配置

### alarm.yml

```yaml
# 服务响应时间告警
service_resp_time_rule:
  expression: avg(service_resp_time > 1000)
  period: 10
  count: 3
  silence: 300
  message: "服务 {name} 响应时间超过 1 秒"

# 服务成功率告警
service_sla_rule:
  expression: service_sla < 90
  period: 10
  count: 3
  silence: 300
  message: "服务 {name} 成功率低于 90%"

# 服务实例下线告警
service_instance_num_rule:
  expression: count(service_instance < 1)
  period: 10
  count: 1
  silence: 300
  message: "服务 {name} 所有实例已下线"

# JVM GC 告警
jvm_gc_rule:
  expression: jvm_gc_count > 100
  period: 10
  count: 5
  silence: 600
  message: "服务 {name} JVM GC 过于频繁"
```

## 常用追踪注解

```java
import org.apache.skywalking.apm.toolkit.trace.Continuation;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Trace;

@Service
public class AuthService {

    @Trace
    @Tag(key = "userId", value = "arg[0]")
    public LoginResponse login(Long userId, String password) {
        // 方法调用会被自动追踪
        // userId 参数值会作为 tag 记录
        return null;
    }

    @Trace(async = true)
    public void asyncOperation() {
        // 异步操作追踪
        Continuation.continued();
    }
}
```

## 验证步骤

1. **启动 SkyWalking OAP 和 UI**
   ```bash
   docker-compose up -d
   ```

2. **访问 SkyWalking UI**
   - 地址：http://localhost:8080
   - 查看服务列表、拓扑图、调用链

3. **验证服务接入**
   - 在服务列表中找到对应的服务
   - 查看 Topology（服务拓扑图）
   - 查看 Trace（调用链）

4. **验证日志集成**
   - 查看日志是否包含 Trace ID
   - 在 SkyWalking UI 中通过 Trace ID 查询完整链路

## 故障排查

### Agent 未上报数据

1. 检查 Agent 路径是否正确
2. 检查 `backend_service` 地址是否可达
3. 检查 `service_name` 是否正确
4. 查看 Agent 日志：`-Dskywalking.logging.dir=/path/to/logs`

### 数据不完整

1. 检查是否所有服务都接入了 Agent
2. 检查跨服务调用是否使用了支持的组件
3. 确认数据库连接池等中间件在支持列表中

## 性能优化

1. **采样率设置**
   ```bash
   -Dskywalking.sample_n_per_3_secs=60
   ```
   每秒最多采样 60 条链路

2. **禁用不必要的插件**
   ```bash
   -Dskywalking.agent.plugin.exclude=grpc,mongodb
   ```

3. **调整缓冲区大小**
   ```bash
   -Dskywalking.buffer_size=500
   ```
