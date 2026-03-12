# API 接口测试用例

## 模块：认证管理
**基础路径：** `/api/v1/auth`

---

### 1.1 用户登录
| 字段 | 内容 |
|------|------|
| **用例编号** | AUTH-001 |
| **接口名称** | 用户登录 |
| **接口路径** | `POST /api/v1/auth/login` |
| **请求头** | `Content-Type: application/json` |
| **请求体** | ```json { "usernameOrEmail": "string", "password": "string" } ``` |
| **请求示例** | ```json { "usernameOrEmail": "testuser", "password": "Test@123" } ``` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "accessToken": "string", "refreshToken": "string", "expiresIn": 3600 } } ``` |

#### 测试点
- [ ] ✅ 正常登录 - 用户名 + 正确密码
- [ ] ✅ 正常登录 - 邮箱 + 正确密码
- [ ] ❌ 用户名/邮箱不存在
- [ ] ❌ 密码错误
- [ ] ❌ 用户名/邮箱为空
- [ ] ❌ 密码为空
- [ ] ❌ 请求体格式错误

---

### 1.2 用户注册
| 字段 | 内容 |
|------|------|
| **用例编号** | AUTH-002 |
| **接口名称** | 用户注册 |
| **接口路径** | `POST /api/v1/auth/register` |
| **请求头** | `Content-Type: application/json` |
| **请求体** | ```json { "username": "string", "email": "string", "password": "string", "nickname": "string" } ``` |
| **请求示例** | ```json { "username": "newuser", "email": "new@test.com", "password": "Test@123", "nickname": "新用户" } ``` |
| **响应状态码** | 201 Created |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "accessToken": "string", "refreshToken": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常注册 - 填写所有必填字段
- [ ] ✅ 正常注册 - 只填写必填字段
- [ ] ❌ 用户名已存在
- [ ] ❌ 邮箱已注册
- [ ] ❌ 用户名为空
- [ ] ❌ 邮箱格式错误
- [ ] ❌ 密码强度不足
- [ ] ❌ 密码为空

---

### 1.3 刷新 Token
| 字段 | 内容 |
|------|------|
| **用例编号** | AUTH-003 |
| **接口名称** | 刷新 Token |
| **接口路径** | `POST /api/v1/auth/refresh` |
| **请求头** | `Content-Type: application/json` |
| **请求体** | ```json { "refreshToken": "string" } ``` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "accessToken": "string", "refreshToken": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常刷新 - 有效 refreshToken
- [ ] ❌ refreshToken 为空
- [ ] ❌ refreshToken 无效/过期

---

### 1.4 用户登出
| 字段 | 内容 |
|------|------|
| **用例编号** | AUTH-004 |
| **接口名称** | 用户登出 |
| **接口路径** | `POST /api/v1/auth/logout` |
| **请求头** | `Authorization: Bearer <token>` |
| **请求体** | 无 |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": null } ``` |

#### 测试点
- [ ] ✅ 正常登出 - 有效 token
- [ ] ❌ 未登录（无 token）
- [ ] ❌ token 无效

---

### 1.5 忘记密码
| 字段 | 内容 |
|------|------|
| **用例编号** | AUTH-005 |
| **接口名称** | 忘记密码 |
| **接口路径** | `POST /api/v1/auth/forgot-password` |
| **请求头** | `Content-Type: application/json` |
| **请求体** | ```json { "email": "string" } ``` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": null } ``` |

#### 测试点
- [ ] ✅ 正常请求 - 邮箱已注册
- [ ] ❌ 邮箱未注册（提示信息需一致）
- [ ] ❌ 邮箱格式错误

---

### 1.6 重置密码
| 字段 | 内容 |
|------|------|
| **用例编号** | AUTH-006 |
| **接口名称** | 重置密码 |
| **接口路径** | `POST /api/v1/auth/reset-password` |
| **请求头** | `Content-Type: application/json` |
| **请求体** | ```json { "resetToken": "string", "newPassword": "string" } ``` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": null } ``` |

