package com.projecthub.module.task.controller;

import com.projecthub.common.response.PageResult;
import com.projecthub.common.response.Result;
import com.projecthub.module.task.dto.TaskVO;
import com.projecthub.module.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 任务控制器 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
@RequiredArgsConstructor
@Tag(name = "任务管理", description = "任务相关接口")
public class TaskController {

  private final TaskService taskService;

  /** 创建任务 */
  @PostMapping
  @Operation(summary = "创建任务", description = "在项目下创建新任务")
  public Result<TaskVO> createTask(
      @PathVariable Long projectId, @Valid @RequestBody TaskVO.CreateRequest request) {
    TaskVO task = taskService.createTask(projectId, request);
    return Result.success(task);
  }

  /** 获取任务详情 */
  @GetMapping("/{id}")
  @Operation(summary = "获取任务详情", description = "根据任务 ID 获取详细信息")
  public Result<TaskVO> getTask(@PathVariable Long id) {
    TaskVO task = taskService.getTask(id);
    return Result.success(task);
  }

  /** 更新任务 */
  @PutMapping("/{id}")
  @Operation(summary = "更新任务", description = "更新任务信息")
  public Result<TaskVO> updateTask(
      @PathVariable Long id, @Valid @RequestBody TaskVO.UpdateRequest request) {
    TaskVO task = taskService.updateTask(id, request);
    return Result.success(task);
  }

  /** 删除任务 */
  @DeleteMapping("/{id}")
  @Operation(summary = "删除任务", description = "删除指定任务")
  public Result<Void> deleteTask(@PathVariable Long id) {
    taskService.deleteTask(id);
    return Result.success();
  }

  /** 移动任务（状态变更） */
  @PostMapping("/{id}/move")
  @Operation(summary = "移动任务", description = "移动任务状态或位置")
  public Result<TaskVO> moveTask(
      @PathVariable Long id, @Valid @RequestBody TaskVO.MoveRequest request) {
    TaskVO task = taskService.moveTask(id, request);
    return Result.success(task);
  }

  /** 获取任务列表 */
  @GetMapping
  @Operation(summary = "获取任务列表", description = "获取项目下的任务列表，支持筛选")
  public Result<PageResult<TaskVO>> listTasks(
      @PathVariable Long projectId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String priority,
      @RequestParam(required = false) Long assigneeId,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "20") Integer size) {

    TaskVO.FilterRequest filter =
        TaskVO.FilterRequest.builder()
            .status(status)
            .priority(priority)
            .assigneeId(assigneeId)
            .keyword(keyword)
            .build();

    PageResult<TaskVO> result = taskService.listTasks(projectId, filter, page, size);
    return Result.success(result);
  }

  /** 获取子任务列表 */
  @GetMapping("/{id}/subtasks")
  @Operation(summary = "获取子任务列表", description = "获取指定任务的子任务列表")
  public Result<List<TaskVO>> getSubTasks(@PathVariable Long id) {
    List<TaskVO> subTasks = taskService.getSubTasks(id);
    return Result.success(subTasks);
  }

  /** 切换子任务完成状态 */
  @PostMapping("/{id}/toggle-complete")
  @Operation(summary = "切换任务完成状态", description = "切换任务的完成状态（完成/未完成）")
  public Result<TaskVO> toggleSubTaskComplete(@PathVariable Long id) {
    TaskVO task = taskService.toggleSubTaskComplete(id);
    return Result.success(task);
  }

  /** 添加子任务 */
  @PostMapping("/{id}/subtasks")
  @Operation(summary = "添加子任务", description = "为任务添加子任务")
  public Result<TaskVO> addSubTask(
      @PathVariable Long projectId,
      @PathVariable Long id,
      @Valid @RequestBody TaskVO.CreateRequest request) {
    // 设置父任务 ID
    request.setParentId(id);
    TaskVO task = taskService.createTask(projectId, request);
    return Result.success(task);
  }
}
