// 任务管理 API 客户端

import { api } from './axios';
import { endpoints } from './endpoints';
import type { CreateTaskDto, UpdateTaskDto, TaskStatus, Priority, SubTask, TaskComment, Task } from '@/types/task';
export type { TaskStatus, Priority, CreateTaskDto, UpdateTaskDto, Task, SubTask, TaskComment };

/** 后端返回的任务数据结构 */
interface TaskResponse {
  id: number | string;
  projectId: number | string;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: Priority;
  assigneeId?: number | string;
  reporterId?: number | string;
  storyPoints?: number;
  dueDate?: string;
  tags: string[];
  parentId?: number | string;
  order: number;
  subtaskCount: number;
  completedSubtaskCount: number;
  commentCount: number;
  createdAt: string;
  updatedAt?: string;
}

/** 前端看板任务数据结构（用于任务看板显示） */
export interface TaskBoardItem {
  id: string;
  title: string;
  description?: string;
  priority: Priority;
  assignee?: string;
  assigneeId?: number;
  dueDate?: string;
  columnId: string;
  storyPoints?: number;
  taskId: number;
  projectId: number;
}

/**
 * 将后端任务数据转换为前端格式
 */
const mapTaskResponse = (response: TaskResponse, columnId?: string): Task => {
  // 处理状态映射：如果没有提供 columnId，从 status 转换
  const statusToColumn: Record<TaskStatus, string> = {
    'todo': 'todo',
    'in_progress': 'in-progress',
    'testing': 'testing',
    'done': 'done',
  };

  return {
    id: typeof response.id === 'string' ? parseInt(response.id, 10) : (response.id as number),
    projectId: typeof response.projectId === 'string' ? parseInt(response.projectId, 10) : (response.projectId as number),
    title: response.title || '',
    description: response.description || '',
    status: response.status || 'todo',
    priority: response.priority || 'medium',
    assigneeId: response.assigneeId ? (typeof response.assigneeId === 'string' ? parseInt(response.assigneeId, 10) : response.assigneeId) : undefined,
    reporterId: response.reporterId ? (typeof response.reporterId === 'string' ? parseInt(response.reporterId, 10) : response.reporterId) : undefined,
    storyPoints: response.storyPoints,
    dueDate: response.dueDate,
    tags: response.tags || [],
    parentId: response.parentId ? (typeof response.parentId === 'string' ? parseInt(response.parentId, 10) : response.parentId) : undefined,
    order: response.order || 0,
    subtaskCount: response.subtaskCount || 0,
    completedSubtaskCount: response.completedSubtaskCount || 0,
    commentCount: response.commentCount || 0,
    createdAt: response.createdAt || '',
    updatedAt: response.updatedAt,
  };
};

/**
 * 将任务数据转换为看板项格式
 */
const mapTaskToBoardItem = (task: Task, assigneeName?: string): TaskBoardItem => {
  const statusToColumn: Record<TaskStatus, string> = {
    'todo': 'todo',
    'in_progress': 'in-progress',
    'testing': 'testing',
    'done': 'done',
  };

  return {
    id: String(task.id),
    title: task.title,
    description: task.description,
    priority: task.priority,
    assignee: assigneeName,
    assigneeId: task.assigneeId,
    dueDate: task.dueDate,
    columnId: statusToColumn[task.status] || 'todo',
    storyPoints: task.storyPoints,
    taskId: task.id,
    projectId: task.projectId,
  };
};

/**
 * 获取项目任务列表
 * @param projectId 项目 ID
 */
export const getTasks = async (projectId: number) => {
  const result = await api.get<{
    list: TaskResponse[];
    total: number;
    page: number;
    size: number;
  }>(endpoints.task.list(projectId));

  console.log('[task.api] getTasks result:', result);

  return {
    list: (result.data.data?.list || []).map((item) => mapTaskResponse(item)),
    total: result.data.data?.total || 0,
    page: result.data.data?.page || 1,
    size: result.data.data?.size || 20,
  };
};

/**
 * 获取项目任务列表（转换为看板格式）
 * @param projectId 项目 ID
 */
export const getTasksForBoard = async (projectId: number): Promise<TaskBoardItem[]> => {
  const result = await getTasks(projectId);
  // 返回看板项格式，暂时不获取负责人名称
  return result.list.map((task) => mapTaskToBoardItem(task));
};

/**
 * 获取任务详情
 * @param projectId 项目 ID
 * @param taskId 任务 ID
 */
export const getTask = async (projectId: number, taskId: number) => {
  const result = await api.get<TaskResponse>(endpoints.task.detail(projectId, taskId));
  return mapTaskResponse(result.data.data);
};

/**
 * 创建任务
 * @param projectId 项目 ID
 * @param data 任务数据
 */
