package com.projecthub.notification.service;

import com.projecthub.notification.dto.CreateNotificationRequest;
import com.projecthub.notification.dto.NotificationVO;
import com.projecthub.notification.entity.Notification;
import com.projecthub.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Notification Service Unit Tests
 */
class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private JavaMailSender mailSender;
    private NotificationService notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        mailSender = mock(JavaMailSender.class);
        notificationService = new NotificationService(notificationRepository, mailSender);

        // Create test Notification
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUserId(100L);
        testNotification.setType("TASK_ASSIGNED");
        testNotification.setTitle("新任务分配");
        testNotification.setContent("您有一个新任务已分配");
        testNotification.setChannel("IN_APP");
        testNotification.setStatus("UNREAD");
        testNotification.setRetryCount(0);
        testNotification.setCreatedAt(LocalDateTime.now());
        testNotification.setUpdatedAt(LocalDateTime.now());
        testNotification.setDeleted(0);
    }

    // ==================== Create In-App Notification Tests ====================

    @Test
    void createInAppNotification_Success() {
        // Given
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(100L);
        request.setType("TASK_ASSIGNED");
        request.setTitle("新任务");
        request.setContent("您有新任务");
        request.setChannel("IN_APP");

        when(notificationRepository.insert(any(Notification.class))).thenReturn(1);
        doAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            return null;
        }).when(notificationRepository).insert(any(Notification.class));

        // When
        NotificationVO result = notificationService.createInAppNotification(request);

        // Then
        assertNotNull(result);
        assertEquals("新任务", result.getTitle());
        assertEquals("IN_APP", result.getChannel());
        assertEquals("UNREAD", result.getStatus());
        assertEquals(100L, result.getUserId());

        verify(notificationRepository).insert(any(Notification.class));
    }

    @Test
    void createInAppNotification_SetsDefaultStatus() {
        // Given
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(100L);
        request.setTitle("Test");

        when(notificationRepository.insert(any(Notification.class))).thenReturn(1);
        doAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            return null;
        }).when(notificationRepository).insert(any(Notification.class));

        // When
        NotificationVO result = notificationService.createInAppNotification(request);

        // Then
        assertEquals("UNREAD", result.getStatus());
        assertEquals(0, result.getRetryCount());
    }

    // ==================== Send Email Notification Tests ====================

    @Test
    void sendEmailNotification_Success() {
        // Given
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(100L);
        request.setTitle("Email Notification");
        request.setContent("Test content");
        request.setRecipient("user@example.com");
        request.setChannel("EMAIL");

        when(notificationRepository.insert(any(Notification.class))).thenReturn(1);
        when(notificationRepository.updateById(any(Notification.class))).thenReturn(1);
        doAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            return null;
        }).when(notificationRepository).insert(any(Notification.class));

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        NotificationVO result = notificationService.sendEmailNotification(request);

        // Then
        assertNotNull(result);
        assertEquals("EMAIL", result.getChannel());
        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(notificationRepository).updateById(any(Notification.class));
    }

    @Test
    void sendEmailNotification_Failure() {
        // Given
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(100L);
        request.setTitle("Email Notification");
        request.setContent("Test content");
        request.setRecipient("user@example.com");

        when(notificationRepository.insert(any(Notification.class))).thenReturn(1);
        when(notificationRepository.updateById(any(Notification.class))).thenReturn(1);
        doAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            return null;
        }).when(notificationRepository).insert(any(Notification.class));

        doThrow(new RuntimeException("Mail server unavailable"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When
        NotificationVO result = notificationService.sendEmailNotification(request);

        // Then
        assertNotNull(result);
        assertEquals("FAILED", result.getStatus());
        assertEquals(1, result.getRetryCount());
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    // ==================== Get User Notifications Tests ====================

    @Test
    void getUserNotifications_AllNotifications() {
        // Given
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserId(100L)).thenReturn(notifications);

        // When
        List<NotificationVO> result = notificationService.getUserNotifications(100L, null, 1, 20);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("新任务分配", result.get(0).getTitle());
    }

    @Test
    void getUserNotifications_FilterByStatus() {
        // Given
        Notification readNotification = new Notification();
        readNotification.setId(2L);
        readNotification.setUserId(100L);
        readNotification.setStatus("READ");

        when(notificationRepository.findByUserId(100L))
                .thenReturn(Arrays.asList(testNotification, readNotification));

        // When
        List<NotificationVO> result = notificationService.getUserNotifications(100L, "UNREAD", 1, 20);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("UNREAD", result.get(0).getStatus());
    }

    @Test
    void getUserNotifications_Empty() {
        // Given
        when(notificationRepository.findByUserId(100L)).thenReturn(Arrays.asList());

        // When
        List<NotificationVO> result = notificationService.getUserNotifications(100L, null, 1, 20);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Mark As Read Tests ====================

    @Test
    void markAsRead_Success() {
        // Given
        when(notificationRepository.selectById(1L)).thenReturn(testNotification);
        when(notificationRepository.updateById(any(Notification.class))).thenReturn(1);

        // When
        notificationService.markAsRead(1L, 100L);

        // Then
        verify(notificationRepository).selectById(1L);
        verify(notificationRepository).updateById(any(Notification.class));
        assertEquals("READ", testNotification.getStatus());
    }

    @Test
    void markAsRead_NotFound() {
        // Given
        when(notificationRepository.selectById(999L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            notificationService.markAsRead(999L, 100L);
        });
        assertEquals("通知不存在：999", exception.getMessage());
    }

    @Test
    void markAsRead_Unauthorized() {
        // Given
        when(notificationRepository.selectById(1L)).thenReturn(testNotification);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            notificationService.markAsRead(1L, 999L); // Different user ID
        });
        assertEquals("无权操作此通知", exception.getMessage());
    }

    // ==================== Mark All As Read Tests ====================

    @Test
    void markAllAsRead_Success() {
        // Given
        Notification unread1 = new Notification();
        unread1.setId(1L);
        unread1.setUserId(100L);
        unread1.setStatus("UNREAD");

        Notification unread2 = new Notification();
        unread2.setId(2L);
        unread2.setUserId(100L);
        unread2.setStatus("UNREAD");

        Notification alreadyRead = new Notification();
        alreadyRead.setId(3L);
        alreadyRead.setUserId(100L);
        alreadyRead.setStatus("READ");

        when(notificationRepository.findByUserId(100L))
                .thenReturn(Arrays.asList(unread1, unread2, alreadyRead));
        when(notificationRepository.updateById(any(Notification.class))).thenReturn(1);

        // When
        notificationService.markAllAsRead(100L);

        // Then
        verify(notificationRepository).findByUserId(100L);
        verify(notificationRepository, times(2)).updateById(any(Notification.class)); // Only 2 unread
    }

    @Test
    void markAllAsRead_NoUnreadNotifications() {
        // Given
        Notification readNotification = new Notification();
        readNotification.setId(1L);
        readNotification.setUserId(100L);
        readNotification.setStatus("READ");

        when(notificationRepository.findByUserId(100L))
                .thenReturn(Arrays.asList(readNotification));

        // When
        notificationService.markAllAsRead(100L);

        // Then
        verify(notificationRepository).findByUserId(100L);
        verify(notificationRepository, never()).updateById(any(Notification.class));
    }
}
