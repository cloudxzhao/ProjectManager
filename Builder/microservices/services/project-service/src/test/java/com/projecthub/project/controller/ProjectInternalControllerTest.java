package com.projecthub.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecthub.common.api.result.Result;
import com.projecthub.common.core.exception.BusinessException;
import com.projecthub.common.core.exception.GlobalExceptionHandler;
import com.projecthub.project.dto.ProjectVO;
import com.projecthub.project.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProjectInternalController 单元测试
 */
@DisplayName("ProjectInternalController 单元测试")
class ProjectInternalControllerTest {

    private MockMvc mockMvc;
    private ProjectService projectService;
    private ObjectMapper objectMapper;
    private ProjectVO testProjectVO;

    @BeforeEach
    void setUp() {
        projectService = mock(ProjectService.class);
        ProjectInternalController controller = new ProjectInternalController(projectService);

        // 配置 MockMvc 以支持 @PathVariable 参数名解析
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        testProjectVO = new ProjectVO();
        testProjectVO.setId(1L);
        testProjectVO.setName("测试项目");
        testProjectVO.setDescription("测试描述");
        testProjectVO.setStatus("ACTIVE");
        testProjectVO.setProgress(50);
        testProjectVO.setMemberCount(3);
    }

    @Test
    @DisplayName("内部接口 - 根据 ID 获取项目成功")
    void internalGetProjectById_Success() throws Exception {
        // Given
        when(projectService.getProjectById(1L)).thenReturn(testProjectVO);

        // When & Then
        mockMvc.perform(get("/internal/projects/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试项目"));

        verify(projectService).getProjectById(1L);
    }

    @Test
    @DisplayName("内部接口 - 根据 ID 获取项目失败")
    void internalGetProjectById_NotFound() throws Exception {
        // Given
        when(projectService.getProjectById(999L)).thenThrow(new BusinessException(3001, "项目不存在"));

        // When & Then
        mockMvc.perform(get("/internal/projects/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(3001))
                .andExpect(jsonPath("$.message").value("项目不存在"));

        verify(projectService).getProjectById(999L);
    }

    @Test
    @DisplayName("内部接口 - 批量获取项目成功")
    void internalGetProjectsByIds_Success() throws Exception {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L);

        when(projectService.getProjectById(1L)).thenReturn(testProjectVO);
        when(projectService.getProjectById(2L)).thenThrow(new BusinessException(3001, "项目不存在"));

        // When & Then
        mockMvc.perform(post("/internal/projects/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(projectService).getProjectById(1L);
        verify(projectService).getProjectById(2L);
    }

    @Test
    @DisplayName("内部接口 - 检查成员关系 - 是成员")
    void internalIsMember_True() throws Exception {
        // Given
        when(projectService.isMember(1L, 2L)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/internal/projects/1/members/2/check")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(projectService).isMember(1L, 2L);
    }

    @Test
    @DisplayName("内部接口 - 检查成员关系 - 不是成员")
    void internalIsMember_False() throws Exception {
        // Given
        when(projectService.isMember(1L, 999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/internal/projects/1/members/999/check")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));

        verify(projectService).isMember(1L, 999L);
    }

    @Test
    @DisplayName("内部接口 - 获取项目统计成功")
    void internalGetProjectStats_Success() throws Exception {
        // Given
        when(projectService.getProjectById(1L)).thenReturn(testProjectVO);

        // When & Then
        mockMvc.perform(get("/internal/projects/1/stats")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.progress").value(50))
                .andExpect(jsonPath("$.data.memberCount").value(3));

        verify(projectService).getProjectById(1L);
    }

    @Test
    @DisplayName("内部接口 - 获取项目统计失败")
    void internalGetProjectStats_NotFound() throws Exception {
        // Given
        when(projectService.getProjectById(999L)).thenThrow(new BusinessException(3001, "项目不存在"));

        // When & Then
        mockMvc.perform(get("/internal/projects/999/stats")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(3001))
                .andExpect(jsonPath("$.message").value("项目不存在"));

        verify(projectService).getProjectById(999L);
    }
}
