// 史诗 (Epic) 管理模块 API

import { api } from './axios';
import { endpoints } from './endpoints';

export interface Epic {
  id: number;
  projectId: number;
  name: string;
  description: string;
  color: string;
  startDate: string;
  endDate: string;
  status: EpicStatus;
  progress: number;
  issueCount: number;
  completedIssueCount: number;
  createdAt: string;
  updatedAt?: string;
}

export type EpicStatus = 'planning' | 'active' | 'completed' | 'archived';

export interface CreateEpicDto {
  projectId: number;
  name: string;
  description?: string;
  color?: string;
  startDate?: string;
  endDate?: string;
  status?: EpicStatus;
}

export interface UpdateEpicDto {
  name?: string;
  description?: string;
  color?: string;
  startDate?: string;
  endDate?: string;
  status?: EpicStatus;
  progress?: number;
}

/**
 * 获取项目下所有史诗
 * @param projectId 项目 ID
 */
export const getEpics = (projectId: number) =>
  api.get<Epic[]>(endpoints.epic.list(projectId));

/**
 * 获取史诗详情
 * @param projectId 项目 ID
 * @param id 史诗 ID
 */
export const getEpic = (projectId: number, id: number) =>
  api.get<Epic>(endpoints.epic.detail(projectId, id));

/**
 * 创建史诗
 * @param projectId 项目 ID
 * @param data 创建数据
 */
export const createEpic = (projectId: number, data: CreateEpicDto) =>
  api.post<Epic>(endpoints.epic.create(projectId), data);

/**
 * 更新史诗
 * @param projectId 项目 ID
 * @param id 史诗 ID
 * @param data 更新数据
 */
export const updateEpic = (projectId: number, id: number, data: UpdateEpicDto) =>
  api.put<Epic>(endpoints.epic.update(projectId, id), data);

/**
 * 删除史诗
 * @param projectId 项目 ID
 * @param id 史诗 ID
 */
export const deleteEpic = (projectId: number, id: number) =>
  api.delete(endpoints.epic.delete(projectId, id));
