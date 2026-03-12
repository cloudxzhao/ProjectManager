// Wiki 管理模块 API

import { api } from './axios';
import { endpoints } from './endpoints';

export interface Wiki {
  id: number;
  projectId: number;
  parentDocId?: number;
  title: string;
  content: string;
  summary?: string;
  order: number;
  authorId: number;
  authorName?: string;
  tags: string[];
  isPublished: boolean;
  viewCount: number;
  createdAt: string;
  updatedAt?: string;
}

export interface QueryParams {
  parentDocId?: number;
  authorId?: number;
  isPublished?: boolean;
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}

export interface CreateWikiDto {
  projectId: number;
  parentDocId?: number;
  title: string;
  content?: string;
  summary?: string;
  order?: number;
  tags?: string[];
  isPublished?: boolean;
}

export interface UpdateWikiDto {
  title?: string;
  content?: string;
  summary?: string;
  order?: number;
  tags?: string[];
  isPublished?: boolean;
}

/**
 * 获取项目下所有 Wiki 文档
 * @param projectId 项目 ID
 * @param params 查询参数
 */
export const getWikis = (projectId: number, params?: QueryParams) =>
  api.get<Wiki[]>(endpoints.wiki.list(projectId), { params });

/**
 * 获取 Wiki 文档详情
 * @param projectId 项目 ID
 * @param id 文档 ID
 */
export const getWiki = (projectId: number, id: number) =>
  api.get<Wiki>(endpoints.wiki.detail(projectId, id));

/**
 * 创建 Wiki 文档
 * @param projectId 项目 ID
 * @param data 创建数据
 */
export const createWiki = (projectId: number, data: CreateWikiDto) =>
  api.post<Wiki>(endpoints.wiki.create(projectId), data);

/**
 * 更新 Wiki 文档
 * @param projectId 项目 ID
 * @param id 文档 ID
 * @param data 更新数据
 */
export const updateWiki = (projectId: number, id: number, data: UpdateWikiDto) =>
  api.put<Wiki>(endpoints.wiki.update(projectId, id), data);

/**
 * 删除 Wiki 文档
 * @param projectId 项目 ID
 * @param id 文档 ID
 */
export const deleteWiki = (projectId: number, id: number) =>
  api.delete(endpoints.wiki.delete(projectId, id));
