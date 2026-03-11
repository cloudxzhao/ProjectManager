package com.projecthub.module.report.controller;

import com.projecthub.common.response.Result;
import com.projecthub.module.report.dto.BurndownChartVO;
import com.projecthub.module.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 报表控制器 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/reports")
@RequiredArgsConstructor
@Tag(name = "报表管理", description = "项目报表相关接口")
public class ReportController {

  private final ReportService reportService;

  /** 获取燃尽图数据 */
  @GetMapping("/burndown")
  @Operation(summary = "燃尽图数据", description = "获取项目的燃尽图数据")
  public Result<BurndownChartVO> getBurndownChart(@PathVariable Long projectId) {
    BurndownChartVO burndownChart = reportService.getBurndownChart(projectId);
    return Result.success(burndownChart);
  }
}