'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Card, Tabs, TabsProps, Avatar, Tag, Button, Empty, Dropdown, MenuProps, message, Modal, Form, Input, DatePicker, Select, ColorPicker, Drawer, Spin, Pagination, Table, TableColumnsType, Tree, TreeProps } from 'antd';
import {
  DeleteOutlined,
  EditOutlined,
  EyeOutlined,
  PlusOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  ProjectOutlined,
  CloseOutlined,
  UserAddOutlined,
  BarChartOutlined,
  FileTextOutlined,
  BugOutlined,
  BookOutlined,
  TeamOutlined,
  CalendarOutlined,
  UserOutlined,
  MessageOutlined,
  CodeOutlined,
  CheckSquareOutlined,
  FolderOutlined,
  FileOutlined,
  RightOutlined,
  DownOutlined,
  FilterOutlined,
  ClearOutlined,
} from '@ant-design/icons';
import Link from 'next/link';
import dayjs from 'dayjs';
import { getProject, deleteProject, updateProject, getProjectMembers } from '@/lib/api/project';
import { getTasks, getTask, updateTask, deleteTask, createTask, CreateTaskDto } from '@/lib/api/task';
import { getStories, getStory, updateStory, deleteStory, createStory, CreateUserStoryDto } from '@/lib/api/story';
import { getIssues, getIssue, updateIssue, deleteIssue, createIssue, CreateIssueDto } from '@/lib/api/issue';
import { getWikiTree, createWiki, CreateWikiDto } from '@/lib/api/wiki';
import { getBurndown } from '@/lib/api/report';
import { getEpics, Epic } from '@/lib/api/epic';
import type { Project, ProjectStatus, ProjectMemberResponse } from '@/lib/api/project';
import type { Task } from '@/lib/api/task';
import type { UserStory } from '@/lib/api/story';
import type { PageInfo } from '@/types/api';
import type { Issue } from '@/lib/api/issue';
import type { Wiki, WikiTreeNode } from '@/lib/api/wiki';
import type { BurndownData } from '@/lib/api/report';

const { Option } = Select;
const { TextArea } = Input;

const statusColorMap: Record<string, string> = {
  ACTIVE: 'processing',
  COMPLETED: 'success',
  ARCHIVED: 'default',
  PLANNING: 'default',
};

const statusTextMap: Record<string, string> = {
  ACTIVE: '进行中',
  COMPLETED: '已完成',
  ARCHIVED: '已归档',
  PLANNING: '规划中',
};

const roleColorMap: Record<string, string> = {
  admin: 'purple',
  manager: 'blue',
  member: 'gray',
};

const roleTextMap: Record<string, string> = {
  admin: '管理员',
  manager: '项目经理',
  member: '成员',
};

const projectIcons = ['🛒', '📱', '📊', '🤝', '🌐', '🔧', '💼', '🎯', '🚀', '💡'];

interface EditProjectFormValues {
  name: string;
  description: string;
  status: ProjectStatus;
  startDate?: dayjs.Dayjs;
  endDate?: dayjs.Dayjs;
  color?: string;
  icon?: string;
}

// 看板任务卡片组件
const TaskCard = ({ task }: { task: { id: string; title: string; priority: string; assignee?: string; comments: number } }) => {
  const priorityClass = `priority-${task.priority}`;
  const assigneeGradient = task.assignee
    ? `var(--accent-purple) 0%, var(--accent-pink) 100%`
    : `var(--accent-blue) 0%, var(--accent-cyan) 100%`;

  return (
    <div className="task-card">
      <div className="task-header">
        <span className="task-id">{task.id}</span>
        <span className={`task-priority ${priorityClass}`}>{task.priority}</span>
      </div>
      <div className="task-title">{task.title}</div>
      <div className="task-footer">
        {task.assignee && (
          <div className="task-assignee" style={{ background: `linear-gradient(135deg, ${assigneeGradient})` }}>
            {task.assignee[0]}
          </div>
        )}
        <div className="task-comments">
          <MessageOutlined />
          <span>{task.comments}</span>
        </div>
      </div>
    </div>
  );
};

// 问题列表项组件
const IssueItem = ({ issue }: { issue: { id: string; title: string; type: string; severity: string; status: string; assignee?: string } }) => (
  <div className="issue-item">
    <div className={`issue-type-icon ${issue.type}`}>
      {issue.type === 'BUG' ? <BugOutlined /> : issue.type === 'FEATURE' ? <CheckSquareOutlined /> : <CodeOutlined />}
    </div>
    <div className="issue-content">
      <div className="issue-header">
        <span className="issue-id">{issue.id}</span>
        <span className="issue-title">{issue.title}</span>
      </div>
      <div className="issue-meta">
        <span className="issue-meta-item">
          <UserOutlined />
          {issue.assignee || '未分配'}
        </span>
        <span className="issue-meta-item">
          <ClockCircleOutlined />
          2天前
        </span>
      </div>
    </div>
    <div className="issue-actions">
      <span className={`issue-severity severity-${issue.severity}`}>{issue.severity}</span>
      <span className="status-dot" style={{ backgroundColor: issue.status === 'open' ? 'var(--danger)' : 'var(--success)' }} />
    </div>
  </div>
);

