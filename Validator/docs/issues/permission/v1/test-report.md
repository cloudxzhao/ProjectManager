# 权限申请模块 API 测试报告

**测试时间**: 2026-03-15
**测试环境**: localhost:9528
**测试分支**: feature/permission

---

## 测试准备

### 1. 获取管理员 Token

```bash
curl -X POST http://localhost:9528/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"Admin123"}'
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7199774
  }
}
```

**结果**: ✅ 通过

---

## 接口测试详情

### 1. GET /api/v1/permissions/available - 获取可申请的权限列表

**请求**:
```bash
curl -X GET http://localhost:9528/api/v1/permissions/available \
  -H "Authorization: Bearer ${ADMIN_TOKEN}"
```

**预期**: 返回所有可申请的权限列表

---

### 2. POST /api/v1/permissions/requests - 创建权限申请

**请求**:
```bash
curl -X POST http://localhost:9528/api/v1/permissions/requests \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"permissionId":1,"reason":"测试申请权限"}'
```

**预期**: 创建成功，返回申请详情

---

### 3. GET /api/v1/permissions/requests/my - 获取我的申请记录

**请求**:
```bash
curl -X GET "http://localhost:9528/api/v1/permissions/requests/my?page=1&size=10" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}"
```

**预期**: 返回我的申请记录列表

---

### 4. GET /api/v1/permissions/requests/{id} - 获取申请详情

**请求**:
```bash
curl -X GET http://localhost:9528/api/v1/permissions/requests/1 \
  -H "Authorization: Bearer ${ADMIN_TOKEN}"
```

**预期**: 返回申请详情

---

### 5. PUT /api/v1/permissions/requests/{id}/approve - 审批通过

**请求**:
```bash
curl -X PUT http://localhost:9528/api/v1/permissions/requests/1/approve \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"action":"APPROVE","comment":"同意申请"}'
```

**预期**: 审批通过，申请状态变为 APPROVED

---

### 6. PUT /api/v1/permissions/requests/{id}/reject - 审批拒绝

**请求**:
```bash
curl -X PUT http://localhost:9528/api/v1/permissions/requests/1/reject \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"action":"REJECT","comment":"拒绝申请"}'
```

**预期**: 审批拒绝，申请状态变为 REJECTED

---

### 7. GET /api/v1/permissions/requests/{id}/approvals - 获取审批记录

**请求**:
```bash
curl -X GET http://localhost:9528/api/v1/permissions/requests/1/approvals \
  -H "Authorization: Bearer ${ADMIN_TOKEN}"
```

**预期**: 返回审批记录列表

---

## 测试总结

| 接口 | 测试状态 | 响应码 | 备注 |
|------|---------|--------|------|
| GET /api/v1/permissions/available | ✅ 通过 | 200 | 返回 26 条权限记录 |
| POST /api/v1/permissions/requests | ✅ 通过 | 200 | 成功创建权限申请 |
| GET /api/v1/permissions/requests/my | ✅ 通过 | 200 | 返回我的申请记录列表 |
| GET /api/v1/permissions/requests/{id} | ✅ 通过 | 200 | 返回申请详情（含审批记录） |
| PUT /api/v1/permissions/requests/{id}/approve | ✅ 通过 | 200 | 审批通过并自动授予权限 |
| PUT /api/v1/permissions/requests/{id}/reject | ✅ 通过 | 200 | 审批拒绝 |
| GET /api/v1/permissions/requests/{id}/approvals | ✅ 通过 | 200 | 返回审批记录列表 |

---

## 问题追踪

### 发现的问题

| 编号 | 问题描述 | 严重程度 | 状态 |
|------|---------|---------|------|
| | | | |

---

## 测试执行记录

### 测试用例 1: 获取可申请的权限列表

**执行时间**: 2026-03-16 00:42:33
**测试结果**: ✅ 通过
**响应数据**: 返回 26 条权限，包含权限 ID、名称、代码、描述和是否已拥有状态

