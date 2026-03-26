package com.projecthub.user.controller;

import com.projecthub.common.api.result.Result;
import com.projecthub.user.dto.UserVO;
import com.projecthub.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户内部接口控制器
 * 供其他服务通过 Feign 调用
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserService userService;

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    /**
     * 根据ID列表批量获取用户信息
     */
    @PostMapping("/batch")
    public Result<List<UserVO>> getUsersByIds(@RequestBody List<Long> ids) {
        return Result.success(userService.getUsersByIds(ids));
    }

    /**
     * 根据邮箱获取用户信息
     */
    @GetMapping("/by-email")
    public Result<UserVO> getUserByEmail(@RequestParam String email) {
        return Result.success(userService.getUserByEmail(email));
    }

    /**
     * 检查用户是否存在
     */
    @GetMapping("/{id}/exists")
    public Result<Boolean> existsUser(@PathVariable Long id) {
        try {
            userService.getUserById(id);
            return Result.success(true);
        } catch (Exception e) {
            return Result.success(false);
        }
    }

}