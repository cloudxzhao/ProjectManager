# Wiki 模块测试用例说明

## 测试文件列表

### 1. WikiServiceTest.java
**路径**: `src/test/java/com/projecthub/module/wiki/WikiServiceTest.java`

**说明**: Wiki 服务层集成测试，包含以下测试场景：

#### 1.1 基础 CRUD 测试 (任务 1)
| 测试 ID | 测试方法 | 说明 |
|---------|----------|------|
| 1.1 | createDocument_Success | 创建文档成功 |
| 1.2 | createDocument_ParentNotFound | 父文档不存在 |
| 1.3 | createDocument_ProjectNotFound | 项目不存在 |
| 1.4 | getDocument_Success | 获取文档详情成功 |
| 1.5 | getDocument_NotFound | 文档不存在 |
| 1.6 | updateDocument_Success | 更新文档成功 |
| 1.7 | updateDocument_TitleOnly | 只更新标题 |
| 1.8 | updateDocument_ContentOnly | 只更新内容 |
| 1.9 | updateDocument_NotFound | 文档不存在 |
| 1.10 | deleteDocument_Success | 删除文档成功 |
| 1.11 | deleteDocument_NotFound | 文档不存在 |
| 1.12 | getDocumentTree_Success | 获取文档树成功 |
| 1.13 | getDocumentTree_Empty | 空项目 |
| 1.14 | getDocumentHistory_Success | 获取历史记录成功 |
| 1.15 | createDocument_WithParent_Success | 创建带父子关系的文档 |

#### 2.1 树形结构测试 (任务 3)
| 测试 ID | 测试方法 | 说明 |
|---------|----------|------|
| 2.1 | treeStructure_ThreeLevels | 三层树形结构测试 |
| 2.2 | treeStructure_Depth_FiveLevels | 五层深度测试 |
| 2.3 | treeStructure_Width_TenChildren | 10 个子节点宽度测试 |

#### 3.1 压力测试 (任务 2)
| 测试 ID | 测试方法 | 说明 |
|---------|----------|------|
| 3.1 | stressTest_Create100FlatDocuments | 创建 100 条扁平文档 |
| 3.2 | stressTest_Create100TreeDocuments | 创建 100 条树形结构文档 |
| 3.3 | stressTest_ComplexTreeStructure | 复杂树形结构 (100 条，多层) |
| 3.4 | stressTest_BatchCreatePerformance | 批量创建性能测试 |

#### 4.1 边界条件测试
| 测试 ID | 测试方法 | 说明 |
|---------|----------|------|
| 4.1 | boundaryTest_EmptyTitle | 标题为空 |
| 4.2 | boundaryTest_TitleTooLong | 标题过长 (超过 200 字符) |
| 4.3 | boundaryTest_EmptyContent | 内容为空 (允许) |
| 4.4 | boundaryTest_NullContent | 内容为 null (允许) |

---

### 2. WikiControllerTest.java
**路径**: `src/test/java/com/projecthub/module/wiki/WikiControllerTest.java`

**说明**: Wiki 控制器层集成测试，测试 REST API 接口

| 测试 ID | 测试方法 | 说明 |
|---------|----------|------|
| 1 | createDocument_Success | 创建文档 API - 成功 |
| 2 | createDocument_WithParent_Success | 创建子文档 API |
| 3 | createDocument_EmptyTitle_Fail | 标题为空 (失败) |
| 4 | createDocument_TitleTooLong_Fail | 标题过长 (失败) |
| 5 | getDocument_Success | 获取文档详情 API - 成功 |
| 6 | getDocument_NotFound | 文档不存在 |
| 7 | getDocumentTree_Success | 获取文档树 API - 成功 |
| 8 | getDocumentTree_Empty | 空项目 |
| 9 | updateDocument_Success | 更新文档 API - 成功 |
| 10 | updateDocument_TitleOnly | 只更新标题 |
| 11 | updateDocument_NotFound | 文档不存在 |
| 12 | deleteDocument_Success | 删除文档 API - 成功 |
| 13 | deleteDocument_NotFound | 文档不存在 |
| 14 | getDocumentHistory_Success | 获取历史记录 API |
| 15 | stressTest_BatchCreate100Documents | 批量创建 100 条文档 |
| 16 | stressTest_CreateTreeStructure | 创建树形结构 (100 条) |
| 17 | treeStructure_ThreeLevels | 三层嵌套结构验证 |

---

## 运行测试

### 运行所有测试
```bash
cd ../Builder/backend
mvn test -Dtest=WikiServiceTest
```

### 运行控制器测试
```bash
mvn test -Dtest=WikiControllerTest
```

### 运行单个测试方法
```bash
mvn test -Dtest=WikiServiceTest#createDocument_Success
```

### 运行特定类别的测试
```bash
# 运行所有 CRUD 测试
mvn test -Dtest=WikiServiceTest#"1.*"

# 运行所有压力测试
mvn test -Dtest=WikiServiceTest#"3.*"
```

---

## 测试配置

### 测试环境 (application-test.yml)
- **数据库**: H2 内存数据库
- **Flyway**: 禁用
- **JPA**: create-drop 模式
- **日志**: DEBUG 级别

### 测试数据清理
每次测试运行后，H2 内存数据库会自动清理，确保测试之间互不影响。

---

## 树形结构说明

Wiki 文档采用树形结构存储：
- **根节点**: parentId 为 null 的文档
- **子节点**: parentId 指向父文档 ID
- **深度**: 理论上无限制，实际受数据库递归查询限制
- **排序**: 同层级文档按 position 字段升序排列

### 树形结构示例
```
根文档 1
├── 子文档 1-1
│   ├── 孙文档 1-1-1
│   └── 孙文档 1-1-2
└── 子文档 1-2

根文档 2
└── 子文档 2-1
```

---

## 测试数据说明

### 100 条树形结构压力测试
测试场景 3.2 和 3.3 验证了树形存储的正确性：
- **场景 3.2**: 10 个根节点，每个根节点 9 个子节点 (共 100 条)
- **场景 3.3**: 5 个根节点 → 15 个二级节点 → 75 个三级节点 + 5 个独立文档 (共 100 条)

### 树形存储验证点
1. 父子关系正确建立
2. 树形结构查询返回正确的层级
3. 软删除不影响未删除的文档
4. 批量创建性能在可接受范围内

---

## 根据结构文档的入参测试

Wiki 模块的入参结构遵循以下规范：

### CreateRequest (创建请求)
```json
{
  "parentId": 1,        // 可选，父文档 ID
  "title": "文档标题",   // 必填，1-200 字符
  "content": "内容"     // 可选，TEXT 类型
}
```

### UpdateRequest (更新请求)
```json
{
  "title": "新标题",    // 可选，1-200 字符
  "content": "新内容"   // 可选，TEXT 类型
}
```

### WikiVO (返回对象)
```json
{
  "id": 1,
  "projectId": 1,
  "parentId": null,
  "title": "文档标题",
  "content": "内容",
  "position": 0,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00",
  "children": []  // 子文档列表，仅在树形查询时返回
}
```

---

## 注意事项

1. **权限校验**: 测试中使用了 OWNER 角色，确保有所有权限
2. **软删除**: 文档删除使用软删除，deleted_at 字段标记
3. **历史记录**: 每次更新会自动保存历史版本到 wiki_history 表
4. **事务回滚**: 每个测试方法在事务中运行，测试后自动回滚
