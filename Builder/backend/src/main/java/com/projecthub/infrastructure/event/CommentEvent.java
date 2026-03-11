package com.projecthub.infrastructure.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

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
