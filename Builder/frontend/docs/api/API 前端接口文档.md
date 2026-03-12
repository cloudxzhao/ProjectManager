# ProjectHub 前端 API 接口文档 (OpenAPI 规范)

> 本文档根据前端代码整理，采用 OpenAPI 3.0 规范描述所有前端调用的 API 接口。

---

## OpenAPI 规范文档

```yaml
openapi: 3.0.3
info:
  title: ProjectHub 前端 API 接口文档
  description: |
    ProjectHub 项目管理系统前端 API 接口规范

    本文档根据前端代码整理，包含所有前端调用的 API 接口定义。

    ## 基础配置

    | 配置项 | 值 |
    |--------|-----|
    | 基础 URL | `http://localhost:8080/api/v1` |
    | 超时时间 | 10000ms (10 秒) |
    | 认证方式 | Bearer Token (JWT) |

    ## 统一响应格式

    ```typescript
    interface Result<T> {
      code: number;
      message: string;
      data: T;
      timestamp: number;
    }
    ```

    ## 错误处理

    | HTTP 状态码 | 说明 | 前端处理 |
    |------------|------|---------|
    | 401 | 未授权/Token 过期 | 清除 Token，跳转登录页 |
    | 403 | 权限不足 | 显示错误提示 |
    | 404 | 资源不存在 | 显示错误提示 |
    | 500 | 服务器错误 | 显示错误提示 |
  version: 1.0.0
  contact:
    name: ProjectHub Team
    email: support@projecthub.com
  license:
    name: Apache 2.0
    url: https://www.apache.org/licenses/LICENSE-2.0

servers:
  - url: http://localhost:8080/api/v1
    description: 开发环境
  - url: https://api.projecthub.com/api/v1
    description: 生产环境

tags:
  - name: 认证管理
    description: 用户认证相关接口
  - name: 用户管理
    description: 用户资料相关接口
  - name: 项目管理
    description: 项目相关接口
  - name: 任务管理
    description: 任务相关接口
  - name: 通知管理
    description: 通知相关接口

security:
  - bearerAuth: []

