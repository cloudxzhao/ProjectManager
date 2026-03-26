package com.projecthub.user.client;

import com.projecthub.common.api.result.Result;
import com.projecthub.common.feign.config.FeignConfig;
import com.projecthub.user.dto.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户服务 Feign 客户端
 */
@FeignClient(
        name = "user-service",
        contextId = "userClient",
        configuration = FeignConfig.class
)
public interface UserClient {

    /**
     * 根据ID获取用户
     */
    @GetMapping("/internal/users/{id}")
    Result<UserVO> getUserById(@PathVariable("id") Long id);

    /**
     * 批量获取用户
     */
    @PostMapping("/internal/users/batch")
    Result<List<UserVO>> getUsersByIds(@RequestBody List<Long> ids);

    /**
     * 根据邮箱获取用户
     */
    @GetMapping("/internal/users/by-email")
    Result<UserVO> getUserByEmail(@RequestParam("email") String email);

    /**
     * 检查用户是否存在
     */
    @GetMapping("/internal/users/{id}/exists")
    Result<Boolean> existsUser(@PathVariable("id") Long id);

}