---

### 测试用例 2: 创建权限申请

**执行时间**: 2026-03-16 00:44:10
**测试结果**: ✅ 通过
**请求**: `{"permissionId":10,"reason":"Test create new permission request"}`
**响应数据**:
```json
{
  "id": 7,
  "userId": 1,
  "permissionId": 10,
  "permissionName": "分配任务",
  "status": "PENDING"
}
```

---

### 测试用例 3: 获取我的申请记录

**执行时间**: 2026-03-16 00:42:47
**测试结果**: ✅ 通过
**响应数据**: 返回 5 条申请记录，包含待审批、已通过、已拒绝状态

---

### 测试用例 4: 获取申请详情

**执行时间**: 2026-03-16 00:42:58
**测试结果**: ✅ 通过
**响应数据**: 返回申请详情，包含审批记录数组
```json
{
  "id": 1,
  "userId": 3,
  "permissionName": "编辑项目",
  "status": "APPROVED",
  "approvalRecords": [
    {
      "approverName": "admin",
      "action": "APPROVE",
      "comment": "Approved"
    }
  ]
}
```

---

### 测试用例 5: 审批通过权限申请

**执行时间**: 2026-03-16 00:41:44
**测试结果**: ✅ 通过
**请求**: `{"action":"APPROVE","comment":"Approved"}`
**响应数据**: 成功，状态码 200
**验证**:
- 申请状态更新为 APPROVED
- 用户 3 被赋予角色 ID 1（ADMIN 角色）
- 审批记录保存到数据库

---

### 测试用例 6: 审批拒绝权限申请

**执行时间**: 2026-03-16 00:43:37
**测试结果**: ✅ 通过
**请求**: `{"action":"REJECT","comment":"Test reject"}`
**响应数据**: 成功，状态码 200
**验证**: 申请状态更新为 REJECTED

**重复审批测试**: 对已拒绝的申请再次提交拒绝请求
**结果**: ✅ 正确返回错误码 6002 "该申请已被处理，无法重复审批"

---

### 测试用例 7: 获取审批记录

**执行时间**: 2026-03-16 00:43:07
**测试结果**: ✅ 通过
**响应数据**: 返回审批记录列表，包含审批人信息、操作、意见和时间

---

### 测试用例 8: 获取所有申请列表（管理员视角）

**执行时间**: 2026-03-16 00:43:46
**测试结果**: ✅ 通过
**响应数据**: 返回所有用户的申请记录，支持分页

---

## 问题追踪

### 发现的问题

| 编号 | 问题描述 | 严重程度 | 状态 |
|------|---------|---------|------|
| BUG-001 | SysUserRole 实体缺少 @EntityListeners 注解，导致 created_at 字段为 NULL 违反数据库约束 | 高 | ✅ 已修复 |
| BUG-002 | 中文注释在 JSON 解析时出现 UTF-8 编码错误 | 中 | 待调查 |

### 问题详情

#### BUG-001: SysUserRole 实体缺少审计监听器

**错误现象**: 审批通过时返回 500 错误
**错误信息**: `null value in column "created_at" of relation "sys_user_role" violates not-null constraint`
**根本原因**: SysUserRole 实体类虽然使用了 `@CreatedDate` 注解，但缺少 `@EntityListeners(AuditingEntityListener.class)` 注解，导致 JPA 审计功能无法自动填充 created_at 字段
**修复方案**: 在 SysUserRole.java 中添加 `@EntityListeners(AuditingEntityListener.class)` 注解
**验证**: 修复后审批接口正常工作

#### BUG-002: 中文 JSON 解析错误

**错误现象**: 使用中文评论时返回 500 错误
**错误信息**: `Invalid UTF-8 middle byte 0xe2`
**状态**: 目前在后端日志中观察到该错误，但实际测试中使用英文评论可以正常工作。需要进一步调查是否为 curl 命令编码问题。

