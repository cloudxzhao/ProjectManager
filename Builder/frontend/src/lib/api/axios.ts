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
    // 检查响应体中的 code 字段（业务错误码）
    const responseData = response.data as Result<unknown>;
    if (responseData && responseData.code !== 200 && responseData.code !== 201) {
      // 业务错误，根据 code 显示不同提示
      const message = responseData.message || getErrorMessageByCode(responseData.code);
      console.error(`[API Business Error ${responseData.code}]:`, message);
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('api-error', {
          detail: { code: responseData.code, message, httpStatus: response.status }
        }));
      }
      // 返回 reject，让调用方知道出错了
      const error: AxiosError<Result<unknown>> = new AxiosError(message);
      error.response = response;
      error.code = responseData.code.toString();
      return Promise.reject(error);
    }
    // 返回完整响应，在 api 对象的方法中解包
    return response;
  },
  (error: AxiosError<Result<unknown>>) => {
    // HTTP 错误，根据 status 显示不同提示
    const status = error.response?.status;
    const responseData = error.response?.data;
    const message = responseData?.message || getErrorMessageByCode(status) || '网络错误，请稍后重试';

    // 400 错误 - 请求参数错误
    if (status === 400) {
      console.error('[API 400 Error]:', message);
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('api-error', {
          detail: { code: responseData?.code || 400, message, httpStatus: status }
        }));
      }
    }

    // 401 错误 - Token 过期或未授权
    if (status === 401) {
      if (typeof window !== 'undefined') {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        // 跳转到登录页
        window.location.href = '/login';
      }
    }

    // 403 错误 - 权限不足
    if (status === 403) {
      console.error('[API 403 Error]:', message);
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('api-error', {
          detail: { code: responseData?.code || 403, message, httpStatus: status }
        }));
      }
    }

    // 404 错误 - 资源不存在
    if (status === 404) {
      console.error('[API 404 Error]:', message);
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('api-error', {
          detail: { code: responseData?.code || 404, message, httpStatus: status }
        }));
      }
    }

    // 500 错误 - 服务器错误
    if (status === 500) {
      console.error('[API 500 Error]:', message);
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('api-error', {
          detail: { code: responseData?.code || 500, message, httpStatus: status }
        }));
      }
    }

    return Promise.reject(error);
  }
);

// 根据错误码返回友好的错误信息
const getErrorMessageByCode = (code?: number): string => {
  if (!code) return '网络错误，请稍后重试';

  const errorMessages: Record<number, string> = {
    200: '请求成功',
    201: '创建成功',
    400: '请求参数错误，请检查后重试',
    401: '登录已过期，请重新登录',
    403: '权限不足，请联系管理员',
    404: '请求的资源不存在',
    500: '服务器内部错误，技术团队正在处理中',
    502: '网关错误，请稍后重试',
    503: '服务暂时不可用，请稍后重试',
    504: '网关超时，请稍后重试',
  };

  return errorMessages[code] || '网络错误，请稍后重试';
};

// 封装的请求方法
export const api = {
  get: <T>(url: string, config?: AxiosRequestConfig) =>
    apiClient.get<Result<T>>(url, config).then((res) => res),

  post: <T>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
    apiClient.post<Result<T>>(url, data, config).then((res) => res),

  put: <T>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
    apiClient.put<Result<T>>(url, data, config).then((res) => res),

  patch: <T>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
    apiClient.patch<Result<T>>(url, data, config).then((res) => res),

  delete: <T>(url: string, config?: AxiosRequestConfig) =>
    apiClient.delete<Result<T>>(url, config).then((res) => res),
};

export default apiClient;
