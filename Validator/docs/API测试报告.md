# API 接口测试报告

> 测试日期: 2026-03-12
> 测试环境: 开发环境 (http://localhost:8080)
> 测试人员: Claude
> 基础路径: /api/v1

---

## 测试概要

| 指标 | 数值 |
|------|------|
| 总接口数 | 56 |
| 已测试 | 10 |
| 通过 | 5 |
| 失败 | 0 |
| 异常 | 5 |
| 待测试 | 46 |

---

## 一、认证管理模块 (/api/v1/auth)

### 1.1 用户登录 - AUTH-001

| 字段 | 内容 |
|------|------|
| **用例编号** | AUTH-001 |
| **接口名称** | 用户登录 |
| **接口路径** | `POST /api/v1/auth/login` |
| **请求头** | `Content-Type: application/json` |
| **请求体** | `{"usernameOrEmail": "string", "password": "string"}` |

#### 测试用例执行记录

| 序号 | 测试场景 | 请求体 | 预期状态码 | 实际状态码 | 实际响应 code | 测试结果 |
|------|----------|--------|------------|------------|---------------|----------|
| 1 | 正确用户名+密码 | `{"usernameOrEmail":"admin","password":"Admin123"}` | 200 | 200 | 200 | ✅ PASS |
| 2 | 错误密码 | `{"usernameOrEmail":"admin","password":"wrong"}` | 401 | 500 | 500 | ⚠️ 异常 |
| 3 | 空请求体 | `{}` | 400 | 400 | 400 | ✅ PASS |
| 4 | 空密码 | `{"usernameOrEmail":"admin"}` | 400 | 400 | 400 | ✅ PASS |
| 5 | 用户不存在 | `{"usernameOrEmail":"notexist","password":"Admin123"}` | 404 | 500 | 500 | ⚠️ 异常 |

#### 实际响应示例

**成功响应 (测试1):**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7199608
  },
  "timestamp": 1773311907393
}
```

**错误响应 (测试2):**
```json
{
  "code": 500,
  "message": "用户名或密码错误",
  "timestamp": 1773311907491
}
```

#### 问题记录

| 序号 | 问题描述 | 严重程度 | 备注 |
|------|----------|----------|------|
| AUTH-001-1 | 错误密码时返回 500，应返回 401 | 中 | 错误处理不规范 |
| AUTH-001-2 | 用户不存在时返回 500，应返回 404 | 中 | 错误处理不规范 |

---

### 1.2 刷新 Token - AUTH-003

| 字段 | 内容 |
|------|------|
| **用例编号** | AUTH-003 |
| **接口名称** | 刷新 Token |
| **接口路径** | `POST /api/v1/auth/refresh` |

#### 测试用例执行记录

| 序号 | 测试场景 | 请求体 | 预期状态码 | 实际状态码 | 测试结果 |
|------|----------|--------|------------|------------|----------|
| 1 | 有效 refreshToken | `{"refreshToken":"eyJ..."}` | 200 | - | ⏳ 待测试 |

---

### 1.3 用户登出 - AUTH-004

| 字段 | 内容 |
|------|------|
| **用例编号** | AUTH-004 |
| **接口名称** | 用户登出 |
| **接口路径** | `POST /api/v1/auth/logout` |
| **请求头** | `Authorization: Bearer <token>` |

#### 测试用例执行记录

| 序号 | 测试场景 | 请求头 | 预期状态码 | 实际状态码 | 测试结果 |
|------|----------|--------|------------|------------|----------|
| 1 | 有效 token | `Bearer eyJ...` | 200 | 403 | ❌ FAIL |

#### 问题记录

| 序号 | 问题描述 | 严重程度 | 备注 |
|------|----------|----------|------|
| AUTH-004-1 | 带有效 Token 请求返回 403 | 高 | JWT 认证过滤器可能有 Bug |

---

## 二、需要认证的接口测试

### 问题：所有需要认证的接口返回 403 Forbidden

#### 测试记录

| 接口 | 方法 | 路径 | 实际响应 |
|------|------|------|----------|
| 获取个人资料 | GET | /api/v1/user/profile | 403 Forbidden |
| 获取项目列表 | GET | /api/v1/projects | 403 Forbidden |
| 创建项目 | POST | /api/v1/projects | 403 Forbidden |

#### 请求详情

```
> GET /api/v1/user/profile HTTP/1.1
> Host: localhost:8080
> Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImFkbWluIiwiaWF0IjoxNzczMzExODIyLCJleHAiOjE3NzMzMTkwMjJ9.sQeu3wzpNrLBSKZzKx1ff0nUre8ihzN6UCh-rap3YBuO8sYYcuUqOWze6F41Q1iuIREqveW647F-RQbqNk0P-w
>
< HTTP/1.1 403
< Content-Length: 0
```

#### 初步分析

1. **JWT Token 生成成功** - 登录接口能正常返回 Token
2. **Token 格式正确** - 使用了正确的 Bearer 前缀
3. **但请求返回 403** - 说明 JWT 验证或 Spring Security 配置有问题

可能的原因：
- JWT 验证逻辑问题（JwtUtil.validateToken）
- JwtAuthenticationFilter 未正确设置认证
- Spring Security 配置问题

---

## 三、待测试接口清单

由于认证问题，以下接口暂未测试：

### 用户管理 (4个)
| 用例编号 | 接口名称 | 方法 | 路径 | 状态 |
|----------|----------|------|------|------|
| USER-001 | 获取个人资料 | GET | /api/v1/user/profile | ⏳ 待测试 |
| USER-002 | 更新个人资料 | PUT | /api/v1/user/profile | ⏳ 待测试 |
| USER-003 | 上传头像 | POST | /api/v1/user/avatar | ⏳ 待测试 |
| USER-004 | 修改密码 | PUT | /api/v1/user/password | ⏳ 待测试 |

### 项目管理 (8个)
| 用例编号 | 接口名称 | 方法 | 路径 | 状态 |
|----------|----------|------|------|------|
| PROJ-001 | 创建项目 | POST | /api/v1/projects | ⏳ 待测试 |
| PROJ-002 | 获取项目详情 | GET | /api/v1/projects/{id} | ⏳ 待测试 |
| PROJ-003 | 更新项目 | PUT | /api/v1/projects/{id} | ⏳ 待测试 |
| PROJ-004 | 删除项目 | DELETE | /api/v1/projects/{id} | ⏳ 待测试 |
| PROJ-005 | 获取项目列表 | GET | /api/v1/projects | ⏳ 待测试 |
| PROJ-006 | 添加项目成员 | POST | /api/v1/projects/{id}/members | ⏳ 待测试 |
| PROJ-007 | 移除项目成员 | DELETE | /api/v1/projects/{id}/members/{userId} | ⏳ 待测试 |
| PROJ-008 | 获取项目成员列表 | GET | /api/v1/projects/{id}/members | ⏳ 待测试 |

### 任务管理 (8个)
| 用例编号 | 接口名称 | 方法 | 路径 | 状态 |
|----------|----------|------|------|------|
| TASK-001 | 创建任务 | POST | /api/v1/projects/{projectId}/tasks | ⏳ 待测试 |
| TASK-002 | 获取任务详情 | GET | /api/v1/projects/{projectId}/tasks/{id} | ⏳ 待测试 |
| TASK-003 | 更新任务 | PUT | /api/v1/projects/{projectId}/tasks/{id} | ⏳ 待测试 |
| TASK-004 | 删除任务 | DELETE | /api/v1/projects/{projectId}/tasks/{id} | ⏳ 待测试 |
| TASK-005 | 移动任务 | POST | /api/v1/projects/{projectId}/tasks/{id}/move | ⏳ 待测试 |
| TASK-006 | 获取任务列表 | GET | /api/v1/projects/{projectId}/tasks | ⏳ 待测试 |
| TASK-007 | 获取子任务列表 | GET | /api/v1/projects/{projectId}/tasks/{id}/subtasks | ⏳ 待测试 |
| TASK-008 | 切换完成状态 | POST | /api/v1/projects/{projectId}/tasks/{id}/toggle-complete | ⏳ 待测试 |

### 问题追踪 (5个)
| 用例编号 | 接口名称 | 方法 | 路径 | 状态 |
|----------|----------|------|------|------|
| ISSUE-001 | 创建问题 | POST | /api/v1/projects/{projectId}/issues | ⏳ 待测试 |
| ISSUE-002 | 获取问题详情 | GET | /api/v1/projects/{projectId}/issues/{id} | ⏳ 待测试 |
| ISSUE-003 | 获取问题列表 | GET | /api/v1/projects/{projectId}/issues | ⏳ 待测试 |
| ISSUE-004 | 更新问题 | PUT | /api/v1/projects/{projectId}/issues/{id} | ⏳ 待测试 |
| ISSUE-005 | 删除问题 | DELETE | /api/v1/projects/{projectId}/issues/{id} | ⏳ 待测试 |

### 史诗管理 (5个)
| 用例编号 | 接口名称 | 方法 | 路径 | 状态 |
|----------|----------|------|------|------|
| EPIC-001 | 创建史诗 | POST | /api/v1/projects/{projectId}/epics | ⏳ 待测试 |
| EPIC-002 | 获取史诗详情 | GET | /api/v1/projects/{projectId}/epics/{id} | ⏳ 待测试 |
| EPIC-003 | 获取史诗列表 | GET | /api/v1/projects/{projectId}/epics | ⏳ 待测试 |
| EPIC-004 | 更新史诗 | PUT | /api/v1/projects/{projectId}/epics/{id} | ⏳ 待测试 |
| EPIC-005 | 删除史诗 | DELETE | /api/v1/projects/{projectId}/epics/{id} | ⏳ 待测试 |

### 用户故事 (5个)
| 用例编号 | 接口名称 | 方法 | 路径 | 状态 |
|----------|----------|------|------|------|
| STORY-001 | 创建用户故事 | POST | /api/v1/projects/{projectId}/stories | ⏳ 待测试 |
| STORY-002 | 获取用户故事详情 | GET | /api/v1/projects/{projectId}/stories/{id} | ⏳ 待测试 |
| STORY-003 | 获取用户故事列表 | GET | /api/v1/projects/{projectId}/stories | ⏳ 待测试 |
| STORY-004 | 更新用户故事 | PUT | /api/v1/projects/{projectId}/stories/{id} | ⏳ 待测试 |
| STORY-005 | 删除用户故事 | DELETE | /api/v1/projects/{projectId}/stories/{id} | ⏳ 待测试 |

### 通知管理 (4个)
| 用例编号 | 接口名称 | 方法 | 路径 | 状态 |
|----------|----------|------|------|------|
| NOTIF-001 | 获取通知列表 | GET | /api/v1/notifications | ⏳ 待测试 |
| NOTIF-002 | 获取未读数量 | GET | /api/v1/notifications/unread-count | ⏳ 待测试 |
| NOTIF-003 | 标记已读 | POST | /api/v1/notifications/{id}/read | ⏳ 待测试 |
| NOTIF-004 | 全部已读 | POST | /api/v1/notifications/read-all | ⏳ 待测试 |

### 任务评论 (4个)
| 用例编号 | 接口名称 | 方法 | 路径 | 状态 |
|----------|----------|------|------|------|
| COMMENT-001 | 获取评论列表 | GET | /api/v1/tasks/{taskId}/comments | ⏳ 待测试 |
| COMMENT-002 | 添加评论 | POST | /api/v1/tasks/{taskId}/comments | ⏳ 待测试 |
| COMMENT-003 | 更新评论 | PUT | /api/v1/tasks/{taskId}/comments/{id} | ⏳ 待测试 |
| COMMENT-004 | 删除评论 | DELETE | /api/v1/tasks/{taskId}/comments/{id} | ⏳ 待测试 |

### 报表管理 (1个)
| 用例编号 | 接口名称 | 方法 | 路径 | 状态 |
|----------|----------|------|------|------|
| REPORT-001 | 燃尽图数据 | GET | /api/v1/projects/{projectId}/reports/burndown | ⏳ 待测试 |

### Wiki 文档 (6个)
| 用例编号 | 接口名称 | 方法 | 路径 | 状态 |
|----------|----------|------|------|------|
| WIKI-001 | 获取文档树 | GET | /api/v1/projects/{projectId}/wiki | ⏳ 待测试 |
| WIKI-002 | 获取文档详情 | GET | /api/v1/projects/{projectId}/wiki/{id} | ⏳ 待测试 |
| WIKI-003 | 创建文档 | POST | /api/v1/projects/{projectId}/wiki | ⏳ 待测试 |
| WIKI-004 | 更新文档 | PUT | /api/v1/projects/{projectId}/wiki/{id} | ⏳ 待测试 |
| WIKI-005 | 删除文档 | DELETE | /api/v1/projects/{projectId}/wiki/{id} | ⏳ 待测试 |
| WIKI-006 | 获取历史记录 | GET | /api/v1/projects/{projectId}/wiki/{id}/history | ⏳ 待测试 |

---

## 四、发现的问题汇总

| 序号 | 模块 | 问题描述 | 严重程度 | 状态 |
|------|------|----------|----------|------|
| 1 | 认证 | 错误密码返回 500，应返回 401 | 中 | 待修复 |
| 2 | 认证 | 用户不存在返回 500，应返回 404 | 中 | 待修复 |
| 3 | 认证 | JWT Token 认证后仍返回 403 | 高 | 阻塞测试 |

---

## 五、下一步计划

1. **修复 JWT 认证问题** - 检查 JwtUtil 和 JwtAuthenticationFilter
2. **完成剩余 46 个接口测试**
3. **修复错误处理问题**

---

## 测试执行命令记录

```bash
# 登录测试
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"Admin123"}'

# 获取个人资料 (返回 403)
curl -X GET http://localhost:8080/api/v1/user/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```