#### 测试点
- [ ] ✅ 正常重置 - 有效 token + 符合强度密码
- [ ] ❌ resetToken 无效/过期
- [ ] ❌ 密码强度不足

---

## 模块：用户管理
**基础路径：** `/api/v1/user`

---

### 2.1 获取个人资料
| 字段 | 内容 |
|------|------|
| **用例编号** | USER-001 |
| **接口名称** | 获取个人资料 |
| **接口路径** | `GET /api/v1/user/profile` |
| **请求头** | `Authorization: Bearer <token>` |
| **请求体** | 无 |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "username": "string", "email": "string", "nickname": "string", "avatar": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常获取 - 已登录用户
- [ ] ❌ 未登录

---

### 2.2 更新个人资料
| 字段 | 内容 |
|------|------|
| **用例编号** | USER-002 |
| **接口名称** | 更新个人资料 |
| **接口路径** | `PUT /api/v1/user/profile` |
| **请求头** | `Authorization: Bearer <token>` |
| **请求参数** | `nickname?: string`, `avatar?: string` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "username": "string", "email": "string", "nickname": "string", "avatar": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常更新 - 填写 nickname
- [ ] ✅ 正常更新 - 填写 avatar
- [ ] ✅ 正常更新 - 填写两者

---

### 2.3 上传头像
| 字段 | 内容 |
|------|------|
| **用例编号** | USER-003 |
| **接口名称** | 上传头像 |
| **接口路径** | `POST /api/v1/user/avatar` |
| **请求头** | `Authorization: Bearer <token>`, `Content-Type: multipart/form-data` |
| **请求参数** | `file: MultipartFile` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "username": "string", "avatar": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常上传 - 图片格式 (jpg/png)
- [ ] ❌ 文件为空
- [ ] ❌ 文件类型错误（非图片）
- [ ] ❌ 文件过大

---

### 2.4 修改密码
| 字段 | 内容 |
|------|------|
| **用例编号** | USER-004 |
| **接口名称** | 修改密码 |
| **接口路径** | `PUT /api/v1/user/password` |
| **请求头** | `Authorization: Bearer <token>` |
| **请求参数** | `oldPassword: string`, `newPassword: string` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": null } ``` |

#### 测试点
- [ ] ✅ 正常修改 - 正确旧密码 + 符合强度新密码
- [ ] ❌ 旧密码错误
- [ ] ❌ 新密码强度不足
- [ ] ❌ 新密码与旧密码相同

---

## 模块：项目管理
**基础路径：** `/api/v1/projects`

---

### 3.1 创建项目
| 字段 | 内容 |
|------|------|
| **用例编号** | PROJ-001 |
| **接口名称** | 创建项目 |
| **接口路径** | `POST /api/v1/projects` |
| **请求头** | `Authorization: Bearer <token>`, `Content-Type: application/json` |
| **请求体** | ```json { "name": "string", "description": "string", "key": "string" } ``` |
| **响应状态码** | 201 Created |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "name": "string", "key": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常创建 - 填写所有字段
- [ ] ❌ 项目 key 已存在
- [ ] ❌ 名称为空
- [ ] ❌ key 为空

---

### 3.2 获取项目详情
| 字段 | 内容 |
|------|------|
| **用例编号** | PROJ-002 |
| **接口名称** | 获取项目详情 |
| **接口路径** | `GET /api/v1/projects/{id}` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "name": "string", "key": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常获取 - 项目成员
- [ ] ❌ 项目不存在
- [ ] ❌ 无权限访问

---

