package com.projecthub.wiki.controller;

import com.projecthub.common.api.result.Result;
import com.projecthub.wiki.dto.PageVO;
import com.projecthub.wiki.dto.SpaceVO;
import com.projecthub.wiki.service.WikiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库服务内部接口控制器
 */
@RestController
@RequestMapping("/internal/wiki")
@RequiredArgsConstructor
public class WikiInternalController {

    private final WikiService wikiService;

    /**
     * 根据ID获取知识空间
     */
    @GetMapping("/spaces/{id}")
    public Result<SpaceVO> getSpaceById(@PathVariable Long id) {
        return Result.success(wikiService.getSpaceById(id));
    }

    /**
     * 获取项目的知识空间列表
     */
    @GetMapping("/spaces/project/{projectId}")
    public Result<List<SpaceVO>> getSpacesByProjectId(@PathVariable Long projectId) {
        return Result.success(wikiService.getSpacesByProjectId(projectId));
    }
}