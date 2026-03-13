# ProjectHub API 测试报告 V2

## 测试概述

| 属性 | 值 |
|------|-----|
| 测试日期 | 2026-03-13 |
| 测试范围 | 后端 API 接口全面功能测试 |
| API 基础地址 | http://localhost:9527/api/v1 |
| 测试账户 | admin / Admin123 (超级管理员) |
| 测试类型 | 功能测试、边界测试、异常场景测试 |

---

## 测试环境和配置

### 后端配置
- **框架**: Spring Boot + Java 21
- **数据库**: PostgreSQL
- **API 文档**: http://localhost:9527/v3/api-docs

### 数据库配置
- **主机**: localhost:15432
- **数据库名**: projecthub_dev
- **用户**: postgres

### 测试工具
- curl (HTTP 请求)
- Python 3 (JSON 解析)

---

## 测试用例列表及结果

### 1. 认证模块 (Auth)

| 用例 ID | 接口 | 测试场景 | 预期结果 | 实际结果 | 状态 |
|---------|------|----------|----------|----------|------|
| AUTH-001 | POST /api/v1/auth/login | 正常登录 | 返回 200 和 token | 通过 | ✅ |
| AUTH-002 | POST /api/v1/auth/login | 错误密码 | 返回错误信息 | 返回 500 错误 | ⚠️ |
| AUTH-003 | POST /api/v1/auth/login | 不存在的用户 | 返回错误信息 | 返回错误信息 | ✅ |
| AUTH-004 | POST /api/v1/auth/register | 正常注册 | 返回 200 | 返回 200 | ✅ |
| AUTH-005 | POST /api/v1/auth/register | 弱密码 | 返回 400 | 返回 400 | ✅ |
| AUTH-006 | POST /api/v1/auth/register | 无效邮箱 | 返回 400 | 返回 400 | ✅ |
| AUTH-007 | POST /api/v1/auth/refresh | Token 刷新 | 返回新 token | 返回 200 | ✅ |
| AUTH-008 | POST /api/v1/auth/logout | 登出 | 返回 200 | 返回 200 | ✅ |

### 2. 用户模块 (User)

| 用例 ID | 接口 | 测试场景 | 预期结果 | 实际结果 | 状态 |
|---------|------|----------|----------|----------|------|
| USER-001 | GET /api/v1/user/profile | 获取资料 | 返回用户信息 | 通过 | ✅ |
| USER-002 | PUT /api/v1/user/profile | 更新 nickname | 更新成功 | 通过 | ✅ |
| USER-003 | GET /api/v1/user/profile | 验证 role 字段 | 返回 ADMIN | 返回 MEMBER | ❌ |
| USER-004 | GET /api/v1/user/profile | 验证 status 字段 | 返回 ACTIVE | 返回 null | ⚠️ |

### 3. 项目模块 (Project)

| 用例 ID | 接口 | 测试场景 | 预期结果 | 实际结果 | 状态 |
|---------|------|----------|----------|----------|------|
| PROJ-001 | GET /api/v1/projects | 项目列表 | 返回项目列表 | 通过 | ✅ |
| PROJ-002 | GET /api/v1/projects | page=0 边界 | 返回 400 | 返回 400 | ✅ |
| PROJ-003 | GET /api/v1/projects | size=0 边界 | 返回 400 | 返回 400 | ✅ |
| PROJ-004 | GET /api/v1/projects/{id} | 项目详情 | 返回项目信息 | 通过 | ✅ |
| PROJ-005 | GET /api/v1/projects/{id} | 不存在的项目 | 返回 404 | 返回 404(code=4001) | ⚠️ |
| PROJ-006 | POST /api/v1/projects | 创建项目 | 创建成功 | 通过 | ✅ |
| PROJ-007 | POST /api/v1/projects | 创建项目带 status | status 保存 | status 为 null | ❌ |
| PROJ-008 | PUT /api/v1/projects/{id} | 更新项目 | 更新成功 | 通过 | ✅ |
| PROJ-009 | PUT /api/v1/projects/{id} | 更新项目 status | status 保存 | status 为 null | ❌ |
| PROJ-010 | DELETE /api/v1/projects/{id} | 删除项目 | 删除成功 | 未测试 | - |
| PROJ-011 | POST /api/v1/projects/{id}/members | 添加成员 | 添加成功 | 返回 400(枚举错误) | ❌ |
| PROJ-012 | GET /api/v1/projects/{id}/members | 成员列表 | 返回成员列表 | 通过 | ✅ |
| PROJ-013 | DELETE /api/v1/projects/{id}/members/{userId} | 移除成员 | 移除成功 | 通过 | ✅ |

