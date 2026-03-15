package com.projecthub.module.project;

import static org.junit.jupiter.api.Assertions.*;

import com.projecthub.module.project.entity.Project;
import com.projecthub.module.project.entity.ProjectMember;
import com.projecthub.module.project.repository.ProjectMemberRepository;
import com.projecthub.module.project.repository.ProjectRepository;
import com.projecthub.module.project.service.PermissionService;
import com.projecthub.module.user.entity.SysPermission;
import com.projecthub.module.user.entity.SysRole;
import com.projecthub.module.user.entity.SysRolePermission;
import com.projecthub.module.user.entity.SysUserRole;
import com.projecthub.module.user.entity.User;
import com.projecthub.module.user.repository.SysPermissionRepository;
import com.projecthub.module.user.repository.SysRolePermissionRepository;
import com.projecthub.module.user.repository.SysRoleRepository;
import com.projecthub.module.user.repository.SysUserRoleRepository;
import com.projecthub.module.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** 权限服务集成测试类 包含全局权限校验和项目级权限校验测试 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("权限服务测试")
class PermissionServiceTest {

  @Autowired private PermissionService permissionService;

  @Autowired private UserRepository userRepository;

  @Autowired private SysRoleRepository sysRoleRepository;

  @Autowired private SysPermissionRepository sysPermissionRepository;

  @Autowired private SysUserRoleRepository sysUserRoleRepository;

  @Autowired private SysRolePermissionRepository sysRolePermissionRepository;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private ProjectMemberRepository projectMemberRepository;

  private Long adminUserId;
  private Long normalUserId;
  private Long guestUserId;
  private Long testProjectId;
  private Long otherProjectId;

  @BeforeEach
  void setUp() {
    // 清理测试数据（如果存在）
    cleanupTestData();

    // 初始化角色和权限数据
    initRolesAndPermissions();

    // ==================== 创建测试用户 ====================

    // 管理员用户
    User adminUser =
        User.builder()
            .username("permission_admin")
            .email("permission_admin@test.com")
            .password("$2a$10$49QV.C712rCF9d6DhN7WSOLnoWxJh0vjFXzXbdEp8eT9PDTJw4Eoe")
            .status(User.UserStatus.ACTIVE)
            .build();
    adminUser = userRepository.save(adminUser);
    adminUserId = adminUser.getId();

    // 普通用户
    User normalUser =
        User.builder()
            .username("permission_normal")
            .email("permission_normal@test.com")
            .password("$2a$10$49QV.C712rCF9d6DhN7WSOLnoWxJh0vjFXzXbdEp8eT9PDTJw4Eoe")
            .status(User.UserStatus.ACTIVE)
            .build();
    normalUser = userRepository.save(normalUser);
    normalUserId = normalUser.getId();

    // 访客用户
    User guestUser =
        User.builder()
            .username("permission_guest")
            .email("permission_guest@test.com")
            .password("$2a$10$49QV.C712rCF9d6DhN7WSOLnoWxJh0vjFXzXbdEp8eT9PDTJw4Eoe")
            .status(User.UserStatus.ACTIVE)
            .build();
    guestUser = userRepository.save(guestUser);
    guestUserId = guestUser.getId();

    // ==================== 分配角色 ====================

    // 为管理员用户分配 ADMIN 角色 (ID=1)
    SysUserRole adminRole =
        SysUserRole.builder().userId(adminUserId).roleId(1L).createdAt(LocalDateTime.now()).build();
    sysUserRoleRepository.save(adminRole);

    // 为普通用户分配 MEMBER 角色 (ID=4)
    SysUserRole normalRole =
        SysUserRole.builder()
            .userId(normalUserId)
            .roleId(4L)
            .createdAt(LocalDateTime.now())
            .build();
    sysUserRoleRepository.save(normalRole);

    // 为访客用户分配 GUEST 角色 (ID=5)
    SysUserRole guestRole =
        SysUserRole.builder().userId(guestUserId).roleId(5L).createdAt(LocalDateTime.now()).build();
    sysUserRoleRepository.save(guestRole);

    // ==================== 创建测试项目 ====================

    // 测试项目（管理员为所有者）
    Project testProject =
        new Project(
            null,
            "权限测试项目",
            "用于权限测试的项目",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            adminUserId,
            Project.ProjectStatus.ACTIVE,
            null,
            null,
            null,
            null,
            null);
    testProject = projectRepository.save(testProject);
    testProjectId = testProject.getId();

    // 其他项目
    Project otherProject =
        new Project(
            null,
            "其他项目",
            "其他项目",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            normalUserId,
            Project.ProjectStatus.ACTIVE,
            null,
            null,
            null,
            null,
            null);
    otherProject = projectRepository.save(otherProject);
    otherProjectId = otherProject.getId();

    // ==================== 添加项目成员 ====================

    // 管理员在自己的项目中为 OWNER
    ProjectMember adminMember =
        ProjectMember.builder()
            .projectId(testProjectId)
            .userId(adminUserId)
            .role(ProjectMember.ProjectMemberRole.OWNER)
            .joinedAt(LocalDateTime.now())
            .build();
    projectMemberRepository.save(adminMember);

    // 普通用户为项目 MEMBER
    ProjectMember normalMember =
        ProjectMember.builder()
            .projectId(testProjectId)
            .userId(normalUserId)
            .role(ProjectMember.ProjectMemberRole.MEMBER)
            .joinedAt(LocalDateTime.now())
            .build();
    projectMemberRepository.save(normalMember);

    // 访客用户为项目 GUEST（只读）
    ProjectMember guestMember =
        ProjectMember.builder()
            .projectId(testProjectId)
            .userId(guestUserId)
            .role(ProjectMember.ProjectMemberRole.MEMBER)
            .joinedAt(LocalDateTime.now())
            .build();
    projectMemberRepository.save(guestMember);
  }

