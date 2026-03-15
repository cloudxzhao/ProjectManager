package com.projecthub.module.project.service;

import com.projecthub.common.constant.ErrorCode;
import com.projecthub.common.exception.BusinessException;
import com.projecthub.common.response.PageResult;
import com.projecthub.common.util.BeanCopyUtil;
import com.projecthub.module.project.dto.CreateProjectRequest;
import com.projecthub.module.project.dto.ProjectMemberDTO;
import com.projecthub.module.project.dto.ProjectMemberVO;
import com.projecthub.module.project.dto.ProjectStatsDTO;
import com.projecthub.module.project.dto.ProjectVO;
import com.projecthub.module.project.dto.UpdateProjectRequest;
import com.projecthub.module.project.entity.Project;
import com.projecthub.module.project.entity.ProjectMember;
import com.projecthub.module.project.repository.ProjectMemberRepository;
import com.projecthub.module.project.repository.ProjectRepository;
import com.projecthub.module.user.entity.User;
import com.projecthub.module.user.repository.UserRepository;
import com.projecthub.security.UserDetailsImpl;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 项目服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final ProjectMemberRepository memberRepository;
  private final PermissionService permissionService;
  private final UserRepository userRepository;

  /** 填充项目统计信息 */
  private void populateProjectStats(ProjectVO projectVO) {
    if (projectVO.getId() == null) {
      return;
    }

    // 统计成员数量
    Long memberCount = projectRepository.countMembersByProjectId(projectVO.getId());
    projectVO.setMemberCount(memberCount.intValue());

    // 统计任务总数
    Long taskCount = projectRepository.countTasksByProjectId(projectVO.getId());
    projectVO.setTaskCount(taskCount.intValue());

    // 统计已完成任务数
    Long completedTaskCount = projectRepository.countCompletedTasksByProjectId(projectVO.getId());
    projectVO.setCompletedTaskCount(completedTaskCount.intValue());
  }

  /** 创建项目 */
  @Transactional
  public ProjectVO createProject(CreateProjectRequest request) {
    // 获取当前用户
    Long ownerId = getCurrentUserId();

    // 验证日期
    if (request.getEndDate().isBefore(request.getStartDate())) {
      throw new BusinessException(ErrorCode.PROJECT_DATE_INVALID, "结束日期不能早于开始日期");
    }

    // 创建项目
    Project project = new Project();
    project.setName(request.getName());
    project.setDescription(request.getDescription());
    project.setStartDate(request.getStartDate());
    project.setEndDate(request.getEndDate());
    project.setOwnerId(ownerId);
    // 支持传入 status，如果没有传入则默认为 ACTIVE
    if (request.getStatus() != null) {
      project.setStatus(Project.ProjectStatus.valueOf(request.getStatus()));
    } else {
      project.setStatus(Project.ProjectStatus.ACTIVE);
    }
    project.setIcon(request.getIcon());
    project.setThemeColor(request.getThemeColor());

    projectRepository.save(project);
    log.info("创建项目成功：projectId={}, ownerId={}", project.getId(), ownerId);

    // 将创建者添加为项目 OWNER 成员
    ProjectMember ownerMember = new ProjectMember();
    ownerMember.setProjectId(project.getId());
    ownerMember.setUserId(ownerId);
    ownerMember.setRole(ProjectMember.ProjectMemberRole.OWNER);
    memberRepository.save(ownerMember);
    log.info("添加项目创建者为 OWNER 成员：projectId={}, userId={}", project.getId(), ownerId);

    ProjectVO projectVO = BeanCopyUtil.copyProperties(project, ProjectVO.class);
    // 手动设置枚举字段的字符串表示
    projectVO.setStatus(project.getStatus().name());
    populateProjectStats(projectVO);
    return projectVO;
  }

  /** 获取项目详情 */
  @Transactional(readOnly = true)
  public ProjectVO getProject(Long projectId) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    ProjectVO projectVO = BeanCopyUtil.copyProperties(project, ProjectVO.class);
    // 手动设置枚举字段的字符串表示
    projectVO.setStatus(project.getStatus().name());
    populateProjectStats(projectVO);
    return projectVO;
  }

  /** 更新项目 */
  @Transactional
  public ProjectVO updateProject(Long projectId, UpdateProjectRequest request) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    // 权限校验：只有项目所有者可以更新
    if (!project.getOwnerId().equals(getCurrentUserId())) {
      throw new BusinessException(ErrorCode.PROJECT_PERMISSION_DENIED, "只有项目所有者可以更新项目");
    }

    // 验证日期
    if (request.getEndDate() != null
        && request.getStartDate() != null
        && request.getEndDate().isBefore(request.getStartDate())) {
      throw new BusinessException(ErrorCode.PROJECT_DATE_INVALID, "结束日期不能早于开始日期");
    }

    // 更新字段
    if (request.getName() != null) {
      project.setName(request.getName());
    }
    if (request.getDescription() != null) {
      project.setDescription(request.getDescription());
    }
    if (request.getStartDate() != null) {
      project.setStartDate(request.getStartDate());
    }
    if (request.getEndDate() != null) {
      project.setEndDate(request.getEndDate());
    }
    if (request.getStatus() != null) {
      project.setStatus(Project.ProjectStatus.valueOf(request.getStatus()));
    }
    if (request.getIcon() != null) {
      project.setIcon(request.getIcon());
    }
    if (request.getThemeColor() != null) {
      project.setThemeColor(request.getThemeColor());
    }

    projectRepository.save(project);
    log.info("更新项目成功：projectId={}", projectId);

    ProjectVO projectVO = BeanCopyUtil.copyProperties(project, ProjectVO.class);
    // 手动设置枚举字段的字符串表示
    projectVO.setStatus(project.getStatus().name());
    populateProjectStats(projectVO);
    return projectVO;
  }

  /** 删除项目 */
  @Transactional
  public void deleteProject(Long projectId) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    // 权限校验：只有项目所有者可以删除
    if (!project.getOwnerId().equals(getCurrentUserId())) {
      throw new BusinessException(ErrorCode.PROJECT_PERMISSION_DENIED, "只有项目所有者可以删除项目");
    }

    projectRepository.delete(project);
    log.info("删除项目成功：projectId={}", projectId);
  }

  /** 获取项目列表（用户参与的） */
  @Transactional(readOnly = true)
  public PageResult<ProjectVO> getUserProjects(
      Integer page, Integer size, String keyword, String status, String sort, String order) {
    Long userId = getCurrentUserId();

    // 验证排序字段
    List<String> validSortFields = List.of("createdAt", "name", "startDate", "endDate");
    String sortField = validSortFields.contains(sort) ? sort : "createdAt";
    Sort.Direction sortDirection =
        "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortField));

    // 查询用户参与的项目 ID 列表
    List<Long> projectIds = permissionService.getUserProjectIds(userId);

    if (projectIds.isEmpty()) {
      return PageResult.of(List.of(), 0L, page, size);
    }

    // 查询项目
    org.springframework.data.domain.Page<Project> projectPage;
    if (keyword != null && !keyword.isEmpty()) {
      projectPage =
          projectRepository.findAll(
              (root, query, cb) -> {
                List<jakarta.persistence.criteria.Predicate> predicates =
                    new java.util.ArrayList<>();
                predicates.add(root.get("id").in(projectIds));
                predicates.add(cb.isNull(root.get("deletedAt")));
                predicates.add(cb.like(root.get("name"), "%" + keyword + "%"));
                // 状态过滤
                if (status != null && !status.isEmpty()) {
                  predicates.add(cb.equal(root.get("status"), status));
                }
                return query
                    .where(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]))
                    .getRestriction();
              },
              pageable);
    } else {
      projectPage =
          projectRepository.findAll(
              (root, query, cb) -> {
                List<jakarta.persistence.criteria.Predicate> predicates =
                    new java.util.ArrayList<>();
                predicates.add(root.get("id").in(projectIds));
                predicates.add(cb.isNull(root.get("deletedAt")));
                // 状态过滤
                if (status != null && !status.isEmpty()) {
                  predicates.add(cb.equal(root.get("status"), status));
                }
                return query
                    .where(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]))
                    .getRestriction();
              },
              pageable);
    }

    List<ProjectVO> content =
        projectPage.getContent().stream()
            .map(
                project -> {
                  ProjectVO projectVO = BeanCopyUtil.copyProperties(project, ProjectVO.class);
                  // 手动设置枚举字段的字符串表示
                  projectVO.setStatus(project.getStatus().name());
                  populateProjectStats(projectVO);
                  return projectVO;
                })
            .collect(Collectors.toList());

    return PageResult.of(content, projectPage.getTotalElements(), page, size);
  }

  /** 添加项目成员 */
  @Transactional
  public void addProjectMember(Long projectId, ProjectMemberDTO request) {
    // 检查项目是否存在
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    // 权限校验
    checkProjectPermission(projectId, "PROJECT_MEMBER_MANAGE", project);

    // 检查角色参数是否有效
    if (request.getRole() == null || request.getRole().trim().isEmpty()) {
      throw new BusinessException(400, "角色不能为空");
    }

    // 检查用户是否已是成员
    ProjectMember existingMember =
        memberRepository.findByProjectIdAndUserId(projectId, request.getUserId()).orElse(null);
    if (existingMember != null) {
      // 如果已是成员，更新角色
      existingMember.setRole(ProjectMember.ProjectMemberRole.valueOf(request.getRole()));
      memberRepository.save(existingMember);
      log.info(
          "更新项目成员角色成功：projectId={}, userId={}, role={}",
          projectId,
          request.getUserId(),
          request.getRole());
    } else {
      // 添加新成员
      ProjectMember member = new ProjectMember();
      member.setProjectId(projectId);
      member.setUserId(request.getUserId());
      member.setRole(ProjectMember.ProjectMemberRole.valueOf(request.getRole()));
      memberRepository.save(member);
      log.info(
          "添加项目成员成功：projectId={}, userId={}, role={}",
          projectId,
          request.getUserId(),
          request.getRole());
    }
  }

  /** 更新项目成员角色 */
  @Transactional
  public void updateProjectMemberRole(Long projectId, Long userId, String role) {
    // 检查项目是否存在
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    // 权限校验
    checkProjectPermission(projectId, "PROJECT_MEMBER_MANAGE", project);

    // 检查角色参数是否有效
    if (role == null || role.trim().isEmpty()) {
      throw new BusinessException(400, "角色不能为空");
    }

    // 查找成员
    ProjectMember member =
        memberRepository
            .findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> new BusinessException(404, "用户不是项目成员"));

    // 更新角色
    try {
      member.setRole(ProjectMember.ProjectMemberRole.valueOf(role));
    } catch (IllegalArgumentException e) {
      throw new BusinessException(400, "无效的角色：" + role);
    }

    memberRepository.save(member);
    log.info("更新项目成员角色成功：projectId={}, userId={}, role={}", projectId, userId, role);
  }

  /** 移除项目成员 */
  @Transactional
  public void removeProjectMember(Long projectId, Long userId) {
    // 权限校验
    checkProjectPermission(projectId, "PROJECT_MEMBER_MANAGE");

    memberRepository.deleteByProjectIdAndUserId(projectId, userId);
    log.info("移除项目成员成功：projectId={}, userId={}", projectId, userId);
  }

  /** 获取项目成员列表 */
  @Transactional(readOnly = true)
  public List<ProjectMemberVO> getProjectMembers(Long projectId) {
    // 查询项目成员
    List<ProjectMember> members = memberRepository.findByProjectId(projectId);

    if (members.isEmpty()) {
      return List.of();
    }

    // 提取用户 ID 列表
    List<Long> userIds =
        members.stream().map(ProjectMember::getUserId).collect(Collectors.toList());

    // 批量查询用户信息
    List<User> users = userRepository.findByIds(userIds);
    Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

    //  DateTimeFormatter
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 封装返回结果
    return members.stream()
        .map(
            member -> {
              User user = userMap.get(member.getUserId());
              return ProjectMemberVO.builder()
                  .id(member.getId())
                  .projectId(member.getProjectId())
                  .userId(member.getUserId())
                  .username(user != null ? user.getUsername() : "")
                  .nickname(user != null ? user.getNickname() : "")
                  .avatar(user != null ? user.getAvatar() : "")
                  .email(user != null ? user.getEmail() : "")
                  .role(member.getRole().name())
                  .joinedAt(member.getJoinedAt().format(formatter))
                  .build();
            })
        .collect(Collectors.toList());
  }

  /** 获取项目统计信息 */
  @Transactional(readOnly = true)
  public ProjectStatsDTO getProjectStats() {
    Long userId = getCurrentUserId();

    // 查询用户参与的项目 ID 列表
    List<Long> projectIds = permissionService.getUserProjectIds(userId);

    if (projectIds.isEmpty()) {
      return ProjectStatsDTO.builder()
          .activeCount(0L)
          .completedCount(0L)
          .archivedCount(0L)
          .planningCount(0L)
          .build();
    }

    // 统计各项目状态的数量
    List<Object[]> results = projectRepository.countProjectsByStatus(projectIds);

    // 将查询结果转换为 Map
    java.util.Map<String, Long> stats = new java.util.HashMap<>();
    for (Object[] result : results) {
      stats.put(result[0].toString(), (Long) result[1]);
    }

    return ProjectStatsDTO.builder()
        .activeCount(stats.getOrDefault("ACTIVE", 0L))
        .completedCount(stats.getOrDefault("COMPLETED", 0L))
        .archivedCount(stats.getOrDefault("ARCHIVED", 0L))
        .planningCount(stats.getOrDefault("PLANNING", 0L))
        .build();
  }

  /** 获取用户有权限的项目 ID 和名称列表（不分页） */
  @Transactional(readOnly = true)
  public List<ProjectVO.ProjectIdName> getUserAuthorizedProjects() {
    Long userId = getCurrentUserId();

    // 查询用户参与的项目 ID 列表
    List<Long> projectIds = permissionService.getUserProjectIds(userId);

    if (projectIds.isEmpty()) {
      return List.of();
    }

    // 查询项目 ID 和名称
    List<Object[]> results = projectRepository.findProjectIdsAndNamesByIds(projectIds);

    return results.stream()
        .map(
            result ->
                ProjectVO.ProjectIdName.builder()
                    .id((Long) result[0])
                    .name((String) result[1])
                    .build())
        .collect(Collectors.toList());
  }

  /** 检查项目权限 */
  private void checkProjectPermission(Long projectId, String permissionCode) {
    Long userId = getCurrentUserId();

    // 检查是否是项目所有者
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    if (project.getOwnerId().equals(userId)) {
      return; // 所有者拥有全部权限
    }

    // 检查是否是项目成员
    ProjectMember member =
        memberRepository
            .findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_PERMISSION_DENIED));

    // 根据角色判断权限
    if (permissionCode.equals("PROJECT_MEMBER_MANAGE")
        && member.getRole() != ProjectMember.ProjectMemberRole.OWNER) {
      throw new BusinessException(ErrorCode.PROJECT_PERMISSION_DENIED, "权限不足");
    }
  }

  /** 检查项目权限（已获取项目的情况） */
  private void checkProjectPermission(Long projectId, String permissionCode, Project project) {
    Long userId = getCurrentUserId();

    if (project.getOwnerId().equals(userId)) {
      return; // 所有者拥有全部权限
    }

    // 检查是否是项目成员
    ProjectMember member =
        memberRepository
            .findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_PERMISSION_DENIED));

    // 根据角色判断权限
    if (permissionCode.equals("PROJECT_MEMBER_MANAGE")
        && member.getRole() != ProjectMember.ProjectMemberRole.OWNER) {
      throw new BusinessException(ErrorCode.PROJECT_PERMISSION_DENIED, "权限不足");
    }
  }

  /** 获取当前用户 ID */
  private Long getCurrentUserId() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof UserDetailsImpl) {
      return ((UserDetailsImpl) principal).getId();
    }
    throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
  }
}