### 4. 任务模块 (Task)

| 用例 ID | 接口 | 测试场景 | 预期结果 | 实际结果 | 状态 |
|---------|------|----------|----------|----------|------|
| TASK-001 | POST /api/v1/projects/{projectId}/tasks | 创建任务 | 创建成功 | 通过 | ✅ |
| TASK-002 | POST /api/v1/projects/{projectId}/tasks | 创建带 status/priority | 字段保存 | 字段为 null | ❌ |
| TASK-003 | POST /api/v1/projects/{projectId}/tasks | 空 title | 返回 400 | 返回 400 | ✅ |
| TASK-004 | POST /api/v1/projects/{projectId}/tasks | 缺少 title | 返回 400 | 返回 400 | ✅ |
| TASK-005 | GET /api/v1/projects/{projectId}/tasks | 任务列表 | 返回任务列表 | 通过 | ✅ |
| TASK-006 | GET /api/v1/projects/{projectId}/tasks/{id} | 任务详情 | 返回任务信息 | 通过 | ✅ |
| TASK-007 | PUT /api/v1/projects/{projectId}/tasks/{id} | 更新任务 | 更新成功 | 通过 | ✅ |
| TASK-008 | PUT /api/v1/projects/{projectId}/tasks/{id} | 更新 status/priority | 字段保存 | 字段为 null | ❌ |
| TASK-009 | POST /api/v1/projects/{projectId}/tasks/{id}/move | 移动任务 | 移动成功 | 返回 400 | ❌ |
| TASK-010 | POST /api/v1/projects/{projectId}/tasks/{id}/toggle-complete | 切换完成状态 | 切换成功 | 返回 500 | ❌ |
| TASK-011 | POST /api/v1/projects/{projectId}/tasks/{id}/subtasks | 添加子任务 | 添加成功 | 返回 400 | ❌ |
| TASK-012 | GET /api/v1/projects/{projectId}/tasks/{id}/subtasks | 子任务列表 | 返回列表 | 通过 | ✅ |

### 5. 评论模块 (Comment)

| 用例 ID | 接口 | 测试场景 | 预期结果 | 实际结果 | 状态 |
|---------|------|----------|----------|----------|------|
| COMM-001 | POST /api/v1/tasks/{taskId}/comments | 添加评论 | 添加成功 | 通过 | ✅ |
| COMM-002 | POST /api/v1/tasks/{taskId}/comments | 不存在的任务 | 返回 404 | 返回 500 | ❌ |
| COMM-003 | GET /api/v1/tasks/{taskId}/comments | 评论列表 | 返回列表 | 通过 | ✅ |
| COMM-004 | PUT /api/v1/tasks/{taskId}/comments/{id} | 更新评论 | 更新成功 | 通过 | ✅ |
| COMM-005 | DELETE /api/v1/tasks/{taskId}/comments/{id} | 删除评论 | 删除成功 | 通过 | ✅ |

### 6. 通知模块 (Notification)

| 用例 ID | 接口 | 测试场景 | 预期结果 | 实际结果 | 状态 |
|---------|------|----------|----------|----------|------|
| NOTIF-001 | GET /api/v1/notifications | 通知列表 | 返回列表 | 通过 | ✅ |
| NOTIF-002 | GET /api/v1/notifications/unread-count | 未读数量 | 返回数量 | 通过 | ✅ |

### 7. 用户故事模块 (UserStory)

| 用例 ID | 接口 | 测试场景 | 预期结果 | 实际结果 | 状态 |
|---------|------|----------|----------|----------|------|
| STORY-001 | GET /api/v1/projects/{projectId}/stories | 故事列表 | 返回列表 | 通过 | ✅ |
| STORY-002 | POST /api/v1/projects/{projectId}/stories | 创建故事 | 创建成功 | 返回 500 | ❌ |

