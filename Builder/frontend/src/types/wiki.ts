// Wiki 相关类型定义

/** Wiki 文档状态 */
export enum WikiStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  ARCHIVED = 'ARCHIVED',
}

/** Wiki 变更类型 */
export enum WikiChangeType {
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  RESTORE = 'RESTORE',
}

/** Wiki 文档 */
export interface Wiki {
  id: number;
  projectId: number;
  parentId?: number;
  title: string;
  content: string;
  contentHtml?: string;
  summary?: string;
  authorId: number;
  authorName?: string;
  version: number;
  orderNum: number;
  status: WikiStatus;
  parentPath?: string;
  level: number;
  viewCount: number;
  hasChildren?: boolean;
  children?: Wiki[];
  createdAt: string;
  updatedAt?: string;
}

/** Wiki 文档详情 */
export interface WikiDetail {
  id: number;
  projectId: number;
  parentId?: number;
  title: string;
  content: string;
  contentHtml?: string;
  summary?: string;
  authorId: number;
  authorName?: string;
  version: number;
  orderNum: number;
  status: WikiStatus;
  parentPath?: string;
  level: number;
  viewCount: number;
  hasChildren: boolean;
  children?: WikiDetail[];
  createdAt: string;
  updatedAt?: string;
}

/** 创建 Wiki 请求 */
export interface CreateWikiDto {
  parentId?: number;
  title: string;
  content?: string;
  status?: WikiStatus;
}

/** 更新 Wiki 请求 */
export interface UpdateWikiDto {
  title?: string;
  content?: string;
  status?: WikiStatus;
  changeLog?: string;
}

/** 移动 Wiki 请求 */
export interface MoveWikiDto {
  parentId?: number;
  orderNum?: number;
}

/** Wiki 版本信息 */
export interface WikiVersion {
  id?: number;
  documentId: number;
  version: number;
  content: string;
  contentHtml?: string;
  changeLog?: string;
  changeType: WikiChangeType;
  userId: number;
  userName?: string;
  createdAt: string;
}

/** 版本对比结果 */
export interface VersionDiff {
  documentId: number;
  title: string;
  oldVersion: number;
  newVersion: number;
  oldContent: string;
  newContent: string;
  diffLines: DiffLine[];
  diffHtml: string;
  addedLines: number;
  removedLines: number;
  changedLines: number;
}

/** 差异行 */
export interface DiffLine {
  content: string;
  type: 'ADDED' | 'REMOVED' | 'UNCHANGED';
  oldLineNumber: number;
  newLineNumber: number;
}

/** Wiki 搜索结果 */
export interface WikiSearchResult {
  id: number;
  projectId: number;
  title: string;
  summary?: string;
  highlight?: string;
  status: WikiStatus;
  authorId: number;
  authorName?: string;
  viewCount: number;
  createdAt: string;
  updatedAt?: string;
}

/** Wiki 查询参数 */
export interface WikiQueryParams {
  parentId?: number;
  authorId?: number;
  status?: WikiStatus;
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}