### 3.3 更新项目
| 字段 | 内容 |
|------|------|
| **用例编号** | PROJ-003 |
| **接口名称** | 更新项目 |
| **接口路径** | `PUT /api/v1/projects/{id}` |
| **请求头** | `Authorization: Bearer <token>`, `Content-Type: application/json` |
| **路径参数** | `id: Long` |
| **请求体** | ```json { "name": "string", "description": "string" } ``` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "name": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常更新 - 项目管理员
- [ ] ❌ 项目不存在
- [ ] ❌ 无权限

---

### 3.4 删除项目
| 字段 | 内容 |
|------|------|
| **用例编号** | PROJ-004 |
| **接口名称** | 删除项目 |
| **接口路径** | `DELETE /api/v1/projects/{id}` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": null } ``` |

#### 测试点
- [ ] ✅ 正常删除 - 项目所有者
- [ ] ❌ 项目不存在
- [ ] ❌ 无权限

---

### 3.5 获取项目列表
| 字段 | 内容 |
|------|------|
| **用例编号** | PROJ-005 |
| **接口名称** | 获取项目列表 |
| **接口路径** | `GET /api/v1/projects` |
| **请求头** | `Authorization: Bearer <token>` |
| **请求参数** | `page?: Integer`, `size?: Integer`, `keyword?: String` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "list": [], "total": 0, "page": 1 } } ``` |

#### 测试点
- [ ] ✅ 正常获取 - 有项目
- [ ] ✅ 正常获取 - 无项目
- [ ] ✅ 分页参数正常
- [ ] ✅ 关键字搜索

---

### 3.6 添加项目成员
| 字段 | 内容 |
|------|------|
| **用例编号** | PROJ-006 |
| **接口名称** | 添加项目成员 |
| **接口路径** | `POST /api/v1/projects/{id}/members` |
| **请求头** | `Authorization: Bearer <token>`, `Content-Type: application/json` |
| **路径参数** | `id: Long` |
| **请求体** | ```json { "userId": 1, "role": "string" } ``` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": null } ``` |

#### 测试点
- [ ] ✅ 正常添加 - 新成员
- [ ] ❌ 成员已存在
- [ ] ❌ 用户不存在
- [ ] ❌ 无权限

---

### 3.7 移除项目成员
| 字段 | 内容 |
|------|------|
| **用例编号** | PROJ-007 |
| **接口名称** | 移除项目成员 |
| **接口路径** | `DELETE /api/v1/projects/{id}/members/{userId}` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `id: Long`, `userId: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": null } ``` |

#### 测试点
- [ ] ✅ 正常移除 - 移除成员
- [ ] ❌ 成员不存在
- [ ] ❌ 无权限

---

### 3.8 获取项目成员列表
| 字段 | 内容 |
|------|------|
| **用例编号** | PROJ-008 |
| **接口名称** | 获取项目成员列表 |
| **接口路径** | `GET /api/v1/projects/{id}/members` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": [] } ``` |

#### 测试点
- [ ] ✅ 正常获取 - 有成员
- [ ] ✅ 正常获取 - 无成员
- [ ] ❌ 项目不存在

---

## 模块：任务管理
**基础路径：** `/api/v1/projects/{projectId}/tasks`

---

### 4.1 创建任务
| 字段 | 内容 |
|------|------|
| **用例编号** | TASK-001 |
| **接口名称** | 创建任务 |
| **接口路径** | `POST /api/v1/projects/{projectId}/tasks` |
| **请求头** | `Authorization: Bearer <token>`, `Content-Type: application/json` |
| **路径参数** | `projectId: Long` |
| **请求体** | ```json { "title": "string", "description": "string", "priority": "string", "assigneeId": 1 } ``` |
| **响应状态码** | 201 Created |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "title": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常创建 - 填写必填字段
- [ ] ❌ 标题为空
- [ ] ❌ 项目不存在

---

### 4.2 获取任务详情
| 字段 | 内容 |
|------|------|
| **用例编号** | TASK-002 |
| **接口名称** | 获取任务详情 |
| **接口路径** | `GET /api/v1/projects/{projectId}/tasks/{id}` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "title": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常获取 - 任务存在
- [ ] ❌ 任务不存在

