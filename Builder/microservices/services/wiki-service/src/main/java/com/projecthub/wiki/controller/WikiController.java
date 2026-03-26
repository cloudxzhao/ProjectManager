package com.projecthub.wiki.controller;

import com.projecthub.common.api.result.Result;
import com.projecthub.wiki.dto.*;
import com.projecthub.wiki.service.WikiService;
import com.projecthub.wiki.util.MarkdownRenderer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wiki")
@RequiredArgsConstructor
@Tag(name = "知识库管理", description = "知识库空间和页面的增删改查接口")
public class WikiController {

    private final WikiService wikiService;
    private final MarkdownRenderer markdownRenderer;

    // Space endpoints

    @PostMapping("/spaces")
    @Operation(summary = "创建知识空间")
    public Result<SpaceVO> createSpace(@Valid @RequestBody CreateSpaceRequest request) {
        return Result.success(wikiService.createSpace(request));
    }

    @GetMapping("/spaces/{id}")
    @Operation(summary = "获取知识空间详情")
    public Result<SpaceVO> getSpaceById(@PathVariable Long id) {
        return Result.success(wikiService.getSpaceById(id));
    }

    @GetMapping("/spaces/project/{projectId}")
    @Operation(summary = "获取项目的知识空间列表")
    public Result<List<SpaceVO>> getSpacesByProjectId(@PathVariable Long projectId) {
        return Result.success(wikiService.getSpacesByProjectId(projectId));
    }

    @PutMapping("/spaces/{id}")
    @Operation(summary = "更新知识空间")
    public Result<SpaceVO> updateSpace(@PathVariable Long id, @RequestBody CreateSpaceRequest request) {
        return Result.success(wikiService.updateSpace(id, request));
    }

    @DeleteMapping("/spaces/{id}")
    @Operation(summary = "删除知识空间")
    public Result<Void> deleteSpace(@PathVariable Long id) {
        wikiService.deleteSpace(id);
        return Result.success();
    }

    // Page endpoints

    @PostMapping("/pages")
    @Operation(summary = "创建知识页面")
    public Result<PageVO> createPage(@Valid @RequestBody CreatePageRequest request) {
        return Result.success(wikiService.createPage(request));
    }

    @GetMapping("/pages/{id}")
    @Operation(summary = "获取知识页面详情")
    public Result<PageVO> getPageById(@PathVariable Long id) {
        return Result.success(wikiService.getPageById(id));
    }

    @GetMapping("/pages/space/{spaceId}")
    @Operation(summary = "获取空间的所有页面")
    public Result<List<PageVO>> getPagesBySpaceId(@PathVariable Long spaceId) {
        return Result.success(wikiService.getPagesBySpaceId(spaceId));
    }

    @GetMapping("/pages/parent/{parentId}")
    @Operation(summary = "获取子页面列表")
    public Result<List<PageVO>> getPagesByParentId(@PathVariable Long parentId) {
        return Result.success(wikiService.getPagesByParentId(parentId));
    }

    @PutMapping("/pages/{id}")
    @Operation(summary = "更新知识页面")
    public Result<PageVO> updatePage(@PathVariable Long id, @RequestBody UpdatePageRequest request) {
        return Result.success(wikiService.updatePage(id, request));
    }

    @GetMapping("/pages/{id}/history")
    @Operation(summary = "获取页面版本历史")
    public Result<List<PageHistoryVO>> getPageHistory(@PathVariable Long id) {
        return Result.success(wikiService.getPageHistory(id));
    }

    @GetMapping("/pages/{id}/history/{version}")
    @Operation(summary = "获取页面指定版本")
    public Result<PageHistoryVO> getPageHistoryByVersion(@PathVariable Long id, @PathVariable Integer version) {
        return Result.success(wikiService.getPageHistoryByVersion(id, version));
    }

    @PostMapping("/pages/{id}/rollback/{version}")
    @Operation(summary = "回滚到指定版本")
    public Result<PageVO> rollbackToVersion(@PathVariable Long id, @PathVariable Integer version) {
        return Result.success(wikiService.rollbackToVersion(id, version));
    }

    @DeleteMapping("/pages/{id}")
    @Operation(summary = "删除知识页面")
    public Result<Void> deletePage(@PathVariable Long id) {
        wikiService.deletePage(id);
        return Result.success();
    }

    @PostMapping("/render-markdown")
    @Operation(summary = "Markdown 渲染")
    public Result<Map<String, String>> renderMarkdown(@RequestBody Map<String, String> request) {
        String markdown = request.get("markdown");
        String html = markdownRenderer.render(markdown);
        return Result.success(Map.of("html", html));
    }
}