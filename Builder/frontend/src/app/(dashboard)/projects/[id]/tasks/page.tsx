'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Card, Button, Tag, Avatar, Popover, Drawer, Form, Input, Select, DatePicker, message, Spin } from 'antd';
import { PlusOutlined, MoreOutlined, ClockCircleOutlined, FlagOutlined } from '@ant-design/icons';
import { DndContext, DragEndEvent, closestCenter } from '@dnd-kit/core';
import { SortableContext, horizontalListSortingStrategy, useSortable, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { getTasksForBoard, createTask as createTaskApi, moveTask, deleteTask } from '@/lib/api/task';
import { getProjectMembers, type ProjectMemberResponse } from '@/lib/api/project';
import { useAuth } from '@/lib/hooks/useAuth';
import type { TaskBoardItem, TaskStatus, Priority } from '@/lib/api/task';

const { Option } = Select;
const { TextArea } = Input;

// 看板列定义
const initialColumns = [
  { id: 'todo', title: '待办', color: '#6b7280' },
  { id: 'in-progress', title: '进行中', color: '#3b82f6' },
  { id: 'testing', title: '测试中', color: '#f59e0b' },
  { id: 'done', title: '已完成', color: '#10b981' },
];

// 前端看板任务类型
interface TaskItem {
  id: string;
  title: string;
  priority: Priority;
  assignee?: string;
  dueDate?: string;
  columnId: string;
  storyPoints?: number;
  taskId: number;
  projectId: number;
}

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

// 可排序的任务卡片组件
interface SortableTaskCardProps {
  task: TaskItem;
  onClick: (task: TaskItem) => void;
}

const SortableTaskCard: React.FC<SortableTaskCardProps> = ({ task, onClick }) => {
  const { attributes, listeners, setNodeRef, transform, transition } = useSortable({ id: task.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  return (
    <div ref={setNodeRef} style={style} {...attributes} {...listeners}>
      <TaskCard task={task} onClick={onClick} />
    </div>
  );
};

// 任务卡片组件
interface TaskCardProps {
  task: TaskItem;
  onClick: (task: TaskItem) => void;
}

const TaskCard: React.FC<TaskCardProps> = ({ task, onClick }) => {
  return (
    <Card
      hoverable
      size="small"
      onClick={() => onClick(task)}
      className="bg-gray-700/50 border-gray-600 hover:border-orange-500/30 transition-all cursor-pointer mb-3"
      bodyStyle={{ padding: '12px' }}
    >
      <div className="flex items-start justify-between mb-2">
        <span className="text-xs text-gray-500">TASK-{task.id}</span>
        <Tag color={priorityColors[task.priority]} className="text-xs">
          {priorityText[task.priority]}
        </Tag>
      </div>
      <h4 className="text-white text-sm font-medium mb-3 line-clamp-2">{task.title}</h4>
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3 text-xs text-gray-400">
          <span className="flex items-center gap-1">
            <ClockCircleOutlined />
            {task.dueDate}
          </span>
          <span className="flex items-center gap-1">
            <FlagOutlined />
            {task.storyPoints} pts
          </span>
        </div>
        <Avatar size={24} className="bg-gray-600">
          {task.assignee?.[0]}
        </Avatar>
      </div>
    </Card>
  );
};

// 可排序的列组件
interface SortableColumnProps {
  column: typeof initialColumns[0];
  tasks: TaskItem[];
  onTaskClick: (task: TaskItem) => void;
  onAddTask: (columnId: string) => void;
}

const SortableColumn: React.FC<SortableColumnProps> = ({ column, tasks, onTaskClick, onAddTask }) => {
  const { setNodeRef } = useSortable({ id: column.id });

  return (
    <div className="flex-shrink-0 w-80">
      <div className="flex items-center justify-between mb-3 px-1">
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full" style={{ backgroundColor: column.color }} />
          <h3 className="text-white font-semibold">{column.title}</h3>
          <span className="text-gray-500 text-sm">({tasks.length})</span>
        </div>
        <Button type="text" size="small" icon={<PlusOutlined />} onClick={() => onAddTask(column.id)} />
      </div>
      <div
        ref={setNodeRef}
        className="min-h-[500px] bg-gray-800/30 rounded-lg p-3"
      >
        <SortableContext items={tasks.map((t) => t.id)} strategy={verticalListSortingStrategy}>
          {tasks.map((task) => (
            <SortableTaskCard key={task.id} task={task} onClick={onTaskClick} />
          ))}
        </SortableContext>
      </div>
    </div>
  );
};

// 新建/编辑任务表单
interface TaskFormModalProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (values: { title: string; description?: string; priority?: Priority; storyPoints?: number; assignee?: string; dueDate?: string }) => void;
  initialColumnId?: string;
  projectMembers: ProjectMemberResponse[];
  currentUserId?: number;
}

const TaskFormModal: React.FC<TaskFormModalProps> = ({ open, onClose, onSubmit, initialColumnId, projectMembers, currentUserId }) => {
  const [form] = Form.useForm();

  // 当对话框打开且项目成员加载完成后，自动设置当前用户为默认负责人
  useEffect(() => {
    if (open && currentUserId && projectMembers.length > 0) {
      const isMember = projectMembers.some((m) => m.userId === currentUserId);
      if (isMember) {
        form.setFieldValue('assignee', currentUserId);
      }
    }
  }, [open, currentUserId, projectMembers, form]);

  const handleFinish = (values: { title: string; description?: string; priority?: Priority; storyPoints?: number; assignee?: string; dueDate?: string }) => {
    onSubmit(values);
    form.resetFields();
    onClose();
  };

  return (
    <Drawer
      title="创建任务"
      placement="right"
      size="large"
      open={open}
      onClose={onClose}
      className="bg-gray-800"
    >
      <Form
        form={form}
        layout="vertical"
        requiredMark={false}
        onFinish={handleFinish}
        size="large"
        initialValues={{
          priority: 'medium',
          columnId: initialColumnId,
        }}
      >
        <Form.Item
          name="title"
          label="任务标题"
          rules={[{ required: true, message: '请输入任务标题' }]}
        >
          <Input placeholder="请输入任务标题" className="bg-gray-700/50 border-gray-600 text-white" />
        </Form.Item>

        <Form.Item
          name="description"
          label="任务描述"
        >
          <TextArea
            rows={4}
            placeholder="请输入任务描述"
            className="bg-gray-700/50 border-gray-600 text-white"
          />
        </Form.Item>

        <div className="grid grid-cols-2 gap-4">
          <Form.Item
            name="priority"
            label="优先级"
          >
            <Select className="bg-gray-700/50 border-gray-600">
              <Option value="high">高</Option>
              <Option value="medium">中</Option>
              <Option value="low">低</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="storyPoints"
            label="故事点"
          >
            <Select className="bg-gray-700/50 border-gray-600">
              <Option value={1}>1</Option>
              <Option value={2}>2</Option>
              <Option value={3}>3</Option>
              <Option value={5}>5</Option>
              <Option value={8}>8</Option>
              <Option value={13}>13</Option>
            </Select>
          </Form.Item>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Form.Item
            name="assignee"
            label="负责人"
          >
            <Select
              placeholder="选择负责人"
              className="bg-gray-700/50 border-gray-600"
              showSearch
              filterOption={(input, option) =>
                String(option?.label ?? '').toLowerCase().includes(input.toLowerCase())
              }
            >
              {projectMembers.length > 0 ? (
                projectMembers.map((member) => (
                  <Option
                    key={member.userId}
                    value={member.userId}
                    label={`${member.nickname || member.username} (${member.email})`}
                  >
                    {member.nickname || member.username} ({member.email})
                  </Option>
                ))
              ) : (
                <Option value="" disabled>暂无成员</Option>
              )}
            </Select>
          </Form.Item>

          <Form.Item
            name="dueDate"
            label="截止日期"
          >
            <DatePicker className="w-full bg-gray-700/50 border-gray-600 text-white" format="YYYY-MM-DD" />
          </Form.Item>
        </div>

        <Form.Item>
          <div className="flex gap-4 pt-4">
            <Button type="primary" htmlType="submit" className="flex-1 bg-gradient-to-r from-orange-500 to-orange-600">
              创建任务
            </Button>
            <Button onClick={onClose} className="flex-1">
              取消
            </Button>
          </div>
        </Form.Item>
      </Form>
    </Drawer>
  );
};

export default function TaskBoardPage() {
  const { user } = useAuth();
  const params = useParams();
  const router = useRouter();
  const projectIdNum = Number(params.id);

  const [columns] = useState(initialColumns);
  const [tasks, setTasks] = useState<TaskItem[]>([]);
  const [projectMembers, setProjectMembers] = useState<ProjectMemberResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [formOpen, setFormOpen] = useState(false);
  const [initialColumnId, setInitialColumnId] = useState<string | undefined>();
  const [selectedTask, setSelectedTask] = useState<TaskItem | null>(null);

  // 加载项目成员列表
  const loadProjectMembers = async () => {
    try {
      const members = await getProjectMembers(projectIdNum);
      setProjectMembers(members || []);
    } catch (error) {
      console.error('加载项目成员失败:', error);
    }
  };

  // 加载任务列表
  const loadTasks = async () => {
    try {
      setLoading(true);
      const taskList = await getTasksForBoard(projectIdNum);
      setTasks(taskList);
    } catch (error) {
      console.error('加载任务失败:', error);
      message.error('加载任务失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (projectIdNum) {
      loadProjectMembers();
      loadTasks();
    }
  }, [projectIdNum]);

  // 状态到列的映射
  const statusToColumn: Record<string, TaskStatus> = {
    'todo': 'todo',
    'in-progress': 'in_progress',
    'testing': 'testing',
    'done': 'done',
  };

  // 列到状态的映射
  const columnToStatus: Record<string, string> = {
    'todo': 'todo',
    'in-progress': 'in_progress',
    'testing': 'testing',
    'done': 'done',
  };

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;

    if (over) {
      const activeId = active.id as string;
      const overId = over.id as string;

      const activeTask = tasks.find((t) => t.id === activeId);
      if (!activeTask) return;

      // 检查是否拖到列上
      const targetColumn = columns.find((c) => c.id === overId);
      let newColumnId = activeTask.columnId;

      if (targetColumn) {
        newColumnId = targetColumn.id;
      } else {
        const overTask = tasks.find((t) => t.id === overId);
        if (overTask) {
          newColumnId = overTask.columnId;
        }
      }

      // 如果状态没有变化，不调用API
      if (newColumnId === activeTask.columnId) {
        return;
      }

      // 更新本地状态（乐观更新）
      setTasks((prevTasks) =>
        prevTasks.map((t) =>
          t.id === activeId ? { ...t, columnId: newColumnId } : t
        )
      );

      // 调用API更新任务状态
      try {
        const newStatus = statusToColumn[newColumnId] || 'todo';
        await moveTask(projectIdNum, activeTask.taskId, newStatus);
        message.success('任务已移动');
      } catch (error) {
        console.error('移动任务失败:', error);
        message.error('移动任务失败');
        // 回滚本地状态
        loadTasks();
      }
    }
  };

  const handleAddTask = (columnId: string) => {
    setInitialColumnId(columnId);
    setFormOpen(true);
  };

  const handleTaskClick = (task: TaskItem) => {
    setSelectedTask(task);
    router.push(`/tasks/${task.taskId}`);
  };

  const handleCreateTask = async (values: { title: string; description?: string; priority?: Priority; storyPoints?: number; assignee?: string; dueDate?: string }) => {
    try {
      // 将列ID转换为状态
      const columnId = initialColumnId || 'todo';
      const status = columnToStatus[columnId] || 'todo';

      await createTaskApi(projectIdNum, {
        title: values.title,
        description: values.description,
        priority: values.priority || 'medium',
        storyPoints: values.storyPoints,
        dueDate: values.dueDate,
        status: status as TaskStatus,
      });

      message.success('任务创建成功');
      setFormOpen(false);
      loadTasks();
    } catch (error) {
      console.error('创建任务失败:', error);
      message.error('创建任务失败');
    }
  };

  return (
    <div className="space-y-4">
      {/* 页面标题 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">任务看板</h1>
          <p className="text-gray-400 mt-1">拖拽任务卡片来更新状态</p>
        </div>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => handleAddTask('todo')}
          className="bg-gradient-to-r from-orange-500 to-orange-600 border-none"
        >
          创建任务
        </Button>
      </div>

      {/* 加载状态 */}
      {loading ? (
        <div className="flex justify-center items-center py-20">
          <Spin size="large" />
        </div>
      ) : (
        <>
          {/* Kanban 看板 */}
          <div className="overflow-x-auto pb-4">
            <DndContext collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
              <div className="flex gap-4">
                {columns.map((column) => {
                  const columnTasks = tasks.filter((t) => t.columnId === column.id);
                  return (
                    <SortableColumn
                      key={column.id}
                      column={column}
                      tasks={columnTasks}
                      onTaskClick={handleTaskClick}
                      onAddTask={handleAddTask}
                    />
                  );
                })}
              </div>
            </DndContext>
          </div>
        </>
      )}

      {/* 创建任务表单 */}
      <TaskFormModal
        open={formOpen}
        onClose={() => setFormOpen(false)}
        onSubmit={handleCreateTask}
        initialColumnId={initialColumnId}
        projectMembers={projectMembers}
        currentUserId={user?.id}
      />
    </div>
  );
}
