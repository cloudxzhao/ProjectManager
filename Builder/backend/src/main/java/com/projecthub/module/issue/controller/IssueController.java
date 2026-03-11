package com.projecthub.module.issue.controller;

import com.projecthub.common.response.PageResult;
import com.projecthub.common.response.Result;
import com.projecthub.module.issue.dto.IssueVO;
import com.projecthub.module.issue.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 问题控制器 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/issues")
@RequiredArgsConstructor
@Tag(name = "问题追踪管理", description = "问题相关接口")
public class IssueController {

  private final IssueService issueService;

  /** 创建问题 */
  @PostMapping
  @Operation(summary = "创建问题", description = "在项目下创建新问题")
  public Result<IssueVO> createIssue(
      @PathVariable Long projectId, @Valid @RequestBody IssueVO.CreateRequest request) {
    IssueVO issue = issueService.createIssue(projectId, request);
    return Result.success("问题创建成功", issue);
  }

  /** 获取问题详情 */
  @GetMapping("/{id}")
  @Operation(summary = "获取问题详情", description = "根据问题 ID 获取详细信息")
  public Result<IssueVO> getIssue(@PathVariable Long id) {
    IssueVO issue = issueService.getIssue(id);
    return Result.success(issue);
  }

  /** 获取问题列表 */
  @GetMapping
  @Operation(summary = "获取问题列表", description = "获取项目下的问题列表，支持筛选")
  public Result<PageResult<IssueVO>> listIssues(
      @PathVariable Long projectId,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String severity,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long assigneeId,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "20") Integer size) {

    IssueVO.FilterRequest filter =
        IssueVO.FilterRequest.builder()
            .type(type)
            .severity(severity)
            .status(status)
            .assigneeId(assigneeId)
            .keyword(keyword)
            .build();

    PageResult<IssueVO> result = issueService.listIssues(projectId, filter, page, size);
    return Result.success(result);
  }

  /** 更新问题 */
  @PutMapping("/{id}")
  @Operation(summary = "更新问题", description = "更新问题信息")
  public Result<IssueVO> updateIssue(
      @PathVariable Long id, @Valid @RequestBody IssueVO.UpdateRequest request) {
    IssueVO issue = issueService.updateIssue(id, request);
    return Result.success("问题更新成功", issue);
  }

  /** 删除问题 */
  @DeleteMapping("/{id}")
  @Operation(summary = "删除问题", description = "删除指定问题")
  public Result<Void> deleteIssue(@PathVariable Long id) {
    issueService.deleteIssue(id);
    return Result.success("问题删除成功");
  }
}