---

### 4.3 更新任务
| 字段 | 内容 |
|------|------|
| **用例编号** | TASK-003 |
| **接口名称** | 更新任务 |
| **接口路径** | `PUT /api/v1/projects/{projectId}/tasks/{id}` |
| **请求头** | `Authorization: Bearer <token>`, `Content-Type: application/json` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **请求体** | ```json { "title": "string", "description": "string", "priority": "string" } ``` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "title": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常更新
- [ ] ❌ 任务不存在

---

### 4.4 删除任务
| 字段 | 内容 |
|------|------|
| **用例编号** | TASK-004 |
| **接口名称** | 删除任务 |
| **接口路径** | `DELETE /api/v1/projects/{projectId}/tasks/{id}` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": null } ``` |

#### 测试点
- [ ] ✅ 正常删除
- [ ] ❌ 任务不存在

---

### 4.5 移动任务
| 字段 | 内容 |
|------|------|
| **用例编号** | TASK-005 |
| **接口名称** | 移动任务 |
| **接口路径** | `POST /api/v1/projects/{projectId}/tasks/{id}/move` |
| **请求头** | `Authorization: Bearer <token>`, `Content-Type: application/json` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **请求体** | ```json { "status": "string", "position": 1 } ``` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "status": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常移动 - 状态变更
- [ ] ✅ 正常移动 - 位置变更
- [ ] ❌ 状态值无效

---

### 4.6 获取任务列表
| 字段 | 内容 |
|------|------|
| **用例编号** | TASK-006 |
| **接口名称** | 获取任务列表 |
| **接口路径** | `GET /api/v1/projects/{projectId}/tasks` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long` |
| **请求参数** | `status?: String`, `priority?: String`, `assigneeId?: Long`, `keyword?: String`, `page?: Integer`, `size?: Integer` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "list": [], "total": 0 } } ``` |

#### 测试点
- [ ] ✅ 正常获取 - 有任务
- [ ] ✅ 筛选 - 按状态
- [ ] ✅ 筛选 - 按优先级
- [ ] ✅ 筛选 - 按负责人
- [ ] ✅ 关键字搜索
- [ ] ✅ 分页正常

---

### 4.7 获取子任务列表
| 字段 | 内容 |
|------|------|
| **用例编号** | TASK-007 |
| **接口名称** | 获取子任务列表 |
| **接口路径** | `GET /api/v1/projects/{projectId}/tasks/{id}/subtasks` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": [] } ``` |

#### 测试点
- [ ] ✅ 正常获取 - 有子任务
- [ ] ✅ 正常获取 - 无子任务

---

