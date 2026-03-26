package com.projecthub.project.controller;

import com.projecthub.common.api.result.Result;
import com.projecthub.project.dto.ProjectVO;
import com.projecthub.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目内部接口控制器
 */
@RestController
@RequestMapping("/internal/projects")
@RequiredArgsConstructor
public class ProjectInternalController {

    private final ProjectService projectService;

    /**
     * 根据ID获取项目
     */
    @GetMapping("/{id}")
    public Result<ProjectVO> getProjectById(@PathVariable Long id) {
        return Result.success(projectService.getProjectById(id));
    }

    /**
     * 批量获取项目
     */
    @PostMapping("/batch")
    public Result<List<ProjectVO>> getProjectsByIds(@RequestBody List<Long> ids) {
        List<ProjectVO> projects = ids.stream()
                .map(id -> {
                    try {
                        return projectService.getProjectById(id);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(p -> p != null)
                .collect(Collectors.toList());
        return Result.success(projects);
    }

    /**
     * 检查用户是否是项目成员
     */
    @GetMapping("/{projectId}/members/{userId}/check")
    public Result<Boolean> isMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        return Result.success(projectService.isMember(projectId, userId));
    }

    /**
     * 获取项目统计
     */
    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> getProjectStats(@PathVariable Long id) {
        ProjectVO project = projectService.getProjectById(id);
        return Result.success(Map.of(
                "id", project.getId(),
                "status", project.getStatus(),
                "progress", project.getProgress(),
                "memberCount", project.getMemberCount()
        ));
    }

}