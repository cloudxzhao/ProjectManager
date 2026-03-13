# ProjectHub API 测试报告

## 测试概述

| 属性 | 值 |
|------|-----|
| 测试日期 | 2026-03-13 |
| 测试范围 | 后端 API 接口 |
| API 基础地址 | http://localhost:9527/api/v1 |
| 测试账户 | admin / Admin123 (超级管理员) |

---

## 发现的问题清单

### 问题 1: 任务状态和优先级未正确保存

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-001 |
| **title** | Task 状态(status)和优先级(priority)未正确保存和返回 |
| **severity** | high |
| **category** | data_persistence |
| **description** | 创建任务时传入的 status (如 "TODO") 和 priority (如 "HIGH") 没有被正确保存到数据库，返回的响应中这些字段始终为 null。更新任务和移动任务后同样无法正确保存。 |
| **affected_apis** | ["POST /api/v1/projects/{projectId}/tasks", "PUT /api/v1/projects/{projectId}/tasks/{id}", "POST /api/v1/projects/{projectId}/tasks/{id}/move"] |

**复现步骤：**
```bash
# 创建任务
curl -X POST "http://localhost:9527/api/v1/projects/2/tasks" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Task","status":"TODO","priority":"HIGH"}'
# 返回: status:null, priority:null

# 获取任务详情
curl -X GET "http://localhost:9527/api/v1/projects/2/tasks/1" -H "Authorization: Bearer $TOKEN"
# 返回: status:null, priority:null
```

**当前状态：**
```json
{
  "status": null,
  "priority": null
}
```

**期望状态：**
```json
{
  "status": "TODO",
  "priority": "HIGH"
}
```

---

### 问题 2: 用户 Profile 更新接口存在字段映射错误

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-002 |
| **title** | 用户更新 profile 时 nickname 参数错误地覆盖了 username |
| **severity** | critical |
| **category** | data_integrity |
| **description** | UserController 的 updateProfile 方法接受 nickname 参数，但实际上错误地将其设置为 username 字段，导致用户无法使用原用户名登录。这是一个严重的数据完整性问题。 |
| **affected_apis** | ["PUT /api/v1/user/profile"] |

**复现步骤：**
```bash
# 更新用户昵称
curl -X PUT "http://localhost:9527/api/v1/user/profile?nickname=AdminUser" -H "Authorization: Bearer $TOKEN"
# 返回: username 变成了 "AdminUser"

# 尝试使用原用户名登录
curl -X POST http://localhost:9527/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"Admin123"}'
# 返回: {"code":500,"message":"用户名或密码错误"}
```

**当前状态：**
- nickname 参数被写入 username 字段
- 原始 username 丢失
- 用户无法登录

**期望状态：**
- nickname 参数应该写入独立的 nickname 字段（如果存在）
- username 保持不变

---

### 问题 3: 用户角色字段未正确返回

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-003 |
| **title** | 用户角色(role)字段在 API 响应中始终显示为 MEMBER |
| **severity** | medium |
| **category** | response |
| **description** | 超级管理员账户的角色应该是 ADMIN，但 API 返回的 role 字段始终显示为 MEMBER。UserVO.fromEntity() 方法已废弃但可能仍在使用，导致 role 字段没有被正确映射。 |
| **affected_apis** | ["GET /api/v1/user/profile"] |

**当前状态：**
```json
{
  "id": 1,
  "username": "admin",
  "role": "MEMBER"
}
```

**期望状态：**
```json
{
  "id": 1,
  "username": "admin",
  "role": "ADMIN"
}
```

---

### 问题 4: 错误响应 HTTP 状态码不正确

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-004 |
| **title** | 资源不存在时返回 500 错误而非 404 |
| **severity** | medium |
| **category** | error_handling |
| **description** | 当请求的资源不存在（如项目、任务等）时，后端返回 HTTP 500 状态码而不是更准确的 404 Not Found。这不符合 RESTful API 最佳实践。 |
| **affected_apis** | ["GET /api/v1/projects/{id}", "PUT /api/v1/projects/{id}", "DELETE /api/v1/projects/{id}", "PUT /api/v1/projects/{projectId}/tasks/{id}", "DELETE /api/v1/projects/{projectId}/tasks/{id}", "POST /api/v1/tasks/{taskId}/comments"] |

**复现步骤：**
```bash
# 请求不存在的项目
curl -s -o /dev/null -w "%{http_code}" "http://localhost:9527/api/v1/projects/9999" -H "Authorization: Bearer $TOKEN"
# 返回: 500 (应该是 404)
```

**当前状态：** HTTP 500

**期望状态：** HTTP 404

---

### 问题 5: 无认证访问返回 403 而非 401

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-005 |
| **title** | 未认证用户访问受保护接口返回 HTTP 403 |
| **severity** | medium |
| **category** | security |
| **description** | 当用户未提供 token 或 token 无效时访问受保护接口，应返回 HTTP 401 Unauthorized 而不是 HTTP 403 Forbidden。 |
| **affected_apis** | 所有需要认证的接口 |