### 4.8 切换子任务完成状态
| 字段 | 内容 |
|------|------|
| **用例编号** | TASK-008 |
| **接口名称** | 切换子任务完成状态 |
| **接口路径** | `POST /api/v1/projects/{projectId}/tasks/{id}/toggle-complete` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "completed": true } } ``` |

#### 测试点
- [ ] ✅ 切换为完成
- [ ] ✅ 切换为未完成

---

## 模块：问题追踪
**基础路径：** `/api/v1/projects/{projectId}/issues`

---

### 5.1 创建问题
| 字段 | 内容 |
|------|------|
| **用例编号** | ISSUE-001 |
| **接口名称** | 创建问题 |
| **接口路径** | `POST /api/v1/projects/{projectId}/issues` |
| **请求头** | `Authorization: Bearer <token>`, `Content-Type: application/json` |
| **路径参数** | `projectId: Long` |
| **请求体** | ```json { "title": "string", "description": "string", "type": "string", "severity": "string" } ``` |
| **响应状态码** | 201 Created |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "title": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常创建
- [ ] ❌ 标题为空
- [ ] ❌ 类型值无效

---

### 5.2 获取问题详情
| 字段 | 内容 |
|------|------|
| **用例编号** | ISSUE-002 |
| **接口名称** | 获取问题详情 |
| **接口路径** | `GET /api/v1/projects/{projectId}/issues/{id}` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "title": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常获取
- [ ] ❌ 问题不存在

---

### 5.3 获取问题列表
| 字段 | 内容 |
|------|------|
| **用例编号** | ISSUE-003 |
| **接口名称** | 获取问题列表 |
| **接口路径** | `GET /api/v1/projects/{projectId}/issues` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long` |
| **请求参数** | `type?: String`, `severity?: String`, `status?: String`, `assigneeId?: Long`, `keyword?: String`, `page?: Integer`, `size?: Integer` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "list": [], "total": 0 } } ``` |

#### 测试点
- [ ] ✅ 正常获取
- [ ] ✅ 筛选 - 按类型
- [ ] ✅ 筛选 - 按严重程度
- [ ] ✅ 筛选 - 按状态

---

### 5.4 更新问题
| 字段 | 内容 |
|------|------|
| **用例编号** | ISSUE-004 |
| **接口名称** | 更新问题 |
| **接口路径** | `PUT /api/v1/projects/{projectId}/issues/{id}` |
| **请求头** | `Authorization: Bearer <token>`, `Content-Type: application/json` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **请求体** | ```json { "title": "string", "description": "string", "severity": "string" } ``` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1 } } ``` |

#### 测试点
- [ ] ✅ 正常更新
- [ ] ❌ 问题不存在

---

### 5.5 删除问题
| 字段 | 内容 |
|------|------|
| **用例编号** | ISSUE-005 |
| **接口名称** | 删除问题 |
| **接口路径** | `DELETE /api/v1/projects/{projectId}/issues/{id}` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": null } ``` |

#### 测试点
- [ ] ✅ 正常删除
- [ ] ❌ 问题不存在

---

## 模块：史诗管理
**基础路径：** `/api/v1/projects/{projectId}/epics`

---

### 6.1 创建史诗
| 字段 | 内容 |
|------|------|
| **用例编号** | EPIC-001 |
| **接口名称** | 创建史诗 |
| **接口路径** | `POST /api/v1/projects/{projectId}/epics` |
| **请求头** | `Authorization: Bearer <token>`, `Content-Type: application/json` |
| **路径参数** | `projectId: Long` |
| **请求体** | ```json { "name": "string", "description": "string" } ``` |
| **响应状态码** | 201 Created |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "name": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常创建
- [ ] ❌ 名称为空

---

### 6.2 获取史诗详情
| 字段 | 内容 |
|------|------|
| **用例编号** | EPIC-002 |
| **接口名称** | 获取史诗详情 |
| **接口路径** | `GET /api/v1/projects/{projectId}/epics/{id}` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "name": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常获取
- [ ] ❌ 史诗不存在

---

### 6.3 获取史诗列表
| 字段 | 内容 |
|------|------|
| **用例编号** | EPIC-003 |
| **接口名称** | 获取史诗列表 |
| **接口路径** | `GET /api/v1/projects/{projectId}/epics` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": [] } ``` |

#### 测试点
- [ ] ✅ 正常获取 - 有史诗
- [ ] ✅ 正常获取 - 无史诗

---

### 6.4 更新史诗
| 字段 | 内容 |
|------|------|
| **用例编号** | EPIC-004 |
| **接口名称** | 更新史诗 |
| **接口路径** | `PUT /api/v1/projects/{projectId}/epics/{id}` |
| **请求头** | `Authorization: Bearer <token>`, `Content-Type: application/json` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **请求体** | ```json { "name": "string", "description": "string" } ``` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1 } } ``` |

#### 测试点
- [ ] ✅ 正常更新
- [ ] ❌ 史诗不存在

---

### 6.5 删除史诗
| 字段 | 内容 |
|------|------|
| **用例编号** | EPIC-005 |
| **接口名称** | 删除史诗 |
| **接口路径** | `DELETE /api/v1/projects/{projectId}/epics/{id}` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": null } ``` |

