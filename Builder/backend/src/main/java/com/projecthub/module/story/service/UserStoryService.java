package com.projecthub.module.story.service;

import com.projecthub.common.constant.TaskStatus;
import com.projecthub.common.exception.BusinessException;
import com.projecthub.common.response.PageResult;
import com.projecthub.common.util.BeanCopyUtil;
import com.projecthub.module.project.repository.ProjectRepository;
import com.projecthub.module.project.service.PermissionService;
import com.projecthub.module.story.dto.UserStoryVO;
import com.projecthub.module.story.entity.UserStory;
import com.projecthub.module.story.repository.UserStoryRepository;
import com.projecthub.module.user.repository.UserRepository;
import com.projecthub.security.UserDetailsImpl;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 用户故事服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserStoryService {

  private final UserStoryRepository userStoryRepository;
  private final UserRepository userRepository;
  private final PermissionService permissionService;
  private final ProjectRepository projectRepository;

  /** 创建用户故事 */
  @Transactional
  public UserStoryVO createUserStory(Long projectId, UserStoryVO.CreateRequest request) {
    Long userId = getCurrentUserId();

    // 检查项目是否存在
    if (!projectRepository.existsById(projectId)) {
      throw new BusinessException(404, 404, "项目不存在");
    }

    // 权限校验
    if (!permissionService.hasPermission(userId, projectId, "STORY_CREATE")) {
      throw new BusinessException(403, "无创建用户故事权限");
    }

    // 获取最大位置
    Integer maxPosition = userStoryRepository.findMaxPosition(projectId);

    // 创建用户故事
    UserStory userStory =
        UserStory.builder()
            .projectId(projectId)
            .epicId(request.getEpicId())
            .title(request.getTitle())
            .description(request.getDescription())
            .status(TaskStatus.TODO)
            .acceptanceCriteria(request.getAcceptanceCriteria())
            .priority(getPriorityFromString(request.getPriority()))
            .storyPoints(request.getStoryPoints())
            .assigneeId(request.getAssigneeId())
            .position(maxPosition + 1)
            .build();

    userStoryRepository.save(userStory);
    log.info("创建用户故事成功：storyId={}, projectId={}", userStory.getId(), projectId);

    return buildUserStoryVO(userStory);
  }

  /** 从字符串获取优先级，处理 null 和空字符串情况 */
  private UserStory.Priority getPriorityFromString(String priority) {
    if (priority == null || priority.trim().isEmpty()) {
      return UserStory.Priority.MEDIUM;
    }
    try {
      return UserStory.Priority.valueOf(priority);
    } catch (IllegalArgumentException e) {
      throw new BusinessException(400, "无效的优先级：" + priority);
    }
  }

  /** 从字符串获取任务状态，处理 null 和空字符串情况 */
  private TaskStatus getTaskStatusFromString(String status) {
    if (status == null || status.trim().isEmpty()) {
      return TaskStatus.TODO;
    }
    try {
      return TaskStatus.valueOf(status);
    } catch (IllegalArgumentException e) {
      throw new BusinessException(400, "无效的状态：" + status);
    }
  }

  /** 获取用户故事详情 */
  @Transactional(readOnly = true)
  public UserStoryVO getUserStory(Long storyId) {
    UserStory userStory =
        userStoryRepository
            .findById(storyId)
            .orElseThrow(() -> new BusinessException(404, 404, "用户故事不存在"));

    return buildUserStoryVO(userStory);
  }

  /** 获取项目下的用户故事列表 */
  @Transactional(readOnly = true)
  public PageResult<UserStoryVO> listUserStories(
      Long projectId, UserStoryVO.FilterRequest filter, Integer page, Integer size) {
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    Specification<UserStory> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();

          // 项目 ID 筛选
          predicates.add(cb.equal(root.get("projectId"), projectId));

          // 添加其他筛选条件
          addFilterPredicates(predicates, filter, cb, root);

          return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };

    Page<UserStory> storyPage = userStoryRepository.findAll(spec, pageable);

    List<UserStoryVO> content =
        storyPage.getContent().stream().map(this::buildUserStoryVO).collect(Collectors.toList());

    return PageResult.of(content, storyPage.getTotalElements(), page, size);
  }

  /** 搜索用户故事列表（支持项目筛选和权限校验） */
  @Transactional(readOnly = true)
  public PageResult<UserStoryVO> searchUserStories(
      UserStoryVO.FilterRequest filter, Integer page, Integer size) {
    Long userId = getCurrentUserId();

    // 获取用户有权限访问的所有项目 ID
    List<Long> userProjectIds = permissionService.getUserProjectIds(userId);

    if (userProjectIds.isEmpty()) {
      return PageResult.of(List.of(), 0L, page, size);
    }

    // 如果请求中指定了项目 ID 筛选，需要校验权限
    List<Long> targetProjectIds;
    if (filter != null && filter.getProjectIds() != null && !filter.getProjectIds().isEmpty()) {
      // 过滤出用户有权限的项目 ID
      targetProjectIds =
          filter.getProjectIds().stream()
              .filter(userProjectIds::contains)
              .collect(Collectors.toList());
      log.info("用户请求查询项目 {}，实际有权限的项目 {}", filter.getProjectIds(), targetProjectIds);
    } else {
      targetProjectIds = userProjectIds;
    }

    if (targetProjectIds.isEmpty()) {
      return PageResult.of(List.of(), 0L, page, size);
    }

    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    Specification<UserStory> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();

          // 项目 ID 筛选 - 用户有权限的项目
          predicates.add(root.get("projectId").in(targetProjectIds));

          // 添加其他筛选条件
          addFilterPredicates(predicates, filter, cb, root);

          return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };

    Page<UserStory> storyPage = userStoryRepository.findAll(spec, pageable);

    List<UserStoryVO> content =
        storyPage.getContent().stream().map(this::buildUserStoryVO).collect(Collectors.toList());

    return PageResult.of(content, storyPage.getTotalElements(), page, size);
  }

  /** 添加筛选条件 */
  private void addFilterPredicates(
      List<Predicate> predicates,
      UserStoryVO.FilterRequest filter,
      jakarta.persistence.criteria.CriteriaBuilder cb,
      jakarta.persistence.criteria.Root<UserStory> root) {
    // 史诗筛选
    if (filter != null && filter.getEpicId() != null) {
      predicates.add(cb.equal(root.get("epicId"), filter.getEpicId()));
    }

    // 状态筛选
    if (filter != null && filter.getStatus() != null) {
      predicates.add(cb.equal(root.get("status"), TaskStatus.valueOf(filter.getStatus())));
    }

    // 优先级筛选
    if (filter != null && filter.getPriority() != null) {
      predicates.add(
          cb.equal(root.get("priority"), UserStory.Priority.valueOf(filter.getPriority())));
    }

    // 负责人筛选
    if (filter != null && filter.getAssigneeId() != null) {
      predicates.add(cb.equal(root.get("assigneeId"), filter.getAssigneeId()));
    }

    // 标题关键字筛选
    if (filter != null && StringUtils.hasText(filter.getKeyword())) {
      predicates.add(cb.like(root.get("title"), "%" + filter.getKeyword() + "%"));
    }
  }

  /** 更新用户故事 */
  @Transactional
  public UserStoryVO updateUserStory(Long storyId, UserStoryVO.UpdateRequest request) {
    Long userId = getCurrentUserId();

    UserStory userStory =
        userStoryRepository
            .findById(storyId)
            .orElseThrow(() -> new BusinessException(404, 404, "用户故事不存在"));

    // 权限校验
    if (!permissionService.hasPermission(userId, userStory.getProjectId(), "STORY_EDIT")) {
      throw new BusinessException(403, "无编辑用户故事权限");
    }

    // 更新字段
    if (request.getEpicId() != null) {
      userStory.setEpicId(request.getEpicId());
    }
    if (request.getTitle() != null) {
      userStory.setTitle(request.getTitle());
    }
    if (request.getDescription() != null) {
      userStory.setDescription(request.getDescription());
    }
    if (request.getAcceptanceCriteria() != null) {
      userStory.setAcceptanceCriteria(request.getAcceptanceCriteria());
    }
    if (request.getPriority() != null) {
      userStory.setPriority(getPriorityFromString(request.getPriority()));
    }
    if (request.getStoryPoints() != null) {
      userStory.setStoryPoints(request.getStoryPoints());
    }
    if (request.getAssigneeId() != null) {
      userStory.setAssigneeId(request.getAssigneeId());
    }
    if (request.getStatus() != null) {
      userStory.setStatus(getTaskStatusFromString(request.getStatus()));
    }

    userStoryRepository.save(userStory);
    log.info("更新用户故事成功：storyId={}", storyId);

    return buildUserStoryVO(userStory);
  }

  /** 删除用户故事 */
  @Transactional
  public void deleteUserStory(Long storyId) {
    Long userId = getCurrentUserId();

    UserStory userStory =
        userStoryRepository
            .findById(storyId)
            .orElseThrow(() -> new BusinessException(404, 404, "用户故事不存在"));

    // 权限校验
    if (!permissionService.hasPermission(userId, userStory.getProjectId(), "STORY_DELETE")) {
      throw new BusinessException(403, "无删除用户故事权限");
    }

    userStoryRepository.delete(userStory);
    log.info("删除用户故事成功：storyId={}", storyId);
  }

  /** 构建用户故事 VO */
  private UserStoryVO buildUserStoryVO(UserStory userStory) {
    UserStoryVO vo = BeanCopyUtil.copyProperties(userStory, UserStoryVO.class);
    // 手动设置枚举字段的字符串表示
    vo.setStatus(userStory.getStatus().name());
    vo.setPriority(userStory.getPriority().name());

    // 查询负责人姓名
    if (userStory.getAssigneeId() != null) {
      userRepository
          .findById(userStory.getAssigneeId())
          .ifPresent(user -> vo.setAssigneeName(user.getUsername()));
    }

    return vo;
  }

  /** 获取当前用户 ID */
  private Long getCurrentUserId() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof UserDetailsImpl) {
      return ((UserDetailsImpl) principal).getId();
    }
    throw new BusinessException("用户未登录");
  }
}
