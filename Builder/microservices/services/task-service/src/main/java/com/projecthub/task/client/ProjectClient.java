package com.projecthub.task.client;

import com.projecthub.common.api.result.Result;
import com.projecthub.common.feign.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 项目服务 Feign 客户端
 */
@FeignClient(
        name = "project-service",
        contextId = "taskProjectClient",
        configuration = FeignConfig.class
)
public interface ProjectClient {

    @GetMapping("/internal/projects/{id}")
    Result<Map<String, Object>> getProjectById(@PathVariable("id") Long id);

    @GetMapping("/{projectId}/members/{userId}/check")
    Result<Boolean> isMember(@PathVariable("projectId") Long projectId, @PathVariable("userId") Long userId);

}