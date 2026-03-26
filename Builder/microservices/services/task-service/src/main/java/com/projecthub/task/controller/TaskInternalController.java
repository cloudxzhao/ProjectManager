package com.projecthub.task.controller;

import com.projecthub.common.api.result.Result;
import com.projecthub.task.dto.TaskVO;
import com.projecthub.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务内部接口控制器
 */
@RestController
@RequestMapping("/internal/tasks")
@RequiredArgsConstructor
public class TaskInternalController {

    private final TaskService taskService;

    /**
     * 根据ID获取任务
     */
    @GetMapping("/{id}")
    public Result<TaskVO> getTaskById(@PathVariable Long id) {
        return Result.success(taskService.getTaskById(id));
    }

    /**
     * 获取项目的任务列表
     */
    @GetMapping("/project/{projectId}")
    public Result<List<TaskVO>> getTasksByProjectId(@PathVariable Long projectId) {
        return Result.success(taskService.getTasksByProjectId(projectId));
    }

    /**
     * 获取看板数据
     */
    @GetMapping("/project/{projectId}/kanban")
    public Result<Map<String, List<TaskVO>>> getKanbanData(@PathVariable Long projectId) {
        return Result.success(taskService.getKanbanData(projectId));
    }

}