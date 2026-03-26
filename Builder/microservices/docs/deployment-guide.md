# ProjectHub 微服务部署指南

## 目录

1. [部署架构](#部署架构)
2. [环境要求](#环境要求)
3. [本地开发部署](#本地开发部署)
4. [生产环境部署](#生产环境部署)
5. [CI/CD 流水线](# cicd 流水线)
6. [监控与告警](#监控与告警)
7. [故障排查](#故障排查)

---

## 部署架构

### 架构图

```
                                    ┌─────────────────┐
                                    │   Cloudflare    │
                                    │      CDN        │
                                    └────────┬────────┘
                                             │
                                    ┌────────▼────────┐
                                    │     Nginx       │
                                    │  (负载均衡)      │
                                    └────────┬────────┘
                                             │
              ┌──────────────────────────────┼──────────────────────────────┐
              │                              │                              │
     ┌────────▼────────┐           ┌────────▼────────┐           ┌────────▼────────┐
     │   Gateway       │           │   Gateway       │           │   Gateway       │
     │   Instance 1    │           │   Instance 2    │           │   Instance 3    │
     │   :8080         │           │   :8080         │           │   :8080         │
     └────────┬────────┘           └────────┬────────┘           └────────┬────────┘
              │                              │                              │
              └──────────────────────────────┼──────────────────────────────┘
                                             │
              ┌──────────────────────────────┼──────────────────────────────┐
              │                              │                              │
     ┌────────▼────────┐           ┌────────▼────────┐           ┌────────▼────────┐
     │    Nacos        │           │   RabbitMQ      │           │  PostgreSQL     │
     │   Cluster       │           │   Cluster       │           │   Cluster       │
     └─────────────────┘           └─────────────────┘           └─────────────────┘

     ┌──────────────────────────────────────────────────────────────────────────┐
     │                           Kubernetes Cluster                              │
     │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐        │
     │  │auth-service │ │user-service │ │project-svc  │ │task-service │        │
     │  │   x2        │ │   x2        │ │   x3        │ │   x3        │        │
     │  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘        │
     │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐        │
     │  │story-service│ │issue-service│ │wiki-service │ │notification │        │
     │  │   x2        │ │   x2        │ │   x2        │ │   x2        │        │
     │  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘        │
     └──────────────────────────────────────────────────────────────────────────┘
```

---

## 环境要求

### 最低配置

| 组件 | CPU | 内存 | 磁盘 |
|------|-----|------|------|
| Gateway (x2) | 2 核 | 4GB | 10GB |
| 每个微服务 (x2) | 1 核 | 2GB | 5GB |
| Nacos | 2 核 | 4GB | 20GB |
| RabbitMQ | 2 核 | 4GB | 50GB |
| PostgreSQL | 4 核 | 8GB | 500GB |
| Redis | 2 核 | 4GB | 20GB |

**总计**: 至少 16 核 CPU, 64GB 内存，1TB 磁盘

### 推荐配置

| 组件 | CPU | 内存 | 磁盘 |
|------|-----|------|------|
| Gateway (x3) | 4 核 | 8GB | 20GB |
| 每个微服务 (x3) | 2 核 | 4GB | 10GB |
| Nacos Cluster (x3) | 4 核 | 8GB | 50GB |
| RabbitMQ Cluster (x3) | 4 核 | 8GB | 100GB |
| PostgreSQL (主从) | 8 核 | 16GB | 1TB SSD |
| Redis Cluster (x3) | 4 核 | 8GB | 50GB |

**总计**: 至少 64 核 CPU, 256GB 内存，5TB SSD

---

## 本地开发部署

### 1. 启动基础设施

```bash
cd Builder/microservices

# 启动所有基础设施（Nacos, RabbitMQ, PostgreSQL, SkyWalking）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f nacos
```

### 2. 初始化数据库

```bash
# 执行初始化脚本
psql -h localhost -U postgres -f init-scripts/auth-db-init.sql
psql -h localhost -U postgres -f init-scripts/user-db-init.sql
psql -h localhost -U postgres -f init-scripts/project-db-init.sql
psql -h localhost -U postgres -f init-scripts/task-db-init.sql
psql -h localhost -U postgres -f init-scripts/story-db-init.sql
psql -h localhost -U postgres -f init-scripts/issue-db-init.sql
psql -h localhost -U postgres -f init-scripts/wiki-db-init.sql
psql -h localhost -U postgres -f init-scripts/notify-db-init.sql
```

### 3. 启动微服务

```bash
# 启动 Auth Service
cd services/auth-service
./mvnw spring-boot:run

# 启动 User Service
cd services/user-service
./mvnw spring-boot:run

# ... 启动其他服务
```

### 4. 启动 Gateway

```bash
cd gateway
./mvnw spring-boot:run
```

### 5. 验证部署

```bash
# 检查 Nacos 控制台
open http://localhost:8848/nacos

# 检查 Gateway
curl http://localhost:8080/actuator/gateway/routes

# 检查服务注册
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=auth-service
```

---

## 生产环境部署

### Docker Compose 部署

#### 1. 准备配置文件

```bash
# 创建生产环境目录
mkdir -p /opt/projecthub/{config,logs,data}

# 复制配置文件
cp Builder/microservices/gateway/src/main/resources/application-prod.yml /opt/projecthub/config/
cp Builder/microservices/services/*/src/main/resources/application-prod.yml /opt/projecthub/config/
```

#### 2. 部署 Gateway

```yaml
# /opt/projecthub/docker-compose.yml
version: '3.8'

services:
  gateway:
    image: projecthub/gateway:1.0.0
    container_name: gateway
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - NACOS_SERVER_ADDR=nacos:8848
      - JAVA_OPTS=-Xms2g -Xmx4g
    volumes:
      - ./config:/app/config
      - ./logs:/app/logs
    depends_on:
      - nacos
      - rabbitmq
    restart: always
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  auth-service:
    image: projecthub/auth-service:1.0.0
    container_name: auth-service
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - NACOS_SERVER_ADDR=nacos:8848
      - JAVA_OPTS=-Xms1g -Xmx2g
    volumes:
      - ./config:/app/config
      - ./logs:/app/logs
    depends_on:
      - nacos
      - rabbitmq
      - postgres-auth
    restart: always
    deploy:
      replicas: 2

  # ... 其他服务配置

  nacos:
    image: nacos/nacos-server:2.2.0
    container_name: nacos
    environment:
      - MODE=cluster
      - NACOS_SERVERS=nacos-1:8848 nacos-2:8848 nacos-3:8848
    volumes:
      - ./data/nacos:/home/nacos/data
    restart: always

  rabbitmq:
    image: rabbitmq:3.12-management
    container_name: rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      - RABBITMQ_DEFAULT_USER=admin
      - RABBITMQ_DEFAULT_PASS=your-password
    volumes:
      - ./data/rabbitmq:/var/lib/rabbitmq
    restart: always

  postgres:
    image: postgres:15
    container_name: postgres
    environment:
      - POSTGRES_PASSWORD=your-password
    volumes:
      - ./data/postgres:/var/lib/postgresql/data
    restart: always
```

#### 3. 启动服务

```bash
cd /opt/projecthub

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f gateway
```

### Kubernetes 部署

#### 1. 创建命名空间

```bash
kubectl create namespace projecthub
```

#### 2. 部署配置

```yaml
# k8s/gateway-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gateway
  namespace: projecthub
  labels:
    app: gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: gateway
  template:
    metadata:
      labels:
        app: gateway
    spec:
      containers:
      - name: gateway
        image: projecthub/gateway:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: NACOS_SERVER_ADDR
          value: "nacos.projecthub.svc.cluster.local:8848"
        - name: JAVA_OPTS
          value: "-Xms2g -Xmx4g"
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: gateway
  namespace: projecthub
spec:
  selector:
    app: gateway
  ports:
  - port: 8080
    targetPort: 8080
  type: LoadBalancer
```

#### 3. 应用配置

```bash
# 应用所有配置
kubectl apply -f k8s/

# 查看部署状态
kubectl get deployments -n projecthub

# 查看服务状态
kubectl get services -n projecthub

# 查看日志
kubectl logs -f deployment/gateway -n projecthub
```

---

## CI/CD 流水线

### GitHub Actions 配置

```yaml
# .github/workflows/deploy.yml
name: Deploy Microservices

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        service: [gateway, auth-service, user-service, project-service, task-service]

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'

    - name: Build with Maven
      run: |
        cd Builder/microservices/${{ matrix.service }}
        ./mvnw clean package -DskipTests

    - name: Build Docker image
      run: |
        docker build -t projecthub/${{ matrix.service }}:${{ github.sha }} Builder/microservices/${{ matrix.service }}
        docker tag projecthub/${{ matrix.service }}:${{ github.sha }} projecthub/${{ matrix.service }}:latest

    - name: Push to Docker Hub
      run: |
        echo "${{ secrets.DOCKER_PASSWORD }}" | docker login -u "${{ secrets.DOCKER_USERNAME }}" --password-stdin
        docker push projecthub/${{ matrix.service }}:${{ github.sha }}
        docker push projecthub/${{ matrix.service }}:latest

  deploy:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'

    steps:
    - uses: actions/checkout@v3

    - name: Deploy to Kubernetes
      run: |
        kubectl set image deployment/${{ matrix.service }} ${{ matrix.service }}=projecthub/${{ matrix.service }}:${{ github.sha }} -n projecthub
        kubectl rollout status deployment/${{ matrix.service }} -n projecthub
```

---

## 监控与告警

### SkyWalking 配置

```yaml
# SkyWalking Agent 配置
# 在每个服务的启动脚本中添加
-javaagent:/path/to/skywalking-agent.jar
-Dskywalking.agent.service_name=auth-service
-Dskywalking.collector.backend_service=oap:11800
```

### Prometheus 配置

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'gateway'
    static_configs:
      - targets: ['gateway:8080']
    metrics_path: '/actuator/prometheus'

  - job_name: 'microservices'
    kubernetes_sd_configs:
      - role: pod
    relabel_configs:
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_scrape]
        action: keep
        regex: true
```

### 告警规则

详见 `skywalking/config/alarm.yml`

---

## 故障排查

### 问题 1: 服务无法启动

**排查步骤**:
1. 查看日志：`docker-compose logs <service-name>`
2. 检查配置：确认 Nacos 地址正确
3. 检查依赖：确认数据库、RabbitMQ 可用
4. 检查内存：`docker stats`

### 问题 2: 服务间调用失败

**排查步骤**:
1. 检查服务注册：Nacos 控制台查看服务状态
2. 检查网络：`docker network inspect`
3. 检查 Feign 配置：超时时间、重试策略
4. 查看 SkyWalking 链路追踪

### 问题 3: 数据库连接失败

**排查步骤**:
1. 检查数据库状态：`docker-compose ps postgres`
2. 检查连接池：查看 HikariCP 指标
3. 检查网络：`telnet postgres 5432`
4. 查看数据库日志

### 问题 4: Gateway 路由失败

**排查步骤**:
1. 查看路由配置：`curl http://gateway:8080/actuator/gateway/routes`
2. 检查服务发现：`curl http://nacos:8848/nacos/v1/ns/instance/list`
3. 查看 Gateway 日志
4. 检查熔断配置

---

## 运维手册

### 日常巡检

```bash
# 1. 检查服务状态
docker-compose ps

# 2. 检查资源使用
docker stats

# 3. 查看错误日志
docker-compose logs --since 1h | grep ERROR

# 4. 检查 Nacos 控制台
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=gateway

# 5. 检查数据库连接
psql -h localhost -U postgres -c "SELECT count(*) FROM pg_stat_activity"
```

### 服务扩容

```bash
# Docker Compose
docker-compose up -d --scale auth-service=3

# Kubernetes
kubectl scale deployment auth-service --replicas=5 -n projecthub
```

### 服务重启

```bash
# 单个服务
docker-compose restart auth-service

# 所有服务
docker-compose restart

# Kubernetes
kubectl rollout restart deployment/auth-service -n projecthub
```

### 日志收集

```bash
# 查看最近 1 小时日志
docker-compose logs --since 1h

# 查看特定服务日志
docker-compose logs -f auth-service

# 导出日志
docker-compose logs auth-service > auth-service.log
```

---

## 备份策略

### 数据库备份

```bash
#!/bin/bash
# backup.sh

BACKUP_DIR="/backup/postgres"
DATE=$(date +%Y%m%d_%H%M%S)

# 备份所有数据库
pg_dumpall -h localhost -U postgres | gzip > ${BACKUP_DIR}/all_databases_${DATE}.sql.gz

# 删除 30 天前的备份
find ${BACKUP_DIR} -name "*.sql.gz" -mtime +30 -delete

echo "Backup completed: ${BACKUP_DIR}/all_databases_${DATE}.sql.gz"
```

### 配置文件备份

```bash
# 备份配置
tar -czf /backup/config/config_$(date +%Y%m%d).tar.gz /opt/projecthub/config/
```

---

## 性能优化

### JVM 调优

```bash
# Gateway (高并发)
JAVA_OPTS="-Xms4g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError"

# 微服务 (标准)
JAVA_OPTS="-Xms2g -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=500"
```

### 数据库调优

```sql
-- 增加连接数
ALTER SYSTEM SET max_connections = 500;

-- 增加共享缓冲区
ALTER SYSTEM SET shared_buffers = '4GB';

-- 增加工作内存
ALTER SYSTEM SET work_mem = '64MB';
```

---

## 版本发布流程

1. **代码冻结**: 合并所有 PR 到 main 分支
2. **打标签**: `git tag -a v1.0.0 -m "Release v1.0.0"`
3. **构建镜像**: CI/CD自动构建 Docker 镜像
4. **灰度发布**: 先发布 1 个实例，验证正常
5. **全量发布**: 逐步增加到所有实例
6. **验证**: 运行冒烟测试
7. **观察**: 监控指标 30 分钟

---

**文档维护**: 运维团队
**最后更新**: 2024-03-25
