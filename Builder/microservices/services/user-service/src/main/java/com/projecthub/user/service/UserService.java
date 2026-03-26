package com.projecthub.user.service;

import com.projecthub.common.core.exception.BusinessException;
import com.projecthub.common.mq.constant.EventType;
import com.projecthub.common.mq.domain.EventMessage;
import com.projecthub.common.mq.service.EventPublisher;
import com.projecthub.user.dto.UpdateUserRequest;
import com.projecthub.user.dto.UserVO;
import com.projecthub.user.entity.User;
import com.projecthub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    /**
     * 根据ID获取用户
     */
    public UserVO getUserById(Long id) {
        User user = userRepository.selectById(id);
        if (user == null) {
            throw new BusinessException(2001, "用户不存在");
        }
        return toVO(user);
    }

    /**
     * 根据邮箱获取用户
     */
    public UserVO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(2001, "用户不存在"));
        return toVO(user);
    }

    /**
     * 根据用户名获取用户
     */
    public UserVO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(2001, "用户不存在"));
        return toVO(user);
    }

    /**
     * 获取所有用户
     */
    public List<UserVO> getAllUsers() {
        return userRepository.selectList(null).stream()
                .filter(u -> u.getDeleted() == 0)
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID列表获取用户
     */
    public List<UserVO> getUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return userRepository.findByIds(ids).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public UserVO updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.selectById(id);
        if (user == null) {
            throw new BusinessException(2001, "用户不存在");
        }

        // 更新字段
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        userRepository.updateById(user);
        log.info("用户信息更新成功: {}", user.getId());

        // 发布用户更新事件
        EventMessage<UserVO> event = EventMessage.of(
                EventType.USER_UPDATED,
                "user-service",
                toVO(user)
        );
        eventPublisher.publish("user.updated", event);

        return toVO(user);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.selectById(id);
        if (user == null) {
            throw new BusinessException(2001, "用户不存在");
        }

        userRepository.deleteById(id);
        log.info("用户删除成功: {}", id);

        // 发布用户删除事件
        EventMessage<Long> event = EventMessage.of(
                EventType.USER_DELETED,
                "user-service",
                id
        );
        eventPublisher.publish("user.deleted", event);
    }

    /**
     * 实体转VO
     */
    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        return vo;
    }

}