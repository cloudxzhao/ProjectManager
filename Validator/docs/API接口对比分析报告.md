# 前后端 API 接口一致性对比分析报告

> 生成时间: 2026-03-12
> 分析工具: Claude Code

---

## 一、差异汇总表

| 模块 | URI 差异 | 入参差异 | 响应差异 | 严重程度 |
|------|----------|----------|----------|----------|
| 认证管理 | 2 | 2 | 0 | 🔴 高 |
| 用户管理 | 0 | 1 | 2 | 🟡 中 |
| 项目管理 | 0 | 0 | 3 | 🟡 中 |
| 任务管理 | 9 | 4 | 4 | 🔴 高 |
| 通知管理 | 0 | 0 | 0 | 🟢 低 |
| 史诗管理 | 5 | 0 | 0 | 🟡 中 |
| 用户故事管理 | 5 | 0 | 0 | 🟡 中 |
| 问题追踪管理 | 5 | 0 | 0 | 🟡 中 |
| Wiki 文档管理 | 5 | 0 | 0 | 🟡 中 |
| 任务评论管理 | 4 | 0 | 0 | 🟡 中 |
| 报表管理 | 1 | 0 | 0 | 🟢 低 |

---

## 二、URI 差异详情

### 2.1 完全匹配的接口 (20 个)

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户登录 | POST | /api/v1/auth/login | ✅ 一致 |
| 用户登出 | POST | /api/v1/auth/logout | ✅ 一致 |
| 刷新 Token | POST | /api/v1/auth/refresh | ✅ 一致 |
| 用户注册 | POST | /api/v1/auth/register | ✅ 一致 |
| 获取通知列表 | GET | /api/v1/notifications | ✅ 一致 |
| 全部已读 | POST | /api/v1/notifications/read-all | ✅ 一致 |
| 获取未读数量 | GET | /api/v1/notifications/unread-count | ✅ 一致 |
| 标记已读 | POST | /api/v1/notifications/{id}/read | ✅ 一致 |
| 获取项目列表 | GET | /api/v1/projects | ✅ 一致 |
| 创建项目 | POST | /api/v1/projects | ✅ 一致 |
| 获取项目详情 | GET | /api/v1/projects/{id} | ✅ 一致 |
| 更新项目 | PUT | /api/v1/projects/{id} | ✅ 一致 |
| 删除项目 | DELETE | /api/v1/projects/{id} | ✅ 一致 |
| 获取项目成员 | GET | /api/v1/projects/{id}/members | ✅ 一致 |
| 添加项目成员 | POST | /api/v1/projects/{id}/members | ✅ 一致 |
| 获取任务列表 | GET | /api/v1/projects/{projectId}/tasks | ✅ 一致 |
| 创建任务 | POST | /api/v1/projects/{projectId}/tasks | ✅ 一致 |
| 上传头像 | POST | /api/v1/user/avatar | ✅ 一致 |
| 获取个人资料 | GET | /api/v1/user/profile | ✅ 一致 |
| 更新个人资料 | PUT | /api/v1/user/profile | ✅ 一致 |

### 2.2 前端有但后端缺失的接口 (9 个) 🔴 高风险

| 接口 | 前端路径 | 前端期望 | 后端实际 | 影响 |
|------|----------|----------|----------|------|
| 忘记密码 | POST /api/v1/auth/password/reset | 发送重置邮件 | 不存在 | 功能缺失 |
| 重置密码 | POST /api/v1/auth/password/reset/confirm | 确认重置 | 不存在 | 功能缺失 |
| 获取任务详情 | GET /api/v1/tasks/{id} | 获取详情 | 不存在 | 需改用项目路径 |
| 更新任务 | PUT /api/v1/tasks/{id} | 更新任务 | 不存在 | 需改用项目路径 |
| 删除任务 | DELETE /api/v1/tasks/{id} | 删除任务 | 不存在 | 需改用项目路径 |
| 移动任务 | POST /api/v1/tasks/{id}/move | 移动任务 | 不存在 | 需改用项目路径 |
| 获取子任务 | GET /api/v1/tasks/{id}/subtasks | 获取列表 | 不存在 | 需改用项目路径 |
| 切换完成状态 | POST /api/v1/tasks/{id}/toggle-complete | 切换状态 | 不存在 | 需改用项目路径 |
| 移除项目成员 | DELETE /api/v1/projects/{projectId}/members/{userId} | 移除成员 | 路径参数不同 | 参数名不一致 |

