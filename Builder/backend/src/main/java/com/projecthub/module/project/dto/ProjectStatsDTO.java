package com.projecthub.module.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 项目统计信息 DTO */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectStatsDTO {
  /** 进行中的项目数量 */
  private Long activeCount;

  /** 已完成的项目数量 */
  private Long completedCount;

  /** 已归档的项目数量 */
  private Long archivedCount;

  /** 规划中的项目数量 */
  private Long planningCount;
}
