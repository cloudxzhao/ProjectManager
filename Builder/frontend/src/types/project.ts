// 项目相关类型定义

export interface Project {
  id: number;
  name: string;
  description: string;
  status: ProjectStatus;
  startDate: string;
  endDate: string;
  color: string; // 后端返回的是 themeColor，通过映射转换
  icon?: string;
  memberCount: number;
  taskCount: number;
  completedTaskCount: number;
  ownerId: number;
  createdAt: string;
  updatedAt?: string;
}

/** 后端返回的项目数据结构（用于 API 响应） */
export interface ProjectResponse {
  id: number;
  name: string;
  description: string;
  status: ProjectStatus;
  startDate: string;
  endDate: string;
  themeColor: string;
  icon?: string;
  memberCount: number;
  taskCount: number;
  completedTaskCount: number;
  ownerId: number;
  createdAt: string;
  updatedAt?: string;
}

export type ProjectStatus = 'PLANNING' | 'ACTIVE' | 'COMPLETED' | 'ARCHIVED';

export interface CreateProjectDto {
  name: string;
  description: string;
  status?: ProjectStatus;
  startDate: string;
  endDate: string;
  color: string;
  icon?: string;
  memberIds?: number[];
}

/** 后端创建项目请求 DTO */
export interface CreateProjectRequest {
  name: string;
  description: string;
  startDate: string;
  endDate: string;
  icon: string;
  themeColor: string;
}

export interface UpdateProjectDto {
  name?: string;
  description?: string;
  status?: ProjectStatus;
  startDate?: string;
  endDate?: string;
  color?: string;
  icon?: string;
}

export interface ProjectMember {
  id: number;
  userId: number;
  projectId: number;
  role: MemberRole;
  joinedAt: string;
}

export type MemberRole = 'owner' | 'admin' | 'member';

/** 项目统计信息 */
export interface ProjectStats {
  activeCount: number;
  completedCount: number;
  archivedCount: number;
  planningCount: number;
}
