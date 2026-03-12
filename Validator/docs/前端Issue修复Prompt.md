# 前端 Issue 修复任务 Prompt

## 项目信息

| 属性 | 值 |
|------|-----|
| 项目名称 | ProjectHub |
| 模块 | Frontend (Vue 3 + TypeScript) |
| 问题数量 | 8 个 |
| 后端 API 基础地址 | `http://localhost:8080/api/v1` |

---

## 修复优先级

### 🔴 优先级 1 - 阻断性问题（必须立即修复）

1. **FRONTEND-001**: 任务操作接口路径缺少 projectId 参数
2. **FRONTEND-003**: 认证接口路径不一致

### 🔴 优先级 2 - 高优先级问题（应在本周内修复）

3. **FRONTEND-002**: ID 类型定义不一致

### 🟡 优先级 3 - 中优先级问题（建议修复）

4. **FRONTEND-004**: 移除项目成员接口路径参数名不一致
5. **FRONTEND-005**: 缺少史诗管理模块接口调用
6. **FRONTEND-006**: 缺少用户故事管理模块接口调用
7. **FRONTEND-007**: 缺少问题追踪管理模块接口调用

### 🟢 优先级 4 - 低优先级问题（可延后修复）

8. **FRONTEND-008**: 缺少 Wiki、评论、报表等扩展模块接口调用

---

## 详细修复任务

### Issue FRONTEND-001: 任务操作接口路径缺少 projectId 参数

**严重程度**: critical (阻断)

**问题描述**:
前端任务相关接口使用 `/api/v1/tasks/{id}` 路径，但后端实际路径需要包含 projectId 参数。这会导致所有任务详情、更新、删除、移动等操作返回 404 错误。

**受影响的 API**:
- `GET /api/v1/tasks/{id}` → 应改为 `GET /api/v1/projects/{projectId}/tasks/{id}`
- `PUT /api/v1/tasks/{id}` → 应改为 `PUT /api/v1/projects/{projectId}/tasks/{id}`
- `DELETE /api/v1/tasks/{id}` → 应改为 `DELETE /api/v1/projects/{projectId}/tasks/{id}`
- `POST /api/v1/tasks/{id}/move` → 应改为 `POST /api/v1/projects/{projectId}/tasks/{id}/move`
- `GET /api/v1/tasks/{id}/subtasks` → 应改为 `GET /api/v1/projects/{projectId}/tasks/{id}/subtasks`
- `POST /api/v1/tasks/{id}/toggle-complete` → 应改为 `POST /api/v1/projects/{projectId}/tasks/{id}/toggle-complete`

**修复方案**:
```typescript
// 修改前 (src/api/task.ts)
export const getTask = (id: string) => request.get(`/tasks/${id}`)
export const updateTask = (id: string, data: Task) => request.put(`/tasks/${id}`, data)
export const deleteTask = (id: string) => request.delete(`/tasks/${id}`)
export const moveTask = (id: string, position: number) => request.post(`/tasks/${id}/move`, { position })
export const getSubtasks = (id: string) => request.get(`/tasks/${id}/subtasks`)
export const toggleComplete = (id: string) => request.post(`/tasks/${id}/toggle-complete`)

// 修改后 - 需要从项目上下文获取 projectId
export const getTask = (projectId: number, id: number) =>
  request.get(`/projects/${projectId}/tasks/${id}`)

export const updateTask = (projectId: number, id: number, data: Task) =>
  request.put(`/projects/${projectId}/tasks/${id}`, data)

export const deleteTask = (projectId: number, id: number) =>
  request.delete(`/projects/${projectId}/tasks/${id}`)

export const moveTask = (projectId: number, id: number, position: number) =>
  request.post(`/projects/${projectId}/tasks/${id}/move`, { position })

export const getSubtasks = (projectId: number, id: number) =>
  request.get(`/projects/${projectId}/tasks/${id}/subtasks`)

export const toggleComplete = (projectId: number, id: number) =>
  request.post(`/projects/${projectId}/tasks/${id}/toggle-complete`)
```

**需要修改的文件**:
- `src/api/task.ts` - 修改所有任务 API 调用方法
- `src/views/task/TaskDetail.vue` - 页面调用处添加 projectId
- `src/views/task/TaskEdit.vue` - 页面调用处添加 projectId
- `src/stores/task.ts` - Store 方法签名修改

**验证方式**: 调用任务相关功能，确认不再返回 404 错误。

---

### Issue FRONTEND-003: 认证接口路径不一致

**严重程度**: high

**问题描述**:
前端忘记密码和重置密码的接口路径与后端不匹配，导致密码找回功能无法使用。

**受影响的 API**:
| 前端当前路径 | 后端实际路径 |
|-------------|-------------|
| POST /api/v1/auth/password/reset | POST /api/v1/auth/forgot-password |
| POST /api/v1/auth/password/reset/confirm | POST /api/v1/auth/reset-password |

**修复方案**:
```typescript
// src/api/auth.ts

// 修改前
export const forgotPassword = (email: string) =>
  request.post('/auth/password/reset', { email })

export const resetPassword = (token: string, password: string) =>
  request.post('/auth/password/reset/confirm', { token, password })

// 修改后
export const forgotPassword = (email: string) =>
  request.post('/auth/forgot-password', { email })

export const resetPassword = (token: string, password: string) =>
  request.post('/auth/reset-password', { token, password })
```

**需要修改的文件**:
- `src/api/auth.ts`
- `src/views/auth/ForgotPassword.vue`
- `src/views/auth/ResetPassword.vue`

---

### Issue FRONTEND-002: ID 类型定义不一致

**严重程度**: high

