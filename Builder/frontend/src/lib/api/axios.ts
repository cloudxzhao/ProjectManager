import axios, { AxiosInstance, AxiosError, AxiosRequestConfig } from 'axios';
import { Result } from '@/types/api';

// 使用相对路径，通过 Nginx 反向代理到后端 API
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || '/api/v1';

// 创建 Axios 实例
export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 辅助函数：从 cookie 获取 token
const getCookieValue = (name: string): string | null => {
  if (typeof document === 'undefined') return null;
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    const cookieValue = parts.pop()?.split(';').shift();
    if (cookieValue) {
      try {
        // auth-storage cookie 的值是 JSON 格式
        const parsed = JSON.parse(decodeURIComponent(cookieValue));
        return parsed.token || null;
      } catch {
        return cookieValue || null;
      }
    }
  }
  return null;
};

// 请求拦截器 - 自动注入 Token
apiClient.interceptors.request.use(
  (config) => {
    // 优先从 localStorage 获取 token，如果没有则从 cookie 获取
    let token: string | null = null;

    if (typeof window !== 'undefined') {
      // 1. 先尝试从 localStorage 获取
      token = localStorage.getItem('access_token');

      // 2. 如果 localStorage 没有，尝试从 cookie 获取（双重保障）
      if (!token) {
        token = getCookieValue('auth-storage');
        // 如果从 cookie 获取到 token，同步到 localStorage
        if (token) {
          localStorage.setItem('access_token', token);
          console.log('[API] Token restored from cookie to localStorage');
        }
      }
    }

    if (token) {
      // 使用 set 方法确保 header 正确设置
      config.headers.set('Authorization', `Bearer ${token}`);
    } else {
      console.warn('[API] No token found for request:', config.url);
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
    apiClient.get<Result<T>>(url, config) as Promise<Result<T>>,

  post: <T>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
    apiClient.post<Result<T>>(url, data, config) as Promise<Result<T>>,

  put: <T>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
    apiClient.put<Result<T>>(url, data, config) as Promise<Result<T>>,

  patch: <T>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
    apiClient.patch<Result<T>>(url, data, config) as Promise<Result<T>>,

  delete: <T>(url: string, config?: AxiosRequestConfig) =>
    apiClient.delete<Result<T>>(url, config) as Promise<Result<T>>,
};

export default apiClient;
