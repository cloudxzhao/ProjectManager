package com.projecthub.project.service;

import com.projecthub.common.api.result.Result;
import com.projecthub.common.core.exception.BusinessException;
import com.projecthub.common.mq.constant.EventType;
import com.projecthub.common.mq.domain.EventMessage;
import com.projecthub.common.mq.service.EventPublisher;
import com.projecthub.common.security.domain.LoginUser;
import com.projecthub.common.security.util.UserContextHolder;
import com.projecthub.project.client.UserClient;
import com.projecthub.project.dto.AddMembersRequest;
import com.projecthub.project.dto.CreateProjectRequest;
import com.projecthub.project.dto.ProjectMemberVO;
import com.projecthub.project.dto.ProjectVO;
import com.projecthub.project.dto.UpdateProjectRequest;
import com.projecthub.project.entity.Project;
import com.projecthub.project.entity.ProjectMember;
import com.projecthub.project.repository.ProjectMemberRepository;
import com.projecthub.project.repository.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Project Service 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService 单元测试")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private EventPublisher eventPublisher;

    private ProjectService projectService;

    private Project testProject;
    private ProjectMember testMember;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, projectMemberRepository, userClient, eventPublisher);

        // 设置测试用户上下文
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(1L);
        loginUser.setUsername("testuser");
        UserContextHolder.setUser(loginUser);

        // 创建测试项目
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("测试项目");
        testProject.setDescription("这是一个测试项目");
        testProject.setIcon("icon.png");
        testProject.setColor("#FF5722");
        testProject.setOwnerId(1L);
        testProject.setStartDate(LocalDate.of(2024, 1, 1));
        testProject.setEndDate(LocalDate.of(2024, 12, 31));
        testProject.setStatus("ACTIVE");
        testProject.setProgress(50);
        testProject.setCreatorId(1L);
        testProject.setDeleted(0);
        testProject.setCreatedAt(LocalDateTime.now());

        // 创建测试成员
        testMember = new ProjectMember();
        testMember.setId(1L);
        testMember.setProjectId(1L);
        testMember.setUserId(1L);
        testMember.setRole("OWNER");
        testMember.setJoinedAt(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("创建项目成功")
    void createProject_Success() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("新项目");
        request.setDescription("新描述");
        request.setColor("#2196F3");

        when(projectRepository.insert(any(Project.class))).thenReturn(1);
        when(projectMemberRepository.insert(any(ProjectMember.class))).thenReturn(1);

        // When
        ProjectVO result = projectService.createProject(request);

        // Then
        assertNotNull(result);
        assertEquals("新项目", result.getName());

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).insert(projectCaptor.capture());
        Project capturedProject = projectCaptor.getValue();
        assertEquals("新项目", capturedProject.getName());
        assertEquals(1L, capturedProject.getCreatorId());

        verify(projectMemberRepository, times(1)).insert(any(ProjectMember.class));
        verify(eventPublisher).publish(eq("project.created"), any(EventMessage.class));
    }

    @Test
    @DisplayName("创建项目并添加成员")
    void createProject_WithMembers() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("团队项目");
        request.setMemberIds(Arrays.asList(2L, 3L));

        when(projectRepository.insert(any(Project.class))).thenReturn(1);
        when(projectMemberRepository.insert(any(ProjectMember.class))).thenReturn(1);

        // When
        ProjectVO result = projectService.createProject(request);

        // Then
        assertNotNull(result);
        verify(projectMemberRepository, times(3)).insert(any(ProjectMember.class)); // Owner + 2 members
    }

    @Test
    @DisplayName("更新项目成功")
    void updateProject_Success() {
        // Given
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("更新后的项目名");
        request.setDescription("更新后的描述");
        request.setStatus("COMPLETED");
        request.setProgress(100);

        when(projectRepository.selectById(1L)).thenReturn(testProject);
        when(projectRepository.updateById(any(Project.class))).thenReturn(1);

        // When
        ProjectVO result = projectService.updateProject(1L, request);

        // Then
        assertNotNull(result);
        assertEquals("更新后的项目名", result.getName());

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).updateById(projectCaptor.capture());
        Project capturedProject = projectCaptor.getValue();
        assertEquals("更新后的项目名", capturedProject.getName());
        assertEquals("COMPLETED", capturedProject.getStatus());
        assertEquals(100, capturedProject.getProgress());

        verify(eventPublisher).publish(eq("project.updated"), any(EventMessage.class));
    }

    @Test
    @DisplayName("更新项目失败 - 项目不存在")
    void updateProject_NotFound() {
        // Given
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("新名称");
        when(projectRepository.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            projectService.updateProject(999L, request);
        });
        assertEquals(3001, exception.getCode());
        assertEquals("项目不存在", exception.getMessage());
    }

    @Test
    @DisplayName("删除项目成功")
    void deleteProject_Success() {
        // Given
        when(projectRepository.selectById(1L)).thenReturn(testProject);
        when(projectMemberRepository.findByProjectId(1L)).thenReturn(Arrays.asList(testMember));
        when(projectMemberRepository.deleteById(1L)).thenReturn(1);
        when(projectRepository.deleteById(1L)).thenReturn(1);

        // When
        projectService.deleteProject(1L);

        // Then
        verify(projectRepository).deleteById(1L);
        verify(projectMemberRepository).deleteById(1L);
        verify(eventPublisher).publish(eq("project.deleted"), any(EventMessage.class));
    }

    @Test
    @DisplayName("删除项目失败 - 项目不存在")
    void deleteProject_NotFound() {
        // Given
        when(projectRepository.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            projectService.deleteProject(999L);
        });
        assertEquals(3001, exception.getCode());
        assertEquals("项目不存在", exception.getMessage());
    }

    @Test
    @DisplayName("根据 ID 获取项目成功")
    void getProjectById_Success() {
        // Given
        when(projectRepository.selectById(1L)).thenReturn(testProject);

        // When
        ProjectVO result = projectService.getProjectById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试项目", result.getName());
    }

    @Test
    @DisplayName("根据 ID 获取项目失败 - 项目不存在")
    void getProjectById_NotFound() {
        // Given
        when(projectRepository.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            projectService.getProjectById(999L);
        });
        assertEquals(3001, exception.getCode());
        assertEquals("项目不存在", exception.getMessage());
    }

    @Test
    @DisplayName("获取所有项目")
    void getAllProjects_Success() {
        // Given
        Project p1 = new Project();
        p1.setId(1L);
        p1.setName("项目 1");
        p1.setDeleted(0);

        Project p2 = new Project();
        p2.setId(2L);
        p2.setName("项目 2");
        p2.setDeleted(0);

        Project deleted = new Project();
        deleted.setId(3L);
        deleted.setName("已删除");
        deleted.setDeleted(1);

        when(projectRepository.selectList(null)).thenReturn(Arrays.asList(p1, p2, deleted));

        // When
        List<ProjectVO> result = projectService.getAllProjects();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("根据状态获取项目")
    void getProjectsByStatus_Success() {
        // Given
        Project p1 = new Project();
        p1.setId(1L);
        p1.setStatus("ACTIVE");

        Project p2 = new Project();
        p2.setId(2L);
        p2.setStatus("ACTIVE");

        when(projectRepository.findByStatus("ACTIVE")).thenReturn(Arrays.asList(p1, p2));

        // When
        List<ProjectVO> result = projectService.getProjectsByStatus("ACTIVE");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
    }

    @Test
    @DisplayName("获取我的项目")
    void getMyProjects_Success() {
        // Given
        Project p1 = new Project();
        p1.setId(1L);
        p1.setName("我的项目 1");

        Project p2 = new Project();
        p2.setId(2L);
        p2.setName("我的项目 2");

        when(projectRepository.findByMemberId(1L)).thenReturn(Arrays.asList(p1, p2));

        // When
        List<ProjectVO> result = projectService.getMyProjects();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("获取我的项目 - 用户未登录")
    void getMyProjects_NoUser() {
        // Given
        UserContextHolder.clear();

        // When
        List<ProjectVO> result = projectService.getMyProjects();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("添加项目成员成功")
    void addMembers_Success() {
        // Given
        AddMembersRequest request = new AddMembersRequest();
        request.setUserIds(Arrays.asList(2L, 3L));
        request.setRole("MEMBER");

        when(projectRepository.selectById(1L)).thenReturn(testProject);
        when(projectMemberRepository.isMember(1L, 2L)).thenReturn(false);
        when(projectMemberRepository.isMember(1L, 3L)).thenReturn(false);
        when(projectMemberRepository.insert(any(ProjectMember.class))).thenReturn(1);

        // When
        List<ProjectMemberVO> result = projectService.addMembers(1L, request);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(projectMemberRepository, times(2)).insert(any(ProjectMember.class));
        verify(eventPublisher, times(2)).publish(eq("project.member.added"), any(EventMessage.class));
    }

    @Test
    @DisplayName("添加项目成员 - 跳过已是成员的")
    void addMembers_SkipExisting() {
        // Given
        AddMembersRequest request = new AddMembersRequest();
        request.setUserIds(Arrays.asList(2L, 3L));
        request.setRole("MEMBER");

        when(projectRepository.selectById(1L)).thenReturn(testProject);
        when(projectMemberRepository.isMember(1L, 2L)).thenReturn(true); // Already a member
        when(projectMemberRepository.isMember(1L, 3L)).thenReturn(false);
        when(projectMemberRepository.insert(any(ProjectMember.class))).thenReturn(1);

        // When
        List<ProjectMemberVO> result = projectService.addMembers(1L, request);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Only one new member added
        verify(projectMemberRepository, times(1)).insert(any(ProjectMember.class));
    }

    @Test
    @DisplayName("添加项目成员失败 - 项目不存在")
    void addMembers_ProjectNotFound() {
        // Given
        AddMembersRequest request = new AddMembersRequest();
        request.setUserIds(Arrays.asList(2L));
        when(projectRepository.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            projectService.addMembers(999L, request);
        });
        assertEquals(3001, exception.getCode());
        assertEquals("项目不存在", exception.getMessage());
    }

    @Test
    @DisplayName("移除项目成员成功")
    void removeMember_Success() {
        // Given
        ProjectMember member = new ProjectMember();
        member.setId(1L);
        member.setProjectId(1L);
        member.setUserId(2L);
        member.setRole("MEMBER");

        when(projectMemberRepository.findByProjectIdAndUserId(1L, 2L)).thenReturn(Optional.of(member));
        when(projectMemberRepository.deleteById(1L)).thenReturn(1);

        // When
        projectService.removeMember(1L, 2L);

        // Then
        verify(projectMemberRepository).deleteById(1L);
        verify(eventPublisher).publish(eq("project.member.removed"), any(EventMessage.class));
    }

    @Test
    @DisplayName("移除项目成员失败 - 成员不存在")
    void removeMember_MemberNotFound() {
        // Given
        when(projectMemberRepository.findByProjectIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            projectService.removeMember(1L, 2L);
        });
        assertEquals(3004, exception.getCode());
        assertEquals("成员不存在", exception.getMessage());
    }

    @Test
    @DisplayName("移除项目成员失败 - 不能移除所有者")
    void removeMember_CannotRemoveOwner() {
        // Given
        ProjectMember owner = new ProjectMember();
        owner.setId(1L);
        owner.setProjectId(1L);
        owner.setUserId(1L);
        owner.setRole("OWNER");

        when(projectMemberRepository.findByProjectIdAndUserId(1L, 1L)).thenReturn(Optional.of(owner));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            projectService.removeMember(1L, 1L);
        });
        assertEquals(400, exception.getCode());
        assertEquals("不能移除项目所有者", exception.getMessage());
    }

    @Test
    @DisplayName("获取项目成员列表")
    void getProjectMembers_Success() {
        // Given
        ProjectMember member = new ProjectMember();
        member.setId(1L);
        member.setProjectId(1L);
        member.setUserId(2L);
        member.setRole("MEMBER");

        when(projectMemberRepository.findByProjectId(1L)).thenReturn(Arrays.asList(member));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 2L);
        userInfo.put("username", "user2");
        userInfo.put("email", "user2@example.com");
        userInfo.put("avatar", "avatar.jpg");

        Result<List<Map<String, Object>>> userResult = Result.success(Arrays.asList(userInfo));
        when(userClient.getUsersByIds(Arrays.asList(2L))).thenReturn(userResult);

        // When
        List<ProjectMemberVO> result = projectService.getProjectMembers(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user2", result.get(0).getUsername());
    }

    @Test
    @DisplayName("获取项目成员列表 - 获取用户信息失败")
    void getProjectMembers_UserServiceError() {
        // Given
        ProjectMember member = new ProjectMember();
        member.setId(1L);
        member.setProjectId(1L);
        member.setUserId(2L);
        member.setRole("MEMBER");

        when(projectMemberRepository.findByProjectId(1L)).thenReturn(Arrays.asList(member));
        when(userClient.getUsersByIds(Arrays.asList(2L))).thenThrow(new RuntimeException("Service unavailable"));

        // When
        List<ProjectMemberVO> result = projectService.getProjectMembers(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getUsername()); // User info not populated
    }

    @Test
    @DisplayName("检查是否是项目成员 - 是")
    void isMember_True() {
        // Given
        when(projectMemberRepository.isMember(1L, 2L)).thenReturn(true);

        // When
        boolean result = projectService.isMember(1L, 2L);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("检查是否是项目成员 - 否")
    void isMember_False() {
        // Given
        when(projectMemberRepository.isMember(1L, 999L)).thenReturn(false);

        // When
        boolean result = projectService.isMember(1L, 999L);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("发布项目创建事件验证")
    void createProject_PublishesEvent() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("事件测试项目");
        when(projectRepository.insert(any(Project.class))).thenReturn(1);
        when(projectMemberRepository.insert(any(ProjectMember.class))).thenReturn(1);

        // When
        projectService.createProject(request);

        // Then
        ArgumentCaptor<EventMessage> eventCaptor = ArgumentCaptor.forClass(EventMessage.class);
        verify(eventPublisher).publish(eq("project.created"), eventCaptor.capture());

        EventMessage capturedEvent = eventCaptor.getValue();
        assertEquals(EventType.PROJECT_CREATED, capturedEvent.getEventType());
        assertEquals("project-service", capturedEvent.getSource());
    }

    @Test
    @DisplayName("发布项目更新事件验证")
    void updateProject_PublishesEvent() {
        // Given
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("更新");
        when(projectRepository.selectById(1L)).thenReturn(testProject);
        when(projectRepository.updateById(any(Project.class))).thenReturn(1);

        // When
        projectService.updateProject(1L, request);

        // Then
        ArgumentCaptor<EventMessage> eventCaptor = ArgumentCaptor.forClass(EventMessage.class);
        verify(eventPublisher).publish(eq("project.updated"), eventCaptor.capture());

        EventMessage capturedEvent = eventCaptor.getValue();
        assertEquals(EventType.PROJECT_UPDATED, capturedEvent.getEventType());
        assertEquals("project-service", capturedEvent.getSource());
        assertEquals(1L, capturedEvent.getData());
    }

    @Test
    @DisplayName("发布项目删除事件验证")
    void deleteProject_PublishesEvent() {
        // Given
        when(projectRepository.selectById(1L)).thenReturn(testProject);
        when(projectMemberRepository.findByProjectId(1L)).thenReturn(Arrays.asList(testMember));
        when(projectMemberRepository.deleteById(1L)).thenReturn(1);
        when(projectRepository.deleteById(1L)).thenReturn(1);

        // When
        projectService.deleteProject(1L);

        // Then
        ArgumentCaptor<EventMessage> eventCaptor = ArgumentCaptor.forClass(EventMessage.class);
        verify(eventPublisher).publish(eq("project.deleted"), eventCaptor.capture());

        EventMessage capturedEvent = eventCaptor.getValue();
        assertEquals(EventType.PROJECT_DELETED, capturedEvent.getEventType());
        assertEquals("project-service", capturedEvent.getSource());
        assertEquals(1L, capturedEvent.getData());
    }
}