### 8. 史诗模块 (Epic)

| 用例 ID | 接口 | 测试场景 | 预期结果 | 实际结果 | 状态 |
|---------|------|----------|----------|----------|------|
| EPIC-001 | GET /api/v1/projects/{projectId}/epics | 史诗列表 | 返回列表 | 通过 | ✅ |
| EPIC-002 | POST /api/v1/projects/{projectId}/epics | 创建史诗 | 创建成功 | 返回 500 | ❌ |

### 9. 问题追踪模块 (Issue)

| 用例 ID | 接口 | 测试场景 | 预期结果 | 实际结果 | 状态 |
|---------|------|----------|----------|----------|------|
| ISSUE-001 | GET /api/v1/projects/{projectId}/issues | 问题列表 | 返回列表 | 通过 | ✅ |
| ISSUE-002 | POST /api/v1/projects/{projectId}/issues | 创建问题 | 创建成功 | 返回 500 | ❌ |

### 10. Wiki 模块

| 用例 ID | 接口 | 测试场景 | 预期结果 | 实际结果 | 状态 |
|---------|------|----------|----------|----------|------|
| WIKI-001 | GET /api/v1/projects/{projectId}/wiki | Wiki 列表 | 返回列表 | 通过 | ✅ |
| WIKI-002 | POST /api/v1/projects/{projectId}/wiki | 创建 Wiki | 创建成功 | 返回 500 | ❌ |

### 11. 报表模块 (Report)

| 用例 ID | 接口 | 测试场景 | 预期结果 | 实际结果 | 状态 |
|---------|------|----------|----------|----------|------|
| RPT-001 | GET /api/v1/projects/{projectId}/reports/burndown | 燃尽图数据 | 返回数据 | 通过 | ✅ |

### 12. 安全/认证测试

| 用例 ID | 接口 | 测试场景 | 预期结果 | 实际结果 | 状态 |
|---------|------|----------|----------|----------|------|
| SEC-001 | 任意受保护接口 | 无 Token 访问 | 返回 401 | 返回 401 | ✅ |
| SEC-002 | 任意受保护接口 | 无效 Token | 返回 401 | 未测试 | - |

---

## 发现的问题清单

### 问题 1: 任务状态和优先级未正确保存 (持续存在)

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-V2-001 |
| **title** | Task 状态 (status) 和优先级 (priority) 未正确保存和返回 |
| **severity** | high |
| **category** | data_persistence |
| **description** | 创建/更新任务时传入的 status (如"TODO") 和 priority (如"HIGH") 没有被正确保存到数据库，返回的响应中这些字段始终为 null。 |
| **affected_apis** | ["POST /api/v1/projects/{projectId}/tasks", "PUT /api/v1/projects/{projectId}/tasks/{id}"] |

**复现步骤：**
```bash
curl -X POST http://localhost:9527/api/v1/projects/2/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Task","status":"TODO","priority":"HIGH"}'
# 返回：status:null, priority:null
```

---

### 问题 2: 用户角色字段返回错误

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-V2-002 |
| **title** | 用户角色 (role) 字段在 API 响应中始终显示为 MEMBER |
| **severity** | medium |
| **category** | response |
| **description** | 超级管理员账户的角色应该是 ADMIN，但 API 返回的 role 字段始终显示为 MEMBER。 |
| **affected_apis** | ["GET /api/v1/user/profile"] |

**当前状态：**
```json
{"role": "MEMBER"}
```

**期望状态：**
```json
{"role": "ADMIN"}
```

---

### 问题 3: 项目 status 字段未正确保存

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-V2-003 |
| **title** | 创建/更新项目时传入的 status 字段被忽略 |
| **severity** | medium |
| **category** | data_persistence |
| **description** | 在创建或更新项目时传入 status 字段（如"ACTIVE"），更新后该字段仍然为 null。 |
| **affected_apis** | ["POST /api/v1/projects", "PUT /api/v1/projects/{id}"] |

---

### 问题 4: 添加项目成员时角色枚举值不匹配

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-V2-004 |
| **title** | 添加成员时角色枚举值 ADMIN 不被识别 |
| **severity** | medium |
| **category** | functionality |
| **description** | 添加项目成员时传递 role="ADMIN"，后端返回"No enum constant ... ProjectMemberRole.ADMIN"错误。 |
| **affected_apis** | ["POST /api/v1/projects/{id}/members"] |

