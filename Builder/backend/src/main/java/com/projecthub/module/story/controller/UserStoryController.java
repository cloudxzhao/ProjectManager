package com.projecthub.module.story.controller;

import com.projecthub.common.response.PageResult;
import com.projecthub.common.response.Result;
import com.projecthub.module.story.dto.UserStoryVO;
import com.projecthub.module.story.service.UserStoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 用户故事控制器 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/stories")
@RequiredArgsConstructor
@Tag(name = "用户故事管理", description = "用户故事相关接口")
public class UserStoryController {

  private final UserStoryService userStoryService;

  /** 创建用户故事 */
  @PostMapping
  @Operation(summary = "创建用户故事", description = "在项目下创建新用户故事")
  public Result<UserStoryVO> createUserStory(
      @PathVariable Long projectId, @Valid @RequestBody UserStoryVO.CreateRequest request) {
    UserStoryVO story = userStoryService.createUserStory(projectId, request);
    return Result.success(story);
  }

  /** 获取用户故事详情 */
  @GetMapping("/{id}")
  @Operation(summary = "获取用户故事详情", description = "根据用户故事 ID 获取详细信息")
  public Result<UserStoryVO> getUserStory(@PathVariable Long id) {
    UserStoryVO story = userStoryService.getUserStory(id);
    return Result.success(story);
  }

  /** 获取用户故事列表 */
  @GetMapping
  @Operation(summary = "获取用户故事列表", description = "获取项目下的用户故事列表，支持筛选")
  public Result<PageResult<UserStoryVO>> listUserStories(
      @PathVariable Long projectId,
      @RequestParam(required = false) Long epicId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String priority,
      @RequestParam(required = false) Long assigneeId,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "20") Integer size) {

    UserStoryVO.FilterRequest filter =
        UserStoryVO.FilterRequest.builder()
            .epicId(epicId)
            .status(status)
            .priority(priority)
            .assigneeId(assigneeId)
            .keyword(keyword)
            .build();

    PageResult<UserStoryVO> result =
        userStoryService.listUserStories(projectId, filter, page, size);
    return Result.success(result);
  }

  /** 更新用户故事 */
  @PutMapping("/{id}")
  @Operation(summary = "更新用户故事", description = "更新用户故事信息")
  public Result<UserStoryVO> updateUserStory(
      @PathVariable Long id, @Valid @RequestBody UserStoryVO.UpdateRequest request) {
    UserStoryVO story = userStoryService.updateUserStory(id, request);
    return Result.success(story);
  }

  /** 删除用户故事 */
  @DeleteMapping("/{id}")
  @Operation(summary = "删除用户故事", description = "删除指定用户故事")
  public Result<Void> deleteUserStory(@PathVariable Long id) {
    userStoryService.deleteUserStory(id);
    return Result.success();
  }
}
