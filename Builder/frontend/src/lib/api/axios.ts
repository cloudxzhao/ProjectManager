import axios, { AxiosInstance, AxiosError, AxiosRequestConfig } from 'axios';
import { Result } from '@/types/api';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

// 创建 Axios 实例
export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器 - 自动注入 Token
apiClient.interceptors.request.use(
  (config) => {
    // 优先从 Zustand store 获取 token
    let token: string | null = null;

    if (typeof window !== 'undefined') {
      token = localStorage.getItem('access_token');
    }

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// 响应拦截器 - 统一错误处理
apiClient.interceptors.response.use(
  (response) => {
    // 返回响应数据
    return response.data;
  },
  (error: AxiosError<Result<unknown>>) => {
    // 401 错误 - Token 过期或未授权
    if (error.response?.status === 401) {
      if (typeof window !== 'undefined') {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        // 跳转到登录页
        window.location.href = '/login';
      }
    }

    // 403 错误 - 权限不足
    if (error.response?.status === 403) {
      console.error('权限不足');
    }

    // 404 错误 - 资源不存在
    if (error.response?.status === 404) {
      console.error('资源不存在');
    }

    // 500 错误 - 服务器错误
    if (error.response?.status === 500) {
      console.error('服务器错误');
    }

    return Promise.reject(error);
  }
);

// 封装的请求方法
export const api = {
  get: <T>(url: string, config?: AxiosRequestConfig) =>
    apiClient.get<Result<T>>(url, config).then((res) => res.data),

  post: <T>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
    apiClient.post<Result<T>>(url, data, config).then((res) => res.data),

  put: <T>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
    apiClient.put<Result<T>>(url, data, config).then((res) => res.data),

  patch: <T>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
    apiClient.patch<Result<T>>(url, data, config).then((res) => res.data),

  delete: <T>(url: string, config?: AxiosRequestConfig) =>
    apiClient.delete<Result<T>>(url, config).then((res) => res.data),
};

export default apiClient;
