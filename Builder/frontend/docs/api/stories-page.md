# 用户故事页面逻辑说明

## 页面路径
`/stories` - 用户故事管理页面

---

## 核心接口

### 1. 主查询接口（唯一数据源）

**接口**: `POST /api/v1/stories/search`

**用途**: 查询用户故事列表，支持多维度筛选和分页

**请求参数**:
```typescript
{
  // Query 参数
  page?: number;      // 页码，默认 1
  size?: number;      // 每页数量，默认 20

  // Body 参数
  projectIds?: number[];    // 项目 ID 列表（可选，支持多选）
  status?: string;          // 状态（可选）：TODO | IN_PROGRESS | TESTING | DONE
  priority?: string;        // 优先级（可选）：LOW | MEDIUM | HIGH | URGENT
  assigneeId?: number;      // 负责人 ID（可选）
  keyword?: string;         // 关键词搜索（标题、描述）
  epicId?: number;          // 史诗 ID（可选）
}
```

**响应数据**:
```typescript
{
  code: number;
  message: string;
  data: {
    list: UserStory[];
    total: number;
    page: number;
    size: number;
    pages: number;
  };
}
```

**权限控制**: 自动根据当前用户权限过滤可访问的项目

---

### 2. 辅助接口

#### 2.1 获取用户有权限的项目列表

**接口**: `GET /api/v1/projects/authorized`

**用途**: 获取当前用户有权限访问的所有项目，用于项目筛选下拉框

**响应数据**:
```typescript
{
  code: number;
  message: string;
  data: {
    list: Project[];
    total: number;
    page: number;
    size: number;
    pages: number;
  };
}
```

---

#### 2.2 获取项目成员列表

**接口**: `GET /api/v1/projects/{id}/members`

**用途**:
- 页面筛选：当用户选择项目后，加载该项目的成员列表用于"责任人"筛选
- 表单填写：创建/编辑用户故事时，选择项目后加载成员列表用于"负责人"选择

**响应数据**:
```typescript
{
  code: number;
  message: string;
  data: ProjectMemberResponse[];
}
```

**ProjectMemberResponse 结构**:
```typescript
{
  id: number;
  user: {
    id: number;
    username: string;
    nickname?: string;
    email: string;
    avatar?: string;
  };
  role: 'OWNER' | 'MEMBER' | 'VIEWER';
  joinedAt: string;
}
```

---

## 页面功能模块

### 1. 筛选区域

**筛选条件**:
| 条件 | 类型 | 数据源 | 说明 |
|------|------|--------|------|
| 关键词搜索 | Input | 用户输入 | 搜索标题、描述 |
| 项目选择 | Select (多选) | `/api/v1/projects/authorized` | 默认全部，可多选 |
| 责任人 | Select (单选) | 动态加载（见下方流程） | 需先选择项目 |
| 状态 | Select (单选) | 固定选项 | 待办/进行中/测试中/已完成 |

**责任人筛选数据加载流程**:
```
1. 用户选择项目（可多选）
   ↓
2. 并行调用 GET /api/v1/projects/{id}/members
   ↓
3. 合并所有选中项目的成员（去重）
   ↓
4. 渲染到责任人下拉框
   格式：{nickname/username} ({email})
```

---

### 2. 故事列表展示

**展示形式**: 卡片网格布局（响应式）
- 1 列（小屏）/ 2 列（中屏）/ 3 列（大屏）/ 4 列（超大屏）

**卡片内容**:
- 项目图标 + 项目名称
- 状态标签（带颜色）
- 优先级标签（带颜色）
- 标题（最多 2 行）
- 描述（最多 2 行）
- 故事点（可选）
- 负责人头像
- 操作按钮：查看 / 编辑 / 删除

**分页**: 支持翻页，每页 10 条

---

### 3. 新建/编辑用户故事

**表单字段**:
| 字段 | 必填 | 说明 |
|------|------|------|
| 所属项目 | ✅ 创建时 | 编辑时不可修改 |
| 故事标题 | ✅ | |
| 故事描述 | ❌ | 文本域 |
| 状态 | ❌ | 仅编辑时显示 |
| 优先级 | ❌ | 默认 MEDIUM |
| 故事点 | ❌ | 斐波那契数列：1,2,3,5,8,13,21 |
| 负责人 | ❌ | 依赖项目成员 |
| 验收标准 | ❌ | 文本域 |

**负责人选择数据加载流程**:
```
1. 用户选择"所属项目"
   ↓
2. 检查本地缓存是否有该项目成员
   ↓
3. 无缓存则调用 GET /api/v1/projects/{id}/members
   ↓
4. 渲染到负责人下拉框
   格式：{nickname/username} ({email})
```

---

## 状态管理

### 核心 State

```typescript
// 用户故事列表
const [stories, setStories] = useState<UserStory[]>([]);

// 项目列表（用于筛选和表单）
const [projects, setProjects] = useState<Project[]>([]);

// 所有选中项目的成员缓存（Map 结构，projectId -> members）
const [allProjectMembers, setAllProjectMembers] = useState<Map<number, ProjectMemberResponse[]>>(new Map());

// 当前表单中项目对应的成员列表
const [currentProjectMembers, setCurrentProjectMembers] = useState<ProjectMemberResponse[]>([]);

// 筛选条件
const [selectedProjectIds, setSelectedProjectIds] = useState<number[]>([]);  // 多选
const [selectedStatus, setSelectedStatus] = useState<StoryStatus | undefined>(undefined);
const [selectedAssigneeId, setSelectedAssigneeId] = useState<number | undefined>(undefined);
const [searchText, setSearchText] = useState('');

// 分页
const [page, setPage] = useState(1);
const [total, setTotal] = useState(0);
```

