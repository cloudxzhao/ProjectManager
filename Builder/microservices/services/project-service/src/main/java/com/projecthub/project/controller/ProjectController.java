package com.projecthub.project.controller;

import com.projecthub.common.api.result.Result;
import com.projecthub.common.security.util.UserContextHolder;
import com.projecthub.project.dto.*;
import com.projecthub.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目控制器
 */
@Tag(name = "项目管理", description = "项目CRUD、成员管理接口")
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 创建项目
     */
    @Operation(summary = "创建项目")
    @PostMapping
    public Result<ProjectVO> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return Result.success("创建成功", projectService.createProject(request));
    }

    /**
     * 更新项目
     */
    @Operation(summary = "更新项目")
    @PutMapping("/{id}")
    public Result<ProjectVO> updateProject(@PathVariable Long id, @Valid @RequestBody UpdateProjectRequest request) {
        return Result.success("更新成功", projectService.updateProject(id, request));
    }

    /**
     * 删除项目
     */
    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return Result.success("删除成功", null);
    }

    /**
     * 获取项目详情
     */
    @Operation(summary = "获取项目详情")
    @GetMapping("/{id}")
    public Result<ProjectVO> getProjectById(@PathVariable Long id) {
        return Result.success(projectService.getProjectById(id));
    }

    /**
     * 获取所有项目
     */
    @Operation(summary = "获取所有项目")
    @GetMapping
    public Result<List<ProjectVO>> getAllProjects() {
        return Result.success(projectService.getAllProjects());
    }

    /**
     * 根据状态获取项目
     */
    @Operation(summary = "根据状态获取项目")
    @GetMapping("/status/{status}")
    public Result<List<ProjectVO>> getProjectsByStatus(@PathVariable String status) {
        return Result.success(projectService.getProjectsByStatus(status));
    }

    /**
     * 获取我的项目
     */
    @Operation(summary = "获取我的项目")
    @GetMapping("/my")
    public Result<List<ProjectVO>> getMyProjects() {
        return Result.success(projectService.getMyProjects());
    }

    /**
     * 添加项目成员
     */
    @Operation(summary = "添加项目成员")
    @PostMapping("/{id}/members")
    public Result<List<ProjectMemberVO>> addMembers(
            @PathVariable("id") Long projectId,
            @Valid @RequestBody AddMembersRequest request) {
        return Result.success("添加成功", projectService.addMembers(projectId, request));
    }

    /**
     * 移除项目成员
     */
    @Operation(summary = "移除项目成员")
    @DeleteMapping("/{id}/members/{userId}")
    public Result<Void> removeMember(
            @PathVariable("id") Long projectId,
            @PathVariable Long userId) {
        projectService.removeMember(projectId, userId);
        return Result.success("移除成功", null);
    }

    /**
     * 获取项目成员列表
     */
    @Operation(summary = "获取项目成员列表")
    @GetMapping("/{id}/members")
    public Result<List<ProjectMemberVO>> getProjectMembers(@PathVariable("id") Long projectId) {
        return Result.success(projectService.getProjectMembers(projectId));
    }

    /**
     * 检查是否是项目成员
     */
    @Operation(summary = "检查是否是项目成员")
    @GetMapping("/{id}/members/check")
    public Result<Boolean> checkMembership(@PathVariable("id") Long projectId) {
        Long userId = UserContextHolder.getUserId();
        return Result.success(projectService.isMember(projectId, userId));
    }

}