package com.projecthub.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecthub.common.api.result.Result;
import com.projecthub.task.dto.TaskVO;
import com.projecthub.task.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TaskInternalController 单元测试
 */
@DisplayName("TaskInternalController 单元测试")
class TaskInternalControllerTest {

    private MockMvc mockMvc;
    private TaskService taskService;
    private ObjectMapper objectMapper;
    private TaskVO testTaskVO;

    @BeforeEach
    void setUp() {
        taskService = mock(TaskService.class);
        TaskInternalController controller = new TaskInternalController(taskService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();

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
    @DisplayName("内部接口 - 获取任务详情成功")
    void getTaskById_Success() throws Exception {
        // Given
        when(taskService.getTaskById(1L)).thenReturn(testTaskVO);

        // When & Then
        mockMvc.perform(get("/internal/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.taskKey").value("TASK-001"))
                .andExpect(jsonPath("$.data.title").value("测试任务"));

        verify(taskService).getTaskById(1L);
    }

    @Test
    @DisplayName("内部接口 - 获取项目的任务列表成功")
    void getTasksByProjectId_Success() throws Exception {
        // Given
        TaskVO task1 = testTaskVO;
        TaskVO task2 = new TaskVO();
        task2.setId(2L);
        task2.setTaskKey("TASK-002");
        task2.setTitle("任务 2");
        task2.setProjectId(100L);
        task2.setStatus("IN_PROGRESS");

        List<TaskVO> tasks = Arrays.asList(task1, task2);
        when(taskService.getTasksByProjectId(100L)).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/internal/tasks/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[1].id").value(2));

        verify(taskService).getTasksByProjectId(100L);
    }

    @Test
    @DisplayName("内部接口 - 获取项目的任务列表 - 空列表")
    void getTasksByProjectId_EmptyList() throws Exception {
        // Given
        when(taskService.getTasksByProjectId(100L)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/internal/tasks/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(taskService).getTasksByProjectId(100L);
    }

    @Test
    @DisplayName("内部接口 - 获取看板数据成功")
    void getKanbanData_Success() throws Exception {
        // Given
        TaskVO todoTask = new TaskVO();
        todoTask.setId(1L);
        todoTask.setTaskKey("TASK-001");
        todoTask.setTitle("待办任务");
        todoTask.setStatus("TODO");

        TaskVO inProgressTask = new TaskVO();
        inProgressTask.setId(2L);
        inProgressTask.setTaskKey("TASK-002");
        inProgressTask.setTitle("进行中任务");
        inProgressTask.setStatus("IN_PROGRESS");

        Map<String, List<TaskVO>> kanbanData = Map.of(
                "TODO", Arrays.asList(todoTask),
                "IN_PROGRESS", Arrays.asList(inProgressTask)
        );

        when(taskService.getKanbanData(100L)).thenReturn(kanbanData);

        // When & Then
        mockMvc.perform(get("/internal/tasks/project/100/kanban"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.TODO").isArray())
                .andExpect(jsonPath("$.data.IN_PROGRESS").isArray())
                .andExpect(jsonPath("$.data.TODO.length()").value(1))
                .andExpect(jsonPath("$.data.IN_PROGRESS.length()").value(1))
                .andExpect(jsonPath("$.data.TODO[0].title").value("待办任务"))
                .andExpect(jsonPath("$.data.IN_PROGRESS[0].title").value("进行中任务"));

        verify(taskService).getKanbanData(100L);
    }

    @Test
    @DisplayName("内部接口 - 获取看板数据 - 空数据")
    void getKanbanData_EmptyData() throws Exception {
        // Given
        when(taskService.getKanbanData(100L)).thenReturn(Map.of());

        // When & Then
        mockMvc.perform(get("/internal/tasks/project/100/kanban"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(taskService).getKanbanData(100L);
    }
}
