package com.projecthub.module.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.*;

/** 创建项目请求 DTO */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
public class CreateProjectRequest {

  @NotBlank(message = "项目名称不能为空")
  private String name;

  private String description;

  @NotNull(message = "开始日期不能为空")
  private LocalDate startDate;

  @NotNull(message = "结束日期不能为空")
  private LocalDate endDate;

  private String status;

  private String icon;

  private String themeColor;
}
