package com.projecthub.auth.dto;

import com.projecthub.auth.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 认证响应
 */
@Data
@Schema(description = "认证响应")
public class AuthResponse {

    @Schema(description = "访问 Token")
    private String accessToken;

    @Schema(description = "刷新 Token")
    private String refreshToken;

    @Schema(description = "Token 类型", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "过期时间（秒）", example = "7200")
    private Long expiresIn;

    @Schema(description = "用户信息")
    private UserInfo user;

    @Data
    @Schema(description = "用户信息")
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String avatar;
        private String role;
    }

    public static AuthResponse of(String accessToken, String refreshToken, Long expiresIn, User user) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(expiresIn);

        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRole(user.getRole());
        response.setUser(userInfo);

        return response;
    }

}