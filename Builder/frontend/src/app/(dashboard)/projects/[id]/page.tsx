'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Card, Tabs, TabsProps, Avatar, Tag, Button, Empty, Dropdown, MenuProps, message, Modal, Form, Input, DatePicker, Select, ColorPicker, Drawer, Spin } from 'antd';
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
import type { Project, ProjectStatus } from '@/lib/api/project';

const { Option } = Select;
const { TextArea } = Input;

const statusColorMap: Record<string, string> = {
  active: 'processing',
  completed: 'success',
  archived: 'default',
};

const statusTextMap: Record<string, string> = {
  active: '进行中',
  completed: '已完成',
  archived: '已归档',
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

// 用户故事卡片组件
const StoryCard = ({ story }: { story: { id: string; title: string; description: string; points: number; tags: string[] } }) => (
  <div className="story-card">
    <div className="story-header">
      <span className="story-id">{story.id}</span>
    </div>
    <h3 className="story-title">{story.title}</h3>
    <p className="story-description">{story.description}</p>
    <div className="story-meta">
      <div className="story-points">
        <div className="points-badge">{story.points}</div>
      </div>
      <div className="story-tags">
        {story.tags.map((tag) => (
          <span key={tag} className={`story-tag tag-${tag}`}>
            {tag === 'feature' ? '功能' : tag === 'bug' ? '缺陷' : '史诗'}
          </span>
        ))}
      </div>
    </div>
  </div>
);

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
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [editDrawerOpen, setEditDrawerOpen] = useState(false);
  const [selectedIcon, setSelectedIcon] = useState<string>('🛒');
  const [form] = Form.useForm();

  // 获取项目详情
  const fetchProject = async () => {
    setFetchLoading(true);
    try {
      const response = await getProject(Number(projectId));
      const projectData = response.data;
      setProject(projectData);
      setSelectedIcon(projectData.icon || '🛒');
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

  // Mock 数据 - 用户故事
  const mockStories = [
    { id: 'STORY-001', title: '用户登录功能', description: '实现基于JWT的用户认证系统，支持邮箱密码登录', points: 5, tags: ['feature'] },
    { id: 'STORY-002', title: '项目管理模块', description: '创建、编辑、删除项目的基本CRUD操作', points: 8, tags: ['feature'] },
    { id: 'STORY-003', title: '任务看板优化', description: '优化看板拖拽交互体验', points: 3, tags: ['bug'] },
  ];

  // Mock 数据 - 看板任务
  const mockTasks = {
    analysis: [
      { id: 'TASK-001', title: '需求分析文档编写', priority: 'high', assignee: '张三', comments: 2 },
      { id: 'TASK-002', title: '技术方案设计', priority: 'medium', assignee: '李四', comments: 1 },
    ],
    development: [
      { id: 'TASK-003', title: '前端架构搭建', priority: 'critical', assignee: '王五', comments: 5 },
      { id: 'TASK-004', title: '用户认证模块', priority: 'high', assignee: '赵六', comments: 3 },
    ],
    testing: [
      { id: 'TASK-005', title: '单元测试编写', priority: 'medium', assignee: '钱七', comments: 0 },
    ],
    completed: [
      { id: 'TASK-006', title: '项目初始化', priority: 'low', assignee: '张三', comments: 1 },
      { id: 'TASK-007', title: '环境配置', priority: 'low', comments: 0 },
    ],
  };

  // Mock 数据 - 问题列表
  const mockIssues = [
    { id: 'ISSUE-001', title: '登录页面在Safari浏览器显示异常', type: 'bug', severity: 'high', status: 'open', assignee: '王五' },
    { id: 'ISSUE-002', title: '添加项目成员功能缺失', type: 'feature', severity: 'medium', status: 'open', assignee: '李四' },
    { id: 'ISSUE-003', title: '优化大数据量渲染性能', type: 'improvement', severity: 'low', status: 'closed' },
  ];

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

  // 页签内容 - 任务看板、用户故事等功能后续集成
  const tabItems: TabsProps['items'] = [
    {
      key: 'tasks',
      label: (
        <span className="flex items-center gap-2">
          <CheckSquareOutlined />
          任务看板
        </span>
      ),
      children: (
        <div className="text-center py-12">
          <Link href={`/projects/${projectId}/tasks`}>
            <Button type="primary" icon={<CheckSquareOutlined />}>
              进入任务看板
            </Button>
          </Link>
        </div>
      ),
    },
    {
      key: 'stories',
      label: (
        <span className="flex items-center gap-2">
          <FileTextOutlined />
          用户故事
        </span>
      ),
      children: (
        <div className="empty-state">
          <FileTextOutlined style={{ fontSize: 64 }} />
          <h3>用户故事</h3>
          <p>功能开发中</p>
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
        <div className="empty-state">
          <BugOutlined style={{ fontSize: 64 }} />
          <h3>问题追踪</h3>
          <p>功能开发中</p>
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
        <div className="empty-state">
          <BookOutlined style={{ fontSize: 64 }} />
          <h3>Wiki 知识库</h3>
          <p>暂无文档内容</p>
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
        <div className="empty-state">
          <BarChartOutlined style={{ fontSize: 64 }} />
          <h3>数据报表</h3>
          <p>报表功能开发中</p>
        </div>
      ),
    },
    {
      key: 'members',
      label: (
        <span className="flex items-center gap-2">
          <TeamOutlined />
          成员管理
        </span>
      ),
      children: (
        <div className="text-center py-12">
          <Link href={`/projects/${projectId}/members`}>
            <Button type="primary" icon={<UserAddOutlined />}>
              进入成员管理
            </Button>
          </Link>
        </div>
      ),
    },
  ];

  // 加载状态
  if (fetchLoading) {
    return (
      <div className="flex justify-center items-center py-20">
        <Spin size="large" tip="加载项目中..." />
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
        width={600}
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
              <Option value="active">进行中</Option>
              <Option value="completed">已完成</Option>
              <Option value="archived">已归档</Option>
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