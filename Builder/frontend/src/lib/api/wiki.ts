// Wiki 管理模块 API

import { api } from './axios';
import { endpoints } from './endpoints';
import type { Wiki, CreateWikiDto, UpdateWikiDto, WikiQueryParams as QueryParams } from '@/types/wiki';
export type { Wiki, QueryParams, CreateWikiDto, UpdateWikiDto };

export interface WikiTreeNode {
  key: string | number;
  title: React.ReactNode;
  icon?: React.ReactNode;
  isLeaf?: boolean;
  children?: WikiTreeNode[];
  data?: Wiki;
}

/**
 * 获取项目下所有 Wiki 文档（树形结构）
 * @param projectId 项目 ID
 */
export const getWikiTree = async (projectId: number) => {
  const res = await api.get<Wiki[]>(endpoints.wiki.list(projectId));
  console.log('[wiki.api] getWikiTree result:', res);
  console.log('[wiki.api] getWikiTree response data:', res.data.data);

  const data = res.data.data;

  // 如果是数组格式，直接返回
  if (data && Array.isArray(data)) {
    console.log('[wiki.api] Wiki data is array, length:', data.length);
    return data;
  }

  // 如果是分页格式 { list, total, ... }，返回 list
  if (data && typeof data === 'object' && 'list' in data) {
    console.log('[wiki.api] Wiki data is paginated, list length:', (data as any).list?.length);
    return (data as any).list || [];
  }

  console.warn('[wiki.api] Unexpected wiki data format:', data);
  return [];
};

/**
 * 获取项目下所有 Wiki 文档
 * @param projectId 项目 ID
 * @param params 查询参数
 */
export const getWikis = async (projectId: number, params?: QueryParams) => {
  const res = await api.get<Wiki[]>(endpoints.wiki.list(projectId), { params });
  console.log('[wiki.api] getWikis result:', res);
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
