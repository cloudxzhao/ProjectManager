# 后端代码审查报告

**审查日期**: 2026-03-14
**审查人**: backend-code-reviewer
**审查范围**: `Builder/backend/src/main/java/com/projecthub/`

---

## 执行摘要

| 维度 | 评分 | 说明 |
|------|------|------|
| 整体质量 | ⚠️ 中等 | 基础架构良好，但存在多处需要改进的问题 |
| 安全性 | ⚠️ 中等 | 认证授权机制健全，但存在 CSRF 和 CORS 配置风险 |
| 性能 | ✅ 良好 | 使用了缓存、索引和分页 |
| 可维护性 | ⚠️ 中等 | 代码结构清晰，但部分实现需要优化 |

**风险等级**: 🟡 中等 - 存在需要优先处理的安全和代码质量问题

---

## 审查范围

审查了以下核心模块：

- **安全认证**: `SecurityConfig.java`, `JwtAuthenticationFilter.java`, `JwtUtil.java`, `PasswordUtil.java`
- **权限控制**: `PermissionAspect.java`, `PermissionService.java`
- **异常处理**: `GlobalExceptionHandler.java`, `BusinessException.java`, `ErrorCode.java`
- **业务模块**: `ProjectService.java`, `TaskService.java`, `AuthService.java`
- **控制器**: `ProjectController.java`, `AuthController.java`
- **工具类**: `HtmlUtil.java`, `BeanCopyUtil.java`

---

## 发现的问题

### 🔴 严重问题 (Critical)

#### 1. CORS 配置过于宽松 (SecurityConfig.java:127-131)

```java
configuration.addAllowedOriginPattern("*");  // ❌ 允许所有源
configuration.setAllowCredentials(true);      // ❌ 允许携带凭证
```

**风险**: 允许所有来源携带凭证的请求，可能导致 CSRF 攻击。

**建议**:
```java
// 仅允许信任的前端域名
configuration.addAllowedOriginPattern("http://localhost:3000");
configuration.addAllowedOriginPattern("https://yourdomain.com");
```

#### 2. BeanCopyUtil 使用反射性能低下且无类型安全 (BeanCopyUtil.java)

```java
T instance = target.getDeclaredConstructor().newInstance();
BeanUtils.copyProperties(source, instance);
```

**问题**:
- 使用反射性能较差
- 编译时无法检查属性匹配
- 静默忽略不匹配的属性

**建议**: 使用 MapStruct 进行编译时类型安全的对象映射。

#### 3. 权限检查逻辑不一致 (PermissionService.java:20-33)

```java
public boolean hasPermission(Long userId, Long projectId, String permissionCode) {
    // ...
    // 项目成员默认有基本权限，具体权限校验可根据 permissionCode 细化
    return true;  // ❌ 总是返回 true，未实际校验 permissionCode
}
```

**风险**: 权限校验形同虚设，任何项目成员都有所有权限。

**建议**: 实现基于角色的权限矩阵校验。

#### 4. Token 支持从 query parameter 获取 (JwtAuthenticationFilter.java:98-102)

```java
// 也支持从 query parameter 中获取 token
String token = request.getParameter("token");
if (StringUtils.hasText(token)) {
    return token;
}
```

**风险**: Token 可能出现在服务器日志、浏览器历史中。

**建议**: 仅支持从 Authorization header 获取 Token。

---

### 🟡 中等问题 (High)

#### 1. 全局权限检查未实现 (PermissionService.java:36-41)

```java
public boolean hasGlobalPermission(Long userId, String permissionCode) {
    // TODO: 实现全局权限检查（基于角色和权限表）
    // 暂时返回 true，后续完善
    return true;
}
```

**问题**: 全局权限校验缺失，管理员权限控制不完整。

#### 2. 日期验证不完整 (ProjectService.java:63-66)

```java
if (request.getEndDate().isBefore(request.getStartDate())) {
    throw new BusinessException(ErrorCode.PROJECT_DATE_INVALID, "结束日期不能早于开始日期");
}
```

