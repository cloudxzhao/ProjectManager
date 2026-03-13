# 代码审查文档

本目录包含 ProjectHub 项目的代码审查报告和 related 文档。

## 目录结构

```
code-review/
├── README.md                       # 本文件
├── backend-review/                 # 后端代码审查
│   ├── latest-report.md            # 最新审查报告
│   └── issues.json                 # 问题列表 (JSON 格式)
├── frontend-review/                # 前端代码审查
│   ├── latest-report.md            # 最新审查报告
│   └── issues.json                 # 问题列表 (JSON 格式)
└── summary/                        # 综合审查报告
    ├── latest-summary.md           # 最新综合报告
    └── metrics.json                # 质量指标
```

## 审查团队

code-review 团队由以下 Agent 组成：

| Agent | 职责 | 审查范围 |
|-------|------|----------|
| **code-review-leader** | 审查协调和报告汇总 | 全栈 |
| **backend-code-reviewer** | 后端代码审查 | Builder/backend/src/main/java/ |
| **frontend-code-reviewer** | 前端代码审查 | Builder/frontend/src/ |

## 使用方式

### 启动代码审查

```bash
# 启动完整的代码审查团队
/agent code-review

# 或单独启动某个审查 agent
/agent backend-code-reviewer
/agent frontend-code-reviewer
```

### 查看审查报告

- **后端审查报告**: `backend-review/latest-report.md`
- **前端审查报告**: `frontend-review/latest-report.md`
- **综合审查报告**: `summary/latest-summary.md`

## 问题严重程度定义

| 级别 | 说明 | 响应时间 |
|------|------|----------|
| **Critical** | 安全漏洞、数据损坏、系统崩溃 | 立即修复 |
| **High** | 功能缺陷、严重性能问题 | 24 小时内 |
| **Medium** | 代码质量问题、可维护性问题 | 本周内 |
| **Low** | 代码风格、注释缺失 | 下次迭代 |

## 审查检查清单

### 后端审查重点

- [ ] 代码规范符合性
- [ ] Spring Boot 最佳实践
- [ ] SQL 注入防护
- [ ] 认证授权检查
- [ ] 性能优化（N+1 查询、索引）
- [ ] 异常处理
- [ ] 单元测试覆盖

### 前端审查重点

- [ ] TypeScript 类型安全
- [ ] React Hooks 正确使用
- [ ] Next.js 最佳实践
- [ ] XSS 防护
- [ ] 性能优化（重渲染、代码分割）
- [ ] 无障碍访问
- [ ] 响应式设计

## 审查报告格式

每个审查报告包含以下内容：

1. **审查信息** - 日期、范围、文件数
2. **问题汇总** - 按严重程度统计
3. **问题详情** - 每个问题的详细描述、位置、建议
4. **总体评价** - 代码质量总体评价
5. **改进建议** - 优先级改进建议

## 历史报告

| 日期 | 后端报告 | 前端报告 | 综合报告 |
|------|----------|----------|----------|
| 2026-03-14 | [查看](backend-review/latest-report.md) | [查看](frontend-review/latest-report.md) | [查看](summary/latest-summary.md) |

---

*最后更新：2026-03-14*
