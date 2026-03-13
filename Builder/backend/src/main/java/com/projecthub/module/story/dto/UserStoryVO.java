package com.projecthub.module.story.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 用户故事 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStoryVO {

  /** 用户故事 ID */
  private Long id;

  /** 史诗 ID */
  private Long epicId;

  /** 项目 ID */
  private Long projectId;

  /** 标题 */
  private String title;

  /** 描述 */
  private String description;

  /** 验收标准 */
  private String acceptanceCriteria;

  /** 优先级 */
  private String priority;

  /** 故事点 */
  private Integer storyPoints;

  /** 负责人 ID */
  private Long assigneeId;

  /** 负责人姓名 */
  private String assigneeName;

  /** 状态 */
  private String status;

  /** 位置 */
  private Integer position;

  /** 创建时间 */
  private LocalDateTime createdAt;

  /** 更新时间 */
  private LocalDateTime updatedAt;

  /** 创建用户故事请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreateRequest {
    /** 史诗 ID */
    private Long epicId;

    @NotBlank(message = "用户故事标题不能为空")
    @Size(max = 200, message = "用户故事标题最多 200 字符")
    private String title;

    /** 描述 */
    private String description;

    /** 验收标准 */
    private String acceptanceCriteria;

    /** 优先级 */
    private String priority;

    /** 故事点 */
    private Integer storyPoints;

    /** 负责人 ID */
    private Long assigneeId;
  }

  /** 更新用户故事请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UpdateRequest {
    /** 史诗 ID */
    private Long epicId;

    @Size(max = 200, message = "用户故事标题最多 200 字符")
    private String title;

    /** 描述 */
    private String description;

    /** 验收标准 */
    private String acceptanceCriteria;

    /** 优先级 */
    private String priority;

    /** 故事点 */
    private Integer storyPoints;

    /** 负责人 ID */
    private Long assigneeId;

    /** 状态 */
    private String status;
  }

  /** 用户故事筛选请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FilterRequest {
    /** 史诗 ID */
    private Long epicId;

    /** 状态 */
    private String status;

    /** 优先级 */
    private String priority;

    /** 负责人 ID */
    private Long assigneeId;

    /** 关键字 */
    private String keyword;
  }
}
