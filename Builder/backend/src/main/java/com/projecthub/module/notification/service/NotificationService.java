package com.projecthub.module.notification.service;

import com.projecthub.common.exception.BusinessException;
import com.projecthub.common.response.PageResult;
import com.projecthub.common.util.BeanCopyUtil;
import com.projecthub.module.notification.dto.NotificationVO;
import com.projecthub.module.notification.entity.Notification;
import com.projecthub.module.notification.repository.NotificationRepository;
import com.projecthub.security.UserDetailsImpl;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 通知服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  /** 获取通知列表 */
  @Transactional(readOnly = true)
  public PageResult<NotificationVO> getNotifications(Integer page, Integer size) {
    Long userId = getCurrentUserId();
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    Page<Notification> notificationPage = notificationRepository.findByUserId(userId, pageable);

    List<NotificationVO> content =
        notificationPage.getContent().stream()
            .map(notification -> BeanCopyUtil.copyProperties(notification, NotificationVO.class))
            .collect(Collectors.toList());

    return PageResult.of(content, notificationPage.getTotalElements(), page, size);
  }

  /** 获取未读通知数量 */
  @Transactional(readOnly = true)
  public Long getUnreadCount() {
    Long userId = getCurrentUserId();
    return notificationRepository.countUnreadByUserId(userId);
  }

  /** 标记通知为已读 */
  @Transactional
  public void markAsRead(Long notificationId) {
    Long userId = getCurrentUserId();

    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new BusinessException("通知不存在"));

    // 检查是否是该用户的通知
    if (!notification.getUserId().equals(userId)) {
      throw new BusinessException(403, "无权限操作");
    }

    notification.setIsRead(true);
    notificationRepository.save(notification);
    log.info("标记通知为已读：notificationId={}", notificationId);
  }

  /** 标记所有通知为已读 */
  @Transactional
  public void markAllAsRead() {
    Long userId = getCurrentUserId();
    notificationRepository.markAllAsReadByUserId(userId);
    log.info("标记所有通知为已读：userId={}", userId);
  }

  /** 创建通知 */
  @Transactional
  public NotificationVO createNotification(
      Long userId, String title, String content, String type, Long relatedId, String relatedType) {
    Notification notification =
        Notification.builder()
            .userId(userId)
            .title(title)
            .content(content)
            .type(type)
            .relatedId(relatedId)
            .relatedType(relatedType)
            .isRead(false)
            .build();

    notificationRepository.save(notification);
    log.info("创建通知成功：notificationId={}, userId={}", notification.getId(), userId);

    return BeanCopyUtil.copyProperties(notification, NotificationVO.class);
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
