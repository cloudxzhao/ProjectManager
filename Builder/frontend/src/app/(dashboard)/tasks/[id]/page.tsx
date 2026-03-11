'use client';

import { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import {
  Card, Tag, Avatar, Button, Input, List, Checkbox, Divider, Dropdown, MenuProps, message, Modal, Form, DatePicker, Select,
} from 'antd';
import {
  ArrowLeftOutlined,
  MoreOutlined,
  EditOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  FlagOutlined,
  UserOutlined,
  MessageOutlined,
  PaperClipOutlined,
  PlusOutlined,
  SendOutlined,
} from '@ant-design/icons';
import Link from 'next/link';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;

// Mock 数据
const mockTask = {
  id: '1',
  title: '完成用户登录模块',
  description: '## 任务描述\n\n实现用户登录功能，包括：\n- 邮箱密码输入\n- 表单验证\n- Token 存储\n- 错误处理\n\n## 技术要求\n\n- 使用 React Hook Form\n- Zod 验证\n- Axios 请求',
  status: 'in-progress',
  priority: 'high',
  assignee: { id: '1', name: '张三', avatar: null },
  reporter: { id: '2', name: '李四', avatar: null },
  dueDate: '2024-03-15',
  storyPoints: 5,
  labels: ['前端', '认证模块'],
  subtasks: [
    { id: '1', title: '设计登录表单 UI', completed: true },
    { id: '2', title: '实现表单验证逻辑', completed: true },
    { id: '3', title: '调用登录 API', completed: false },
    { id: '4', title: '处理错误状态', completed: false },
  ],
  comments: [
    {
      id: '1',
      author: { name: '李四', avatar: null },
      content: '这个任务优先级比较高，希望能在本周内完成。',
      createdAt: '2024-03-10 10:30',
    },
    {
      id: '2',
      author: { name: '张三', avatar: null },
      content: '好的，我正在处理中，预计明天可以完成。',
      createdAt: '2024-03-10 14:20',
    },
  ],
  activities: [
    { id: '1', user: '张三', action: '将状态改为进行中', time: '2024-03-09 09:00' },
    { id: '2', user: '李四', action: '指派给张三', time: '2024-03-08 15:00' },
    { id: '3', user: '李四', action: '创建了任务', time: '2024-03-08 14:30' },
  ],
};

const statusColorMap: Record<string, string> = {
  todo: 'default',
  'in-progress': 'blue',
  testing: 'orange',
  done: 'green',
};

const statusTextMap: Record<string, string> = {
  todo: '待办',
  'in-progress': '进行中',
  testing: '测试中',
  done: '已完成',
};

const priorityColors: Record<string, string> = {
  high: 'red',
  medium: 'orange',
  low: 'green',
};

const priorityText: Record<string, string> = {
  high: '高',
  medium: '中',
  low: '低',
};

interface CommentFormValues {
  content: string;
}

interface SubTaskFormValues {
  title: string;
}

export default function TaskDetailPage() {
  const params = useParams();
  const router = useRouter();
  const taskId = params.id as string;

  const [task, setTask] = useState(mockTask);
  const [loading, setLoading] = useState(false);
  const [commentForm] = Form.useForm();
  const [subTaskForm] = Form.useForm();
  const [newComment, setNewComment] = useState('');
  const [newSubTask, setNewSubTask] = useState('');

  // 任务操作菜单
  const taskMenuItems: MenuProps['items'] = [
    {
      key: 'edit',
      icon: <EditOutlined />,
      label: '编辑任务',
      onClick: () => message.info('编辑功能开发中'),
    },
    {
      key: 'delete',
      icon: <DeleteOutlined className="text-red-500" />,
      label: <span className="text-red-500">删除任务</span>,
      onClick: () => message.info('删除功能开发中'),
      danger: true,
    },
  ];

  // 添加子任务
  const handleAddSubTask = () => {
    if (!newSubTask.trim()) {
      message.warning('请输入子任务标题');
      return;
    }

    const subtask = {
      id: String(task.subtasks.length + 1),
      title: newSubTask,
      completed: false,
    };

    setTask({
      ...task,
      subtasks: [...task.subtasks, subtask],
    });
    setNewSubTask('');
    message.success('子任务添加成功');
  };

  // 切换子任务状态
  const toggleSubTask = (subtaskId: string) => {
    setTask({
      ...task,
      subtasks: task.subtasks.map((st) =>
        st.id === subtaskId ? { ...st, completed: !st.completed } : st
      ),
    });
  };

  // 删除子任务
  const deleteSubTask = (subtaskId: string) => {
    setTask({
      ...task,
      subtasks: task.subtasks.filter((st) => st.id !== subtaskId),
    });
    message.success('子任务已删除');
  };

  // 发表评论
  const handlePostComment = () => {
    if (!newComment.trim()) {
      message.warning('请输入评论内容');
      return;
    }

    const comment = {
      id: String(task.comments.length + 1),
      author: { name: '当前用户', avatar: null },
      content: newComment,
      createdAt: '刚刚',
    };

    setTask({
      ...task,
      comments: [...task.comments, comment],
    });
    setNewComment('');
    message.success('评论发表成功');
  };

  // 计算子任务进度
  const completedSubTasks = task.subtasks.filter((st) => st.completed).length;
  const subTaskProgress = task.subtasks.length > 0
    ? Math.round((completedSubTasks / task.subtasks.length) * 100)
    : 0;

  return (
    <div className="space-y-6">
      {/* 页面导航 */}
      <div className="flex items-center gap-4">
        <Link href={`/projects/${taskId}/tasks`}>
          <Button type="text" icon={<ArrowLeftOutlined />} className="text-gray-400 hover:text-white">
            返回
          </Button>
        </Link>
        <div className="flex-1">
          <h1 className="text-2xl font-bold text-white">{task.title}</h1>
          <div className="flex items-center gap-2 mt-1 text-sm text-gray-400">
            <span>TASK-{taskId}</span>
            <span>·</span>
            <span>由 {task.reporter.name} 创建</span>
          </div>
        </div>
        <Dropdown menu={{ items: taskMenuItems }} trigger={['click']}>
          <Button icon={<MoreOutlined />} className="text-gray-400" />
        </Dropdown>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 左侧主要内容 */}
        <div className="lg:col-span-2 space-y-6">
          {/* 任务描述 */}
          <Card className="bg-gray-800/50 border-gray-700" title="任务描述">
            <div className="prose prose-invert max-w-none">
              {task.description.split('\n').map((line, index) => {
                if (line.startsWith('## ')) {
                  return <h2 key={index} className="text-lg font-bold text-white mt-4 mb-2">{line.replace('## ', '')}</h2>;
                }
                if (line.startsWith('- ')) {
                  return <li key={index} className="text-gray-300 ml-4">{line.replace('- ', '')}</li>;
                }
                if (line.trim()) {
                  return <p key={index} className="text-gray-300">{line}</p>;
                }
                return <br key={index} />;
              })}
            </div>
          </Card>

          {/* 子任务 */}
          <Card
            className="bg-gray-800/50 border-gray-700"
            title={
              <div className="flex items-center gap-2">
                <CheckCircleOutlined />
                <span>子任务</span>
                <span className="text-gray-400 text-sm">({completedSubTasks}/{task.subtasks.length})</span>
              </div>
            }
          >
            <div className="mb-4">
              <div className="flex gap-2">
                <Input
                  placeholder="添加子任务..."
                  value={newSubTask}
                  onChange={(e) => setNewSubTask(e.target.value)}
                  onPressEnter={handleAddSubTask}
                  className="bg-gray-700/50 border-gray-600 text-white"
                  suffix={
                    <Button type="primary" icon={<PlusOutlined />} onClick={handleAddSubTask}>
                      添加
                    </Button>
                  }
                />
              </div>
            </div>
            <List
              dataSource={task.subtasks}
              renderItem={(subtask) => (
                <List.Item
                  className="border-gray-700 hover:bg-gray-700/30 transition-all px-0"
                  actions={[
                    <Button
                      key="delete"
                      type="text"
                      size="small"
                      danger
                      onClick={() => deleteSubTask(subtask.id)}
                    >
                      删除
                    </Button>,
                  ]}
                >
                  <Checkbox
                    checked={subtask.completed}
                    onChange={() => toggleSubTask(subtask.id)}
                    className={subtask.completed ? 'text-gray-500 line-through' : 'text-gray-300'}
                  >
                    {subtask.title}
                  </Checkbox>
                </List.Item>
              )}
            />
          </Card>

          {/* 评论区 */}
          <Card
            className="bg-gray-800/50 border-gray-700"
            title={
              <div className="flex items-center gap-2">
                <MessageOutlined />
                <span>评论</span>
                <span className="text-gray-400 text-sm">({task.comments.length})</span>
              </div>
            }
          >
            <List
              dataSource={task.comments}
              renderItem={(comment) => (
                <List.Item className="border-gray-700 py-4">
                  <div className="flex gap-3 w-full">
                    <Avatar
                      size={36}
                      className="bg-gradient-to-br from-orange-400 to-amber-500 flex-shrink-0"
                    >
                      {comment.author.name[0]}
                    </Avatar>
                    <div className="flex-1">
                      <div className="flex items-center justify-between mb-1">
                        <span className="font-medium text-white">{comment.author.name}</span>
                        <span className="text-gray-500 text-sm">{comment.createdAt}</span>
                      </div>
                      <p className="text-gray-300">{comment.content}</p>
                    </div>
                  </div>
                </List.Item>
              )}
            />

            <Divider className="border-gray-700" />

            <div className="flex gap-3">
              <Avatar
                size={36}
                className="bg-gradient-to-br from-orange-400 to-amber-500 flex-shrink-0"
              >
                <UserOutlined />
              </Avatar>
              <div className="flex-1 flex gap-2">
                <TextArea
                  placeholder="写下你的评论..."
                  value={newComment}
                  onChange={(e) => setNewComment(e.target.value)}
                  rows={2}
                  className="bg-gray-700/50 border-gray-600 text-white resize-none"
                />
                <Button
                  type="primary"
                  icon={<SendOutlined />}
                  onClick={handlePostComment}
                  className="bg-gradient-to-r from-orange-500 to-orange-600 border-none self-end"
                >
                  发表
                </Button>
              </div>
            </div>
          </Card>
        </div>

        {/* 右侧信息栏 */}
        <div className="space-y-4">
          {/* 状态和优先级 */}
          <Card className="bg-gray-800/50 border-gray-700">
            <div className="space-y-4">
              <div>
                <label className="text-gray-400 text-sm block mb-2">状态</label>
                <Tag color={statusColorMap[task.status]} className="text-base px-3 py-1">
                  {statusTextMap[task.status]}
                </Tag>
              </div>
              <div>
                <label className="text-gray-400 text-sm block mb-2">优先级</label>
                <Tag color={priorityColors[task.priority]} className="text-base px-3 py-1">
                  {priorityText[task.priority]}优先级
                </Tag>
              </div>
              <div>
                <label className="text-gray-400 text-sm block mb-2">故事点</label>
                <div className="flex items-center gap-2 text-white">
                  <FlagOutlined />
                  <span>{task.storyPoints} 点</span>
                </div>
              </div>
            </div>
          </Card>

          {/* 负责人 */}
          <Card className="bg-gray-800/50 border-gray-700">
            <div className="space-y-4">
              <div>
                <label className="text-gray-400 text-sm block mb-2">负责人</label>
                <div className="flex items-center gap-2">
                  <Avatar
                    size={32}
                    className="bg-gradient-to-br from-orange-400 to-amber-500"
                    icon={<UserOutlined />}
                  >
                    {task.assignee?.name[0]}
                  </Avatar>
                  <span className="text-white">{task.assignee?.name || '未分配'}</span>
                </div>
              </div>
              <div>
                <label className="text-gray-400 text-sm block mb-2">截止日期</label>
                <div className="flex items-center gap-2 text-white">
                  <ClockCircleOutlined />
                  <span>{task.dueDate}</span>
                </div>
              </div>
            </div>
          </Card>

          {/* 标签 */}
          <Card className="bg-gray-800/50 border-gray-700">
            <label className="text-gray-400 text-sm block mb-2">标签</label>
            <div className="flex flex-wrap gap-2">
              {task.labels.map((label) => (
                <Tag key={label} color="blue" className="cursor-pointer hover:opacity-80">
                  {label}
                </Tag>
              ))}
              <Button size="small" icon={<PlusOutlined />} className="h-6">
                添加
              </Button>
            </div>
          </Card>

          {/* 活动历史 */}
          <Card className="bg-gray-800/50 border-gray-700" title="活动历史">
            <List
              dataSource={task.activities}
              split={false}
              renderItem={(activity) => (
                <List.Item className="border-gray-700 py-2">
                  <div className="text-sm">
                    <span className="text-white font-medium">{activity.user}</span>{' '}
                    <span className="text-gray-400">{activity.action}</span>
                    <div className="text-gray-500 text-xs mt-1">{activity.time}</div>
                  </div>
                </List.Item>
              )}
            />
          </Card>
        </div>
      </div>
    </div>
  );
}
