package com.projecthub.module.task.service;

import com.projecthub.common.constant.ErrorCode;
import com.projecthub.common.exception.BusinessException;
import com.projecthub.common.util.BeanCopyUtil;
import com.projecthub.module.project.service.PermissionService;
import com.projecthub.module.task.dto.CommentVO;
import com.projecthub.module.task.entity.Comment;
import com.projecthub.module.task.repository.CommentRepository;
import com.projecthub.module.task.repository.TaskRepository;
import com.projecthub.module.user.repository.UserRepository;
import com.projecthub.security.UserDetailsImpl;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 评论服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final TaskRepository taskRepository;
  private final UserRepository userRepository;
  private final PermissionService permissionService;

  /** 添加评论 */
  @Transactional
  public CommentVO addComment(Long taskId, CommentVO.CreateRequest request) {
    Long userId = getCurrentUserId();

    // 检查任务是否存在
    var taskOpt = taskRepository.findById(taskId);
    if (taskOpt.isEmpty()) {
      throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
    }

    // 权限校验
    Long projectId = taskOpt.get().getProjectId();
    if (!permissionService.hasPermission(userId, projectId, "TASK_COMMENT")) {
      throw new BusinessException(403, "无评论权限");
    }

    // 如果有 parentId，检查父评论是否存在
    if (request.getParentId() != null) {
      commentRepository
          .findById(request.getParentId())
          .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND, "父评论不存在"));
    }

    // 创建评论
    Comment comment =
        Comment.builder()
            .taskId(taskId)
            .userId(userId)
            .content(request.getContent())
            .parentId(request.getParentId())
            .build();

    commentRepository.save(comment);
    log.info("添加评论成功：commentId={}, taskId={}", comment.getId(), taskId);

    return buildCommentVO(comment);
  }

  /** 获取任务评论列表 */
  @Transactional(readOnly = true)
  public List<CommentVO> getComments(Long taskId) {
    // 检查任务是否存在
    taskRepository
        .findById(taskId)
        .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

    List<Comment> comments = commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);

    return comments.stream().map(this::buildCommentVO).collect(Collectors.toList());
  }

  /** 更新评论 */
  @Transactional
  public CommentVO updateComment(Long commentId, CommentVO.UpdateRequest request) {
    Long userId = getCurrentUserId();

    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND, "评论不存在"));

    // 检查是否是评论作者
    if (!comment.getUserId().equals(userId)) {
      throw new BusinessException(403, "无权限修改他人评论");
    }

    comment.setContent(request.getContent());
    commentRepository.save(comment);
    log.info("更新评论成功：commentId={}", commentId);

    return buildCommentVO(comment);
  }

  /** 删除评论 */
  @Transactional
  public void deleteComment(Long commentId) {
    Long userId = getCurrentUserId();

    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND, "评论不存在"));

    // 检查是否是评论作者
    if (!comment.getUserId().equals(userId)) {
      throw new BusinessException(403, "无权限删除他人评论");
    }

    commentRepository.delete(comment);
    log.info("删除评论成功：commentId={}", commentId);
  }

  /** 构建评论 VO */
  private CommentVO buildCommentVO(Comment comment) {
    CommentVO vo = BeanCopyUtil.copyProperties(comment, CommentVO.class);

    // 查询用户名
    userRepository
        .findById(comment.getUserId())
        .ifPresent(user -> vo.setUsername(user.getUsername()));

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