**问题**:
- 未检查日期是否为 null（虽然 DTO 有@NotNull 注解）
- 未检查日期是否在过去

#### 3. 密码策略缺失 (AuthService.java)

```java
String encodedPassword = passwordUtil.encode(request.getPassword());
```

**问题**: 未对密码强度进行校验（长度、复杂度等）。

**建议**: 添加密码强度验证规则。

#### 4. 重复代码 - getCurrentUserId 方法

`TaskService.java`、`ProjectService.java` 都实现了相同的 `getCurrentUserId()` 方法。

**建议**: 提取到公共基类或工具类 `SecurityUtil`。

#### 5. 枚举字符串转换重复逻辑

多个 Service 中都有相同的枚举转字符串逻辑：

```java
taskVO.setStatus(task.getStatus().name());
taskVO.setPriority(task.getPriority().name());
```

**建议**: 在 VO 类内部处理转换逻辑。

---

### 🟢 轻微问题 (Medium/Low)

#### 1. 错误码命名不规范 (ErrorCode.java)

**问题**: 错误码消息应更具体，便于前端展示。

#### 2. 日志级别使用不一致

**问题**: 部分地方使用 `log.info` 记录正常流程，有些地方使用 `log.warn` 记录预期内的业务异常。

#### 3. HtmlUtil 使用正则匹配脚本标签

```java
private static final Pattern SCRIPT_PATTERN =
    Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
```

**建议**: 完全依赖 Jsoup 进行 HTML 清理，移除正则匹配逻辑。

---

## 正面发现

### ✅ 优点和最佳实践

1. **统一响应格式**: `Result<T>` 和 `PageResult<T>` 提供一致的 API 响应
2. **全局异常处理**: `GlobalExceptionHandler` 统一处理各类异常
3. **JWT 黑名单机制**: 使用 Redis 实现 Token 登出功能
4. **密码加密**: 使用 BCrypt 加密存储密码
5. **AOP 权限切面**: `PermissionAspect` 实现声明式权限控制
6. **XSS 防护**: `HtmlUtil` 提供 HTML 清理功能
7. **错误码规范**: 按模块划分的错误码体系
8. **事务管理**: 使用 `@Transactional` 保证数据一致性

---

## 改进建议

### 高优先级（建议尽快修复）

| 优先级 | 问题 | 建议 |
|--------|------|------|
| P0 | CORS 配置过于宽松 | 限制为具体域名 |
| P0 | 权限检查未实际执行 | 实现完整的 RBAC 权限矩阵 |
| P0 | Query Parameter 传 Token | 移除该支持，仅使用 Header |
| P1 | 全局权限检查缺失 | 实现基于角色的全局权限 |
| P1 | 密码策略缺失 | 添加密码强度验证 |

### 中优先级（技术债务）

| 优先级 | 问题 | 建议 |
|--------|------|------|
| P2 | BeanCopyUtil 使用反射 | 迁移到 MapStruct |
| P2 | getCurrentUserId 重复 | 提取到 SecurityUtil |
| P2 | 枚举转换重复 | 在 VO 层处理 |

### 低优先级（持续改进）

| 优先级 | 问题 | 建议 |
|--------|------|------|
| P3 | 日志级别不一致 | 制定日志规范 |
| P3 | 单元测试缺失 | 增加核心逻辑的单元测试 |
| P3 | API 文档待完善 | 补充 Swagger 注解 |

---

## 总结

ProjectHub 后端代码整体架构清晰，遵循 Spring Boot 最佳实践。主要优势在于统一响应格式、全局异常处理和 JWT 认证机制。但存在以下关键问题需要优先解决：

1. **安全性**: CORS 配置、权限校验逻辑、Token 传输方式需要修复
2. **代码质量**: 减少重复代码，使用类型安全的对象映射
3. **完整性**: 完善全局权限检查和密码策略

建议在下一阶段优先修复 P0 级别的安全问题，然后逐步解决技术债务。
