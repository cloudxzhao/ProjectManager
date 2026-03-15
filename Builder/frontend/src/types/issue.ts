// 问题 (Issue) 相关类型定义

export interface Issue {
  id: number;
  projectId: number;
  title: string;
  description: string;
  type: IssueType;
  severity: Severity;
  status: IssueStatus;
  priority: Priority;
  assigneeId?: number;
  reporterId: number;
  resolution?: string;
  resolvedAt?: string;
  dueDate?: string;
  tags: string[];
  createdAt: string;
  updatedAt?: string;
}

export type IssueType = 'BUG' | 'FEATURE' | 'IMPROVEMENT' | 'TASK';

export type Severity = 'LOW' | 'NORMAL' | 'HIGH' | 'CRITICAL';

export type IssueStatus = 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE';

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface CreateIssueDto {
  title: string;
  description?: string;
  type?: IssueType;
  severity?: Severity;
  status?: IssueStatus;
  priority?: Priority;
  assigneeId?: number;
  dueDate?: string;
  tags?: string[];
}

export interface UpdateIssueDto {
  title?: string;
  description?: string;
  type?: IssueType;
  severity?: Severity;
  status?: IssueStatus;
  priority?: Priority;
  assigneeId?: number;
  resolution?: string;
  dueDate?: string;
  tags?: string[];
}

export interface IssueQueryParams {
  type?: IssueType;
  severity?: Severity;
  status?: IssueStatus;
  priority?: Priority;
  assigneeId?: number;
  reporterId?: number;
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}
