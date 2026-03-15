package com.projecthub.module.task.service;

import com.projecthub.common.constant.ErrorCode;
import com.projecthub.common.constant.TaskStatus;
import com.projecthub.common.exception.BusinessException;
import com.projecthub.common.response.PageResult;
import com.projecthub.common.util.BeanCopyUtil;
import com.projecthub.module.project.service.PermissionService;
import com.projecthub.module.story.repository.EpicRepository;
import com.projecthub.module.story.repository.UserStoryRepository;
import com.projecthub.module.task.dto.TaskVO;
import com.projecthub.module.task.entity.Task;
import com.projecthub.module.task.repository.CommentRepository;
import com.projecthub.module.task.repository.TaskRepository;
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

/** 任务服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

  private final TaskRepository taskRepository;
  private final PermissionService permissionService;
  private final CommentRepository commentRepository;
  private final UserStoryRepository userStoryRepository;
  private final EpicRepository epicRepository;

  /** 创建任务 */
  @Transactional
  public TaskVO createTask(Long projectId, TaskVO.CreateRequest request) {
    Long userId = getCurrentUserId();

    // 权限校验
    if (!permissionService.hasPermission(userId, projectId, "TASK_CREATE")) {
      throw new BusinessException(ErrorCode.TASK_PERMISSION_DENIED, "无创建任务权限");
    }

    // 获取最大位置
    Integer maxPosition = taskRepository.findMaxPosition(projectId);

    // 创建任务
    Task task = new Task();
    task.setProjectId(projectId);
    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setStatus(
        request.getStatus() != null ? TaskStatus.valueOf(request.getStatus()) : TaskStatus.TODO);
    task.setPriority(
        request.getPriority() != null
            ? Task.Priority.valueOf(request.getPriority())
            : Task.Priority.MEDIUM);
    task.setAssigneeId(request.getAssigneeId());
    task.setCreatorId(userId);
    task.setParentId(request.getParentId());
    task.setUserStoryId(request.getUserStoryId());
    task.setEpicId(request.getEpicId());
    task.setDueDate(request.getDueDate());
    task.setStoryPoints(request.getStoryPoints());
    task.setPosition(maxPosition + 1);

    taskRepository.save(task);
    log.info("创建任务成功：taskId={}, projectId={}", task.getId(), projectId);

    TaskVO taskVO = BeanCopyUtil.copyProperties(task, TaskVO.class);
    // 手动设置枚举字段的字符串表示
    taskVO.setStatus(task.getStatus().name());
    taskVO.setPriority(task.getPriority().name());
    populateTaskStats(taskVO);
    // 填充用户故事信息
    populateUserStoryInfo(taskVO);
    // 填充史诗信息
    populateEpicInfo(taskVO);
    return taskVO;
  }

  /** 获取任务详情 */
  @Transactional(readOnly = true)
  public TaskVO getTask(Long taskId) {
    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

    TaskVO taskVO = BeanCopyUtil.copyProperties(task, TaskVO.class);
    // 手动设置枚举字段的字符串表示
    taskVO.setStatus(task.getStatus().name());
    taskVO.setPriority(task.getPriority().name());
    populateTaskStats(taskVO);
    // 填充史诗信息
    populateEpicInfo(taskVO);
    return taskVO;
  }

  /** 更新任务 */
  @Transactional
  public TaskVO updateTask(Long taskId, TaskVO.UpdateRequest request) {
    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

    // 权限校验
    checkTaskPermission(task.getProjectId(), "TASK_EDIT");

    // 更新字段
    if (request.getTitle() != null) {
      task.setTitle(request.getTitle());
    }
    if (request.getDescription() != null) {
      task.setDescription(request.getDescription());
    }
    if (request.getStatus() != null) {
      task.setStatus(TaskStatus.valueOf(request.getStatus()));
    }
    if (request.getPriority() != null) {
      task.setPriority(Task.Priority.valueOf(request.getPriority()));
    }
    if (request.getAssigneeId() != null) {
      task.setAssigneeId(request.getAssigneeId());
    }
    if (request.getUserStoryId() != null) {
      task.setUserStoryId(request.getUserStoryId());
    }
    if (request.getEpicId() != null) {
      task.setEpicId(request.getEpicId());
    }
    if (request.getDueDate() != null) {
      task.setDueDate(request.getDueDate());
    }
    if (request.getStoryPoints() != null) {
      task.setStoryPoints(request.getStoryPoints());
    }

    taskRepository.save(task);
    log.info("更新任务成功：taskId={}", taskId);

    TaskVO taskVO = BeanCopyUtil.copyProperties(task, TaskVO.class);
    // 手动设置枚举字段的字符串表示
    taskVO.setStatus(task.getStatus().name());
    taskVO.setPriority(task.getPriority().name());
    populateTaskStats(taskVO);
    // 填充用户故事信息
    populateUserStoryInfo(taskVO);
    // 填充史诗信息
    populateEpicInfo(taskVO);
    return taskVO;
  }

  /** 删除任务 */
  @Transactional
  public void deleteTask(Long taskId) {
    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

    // 权限校验
    checkTaskPermission(task.getProjectId(), "TASK_DELETE");

    taskRepository.delete(task);
    log.info("删除任务成功：taskId={}", taskId);
  }

  /** 移动任务（状态变更） */
  @Transactional
  public TaskVO moveTask(Long taskId, TaskVO.MoveRequest request) {
    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

    // 权限校验
    checkTaskPermission(task.getProjectId(), "TASK_MOVE");

    // 更新状态
    if (request.getStatus() != null) {
      task.setStatus(TaskStatus.valueOf(request.getStatus()));
    }

    // 更新位置
    if (request.getPosition() != null) {
      task.setPosition(request.getPosition());
    }

    taskRepository.save(task);
    log.info("移动任务成功：taskId={}, status={}", taskId, task.getStatus());

    TaskVO taskVO = BeanCopyUtil.copyProperties(task, TaskVO.class);
    // 手动设置枚举字段的字符串表示
    taskVO.setStatus(task.getStatus().name());
    taskVO.setPriority(task.getPriority().name());
    populateTaskStats(taskVO);
    // 填充史诗信息
    populateEpicInfo(taskVO);
    return taskVO;
  }

  /** 获取任务列表（支持多维度筛选） */
  @Transactional(readOnly = true)
  public PageResult<TaskVO> listTasks(
      Long projectId, TaskVO.FilterRequest filter, Integer page, Integer size) {
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    Specification<Task> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();

          // 项目 ID 筛选
          predicates.add(cb.equal(root.get("projectId"), projectId));

          // 状态筛选
          if (filter != null && filter.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), TaskStatus.valueOf(filter.getStatus())));
          }

          // 优先级筛选
          if (filter != null && filter.getPriority() != null) {
            predicates.add(
                cb.equal(root.get("priority"), Task.Priority.valueOf(filter.getPriority())));
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

    Page<Task> taskPage = taskRepository.findAll(spec, pageable);

    List<TaskVO> content =
        taskPage.getContent().stream()
            .map(
                task -> {
                  TaskVO taskVO = BeanCopyUtil.copyProperties(task, TaskVO.class);
                  // 手动设置枚举字段的字符串表示
                  taskVO.setStatus(task.getStatus().name());
                  taskVO.setPriority(task.getPriority().name());
                  populateTaskStats(taskVO);
                  populateUserStoryInfo(taskVO);
                  populateEpicInfo(taskVO);
                  return taskVO;
                })
            .collect(Collectors.toList());

    return PageResult.of(content, taskPage.getTotalElements(), page, size);
  }

  /** 填充任务统计信息 */
  private void populateTaskStats(TaskVO taskVO) {
    if (taskVO.getId() == null) {
      return;
    }

    // 统计子任务数量
    Long subtaskCount = taskRepository.countByParentId(taskVO.getId());
    taskVO.setSubtaskCount(subtaskCount.intValue());

    // 统计已完成子任务数量
    Long completedSubtaskCount = taskRepository.countCompletedSubTasksByParentId(taskVO.getId());
    taskVO.setCompletedSubtaskCount(completedSubtaskCount.intValue());

    // 统计评论数量
    Long commentCount = commentRepository.countByTaskId(taskVO.getId());
    taskVO.setCommentCount(commentCount.intValue());
  }

  /** 填充用户故事信息 */
  private void populateUserStoryInfo(TaskVO taskVO) {
    if (taskVO.getUserStoryId() == null) {
      return;
    }

    userStoryRepository
        .findById(taskVO.getUserStoryId())
        .ifPresent(
            userStory -> {
              taskVO.setUserStoryTitle(userStory.getTitle());
            });
  }

  /** 填充史诗信息 */
  private void populateEpicInfo(TaskVO taskVO) {
    if (taskVO.getEpicId() == null) {
      return;
    }

    epicRepository
        .findById(taskVO.getEpicId())
        .ifPresent(
            epic -> {
              taskVO.setEpicTitle(epic.getTitle());
            });
  }

  /** 检查任务权限 */
  private void checkTaskPermission(Long projectId, String permissionCode) {
    Long userId = getCurrentUserId();

    if (!permissionService.hasPermission(userId, projectId, permissionCode)) {
      throw new BusinessException(ErrorCode.TASK_PERMISSION_DENIED, "权限不足");
    }
  }

  /** 搜索任务列表（支持项目筛选和权限校验） */
  @Transactional(readOnly = true)
  public PageResult<TaskVO> searchTasks(TaskVO.FilterRequest filter, Integer page, Integer size) {
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

    Specification<Task> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();

          // 项目 ID 筛选 - 用户有权限的项目
          predicates.add(root.get("projectId").in(targetProjectIds));

          // 添加其他筛选条件
          addFilterPredicates(predicates, filter, cb, root);

          return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };

    Page<Task> taskPage = taskRepository.findAll(spec, pageable);

    List<TaskVO> content =
        taskPage.getContent().stream()
            .map(
                task -> {
                  TaskVO taskVO = BeanCopyUtil.copyProperties(task, TaskVO.class);
                  // 手动设置枚举字段的字符串表示
                  taskVO.setStatus(task.getStatus().name());
                  taskVO.setPriority(task.getPriority().name());
                  populateTaskStats(taskVO);
                  populateUserStoryInfo(taskVO);
                  populateEpicInfo(taskVO);
                  return taskVO;
                })
            .collect(Collectors.toList());

    return PageResult.of(content, taskPage.getTotalElements(), page, size);
  }

  /** 添加筛选条件 */
  private void addFilterPredicates(
      List<Predicate> predicates,
      TaskVO.FilterRequest filter,
      jakarta.persistence.criteria.CriteriaBuilder cb,
      jakarta.persistence.criteria.Root<Task> root) {
    // 状态筛选
    if (filter != null && filter.getStatus() != null) {
      predicates.add(cb.equal(root.get("status"), TaskStatus.valueOf(filter.getStatus())));
    }

    // 优先级筛选
    if (filter != null && filter.getPriority() != null) {
      predicates.add(cb.equal(root.get("priority"), Task.Priority.valueOf(filter.getPriority())));
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

  /** 获取当前用户 ID */
  private Long getCurrentUserId() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof UserDetailsImpl) {
      return ((UserDetailsImpl) principal).getId();
    }
    throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
  }

  /** 获取任务的子任务列表 */
  @Transactional(readOnly = true)
  public List<TaskVO> getSubTasks(Long parentId) {
    Task parentTask =
        taskRepository.findById(parentId).orElseThrow(() -> new BusinessException("父任务不存在"));

    // 权限校验
    checkTaskPermission(parentTask.getProjectId(), "TASK_VIEW");

    List<Task> subTasks = taskRepository.findByParentId(parentId);
    return subTasks.stream()
        .map(
            task -> {
              TaskVO taskVO = BeanCopyUtil.copyProperties(task, TaskVO.class);
              // 手动设置枚举字段的字符串表示
              taskVO.setStatus(task.getStatus().name());
              taskVO.setPriority(task.getPriority().name());
              populateTaskStats(taskVO);
              populateUserStoryInfo(taskVO);
              populateEpicInfo(taskVO);
              return taskVO;
            })
        .collect(Collectors.toList());
  }

  /** 切换任务完成状态（主任务和子任务通用） */
  @Transactional
  public TaskVO toggleSubTaskComplete(Long taskId) {
    Task task = taskRepository.findById(taskId).orElseThrow(() -> new BusinessException("任务不存在"));

    // 权限校验
    checkTaskPermission(task.getProjectId(), "TASK_EDIT");

    // 切换状态：如果当前是 DONE，则改为 TODO，否则改为 DONE
    if (task.getStatus() == TaskStatus.DONE) {
      task.setStatus(TaskStatus.TODO);
    } else {
      task.setStatus(TaskStatus.DONE);
    }

    taskRepository.save(task);
    log.info("切换任务完成状态：taskId={}, status={}", taskId, task.getStatus());

    TaskVO taskVO = BeanCopyUtil.copyProperties(task, TaskVO.class);
    // 手动设置枚举字段的字符串表示
    taskVO.setStatus(task.getStatus().name());
    taskVO.setPriority(task.getPriority().name());
    populateTaskStats(taskVO);
    populateUserStoryInfo(taskVO);
    populateEpicInfo(taskVO);
    return taskVO;
  }
}
