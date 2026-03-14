# ProjectHub Backend API 测试报告 v6

## 测试概要

| 项目 | 值 |
|------|-----|
| 测试日期 | 2026/3/14 |
| 测试人员 | API Test Engineer |
| API 地址 | http://localhost:9527/api/v1 |
| 总测试数 | 22 |
| 通过数 | 17 |
| 失败数 | 5 |
| 通过率 | 77.3% |

## 发现的问题 (5 个)


### BACKEND-V6-001: 登出 - 无认证返回 401

- **严重程度**: low
- **类别**: functionality
- **影响接口**: POST /api/v1/auth/logout
- **描述**: 无认证登出的响应测试
- **当前状态**: HTTP 500
- **期望状态**: HTTP 401
- **复现步骤**:
  1. 不带 Token 发送登出请求
- **标签**: auth, logout


### BACKEND-V6-002: 修改密码 - 旧密码错误返回 401

- **严重程度**: low
- **类别**: functionality
- **影响接口**: POST /api/v1/user/change-password
- **描述**: 错误旧密码修改密码的测试
- **当前状态**: HTTP 404, code=404
- **期望状态**: HTTP 401 或 code=3001
- **复现步骤**:
  1. 登录后获取 Token
  2. 使用错误旧密码修改密码
- **标签**: user, password


### BACKEND-V6-003: 修改密码 - 空新密码返回 400

- **严重程度**: low
- **类别**: validation
- **影响接口**: POST /api/v1/user/change-password
- **描述**: 空新密码的验证测试
- **当前状态**: HTTP 404, code=404
- **期望状态**: HTTP 400 或 code=400
- **复现步骤**:
  1. 登录后获取 Token
  2. 使用空新密码修改密码
- **标签**: user, password, validation


### BACKEND-V6-004: 创建项目返回 200

- **严重程度**: medium
- **类别**: functionality
- **影响接口**: POST /api/v1/projects
- **描述**: 创建项目的测试
- **当前状态**: HTTP 400, code=400
- **期望状态**: HTTP 200, code=200
- **复现步骤**:
  1. 登录后获取 Token
  2. POST 创建项目
- **标签**: project, create


### BACKEND-V6-005: 全部标记已读返回 200

- **严重程度**: medium
- **类别**: functionality
- **影响接口**: POST /api/v1/notifications/mark-all-read
- **描述**: 全部标记已读的测试
- **当前状态**: HTTP 404, code=404
- **期望状态**: HTTP 200, code=200
- **复现步骤**:
  1. 登录后获取 Token
  2. POST 全部标记已读
- **标签**: notification, mark-read

