package com.projecthub.wiki.feign;

import com.projecthub.common.api.result.Result;
import com.projecthub.wiki.feign.dto.ProjectInfoDTO;
import org.springframework.stereotype.Component;

/**
 * Project Service Feign Client Fallback
 */
@Component
public class ProjectServiceClientFallback implements ProjectServiceClient {

    @Override
    public Result<ProjectInfoDTO> getProjectById(Long id) {
        return Result.error("服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<Boolean> isMember(Long projectId, Long userId) {
        return Result.error("服务暂时不可用，请稍后重试");
    }
}
