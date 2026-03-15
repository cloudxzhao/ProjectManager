package com.projecthub.module.permission;

import static org.junit.jupiter.api.Assertions.*;

import com.projecthub.common.exception.BusinessException;
import com.projecthub.module.permission.dto.ApprovePermissionRequestDTO;
import com.projecthub.module.permission.dto.CreatePermissionRequestDTO;
import com.projecthub.module.permission.repository.PermissionRequestRepository;
import com.projecthub.module.permission.service.PermissionRequestService;
import com.projecthub.module.permission.vo.AvailablePermissionVO;
import com.projecthub.module.permission.vo.PermissionApprovalVO;
import com.projecthub.module.permission.vo.PermissionRequestVO;
import com.projecthub.module.project.entity.Project;
import com.projecthub.module.project.entity.ProjectMember;
import com.projecthub.module.project.entity.ProjectMember.ProjectMemberRole;
import com.projecthub.module.project.repository.ProjectMemberRepository;
import com.projecthub.module.project.repository.ProjectRepository;
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
import com.projecthub.security.UserDetailsImpl;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** 权限申请服务集成测试类 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("权限申请服务测试")
class PermissionRequestServiceTest {

  @Autowired private PermissionRequestService permissionRequestService;

  @Autowired private PermissionRequestRepository permissionRequestRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private SysPermissionRepository sysPermissionRepository;

  @Autowired private SysRoleRepository sysRoleRepository;

  @Autowired private SysRolePermissionRepository sysRolePermissionRepository;

  @Autowired private SysUserRoleRepository sysUserRoleRepository;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private ProjectMemberRepository projectMemberRepository;

  private User testUser;
  private User adminUser;
  private SysPermission testPermission;
  private SysRole adminRole;
  private Project testProject;
  private UserDetailsImpl testUserDetails;
  private UserDetailsImpl adminUserDetails;

  @BeforeEach
  void setUp() {
    // 清理安全上下文
    SecurityContextHolder.clearContext();

    // 创建测试用户
    testUser =
        User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iRF5CqkU1Ry7obgY.wcLDQjT0VZ6")
            .status(User.UserStatus.ACTIVE)
            .build();
    testUser = userRepository.save(testUser);

    // 创建管理员用户
    adminUser =
        User.builder()
            .username("admin")
            .email("admin@example.com")
            .password("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iRF5CqkU1Ry7obgY.wcLDQjT0VZ6")
            .status(User.UserStatus.ACTIVE)
            .build();
    adminUser = userRepository.save(adminUser);

    // 创建测试权限
    testPermission =
        SysPermission.builder().name("测试权限").code("TEST_PERMISSION").description("用于测试的权限").build();
    testPermission = sysPermissionRepository.save(testPermission);

    // 创建管理员角色
    adminRole = SysRole.builder().name("管理员").code("ADMIN").description("系统管理员").build();
    adminRole = sysRoleRepository.save(adminRole);

    // 给管理员角色分配权限
    SysRolePermission rolePermission =
        SysRolePermission.builder()
            .roleId(adminRole.getId())
            .permissionId(testPermission.getId())
            .build();
    sysRolePermissionRepository.save(rolePermission);

    // 给管理员用户分配角色
    SysUserRole adminUserRole =
        SysUserRole.builder().userId(adminUser.getId()).roleId(adminRole.getId()).build();
    sysUserRoleRepository.save(adminUserRole);

    // 创建测试项目
    testProject = new Project();
    testProject.setName("测试项目");
    testProject.setDescription("用于权限申请测试的项目");
    testProject.setOwnerId(testUser.getId());
    testProject.setStartDate(LocalDateTime.now().toLocalDate());
    testProject.setEndDate(LocalDateTime.now().plusMonths(6).toLocalDate());
    testProject = projectRepository.save(testProject);

    // 添加项目成员
    ProjectMember member = new ProjectMember();
    member.setProjectId(testProject.getId());
    member.setUserId(testUser.getId());
    member.setRole(ProjectMemberRole.OWNER);
    projectMemberRepository.save(member);

    // 设置测试用户的安全上下文
    testUserDetails =
        new UserDetailsImpl(
            testUser.getId(),
            testUser.getUsername(),
            testUser.getEmail(),
            testUser.getPassword(),
            null,
            true,
            true,
            true,
            true);
  }

