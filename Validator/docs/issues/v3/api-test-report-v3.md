# ProjectHub API 测试报告 V3

**测试日期**: 2026-03-13
**测试人员**: API Test Engineer
**API 基础地址**: http://localhost:9527/api/v1

---

## 测试摘要

| 指标 | 数值 |
|------|------|
| 总测试用例 | 60 |
| 通过 | 45 |
| 失败 | 15 |
| 通过率 | 75% |

---

## 发现的问题

### 1. 登录失败返回 HTTP 500 (BACKEND-V3-001)

- **严重程度**: Medium
- **影响接口**: `POST /api/v1/auth/login`
- **问题**: 登录失败（错误密码或不存在用户）返回 HTTP 500，应返回 HTTP 401
- **当前行为**: `{"code":500,"message":"用户名或密码错误"}`
- **期望行为**: HTTP 401 状态码

### 2. 用户 Profile 更新 nickname 无法保存 (BACKEND-V3-002)

- **严重程度**: High
- **影响接口**: `PUT /api/v1/user/profile`
- **问题**: 更新 nickname 后，数据库中 nickname 仍为空字符串
- **复现步骤**:
  1. 登录获取 token
  2. 发送 `PUT /api/v1/user/profile` with `{"nickname":"TestNickname"}`
  3. 发送 `GET /api/v1/user/profile`
  4. nickname 仍为空字符串

### 3. 修改密码返回 HTTP 500 (BACKEND-V3-003)

- **严重程度**: High
- **影响接口**: `PUT /api/v1/user/password`
- **问题**: 修改密码返回服务器内部错误

### 4. 获取不存在的任务 HTTP 状态码不正确 (BACKEND-V3-004)

- **严重程度**: Medium
- **影响接口**: `GET /api/v1/projects/{projectId}/tasks/{id}`
- **问题**: HTTP 200 返回业务码 5001，应返回 HTTP 404

### 5-6. UserStory 创建和获取问题 (BACKEND-V3-005, V3-006)

- **严重程度**: High/Medium
- **问题**: 创建返回 500，获取不存在返回 500

### 7-8. Epic 创建和获取问题 (BACKEND-V3-007, V3-008)

- **严重程度**: High/Medium
- **问题**: 创建返回 500，获取不存在返回 500

### 9-10. Wiki 创建和获取问题 (BACKEND-V3-009, V3-010)

- **严重程度**: High/Medium
- **问题**: 创建返回 500，获取不存在返回 500

### 11-12. Issue 创建和获取问题 (BACKEND-V3-011, V3-012)

- **严重程度**: High/Medium
- **问题**: 创建返回 500，获取不存在返回 500

### 13. 项目成员添加返回 500 (BACKEND-V3-013)

- **严重程度**: High
- **影响接口**: `POST /api/v1/projects/{id}/members`
- **问题**: 添加项目成员返回服务器内部错误

### 14. 评论操作返回状态码不正确 (BACKEND-V3-014)

- **严重程度**: Medium
- **影响接口**: `PUT/DELETE /api/v1/tasks/{taskId}/comments/{id}`
- **问题**: 操作不存在的评论返回 500 而非 404

### 15. 通知标记已读状态码不正确 (BACKEND-V3-015)

- **严重程度**: Low
- **影响接口**: `POST /api/v1/notifications/{id}/read`
- **问题**: 通知不存在时返回 500 而非 404

---

## 正常工作功能

- 用户登录和 Token 刷新功能
- 项目 CRUD 操作
- 项目状态保存 (ACTIVE/COMPLETED/ARCHIVED)
- 任务 CRUD 操作
- 任务状态和优先级保存
- 任务完成切换和移动
- 子任务操作
- 评论功能
- 通知功能
- 无认证访问返回 401

---

## 测试文件

JSON 报告: `D:\data\project\ClaudeStudy\ProjectManagerStudy\Validator\docs\issues\v3\api-test-report-v3.json`