  /** 清理测试数据 */
  private void cleanupTestData() {
    // 删除可能存在的测试用户
    userRepository
        .findByUsername("permission_admin")
        .ifPresent(
            user -> {
              sysUserRoleRepository.deleteByUserId(user.getId());
              userRepository.delete(user);
            });
    userRepository
        .findByUsername("permission_normal")
        .ifPresent(
            user -> {
              sysUserRoleRepository.deleteByUserId(user.getId());
              userRepository.delete(user);
            });
    userRepository
        .findByUsername("permission_guest")
        .ifPresent(
            user -> {
              sysUserRoleRepository.deleteByUserId(user.getId());
              userRepository.delete(user);
            });
  }

  /** 初始化角色和权限数据 */
  private void initRolesAndPermissions() {
    LocalDateTime now = LocalDateTime.now();

    // 创建角色
    SysRole adminRole =
        SysRole.builder().name("超级管理员").code("ADMIN").description("系统超级管理员").createdAt(now).build();
    SysRole enterpriseRole =
        SysRole.builder()
            .name("企业管理员")
            .code("ENTERPRISE_ADMIN")
            .description("企业管理员")
            .createdAt(now)
            .build();
    SysRole managerRole =
        SysRole.builder()
            .name("项目经理")
            .code("PROJECT_MANAGER")
            .description("项目经理")
            .createdAt(now)
            .build();
    SysRole memberRole =
        SysRole.builder().name("团队成员").code("MEMBER").description("普通成员").createdAt(now).build();
    SysRole guestRole =
        SysRole.builder().name("访客").code("GUEST").description("访客").createdAt(now).build();

    sysRoleRepository.saveAll(
        List.of(adminRole, enterpriseRole, managerRole, memberRole, guestRole));

    // 创建权限
    List<String> permissionCodes =
        List.of(
            "PROJECT_VIEW",
            "PROJECT_CREATE",
            "PROJECT_EDIT",
            "PROJECT_DELETE",
            "PROJECT_MEMBER_MANAGE",
            "TASK_VIEW",
            "TASK_CREATE",
            "TASK_EDIT",
            "TASK_DELETE",
            "TASK_ASSIGN",
            "TASK_MOVE",
            "TASK_COMMENT",
            "STORY_VIEW",
            "STORY_CREATE",
            "STORY_EDIT",
            "STORY_DELETE",
            "EPIC_VIEW",
            "EPIC_CREATE",
            "EPIC_EDIT",
            "EPIC_DELETE",
            "ISSUE_VIEW",
            "ISSUE_CREATE",
            "ISSUE_EDIT",
            "ISSUE_DELETE",
            "WIKI_VIEW",
            "WIKI_EDIT",
            "WIKI_DELETE",
            "REPORT_VIEW",
            "ADMIN_ACCESS",
            "USER_MANAGE",
            "SYSTEM_CONFIG",
            "MEMBER_MANAGE");

    List<SysPermission> permissions =
        permissionCodes.stream()
            .map(
                code ->
                    SysPermission.builder()
                        .name(code)
                        .code(code)
                        .description("权限 " + code)
                        .createdAt(now)
                        .build())
            .toList();

    sysPermissionRepository.saveAll(permissions);

    // 分配权限给角色
    // ADMIN (ID=1) - 所有权限
    for (SysPermission permission : permissions) {
      SysRolePermission rp =
          SysRolePermission.builder().roleId(1L).permissionId(permission.getId()).build();
      sysRolePermissionRepository.save(rp);
    }

    // ENTERPRISE_ADMIN (ID=2) - 大部分权限
    List<String> enterprisePermissions =
        List.of(
            "PROJECT_VIEW",
            "PROJECT_CREATE",
            "PROJECT_EDIT",
            "PROJECT_DELETE",
            "PROJECT_MEMBER_MANAGE",
            "TASK_VIEW",
            "TASK_CREATE",
            "TASK_EDIT",
            "TASK_DELETE",
            "TASK_ASSIGN",
            "TASK_MOVE",
            "TASK_COMMENT",
            "STORY_VIEW",
            "STORY_CREATE",
            "STORY_EDIT",
            "STORY_DELETE",
            "EPIC_VIEW",
            "EPIC_CREATE",
            "EPIC_EDIT",
            "EPIC_DELETE",
            "ISSUE_VIEW",
            "ISSUE_CREATE",
            "ISSUE_EDIT",
            "ISSUE_DELETE",
            "WIKI_VIEW",
            "WIKI_EDIT",
            "WIKI_DELETE",
            "REPORT_VIEW");
    saveRolePermissions(2L, enterprisePermissions);

    // PROJECT_MANAGER (ID=3)
    List<String> managerPermissions =
        List.of(
            "PROJECT_VIEW",
            "PROJECT_CREATE",
            "PROJECT_EDIT",
            "PROJECT_MEMBER_MANAGE",
            "TASK_VIEW",
            "TASK_CREATE",
            "TASK_EDIT",
            "TASK_MOVE",
            "TASK_ASSIGN",
            "TASK_COMMENT",
            "STORY_VIEW",
            "STORY_CREATE",
            "STORY_EDIT",
            "EPIC_VIEW",
            "EPIC_CREATE",
            "EPIC_EDIT",
            "ISSUE_VIEW",
            "ISSUE_CREATE",
            "ISSUE_EDIT",
            "WIKI_VIEW",
            "WIKI_EDIT",
            "REPORT_VIEW");
    saveRolePermissions(3L, managerPermissions);

    // MEMBER (ID=4)
    List<String> memberPermissions =
        List.of(
            "PROJECT_VIEW",
            "PROJECT_CREATE",
            "TASK_VIEW",
            "TASK_CREATE",
            "TASK_EDIT",
            "TASK_COMMENT",
            "STORY_VIEW",
            "EPIC_VIEW",
            "ISSUE_VIEW",
            "WIKI_VIEW",
            "REPORT_VIEW");
    saveRolePermissions(4L, memberPermissions);

    // GUEST (ID=5) - 只读权限
    List<String> guestPermissions =
        List.of(
            "PROJECT_VIEW",
            "TASK_VIEW",
            "TASK_COMMENT",
            "STORY_VIEW",
            "EPIC_VIEW",
            "ISSUE_VIEW",
            "WIKI_VIEW",
            "REPORT_VIEW");
    saveRolePermissions(5L, guestPermissions);
  }

