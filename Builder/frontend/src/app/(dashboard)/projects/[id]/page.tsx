'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Card, Tabs, TabsProps, Avatar, Tag, Button, Empty, Dropdown, MenuProps, message, Modal, Form, Input, DatePicker, Select, ColorPicker, Drawer, Spin, Pagination, Table, TableColumnsType } from 'antd';
import {
  EditOutlined,
  DeleteOutlined,
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
} from '@ant-design/icons';
import Link from 'next/link';
import dayjs from 'dayjs';
import { getProject, deleteProject, updateProject } from '@/lib/api/project';
import { getTasks } from '@/lib/api/task';
import { getStories } from '@/lib/api/story';
import { getIssues } from '@/lib/api/issue';
import { getWikis } from '@/lib/api/wiki';
import { getBurndown } from '@/lib/api/report';
import type { Project, ProjectStatus } from '@/lib/api/project';
import type { Task } from '@/lib/api/task';
import type { UserStory } from '@/lib/api/story';
import type { PageInfo } from '@/types/api';
import type { Issue } from '@/lib/api/issue';
import type { Wiki } from '@/lib/api/wiki';
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
      {issue.type === 'bug' ? <BugOutlined /> : issue.type === 'feature' ? <CheckSquareOutlined /> : <CodeOutlined />}
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
  // 用户故事分页相关状态
  const [stories, setStories] = useState<UserStory[]>([]);
  const [storiesTotal, setStoriesTotal] = useState(0);
  const [storiesPage, setStoriesPage] = useState(1);
  const [storiesPageSize] = useState(10);
  const [storiesLoading, setStoriesLoading] = useState(false);
  const [issues, setIssues] = useState<Issue[]>([]);
  const [wikis, setWikis] = useState<Wiki[]>([]);
  const [burndownData, setBurndownData] = useState<BurndownData[]>([]);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [editDrawerOpen, setEditDrawerOpen] = useState(false);
  const [selectedIcon, setSelectedIcon] = useState<string>('🛒');
  const [form] = Form.useForm();

  // 获取项目详情
  const fetchProject = async () => {
    setFetchLoading(true);
    try {
      const projectData = await getProject(Number(projectId));
      setProject(projectData);
      setSelectedIcon(projectData.icon || '🛒');

      // 获取任务列表
      const tasksResult = await getTasks(Number(projectId));
      setTasks(tasksResult.list || []);

      // 获取用户故事列表（分页）
      await fetchStories(Number(projectId), 1);

      // 获取问题列表
      const issuesResult = await getIssues(Number(projectId));
      setIssues(issuesResult || []);

      // 获取 Wiki 文档列表
      const wikisResult = await getWikis(Number(projectId));
      setWikis(wikisResult || []);

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
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '获取用户故事失败';
      message.error(errorMessage);
    } finally {
      setStoriesLoading(false);
    }
  };

  // 页码切换
  const handleStoriesPageChange = (page: number) => {
    setStoriesPage(page);
    fetchStories(Number(projectId), page);
  };

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

  // 格式化故事状态（支持大写和小写）
  const getStoryStatusText = (status: string) => {
    const map: Record<string, string> = {
      todo: '待办',
      in_progress: '进行中',
      testing: '测试中',
      done: '已完成',
      TODO: '待办',
      IN_PROGRESS: '进行中',
      TESTING: '测试中',
      DONE: '已完成',
    };
    return map[status] || status;
  };

  // 获取故事状态颜色（支持大写和小写）
  const getStoryStatusColor = (status: string) => {
    const map: Record<string, string> = {
      todo: 'default',
      in_progress: 'processing',
      testing: 'warning',
      done: 'success',
      TODO: 'default',
      IN_PROGRESS: 'processing',
      TESTING: 'warning',
      DONE: 'success',
    };
    return map[status] || 'default';
  };

  // 获取问题类型文本
  const getIssueTypeText = (type: string) => {
    const map: Record<string, string> = {
      bug: '缺陷',
      feature: '功能',
      improvement: '改进',
      task: '任务',
    };
    return map[type] || type;
  };

  // 获取问题严重程度颜色
  const getIssueSeverityColor = (severity: string) => {
    const map: Record<string, string> = {
      critical: 'red',
      high: 'orange',
      medium: 'blue',
      low: 'gray',
    };
    return map[severity] || 'gray';
  };

  // 获取问题状态文本
  const getIssueStatusText = (status: string) => {
    const map: Record<string, string> = {
      open: '打开',
      in_progress: '进行中',
      resolved: '已解决',
      closed: '已关闭',
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
          <div className="flex justify-end mb-4">
            <Link href={`/projects/${projectId}/tasks`}>
              <Button type="primary" icon={<CheckSquareOutlined />}>
                进入任务看板
              </Button>
            </Link>
          </div>
          {storiesLoading ? (
            <div className="text-center py-12">
              <Spin size="large" description="加载用户故事中..." />
            </div>
          ) : stories.length > 0 ? (
            <>
              <Table<UserStory>
                dataSource={stories}
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
                    title: '验收标准',
                    dataIndex: 'acceptanceCriteria',
                    key: 'acceptanceCriteria',
                    width: 120,
                    render: (acceptanceCriteria?: string) => (
                      acceptanceCriteria ? (
                        <Tag color="blue">已定义</Tag>
                      ) : (
                        <span className="text-gray-500">-</span>
                      )
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
      key: 'issues',
      label: (
        <span className="flex items-center gap-2">
          <BugOutlined />
          问题追踪
        </span>
      ),
      children: (
        <div className="space-y-4">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-semibold text-white">问题列表</h3>
            <span className="text-gray-400">共 {issues.length} 个问题</span>
          </div>
          {issues.length > 0 ? (
            <div className="space-y-3">
              {issues.map((issue) => (
                <div
                  key={issue.id}
                  className="bg-gray-800/50 border border-gray-700 rounded-lg p-4 hover:border-orange-500/50 transition-colors"
                >
                  <div className="flex items-start gap-3">
                    <div className={`flex-shrink-0 w-10 h-10 rounded-lg flex items-center justify-center ${
                      issue.type === 'bug' ? 'bg-red-500/20' :
                      issue.type === 'feature' ? 'bg-green-500/20' :
                      'bg-blue-500/20'
                    }`}>
                      {issue.type === 'bug' ? <BugOutlined className={issue.type === 'bug' ? 'text-red-400' : ''} /> :
                       issue.type === 'feature' ? <CheckSquareOutlined className="text-green-400" /> :
                       <CodeOutlined className="text-blue-400" />}
                    </div>
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="text-gray-400 text-sm">#{issue.id}</span>
                        <h4 className="text-white font-medium flex-1">{issue.title}</h4>
                        <Tag color={getIssueSeverityColor(issue.severity)}>
                          {getIssueTypeText(issue.type)} · {issue.severity}
                        </Tag>
                      </div>
                      <div className="flex items-center gap-4 text-sm text-gray-400">
                        <span>状态：{getIssueStatusText(issue.status)}</span>
                        <span>优先级：{issue.priority}</span>
                        {issue.dueDate && <span>截止日期：{formatDate(issue.dueDate)}</span>}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <BugOutlined style={{ fontSize: 64, color: '#6b7280' }} />
              <h3 className="text-gray-400 mt-4">暂无问题</h3>
              <p className="text-gray-500 mt-2">问题追踪功能开发中</p>
            </div>
          )}
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
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-semibold text-white">Wiki 文档</h3>
            <span className="text-gray-400">共 {wikis.length} 篇文档</span>
          </div>
          {wikis.length > 0 ? (
            <div className="space-y-3">
              {wikis.map((wiki) => (
                <div
                  key={wiki.id}
                  className="bg-gray-800/50 border border-gray-700 rounded-lg p-4 hover:border-orange-500/50 transition-colors"
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <h4 className="text-white font-medium text-lg mb-2">{wiki.title}</h4>
                      {wiki.summary && (
                        <p className="text-gray-400 text-sm mb-3">{wiki.summary}</p>
                      )}
                      <div className="flex items-center gap-4 text-sm text-gray-500">
                        <span>作者：{wiki.authorName || '未知'}</span>
                        <span>浏览：{wiki.viewCount}</span>
                        <span>更新：{formatDate(wiki.updatedAt || wiki.createdAt)}</span>
                        {!wiki.isPublished && (
                          <Tag color="warning">未发布</Tag>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
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
        <div className="project-actions" style={{ display: 'flex', gap: '0.75rem' }}>
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
          defaultActiveKey="tasks"
          items={tabItems}
          className="project-detail-tabs"
          tabBarStyle={{
            borderBottomColor: 'var(--border)',
            background: 'var(--surface)',
            padding: '0 1rem',
          }}
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
    </div>
  );
}