  private void setCurrentUser(UserDetailsImpl userDetails) {
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  // ==================== 获取可申请权限列表测试 ====================

  @Test
  @DisplayName("1.1 获取可申请的权限列表 - 成功")
  void getAvailablePermissions_Success() {
    // Given
    setCurrentUser(testUserDetails);

    // When
    List<AvailablePermissionVO> permissions = permissionRequestService.getAvailablePermissions();

    // Then
    assertNotNull(permissions);
    assertFalse(permissions.isEmpty());
    assertTrue(permissions.stream().anyMatch(p -> p.getId().equals(testPermission.getId())));
  }

  // ==================== 创建权限申请测试 ====================

  @Test
  @DisplayName("2.1 创建权限申请 - 成功")
  void createPermissionRequest_Success() {
    // Given
    setCurrentUser(testUserDetails);
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .reason("申请测试权限用于功能开发")
            .build();

    // When
    PermissionRequestVO result = permissionRequestService.createPermissionRequest(dto);

    // Then
    assertNotNull(result);
    assertNotNull(result.getId());
    assertEquals(testUser.getId(), result.getUserId());
    assertEquals(testPermission.getId(), result.getPermissionId());
    assertEquals("PENDING", result.getStatus());
    assertEquals("申请测试权限用于功能开发", result.getReason());
  }

  @Test
  @DisplayName("2.2 创建权限申请 - 权限不存在")
  void createPermissionRequest_PermissionNotFound() {
    // Given
    setCurrentUser(testUserDetails);
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder().permissionId(99999L).reason("申请权限").build();

    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              permissionRequestService.createPermissionRequest(dto);
            });
    assertEquals("权限不存在", exception.getMessage());
  }

  @Test
  @DisplayName("2.3 创建权限申请 - 项目不存在")
  void createPermissionRequest_ProjectNotFound() {
    // Given
    setCurrentUser(testUserDetails);
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .projectId(99999L)
            .reason("申请项目权限")
            .build();

    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              permissionRequestService.createPermissionRequest(dto);
            });
    assertEquals("项目不存在", exception.getMessage());
  }

  @Test
  @DisplayName("2.4 创建权限申请 - 重复申请")
  void createPermissionRequest_Duplicate() {
    // Given
    setCurrentUser(testUserDetails);
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .reason("第一次申请")
            .build();

    // 创建第一个申请
    permissionRequestService.createPermissionRequest(dto);

    // When & Then - 第二次申请应该失败
    dto.setReason("第二次申请");
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              permissionRequestService.createPermissionRequest(dto);
            });
    assertEquals("您已有待审批的相同权限申请", exception.getMessage());
  }

  @Test
  @DisplayName("2.5 创建权限申请 - 带项目 ID")
  void createPermissionRequest_WithProjectId() {
    // Given
    setCurrentUser(testUserDetails);
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .projectId(testProject.getId())
            .reason("申请项目权限")
            .build();

    // When
    PermissionRequestVO result = permissionRequestService.createPermissionRequest(dto);

    // Then
    assertNotNull(result);
    assertEquals(testProject.getId(), result.getProjectId());
  }

  // ==================== 获取申请列表测试 ====================

  @Test
  @DisplayName("3.1 获取我的申请记录 - 成功")
  void getMyPermissionRequests_Success() {
    // Given
    setCurrentUser(testUserDetails);
    // 创建测试数据
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .reason("测试申请")
            .build();
    permissionRequestService.createPermissionRequest(dto);

    // When
    var result = permissionRequestService.getMyPermissionRequests(1, 10, null);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getList().size());
    assertEquals(1L, result.getTotal());
  }

  @Test
  @DisplayName("3.2 获取我的申请记录 - 按状态筛选")
  void getMyPermissionRequests_ByStatus() {
    // Given
    setCurrentUser(testUserDetails);
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .reason("测试申请")
            .build();
    permissionRequestService.createPermissionRequest(dto);

    // When
    var result = permissionRequestService.getMyPermissionRequests(1, 10, "PENDING");

    // Then
    assertNotNull(result);
    assertEquals(1, result.getList().size());
    assertEquals("PENDING", result.getList().get(0).getStatus());
  }

  // ==================== 获取申请详情测试 ====================

  @Test
  @DisplayName("4.1 获取申请详情 - 成功")
  void getPermissionRequestDetail_Success() {
    // Given
    setCurrentUser(testUserDetails);
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .reason("测试申请")
            .build();
    PermissionRequestVO created = permissionRequestService.createPermissionRequest(dto);

    // When
    PermissionRequestVO result =
        permissionRequestService.getPermissionRequestWithApprovals(created.getId());

    // Then
    assertNotNull(result);
    assertEquals(created.getId(), result.getId());
    assertEquals("测试申请", result.getReason());
  }

  @Test
  @DisplayName("4.2 获取申请详情 - 申请不存在")
  void getPermissionRequestDetail_NotFound() {
    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              permissionRequestService.getPermissionRequestWithApprovals(99999L);
            });
    assertEquals("权限申请记录不存在", exception.getMessage());
  }

  // ==================== 审批测试 ====================

  @Test
  @DisplayName("5.1 审批通过 - 成功")
  void approvePermissionRequest_Success() {
    // Given
    setCurrentUser(testUserDetails);
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .reason("申请权限")
            .build();
    PermissionRequestVO request = permissionRequestService.createPermissionRequest(dto);

    // 切换到管理员用户
    adminUserDetails =
        new UserDetailsImpl(
            adminUser.getId(),
            adminUser.getUsername(),
            adminUser.getEmail(),
            adminUser.getPassword(),
            null,
            true,
            true,
            true,
            true);
    setCurrentUser(adminUserDetails);

    ApprovePermissionRequestDTO approveDto =
        ApprovePermissionRequestDTO.builder().action("APPROVE").comment("同意申请").build();

    // When
    assertDoesNotThrow(
        () -> {
          permissionRequestService.approvePermissionRequest(request.getId(), approveDto);
        });

    // Then
    PermissionRequestVO result =
        permissionRequestService.getPermissionRequestWithApprovals(request.getId());
    assertEquals("APPROVED", result.getStatus());
    assertNotNull(result.getApprovalRecords());
    assertFalse(result.getApprovalRecords().isEmpty());
    assertEquals("APPROVE", result.getApprovalRecords().get(0).getAction());
  }

  @Test
  @DisplayName("5.2 审批拒绝 - 成功")
  void rejectPermissionRequest_Success() {
    // Given
    setCurrentUser(testUserDetails);
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .reason("申请权限")
            .build();
    PermissionRequestVO request = permissionRequestService.createPermissionRequest(dto);

    // 切换到管理员用户
    adminUserDetails =
        new UserDetailsImpl(
            adminUser.getId(),
            adminUser.getUsername(),
            adminUser.getEmail(),
            adminUser.getPassword(),
            null,
            true,
            true,
            true,
            true);
    setCurrentUser(adminUserDetails);

    ApprovePermissionRequestDTO rejectDto =
        ApprovePermissionRequestDTO.builder().action("REJECT").comment("拒绝申请，理由不充分").build();

    // When
    assertDoesNotThrow(
        () -> {
          permissionRequestService.rejectPermissionRequest(request.getId(), rejectDto);
        });

    // Then
    PermissionRequestVO result =
        permissionRequestService.getPermissionRequestWithApprovals(request.getId());
    assertEquals("REJECTED", result.getStatus());
    assertEquals("REJECT", result.getApprovalRecords().get(0).getAction());
  }

  @Test
  @DisplayName("5.3 审批 - 非管理员用户")
  void approvePermissionRequest_NonAdmin() {
    // Given
    setCurrentUser(testUserDetails);
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .reason("申请权限")
            .build();
    PermissionRequestVO request = permissionRequestService.createPermissionRequest(dto);

    // 普通用户尝试审批
    ApprovePermissionRequestDTO approveDto =
        ApprovePermissionRequestDTO.builder().action("APPROVE").comment("同意").build();

    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              permissionRequestService.approvePermissionRequest(request.getId(), approveDto);
            });
    assertEquals("只有管理员可以审批权限申请", exception.getMessage());
  }

  @Test
  @DisplayName("5.4 审批 - 申请已被处理")
  void approvePermissionRequest_AlreadyProcessed() {
    // Given
    setCurrentUser(testUserDetails);
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .reason("申请权限")
            .build();
    PermissionRequestVO request = permissionRequestService.createPermissionRequest(dto);

    // 管理员审批
    adminUserDetails =
        new UserDetailsImpl(
            adminUser.getId(),
            adminUser.getUsername(),
            adminUser.getEmail(),
            adminUser.getPassword(),
            null,
            true,
            true,
            true,
            true);
    setCurrentUser(adminUserDetails);

    ApprovePermissionRequestDTO approveDto =
        ApprovePermissionRequestDTO.builder().action("APPROVE").comment("同意").build();
    permissionRequestService.approvePermissionRequest(request.getId(), approveDto);

    // 再次审批应该失败
    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              permissionRequestService.approvePermissionRequest(request.getId(), approveDto);
            });
    assertEquals("该申请已被处理，无法重复审批", exception.getMessage());
  }

  // ==================== 获取审批记录测试 ====================

  @Test
  @DisplayName("6.1 获取审批记录 - 成功")
  void getApprovalRecords_Success() {
    // Given
    setCurrentUser(testUserDetails);
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .reason("申请权限")
            .build();
    PermissionRequestVO request = permissionRequestService.createPermissionRequest(dto);

    // 管理员审批
    adminUserDetails =
        new UserDetailsImpl(
            adminUser.getId(),
            adminUser.getUsername(),
            adminUser.getEmail(),
            adminUser.getPassword(),
            null,
            true,
            true,
            true,
            true);
    setCurrentUser(adminUserDetails);

    ApprovePermissionRequestDTO approveDto =
        ApprovePermissionRequestDTO.builder().action("APPROVE").comment("同意申请").build();
    permissionRequestService.approvePermissionRequest(request.getId(), approveDto);

    // When
    List<PermissionApprovalVO> records =
        permissionRequestService.getApprovalRecords(request.getId());

    // Then
    assertNotNull(records);
    assertFalse(records.isEmpty());
    assertEquals(1, records.size());
    assertEquals(adminUser.getId(), records.get(0).getApproverId());
    assertEquals("同意申请", records.get(0).getComment());
  }

  @Test
  @DisplayName("6.2 获取审批记录 - 空记录")
  void getApprovalRecords_Empty() {
    // Given
    setCurrentUser(testUserDetails);
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .reason("申请权限")
            .build();
    PermissionRequestVO request = permissionRequestService.createPermissionRequest(dto);

    // When
    List<PermissionApprovalVO> records =
        permissionRequestService.getApprovalRecords(request.getId());

    // Then
    assertNotNull(records);
    assertTrue(records.isEmpty());
  }
}