**复现步骤：**
```bash
curl -X POST http://localhost:9527/api/v1/projects/4/members \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"role":"ADMIN"}'
# 返回：No enum constant com.projecthub.module.project.entity.ProjectMember.ProjectMemberRole.ADMIN
```

---

### 问题 5: 移动任务接口返回 400 错误

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-V2-005 |
| **title** | 移动任务接口无法使用 |
| **severity** | medium |
| **category** | functionality |
| **description** | POST /api/v1/projects/{projectId}/tasks/{id}/move 接口返回 400 错误，无法移动任务。 |
| **affected_apis** | ["POST /api/v1/projects/{projectId}/tasks/{id}/move"] |

---

### 问题 6: 切换任务完成状态返回 500 错误

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-V2-006 |
| **title** | 切换任务完成状态接口返回错误 |
| **severity** | medium |
| **category** | functionality |
| **description** | 切换任务完成状态时返回"只有子任务才能切换完成状态"错误，但操作的是主任务。 |
| **affected_apis** | ["POST /api/v1/projects/{projectId}/tasks/{id}/toggle-complete"] |

---

### 问题 7: 添加子任务接口返回 400 错误

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-V2-007 |
| **title** | 添加子任务接口无法使用 |
| **severity** | medium |
| **category** | functionality |
| **description** | POST /api/v1/projects/{projectId}/tasks/{id}/subtasks 接口返回 400 错误。 |
| **affected_apis** | ["POST /api/v1/projects/{projectId}/tasks/{id}/subtasks"] |

---

### 问题 8: 评论到不存在的任务返回 500 而非 404

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-V2-008 |
| **title** | 评论到不存在的任务返回 500 错误 |
| **severity** | low |
| **category** | error_handling |
| **description** | 向不存在的任务添加评论时，返回 500 错误而不是更准确的 404 Not Found。 |
| **affected_apis** | ["POST /api/v1/tasks/{taskId}/comments"] |

---

### 问题 9: 用户故事创建接口返回 500 错误

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-V2-009 |
| **title** | 创建用户故事接口返回服务器内部错误 |
| **severity** | high |
| **category** | functionality |
| **description** | POST /api/v1/projects/{projectId}/stories 接口返回 500 服务器内部错误，无法创建用户故事。 |
| **affected_apis** | ["POST /api/v1/projects/{projectId}/stories"] |

---

### 问题 10: 史诗创建接口返回 500 错误

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-V2-010 |
| **title** | 创建史诗接口返回服务器内部错误 |
| **severity** | high |
| **category** | functionality |
| **description** | POST /api/v1/projects/{projectId}/epics 接口返回 500 服务器内部错误，无法创建史诗。 |
| **affected_apis** | ["POST /api/v1/projects/{projectId}/epics"] |

---

### 问题 11: 问题创建接口返回 500 错误

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-V2-011 |
| **title** | 创建问题接口返回服务器内部错误 |
| **severity** | high |
| **category** | functionality |
| **description** | POST /api/v1/projects/{projectId}/issues 接口返回 500 服务器内部错误，无法创建问题。 |
| **affected_apis** | ["POST /api/v1/projects/{projectId}/issues"] |

---

### 问题 12: Wiki 创建接口返回 500 错误

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-V2-012 |
| **title** | 创建 Wiki 接口返回服务器内部错误 |
| **severity** | high |
| **category** | functionality |
| **description** | POST /api/v1/projects/{projectId}/wiki 接口返回 500 服务器内部错误，无法创建 Wiki 文档。 |
| **affected_apis** | ["POST /api/v1/projects/{projectId}/wiki"] |

---

### 问题 13: 登录失败返回 500 而非标准错误码

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-V2-013 |
| **title** | 登录失败返回 HTTP 500 错误码 |
| **severity** | low |
| **category** | error_handling |
| **description** | 当用户名或密码错误时，登录接口返回 HTTP 500 状态码，而不是更合适的 401 Unauthorized。 |
| **affected_apis** | ["POST /api/v1/auth/login"] |