### 2.3 后端有但前端未使用的接口 (36 个)

这些接口在后端已实现，但前端文档中未定义：

**史诗管理 (5 个)**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/projects/{projectId}/epics | 获取史诗列表 |
| POST | /api/v1/projects/{projectId}/epics | 创建史诗 |
| GET | /api/v1/projects/{projectId}/epics/{id} | 获取史诗详情 |
| PUT | /api/v1/projects/{projectId}/epics/{id} | 更新史诗 |
| DELETE | /api/v1/projects/{projectId}/epics/{id} | 删除史诗 |

**用户故事管理 (5 个)**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/projects/{projectId}/stories | 获取故事列表 |
| POST | /api/v1/projects/{projectId}/stories | 创建故事 |
| GET | /api/v1/projects/{projectId}/stories/{id} | 获取故事详情 |
| PUT | /api/v1/projects/{projectId}/stories/{id} | 更新故事 |
| DELETE | /api/v1/projects/{projectId}/stories/{id} | 删除故事 |

**问题追踪管理 (5 个)**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/projects/{projectId}/issues | 获取问题列表 |
| POST | /api/v1/projects/{projectId}/issues | 创建问题 |
| GET | /api/v1/projects/{projectId}/issues/{id} | 获取问题详情 |
| PUT | /api/v1/projects/{projectId}/issues/{id} | 更新问题 |
| DELETE | /api/v1/projects/{projectId}/issues/{id} | 删除问题 |

**Wiki 文档管理 (5 个)**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/projects/{projectId}/wiki | 获取文档树 |
| POST | /api/v1/projects/{projectId}/wiki | 创建文档 |
| GET | /api/v1/projects/{projectId}/wiki/{id} | 获取文档详情 |
| PUT | /api/v1/projects/{projectId}/wiki/{id} | 更新文档 |
| DELETE | /api/v1/projects/{projectId}/wiki/{id} | 删除文档 |

**任务评论管理 (4 个)**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/tasks/{taskId}/comments | 获取评论列表 |
| POST | /api/v1/tasks/{taskId}/comments | 添加评论 |
| PUT | /api/v1/tasks/{taskId}/comments/{id} | 更新评论 |
| DELETE | /api/v1/tasks/{taskId}/comments/{id} | 删除评论 |

**报表管理 (1 个)**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/projects/{projectId}/reports/burndown | 燃尽图数据 |

**任务扩展操作 (4 个)**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/projects/{projectId}/tasks/{id} | 获取任务详情 |
| PUT | /api/v1/projects/{projectId}/tasks/{id} | 更新任务 |
| DELETE | /api/v1/projects/{projectId}/tasks/{id} | 删除任务 |
| POST | /api/v1/projects/{projectId}/tasks/{id}/move | 移动任务 |

**其他 (3 个)**
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/auth/forgot-password | 忘记密码 |
| POST | /api/v1/auth/reset-password | 重置密码 |
| PUT | /api/v1/user/password | 修改密码 |

---

## 三、入参差异详情

### 3.1 认证管理

#### 登录接口
| 参数位置 | 前端定义 | 后端定义 | 差异说明 |
|----------|----------|----------|----------|
| RequestBody | LoginCredentials | LoginRequest | Schema 名称不同，需验证字段是否一致 |

#### 忘记密码/重置密码
| 前端期望 | 后端实际 | 差异说明 |
|----------|----------|----------|
| POST /auth/password/reset | POST /auth/forgot-password | **路径不一致** |
| POST /auth/password/reset/confirm | POST /auth/reset-password | **路径不一致** |

