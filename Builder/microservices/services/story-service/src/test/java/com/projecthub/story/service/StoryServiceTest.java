package com.projecthub.story.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.projecthub.common.api.result.Result;
import com.projecthub.common.security.domain.LoginUser;
import com.projecthub.common.security.util.UserContextHolder;
import com.projecthub.story.dto.*;
import com.projecthub.story.entity.Epic;
import com.projecthub.story.entity.UserStory;
import com.projecthub.story.feign.ProjectServiceClient;
import com.projecthub.story.feign.dto.ProjectInfoDTO;
import com.projecthub.story.repository.EpicRepository;
import com.projecthub.story.repository.UserStoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Story Service 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StoryService 单元测试")
class StoryServiceTest {

    @Mock
    private EpicRepository epicRepository;

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private ProjectServiceClient projectServiceClient;

    private StoryService storyService;

    private Epic testEpic;
    private UserStory testStory;

    @BeforeEach
    void setUp() {
        storyService = new StoryService(epicRepository, userStoryRepository, projectServiceClient);

        // 设置测试用户上下文
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(1L);
        loginUser.setUsername("testuser");
        UserContextHolder.setUser(loginUser);

        // 创建测试史诗
        testEpic = new Epic();
        testEpic.setId(1L);
        testEpic.setName("测试史诗");
        testEpic.setDescription("这是一个测试史诗");
        testEpic.setProjectId(1L);
        testEpic.setStatus("OPEN");
        testEpic.setPriority(1);
        testEpic.setCreatorId(1L);
        testEpic.setDeleted(0);
        testEpic.setCreatedAt(LocalDateTime.now());

        // 创建测试用户故事
        testStory = new UserStory();
        testStory.setId(1L);
        testStory.setStoryKey("STORY-1-ABC123");
        testStory.setTitle("测试用户故事");
        testStory.setDescription("这是一个测试用户故事");
        testStory.setAcceptanceCriteria("验收标准");
        testStory.setEpicId(1L);
        testStory.setProjectId(1L);
        testStory.setAssigneeId(2L);
        testStory.setStatus("OPEN");
        testStory.setPriority("MEDIUM");
        testStory.setStoryPoints(5);
        testStory.setCreatorId(1L);
        testStory.setDeleted(0);
        testStory.setCreatedAt(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    // ==================== Epic 测试 ====================

    @Test
    @DisplayName("创建史诗成功")
    void createEpic_Success() {
        // Given
        CreateEpicRequest request = new CreateEpicRequest();
        request.setName("新史诗");
        request.setDescription("新描述");
        request.setProjectId(1L);
        request.setPriority(2);

        when(epicRepository.insert(any(Epic.class))).thenReturn(1);

        // When
        EpicVO result = storyService.createEpic(request);

        // Then
        assertNotNull(result);
        assertEquals("新史诗", result.getName());
        assertEquals("新描述", result.getDescription());

        ArgumentCaptor<Epic> epicCaptor = ArgumentCaptor.forClass(Epic.class);
        verify(epicRepository).insert(epicCaptor.capture());
        Epic capturedEpic = epicCaptor.getValue();
        assertEquals("新史诗", capturedEpic.getName());
        assertEquals("OPEN", capturedEpic.getStatus());
        assertEquals(1L, capturedEpic.getCreatorId());
    }

    @Test
    @DisplayName("创建史诗 - 优先级为空时使用默认值")
    void createEpic_DefaultPriority() {
        // Given
        CreateEpicRequest request = new CreateEpicRequest();
        request.setName("史诗");
        request.setProjectId(1L);
        // priority not set

        when(epicRepository.insert(any(Epic.class))).thenReturn(1);

        // When
        EpicVO result = storyService.createEpic(request);

        // Then
        assertNotNull(result);
        ArgumentCaptor<Epic> epicCaptor = ArgumentCaptor.forClass(Epic.class);
        verify(epicRepository).insert(epicCaptor.capture());
        Epic capturedEpic = epicCaptor.getValue();
        assertEquals(0, capturedEpic.getPriority());
    }

    @Test
    @DisplayName("根据 ID 获取史诗成功")
    void getEpicById_Success() {
        // Given
        when(epicRepository.selectById(1L)).thenReturn(testEpic);
        when(userStoryRepository.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        // When
        EpicVO result = storyService.getEpicById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试史诗", result.getName());
        assertEquals(3, result.getStoryCount());
    }

    @Test
    @DisplayName("根据 ID 获取史诗失败 - 史诗不存在")
    void getEpicById_NotFound() {
        // Given
        when(epicRepository.selectById(999L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            storyService.getEpicById(999L);
        });
        assertEquals("Epic not found: 999", exception.getMessage());
    }

    @Test
    @DisplayName("根据项目 ID 获取史诗列表")
    void getEpicsByProjectId_Success() {
        // Given
        Epic epic2 = new Epic();
        epic2.setId(2L);
        epic2.setName("史诗 2");
        epic2.setProjectId(1L);

        when(epicRepository.findByProjectId(1L)).thenReturn(Arrays.asList(testEpic, epic2));
        when(userStoryRepository.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // When
        List<EpicVO> result = storyService.getEpicsByProjectId(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("更新史诗成功")
    void updateEpic_Success() {
        // Given
        CreateEpicRequest request = new CreateEpicRequest();
        request.setName("更新后的史诗");
        request.setDescription("更新后的描述");
        request.setPriority(5);

        when(epicRepository.selectById(1L)).thenReturn(testEpic);
        when(epicRepository.updateById(any(Epic.class))).thenReturn(1);

        // When
        EpicVO result = storyService.updateEpic(1L, request);

        // Then
        assertNotNull(result);
        assertEquals("更新后的史诗", result.getName());

        ArgumentCaptor<Epic> epicCaptor = ArgumentCaptor.forClass(Epic.class);
        verify(epicRepository).updateById(epicCaptor.capture());
        Epic capturedEpic = epicCaptor.getValue();
        assertEquals("更新后的史诗", capturedEpic.getName());
        assertEquals(5, capturedEpic.getPriority());
        assertNotNull(capturedEpic.getUpdatedAt());
    }

    @Test
    @DisplayName("更新史诗部分字段")
    void updateEpic_PartialFields() {
        // Given
        CreateEpicRequest request = new CreateEpicRequest();
        request.setName("只更新名称");
        // description and priority not set

        when(epicRepository.selectById(1L)).thenReturn(testEpic);
        when(epicRepository.updateById(any(Epic.class))).thenReturn(1);

        // When
        EpicVO result = storyService.updateEpic(1L, request);

        // Then
        ArgumentCaptor<Epic> epicCaptor = ArgumentCaptor.forClass(Epic.class);
        verify(epicRepository).updateById(epicCaptor.capture());
        Epic capturedEpic = epicCaptor.getValue();
        assertEquals("只更新名称", capturedEpic.getName());
        assertEquals("这是一个测试史诗", capturedEpic.getDescription()); // Unchanged
    }

    @Test
    @DisplayName("更新史诗失败 - 史诗不存在")
    void updateEpic_NotFound() {
        // Given
        CreateEpicRequest request = new CreateEpicRequest();
        when(epicRepository.selectById(999L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            storyService.updateEpic(999L, request);
        });
        assertEquals("Epic not found: 999", exception.getMessage());
    }

    @Test
    @DisplayName("删除史诗成功")
    void deleteEpic_Success() {
        // Given
        when(epicRepository.deleteById(1L)).thenReturn(1);

        // When
        storyService.deleteEpic(1L);

        // Then
        verify(epicRepository).deleteById(1L);
    }

    // ==================== User Story 测试 ====================

    @Test
    @DisplayName("创建用户故事成功")
    void createStory_Success() {
        // Given
        CreateStoryRequest request = new CreateStoryRequest();
        request.setTitle("新故事");
        request.setDescription("新描述");
        request.setAcceptanceCriteria("验收标准");
        request.setEpicId(1L);
        request.setProjectId(1L);
        request.setAssigneeId(2L);
        request.setPriority("HIGH");
        request.setStoryPoints(8);

        when(userStoryRepository.insert(any(UserStory.class))).thenReturn(1);

        // When
        StoryVO result = storyService.createStory(request);

        // Then
        assertNotNull(result);
        assertEquals("新故事", result.getTitle());

        ArgumentCaptor<UserStory> storyCaptor = ArgumentCaptor.forClass(UserStory.class);
        verify(userStoryRepository).insert(storyCaptor.capture());
        UserStory capturedStory = storyCaptor.getValue();
        assertEquals("新故事", capturedStory.getTitle());
        assertEquals("HIGH", capturedStory.getPriority());
        assertEquals("OPEN", capturedStory.getStatus());
        assertEquals(1L, capturedStory.getCreatorId());
        assertNotNull(capturedStory.getStoryKey());
    }

    @Test
    @DisplayName("创建用户故事 - 优先级为空时使用默认值")
    void createStory_DefaultPriority() {
        // Given
        CreateStoryRequest request = new CreateStoryRequest();
        request.setTitle("故事");
        request.setProjectId(1L);
        // priority not set

        when(userStoryRepository.insert(any(UserStory.class))).thenReturn(1);

        // When
        StoryVO result = storyService.createStory(request);

        // Then
        assertNotNull(result);
        ArgumentCaptor<UserStory> storyCaptor = ArgumentCaptor.forClass(UserStory.class);
        verify(userStoryRepository).insert(storyCaptor.capture());
        UserStory capturedStory = storyCaptor.getValue();
        assertEquals("MEDIUM", capturedStory.getPriority());
    }

    @Test
    @DisplayName("根据 ID 获取用户故事成功")
    void getStoryById_Success() {
        // Given
        when(userStoryRepository.selectById(1L)).thenReturn(testStory);

        // When
        StoryVO result = storyService.getStoryById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试用户故事", result.getTitle());
        assertEquals("STORY-1-ABC123", result.getStoryKey());
    }

    @Test
    @DisplayName("根据 ID 获取用户故事失败 - 故事不存在")
    void getStoryById_NotFound() {
        // Given
        when(userStoryRepository.selectById(999L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            storyService.getStoryById(999L);
        });
        assertEquals("User story not found: 999", exception.getMessage());
    }

    @Test
    @DisplayName("根据项目 ID 获取故事列表")
    void getStoriesByProjectId_Success() {
        // Given
        UserStory story2 = new UserStory();
        story2.setId(2L);
        story2.setTitle("故事 2");
        story2.setProjectId(1L);

        when(userStoryRepository.findByProjectId(1L)).thenReturn(Arrays.asList(testStory, story2));

        // When
        List<StoryVO> result = storyService.getStoriesByProjectId(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("根据史诗 ID 获取故事列表")
    void getStoriesByEpicId_Success() {
        // Given
        UserStory story2 = new UserStory();
        story2.setId(2L);
        story2.setTitle("故事 2");
        story2.setEpicId(1L);

        when(userStoryRepository.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testStory, story2));

        // When
        List<StoryVO> result = storyService.getStoriesByEpicId(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("分页获取用户故事")
    void getStoriesPage_Success() {
        // Given
        Page<UserStory> mockPage = new Page<>(1L, 10L);
        mockPage.setRecords(Arrays.asList(testStory));
        mockPage.setTotal(1L);

        when(userStoryRepository.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<StoryVO> result = storyService.getStoriesPage(1L, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("更新用户故事成功")
    void updateStory_Success() {
        // Given
        CreateStoryRequest request = new CreateStoryRequest();
        request.setTitle("更新后的标题");
        request.setDescription("更新后的描述");
        request.setAcceptanceCriteria("新验收标准");
        request.setEpicId(2L);
        request.setAssigneeId(3L);
        request.setPriority("LOW");
        request.setStoryPoints(13);

        when(userStoryRepository.selectById(1L)).thenReturn(testStory);
        when(userStoryRepository.updateById(any(UserStory.class))).thenReturn(1);

        // When
        StoryVO result = storyService.updateStory(1L, request);

        // Then
        assertNotNull(result);
        assertEquals("更新后的标题", result.getTitle());

        ArgumentCaptor<UserStory> storyCaptor = ArgumentCaptor.forClass(UserStory.class);
        verify(userStoryRepository).updateById(storyCaptor.capture());
        UserStory capturedStory = storyCaptor.getValue();
        assertEquals("更新后的标题", capturedStory.getTitle());
        assertEquals("LOW", capturedStory.getPriority());
        assertEquals(13, capturedStory.getStoryPoints());
        assertNotNull(capturedStory.getUpdatedAt());
    }

    @Test
    @DisplayName("更新用户故事部分字段")
    void updateStory_PartialFields() {
        // Given
        CreateStoryRequest request = new CreateStoryRequest();
        request.setTitle("只更新标题");
        // Other fields not set

        when(userStoryRepository.selectById(1L)).thenReturn(testStory);
        when(userStoryRepository.updateById(any(UserStory.class))).thenReturn(1);

        // When
        StoryVO result = storyService.updateStory(1L, request);

        // Then
        ArgumentCaptor<UserStory> storyCaptor = ArgumentCaptor.forClass(UserStory.class);
        verify(userStoryRepository).updateById(storyCaptor.capture());
        UserStory capturedStory = storyCaptor.getValue();
        assertEquals("只更新标题", capturedStory.getTitle());
        assertEquals("这是一个测试用户故事", capturedStory.getDescription()); // Unchanged
    }

    @Test
    @DisplayName("更新用户故事失败 - 故事不存在")
    void updateStory_NotFound() {
        // Given
        CreateStoryRequest request = new CreateStoryRequest();
        when(userStoryRepository.selectById(999L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            storyService.updateStory(999L, request);
        });
        assertEquals("User story not found: 999", exception.getMessage());
    }

    @Test
    @DisplayName("更新用户故事状态成功")
    void updateStoryStatus_Success() {
        // Given
        String newStatus = "IN_PROGRESS";
        when(userStoryRepository.selectById(1L)).thenReturn(testStory);
        when(userStoryRepository.updateById(any(UserStory.class))).thenReturn(1);

        // When
        StoryVO result = storyService.updateStoryStatus(1L, newStatus);

        // Then
        assertNotNull(result);
        assertEquals("IN_PROGRESS", result.getStatus());

        ArgumentCaptor<UserStory> storyCaptor = ArgumentCaptor.forClass(UserStory.class);
        verify(userStoryRepository).updateById(storyCaptor.capture());
        UserStory capturedStory = storyCaptor.getValue();
        assertEquals("IN_PROGRESS", capturedStory.getStatus());
        assertNotNull(capturedStory.getUpdatedAt());
    }

    @Test
    @DisplayName("更新用户故事状态失败 - 故事不存在")
    void updateStoryStatus_NotFound() {
        // Given
        when(userStoryRepository.selectById(999L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            storyService.updateStoryStatus(999L, "DONE");
        });
        assertEquals("User story not found: 999", exception.getMessage());
    }

    @Test
    @DisplayName("删除用户故事成功")
    void deleteStory_Success() {
        // Given
        when(userStoryRepository.deleteById(1L)).thenReturn(1);

        // When
        storyService.deleteStory(1L);

        // Then
        verify(userStoryRepository).deleteById(1L);
    }

    // ==================== Story Points Statistics 测试 ====================

    @Test
    @DisplayName("获取故事点统计 - 总体统计")
    void getStoryPointsStats_Success() {
        // Given
        UserStory story1 = new UserStory();
        story1.setId(1L);
        story1.setStoryPoints(5);
        story1.setStatus("DONE");

        UserStory story2 = new UserStory();
        story2.setId(2L);
        story2.setStoryPoints(8);
        story2.setStatus("IN_PROGRESS");

        UserStory story3 = new UserStory();
        story3.setId(3L);
        story3.setStoryPoints(3);
        story3.setStatus("OPEN");

        when(userStoryRepository.findByProjectId(1L))
                .thenReturn(Arrays.asList(story1, story2, story3));
        when(epicRepository.findByProjectId(1L)).thenReturn(Arrays.asList(testEpic));

        LambdaQueryWrapper<UserStory> wrapper = mock(LambdaQueryWrapper.class);
        when(userStoryRepository.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(story1));

        // When
        StoryPointsStatsVO stats = storyService.getStoryPointsStats(1L);

        // Then
        assertNotNull(stats);
        assertEquals(1L, stats.getProjectId());
        assertEquals(16, stats.getTotalPoints()); // 5 + 8 + 3
        assertEquals(5, stats.getCompletedPoints());
        assertEquals(8, stats.getInProgressPoints());
        assertEquals(3, stats.getNotStartedPoints());
        assertNotNull(stats.getCompletionRate());
    }

    @Test
    @DisplayName("获取故事点统计 - 无故事点")
    void getStoryPointsStats_NoStoryPoints() {
        // Given
        UserStory story = new UserStory();
        story.setId(1L);
        story.setStoryPoints(null); // No story points
        story.setStatus("OPEN");

        when(userStoryRepository.findByProjectId(1L)).thenReturn(Arrays.asList(story));
        when(epicRepository.findByProjectId(1L)).thenReturn(Arrays.asList());

        // When
        StoryPointsStatsVO stats = storyService.getStoryPointsStats(1L);

        // Then
        assertNotNull(stats);
        assertEquals(0, stats.getTotalPoints());
        assertEquals(0.0, stats.getCompletionRate());
    }

    @Test
    @DisplayName("获取故事点统计 - 完成率为 100%")
    void getStoryPointsStats_FullCompletion() {
        // Given
        UserStory story1 = new UserStory();
        story1.setId(1L);
        story1.setStoryPoints(5);
        story1.setStatus("DONE");

        UserStory story2 = new UserStory();
        story2.setId(2L);
        story2.setStoryPoints(8);
        story2.setStatus("COMPLETED");

        when(userStoryRepository.findByProjectId(1L))
                .thenReturn(Arrays.asList(story1, story2));
        when(epicRepository.findByProjectId(1L)).thenReturn(Arrays.asList());

        // When
        StoryPointsStatsVO stats = storyService.getStoryPointsStats(1L);

        // Then
        assertNotNull(stats);
        assertEquals(13, stats.getTotalPoints());
        assertEquals(13, stats.getCompletedPoints());
        assertEquals(100.0, stats.getCompletionRate());
    }

    // ==================== Project Service Feign Client 测试 ====================

    @Test
    @DisplayName("获取项目信息成功")
    void getProjectInfo_Success() {
        // Given
        ProjectInfoDTO projectInfo = new ProjectInfoDTO();
        projectInfo.setId(1L);
        projectInfo.setName("测试项目");

        Result<ProjectInfoDTO> result = Result.success(projectInfo);
        when(projectServiceClient.getProjectById(1L)).thenReturn(result);

        // When
        ProjectInfoDTO info = storyService.getProjectInfo(1L);

        // Then
        assertNotNull(info);
        assertEquals(1L, info.getId());
        assertEquals("测试项目", info.getName());
    }

    @Test
    @DisplayName("获取项目信息失败 - 返回 null")
    void getProjectInfo_ReturnsNull() {
        // Given
        Result<ProjectInfoDTO> result = Result.success(null);
        when(projectServiceClient.getProjectById(1L)).thenReturn(result);

        // When
        ProjectInfoDTO info = storyService.getProjectInfo(1L);

        // Then
        assertNull(info);
    }

    @Test
    @DisplayName("获取项目信息失败 - 异常")
    void getProjectInfo_Exception() {
        // Given
        when(projectServiceClient.getProjectById(1L))
                .thenThrow(new RuntimeException("Connection refused"));

        // When
        ProjectInfoDTO info = storyService.getProjectInfo(1L);

        // Then
        assertNull(info);
    }

    @Test
    @DisplayName("验证项目是否存在 - 存在")
    void isProjectValid_True() {
        // Given
        ProjectInfoDTO projectInfo = new ProjectInfoDTO();
        projectInfo.setId(1L);
        Result<ProjectInfoDTO> result = Result.success(projectInfo);
        when(projectServiceClient.getProjectById(1L)).thenReturn(result);

        // When
        boolean isValid = storyService.isProjectValid(1L);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("验证项目是否存在 - 不存在")
    void isProjectValid_False() {
        // Given
        when(projectServiceClient.getProjectById(999L))
                .thenThrow(new RuntimeException("Not found"));

        // When
        boolean isValid = storyService.isProjectValid(999L);

        // Then
        assertFalse(isValid);
    }

    // ==================== Epic VO 转换测试 ====================

    @Test
    @DisplayName("Epic VO 转换包含故事数量")
    void convertToEpicVO_WithStoryCount() {
        // Given
        when(epicRepository.selectById(1L)).thenReturn(testEpic);
        when(userStoryRepository.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // When
        EpicVO vo = storyService.getEpicById(1L);

        // Then
        assertNotNull(vo);
        assertEquals(5, vo.getStoryCount());
    }
}
