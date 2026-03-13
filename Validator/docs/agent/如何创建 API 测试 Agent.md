# 如何创建 API 测试 Agent

本文档说明如何在 `.claude/agents/` 目录下创建一个专门负责后端 API 测试的 Agent。

---

## 步骤 1：创建 Agent 配置文件

在项目根目录下创建 `.claude/agents/api-tester.md` 文件：

```markdown
---
description: 后端 API 接口测试专家，负责执行测试并生成报告
tools: ["Bash", "Read", "Write", "Glob", "Grep", "WebFetch", "TaskCreate", "TaskList", "TaskUpdate"]
model: "sonnet"
---

# API 测试 Agent

## 角色

你是一名**资深后端 API 测试工程师**，专注于 Spring Boot 应用的接口测试。

## 核心职责

- ✅ 执行后端 API 接口的功能测试、边界测试、异常场景测试
- ✅ 记录发现的所有问题和缺陷
- ✅ 生成详细的测试报告
- ❌ **不修改代码** - 只记录问题，不进行修复

## 工作原则

1. **严谨细致**：不放过任何可疑的测试点和边界情况
2. **客观记录**：如实记录测试结果，不遗漏任何失败用例
3. **可复现**：每个问题都要提供清晰的复现步骤
4. **只测不改**：发现缺陷时只记录，不进行代码修复

## 项目信息

- **API 基础地址**: `http://localhost:9527/api/v1`
- **OpenAPI 文档**: `http://localhost:8080/v3/api-docs`
- **后端代码目录**: `/data/project/ProjectManager/Builder/backend/`

## 测试账户

| 字段 | 值 |
|------|-----|
| 邮箱 | admin@projecthub.com |
| 密码 | Admin123 |
| 角色 | ADMIN |

## 测试流程

### 1. 准备工作
- 获取并解析 OpenAPI 文档
- 确认服务已启动
- 登录获取 Token

### 2. 执行测试
- 功能测试：验证正常场景
- 边界测试：验证边界值
- 异常测试：验证错误处理
- 认证测试：验证权限控制

### 3. 记录问题
使用以下格式记录每个问题：

```
### [BUG-XXX] 问题标题
- 严重程度：HIGH/MEDIUM/LOW
- 接口：METHOD /path
- 预期：xxx
- 实际：xxx
- 复现步骤：1... 2... 3...
```

### 4. 生成报告
输出到 `docs/API 测试报告.md`

## 输出物

1. `docs/backend-issues.json` - 问题列表
2. `docs/API 测试报告.md` - 完整测试报告
```

---

## 步骤 2：创建测试配置（可选）

创建 `.claude/agents/api-tester-config.json` 存储测试配置：

```json
{
  "apiBaseUrl": "http://localhost:9527/api/v1",
  "openApiUrl": "http://localhost:8080/v3/api-docs",
  "backendPath": "/data/project/ProjectManager/Builder/backend",
  "testAccount": {
    "email": "admin@projecthub.com",
    "password": "Admin123"
  },
  "outputDir": "docs"
}
```

---

## 步骤 3：创建测试用例模板

创建 `docs/API 测试用例模板.md`：

```markdown
# API 测试用例

## 接口：[接口名称]

| 用例 ID | 测试场景 | 输入数据 | 预期结果 | 实际结果 | 状态 |
|---------|----------|----------|----------|----------|------|
| TC-001  | 正常场景 | {...}    | 200 OK   |          | ⏳   |
| TC-002  | 边界测试 | {...}    | 400 Bad  |          | ⏳   |
| TC-003  | 异常测试 | {...}    | 4xx 错误  |          | ⏳   |
```

---

## 使用方法

### 方式 1：直接调用

在对话中使用：
```
@api-tester 请测试项目管理的 CRUD 接口
```

### 方式 2：使用 Slash 命令

创建 `.claude/commands/test-api.md`：
```markdown
<%
const agent = await agent.create({
  name: "api-tester",
  type: "api-tester"
});
%>
```

然后使用：
```
/test-api 项目管理模块
```

### 方式 3：使用 Agent 工具

```
使用 Agent 工具，subagent_type 选择 "api-tester"
prompt: "测试所有用户相关的 API 接口"
```

---

## 测试报告示例

执行测试后，Agent 会生成类似以下的报告：

```markdown
# API 测试报告

## 测试概览
- 测试日期：2026-03-13
- 接口总数：25
- 已测接口数：25
- 发现问题数：5
- 通过率：80%

## 问题汇总

### 严重问题 (HIGH)
1. [BUG-001] 创建项目时颜色字段为 null 导致 500 错误
2. [BUG-002] 用户邮箱未做唯一性校验

### 中等问题 (MEDIUM)
3. [BUG-003] 分页参数 page=0 未返回第一页
4. [BUG-004] 删除不存在的资源返回 500 而非 404

### 轻微问题 (LOW)
5. [BUG-005] 响应时间戳格式不统一
```

---

## 验证 Agent 是否正常工作

创建 Agent 后，运行以下命令验证：

```bash
# 检查 Agent 文件是否存在
ls -la .claude/agents/api-tester.md

# 测试调用
claude '@api-tester 请检查 API 服务是否正常运行'
```

---

## 注意事项

1. **权限控制**：确保 Agent 只有测试相关的工具权限
2. **数据安全**：使用测试环境数据库，避免影响生产数据
3. **清理工作**：测试完成后清理生成的测试数据
4. **日志保存**：保留所有测试请求和响应的日志
5. **问题追踪**：每个问题必须有唯一的 ID 便于追踪

---

## 相关文件

| 文件 | 说明 |
|------|------|
| `docs/agent/API 测试 Agent.md` | Agent 详细文档 |
| `docs/API 测试报告.md` | 测试报告输出 |
| `docs/backend-issues.json` | 问题列表 JSON |
| `docs/API 测试用例模板.md` | 测试用例模板 |
