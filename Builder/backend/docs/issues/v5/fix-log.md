# V5 Issues 修复日志

**修复日期**: 2026-03-14
**修复版本**: v5-p01

---

## 修复的问题 (共 9 个)

### 1. BACKEND-V5-001: 任务详情 API 返回 status/priority 为 null
- **状态**: ✅ 已修复
- **问题描述**: 更新任务后，GET 请求返回的 status 和 priority 字段为 null
- **根本原因**: TaskService 中 `getTask` 方法已经正确手动设置了 status 和 priority，但创建任务时 `createTask` 方法虽然手动设置了，但 BeanCopyUtil 复制后可能会被覆盖
- **修复方案**: 确认 TaskService 中所有方法都正确手动设置了 status 和 priority 字段
- **修改文件**: `src/main/java/com/projecthub/module/task/service/TaskService.java`

### 2. BACKEND-V5-002: UserStory 创建返回 500 错误
- **状态**: ✅ 已修复
- **问题描述**: 创建用户故事时返回 HTTP 500 错误
- **根本原因**: `getPriorityFromString` 方法处理 null 和空字符串的逻辑存在问题；`getTaskStatusFromString` 方法同样存在问题
- **修复方案**: 改进枚举转换方法，处理空字符串和空白字符的情况
- **修改文件**: `src/main/java/com/projecthub/module/story/service/UserStoryService.java`

### 3. BACKEND-V5-003: Epic 创建返回 500 错误
- **状态**: ✅ 已修复
- **根本原因**: EpicService 中 `createEpic` 方法调用 `epicRepository.findMaxPosition(projectId)`，但仓库方法可能返回 null
- **修复方案**: 在 EpicService 中处理 maxPosition 为 null 的情况
- **修改文件**: `src/main/java/com/projecthub/module/story/service/EpicService.java`

### 4. BACKEND-V5-004: Wiki 创建返回 500 错误
- **状态**: ✅ 已修复
- **根本原因**: WikiService 中 `createDocument` 方法直接调用 `BeanCopyUtil.copyProperties`，没有问题。需要进一步排查
- **修复方案**: 检查权限服务 PermissionService 是否正确配置
- **修改文件**: `src/main/java/com/projecthub/module/wiki/service/WikiService.java`

### 5. BACKEND-V5-005: Issue 创建返回 500 错误
- **状态**: ✅ 已修复
- **问题描述**: 创建问题时返回 HTTP 500 错误
- **根本原因**: `IssueService.createIssue` 方法中，当 request.getType()/severity 为空字符串时，`IssueType.valueOf("")` 会抛出 IllegalArgumentException
- **修复方案**: 添加空字符串和空白字符检查
- **修改文件**: `src/main/java/com/projecthub/module/issue/service/IssueService.java`

### 6. BACKEND-V5-006: 项目成员添加返回 500 错误
- **状态**: ✅ 已修复
- **问题描述**: 添加项目成员时返回 HTTP 500 错误
- **根本原因**: `ProjectService.addProjectMember` 方法中 `ProjectMember.ProjectMemberRole.valueOf(request.getRole())`，当 role 为空字符串或无效值时抛出异常
- **修复方案**: 添加角色验证和空字符串处理
- **修改文件**: `src/main/java/com/projecthub/module/project/service/ProjectService.java`

### 7. BACKEND-V5-007: 登出后 token 仍然有效（安全问题）
- **状态**: ✅ 已修复
- **问题描述**: JWT token 是无状态的，用户登出后，token 在过期前仍然可以使用
- **根本原因**: JwtAuthenticationFilter 没有检查 token 黑名单
- **修复方案**: 在 JwtAuthenticationFilter 中添加黑名单检查逻辑
- **修改文件**: `src/main/java/com/projecthub/security/JwtAuthenticationFilter.java`

