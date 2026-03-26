package com.projecthub.issue.controller;

import com.projecthub.common.api.result.Result;
import com.projecthub.issue.dto.IssueVO;
import com.projecthub.issue.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 问题服务内部接口控制器
 */
@RestController
@RequestMapping("/internal/issues")
@RequiredArgsConstructor
public class IssueInternalController {

    private final IssueService issueService;

    /**
     * 根据ID获取问题
     */
    @GetMapping("/{id}")
    public Result<IssueVO> getIssueById(@PathVariable Long id) {
        return Result.success(issueService.getIssueById(id));
    }

    /**
     * 获取项目的问题列表
     */
    @GetMapping("/project/{projectId}")
    public Result<List<IssueVO>> getIssuesByProjectId(@PathVariable Long projectId) {
        return Result.success(issueService.getIssuesByProjectId(projectId));
    }
}