// 项目相关类型定义

export interface Project {
  id: string;
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
  ownerId: string;
  createdAt: string;
  updatedAt: string;
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
  memberIds?: string[];
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
  id: string;
  userId: string;
  projectId: string;
  role: MemberRole;
  joinedAt: string;
}

export type MemberRole = 'owner' | 'admin' | 'member';
