// 用户故事 (User Story) 管理模块 API

import { api } from './axios';
import { endpoints } from './endpoints';

export interface UserStory {
  id: number;
  projectId: number;
  epicId?: number;
  title: string;
  description: string;
  status: StoryStatus;
  priority: Priority;
  assigneeId?: number;
  reporterId?: number;
  storyPoints?: number;
  dueDate?: string;
  tags: string[];
  createdAt: string;
  updatedAt?: string;
}

export type StoryStatus = 'todo' | 'in_progress' | 'testing' | 'done';

export type Priority = 'low' | 'medium' | 'high' | 'urgent';

export interface QueryParams {
  status?: StoryStatus;
  priority?: Priority;
  assigneeId?: number;
  epicId?: number;
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}

export interface CreateUserStoryDto {
  projectId: number;
  epicId?: number;
  title: string;
  description?: string;
  status?: StoryStatus;
  priority?: Priority;
  assigneeId?: number;
  storyPoints?: number;
  dueDate?: string;
  tags?: string[];
}

export interface UpdateUserStoryDto {
  epicId?: number;
  title?: string;
  description?: string;
  status?: StoryStatus;
  priority?: Priority;
  assigneeId?: number;
  storyPoints?: number;
  dueDate?: string;
  tags?: string[];
  order?: number;
}

/**
 * 获取项目下所有用户故事
 * @param projectId 项目 ID
 * @param params 查询参数
 */
export const getStories = (projectId: number, params?: QueryParams) =>
  api.get<UserStory[]>(endpoints.story.list(projectId), { params });

/**
 * 获取用户故事详情
 * @param projectId 项目 ID
 * @param id 故事 ID
 */
export const getStory = (projectId: number, id: number) =>
  api.get<UserStory>(endpoints.story.detail(projectId, id));

/**
 * 创建用户故事
 * @param projectId 项目 ID
 * @param data 创建数据
 */
export const createStory = (projectId: number, data: CreateUserStoryDto) =>
  api.post<UserStory>(endpoints.story.create(projectId), data);

/**
 * 更新用户故事
 * @param projectId 项目 ID
 * @param id 故事 ID
 * @param data 更新数据
 */
export const updateStory = (projectId: number, id: number, data: UpdateUserStoryDto) =>
  api.put<UserStory>(endpoints.story.update(projectId, id), data);

/**
 * 删除用户故事
 * @param projectId 项目 ID
 * @param id 故事 ID
 */
export const deleteStory = (projectId: number, id: number) =>
  api.delete(endpoints.story.delete(projectId, id));
