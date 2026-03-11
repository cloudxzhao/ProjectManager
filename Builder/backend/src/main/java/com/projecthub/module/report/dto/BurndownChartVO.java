package com.projecthub.module.report.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 燃尽图数据 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BurndownChartVO {

  /** 项目 ID */
  private Long projectId;

  /** 项目名称 */
  private String projectName;

  /** 开始日期 */
  private String startDate;

  /** 结束日期 */
  private String endDate;

  /** 总故事点 */
  private Integer totalStoryPoints;

  /** 每日数据列表 */
  private List<DailyData> dailyDataList;

  /** 每日数据 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DailyData {
    /** 日期 */
    private String date;

    /** 剩余故事点 */
    private Integer remainingPoints;

    /** 理想剩余故事点 */
    private Double idealRemainingPoints;

    /** 当日完成故事点 */
    private Integer completedPoints;
  }
}
