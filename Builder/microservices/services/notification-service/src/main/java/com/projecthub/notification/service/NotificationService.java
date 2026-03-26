package com.projecthub.notification.service;

import com.projecthub.common.mq.domain.EventMessage;
import com.projecthub.notification.dto.CreateNotificationRequest;
import com.projecthub.notification.dto.NotificationVO;
import com.projecthub.notification.entity.Notification;
import com.projecthub.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Notification Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    /**
     * 处理事件消息
     */
    @Transactional
    public void processEvent(EventMessage<?> event) {
        log.info("处理事件：eventType={}, eventId={}", event.getEventType(), event.getEventId());

        // 根据事件类型创建站内通知
        createInAppNotification(event);
    }

    /**
     * 创建站内通知
     */
    @Transactional
    public NotificationVO createInAppNotification(CreateNotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setChannel("IN_APP");
        notification.setStatus("UNREAD");
        notification.setRetryCount(0);

        notificationRepository.insert(notification);
        log.info("创建站内通知：userId={}, title={}", request.getUserId(), request.getTitle());

        return convertToNotificationVO(notification);
    }

    /**
     * 发送邮件通知
     */
    @Transactional
    public NotificationVO sendEmailNotification(CreateNotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setChannel("EMAIL");
        notification.setRecipient(request.getRecipient());
        notification.setStatus("PENDING");
        notification.setRetryCount(0);

        notificationRepository.insert(notification);

        try {
            // 发送邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getRecipient());
            message.setSubject(request.getTitle());
            message.setText(request.getContent());

            mailSender.send(message);

            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.updateById(notification);

            log.info("发送邮件通知成功：recipient={}, title={}", request.getRecipient(), request.getTitle());
        } catch (Exception e) {
            log.error("发送邮件通知失败：recipient={}", request.getRecipient(), e);
            notification.setStatus("FAILED");
            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setErrorMessage(e.getMessage());
            notificationRepository.updateById(notification);
        }

        return convertToNotificationVO(notification);
    }

    /**
     * 获取用户通知列表
     */
    public List<NotificationVO> getUserNotifications(Long userId, String status, int pageNum, int pageSize) {
        // TODO: 实现分页查询
        List<Notification> notifications = notificationRepository.findByUserId(userId);

        return notifications.stream()
                .filter(n -> status == null || status.equals(n.getStatus()))
                .map(this::convertToNotificationVO)
                .collect(Collectors.toList());
    }

    /**
     * 标记通知为已读
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.selectById(notificationId);
        if (notification == null) {
            throw new RuntimeException("通知不存在：" + notificationId);
        }

        if (!userId.equals(notification.getUserId())) {
            throw new RuntimeException("无权操作此通知");
        }

        notification.setStatus("READ");
        notificationRepository.updateById(notification);
        log.info("标记通知为已读：notificationId={}", notificationId);
    }

    /**
     * 批量标记通知为已读
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        for (Notification notification : notifications) {
            if ("UNREAD".equals(notification.getStatus())) {
                notification.setStatus("READ");
                notificationRepository.updateById(notification);
            }
        }
        log.info("批量标记通知为已读：userId={}", userId);
    }

    // Private helper methods

    private void createInAppNotification(EventMessage<?> event) {
        // 根据事件类型创建通知
        String title = buildNotificationTitle(event);
        String content = buildNotificationContent(event);

        if (title == null || content == null) {
            return; // 不处理的事件类型
        }

        // TODO: 从事件中获取目标用户 ID
        Long userId = extractUserIdFromEvent(event);
        if (userId == null) {
            return;
        }

        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setType(event.getEventType());
        request.setTitle(title);
        request.setContent(content);
        request.setChannel("IN_APP");

        createInAppNotification(request);
    }

    private String buildNotificationTitle(EventMessage<?> event) {
        switch (event.getEventType()) {
            case "task.assigned":
                return "新任务分配";
            case "task.status.changed":
                return "任务状态变更";
            case "issue.created":
                return "新问题创建";
            case "comment.created":
                return "新评论";
            case "project.member.added":
                return "新项目成员加入";
            default:
                return null;
        }
    }

    private String buildNotificationContent(EventMessage<?> event) {
        switch (event.getEventType()) {
            case "task.assigned":
                return "您有一个新任务已分配";
            case "task.status.changed":
                return "任务状态已更新";
            case "issue.created":
                return "新问题已创建";
            case "comment.created":
                return "收到新评论";
            case "project.member.added":
                return "您已加入新项目";
            default:
                return null;
        }
    }

    private Long extractUserIdFromEvent(EventMessage<?> event) {
        // TODO: 根据事件类型和数据提取目标用户 ID
        // 这里需要从事件数据中解析出用户 ID
        // 简化处理，暂时返回 null
        return null;
    }

    private NotificationVO convertToNotificationVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setUserId(notification.getUserId());
        vo.setType(notification.getType());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setChannel(notification.getChannel());
        vo.setRecipient(notification.getRecipient());
        vo.setStatus(notification.getStatus());
        vo.setSentAt(notification.getSentAt());
        vo.setCreatedAt(notification.getCreatedAt());
        return vo;
    }
}
