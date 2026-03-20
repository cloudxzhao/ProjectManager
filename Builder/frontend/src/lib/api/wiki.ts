// Wiki 管理模块 API

import { api } from './axios';
import { endpoints } from './endpoints';
import type {
  Wiki,
  WikiDetail,
  CreateWikiDto,
  UpdateWikiDto,
  MoveWikiDto,
  WikiVersion,
  VersionDiff,
  WikiSearchResult,
  WikiQueryParams,
} from '@/types/wiki';

export type {
  Wiki,
  WikiDetail,
  CreateWikiDto,
  UpdateWikiDto,
  MoveWikiDto,
  WikiVersion,
  VersionDiff,
  WikiSearchResult,
  WikiQueryParams,
};

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
  const data = res.data.data;

  if (data && Array.isArray(data)) {
    return data;
  }

  if (data && typeof data === 'object' && 'list' in data) {
    return (data as any).list || [];
  }

  return [];
};

/**
 * 获取项目下所有 Wiki 文档
 * @param projectId 项目 ID
 * @param params 查询参数
 */
export const getWikis = async (projectId: number, params?: WikiQueryParams) => {
  const res = await api.get<Wiki[]>(endpoints.wiki.list(projectId), { params });
  const data = res.data.data;
  if (data && Array.isArray(data)) {
    return data;
  }
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
  const res = await api.get<WikiDetail>(endpoints.wiki.detail(projectId, id));
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

/**
 * 移动 Wiki 文档
 * @param projectId 项目 ID
 * @param id 文档 ID
 * @param data 移动数据
 */
export const moveWiki = async (projectId: number, id: number, data: MoveWikiDto) => {
  const res = await api.put<Wiki>(endpoints.wiki.move(projectId, id), data);
  return res.data.data;
};

/**
 * 搜索 Wiki 文档
 * @param projectId 项目 ID
 * @param keyword 关键词
 * @param limit 限制数量
 */
export const searchWiki = async (
  projectId: number,
  keyword: string,
  limit?: number
): Promise<WikiSearchResult[]> => {
  const res = await api.post<WikiSearchResult[]>(
    endpoints.wiki.search(projectId),
    null,
    { params: { keyword, limit } }
  );
  return res.data.data || [];
};

/**
 * 检查是否有子文档
 * @param projectId 项目 ID
 * @param id 文档 ID
 */
export const hasWikiChildren = async (projectId: number, id: number): Promise<boolean> => {
  const res = await api.get<boolean>(endpoints.wiki.hasChildren(projectId, id));
  return res.data.data;
};

/**
 * 获取版本列表
 * @param projectId 项目 ID
 * @param wikiId 文档 ID
 */
export const getWikiVersions = async (projectId: number, wikiId: number) => {
  const res = await api.get<WikiVersion[]>(endpoints.wiki.versions(projectId, wikiId));
  return res.data.data || [];
};

/**
 * 版本对比
 * @param projectId 项目 ID
 * @param wikiId 文档 ID
 * @param versionId 版本 ID
 * @param compareVersionId 对比版本 ID
 */
export const diffWikiVersions = async (
  projectId: number,
  wikiId: number,
  versionId?: number,
  compareVersionId?: number
) => {
  const res = await api.get<VersionDiff>(endpoints.wiki.diff(projectId, wikiId), {
    params: { versionId, compareVersionId },
  });
  return res.data.data;
};

/**
 * 恢复版本
 * @param projectId 项目 ID
 * @param wikiId 文档 ID
 * @param versionId 版本 ID
 * @param changeLog 变更日志
 */
export const restoreWikiVersion = async (
  projectId: number,
  wikiId: number,
  versionId: number,
  changeLog?: string
) => {
  const res = await api.post<WikiVersion>(
    endpoints.wiki.restore(projectId, wikiId, versionId),
    { changeLog }
  );
  return res.data.data;
};

/**
 * 获取文档历史
 * @param projectId 项目 ID
 * @param id 文档 ID
 */
export const getWikiHistory = async (projectId: number, id: number) => {
  const res = await api.get<any[]>(endpoints.wiki.history(projectId, id));
  return res.data.data || [];
};