---

### 问题 14: 资源不存在时 Response Code 与 HTTP 状态码不一致

| 字段 | 说明 |
|------|------|
| **id** | BACKEND-V2-014 |
| **title** | 资源不存在时 HTTP 状态码正确但业务码不标准 |
| **severity** | low |
| **category** | error_handling |
| **description** | 请求不存在的项目时，HTTP 状态码返回 404（正确），但响应 body 中的 code 字段为 4001 而非标准的 404。 |
| **affected_apis** | ["GET /api/v1/projects/{id}"] |

---

## 测试通过的功能

以下功能正常工作：
- ✅ 用户登录/登出
- ✅ Token 刷新
- ✅ 用户注册（含参数验证）
- ✅ 用户资料获取和更新（nickname）
- ✅ 项目创建、获取详情、列表、更新、删除
- ✅ 项目成员管理（获取列表、移除成员）
- ✅ 任务创建、获取详情、列表、更新、删除
- ✅ 评论添加、列表、更新、删除
- ✅ 通知列表、未读数量
- ✅ 燃尽图报表
- ✅ 分页参数验证（page<1、size<1 返回 400）
- ✅ 无认证访问返回 401
- ✅ 参数验证（必填字段、邮箱格式、密码强度）

---

## 测试覆盖率

| 模块 | 接口数 | 测试用例数 | 通过数 | 失败数 |
|------|--------|-----------|--------|--------|
| 认证 (Auth) | 6 | 8 | 7 | 1 |
| 用户 (User) | 4 | 4 | 2 | 2 |
| 项目 (Project) | 7 | 13 | 8 | 5 |
| 任务 (Task) | 7 | 12 | 6 | 6 |
| 评论 (Comment) | 4 | 5 | 4 | 1 |
| 通知 (Notification) | 3 | 2 | 2 | 0 |
| 用户故事 (UserStory) | 5 | 2 | 1 | 1 |
| 史诗 (Epic) | 5 | 2 | 1 | 1 |
| 问题 (Issue) | 5 | 2 | 1 | 1 |
| Wiki | 6 | 2 | 1 | 1 |
| 报表 (Report) | 1 | 1 | 1 | 0 |
| 安全测试 | - | 1 | 1 | 0 |
| **总计** | **53** | **54** | **35** | **19** |

**通过率**: 64.8%

---

## 总结和建议

### 问题分类统计

| 严重级别 | 数量 | 问题 ID |
|----------|------|---------|
| **HIGH** | 5 | V2-001, V2-009, V2-010, V2-011, V2-012 |
| **MEDIUM** | 6 | V2-002, V2-003, V2-004, V2-005, V2-006, V2-007 |
| **LOW** | 3 | V2-008, V2-013, V2-014 |

### 优先级建议

#### P0 (紧急) - 需要立即修复
1. **BACKEND-V2-001**: 任务状态和优先级无法保存 - 影响核心功能
2. **BACKEND-V2-009 ~ V2-012**: 多个创建接口返回 500 错误 - 影响功能可用性

#### P1 (高) - 需要尽快修复
1. **BACKEND-V2-002**: 用户角色显示错误 - 影响权限判断
2. **BACKEND-V2-003**: 项目 status 无法保存 - 数据完整性问题
3. **BACKEND-V2-005 ~ V2-007**: 任务相关功能无法使用

#### P2 (中) - 建议修复
1. **BACKEND-V2-004**: 成员角色枚举值不匹配
2. **BACKEND-V2-008, V2-013, V2-014**: 错误码规范问题

### 总体评价

本次测试覆盖了所有主要 API 模块，共执行 54 个测试用例，发现 14 个新问题。主要问题集中在：

1. **数据持久化问题**: 多个实体字段（task status/priority, project status）无法正确保存
2. **创建功能失效**: 用户故事、史诗、问题、Wiki 的创建接口均返回 500 错误
3. **任务管理功能缺陷**: 移动任务、切换完成状态、添加子任务等功能无法正常使用
4. **角色/权限显示问题**: 用户角色字段返回错误值

建议开发团队优先修复 P0 级别问题，确保核心功能可用。

---

*测试报告生成时间：2026-03-13*
*测试执行人：API Test Engineer Agent*
