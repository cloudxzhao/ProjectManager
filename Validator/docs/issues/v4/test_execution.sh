#!/bin/bash
# API Test Execution Script - V4
# 测试日期: 2026-03-14

API_BASE="http://localhost:9527/api/v1"

# 登录获取 token
echo "=== Login ==="
LOGIN_RESPONSE=$(curl -s -X POST "$API_BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"Admin123"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
REFRESH_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"refreshToken":"[^"]*"' | cut -d'"' -f4)

echo "Token: ${TOKEN:0:50}..."
echo "Refresh Token: ${REFRESH_TOKEN:0:50}..."

# 测试函数
test_api() {
  local method=$1
  local endpoint=$2
  local data=$3
  local description=$4

  if [ "$method" = "GET" ]; then
    echo "=== $description ==="
    curl -s -X "$method" "$API_BASE$endpoint" -H "Authorization: Bearer $TOKEN"
    echo ""
  else
    echo "=== $description ==="
    curl -s -X "$method" "$API_BASE$endpoint" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$data"
    echo ""
  fi
}

echo ""
echo "=========================================="
echo "V4 API Test Execution"
echo "=========================================="

# 用户模块测试
echo ""
echo "=== User Module ==="
test_api "GET" "/user/profile" "" "Get Profile"
test_api "PUT" "/user/profile" '{"nickname":"TestNickname","avatar":"https://example.com/avatar.png"}' "Update Profile"
test_api "PUT" "/user/password" '{"oldPassword":"Admin123","newPassword":"Admin123"}' "Change Password"

# 项目模块测试
echo ""
echo "=== Project Module ==="
test_api "GET" "/projects?page=1&size=10" "" "List Projects"
test_api "POST" "/projects" '{"name":"API Test Project","description":"Test","status":"ACTIVE","startDate":"2026-03-01","endDate":"2026-12-31"}' "Create Project"
test_api "GET" "/projects/15" "" "Get Project Detail"
test_api "PUT" "/projects/15" '{"name":"Updated Project","status":"COMPLETED"}' "Update Project"

# 任务模块测试
echo ""
echo "=== Task Module ==="
test_api "GET" "/projects/15/tasks" "" "List Tasks"
test_api "POST" "/projects/15/tasks" '{"title":"Test Task","status":"TODO","priority":"HIGH"}' "Create Task"
test_api "GET" "/projects/15/tasks/17" "" "Get Task Detail"
test_api "PUT" "/projects/15/tasks/17" '{"title":"Updated Task","status":"IN_PROGRESS","priority":"LOW"}' "Update Task"
test_api "POST" "/projects/15/tasks/17/toggle-complete" "" "Toggle Complete"
test_api "DELETE" "/projects/15/tasks/17" "" "Delete Task"

# UserStory 模块测试
echo ""
echo "=== UserStory Module ==="
test_api "GET" "/projects/15/stories" "" "List UserStories"
test_api "POST" "/projects/15/stories" '{"title":"User Story 1","status":"IN_PROGRESS","priority":"HIGH"}' "Create UserStory"

# Epic 模块测试
echo ""
echo "=== Epic Module ==="
test_api "GET" "/projects/15/epics" "" "List Epics"
test_api "POST" "/projects/15/epics" '{"name":"Epic 1","status":"IN_PROGRESS","color":"#FF0000"}' "Create Epic"

# Wiki 模块测试
echo ""
echo "=== Wiki Module ==="
test_api "GET" "/projects/9/wiki" "" "List Wiki"
test_api "POST" "/projects/9/wiki" '{"title":"Test Wiki","content":"Test"}' "Create Wiki"

# Issue 模块测试
echo ""
echo "=== Issue Module ==="
test_api "GET" "/projects/9/issues" "" "List Issues"
test_api "POST" "/projects/9/issues" '{"title":"Test Issue","type":"BUG","priority":"HIGH","status":"OPEN"}' "Create Issue"

# 评论模块测试
echo ""
echo "=== Comment Module ==="
test_api "GET" "/projects/9/tasks/9/comments" "" "List Comments"
test_api "POST" "/projects/9/tasks/9/comments" '{"content":"Test comment"}' "Add Comment"

# 通知模块测试
echo ""
echo "=== Notification Module ==="
test_api "GET" "/notifications" "" "List Notifications"
test_api "GET" "/notifications/unread-count" "" "Unread Count"
test_api "POST" "/notifications/read-all" "" "Mark All Read"

# 异常场景测试
echo ""
echo "=== Error Scenarios ==="
echo "=== No Auth ==="
curl -s -w "\nHTTP: %{http_code}" -X GET "$API_BASE/projects"

echo ""
echo "=== Wrong Password Login ==="
curl -s -w "\nHTTP: %{http_code}" -X POST "$API_BASE/auth/login" -H "Content-Type: application/json" -d '{"usernameOrEmail":"admin","password":"WrongPassword"}'

echo ""
echo "=== Not Found Project ==="
curl -s -w "\nHTTP: %{http_code}" -X GET "$API_BASE/projects/99999" -H "Authorization: Bearer $TOKEN"

echo ""
echo "=== Task Not Found ==="
curl -s -w "\nHTTP: %{http_code}" -X GET "$API_BASE/projects/999/tasks/99999" -H "Authorization: Bearer $TOKEN"

echo ""
echo "=========================================="
echo "Test Complete"
echo "=========================================="