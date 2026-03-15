// 任务相关类型定义

export interface Task {
  id: number;
  projectId: number;
  title: string;
  description: string;
  status: TaskStatus;
  priority: Priority;
  assigneeId?: number;
  assigneeName?: string;  // 负责人名称（后端返回）
  reporterId?: number;
  storyPoints?: number;
  dueDate?: string;
  tags: string[];
  parentId?: number;
  order: number;
  subtaskCount: number;
  completedSubtaskCount: number;
  commentCount: number;
  userStoryId?: number;
  userStoryTitle?: string;
  epicId?: number;
  epicTitle?: string;
  createdAt: string;
  updatedAt?: string;
}

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE';

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface CreateTaskDto {
  title: string;
  description?: string;
  status?: TaskStatus;
  priority?: Priority;
  assigneeId?: number;
  storyPoints?: number;
  dueDate?: string;
  tags?: string[];
  parentId?: number;  // 关联父任务 ID
  userStoryId?: number;
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
  userStoryId?: number;
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
