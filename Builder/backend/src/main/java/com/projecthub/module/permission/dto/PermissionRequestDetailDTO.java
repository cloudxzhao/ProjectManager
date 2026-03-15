package com.projecthub.module.permission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 权限申请详情 DTO（用于 Service 层内部传输） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequestDetailDTO {

  /** 申请 ID */
  private Long id;

  /** 申请人 ID */
  private Long userId;

  /** 申请人用户名 */
  private String username;

  /** 申请人邮箱 */
  private String email;

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
  private String createdAt;
}
