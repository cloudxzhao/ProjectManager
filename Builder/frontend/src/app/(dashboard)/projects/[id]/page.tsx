'use client';

import { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Card, Tabs, TabsProps, Avatar, Tag, Progress, Button, Empty, Dropdown, MenuProps, message, Modal, Form, Input, DatePicker, Select, ColorPicker, Drawer } from 'antd';
import {
  EditOutlined,
  DeleteOutlined,
  PlusOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  ProjectOutlined,
  CloseOutlined,
  UserAddOutlined,
} from '@ant-design/icons';
import Link from 'next/link';
import dayjs from 'dayjs';
import { api } from '@/lib/api/axios';
import { endpoints } from '@/lib/api/endpoints';

const { Option } = Select;
const { TextArea } = Input;

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

const projectIcons = ['🛒', '📱', '📊', '🤝', '🌐', '🔧', '💼', '🎯', '🚀', '💡'];

interface EditProjectFormValues {
  name: string;
  description: string;
  status: string;
  startDate?: dayjs.Dayjs;
  endDate?: dayjs.Dayjs;
  color?: string;
  icon?: string;
}

export default function ProjectDetailPage() {
  const params = useParams();
  const router = useRouter();
  const projectId = params.id as string;

  const [loading, setLoading] = useState(false);
  const [project, setProject] = useState(mockProject);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [editDrawerOpen, setEditDrawerOpen] = useState(false);
  const [selectedIcon, setSelectedIcon] = useState(project.icon);
  const [form] = Form.useForm();

  // 项目操作菜单
  const projectMenuItems: MenuProps['items'] = [
    {
      key: 'edit',
      icon: <EditOutlined />,
      label: '编辑项目',
      onClick: () => {
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
        setSelectedIcon(project.icon);
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
      await api.put(endpoints.project.update(Number(projectId)), {
        name: values.name,
        description: values.description,
        status: values.status,
        startDate: values.startDate?.format('YYYY-MM-DD'),
        endDate: values.endDate?.format('YYYY-MM-DD'),
        color: values.color,
        icon: values.icon || selectedIcon,
      });

      // 更新本地状态
      setProject({
        ...project,
        name: values.name,
        description: values.description,
        status: values.status,
        startDate: values.startDate?.format('YYYY-MM-DD') || project.startDate,
        endDate: values.endDate?.format('YYYY-MM-DD') || project.endDate,
        color: values.color || project.color,
        icon: values.icon || selectedIcon,
      });

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
      await api.delete(endpoints.project.delete(Number(projectId)));
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
            <Button size="large" icon={<CloseOutlined />} className="text-gray-400">
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
        className="bg-gray-900"
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
                  className={`w-12 h-12 text-2xl rounded-lg flex items-center justify-center transition-all ${
                    selectedIcon === icon
                      ? 'bg-orange-500 ring-2 ring-orange-400'
                      : 'bg-gray-700/50 hover:bg-gray-600'
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
