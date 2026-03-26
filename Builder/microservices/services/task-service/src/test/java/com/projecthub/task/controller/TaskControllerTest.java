package com.projecthub.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecthub.common.api.result.Result;
import com.projecthub.common.core.exception.BusinessException;
import com.projecthub.common.core.exception.GlobalExceptionHandler;
import com.projecthub.common.security.domain.LoginUser;
import com.projecthub.common.security.util.UserContextHolder;
import com.projecthub.task.dto.*;
import com.projecthub.task.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Task Controller 单元测试
 */
@DisplayName("TaskController 单元测试")
class TaskControllerTest {

    private MockMvc mockMvc;
    private TaskService taskService;
    private ObjectMapper objectMapper;
    private TaskVO testTaskVO;

    @BeforeEach
    void setUp() {
        taskService = mock(TaskService.class);
        TaskController taskController = new TaskController(taskService);
        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // 设置模拟用户上下文
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(1L);
        loginUser.setUsername("testuser");
        UserContextHolder.setUser(loginUser);

        testTaskVO = new TaskVO();
        testTaskVO.setId(1L);
        testTaskVO.setTaskKey("TASK-001");
        testTaskVO.setTitle("测试任务");
        testTaskVO.setDescription("这是一个测试任务");
        testTaskVO.setProjectId(100L);
        testTaskVO.setProjectName("测试项目");
        testTaskVO.setAssigneeId(1L);
        testTaskVO.setAssigneeName("测试用户");
        testTaskVO.setCreatorId(1L);
        testTaskVO.setStatus("TODO");
        testTaskVO.setPriority("MEDIUM");
        testTaskVO.setStoryPoints(3);
        testTaskVO.setDueDate(LocalDate.now().plusDays(7));
        testTaskVO.setSortOrder(0);
    }

    @Test
    @DisplayName("创建任务成功")
    void createTask_Success() throws Exception {
        // Given
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("新任务");
        request.setDescription("新任务描述");
        request.setProjectId(100L);
        request.setAssigneeId(1L);
        request.setPriority("HIGH");
        request.setStoryPoints(5);

        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(testTaskVO);

        // When & Then
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("创建成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.taskKey").value("TASK-001"))
                .andExpect(jsonPath("$.data.title").value("测试任务"));

