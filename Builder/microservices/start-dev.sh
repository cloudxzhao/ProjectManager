#!/bin/bash

# ProjectHub 微服务本地开发启动脚本
# 使用本地 Docker 容器作为数据库和缓存

echo "========================================"
echo "  ProjectHub 微服务启动脚本 (本地开发)  "
echo "========================================"
echo ""

# 加载环境变量
set -a
source .env.local 2>/dev/null || source .env 2>/dev/null || {
    echo "❌ 错误：找不到 .env.local 或 .env 文件"
    exit 1
}
set +a

echo "✅ 环境变量已加载"
echo ""

# 检查 Docker 容器状态
echo "🔍 检查 Docker 容器状态..."

if ! docker ps --format '{{.Names}}' | grep -q "project-manager-postgres"; then
    echo "❌ PostgreSQL 容器未运行"
    exit 1
fi

if ! docker ps --format '{{.Names}}' | grep -q "project-manager-redis"; then
    echo "❌ Redis 容器未运行"
    exit 1
fi

echo "✅ Docker 容器运行正常"
echo ""

# 显示配置
echo "📋 当前配置:"
echo "   PostgreSQL: $DB_HOST:$DB_PORT / $DB_NAME"
echo "   Redis: $REDIS_HOST:$REDIS_PORT"
echo "   Nacos: $NACOS_SERVER"
echo ""

# 导出环境变量供 Maven/Java 使用
export DB_HOST DB_PORT DB_NAME DB_USER DB_PASSWORD
export REDIS_HOST REDIS_PORT REDIS_PASSWORD
export NACOS_SERVER
export JWT_SECRET

# 进入项目目录
cd "$(dirname "$0")"

# 使用 Maven 启动（开发模式）
echo "🚀 启动微服务..."
echo ""

# 并行启动所有服务（后台运行）
# 如果需要前台运行，去掉 '& rm -rf' 后面的部分

mvn spring-boot:run -pl services/auth-service -am -Dspring-boot.run.profiles=dev &
mvn spring-boot:run -pl services/user-service -am -Dspring-boot.run.profiles=dev &
mvn spring-boot:run -pl services/project-service -am -Dspring-boot.run.profiles=dev &
mvn spring-boot:run -pl services/task-service -am -Dspring-boot.run.profiles=dev &
mvn spring-boot:run -pl services/story-service -am -Dspring-boot.run.profiles=dev &
mvn spring-boot:run -pl services/issue-service -am -Dspring-boot.run.profiles=dev &
mvn spring-boot:run -pl services/wiki-service -am -Dspring-boot.run.profiles=dev &
mvn spring-boot:run -pl services/notification-service -am -Dspring-boot.run.profiles=dev &

echo ""
echo "✅ 所有服务已启动（后台运行）"
echo "📌 使用 'Ctrl+C' 停止或查看日志"
echo ""

# 等待用户中断
wait
