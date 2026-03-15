package com.projecthub.module.permission.controller;

import com.projecthub.common.response.PageResult;
import com.projecthub.common.response.Result;
import com.projecthub.module.permission.dto.ApprovePermissionRequestDTO;
import com.projecthub.module.permission.dto.CreatePermissionRequestDTO;
import com.projecthub.module.permission.service.PermissionRequestService;
import com.projecthub.module.permission.vo.AvailablePermissionVO;
import com.projecthub.module.permission.vo.PermissionApprovalVO;
import com.projecthub.module.permission.vo.PermissionRequestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 权限申请控制器 */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "权限申请管理", description = "权限申请和审批相关接口")
public class PermissionRequestController {

  private final PermissionRequestService permissionRequestService;

  /** 获取可申请的权限列表 */
  @GetMapping("/available")
  @Operation(summary = "获取可申请的权限列表", description = "获取所有可申请的系统权限")
  public Result<List<AvailablePermissionVO>> getAvailablePermissions() {
    List<AvailablePermissionVO> permissions = permissionRequestService.getAvailablePermissions();
    return Result.success(permissions);
  }

  /** 提交权限申请 */
  @PostMapping("/requests")
  @Operation(summary = "提交权限申请", description = "用户提交权限申请")
  public Result<PermissionRequestVO> createPermissionRequest(
      @RequestBody @Valid CreatePermissionRequestDTO dto) {
    PermissionRequestVO request = permissionRequestService.createPermissionRequest(dto);
    return Result.success(request);
  }

  /** 获取申请列表 */
  @GetMapping("/requests")
  @Operation(summary = "获取权限申请列表", description = "支持按用户、权限、状态筛选")
  public Result<PageResult<PermissionRequestVO>> getPermissionRequests(
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) Long permissionId) {
    PageResult<PermissionRequestVO> result =
        permissionRequestService.getPermissionRequests(page, size, status, userId, permissionId);
    return Result.success(result);
  }

  /** 获取我的申请记录 */
  @GetMapping("/requests/my")
  @Operation(summary = "获取我的申请记录", description = "获取当前用户的权限申请记录")
  public Result<PageResult<PermissionRequestVO>> getMyPermissionRequests(
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) String status) {
    PageResult<PermissionRequestVO> result =
        permissionRequestService.getMyPermissionRequests(page, size, status);
    return Result.success(result);
  }

  /** 获取申请详情 */
  @GetMapping("/requests/{id}")
  @Operation(summary = "获取申请详情", description = "获取权限申请的详细信息")
  public Result<PermissionRequestVO> getPermissionRequestDetail(@PathVariable Long id) {
    PermissionRequestVO request = permissionRequestService.getPermissionRequestWithApprovals(id);
    return Result.success(request);
  }

  /** 审批通过 */
  @PutMapping("/requests/{id}/approve")
  @Operation(summary = "审批通过", description = "管理员审批通过权限申请")
  public Result<Void> approvePermissionRequest(
      @PathVariable Long id, @RequestBody @Valid ApprovePermissionRequestDTO dto) {
    permissionRequestService.approvePermissionRequest(id, dto);
    return Result.success();
  }

  /** 审批拒绝 */
  @PutMapping("/requests/{id}/reject")
  @Operation(summary = "审批拒绝", description = "管理员审批拒绝权限申请")
  public Result<Void> rejectPermissionRequest(
      @PathVariable Long id, @RequestBody @Valid ApprovePermissionRequestDTO dto) {
    permissionRequestService.rejectPermissionRequest(id, dto);
    return Result.success();
  }

  /** 获取申请的审批记录 */
  @GetMapping("/requests/{id}/approvals")
  @Operation(summary = "获取审批记录", description = "获取权限申请的审批历史记录")
  public Result<List<PermissionApprovalVO>> getApprovalRecords(@PathVariable Long id) {
    List<PermissionApprovalVO> records = permissionRequestService.getApprovalRecords(id);
    return Result.success(records);
  }
}
