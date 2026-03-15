'use client';

import { useState, useEffect, useMemo, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import {
  Card, Button, Tag, Avatar, message, Spin, Select, Input, Pagination, Form, DatePicker, Drawer,
} from 'antd';
import { PlusOutlined, ClockCircleOutlined, FlagOutlined, UserOutlined, SearchOutlined, ClearOutlined } from '@ant-design/icons';
import { DndContext, DragEndEvent, closestCenter } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy, useSortable } from '@dnd-kit/sortable';
import { useDroppable } from '@dnd-kit/core';
import { CSS } from '@dnd-kit/utilities';
import { searchTasks as searchTasksApi, moveTask as moveTaskApi, createTask as createTaskApi, type Task, type TaskStatus, type Priority } from '@/lib/api/task';
import { getAuthorizedProjects, getProjectMembers } from '@/lib/api/project';
import type { Project } from '@/lib/api/project';
import type { ProjectMemberResponse } from '@/lib/api/project';
import dayjs from 'dayjs';

const { Option } = Select;

// 状态文本映射
const statusTextMap: Record<string, string> = {
  TODO: '待办',
  IN_PROGRESS: '开发中',
  IN_REVIEW: '测试中',
  DONE: '已完成',
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
  { id: 'TODO', title: '待办', color: '#6b7280' },
  { id: 'IN_PROGRESS', title: '开发中', color: '#3b82f6' },
  { id: 'IN_REVIEW', title: '测试中', color: '#f59e0b' },
  { id: 'DONE', title: '已完成', color: '#10b981' },
];

