package com.projecthub.infrastructure.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

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