export const createTask = async (projectId: number, data: CreateTaskDto) => {
  // 转换前端字段到后端字段
  const requestBody = {
    projectId,
    title: data.title,
    description: data.description || '',
    status: data.status || 'todo',
    priority: data.priority || 'medium',
    assigneeId: data.assigneeId,
    storyPoints: data.storyPoints,
    dueDate: data.dueDate,
    tags: data.tags || [],
    parentId: data.parentId,
  };

  const result = await api.post<TaskResponse>(endpoints.task.create(projectId), requestBody);
  return mapTaskResponse(result.data.data);
};

/**
 * 更新任务
 * @param projectId 项目 ID
 * @param taskId 任务 ID
 * @param data 更新数据
 */
export const updateTask = async (projectId: number, taskId: number, data: UpdateTaskDto) => {
  const requestBody: Record<string, unknown> = {};

  if (data.title !== undefined) requestBody.title = data.title;
  if (data.description !== undefined) requestBody.description = data.description;
  if (data.status !== undefined) requestBody.status = data.status;
  if (data.priority !== undefined) requestBody.priority = data.priority;
  if (data.assigneeId !== undefined) requestBody.assigneeId = data.assigneeId;
  if (data.storyPoints !== undefined) requestBody.storyPoints = data.storyPoints;
  if (data.dueDate !== undefined) requestBody.dueDate = data.dueDate;
  if (data.tags !== undefined) requestBody.tags = data.tags;
  if (data.order !== undefined) requestBody.order = data.order;

  const result = await api.put<TaskResponse>(endpoints.task.update(projectId, taskId), requestBody);
  return mapTaskResponse(result.data.data);
};

/**
 * 删除任务
 * @param projectId 项目 ID
 * @param taskId 任务 ID
 */
export const deleteTask = async (projectId: number, taskId: number) => {
  return api.delete<void>(endpoints.task.delete(projectId, taskId));
};

/**
 * 移动任务（更新任务状态）
 * @param projectId 项目 ID
 * @param taskId 任务 ID
 * @param newStatus 新状态
 * @param newOrder 新排序
 */
export const moveTask = async (
  projectId: number,
  taskId: number,
  newStatus: TaskStatus,
  newOrder?: number
) => {
  const requestBody: Record<string, unknown> = {
    status: newStatus,
  };

  if (newOrder !== undefined) {
    requestBody.order = newOrder;
  }

  const result = await api.post<TaskResponse>(endpoints.task.move(projectId, taskId), requestBody);
  return mapTaskResponse(result.data.data);
};

/**
 * 切换任务完成状态
 * @param projectId 项目 ID
 * @param taskId 任务 ID
 */
export const toggleTaskComplete = async (projectId: number, taskId: number) => {
  const result = await api.post<TaskResponse>(endpoints.task.toggleComplete(projectId, taskId), {});
  return mapTaskResponse(result.data.data);
};

/**
 * 获取任务子任务列表
 * @param projectId 项目 ID
 * @param taskId 任务 ID
 */
export const getSubTasks = async (projectId: number, taskId: number) => {
  const result = await api.get<SubTask[]>(endpoints.task.subtasks(projectId, taskId));
  return result.data.data || [];
};

/**
 * 创建子任务
 * @param projectId 项目 ID
 * @param taskId 任务 ID
 * @param title 子任务标题
 */
export const createSubTask = async (projectId: number, taskId: number, title: string) => {
  const result = await api.post<SubTask>(endpoints.task.subtasks(projectId, taskId), { title });
  return result;
};

/**
 * 删除子任务
 * @param projectId 项目 ID
 * @param taskId 任务 ID
 * @param subtaskId 子任务 ID
 */
export const deleteSubTask = async (projectId: number, taskId: number, subtaskId: number) => {
  return api.delete<void>(`${endpoints.task.subtasks(projectId, taskId)}/${subtaskId}`);
};

/**
 * 获取任务评论列表
 * @param projectId 项目 ID
 * @param taskId 任务 ID
 */
export const getTaskComments = async (projectId: number, taskId: number) => {
  const result = await api.get<TaskComment[]>(endpoints.task.comments(projectId, taskId));
  return result.data.data || [];
};

/**
 * 创建任务评论
 * @param projectId 项目 ID
 * @param taskId 任务 ID
 * @param content 评论内容
 * @param parentId 父评论 ID（用于回复）
 */
export const createTaskComment = async (
  projectId: number,
  taskId: number,
  content: string,
  parentId?: number
) => {
  const requestBody: Record<string, unknown> = {
    content,
  };

  if (parentId) {
    requestBody.parentId = parentId;
  }

  const result = await api.post<TaskComment>(endpoints.task.comments(projectId, taskId), requestBody);
  return result.data;
};