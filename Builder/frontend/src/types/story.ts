// 用户故事 (User Story) 相关类型定义

export interface UserStory {
  id: number;
  projectId: number;
  epicId?: number;
  title: string;
  description: string;
  status: StoryStatus;
  priority: Priority;
  assigneeId?: number;
  reporterId?: number;
  storyPoints?: number;
  dueDate?: string;
  tags: string[];
  createdAt: string;
  updatedAt?: string;
}

export type StoryStatus = 'todo' | 'in_progress' | 'testing' | 'done';

export type Priority = 'low' | 'medium' | 'high' | 'urgent';

export interface CreateUserStoryDto {
  projectId: number;
  epicId?: number;
  title: string;
  description?: string;
  status?: StoryStatus;
  priority?: Priority;
  assigneeId?: number;
  storyPoints?: number;
  dueDate?: string;
  tags?: string[];
}

export interface UpdateUserStoryDto {
  epicId?: number;
  title?: string;
  description?: string;
  status?: StoryStatus;
  priority?: Priority;
  assigneeId?: number;
  storyPoints?: number;
  dueDate?: string;
  tags?: string[];
  order?: number;
}

export interface StoryQueryParams {
  status?: StoryStatus;
  priority?: Priority;
  assigneeId?: number;
  epicId?: number;
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}
