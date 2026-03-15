// 权限申请 API 客户端

import { api } from './axios';
import { endpoints } from './endpoints';
import type {
  AvailablePermission,
  PermissionRequest,
  PermissionApproval,
  CreatePermissionRequestDTO,
  ApprovePermissionRequestDTO,
  PermissionRequestListParams,
  PermissionRequestListResponse,
} from '@/types/permission';

/**
 * 获取可申请的权限列表
 */
export const getAvailablePermissions = async () => {
  const result = await api.get<AvailablePermission[]>(endpoints.permission.available);
  return result.data.data;
};

/**
 * 创建权限申请
 * @param data 申请数据
 */
export const createPermissionRequest = async (data: CreatePermissionRequestDTO) => {
  const result = await api.post<PermissionRequest>(endpoints.permission.requests, data);
  return result.data.data;
};

/**
 * 获取权限申请列表（支持筛选）
 * @param params 查询参数
 */
export const getPermissionRequests = async (params: PermissionRequestListParams) => {
  const result = await api.get<PermissionRequestListResponse>(endpoints.permission.requests, {
    params: {
      page: params.page || 1,
      size: params.size || 10,
      status: params.status,
      userId: params.userId,
      permissionId: params.permissionId,
    },
  });
  return result.data.data;
};

/**
 * 获取我的权限申请记录
 * @param page 页码
 * @param size 每页数量
 * @param status 状态过滤
 */
export const getMyPermissionRequests = async (
  page: number = 1,
  size: number = 10,
  status?: string
) => {
  const result = await api.get<PermissionRequestListResponse>(endpoints.permission.myRequests, {
    params: { page, size, status },
  });
  return result.data.data;
};

/**
 * 获取权限申请详情
 * @param id 申请 ID
 */
export const getPermissionRequestDetail = async (id: number) => {
  const result = await api.get<PermissionRequest>(endpoints.permission.requestDetail(id));
  return result.data.data;
};

/**
 * 审批通过权限申请
 * @param id 申请 ID
 * @param data 审批数据
 */
export const approvePermissionRequest = async (
  id: number,
  data: ApprovePermissionRequestDTO
) => {
  const result = await api.put<void>(endpoints.permission.approve(id), data);
  return result.data;
};

/**
 * 审批拒绝权限申请
 * @param id 申请 ID
 * @param data 审批数据
 */
export const rejectPermissionRequest = async (
  id: number,
  data: ApprovePermissionRequestDTO
) => {
  const result = await api.put<void>(endpoints.permission.reject(id), data);
  return result.data;
};

/**
 * 获取权限申请审批记录
 * @param id 申请 ID
 */
export const getApprovalRecords = async (id: number) => {
  const result = await api.get<PermissionApproval[]>(endpoints.permission.approvals(id));
  return result.data.data;
};
