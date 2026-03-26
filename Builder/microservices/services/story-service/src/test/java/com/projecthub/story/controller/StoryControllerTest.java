package com.projecthub.story.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecthub.common.api.result.Result;
import com.projecthub.common.core.exception.BusinessException;
import com.projecthub.common.core.exception.GlobalExceptionHandler;
import com.projecthub.story.dto.*;
import com.projecthub.story.service.StoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Story Controller Unit Tests
 */
class StoryControllerTest {

    private MockMvc mockMvc;
    private StoryService storyService;
    private ObjectMapper objectMapper;

    private StoryVO testStoryVO;
    private EpicVO testEpicVO;

    @BeforeEach
    void setUp() {
        storyService = mock(StoryService.class);
        StoryController controller = new StoryController(storyService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // Create test Story VO
        testStoryVO = new StoryVO();
        testStoryVO.setId(1L);
        testStoryVO.setStoryKey("STORY-1-ABC123");
        testStoryVO.setTitle("Test Story");
        testStoryVO.setDescription("This is a test story");
        testStoryVO.setAcceptanceCriteria("Acceptance criteria");
        testStoryVO.setEpicId(1L);
        testStoryVO.setProjectId(1L);
        testStoryVO.setAssigneeId(2L);
        testStoryVO.setStatus("OPEN");
        testStoryVO.setPriority("MEDIUM");
        testStoryVO.setStoryPoints(5);
        testStoryVO.setCreatorId(1L);

        // Create test Epic VO
        testEpicVO = new EpicVO();
        testEpicVO.setId(1L);
        testEpicVO.setName("Test Epic");
        testEpicVO.setDescription("This is a test epic");
        testEpicVO.setProjectId(1L);
        testEpicVO.setStatus("OPEN");
        testEpicVO.setPriority(1);
        testEpicVO.setStoryCount(3);
        testEpicVO.setCreatorId(1L);
    }

    // ==================== User Story Tests ====================

    @Test
    void createStory_Success() throws Exception {
        // Given
        CreateStoryRequest request = new CreateStoryRequest();
        request.setTitle("New Story");
        request.setDescription("New description");
        request.setProjectId(1L);
        request.setStoryPoints(5);

        when(storyService.createStory(any(CreateStoryRequest.class))).thenReturn(testStoryVO);

        // When & Then
        mockMvc.perform(post("/api/v1/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Story"));

        verify(storyService).createStory(any(CreateStoryRequest.class));
    }

    @Test
    void createStory_EmptyTitle() throws Exception {
        // Given
        CreateStoryRequest request = new CreateStoryRequest();
        request.setTitle("");
        request.setProjectId(1L);

        // When & Then
        mockMvc.perform(post("/api/v1/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(storyService, never()).createStory(any(CreateStoryRequest.class));
    }

    @Test
    void createStory_EmptyProjectId() throws Exception {
        // Given
        CreateStoryRequest request = new CreateStoryRequest();
        request.setTitle("Story");
        request.setProjectId(null);

        // When & Then
        mockMvc.perform(post("/api/v1/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(storyService, never()).createStory(any(CreateStoryRequest.class));
    }

    @Test
    void getStoryById_Success() throws Exception {
        // Given
        when(storyService.getStoryById(1L)).thenReturn(testStoryVO);

        // When & Then
        mockMvc.perform(get("/api/v1/stories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Story"))
                .andExpect(jsonPath("$.data.storyKey").value("STORY-1-ABC123"));

        verify(storyService).getStoryById(1L);
    }

    @Test
    void getStoryById_NotFound() throws Exception {
        // Given
        when(storyService.getStoryById(999L))
                .thenThrow(new RuntimeException("User story not found: 999"));

        // When & Then
        mockMvc.perform(get("/api/v1/stories/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("User story not found: 999"));

        verify(storyService).getStoryById(999L);
    }

    @Test
    void getStoriesByProjectId_Success() throws Exception {
        // Given
        List<StoryVO> stories = Arrays.asList(testStoryVO);
        when(storyService.getStoriesByProjectId(1L)).thenReturn(stories);

        // When & Then
        mockMvc.perform(get("/api/v1/stories/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(storyService).getStoriesByProjectId(1L);
    }

    @Test
    void getStoriesByProjectId_Empty() throws Exception {
        // Given
        when(storyService.getStoriesByProjectId(1L)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/stories/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(storyService).getStoriesByProjectId(1L);
    }

    @Test
    void getStoriesByEpicId_Success() throws Exception {
        // Given
        List<StoryVO> stories = Arrays.asList(testStoryVO);
        when(storyService.getStoriesByEpicId(1L)).thenReturn(stories);

        // When & Then
        mockMvc.perform(get("/api/v1/stories/epic/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(storyService).getStoriesByEpicId(1L);
    }

    @Test
    void getStoriesPage_Success() throws Exception {
        // Given
        com.baomidou.mybatisplus.core.metadata.IPage<StoryVO> mockPage =
            mock(com.baomidou.mybatisplus.core.metadata.IPage.class);
        when(mockPage.getCurrent()).thenReturn(1L);
        when(mockPage.getSize()).thenReturn(10L);
        when(mockPage.getTotal()).thenReturn(1L);
        when(mockPage.getRecords()).thenReturn(Arrays.asList(testStoryVO));
        when(mockPage.getPages()).thenReturn(1L);

        when(storyService.getStoriesPage(eq(1L), eq(1), eq(10))).thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/v1/stories/project/1/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.total").value(1));

        verify(storyService).getStoriesPage(eq(1L), eq(1), eq(10));
    }

    @Test
    void updateStory_Success() throws Exception {
        // Given
        CreateStoryRequest request = new CreateStoryRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated description");

        StoryVO updated = new StoryVO();
        updated.setId(1L);
        updated.setTitle("Updated Title");
        updated.setDescription("Updated description");

        when(storyService.updateStory(eq(1L), any(CreateStoryRequest.class))).thenReturn(updated);

        // When & Then
        mockMvc.perform(put("/api/v1/stories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Updated Title"));

        verify(storyService).updateStory(eq(1L), any(CreateStoryRequest.class));
    }

    @Test
    void updateStory_NotFound() throws Exception {
        // Given
        CreateStoryRequest request = new CreateStoryRequest();
        request.setTitle("New Title");

        when(storyService.updateStory(eq(999L), any(CreateStoryRequest.class)))
                .thenThrow(new RuntimeException("User story not found: 999"));

        // When & Then
        mockMvc.perform(put("/api/v1/stories/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("User story not found: 999"));

        verify(storyService).updateStory(eq(999L), any(CreateStoryRequest.class));
    }

    @Test
    void updateStoryStatus_Success() throws Exception {
        // Given
        StoryVO updated = new StoryVO();
        updated.setId(1L);
        updated.setTitle("Test Story");
        updated.setStatus("IN_PROGRESS");

        when(storyService.updateStoryStatus(eq(1L), eq("IN_PROGRESS"))).thenReturn(updated);

        // When & Then
        mockMvc.perform(put("/api/v1/stories/1/status")
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        verify(storyService).updateStoryStatus(eq(1L), eq("IN_PROGRESS"));
    }

    @Test
    void deleteStory_Success() throws Exception {
        // Given - deleteStory is void, use doNothing
        doNothing().when(storyService).deleteStory(1L);

        // When & Then
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/stories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(storyService).deleteStory(1L);
    }

    @Test
    void deleteStory_NotFound() throws Exception {
        // Given - deleteStory is void, use doThrow
        doThrow(new RuntimeException("User story not found: 999"))
                .when(storyService).deleteStory(999L);

        // When & Then
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/stories/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("User story not found: 999"));

        verify(storyService).deleteStory(999L);
    }

    // ==================== Epic Tests ====================

    @Test
    void createEpic_Success() throws Exception {
        // Given
        CreateEpicRequest request = new CreateEpicRequest();
        request.setName("New Epic");
        request.setDescription("New description");
        request.setProjectId(1L);
        request.setPriority(2);

        when(storyService.createEpic(any(CreateEpicRequest.class))).thenReturn(testEpicVO);

        // When & Then
        mockMvc.perform(post("/api/v1/stories/epics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Epic"));

        verify(storyService).createEpic(any(CreateEpicRequest.class));
    }

    @Test
    void createEpic_EmptyName() throws Exception {
        // Given
        CreateEpicRequest request = new CreateEpicRequest();
        request.setName("");
        request.setProjectId(1L);

        // When & Then
        mockMvc.perform(post("/api/v1/stories/epics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(storyService, never()).createEpic(any(CreateEpicRequest.class));
    }

    @Test
    void createEpic_EmptyProjectId() throws Exception {
        // Given
        CreateEpicRequest request = new CreateEpicRequest();
        request.setName("Epic");
        request.setProjectId(null);

        // When & Then
        mockMvc.perform(post("/api/v1/stories/epics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(storyService, never()).createEpic(any(CreateEpicRequest.class));
    }

    @Test
    void getEpicById_Success() throws Exception {
        // Given
        when(storyService.getEpicById(1L)).thenReturn(testEpicVO);

        // When & Then
        mockMvc.perform(get("/api/v1/stories/epics/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Epic"))
                .andExpect(jsonPath("$.data.storyCount").value(3));

        verify(storyService).getEpicById(1L);
    }

    @Test
    void getEpicById_NotFound() throws Exception {
        // Given
        when(storyService.getEpicById(999L))
                .thenThrow(new RuntimeException("Epic not found: 999"));

        // When & Then
        mockMvc.perform(get("/api/v1/stories/epics/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Epic not found: 999"));

        verify(storyService).getEpicById(999L);
    }

    @Test
    void getEpicsByProjectId_Success() throws Exception {
        // Given
        List<EpicVO> epics = Arrays.asList(testEpicVO);
        when(storyService.getEpicsByProjectId(1L)).thenReturn(epics);

        // When & Then
        mockMvc.perform(get("/api/v1/stories/epics/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(storyService).getEpicsByProjectId(1L);
    }

    @Test
    void updateEpic_Success() throws Exception {
        // Given
        CreateEpicRequest request = new CreateEpicRequest();
        request.setName("Updated Epic");
        request.setDescription("Updated description");
        request.setProjectId(1L);
        request.setPriority(5);

        EpicVO updated = new EpicVO();
        updated.setId(1L);
        updated.setName("Updated Epic");
        updated.setPriority(5);

        when(storyService.updateEpic(eq(1L), any(CreateEpicRequest.class))).thenReturn(updated);

        // When & Then
        mockMvc.perform(put("/api/v1/stories/epics/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("Updated Epic"));

        verify(storyService).updateEpic(eq(1L), any(CreateEpicRequest.class));
    }

    @Test
    void updateEpic_NotFound() throws Exception {
        // Given
        CreateEpicRequest request = new CreateEpicRequest();
        request.setName("New Name");

        when(storyService.updateEpic(eq(999L), any(CreateEpicRequest.class)))
                .thenThrow(new RuntimeException("Epic not found: 999"));

        // When & Then
        mockMvc.perform(put("/api/v1/stories/epics/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Epic not found: 999"));

        verify(storyService).updateEpic(eq(999L), any(CreateEpicRequest.class));
    }

    @Test
    void deleteEpic_Success() throws Exception {
        // Given - deleteEpic is void, use doNothing
        doNothing().when(storyService).deleteEpic(1L);

        // When & Then
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/stories/epics/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(storyService).deleteEpic(1L);
    }

    @Test
    void deleteEpic_NotFound() throws Exception {
        // Given - deleteEpic is void, use doThrow
        doThrow(new RuntimeException("Epic not found: 999"))
                .when(storyService).deleteEpic(999L);

        // When & Then
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/stories/epics/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Epic not found: 999"));

        verify(storyService).deleteEpic(999L);
    }

    // ==================== Story Points Statistics Tests ====================

    @Test
    void getStoryPointsStats_Success() throws Exception {
        // Given
        StoryPointsStatsVO stats = new StoryPointsStatsVO();
        stats.setProjectId(1L);
        stats.setTotalPoints(21);
        stats.setCompletedPoints(8);
        stats.setInProgressPoints(8);
        stats.setNotStartedPoints(5);
        stats.setCompletionRate(38.1);

        when(storyService.getStoryPointsStats(1L)).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/v1/stories/project/1/story-points-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.totalPoints").value(21))
                .andExpect(jsonPath("$.data.completedPoints").value(8))
                .andExpect(jsonPath("$.data.completionRate").value(38.1));

        verify(storyService).getStoryPointsStats(1L);
    }

    @Test
    void getStoryPointsStats_Empty() throws Exception {
        // Given
        StoryPointsStatsVO stats = new StoryPointsStatsVO();
        stats.setProjectId(1L);
        stats.setTotalPoints(0);
        stats.setCompletedPoints(0);
        stats.setCompletionRate(0.0);

        when(storyService.getStoryPointsStats(1L)).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/v1/stories/project/1/story-points-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalPoints").value(0))
                .andExpect(jsonPath("$.data.completionRate").value(0.0));

        verify(storyService).getStoryPointsStats(1L);
    }
}
