package com.projecthub.story.controller;

import com.projecthub.common.api.result.Result;
import com.projecthub.story.dto.StoryVO;
import com.projecthub.story.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 故事服务内部接口控制器
 */
@RestController
@RequestMapping("/internal/stories")
@RequiredArgsConstructor
public class StoryInternalController {

    private final StoryService storyService;

    /**
     * 根据ID获取用户故事
     */
    @GetMapping("/{id}")
    public Result<StoryVO> getStoryById(@PathVariable Long id) {
        return Result.success(storyService.getStoryById(id));
    }

    /**
     * 获取项目的用户故事列表
     */
    @GetMapping("/project/{projectId}")
    public Result<List<StoryVO>> getStoriesByProjectId(@PathVariable Long projectId) {
        return Result.success(storyService.getStoriesByProjectId(projectId));
    }
}