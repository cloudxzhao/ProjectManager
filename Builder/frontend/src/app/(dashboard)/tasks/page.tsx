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
import { searchTasks as searchTasksApi, moveTask as moveTaskApi, type Task, type TaskStatus, type Priority } from '@/lib/api/task';
import { getAuthorizedProjects, getProjectMembers } from '@/lib/api/project';
import type { Project } from '@/lib/api/project';
import type { ProjectMemberResponse } from '@/lib/api/project';

const { Option } = Select;

// 状态文本映射
const statusTextMap: Record<string, string> = {
  todo: '待办',
  in_progress: '进行中',
  testing: '测试中',
  done: '已完成',
};

// 优先级文本映射
const priorityTextMap: Record<string, string> = {
  low: '低',
  medium: '中',
  high: '高',
  urgent: '紧急',
};

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
        data-column-id={column.id}
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
  const [allProjectMembers, setAllProjectMembers] = useState<Map<number, ProjectMemberResponse[]>>(new Map());  // 所有项目的成员缓存
  const [tasks, setTasks] = useState<TaskBoardItem[]>([]);

  // 筛选状态
  const [selectedProjectIds, setSelectedProjectIds] = useState<number[]>([]);  // 支持多选项目
  const [selectedStatus, setSelectedStatus] = useState<TaskStatus | undefined>(undefined);
  const [selectedPriority, setSelectedPriority] = useState<Priority | undefined>(undefined);
  const [selectedAssigneeId, setSelectedAssigneeId] = useState<number | undefined>(undefined);  // 责任人筛选
  const [searchKeyword, setSearchKeyword] = useState<string>('');
  const [isFiltering, setIsFiltering] = useState(false);

  // 加载项目成员列表
  const fetchProjectMembers = async (projectId: number) => {
    try {
      const members = await getProjectMembers(projectId);
      setAllProjectMembers((prev) => new Map(prev).set(projectId, members || []));
    } catch (error) {
      console.error('加载项目成员失败:', error);
    }
  };

  // 加载所有选中项目的成员（用于责任人筛选下拉）
  const fetchSelectedProjectMembers = async (projectIds: number[]) => {
    if (projectIds.length === 0) return;

    const promises = projectIds.map((id) => getProjectMembers(id));
    try {
      const results = await Promise.all(promises);
      const newMembersMap = new Map(allProjectMembers);
      projectIds.forEach((id, index) => {
        newMembersMap.set(id, results[index] || []);
      });
      setAllProjectMembers(newMembersMap);
    } catch (error) {
      console.error('批量加载项目成员失败:', error);
    }
  };

  // 获取所有选中项目的成员（用于责任人筛选下拉）
  const getFilterMembers = () => {
    if (selectedProjectIds.length === 0) return [];
    const members: ProjectMemberResponse[] = [];
    const seenIds = new Set<number>();
    selectedProjectIds.forEach((projectId) => {
      const projectMembers = allProjectMembers.get(projectId) || [];
      projectMembers.forEach((m) => {
        if (!seenIds.has(m.userId)) {
          seenIds.add(m.userId);
          members.push(m);
        }
      });
    });
    return members;
  };

  // 加载任务（使用筛选条件）
  const loadTasks = useCallback(async () => {
    setIsFiltering(true);
    try {
      const searchParams: Record<string, any> = {
        page: 1,
        pageSize: 100,
      };

      // 项目筛选（支持多选）
      if (selectedProjectIds && selectedProjectIds.length > 0) {
        searchParams.projectIds = selectedProjectIds;
      }

      // 状态筛选
      if (selectedStatus) {
        searchParams.status = selectedStatus.toUpperCase();
      }

      // 优先级筛选
      if (selectedPriority) {
        searchParams.priority = selectedPriority.toUpperCase();
      }

      // 责任人筛选
      if (selectedAssigneeId) {
        searchParams.assigneeId = selectedAssigneeId;
      }

      // 关键词筛选
      if (searchKeyword) {
        searchParams.keyword = searchKeyword;
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
          assigneeName: task.assigneeName,  // 使用后端返回的 assigneeName
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
  }, [projects, selectedProjectIds, selectedStatus, selectedPriority, selectedAssigneeId, searchKeyword]);

  // 加载项目列表
  useEffect(() => {
    const fetchProjects = async () => {
      try {
        const result = await getAuthorizedProjects();
        setProjects(result?.list || []);
      } catch (error) {
        console.error('加载项目列表失败:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchProjects();
  }, []);

  // 当选中项目变化时，加载成员列表
  useEffect(() => {
    if (selectedProjectIds.length > 0) {
      fetchSelectedProjectMembers(selectedProjectIds);
    }
  }, [selectedProjectIds]);

  // 筛选条件变化时重新加载任务
  useEffect(() => {
    if (projects.length > 0) {
      loadTasks();
    }
  }, [selectedProjectIds, selectedStatus, selectedPriority, selectedAssigneeId, searchKeyword, loadTasks, projects.length]);

  // 筛选后的任务（本地不再过滤，直接使用 API 返回的结果）
  const filteredTasks = tasks;

  // 清空筛选
  const handleClearFilters = () => {
    setSelectedProjectIds([]);
    setSelectedStatus(undefined);
    setSelectedPriority(undefined);
    setSelectedAssigneeId(undefined);
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
            placeholder="搜索任务标题或描述..."
            prefix={<SearchOutlined className="text-gray-400" />}
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            allowClear
            className="w-64 bg-gray-800/50 border-gray-700 text-white placeholder-gray-500"
          />

          {/* 项目筛选（支持多选） */}
          <Select
            mode="multiple"
            value={selectedProjectIds}
            onChange={(value) => {
              setSelectedProjectIds(value);
              setSelectedAssigneeId(undefined);  // 清空责任人筛选
            }}
            className="w-56"
            placeholder="选择项目（可多选）"
            allowClear
            maxTagCount="responsive"
            dropdownClassName="bg-gray-800 border-gray-700"
          >
            {projects.map((project) => (
              <Option key={project.id} value={project.id}>
                {project.icon || '📁'} {project.name}
              </Option>
            ))}
          </Select>

          {/* 责任人筛选 */}
          <Select
            placeholder="选择责任人"
            value={selectedAssigneeId}
            onChange={(value) => {
              setSelectedAssigneeId(value);
            }}
            className="w-40 bg-gray-800/50 border-gray-700"
            allowClear
            showSearch
            filterOption={(input, option) =>
              String(option?.label ?? '').toLowerCase().includes(input.toLowerCase())
            }
            options={getFilterMembers().map((member) => ({
              label: member.username,
              value: member.userId,
            }))}
            disabled={selectedProjectIds.length === 0}
          />

          {/* 状态筛选 */}
          <Select
            placeholder="选择状态"
            value={selectedStatus}
            onChange={setSelectedStatus}
            className="w-32"
            allowClear
            dropdownClassName="bg-gray-800 border-gray-700"
          >
            <Option value="todo">待办</Option>
            <Option value="in_progress">进行中</Option>
            <Option value="testing">测试中</Option>
            <Option value="done">已完成</Option>
          </Select>

          {/* 优先级筛选 */}
          <Select
            placeholder="选择优先级"
            value={selectedPriority}
            onChange={setSelectedPriority}
            className="w-32"
            allowClear
            dropdownClassName="bg-gray-800 border-gray-700"
          >
            <Option value="low">低</Option>
            <Option value="medium">中</Option>
            <Option value="high">高</Option>
            <Option value="urgent">紧急</Option>
          </Select>

          {/* 清空筛选按钮 */}
          {(selectedProjectIds.length > 0 || selectedStatus || selectedPriority || selectedAssigneeId || searchKeyword) && (
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
