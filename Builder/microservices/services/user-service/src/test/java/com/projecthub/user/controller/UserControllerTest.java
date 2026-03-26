package com.projecthub.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecthub.common.api.result.Result;
import com.projecthub.common.core.exception.BusinessException;
import com.projecthub.common.core.exception.GlobalExceptionHandler;
import com.projecthub.common.security.domain.LoginUser;
import com.projecthub.common.security.util.UserContextHolder;
import com.projecthub.user.dto.UpdateUserRequest;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * User Controller 单元测试
 */
@DisplayName("UserController 单元测试")
class UserControllerTest {

    private MockMvc mockMvc;
    private UserService userService;
    private ObjectMapper objectMapper;
    private UserVO testUserVO;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        UserController userController = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // 设置模拟用户上下文
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(1L);
        loginUser.setUsername("testuser");
        UserContextHolder.setUser(loginUser);

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
    @DisplayName("获取当前用户信息成功")
    void getCurrentUser_Success() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(testUserVO);

        // When & Then
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("根据 ID 获取用户成功")
    void getUserById_Success() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(testUserVO);

        // When & Then
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("根据 ID 获取用户失败 - 用户不存在")
    void getUserById_UserNotFound() throws Exception {
        // Given
        when(userService.getUserById(999L)).thenThrow(new com.projecthub.common.core.exception.BusinessException(2001, "用户不存在"));

        // When & Then
        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2001))
                .andExpect(jsonPath("$.message").value("用户不存在"));

        verify(userService).getUserById(999L);
    }

    @Test
    @DisplayName("获取所有用户成功")
    void getAllUsers_Success() throws Exception {
        // Given
        List<UserVO> users = Arrays.asList(testUserVO);
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("更新用户信息成功")
    void updateUser_Success() throws Exception {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setNickname("新昵称");
        request.setPhone("13900139000");

        UserVO updatedUser = new UserVO();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("test@example.com");
        updatedUser.setNickname("新昵称");
        updatedUser.setPhone("13900139000");

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("新昵称"))
                .andExpect(jsonPath("$.data.phone").value("13900139000"));

        verify(userService).updateUser(eq(1L), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("更新当前用户信息成功")
    void updateCurrentUser_Success() throws Exception {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setNickname("新昵称");

        UserVO updatedUser = new UserVO();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setNickname("新昵称");

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("新昵称"));

        verify(userService).updateUser(eq(1L), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("更新用户失败 - 请求体为空")
    void updateUser_EmptyRequest() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("删除用户成功")
    void deleteUser_Success() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("删除用户失败 - 用户不存在")
    void deleteUser_UserNotFound() throws Exception {
        // Given
        doThrow(new com.projecthub.common.core.exception.BusinessException(2001, "用户不存在"))
                .when(userService).deleteUser(999L);

        // When & Then
        mockMvc.perform(delete("/api/v1/users/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2001))
                .andExpect(jsonPath("$.message").value("用户不存在"));

        verify(userService).deleteUser(999L);
    }
}