### 3.2 任务管理

#### 获取任务列表
| 参数 | 前端类型 | 后端类型 | 差异说明 |
|------|----------|----------|----------|
| projectId (path) | string | integer | 🔴 **类型不一致** |
| assigneeId (query) | string | integer | 🔴 **类型不一致** |

#### 任务操作路径差异 🔴 高风险

前端使用的是 `/api/v1/tasks/{id}` 路径，后端实际需要 `/api/v1/projects/{projectId}/tasks/{id}` 路径：

| 操作 | 前端路径 | 后端路径 | 影响 |
|------|----------|----------|------|
| 获取详情 | GET /tasks/{id} | GET /projects/{projectId}/tasks/{id} | 接口404 |
| 更新任务 | PUT /tasks/{id} | PUT /projects/{projectId}/tasks/{id} | 接口404 |
| 删除任务 | DELETE /tasks/{id} | DELETE /projects/{projectId}/tasks/{id} | 接口404 |
| 移动任务 | POST /tasks/{id}/move | POST /projects/{projectId}/tasks/{id}/move | 接口404 |
| 获取子任务 | GET /tasks/{id}/subtasks | GET /projects/{projectId}/tasks/{id}/subtasks | 接口404 |
| 切换完成 | POST /tasks/{id}/toggle-complete | POST /projects/{projectId}/tasks/{id}/toggle-complete | 接口404 |

### 3.3 项目管理

#### 移除项目成员
| 参数 | 前端定义 | 后端定义 | 差异说明 |
|------|----------|----------|----------|
| 路径参数 | {projectId}/members/{userId} | {id}/members/{userId} | 项目ID参数名不同 |

---

## 四、响应差异详情

### 4.1 统一响应格式

| 字段 | 前端定义 | 后端定义 | 差异说明 |
|------|----------|----------|----------|
| code | number | integer | 类型一致（JSON 不区分） |
| message | string | string | ✅ 一致 |
| data | T | T | ✅ 一致 |
| timestamp | number | integer | 类型一致 |

### 4.2 Schema 字段差异

#### User 对比

| 字段 | 前端类型 | 后端类型 | 差异说明 |
|------|----------|----------|----------|
| id | string | integer | 🔴 **类型不一致** |
| email | string | string | ✅ 一致 |
| username | string | string | ✅ 一致 |
| avatar | string | string | ✅ 一致 |
| role | UserRole | - | 前端有，后端无 |
| status | - | string | 后端有，前端无 |
| createdAt | string | string | ✅ 一致 |
| updatedAt | string | - | 前端有，后端无 |

#### Task 对比

| 字段 | 前端类型 | 后端类型 | 差异说明 |
|------|----------|----------|----------|
| id | string | integer | 🔴 **类型不一致** |
| projectId | string | integer | 🔴 **类型不一致** |
| title | string | string | ✅ 一致 |
| description | string | string | ✅ 一致 |
| status | TaskStatus | string | 枚举类型不同 |
| priority | Priority | string | 枚举类型不同 |
| assigneeId | string | integer | 🔴 **类型不一致** |
| assigneeName | - | string | 后端新增字段 |
| reporterId | string | - | 前端有，后端无 |
| creatorId | - | integer | 后端有，前端无 |
| storyPoints | integer | integer | ✅ 一致 |
| dueDate | string | string | ✅ 一致 |
| tags | array | - | 前端有，后端无 |
| parentId | string | integer | 🔴 **类型不一致** |
| order | integer | - | 前端有 |
| position | - | integer | 后端有 |
| subtaskCount | integer | - | 前端有，后端无 |
| completedSubtaskCount | integer | - | 前端有，后端无 |
| commentCount | integer | - | 前端有，后端无 |
| createdAt | string | string | ✅ 一致 |
| updatedAt | string | string | ✅ 一致 |

#### Project 对比

