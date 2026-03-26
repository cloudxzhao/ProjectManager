package com.projecthub.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目成员 VO
 */
@Data
@Schema(description = "项目成员信息")
public class ProjectMemberVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "加入时间")
    private LocalDateTime joinedAt;

}