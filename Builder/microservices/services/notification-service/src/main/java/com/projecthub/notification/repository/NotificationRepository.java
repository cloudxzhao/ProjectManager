package com.projecthub.notification.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.projecthub.notification.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Notification Repository
 */
@Mapper
public interface NotificationRepository extends BaseMapper<Notification> {

    /**
     * Find notifications by user ID
     */
    List<Notification> findByUserId(Long userId);

}
