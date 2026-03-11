package com.projecthub.module.issue.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 问题 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueVO {

  /** 问题 ID */
  private Long id;

  /** 项目 ID */
  private Long projectId;

  /** 标题 */
  private String title;

  /** 描述 */
  private String description;

  /** 问题类型 */
  private String type;

  /** 严重程度 */
  private String severity;

  /** 状态 */
  private String status;

  /** 负责人 ID */
  private Long assigneeId;

  /** 负责人姓名 */
  private String assigneeName;

  /** 报告人 ID */
  private Long reporterId;

  /** 报告人姓名 */
  private String reporterName;

  /** 发现日期 */
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate foundDate;

  /** 解决日期 */
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate resolvedDate;

  /** 创建时间 */
  private LocalDateTime createdAt;

  /** 更新时间 */
  private LocalDateTime updatedAt;

  /** 创建问题请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreateRequest {
    /** 标题 */
    private String title;

    /** 描述 */
    private String description;

    /** 问题类型 */
    private String type;

    /** 严重程度 */
    private String severity;

    /** 负责人 ID */
    private Long assigneeId;

    /** 发现日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate foundDate;
  }

  /** 更新问题请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UpdateRequest {
    /** 标题 */
    private String title;

    /** 描述 */
    private String description;

    /** 问题类型 */
    private String type;

    /** 严重程度 */
    private String severity;

    /** 状态 */
    private String status;

    /** 负责人 ID */
    private Long assigneeId;

    /** 发现日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate foundDate;

    /** 解决日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate resolvedDate;
  }

  /** 问题筛选请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FilterRequest {
    /** 问题类型 */
    private String type;

    /** 严重程度 */
    private String severity;

    /** 状态 */
    private String status;

    /** 负责人 ID */
    private Long assigneeId;

    /** 关键字 */
    private String keyword;
  }
}
