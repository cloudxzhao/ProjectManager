package com.projecthub.module.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** 项目 VO */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
public class ProjectVO {

  /** 项目 ID */
  @JsonFormat(shape = JsonFormat.Shape.STRING) // 将数字格式化为字符串
  private Long id;

  /** 项目名称 */
  private String name;

  /** 项目描述 */
  private String description;

  /** 开始日期 */
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate startDate;

  /** 结束日期 */
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate endDate;

  /** 项目所有者 ID */
  private Long ownerId;

  /** 项目状态 */
  private String status;

  /** 项目图标 */
  private String icon;

  /** 主题颜色 */
  private String themeColor;

  /** 项目成员数 */
  private Integer memberCount;

  /** 任务总数 */
  private Integer taskCount;

  /** 已完成任务数 */
  private Integer completedTaskCount;

  /** 创建时间 */
  private LocalDateTime createdAt;

  /** 创建项目请求 */
  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Setter
  @Builder(toBuilder = true)
  public static class CreateRequest {
    @NotBlank(message = "项目名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    private String icon;

    @JsonProperty("color")
    private String themeColor;
  }

  /** 更新项目请求 */
  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Setter
  @Builder(toBuilder = true)
  public static class UpdateRequest {
    private String name;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;

    private String icon;

    @JsonProperty("color")
    private String themeColor;
  }
}