export default function ProjectDetailPage() {
  const params = useParams();
  const router = useRouter();
  const projectId = params.id as string;

  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(true);
  const [project, setProject] = useState<Project | null>(null);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [activeTab, setActiveTab] = useState<string>('tasks');
  // 用户故事分页相关状态
  const [stories, setStories] = useState<UserStory[]>([]);
  const [storiesTotal, setStoriesTotal] = useState(0);
  const [storiesPage, setStoriesPage] = useState(1);
  const [storiesPageSize] = useState(10);
  const [storiesLoading, setStoriesLoading] = useState(false);
  const [issues, setIssues] = useState<Issue[]>([]);
  const [wikiTreeData, setWikiTreeData] = useState<WikiTreeNode[]>([]);
  const [wikiLoading, setWikiLoading] = useState(false);
  const [burndownData, setBurndownData] = useState<BurndownData[]>([]);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [editDrawerOpen, setEditDrawerOpen] = useState(false);
  const [selectedIcon, setSelectedIcon] = useState<string>('🛒');
  const [form] = Form.useForm();

  // 详情和编辑相关状态
  const [storyDetailOpen, setStoryDetailOpen] = useState(false);
  const [storyDetailLoading, setStoryDetailLoading] = useState(false);
  const [selectedStory, setSelectedStory] = useState<UserStory | null>(null);
  const [storyEditOpen, setStoryEditOpen] = useState(false);
  const [storyEditLoading, setStoryEditLoading] = useState(false);
  const [storyForm] = Form.useForm();

  const [taskDetailOpen, setTaskDetailOpen] = useState(false);
  const [taskDetailLoading, setTaskDetailLoading] = useState(false);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [taskEditDrawerOpen, setTaskEditDrawerOpen] = useState(false);
  const [taskEditLoading, setTaskEditLoading] = useState(false);
  const [taskForm] = Form.useForm();

  const [issueDetailOpen, setIssueDetailOpen] = useState(false);
  const [issueDetailLoading, setIssueDetailLoading] = useState(false);
  const [selectedIssue, setSelectedIssue] = useState<Issue | null>(null);
  const [issueEditDrawerOpen, setIssueEditDrawerOpen] = useState(false);
  const [issueEditLoading, setIssueEditLoading] = useState(false);
  const [issueForm] = Form.useForm();

  // 创建相关状态
  const [storyCreateOpen, setStoryCreateOpen] = useState(false);
  const [storyCreateLoading, setStoryCreateLoading] = useState(false);
  const [storyCreateForm] = Form.useForm();

  const [taskCreateOpen, setTaskCreateOpen] = useState(false);
  const [taskCreateLoading, setTaskCreateLoading] = useState(false);
  const [taskCreateForm] = Form.useForm();

  const [issueCreateOpen, setIssueCreateOpen] = useState(false);
  const [issueCreateLoading, setIssueCreateLoading] = useState(false);
  const [issueCreateForm] = Form.useForm();

  const [wikiCreateOpen, setWikiCreateOpen] = useState(false);
  const [wikiCreateLoading, setWikiCreateLoading] = useState(false);
  const [wikiCreateForm] = Form.useForm();

  // 项目成员和用户故事列表（用于创建弹框的下拉选项）
  const [projectMembers, setProjectMembers] = useState<ProjectMemberResponse[]>([]);
  const [storiesList, setStoriesList] = useState<UserStory[]>([]);
  const [membersLoading, setMembersLoading] = useState(false);

  // 用户故事关联的任务和展开状态
  const [storyTasks, setStoryTasks] = useState<Map<number, Task[]>>(new Map());
  const [expandedRowKeys, setExpandedRowKeys] = useState<React.Key[]>([]);

  // 服务（Epic）筛选相关状态
  const [epics, setEpics] = useState<Epic[]>([]);
  const [selectedEpicId, setSelectedEpicId] = useState<number | undefined>(undefined);
  const [epicsLoading, setEpicsLoading] = useState(false);

  // 获取项目详情
  const fetchProject = async () => {
    setFetchLoading(true);
    try {
      const projectData = await getProject(Number(projectId));
      setProject(projectData);
      setSelectedIcon(projectData.icon || '🛒');

      // 获取服务（Epic）列表用于筛选
      await fetchEpics(Number(projectId));

      // 获取任务列表
      const tasksResult = await getTasks(Number(projectId));
      setTasks(tasksResult.list || []);

      // 获取项目成员列表（用于创建弹框的负责人选择）
      await fetchProjectMembers(Number(projectId));

      // 获取用户故事列表（分页）
      await fetchStories(Number(projectId), 1);

      // 获取问题列表
      const issuesResult = await getIssues(Number(projectId));
      setIssues(issuesResult || []);

      // 获取 Wiki 文档树
      await fetchWikiTree(Number(projectId));

      // 获取燃尽图数据
      const burndownResult = await getBurndown(Number(projectId));
      setBurndownData(burndownResult.data?.data || []);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '获取项目详情失败';
      message.error(errorMessage);
    } finally {
      setFetchLoading(false);
    }
  };

  useEffect(() => {
    if (projectId) {
      fetchProject();
    }
  }, [projectId]);

  // 获取服务（Epic）列表
  const fetchEpics = async (projectIdNum: number) => {
    setEpicsLoading(true);
    try {
      const data = await getEpics(projectIdNum);
      setEpics(data);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '获取服务列表失败';
      message.error(errorMessage);
    } finally {
      setEpicsLoading(false);
    }
  };

  // 获取 Wiki 树
  const fetchWikiTree = async (projectIdNum: number) => {
    setWikiLoading(true);
    try {
      const wikisResult = await getWikiTree(projectIdNum);
      const treeData = buildWikiTree(wikisResult || []);
      setWikiTreeData(treeData);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '获取 Wiki 文档失败';
      message.error(errorMessage);
    } finally {
      setWikiLoading(false);
    }
  };

  // 构建 Wiki 树形数据
  const buildWikiTree = (wikis: Wiki[]): WikiTreeNode[] => {
    const tree: WikiTreeNode[] = [];
    const wikiMap = new Map<number, WikiTreeNode>();

    // 初始化所有节点
    wikis.forEach((wiki) => {
      wikiMap.set(wiki.id, {
        key: wiki.id,
        title: (
          <div className="flex items-center gap-2">
            <FolderOutlined style={{ color: '#faad14' }} />
            <span className="text-white">{wiki.title}</span>
            {!wiki.isPublished && (
              <Tag color="warning" className="text-xs">未发布</Tag>
            )}
          </div>
        ),
        isLeaf: false, // 先假设不是叶子节点
        children: [],
        data: wiki,
      });
    });

    // 构建树形结构
    wikis.forEach((wiki) => {
      const node = wikiMap.get(wiki.id);
      if (node && wiki.parentDocId) {
        const parent = wikiMap.get(wiki.parentDocId);
        if (parent) {
          parent.children?.push(node);
        }
      }
    });

    // 收集根节点（没有父节点的节点）
    wikis.forEach((wiki) => {
      if (!wiki.parentDocId) {
        const node = wikiMap.get(wiki.id);
        if (node) {
          tree.push(node);
        }
      }
    });

    return tree;
  };

  // 获取用户故事（分页）
  const fetchStories = async (projectIdNum: number, page: number) => {
    setStoriesLoading(true);
    try {
      const storiesResult = await getStories(projectIdNum, {
        page,
        size: storiesPageSize,  // 后端使用 size 不是 pageSize
      });
      setStories(storiesResult?.items || []);
      setStoriesTotal(storiesResult?.total || 0);
      // 同时更新 storiesList 用于创建弹框的下拉选项
      setStoriesList(storiesResult?.items || []);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '获取用户故事失败';
      message.error(errorMessage);
    } finally {
      setStoriesLoading(false);
    }
  };

  // 获取用户故事关联的任务
  const fetchStoryTasks = async (projectIdNum: number, storyId: number) => {
    // 如果已经加载过该故事的任务，则不再重复加载
    if (storyTasks.has(storyId)) {
      return;
    }

    try {
      const tasksResult = await getTasks(projectIdNum);
      const storyTasksList = tasksResult.list.filter((task) => task.userStoryId === storyId);
      setStoryTasks((prev) => new Map(prev).set(storyId, storyTasksList));
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '获取关联任务失败';
      message.error(errorMessage);
    }
  };

  // 处理展开/折叠
  const handleStoryExpand = (expanded: boolean, record: UserStory) => {
    const projectIdNum = Number(projectId);
    if (expanded && projectIdNum) {
      fetchStoryTasks(projectIdNum, record.id);
    }
    setExpandedRowKeys(expanded ? [...expandedRowKeys, record.id] : expandedRowKeys.filter((key) => key !== record.id));
  };

  // 获取项目成员列表
  const fetchProjectMembers = async (projectIdNum: number) => {
    setMembersLoading(true);
    try {
      const members = await getProjectMembers(projectIdNum);
      setProjectMembers(members || []);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '获取项目成员失败';
      message.error(errorMessage);
    } finally {
      setMembersLoading(false);
    }
  };

  // 页码切换
  const handleStoriesPageChange = (page: number) => {
    setStoriesPage(page);
    fetchStories(Number(projectId), page);
  };

  // 清除服务筛选
  const clearEpicFilter = () => {
    setSelectedEpicId(undefined);
  };

  // 按服务筛选任务
  const filteredTasks = selectedEpicId
    ? tasks.filter((task) => task.epicId === selectedEpicId)
    : tasks;

  // 按服务筛选用户故事（如果后端支持故事与 Epic 关联）
  const filteredStories = selectedEpicId
    ? stories.filter((story) => (story as any).epicId === selectedEpicId)
    : stories;

  // 按服务筛选问题（如果后端支持问题与 Epic 关联）
  const filteredIssues = selectedEpicId
    ? issues.filter((issue) => (issue as any).epicId === selectedEpicId)
    : issues;

  // 计算进度百分比
  const calculateProgress = () => {
    if (!project) return 0;
    if (project.taskCount === 0) return 0;
    return Math.round((project.completedTaskCount / project.taskCount) * 100);
  };

  // 格式化日期显示
  const formatDate = (dateStr: string) => {
    return dayjs(dateStr).format('YYYY-MM-DD');
  };

  // 格式化故事状态
  const getStoryStatusText = (status: string) => {
    const map: Record<string, string> = {
      TODO: '待办',
      IN_PROGRESS: '进行中',
      IN_REVIEW: '测试中',
      DONE: '已完成',
    };
    return map[status] || status;
  };

  // 获取故事状态颜色
  const getStoryStatusColor = (status: string) => {
    const map: Record<string, string> = {
      TODO: 'default',
      IN_PROGRESS: 'processing',
      IN_REVIEW: 'warning',
      DONE: 'success',
    };
    return map[status] || 'default';
  };

  // 格式化任务状态
  const getTaskStatusText = (status: string) => {
    const map: Record<string, string> = {
      TODO: '待办',
      IN_PROGRESS: '进行中',
      IN_REVIEW: '测试中',
      DONE: '已完成',
    };
    return map[status] || status;
  };

  // 获取任务状态颜色
  const getTaskStatusColor = (status: string) => {
    const map: Record<string, string> = {
      TODO: 'default',
      IN_PROGRESS: 'processing',
      IN_REVIEW: 'warning',
      DONE: 'success',
    };
    return map[status] || 'default';
  };

  // 获取问题类型文本
  const getIssueTypeText = (type: string) => {
    const map: Record<string, string> = {
      BUG: '缺陷',
      FEATURE: '功能',
      IMPROVEMENT: '改进',
      TASK: '任务',
    };
    return map[type] || type;
  };

  // 获取问题严重程度颜色
  const getIssueSeverityColor = (severity: string) => {
    const map: Record<string, string> = {
      CRITICAL: 'red',
      HIGH: 'orange',
      NORMAL: 'blue',
      LOW: 'gray',
    };
    return map[severity] || 'gray';
  };

  // 获取问题状态文本
  const getIssueStatusText = (status: string) => {
    const map: Record<string, string> = {
      TODO: '打开',
      IN_PROGRESS: '进行中',
      IN_REVIEW: '测试中',
      DONE: '已关闭',
    };
    return map[status] || status;
  };


  // 项目操作菜单
  const projectMenuItems: MenuProps['items'] = [
    {
      key: 'edit',
      icon: <EditOutlined />,
      label: '编辑项目',
      onClick: () => {
        if (!project) return;
        setEditDrawerOpen(true);
        form.setFieldsValue({
          name: project.name,
          description: project.description,
          status: project.status,
          startDate: dayjs(project.startDate),
          endDate: dayjs(project.endDate),
          color: project.color,
          icon: project.icon,
        });
        setSelectedIcon(project.icon || '🛒');
      },
    },
    {
      key: 'delete',
      icon: <DeleteOutlined className="text-red-500" />,
      label: <span className="text-red-500">删除项目</span>,
      onClick: () => setDeleteModalOpen(true),
      danger: true,
    },
  ];

  // 处理编辑提交
  const handleEditSubmit = async (values: EditProjectFormValues) => {
    setLoading(true);
    try {
      await updateProject(Number(projectId), {
        name: values.name,
        description: values.description,
        status: values.status,
        startDate: values.startDate?.format('YYYY-MM-DD'),
        endDate: values.endDate?.format('YYYY-MM-DD'),
        color: values.color,
        icon: values.icon || selectedIcon,
      });

      // 重新获取项目详情
      await fetchProject();

      message.success('项目更新成功');
      setEditDrawerOpen(false);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '更新失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // 处理删除
  const handleDelete = async () => {
    setLoading(true);
    try {
      await deleteProject(Number(projectId));
      message.success('项目删除成功');
      router.push('/projects');
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '删除失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setLoading(false);
      setDeleteModalOpen(false);
    }
  };

  // 页签按钮文本映射
  const tabButtonTextMap: Record<string, string> = {
    stories: '创建故事',
    tasks: '创建任务',
    issues: '创建问题',
    wiki: '创建 Wiki',
  };

  // 创建按钮点击处理
  const handleCreateButtonClick = () => {
    switch (activeTab) {
      case 'stories':
        setStoryCreateOpen(true);
        break;
      case 'tasks':
        setTaskCreateOpen(true);
        break;
      case 'issues':
        setIssueCreateOpen(true);
        break;
      case 'wiki':
        setWikiCreateOpen(true);
        break;
      default:
        message.info('当前页签暂不支持创建功能');
    }
  };

  // 页签切换处理
  const handleTabChange = (key: string) => {
    setActiveTab(key);
  };

  // 处理查看详情
  const handleViewStory = async (story: UserStory) => {
    setStoryDetailLoading(true);
    try {
      const storyDetail = await getStory(story.id);
      setSelectedStory(storyDetail || story);
      setStoryDetailOpen(true);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '获取用户故事详情失败';
      message.error(errorMessage);
    } finally {
      setStoryDetailLoading(false);
    }
  };

  const handleViewTask = async (task: Task) => {
    setTaskDetailLoading(true);
    try {
      const taskDetail = await getTask(Number(projectId), task.id);
      setSelectedTask(taskDetail || task);
      setTaskDetailOpen(true);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '获取任务详情失败';
      message.error(errorMessage);
    } finally {
      setTaskDetailLoading(false);
    }
  };

  const handleViewIssue = async (issue: Issue) => {
    setIssueDetailLoading(true);
    try {
      const issueDetail = await getIssue(Number(projectId), issue.id);
      setSelectedIssue(issueDetail || issue);
      setIssueDetailOpen(true);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '获取问题详情失败';
      message.error(errorMessage);
    } finally {
      setIssueDetailLoading(false);
    }
  };

  // 处理编辑
  const handleEditStory = async (story: UserStory) => {
    setStoryEditLoading(true);
    try {
      const storyDetail = await getStory(story.id);
      if (storyDetail) {
        setSelectedStory(storyDetail);
        storyForm.setFieldsValue({
          title: storyDetail.title,
          description: storyDetail.description,
          acceptanceCriteria: storyDetail.acceptanceCriteria,
          status: storyDetail.status,
          priority: storyDetail.priority,
          assigneeId: storyDetail.assigneeId,
          storyPoints: storyDetail.storyPoints,
        });
        setStoryEditOpen(true);
      }
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '获取用户故事详情失败';
      message.error(errorMessage);
    } finally {
      setStoryEditLoading(false);
    }
  };

  const handleEditTask = async (task: Task) => {
    setTaskEditLoading(true);
    try {
      const taskDetail = await getTask(Number(projectId), task.id);
      if (taskDetail) {
        setSelectedTask(taskDetail);
        taskForm.setFieldsValue({
          title: taskDetail.title,
          description: taskDetail.description,
          status: taskDetail.status,
          priority: taskDetail.priority,
          assigneeId: taskDetail.assigneeId,
          storyPoints: taskDetail.storyPoints,
          dueDate: taskDetail.dueDate ? dayjs(taskDetail.dueDate) : undefined,
          tags: taskDetail.tags?.join(', '),
          userStoryId: taskDetail.userStoryId,
        });
        setTaskEditDrawerOpen(true);
      }
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '获取任务详情失败';
      message.error(errorMessage);
    } finally {
      setTaskEditLoading(false);
    }
  };

  const handleEditIssue = async (issue: Issue) => {
    setIssueEditLoading(true);
    try {
      const issueDetail = await getIssue(Number(projectId), issue.id);
      if (issueDetail) {
        setSelectedIssue(issueDetail);
        issueForm.setFieldsValue({
          title: issueDetail.title,
          description: issueDetail.description,
          type: issueDetail.type,
          severity: issueDetail.severity,
          status: issueDetail.status,
          priority: issueDetail.priority,
          assigneeId: issueDetail.assigneeId,
          dueDate: issueDetail.dueDate ? dayjs(issueDetail.dueDate) : undefined,
          tags: issueDetail.tags?.join(', '),
        });
        setIssueEditDrawerOpen(true);
      }
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '获取问题详情失败';
      message.error(errorMessage);
    } finally {
      setIssueEditLoading(false);
    }
  };

  // 处理用户故事编辑提交
  const handleStoryEditSubmit = async (values: any) => {
    if (!selectedStory) return;

    setStoryEditLoading(true);
    try {
      await updateStory(selectedStory.id, {
        title: values.title,
        description: values.description,
        acceptanceCriteria: values.acceptanceCriteria,
        status: values.status,
        priority: values.priority,
        assigneeId: values.assigneeId,
        storyPoints: values.storyPoints,
      });

      message.success('用户故事更新成功');
      setStoryEditOpen(false);
      // 刷新用户故事列表
      await fetchStories(Number(projectId), storiesPage);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '更新失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setStoryEditLoading(false);
    }
  };

  // 处理任务编辑提交
  const handleTaskEditSubmit = async (values: any) => {
    if (!selectedTask) return;

    setTaskEditLoading(true);
    try {
      await updateTask(Number(projectId), selectedTask.id, {
        title: values.title,
        description: values.description,
        status: values.status,
        priority: values.priority,
        assigneeId: values.assigneeId,
        storyPoints: values.storyPoints,
        dueDate: values.dueDate ? dayjs(values.dueDate).format('YYYY-MM-DD') : undefined,
        tags: values.tags ? values.tags.split(',').map((t: string) => t.trim()) : [],
        userStoryId: values.userStoryId,
      });

      message.success('任务更新成功');
      setTaskEditDrawerOpen(false);
      // 刷新任务列表
      await fetchProject();
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '更新失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setTaskEditLoading(false);
    }
  };

  // 处理问题编辑提交
  const handleIssueEditSubmit = async (values: any) => {
    if (!selectedIssue) return;

    setIssueEditLoading(true);
    try {
      await updateIssue(Number(projectId), selectedIssue.id, {
        title: values.title,
        description: values.description,
        type: values.type,
        severity: values.severity,
        status: values.status,
        priority: values.priority,
        assigneeId: values.assigneeId,
        dueDate: values.dueDate ? dayjs(values.dueDate).format('YYYY-MM-DD') : undefined,
        tags: values.tags ? values.tags.split(',').map((t: string) => t.trim()) : [],
      });

      message.success('问题更新成功');
      setIssueEditDrawerOpen(false);
      // 刷新问题列表
      await fetchProject();
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '更新失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setIssueEditLoading(false);
    }
  };

  // 处理用户故事创建提交
  const handleStoryCreateSubmit = async (values: any) => {
    setStoryCreateLoading(true);
    try {
      await createStory(Number(projectId), {
        title: values.title,
        description: values.description,
        acceptanceCriteria: values.acceptanceCriteria,
        priority: values.priority,
        assigneeId: values.assigneeId,
        storyPoints: values.storyPoints,
        epicId: values.epicId,
      });

      message.success('用户故事创建成功');
      setStoryCreateOpen(false);
      storyCreateForm.resetFields();
      // 刷新用户故事列表
      await fetchStories(Number(projectId), storiesPage);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '创建失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setStoryCreateLoading(false);
    }
  };

  // 处理任务创建提交
  const handleTaskCreateSubmit = async (values: any) => {
    setTaskCreateLoading(true);
    try {
      await createTask(Number(projectId), {
        title: values.title,
        description: values.description,
        status: values.status,
        priority: values.priority,
        assigneeId: values.assigneeId,
        storyPoints: values.storyPoints,
        dueDate: values.dueDate ? dayjs(values.dueDate).format('YYYY-MM-DD') : undefined,
        tags: values.tags ? values.tags.split(',').map((t: string) => t.trim()) : [],
        userStoryId: values.userStoryId,
      });

      message.success('任务创建成功');
      setTaskCreateOpen(false);
      taskCreateForm.resetFields();
      // 刷新任务列表
      await fetchProject();
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '创建失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setTaskCreateLoading(false);
    }
  };

  // 处理问题创建提交
  const handleIssueCreateSubmit = async (values: any) => {
    setIssueCreateLoading(true);
    try {
      await createIssue(Number(projectId), {
        title: values.title,
        description: values.description,
        type: values.type,
        severity: values.severity,
        status: values.status,
        priority: values.priority,
        assigneeId: values.assigneeId,
        dueDate: values.dueDate ? dayjs(values.dueDate).format('YYYY-MM-DD') : undefined,
        tags: values.tags ? values.tags.split(',').map((t: string) => t.trim()) : [],
      });

      message.success('问题创建成功');
      setIssueCreateOpen(false);
      issueCreateForm.resetFields();
      // 刷新问题列表
      await fetchProject();
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '创建失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setIssueCreateLoading(false);
    }
  };

  // 处理 Wiki 创建提交
  const handleWikiCreateSubmit = async (values: any) => {
    setWikiCreateLoading(true);
    try {
      await createWiki(Number(projectId), {
        title: values.title,
        content: values.content || '',
        summary: values.summary,
        isPublished: values.isPublished ?? false,
        tags: values.tags ? values.tags.split(',').map((t: string) => t.trim()) : [],
      });

      message.success('Wiki 文档创建成功');
      setWikiCreateOpen(false);
      wikiCreateForm.resetFields();
      // 刷新 Wiki 树
      await fetchWikiTree(Number(projectId));
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '创建失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setWikiCreateLoading(false);
    }
  };

  // 页签内容
  const tabItems: TabsProps['items'] = [
    {
      key: 'stories',
      label: (
        <span className="flex items-center gap-2">
          <FileTextOutlined />
          用户故事
        </span>
      ),
      children: (
        <div className="space-y-4">
          {/* 进入任务看板按钮 */}
          {storiesLoading ? (
            <div className="text-center py-12">
              <Spin size="large" description="加载用户故事中..." />
            </div>
          ) : stories.length > 0 ? (
            <>
              <Table<UserStory>
                dataSource={filteredStories}
                rowKey="id"
                pagination={{
                  current: storiesPage,
                  total: storiesTotal,
                  pageSize: storiesPageSize,
                  onChange: handleStoriesPageChange,
                  showSizeChanger: false,
                  showTotal: (total) => `共 ${total} 条`,
                }}
                className="stories-table"
                expandable={{
                  expandedRowKeys,
                  onExpand: handleStoryExpand,
                  expandIcon: ({ expanded, onExpand, record }) => (
                    <Button
                      type="text"
                      size="small"
                      className="story-expand-btn"
                      onClick={(e) => {
                        e.stopPropagation();
                        onExpand(record, e);
                      }}
                    >
                      {expanded ? <DownOutlined /> : <RightOutlined />}
                    </Button>
                  ),
                  expandedRowRender: (record) => {
                    const tasks = storyTasks.get(record.id) || [];
                    return (
                      <div className="story-tasks-container">
                        {tasks.length === 0 ? (
                          <div className="text-center py-8 text-gray-500">
                            <Empty description="暂无关联任务" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                          </div>
                        ) : (
                          <div className="space-y-2">
                            {tasks.map((task) => (
                              <div key={task.id} className="task-sub-row">
                                <div className="task-sub-row-cell">
                                  <span className="task-title">{task.title}</span>
                                  {task.storyPoints && (
                                    <span className="pts-tag">{task.storyPoints} pts</span>
                                  )}
                                </div>
                                <div className="task-sub-row-cell">
                                  <span className="text-gray-400 text-sm" title={task.description}>
                                    {task.description?.length > 30 ? `${task.description.slice(0, 30)}...` : task.description || '-'}
                                  </span>
                                </div>
                                <div className="task-sub-row-cell">
                                  <Tag color={getTaskStatusColor(task.status)}>{getTaskStatusText(task.status)}</Tag>
                                </div>
                                <div className="task-sub-row-cell">
                                  <span className="text-gray-300">
                                    {task.assigneeName || <span className="text-gray-500">未分配</span>}
                                  </span>
                                </div>
                                <div className="task-sub-row-cell">
                                  <Tag color={task.priority === 'HIGH' ? 'red' : task.priority === 'MEDIUM' ? 'orange' : task.priority === 'URGENT' ? 'purple' : 'green'}>
                                    {task.priority === 'HIGH' ? '高' : task.priority === 'MEDIUM' ? '中' : task.priority === 'URGENT' ? '紧急' : '低'}
                                  </Tag>
                                </div>
                                <div className="task-sub-row-cell">
                                  <div className="flex items-center gap-2">
                                    <Button
                                      type="text"
                                      size="small"
                                      icon={<EyeOutlined />}
                                      onClick={(e) => {
                                        e.stopPropagation();
                                        handleViewTask(task);
                                      }}
                                      title="查看详情"
                                    />
                                    <Button
                                      type="text"
                                      size="small"
                                      icon={<EditOutlined />}
                                      onClick={(e) => {
                                        e.stopPropagation();
                                        handleEditTask(task);
                                      }}
                                      title="编辑"
                                    />
                                  </div>
                                </div>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    );
                  },
                }}
                columns={[
                  {
                    title: '故事标题',
                    dataIndex: 'title',
                    key: 'title',
                    ellipsis: true,
                    render: (title: string, record: UserStory) => (
                      <div className="font-medium text-white">
                        {title}
                        {record.storyPoints && (
                          <span className="ml-2 px-2 py-0.5 bg-orange-500/20 text-orange-400 text-xs rounded">
                            {record.storyPoints} pts
                          </span>
                        )}
                      </div>
                    ),
                  },
                  {
                    title: '描述',
                    dataIndex: 'description',
                    key: 'description',
                    ellipsis: true,
                    width: 300,
                    render: (description: string) => (
                      <span className="text-gray-400 text-sm" title={description}>
                        {description?.length > 50 ? `${description.slice(0, 50)}...` : description}
                      </span>
                    ),
                  },
                  {
                    title: '状态',
                    dataIndex: 'status',
                    key: 'status',
                    width: 100,
                    render: (status: string) => (
                      <Tag color={getStoryStatusColor(status)}>{getStoryStatusText(status)}</Tag>
                    ),
                  },
                  {
                    title: '负责人',
                    dataIndex: 'assigneeName',
                    key: 'assigneeName',
                    width: 100,
                    render: (assigneeName?: string) => (
                      <span className="text-gray-300">
                        {assigneeName || <span className="text-gray-500">未分配</span>}
                      </span>
                    ),
                  },
                  {
                    title: '优先级',
                    dataIndex: 'priority',
                    key: 'priority',
                    width: 100,
                    render: (priority: string) => {
                      const colorMap: Record<string, string> = {
                        LOW: 'green',
                        MEDIUM: 'orange',
                        HIGH: 'red',
                        URGENT: 'purple',
                      };
                      const textMap: Record<string, string> = {
                        LOW: '低',
                        MEDIUM: '中',
                        HIGH: '高',
                        URGENT: '紧急',
                      };
                      return (
                        <Tag color={colorMap[priority] || 'default'}>
                          {textMap[priority] || priority}
                        </Tag>
                      );
                    },
                  },
                  {
                    title: '操作',
                    key: 'action',
                    width: 100,
                    fixed: 'right',
                    render: (_: unknown, record: UserStory) => (
                      <div className="flex items-center gap-3">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            handleViewStory(record);
                          }}
                          className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110"
                          style={{ color: '#636e72' }}
                          title="查看"
                        >
                          👁️
                        </button>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            handleEditStory(record);
                          }}
                          className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110"
                          style={{ color: '#636e72' }}
                          title="编辑"
                        >
                          ✏️
                        </button>
                      </div>
                    ),
                  },
                ].filter(Boolean) as TableColumnsType<UserStory>}
              />
            </>
          ) : (
            <div className="text-center py-12">
              <FileTextOutlined style={{ fontSize: 64, color: '#6b7280' }} />
              <h3 className="text-gray-400 mt-4">暂无用户故事</h3>
              <p className="text-gray-500 mt-2">故事功能开发中</p>
            </div>
          )}
        </div>
      ),
    },
    {
      key: 'tasks',
      label: (
        <span className="flex items-center gap-2">
          <CheckSquareOutlined />
          任务列表
        </span>
      ),
      children: (
        <div className="space-y-4">
          <Table<Task>
            dataSource={filteredTasks}
            rowKey="id"
            pagination={false}
            className="tasks-table"
            columns={[
              {
                title: '任务标题',
                dataIndex: 'title',
                key: 'title',
                ellipsis: true,
                render: (title: string, record: Task) => (
                  <div className="font-medium text-white">
                    {title}
                    <span className={`ml-2 px-2 py-0.5 text-xs rounded ${
                      record.priority === 'HIGH' ? 'bg-red-500/20 text-red-400' :
                      record.priority === 'MEDIUM' ? 'bg-orange-500/20 text-orange-400' :
                      record.priority === 'URGENT' ? 'bg-purple-500/20 text-purple-400' :
                      'bg-gray-500/20 text-gray-400'
                    }`}>
                      {record.priority}
                    </span>
                  </div>
                ),
              },
              {
                title: '描述',
                dataIndex: 'description',
                key: 'description',
                ellipsis: true,
                width: 250,
                render: (description?: string) => (
                  <span className="text-gray-400 text-sm" title={description}>
                    {description && description.length > 50 ? `${description.slice(0, 50)}...` : description || '-'}
                  </span>
                ),
              },
              {
                title: '状态',
                dataIndex: 'status',
                key: 'status',
                width: 100,
                render: (status: string) => {
                  const statusConfig: Record<string, { color: string; text: string }> = {
                    TODO: { color: 'default', text: '待办' },
                    IN_PROGRESS: { color: 'processing', text: '进行中' },
                    IN_REVIEW: { color: 'warning', text: '测试中' },
                    DONE: { color: 'success', text: '已完成' },
                  };
                  const config = statusConfig[status] || { color: 'default', text: status };
                  return <Tag color={config.color}>{config.text}</Tag>;
                },
              },
              {
                title: '负责人',
                dataIndex: 'assigneeName',
                key: 'assigneeName',
                width: 100,
                render: (assigneeName?: string) => (
                  <span className="text-gray-300">
                    {assigneeName || <span className="text-gray-500">未分配</span>}
                  </span>
                ),
              },
              {
                title: '关联用户故事',
                dataIndex: 'userStoryTitle',
                key: 'userStoryTitle',
                width: 150,
                ellipsis: true,
                render: (_text: string | undefined, record: Task) => (
                  record.userStoryId ? (
                    <span className="text-gray-300" title={record.userStoryTitle || '无标题'}>
                      #{record.userStoryId} {record.userStoryTitle?.slice(0, 20) || '无标题'}
                    </span>
                  ) : (
                    <span className="text-gray-500">-</span>
                  )
                ),
              },
              {
                title: '操作',
                key: 'action',
                width: 100,
                fixed: 'right',
                render: (_: unknown, record: Task) => (
                  <div className="flex items-center gap-3">
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleViewTask(record);
                      }}
                      className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110"
                      style={{ color: '#636e72' }}
                      title="查看"
                    >
                      👁️
                    </button>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleEditTask(record);
                      }}
                      className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110"
                      style={{ color: '#636e72' }}
                      title="编辑"
                    >
                      ✏️
                    </button>
                  </div>
                ),
              },
            ].filter(Boolean) as TableColumnsType<Task>}
          />
        </div>
      ),
    },
    {
      key: 'issues',
      label: (
        <span className="flex items-center gap-2">
          <BugOutlined />
          问题追踪
        </span>
      ),
      children: (
        <div className="space-y-4">
          <Table<Issue>
            dataSource={filteredIssues}
            rowKey="id"
            pagination={false}
            className="issues-table"
            columns={[
              {
                title: '问题标题',
                dataIndex: 'title',
                key: 'title',
                ellipsis: true,
                render: (title: string, record: Issue) => (
                  <div className="flex items-center gap-2">
                    <span className="text-gray-400 text-sm">#{record.id}</span>
                    <span className="font-medium text-white">{title}</span>
                    <Tag color={getIssueSeverityColor(record.severity)}>
                      {getIssueTypeText(record.type)}
                    </Tag>
                  </div>
                ),
              },
              {
                title: '状态',
                dataIndex: 'status',
                key: 'status',
                width: 100,
                render: (status: string) => {
                  const statusConfig: Record<string, { color: string; text: string }> = {
                    TODO: { color: 'default', text: '待办' },
                    IN_PROGRESS: { color: 'warning', text: '进行中' },
                    IN_REVIEW: { color: 'success', text: '已解决' },
                    DONE: { color: 'default', text: '已关闭' },
                  };
                  const config = statusConfig[status] || { color: 'default', text: status };
                  return <Tag color={config.color}>{config.text}</Tag>;
                },
              },
              {
                title: '优先级',
                dataIndex: 'priority',
                key: 'priority',
                width: 90,
                render: (priority?: string) => (
                  <span className="text-gray-300">{priority || '-'}</span>
                ),
              },
              {
                title: '严重程度',
                dataIndex: 'severity',
                key: 'severity',
                width: 100,
                render: (severity: string) => {
                  const severityColor: Record<string, string> = {
                    CRITICAL: 'red',
                    HIGH: 'orange',
                    NORMAL: 'blue',
                    LOW: 'gray',
                  };
                  return <Tag color={severityColor[severity] || 'default'}>{severity}</Tag>;
                },
              },
              {
                title: '负责人',
                dataIndex: 'assigneeName',
                key: 'assigneeName',
                width: 100,
                render: (assigneeName?: string) => (
                  <span className="text-gray-300">
                    {assigneeName || <span className="text-gray-500">未分配</span>}
                  </span>
                ),
              },
              {
                title: '截止日期',
                dataIndex: 'dueDate',
                key: 'dueDate',
                width: 120,
                render: (dueDate?: string) => (
                  dueDate ? (
                    <span className="text-gray-300">{formatDate(dueDate)}</span>
                  ) : (
                    <span className="text-gray-500">-</span>
                  )
                ),
              },
              {
                title: '操作',
                key: 'action',
                width: 100,
                fixed: 'right',
                render: (_: unknown, record: Issue) => (
                  <div className="flex items-center gap-3">
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleViewIssue(record);
                      }}
                      className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110"
                      style={{ color: '#636e72' }}
                      title="查看"
                    >
                      👁️
                    </button>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleEditIssue(record);
                      }}
                      className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110"
                      style={{ color: '#636e72' }}
                      title="编辑"
                    >
                      ✏️
                    </button>
                  </div>
                ),
              },
            ].filter(Boolean) as TableColumnsType<Issue>}
          />
        </div>
      ),
    },
    {
      key: 'wiki',
      label: (
        <span className="flex items-center gap-2">
          <BookOutlined />
          Wiki
        </span>
      ),
      children: (
        <div className="space-y-4">
          {wikiLoading ? (
            <div className="text-center py-12">
              <Spin size="large" description="加载 Wiki 文档中..." />
            </div>
          ) : wikiTreeData.length > 0 ? (
            <Tree
              defaultExpandAll
              showLine={{ showLeafIcon: false }}
              blockNode
              treeData={wikiTreeData}
              className="wiki-tree"
            />
          ) : (
            <div className="text-center py-12">
              <BookOutlined style={{ fontSize: 64, color: '#6b7280' }} />
              <h3 className="text-gray-400 mt-4">暂无文档</h3>
              <p className="text-gray-500 mt-2">Wiki 功能开发中</p>
            </div>
          )}
        </div>
      ),
    },
    {
      key: 'reports',
      label: (
        <span className="flex items-center gap-2">
          <BarChartOutlined />
          数据报表
        </span>
      ),
      children: (
        <div className="space-y-6">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="bg-gray-800/50 border border-gray-700 rounded-lg p-4 text-center">
              <div className="text-2xl font-bold text-white">{project?.taskCount || 0}</div>
              <div className="text-gray-400 text-sm mt-1">总任务数</div>
            </div>
            <div className="bg-gray-800/50 border border-gray-700 rounded-lg p-4 text-center">
              <div className="text-2xl font-bold text-green-400">{project?.completedTaskCount || 0}</div>
              <div className="text-gray-400 text-sm mt-1">已完成</div>
            </div>
            <div className="bg-gray-800/50 border border-gray-700 rounded-lg p-4 text-center">
              <div className="text-2xl font-bold text-blue-400">{stories.length}</div>
              <div className="text-gray-400 text-sm mt-1">用户故事</div>
            </div>
            <div className="bg-gray-800/50 border border-gray-700 rounded-lg p-4 text-center">
              <div className="text-2xl font-bold text-orange-400">{issues.length}</div>
              <div className="text-gray-400 text-sm mt-1">问题</div>
            </div>
          </div>

          {burndownData.length > 0 ? (
            <div className="bg-gray-800/50 border border-gray-700 rounded-lg p-6">
              <h4 className="text-white font-medium mb-4">燃尽图数据</h4>
              <div className="h-48 flex items-end gap-2">
                {burndownData.slice(-7).map((item, idx) => (
                  <div key={idx} className="flex-1 flex flex-col items-center gap-2">
                    <div
                      className="w-full bg-gradient-to-t from-orange-500 to-orange-400 rounded-t transition-all"
                      style={{ height: `${Math.max(10, (item.remaining / Math.max(...burndownData.map(d => d.remaining))) * 100)}%` }}
                    />
                    <span className="text-xs text-gray-400">{item.date.slice(5)}</span>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="text-center py-12 bg-gray-800/30 rounded-lg">
              <BarChartOutlined style={{ fontSize: 64, color: '#6b7280' }} />
              <h3 className="text-gray-400 mt-4">暂无报表数据</h3>
              <p className="text-gray-500 mt-2">报表功能开发中</p>
            </div>
          )}
        </div>
      ),
    },
  ];

  // 加载状态
  if (fetchLoading) {
    return (
      <div className="flex justify-center items-center py-20">
        <Spin size="large" description="加载项目中..." />
      </div>
    );
  }

  // 项目不存在
  if (!project) {
    return (
      <Empty description="项目不存在或已被删除">
        <Link href="/projects">
          <Button type="primary">返回项目列表</Button>
        </Link>
      </Empty>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      {/* 项目信息卡片 - 使用HTML设计风格 */}
      <div className="project-header" style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'flex-start',
        marginBottom: '2rem',
        padding: '2rem',
        background: 'var(--surface)',
        border: '1px solid var(--border)',
        borderRadius: '16px',
      }}>
        <div className="project-info" style={{ display: 'flex', gap: '1.5rem' }}>
          <div
            className="project-icon-large"
            style={{
              width: '72px',
              height: '72px',
              borderRadius: '16px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              background: `linear-gradient(135deg, ${project.color} 0%, ${project.color}dd 100%)`,
              boxShadow: `0 12px 24px -4px ${project.color}40`,
              flexShrink: 0,
              fontSize: '2rem',
            }}
          >
            {project.icon}
          </div>
          <div className="project-details">
            <h1 style={{
              fontFamily: 'var(--font-display)',
              fontSize: '1.75rem',
              fontWeight: 700,
              marginBottom: '0.5rem',
              color: 'var(--text-light)',
            }}>
              {project.name}
            </h1>
            <p className="project-description" style={{
              color: 'var(--text-muted)',
              fontSize: '0.95rem',
              lineHeight: 1.6,
              marginBottom: '1rem',
              maxWidth: '600px',
            }}>
              {project.description}
            </p>
            <div className="project-meta-inline" style={{
              display: 'flex',
              gap: '1.5rem',
              flexWrap: 'wrap',
            }}>
              <div className="meta-badge" style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
                fontSize: '0.85rem',
                color: 'var(--text-muted)',
              }}>
                <Tag color={statusColorMap[project.status]}>
                  {statusTextMap[project.status]}
                </Tag>
              </div>
              <div className="meta-badge">
                <CalendarOutlined style={{ color: 'var(--accent)' }} />
                {formatDate(project.startDate)} 至 {formatDate(project.endDate)}
              </div>
            </div>
          </div>
        </div>
        <div className="project-actions" style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          {/* 服务筛选框 */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <FilterOutlined className="text-gray-400" />
            <span className="text-gray-300 text-sm font-medium">服务筛选：</span>
            <Select
              placeholder="全部服务"
              value={selectedEpicId}
              onChange={setSelectedEpicId}
              allowClear
              className="w-48"
              size="small"
              loading={epicsLoading}
              options={epics.map((epic) => ({
                value: epic.id,
                label: (
                  <div className="flex items-center gap-2">
                    <div
                      className="w-2 h-2 rounded-full"
                      style={{ backgroundColor: epic.color || '#1890ff' }}
                    />
                    <span>{epic.title}</span>
                  </div>
                ),
              }))}
            />
            {selectedEpicId && (
              <Button
                type="text"
                size="small"
                icon={<ClearOutlined />}
                onClick={clearEpicFilter}
                className="text-gray-400 hover:text-white"
              >
                清除
              </Button>
            )}
          </div>
          <Dropdown menu={{ items: projectMenuItems }} trigger={['click']}>
            <Button
              size="large"
              icon={<CloseOutlined />}
              style={{
                background: 'var(--surface)',
                border: '1px solid var(--border)',
                color: 'var(--text-muted)',
              }}
            >
              项目操作
            </Button>
          </Dropdown>
        </div>
      </div>

      {/* 统计卡片 */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(4, 1fr)',
        gap: '1rem',
        marginBottom: '2rem',
      }}>
        <div style={{
          background: 'var(--surface)',
          border: '1px solid var(--border)',
          borderRadius: '12px',
          padding: '1.5rem',
          textAlign: 'center',
        }}>
          <div style={{ fontSize: '2rem', fontWeight: 700, color: 'var(--accent)', marginBottom: '0.5rem' }}>
            {calculateProgress()}%
          </div>
          <div style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>项目进度</div>
        </div>
        <div style={{
          background: 'var(--surface)',
          border: '1px solid var(--border)',
          borderRadius: '12px',
          padding: '1.5rem',
          textAlign: 'center',
        }}>
          <div style={{ fontSize: '2rem', fontWeight: 700, color: 'var(--accent-blue)', marginBottom: '0.5rem' }}>
            {project.taskCount || 0}
          </div>
          <div style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>总任务数</div>
        </div>
        <div style={{
          background: 'var(--surface)',
          border: '1px solid var(--border)',
          borderRadius: '12px',
          padding: '1.5rem',
          textAlign: 'center',
        }}>
          <div style={{ fontSize: '2rem', fontWeight: 700, color: 'var(--success)', marginBottom: '0.5rem' }}>
            {project.completedTaskCount || 0}
          </div>
          <div style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>已完成</div>
        </div>
        <div style={{
          background: 'var(--surface)',
          border: '1px solid var(--border)',
          borderRadius: '12px',
          padding: '1.5rem',
          textAlign: 'center',
        }}>
          <div style={{ fontSize: '2rem', fontWeight: 700, color: 'var(--accent-purple)', marginBottom: '0.5rem' }}>
            {project.memberCount || 1}
          </div>
          <div style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>项目成员</div>
        </div>
      </div>

      {/* 成员展示 */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '1rem',
        padding: '1rem 1.5rem',
        background: 'var(--surface)',
        border: '1px solid var(--border)',
        borderRadius: '12px',
      }}>
        <span style={{ color: 'var(--text-muted)' }}>项目成员：</span>
        <div style={{ display: 'flex', gap: '-8px' }}>
          {Array.from({ length: Math.min(project.memberCount || 1, 4) }).map((_, i) => (
            <Avatar
              key={i}
              size={36}
              style={{
                background: `linear-gradient(135deg, var(--accent-purple) 0%, var(--accent-pink) 100%)`,
                border: '2px solid var(--primary)',
              }}
            >
              U{i + 1}
            </Avatar>
          ))}
          {(project.memberCount || 0) > 4 && (
            <Avatar
              size={36}
              style={{
                background: 'var(--surface-hover)',
                border: '2px solid var(--primary)',
              }}
              icon={<PlusOutlined />}
            />
          )}
        </div>
        <span style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
          {project.memberCount || 1} 名成员
        </span>
        <div style={{ marginLeft: 'auto' }}>
          <Link href={`/projects/${projectId}/members`}>
            <Button type="primary" icon={<UserAddOutlined />} size="small">
              进入成员管理
            </Button>
          </Link>
        </div>
      </div>

      {/* 页签导航 - 使用自定义样式 */}
      <div className="tabs-container">
        <Tabs
          activeKey={activeTab}
          onChange={handleTabChange}
          items={tabItems}
          className="project-detail-tabs"
          tabBarStyle={{
            borderBottomColor: 'var(--border)',
            background: 'var(--surface)',
            padding: '0 1rem',
          }}
          tabBarExtraContent={
            <Button
              type="primary"
              icon={<PlusOutlined />}
              size="small"
              className="whitespace-nowrap"
              onClick={handleCreateButtonClick}
            >
              {tabButtonTextMap[activeTab] || '创建'}
            </Button>
          }
        />
      </div>

      {/* 删除确认对话框 */}
      <Modal
        title={
          <div className="flex items-center gap-3">
            <DeleteOutlined className="text-red-500 text-xl" />
            <span>确认删除</span>
          </div>
        }
        open={deleteModalOpen}
        onOk={handleDelete}
        onCancel={() => setDeleteModalOpen(false)}
        okButtonProps={{ danger: true, loading }}
        okText="确认删除"
        cancelText="取消"
        className="glass-dark"
      >
        <p className="text-gray-300">
          确定要删除项目 <span className="text-white font-semibold">{project.name}</span> 吗？
        </p>
        <p className="text-red-400 mt-2">
          删除后所有相关数据（任务、文档等）都将被删除，此操作不可恢复。
        </p>
      </Modal>

      {/* 编辑项目抽屉 */}
      <Drawer
        title="编辑项目"
        placement="right"
        size="large"
        open={editDrawerOpen}
        onClose={() => setEditDrawerOpen(false)}
        className="glass-dark"
      >
        <Form
          form={form}
          layout="vertical"
          requiredMark={false}
          onFinish={handleEditSubmit}
          size="large"
        >
          <Form.Item
            name="name"
            label="项目名称"
            rules={[
              { required: true, message: '请输入项目名称' },
              { min: 2, message: '项目名称至少 2 个字符' },
              { max: 50, message: '项目名称不能超过 50 个字符' },
            ]}
          >
            <Input
              placeholder="请输入项目名称"
              className="bg-gray-700/50 border-gray-600 text-white"
              prefix={<ProjectOutlined className="text-gray-400" />}
            />
          </Form.Item>

          <Form.Item
            name="description"
            label="项目描述"
            rules={[{ required: true, message: '请输入项目描述' }]}
          >
            <TextArea
              rows={4}
              placeholder="描述项目目标、范围等..."
              className="bg-gray-700/50 border-gray-600 text-white"
              showCount
              maxLength={500}
            />
          </Form.Item>

          <Form.Item
            name="status"
            label="项目状态"
          >
            <Select className="bg-gray-700/50 border-gray-600">
              <Option value="ACTIVE">进行中</Option>
              <Option value="COMPLETED">已完成</Option>
              <Option value="ARCHIVED">已归档</Option>
            </Select>
          </Form.Item>

          <div className="grid grid-cols-2 gap-4">
            <Form.Item
              name="startDate"
              label="开始日期"
            >
              <DatePicker
                className="w-full bg-gray-700/50 border-gray-600 text-white"
                format="YYYY-MM-DD"
              />
            </Form.Item>

            <Form.Item
              name="endDate"
              label="结束日期"
            >
              <DatePicker
                className="w-full bg-gray-700/50 border-gray-600 text-white"
                format="YYYY-MM-DD"
              />
            </Form.Item>
          </div>

          <Form.Item
            name="icon"
            label="项目图标"
          >
            <div className="flex gap-2 flex-wrap">
              {projectIcons.map((icon) => (
                <button
                  key={icon}
                  type="button"
                  onClick={() => setSelectedIcon(icon)}
                  className={`w-12 h-12 text-2xl rounded-lg flex items-center justify-center ${
                    selectedIcon === icon
                      ? 'bg-orange-500 ring-2 ring-orange-400'
                      : 'bg-gray-700/50'
                  }`}
                >
                  {icon}
                </button>
              ))}
            </div>
          </Form.Item>

          <Form.Item
            name="color"
            label="项目颜色"
          >
            <ColorPicker format="hex" showText className="w-full" />
          </Form.Item>

          <Form.Item className="pt-4">
            <div className="flex gap-4">
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                className="flex-1 bg-gradient-to-r from-orange-500 to-orange-600 border-none"
              >
                保存修改
              </Button>
              <Button
                onClick={() => setEditDrawerOpen(false)}
                className="flex-1 border-gray-600 text-gray-300"
              >
                取消
              </Button>
            </div>
          </Form.Item>
        </Form>
      </Drawer>

      {/* 用户故事详情抽屉 */}
      <Drawer
        title="用户故事详情"
        placement="right"
        size="large"
        open={storyDetailOpen}
        onClose={() => setStoryDetailOpen(false)}
        className="glass-dark"
      >
        {storyDetailLoading ? (
          <div className="flex justify-center py-12">
            <Spin size="large" />
          </div>
        ) : selectedStory ? (
          <div className="space-y-4">
            <div>
              <h3 className="text-lg font-semibold text-white mb-2">{selectedStory.title}</h3>
              <div className="flex items-center gap-2 mb-3">
                <Tag color={selectedStory.storyPoints ? 'orange' : 'default'}>
                  {selectedStory.storyPoints || 0} pts
                </Tag>
                <Tag color="processing">
                  {selectedStory.status === 'TODO' && '待办'}
                  {selectedStory.status === 'IN_PROGRESS' && '进行中'}
                  {selectedStory.status === 'IN_REVIEW' && '审核中'}
                  {selectedStory.status === 'DONE' && '已完成'}
                </Tag>
              </div>
            </div>
            <div>
              <h4 className="text-sm font-medium text-gray-400 mb-2">描述</h4>
              <p className="text-gray-300 text-sm whitespace-pre-wrap">
                {selectedStory.description || '无描述'}
              </p>
            </div>
            {selectedStory.acceptanceCriteria && (
              <div>
                <h4 className="text-sm font-medium text-gray-400 mb-2">验收标准</h4>
                <p className="text-gray-300 text-sm whitespace-pre-wrap">
                  {selectedStory.acceptanceCriteria}
                </p>
              </div>
            )}
            <div className="pt-4 border-t border-gray-700">
              <div className="flex items-center justify-between text-sm text-gray-400">
                <span>负责人：{selectedStory.assigneeName || '未分配'}</span>
                <span>创建时间：{dayjs(selectedStory.createdAt).format('YYYY-MM-DD')}</span>
              </div>
            </div>
          </div>
        ) : (
          <Empty description="暂无详情" />
        )}
      </Drawer>

      {/* 用户故事编辑抽屉 - 左侧边栏式 */}
      <Drawer
        title="编辑用户故事"
        placement="left"
        open={storyEditOpen}
        onClose={() => setStoryEditOpen(false)}
        width={600}
        styles={{
          body: { padding: 0, background: '#161b22', color: '#f0f6fc' },
          header: {
            background: '#161b22',
            borderBottom: '1px solid rgba(255,255,255,0.05)',
            padding: '20px 24px',
          },
          footer: {
            background: '#161b22',
            borderTop: '1px solid rgba(255,255,255,0.05)',
          },
        }}
        footer={
          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
            <Button
              onClick={() => setStoryEditOpen(false)}
              style={{
                background: 'transparent',
                border: '1px solid #30363d',
                color: '#c9d1d9',
                borderRadius: '6px',
                padding: '8px 16px',
              }}
            >
              取消
            </Button>
            <Button
              type="primary"
              onClick={() => storyForm.submit()}
              loading={storyEditLoading}
              style={{
                background: '#ff8c42',
                border: 'none',
                borderRadius: '6px',
                padding: '8px 24px',
                fontWeight: 'bold',
              }}
            >
              保存修改
            </Button>
          </div>
        }
      >
        <div className="max-h-full overflow-y-auto pr-4" style={{ background: '#161b22', minHeight: '100%' }}>
          <Form
            form={storyForm}
            layout="vertical"
            onFinish={handleStoryEditSubmit}
            size="large"
          >
            <Form.Item
              name="title"
              label={<span style={{ color: '#8b949e', fontSize: '13px' }}>故事标题</span>}
              rules={[{ required: true, message: '请输入用户故事标题' }]}
            >
              <Input
                placeholder="请输入标题"
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  borderRadius: '6px',
                  padding: '10px',
                  color: 'white',
                }}
              />
            </Form.Item>

            <Form.Item
              name="description"
              label={<span style={{ color: '#8b949e', fontSize: '13px' }}>故事描述</span>}
            >
              <TextArea
                rows={10}
                placeholder="作为一名...用户，我想要...功能，以便我可以...价值"
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  borderRadius: '6px',
                  padding: '10px',
                  color: 'white',
                  minHeight: '200px',
                }}
              />
            </Form.Item>

            <Form.Item
              name="acceptanceCriteria"
              label={<span style={{ color: '#8b949e', fontSize: '13px' }}>验收标准</span>}
            >
              <TextArea
                rows={4}
                placeholder="输入验收标准，用逗号分隔多条标准..."
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  borderRadius: '6px',
                  padding: '10px',
                  color: 'white',
                }}
              />
            </Form.Item>

            <Form.Item
              name="epicId"
              label={<span style={{ color: '#8b949e', fontSize: '13px' }}>所属服务</span>}
            >
              <Select
                placeholder="请选择所属服务"
                allowClear
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  borderRadius: '6px',
                  color: 'white',
                }}
                loading={epicsLoading}
                optionLabelProp="label"
              >
                {epics.map((epic) => (
                  <Select.Option
                    key={epic.id}
                    value={epic.id}
                    label={
                      <div className="flex items-center gap-2">
                        <div
                          className="w-2 h-2 rounded-full"
                          style={{ backgroundColor: epic.color || '#1890ff' }}
                        />
                        <span>{epic.title}</span>
                      </div>
                    }
                  >
                    <div className="flex items-center gap-2">
                      <div
                        className="w-2 h-2 rounded-full"
                        style={{ backgroundColor: epic.color || '#1890ff' }}
                      />
                      <span>{epic.title}</span>
                    </div>
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>

            <div className="grid grid-cols-2 gap-4">
              <Form.Item
                name="status"
                label={<span style={{ color: '#8b949e', fontSize: '13px' }}>状态</span>}
              >
                <Select
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    borderRadius: '6px',
                    color: 'white',
                  }}
                  dropdownStyle={{
                    background: '#161b22',
                    color: '#f0f6fc',
                  }}
                >
                  <Select.Option value="TODO">待办</Select.Option>
                  <Select.Option value="IN_PROGRESS">进行中</Select.Option>
                  <Select.Option value="IN_REVIEW">测试中</Select.Option>
                  <Select.Option value="DONE">已完成</Select.Option>
                </Select>
              </Form.Item>

              <Form.Item
                name="priority"
                label={<span style={{ color: '#8b949e', fontSize: '13px' }}>优先级</span>}
              >
                <Select
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    borderRadius: '6px',
                    color: 'white',
                  }}
                  dropdownStyle={{
                    background: '#161b22',
                    color: '#f0f6fc',
                  }}
                >
                  <Select.Option value="LOW">低</Select.Option>
                  <Select.Option value="MEDIUM">中</Select.Option>
                  <Select.Option value="HIGH">高</Select.Option>
                  <Select.Option value="URGENT">紧急</Select.Option>
                </Select>
              </Form.Item>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Form.Item
                name="assigneeId"
                label={<span style={{ color: '#8b949e', fontSize: '13px' }}>负责人</span>}
              >
                <Select
                  placeholder="请选择负责人"
                  allowClear
                  loading={membersLoading}
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    borderRadius: '6px',
                    color: 'white',
                  }}
                  dropdownStyle={{
                    background: '#161b22',
                    color: '#f0f6fc',
                  }}
                >
                  {projectMembers.map((member) => (
                    <Select.Option key={member.userId} value={member.userId}>
                      {member.nickname || member.username || member.email}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>

              <Form.Item
                name="storyPoints"
                label={<span style={{ color: '#8b949e', fontSize: '13px' }}>故事点数</span>}
              >
                <Input
                  type="number"
                  placeholder="输入故事点数"
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    borderRadius: '6px',
                    padding: '10px',
                    color: 'white',
                  }}
                />
              </Form.Item>
            </div>
          </Form>
        </div>
      </Drawer>

      {/* 任务详情抽屉 */}
      <Drawer
        title="任务详情"
        placement="right"
        size="large"
        open={taskDetailOpen}
        onClose={() => setTaskDetailOpen(false)}
        className="glass-dark"
      >
        {taskDetailLoading ? (
          <div className="flex justify-center py-12">
            <Spin size="large" />
          </div>
        ) : selectedTask ? (
          <div className="space-y-4">
            <div>
              <h3 className="text-lg font-semibold text-white mb-2">{selectedTask.title}</h3>
              <div className="flex items-center gap-2 mb-3">
                <Tag color={
                  selectedTask.priority === 'HIGH' ? 'red' :
                  selectedTask.priority === 'MEDIUM' ? 'orange' :
                  selectedTask.priority === 'URGENT' ? 'purple' : 'gray'
                }>
                  {selectedTask.priority}
                </Tag>
                <Tag color="processing">
                  {selectedTask.status === 'TODO' && '待办'}
                  {selectedTask.status === 'IN_PROGRESS' && '进行中'}
                  {selectedTask.status === 'IN_REVIEW' && '审核中'}
                  {selectedTask.status === 'DONE' && '已完成'}
                </Tag>
              </div>
            </div>
            <div>
              <h4 className="text-sm font-medium text-gray-400 mb-2">描述</h4>
              <p className="text-gray-300 text-sm whitespace-pre-wrap">
                {selectedTask.description || '无描述'}
              </p>
            </div>
            {selectedTask.dueDate && (
              <div>
                <h4 className="text-sm font-medium text-gray-400 mb-2">截止日期</h4>
                <p className="text-gray-300 text-sm">{dayjs(selectedTask.dueDate).format('YYYY-MM-DD')}</p>
              </div>
            )}
            {selectedTask.userStoryId && (
              <div>
                <h4 className="text-sm font-medium text-gray-400 mb-2">关联用户故事</h4>
                <p className="text-gray-300 text-sm">
                  #{selectedTask.userStoryId} {selectedTask.userStoryTitle || '(无标题)'}
                </p>
              </div>
            )}
            <div className="pt-4 border-t border-gray-700">
              <div className="flex items-center justify-between text-sm text-gray-400">
                <span>负责人：{selectedTask.assigneeId ? `用户${selectedTask.assigneeId}` : '未分配'}</span>
                <span>创建时间：{dayjs(selectedTask.createdAt).format('YYYY-MM-DD')}</span>
              </div>
            </div>
          </div>
        ) : (
          <Empty description="暂无详情" />
        )}
      </Drawer>

      {/* 问题详情抽屉 */}
      <Drawer
        title="问题详情"
        placement="right"
        size="large"
        open={issueDetailOpen}
        onClose={() => setIssueDetailOpen(false)}
        className="glass-dark"
      >
        {issueDetailLoading ? (
          <div className="flex justify-center py-12">
            <Spin size="large" />
          </div>
        ) : selectedIssue ? (
          <div className="space-y-4">
            <div>
              <h3 className="text-lg font-semibold text-white mb-2">
                #{selectedIssue.id} {selectedIssue.title}
              </h3>
              <div className="flex items-center gap-2 mb-3">
                <Tag color={
                  selectedIssue.severity === 'CRITICAL' ? 'red' :
                  selectedIssue.severity === 'HIGH' ? 'orange' :
                  selectedIssue.severity === 'NORMAL' ? 'blue' : 'gray'
                }>
                  {selectedIssue.severity}
                </Tag>
                <Tag color="processing">
                  {selectedIssue.status === 'TODO' && '待办'}
                  {selectedIssue.status === 'IN_PROGRESS' && '进行中'}
                  {selectedIssue.status === 'IN_REVIEW' && '已解决'}
                  {selectedIssue.status === 'DONE' && '已关闭'}
                </Tag>
                <Tag color="default">
                  {selectedIssue.type === 'BUG' && '缺陷'}
                  {selectedIssue.type === 'FEATURE' && '功能'}
                  {selectedIssue.type === 'IMPROVEMENT' && '改进'}
                  {selectedIssue.type === 'TASK' && '任务'}
                </Tag>
              </div>
            </div>
            <div>
              <h4 className="text-sm font-medium text-gray-400 mb-2">描述</h4>
              <p className="text-gray-300 text-sm whitespace-pre-wrap">
                {selectedIssue.description || '无描述'}
              </p>
            </div>
            {selectedIssue.resolution && (
              <div>
                <h4 className="text-sm font-medium text-gray-400 mb-2">解决方案</h4>
                <p className="text-gray-300 text-sm whitespace-pre-wrap">
                  {selectedIssue.resolution}
                </p>
              </div>
            )}
            <div className="pt-4 border-t border-gray-700">
              <div className="grid grid-cols-2 gap-4 text-sm text-gray-400">
                <div>
                  <span className="text-gray-500">负责人：</span>
                  {selectedIssue.assigneeName || (selectedIssue.assigneeId ? `用户${selectedIssue.assigneeId}` : '未分配')}
                </div>
                <div>
                  <span className="text-gray-500">优先级：</span>
                  {selectedIssue.priority || '-'}
                </div>
                {selectedIssue.dueDate && (
                  <div>
                    <span className="text-gray-500">截止日期：</span>
                    {dayjs(selectedIssue.dueDate).format('YYYY-MM-DD')}
                  </div>
                )}
                <div>
                  <span className="text-gray-500">创建时间：</span>
                  {dayjs(selectedIssue.createdAt).format('YYYY-MM-DD')}
                </div>
              </div>
            </div>
          </div>
        ) : (
          <Empty description="暂无详情" />
        )}
      </Drawer>

      {/* 任务编辑抽屉 - 左侧边栏式 */}
      <Drawer
        title="编辑任务"
        placement="left"
        open={taskEditDrawerOpen}
        onClose={() => setTaskEditDrawerOpen(false)}
        width={600}
        styles={{
          body: {
            padding: 0,
            background: '#161b22',
            color: '#f0f6fc',
          },
          header: {
            background: '#161b22',
            borderBottom: '1px solid rgba(255,255,255,0.05)',
            padding: '20px 24px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          },
          footer: {
            background: '#161b22',
            borderTop: '1px solid rgba(255,255,255,0.05)',
            padding: '16px 24px',
            display: 'flex',
            gap: '12px',
            justifyContent: 'flex-end',
          },
        }}
        footer={
          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
            <Button
              onClick={() => setTaskEditDrawerOpen(false)}
              style={{
                background: 'transparent',
                border: '1px solid #30363d',
                color: '#c9d1d9',
                borderRadius: '6px',
                padding: '8px 16px',
              }}
            >
              取消
            </Button>
            <Button
              type="primary"
              onClick={() => taskForm.submit()}
              loading={taskEditLoading}
              style={{
                background: '#ff8c42',
                border: 'none',
                borderRadius: '6px',
                padding: '8px 16px',
                fontWeight: 500,
              }}
            >
              保存修改
            </Button>
          </div>
        }
      >
        <div className="max-h-[70vh] overflow-y-auto pr-4">
          <Form
            form={taskForm}
            layout="vertical"
            onFinish={handleTaskEditSubmit}
            size="large"
          >
            <Form.Item
              name="title"
              label="标题"
              rules={[{ required: true, message: '请输入任务标题' }]}
            >
              <Input
                placeholder="请输入标题"
                className="bg-gray-700/50 border-gray-600 text-white"
              />
            </Form.Item>

            <Form.Item
              name="description"
              label="描述"
            >
              <TextArea
                rows={4}
                placeholder="描述任务..."
                className="bg-gray-700/50 border-gray-600 text-white"
              />
            </Form.Item>

            <div className="grid grid-cols-2 gap-4">
              <Form.Item
                name="status"
                label="状态"
              >
                <Select className="bg-gray-700/50 border-gray-600">
                  <Select.Option value="TODO">待办</Select.Option>
                  <Select.Option value="IN_PROGRESS">进行中</Select.Option>
                  <Select.Option value="IN_REVIEW">测试中</Select.Option>
                  <Select.Option value="DONE">已完成</Select.Option>
                </Select>
              </Form.Item>

              <Form.Item
                name="priority"
                label="优先级"
              >
                <Select className="bg-gray-700/50 border-gray-600">
                  <Select.Option value="LOW">低</Select.Option>
                  <Select.Option value="MEDIUM">中</Select.Option>
                  <Select.Option value="HIGH">高</Select.Option>
                  <Select.Option value="URGENT">紧急</Select.Option>
                </Select>
              </Form.Item>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Form.Item
                name="assigneeId"
                label="负责人 ID"
              >
                <Input
                  type="number"
                  placeholder="输入负责人 ID"
                  className="bg-gray-700/50 border-gray-600 text-white"
                />
              </Form.Item>

              <Form.Item
                name="storyPoints"
                label="故事点数"
              >
                <Input
                  type="number"
                  placeholder="输入故事点数"
                  className="bg-gray-700/50 border-gray-600 text-white"
                />
              </Form.Item>
            </div>

            <Form.Item
              name="dueDate"
              label="截止日期"
            >
              <DatePicker
                className="w-full bg-gray-700/50 border-gray-600 text-white"
                format="YYYY-MM-DD"
              />
            </Form.Item>

            <Form.Item
              name="tags"
              label="标签（逗号分隔）"
            >
              <Input
                placeholder="输入标签，用逗号分隔"
                className="bg-gray-700/50 border-gray-600 text-white"
              />
            </Form.Item>

            <Form.Item
              name="userStoryId"
              label="关联用户故事 ID"
            >
              <Input
                type="number"
                placeholder="输入用户故事 ID（可选）"
                className="bg-gray-700/50 border-gray-600 text-white"
              />
            </Form.Item>
          </Form>
        </div>
      </Drawer>

      {/* 问题编辑抽屉 - 左侧边栏式 */}
      <Drawer
        title="编辑问题"
        placement="left"
        open={issueEditDrawerOpen}
        onClose={() => setIssueEditDrawerOpen(false)}
        width={600}
        styles={{
          body: {
            padding: 0,
            background: '#161b22',
            color: '#f0f6fc',
          },
          header: {
            background: '#161b22',
            borderBottom: '1px solid rgba(255,255,255,0.05)',
            padding: '20px 24px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          },
          footer: {
            background: '#161b22',
            borderTop: '1px solid rgba(255,255,255,0.05)',
            padding: '16px 24px',
            display: 'flex',
            gap: '12px',
            justifyContent: 'flex-end',
          },
        }}
        footer={
          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
            <Button
              onClick={() => setIssueEditDrawerOpen(false)}
              style={{
                background: 'transparent',
                border: '1px solid #30363d',
                color: '#c9d1d9',
                borderRadius: '6px',
                padding: '8px 16px',
              }}
            >
              取消
            </Button>
            <Button
              type="primary"
              onClick={() => issueForm.submit()}
              loading={issueEditLoading}
              style={{
                background: '#ff8c42',
                border: 'none',
                borderRadius: '6px',
                padding: '8px 16px',
                fontWeight: 500,
              }}
            >
              保存修改
            </Button>
          </div>
        }
      >
        <div className="max-h-[70vh] overflow-y-auto pr-4">
          <Form
            form={issueForm}
            layout="vertical"
            onFinish={handleIssueEditSubmit}
            size="large"
          >
            <Form.Item
              name="title"
              label="标题"
              rules={[{ required: true, message: '请输入问题标题' }]}
            >
              <Input
                placeholder="请输入标题"
                className="bg-gray-700/50 border-gray-600 text-white"
              />
            </Form.Item>

            <Form.Item
              name="description"
              label="描述"
            >
              <TextArea
                rows={4}
                placeholder="描述问题..."
                className="bg-gray-700/50 border-gray-600 text-white"
              />
            </Form.Item>

            <div className="grid grid-cols-2 gap-4">
              <Form.Item
                name="type"
                label="类型"
              >
                <Select className="bg-gray-700/50 border-gray-600">
                  <Select.Option value="BUG">缺陷</Select.Option>
                  <Select.Option value="FEATURE">功能</Select.Option>
                  <Select.Option value="IMPROVEMENT">改进</Select.Option>
                  <Select.Option value="TASK">任务</Select.Option>
                </Select>
              </Form.Item>

              <Form.Item
                name="severity"
                label="严重程度"
              >
                <Select className="bg-gray-700/50 border-gray-600">
                  <Select.Option value="CRITICAL">严重</Select.Option>
                  <Select.Option value="HIGH">高</Select.Option>
                  <Select.Option value="NORMAL">中</Select.Option>
                  <Select.Option value="LOW">低</Select.Option>
                </Select>
              </Form.Item>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Form.Item
                name="status"
                label="状态"
              >
                <Select className="bg-gray-700/50 border-gray-600">
                  <Select.Option value="TODO">待办</Select.Option>
                  <Select.Option value="IN_PROGRESS">进行中</Select.Option>
                  <Select.Option value="IN_REVIEW">已解决</Select.Option>
                  <Select.Option value="DONE">已关闭</Select.Option>
                </Select>
              </Form.Item>

              <Form.Item
                name="priority"
                label="优先级"
              >
                <Select className="bg-gray-700/50 border-gray-600">
                  <Select.Option value="LOW">低</Select.Option>
                  <Select.Option value="MEDIUM">中</Select.Option>
                  <Select.Option value="HIGH">高</Select.Option>
                  <Select.Option value="URGENT">紧急</Select.Option>
                </Select>
              </Form.Item>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Form.Item
                name="assigneeId"
                label="负责人"
              >
                <Select
                  placeholder="选择负责人"
                  allowClear
                  className="bg-gray-700/50 border-gray-600 text-white"
                  dropdownStyle={{ backgroundColor: '#1f242c', border: '1px solid #30363d' }}
                >
                  {projectMembers.map((member) => (
                    <Select.Option key={member.userId} value={member.userId}>
                      {member.nickname || member.username || member.email}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>

              <Form.Item
                name="dueDate"
                label="截止日期"
              >
                <DatePicker
                  className="w-full bg-gray-700/50 border-gray-600 text-white"
                  format="YYYY-MM-DD"
                />
              </Form.Item>
            </div>

            <Form.Item
              name="tags"
              label="标签（逗号分隔）"
            >
              <Input
                placeholder="输入标签，用逗号分隔"
                className="bg-gray-700/50 border-gray-600 text-white"
              />
            </Form.Item>
          </Form>
        </div>
      </Drawer>

      {/* 用户故事创建抽屉 - 左侧边栏式 */}
      <Drawer
        title="创建用户故事"
        placement="left"
        open={storyCreateOpen}
        onClose={() => setStoryCreateOpen(false)}
        width={600}
        className="story-drawer"
        styles={{
          body: {
            padding: 0,
            background: '#161b22',
            color: '#f0f6fc',
          },
          header: {
            background: '#161b22',
            borderBottom: '1px solid rgba(255,255,255,0.05)',
            padding: '20px 24px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          },
          footer: {
            background: '#161b22',
            borderTop: '1px solid rgba(255,255,255,0.05)',
            padding: '16px 24px',
            display: 'flex',
            gap: '12px',
            justifyContent: 'flex-end',
          },
        }}
        footer={
          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
            <Button
              onClick={() => setStoryCreateOpen(false)}
              style={{
                background: 'transparent',
                border: '1px solid #30363d',
                color: '#c9d1d9',
                borderRadius: '6px',
                padding: '8px 16px',
              }}
            >
              取消
            </Button>
            <Button
              type="primary"
              onClick={() => storyCreateForm.submit()}
              loading={storyCreateLoading}
              style={{
                background: '#ff8c42',
                border: 'none',
                borderRadius: '6px',
                padding: '8px 24px',
                fontWeight: 'bold',
              }}
            >
              创建故事
            </Button>
          </div>
        }
      >
        <div className="max-h-full overflow-y-auto pr-4" style={{ background: '#161b22', minHeight: '100%' }}>
          <Form
            form={storyCreateForm}
            layout="vertical"
            onFinish={handleStoryCreateSubmit}
            size="large"
          >
            <Form.Item
              name="title"
              label={<span style={{ color: '#8b949e', fontSize: '13px' }}>故事标题</span>}
              rules={[{ required: true, message: '请输入用户故事标题' }]}
            >
              <Input
                placeholder="请输入标题"
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  borderRadius: '6px',
                  padding: '10px',
                  color: 'white',
                }}
              />
            </Form.Item>

            <Form.Item
              name="description"
              label={<span style={{ color: '#8b949e', fontSize: '13px' }}>故事描述</span>}
            >
              <TextArea
                rows={10}
                placeholder="作为一名...用户，我想要...功能，以便我可以...价值"
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  borderRadius: '6px',
                  padding: '10px',
                  color: 'white',
                  minHeight: '200px',
                }}
              />
            </Form.Item>

            <Form.Item
              name="epicId"
              label={<span style={{ color: '#8b949e', fontSize: '13px' }}>所属服务</span>}
            >
              <Select
                placeholder="请选择所属服务"
                allowClear
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  borderRadius: '6px',
                  color: 'white',
                }}
                loading={epicsLoading}
                optionLabelProp="label"
              >
                {epics.map((epic) => (
                  <Select.Option
                    key={epic.id}
                    value={epic.id}
                    label={
                      <div className="flex items-center gap-2">
                        <div
                          className="w-2 h-2 rounded-full"
                          style={{ backgroundColor: epic.color || '#1890ff' }}
                        />
                        <span>{epic.title}</span>
                      </div>
                    }
                  >
                    <div className="flex items-center gap-2">
                      <div
                        className="w-2 h-2 rounded-full"
                        style={{ backgroundColor: epic.color || '#1890ff' }}
                      />
                      <span>{epic.title}</span>
                    </div>
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>

            <div className="grid grid-cols-2 gap-4">
              <Form.Item
                name="priority"
                label={<span style={{ color: '#8b949e', fontSize: '13px' }}>优先级</span>}
              >
                <Select
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    borderRadius: '6px',
                    color: 'white',
                  }}
                >
                  <Select.Option value="LOW">低</Select.Option>
                  <Select.Option value="MEDIUM">中</Select.Option>
                  <Select.Option value="HIGH">高</Select.Option>
                  <Select.Option value="URGENT">紧急</Select.Option>
                </Select>
              </Form.Item>

              <Form.Item
                name="storyPoints"
                label={<span style={{ color: '#8b949e', fontSize: '13px' }}>故事点数</span>}
              >
                <Input
                  type="number"
                  placeholder="输入故事点数"
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    borderRadius: '6px',
                    padding: '10px',
                    color: 'white',
                  }}
                />
              </Form.Item>
            </div>

            <Form.Item
              name="assigneeId"
              label={<span style={{ color: '#8b949e', fontSize: '13px' }}>负责人</span>}
            >
              <Select
                placeholder="请选择负责人"
                allowClear
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  borderRadius: '6px',
                  color: 'white',
                }}
                loading={membersLoading}
              >
                {projectMembers.map((member) => (
                  <Select.Option key={member.userId} value={member.userId}>
                    {member.nickname || member.username || member.email}
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>

            <Form.Item
              name="acceptanceCriteria"
              label={<span style={{ color: '#8b949e', fontSize: '13px' }}>验收标准</span>}
            >
              <TextArea
                rows={4}
                placeholder="输入验收标准，用逗号分隔多条标准..."
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  borderRadius: '6px',
                  padding: '10px',
                  color: 'white',
                }}
              />
            </Form.Item>
          </Form>
        </div>
      </Drawer>

      {/* 任务创建抽屉 */}
      <Drawer
        title="创建任务"
        placement="left"
        open={taskCreateOpen}
        onClose={() => setTaskCreateOpen(false)}
        width={600}
        styles={{
          body: { padding: 0, background: '#161b22', color: '#f0f6fc' },
          header: {
            background: '#161b22',
            borderBottom: '1px solid rgba(255,255,255,0.05)',
            padding: '20px 24px',
          },
          footer: {
            background: '#161b22',
            borderTop: '1px solid rgba(255,255,255,0.05)',
          },
        }}
        footer={
          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
            <Button
              onClick={() => setTaskCreateOpen(false)}
              style={{
                background: 'transparent',
                border: '1px solid #30363d',
                color: '#c9d1d9',
                borderRadius: '6px',
                padding: '8px 16px',
              }}
            >
              取消
            </Button>
            <Button
              type="primary"
              onClick={() => taskCreateForm.submit()}
              loading={taskCreateLoading}
              style={{
                background: '#ff8c42',
                border: 'none',
                borderRadius: '6px',
                padding: '8px 24px',
                fontWeight: 'bold',
              }}
            >
              创建任务
            </Button>
          </div>
        }
      >
        <div style={{ padding: '24px', height: '100%', overflowY: 'auto' }}>
          <Form
            form={taskCreateForm}
            layout="vertical"
            onFinish={handleTaskCreateSubmit}
            size="large"
          >
            <Form.Item
              name="title"
              label="标题"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              rules={[{ required: true, message: '请输入任务标题' }]}
            >
              <Input
                placeholder="请输入标题"
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                  padding: '10px',
                }}
              />
            </Form.Item>

            <Form.Item
              name="description"
              label="描述"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
            >
              <TextArea
                rows={10}
                placeholder="描述任务..."
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                  padding: '10px',
                  minHeight: '200px',
                }}
              />
            </Form.Item>

            <div className="grid grid-cols-2 gap-4">
              <Form.Item
                name="status"
                label="状态"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              >
                <Select
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: '#f0f6fc',
                    borderRadius: '6px',
                  }}
                  dropdownStyle={{
                    background: '#161b22',
                    color: '#f0f6fc',
                  }}
                >
                  <Select.Option value="TODO">待办</Select.Option>
                  <Select.Option value="IN_PROGRESS">进行中</Select.Option>
                  <Select.Option value="IN_REVIEW">测试中</Select.Option>
                  <Select.Option value="DONE">已完成</Select.Option>
                </Select>
              </Form.Item>

              <Form.Item
                name="priority"
                label="优先级"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              >
                <Select
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: '#f0f6fc',
                    borderRadius: '6px',
                  }}
                  dropdownStyle={{
                    background: '#161b22',
                    color: '#f0f6fc',
                  }}
                >
                  <Select.Option value="LOW">低</Select.Option>
                  <Select.Option value="MEDIUM">中</Select.Option>
                  <Select.Option value="HIGH">高</Select.Option>
                  <Select.Option value="URGENT">紧急</Select.Option>
                </Select>
              </Form.Item>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Form.Item
                name="assigneeId"
                label="负责人"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              >
                <Select
                  placeholder="请选择负责人"
                  allowClear
                  loading={membersLoading}
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: '#f0f6fc',
                    borderRadius: '6px',
                  }}
                  dropdownStyle={{
                    background: '#161b22',
                    color: '#f0f6fc',
                  }}
                >
                  {projectMembers.map((member) => (
                    <Select.Option key={member.userId} value={member.userId}>
                      {member.nickname || member.username || member.email}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>

              <Form.Item
                name="storyPoints"
                label="故事点数"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              >
                <Input
                  type="number"
                  placeholder="输入故事点数"
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: '#f0f6fc',
                    borderRadius: '6px',
                    padding: '10px',
                  }}
                />
              </Form.Item>
            </div>

            <Form.Item
              name="dueDate"
              label="截止日期"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
            >
              <DatePicker
                style={{
                  width: '100%',
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                }}
                format="YYYY-MM-DD"
              />
            </Form.Item>

            <Form.Item
              name="tags"
              label="标签（逗号分隔）"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
            >
              <Input
                placeholder="输入标签，用逗号分隔"
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                  padding: '10px',
                }}
              />
            </Form.Item>

            <Form.Item
              name="userStoryId"
              label="关联用户故事"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
            >
              <Select
                placeholder="请选择用户故事（可选）"
                allowClear
                loading={storiesLoading}
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                }}
                dropdownStyle={{
                  background: '#161b22',
                  color: '#f0f6fc',
                }}
              >
                {storiesList.map((story) => (
                  <Select.Option key={story.id} value={story.id}>
                    {story.title}
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
          </Form>
        </div>
      </Drawer>

      {/* 问题创建抽屉 */}
      <Drawer
        title="创建问题"
        placement="left"
        open={issueCreateOpen}
        onClose={() => setIssueCreateOpen(false)}
        width={600}
        styles={{
          body: { padding: 0, background: '#161b22', color: '#f0f6fc' },
          header: {
            background: '#161b22',
            borderBottom: '1px solid rgba(255,255,255,0.05)',
            padding: '20px 24px',
          },
          footer: {
            background: '#161b22',
            borderTop: '1px solid rgba(255,255,255,0.05)',
          },
        }}
        footer={
          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
            <Button
              onClick={() => setIssueCreateOpen(false)}
              style={{
                background: 'transparent',
                border: '1px solid #30363d',
                color: '#c9d1d9',
                borderRadius: '6px',
                padding: '8px 16px',
              }}
            >
              取消
            </Button>
            <Button
              type="primary"
              onClick={() => issueCreateForm.submit()}
              loading={issueCreateLoading}
              style={{
                background: '#ff8c42',
                border: 'none',
                borderRadius: '6px',
                padding: '8px 24px',
                fontWeight: 'bold',
              }}
            >
              创建问题
            </Button>
          </div>
        }
      >
        <div style={{ padding: '24px', height: '100%', overflowY: 'auto' }}>
          <Form
            form={issueCreateForm}
            layout="vertical"
            onFinish={handleIssueCreateSubmit}
            size="large"
          >
            <Form.Item
              name="title"
              label="标题"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              rules={[{ required: true, message: '请输入问题标题' }]}
            >
              <Input
                placeholder="请输入标题"
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                  padding: '10px',
                }}
              />
            </Form.Item>

            <Form.Item
              name="description"
              label="描述"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
            >
              <TextArea
                rows={10}
                placeholder="描述问题..."
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                  padding: '10px',
                  minHeight: '200px',
                }}
              />
            </Form.Item>

            <div className="grid grid-cols-2 gap-4">
              <Form.Item
                name="type"
                label="类型"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              >
                <Select
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: '#f0f6fc',
                    borderRadius: '6px',
                  }}
                  dropdownStyle={{
                    background: '#161b22',
                    color: '#f0f6fc',
                  }}
                >
                  <Select.Option value="BUG">缺陷</Select.Option>
                  <Select.Option value="FEATURE">功能</Select.Option>
                  <Select.Option value="IMPROVEMENT">改进</Select.Option>
                  <Select.Option value="TASK">任务</Select.Option>
                </Select>
              </Form.Item>

              <Form.Item
                name="severity"
                label="严重程度"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              >
                <Select
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: '#f0f6fc',
                    borderRadius: '6px',
                  }}
                  dropdownStyle={{
                    background: '#161b22',
                    color: '#f0f6fc',
                  }}
                >
                  <Select.Option value="CRITICAL">严重</Select.Option>
                  <Select.Option value="HIGH">高</Select.Option>
                  <Select.Option value="NORMAL">中</Select.Option>
                  <Select.Option value="LOW">低</Select.Option>
                </Select>
              </Form.Item>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Form.Item
                name="status"
                label="状态"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              >
                <Select
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: '#f0f6fc',
                    borderRadius: '6px',
                  }}
                  dropdownStyle={{
                    background: '#161b22',
                    color: '#f0f6fc',
                  }}
                >
                  <Select.Option value="TODO">待办</Select.Option>
                  <Select.Option value="IN_PROGRESS">进行中</Select.Option>
                  <Select.Option value="IN_REVIEW">已解决</Select.Option>
                  <Select.Option value="DONE">已关闭</Select.Option>
                </Select>
              </Form.Item>

              <Form.Item
                name="priority"
                label="优先级"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              >
                <Select
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: '#f0f6fc',
                    borderRadius: '6px',
                  }}
                  dropdownStyle={{
                    background: '#161b22',
                    color: '#f0f6fc',
                  }}
                >
                  <Select.Option value="LOW">低</Select.Option>
                  <Select.Option value="MEDIUM">中</Select.Option>
                  <Select.Option value="HIGH">高</Select.Option>
                  <Select.Option value="URGENT">紧急</Select.Option>
                </Select>
              </Form.Item>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Form.Item
                name="assigneeId"
                label="负责人"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              >
                <Select
                  placeholder="请选择负责人"
                  allowClear
                  loading={membersLoading}
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: '#f0f6fc',
                    borderRadius: '6px',
                  }}
                  dropdownStyle={{
                    background: '#161b22',
                    color: '#f0f6fc',
                  }}
                >
                  {projectMembers.map((member) => (
                    <Select.Option key={member.userId} value={member.userId}>
                      {member.nickname || member.username || member.email}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>

              <Form.Item
                name="dueDate"
                label="截止日期"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              >
                <DatePicker
                  style={{
                    width: '100%',
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: '#f0f6fc',
                    borderRadius: '6px',
                  }}
                  format="YYYY-MM-DD"
                />
              </Form.Item>
            </div>

            <Form.Item
              name="tags"
              label="标签（逗号分隔）"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
            >
              <Input
                placeholder="输入标签，用逗号分隔"
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                  padding: '10px',
                }}
              />
            </Form.Item>
          </Form>
        </div>
      </Drawer>

      {/* Wiki 创建抽屉 */}
      <Drawer
        title="创建 Wiki 文档"
        placement="left"
        open={wikiCreateOpen}
        onClose={() => setWikiCreateOpen(false)}
        width={600}
        styles={{
          body: { padding: 0, background: '#161b22', color: '#f0f6fc' },
          header: {
            background: '#161b22',
            borderBottom: '1px solid rgba(255,255,255,0.05)',
            padding: '20px 24px',
          },
          footer: {
            background: '#161b22',
            borderTop: '1px solid rgba(255,255,255,0.05)',
          },
        }}
        footer={
          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
            <Button
              onClick={() => setWikiCreateOpen(false)}
              style={{
                background: 'transparent',
                border: '1px solid #30363d',
                color: '#c9d1d9',
                borderRadius: '6px',
                padding: '8px 16px',
              }}
            >
              取消
            </Button>
            <Button
              type="primary"
              onClick={() => wikiCreateForm.submit()}
              loading={wikiCreateLoading}
              style={{
                background: '#ff8c42',
                border: 'none',
                borderRadius: '6px',
                padding: '8px 24px',
                fontWeight: 'bold',
              }}
            >
              创建文档
            </Button>
          </div>
        }
      >
        <div style={{ padding: '24px', height: '100%', overflowY: 'auto' }}>
          <Form
            form={wikiCreateForm}
            layout="vertical"
            onFinish={handleWikiCreateSubmit}
            size="large"
          >
            <Form.Item
              name="title"
              label="标题"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              rules={[{ required: true, message: '请输入 Wiki 标题' }]}
            >
              <Input
                placeholder="请输入标题"
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                  padding: '10px',
                }}
              />
            </Form.Item>

            <Form.Item
              name="summary"
              label="摘要"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
            >
              <TextArea
                rows={4}
                placeholder="输入文档摘要..."
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                  padding: '10px',
                }}
              />
            </Form.Item>

            <Form.Item
              name="content"
              label="内容"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
            >
              <TextArea
                rows={12}
                placeholder="输入 Wiki 内容..."
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                  padding: '10px',
                  minHeight: '250px',
                }}
              />
            </Form.Item>

            <Form.Item
              name="tags"
              label="标签（逗号分隔）"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
            >
              <Input
                placeholder="输入标签，用逗号分隔"
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                  padding: '10px',
                }}
              />
            </Form.Item>

            <Form.Item
              name="isPublished"
              label="发布状态"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              valuePropName="checked"
            >
              <Select
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                }}
                dropdownStyle={{
                  background: '#161b22',
                  color: '#f0f6fc',
                }}
              >
                <Select.Option value={false}>草稿</Select.Option>
                <Select.Option value={true}>已发布</Select.Option>
              </Select>
            </Form.Item>
          </Form>
        </div>
      </Drawer>
    </div>
  );
}