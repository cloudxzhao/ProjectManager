package com.projecthub.module.task.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 任务 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskVO {

  /** 任务 ID */
  private Long id;

  /** 项目 ID */
  private Long projectId;

  /** 任务标题 */
  private String title;

  /** 任务描述 */
  private String description;

  /** 任务状态 */
  private String status;

  /** 优先级 */
  private String priority;

  /** 负责人 ID */
  private Long assigneeId;

  /** 负责人姓名 */
  private String assigneeName;

  /** 创建者 ID */
  @JsonProperty("reporterId")
  private Long creatorId;

  /** 父任务 ID */
  private Long parentId;

  /** 截止日期 */
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate dueDate;

  /** 故事点 */
  private Integer storyPoints;

  /** 位置 */
  @JsonProperty("order")
  private Integer position;

  /** 子任务总数 */
  private Integer subtaskCount;

  /** 已完成子任务数 */
  private Integer completedSubtaskCount;

  /** 评论总数 */
  private Integer commentCount;

  /** 创建时间 */
  private LocalDateTime createdAt;

  /** 更新时间 */
  private LocalDateTime updatedAt;

  /** 创建任务请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreateRequest {
    @NotBlank(message = "任务标题不能为空")
    private String title;

    private String description;

    private String status;

    private String priority;

    private Long assigneeId;

    private Long parentId;

    private LocalDate dueDate;

    private Integer storyPoints;
  }

  /** 更新任务请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UpdateRequest {
    private String title;

    private String description;

    private String status;

    private String priority;

    private Long assigneeId;

    private LocalDate dueDate;

    private Integer storyPoints;
  }

  /** 移动任务请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MoveRequest {
    /** 目标状态 */
    private String status;

    /** 目标位置 */
    @JsonProperty("order")
    @JsonAlias("newOrder")
    private Integer position;
  }

  /** 任务筛选请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FilterRequest {
    /** 状态筛选 */
    private String status;

    /** 优先级筛选 */
    private String priority;

    /** 负责人 ID 筛选 */
    private Long assigneeId;

    /** 关键字筛选 */
    private String keyword;
  }
}