#### 测试点
- [ ] ✅ 正常删除
- [ ] ❌ 史诗不存在

---

## 模块：用户故事
**基础路径：** `/api/v1/projects/{projectId}/stories`

---

### 7.1 创建用户故事
| 字段 | 内容 |
|------|------|
| **用例编号** | STORY-001 |
| **接口名称** | 创建用户故事 |
| **接口路径** | `POST /api/v1/projects/{projectId}/stories` |
| **请求头** | `Authorization: Bearer <token>`, `Content-Type: application/json` |
| **路径参数** | `projectId: Long` |
| **请求体** | ```json { "title": "string", "description": "string", "priority": "string", "epicId": 1 } ``` |
| **响应状态码** | 201 Created |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "title": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常创建
- [ ] ❌ 标题为空

---

### 7.2 获取用户故事详情
| 字段 | 内容 |
|------|------|
| **用例编号** | STORY-002 |
| **接口名称** | 获取用户故事详情 |
| **接口路径** | `GET /api/v1/projects/{projectId}/stories/{id}` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1 } } ``` |

#### 测试点
- [ ] ✅ 正常获取
- [ ] ❌ 故事不存在

---

### 7.3 获取用户故事列表
| 字段 | 内容 |
|------|------|
| **用例编号** | STORY-003 |
| **接口名称** | 获取用户故事列表 |
| **接口路径** | `GET /api/v1/projects/{projectId}/stories` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long` |
| **请求参数** | `epicId?: Long`, `status?: String`, `priority?: String`, `assigneeId?: Long`, `keyword?: String`, `page?: Integer`, `size?: Integer` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "list": [], "total": 0 } } ``` |

#### 测试点
- [ ] ✅ 正常获取
- [ ] ✅ 筛选 - 按史诗
- [ ] ✅ 筛选 - 按状态
- [ ] ✅ 筛选 - 按优先级

---

### 7.4 更新用户故事
| 字段 | 内容 |
|------|------|
| **用例编号** | STORY-004 |
| **接口名称** | 更新用户故事 |
| **接口路径** | `PUT /api/v1/projects/{projectId}/stories/{id}` |
| **请求头** | `Authorization: Bearer <token>`, `Content-Type: application/json` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **请求体** | ```json { "title": "string", "description": "string", "priority": "string" } ``` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1 } } ``` |

#### 测试点
- [ ] ✅ 正常更新
- [ ] ❌ 故事不存在

---

### 7.5 删除用户故事
| 字段 | 内容 |
|------|------|
| **用例编号** | STORY-005 |
| **接口名称** | 删除用户故事 |
| **接口路径** | `DELETE /api/v1/projects/{projectId}/stories/{id}` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": null } ``` |

#### 测试点
- [ ] ✅ 正常删除
- [ ] ❌ 故事不存在

---

## 模块：通知管理
**基础路径：** `/api/v1/notifications`

---

### 8.1 获取通知列表
| 字段 | 内容 |
|------|------|
| **用例编号** | NOTIF-001 |
| **接口名称** | 获取通知列表 |
| **接口路径** | `GET /api/v1/notifications` |
| **请求头** | `Authorization: Bearer <token>` |
| **请求参数** | `page?: Integer`, `size?: Integer` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "list": [], "total": 0 } } ``` |

#### 测试点
- [ ] ✅ 正常获取 - 有通知
- [ ] ✅ 正常获取 - 无通知

---

### 8.2 获取未读通知数量
| 字段 | 内容 |
|------|------|
| **用例编号** | NOTIF-002 |
| **接口名称** | 获取未读通知数量 |
| **接口路径** | `GET /api/v1/notifications/unread-count` |
| **请求头** | `Authorization: Bearer <token>` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": 0 } ``` |

