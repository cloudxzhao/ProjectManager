package com.projecthub.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecthub.common.api.result.Result;
import com.projecthub.common.core.exception.BusinessException;
import com.projecthub.common.core.exception.GlobalExceptionHandler;
import com.projecthub.common.security.domain.LoginUser;
import com.projecthub.common.security.util.UserContextHolder;
import com.projecthub.project.dto.AddMembersRequest;
import com.projecthub.project.dto.CreateProjectRequest;
import com.projecthub.project.dto.ProjectVO;
import com.projecthub.project.dto.UpdateProjectRequest;
import com.projecthub.project.service.ProjectService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Project Controller 单元测试
 */
@DisplayName("ProjectController 单元测试")
class ProjectControllerTest {

    private MockMvc mockMvc;
    private ProjectService projectService;
    private ObjectMapper objectMapper;
    private ProjectVO testProjectVO;

    @BeforeEach
    void setUp() {
        projectService = mock(ProjectService.class);
        ProjectController controller = new ProjectController(projectService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // 设置模拟用户上下文
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(1L);
        loginUser.setUsername("testuser");
        UserContextHolder.setUser(loginUser);

        testProjectVO = new ProjectVO();
        testProjectVO.setId(1L);
        testProjectVO.setName("测试项目");
        testProjectVO.setDescription("这是一个测试项目");
        testProjectVO.setIcon("icon.png");
        testProjectVO.setColor("#FF5722");
        testProjectVO.setOwnerId(1L);
        testProjectVO.setOwnerName("testuser");
        testProjectVO.setStartDate(LocalDate.of(2024, 1, 1));
        testProjectVO.setEndDate(LocalDate.of(2024, 12, 31));
        testProjectVO.setStatus("ACTIVE");
        testProjectVO.setProgress(50);
        testProjectVO.setMemberCount(3);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("创建项目成功")
    void createProject_Success() throws Exception {
        // Given
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("新项目");
        request.setDescription("新描述");
        request.setColor("#2196F3");

        ProjectVO created = new ProjectVO();
        created.setId(1L);
        created.setName("新项目");
        created.setDescription("新描述");

        when(projectService.createProject(any(CreateProjectRequest.class))).thenReturn(created);

        // When & Then
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("创建成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("新项目"));

        verify(projectService).createProject(any(CreateProjectRequest.class));
    }

    @Test
    @DisplayName("更新项目成功")
    void updateProject_Success() throws Exception {
        // Given
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("更新后的项目");
        request.setStatus("COMPLETED");

        ProjectVO updated = new ProjectVO();
        updated.setId(1L);
        updated.setName("更新后的项目");
        updated.setStatus("COMPLETED");

        when(projectService.updateProject(eq(1L), any(UpdateProjectRequest.class))).thenReturn(updated);

        // When & Then
        mockMvc.perform(put("/api/v1/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新成功"))
                .andExpect(jsonPath("$.data.name").value("更新后的项目"));

        verify(projectService).updateProject(eq(1L), any(UpdateProjectRequest.class));
    }

    @Test
    @DisplayName("更新项目失败 - 项目不存在")
    void updateProject_NotFound() throws Exception {
        // Given
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("新名称");

        when(projectService.updateProject(eq(999L), any(UpdateProjectRequest.class)))
                .thenThrow(new BusinessException(3001, "项目不存在"));

        // When & Then
        mockMvc.perform(put("/api/v1/projects/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(3001))
                .andExpect(jsonPath("$.message").value("项目不存在"));

        verify(projectService).updateProject(eq(999L), any(UpdateProjectRequest.class));
    }

    @Test
    @DisplayName("删除项目成功")
    void deleteProject_Success() throws Exception {
        // Given
        doNothing().when(projectService).deleteProject(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(projectService).deleteProject(1L);
    }

    @Test
    @DisplayName("删除项目失败 - 项目不存在")
    void deleteProject_NotFound() throws Exception {
        // Given
        doThrow(new BusinessException(3001, "项目不存在")).when(projectService).deleteProject(999L);

        // When & Then
        mockMvc.perform(delete("/api/v1/projects/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(3001))
                .andExpect(jsonPath("$.message").value("项目不存在"));

        verify(projectService).deleteProject(999L);
    }

    @Test
    @DisplayName("获取项目详情成功")
    void getProjectById_Success() throws Exception {
        // Given
        when(projectService.getProjectById(1L)).thenReturn(testProjectVO);

        // When & Then
        mockMvc.perform(get("/api/v1/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试项目"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(projectService).getProjectById(1L);
    }

    @Test
    @DisplayName("获取项目详情失败 - 项目不存在")
    void getProjectById_NotFound() throws Exception {
        // Given
        when(projectService.getProjectById(999L)).thenThrow(new BusinessException(3001, "项目不存在"));

        // When & Then
        mockMvc.perform(get("/api/v1/projects/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(3001))
                .andExpect(jsonPath("$.message").value("项目不存在"));

        verify(projectService).getProjectById(999L);
    }

    @Test
    @DisplayName("获取所有项目成功")
    void getAllProjects_Success() throws Exception {
        // Given
        List<ProjectVO> projects = Arrays.asList(testProjectVO);
        when(projectService.getAllProjects()).thenReturn(projects);

        // When & Then
        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(projectService).getAllProjects();
    }

    @Test
    @DisplayName("根据状态获取项目成功")
    void getProjectsByStatus_Success() throws Exception {
        // Given
        List<ProjectVO> projects = Arrays.asList(testProjectVO);
        when(projectService.getProjectsByStatus("ACTIVE")).thenReturn(projects);

        // When & Then
        mockMvc.perform(get("/api/v1/projects/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(projectService).getProjectsByStatus("ACTIVE");
    }

    @Test
    @DisplayName("获取我的项目成功")
    void getMyProjects_Success() throws Exception {
        // Given
        List<ProjectVO> projects = Arrays.asList(testProjectVO);
        when(projectService.getMyProjects()).thenReturn(projects);

        // When & Then
        mockMvc.perform(get("/api/v1/projects/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(projectService).getMyProjects();
    }

    @Test
    @DisplayName("添加项目成员成功")
    void addMembers_Success() throws Exception {
        // Given
        AddMembersRequest request = new AddMembersRequest();
        request.setUserIds(Arrays.asList(2L, 3L));
        request.setRole("MEMBER");

        when(projectService.addMembers(eq(1L), any(AddMembersRequest.class)))
                .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(post("/api/v1/projects/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("添加成功"));

        verify(projectService).addMembers(eq(1L), any(AddMembersRequest.class));
    }

    @Test
    @DisplayName("移除项目成员成功")
    void removeMember_Success() throws Exception {
        // Given
        doNothing().when(projectService).removeMember(1L, 2L);

        // When & Then
        mockMvc.perform(delete("/api/v1/projects/1/members/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("移除成功"));

        verify(projectService).removeMember(1L, 2L);
    }

    @Test
    @DisplayName("移除项目成员失败 - 不能移除所有者")
    void removeMember_CannotRemoveOwner() throws Exception {
        // Given
        doThrow(new BusinessException(400, "不能移除项目所有者"))
                .when(projectService).removeMember(1L, 1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/projects/1/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能移除项目所有者"));

        verify(projectService).removeMember(1L, 1L);
    }

    @Test
    @DisplayName("获取项目成员列表成功")
    void getProjectMembers_Success() throws Exception {
        // Given
        when(projectService.getProjectMembers(1L)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/projects/1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(projectService).getProjectMembers(1L);
    }

    @Test
    @DisplayName("检查是否是项目成员成功")
    void checkMembership_Success() throws Exception {
        // Given
        when(projectService.isMember(1L, 1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/projects/1/members/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(projectService).isMember(1L, 1L);
    }
}
