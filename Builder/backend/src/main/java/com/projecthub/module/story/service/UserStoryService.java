package com.projecthub.module.story.service;

import com.projecthub.common.exception.BusinessException;
import com.projecthub.common.response.PageResult;
import com.projecthub.common.util.BeanCopyUtil;
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

  /** 创建用户故事 */
  @Transactional
  public UserStoryVO createUserStory(Long projectId, UserStoryVO.CreateRequest request) {
    Long userId = getCurrentUserId();

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
            .acceptanceCriteria(request.getAcceptanceCriteria())
            .priority(
                request.getPriority() != null
                    ? UserStory.Priority.valueOf(request.getPriority())
                    : UserStory.Priority.MEDIUM)
            .storyPoints(request.getStoryPoints())
            .assigneeId(request.getAssigneeId())
            .position(maxPosition + 1)
            .build();

    userStoryRepository.save(userStory);
    log.info("创建用户故事成功：storyId={}, projectId={}", userStory.getId(), projectId);

    return buildUserStoryVO(userStory);
  }

  /** 获取用户故事详情 */
  @Transactional(readOnly = true)
  public UserStoryVO getUserStory(Long storyId) {
    UserStory userStory =
        userStoryRepository.findById(storyId).orElseThrow(() -> new BusinessException("用户故事不存在"));

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

          // 史诗筛选
          if (filter != null && filter.getEpicId() != null) {
            predicates.add(cb.equal(root.get("epicId"), filter.getEpicId()));
          }

          // 状态筛选
          if (filter != null && filter.getStatus() != null) {
            predicates.add(
                cb.equal(root.get("status"), UserStory.TaskStatus.valueOf(filter.getStatus())));
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

          return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };

    Page<UserStory> storyPage = userStoryRepository.findAll(spec, pageable);

    List<UserStoryVO> content =
        storyPage.getContent().stream().map(this::buildUserStoryVO).collect(Collectors.toList());

    return PageResult.of(content, storyPage.getTotalElements(), page, size);
  }

  /** 更新用户故事 */
  @Transactional
  public UserStoryVO updateUserStory(Long storyId, UserStoryVO.UpdateRequest request) {
    Long userId = getCurrentUserId();

    UserStory userStory =
        userStoryRepository.findById(storyId).orElseThrow(() -> new BusinessException("用户故事不存在"));

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
      userStory.setPriority(UserStory.Priority.valueOf(request.getPriority()));
    }
    if (request.getStoryPoints() != null) {
      userStory.setStoryPoints(request.getStoryPoints());
    }
    if (request.getAssigneeId() != null) {
      userStory.setAssigneeId(request.getAssigneeId());
    }
    if (request.getStatus() != null) {
      userStory.setStatus(UserStory.TaskStatus.valueOf(request.getStatus()));
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
        userStoryRepository.findById(storyId).orElseThrow(() -> new BusinessException("用户故事不存在"));

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