  /** 保存角色权限关联 */
  private void saveRolePermissions(Long roleId, List<String> permissionCodes) {
    List<SysPermission> permissions = sysPermissionRepository.findAllByCodeIn(permissionCodes);
    for (SysPermission permission : permissions) {
      SysRolePermission rp =
          SysRolePermission.builder().roleId(roleId).permissionId(permission.getId()).build();
      sysRolePermissionRepository.save(rp);
    }
  }

  // ==================== 全局权限校验测试 ====================

  @Test
  @DisplayName("1.1 管理员用户拥有全局权限 PROJECT_CREATE")
  void hasGlobalPermission_Admin_Success() {
    // When
    boolean hasPermission = permissionService.hasGlobalPermission(adminUserId, "PROJECT_CREATE");

    // Then
    assertTrue(hasPermission, "管理员应该拥有 PROJECT_CREATE 权限");
  }

  @Test
  @DisplayName("1.2 普通用户拥有全局权限 PROJECT_CREATE")
  void hasGlobalPermission_Normal_Success() {
    // When
    boolean hasPermission = permissionService.hasGlobalPermission(normalUserId, "PROJECT_CREATE");

    // Then
    assertTrue(hasPermission, "普通成员应该拥有 PROJECT_CREATE 权限");
  }

  @Test
  @DisplayName("1.3 访客用户没有全局权限 PROJECT_CREATE")
  void hasGlobalPermission_Guest_NoPermission() {
    // When
    boolean hasPermission = permissionService.hasGlobalPermission(guestUserId, "PROJECT_CREATE");

    // Then
    assertFalse(hasPermission, "访客用户不应该拥有 PROJECT_CREATE 权限");
  }

