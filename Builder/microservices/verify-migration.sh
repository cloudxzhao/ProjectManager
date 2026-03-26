#!/bin/bash

# ProjectHub 数据迁移验证脚本

echo "========================================"
echo "  ProjectHub 数据迁移验证              "
echo "========================================"
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查计数
check_count() {
    local table=$1
    local expected=$2
    local count=$(docker exec project-manager-postgres psql -U postgres -d projecthub_dev -t -c "SELECT count(*) FROM $table;" 2>/dev/null | tr -d ' ')

    if [ "$count" -ge "$expected" ]; then
        echo -e "${GREEN}✓${NC} $table: $count 条记录"
        return 0
    else
        echo -e "${RED}✗${NC} $table: $count 条记录 (期望 >= $expected)"
        return 1
    fi
}

# 检查表是否存在
check_table() {
    local table=$1
    local exists=$(docker exec project-manager-postgres psql -U postgres -d projecthub_dev -t -c "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = '$table');" 2>/dev/null | tr -d ' ')

    if [ "$exists" = "t" ]; then
        echo -e "${GREEN}✓${NC} $table 表存在"
        return 0
    else
        echo -e "${RED}✗${NC} $table 表不存在"
        return 1
    fi
}

# 1. 检查 Docker 容器
echo "1. 检查 Docker 容器状态"
echo "------------------------"

if docker ps --format '{{.Names}}' | grep -q "project-manager-postgres"; then
    echo -e "${GREEN}✓${NC} PostgreSQL 容器运行中"
else
    echo -e "${RED}✗${NC} PostgreSQL 容器未运行"
    exit 1
fi

if docker ps --format '{{.Names}}' | grep -q "project-manager-redis"; then
    echo -e "${GREEN}✓${NC} Redis 容器运行中"
else
    echo -e "${RED}✗${NC} Redis 容器未运行"
    exit 1
fi

echo ""

# 2. 测试数据库连接
echo "2. 测试数据库连接"
echo "------------------------"

if docker exec project-manager-postgres psql -U postgres -d projecthub_dev -c "SELECT 1;" >/dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} PostgreSQL 连接成功"
else
    echo -e "${RED}✗${NC} PostgreSQL 连接失败"
    exit 1
fi

if docker exec project-manager-redis redis-cli -a "Admin_2026" ping 2>&1 | grep -q "PONG"; then
    echo -e "${GREEN}✓${NC} Redis 连接成功"
else
    echo -e "${RED}✗${NC} Redis 连接失败"
    exit 1
fi

echo ""

# 3. 检查核心表
echo "3. 检查核心表结构"
echo "------------------------"

check_table "sys_user"
check_table "project"
check_table "task"
check_table "epic"
check_table "user_story"
check_table "issue"
check_table "wiki_document"

echo ""

# 4. 检查数据完整性
echo "4. 检查数据完整性"
echo "------------------------"

check_count "sys_user" 1
check_count "project" 0

echo ""

# 5. 检查服务端口
echo "5. 检查服务端口"
echo "------------------------"

services=(
    "8080:API Gateway"
    "9521:Auth Service"
    "9522:User Service"
    "9523:Project Service"
    "9524:Task Service"
)

for service in "${services[@]}"; do
    port="${service%%:*}"
    name="${service##*:}"
    if netstat -an 2>/dev/null | grep -q ":$port.*LISTEN"; then
        echo -e "${GREEN}✓${NC} $name (端口 $port) 监听中"
    else
        echo -e "${YELLOW}○${NC} $name (端口 $port) 未监听"
    fi
done

echo ""
echo "========================================"
echo "  验证完成                              "
echo "========================================"
