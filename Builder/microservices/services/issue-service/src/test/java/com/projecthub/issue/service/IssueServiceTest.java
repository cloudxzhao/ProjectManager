package com.projecthub.issue.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.projecthub.common.mq.service.EventPublisher;
import com.projecthub.issue.dto.CreateIssueRequest;
import com.projecthub.issue.dto.IssueVO;
import com.projecthub.issue.entity.Issue;
import com.projecthub.issue.repository.IssueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Issue Service Unit Tests
 */
class IssueServiceTest {

    private IssueRepository issueRepository;
    private EventPublisher eventPublisher;
    private IssueService issueService;

    private Issue testIssue;

    @BeforeEach
    void setUp() {
        issueRepository = mock(IssueRepository.class);
        eventPublisher = mock(EventPublisher.class);
        issueService = new IssueService(issueRepository, eventPublisher);

        // Create test Issue
        testIssue = new Issue();
        testIssue.setId(1L);
        testIssue.setIssueKey("ISSUE-1-ABC123");
        testIssue.setTitle("Test Issue");
        testIssue.setDescription("This is a test issue");
        testIssue.setStepsToReproduce("Step 1: Open app");
        testIssue.setEnvironment("Windows 11, Chrome");
        testIssue.setProjectId(1L);
        testIssue.setTaskId(2L);
        testIssue.setAssigneeId(3L);
        testIssue.setReporterId("1");
        testIssue.setStatus("OPEN");
        testIssue.setPriority("HIGH");
        testIssue.setSeverity("CRITICAL");
        testIssue.setType("BUG");
        testIssue.setStoryPoints(5);
        testIssue.setCreatorId(1L);
        testIssue.setCreatedAt(LocalDateTime.now());
        testIssue.setUpdatedAt(LocalDateTime.now());
        testIssue.setDeleted(0);
    }

    // ==================== Create Issue Tests ====================

