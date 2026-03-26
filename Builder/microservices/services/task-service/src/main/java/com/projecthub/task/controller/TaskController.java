package com.projecthub.task.controller;

import com.projecthub.common.api.result.Result;
import com.projecthub.common.security.util.UserContextHolder;
import com.projecthub.task.dto.*;
import com.projecthub.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务控制器
 */
@Tag(name = "任务管理", description = "任务CRUD、看板、评论接口")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * 创建任务
     */
    @Operation(summary = "创建任务")
    @PostMapping
    public Result<TaskVO> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return Result.success("创建成功", taskService.createTask(request));
    }

    /**
     * 更新任务
     */
    @Operation(summary = "更新任务")
    @PutMapping("/{id}")
    public Result<TaskVO> updateTask(@PathVariable("id") Long id, @Valid @RequestBody UpdateTaskRequest request) {
        return Result.success("更新成功", taskService.updateTask(id, request));
    }

    /**
     * 删除任务
     */
    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTask(@PathVariable("id") Long id) {
        taskService.deleteTask(id);
        return Result.success("删除成功", null);
    }

    /**
     * 移动任务
     */
    @Operation(summary = "移动任务（改变状态/排序）")
    @PutMapping("/{id}/move")
    public Result<TaskVO> moveTask(@PathVariable("id") Long id, @Valid @RequestBody MoveTaskRequest request) {
        return Result.success("移动成功", taskService.moveTask(id, request));
    }

    /**
     * 获取任务详情
     */
    @Operation(summary = "获取任务详情")
    @GetMapping("/{id}")
    public Result<TaskVO> getTaskById(@PathVariable("id") Long id) {
        return Result.success(taskService.getTaskById(id));
    }

    /**
     * 获取我的任务
     */
    @Operation(summary = "获取我的任务")
    @GetMapping("/my")
    public Result<List<TaskVO>> getMyTasks() {
        return Result.success(taskService.getMyTasks());
    }

    /**
     * 获取任务评论
     */
    @Operation(summary = "获取任务评论")
    @GetMapping("/{id}/comments")
    public Result<List<CommentVO>> getComments(@PathVariable("id") Long taskId) {
        return Result.success(taskService.getComments(taskId));
    }

    /**
     * 添加评论
     */
    @Operation(summary = "添加评论")
    @PostMapping("/{id}/comments")
    public Result<CommentVO> addComment(
            @PathVariable("id") Long taskId,
            @Valid @RequestBody CreateCommentRequest request) {
        return Result.success("添加成功", taskService.addComment(taskId, request));
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论")
    @DeleteMapping("/{taskId}/comments/{commentId}")
    public Result<Void> deleteComment(
            @PathVariable("taskId") Long taskId,
            @PathVariable("commentId") Long commentId) {
        taskService.deleteComment(taskId, commentId);
        return Result.success("删除成功", null);
    }

}