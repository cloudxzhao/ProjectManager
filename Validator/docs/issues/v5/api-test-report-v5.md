# API Test Report V5

**Test Date**: 2026-03-14
**Tester**: API Test Engineer
**API Base URL**: http://localhost:9527/api/v1
**Total Tests**: 72
**Passed**: 50
**Failed**: 22
**Pass Rate**: 69.4%

---

## Summary

This is the V5 test report for ProjectHub backend API testing. This version includes new issues discovered from additional boundary testing.

---

## Issues Found (12)

### HIGH Priority (6)

| ID | Issue | Category |
|----|-------|----------|
| BACKEND-V5-001 | 任务详情 API 返回的 status 和 priority 为 null | Data |
| BACKEND-V5-002 | UserStory 创建返回 HTTP 500 错误 | Functionality |
| BACKEND-V5-003 | Epic 创建返回 HTTP 500 错误 | Functionality |
| BACKEND-V5-004 | Wiki 创建返回 HTTP 500 错误 | Functionality |
| BACKEND-V5-005 | Issue 创建返回 HTTP 500 错误 | Functionality |
| BACKEND-V5-006 | 项目成员添加返回 HTTP 500 错误 | Functionality |
| BACKEND-V5-012 | Epic 获取不存在返回 HTTP 500 | Functionality |

### MEDIUM Priority (4)

| ID | Issue | Category |
|----|-------|----------|
| BACKEND-V5-007 | 登出后 token 仍然有效 | Security |
| BACKEND-V5-008 | 超长标题输入导致 HTTP 500 错误 | Validation |
| BACKEND-V5-009 | 用户输入未进行 XSS 过滤 | Security |
| BACKEND-V5-010 | 获取不存在的评论返回 HTTP 405 | Functionality |

### LOW Priority (1)

| ID | Issue | Category |
|----|-------|----------|
| BACKEND-V5-011 | 任务不存在时 POST 评论的业务码不一致 | Functionality |

---

## Working Features

- 用户登录和 Token 刷新功能正常
- 登录失败现在返回 HTTP 401 (已修复)
- 用户 profile 更新 nickname 正常工作 (已修复)
- 修改密码功能正常工作 (已修复)
- 项目创建、列表、详情、更新、删除功能正常
- 项目状态保存正常 (ACTIVE/COMPLETED/ARCHIVED)
- 任务创建、列表、更新、删除功能正常
- 任务完成切换功能正常 (toggle-complete)
- 任务移动功能正常
- 子任务创建和列表功能正常
- 评论创建、列表功能正常
- 通知列表、未读数量、全部标记已读功能正常
- Epic 列表功能正常
- 无认证访问返回 401 (已修复)
- 资源不存在时返回 HTTP 404 (已修复)
- 分页参数验证正常 (负数页码、size=0 返回 400)
- 删除不存在的评论返回 HTTP 404 (已修复)
- 项目下 Epic 不存在返回 HTTP 404 (已修复)

---

## New Issues in V5

### BACKEND-V5-010: 获取不存在的评论返回 HTTP 405

**Severity**: MEDIUM
**Category**: Functionality
**Affected API**: `GET /api/v1/tasks/{taskId}/comments/{commentId}`

**Current State**:
```
GET /api/v1/tasks/9/comments/99999
HTTP Status: 405
Response: {"code":400,"message":"请求参数错误"}
```

**Expected State**: 应返回 HTTP 404 和业务码 404，提示评论不存在

---

### BACKEND-V5-011: 任务不存在时 POST 评论的业务码不一致

**Severity**: LOW
**Category**: Functionality
**Affected API**: `POST /api/v1/tasks/{taskId}/comments`

**Current State**:
```
POST /api/v1/tasks/99999/comments
HTTP Status: 404
Response: {"code":5001,"message":"任务不存在"}
```

**Expected State**: 应返回标准 HTTP 404 状态码，业务码应为 404

---

### BACKEND-V5-012: Epic 获取不存在返回 HTTP 500

**Severity**: HIGH
**Category**: Functionality
**Affected API**: `GET /api/v1/projects/{projectId}/epics/{id}`

**Current State**:
```
GET /api/v1/projects/9/epics/99999
HTTP Status: 500
Response: {"code":500,"message":"服务器内部错误"}
```

**Expected State**: 应返回 HTTP 404，提示史诗不存在