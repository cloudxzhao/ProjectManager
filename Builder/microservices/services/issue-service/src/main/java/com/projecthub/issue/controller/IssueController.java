package com.projecthub.issue.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.projecthub.common.api.result.Result;
import com.projecthub.issue.dto.*;
import com.projecthub.issue.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/issues")
@RequiredArgsConstructor
@Tag(name = "问题管理", description = "问题跟踪的增删改查接口")
public class IssueController {

    private final IssueService issueService;

    @PostMapping
    @Operation(summary = "创建问题")
    public Result<IssueVO> createIssue(@Valid @RequestBody CreateIssueRequest request) {
        return Result.success(issueService.createIssue(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取问题详情")
    public Result<IssueVO> getIssueById(@PathVariable Long id) {
        return Result.success(issueService.getIssueById(id));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "获取项目的问题列表")
    public Result<List<IssueVO>> getIssuesByProjectId(@PathVariable Long projectId) {
        return Result.success(issueService.getIssuesByProjectId(projectId));
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "获取任务的问题列表")
    public Result<List<IssueVO>> getIssuesByTaskId(@PathVariable Long taskId) {
        return Result.success(issueService.getIssuesByTaskId(taskId));
    }

    @GetMapping("/project/{projectId}/page")
    @Operation(summary = "分页获取问题列表")
    public Result<com.projecthub.common.api.result.PageResult<IssueVO>> getIssuesPage(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<IssueVO> pageResult = issueService.getIssuesPage(projectId, pageNum, pageSize);
        com.projecthub.common.api.result.PageResult<IssueVO> result = new com.projecthub.common.api.result.PageResult<>(
                pageResult.getCurrent(),
                pageResult.getSize(),
                pageResult.getTotal(),
                pageResult.getRecords()
        );
        return Result.success(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新问题")
    public Result<IssueVO> updateIssue(@PathVariable Long id, @RequestBody CreateIssueRequest request) {
        return Result.success(issueService.updateIssue(id, request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新问题状态")
    public Result<IssueVO> updateIssueStatus(@PathVariable Long id, @RequestParam String status) {
        return Result.success(issueService.updateIssueStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除问题")
    public Result<Void> deleteIssue(@PathVariable Long id) {
        issueService.deleteIssue(id);
        return Result.success();
    }
}