'use client';

import { useAuthStore } from '@/stores/auth.store';
import { LoginCredentials, RegisterData } from '@/types/user';

/**
 * 认证相关 Hook
 */
export function useAuth() {
  const {
    user,
    token,
    isAuthenticated,
    isLoading,
    error,
    login,
    register,
    logout,
    updateUser,
    clearError,
  } = useAuthStore();

  return {
    user,
    token,
    isAuthenticated,
    isLoading,
    error,
    login,
    register,
    logout,
    updateUser,
    clearError,
  };
}

/**
 * 检查是否已登录
 */
export function useRequireAuth(redirectTo = '/login') {
  const { isAuthenticated, isLoading } = useAuth();

  return {
    isReady: !isLoading && isAuthenticated,
    isLoading,
    isAuthenticated,
  };
}
