package com.projecthub.infrastructure.event;

import com.projecthub.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/** 事件监听器 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventListenerHandler {

  private final NotificationService notificationService;

  /** 处理任务创建事件 */
  @Async
  @EventListener
  public void handleTaskCreatedEvent(TaskCreatedEvent event) {
    log.info("处理任务创建事件：taskId={}", event.getTaskId());

    // 如果有负责人，发送通知
    if (event.getAssigneeId() != null) {
      String title = "新任务分配";
      String content = String.format("您有一个新任务：%s（创建者：%s）",
          event.getTaskTitle(), event.getCreatorName());
      notificationService.createNotification(
          event.getAssigneeId(), title, content, "TASK_CREATED", event.getTaskId(), "TASK");
    }
  }

  /** 处理任务分配事件 */
  @Async
  @EventListener
  public void handleTaskAssignedEvent(TaskAssignedEvent event) {
    log.info("处理任务分配事件：taskId={}", event.getTaskId());

    String title = "任务分配通知";
    String content = String.format("您被分配了任务：%s", event.getTaskTitle());
    notificationService.createNotification(
        event.getAssigneeId(), title, content, "TASK_ASSIGNED", event.getTaskId(), "TASK");
  }

  /** 处理评论事件 */
  @Async
  @EventListener
  public void handleCommentEvent(CommentEvent event) {
    log.info("处理评论事件：taskId={}", event.getTaskId());

    String title = "新评论通知";
    String content = String.format("%s 评论了任务：%s",
        event.getCommentatorName(),
        event.getCommentContent().length() > 50
            ? event.getCommentContent().substring(0, 50) + "..."
            : event.getCommentContent());

    notificationService.createNotification(
        event.getReceiverId(), title, content, "COMMENT", event.getTaskId(), "TASK");
  }
}