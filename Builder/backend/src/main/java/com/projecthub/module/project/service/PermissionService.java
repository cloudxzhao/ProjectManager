package com.projecthub.module.project.service;

import com.projecthub.module.project.entity.ProjectMember;
import com.projecthub.module.project.repository.ProjectMemberRepository;
import com.projecthub.module.project.repository.ProjectRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 权限服务 */
@Service
@RequiredArgsConstructor
public class PermissionService {

  private final ProjectMemberRepository projectMemberRepository;
  private final ProjectRepository projectRepository;

  /** 检查用户在项目中是否有指定权限 */
  @Transactional(readOnly = true)
  public boolean hasPermission(Long userId, Long projectId, String permissionCode) {
    // 检查是否是项目成员
    ProjectMember member =
        projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElse(null);

    if (member == null) {
      // 检查是否是项目所有者
      return projectRepository.existsByIdAndOwnerId(projectId, userId);
    }

    // 项目成员默认有基本权限，具体权限校验可根据 permissionCode 细化
    return true;
  }

  /** 检查用户是否有全局权限 */
  @Transactional(readOnly = true)
  public boolean hasGlobalPermission(Long userId, String permissionCode) {
    // TODO: 实现全局权限检查（基于角色和权限表）
    // 暂时返回 true，后续完善
    return true;
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
    java.util.stream.Stream.concat(ownerProjectIds.stream(), memberProjectIds.stream())
        .distinct()
        .collect(java.util.stream.Collectors.toList());

    return java.util.stream.Stream.concat(ownerProjectIds.stream(), memberProjectIds.stream())
        .distinct()
        .collect(java.util.stream.Collectors.toList());
  }
}
