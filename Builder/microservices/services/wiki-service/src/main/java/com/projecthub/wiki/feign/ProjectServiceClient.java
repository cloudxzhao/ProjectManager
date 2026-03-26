package com.projecthub.wiki.feign;

import com.projecthub.common.api.result.Result;
import com.projecthub.wiki.feign.dto.ProjectInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Project Service Feign Client
 */
@FeignClient(
        name = "project-service",
        path = "/internal/projects",
        fallback = ProjectServiceClientFallback.class
)
public interface ProjectServiceClient {

    /**
     * Get project by ID
     */
    @GetMapping("/{id}")
    Result<ProjectInfoDTO> getProjectById(@PathVariable("id") Long id);

    /**
     * Check if user is a project member
     */
    @GetMapping("/{projectId}/members/{userId}/check")
    Result<Boolean> isMember(@PathVariable("projectId") Long projectId, @PathVariable("userId") Long userId);
}
