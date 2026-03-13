// 用户管理 API 客户端

import { api } from './axios';
import { endpoints } from './endpoints';

/** 用户数据结构 */
export interface User {
  id: number;
  username: string;
  email: string;
  phone?: string;
  avatar?: string;
  bio?: string;
  company?: string;
  location?: string;
  createdAt: string;
  updatedAt?: string;
}

/** 更新用户资料 DTO */
export interface UpdateProfileDto {
  username?: string;
  bio?: string;
  company?: string;
  location?: string;
}

/** 修改密码 DTO */
export interface ChangePasswordDto {
  currentPassword: string;
  newPassword: string;
}

/**
 * 获取用户 profile
 */
export const getUserProfile = async () => {
  const result = await api.get<User>(endpoints.user.profile);
  return result;
};

/**
 * 更新用户资料
 * @param data 更新数据
 */
export const updateProfile = async (data: UpdateProfileDto) => {
  const result = await api.put<User>(endpoints.user.update, data);
  return result;
};

/**
 * 上传头像
 * @param formData 包含头像文件的 FormData
 */
export const uploadAvatar = async (formData: FormData) => {
  const result = await api.put<{ avatar: string }>(endpoints.user.avatar, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return result;
};

/**
 * 修改密码
 * @param data 密码数据
 */
export const changePassword = async (data: ChangePasswordDto) => {
  return api.put<void>('/user/password', data);
};
