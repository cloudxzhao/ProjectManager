package com.projecthub.module.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 创建权限申请请求 DTO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePermissionRequestDTO {

  /** 申请的权限 ID */
  @NotNull(message = "权限 ID 不能为空")
  private Long permissionId;

  /** 关联项目 ID（可选，项目级权限申请） */
  private Long projectId;

  /** 申请理由 */
  @NotBlank(message = "申请理由不能为空")
  @Size(max = 500, message = "申请理由最多 500 字符")
  private String reason;
}
