# API Test Engineer Memory

## 项目信息
- 项目名称：ProjectHub
- API 基础地址：http://localhost:9527/api/v1
- 测试账户：admin / Admin123

## V3 测试更新 (2026-03-13)

### 已修复问题
- ~~任务 status/priority 无法保存~~ - 现在可以正确保存
- ~~项目 status 无法保存~~ - 现在可以正确保存 (ACTIVE/COMPLETED/ARCHIVED)
- ~~用户 role 返回 MEMBER~~ - 现在返回正确的 ADMIN
- ~~token 刷新失败~~ - 现在正常工作
- ~~无认证访问返回 403~~ - 现在返回 401

### 持续存在的问题
1. **用户 nickname 无法保存** - profile 更新时 nickname 始终为空字符串 (BACKEND-V3-002)
2. **修改密码返回 500** - PUT /api/v1/user/password (BACKEND-V3-003)
3. **多个创建接口返回 500** - UserStory, Epic, Issue, Wiki, 项目成员添加 (BACKEND-V3-005,007,009,011,013)
4. **资源不存在时 HTTP 状态码不正确** - 返回 HTTP 200 配合业务码 而非 HTTP 404 (多个接口)

### 新增发现问题 (V3)
- 登录失败返回 500 而非 401
- 获取不存在的任务/项目/UserStory/Epic/Wiki/Issue 返回 HTTP 200 配合业务码 5001/4001
- 评论操作不存在返回 500 而非 404
- 通知标记已读不存在返回 500 而非 404

### V3 测试统计
- 测试用例总数：60
- 通过：45 (75%)
- 失败：15
- 新增问题：15 个 (BACKEND-V3-001 ~ V3-015)

## V2 测试更新 (2026-03-13)

### 已修复问题
- 用户 profile 更新 nickname 现在正常工作
- Token 刷新功能正常
- 无认证访问返回 401 (之前是 403)

### 持续存在的问题
1. **任务 status/priority 无法保存** - 创建/更新时传入的值始终返回 null
2. **用户 role 返回 MEMBER** - 管理员显示为 MEMBER 而非 ADMIN
3. **项目 status 无法保存** - 创建/更新时 status 字段为 null
4. **多个创建接口返回 500** - UserStory, Epic, Issue, Wiki 创建均失败
5. **任务子功能不可用** - 移动任务、切换完成状态、添加子任务返回错误

### 新增发现问题
- 添加成员时角色枚举值不匹配 (ADMIN 不被识别)
- 评论到不存在的任务返回 500 而非 404
- 登录失败返回 500 而非 401
- 资源不存在时 HTTP 404 但业务码为 4001

## 原测试发现的关键问题

### 1. 任务状态/优先级未保存 (HIGH)
- 创建/更新任务时 status 和 priority 字段无法正确保存
- 影响的接口：Task CRUD, Task Move

### 2. 用户 profile 更新 bug (CRITICAL) - 已修复
- nickname 参数错误地覆盖了 username
- 导致用户无法登录

### 3. HTTP 状态码问题
- 资源不存在返回 500 而非 404
- ~~未认证访问返回 403 而非 401~~ (已修复为 401)

### 4. Token 刷新失败 - 已修复
- ~~refresh token 接口返回 500 错误~~ (现在正常)

### 5. 用户故事创建失败
- POST /api/v1/projects/{projectId}/stories 返回 500

## V2 测试统计
- 测试用例总数：54
- 通过：35 (64.8%)
- 失败：19
- 新增问题：14 个 (BACKEND-V2-001 ~ V2-014)

## API 端点结构

### 认证 (Auth)
- POST /api/v1/auth/login - 登录 (参数：usernameOrEmail, password)
- POST /api/v1/auth/register - 注册
- POST /api/v1/auth/refresh - 刷新 token
- POST /api/v1/auth/logout - 登出
- POST /api/v1/auth/forgot-password - 忘记密码
- POST /api/v1/auth/reset-password - 重置密码

### 用户 (User)
- GET /api/v1/user/profile - 获取资料
- PUT /api/v1/user/profile - 更新资料 (参数：nickname, avatar)
- POST /api/v1/user/avatar - 上传头像
- PUT /api/v1/user/password - 修改密码

### 项目 (Project)
- GET /api/v1/projects - 项目列表 (分页)
- POST /api/v1/projects - 创建项目
- GET /api/v1/projects/{id} - 项目详情
- PUT /api/v1/projects/{id} - 更新项目
- DELETE /api/v1/projects/{id} - 删除项目
- POST /api/v1/projects/{id}/members - 添加成员
- DELETE /api/v1/projects/{id}/members/{userId} - 移除成员
- GET /api/v1/projects/{id}/members - 成员列表

### 任务 (Task)
- GET /api/v1/projects/{projectId}/tasks - 任务列表 (支持筛选)
- POST /api/v1/projects/{projectId}/tasks - 创建任务
- GET /api/v1/projects/{projectId}/tasks/{id} - 任务详情
- PUT /api/v1/projects/{projectId}/tasks/{id} - 更新任务
- DELETE /api/v1/projects/{projectId}/tasks/{id} - 删除任务
- POST /api/v1/projects/{projectId}/tasks/{id}/move - 移动任务
- GET /api/v1/projects/{projectId}/tasks/{id}/subtasks - 子任务列表
- POST /api/v1/projects/{projectId}/tasks/{id}/toggle-complete - 切换完成状态

### 评论 (Comment)
- GET /api/v1/tasks/{taskId}/comments - 评论列表
- POST /api/v1/tasks/{taskId}/comments - 添加评论
- PUT /api/v1/tasks/{taskId}/comments/{id} - 更新评论
- DELETE /api/v1/tasks/{taskId}/comments/{id} - 删除评论

### 通知 (Notification)
- GET /api/v1/notifications - 通知列表
- GET /api/v1/notifications/unread-count - 未读数量
- POST /api/v1/notifications/{id}/read - 标记已读
- POST /api/v1/notifications/read-all - 全部标记已读

### 用户故事 (UserStory)
- GET /api/v1/projects/{projectId}/stories - 列表
- POST /api/v1/projects/{projectId}/stories - 创建 (当前不可用)
- GET /api/v1/projects/{projectId}/stories/{id} - 详情
- PUT /api/v1/projects/{projectId}/stories/{id} - 更新
- DELETE /api/v1/projects/{projectId}/stories/{id} - 删除

## 响应格式
所有响应遵循统一格式:
```json
{
  "code": 200,
  "message": "success",
  "data": {...},
  "timestamp": 1234567890
}
```

## 测试技巧
- 使用 curl 测试 API，记得加上 -H "Authorization: Bearer $TOKEN"
- 登录参数是 usernameOrEmail 不是 username
- 项目创建需要 startDate 和 endDate (LocalDate 格式)
- 任务状态和优先级传递后未正确保存 (已发现 bug)
- 分页参数：page 从 1 开始，size 最小为 1
