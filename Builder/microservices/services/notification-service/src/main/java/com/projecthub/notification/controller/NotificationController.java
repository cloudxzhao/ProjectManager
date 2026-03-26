package com.projecthub.notification.controller;

import com.projecthub.common.api.result.Result;
import com.projecthub.notification.dto.CreateNotificationRequest;
import com.projecthub.notification.dto.NotificationVO;
import com.projecthub.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Notification Controller
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "站内通知和邮件通知")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取用户通知列表
     */
    @GetMapping
    @Operation(summary = "获取用户通知列表")
    public Result<List<NotificationVO>> getUserNotifications(
            @RequestParam Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {

        List<NotificationVO> notifications = notificationService.getUserNotifications(userId, status, pageNum, pageSize);
        return Result.success(notifications);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记通知为已读")
    public Result<Void> markAsRead(
            @PathVariable Long id,
            @RequestParam Long userId) {

        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    /**
     * 批量标记通知为已读
     */
    @PutMapping("/read-all")
    @Operation(summary = "批量标记通知为已读")
    public Result<Void> markAllAsRead(@RequestParam Long userId) {

        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    /**
     * 创建站内通知（内部接口）
     */
    @PostMapping("/internal")
    @Operation(summary = "创建站内通知（内部接口）")
    public Result<NotificationVO> createInAppNotification(@RequestBody CreateNotificationRequest request) {

        NotificationVO notification = notificationService.createInAppNotification(request);
        return Result.success(notification);
    }

    /**
     * 发送邮件通知（内部接口）
     */
    @PostMapping("/internal/email")
    @Operation(summary = "发送邮件通知（内部接口）")
    public Result<NotificationVO> sendEmailNotification(@RequestBody CreateNotificationRequest request) {

        NotificationVO notification = notificationService.sendEmailNotification(request);
        return Result.success(notification);
    }
}
