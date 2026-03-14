'use client';

import { useState, useEffect, useMemo, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import {
  Card, Button, Tag, Avatar, message, Spin, Select, Input,
} from 'antd';
import { PlusOutlined, ClockCircleOutlined, FlagOutlined, UserOutlined, SearchOutlined, ClearOutlined } from '@ant-design/icons';
import { DndContext, DragEndEvent, closestCenter } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy, useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { searchTasks as searchTasksApi, moveTask as moveTaskApi, type Task, type TaskStatus } from '@/lib/api/task';
import { getProjects } from '@/lib/api/project';
import type { Project } from '@/lib/api/project';

const { Option } = Select;

// 看板列定义
const columns = [
  { id: 'todo', title: '待办', color: '#6b7280' },
  { id: 'in_progress', title: '进行中', color: '#3b82f6' },
  { id: 'testing', title: '测试中', color: '#f59e0b' },
  { id: 'done', title: '已完成', color: '#10b981' },
];

// 任务状态映射（后端状态 -> 看板列 ID）
const statusToColumn: Record<TaskStatus, string> = {
  'todo': 'todo',
  'in_progress': 'in_progress',
  'testing': 'testing',
  'done': 'done',
};

// 看板任务项
interface TaskBoardItem {
  id: string;
  taskId: number;
  projectId: number;
  projectName?: string;
  title: string;
  description?: string;
  priority: string;
  status: TaskStatus;
  assigneeId?: number;
  assigneeName?: string;
  dueDate?: string;
  storyPoints?: number;
  columnId: string;
}

const priorityColors: Record<string, string> = {
  high: 'red',
  medium: 'orange',
  low: 'green',
  urgent: 'purple',
};

const priorityText: Record<string, string> = {
  high: '高',
  medium: '中',
  low: '低',
  urgent: '紧急',
};

// 可排序的任务卡片组件
interface SortableTaskCardProps {
  task: TaskBoardItem;
  onClick: (task: TaskBoardItem) => void;
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
  task: TaskBoardItem;
  onClick: (task: TaskBoardItem) => void;
}

const TaskCard: React.FC<TaskCardProps> = ({ task, onClick }) => {
  return (
    <Card
      hoverable
      size="small"
      onClick={() => onClick(task)}
      className="bg-gray-700/50 border-gray-600 hover:border-orange-500/30 transition-all cursor-pointer mb-3 group"
      bodyStyle={{ padding: '12px' }}
    >
      <div className="flex items-start justify-between mb-2">
        <span className="text-xs text-gray-500">TASK-{task.taskId}</span>
        <Tag color={priorityColors[task.priority]} className="text-xs">
          {priorityText[task.priority]}
        </Tag>
      </div>
      <h4 className="text-white text-sm font-medium mb-2 line-clamp-2">{task.title}</h4>
      {task.projectName && (
        <Tag color="blue" className="text-xs mb-2">
          {task.projectName}
        </Tag>
      )}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3 text-xs text-gray-400">
          {task.dueDate && (
            <span className="flex items-center gap-1">
              <ClockCircleOutlined />
              {task.dueDate.slice(0, 10)}
            </span>
          )}
          {task.storyPoints && (
            <span className="flex items-center gap-1">
              <FlagOutlined />
              {task.storyPoints} pts
            </span>
          )}
        </div>
        {task.assigneeName ? (
          <Avatar size={24} className="bg-gradient-to-br from-purple-400 to-pink-500">
            {task.assigneeName[0]}
          </Avatar>
        ) : (
          <Avatar size={24} className="bg-gray-600">
            <UserOutlined />
          </Avatar>
        )}
      </div>
    </Card>
  );
};

// 可排序的列组件
interface SortableColumnProps {
  column: typeof columns[0];
  tasks: TaskBoardItem[];
  onTaskClick: (task: TaskBoardItem) => void;
}

const SortableColumn: React.FC<SortableColumnProps> = ({ column, tasks, onTaskClick }) => {
  return (
    <div className="flex-shrink-0 w-80">
      <div className="flex items-center justify-between mb-3 px-1">
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full" style={{ backgroundColor: column.color }} />
          <h3 className="text-white font-semibold">{column.title}</h3>
          <span className="text-gray-500 text-sm">({tasks.length})</span>
        </div>
      </div>
      <div
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

export default function TaskBoardPage() {
  const router = useRouter();

  const [loading, setLoading] = useState(true);
  const [projects, setProjects] = useState<Project[]>([]);
  const [tasks, setTasks] = useState<TaskBoardItem[]>([]);

  // 筛选状态
  const [selectedProject, setSelectedProject] = useState<string>('all');
  const [selectedPriority, setSelectedPriority] = useState<string>('all');
  const [searchKeyword, setSearchKeyword] = useState<string>('');
  const [isFiltering, setIsFiltering] = useState(false);

  // 加载任务（使用筛选条件）
  const loadTasks = useCallback(async (projectFilter: string, priorityFilter: string, keyword: string) => {
    setIsFiltering(true);
    try {
      const searchParams: Record<string, any> = {
        page: 1,
        pageSize: 100,
      };

      // 如果选择了特定项目，添加项目筛选
      if (projectFilter !== 'all') {
        searchParams.projectIds = [Number(projectFilter)];
      }

      // 如果选择了优先级，添加优先级筛选
      if (priorityFilter !== 'all') {
        searchParams.priority = priorityFilter.toUpperCase();
      }

      // 如果有关键词，添加关键词筛选
      if (keyword) {
        searchParams.keyword = keyword;
      }

      const tasksResult = await searchTasksApi(searchParams);

      // 将任务数据转换为看板格式，并添加项目名称
      const tasksWithProjectName: TaskBoardItem[] = (tasksResult.list || []).map((task: Task) => {
        const project = projects.find((p: Project) => p.id === task.projectId);
        return {
          id: `task-${task.id}`,
          taskId: task.id,
          projectId: task.projectId,
          projectName: project?.name,
          title: task.title,
          description: task.description,
          status: task.status,
          priority: task.priority,
          assigneeId: task.assigneeId,
          assigneeName: task.assigneeId?.toString(),
          dueDate: task.dueDate,
          storyPoints: task.storyPoints,
          columnId: statusToColumn[task.status] || 'todo',
        };
      });

      setTasks(tasksWithProjectName);
    } catch (error) {
      console.error('加载任务失败:', error);
      message.error('加载任务失败');
    } finally {
      setIsFiltering(false);
    }
  }, [projects]);

  // 加载项目和任务
  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      try {
        // 获取项目列表
        const projectsData = await getProjects(1, 100);
        setProjects(projectsData.list || []);
      } catch (error) {
        console.error('加载项目失败:', error);
        message.error('加载项目失败');
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  // 筛选条件变化时重新加载任务
  useEffect(() => {
    if (projects.length > 0) {
      loadTasks(selectedProject, selectedPriority, searchKeyword);
    }
  }, [selectedProject, selectedPriority, searchKeyword, loadTasks, projects.length]);

  // 筛选后的任务（本地不再过滤，直接使用 API 返回的结果）
  const filteredTasks = tasks;

  // 清空筛选
  const handleClearFilters = () => {
    setSelectedProject('all');
    setSelectedPriority('all');
    setSearchKeyword('');
  };

  // 处理拖拽结束
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

      // 如果状态没有变化，不调用 API
      if (newColumnId === activeTask.columnId) {
        return;
      }

      // 更新本地状态（乐观更新）
      setTasks((prevTasks) =>
        prevTasks.map((t) =>
          t.id === activeId ? { ...t, columnId: newColumnId } : t
        )
      );

      // 调用 API 更新任务状态
      try {
        const newStatus = newColumnId as TaskStatus;
        await moveTaskApi(activeTask.projectId, activeTask.taskId, newStatus);
        message.success('任务状态已更新');
      } catch (error) {
        console.error('移动任务失败:', error);
        message.error('移动任务失败');
        // 回滚本地状态
        setTasks((prevTasks) =>
          prevTasks.map((t) =>
            t.id === activeId ? { ...t, columnId: activeTask.columnId } : t
          )
        );
      }
    }
  };

  // 处理任务点击
  const handleTaskClick = (task: TaskBoardItem) => {
    router.push(`/tasks/${task.taskId}`);
  };

  return (
    <div className="space-y-4">
      {/* 页面头部 - 筛选区域 */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-white">任务看板</h1>
            <p className="text-gray-400 text-sm mt-1">查看所有项目的任务，拖拽卡片更新状态</p>
          </div>
        </div>

        {/* 筛选工具栏 */}
        <div className="flex items-center gap-3 flex-wrap">
          {/* 关键词搜索 */}
          <Input
            placeholder="搜索任务标题..."
            prefix={<SearchOutlined className="text-gray-400" />}
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            allowClear
            className="w-64 bg-gray-800/50 border-gray-700 text-white placeholder-gray-500"
          />

          {/* 项目筛选 */}
          <Select
            value={selectedProject}
            onChange={setSelectedProject}
            className="w-40"
            placeholder="选择项目"
            allowClear
            dropdownClassName="bg-gray-800 border-gray-700"
          >
            <Option value="all">全部项目</Option>
            {projects.map((project) => (
              <Option key={project.id} value={project.id}>
                {project.name}
              </Option>
            ))}
          </Select>

          {/* 优先级筛选 */}
          <Select
            value={selectedPriority}
            onChange={setSelectedPriority}
            className="w-32"
            placeholder="优先级"
            allowClear
            dropdownClassName="bg-gray-800 border-gray-700"
          >
            <Option value="all">全部优先级</Option>
            <Option value="urgent">紧急</Option>
            <Option value="high">高</Option>
            <Option value="medium">中</Option>
            <Option value="low">低</Option>
          </Select>

          {/* 清空筛选按钮 */}
          {(selectedProject !== 'all' || selectedPriority !== 'all' || searchKeyword) && (
            <Button
              icon={<ClearOutlined />}
              onClick={handleClearFilters}
              className="flex items-center gap-1"
            >
              清空筛选
            </Button>
          )}

          {/* 筛选结果统计 */}
          <span className="text-gray-400 text-sm ml-auto">
            显示 <span className="text-orange-400 font-medium">{filteredTasks.length}</span> 个任务
          </span>
        </div>
      </div>

      {/* 加载状态 */}
      {loading ? (
        <div className="flex items-center justify-center py-16">
          <Spin size="large" tip="加载中..." />
        </div>
      ) : (
        <>
          {/* 看板 */}
          <div className="overflow-x-auto pb-4">
            <DndContext collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
              <div className="flex gap-4">
                {columns.map((column) => {
                  const columnTasks = filteredTasks.filter((t) => t.columnId === column.id);
                  return (
                    <SortableColumn
                      key={column.id}
                      column={column}
                      tasks={columnTasks}
                      onTaskClick={handleTaskClick}
                    />
                  );
                })}
              </div>
            </DndContext>
          </div>
        </>
      )}
    </div>
  );
}
