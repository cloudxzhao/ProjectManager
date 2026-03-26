package com.projecthub.user.controller;

import com.projecthub.common.api.result.PageResult;
import com.projecthub.common.api.result.Result;
import com.projecthub.common.security.util.UserContextHolder;
import com.projecthub.user.dto.UpdateUserRequest;
import com.projecthub.user.dto.UserVO;
import com.projecthub.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户信息管理接口")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser() {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(userService.getUserById(userId));
    }

    /**
     * 根据ID获取用户
     */
    @Operation(summary = "根据ID获取用户")
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    /**
     * 获取所有用户
     */
    @Operation(summary = "获取所有用户")
    @GetMapping
    public Result<List<UserVO>> getAllUsers() {
        return Result.success(userService.getAllUsers());
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<UserVO> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return Result.success(userService.updateUser(id, request));
    }

    /**
     * 更新当前用户信息
     */
    @Operation(summary = "更新当前用户信息")
    @PutMapping("/me")
    public Result<UserVO> updateCurrentUser(@Valid @RequestBody UpdateUserRequest request) {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(userService.updateUser(userId, request));
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功", null);
    }

}