#### 测试点
- [ ] ✅ 正常获取

---

### 8.3 标记通知为已读
| 字段 | 内容 |
|------|------|
| **用例编号** | NOTIF-003 |
| **接口名称** | 标记已读 |
| **接口路径** | `POST /api/v1/notifications/{id}/read` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": null } ``` |

#### 测试点
- [ ] ✅ 正常标记 - 通知存在
- [ ] ❌ 通知不存在

---

### 8.4 标记所有通知为已读
| 字段 | 内容 |
|------|------|
| **用例编号** | NOTIF-004 |
| **接口名称** | 全部已读 |
| **接口路径** | `POST /api/v1/notifications/read-all` |
| **请求头** | `Authorization: Bearer <token>` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": null } ``` |

#### 测试点
- [ ] ✅ 正常标记 - 有未读通知
- [ ] ✅ 正常标记 - 无未读通知

---

## 模块：任务评论
**基础路径：** `/api/v1/tasks/{taskId}/comments`

---

### 9.1 获取评论列表
| 字段 | 内容 |
|------|------|
| **用例编号** | COMMENT-001 |
| **接口名称** | 获取评论列表 |
| **接口路径** | `GET /api/v1/tasks/{taskId}/comments` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `taskId: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": [] } ``` |

#### 测试点
- [ ] ✅ 正常获取 - 有评论
- [ ] ✅ 正常获取 - 无评论

---

### 9.2 添加评论
| 字段 | 内容 |
|------|------|
| **用例编号** | COMMENT-002 |
| **接口名称** | 添加评论 |
| **接口路径** | `POST /api/v1/tasks/{taskId}/comments` |
| **请求头** | `Authorization: Bearer <token>`, `Content-Type: application/json` |
| **路径参数** | `taskId: Long` |
| **请求体** | ```json { "content": "string", "parentId": 1 } ``` |
| **响应状态码** | 201 Created |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "content": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常添加 - 新评论
- [ ] ✅ 正常添加 - 回复评论
- [ ] ❌ 内容为空

---

### 9.3 更新评论
| 字段 | 内容 |
|------|------|
| **用例编号** | COMMENT-003 |
| **接口名称** | 更新评论 |
| **接口路径** | `PUT /api/v1/tasks/{taskId}/comments/{id}` |
| **请求头** | `Authorization: Bearer <token>`, `Content-Type: application/json` |
| **路径参数** | `taskId: Long`, `id: Long` |
| **请求体** | ```json { "content": "string" } ``` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1 } } ``` |

#### 测试点
- [ ] ✅ 正常更新 - 作者本人
- [ ] ❌ 非作者本人
- [ ] ❌ 评论不存在

---

### 9.4 删除评论
| 字段 | 内容 |
|------|------|
| **用例编号** | COMMENT-004 |
| **接口名称** | 删除评论 |
| **接口路径** | `DELETE /api/v1/tasks/{taskId}/comments/{id}` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `taskId: Long`, `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": null } ``` |

#### 测试点
- [ ] ✅ 正常删除 - 作者本人
- [ ] ❌ 非作者本人
- [ ] ❌ 评论不存在

---

## 模块：报表管理
**基础路径：** `/api/v1/projects/{projectId}/reports`

---

### 10.1 燃尽图数据
| 字段 | 内容 |
|------|------|
| **用例编号** | REPORT-001 |
| **接口名称** | 燃尽图数据 |
| **接口路径** | `GET /api/v1/projects/{projectId}/reports/burndown` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "dates": [], "remaining": [] } } ``` |

#### 测试点
- [ ] ✅ 正常获取 - 有数据
- [ ] ✅ 正常获取 - 无数据
- [ ] ❌ 项目不存在

---

## 模块：Wiki 文档
**基础路径：** `/api/v1/projects/{projectId}/wiki`

---

### 11.1 获取文档树
| 字段 | 内容 |
|------|------|
| **用例编号** | WIKI-001 |
| **接口名称** | 获取文档树 |
| **接口路径** | `GET /api/v1/projects/{projectId}/wiki` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": [] } ``` |