**问题描述**:
前端所有实体的 ID 字段定义为 string 类型，但后端返回的是 integer 类型。这会导致类型校验失败和潜在的运行时错误。

**修复方案**:
```typescript
// src/types/user.ts

// 修改前
interface User {
  id: string
  username: string
  email: string
  avatar: string
  role: UserRole
  createdAt: string
  updatedAt: string
}

// 修改后
interface User {
  id: number
  username: string
  email: string
  avatar: string
  role: UserRole
  createdAt: string
  updatedAt?: string
}
```

**需要修改的文件**:
- `src/types/user.ts` - User.id 从 string 改为 number
- `src/types/task.ts` - Task.id, Task.projectId, Task.assigneeId, Task.parentId 从 string 改为 number
- `src/types/project.ts` - Project.id, Project.ownerId 从 string 改为 number
- `src/types/notification.ts` - Notification.id 从 string 改为 number

---

### Issue FRONTEND-004: 移除项目成员接口路径参数名不一致

**严重程度**: medium

**问题描述**:
前端使用 `{projectId}` 作为路径参数名，后端使用 `{id}`。

**当前路径**: `DELETE /api/v1/projects/{projectId}/members/{userId}`
**后端路径**: `DELETE /api/v1/projects/{id}/members/{userId}`

**修复方案**:
此问题为文档/注释问题，实际调用时后端会将 `{id}` 解析为项目 ID。更新 API 文档注释即可。

**需要修改的文件**:
- `src/api/project.ts` - 更新注释/文档
- `docs/api/API 前端接口文档.md`

---

### Issue FRONTEND-005 ~ FRONTEND-007: 缺少功能模块接口调用

**严重程度**: medium

这些问题是新增功能模块，可以根据项目计划分阶段实现。

#### FRONTEND-005: 史诗管理模块
```typescript
// src/api/epic.ts (新建)
import request from './request'

export interface Epic {
  id: number
  projectId: number
  name: string
  description: string
  color: string
  startDate: string
  endDate: string
  status: string
  createdAt: string
  updatedAt: string
}

export const getEpics = (projectId: number) =>
  request.get<Epic[]>(`/projects/${projectId}/epics`)

export const getEpic = (projectId: number, id: number) =>
  request.get<Epic>(`/projects/${projectId}/epics/${id}`)

export const createEpic = (projectId: number, data: Partial<Epic>) =>
  request.post<Epic>(`/projects/${projectId}/epics`, data)

export const updateEpic = (projectId: number, id: number, data: Partial<Epic>) =>
  request.put<Epic>(`/projects/${projectId}/epics/${id}`, data)

export const deleteEpic = (projectId: number, id: number) =>
  request.delete(`/projects/${projectId}/epics/${id}`)
```

#### FRONTEND-006: 用户故事管理模块
```typescript
// src/api/story.ts (新建)
export interface UserStory {
  id: number
  projectId: number
  epicId?: number
  title: string
  description: string
  status: string
  priority: string
  assigneeId?: number
  storyPoints?: number
  createdAt: string
  updatedAt: string
}

export const getStories = (projectId: number, params?: QueryParams) =>
  request.get<UserStory[]>(`/projects/${projectId}/stories`, { params })

export const getStory = (projectId: number, id: number) =>
  request.get<UserStory>(`/projects/${projectId}/stories/${id}`)

export const createStory = (projectId: number, data: Partial<UserStory>) =>
  request.post<UserStory>(`/projects/${projectId}/stories`, data)

export const updateStory = (projectId: number, id: number, data: Partial<UserStory>) =>
  request.put<UserStory>(`/projects/${projectId}/stories/${id}`, data)

export const deleteStory = (projectId: number, id: number) =>
  request.delete(`/projects/${projectId}/stories/${id}`)
```

#### FRONTEND-007: 问题追踪管理模块
```typescript
// src/api/issue.ts (新建)
export interface Issue {
  id: number
  projectId: number
  title: string
  description: string
  type: string  // BUG, FEATURE, IMPROVEMENT
  severity: string  // CRITICAL, HIGH, MEDIUM, LOW
  status: string
  priority: string
  assigneeId?: number
  reporterId: number
  createdAt: string
  updatedAt: string
}

export const getIssues = (projectId: number, params?: QueryParams) =>
  request.get<Issue[]>(`/projects/${projectId}/issues`, { params })

export const getIssue = (projectId: number, id: number) =>
  request.get<Issue>(`/projects/${projectId}/issues/${id}`)

export const createIssue = (projectId: number, data: Partial<Issue>) =>
  request.post<Issue>(`/projects/${projectId}/issues`, data)

export const updateIssue = (projectId: number, id: number, data: Partial<Issue>) =>
  request.put<Issue>(`/projects/${projectId}/issues/${id}`, data)

export const deleteIssue = (projectId: number, id: number) =>
  request.delete(`/projects/${projectId}/issues/${id}`)
```

---

## 验证检查清单

完成修复后，请验证以下场景：

- [ ] 任务列表页面可以正常获取任务数据
- [ ] 任务详情页可以正常显示任务信息
- [ ] 创建、编辑、删除任务功能正常
- [ ] 移动任务位置功能正常
- [ ] 子任务列表显示正常
- [ ] 任务完成状态切换正常
- [ ] 忘记密码功能正常（发送重置邮件）
- [ ] 重置密码功能正常
- [ ] 登录后用户信息显示正常
- [ ] 项目列表显示正常
- [ ] ID 类型为 number，无类型报错

---

## 参考资源

- 后端 API 文档: `http://localhost:8080/v3/api-docs`
- 前端 API 文档: `/data/project/ProjectManager/Builder/frontend/docs/api/API 前端接口文档.md`
- 前端 issue 问题追踪单：`frontend-issues.json`
