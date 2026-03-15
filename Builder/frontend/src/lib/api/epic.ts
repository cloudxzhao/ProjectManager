// 史诗 (Epic) 管理模块 API - 服务管理

import { api } from './axios';
import { endpoints } from './endpoints';

// 后端 PageResult 类型
interface PageResult<T> {
  list: T[];
  total: number;
  page: number;
  size: number;
  pages: number;
}

// 史诗数据结构（与后端 EpicVO 对应）
export interface Epic {
  id: number;
  projectId: number;
  title: string;
  description: string;
  color: string;
  position?: number;
  createdAt: string;
  updatedAt?: string;
}

// 创建请求 DTO（与后端 CreateRequest 对应）
export interface CreateEpicDto {
  title: string;
  description?: string;
  color?: string;
  position?: number;
}

// 更新请求 DTO（与后端 UpdateRequest 对应）
export interface UpdateEpicDto {
  title?: string;
  description?: string;
  color?: string;
  position?: number;
}

/**
 * 获取项目下所有史诗（服务列表）
 * @param projectId 项目 ID
 */
export const getEpics = async (projectId: number) => {
  const res = await api.get<Epic[]>(endpoints.epic.list(projectId));
  return res.data.data || [];
};

/**
 * 获取史诗详情
 * @param projectId 项目 ID
 * @param id 史诗 ID
 */
export const getEpic = async (projectId: number, id: number) => {
  const res = await api.get<Epic>(endpoints.epic.detail(projectId, id));
  return res.data.data;
};

/**
 * 创建史诗（服务）
 * @param projectId 项目 ID
 * @param data 创建数据
 */
export const createEpic = async (projectId: number, data: CreateEpicDto) => {
  const res = await api.post<Epic>(endpoints.epic.create(projectId), data);
  return res.data.data;
};

/**
 * 更新史诗（服务）
 * @param projectId 项目 ID
 * @param id 史诗 ID
 * @param data 更新数据
 */
export const updateEpic = async (projectId: number, id: number, data: UpdateEpicDto) => {
  const res = await api.put<Epic>(endpoints.epic.update(projectId, id), data);
  return res.data.data;
};

/**
 * 删除史诗（服务）
 * @param projectId 项目 ID
 * @param id 史诗 ID
 */
export const deleteEpic = async (projectId: number, id: number) => {
  const res = await api.delete(endpoints.epic.delete(projectId, id));
  return res.data.data;
};
