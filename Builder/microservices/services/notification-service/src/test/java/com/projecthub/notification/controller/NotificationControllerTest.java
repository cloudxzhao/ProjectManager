package com.projecthub.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecthub.common.api.result.Result;
import com.projecthub.common.core.exception.GlobalExceptionHandler;
import com.projecthub.notification.dto.CreateNotificationRequest;
import com.projecthub.notification.dto.NotificationVO;
import com.projecthub.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Notification Controller Unit Tests
 */
class NotificationControllerTest {

    private MockMvc mockMvc;
    private NotificationService notificationService;
    private ObjectMapper objectMapper;

    private NotificationVO testNotificationVO;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        NotificationController controller = new NotificationController(notificationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // Create test Notification VO
        testNotificationVO = new NotificationVO();
        testNotificationVO.setId(1L);
        testNotificationVO.setUserId(100L);
        testNotificationVO.setType("TASK_ASSIGNED");
        testNotificationVO.setTitle("新任务分配");
        testNotificationVO.setContent("您有一个新任务已分配");
        testNotificationVO.setChannel("IN_APP");
        testNotificationVO.setStatus("UNREAD");
        testNotificationVO.setCreatedAt(LocalDateTime.now());
    }

    // ==================== Get User Notifications Tests ====================

    @Test
    void getUserNotifications_Success() throws Exception {
        // Given
        List<NotificationVO> notifications = Arrays.asList(testNotificationVO);
        when(notificationService.getUserNotifications(eq(100L), any(), anyInt(), anyInt()))
                .thenReturn(notifications);

        // When & Then
        mockMvc.perform(get("/notifications")
                        .param("userId", "100")
                        .param("pageNum", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("新任务分配"));

        verify(notificationService).getUserNotifications(eq(100L), any(), eq(1), eq(20));
    }

    @Test
    void getUserNotifications_WithStatus() throws Exception {
        // Given
        List<NotificationVO> notifications = Arrays.asList(testNotificationVO);
        when(notificationService.getUserNotifications(eq(100L), eq("UNREAD"), anyInt(), anyInt()))
                .thenReturn(notifications);

        // When & Then
        mockMvc.perform(get("/notifications")
                        .param("userId", "100")
                        .param("status", "UNREAD")
                        .param("pageNum", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(notificationService).getUserNotifications(eq(100L), eq("UNREAD"), eq(1), eq(20));
    }

    @Test
    void getUserNotifications_Empty() throws Exception {
        // Given
        when(notificationService.getUserNotifications(eq(100L), any(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/notifications")
                        .param("userId", "100")
                        .param("pageNum", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(notificationService).getUserNotifications(eq(100L), any(), eq(1), eq(20));
    }

    // ==================== Mark As Read Tests ====================

    @Test
    void markAsRead_Success() throws Exception {
        // Given
        doNothing().when(notificationService).markAsRead(1L, 100L);

        // When & Then
        mockMvc.perform(put("/notifications/1/read")
                        .param("userId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(notificationService).markAsRead(1L, 100L);
    }

    @Test
    void markAsRead_NotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("通知不存在：999"))
                .when(notificationService).markAsRead(999L, 100L);

        // When & Then
        mockMvc.perform(put("/notifications/999/read")
                        .param("userId", "100"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("通知不存在：999"));

        verify(notificationService).markAsRead(999L, 100L);
    }

    @Test
    void markAsRead_Unauthorized() throws Exception {
        // Given
        doThrow(new RuntimeException("无权操作此通知"))
                .when(notificationService).markAsRead(1L, 999L);

        // When & Then
        mockMvc.perform(put("/notifications/1/read")
                        .param("userId", "999"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("无权操作此通知"));

        verify(notificationService).markAsRead(1L, 999L);
    }

    // ==================== Mark All As Read Tests ====================

    @Test
    void markAllAsRead_Success() throws Exception {
        // Given
        doNothing().when(notificationService).markAllAsRead(100L);

        // When & Then
        mockMvc.perform(put("/notifications/read-all")
                        .param("userId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(notificationService).markAllAsRead(100L);
    }

    // ==================== Create In-App Notification Tests ====================

    @Test
    void createInAppNotification_Success() throws Exception {
        // Given
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(100L);
        request.setType("TASK_ASSIGNED");
        request.setTitle("新任务");
        request.setContent("您有新任务");
        request.setChannel("IN_APP");

        when(notificationService.createInAppNotification(any(CreateNotificationRequest.class)))
                .thenReturn(testNotificationVO);

        // When & Then
        mockMvc.perform(post("/notifications/internal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("新任务分配"));

        verify(notificationService).createInAppNotification(any(CreateNotificationRequest.class));
    }

    // ==================== Send Email Notification Tests ====================

    @Test
    void sendEmailNotification_Success() throws Exception {
        // Given
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(100L);
        request.setType("TASK_ASSIGNED");
        request.setTitle("新任务通知");
        request.setContent("您有新任务");
        request.setRecipient("user@example.com");
        request.setChannel("EMAIL");

        NotificationVO emailNotification = new NotificationVO();
        emailNotification.setId(2L);
        emailNotification.setUserId(100L);
        emailNotification.setTitle("新任务通知");
        emailNotification.setChannel("EMAIL");
        emailNotification.setStatus("SENT");

        when(notificationService.sendEmailNotification(any(CreateNotificationRequest.class)))
                .thenReturn(emailNotification);

        // When & Then
        mockMvc.perform(post("/notifications/internal/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.channel").value("EMAIL"))
                .andExpect(jsonPath("$.data.status").value("SENT"));

        verify(notificationService).sendEmailNotification(any(CreateNotificationRequest.class));
    }
}