paths:
  # ==================== 认证管理 ====================
  /auth/login:
    post:
      tags:
        - 认证管理
      summary: 用户登录
      description: 使用邮箱和密码登录系统
      operationId: login
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LoginCredentials'
      responses:
        '200':
          description: 登录成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthTokensResult'
        '400':
          description: 请求参数错误
        '401':
          description: 邮箱或密码错误
        '500':
          description: 服务器错误

  /auth/register:
    post:
      tags:
        - 认证管理
      summary: 用户注册
      description: 创建新用户账号
      operationId: register
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RegisterData'
      responses:
        '200':
          description: 注册成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthTokensResult'
        '400':
          description: 请求参数错误
        '409':
          description: 邮箱或用户名已存在
        '500':
          description: 服务器错误

  /auth/refresh:
    post:
      tags:
        - 认证管理
      summary: 刷新 Token
      description: 使用刷新 Token 获取新的访问 Token
      operationId: refreshToken
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RefreshTokenRequest'
      responses:
        '200':
          description: 刷新成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthTokensResult'
        '401':
          description: 刷新 Token 无效或已过期
        '500':
          description: 服务器错误

  /auth/logout:
    post:
      tags:
        - 认证管理
      summary: 用户登出
      description: 使当前 Token 失效
      operationId: logout
      security:
        - bearerAuth: []
      responses:
        '200':
          description: 登出成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VoidResult'
        '401':
          description: 未授权

  /auth/password/reset:
    post:
      tags:
        - 认证管理
      summary: 忘记密码
      description: 发送密码重置邮件到用户邮箱
      operationId: forgotPassword
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ForgotPasswordRequest'
      responses:
        '200':
          description: 邮件发送成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VoidResult'
        '404':
          description: 邮箱不存在

  /auth/password/reset/confirm:
    post:
      tags:
        - 认证管理
      summary: 重置密码
      description: 使用重置 Token 设置新密码
      operationId: resetPassword
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ResetPasswordRequest'
      responses:
        '200':
          description: 密码重置成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VoidResult'
        '400':
          description: 重置 Token 无效或已过期

  # ==================== 用户管理 ====================
  /user/profile:
    get:
      tags:
        - 用户管理
      summary: 获取个人资料
      description: 获取当前登录用户的详细信息
      operationId: getProfile
      security:
        - bearerAuth: []
      responses:
        '200':
          description: 获取成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserResult'
        '401':
          description: 未授权
        '404':
          description: 用户不存在

    put:
      tags:
        - 用户管理
      summary: 更新个人资料
      description: 更新当前用户的基本信息
      operationId: updateProfile
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateProfileRequest'
      responses:
        '200':
          description: 更新成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserResult'
        '400':
          description: 请求参数错误
        '401':
          description: 未授权

  /user/avatar:
    post:
      tags:
        - 用户管理
      summary: 上传头像
      description: 上传用户头像图片
      operationId: uploadAvatar
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              $ref: '#/components/schemas/UploadAvatarRequest'
      responses:
        '200':
          description: 上传成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserResult'
        '400':
          description: 文件格式不支持或超出大小限制
        '401':
          description: 未授权

  # ==================== 项目管理 ====================
  /projects:
    get:
      tags:
        - 项目管理
      summary: 获取项目列表
      description: 获取当前用户参与的项目列表
      operationId: listProjects
      security:
        - bearerAuth: []
      parameters:
        - name: page
          in: query
          description: 页码
          required: false
          schema:
            type: integer
            default: 1
        - name: size
          in: query
          description: 每页数量
          required: false
          schema:
            type: integer
            default: 20
        - name: keyword
          in: query
          description: 搜索关键词
          required: false
          schema:
            type: string
      responses:
        '200':
          description: 获取成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProjectPageResult'
        '401':
          description: 未授权

    post:
      tags:
        - 项目管理
      summary: 创建项目
      description: 创建一个新的项目
      operationId: createProject
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateProjectDto'
      responses:
        '200':
          description: 创建成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProjectVO'
        '400':
          description: 请求参数错误
        '401':
          description: 未授权

  /projects/{id}:
    get:
      tags:
        - 项目管理
      summary: 获取项目详情
      description: 根据项目 ID 获取详细信息
      operationId: getProject
      security:
        - bearerAuth: []
      parameters:
        - name: id
          in: path
          description: 项目 ID
          required: true
          schema:
            type: string
      responses:
        '200':
          description: 获取成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProjectVOResult'
        '401':
          description: 未授权
        '404':
          description: 项目不存在

    put:
      tags:
        - 项目管理
      summary: 更新项目
      description: 更新项目信息
      operationId: updateProject
      security:
        - bearerAuth: []
      parameters:
        - name: id
          in: path
          description: 项目 ID
          required: true
          schema:
            type: string
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateProjectDto'
      responses:
        '200':
          description: 更新成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProjectVOResult'
        '400':
          description: 请求参数错误
        '401':
          description: 未授权
        '404':
          description: 项目不存在

    delete:
      tags:
        - 项目管理
      summary: 删除项目
      description: 删除指定项目
      operationId: deleteProject
      security:
        - bearerAuth: []
      parameters:
        - name: id
          in: path
          description: 项目 ID
          required: true
          schema:
            type: string
      responses:
        '200':
          description: 删除成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VoidResult'
        '401':
          description: 未授权
        '404':
          description: 项目不存在

  /projects/{id}/members:
    get:
      tags:
        - 项目管理
      summary: 获取项目成员列表
      description: 获取项目下的所有成员
      operationId: getProjectMembers
      security:
        - bearerAuth: []
      parameters:
        - name: id
          in: path
          description: 项目 ID
          required: true
          schema:
            type: string
      responses:
        '200':
          description: 获取成功
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/ProjectMember'
        '401':
          description: 未授权
        '404':
          description: 项目不存在

    post:
      tags:
        - 项目管理
      summary: 添加项目成员
      description: 添加用户到项目中
      operationId: addProjectMember
      security:
        - bearerAuth: []
      parameters:
        - name: id
          in: path
          description: 项目 ID
          required: true
          schema:
            type: string
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AddMemberRequest'
      responses:
        '200':
          description: 添加成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VoidResult'
        '400':
          description: 请求参数错误
        '401':
          description: 未授权
        '404':
          description: 项目或用户不存在
        '409':
          description: 用户已是项目成员

  /projects/{projectId}/members/{userId}:
    delete:
      tags:
        - 项目管理
      summary: 移除项目成员
      description: 从项目中移除指定用户
      operationId: removeProjectMember
      security:
        - bearerAuth: []
      parameters:
        - name: projectId
          in: path
          description: 项目 ID
          required: true
          schema:
            type: string
        - name: userId
          in: path
          description: 用户 ID
          required: true
          schema:
            type: string
      responses:
        '200':
          description: 移除成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VoidResult'
        '401':
          description: 未授权
        '404':
          description: 项目或成员不存在

  # ==================== 任务管理 ====================
  /projects/{projectId}/tasks:
    get:
      tags:
        - 任务管理
      summary: 获取任务列表
      description: 获取项目下的任务列表，支持筛选
      operationId: listTasks
      security:
        - bearerAuth: []
      parameters:
        - name: projectId
          in: path
          description: 项目 ID
          required: true
          schema:
            type: string
        - name: status
          in: query
          description: 任务状态
          required: false
          schema:
            $ref: '#/components/schemas/TaskStatus'
        - name: priority
          in: query
          description: 优先级
          required: false
          schema:
            $ref: '#/components/schemas/Priority'
        - name: assigneeId
          in: query
          description: 经办人 ID
          required: false
          schema:
            type: string
        - name: keyword
          in: query
          description: 搜索关键词
          required: false
          schema:
            type: string
        - name: page
          in: query
          description: 页码
          required: false
          schema:
            type: integer
            default: 1
        - name: size
          in: query
          description: 每页数量
          required: false
          schema:
            type: integer
            default: 20
      responses:
        '200':
          description: 获取成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TaskPageResult'
        '401':
          description: 未授权
        '404':
          description: 项目不存在

    post:
      tags:
        - 任务管理
      summary: 创建任务
      description: 在项目下创建新任务
      operationId: createTask
      security:
        - bearerAuth: []
      parameters:
        - name: projectId
          in: path
          description: 项目 ID
          required: true
          schema:
            type: string
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateTaskDto'
      responses:
        '200':
          description: 创建成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TaskVOResult'
        '400':
          description: 请求参数错误
        '401':
          description: 未授权
        '404':
          description: 项目不存在

  /tasks/{id}:
    get:
      tags:
        - 任务管理
      summary: 获取任务详情
      description: 根据任务 ID 获取详细信息
      operationId: getTask
      security:
        - bearerAuth: []
      parameters:
        - name: id
          in: path
          description: 任务 ID
          required: true
          schema:
            type: string
      responses:
        '200':
          description: 获取成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TaskVOResult'
        '401':
          description: 未授权
        '404':
          description: 任务不存在

    put:
      tags:
        - 任务管理
      summary: 更新任务
      description: 更新任务信息
      operationId: updateTask
      security:
        - bearerAuth: []
      parameters:
        - name: id
          in: path
          description: 任务 ID
          required: true
          schema:
            type: string
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateTaskDto'
      responses:
        '200':
          description: 更新成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TaskVOResult'
        '400':
          description: 请求参数错误
        '401':
          description: 未授权
        '404':
          description: 任务不存在

    delete:
      tags:
        - 任务管理
      summary: 删除任务
      description: 删除指定任务
      operationId: deleteTask
      security:
        - bearerAuth: []
      parameters:
        - name: id
          in: path
          description: 任务 ID
          required: true
          schema:
            type: string
      responses:
        '200':
          description: 删除成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VoidResult'
        '401':
          description: 未授权
        '404':
          description: 任务不存在

  /tasks/{id}/move:
    post:
      tags:
        - 任务管理
      summary: 移动任务
      description: 移动任务状态或位置
      operationId: moveTask
      security:
        - bearerAuth: []
      parameters:
        - name: id
          in: path
          description: 任务 ID
          required: true
          schema:
            type: string
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/MoveTaskRequest'
      responses:
        '200':
          description: 移动成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TaskVOResult'
        '400':
          description: 请求参数错误
        '401':
          description: 未授权
        '404':
          description: 任务不存在

  /tasks/{id}/subtasks:
    get:
      tags:
        - 任务管理
      summary: 获取子任务列表
      description: 获取指定任务的子任务列表
      operationId: getSubTasks
      security:
        - bearerAuth: []
      parameters:
        - name: id
          in: path
          description: 任务 ID
          required: true
          schema:
            type: string
      responses:
        '200':
          description: 获取成功
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/SubTask'
        '401':
          description: 未授权
        '404':
          description: 任务不存在

  /tasks/{id}/toggle-complete:
    post:
      tags:
        - 任务管理
      summary: 切换子任务完成状态
      description: 切换子任务的完成状态（完成/未完成）
      operationId: toggleSubTaskComplete
      security:
        - bearerAuth: []
      parameters:
        - name: id
          in: path
          description: 子任务 ID
          required: true
          schema:
            type: string
      responses:
        '200':
          description: 操作成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/SubTaskResult'
        '401':
          description: 未授权
        '404':
          description: 子任务不存在

  # ==================== 通知管理 ====================
  /notifications:
    get:
      tags:
        - 通知管理
      summary: 获取通知列表
      description: 获取当前用户的通知列表
      operationId: getNotifications
      security:
        - bearerAuth: []
      parameters:
        - name: page
          in: query
          description: 页码
          required: false
          schema:
            type: integer
            default: 1
        - name: size
          in: query
          description: 每页数量
          required: false
          schema:
            type: integer
            default: 20
      responses:
        '200':
          description: 获取成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/NotificationPageResult'
        '401':
          description: 未授权

  /notifications/unread-count:
    get:
      tags:
        - 通知管理
      summary: 获取未读通知数量
      description: 获取当前用户未读通知的数量
      operationId: getUnreadCount
      security:
        - bearerAuth: []
      responses:
        '200':
          description: 获取成功
          content:
            application/json:
              schema:
                type: object
                properties:
                  code:
                    type: integer
                  message:
                    type: string
                  data:
                    type: integer
                    description: 未读数量
                  timestamp:
                    type: integer
        '401':
          description: 未授权

  /notifications/{id}/read:
    post:
      tags:
        - 通知管理
      summary: 标记通知为已读
      description: 将指定通知标记为已读
      operationId: markAsRead
      security:
        - bearerAuth: []
      parameters:
        - name: id
          in: path
          description: 通知 ID
          required: true
          schema:
            type: string
      responses:
        '200':
          description: 标记成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VoidResult'
        '401':
          description: 未授权
        '404':
          description: 通知不存在

  /notifications/read-all:
    post:
      tags:
        - 通知管理
      summary: 标记所有通知为已读
      description: 将所有通知标记为已读
      operationId: markAllAsRead
      security:
        - bearerAuth: []
      responses:
        '200':
          description: 标记成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VoidResult'
        '401':
          description: 未授权

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: JWT Token 认证，格式：`Bearer <access_token>`

  schemas:
    # ==================== 通用响应 ====================
    Result:
      type: object
      description: 统一响应格式
      properties:
        code:
          type: integer
          description: 响应码
        message:
          type: string
          description: 响应消息
        data:
          description: 响应数据
        timestamp:
          type: integer
          description: 时间戳
      required:
        - code
        - message
        - data
        - timestamp

    VoidResult:
      type: object
      description: 空数据响应
      properties:
        code:
          type: integer
          example: 200
        message:
          type: string
          example: success
        data:
          type: object
          nullable: true
        timestamp:
          type: integer

    # ==================== 认证相关 ====================
    LoginCredentials:
      type: object
      description: 登录请求
      properties:
        email:
          type: string
          format: email
          description: 邮箱
          example: user@example.com
        password:
          type: string
          format: password
          description: 密码
          minLength: 6
          example: password123
        remember:
          type: boolean
          description: 是否记住登录状态
          default: false
      required:
        - email
        - password

    RegisterData:
      type: object
      description: 注册请求
      properties:
        username:
          type: string
          description: 用户名
          minLength: 2
          maxLength: 20
          example: newuser
        email:
          type: string
          format: email
          description: 邮箱
          example: user@example.com
        password:
          type: string
          format: password
          description: 密码
          minLength: 6
          example: password123
        confirmPassword:
          type: string
          format: password
          description: 确认密码
          example: password123
      required:
        - username
        - email
        - password
        - confirmPassword

    RefreshTokenRequest:
      type: object
      description: 刷新 Token 请求
      properties:
        refreshToken:
          type: string
          description: 刷新 Token
      required:
        - refreshToken

    ForgotPasswordRequest:
      type: object
      description: 忘记密码请求
      properties:
        email:
          type: string
          format: email
          description: 邮箱
      required:
        - email

    ResetPasswordRequest:
      type: object
      description: 重置密码请求
      properties:
        resetToken:
          type: string
          description: 重置 Token
        newPassword:
          type: string
          format: password
          description: 新密码
          minLength: 6
      required:
        - resetToken
        - newPassword

    AuthTokens:
      type: object
      description: 认证 Token 响应
      properties:
        accessToken:
          type: string
          description: 访问 Token
        refreshToken:
          type: string
          description: 刷新 Token
        expiresIn:
          type: integer
          description: 过期时间（秒）
      required:
        - accessToken
        - refreshToken
        - expiresIn

    AuthTokensResult:
      allOf:
        - $ref: '#/components/schemas/Result'
        - type: object
          properties:
            data:
              $ref: '#/components/schemas/AuthTokens'

    # ==================== 用户相关 ====================
    UserRole:
      type: string
      description: 用户角色
      enum:
        - admin
        - manager
        - member

    User:
      type: object
      description: 用户信息
      properties:
        id:
          type: string
          description: 用户 ID
        email:
          type: string
          format: email
          description: 邮箱
        username:
          type: string
          description: 用户名
        avatar:
          type: string
          nullable: true
          description: 头像 URL
        role:
          $ref: '#/components/schemas/UserRole'
        createdAt:
          type: string
          format: date-time
          description: 创建时间
        updatedAt:
          type: string
          format: date-time
          description: 更新时间
      required:
        - id
        - email
        - username
        - role
        - createdAt
        - updatedAt

    UserResult:
      allOf:
        - $ref: '#/components/schemas/Result'
        - type: object
          properties:
            data:
              $ref: '#/components/schemas/User'

    UpdateProfileRequest:
      type: object
      description: 更新个人资料请求
      properties:
        nickname:
          type: string
          description: 昵称
        avatar:
          type: string
          description: 头像 URL

    UploadAvatarRequest:
      type: object
      description: 上传头像请求
      properties:
        file:
          type: string
          format: binary
          description: 头像文件
      required:
        - file

    # ==================== 项目相关 ====================
    ProjectStatus:
      type: string
      description: 项目状态
      enum:
        - planning
        - active
        - completed
        - archived

    Project:
      type: object
      description: 项目信息
      properties:
        id:
          type: string
          description: 项目 ID
        name:
          type: string
          description: 项目名称
        description:
          type: string
          description: 项目描述
        status:
          $ref: '#/components/schemas/ProjectStatus'
        startDate:
          type: string
          format: date
          description: 开始日期
        endDate:
          type: string
          format: date
          nullable: true
          description: 结束日期
        color:
          type: string
          description: 主题颜色
        icon:
          type: string
          nullable: true
          description: 图标
        memberCount:
          type: integer
          description: 成员数量
        taskCount:
          type: integer
          description: 任务数量
        completedTaskCount:
          type: integer
          description: 完成任务数
        ownerId:
          type: string
          description: 所有者 ID
        createdAt:
          type: string
          format: date-time
          description: 创建时间
        updatedAt:
          type: string
          format: date-time
          description: 更新时间
      required:
        - id
        - name
        - status
        - startDate
        - color
        - memberCount
        - taskCount
        - completedTaskCount
        - ownerId
        - createdAt
        - updatedAt

    ProjectVO:
      $ref: '#/components/schemas/Project'

    ProjectVOResult:
      allOf:
        - $ref: '#/components/schemas/Result'
        - type: object
          properties:
            data:
              $ref: '#/components/schemas/ProjectVO'

    CreateProjectDto:
      type: object
      description: 创建项目请求
      properties:
        name:
          type: string
          description: 项目名称
        description:
          type: string
          description: 项目描述
        status:
          $ref: '#/components/schemas/ProjectStatus'
        startDate:
          type: string
          format: date
          description: 开始日期
        endDate:
          type: string
          format: date
          nullable: true
          description: 结束日期
        color:
          type: string
          description: 主题颜色
        icon:
          type: string
          nullable: true
          description: 图标
        memberIds:
          type: array
          items:
            type: string
          description: 成员 ID 列表
      required:
        - name
        - description
        - startDate
        - color

    UpdateProjectDto:
      type: object
      description: 更新项目请求
      properties:
        name:
          type: string
          description: 项目名称
        description:
          type: string
          description: 项目描述
        status:
          $ref: '#/components/schemas/ProjectStatus'
        startDate:
          type: string
          format: date
          description: 开始日期
        endDate:
          type: string
          format: date
          nullable: true
          description: 结束日期
        color:
          type: string
          description: 主题颜色
        icon:
          type: string
          nullable: true
          description: 图标

    MemberRole:
      type: string
      description: 项目成员角色
      enum:
        - owner
        - admin
        - member

    ProjectMember:
      type: object
      description: 项目成员信息
      properties:
        id:
          type: string
          description: 成员 ID
        userId:
          type: string
          description: 用户 ID
        projectId:
          type: string
          description: 项目 ID
        role:
          $ref: '#/components/schemas/MemberRole'
        joinedAt:
          type: string
          format: date-time
          description: 加入时间
      required:
        - id
        - userId
        - projectId
        - role
        - joinedAt

    AddMemberRequest:
      type: object
      description: 添加成员请求
      properties:
        userId:
          type: string
          description: 用户 ID
        role:
          $ref: '#/components/schemas/MemberRole'
      required:
        - userId
        - role

    ProjectPageResult:
      allOf:
        - $ref: '#/components/schemas/Result'
        - type: object
          properties:
            data:
              type: object
              properties:
                items:
                  type: array
                  items:
                    $ref: '#/components/schemas/Project'
                total:
                  type: integer
                page:
                  type: integer
                pageSize:
                  type: integer
                totalPages:
                  type: integer

    # ==================== 任务相关 ====================
    TaskStatus:
      type: string
      description: 任务状态
      enum:
        - todo
        - in_progress
        - testing
        - done

    Priority:
      type: string
      description: 优先级
      enum:
        - low
        - medium
        - high
        - urgent

    Task:
      type: object
      description: 任务信息
      properties:
        id:
          type: string
          description: 任务 ID
        projectId:
          type: string
          description: 项目 ID
        title:
          type: string
          description: 任务标题
        description:
          type: string
          description: 任务描述
        status:
          $ref: '#/components/schemas/TaskStatus'
        priority:
          $ref: '#/components/schemas/Priority'
        assigneeId:
          type: string
          nullable: true
          description: 经办人 ID
        reporterId:
          type: string
          nullable: true
          description: 报告人 ID
        storyPoints:
          type: integer
          nullable: true
          description: 故事点
        dueDate:
          type: string
          format: date
          nullable: true
          description: 截止日期
        tags:
          type: array
          items:
            type: string
          description: 标签列表
        parentId:
          type: string
          nullable: true
          description: 父任务 ID
        order:
          type: integer
          description: 排序位置
        subtaskCount:
          type: integer
          description: 子任务数量
        completedSubtaskCount:
          type: integer
          description: 已完成子任务数量
        commentCount:
          type: integer
          description: 评论数量
        createdAt:
          type: string
          format: date-time
          description: 创建时间
        updatedAt:
          type: string
          format: date-time
          description: 更新时间
      required:
        - id
        - projectId
        - title
        - status
        - priority
        - tags
        - order
        - subtaskCount
        - completedSubtaskCount
        - commentCount
        - createdAt
        - updatedAt

    TaskVO:
      $ref: '#/components/schemas/Task'

    TaskVOResult:
      allOf:
        - $ref: '#/components/schemas/Result'
        - type: object
          properties:
            data:
              $ref: '#/components/schemas/TaskVO'

    CreateTaskDto:
      type: object
      description: 创建任务请求
      properties:
        projectId:
          type: string
          description: 项目 ID
        title:
          type: string
          description: 任务标题
        description:
          type: string
          description: 任务描述
        status:
          $ref: '#/components/schemas/TaskStatus'
        priority:
          $ref: '#/components/schemas/Priority'
        assigneeId:
          type: string
          nullable: true
          description: 经办人 ID
        storyPoints:
          type: integer
          nullable: true
          description: 故事点
        dueDate:
          type: string
          format: date
          nullable: true
          description: 截止日期
        tags:
          type: array
          items:
            type: string
          description: 标签列表
        parentId:
          type: string
          nullable: true
          description: 父任务 ID
      required:
        - projectId
        - title

    UpdateTaskDto:
      type: object
      description: 更新任务请求
      properties:
        title:
          type: string
          description: 任务标题
        description:
          type: string
          description: 任务描述
        status:
          $ref: '#/components/schemas/TaskStatus'
        priority:
          $ref: '#/components/schemas/Priority'
        assigneeId:
          type: string
          nullable: true
          description: 经办人 ID
        storyPoints:
          type: integer
          nullable: true
          description: 故事点
        dueDate:
          type: string
          format: date
          nullable: true
          description: 截止日期
        tags:
          type: array
          items:
            type: string
          description: 标签列表
        order:
          type: integer
          description: 排序位置

    MoveTaskRequest:
      type: object
      description: 移动任务请求
      properties:
        status:
          $ref: '#/components/schemas/TaskStatus'
        position:
          type: integer
          description: 新位置

    SubTask:
      type: object
      description: 子任务信息
      properties:
        id:
          type: string
          description: 子任务 ID
        taskId:
          type: string
          description: 任务 ID
        title:
          type: string
          description: 子任务标题
        completed:
          type: boolean
          description: 是否完成
        order:
          type: integer
          description: 排序位置
        createdAt:
          type: string
          format: date-time
          description: 创建时间
        updatedAt:
          type: string
          format: date-time
          description: 更新时间
      required:
        - id
        - taskId
        - title
        - completed
        - order
        - createdAt
        - updatedAt

    SubTaskResult:
      allOf:
        - $ref: '#/components/schemas/Result'
        - type: object
          properties:
            data:
              $ref: '#/components/schemas/SubTask'

    TaskPageResult:
      allOf:
        - $ref: '#/components/schemas/Result'
        - type: object
          properties:
            data:
              type: object
              properties:
                items:
                  type: array
                  items:
                    $ref: '#/components/schemas/Task'
                total:
                  type: integer
                page:
                  type: integer
                pageSize:
                  type: integer
                totalPages:
                  type: integer

    # ==================== 通知相关 ====================
    NotificationType:
      type: string
      description: 通知类型
      enum:
        - info
        - warning
        - error
        - task
        - project

    Notification:
      type: object
      description: 通知信息
      properties:
        id:
          type: string
          description: 通知 ID
        userId:
          type: string
          description: 用户 ID
        title:
          type: string
          description: 通知标题
        content:
          type: string
          nullable: true
          description: 通知内容
        type:
          $ref: '#/components/schemas/NotificationType'
        isRead:
          type: boolean
          description: 是否已读
        relatedId:
          type: string
          nullable: true
          description: 关联 ID
        relatedType:
          type: string
          nullable: true
          description: 关联类型
        createdAt:
          type: string
          format: date-time
          description: 创建时间
      required:
        - id
        - userId
        - title
        - type
        - isRead
        - createdAt

    NotificationPageResult:
      allOf:
        - $ref: '#/components/schemas/Result'
        - type: object
          properties:
            data:
              type: object
              properties:
                items:
                  type: array
                  items:
                    $ref: '#/components/schemas/Notification'
                total:
                  type: integer
                page:
                  type: integer
                pageSize:
                  type: integer
                totalPages:
                  type: integer
