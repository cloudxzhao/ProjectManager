'use client';

import { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Card, Tabs, TabsProps, Avatar, Tag, Progress, Button, Skeleton, Empty, Dropdown, MenuProps, message, Modal } from 'antd';
import {
  EditOutlined,
  DeleteOutlined,
  PlusOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  ProjectOutlined,
} from '@ant-design/icons';
import Link from 'next/link';

// Mock 数据
const mockProject = {
  id: '1',
  name: '电商平台重构',
  description: '重构现有电商平台，提升用户体验和系统性能。包括前端页面重构、后端 API 优化、数据库性能调优等工作。',
  progress: 65,
  members: [
    { id: '1', name: '张三', avatar: null, role: 'admin' },
    { id: '2', name: '李四', avatar: null, role: 'member' },
    { id: '3', name: '王五', avatar: null, role: 'member' },
    { id: '4', name: '赵六', avatar: null, role: 'member' },
    { id: '5', name: '钱七', avatar: null, role: 'member' },
  ],
  status: 'active',
  color: '#f97316',
  icon: '🛒',
  startDate: '2024-01-01',
  endDate: '2024-06-30',
};

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

export default function ProjectDetailPage() {
  const params = useParams();
  const router = useRouter();
  const projectId = params.id as string;

  const [loading, setLoading] = useState(false);
  const [project, setProject] = useState(mockProject);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);

  // 项目操作菜单
  const projectMenuItems: MenuProps['items'] = [
    {
      key: 'edit',
      icon: <EditOutlined />,
      label: '编辑项目',
      onClick: () => message.info('编辑项目功能开发中'),
    },
    {
      key: 'delete',
      icon: <DeleteOutlined className="text-red-500" />,
      label: <span className="text-red-500">删除项目</span>,
      onClick: () => setDeleteModalOpen(true),
      danger: true,
    },
  ];

  // 页签内容
  const tabItems: TabsProps['items'] = [
    {
      key: 'tasks',
      label: '任务看板',
      children: (
        <div className="text-center py-12">
          <Link href={`/projects/${projectId}/tasks`}>
            <Button type="primary" icon={<ProjectOutlined />}>
              进入任务看板
            </Button>
          </Link>
        </div>
      ),
    },
    {
      key: 'stories',
      label: '用户故事',
      children: <Empty description="用户故事功能开发中" />,
    },
    {
      key: 'issues',
      label: '问题追踪',
      children: <Empty description="问题追踪功能开发中" />,
    },
    {
      key: 'wiki',
      label: 'Wiki',
      children: <Empty description="Wiki 功能开发中" />,
    },
    {
      key: 'reports',
      label: '数据报表',
      children: <Empty description="数据报表功能开发中" />,
    },
    {
      key: 'members',
      label: '成员管理',
      children: (
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <h3 className="text-lg font-semibold text-white">项目成员</h3>
            <Button icon={<PlusOutlined />}>添加成员</Button>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {project.members.map((member) => (
              <Card
                key={member.id}
                className="bg-gray-700/30 border-gray-600"
                size="small"
              >
                <div className="flex items-center gap-3">
                  <Avatar
                    size={40}
                    className="bg-gradient-to-br from-orange-400 to-amber-500"
                  >
                    {member.name[0]}
                  </Avatar>
                  <div className="flex-1">
                    <h4 className="text-white font-medium">{member.name}</h4>
                    <Tag color={roleColorMap[member.role]} className="text-xs">
                      {roleTextMap[member.role]}
                    </Tag>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        </div>
      ),
    },
  ];

  const handleDelete = async () => {
    setLoading(true);
    try {
      // 调用删除接口
      await new Promise((resolve) => setTimeout(resolve, 1000));
      message.success('项目删除成功');
      router.push('/projects');
    } catch (error) {
      message.error('删除失败，请稍后重试');
    } finally {
      setLoading(false);
      setDeleteModalOpen(false);
    }
  };

  if (!projectId) {
    return <Empty description="项目 ID 不存在" />;
  }

  return (
    <div className="space-y-6">
      {/* 项目信息卡片 */}
      <Card
        className="bg-gray-800/50 border-gray-700"
        title={
          <div className="flex items-center gap-4">
            <div
              className="w-14 h-14 rounded-xl flex items-center justify-center text-3xl"
              style={{ backgroundColor: `${project.color}20` }}
            >
              {project.icon}
            </div>
            <div>
              <h1 className="text-2xl font-bold text-white">{project.name}</h1>
              <div className="flex items-center gap-2 mt-1">
                <Tag color={statusColorMap[project.status]}>
                  {statusTextMap[project.status]}
                </Tag>
                <span className="text-gray-400 text-sm">
                  <ClockCircleOutlined className="mr-1" />
                  {project.startDate} 至 {project.endDate}
                </span>
              </div>
            </div>
          </div>
        }
        extra={
          <Dropdown menu={{ items: projectMenuItems }} trigger={['click']}>
            <Button size="large" className="text-gray-400">
              项目操作
            </Button>
          </Dropdown>
        }
      >
        <div className="space-y-4">
          <p className="text-gray-300">{project.description}</p>

          <div className="flex items-center gap-8">
            <div>
              <Progress
                type="circle"
                percent={project.progress}
                size={80}
                strokeColor={{ from: '#f97316', to: '#eab308' }}
                trailColor="rgba(255,255,255,0.1)"
                format={(percent) => (
                  <span className="text-white text-sm">{percent}%</span>
                )}
              />
              <p className="text-gray-400 text-sm mt-2 text-center">项目进度</p>
            </div>

            <div className="flex-1 grid grid-cols-3 gap-4">
              <div className="text-center p-4 bg-gray-700/30 rounded-lg">
                <div className="text-2xl font-bold text-orange-400">24</div>
                <div className="text-gray-400 text-sm mt-1">总任务数</div>
              </div>
              <div className="text-center p-4 bg-gray-700/30 rounded-lg">
                <div className="text-2xl font-bold text-green-400">12</div>
                <div className="text-gray-400 text-sm mt-1">已完成</div>
              </div>
              <div className="text-center p-4 bg-gray-700/30 rounded-lg">
                <div className="text-2xl font-bold text-blue-400">8</div>
                <div className="text-gray-400 text-sm mt-1">进行中</div>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-4 pt-4 border-t border-gray-700">
            <span className="text-gray-400">项目成员：</span>
            <div className="flex -space-x-2">
              {project.members.map((member) => (
                <Avatar
                  key={member.id}
                  size={32}
                  className="bg-gray-600 border-2 border-gray-800 cursor-pointer hover:z-10 transition-all"
                >
                  {member.name[0]}
                </Avatar>
              ))}
              <Avatar
                size={32}
                className="bg-gray-700 border-2 border-gray-800 cursor-pointer hover:z-10 transition-all"
                icon={<PlusOutlined />}
                onClick={() => message.info('添加成员功能开发中')}
              />
            </div>
            <span className="text-gray-400 text-sm">{project.members.length} 名成员</span>
          </div>
        </div>
      </Card>

      {/* 页签导航 */}
      <Tabs
        defaultActiveKey="tasks"
        items={tabItems}
        className="project-detail-tabs"
        tabBarStyle={{ borderBottomColor: '#374151' }}
      />

      {/* 删除确认对话框 */}
      <Modal
        title="确认删除"
        open={deleteModalOpen}
        onOk={handleDelete}
        onCancel={() => setDeleteModalOpen(false)}
        okButtonProps={{ danger: true, loading }}
        okText="确认删除"
        cancelText="取消"
      >
        <p className="text-gray-300">
          确定要删除项目 <span className="text-white font-semibold">{project.name}</span> 吗？
        </p>
        <p className="text-red-400 mt-2">
          删除后所有相关数据（任务、文档等）都将被删除，此操作不可恢复。
        </p>
      </Modal>
    </div>
  );
}
