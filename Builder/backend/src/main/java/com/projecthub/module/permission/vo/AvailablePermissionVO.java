package com.projecthub.module.permission.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 可申请的权限 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailablePermissionVO {

  /** 权限 ID */
  private Long id;

  /** 权限名称 */
  private String name;

  /** 权限编码 */
  private String code;

  /** 权限描述 */
  private String description;

  /** 是否已拥有 */
  private boolean hasPermission;
}
