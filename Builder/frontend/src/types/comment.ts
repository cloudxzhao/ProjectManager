// 评论 (Comment) 相关类型定义

export interface Comment {
  id: number;
  taskId: number;
  userId: number;
  userName?: string;
  userAvatar?: string;
  content: string;
  parentId?: number;
  replyCount: number;
  likeCount: number;
  isLiked?: boolean;
  isEdited: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateCommentDto {
  taskId: number;
  content: string;
  parentId?: number;
}

export interface UpdateCommentDto {
  content: string;
}

export interface CommentQueryParams {
  taskId: number;
  parentId?: number;
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}
