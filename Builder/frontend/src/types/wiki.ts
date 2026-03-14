// Wiki 相关类型定义

export interface Wiki {
  id: number;
  projectId: number;
  parentDocId?: number;
  title: string;
  content: string;
  summary?: string;
  order: number;
  authorId: number;
  authorName?: string;
  tags: string[];
  isPublished: boolean;
  viewCount: number;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateWikiDto {
  parentDocId?: number;
  title: string;
  content?: string;
  summary?: string;
  order?: number;
  tags?: string[];
  isPublished?: boolean;
}

export interface UpdateWikiDto {
  title?: string;
  content?: string;
  summary?: string;
  order?: number;
  tags?: string[];
  isPublished?: boolean;
}

export interface WikiQueryParams {
  parentDocId?: number;
  authorId?: number;
  isPublished?: boolean;
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}
