package com.projecthub.module.permission;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecthub.module.permission.dto.ApprovePermissionRequestDTO;
import com.projecthub.module.permission.dto.CreatePermissionRequestDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** 权限申请控制器集成测试类 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("权限申请控制器测试")
class PermissionRequestControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @Autowired private SysPermissionRepository sysPermissionRepository;

  @Autowired private SysRoleRepository sysRoleRepository;

  @Autowired private SysRolePermissionRepository sysRolePermissionRepository;

  @Autowired private SysUserRoleRepository sysUserRoleRepository;

  private User testUser;
  private User adminUser;
  private SysPermission testPermission;
  private SysRole adminRole;
  private String authToken;

  @BeforeEach
  void setUp() throws Exception {
    // 清理安全上下文
    SecurityContextHolder.clearContext();

    // 创建测试用户
    testUser =
        User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iRF5CqkU1Ry7obgY.wcLDQjT0VZ6")
            .build();
    testUser = userRepository.save(testUser);

    // 创建管理员用户
    adminUser =
        User.builder()
            .username("admin")
            .email("admin@example.com")
            .password("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iRF5CqkU1Ry7obgY.wcLDQjT0VZ6")
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
  }

  @Test
  @DisplayName("1. 获取可申请的权限列表 - 成功")
  void getAvailablePermissions_Success() throws Exception {
    // Given
    authenticateAs(testUser);

    // When & Then
    mockMvc
        .perform(
            get("/api/v1/permissions/available")
                .with(authentication -> authentication)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  @DisplayName("2. 创建权限申请 - 成功")
  void createPermissionRequest_Success() throws Exception {
    // Given
    authenticateAs(testUser);
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .reason("申请测试权限用于功能开发")
            .build();

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/permissions/requests")
                .with(authentication -> authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.id").exists())
        .andExpect(jsonPath("$.data.status").value("PENDING"));
  }

  @Test
  @DisplayName("3. 创建权限申请 - 权限 ID 为空")
  void createPermissionRequest_MissingPermissionId() throws Exception {
    // Given
    authenticateAs(testUser);
    CreatePermissionRequestDTO dto = CreatePermissionRequestDTO.builder().reason("申请权限").build();

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/permissions/requests")
                .with(authentication -> authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("4. 创建权限申请 - 申请理由为空")
  void createPermissionRequest_MissingReason() throws Exception {
    // Given
    authenticateAs(testUser);
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder().permissionId(testPermission.getId()).build();

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/permissions/requests")
                .with(authentication -> authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("5. 获取我的申请记录 - 成功")
  void getMyPermissionRequests_Success() throws Exception {
    // Given
    authenticateAs(testUser);

    // When & Then
    mockMvc
        .perform(
            get("/api/v1/permissions/requests/my")
                .with(authentication -> authentication)
                .param("page", "1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.list").isArray());
  }

  @Test
  @DisplayName("6. 获取申请详情 - 成功")
  void getPermissionRequestDetail_Success() throws Exception {
    // Given
    authenticateAs(testUser);

    // 先创建一个申请
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .reason("测试申请")
            .build();

    String response =
        mockMvc
            .perform(
                post("/api/v1/permissions/requests")
                    .with(authentication -> authentication)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    Long requestId = objectMapper.readTree(response).get("data").get("id").asLong();

    // When & Then
    mockMvc
        .perform(
            get("/api/v1/permissions/requests/" + requestId)
                .with(authentication -> authentication)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.id").value(requestId));
  }

  @Test
  @DisplayName("7. 审批通过 - 成功（管理员）")
  void approvePermissionRequest_Success() throws Exception {
    // Given
    authenticateAs(testUser);

    // 创建一个申请
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .reason("申请权限")
            .build();

    String response =
        mockMvc
            .perform(
                post("/api/v1/permissions/requests")
                    .with(authentication -> authentication)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    Long requestId = objectMapper.readTree(response).get("data").get("id").asLong();

    // 切换到管理员用户
    authenticateAs(adminUser);

    ApprovePermissionRequestDTO approveDto =
        ApprovePermissionRequestDTO.builder().action("APPROVE").comment("同意申请").build();

    // When & Then
    mockMvc
        .perform(
            put("/api/v1/permissions/requests/" + requestId + "/approve")
                .with(authentication -> authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));
  }

  @Test
  @DisplayName("8. 审批拒绝 - 成功（管理员）")
  void rejectPermissionRequest_Success() throws Exception {
    // Given
    authenticateAs(testUser);

    // 创建一个申请
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .reason("申请权限")
            .build();

    String response =
        mockMvc
            .perform(
                post("/api/v1/permissions/requests")
                    .with(authentication -> authentication)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    Long requestId = objectMapper.readTree(response).get("data").get("id").asLong();

    // 切换到管理员用户
    authenticateAs(adminUser);

    ApprovePermissionRequestDTO rejectDto =
        ApprovePermissionRequestDTO.builder().action("REJECT").comment("拒绝申请").build();

    // When & Then
    mockMvc
        .perform(
            put("/api/v1/permissions/requests/" + requestId + "/reject")
                .with(authentication -> authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rejectDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));
  }

  @Test
  @DisplayName("9. 获取审批记录 - 成功")
  void getApprovalRecords_Success() throws Exception {
    // Given
    authenticateAs(testUser);

    // 创建一个申请
    CreatePermissionRequestDTO dto =
        CreatePermissionRequestDTO.builder()
            .permissionId(testPermission.getId())
            .reason("申请权限")
            .build();

    String response =
        mockMvc
            .perform(
                post("/api/v1/permissions/requests")
                    .with(authentication -> authentication)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    Long requestId = objectMapper.readTree(response).get("data").get("id").asLong();

    // When & Then
    mockMvc
        .perform(
            get("/api/v1/permissions/requests/" + requestId + "/approvals")
                .with(authentication -> authentication)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data").isArray());
  }

  /** 模拟用户登录 */
  private void authenticateAs(User user) {
    var userDetails =
        new com.projecthub.security.UserDetailsImpl(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPassword(),
            null,
            true,
            true,
            true,
            true);
    var authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
