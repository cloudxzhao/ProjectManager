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

  public TaskCreatedEvent(
      Object source,
      Long taskId,
      Long projectId,
      Long assigneeId,
      String taskTitle,
      String creatorName) {
    super(source);
    this.taskId = taskId;
    this.projectId = projectId;
    this.assigneeId = assigneeId;
    this.taskTitle = taskTitle;
    this.creatorName = creatorName;
  }
}
