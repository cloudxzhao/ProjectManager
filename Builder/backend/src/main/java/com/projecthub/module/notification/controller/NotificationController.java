package com.projecthub.module.notification.controller;

import com.projecthub.common.response.PageResult;
import com.projecthub.common.response.Result;
import com.projecthub.module.notification.dto.NotificationVO;
import com.projecthub.module.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 通知控制器 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "通知相关接口")
public class NotificationController {

  private final NotificationService notificationService;

  /** 获取通知列表 */
  @GetMapping
  @Operation(summary = "获取通知列表", description = "获取当前用户的通知列表")
  public Result<PageResult<NotificationVO>> getNotifications(
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "20") Integer size) {
    PageResult<NotificationVO> result = notificationService.getNotifications(page, size);
    return Result.success(result);
  }

  /** 获取未读通知数量 */
  @GetMapping("/unread-count")
  @Operation(summary = "获取未读通知数量", description = "获取当前用户未读通知的数量")
  public Result<Long> getUnreadCount() {
    Long count = notificationService.getUnreadCount();
    return Result.success(count);
  }

  /** 标记通知为已读 */
  @PostMapping("/{id}/read")
  @Operation(summary = "标记已读", description = "将指定通知标记为已读")
  public Result<Void> markAsRead(@PathVariable Long id) {
    notificationService.markAsRead(id);
    return Result.success();
  }

  /** 标记所有通知为已读 */
  @PostMapping("/read-all")
  @Operation(summary = "全部已读", description = "将所有通知标记为已读")
  public Result<Void> markAllAsRead() {
    notificationService.markAllAsRead();
    return Result.success();
  }
}
