package com.projecthub.infrastructure.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/** 任务创建事件 */
@Getter
public class TaskCreatedEvent extends ApplicationEvent {

  private final Long taskId;
  private final Long projectId;
  private final Long assigneeId;
  private final String taskTitle;
  private final String creatorName;

  public TaskCreatedEvent(Object source, Long taskId, Long projectId, Long assigneeId,
      String taskTitle, String creatorName) {
    super(source);
    this.taskId = taskId;
    this.projectId = projectId;
    this.assigneeId = assigneeId;
    this.taskTitle = taskTitle;
    this.creatorName = creatorName;
  }
}

/** 任务分配事件 */
@Getter
public class TaskAssignedEvent extends ApplicationEvent {

  private final Long taskId;
  private final Long projectId;
  private final Long assigneeId;
  private final String taskTitle;

  public TaskAssignedEvent(Object source, Long taskId, Long projectId, Long assigneeId,
      String taskTitle) {
    super(source);
    this.taskId = taskId;
    this.projectId = projectId;
    this.assigneeId = assigneeId;
    this.taskTitle = taskTitle;
  }
}

/** 评论通知事件 */
@Getter
public class CommentEvent extends ApplicationEvent {

  private final Long taskId;
  private final Long projectId;
  private final Long receiverId;
  private final String commentContent;
  private final String commentatorName;

  public CommentEvent(Object source, Long taskId, Long projectId, Long receiverId,
      String commentContent, String commentatorName) {
    super(source);
    this.taskId = taskId;
    this.projectId = projectId;
    this.receiverId = receiverId;
    this.commentContent = commentContent;
    this.commentatorName = commentatorName;
  }
}