// API 相关类型定义

export interface Result<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export interface ApiResponse<T> {
  data: T;
  success: boolean;
  message?: string;
}

export interface PageInfo<T> {
  items: T extends any[] ? T : T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export interface PaginationParams {
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}

// 通用错误类型
export interface ApiError {
  code: number;
  message: string;
  details?: Record<string, string[]>;
}
