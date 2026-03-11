package com.projecthub.module.notification.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 通知 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationVO {

  /** 通知 ID */
  private Long id;

  /** 用户 ID */
  private Long userId;

  /** 标题 */
  private String title;

  /** 内容 */
  private String content;

  /** 通知类型 */
  private String type;

  /** 关联 ID */
  private Long relatedId;

  /** 关联类型 */
  private String relatedType;

  /** 是否已读 */
  private Boolean isRead;

  /** 创建时间 */
  private LocalDateTime createdAt;
}