package com.projecthub.module.project.service;

import com.projecthub.module.project.entity.ProjectMember;
import com.projecthub.module.project.repository.ProjectMemberRepository;
import com.projecthub.module.project.repository.ProjectRepository;
import com.projecthub.module.user.repository.SysPermissionRepository;
import com.projecthub.module.user.repository.SysRolePermissionRepository;
import com.projecthub.module.user.repository.SysUserRoleRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 权限服务 */
@Service
@RequiredArgsConstructor
public class PermissionService {

  private final ProjectMemberRepository projectMemberRepository;
  private final ProjectRepository projectRepository;
  private final SysUserRoleRepository userRoleRepository;
  private final SysRolePermissionRepository rolePermissionRepository;
  private final SysPermissionRepository permissionRepository;

  // 项目角色 - 权限映射
  private static final Map<ProjectMember.ProjectMemberRole, Set<String>> ROLE_PERMISSIONS =
      Map.ofEntries(
          Map.entry(ProjectMember.ProjectMemberRole.OWNER, Set.of("*")),
          Map.entry(
              ProjectMember.ProjectMemberRole.ADMIN,
              Set.of(
                  "TASK_CREATE",
                  "TASK_EDIT",
                  "TASK_DELETE",
                  "TASK_MOVE",
                  "TASK_ASSIGN",
                  "TASK_VIEW",
                  "TASK_COMMENT",
                  "STORY_CREATE",
                  "STORY_EDIT",
                  "STORY_DELETE",
                  "STORY_VIEW",
                  "EPIC_CREATE",
                  "EPIC_EDIT",
                  "EPIC_DELETE",
                  "EPIC_VIEW",
                  "ISSUE_CREATE",
                  "ISSUE_EDIT",
                  "ISSUE_DELETE",
                  "ISSUE_VIEW",
                  "WIKI_CREATE",
                  "WIKI_EDIT",
                  "WIKI_DELETE",
                  "WIKI_VIEW",
                  "REPORT_VIEW",
                  "MEMBER_MANAGE")),
          Map.entry(
              ProjectMember.ProjectMemberRole.MANAGER,
              Set.of(
                  "TASK_CREATE",
                  "TASK_EDIT",
                  "TASK_MOVE",
                  "TASK_VIEW",
                  "TASK_COMMENT",
                  "STORY_CREATE",
                  "STORY_EDIT",
                  "STORY_VIEW",
                  "EPIC_CREATE",
                  "EPIC_EDIT",
                  "EPIC_VIEW",
                  "ISSUE_CREATE",
                  "ISSUE_EDIT",
                  "ISSUE_VIEW",
                  "WIKI_CREATE",
                  "WIKI_EDIT",
                  "WIKI_VIEW",
                  "REPORT_VIEW")),
          Map.entry(
              ProjectMember.ProjectMemberRole.MEMBER,
              Set.of(
                  "TASK_CREATE",
                  "TASK_EDIT",
                  "TASK_VIEW",
                  "TASK_COMMENT",
                  "STORY_VIEW",
                  "EPIC_VIEW",
                  "ISSUE_VIEW",
                  "WIKI_VIEW",
                  "REPORT_VIEW")));

  /** 检查用户在项目中是否有指定权限 */
  @Transactional(readOnly = true)
  public boolean hasPermission(Long userId, Long projectId, String permissionCode) {
    // 1. 检查项目成员关系
    ProjectMember member =
        projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElse(null);

    if (member == null) {
      // 2. 检查是否是项目所有者
      if (projectRepository.existsByIdAndOwnerId(projectId, userId)) {
        return true;
      }
      return false;
    }

    // 3. 根据角色 - 权限映射校验
    return hasRolePermission(member.getRole(), permissionCode);
  }

  /** 根据角色检查权限 */
  private boolean hasRolePermission(ProjectMember.ProjectMemberRole role, String permissionCode) {
    Set<String> permissions = ROLE_PERMISSIONS.get(role);
    if (permissions == null) {
      return false;
    }
    return permissions.contains("*") || permissions.contains(permissionCode);
  }

  /** 检查用户是否有全局权限 */
  @Transactional(readOnly = true)
  public boolean hasGlobalPermission(Long userId, String permissionCode) {
    // 1. 获取用户角色 IDs
    List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
    if (roleIds.isEmpty()) {
      return false;
    }

    // 2. 获取角色权限 IDs
    List<Long> permissionIds = rolePermissionRepository.findPermissionIdsByRoleIds(roleIds);
    if (permissionIds.isEmpty()) {
      return false;
    }

    // 3. 检查是否包含指定权限
    return permissionRepository.findAllByIds(permissionIds).stream()
        .anyMatch(p -> permissionCode.equals(p.getCode()));
  }

  /** 获取用户在项目中的角色 */
  @Transactional(readOnly = true)
  public ProjectMember.ProjectMemberRole getUserRoleInProject(Long userId, Long projectId) {
    // 检查是否是项目所有者
    if (projectRepository.existsByIdAndOwnerId(projectId, userId)) {
      return ProjectMember.ProjectMemberRole.OWNER;
    }

    // 检查是否是项目成员
    return projectMemberRepository
        .findByProjectIdAndUserId(projectId, userId)
        .map(ProjectMember::getRole)
        .orElse(null);
  }

  /** 获取用户参与的所有项目 ID */
  @Transactional(readOnly = true)
  public List<Long> getUserProjectIds(Long userId) {
    // 作为所有者的项目
    List<Long> ownerProjectIds = projectRepository.findIdsByOwnerId(userId);

    // 作为成员的项目
    List<Long> memberProjectIds = projectMemberRepository.findProjectIdsByUserId(userId);

    // 合并去重
    return java.util.stream.Stream.concat(ownerProjectIds.stream(), memberProjectIds.stream())
        .distinct()
        .collect(java.util.stream.Collectors.toList());
  }
}
