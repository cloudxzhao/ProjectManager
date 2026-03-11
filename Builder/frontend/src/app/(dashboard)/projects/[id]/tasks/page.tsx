'use client';

import { useState } from 'react';
import { useParams } from 'next/navigation';
import { Card, Button, Tag, Avatar, Popover, Drawer, Form, Input, Select, DatePicker, message } from 'antd';
import { PlusOutlined, MoreOutlined, ClockCircleOutlined, FlagOutlined } from '@ant-design/icons';
import { DndContext, DragEndEvent, closestCenter } from '@dnd-kit/core';
import { SortableContext, horizontalListSortingStrategy, useSortable, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';

const { Option } = Select;
const { TextArea } = Input;

// Mock 数据
const initialColumns = [
  { id: 'todo', title: '待办', color: '#6b7280' },
  { id: 'in-progress', title: '进行中', color: '#3b82f6' },
  { id: 'testing', title: '测试中', color: '#f59e0b' },
  { id: 'done', title: '已完成', color: '#10b981' },
];

const initialTasks = [
  { id: '1', title: '完成用户登录模块', priority: 'high', assignee: '张三', dueDate: '2024-03-15', columnId: 'todo', storyPoints: 5 },
  { id: '2', title: '设计数据库架构', priority: 'high', assignee: '李四', dueDate: '2024-03-12', columnId: 'todo', storyPoints: 8 },
  { id: '3', title: '搭建项目脚手架', priority: 'medium', assignee: '王五', dueDate: '2024-03-10', columnId: 'in-progress', storyPoints: 3 },
  { id: '4', title: '实现 API 接口', priority: 'medium', assignee: '赵六', dueDate: '2024-03-14', columnId: 'in-progress', storyPoints: 5 },
  { id: '5', title: '编写单元测试', priority: 'low', assignee: '钱七', dueDate: '2024-03-16', columnId: 'testing', storyPoints: 3 },
  { id: '6', title: '代码审查', priority: 'medium', assignee: '张三', dueDate: '2024-03-11', columnId: 'done', storyPoints: 2 },
  { id: '7', title: '部署到测试环境', priority: 'high', assignee: '李四', dueDate: '2024-03-08', columnId: 'done', storyPoints: 3 },
];

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
  task: typeof initialTasks[0];
  onClick: (task: typeof initialTasks[0]) => void;
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
  task: typeof initialTasks[0];
  onClick: (task: typeof initialTasks[0]) => void;
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
  tasks: typeof initialTasks;
  onTaskClick: (task: typeof initialTasks[0]) => void;
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
  onSubmit: (values: unknown) => void;
  initialColumnId?: string;
}

const TaskFormModal: React.FC<TaskFormModalProps> = ({ open, onClose, onSubmit, initialColumnId }) => {
  const [form] = Form.useForm();

  const handleFinish = (values: unknown) => {
    onSubmit(values);
    form.resetFields();
    onClose();
  };

  return (
    <Drawer
      title="创建任务"
      placement="right"
      width={500}
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
            <Select placeholder="选择负责人" className="bg-gray-700/50 border-gray-600">
              <Option value="张三">张三</Option>
              <Option value="李四">李四</Option>
              <Option value="王五">王五</Option>
              <Option value="赵六">赵六</Option>
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
  const params = useParams();
  const projectId = params.id as string;

  const [columns] = useState(initialColumns);
  const [tasks, setTasks] = useState(initialTasks);
  const [formOpen, setFormOpen] = useState(false);
  const [initialColumnId, setInitialColumnId] = useState<string | undefined>();
  const [selectedTask, setSelectedTask] = useState<typeof initialTasks[0] | null>(null);

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (over) {
      const activeId = active.id as string;
      const overId = over.id as string;

      setTasks((prevTasks) => {
        const activeTask = prevTasks.find((t) => t.id === activeId);
        const overTask = prevTasks.find((t) => t.id === overId);

        if (activeTask && overTask) {
          // 检查是否拖到列上
          const isColumn = columns.some((c) => c.id === overId);

          if (isColumn) {
            // 移动到不同列
            return prevTasks.map((t) =>
              t.id === activeId ? { ...t, columnId: overId } : t
            );
          } else {
            // 在同一列内或不同列移动
            const overTaskColumn = overTask.columnId;
            return prevTasks.map((t) =>
              t.id === activeId ? { ...t, columnId: overTaskColumn } : t
            );
          }
        }

        return prevTasks;
      });

      message.success('任务已移动');
    }
  };

  const handleAddTask = (columnId: string) => {
    setInitialColumnId(columnId);
    setFormOpen(true);
  };

  const handleTaskClick = (task: typeof initialTasks[0]) => {
    setSelectedTask(task);
    message.info(`查看任务详情：${task.title}`);
  };

  const handleCreateTask = (values: unknown) => {
    console.log('创建任务:', values);
    message.success('任务创建成功');
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

      {/* 创建任务表单 */}
      <TaskFormModal
        open={formOpen}
        onClose={() => setFormOpen(false)}
        onSubmit={handleCreateTask}
        initialColumnId={initialColumnId}
      />
    </div>
  );
}
