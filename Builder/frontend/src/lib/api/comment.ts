// 评论 (Comment) 管理模块 API

import { api } from './axios';

export interface Comment {
  id: number;
  taskId: number;
  userId: number;
  userName?: string;
  userAvatar?: string;
  content: string;
  parentId?: number;
  replyCount: number;
  likeCount: number;
  isLiked?: boolean;
  isEdited: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateCommentDto {
  taskId: number;
  content: string;
  parentId?: number;
}

export interface UpdateCommentDto {
  content: string;
}

export interface QueryParams {
  taskId: number;
  parentId?: number;
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}

/**
 * 获取任务评论列表
 * @param taskId 任务 ID
 * @param params 查询参数
 */
export const getComments = async (taskId: number, params?: QueryParams) => {
  const res = await api.get<Comment[]>(`/projects/${params?.taskId || taskId}/tasks/${taskId}/comments`, { params });
  return res.data.data;
};

/**
 * 获取评论详情
 * @param taskId 任务 ID
 * @param id 评论 ID
 */
export const getComment = async (taskId: number, id: number) => {
  const res = await api.get<Comment>(`/projects/${taskId}/tasks/${taskId}/comments/${id}`);
  return res.data.data;
};

/**
 * 创建评论
 * @param taskId 任务 ID
 * @param data 创建数据
 */
export const createComment = async (taskId: number, data: CreateCommentDto) => {
  const res = await api.post<Comment>(`/projects/${taskId}/tasks/${taskId}/comments`, data);
  return res.data.data;
};

/**
 * 更新评论
 * @param taskId 任务 ID
 * @param id 评论 ID
 * @param data 更新数据
 */
export const updateComment = async (taskId: number, id: number, data: UpdateCommentDto) => {
  const res = await api.put<Comment>(`/projects/${taskId}/tasks/${taskId}/comments/${id}`, data);
  return res.data.data;
};

/**
 * 删除评论
 * @param taskId 任务 ID
 * @param id 评论 ID
 */
export const deleteComment = async (taskId: number, id: number) =>
  api.delete(`/projects/${taskId}/tasks/${taskId}/comments/${id}`);

/**
 * 点赞评论
 * @param taskId 任务 ID
 * @param id 评论 ID
 */
export const toggleLike = async (taskId: number, id: number) => {
  const res = await api.post<{ isLiked: boolean }>(`/projects/${taskId}/tasks/${taskId}/comments/${id}/like`);
  return res.data.data;
};