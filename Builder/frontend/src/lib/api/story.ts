// 用户故事 (User Story) 管理模块 API

import { api } from './axios';
import { endpoints } from './endpoints';
import { PageInfo } from '@/types/api';

// 后端 PageResult 类型（字段名与后端保持一致）
interface PageResult<T> {
  list: T[];
  total: number;
  page: number;
  size: number;
  pages: number;
}

// 用户故事数据结构（与后端 UserStoryVO 对应）
export interface UserStory {
  id: number;
  projectId: number;
  epicId?: number;
  title: string;
  description: string;
  acceptanceCriteria?: string;
  status: StoryStatus;
  priority: Priority;
  assigneeId?: number;
  assigneeName?: string;
  position?: number;
  storyPoints?: number;
  createdAt: string;
  updatedAt?: string;
}

// 状态枚举（与后端保持一致，使用大写）
export type StoryStatus = 'TODO' | 'IN_PROGRESS' | 'TESTING' | 'DONE';

// 优先级枚举（与后端保持一致，使用大写）
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

// 前端映射：小写转大写
export const statusMap: Record<string, StoryStatus> = {
  todo: 'TODO',
  in_progress: 'IN_PROGRESS',
  testing: 'TESTING',
  done: 'DONE',
};

// 前端映射：大写转小写（用于展示）
export const statusTextMap: Record<StoryStatus, string> = {
  TODO: '待办',
  IN_PROGRESS: '进行中',
  TESTING: '测试中',
  DONE: '已完成',
};

export const priorityMap: Record<string, Priority> = {
  low: 'LOW',
  medium: 'MEDIUM',
  high: 'HIGH',
  urgent: 'URGENT',
};

export const priorityTextMap: Record<Priority, string> = {
  LOW: '低',
  MEDIUM: '中',
  HIGH: '高',
  URGENT: '紧急',
};

// 查询参数（与后端 FilterRequest 对应）
export interface QueryParams {
  status?: StoryStatus | string;
  priority?: Priority | string;
  assigneeId?: number;
  epicId?: number;
  keyword?: string;
  page?: number;
  size?: number;  // 后端分页参数使用 size
  pageSize?: number;  // 兼容旧代码
  sort?: string;
  order?: 'asc' | 'desc';
  projectIds?: number[];  // 用于全局搜索接口
}

// 创建请求 DTO（与后端 CreateRequest 对应）
export interface CreateUserStoryDto {
  epicId?: number;
  title: string;
  description?: string;
  acceptanceCriteria?: string;
  priority?: Priority | string;
  assigneeId?: number;
  storyPoints?: number;
}

// 更新请求 DTO（与后端 UpdateRequest 对应）
export interface UpdateUserStoryDto {
  epicId?: number;
  title?: string;
  description?: string;
  acceptanceCriteria?: string;
  status?: StoryStatus | string;
  priority?: Priority | string;
  assigneeId?: number;
  storyPoints?: number;
}

/**
 * 将后端 PageResult 转换为前端 PageInfo
 */
function convertPageResult<T>(pageResult: PageResult<T>): PageInfo<T> {
  return {
    items: pageResult.list,
    total: pageResult.total,
    page: pageResult.page,
    pageSize: pageResult.size,
    totalPages: pageResult.pages,
  };
}

/**
 * 搜索用户故事（全局搜索，支持项目筛选和权限校验）
 * @param params 查询参数（包含 projectIds 筛选）
 */
export const searchStories = async (params?: QueryParams) => {
  // 使用 POST 请求，参数放在 body 中
  const res = await api.post<PageResult<UserStory>>(endpoints.story.search, {
    epicId: params?.epicId,
    status: params?.status ? String(params.status).toUpperCase() : undefined,
    priority: params?.priority ? String(params.priority).toUpperCase() : undefined,
    assigneeId: params?.assigneeId,
    keyword: params?.keyword,
    projectIds: params?.projectIds,
  }, {
    params: {
      page: params?.page || 1,
      size: params?.size || params?.pageSize || 20,
    },
  });
  return convertPageResult(res.data.data);
};

/**
 * 获取项目下所有用户故事（分页）- 使用 GET 请求
 * @param projectId 项目 ID
 * @param params 查询参数
 */
export const getStories = async (projectId: number, params?: QueryParams) => {
  const res = await api.get<PageResult<UserStory>>(endpoints.story.list(projectId), {
    params: {
      ...params,
      status: params?.status ? String(params.status).toUpperCase() : undefined,
      priority: params?.priority ? String(params.priority).toUpperCase() : undefined,
    }
  });
  return convertPageResult(res.data.data);
};

/**
 * 获取用户故事详情
 * @param id 故事 ID
 */
export const getStory = async (id: number) => {
  const res = await api.get<UserStory>(endpoints.story.detail(id));
  return res.data.data;
};

/**
 * 创建用户故事
 * @param projectId 项目 ID
 * @param data 创建数据
 */
export const createStory = async (projectId: number, data: CreateUserStoryDto) => {
  const res = await api.post<UserStory>(endpoints.story.create(projectId), {
    ...data,
    priority: data.priority ? String(data.priority).toUpperCase() : undefined,
  });
  return res.data.data;
};

/**
 * 更新用户故事
 * @param id 故事 ID
 * @param data 更新数据
 */
export const updateStory = async (id: number, data: UpdateUserStoryDto) => {
  const res = await api.put<UserStory>(endpoints.story.update(id), {
    ...data,
    status: data.status ? String(data.status).toUpperCase() : undefined,
    priority: data.priority ? String(data.priority).toUpperCase() : undefined,
  });
  return res.data.data;
};

/**
 * 删除用户故事
 * @param id 故事 ID
 */
export const deleteStory = (id: number) =>
  api.delete(endpoints.story.delete(id));
