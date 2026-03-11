package com.projecthub.module.user.controller;

import com.projecthub.common.response.Result;
import com.projecthub.module.user.dto.UserVO;
import com.projecthub.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
  public Result<UserVO> updateProfile(
      @RequestParam(required = false) String nickname,
      @RequestParam(required = false) String avatar) {
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

  /** 修改密码 */
  @PutMapping("/password")
  @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
  public Result<Void> updatePassword(
      @RequestParam String oldPassword, @RequestParam String newPassword) {
    userService.updatePassword(oldPassword, newPassword);
    return Result.success();
  }
}
