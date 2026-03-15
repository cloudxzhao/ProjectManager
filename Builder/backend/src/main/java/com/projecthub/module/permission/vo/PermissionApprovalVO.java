package com.projecthub.module.permission.vo;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 权限审批记录 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionApprovalVO {

  /** 审批记录 ID */
  private Long id;

  /** 申请 ID */
  private Long requestId;

  /** 审批人 ID */
  private Long approverId;

  /** 审批人用户名 */
  private String approverName;

  /** 审批人昵称 */
  private String approverNickname;

  /** 审批操作 */
  private String action;

  /** 审批意见 */
  private String comment;

  /** 审批时间 */
  private LocalDateTime createdAt;
}