  @Test
  @DisplayName("1.4 访客用户拥有全局权限 PROJECT_VIEW")
  void hasGlobalPermission_Guest_ViewPermission() {
    // When
    boolean hasPermission = permissionService.hasGlobalPermission(guestUserId, "PROJECT_VIEW");

    // Then
    assertTrue(hasPermission, "访客用户应该拥有 PROJECT_VIEW 权限");
  }

  @Test
  @DisplayName("1.5 管理员用户拥有全部全局权限")
  void hasGlobalPermission_Admin_AllPermissions() {
    // Then
    assertTrue(permissionService.hasGlobalPermission(adminUserId, "PROJECT_CREATE"));
    assertTrue(permissionService.hasGlobalPermission(adminUserId, "PROJECT_EDIT"));
    assertTrue(permissionService.hasGlobalPermission(adminUserId, "PROJECT_DELETE"));
    assertTrue(permissionService.hasGlobalPermission(adminUserId, "TASK_CREATE"));
    assertTrue(permissionService.hasGlobalPermission(adminUserId, "TASK_DELETE"));
    assertTrue(permissionService.hasGlobalPermission(adminUserId, "ADMIN_ACCESS"));
    assertTrue(permissionService.hasGlobalPermission(adminUserId, "USER_MANAGE"));
  }

  @Test
  @DisplayName("1.6 不存在的权限代码")
  void hasGlobalPermission_NonExistentPermission() {
    // When
    boolean hasPermission =
        permissionService.hasGlobalPermission(adminUserId, "NON_EXISTENT_PERMISSION");

    // Then
    assertFalse(hasPermission, "不存在的权限代码应该返回 false");
  }

  // ==================== 项目级权限校验测试 ====================