| 字段 | 前端类型 | 后端类型 | 差异说明 |
|------|----------|----------|----------|
| id | string | integer | 🔴 **类型不一致** |
| name | string | string | ✅ 一致 |
| description | string | string | ✅ 一致 |
| status | ProjectStatus | string | 枚举类型不同 |
| startDate | string | string | ✅ 一致 |
| endDate | string | string | ✅ 一致 |
| color | string | - | 前端有 |
| themeColor | - | string | 后端有，命名不同 |
| icon | string | string | ✅ 一致 |
| memberCount | integer | - | 前端有，后端无 |
| taskCount | integer | - | 前端有，后端无 |
| completedTaskCount | integer | - | 前端有，后端无 |
| ownerId | string | integer | 🔴 **类型不一致** |
| createdAt | string | string | ✅ 一致 |
| updatedAt | string | - | 前端有，后端无 |

---

## 五、风险评估

### 🔴 高风险问题 (需立即修复)

| 序号 | 问题 | 影响 | 修复建议 |
|------|------|------|----------|
| 1 | 任务接口路径不一致 | 所有任务操作接口404 | 前端需改为项目路径 |
| 2 | ID 类型不一致 (string vs integer) | 数据传递异常 | 统一使用 integer |
| 3 | 忘记密码路径不一致 | 功能不可用 | 前端改用后端路径 |

### 🟡 中风险问题 (建议修复)

| 序号 | 问题 | 影响 | 修复建议 |
|------|------|------|----------|
| 1 | 前端未使用的后端接口 | 功能缺失 | 前端补充调用 |
| 2 | Schema 字段名称差异 | 数据映射错误 | 统一字段命名 |
| 3 | 枚举类型定义不一致 | 类型校验问题 | 统一枚举定义 |

### 🟢 低风险问题 (可延后修复)

| 序号 | 问题 | 影响 | 修复建议 |
|------|------|------|----------|
| 1 | 文档 Schema 命名差异 | 无实际影响 | 更新前端文档 |

---

## 六、修复建议

### 6.1 需要修改前端代码的接口

1. **任务相关接口路径** - 将 `/api/v1/tasks/{id}` 改为 `/api/v1/projects/{projectId}/tasks/{id}`

```typescript
// 修改前
const getTask = (id: string) => request.get(`/tasks/${id}`)

// 修改后
const getTask = (projectId: string, id: string) =>
  request.get(`/projects/${projectId}/tasks/${id}`)
```

2. **认证接口路径** - 修正忘记密码和重置密码的路径

```typescript
// 修改前
const forgotPassword = (email: string) =>
  request.post('/auth/password/reset', { email })

// 修改后
const forgotPassword = (email: string) =>
  request.post('/auth/forgot-password', { email })
```

3. **ID 类型统一** - 将所有 ID 字段从 `string` 改为 `number`

### 6.2 需要修改后端代码的接口

1. **考虑添加简化路径** - 后端可考虑添加 `/api/v1/tasks/{id}` 等简化路径作为别名

2. **统一字段命名** - `color` vs `themeColor`，`order` vs `position`

### 6.3 需要更新前端文档的内容

1. 补充后端已有的史诗、故事、问题、Wiki、评论等模块接口
2. 更新所有 Schema 定义以匹配后端实际返回
3. 统一 ID 类型为 integer

---

## 七、附录：Schema 映射表

| 前端 Schema | 后端 Schema | 说明 |
|-------------|-------------|------|
| LoginCredentials | LoginRequest | 登录请求 |
| RegisterData | RegisterRequest | 注册请求 |
| AuthTokens | AuthResponse | 认证响应 |
| User | UserVO | 用户信息 |
| Task | TaskVO | 任务信息 |
| Project | ProjectVO | 项目信息 |
| ProjectMember | ProjectMember | 项目成员 |
| Notification | NotificationVO | 通知信息 |
| - | EpicVO | 史诗信息 |
| - | UserStoryVO | 用户故事 |
| - | IssueVO | 问题信息 |
| - | WikiVO | Wiki 文档 |
| - | CommentVO | 评论信息 |

---

*报告生成完毕*