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
export const getComments = (taskId: number, params?: QueryParams) =>
  api.get<Comment[]>(`/projects/${params?.taskId || taskId}/tasks/${taskId}/comments`, { params });

/**
 * 获取评论详情
 * @param taskId 任务 ID
 * @param id 评论 ID
 */
export const getComment = (taskId: number, id: number) =>
  api.get<Comment>(`/projects/${taskId}/tasks/${taskId}/comments/${id}`);

/**
 * 创建评论
 * @param taskId 任务 ID
 * @param data 创建数据
 */
export const createComment = (taskId: number, data: CreateCommentDto) =>
  api.post<Comment>(`/projects/${taskId}/tasks/${taskId}/comments`, data);

/**
 * 更新评论
 * @param taskId 任务 ID
 * @param id 评论 ID
 * @param data 更新数据
 */
export const updateComment = (taskId: number, id: number, data: UpdateCommentDto) =>
  api.put<Comment>(`/projects/${taskId}/tasks/${taskId}/comments/${id}`, data);

/**
 * 删除评论
 * @param taskId 任务 ID
 * @param id 评论 ID
 */
export const deleteComment = (taskId: number, id: number) =>
  api.delete(`/projects/${taskId}/tasks/${taskId}/comments/${id}`);

/**
 * 点赞评论
 * @param taskId 任务 ID
 * @param id 评论 ID
 */
export const toggleLike = (taskId: number, id: number) =>
  api.post<{ isLiked: boolean }>(`/projects/${taskId}/tasks/${taskId}/comments/${id}/like`);
