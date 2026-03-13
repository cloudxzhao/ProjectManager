#!/bin/bash
# ProjectHub API V3 全量测试脚本
# 执行时间：2026-03-13

BASE_URL="http://localhost:9527/api/v1"
TOKEN="eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEsInN1c2IiOiJhZG1pbiIsImlhdCI6MTc3MzQxMzg2NCwiZXhwIjoxNzczNDIxMDY0fQ.BSX-sqkQmr-pz8bStCsh-431cNy4ph_o75JWPdz8MxVo7VxuqsK7q_vHtHNioZY1x8GxFZ4G4pfOJARIcIE4mg"

# 认证测试
echo "=== 认证模块测试 ==="

# 1. 登录测试 - 正确凭证
echo -e "\n[TEST] 登录 - 正确凭证"
curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"Admin123"}' | head -c 500

# 2. 登录测试 - 错误密码
echo -e "\n\n[TEST] 登录 - 错误密码"
curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"WrongPassword"}'

# 3. 登录测试 - 不存在的用户
echo -e "\n\n[TEST] 登录 - 不存在的用户"
curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"nonexistent@user.com","password":"AnyPassword"}'

# 4. 登录测试 - 空参数
echo -e "\n\n[TEST] 登录 - 空参数"
curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{}'

# 5. 刷新 Token
echo -e "\n\n[TEST] 刷新 Token"
curl -s -X POST "$BASE_URL/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImFkbWluIiwiaWF0IjoxNzczNDEzODY0LCJleHAiOjE3NzQwMTg2NjR9.ydTwaNYIoIS0AmBVyc4lyfTG6uhPuajPeOeK5RuJgpyOzH1eP2bLSnE8VIoFNPA8x940I5W7AOsKUQ_wKZ2I0w\"}"

# 用户模块测试
echo -e "\n\n=== 用户模块测试 ==="

# 6. 获取个人资料
echo -e "\n[TEST] 获取个人资料"
curl -s -X GET "$BASE_URL/user/profile" \
  -H "Authorization: Bearer $TOKEN"

# 7. 更新个人资料
echo -e "\n\n[TEST] 更新个人资料"
curl -s -X PUT "$BASE_URL/user/profile?nickname=TestAdmin" \
  -H "Authorization: Bearer $TOKEN"

# 8. 无认证访问个人资料
echo -e "\n\n[TEST] 无认证访问个人资料"
curl -s -X GET "$BASE_URL/user/profile"

# 项目模块测试
echo -e "\n\n=== 项目模块测试 ==="

# 9. 项目列表
echo -e "\n[TEST] 项目列表"
curl -s -X GET "$BASE_URL/projects?page=1&size=10" \
  -H "Authorization: Bearer $TOKEN"

# 10. 创建项目
echo -e "\n\n[TEST] 创建项目"
curl -s -X POST "$BASE_URL/projects" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Project V3","key":"TESTV3","description":"Test project for V3","color":"#FF5733","startDate":"2026-03-01","endDate":"2026-12-31"}'

# 11. 创建项目 - 空名称
echo -e "\n\n[TEST] 创建项目 - 空名称"
curl -s -X POST "$BASE_URL/projects" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"","key":"EMPTY","description":"Empty name project"}'

# 12. 创建项目 - 缺少必填字段
echo -e "\n\n[TEST] 创建项目 - 缺少必填字段"
curl -s -X POST "$BASE_URL/projects" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"key":"NOKEY"}'

echo -e "\n\n=== 测试完成 ==="
