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
  // 扩展字段：接口列表（用于展开显示）
  apis?: ServiceApi[];
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

// ==================== 服务接口管理 API ====================

// 服务接口数据结构
export interface ServiceApi {
  id: number;
  epicId: number;
  name: string;
  path: string;
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
  description?: string;
  status: 'active' | 'deprecated' | 'draft';
  createdAt: string;
  updatedAt?: string;
}

// 创建服务接口请求 DTO
export interface CreateServiceApiDto {
  name: string;
  path: string;
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
  description?: string;
}

// 更新服务接口请求 DTO
export interface UpdateServiceApiDto {
  name?: string;
  path?: string;
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
  description?: string;
  status?: 'active' | 'deprecated' | 'draft';
}

/**
 * 获取服务下的接口列表
 * @param projectId 项目 ID
 * @param epicId 服务 ID
 */
export const getServiceApis = async (projectId: number, epicId: number) => {
  const res = await api.get<ServiceApi[]>(`/projects/${projectId}/epics/${epicId}/apis`);
  return res.data.data || [];
};

/**
 * 创建服务接口
 * @param projectId 项目 ID
 * @param epicId 服务 ID
 * @param data 创建数据
 */
export const createServiceApi = async (projectId: number, epicId: number, data: CreateServiceApiDto) => {
  const res = await api.post<ServiceApi>(`/projects/${projectId}/epics/${epicId}/apis`, data);
  return res.data.data;
};

/**
 * 更新服务接口
 * @param projectId 项目 ID
 * @param epicId 服务 ID
 * @param apiId 接口 ID
 * @param data 更新数据
 */
export const updateServiceApi = async (projectId: number, epicId: number, apiId: number, data: UpdateServiceApiDto) => {
  const res = await api.put<ServiceApi>(`/projects/${projectId}/epics/${epicId}/apis/${apiId}`, data);
  return res.data.data;
};

/**
 * 删除服务接口
 * @param projectId 项目 ID
 * @param epicId 服务 ID
 * @param apiId 接口 ID
 */
export const deleteServiceApi = async (projectId: number, epicId: number, apiId: number) => {
  const res = await api.delete(`/projects/${projectId}/epics/${epicId}/apis/${apiId}`);
  return res.data.data;
};
