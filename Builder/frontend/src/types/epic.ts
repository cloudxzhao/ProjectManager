// 史诗 (Epic) 相关类型定义

export interface Epic {
  id: number;
  projectId: number;
  name: string;
  description: string;
  color: string;
  startDate: string;
  endDate?: string;
  status: EpicStatus;
  progress: number;
  issueCount: number;
  completedIssueCount: number;
  createdAt: string;
  updatedAt?: string;
}

export type EpicStatus = 'planning' | 'active' | 'completed' | 'archived';

export interface CreateEpicDto {
  projectId: number;
  name: string;
  description?: string;
  color?: string;
  startDate?: string;
  endDate?: string;
  status?: EpicStatus;
}

export interface UpdateEpicDto {
  name?: string;
  description?: string;
  color?: string;
  startDate?: string;
  endDate?: string;
  status?: EpicStatus;
  progress?: number;
}

export interface EpicQueryParams {
  status?: EpicStatus;
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}
