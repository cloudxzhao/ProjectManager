package com.projecthub.module.project.controller;

import com.projecthub.common.response.PageResult;
import com.projecthub.common.response.Result;
import com.projecthub.module.project.dto.CreateProjectRequest;
import com.projecthub.module.project.dto.ProjectMemberDTO;
import com.projecthub.module.project.dto.ProjectMemberVO;
import com.projecthub.module.project.dto.ProjectStatsDTO;
import com.projecthub.module.project.dto.ProjectVO;
import com.projecthub.module.project.dto.UpdateProjectRequest;
import com.projecthub.module.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 项目控制器 */
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "项目管理", description = "项目相关接口")
public class ProjectController {

  private final ProjectService projectService;

  /** 创建项目 */
  @PostMapping
  @Operation(summary = "创建项目", description = "创建一个新的项目")
  public Result<ProjectVO> createProject(@Valid @RequestBody CreateProjectRequest request) {
    ProjectVO project = projectService.createProject(request);
    return Result.success(project);
  }

  /** 获取项目详情 */
  @GetMapping("/{id}")
  @Operation(summary = "获取项目详情", description = "根据项目 ID 获取详细信息")
  public Result<ProjectVO> getProject(@PathVariable Long id) {
    ProjectVO project = projectService.getProject(id);
    return Result.success(project);
  }

  /** 更新项目 */
  @PutMapping("/{id}")
  @Operation(summary = "更新项目", description = "更新项目信息")
  public Result<ProjectVO> updateProject(
      @PathVariable Long id, @Valid @RequestBody UpdateProjectRequest request) {
    ProjectVO project = projectService.updateProject(id, request);
    return Result.success(project);
  }

  /** 删除项目 */
  @DeleteMapping("/{id}")
  @Operation(summary = "删除项目", description = "删除指定项目")
  public Result<Void> deleteProject(@PathVariable Long id) {
    projectService.deleteProject(id);
    return Result.success();
  }

  /** 获取项目列表 */
  @GetMapping
  @Operation(summary = "获取项目列表", description = "获取当前用户参与的项目列表")
  public Result<PageResult<ProjectVO>> listProjects(
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "createdAt") String sort,
      @RequestParam(defaultValue = "desc") String order) {
    PageResult<ProjectVO> result =
        projectService.getUserProjects(page, size, keyword, status, sort, order);
    return Result.success(result);
  }

  /** 添加项目成员 */
  @PostMapping("/{id}/members")
  @Operation(summary = "添加项目成员", description = "添加用户到项目中，如果用户已在项目中则更新其角色")
  public Result<Void> addProjectMember(
      @PathVariable Long id, @Valid @RequestBody ProjectMemberDTO request) {
    projectService.addProjectMember(id, request);
    return Result.success();
  }

  /** 更新项目成员角色 */
  @PutMapping("/{id}/members/{userId}/role")
  @Operation(summary = "更新项目成员角色", description = "更新项目中指定用户的角色")
  public Result<Void> updateProjectMemberRole(
      @PathVariable Long id, @PathVariable Long userId, @RequestBody Map<String, String> request) {
    String role = request.get("role");
    projectService.updateProjectMemberRole(id, userId, role);
    return Result.success();
  }

  /** 移除项目成员 */
  @DeleteMapping("/{id}/members/{userId}")
  @Operation(summary = "移除项目成员", description = "从项目中移除指定用户")
  public Result<Void> removeProjectMember(@PathVariable Long id, @PathVariable Long userId) {
    projectService.removeProjectMember(id, userId);
    return Result.success();
  }

  /** 获取项目成员列表 */
  @GetMapping("/{id}/members")
  @Operation(summary = "获取项目成员列表", description = "获取项目下的所有成员")
  public Result<List<ProjectMemberVO>> getProjectMembers(@PathVariable Long id) {
    List<ProjectMemberVO> members = projectService.getProjectMembers(id);
    return Result.success(members);
  }

  /** 获取项目统计信息 */
  @GetMapping("/stats")
  @Operation(summary = "获取项目统计信息", description = "获取当前用户的项目统计信息（进行中、已完成、已归档数量）")
  public Result<ProjectStatsDTO> getProjectStats() {
    ProjectStatsDTO stats = projectService.getProjectStats();
    return Result.success(stats);
  }

  /** 获取用户有权限的项目列表 */
  @GetMapping("/authorized")
  @Operation(summary = "获取用户有权限的项目列表", description = "获取当前用户有权限访问的所有项目的 ID 和名称（不分页）")
  public Result<List<ProjectVO.ProjectIdName>> getAuthorizedProjects() {
    List<ProjectVO.ProjectIdName> projects = projectService.getUserAuthorizedProjects();
    return Result.success(projects);
  }
}