#### 测试点
- [ ] ✅ 正常获取 - 有文档
- [ ] ✅ 正常获取 - 无文档

---

### 11.2 获取文档详情
| 字段 | 内容 |
|------|------|
| **用例编号** | WIKI-002 |
| **接口名称** | 获取文档详情 |
| **接口路径** | `GET /api/v1/projects/{projectId}/wiki/{id}` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "title": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常获取
- [ ] ❌ 文档不存在

---

### 11.3 创建文档
| 字段 | 内容 |
|------|------|
| **用例编号** | WIKI-003 |
| **接口名称** | 创建文档 |
| **接口路径** | `POST /api/v1/projects/{projectId}/wiki` |
| **请求头** | `Authorization: Bearer <token>`, `Content-Type: application/json` |
| **路径参数** | `projectId: Long` |
| **请求体** | ```json { "title": "string", "content": "string", "parentId": 1 } ``` |
| **响应状态码** | 201 Created |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1, "title": "string" } } ``` |

#### 测试点
- [ ] ✅ 正常创建 - 根文档
- [ ] ✅ 正常创建 - 子文档
- [ ] ❌ 标题为空

---

### 11.4 更新文档
| 字段 | 内容 |
|------|------|
| **用例编号** | WIKI-004 |
| **接口名称** | 更新文档 |
| **接口路径** | `PUT /api/v1/projects/{projectId}/wiki/{id}` |
| **请求头** | `Authorization: Bearer <token>`, `Content-Type: application/json` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **请求体** | ```json { "title": "string", "content": "string" } ``` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": { "id": 1 } } ``` |

#### 测试点
- [ ] ✅ 正常更新
- [ ] ❌ 文档不存在

---

### 11.5 删除文档
| 字段 | 内容 |
|------|------|
| **用例编号** | WIKI-005 |
| **接口名称** | 删除文档 |
| **接口路径** | `DELETE /api/v1/projects/{projectId}/wiki/{id}` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": null } ``` |

#### 测试点
- [ ] ✅ 正常删除
- [ ] ❌ 文档不存在

---

### 11.6 获取文档历史记录
| 字段 | 内容 |
|------|------|
| **用例编号** | WIKI-006 |
| **接口名称** | 获取历史记录 |
| **接口路径** | `GET /api/v1/projects/{projectId}/wiki/{id}/history` |
| **请求头** | `Authorization: Bearer <token>` |
| **路径参数** | `projectId: Long`, `id: Long` |
| **响应状态码** | 200 OK |
| **响应结构** | ```json { "code": 0, "message": "success", "data": [] } ``` |

#### 测试点
- [ ] ✅ 正常获取 - 有历史
- [ ] ✅ 正常获取 - 无历史

---

## 接口汇总表

| 模块 | 前缀 | 接口数量 |
|------|------|----------|
| 认证管理 | /api/v1/auth | 6 |
| 用户管理 | /api/v1/user | 4 |
| 项目管理 | /api/v1/projects | 8 |
| 任务管理 | /api/v1/projects/{projectId}/tasks | 8 |
| 问题追踪 | /api/v1/projects/{projectId}/issues | 5 |
| 史诗管理 | /api/v1/projects/{projectId}/epics | 5 |
| 用户故事 | /api/v1/projects/{projectId}/stories | 5 |
| 通知管理 | /api/v1/notifications | 4 |
| 任务评论 | /api/v1/tasks/{taskId}/comments | 4 |
| 报表管理 | /api/v1/projects/{projectId}/reports | 1 |
| Wiki 文档 | /api/v1/projects/{projectId}/wiki | 6 |
| **合计** | **11 个模块** | **56 个** |