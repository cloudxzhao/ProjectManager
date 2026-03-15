package com.projecthub.module.permission.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 权限申请 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequestVO {

  /** 申请 ID */
  private Long id;

  /** 申请人 ID */
  private Long userId;

  /** 申请人用户名 */
  private String username;

  /** 申请人昵称 */
  private String nickname;

  /** 申请人头像 */
  private String avatar;

  /** 申请的权限 ID */
  private Long permissionId;

  /** 权限名称 */
  private String permissionName;

  /** 权限编码 */
  private String permissionCode;

  /** 关联项目 ID */
  private Long projectId;

  /** 项目名称 */
  private String projectName;

  /** 申请理由 */
  private String reason;

  /** 申请状态 */
  private String status;

  /** 申请时间 */
  private LocalDateTime createdAt;

  /** 更新时间 */
  private LocalDateTime updatedAt;

  /** 审批记录列表 */
  @Builder.Default private List<PermissionApprovalVO> approvalRecords = List.of();
}
