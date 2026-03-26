package com.projecthub.issue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecthub.common.api.result.Result;
import com.projecthub.common.core.exception.BusinessException;
import com.projecthub.common.core.exception.GlobalExceptionHandler;
import com.projecthub.issue.dto.*;
import com.projecthub.issue.service.IssueService;
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
 * Issue Controller Unit Tests
 */
class IssueControllerTest {

    private MockMvc mockMvc;
    private IssueService issueService;
    private ObjectMapper objectMapper;

    private IssueVO testIssueVO;

    @BeforeEach
    void setUp() {
        issueService = mock(IssueService.class);
        IssueController controller = new IssueController(issueService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // Create test Issue VO
        testIssueVO = new IssueVO();
        testIssueVO.setId(1L);
        testIssueVO.setIssueKey("ISSUE-1-ABC123");
        testIssueVO.setTitle("Test Issue");
        testIssueVO.setDescription("This is a test issue");
        testIssueVO.setStepsToReproduce("Step 1: Open app");
        testIssueVO.setEnvironment("Windows 11, Chrome");
        testIssueVO.setProjectId(1L);
        testIssueVO.setTaskId(2L);
        testIssueVO.setAssigneeId(3L);
        testIssueVO.setAssigneeName("张三");
        testIssueVO.setReporterId("1");
        testIssueVO.setStatus("OPEN");
        testIssueVO.setPriority("HIGH");
        testIssueVO.setSeverity("CRITICAL");
        testIssueVO.setType("BUG");
        testIssueVO.setStoryPoints(5);
        testIssueVO.setCreatorId(1L);
    }

    // ==================== Create Issue Tests ====================

    @Test
    void createIssue_Success() throws Exception {
        // Given
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("New Issue");
        request.setDescription("New description");
        request.setProjectId(1L);
        request.setPriority("HIGH");
        request.setSeverity("CRITICAL");
        request.setType("BUG");

        when(issueService.createIssue(any(CreateIssueRequest.class))).thenReturn(testIssueVO);

        // When & Then
        mockMvc.perform(post("/api/v1/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Issue"))
                .andExpect(jsonPath("$.data.issueKey").value("ISSUE-1-ABC123"));

        verify(issueService).createIssue(any(CreateIssueRequest.class));
    }

    @Test
    void createIssue_EmptyTitle() throws Exception {
        // Given
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("");
        request.setProjectId(1L);

        // When & Then
        mockMvc.perform(post("/api/v1/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(issueService, never()).createIssue(any(CreateIssueRequest.class));
    }

    @Test
    void createIssue_EmptyProjectId() throws Exception {
        // Given
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("Issue");
        request.setProjectId(null);

        // When & Then
        mockMvc.perform(post("/api/v1/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(issueService, never()).createIssue(any(CreateIssueRequest.class));
    }

    // ==================== Get Issue By ID Tests ====================

    @Test
    void getIssueById_Success() throws Exception {
        // Given
        when(issueService.getIssueById(1L)).thenReturn(testIssueVO);

        // When & Then
        mockMvc.perform(get("/api/v1/issues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Issue"))
                .andExpect(jsonPath("$.data.issueKey").value("ISSUE-1-ABC123"))
                .andExpect(jsonPath("$.data.status").value("OPEN"));

        verify(issueService).getIssueById(1L);
    }

    @Test
    void getIssueById_NotFound() throws Exception {
        // Given
        when(issueService.getIssueById(999L))
                .thenThrow(new RuntimeException("Issue not found: 999"));

        // When & Then
        mockMvc.perform(get("/api/v1/issues/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Issue not found: 999"));

        verify(issueService).getIssueById(999L);
    }

    // ==================== Get Issues By Project ID Tests ====================

    @Test
    void getIssuesByProjectId_Success() throws Exception {
        // Given
        List<IssueVO> issues = Arrays.asList(testIssueVO);
        when(issueService.getIssuesByProjectId(1L)).thenReturn(issues);

        // When & Then
        mockMvc.perform(get("/api/v1/issues/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(issueService).getIssuesByProjectId(1L);
    }

    @Test
    void getIssuesByProjectId_Empty() throws Exception {
        // Given
        when(issueService.getIssuesByProjectId(1L)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/issues/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(issueService).getIssuesByProjectId(1L);
    }

    // ==================== Get Issues By Task ID Tests ====================

    @Test
    void getIssuesByTaskId_Success() throws Exception {
        // Given
        List<IssueVO> issues = Arrays.asList(testIssueVO);
        when(issueService.getIssuesByTaskId(2L)).thenReturn(issues);

        // When & Then
        mockMvc.perform(get("/api/v1/issues/task/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(issueService).getIssuesByTaskId(2L);
    }

    // ==================== Get Issues Page Tests ====================

    @Test
    void getIssuesPage_Success() throws Exception {
        // Given
        com.baomidou.mybatisplus.core.metadata.IPage<IssueVO> mockPage =
            mock(com.baomidou.mybatisplus.core.metadata.IPage.class);
        when(mockPage.getCurrent()).thenReturn(1L);
        when(mockPage.getSize()).thenReturn(10L);
        when(mockPage.getTotal()).thenReturn(1L);
        when(mockPage.getRecords()).thenReturn(Arrays.asList(testIssueVO));
        when(mockPage.getPages()).thenReturn(1L);

        when(issueService.getIssuesPage(eq(1L), eq(1), eq(10))).thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/v1/issues/project/1/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.total").value(1));

        verify(issueService).getIssuesPage(eq(1L), eq(1), eq(10));
    }

    // ==================== Update Issue Tests ====================

    @Test
    void updateIssue_Success() throws Exception {
        // Given
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated description");

        IssueVO updated = new IssueVO();
        updated.setId(1L);
        updated.setTitle("Updated Title");
        updated.setDescription("Updated description");

        when(issueService.updateIssue(eq(1L), any(CreateIssueRequest.class))).thenReturn(updated);

        // When & Then
        mockMvc.perform(put("/api/v1/issues/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Updated Title"));

        verify(issueService).updateIssue(eq(1L), any(CreateIssueRequest.class));
    }

    @Test
    void updateIssue_NotFound() throws Exception {
        // Given
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("New Title");

        when(issueService.updateIssue(eq(999L), any(CreateIssueRequest.class)))
                .thenThrow(new RuntimeException("Issue not found: 999"));

        // When & Then
        mockMvc.perform(put("/api/v1/issues/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Issue not found: 999"));

        verify(issueService).updateIssue(eq(999L), any(CreateIssueRequest.class));
    }

    // ==================== Update Issue Status Tests ====================

    @Test
    void updateIssueStatus_Success() throws Exception {
        // Given
        IssueVO updated = new IssueVO();
        updated.setId(1L);
        updated.setTitle("Test Issue");
        updated.setStatus("RESOLVED");

        when(issueService.updateIssueStatus(eq(1L), eq("RESOLVED"))).thenReturn(updated);

        // When & Then
        mockMvc.perform(put("/api/v1/issues/1/status")
                        .param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));

        verify(issueService).updateIssueStatus(eq(1L), eq("RESOLVED"));
    }

    @Test
    void updateIssueStatus_NotFound() throws Exception {
        // Given
        when(issueService.updateIssueStatus(eq(999L), anyString()))
                .thenThrow(new RuntimeException("Issue not found: 999"));

        // When & Then
        mockMvc.perform(put("/api/v1/issues/999/status")
                        .param("status", "CLOSED"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Issue not found: 999"));

        verify(issueService).updateIssueStatus(eq(999L), eq("CLOSED"));
    }

    // ==================== Delete Issue Tests ====================

    @Test
    void deleteIssue_Success() throws Exception {
        // Given - deleteIssue is void, use doNothing
        doNothing().when(issueService).deleteIssue(1L);

        // When & Then
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/issues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(issueService).deleteIssue(1L);
    }

    @Test
    void deleteIssue_NotFound() throws Exception {
        // Given - deleteIssue is void, use doThrow
        doThrow(new RuntimeException("Issue not found: 999"))
                .when(issueService).deleteIssue(999L);

        // When & Then
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/issues/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Issue not found: 999"));

        verify(issueService).deleteIssue(999L);
    }
}
