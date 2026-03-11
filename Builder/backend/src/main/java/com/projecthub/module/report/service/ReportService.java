package com.projecthub.module.report.service;

import com.projecthub.common.exception.BusinessException;
import com.projecthub.module.project.entity.Project;
import com.projecthub.module.project.repository.ProjectRepository;
import com.projecthub.module.report.dto.BurndownChartVO;
import com.projecthub.module.task.entity.Task;
import com.projecthub.module.task.repository.TaskRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 报表服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

  private final ProjectRepository projectRepository;
  private final TaskRepository taskRepository;

  /** 获取燃尽图数据 */
  @Transactional(readOnly = true)
  public BurndownChartVO getBurndownChart(Long projectId) {
    // 获取项目信息
    Project project =
        projectRepository.findById(projectId).orElseThrow(() -> new BusinessException("项目不存在"));

    // 获取项目下已完成的任务故事点总和
    Integer totalStoryPoints = calculateTotalStoryPoints(projectId);

    // 获取每日数据
    List<BurndownChartVO.DailyData> dailyDataList = calculateDailyData(project, totalStoryPoints);

    return BurndownChartVO.builder()
        .projectId(projectId)
        .projectName(project.getName())
        .startDate(project.getStartDate() != null ? project.getStartDate().toString() : null)
        .endDate(project.getEndDate() != null ? project.getEndDate().toString() : null)
        .totalStoryPoints(totalStoryPoints)
        .dailyDataList(dailyDataList)
        .build();
  }

  /** 计算总故事点 */
  private Integer calculateTotalStoryPoints(Long projectId) {
    List<Task> allTasks = taskRepository.findByProjectId(projectId);
    return allTasks.stream()
        .filter(task -> task.getStoryPoints() != null)
        .mapToInt(Task::getStoryPoints)
        .sum();
  }

  /** 计算每日数据 */
  private List<BurndownChartVO.DailyData> calculateDailyData(
      Project project, Integer totalStoryPoints) {
    List<BurndownChartVO.DailyData> dailyDataList = new ArrayList<>();

    LocalDate startDate = project.getStartDate();
    LocalDate endDate = project.getEndDate();

    if (startDate == null || endDate == null) {
      return dailyDataList;
    }

    long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
    if (totalDays <= 0) {
      return dailyDataList;
    }

    // 遍历每一天
    LocalDate currentDate = startDate;
    double idealRemaining = totalStoryPoints;

    while (!currentDate.isAfter(endDate)) {
      // 计算当天结束后的剩余故事点（已完成的任务）
      Integer remainingPoints = calculateRemainingPoints(project.getId(), currentDate);

      // 计算理想剩余故事点
      long daysPassed = ChronoUnit.DAYS.between(startDate, currentDate);
      double idealDecrement = (double) totalStoryPoints / totalDays;
      idealRemaining = totalStoryPoints - (idealDecrement * daysPassed);

      // 计算当天完成的故事点
      Integer completedPoints = calculateCompletedPoints(project.getId(), currentDate);

      BurndownChartVO.DailyData dailyData =
          BurndownChartVO.DailyData.builder()
              .date(currentDate.toString())
              .remainingPoints(remainingPoints)
              .idealRemainingPoints(Math.round(idealRemaining * 100.0) / 100.0)
              .completedPoints(completedPoints)
              .build();

      dailyDataList.add(dailyData);
      currentDate = currentDate.plusDays(1);
    }

    return dailyDataList;
  }

  /** 计算指定日期结束后的剩余故事点 */
  private Integer calculateRemainingPoints(Long projectId, LocalDate date) {
    List<Task> tasks =
        taskRepository.findByProjectIdAndStatus(projectId, Task.TaskStatus.DONE).stream()
            .filter(task -> task.getStoryPoints() != null)
            .filter(task -> !task.getUpdatedAt().toLocalDate().isAfter(date))
            .toList();

    return tasks.stream().mapToInt(Task::getStoryPoints).sum();
  }

  /** 计算当天完成的故事点 */
  private Integer calculateCompletedPoints(Long projectId, LocalDate date) {
    List<Task> tasks =
        taskRepository.findByProjectIdAndStatus(projectId, Task.TaskStatus.DONE).stream()
            .filter(task -> task.getStoryPoints() != null)
            .filter(task -> task.getUpdatedAt().toLocalDate().equals(date))
            .toList();

    return tasks.stream().mapToInt(Task::getStoryPoints).sum();
  }
}