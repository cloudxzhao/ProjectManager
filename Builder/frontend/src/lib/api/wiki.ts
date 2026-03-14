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
export const getWikis = async (projectId: number, params?: QueryParams) => {
  const res = await api.get<Wiki[]>(endpoints.wiki.list(projectId), { params });
  return res.data.data;
};

/**
 * 获取 Wiki 文档详情
 * @param projectId 项目 ID
 * @param id 文档 ID
 */
export const getWiki = async (projectId: number, id: number) => {
  const res = await api.get<Wiki>(endpoints.wiki.detail(projectId, id));
  return res.data.data;
};

/**
 * 创建 Wiki 文档
 * @param projectId 项目 ID
 * @param data 创建数据
 */
export const createWiki = async (projectId: number, data: CreateWikiDto) => {
  const res = await api.post<Wiki>(endpoints.wiki.create(projectId), data);
  return res.data.data;
};

/**
 * 更新 Wiki 文档
 * @param projectId 项目 ID
 * @param id 文档 ID
 * @param data 更新数据
 */
export const updateWiki = async (projectId: number, id: number, data: UpdateWikiDto) => {
  const res = await api.put<Wiki>(endpoints.wiki.update(projectId, id), data);
  return res.data.data;
};

/**
 * 删除 Wiki 文档
 * @param projectId 项目 ID
 * @param id 文档 ID
 */
export const deleteWiki = (projectId: number, id: number) =>
  api.delete(endpoints.wiki.delete(projectId, id));
