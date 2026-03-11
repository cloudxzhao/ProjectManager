// 任务相关类型定义

export interface Task {
  id: string;
  projectId: string;
  title: string;
  description: string;
  status: TaskStatus;
  priority: Priority;
  assigneeId?: string;
  reporterId?: string;
  storyPoints?: number;
  dueDate?: string;
  tags: string[];
  parentId?: string;
  order: number;
  subtaskCount: number;
  completedSubtaskCount: number;
  commentCount: number;
  createdAt: string;
  updatedAt: string;
}

export type TaskStatus = 'todo' | 'in_progress' | 'testing' | 'done';

export type Priority = 'low' | 'medium' | 'high' | 'urgent';

export interface CreateTaskDto {
  projectId: string;
  title: string;
  description?: string;
  status?: TaskStatus;
  priority?: Priority;
  assigneeId?: string;
  storyPoints?: number;
  dueDate?: string;
  tags?: string[];
  parentId?: string;
}

export interface UpdateTaskDto {
  title?: string;
  description?: string;
  status?: TaskStatus;
  priority?: Priority;
  assigneeId?: string;
  storyPoints?: number;
  dueDate?: string;
  tags?: string[];
  order?: number;
}

export interface SubTask {
  id: string;
  taskId: string;
  title: string;
  completed: boolean;
  order: number;
  createdAt: string;
  updatedAt: string;
}

export interface TaskComment {
  id: string;
  taskId: string;
  userId: string;
  content: string;
  parentId?: string;
  replyCount: number;
  createdAt: string;
  updatedAt: string;
}
