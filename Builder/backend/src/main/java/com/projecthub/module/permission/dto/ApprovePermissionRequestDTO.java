package com.projecthub.module.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 审批权限申请请求 DTO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovePermissionRequestDTO {

  /** 审批意见 */
  @Size(max = 500, message = "审批意见最多 500 字符")
  private String comment;

  /** 是否通过 */
  @NotBlank(message = "审批结果不能为空")
  private String action; // APPROVE 或 REJECT
}
