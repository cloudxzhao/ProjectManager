package com.projecthub.module.wiki.controller;

import com.projecthub.common.response.Result;
import com.projecthub.module.wiki.dto.WikiDetailVO;
import com.projecthub.module.wiki.dto.WikiSearchResultVO;
import com.projecthub.module.wiki.dto.WikiVO;
import com.projecthub.module.wiki.entity.WikiHistory;
import com.projecthub.module.wiki.service.WikiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** Wiki 控制器 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/wiki")
@RequiredArgsConstructor
@Tag(name = "Wiki 文档管理", description = "Wiki 文档相关接口")
public class WikiController {

  private final WikiService wikiService;

  /** 获取文档树 */
  @GetMapping
  @Operation(summary = "获取文档树", description = "获取项目下的文档树形结构")
  public Result<List<WikiVO>> getDocumentTree(@PathVariable Long projectId) {
    List<WikiVO> documents = wikiService.getDocumentTree(projectId);
    return Result.success(documents);
  }

  /** 获取文档详情 */
  @GetMapping("/{id}")
  @Operation(summary = "获取文档详情", description = "获取指定文档的详细信息")
  public Result<WikiDetailVO> getDocument(@PathVariable Long id) {
    WikiDetailVO document = wikiService.getDocument(id);
    return Result.success(document);
  }

  /** 创建文档 */
  @PostMapping
  @Operation(summary = "创建文档", description = "创建新的 Wiki 文档")
  public Result<WikiVO> createDocument(
      @PathVariable Long projectId, @Valid @RequestBody WikiVO.CreateRequest request) {
    WikiVO document = wikiService.createDocument(projectId, request);
    return Result.success(document);
  }

  /** 更新文档 */
  @PutMapping("/{id}")
  @Operation(summary = "更新文档", description = "更新文档内容（自动保存历史版本）")
  public Result<WikiVO> updateDocument(
      @PathVariable Long id, @Valid @RequestBody WikiVO.UpdateRequest request) {
    WikiVO document = wikiService.updateDocument(id, request);
    return Result.success(document);
  }

  /** 删除文档 */
  @DeleteMapping("/{id}")
  @Operation(summary = "删除文档", description = "删除指定的 Wiki 文档")
  public Result<Void> deleteDocument(@PathVariable Long id) {
    wikiService.deleteDocument(id);
    return Result.success();
  }

  /** 获取文档历史记录 */
  @GetMapping("/{id}/history")
  @Operation(summary = "获取历史记录", description = "获取文档的版本历史")
  public Result<List<WikiHistory>> getDocumentHistory(@PathVariable Long id) {
    List<WikiHistory> history = wikiService.getDocumentHistory(id);
    return Result.success(history);
  }

  /** 移动文档 */
  @PutMapping("/{id}/move")
  @Operation(summary = "移动文档", description = "移动文档到新的父文档或调整排序")
  public Result<WikiVO> moveDocument(
      @PathVariable Long projectId,
      @PathVariable Long id,
      @Valid @RequestBody WikiVO.MoveRequest request) {
    WikiVO document = wikiService.moveDocument(id, request);
    return Result.success(document);
  }

  /** 全文搜索 */
  @PostMapping("/search")
  @Operation(summary = "全文搜索", description = "搜索项目下的 Wiki 文档")
  public Result<List<WikiSearchResultVO>> searchDocuments(
      @PathVariable Long projectId,
      @RequestParam String keyword,
      @RequestParam(required = false, defaultValue = "20") Integer limit) {
    List<WikiSearchResultVO> results = wikiService.searchDocuments(projectId, keyword, limit);
    return Result.success(results);
  }

  /** 检查是否有子文档 */
  @GetMapping("/{id}/has-children")
  @Operation(summary = "检查是否有子文档", description = "检查指定文档是否有子文档")
  public Result<Boolean> hasChildren(@PathVariable Long id) {
    Boolean hasChildren = wikiService.hasChildren(id);
    return Result.success(hasChildren);
  }
}