// 任务状态映射（后端状态 -> 看板列 ID）
const statusToColumn: Record<TaskStatus, string> = {
  'TODO': 'TODO',
  'IN_PROGRESS': 'IN_PROGRESS',
  'IN_REVIEW': 'IN_REVIEW',
  'DONE': 'DONE',
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

// 可排序的列组件（支持拖拽放置）
interface SortableColumnProps {
  column: typeof columns[0];
  tasks: TaskBoardItem[];
  onTaskClick: (task: TaskBoardItem) => void;
}

const SortableColumn: React.FC<SortableColumnProps> = ({ column, tasks, onTaskClick }) => {
  const { setNodeRef } = useDroppable({
    id: column.id,
  });

  return (
    <div className="flex-1 min-w-[280px]">
      <div className="flex items-center justify-between mb-3 px-1">
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full" style={{ backgroundColor: column.color }} />
          <h3 className="text-white font-semibold">{column.title}</h3>
          <span className="text-gray-500 text-sm">({tasks.length})</span>
        </div>
      </div>
      <div
        ref={setNodeRef}
        className="min-h-[600px] bg-gray-800/30 rounded-lg p-3"
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

  // 创建任务抽屉状态
  const [createDrawerVisible, setCreateDrawerVisible] = useState(false);
  const [createForm] = Form.useForm();
  const [currentProjectMembers, setCurrentProjectMembers] = useState<ProjectMemberResponse[]>([]);

  // 分页状态
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);  // 每页条数可选
  const [total, setTotal] = useState(0);

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
    setLoading(true);
    try {
      const searchParams: Record<string, any> = {
        page,
        pageSize,
      };

      // 项目筛选（支持多选）
      if (selectedProjectIds && selectedProjectIds.length > 0) {
        searchParams.projectIds = selectedProjectIds;
      }

      // 状态筛选
      if (selectedStatus) {
        searchParams.status = selectedStatus;  // 已经是大写格式
      }

      // 优先级筛选
      if (selectedPriority) {
        searchParams.priority = selectedPriority;
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
          columnId: statusToColumn[task.status] || 'TODO',
        };
      });

      setTasks(tasksWithProjectName);
      setTotal(tasksResult.total || 0);
    } catch (error) {
      console.error('加载任务失败:', error);
      message.error('加载任务失败');
    } finally {
      setIsFiltering(false);
      setLoading(false);
    }
  }, [page, pageSize, projects, selectedProjectIds, selectedStatus, selectedPriority, selectedAssigneeId, searchKeyword]);

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

  // 筛选条件变化时重新加载任务（同时重置页码）
  useEffect(() => {
    if (projects.length > 0) {
      setPage(1);  // 重置到第一页
      loadTasks();
    }
  }, [selectedProjectIds, selectedStatus, selectedPriority, selectedAssigneeId, searchKeyword, loadTasks, projects.length]);

  // 筛选后的任务（本地不再过滤，直接使用 API 返回的结果）
  const filteredTasks = tasks;

  // 清空筛选（同时重置页码）
  const handleClearFilters = () => {
    setSelectedProjectIds([]);
    setSelectedStatus(undefined);
    setSelectedPriority(undefined);
    setSelectedAssigneeId(undefined);
    setSearchKeyword('');
    setPage(1);  // 重置页码
  };

  // 处理拖拽结束
  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;

    if (!over) return;

    const activeId = active.id as string;
    const overId = over.id as string;

    const activeTask = tasks.find((t) => t.id === activeId);
    if (!activeTask) return;

    // 检查是否拖到列上（列 ID 就是状态 ID）
    const targetColumn = columns.find((c) => c.id === overId);
    let newColumnId = activeTask.columnId;

    if (targetColumn) {
      // 直接拖到列上
      newColumnId = targetColumn.id;
    } else {
      // 拖到另一个任务上，获取该任务所在的列
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
        t.id === activeId ? { ...t, columnId: newColumnId, status: newColumnId as TaskStatus } : t
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
          t.id === activeId ? { ...t, columnId: activeTask.columnId, status: activeTask.status } : t
        )
      );
    }
  };

  // 处理任务点击
  const handleTaskClick = (task: TaskBoardItem) => {
    router.push(`/tasks/${task.taskId}`);
  };

  // 打开创建任务抽屉
  const handleCreateTask = () => {
    createForm.resetFields();
    // 如果只选择了一个项目，默认选中
    if (selectedProjectIds.length === 1) {
      createForm.setFieldsValue({ projectId: selectedProjectIds[0] });
      const projectId = selectedProjectIds[0];
      const members = allProjectMembers.get(projectId) || [];
      setCurrentProjectMembers(members);
    } else {
      setCurrentProjectMembers([]);
    }
    setCreateDrawerVisible(true);
  };

  // 创建任务提交
  const handleCreateTaskSubmit = async (values: any) => {
    try {
      const targetProjectId = values.projectId;
      if (!targetProjectId) {
        message.error('请选择所属项目');
        return;
      }

      const payload = {
        title: values.title,
        description: values.description || '',
        status: values.status || 'TODO',
        priority: values.priority || 'medium',
        assigneeId: values.assigneeId,
        storyPoints: values.storyPoints,
        dueDate: values.dueDate ? values.dueDate.format('YYYY-MM-DD') : '',
        tags: values.tags || [],
        userStoryId: values.userStoryId,
      };

      await createTaskApi(targetProjectId, payload);
      message.success('任务创建成功');
      setCreateDrawerVisible(false);
      loadTasks();
    } catch (error) {
      console.error('创建任务失败:', error);
      message.error('创建任务失败');
    }
  };

  // 当项目选择变化时，加载项目成员
  const handleProjectChange = (projectId: number) => {
    const members = allProjectMembers.get(projectId) || [];
    setCurrentProjectMembers(members);
    createForm.setFieldsValue({ assigneeId: undefined });
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
          <Button
            type="primary"
            icon={<PlusOutlined />}
            size="large"
            onClick={handleCreateTask}
            className="bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 border-none shadow-lg shadow-orange-500/20"
          >
            新建任务
          </Button>
        </div>

        {/* 筛选工具栏 - 所有控件放在一行 */}
        <div className="flex items-center gap-2">
          {/* 关键词搜索 */}
          <Input
            placeholder="搜索任务标题或描述..."
            prefix={<SearchOutlined className="text-gray-400" />}
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            allowClear
            className="flex-1 min-w-[200px] bg-gray-800/50 border-gray-700 text-white placeholder-gray-500"
          />

          {/* 项目筛选（支持多选） */}
          <Select
            mode="multiple"
            value={selectedProjectIds}
            onChange={(value) => {
              setSelectedProjectIds(value);
              setSelectedAssigneeId(undefined);  // 清空责任人筛选
            }}
            className="w-48"
            placeholder="选择项目"
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
            placeholder="责任人"
            value={selectedAssigneeId}
            onChange={(value) => {
              setSelectedAssigneeId(value);
            }}
            className="w-32 bg-gray-800/50 border-gray-700"
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
            placeholder="状态"
            value={selectedStatus}
            onChange={setSelectedStatus}
            className="w-28"
            allowClear
            dropdownClassName="bg-gray-800 border-gray-700"
          >
            <Option value="TODO">待办</Option>
            <Option value="IN_PROGRESS">开发中</Option>
            <Option value="IN_REVIEW">测试中</Option>
            <Option value="DONE">已完成</Option>
          </Select>

          {/* 优先级筛选 */}
          <Select
            placeholder="优先级"
            value={selectedPriority}
            onChange={setSelectedPriority}
            className="w-28"
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
          <span className="text-gray-400 text-sm whitespace-nowrap">
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

          {/* 分页组件 */}
          <div className="flex justify-end items-center gap-3 pt-4 border-t border-gray-700">
            <Pagination
              current={page}
              total={total}
              pageSize={pageSize}
              onChange={(newPage) => {
                setPage(newPage);
                window.scrollTo({ top: 0, behavior: 'smooth' });
              }}
              onShowSizeChange={(_, size) => {
                setPageSize(size);
                setPage(1);  // 切换 pageSize 时重置到第一页
              }}
              showSizeChanger
              showTotal={(total) => `共 ${total} 条`}
              pageSizeOptions={[10, 30, 50, 100]}
            />
          </div>
        </>
      )}

      {/* 创建任务抽屉 */}
      <Drawer
        title="创建任务"
        placement="left"
        open={createDrawerVisible}
        onClose={() => {
          setCreateDrawerVisible(false);
          createForm.resetFields();
        }}
        width={600}
        styles={{
          body: { padding: 0, background: '#161b22', color: '#f0f6fc' },
          header: {
            background: '#161b22',
            borderBottom: '1px solid rgba(255,255,255,0.05)',
            padding: '20px 24px',
          },
          footer: {
            background: '#161b22',
            borderTop: '1px solid rgba(255,255,255,0.05)',
          },
        }}
        footer={
          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
            <Button
              onClick={() => {
                setCreateDrawerVisible(false);
                createForm.resetFields();
              }}
              style={{
                background: 'transparent',
                border: '1px solid #30363d',
                color: '#c9d1d9',
                borderRadius: '6px',
                padding: '8px 16px',
              }}
            >
              取消
            </Button>
            <Button
              type="primary"
              onClick={() => createForm.submit()}
              style={{
                background: '#ff8c42',
                border: 'none',
                fontWeight: 'bold',
                borderRadius: '6px',
                padding: '8px 24px',
              }}
            >
              创建任务
            </Button>
          </div>
        }
      >
        <Form
          form={createForm}
          onFinish={handleCreateTaskSubmit}
          layout="vertical"
          style={{ padding: '24px' }}
        >
          {/* 所属项目 */}
          {selectedProjectIds.length !== 1 && (
            <Form.Item
              name="projectId"
              label={<span style={{ color: '#8b949e', fontSize: '13px' }}>所属项目</span>}
              rules={[{ required: true, message: '请选择所属项目' }]}
            >
              <Select
                placeholder="选择项目"
                onChange={handleProjectChange}
                style={{ width: '100%' }}
                dropdownClassName="bg-gray-800 border-gray-700"
              >
                {projects.map((project) => (
                  <Option key={project.id} value={project.id}>
                    {project.icon || '📁'} {project.name}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          )}

          {/* 任务标题 */}
          <Form.Item
            name="title"
            label={<span style={{ color: '#8b949e', fontSize: '13px' }}>任务标题</span>}
            rules={[{ required: true, message: '请输入任务标题' }]}
          >
            <Input
              placeholder="输入任务标题"
              style={{
                background: 'rgba(255,255,255,0.05)',
                border: '1px solid rgba(255,255,255,0.1)',
                borderRadius: '6px',
                padding: '10px',
                color: '#f0f6fc',
              }}
            />
          </Form.Item>

          {/* 任务描述 */}
          <Form.Item
            name="description"
            label={<span style={{ color: '#8b949e', fontSize: '13px' }}>任务描述</span>}
          >
            <Input.TextArea
              placeholder="输入任务描述"
              rows={6}
              style={{
                background: 'rgba(255,255,255,0.05)',
                border: '1px solid rgba(255,255,255,0.1)',
                borderRadius: '6px',
                padding: '10px',
                color: '#f0f6fc',
                minHeight: '150px',
              }}
            />
          </Form.Item>

          {/* 状态和优先级 */}
          <div style={{ display: 'flex', gap: '20px' }}>
            <Form.Item
              name="status"
              label={<span style={{ color: '#8b949e', fontSize: '13px' }}>状态</span>}
              initialValue="TODO"
              style={{ flex: 1 }}
            >
              <Select
                style={{ width: '100%' }}
                dropdownClassName="bg-gray-800 border-gray-700"
              >
                <Option value="TODO">待办</Option>
                <Option value="IN_PROGRESS">开发中</Option>
                <Option value="IN_REVIEW">测试中</Option>
                <Option value="DONE">已完成</Option>
              </Select>
            </Form.Item>

            <Form.Item
              name="priority"
              label={<span style={{ color: '#8b949e', fontSize: '13px' }}>优先级</span>}
              initialValue="medium"
              style={{ flex: 1 }}
            >
              <Select
                style={{ width: '100%' }}
                dropdownClassName="bg-gray-800 border-gray-700"
              >
                <Option value="low">低</Option>
                <Option value="medium">中</Option>
                <Option value="high">高</Option>
                <Option value="urgent">紧急</Option>
              </Select>
            </Form.Item>
          </div>

          {/* 责任人和故事点 */}
          <div style={{ display: 'flex', gap: '20px' }}>
            <Form.Item
              name="assigneeId"
              label={<span style={{ color: '#8b949e', fontSize: '13px' }}>责任人</span>}
              style={{ flex: 1 }}
            >
              <Select
                placeholder="选择责任人"
                allowClear
                showSearch
                filterOption={(input, option) =>
                  String(option?.label ?? '').toLowerCase().includes(input.toLowerCase())
                }
                dropdownClassName="bg-gray-800 border-gray-700"
              >
                {currentProjectMembers.map((member) => (
                  <Option key={member.userId} value={member.userId}>
                    {member.username}
                  </Option>
                ))}
              </Select>
            </Form.Item>

            <Form.Item
              name="storyPoints"
              label={<span style={{ color: '#8b949e', fontSize: '13px' }}>故事点</span>}
              style={{ flex: 1 }}
            >
              <Select
                placeholder="故事点"
                allowClear
                style={{ width: '100%' }}
                dropdownClassName="bg-gray-800 border-gray-700"
              >
                <Option value={1}>1</Option>
                <Option value={2}>2</Option>
                <Option value={3}>3</Option>
                <Option value={5}>5</Option>
                <Option value={8}>8</Option>
                <Option value={13}>13</Option>
                <Option value={21}>21</Option>
              </Select>
            </Form.Item>
          </div>

          {/* 截止日期 */}
          <Form.Item
            name="dueDate"
            label={<span style={{ color: '#8b949e', fontSize: '13px' }}>截止日期</span>}
          >
            <DatePicker
              style={{
                width: '100%',
                background: 'rgba(255,255,255,0.05)',
                border: '1px solid rgba(255,255,255,0.1)',
                borderRadius: '6px',
                padding: '10px',
                color: '#f0f6fc',
              }}
              dropdownClassName="bg-gray-800 border-gray-700"
              placeholder="选择截止日期"
            />
          </Form.Item>

          {/* 关联用户故事 */}
          <Form.Item
            name="userStoryId"
            label={<span style={{ color: '#8b949e', fontSize: '13px' }}>关联用户故事</span>}
          >
            <Select
              placeholder="选择用户故事（可选）"
              allowClear
              style={{ width: '100%' }}
              dropdownClassName="bg-gray-800 border-gray-700"
            >
              <Option value={1}>示例故事 1</Option>
              <Option value={2}>示例故事 2</Option>
            </Select>
          </Form.Item>
        </Form>
      </Drawer>
    </div>
  );
}
