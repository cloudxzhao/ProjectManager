package com.projecthub.user.service;

import com.projecthub.common.core.exception.BusinessException;
import com.projecthub.common.mq.constant.EventType;
import com.projecthub.common.mq.domain.EventMessage;
import com.projecthub.common.mq.service.EventPublisher;
import com.projecthub.user.dto.UpdateUserRequest;
import com.projecthub.user.dto.UserVO;
import com.projecthub.user.entity.User;
import com.projecthub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * User Service 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 单元测试")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventPublisher eventPublisher;

    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, eventPublisher);

        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setNickname("测试用户");
        testUser.setAvatar("https://example.com/avatar.jpg");
        testUser.setBio("这是一个测试用户");
        testUser.setRole("MEMBER");
        testUser.setStatus(1);
        testUser.setDeleted(0);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("根据 ID 获取用户成功")
    void getUserById_Success() {
        // Given
        when(userRepository.selectById(1L)).thenReturn(testUser);

        // When
        UserVO result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).selectById(1L);
    }

    @Test
    @DisplayName("根据 ID 获取用户失败 - 用户不存在")
    void getUserById_UserNotFound() {
        // Given
        when(userRepository.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.getUserById(999L);
        });

        assertEquals(2001, exception.getCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("根据邮箱获取用户成功")
    void getUserByEmail_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserVO result = userService.getUserByEmail("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("根据邮箱获取用户失败 - 用户不存在")
    void getUserByEmail_UserNotFound() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.getUserByEmail("nonexistent@example.com");
        });

        assertEquals(2001, exception.getCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("根据用户名获取用户成功")
    void getUserByUsername_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserVO result = userService.getUserByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("根据用户名获取用户失败 - 用户不存在")
    void getUserByUsername_UserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.getUserByUsername("nonexistent");
        });

        assertEquals(2001, exception.getCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("获取所有用户成功")
    void getAllUsers_Success() {
        // Given
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setDeleted(0);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setDeleted(0);

        User deletedUser = new User();
        deletedUser.setId(3L);
        deletedUser.setUsername("deleted");
        deletedUser.setEmail("deleted@example.com");
        deletedUser.setDeleted(1);

        when(userRepository.selectList(null)).thenReturn(Arrays.asList(user1, user2, deletedUser));

        // When
        List<UserVO> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    @DisplayName("根据 ID 列表获取用户成功")
    void getUsersByIds_Success() {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L);
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setDeleted(0);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setDeleted(0);

        when(userRepository.findByIds(ids)).thenReturn(Arrays.asList(user1, user2));

        // When
        List<UserVO> result = userService.getUsersByIds(ids);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findByIds(ids);
    }

    @Test
    @DisplayName("根据 ID 列表获取用户 - 空列表")
    void getUsersByIds_EmptyList() {
        // When
        List<UserVO> result = userService.getUsersByIds(null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify no DB call was made
        verify(userRepository, never()).findByIds(any());
    }

    @Test
    @DisplayName("更新用户信息成功")
    void updateUser_Success() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setNickname("新昵称");
        request.setPhone("13900139000");
        request.setAvatar("https://example.com/new-avatar.jpg");
        request.setBio("新的个人简介");

        when(userRepository.selectById(1L)).thenReturn(testUser);
        when(userRepository.updateById(any(User.class))).thenReturn(1);

        // When
        UserVO result = userService.updateUser(1L, request);

        // Then
        assertNotNull(result);
        assertEquals("新昵称", result.getNickname());
        assertEquals("13900139000", result.getPhone());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).updateById(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals("新昵称", capturedUser.getNickname());
        assertEquals("13900139000", capturedUser.getPhone());

        verify(eventPublisher).publish(eq("user.updated"), any(EventMessage.class));
    }

    @Test
    @DisplayName("更新用户信息部分字段")
    void updateUser_PartialFields() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setNickname("仅更新昵称");
        // Other fields are null

        when(userRepository.selectById(1L)).thenReturn(testUser);
        when(userRepository.updateById(any(User.class))).thenReturn(1);

        // When
        UserVO result = userService.updateUser(1L, request);

        // Then
        assertNotNull(result);
        assertEquals("仅更新昵称", result.getNickname());
        assertEquals("13800138000", result.getPhone()); // Original value preserved
    }

    @Test
    @DisplayName("更新用户失败 - 用户不存在")
    void updateUser_UserNotFound() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setNickname("新昵称");

        when(userRepository.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.updateUser(999L, request);
        });

        assertEquals(2001, exception.getCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("删除用户成功")
    void deleteUser_Success() {
        // Given
        when(userRepository.selectById(1L)).thenReturn(testUser);
        when(userRepository.deleteById(1L)).thenReturn(1);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).deleteById(1L);
        verify(eventPublisher).publish(eq("user.deleted"), any(EventMessage.class));
    }

    @Test
    @DisplayName("删除用户失败 - 用户不存在")
    void deleteUser_UserNotFound() {
        // Given
        when(userRepository.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.deleteUser(999L);
        });

        assertEquals(2001, exception.getCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("实体转 VO 验证所有字段")
    void toVO_AllFieldsMapped() {
        // This test verifies the private toVO method indirectly through getUserById
        when(userRepository.selectById(1L)).thenReturn(testUser);

        UserVO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getPhone(), result.getPhone());
        assertEquals(testUser.getNickname(), result.getNickname());
        assertEquals(testUser.getAvatar(), result.getAvatar());
        assertEquals(testUser.getBio(), result.getBio());
        assertEquals(testUser.getRole(), result.getRole());
        assertEquals(testUser.getStatus(), result.getStatus());
    }

    @Test
    @DisplayName("发布用户更新事件验证")
    void updateUser_PublishesEvent() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setNickname("新昵称");

        when(userRepository.selectById(1L)).thenReturn(testUser);
        when(userRepository.updateById(any(User.class))).thenReturn(1);

        // When
        userService.updateUser(1L, request);

        // Then
        ArgumentCaptor<EventMessage> eventCaptor = ArgumentCaptor.forClass(EventMessage.class);
        verify(eventPublisher).publish(eq("user.updated"), eventCaptor.capture());

        EventMessage capturedEvent = eventCaptor.getValue();
        assertEquals(EventType.USER_UPDATED, capturedEvent.getEventType());
        assertEquals("user-service", capturedEvent.getSource());
        assertNotNull(capturedEvent.getData());
    }

    @Test
    @DisplayName("发布用户删除事件验证")
    void deleteUser_PublishesEvent() {
        // Given
        when(userRepository.selectById(1L)).thenReturn(testUser);
        when(userRepository.deleteById(1L)).thenReturn(1);

        // When
        userService.deleteUser(1L);

        // Then
        ArgumentCaptor<EventMessage> eventCaptor = ArgumentCaptor.forClass(EventMessage.class);
        verify(eventPublisher).publish(eq("user.deleted"), eventCaptor.capture());

        EventMessage capturedEvent = eventCaptor.getValue();
        assertEquals(EventType.USER_DELETED, capturedEvent.getEventType());
        assertEquals("user-service", capturedEvent.getSource());
        assertEquals(1L, capturedEvent.getData());
    }
}
