package com.projecthub.module.wiki.controller;

import com.projecthub.common.response.Result;
import com.projecthub.module.wiki.dto.VersionDiffVO;
import com.projecthub.module.wiki.dto.WikiVersionVO;
import com.projecthub.module.wiki.service.WikiVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** Wiki 版本控制器 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/wiki/{wikiId}/versions")
@RequiredArgsConstructor
@Tag(name = "Wiki 版本管理", description = "Wiki 文档版本相关接口")
public class WikiVersionController {

  private final WikiVersionService wikiVersionService;

  /** 获取版本列表 */
  @GetMapping
  @Operation(summary = "获取版本列表", description = "获取文档的所有版本历史")
  public Result<List<WikiVersionVO>> getVersionList(
      @PathVariable Long projectId, @PathVariable Long wikiId) {
    List<WikiVersionVO> versions = wikiVersionService.getVersionList(wikiId);
    return Result.success(versions);
  }

  /** 版本对比 */
  @GetMapping("/{versionId}/diff/{compareVersionId}")
  @Operation(summary = "版本对比", description = "对比两个版本的差异")
  public Result<VersionDiffVO> diffVersions(
      @PathVariable Long projectId,
      @PathVariable Long wikiId,
      @PathVariable Long versionId,
      @PathVariable Long compareVersionId) {
    VersionDiffVO diff = wikiVersionService.diffVersions(wikiId, versionId, compareVersionId);
    return Result.success(diff);
  }

  /** 版本对比（旧版本ID为null时对比第一个版本） */
  @GetMapping("/diff")
  @Operation(summary = "版本对比", description = "对比两个版本的差异")
  public Result<VersionDiffVO> diffVersionsByParam(
      @PathVariable Long projectId,
      @PathVariable Long wikiId,
      @RequestParam(required = false) Long versionId,
      @RequestParam(required = false) Long compareVersionId) {
    VersionDiffVO diff = wikiVersionService.diffVersions(wikiId, versionId, compareVersionId);
    return Result.success(diff);
  }

  /** 恢复版本 */
  @PostMapping("/{versionId}/restore")
  @Operation(summary = "恢复版本", description = "恢复到指定的历史版本")
  public Result<WikiVersionVO> restoreVersion(
      @PathVariable Long projectId,
      @PathVariable Long wikiId,
      @PathVariable Long versionId,
      @Valid @RequestBody WikiVersionVO.RestoreRequest request) {
    WikiVersionVO result =
        wikiVersionService.restoreVersion(wikiId, versionId, request.getChangeLog());
    return Result.success(result);
  }
}
