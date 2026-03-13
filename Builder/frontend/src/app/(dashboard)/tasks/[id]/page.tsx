'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import {
  Card, Tag, Avatar, Button, Input, List, Checkbox, Divider, Dropdown, MenuProps, message, Modal, Form, DatePicker, Select, Spin,
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
import {
  getTask,
  toggleTaskComplete as toggleTaskCompleteApi,
  getSubTasks,
  createSubTask as createSubTaskApi,
  deleteSubTask as deleteSubTaskApi,
  getTaskComments,
  createTaskComment as createTaskCommentApi,
} from '@/lib/api/task';
import type { Task, SubTask, TaskComment } from '@/lib/api/task';

const { Option } = Select;
const { TextArea } = Input;

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

  const [task, setTask] = useState<Task | null>(null);
  const [loading, setLoading] = useState(true);
  const [subtasks, setSubtasks] = useState<SubTask[]>([]);
  const [comments, setComments] = useState<TaskComment[]>([]);
  const [commentForm] = Form.useForm();
  const [subTaskForm] = Form.useForm();
  const [newComment, setNewComment] = useState('');
  const [newSubTask, setNewSubTask] = useState('');

  // 加载任务详情
  useEffect(() => {
    const loadTaskDetail = async () => {
      if (!taskId || isNaN(Number(taskId))) {
        message.error('无效的任务 ID');
        return;
      }
      setLoading(true);
      try {
        // 暂时使用固定的 projectId，后续可以通过任务详情获取
        const projectId = 1;
        const [taskData, subtasksData, commentsData] = await Promise.all([
          getTask(projectId, Number(taskId)),
          getSubTasks(projectId, Number(taskId)),
          getTaskComments(projectId, Number(taskId)),
        ]);
        setTask(taskData);
        setSubtasks(subtasksData);
        setComments(commentsData);
      } catch (error) {
        console.error('加载任务详情失败:', error);
        message.error('加载任务详情失败');
      } finally {
        setLoading(false);
      }
    };
    loadTaskDetail();
  }, [taskId]);

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
  const handleAddSubTask = async () => {
    if (!newSubTask.trim()) {
      message.warning('请输入子任务标题');
      return;
    }
    if (!task) return;

    try {
      const projectId = task.projectId;
      await createSubTaskApi(projectId, task.id, newSubTask);
      // 重新加载子任务列表
      const subtasksData = await getSubTasks(projectId, task.id);
      setSubtasks(subtasksData);
      setNewSubTask('');
      message.success('子任务添加成功');
    } catch (error) {
      console.error('添加子任务失败:', error);
      message.error('添加子任务失败');
    }
  };

  // 切换子任务状态
  const toggleSubTask = async (subtaskId: number) => {
    if (!task) return;
    // 暂时只更新本地状态，后续可以添加 API 调用更新状态
    setSubtasks(subtasks.map((st) =>
      st.id === subtaskId ? { ...st, completed: !st.completed } : st
    ));
  };

  // 删除子任务
  const handleDeleteSubTask = async (subtaskId: number) => {
    if (!task) return;
    try {
      const projectId = task.projectId;
      await deleteSubTaskApi(projectId, task.id, subtaskId);
      // 重新加载子任务列表
      const subtasksData = await getSubTasks(projectId, task.id);
      setSubtasks(subtasksData);
      message.success('子任务已删除');
    } catch (error) {
      console.error('删除子任务失败:', error);
      message.error('删除子任务失败');
    }
  };

  // 发表评论
  const handlePostComment = async () => {
    if (!newComment.trim()) {
      message.warning('请输入评论内容');
      return;
    }
    if (!task) return;

    try {
      const projectId = task.projectId;
      await createTaskCommentApi(projectId, task.id, newComment);
      // 重新加载评论列表
      const commentsData = await getTaskComments(projectId, task.id);
      setComments(commentsData);
      setNewComment('');
      message.success('评论发表成功');
    } catch (error) {
      console.error('发表评论失败:', error);
      message.error('发表评论失败');
    }
  };

  // 计算子任务进度
  const completedSubTasks = subtasks.filter((st) => st.completed).length;
  const subTaskProgress = subtasks.length > 0
    ? Math.round((completedSubTasks / subtasks.length) * 100)
    : 0;

  return (
    <div className="space-y-6">
      {/* 页面导航 */}
      {loading ? (
        <div className="flex justify-center items-center py-20">
          <Spin size="large" />
        </div>
      ) : task ? (
        <>
          <div className="flex items-center gap-4">
            <Link href={`/projects/${task.projectId}/tasks`}>
              <Button type="text" icon={<ArrowLeftOutlined />} className="text-gray-400 hover:text-white">
                返回
              </Button>
            </Link>
            <div className="flex-1">
              <h1 className="text-2xl font-bold text-white">{task.title}</h1>
              <div className="flex items-center gap-2 mt-1 text-sm text-gray-400">
                <span>TASK-{task.id}</span>
                <span>·</span>
                <span>由 {task.reporterId ? `用户 ${task.reporterId}` : '未知'} 创建</span>
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
                <span className="text-gray-400 text-sm">({completedSubTasks}/{subtasks.length})</span>
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
              dataSource={subtasks}
              renderItem={(subtask) => (
                <List.Item
                  className="border-gray-700 hover:bg-gray-700/30 transition-all px-0"
                  actions={[
                    <Button
                      key="delete"
                      type="text"
                      size="small"
                      danger
                      onClick={() => handleDeleteSubTask(subtask.id)}
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
                <span className="text-gray-400 text-sm">({comments.length})</span>
              </div>
            }
          >
            <List
              dataSource={comments}
              renderItem={(comment) => (
                <List.Item className="border-gray-700 py-4">
                  <div className="flex gap-3 w-full">
                    <Avatar
                      size={36}
                      className="bg-gradient-to-br from-orange-400 to-amber-500 flex-shrink-0"
                    >
                      {String(comment.userId)[0]}
                    </Avatar>
                    <div className="flex-1">
                      <div className="flex items-center justify-between mb-1">
                        <span className="font-medium text-white">用户 {comment.userId}</span>
                        <span className="text-gray-500 text-sm">{dayjs(comment.createdAt).format('YYYY-MM-DD HH:mm')}</span>
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
                    {task.assigneeId ? String(task.assigneeId)[0] : '未'}
                  </Avatar>
                  <span className="text-white">{task.assigneeId ? `用户 ${task.assigneeId}` : '未分配'}</span>
                </div>
              </div>
              <div>
                <label className="text-gray-400 text-sm block mb-2">截止日期</label>
                <div className="flex items-center gap-2 text-white">
                  <ClockCircleOutlined />
                  <span>{task.dueDate || '-'}</span>
                </div>
              </div>
            </div>
          </Card>

          {/* 标签 */}
          <Card className="bg-gray-800/50 border-gray-700">
            <label className="text-gray-400 text-sm block mb-2">标签</label>
            <div className="flex flex-wrap gap-2">
              {task.tags && task.tags.length > 0 ? (
                task.tags.map((label) => (
                  <Tag key={label} color="blue" className="cursor-pointer hover:opacity-80">
                    {label}
                  </Tag>
                ))
              ) : (
                <span className="text-gray-500 text-sm">暂无标签</span>
              )}
              <Button size="small" icon={<PlusOutlined />} className="h-6">
                添加
              </Button>
            </div>
          </Card>

          {/* 活动历史 */}
          <Card className="bg-gray-800/50 border-gray-700" title="活动历史">
            <List
              dataSource={[]}
              split={false}
              renderItem={() => null}
              locale={{ emptyText: '暂无活动历史' }}
            />
          </Card>
        </div>
      </div>
      </>
    ) : (
      <div className="flex justify-center items-center py-20">
        <Spin size="large" />
      </div>
    )}
    </div>
  );
}
