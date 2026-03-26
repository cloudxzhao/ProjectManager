package com.projecthub.story.feign;

import com.projecthub.common.api.result.Result;
import com.projecthub.story.feign.dto.ProjectInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

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
     * Batch get projects by IDs
     */
    @PostMapping("/batch")
    Result<List<ProjectInfoDTO>> getProjectsByIds(List<Long> ids);

    /**
     * Check if user is a project member
     */
    @GetMapping("/{projectId}/members/{userId}/check")
    Result<Boolean> isMember(@PathVariable("projectId") Long projectId, @PathVariable("userId") Long userId);

    /**
     * Get project statistics
     */
    @GetMapping("/{id}/stats")
    Result<Object> getProjectStats(@PathVariable("id") Long id);
}