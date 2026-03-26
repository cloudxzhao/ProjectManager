package com.projecthub.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 添加成员请求
 */
@Data
@Schema(description = "添加成员请求")
public class AddMembersRequest {

    @Schema(description = "用户ID列表")
    @NotNull(message = "用户ID列表不能为空")
    private List<Long> userIds;

    @Schema(description = "角色", example = "MEMBER")
    private String role = "MEMBER";

}