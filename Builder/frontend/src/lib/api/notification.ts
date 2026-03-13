// 通知管理 API 客户端

import { api } from './axios';
import { endpoints } from './endpoints';

/** 通知类型 */
export type NotificationType = 'task' | 'comment' | 'project' | 'mention' | 'system';

/** 通知数据结构 */
export interface Notification {
  id: number;
  type: NotificationType;
  title: string;
  content: string;
  projectId?: number;
  projectName?: string;
  isRead: boolean;
  isStarred: boolean;
  createdAt: string;
  updatedAt?: string;
}

/** 创建通知 DTO */
export interface CreateNotificationDto {
  type: NotificationType;
  title: string;
  content: string;
  projectId?: number;
}

/**
 * 获取通知列表
 */
export const getNotifications = async (): Promise<Notification[]> => {
  const result = await api.get<Notification[]>(endpoints.notification.list);
  return result.data || [];
};

/**
 * 获取未读通知列表
 */
export const getUnreadNotifications = async () => {
  const result = await api.get<Notification[]>(endpoints.notification.unread);
  return result || [];
};

/**
 * 标记通知为已读
 * @param id 通知 ID
 */
export const markAsRead = async (id: number) => {
  return api.put<void>(endpoints.notification.markRead(id), {});
};

/**
 * 全部标记为已读
 */
export const markAllAsRead = async () => {
  return api.post<void>(endpoints.notification.markAllRead, {});
};

/**
 * 删除通知
 * @param id 通知 ID
 */
export const deleteNotification = async (id: number) => {
  return api.delete<void>(endpoints.notification.delete(id));
};

/**
 * 切换通知收藏状态
 * @param id 通知 ID
 */
export const toggleStar = async (id: number) => {
  return api.post<Notification>(`${endpoints.notification.delete(id)}/star`, {});
};