  @Test
  @DisplayName("2.1 项目所有者拥有全部项目权限")
  void hasPermission_ProjectOwner_AllPermissions() {
    // When & Then
    assertTrue(
        permissionService.hasPermission(adminUserId, testProjectId, "TASK_CREATE"),
        "项目所有者应该拥有 TASK_CREATE 权限");
    assertTrue(
        permissionService.hasPermission(adminUserId, testProjectId, "TASK_DELETE"),
        "项目所有者应该拥有 TASK_DELETE 权限");
    assertTrue(
        permissionService.hasPermission(adminUserId, testProjectId, "TASK_MOVE"),
        "项目所有者应该拥有 TASK_MOVE 权限");
    assertTrue(
        permissionService.hasPermission(adminUserId, testProjectId, "MEMBER_MANAGE"),
        "项目所有者应该拥有 MEMBER_MANAGE 权限");
    assertTrue(
        permissionService.hasPermission(adminUserId, testProjectId, "STORY_DELETE"),
        "项目所有者应该拥有 STORY_DELETE 权限");
  }

  @Test
  @DisplayName("2.2 项目成员 (MEMBER) 拥有基本权限")
  void hasPermission_ProjectMember_BasicPermissions() {
    // When & Then
    assertTrue(
        permissionService.hasPermission(normalUserId, testProjectId, "TASK_CREATE"),
        "项目成员应该拥有 TASK_CREATE 权限");
    assertTrue(
        permissionService.hasPermission(normalUserId, testProjectId, "TASK_EDIT"),
        "项目成员应该拥有 TASK_EDIT 权限");
    assertTrue(
        permissionService.hasPermission(normalUserId, testProjectId, "TASK_VIEW"),
        "项目成员应该拥有 TASK_VIEW 权限");
    assertTrue(
        permissionService.hasPermission(normalUserId, testProjectId, "TASK_COMMENT"),
        "项目成员应该拥有 TASK_COMMENT 权限");
    assertTrue(
        permissionService.hasPermission(normalUserId, testProjectId, "STORY_VIEW"),
        "项目成员应该拥有 STORY_VIEW 权限");
    assertTrue(
        permissionService.hasPermission(normalUserId, testProjectId, "WIKI_VIEW"),
        "项目成员应该拥有 WIKI_VIEW 权限");
  }

  @Test
  @DisplayName("2.3 项目成员 (MEMBER) 没有高级权限")
  void hasPermission_ProjectMember_NoAdvancedPermissions() {
    // When & Then
    assertFalse(
        permissionService.hasPermission(normalUserId, testProjectId, "TASK_DELETE"),
        "项目成员不应该拥有 TASK_DELETE 权限");
    assertFalse(
        permissionService.hasPermission(normalUserId, testProjectId, "TASK_MOVE"),
        "项目成员不应该拥有 TASK_MOVE 权限");
    assertFalse(
        permissionService.hasPermission(normalUserId, testProjectId, "MEMBER_MANAGE"),
        "项目成员不应该拥有 MEMBER_MANAGE 权限");
    assertFalse(
        permissionService.hasPermission(normalUserId, testProjectId, "STORY_DELETE"),
        "项目成员不应该拥有 STORY_DELETE 权限");
  }

  @Test
  @DisplayName("2.4 非项目成员没有项目权限")
  void hasPermission_NonMember_NoPermission() {
    // 创建一个完全无关的用户
    User unrelatedUser =
        User.builder()
            .username("unrelated_user")
            .email("unrelated@test.com")
            .password("$2a$10$49QV.C712rCF9d6DhN7WSOLnoWxJh0vjFXzXbdEp8eT9PDTJw4Eoe")
            .status(User.UserStatus.ACTIVE)
            .build();
    unrelatedUser = userRepository.save(unrelatedUser);
    Long unrelatedUserId = unrelatedUser.getId();

    try {
      // When & Then
      assertFalse(
          permissionService.hasPermission(unrelatedUserId, testProjectId, "TASK_CREATE"),
          "非项目成员不应该有任何项目权限");
      assertFalse(
          permissionService.hasPermission(unrelatedUserId, testProjectId, "TASK_VIEW"),
          "非项目成员不应该有查看权限");
    } finally {
      userRepository.delete(unrelatedUser);
    }
  }