        verify(taskService).createTask(any(CreateTaskRequest.class));
    }

    @Test
    @DisplayName("创建任务失败 - 标题为空")
    void createTask_EmptyTitle() throws Exception {
        // Given
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("");
        request.setProjectId(100L);

        // When & Then - Validation error returns 400
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("创建任务失败 - 项目 ID 为空")
    void createTask_EmptyProjectId() throws Exception {
        // Given
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("新任务");

        // When & Then - Validation error returns 400
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("更新任务成功")
    void updateTask_Success() throws Exception {
        // Given
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("更新后的标题");
        request.setPriority("HIGH");
        request.setStoryPoints(8);

        TaskVO updatedTask = new TaskVO();
        updatedTask.setId(1L);
        updatedTask.setTaskKey("TASK-001");
        updatedTask.setTitle("更新后的标题");
        updatedTask.setPriority("HIGH");
        updatedTask.setStoryPoints(8);

        when(taskService.updateTask(eq(1L), any(UpdateTaskRequest.class))).thenReturn(updatedTask);

        // When & Then
        mockMvc.perform(put("/api/v1/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新成功"))
                .andExpect(jsonPath("$.data.title").value("更新后的标题"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"));

        verify(taskService).updateTask(eq(1L), any(UpdateTaskRequest.class));
    }

    @Test
    @DisplayName("更新任务失败 - 任务不存在")
    void updateTask_TaskNotFound() throws Exception {
        // Given
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("新标题");

        when(taskService.updateTask(eq(999L), any(UpdateTaskRequest.class)))
                .thenThrow(new BusinessException(4001, "任务不存在"));

        // When & Then
        mockMvc.perform(put("/api/v1/tasks/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4001))
                .andExpect(jsonPath("$.message").value("任务不存在"));

        verify(taskService).updateTask(eq(999L), any(UpdateTaskRequest.class));
    }

    @Test
    @DisplayName("删除任务成功")
    void deleteTask_Success() throws Exception {
        // Given
        doNothing().when(taskService).deleteTask(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(taskService).deleteTask(1L);
    }

    @Test
    @DisplayName("删除任务失败 - 任务不存在")
    void deleteTask_TaskNotFound() throws Exception {
        // Given
        doThrow(new BusinessException(4001, "任务不存在")).when(taskService).deleteTask(999L);

        // When & Then
        mockMvc.perform(delete("/api/v1/tasks/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4001))
                .andExpect(jsonPath("$.message").value("任务不存在"));

        verify(taskService).deleteTask(999L);
    }

    @Test
    @DisplayName("移动任务成功")
    void moveTask_Success() throws Exception {
        // Given
        MoveTaskRequest request = new MoveTaskRequest();
        request.setStatus("IN_PROGRESS");
        request.setSortOrder(1);
        request.setColumnId(2L);

        TaskVO movedTask = new TaskVO();
        movedTask.setId(1L);
        movedTask.setTaskKey("TASK-001");
        movedTask.setTitle("测试任务");
        movedTask.setStatus("IN_PROGRESS");
        movedTask.setSortOrder(1);

        when(taskService.moveTask(eq(1L), any(MoveTaskRequest.class))).thenReturn(movedTask);

        // When & Then
        mockMvc.perform(put("/api/v1/tasks/1/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("移动成功"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        verify(taskService).moveTask(eq(1L), any(MoveTaskRequest.class));
    }

    @Test
    @DisplayName("移动任务失败 - 状态为空")
    void moveTask_EmptyStatus() throws Exception {
        // Given
        MoveTaskRequest request = new MoveTaskRequest();
        request.setStatus("");

        // When & Then - Validation error returns 400
        mockMvc.perform(put("/api/v1/tasks/1/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("获取任务详情成功")
    void getTaskById_Success() throws Exception {
        // Given
        when(taskService.getTaskById(1L)).thenReturn(testTaskVO);

        // When & Then
        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.taskKey").value("TASK-001"))
                .andExpect(jsonPath("$.data.title").value("测试任务"));

        verify(taskService).getTaskById(1L);
    }

    @Test
    @DisplayName("获取任务详情失败 - 任务不存在")
    void getTaskById_TaskNotFound() throws Exception {
        // Given
        when(taskService.getTaskById(999L)).thenThrow(new BusinessException(4001, "任务不存在"));

        // When & Then
        mockMvc.perform(get("/api/v1/tasks/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4001))
                .andExpect(jsonPath("$.message").value("任务不存在"));

        verify(taskService).getTaskById(999L);
    }

    @Test
    @DisplayName("获取我的任务成功")
    void getMyTasks_Success() throws Exception {
        // Given
        List<TaskVO> tasks = Arrays.asList(testTaskVO);
        when(taskService.getMyTasks()).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/v1/tasks/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("测试任务"));

        verify(taskService).getMyTasks();
    }

    @Test
    @DisplayName("获取我的任务 - 空列表")
    void getMyTasks_EmptyList() throws Exception {
        // Given
        when(taskService.getMyTasks()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/v1/tasks/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(taskService).getMyTasks();
    }

    @Test
    @DisplayName("获取任务评论成功")
    void getComments_Success() throws Exception {
        // Given
        CommentVO comment1 = new CommentVO();
        comment1.setId(1L);
        comment1.setTaskId(1L);
        comment1.setUserId(1L);
        comment1.setUsername("user1");
        comment1.setContent("评论 1");

        CommentVO comment2 = new CommentVO();
        comment2.setId(2L);
        comment2.setTaskId(1L);
        comment2.setUserId(2L);
        comment2.setUsername("user2");
        comment2.setContent("评论 2");

        List<CommentVO> comments = Arrays.asList(comment1, comment2);
        when(taskService.getComments(1L)).thenReturn(comments);

        // When & Then
        mockMvc.perform(get("/api/v1/tasks/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].content").value("评论 1"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].content").value("评论 2"));

        verify(taskService).getComments(1L);
    }

    @Test
    @DisplayName("添加评论成功")
    void addComment_Success() throws Exception {
        // Given
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("测试评论内容");

        CommentVO commentVO = new CommentVO();
        commentVO.setId(1L);
        commentVO.setTaskId(1L);
        commentVO.setUserId(1L);
        commentVO.setUsername("testuser");
        commentVO.setContent("测试评论内容");

        when(taskService.addComment(eq(1L), any(CreateCommentRequest.class))).thenReturn(commentVO);

        // When & Then
        mockMvc.perform(post("/api/v1/tasks/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("添加成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.content").value("测试评论内容"));

        verify(taskService).addComment(eq(1L), any(CreateCommentRequest.class));
    }

    @Test
    @DisplayName("添加评论失败 - 内容为空")
    void addComment_EmptyContent() throws Exception {
        // Given
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("");

        // When & Then - Validation error returns 400
        mockMvc.perform(post("/api/v1/tasks/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("添加评论失败 - 任务不存在")
    void addComment_TaskNotFound() throws Exception {
        // Given
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("测试评论");

        when(taskService.addComment(eq(999L), any(CreateCommentRequest.class)))
                .thenThrow(new BusinessException(4001, "任务不存在"));

        // When & Then
        mockMvc.perform(post("/api/v1/tasks/999/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4001))
                .andExpect(jsonPath("$.message").value("任务不存在"));

        verify(taskService).addComment(eq(999L), any(CreateCommentRequest.class));
    }

    @Test
    @DisplayName("删除评论成功")
    void deleteComment_Success() throws Exception {
        // Given
        doNothing().when(taskService).deleteComment(1L, 1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/tasks/1/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(taskService).deleteComment(1L, 1L);
    }

    @Test
    @DisplayName("删除评论失败 - 评论不存在")
    void deleteComment_NotFound() throws Exception {
        // Given
        doThrow(new BusinessException(400, "评论不存在")).when(taskService).deleteComment(1L, 999L);

        // When & Then
        mockMvc.perform(delete("/api/v1/tasks/1/comments/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("评论不存在"));

        verify(taskService).deleteComment(1L, 999L);
    }
}
