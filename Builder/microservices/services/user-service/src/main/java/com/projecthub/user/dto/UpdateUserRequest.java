package com.projecthub.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户请求
 */
@Data
@Schema(description = "更新用户请求")
public class UpdateUserRequest {

    @Schema(description = "昵称")
    @Size(max = 50, message = "昵称最长50个字符")
    private String nickname;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "个人简介")
    @Size(max = 500, message = "个人简介最长500个字符")
    private String bio;

}