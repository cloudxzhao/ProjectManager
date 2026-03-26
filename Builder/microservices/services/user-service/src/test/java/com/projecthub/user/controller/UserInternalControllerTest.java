package com.projecthub.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecthub.common.api.result.Result;
import com.projecthub.common.core.exception.BusinessException;
import com.projecthub.common.core.exception.GlobalExceptionHandler;
import com.projecthub.user.dto.UserVO;
import com.projecthub.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * User Internal Controller 单元测试
 */
@DisplayName("UserInternalController 单元测试")
class UserInternalControllerTest {

    private MockMvc mockMvc;
    private UserService userService;
    private ObjectMapper objectMapper;
    private UserVO testUserVO;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        UserInternalController controller = new UserInternalController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setUsername("testuser");
        testUserVO.setEmail("test@example.com");
        testUserVO.setPhone("13800138000");
        testUserVO.setNickname("测试用户");
        testUserVO.setAvatar("https://example.com/avatar.jpg");
        testUserVO.setBio("这是一个测试用户");
        testUserVO.setRole("MEMBER");
        testUserVO.setStatus(1);
    }

    @Test
    @DisplayName("内部接口 - 根据 ID 获取用户成功")
    void internalGetUserById_Success() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(testUserVO);

        // When & Then
        mockMvc.perform(get("/internal/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("内部接口 - 根据 ID 获取用户失败")
    void internalGetUserById_UserNotFound() throws Exception {
        // Given
        when(userService.getUserById(999L)).thenThrow(new com.projecthub.common.core.exception.BusinessException(2001, "用户不存在"));

        // When & Then
        mockMvc.perform(get("/internal/users/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2001))
                .andExpect(jsonPath("$.message").value("用户不存在"));

        verify(userService).getUserById(999L);
    }

    @Test
    @DisplayName("内部接口 - 批量获取用户成功")
    void internalGetUsersByIds_Success() throws Exception {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L);
        List<UserVO> users = Arrays.asList(testUserVO);
        when(userService.getUsersByIds(ids)).thenReturn(users);

        // When & Then
        mockMvc.perform(post("/internal/users/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(userService).getUsersByIds(ids);
    }

    @Test
    @DisplayName("内部接口 - 批量获取用户 - 空列表")
    void internalGetUsersByIds_EmptyList() throws Exception {
        // Given
        List<Long> ids = Arrays.asList();
        when(userService.getUsersByIds(ids)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(post("/internal/users/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(userService).getUsersByIds(ids);
    }

    @Test
    @DisplayName("内部接口 - 根据邮箱获取用户成功")
    void internalGetUserByEmail_Success() throws Exception {
        // Given
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserVO);

        // When & Then
        mockMvc.perform(get("/internal/users/by-email")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService).getUserByEmail("test@example.com");
    }

    @Test
    @DisplayName("内部接口 - 根据邮箱获取用户失败")
    void internalGetUserByEmail_UserNotFound() throws Exception {
        // Given
        when(userService.getUserByEmail("nonexistent@example.com"))
                .thenThrow(new com.projecthub.common.core.exception.BusinessException(2001, "用户不存在"));

        // When & Then
        mockMvc.perform(get("/internal/users/by-email")
                        .param("email", "nonexistent@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2001))
                .andExpect(jsonPath("$.message").value("用户不存在"));

        verify(userService).getUserByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("内部接口 - 检查用户存在 - 用户存在")
    void checkUserExists_UserExists() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(testUserVO);

        // When & Then
        mockMvc.perform(get("/internal/users/1/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("内部接口 - 检查用户存在 - 用户不存在")
    void checkUserExists_UserNotFound() throws Exception {
        // Given
        when(userService.getUserById(999L)).thenThrow(new com.projecthub.common.core.exception.BusinessException(2001, "用户不存在"));

        // When & Then
        mockMvc.perform(get("/internal/users/999/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));

        verify(userService).getUserById(999L);
    }

    @Test
    @DisplayName("内部接口 - 检查用户存在 - 其他异常")
    void checkUserExists_OtherException() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/internal/users/1/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));

        verify(userService).getUserById(1L);
    }
}
