package com.projecthub.module.notification.repository;

import com.projecthub.module.notification.entity.Notification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 通知 Repository 接口 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  /** 查询用户的通知列表 */
  @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
  Page<Notification> findByUserId(@Param("userId") Long userId, Pageable pageable);

  /** 查询用户的未读通知列表 */
  @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
  List<Notification> findUnreadByUserId(@Param("userId") Long userId);

  /** 统计用户的未读通知数量 */
  @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.deletedAt IS NULL")
  Long countUnreadByUserId(@Param("userId") Long userId);

  /** 标记用户所有通知为已读 */
  @Modifying
  @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
  void markAllAsReadByUserId(@Param("userId") Long userId);
}