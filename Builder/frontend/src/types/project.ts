// 项目相关类型定义

export interface Project {
  id: number;
  name: string;
  description: string;
  status: ProjectStatus;
  startDate: string;
  endDate?: string;
  color: string;
  icon?: string;
  memberCount: number;
  taskCount: number;
  completedTaskCount: number;
  ownerId: number;
  createdAt: string;
  updatedAt?: string;
}

export type ProjectStatus = 'planning' | 'active' | 'completed' | 'archived';

export interface CreateProjectDto {
  name: string;
  description: string;
  status?: ProjectStatus;
  startDate: string;
  endDate?: string;
  color: string;
  icon?: string;
  memberIds?: number[];
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
