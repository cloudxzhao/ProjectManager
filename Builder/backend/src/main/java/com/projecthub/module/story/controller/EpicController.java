package com.projecthub.module.story.controller;

import com.projecthub.common.response.Result;
import com.projecthub.module.story.dto.EpicVO;
import com.projecthub.module.story.service.EpicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 史诗控制器 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/epics")
@RequiredArgsConstructor
@Tag(name = "史诗管理", description = "史诗相关接口")
public class EpicController {

  private final EpicService epicService;

  /** 创建史诗 */
  @PostMapping
  @Operation(summary = "创建史诗", description = "在项目下创建新史诗")
  public Result<EpicVO> createEpic(
      @PathVariable Long projectId, @Valid @RequestBody EpicVO.CreateRequest request) {
    EpicVO epic = epicService.createEpic(projectId, request);
    return Result.success(epic);
  }

  /** 获取史诗详情 */
  @GetMapping("/{id}")
  @Operation(summary = "获取史诗详情", description = "根据史诗 ID 获取详细信息")
  public Result<EpicVO> getEpic(@PathVariable Long id) {
    EpicVO epic = epicService.getEpic(id);
    return Result.success(epic);
  }

  /** 获取史诗列表 */
  @GetMapping
  @Operation(summary = "获取史诗列表", description = "获取项目下的史诗列表")
  public Result<List<EpicVO>> listEpics(@PathVariable Long projectId) {
    List<EpicVO> epics = epicService.listEpics(projectId);
    return Result.success(epics);
  }

  /** 更新史诗 */
  @PutMapping("/{id}")
  @Operation(summary = "更新史诗", description = "更新史诗信息")
  public Result<EpicVO> updateEpic(
      @PathVariable Long id, @Valid @RequestBody EpicVO.UpdateRequest request) {
    EpicVO epic = epicService.updateEpic(id, request);
    return Result.success(epic);
  }

  /** 删除史诗 */
  @DeleteMapping("/{id}")
  @Operation(summary = "删除史诗", description = "删除指定史诗")
  public Result<Void> deleteEpic(@PathVariable Long id) {
    epicService.deleteEpic(id);
    return Result.success();
  }
}
