package com.projecthub.project.client;

import com.projecthub.common.api.result.Result;
import com.projecthub.common.feign.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户服务 Feign 客户端
 */
@FeignClient(
        name = "user-service",
        contextId = "projectUserClient",
        configuration = FeignConfig.class
)
public interface UserClient {

    /**
     * 根据ID获取用户
     */
    @GetMapping("/internal/users/{id}")
    Result<Map<String, Object>> getUserById(@PathVariable("id") Long id);

    /**
     * 批量获取用户
     */
    @PostMapping("/internal/users/batch")
    Result<List<Map<String, Object>>> getUsersByIds(@RequestBody List<Long> ids);

}