```

---

## 前端端点配置

### src/lib/api/endpoints.ts

```typescript
export const endpoints = {
  // 认证相关
  auth: {
    login: '/auth/login',
    register: '/auth/register',
    logout: '/auth/logout',
    refreshToken: '/auth/refresh',
    passwordReset: '/auth/password/reset',
    passwordResetConfirm: '/auth/password/reset/confirm',
  },

  // 用户相关
  user: {
    profile: '/user/profile',
    update: '/user/profile',
    avatar: '/user/avatar',
  },

  // 项目相关
  project: {
    list: '/projects',
    create: '/projects',
    detail: (id: string) => `/projects/${id}`,
    update: (id: string) => `/projects/${id}`,
    delete: (id: string) => `/projects/${id}`,
    members: (id: string) => `/projects/${id}/members`,
    addMember: (id: string) => `/projects/${id}/members`,
    removeMember: (projectId: string, userId: string) => `/projects/${projectId}/members/${userId}`,
  },

  // 任务相关
  task: {
    list: '/tasks',
    create: '/tasks',
    detail: (id: string) => `/tasks/${id}`,
    update: (id: string) => `/tasks/${id}`,
    delete: (id: string) => `/tasks/${id}`,
    move: (id: string) => `/tasks/${id}/move`,
    comments: (id: string) => `/tasks/${id}/comments`,
    subtasks: (id: string) => `/tasks/${id}/subtasks`,
  },

  // 用户故事相关
  story: {
    list: (projectId: string) => `/projects/${projectId}/stories`,
    create: (projectId: string) => `/projects/${projectId}/stories`,
    detail: (projectId: string, storyId: string) => `/projects/${projectId}/stories/${storyId}`,
    update: (projectId: string, storyId: string) => `/projects/${projectId}/stories/${storyId}`,
    delete: (projectId: string, storyId: string) => `/projects/${projectId}/stories/${storyId}`,
  },

  // 问题追踪相关
  issue: {
    list: (projectId: string) => `/projects/${projectId}/issues`,
    create: (projectId: string) => `/projects/${projectId}/issues`,
    detail: (projectId: string, issueId: string) => `/projects/${projectId}/issues/${issueId}`,
    update: (projectId: string, issueId: string) => `/projects/${projectId}/issues/${issueId}`,
    delete: (projectId: string, issueId: string) => `/projects/${projectId}/issues/${issueId}`,
  },

  // Wiki 相关
  wiki: {
    list: (projectId: string) => `/projects/${projectId}/wiki`,
    create: (projectId: string) => `/projects/${projectId}/wiki`,
    detail: (projectId: string, docId: string) => `/projects/${projectId}/wiki/${docId}`,
    update: (projectId: string, docId: string) => `/projects/${projectId}/wiki/${docId}`,
    delete: (projectId: string, docId: string) => `/projects/${projectId}/wiki/${docId}`,
  },

  // 报表相关
  report: {
    burndown: (projectId: string) => `/projects/${projectId}/reports/burndown`,
    cumulativeFlow: (projectId: string) => `/projects/${projectId}/reports/cumulative-flow`,
    velocity: (projectId: string) => `/projects/${projectId}/reports/velocity`,
  },

  // 通知相关
  notification: {
    list: '/notifications',
    unread: '/notifications/unread',
    markRead: (id: string) => `/notifications/${id}/read`,
    markAllRead: '/notifications/read-all',
  },
} as const;
```

---

## 前端 HTTP 客户端配置

### src/lib/api/axios.ts

```typescript
import axios, { AxiosInstance, AxiosError, AxiosRequestConfig } from 'axios';
import { Result } from '@/types/api';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

// 创建 Axios 实例
export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器 - 自动注入 Token
apiClient.interceptors.request.use(
  (config) => {
    let token: string | null = null;
    if (typeof window !== 'undefined') {
      token = localStorage.getItem('access_token');
    }
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// 响应拦截器 - 统一错误处理
apiClient.interceptors.response.use(
  (response) => response.data,
  (error: AxiosError<Result<unknown>>) => {
    if (error.response?.status === 401) {
      if (typeof window !== 'undefined') {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// 封装的请求方法
export const api = {
  get: <T>(url: string, config?: AxiosRequestConfig) =>
    apiClient.get<Result<T>>(url, config).then((res) => res.data),
  post: <T>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
    apiClient.post<Result<T>>(url, data, config).then((res) => res.data),
  put: <T>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
    apiClient.put<Result<T>>(url, data, config).then((res) => res.data),
  patch: <T>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
    apiClient.patch<Result<T>>(url, data, config).then((res) => res.data),
  delete: <T>(url: string, config?: AxiosRequestConfig) =>
    apiClient.delete<Result<T>>(url, config).then((res) => res.data),
};

export default apiClient;
```

---

**文档生成时间**: 2026-03-12
**文档版本**: 1.0.0
**OpenAPI 版本**: 3.0.3