---

## 数据流图

```
┌─────────────────────────────────────────────────────────────────┐
│                        页面初始化                                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
              ┌───────────────────────────────┐
              │ GET /api/v1/projects/authorized │
              │ 获取用户有权限的项目            │
              └───────────────────────────────┘
                              │
                              ▼
              ┌───────────────────────────────┐
              │ POST /api/v1/stories/search   │
              │ 获取用户故事列表（默认全部）    │
              └───────────────────────────────┘
                              │
                              ▼
                        渲染页面完成


┌─────────────────────────────────────────────────────────────────┐
│                        用户选择项目筛选                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
              ┌───────────────────────────────┐
              │ 并行调用多个                   │
              │ GET /api/v1/projects/{id}/members │
              │ （每个选中项目调用一次）          │
              └───────────────────────────────┘
                              │
                              ▼
              ┌───────────────────────────────┐
              │ 合并成员列表（去重）            │
              │ 更新到 allProjectMembers       │
              └───────────────────────────────┘
                              │
                              ▼
              ┌───────────────────────────────┐
              │ 责任人筛选下拉框可用            │
              │ 显示所有选中项目的成员          │
              └───────────────────────────────┘


┌─────────────────────────────────────────────────────────────────┐
│                        点击"新建故事"                            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
              ┌───────────────────────────────┐
              │ 如果已选择单个项目             │
              │ → 自动选中该项目               │
              │ → 加载成员到 currentProjectMembers │
              └───────────────────────────────┘
                              │
                              ▼
              ┌───────────────────────────────┐
              │ 用户在表单中选择项目           │
              │ → onChange 触发                │
              │ → 调用 GET /api/v1/projects/{id}/members │
              │ → 更新 currentProjectMembers   │
              └───────────────────────────────┘
                              │
                              ▼
              ┌───────────────────────────────┐
              │ 负责人下拉框动态更新            │
              │ 显示当前项目的成员             │
              └───────────────────────────────┘
```

---

## 接口调用时序

### 场景 1: 页面初始加载
```
用户访问 /stories
    │
    ├─→ GET /api/v1/projects/authorized  → 设置 projects
    │
    └─→ POST /api/v1/stories/search      → 设置 stories
                                          (无筛选条件，返回全部)
```

### 场景 2: 用户选择项目筛选
```
用户选择项目 [A, B]
    │
    ├─→ 设置 selectedProjectIds = [A, B]
    │
    ├─→ 清空 selectedAssigneeId (避免无效筛选)
    │
    ├─→ 并行调用:
    │   ├─→ GET /api/v1/projects/A/members → 缓存到 Map
    │   └─→ GET /api/v1/projects/B/members → 缓存到 Map
    │
    └─→ POST /api/v1/stories/search (projectIds=[A,B]) → 更新 stories
```

### 场景 3: 用户选择责任人筛选
```
用户从下拉框选择责任人
    │
    ├─→ 设置 selectedAssigneeId
    │
    └─→ POST /api/v1/stories/search (assigneeId=xxx) → 更新 stories
```

### 场景 4: 创建用户故事 - 选择项目
```
用户点击"新建故事"
    │
    ├─→ 打开表单弹窗
    │
    └─→ 用户选择"所属项目"
        │
        ├─→ 检查 allProjectMembers 是否有缓存
        │   ├─→ 有 → 直接设置 currentProjectMembers
        │   │
        │   └─→ 无 → 调用 GET /api/v1/projects/{id}/members
        │            │
        │            ├─→ 缓存到 allProjectMembers
        │            │
        │            └─→ 设置 currentProjectMembers
        │
        └─→ 负责人下拉框更新（显示当前项目成员）
```

---

## 数据缓存策略

使用 `Map<number, ProjectMemberResponse[]>` 缓存所有已加载项目的成员列表：

```typescript
const [allProjectMembers, setAllProjectMembers] = useState<Map<number, ProjectMemberResponse[]>>(new Map());

// 缓存示例
// Map {
//   1 → [MemberA, MemberB],  // 项目 1 的成员
//   2 → [MemberC, MemberD],  // 项目 2 的成员
//   ...
// }
```

**优势**:
- 避免重复调用同一项目的成员接口
- 支持多项目成员的快速合并
- 表单和筛选器共享同一份缓存

---

## 注意事项

1. **责任人筛选依赖项目选择**: 用户必须先选择一个或多个项目，才能使用责任人筛选功能

2. **负责人选择依赖项目**: 创建用户故事时，必须先选择"所属项目"，"负责人"下拉框才会显示对应项目的成员

3. **昵称优先显示**: 成员名称优先使用 `nickname`，如果没有则使用 `username`

4. **权限控制**: `/api/v1/stories/search` 接口会自动根据用户权限过滤项目，用户只能看到有权限的项目下的用户故事

5. **表单字段对齐**: 后端枚举值使用大写（如 `TODO`, `MEDIUM`），前端表单使用小写，提交时需转换
