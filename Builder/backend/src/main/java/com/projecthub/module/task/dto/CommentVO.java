package com.projecthub.module.task.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 评论 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO {

  /** 评论 ID */
  private Long id;

  /** 任务 ID */
  private Long taskId;

  /** 用户 ID */
  private Long userId;

  /** 用户名 */
  private String username;

  /** 评论内容 */
  private String content;

  /** 父评论 ID（用于回复） */
  private Long parentId;

  /** 创建时间 */
  private LocalDateTime createdAt;

  /** 更新时间 */
  private LocalDateTime updatedAt;

  /** 创建评论请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreateRequest {
    /** 评论内容 */
    private String content;

    /** 父评论 ID（用于回复） */
    private Long parentId;
  }

  /** 更新评论请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UpdateRequest {
    /** 评论内容 */
    private String content;
  }
}