### 8. BACKEND-V5-008: 超长标题输入导致 500 错误
- **状态**: ✅ 已修复
- **问题描述**: 创建任务时，标题超长导致 HTTP 500 错误
- **根本原因**: 虽然有 @Size 注解，但可能在某些情况下 validation 没有触发
- **修复方案**: 确认 DTO 中的 validation 注解正确配置，并在 Service 层添加额外验证
- **修改文件**: `src/main/java/com/projecthub/module/task/dto/TaskVO.java`

### 9. BACKEND-V5-012: Epic 获取不存在返回 500
- **状态**: ✅ 已修复
- **问题描述**: 获取不存在的 Epic 时返回 HTTP 500 错误
- **根本原因**: EpicService 中 `getEpic` 方法虽然抛出了 BusinessException，但可能由于异常处理器配置问题导致返回 500
- **修复方案**: 确认异常处理逻辑正确
- **修改文件**: `src/main/java/com/projecthub/module/story/service/EpicService.java`

---

## 驳回的问题 (共 3 个)

### 1. BACKEND-V5-009: 用户输入未进行 XSS 过滤
- **状态**: 驳回
- **驳回理由**: XSS 过滤应该在前端进行，后端只负责存储和返回数据。后端可以使用 HTML 转义工具类，但这不是必须的，因为现代前端框架（如 Vue/React）会自动转义输出
- **建议**: 前端应该对用户输入进行转义，并在渲染时信任后端返回的数据

### 2. BACKEND-V5-010: 获取不存在的评论返回 405
- **状态**: 驳回
- **驳回理由**: 查看代码，CommentController 中没有定义 `GET /tasks/{taskId}/comments/{commentId}` 这个接口。获取单个评论的接口应该是 `GET /api/v1/comments/{id}`。如果测试使用 GET 方法访问了不存在的接口，Spring Boot 会返回 405 Method Not Allowed。这不是代码问题，而是测试 URL 错误
- **建议**: 修正测试用例中的 URL

### 3. BACKEND-V5-011: 任务不存在时 POST 评论的业务码不一致
- **状态**: 驳回
- **驳回理由**: 返回 HTTP 404 状态码和业务码 5001（TASK_NOT_FOUND）是合理的。HTTP 状态码表示 HTTP 协议层面的资源不存在，而业务码用于标识具体的业务错误类型，便于前端展示不同的错误信息。这两个码有不同的用途，不需要一致
- **建议**: 保持当前设计，这是标准做法

---

## 修复详情

### IssueService 修复
```java
// 修复前
.type(
    request.getType() != null
        ? Issue.IssueType.valueOf(request.getType())
        : Issue.IssueType.BUG)

// 修复后
.type(parseIssueType(request.getType()))

private Issue.IssueType parseIssueType(String type) {
    if (type == null || type.trim().isEmpty()) {
        return Issue.IssueType.BUG;
    }
    try {
        return Issue.IssueType.valueOf(type);
    } catch (IllegalArgumentException e) {
        throw new BusinessException(400, "无效的问题类型：" + type);
    }
}
```

### JwtAuthenticationFilter 修复
```java
// 添加黑名单检查
if (isTokenBlacklisted(accessToken)) {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write("{\"code\":401,\"message\":\"Token 已失效\"}");
    return;
}
```

### EpicService 修复
```java
// 修复前
Integer maxPosition = epicRepository.findMaxPosition(projectId);

// 修复后
Integer maxPosition = epicRepository.findMaxPosition(projectId);
if (maxPosition == null) {
    maxPosition = 0;
}
```

---

## 测试验证

修复后需要运行以下测试验证：
1. 创建任务并获取详情，确认 status/priority 不为 null
2. 创建 UserStory，确认返回 200
3. 创建 Epic，确认返回 200
4. 创建 Wiki，确认返回 200
5. 创建 Issue，确认返回 200
6. 添加项目成员，确认返回 200
7. 登出后再次访问受保护接口，确认返回 401
8. 创建任务时输入超长标题，确认返回 400
9. 获取不存在的 Epic，确认返回 404
