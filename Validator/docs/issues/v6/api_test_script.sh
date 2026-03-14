#!/bin/bash
# ProjectHub Backend API Test Script v6
# 执行全量 API 接口测试

BASE_URL="http://localhost:9527/api/v1"
ISSUES_FILE="D:/data/project/ClaudeStudy/ProjectManagerStudy/Validator/docs/issues/v6/issues-v6-p01.json"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试计数
TESTS_PASSED=0
TESTS_FAILED=0
TESTS_TOTAL=0

# 存储 issue 的数组
declare -a ISSUES

# 记录 issue
record_issue() {
    local id="$1"
    local title="$2"
    local severity="$3"
    local category="$4"
    local description="$5"
    local api="$6"
    local current="$7"
    local expected="$8"

    ISSUES+=("{\"id\":\"$id\",\"title\":\"$title\",\"severity\":\"$severity\",\"category\":\"$category\",\"description\":\"$description\",\"affected_apis\":[\"$api\"],\"current_state\":\"$current\",\"expected_state\":\"$expected\"}")
}

# 测试函数
test_api() {
    local name="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    local headers="$5"
    local expected_code="$6"
    local expected_http="$7"

    TESTS_TOTAL=$((TESTS_TOTAL + 1))

    if [ -n "$data" ]; then
        response=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X "$method" "$url" -H "Content-Type: application/json" -d "$data" $headers)
    else
        response=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X "$method" "$url" $headers)
    fi

    http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d':' -f2)
    body=$(echo "$response" | sed '/HTTP_CODE:/d')

    if [ "$http_code" == "$expected_http" ]; then
        echo -e "${GREEN}[PASS]${NC} $name - HTTP $http_code"
        TESTS_PASSED=$((TESTS_PASSED + 1))
        return 0
    else
        echo -e "${RED}[FAIL]${NC} $name - Expected HTTP $expected_http, got $http_code"
        echo "Response: $body"
        TESTS_FAILED=$((TESTS_FAILED + 1))
        return 1
    fi
}

echo "========================================="
echo "ProjectHub Backend API Test Suite v6"
echo "========================================="
echo ""

# 1. 登录获取 Token
echo "正在登录..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"usernameOrEmail\":\"admin\",\"password\":\"Admin123\"}")

echo "登录响应：$LOGIN_RESPONSE"

# 提取 Token
TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"refreshToken":"[^"]*"' | cut -d'"' -f4)

echo "Token: $TOKEN"
echo "Refresh Token: $REFRESH_TOKEN"
echo ""

# 2. 测试认证相关 API
echo "=== 认证模块测试 ==="

# 测试登录 - 空用户名
test_api "登录 - 空用户名" "POST" "$BASE_URL/auth/login" "{\"usernameOrEmail\":\"\",\"password\":\"Admin123\"}" "" "400" "400"

# 测试登录 - 空密码
test_api "登录 - 空密码" "POST" "$BASE_URL/auth/login" "{\"usernameOrEmail\":\"admin\"}" "" "400" "400"

# 测试登录 - 错误密码
test_api "登录 - 错误密码" "POST" "$BASE_URL/auth/login" "{\"usernameOrEmail\":\"admin\",\"password\":\"wrong\"}" "" "2001" "401"

# 测试登录 - 不存在的用户
test_api "登录 - 不存在的用户" "POST" "$BASE_URL/auth/login" "{\"usernameOrEmail\":\"nonexistent\",\"password\":\"Admin123\"}" "" "2001" "401"

echo ""
echo "=== 用户模块测试 ==="

# 测试获取用户资料 - 无认证
test_api "获取用户资料 - 无认证" "GET" "$BASE_URL/user/profile" "" "" "401" "401"

# 测试获取用户资料 - 有认证
if [ -n "$TOKEN" ]; then
    test_api "获取用户资料 - 有认证" "GET" "$BASE_URL/user/profile" "" "-H \"Authorization: Bearer $TOKEN\"" "200" "200"
fi

echo ""
echo "=== 测试完成 ==="
echo "总计：$TESTS_TOTAL"
echo "通过：$TESTS_PASSED"
echo "失败：$TESTS_FAILED"
echo "通过率：$((TESTS_PASSED * 100 / TESTS_TOTAL))%"
