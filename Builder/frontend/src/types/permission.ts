// 权限申请相关类型定义

import type { BaseEntity, Timestamps } from './common';

/** 权限申请状态 */
export type PermissionRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

/** 审批操作类型 */
export type ApprovalAction = 'APPROVE' | 'REJECT';

/** 可申请的权限 */
export interface AvailablePermission extends BaseEntity {
  id: number;
  name: string;
  code: string;
  description: string;
  hasPermission: boolean;
}

/** 权限申请记录 */
export interface PermissionRequest extends BaseEntity, Timestamps {
  userId: number;
  username: string;
  nickname: string;
  avatar?: string;
  permissionId: number;
  permissionName: string;
  permissionCode: string;
  projectId?: number;
  projectName?: string;
  reason: string;
  status: PermissionRequestStatus;
  approvalRecords: PermissionApproval[];
}

/** 审批记录 */
export interface PermissionApproval extends BaseEntity, Timestamps {
  requestId: number;
  approverId: number;
  approverName: string;
  approverNickname: string;
  action: ApprovalAction;
  comment?: string;
}

/** 创建权限申请 DTO */
export interface CreatePermissionRequestDTO {
  permissionId: number;
  projectId?: number;
  reason: string;
}

/** 审批权限申请 DTO */
export interface ApprovePermissionRequestDTO {
  action: ApprovalAction;
  comment: string;
}

/** 权限申请列表查询参数 */
export interface PermissionRequestListParams {
  page?: number;
  size?: number;
  status?: PermissionRequestStatus;
  userId?: number;
  permissionId?: number;
}

/** 权限申请列表响应 */
export interface PermissionRequestListResponse {
  list: PermissionRequest[];
  total: number;
  page: number;
  size: number;
  pages: number;
}
