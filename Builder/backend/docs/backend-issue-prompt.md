# 后端 Issue 修复任务 Prompt

## 项目信息

| 属性 | 值 |
|------|-----|
| 项目名称 | ProjectHub |
| 模块 | Backend (Spring Boot + Java 21) |
| 问题数量 | 5 个 |
| 数据库 | PostgreSQL |
| API 基础地址 | `http://localhost:9527/api/v1` |

---

## 修复优先级

### 🟡 优先级 1 - 中优先级问题（应在本周内修复）

1. **BACKEND-001**: TaskVO 缺少前端需要的统计字段
2. **BACKEND-003**: ProjectVO 缺少前端需要的统计字段
3. **BACKEND-005**: UserVO 缺少 role 字段

### 🟢 优先级 2 - 低优先级问题（可延后修复）

4. **BACKEND-002**: TaskVO 字段命名与前端期望不一致
5. **BACKEND-004**: ProjectVO 字段命名不一致 (themeColor vs color)

---

## 详细修复任务

### Issue BACKEND-001: TaskVO 缺少前端需要的统计字段

**严重程度**: medium

**问题描述**:
后端 TaskVO 缺少 subtaskCount、completedSubtaskCount、commentCount 等前端展示所需的统计字段，前端需要额外请求获取这些数据。

**受影响的 API**:
- `GET /api/v1/projects/{projectId}/tasks`
- `GET /api/v1/projects/{projectId}/tasks/{id}`

**需要添加的字段**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| subtaskCount | Integer | 子任务总数 |
| completedSubtaskCount | Integer | 已完成子任务数 |
| commentCount | Integer | 评论总数 |

**修复方案**:

```java
// 1. TaskVO.java - 添加字段
package com.projecthub.task.dto;

@Data
public class TaskVO {
    // ... 现有字段

    private Integer subtaskCount;
    private Integer completedSubtaskCount;
    private Integer commentCount;
}
```

```java
// 2. TaskMapper.java - 添加统计查询方法
List<TaskVO> selectTasksWithCounts(Long projectId);
TaskVO selectTaskWithCounts(Long taskId);
```

```xml
<!-- TaskMapper.xml - 添加统计查询 SQL -->
<select id="selectTaskWithCounts" resultType="com.projecthub.task.dto.TaskVO">
    SELECT t.*,
           (SELECT COUNT(*) FROM task WHERE parent_id = t.id) as subtaskCount,
           (SELECT COUNT(*) FROM task WHERE parent_id = t.id AND status = 'DONE') as completedSubtaskCount,
           (SELECT COUNT(*) FROM task_comment WHERE task_id = t.id) as commentCount
    FROM task t
    WHERE t.id = #{id}
</select>
```

```java
// 3. TaskService.java - 使用新的查询方法
public TaskVO getTaskById(Long projectId, Long id) {
    return taskMapper.selectTaskWithCounts(id);
}

public List<TaskVO> getTasksByProjectId(Long projectId, TaskQuery query) {
    return taskMapper.selectTasksWithCounts(projectId);
}
```

**需要修改的文件**:
- `src/main/java/com/projecthub/task/dto/TaskVO.java`
- `src/main/java/com/projecthub/task/mapper/TaskMapper.java`
- `src/main/resources/mapper/TaskMapper.xml`
- `src/main/java/com/projecthub/task/service/TaskService.java`
- `src/main/java/com/projecthub/task/controller/TaskController.java`

---

### Issue BACKEND-003: ProjectVO 缺少前端需要的统计字段

**严重程度**: medium

**问题描述**:
后端 ProjectVO 缺少 memberCount、taskCount、completedTaskCount 等前端项目列表展示所需的统计字段。

**受影响的 API**:
- `GET /api/v1/projects`
- `GET /api/v1/projects/{id}`

**需要添加的字段**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| memberCount | Integer | 项目成员数 |
| taskCount | Integer | 任务总数 |
| completedTaskCount | Integer | 已完成任务数 |

**修复方案**:

```java
// 1. ProjectVO.java - 添加字段
package com.projecthub.project.dto;

@Data
public class ProjectVO {
    // ... 现有字段

    private Integer memberCount;
    private Integer taskCount;
    private Integer completedTaskCount;
}
```

```java
// 2. ProjectMapper.java - 添加统计查询方法
List<ProjectVO> selectProjectsWithCounts(Long userId);
ProjectVO selectProjectWithCounts(Long projectId);
```

```xml
<!-- ProjectMapper.xml - 添加统计查询 SQL -->
<select id="selectProjectsWithCounts" resultType="com.projecthub.project.dto.ProjectVO">
    SELECT p.*,
           (SELECT COUNT(*) FROM project_member WHERE project_id = p.id) as memberCount,
           (SELECT COUNT(*) FROM task WHERE project_id = p.id) as taskCount,
           (SELECT COUNT(*) FROM task WHERE project_id = p.id AND status = 'DONE') as completedTaskCount
    FROM project p
    WHERE p.id IN (SELECT project_id FROM project_member WHERE user_id = #{userId})
</select>
```

```java
// 3. ProjectService.java - 使用新的查询方法
public List<ProjectVO> getUserProjects(Long userId) {
    return projectMapper.selectProjectsWithCounts(userId);
}

public ProjectVO getProjectById(Long id) {
    return projectMapper.selectProjectWithCounts(id);
}
```

