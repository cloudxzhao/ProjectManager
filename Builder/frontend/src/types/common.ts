// 通用类型定义

export type CommonStatus = 'active' | 'inactive' | 'deleted';

export interface Timestamps {
  createdAt: string;
  updatedAt: string;
}

export interface BaseEntity {
  id: number;
}

export type Optional<T, K extends keyof T> = Pick<Partial<T>, K> & Omit<T, K>;

export type Nullable<T> = T | null;
