package com.projecthub.module.task.controller;

import com.projecthub.common.response.Result;
import com.projecthub.module.task.dto.CommentVO;
import com.projecthub.module.task.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 评论控制器 */
@RestController
@RequestMapping("/api/v1/tasks/{taskId}/comments")
@RequiredArgsConstructor
@Tag(name = "任务评论管理", description = "任务评论相关接口")
public class CommentController {

  private final CommentService commentService;

  /** 获取评论列表 */
  @GetMapping
  @Operation(summary = "获取评论列表", description = "获取任务的评论列表")
  public Result<List<CommentVO>> getComments(@PathVariable Long taskId) {
    List<CommentVO> comments = commentService.getComments(taskId);
    return Result.success(comments);
  }

  /** 添加评论 */
  @PostMapping
  @Operation(summary = "添加评论", description = "为任务添加评论或回复")
  public Result<CommentVO> addComment(
      @PathVariable Long taskId, @Valid @RequestBody CommentVO.CreateRequest request) {
    CommentVO comment = commentService.addComment(taskId, request);
    return Result.success("评论添加成功", comment);
  }

  /** 更新评论 */
  @PutMapping("/{id}")
  @Operation(summary = "更新评论", description = "更新评论内容")
  public Result<CommentVO> updateComment(
      @PathVariable Long taskId,
      @PathVariable Long id,
      @Valid @RequestBody CommentVO.UpdateRequest request) {
    CommentVO comment = commentService.updateComment(id, request);
    return Result.success("评论更新成功", comment);
  }

  /** 删除评论 */
  @DeleteMapping("/{id}")
  @Operation(summary = "删除评论", description = "删除指定评论")
  public Result<Void> deleteComment(@PathVariable Long taskId, @PathVariable Long id) {
    commentService.deleteComment(id);
    return Result.success("评论删除成功");
  }
}