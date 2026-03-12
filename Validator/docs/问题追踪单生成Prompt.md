# 问题追踪单生成任务 Prompt

## 任务目标

根据 `API接口对比分析报告.md` 的分析结果，将接口一致性问题拆分为前端修改任务和后端修改任务，生成结构化的 JSON 格式问题追踪单。

---

## 输入文件

- **位置**: `/data/project/ProjectManager/Validator/docs/API接口对比分析报告.md`
- **内容**: 前后端 API 接口差异分析报告，包含 URI 差异、入参差异、响应差异、风险评估等

---

## 任务要求

### 1. 问题拆分原则

| 原则 | 说明 |
|------|------|
| **单一责任** | 每个问题只归属一方（前端或后端），避免双方都需要修改 |
| **最小改动** | 优先选择改动量小的一方进行修复 |
| **向后兼容** | 如需新增接口，后端优先考虑添加别名而非修改现有接口 |
| **文档同步** | 仅涉及文档描述不一致的，归类为文档问题 |

### 2. 拆分决策矩阵

| 问题类型 | 优先修改方 | 理由 |
|----------|------------|------|
| 前端路径与后端不匹配 | 前端 | 后端接口已实现且可能被其他服务调用 |
| 前端缺少后端已有功能 | 前端 | 后端已实现，前端补充调用即可 |
| 后端缺少前端期望功能 | 后端 | 功能缺失需要后端实现 |
| ID 类型不一致 (string vs integer) | 前端 | 后端数据库设计为整数，前端适配 |
| 字段命名不一致 | 双方协商 | 选择更合理的命名，一方适配 |
| 枚举值定义不一致 | 前端 | 后端为权威来源，前端适配 |
| 响应字段缺失 | 后端 | 补充前端需要的字段 |

### 3. 问题去重规则

- 同一接口的 URI、入参、响应问题合并为一条记录
- 关联性强的一组问题合并为一条（如：所有任务接口路径问题）
- 相同根因的问题合并为一条，在 `affected_apis` 中列出所有受影响接口

---

## 输出格式定义

