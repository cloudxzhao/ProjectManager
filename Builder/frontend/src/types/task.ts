// 任务相关类型定义

export interface Task {
  id: number;
  projectId: number;
  title: string;
  description: string;
  status: TaskStatus;
  priority: Priority;
  assigneeId?: number;
  reporterId?: number;
  storyPoints?: number;
  dueDate?: string;
  tags: string[];
  parentId?: number;
  order: number;
  subtaskCount: number;
  completedSubtaskCount: number;
  commentCount: number;
  createdAt: string;
  updatedAt?: string;
}

export type TaskStatus = 'todo' | 'in_progress' | 'testing' | 'done';

export type Priority = 'low' | 'medium' | 'high' | 'urgent';

export interface CreateTaskDto {
  projectId: number;
  title: string;
  description?: string;
  status?: TaskStatus;
  priority?: Priority;
  assigneeId?: number;
  storyPoints?: number;
  dueDate?: string;
  tags?: string[];
  parentId?: number;
}

export interface UpdateTaskDto {
  title?: string;
  description?: string;
  status?: TaskStatus;
  priority?: Priority;
  assigneeId?: number;
  storyPoints?: number;
  dueDate?: string;
  tags?: string[];
  order?: number;
}

export interface SubTask {
  id: number;
  taskId: number;
  title: string;
  completed: boolean;
  order: number;
  createdAt: string;
  updatedAt?: string;
}

export interface TaskComment {
  id: number;
  taskId: number;
  userId: number;
  content: string;
  parentId?: number;
  replyCount: number;
  createdAt: string;
  updatedAt?: string;
}
