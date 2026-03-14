// 问题追踪 (Issue) 管理模块 API

import { api } from './axios';
import { endpoints } from './endpoints';
import type { CreateIssueDto, UpdateIssueDto, Issue, IssueType, Severity, IssueStatus, Priority, IssueQueryParams as QueryParams } from '@/types/issue';
export type { Issue, IssueType, Severity, IssueStatus, Priority, QueryParams, CreateIssueDto, UpdateIssueDto };

/**
 * 获取项目下所有问题
 * @param projectId 项目 ID
 * @param params 查询参数
 */
export const getIssues = async (projectId: number, params?: QueryParams) => {
  const res = await api.get<Issue[]>(endpoints.issue.list(projectId), { params });
  console.log('[issue.api] getIssues result:', res);
  // 后端可能返回分页格式 { list, total, page, size } 或直接数组
  const data = res.data.data;
  if (data && Array.isArray(data)) {
    return data;
  }
  // 如果是分页格式，返回 list
  if (data && typeof data === 'object' && 'list' in data) {
    return (data as any).list || [];
  }
  return [];
};

/**
 * 获取问题详情
 * @param projectId 项目 ID
 * @param id 问题 ID
 */
export const getIssue = async (projectId: number, id: number) => {
  const res = await api.get<Issue>(endpoints.issue.detail(projectId, id));
  return res.data.data;
};

/**
 * 创建问题
 * @param projectId 项目 ID
 * @param data 创建数据
 */
export const createIssue = async (projectId: number, data: CreateIssueDto) => {
  const res = await api.post<Issue>(endpoints.issue.create(projectId), data);
  return res.data.data;
};

/**
 * 更新问题
 * @param projectId 项目 ID
 * @param id 问题 ID
 * @param data 更新数据
 */
export const updateIssue = async (projectId: number, id: number, data: UpdateIssueDto) => {
  const res = await api.put<Issue>(endpoints.issue.update(projectId, id), data);
  return res.data.data;
};

/**
 * 删除问题
 * @param projectId 项目 ID
 * @param id 问题 ID
 */
export const deleteIssue = (projectId: number, id: number) =>
  api.delete(endpoints.issue.delete(projectId, id));