**复现步骤：**
```bash
# 无 token 访问
curl -s -o /dev/null -w "%{http_code}" "http://localhost:9527/api/v1/user/profile"
# 返回: 403 (应该是 401)

# 无效 token 访问
curl -s -o /dev/null -w "%{http_code}" "http://localhost:9527/api/v1/user/profile" -H "Authorization: Bearer invalid_token"
# 返回: 403 (应该是 401)
```

**当前状态：** HTTP 403

**期望状态：** HTTP 401

---

### 问题 6: 创建用户故事返回 500 错误

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-006 |
| **title** | 创建用户故事接口返回服务器内部错误 |
| **severity** | high |
| **category** | functionality |
| **description** | POST /api/v1/projects/{projectId}/stories 接口返回 500 服务器内部错误，无法创建用户故事。 |
| **affected_apis** | ["POST /api/v1/projects/{projectId}/stories"] |

**复现步骤：**
```bash
curl -X POST "http://localhost:9527/api/v1/projects/2/stories" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"User Story 1","description":"Story description","priority":"HIGH"}'
# 返回: {"code":500,"message":"服务器内部错误"}
```

---

### 问题 7: 任务筛选功能未生效

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-007 |
| **title** | 任务列表筛选功能没有正确过滤结果 |
| **severity** | medium |
| **category** | functionality |
| **description** | 使用 status、priority 等参数筛选任务时，返回的结果不准确。虽然请求参数被接受，但由于 status 和 priority 没有正确存储（见问题1），筛选功能实际上没有生效。 |
| **affected_apis** | ["GET /api/v1/projects/{projectId}/tasks"] |

**复现步骤：**
```bash
# 按 status=TODO 筛选
curl -X GET "http://localhost:9527/api/v1/projects/2/tasks?status=TODO" -H "Authorization: Bearer $TOKEN"
# 应该只返回 TODO 状态的任务，但由于 status 未正确存储，返回了所有任务
```

---

### 问题 8: Token 刷新功能失败

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-008 |
| **title** | Token 刷新接口返回 500 错误 |
| **severity** | high |
| **category** | authentication |
| **description** | 使用 refreshToken 刷新访问令牌时返回 500 错误。用户需要频繁重新登录，影响用户体验。 |
| **affected_apis** | ["POST /api/v1/auth/refresh"] |

**复现步骤：**
```bash
curl -X POST "http://localhost:9527/api/v1/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"eyJhbGci..."}'
# 返回: {"code":500,"message":"刷新 Token 失败"}
```

---

### 问题 9: UserVO 缺少 nickname 字段

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-009 |
| **title** | UserVO 缺少 nickname 字段定义 |
| **severity** | low |
| **category** | response |
| **description** | UserController.updateProfile 接受 nickname 参数，但 UserVO 中没有定义 nickname 字段，导致无法正确返回用户昵称。 |
| **affected_apis** | ["GET /api/v1/user/profile", "PUT /api/v1/user/profile"] |

---

### 问题 10: 项目更新时 status 字段未正确处理

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-010 |
| **title** | 更新项目时传入的 status 字段被忽略 |
| **severity** | low |
| **category** | data_persistence |
| **description** | 在 UpdateProjectRequest 中传入 status 字段（如 "ACTIVE"），更新后该字段仍然为 null。 |
| **affected_apis** | ["PUT /api/v1/projects/{id}"] |

**复现步骤：**
```bash
curl -X PUT "http://localhost:9527/api/v1/projects/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Project","status":"ACTIVE"}'
# 返回: status:null
```

---

## 测试通过的功能

以下功能正常工作：
- 用户登录/登出
- 项目创建、获取详情、列表、删除
- 任务创建、获取详情、列表、删除
- 评论添加、列表
- 通知列表、未读数量
- 用户资料获取
- 分页功能（page=0、size<=0 返回 400 错误，size 超大返回所有数据）
- 参数验证（必填字段、邮箱格式、密码强度）

---

## 测试覆盖率

| 模块 | 接口数 | 测试数 |
|------|--------|--------|
| 认证 (Auth) | 7 | 10 |
| 用户 (User) | 4 | 5 |
| 项目 (Project) | 7 | 10 |
| 任务 (Task) | 7 | 15 |
| 评论 (Comment) | 4 | 3 |
| 通知 (Notification) | 3 | 2 |
| 用户故事 (UserStory) | 5 | 2 |
| **总计** | **37** | **47** |

---

## 建议

1. **优先级高 (P0)：**
   - 修复问题 1：任务状态和优先级存储
   - 修复问题 2：用户 profile 更新逻辑

2. **优先级中 (P1)：**
   - 修复问题 4：错误状态码
   - 修复问题 5：认证状态码
   - 修复问题 6：用户故事创建

3. **优先级低 (P2)：**
   - 修复问题 3：用户角色返回
   - 修复问题 7：筛选功能
   - 修复问题 8：Token 刷新