  @Test
  @DisplayName("2.5 项目所有者自动拥有权限（不依赖成员表）")
  void hasPermission_OwnerWithoutMemberRecord() {
    // 创建一个新项目，用户是所有者但不是成员表中的记录
    Project newProject =
        new Project(
            null,
            "新项目",
            "新项目",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            adminUserId,
            Project.ProjectStatus.ACTIVE,
            null,
            null,
            null,
            null,
            null);
    newProject = projectRepository.save(newProject);
    Long newProjectId = newProject.getId();

    try {
      // When & Then
      assertTrue(
          permissionService.hasPermission(adminUserId, newProjectId, "TASK_CREATE"),
          "项目所有者即使不在成员表中也应该有权限");
      assertTrue(
          permissionService.hasPermission(adminUserId, newProjectId, "TASK_DELETE"),
          "项目所有者应该拥有全部权限");
    } finally {
      projectRepository.delete(newProject);
    }
  }

  @Test
  @DisplayName("2.6 用户在自己拥有的项目中有权限")
  void hasPermission_UserOwnsProject() {
    // When & Then
    assertTrue(
        permissionService.hasPermission(normalUserId, otherProjectId, "TASK_CREATE"),
        "用户在自己拥有的项目中应该有权限");
    assertTrue(
        permissionService.hasPermission(normalUserId, otherProjectId, "TASK_DELETE"),
        "项目所有者应该有全部权限");
  }

  // ==================== 获取用户角色测试 ====================

  @Test
  @DisplayName("3.1 获取用户在项目中的角色 - 所有者")
  void getUserRoleInProject_Owner() {
    // When
    ProjectMember.ProjectMemberRole role =
        permissionService.getUserRoleInProject(adminUserId, testProjectId);

    // Then
    assertEquals(ProjectMember.ProjectMemberRole.OWNER, role);
  }

  @Test
  @DisplayName("3.2 获取用户在项目中的角色 - 成员")
  void getUserRoleInProject_Member() {
    // When
    ProjectMember.ProjectMemberRole role =
        permissionService.getUserRoleInProject(normalUserId, testProjectId);

    // Then
    assertEquals(ProjectMember.ProjectMemberRole.MEMBER, role);
  }

  @Test
  @DisplayName("3.3 获取用户在项目中的角色 - 非成员")
  void getUserRoleInProject_NonMember() {
    // 创建无关用户
    User unrelatedUser =
        User.builder()
            .username("unrelated_user2")
            .email("unrelated2@test.com")
            .password("$2a$10$49QV.C712rCF9d6DhN7WSOLnoWxJh0vjFXzXbdEp8eT9PDTJw4Eoe")
            .status(User.UserStatus.ACTIVE)
            .build();
    unrelatedUser = userRepository.save(unrelatedUser);
    Long unrelatedUserId = unrelatedUser.getId();

    try {
      // When
      ProjectMember.ProjectMemberRole role =
          permissionService.getUserRoleInProject(unrelatedUserId, testProjectId);

      // Then
      assertNull(role, "非项目成员应该返回 null");
    } finally {
      userRepository.delete(unrelatedUser);
    }
  }

  // ==================== 获取用户项目 ID 列表测试 ====================

  @Test
  @DisplayName("4.1 获取用户参与的所有项目 ID")
  void getUserProjectIds_Success() {
    // When
    List<Long> projectIds = permissionService.getUserProjectIds(adminUserId);

    // Then
    assertTrue(projectIds.contains(testProjectId), "应该包含测试项目 ID");
  }

  @Test
  @DisplayName("4.2 获取用户参与的所有项目 ID - 多个项目")
  void getUserProjectIds_MultipleProjects() {
    // When
    List<Long> projectIds = permissionService.getUserProjectIds(normalUserId);

    // Then
    assertTrue(projectIds.contains(otherProjectId), "应该包含用户拥有的项目");
    assertTrue(projectIds.contains(testProjectId), "应该包含用户参与的项目");
  }
}