### 问题单 JSON Schema

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "IssueTracker",
  "type": "object",
  "properties": {
    "metadata": {
      "type": "object",
      "properties": {
        "project": { "type": "string", "description": "项目名称" },
        "module": { "type": "string", "description": "模块名称: frontend/backend" },
        "generated_at": { "type": "string", "format": "date-time" },
        "total_issues": { "type": "integer" },
        "version": { "type": "string" }
      },
      "required": ["project", "module", "generated_at", "total_issues"]
    },
    "issues": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "id": {
            "type": "string",
            "pattern": "^[A-Z]+-[0-9]+$",
            "description": "问题ID，格式: FRONTEND-001 或 BACKEND-001"
          },
          "title": {
            "type": "string",
            "description": "问题标题，简洁描述问题"
          },
          "severity": {
            "type": "string",
            "enum": ["critical", "high", "medium", "low"],
            "description": "严重程度: critical=阻断/阻塞, high=高优先级, medium=中等, low=低优先级"
          },
          "category": {
            "type": "string",
            "enum": ["uri", "parameter", "response", "schema", "authentication", "feature"],
            "description": "问题分类"
          },
          "description": {
            "type": "string",
            "description": "详细问题描述"
          },
          "affected_apis": {
            "type": "array",
            "items": { "type": "string" },
            "description": "受影响的API列表，格式: METHOD /path"
          },
          "current_state": {
            "type": "object",
            "description": "当前状态（前端期望或后端实际）",
            "properties": {
              "path": { "type": "string" },
              "method": { "type": "string" },
              "parameters": { "type": "object" },
              "response": { "type": "object" }
            }
          },
          "expected_state": {
            "type": "object",
            "description": "期望状态（修复后的目标）",
            "properties": {
              "path": { "type": "string" },
              "method": { "type": "string" },
              "parameters": { "type": "object" },
              "response": { "type": "object" }
            }
          },
          "solution": {
            "type": "object",
            "description": "解决方案",
            "properties": {
              "type": {
                "type": "string",
                "enum": ["modify", "add", "delete", "rename", "refactor"]
              },
              "description": { "type": "string" },
              "files_to_modify": {
                "type": "array",
                "items": { "type": "string" },
                "description": "需要修改的文件列表"
              },
              "code_example": {
                "type": "string",
                "description": "修复代码示例（可选）"
              }
            }
          },
          "related_issues": {
            "type": "array",
            "items": { "type": "string" },
            "description": "关联问题ID列表"
          },
          "labels": {
            "type": "array",
            "items": {
              "type": "string",
              "enum": ["api", "breaking-change", "documentation", "performance", "security", "ux"]
            }
          },
          "estimated_effort": {
            "type": "string",
            "enum": ["trivial", "small", "medium", "large"],
            "description": "预估工作量: trivial=几分钟, small=1-2小时, medium=半天, large=1天以上"
          }
        },
        "required": ["id", "title", "severity", "category", "description", "affected_apis", "solution"]
      }
    }
  },
  "required": ["metadata", "issues"]
}
```

---

## 输出文件要求

生成两个独立的 JSON 文件：

| 文件名 | 说明 |
|--------|------|
| `frontend-issues.json` | 前端问题追踪单 |
| `backend-issues.json` | 后端问题追踪单 |

---

## 示例输出

### frontend-issues.json 示例

```json
{
  "metadata": {
    "project": "ProjectHub",
    "module": "frontend",
    "generated_at": "2026-03-12T10:00:00Z",
    "total_issues": 5,
    "version": "1.0.0"
  },
  "issues": [
    {
      "id": "FRONTEND-001",
      "title": "任务接口路径缺少 projectId 参数",
      "severity": "critical",
      "category": "uri",
      "description": "前端任务相关接口使用 /api/v1/tasks/{id} 路径，但后端实际路径需要 projectId 参数",
      "affected_apis": [
        "GET /api/v1/tasks/{id}",
        "PUT /api/v1/tasks/{id}",
        "DELETE /api/v1/tasks/{id}",
        "POST /api/v1/tasks/{id}/move",
        "GET /api/v1/tasks/{id}/subtasks",
        "POST /api/v1/tasks/{id}/toggle-complete"
      ],
      "current_state": {
        "path": "/api/v1/tasks/{id}"
      },
      "expected_state": {
        "path": "/api/v1/projects/{projectId}/tasks/{id}"
      },
      "solution": {
        "type": "modify",
        "description": "修改前端 API 调用路径，添加 projectId 参数",
        "files_to_modify": [
          "src/api/task.ts",
          "src/views/task/TaskDetail.vue"
        ],
        "code_example": "// 修改前\nconst getTask = (id: string) => request.get(`/tasks/${id}`)\n\n// 修改后\nconst getTask = (projectId: string, id: string) =>\n  request.get(`/projects/${projectId}/tasks/${id}`)"
      },
      "related_issues": [],
      "labels": ["api", "breaking-change"],
      "estimated_effort": "medium"
    }
  ]
}
```

### backend-issues.json 示例

```json
{
  "metadata": {
    "project": "ProjectHub",
    "module": "backend",
    "generated_at": "2026-03-12T10:00:00Z",
    "total_issues": 2,
    "version": "1.0.0"
  },
  "issues": [
    {
      "id": "BACKEND-001",
      "title": "TaskVO 缺少前端需要的统计字段",
      "severity": "medium",
      "category": "schema",
      "description": "后端 TaskVO 缺少 subtaskCount, completedSubtaskCount, commentCount 等前端需要的统计字段",
      "affected_apis": [
        "GET /api/v1/projects/{projectId}/tasks",
        "GET /api/v1/projects/{projectId}/tasks/{id}"
      ],
      "current_state": {
        "response": {
          "missing_fields": ["subtaskCount", "completedSubtaskCount", "commentCount"]
        }
      },
      "expected_state": {
        "response": {
          "added_fields": [
            { "name": "subtaskCount", "type": "integer" },
            { "name": "completedSubtaskCount", "type": "integer" },
            { "name": "commentCount", "type": "integer" }
          ]
        }
      },
      "solution": {
        "type": "add",
        "description": "在 TaskVO 中添加统计字段",
        "files_to_modify": [
          "src/main/java/com/projecthub/task/dto/TaskVO.java",
          "src/main/java/com/projecthub/task/service/TaskService.java"
        ]
      },
      "related_issues": [],
      "labels": ["api"],
      "estimated_effort": "small"
    }
  ]
}
```

---

## 执行步骤

1. **阅读分析报告** - 完整阅读 `API接口对比分析报告.md`
2. **问题分类** - 按 URI、入参、响应、Schema 分类整理
3. **责任判定** - 根据拆分决策矩阵确定修改方
4. **问题合并** - 合并相同根因的问题，避免重复
5. **生成 JSON** - 按照定义的 Schema 生成两个 JSON 文件
6. **保存文件** - 输出到 `/data/project/ProjectManager/Validator/docs/` 目录