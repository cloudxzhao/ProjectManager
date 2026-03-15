package com.projecthub.module.permission.service;

import com.projecthub.common.response.PageResult;
import com.projecthub.module.permission.dto.ApprovePermissionRequestDTO;
import com.projecthub.module.permission.dto.CreatePermissionRequestDTO;
import com.projecthub.module.permission.vo.AvailablePermissionVO;
import com.projecthub.module.permission.vo.PermissionApprovalVO;
import com.projecthub.module.permission.vo.PermissionRequestVO;
import java.util.List;

/** 权限申请服务接口 */
public interface PermissionRequestService {

  /** 获取可申请的权限列表 */
  List<AvailablePermissionVO> getAvailablePermissions();

  /** 提交权限申请 */
  PermissionRequestVO createPermissionRequest(CreatePermissionRequestDTO dto);

  /** 获取申请列表（支持筛选） */
  PageResult<PermissionRequestVO> getPermissionRequests(
      Integer page, Integer size, String status, Long userId, Long permissionId);

  /** 获取申请详情 */
  PermissionRequestVO getPermissionRequestDetail(Long requestId);

  /** 获取申请详情（包含审批记录） */
  PermissionRequestVO getPermissionRequestWithApprovals(Long requestId);

  /** 审批通过 */
  void approvePermissionRequest(Long requestId, ApprovePermissionRequestDTO dto);

  /** 审批拒绝 */
  void rejectPermissionRequest(Long requestId, ApprovePermissionRequestDTO dto);

  /** 获取我的申请记录 */
  PageResult<PermissionRequestVO> getMyPermissionRequests(
      Integer page, Integer size, String status);

  /** 获取申请的审批记录 */
  List<PermissionApprovalVO> getApprovalRecords(Long requestId);
}
