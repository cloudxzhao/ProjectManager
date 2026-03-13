package com.projecthub.module.user.controller;

import com.projecthub.common.response.Result;
import com.projecthub.module.user.dto.UserVO;
import com.projecthub.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** 用户控制器 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户资料相关接口")
public class UserController {

  private final UserService userService;

  /** 更新个人资料请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UpdateProfileRequest {
    /** 昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatar;
  }

  /** 获取当前用户信息 */
  @GetMapping("/profile")
  @Operation(summary = "获取个人资料", description = "获取当前登录用户的详细信息")
  public Result<UserVO> getProfile() {
    UserVO user = userService.getCurrentUserInfo();
    return Result.success(user);
  }

  /** 更新用户资料 */
  @PutMapping("/profile")
  @Operation(summary = "更新个人资料", description = "更新当前用户的基本信息")
  public Result<UserVO> updateProfile(@RequestBody(required = false) UpdateProfileRequest request) {
    String nickname = request != null ? request.getNickname() : null;
    String avatar = request != null ? request.getAvatar() : null;
    UserVO user = userService.updateProfile(nickname, avatar);
    return Result.success(user);
  }

  /** 上传头像 */
  @PostMapping("/avatar")
  @Operation(summary = "上传头像", description = "上传用户头像图片")
  public Result<UserVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
    UserVO user = userService.uploadAvatar(file);
    return Result.success(user);
  }

  /** 修改密码请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UpdatePasswordRequest {
    /** 原密码 */
    private String oldPassword;

    /** 新密码 */
    private String newPassword;
  }

  /** 修改密码 */
  @PutMapping("/password")
  @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
  public Result<Void> updatePassword(@RequestBody UpdatePasswordRequest request) {
    userService.updatePassword(request.getOldPassword(), request.getNewPassword());
    return Result.success();
  }
}