    @Test
    void createIssue_Success() {
        // Given
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("New Issue");
        request.setDescription("New description");
        request.setProjectId(1L);
        request.setPriority("HIGH");
        request.setSeverity("CRITICAL");
        request.setType("BUG");

        when(issueRepository.insert(any(Issue.class))).thenReturn(1);
        doAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(1L);
            issue.setIssueKey("ISSUE-1-ABC123");
            return null;
        }).when(issueRepository).insert(any(Issue.class));

        // When
        IssueVO result = issueService.createIssue(request);

        // Then
        assertNotNull(result);
        assertEquals("New Issue", result.getTitle());
        assertEquals("HIGH", result.getPriority());
        assertEquals("CRITICAL", result.getSeverity());
        assertEquals("BUG", result.getType());
        assertEquals("OPEN", result.getStatus());

        verify(issueRepository).insert(any(Issue.class));
        verify(eventPublisher).publish(eq("issue.created"), any());
    }

    @Test
    void createIssue_WithDefaultValues() {
        // Given
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("Issue with defaults");
        request.setProjectId(1L);
        // Not setting priority, severity, type - should use defaults

        when(issueRepository.insert(any(Issue.class))).thenReturn(1);
        doAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(1L);
            issue.setIssueKey("ISSUE-1-ABC123");
            return null;
        }).when(issueRepository).insert(any(Issue.class));

        // When
        IssueVO result = issueService.createIssue(request);

        // Then
        assertNotNull(result);
        assertEquals("MEDIUM", result.getPriority()); // Default
        assertEquals("NORMAL", result.getSeverity()); // Default
        assertEquals("BUG", result.getType()); // Default
        assertEquals("OPEN", result.getStatus());
    }

    @Test
    void createIssue_WithDueDate() {
        // Given
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("Issue with due date");
        request.setProjectId(1L);
        request.setDueDate("2026-04-01T10:00:00");

        when(issueRepository.insert(any(Issue.class))).thenReturn(1);
        doAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(1L);
            issue.setIssueKey("ISSUE-1-ABC123");
            return null;
        }).when(issueRepository).insert(any(Issue.class));

        // When
        IssueVO result = issueService.createIssue(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getDueDate());
    }

    // ==================== Get Issue By ID Tests ====================

    @Test
    void getIssueById_Success() {
        // Given
        when(issueRepository.selectById(1L)).thenReturn(testIssue);

        // When
        IssueVO result = issueService.getIssueById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Issue", result.getTitle());
        assertEquals("ISSUE-1-ABC123", result.getIssueKey());
    }

    @Test
    void getIssueById_NotFound() {
        // Given
        when(issueRepository.selectById(999L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            issueService.getIssueById(999L);
        });
        assertEquals("Issue not found: 999", exception.getMessage());
    }

    // ==================== Get Issues By Project ID Tests ====================

    @Test
    void getIssuesByProjectId_Success() {
        // Given
        List<Issue> issues = Arrays.asList(testIssue);
        when(issueRepository.findByProjectId(1L)).thenReturn(issues);

        // When
        List<IssueVO> result = issueService.getIssuesByProjectId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Issue", result.get(0).getTitle());
    }

    @Test
    void getIssuesByProjectId_Empty() {
        // Given
        when(issueRepository.findByProjectId(1L)).thenReturn(Arrays.asList());

        // When
        List<IssueVO> result = issueService.getIssuesByProjectId(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Get Issues By Task ID Tests ====================

    @Test
    void getIssuesByTaskId_Success() {
        // Given
        List<Issue> issues = Arrays.asList(testIssue);
        when(issueRepository.findByTaskId(2L)).thenReturn(issues);

        // When
        List<IssueVO> result = issueService.getIssuesByTaskId(2L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==================== Get Issues Page Tests ====================

    @Test
    void getIssuesPage_Success() {
        // Given
        Page<Issue> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testIssue));
        mockPage.setTotal(1);

        when(issueRepository.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<IssueVO> result = issueService.getIssuesPage(1L, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCurrent());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    // ==================== Update Issue Tests ====================

    @Test
    void updateIssue_Success() {
        // Given
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated description");
        request.setPriority("LOW");

        when(issueRepository.selectById(1L)).thenReturn(testIssue);
        when(issueRepository.updateById(any(Issue.class))).thenReturn(1);

        // When
        IssueVO result = issueService.updateIssue(1L, request);

        // Then
        assertNotNull(result);
        verify(issueRepository).selectById(1L);
        verify(issueRepository).updateById(any(Issue.class));
    }

    @Test
    void updateIssue_PartialUpdate() {
        // Given
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("Only title updated");
        // Other fields are null - should not update

        when(issueRepository.selectById(1L)).thenReturn(testIssue);
        when(issueRepository.updateById(any(Issue.class))).thenReturn(1);

        // When
        IssueVO result = issueService.updateIssue(1L, request);

        // Then
        assertNotNull(result);
        verify(issueRepository).updateById(any(Issue.class));
    }

    @Test
    void updateIssue_NotFound() {
        // Given
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("New Title");

        when(issueRepository.selectById(999L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            issueService.updateIssue(999L, request);
        });
        assertEquals("Issue not found: 999", exception.getMessage());
    }

    // ==================== Update Issue Status Tests ====================

    @Test
    void updateIssueStatus_Success() {
        // Given
        when(issueRepository.selectById(1L)).thenReturn(testIssue);
        when(issueRepository.updateById(any(Issue.class))).thenReturn(1);

        // When
        IssueVO result = issueService.updateIssueStatus(1L, "IN_PROGRESS");

        // Then
        assertNotNull(result);
        verify(issueRepository).updateById(any(Issue.class));
    }

    @Test
    void updateIssueStatus_ToResolved_SetsResolvedAt() {
        // Given
        when(issueRepository.selectById(1L)).thenReturn(testIssue);
        when(issueRepository.updateById(any(Issue.class))).thenReturn(1);

        // When
        IssueVO result = issueService.updateIssueStatus(1L, "RESOLVED");

        // Then
        assertNotNull(result);
        assertNotNull(testIssue.getResolvedAt());
    }

    @Test
    void updateIssueStatus_ToClosed_SetsResolvedAt() {
        // Given
        when(issueRepository.selectById(1L)).thenReturn(testIssue);
        when(issueRepository.updateById(any(Issue.class))).thenReturn(1);

        // When
        IssueVO result = issueService.updateIssueStatus(1L, "CLOSED");

        // Then
        assertNotNull(result);
        assertNotNull(testIssue.getResolvedAt());
    }

    @Test
    void updateIssueStatus_NotFound() {
        // Given
        when(issueRepository.selectById(999L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            issueService.updateIssueStatus(999L, "CLOSED");
        });
        assertEquals("Issue not found: 999", exception.getMessage());
    }

    // ==================== Delete Issue Tests ====================

    @Test
    void deleteIssue_Success() {
        // Given
        doNothing().when(issueRepository).deleteById(1L);

        // When
        issueService.deleteIssue(1L);

        // Then
        verify(issueRepository).deleteById(1L);
    }

    @Test
    void deleteIssue_SoftDelete() {
        // Given - MyBatis Plus @TableLogic handles soft delete
        when(issueRepository.deleteById(1L)).thenReturn(1);

        // When
        issueService.deleteIssue(1L);

        // Then - Should mark as deleted, not actually remove
        verify(issueRepository).deleteById(1L);
    }
}