**需要修改的文件**:
- `src/main/java/com/projecthub/project/dto/ProjectVO.java`
- `src/main/java/com/projecthub/project/mapper/ProjectMapper.java`
- `src/main/resources/mapper/ProjectMapper.xml`
- `src/main/java/com/projecthub/project/service/ProjectService.java`

---

### Issue BACKEND-005: UserVO 缺少 role 字段

**严重程度**: medium

**问题描述**:
前端 User 类型定义了 role 字段用于权限控制，但后端 UserVO 未返回该字段。

**受影响的 API**:
- `GET /api/v1/user/profile`

**需要添加的字段**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| role | String | 用户角色 (ADMIN, MEMBER, GUEST) |

**修复方案**:

```java
// 1. UserVO.java - 添加 role 字段
package com.projecthub.user.dto;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String status;
    private String role;  // 新增: ADMIN, MEMBER, GUEST
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

```java
// 2. UserService.java - 在获取用户信息时设置 role
public UserVO getCurrentUser() {
    User user = getCurrentUserEntity();
    UserVO vo = userMapper.toVO(user);

    // 根据用户权限设置角色
    Set<String> roles = user.getRoles();
    if (roles.contains("ADMIN")) {
        vo.setRole("ADMIN");
    } else if (roles.contains("MEMBER")) {
        vo.setRole("MEMBER");
    } else {
        vo.setRole("GUEST");
    }

    return vo;
}
```

**需要修改的文件**:
- `src/main/java/com/projecthub/user/dto/UserVO.java`
- `src/main/java/com/projecthub/user/service/UserService.java`
- `src/main/java/com/projecthub/user/mapper/UserMapper.java`

---

### Issue BACKEND-002: TaskVO 字段命名与前端期望不一致

**严重程度**: low

**问题描述**:
TaskVO 中部分字段命名与前端期望不一致：position vs order，creatorId vs reporterId。

**当前字段**: `position`, `creatorId`
**期望字段**: `order`, `reporterId`

**修复方案**:
可以选择以下方案之一：

**方案一**: 后端修改字段名（推荐）
```java
// TaskVO.java - 重命名字段
private Integer order;      // 原 position
private Long reporterId;   // 原 creatorId
```

同时更新数据库字段（需要新建 migration）：
```sql
ALTER TABLE task RENAME COLUMN position TO task_order;
```

**方案二**: 添加 JSON 序列化别名（不推荐，保持单一数据源）

建议使用方案一，保持前后端命名一致性。

**需要修改的文件**:
- `src/main/java/com/projecthub/task/dto/TaskVO.java`
- `src/main/java/com/projecthub/task/entity/Task.java`
- `src/main/resources/db/migration/V*__rename_task_fields.sql`

---

### Issue BACKEND-004: ProjectVO 字段命名不一致 (themeColor vs color)

**严重程度**: low

**问题描述**:
后端 ProjectVO 使用 themeColor 字段名，前端期望使用 color。

**当前字段**: `themeColor`
**期望字段**: `color`

**修复方案**:

```java
// ProjectVO.java - 重命名字段
// 修改前
private String themeColor;

// 修改后
private String color;
```

同时更新数据库字段：
```sql
ALTER TABLE project RENAME COLUMN theme_color TO project_color;
```

**需要修改的文件**:
- `src/main/java/com/projecthub/project/dto/ProjectVO.java`
- `src/main/java/com/projecthub/project/entity/Project.java`
- `src/main/resources/db/migration/V*__rename_project_color.sql`

---

## 验证检查清单

完成修复后，请验证以下场景：

### API 响应验证

- [ ] GET /api/v1/projects - 响应包含 memberCount, taskCount, completedTaskCount
- [ ] GET /api/v1/projects/{id} - 响应包含 memberCount, taskCount, completedTaskCount
- [ ] GET /api/v1/projects/{projectId}/tasks - 响应包含 subtaskCount, completedSubtaskCount, commentCount
- [ ] GET /api/v1/projects/{projectId}/tasks/{id} - 响应包含 subtaskCount, completedSubtaskCount, commentCount
- [ ] GET /api/v1/user/profile - 响应包含 role 字段

### 字段命名验证

- [ ] TaskVO 使用 order 字段（而非 position）
- [ ] TaskVO 使用 reporterId 字段（而非 creatorId）
- [ ] ProjectVO 使用 color 字段（而非 themeColor）

### 数据一致性验证

- [ ] 统计字段数值正确（memberCount, taskCount 等）
- [ ] 字段类型为 Integer/Long（不是 String）
- [ ] OpenAPI 文档自动更新

---

## 数据库 Migration 注意事项

对于字段重命名操作，需要创建 Flyway migration 文件：

```sql
-- src/main/resources/db/migration/V2__fix_field_naming.sql

-- Task 表字段重命名
ALTER TABLE task RENAME COLUMN position TO task_order;
ALTER TABLE task RENAME COLUMN creator_id TO reporter_id;

-- Project 表字段重命名
ALTER TABLE project RENAME COLUMN theme_color TO project_color;
```

---

## 参考资源

- 后端代码目录: `/data/project/ProjectManager/Builder/backend/`
- OpenAPI 文档: `http://localhost:9527/v3/api-docs`
- 数据库配置: `src/main/resources/application-dev.yml`