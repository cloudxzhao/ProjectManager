package com.projecthub.story.feign;

import com.projecthub.common.api.result.Result;
import com.projecthub.story.feign.dto.ProjectInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Project Service Feign Client Fallback
 */
@Slf4j
@Component
public class ProjectServiceClientFallback implements ProjectServiceClient {

    @Override
    public Result<ProjectInfoDTO> getProjectById(Long id) {
        log.warn("Fallback: getProjectById called for id: {}", id);
        return Result.error(503, "Project service unavailable");
    }

    @Override
    public Result<List<ProjectInfoDTO>> getProjectsByIds(List<Long> ids) {
        log.warn("Fallback: getProjectsByIds called for ids: {}", ids);
        return Result.error(503, "Project service unavailable");
    }

    @Override
    public Result<Boolean> isMember(Long projectId, Long userId) {
        log.warn("Fallback: isMember called for projectId: {}, userId: {}", projectId, userId);
        return Result.error(503, "Project service unavailable");
    }

    @Override
    public Result<Object> getProjectStats(Long id) {
        log.warn("Fallback: getProjectStats called for id: {}", id);
        return Result.error(503, "Project service unavailable");
    }
}