// 问题追踪 (Issue) 管理模块 API

import { api } from './axios';
import { endpoints } from './endpoints';

export interface Issue {
  id: number;
  projectId: number;
  title: string;
  description: string;
  type: IssueType;
  severity: Severity;
  status: IssueStatus;
  priority: Priority;
  assigneeId?: number;
  reporterId: number;
  resolution?: string;
  resolvedAt?: string;
  dueDate?: string;
  tags: string[];
  createdAt: string;
  updatedAt?: string;
}

export type IssueType = 'bug' | 'feature' | 'improvement' | 'task';

export type Severity = 'critical' | 'high' | 'medium' | 'low';

export type IssueStatus = 'open' | 'in_progress' | 'resolved' | 'closed';

export type Priority = 'low' | 'medium' | 'high' | 'urgent';

export interface QueryParams {
  type?: IssueType;
  severity?: Severity;
  status?: IssueStatus;
  priority?: Priority;
  assigneeId?: number;
  reporterId?: number;
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}

export interface CreateIssueDto {
  projectId: number;
  title: string;
  description?: string;
  type?: IssueType;
  severity?: Severity;
  status?: IssueStatus;
  priority?: Priority;
  assigneeId?: number;
  dueDate?: string;
  tags?: string[];
}

export interface UpdateIssueDto {
  title?: string;
  description?: string;
  type?: IssueType;
  severity?: Severity;
  status?: IssueStatus;
  priority?: Priority;
  assigneeId?: number;
  resolution?: string;
  dueDate?: string;
  tags?: string[];
}

/**
 * 获取项目下所有问题
 * @param projectId 项目 ID
 * @param params 查询参数
 */
export const getIssues = (projectId: number, params?: QueryParams) =>
  api.get<Issue[]>(endpoints.issue.list(projectId), { params });

/**
 * 获取问题详情
 * @param projectId 项目 ID
 * @param id 问题 ID
 */
export const getIssue = (projectId: number, id: number) =>
  api.get<Issue>(endpoints.issue.detail(projectId, id));

/**
 * 创建问题
 * @param projectId 项目 ID
 * @param data 创建数据
 */
export const createIssue = (projectId: number, data: CreateIssueDto) =>
  api.post<Issue>(endpoints.issue.create(projectId), data);

/**
 * 更新问题
 * @param projectId 项目 ID
 * @param id 问题 ID
 * @param data 更新数据
 */
export const updateIssue = (projectId: number, id: number, data: UpdateIssueDto) =>
  api.put<Issue>(endpoints.issue.update(projectId, id), data);

/**
 * 删除问题
 * @param projectId 项目 ID
 * @param id 问题 ID
 */
export const deleteIssue = (projectId: number, id: number) =>
  api.delete(endpoints.issue.delete(projectId, id));
