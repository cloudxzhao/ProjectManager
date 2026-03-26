package com.projecthub.task.client;

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
        contextId = "taskUserClient",
        configuration = FeignConfig.class
)
public interface UserClient {

    @GetMapping("/internal/users/{id}")
    Result<Map<String, Object>> getUserById(@PathVariable("id") Long id);

    @PostMapping("/internal/users/batch")
    Result<List<Map<String, Object>>> getUsersByIds(@RequestBody List<Long> ids);

}