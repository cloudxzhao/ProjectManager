// 项目管理 API 客户端

import { api } from './axios';
import { endpoints } from './endpoints';
import type { CreateProjectDto, UpdateProjectDto, ProjectMember, MemberRole } from '@/types/project';
import type { ProjectStatus } from '@/types/project';

// 导出类型供外部使用
export type { ProjectStatus };

/** 后端返回的项目数据结构 */
interface ProjectResponse {
  id: number | string;
  name: string;
  description: string;
  status: ProjectStatus;
  startDate: string | { year: number; month: number; day: number };
  endDate: string | { year: number; month: number; day: number };
  themeColor: string;
  icon?: string;
  memberCount: number;
  taskCount: number;
  completedTaskCount: number;
  ownerId: number | string;
  createdAt: string | { year: number; month: number; day: number; hour: number; minute: number; second: number };
  updatedAt?: string;
}

/** 前端项目数据结构 */
export interface Project {
  id: number;
  name: string;
  description: string;
  status: ProjectStatus;
  startDate: string;
  endDate: string;
  color: string;
  icon?: string;
  memberCount: number;
  taskCount: number;
  completedTaskCount: number;
  ownerId: number;
  createdAt: string;
  updatedAt?: string;
}

/** 将后端数据转换为前端格式 */
const mapProjectResponse = (response: ProjectResponse): Project => {
  // 处理日期格式（后端可能返回 LocalDate 对象或字符串）
  const formatDate = (date: any): string => {
    if (!date) return '';
    if (typeof date === 'string') return date;
    if (typeof date === 'object' && date.year) {
      const month = String(date.month || 1).padStart(2, '0');
      const day = String(date.day || 1).padStart(2, '0');
      return `${date.year}-${month}-${day}`;
    }
    return String(date);
  };

  return {
    id: typeof response.id === 'string' ? parseInt(response.id, 10) : (response.id as number),
    name: response.name || '',
    description: response.description || '',
    status: (response.status as ProjectStatus) || 'ACTIVE',
    startDate: formatDate(response.startDate),
    endDate: formatDate(response.endDate),
    color: response.themeColor || '#1677FF', // 后端 themeColor -> 前端 color，默认蓝色
    icon: response.icon || '🛒', // 默认图标
    memberCount: response.memberCount || 0,
    taskCount: response.taskCount || 0,
    completedTaskCount: response.completedTaskCount || 0,
    ownerId: typeof response.ownerId === 'string' ? parseInt(response.ownerId, 10) : (response.ownerId as number),
    createdAt: formatDate(response.createdAt),
    updatedAt: response.updatedAt,
  };
};

/**
 * 获取项目列表
 * @param page 页码
 * @param pageSize 每页数量
 * @param keyword 搜索关键词
 * @param status 状态过滤
 * @param sort 排序字段
 * @param order 排序方向
 */
export const getProjects = async (
  page: number = 1,
  pageSize: number = 20,
  keyword?: string,
  status?: ProjectStatus,
  sort?: string,
  order?: string
) => {
  const params = new URLSearchParams();
  params.append('page', page.toString());
  params.append('size', pageSize.toString());
  if (keyword) {
    params.append('keyword', keyword);
  }
  if (status) {
    params.append('status', status);
  }
  if (sort) {
    params.append('sort', sort);
  }
  if (order) {
    params.append('order', order);
  }

  const result = await api.get<{
    list: ProjectResponse[];
    total: number;
    page: number;
    size: number;
    pages: number;
  }>(endpoints.project.list, { params });

  console.log('[project.api] getProjects result:', result);

  return {
    list: result.list.map(mapProjectResponse),
    total: result.total,
    page: result.page,
    size: result.size,
    pages: result.pages,
  };
};

/**
 * 获取项目详情
 * @param id 项目 ID
 */
export const getProject = async (id: number) => {
  const result = await api.get<ProjectResponse>(endpoints.project.detail(id));
  return mapProjectResponse(result);
};

/**
 * 创建项目
 * @param data 项目数据
 */
export const createProject = async (data: CreateProjectDto) => {
  // 转换前端字段到后端字段
  const requestBody = {
    name: data.name,
    description: data.description || '',
    startDate: data.startDate,
    endDate: data.endDate || data.startDate,
    icon: data.icon || '',
    themeColor: data.color,
  };
  return api.post<ProjectResponse>(endpoints.project.create, requestBody).then((res) => ({
    ...res,
    data: mapProjectResponse(res.data),
  }));
};

/**
 * 更新项目
 * @param id 项目 ID
 * @param data 更新数据
 */
export const updateProject = async (id: number, data: UpdateProjectDto) => {
  const requestBody: Record<string, any> = {};
  if (data.name) requestBody.name = data.name;
  if (data.description !== undefined) requestBody.description = data.description;
  if (data.status) requestBody.status = data.status;
  if (data.startDate) requestBody.startDate = data.startDate;
  if (data.endDate) requestBody.endDate = data.endDate;
  if (data.color) requestBody.themeColor = data.color;
  if (data.icon) requestBody.icon = data.icon;

  return api.put<ProjectResponse>(endpoints.project.update(id), requestBody).then((res) => ({
    ...res,
    data: mapProjectResponse(res.data),
  }));
};

/**
 * 删除项目
 * @param id 项目 ID
 */
export const deleteProject = async (id: number) => {
  return api.delete<void>(endpoints.project.delete(id));
};

/**
 * 获取项目成员列表
 * @param id 项目 ID
 */
export const getProjectMembers = async (id: number) => {
  return api.get<ProjectMember[]>(endpoints.project.members(id));
};

/**
 * 添加项目成员
 * @param id 项目 ID
 * @param member 成员信息
 */
export const addProjectMember = async (
  id: number,
  member: { userId: number; role: MemberRole }
) => {
  return api.post<void>(endpoints.project.addMember(id), member);
};

/**
 * 移除项目成员
 * @param projectId 项目 ID
 * @param userId 用户 ID
 */
export const removeProjectMember = async (projectId: number, userId: number) => {
  return api.delete<void>(endpoints.project.removeMember(projectId, userId));
};

/**
 * 获取项目统计信息
 */
export const getProjectStats = async () => {
  return api.get<{ activeCount: number; completedCount: number; archivedCount: number; planningCount: number }>(endpoints.project.stats);
};
