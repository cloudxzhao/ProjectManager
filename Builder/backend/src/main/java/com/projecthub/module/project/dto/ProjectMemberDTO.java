package com.projecthub.module.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 项目成员 DTO */
@Data
public class ProjectMemberDTO {

  /** 用户 ID */
  @NotNull(message = "用户 ID 不能为空")
  private Long userId;

  /** 角色 */
  @NotBlank(message = "角色不能为空")
  private String role;
}
