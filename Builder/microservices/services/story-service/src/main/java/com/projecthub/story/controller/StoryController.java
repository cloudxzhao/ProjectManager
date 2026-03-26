package com.projecthub.story.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.projecthub.common.api.result.Result;
import com.projecthub.story.dto.*;
import com.projecthub.story.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stories")
@RequiredArgsConstructor
@Tag(name = "用户故事管理", description = "用户故事的增删改查接口")
public class StoryController {

    private final StoryService storyService;

    @PostMapping
    @Operation(summary = "创建用户故事")
    public Result<StoryVO> createStory(@Valid @RequestBody CreateStoryRequest request) {
        return Result.success(storyService.createStory(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户故事详情")
    public Result<StoryVO> getStoryById(@PathVariable Long id) {
        return Result.success(storyService.getStoryById(id));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "获取项目的用户故事列表")
    public Result<List<StoryVO>> getStoriesByProjectId(@PathVariable Long projectId) {
        return Result.success(storyService.getStoriesByProjectId(projectId));
    }

    @GetMapping("/epic/{epicId}")
    @Operation(summary = "获取史诗下的用户故事列表")
    public Result<List<StoryVO>> getStoriesByEpicId(@PathVariable Long epicId) {
        return Result.success(storyService.getStoriesByEpicId(epicId));
    }

    @GetMapping("/project/{projectId}/page")
    @Operation(summary = "分页获取用户故事")
    public Result<com.projecthub.common.api.result.PageResult<StoryVO>> getStoriesPage(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<StoryVO> pageResult = storyService.getStoriesPage(projectId, pageNum, pageSize);
        com.projecthub.common.api.result.PageResult<StoryVO> result = new com.projecthub.common.api.result.PageResult<>(
                pageResult.getCurrent(),
                pageResult.getSize(),
                pageResult.getTotal(),
                pageResult.getRecords()
        );
        return Result.success(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户故事")
    public Result<StoryVO> updateStory(@PathVariable Long id, @RequestBody CreateStoryRequest request) {
        return Result.success(storyService.updateStory(id, request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户故事状态")
    public Result<StoryVO> updateStoryStatus(@PathVariable Long id, @RequestParam String status) {
        return Result.success(storyService.updateStoryStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户故事")
    public Result<Void> deleteStory(@PathVariable Long id) {
        storyService.deleteStory(id);
        return Result.success();
    }

    // Epic endpoints

    @PostMapping("/epics")
    @Operation(summary = "创建史诗")
    public Result<EpicVO> createEpic(@Valid @RequestBody CreateEpicRequest request) {
        return Result.success(storyService.createEpic(request));
    }

    @GetMapping("/epics/{id}")
    @Operation(summary = "获取史诗详情")
    public Result<EpicVO> getEpicById(@PathVariable Long id) {
        return Result.success(storyService.getEpicById(id));
    }

    @GetMapping("/epics/project/{projectId}")
    @Operation(summary = "获取项目的史诗列表")
    public Result<List<EpicVO>> getEpicsByProjectId(@PathVariable Long projectId) {
        return Result.success(storyService.getEpicsByProjectId(projectId));
    }

    @PutMapping("/epics/{id}")
    @Operation(summary = "更新史诗")
    public Result<EpicVO> updateEpic(@PathVariable Long id, @RequestBody CreateEpicRequest request) {
        return Result.success(storyService.updateEpic(id, request));
    }

    @DeleteMapping("/epics/{id}")
    @Operation(summary = "删除史诗")
    public Result<Void> deleteEpic(@PathVariable Long id) {
        storyService.deleteEpic(id);
        return Result.success();
    }

    // Story Points Statistics endpoint

    @GetMapping("/project/{projectId}/story-points-stats")
    @Operation(summary = "获取项目故事点统计")
    public Result<StoryPointsStatsVO> getStoryPointsStats(@PathVariable Long projectId) {
        return Result.success(storyService.getStoryPointsStats(projectId));
    }
}