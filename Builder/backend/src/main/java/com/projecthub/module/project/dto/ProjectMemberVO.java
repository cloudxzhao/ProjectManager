package com.projecthub.module.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 项目成员 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberVO {

  /** 成员 ID */
  private Long id;

  /** 项目 ID */
  private Long projectId;

  /** 用户 ID */
  private Long userId;

  /** 用户名 */
  private String username;

  /** 用户昵称 */
  private String nickname;

  /** 用户头像 */
  private String avatar;

  /** 用户邮箱 */
  private String email;

  /** 角色 */
  private String role;

  /** 加入时间 */
  private String joinedAt;
}
