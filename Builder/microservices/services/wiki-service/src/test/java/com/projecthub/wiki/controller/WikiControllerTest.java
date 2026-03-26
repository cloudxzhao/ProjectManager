package com.projecthub.wiki.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecthub.common.api.result.Result;
import com.projecthub.common.core.exception.GlobalExceptionHandler;
import com.projecthub.wiki.dto.*;
import com.projecthub.wiki.service.WikiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Wiki Controller Unit Tests
 */
class WikiControllerTest {

    private MockMvc mockMvc;
    private WikiService wikiService;
    private ObjectMapper objectMapper;

    private SpaceVO testSpaceVO;
    private PageVO testPageVO;

    @BeforeEach
    void setUp() {
        wikiService = mock(WikiService.class);
        WikiController controller = new WikiController(wikiService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // Create test Space VO
        testSpaceVO = new SpaceVO();
        testSpaceVO.setId(1L);
        testSpaceVO.setName("Test Space");
        testSpaceVO.setDescription("This is a test space");
        testSpaceVO.setProjectId(1L);
        testSpaceVO.setIcon("📚");
        testSpaceVO.setOwnerId("1");
        testSpaceVO.setPageCount(5);

        // Create test Page VO
        testPageVO = new PageVO();
        testPageVO.setId(1L);
        testPageVO.setSpaceId(1L);
        testPageVO.setParentId(null);
        testPageVO.setTitle("Test Page");
        testPageVO.setContent("# Test Content\n\nThis is test content.");
        testPageVO.setSlug("test-page");
        testPageVO.setOrderNum(1);
        testPageVO.setLevel(1);
        testPageVO.setCreatorId(1L);
        testPageVO.setLastEditorId(1L);
    }

    // ==================== Space Tests ====================

    @Test
    void createSpace_Success() throws Exception {
        // Given
        CreateSpaceRequest request = new CreateSpaceRequest();
        request.setName("New Space");
        request.setDescription("New description");
        request.setProjectId(1L);
        request.setIcon("📖");

        when(wikiService.createSpace(any(CreateSpaceRequest.class))).thenReturn(testSpaceVO);

        // When & Then
        mockMvc.perform(post("/api/v1/wiki/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Space"));

        verify(wikiService).createSpace(any(CreateSpaceRequest.class));
    }

    @Test
    void createSpace_EmptyName() throws Exception {
        // Given
        CreateSpaceRequest request = new CreateSpaceRequest();
        request.setName("");
        request.setProjectId(1L);

        // When & Then
        mockMvc.perform(post("/api/v1/wiki/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(wikiService, never()).createSpace(any(CreateSpaceRequest.class));
    }

    @Test
    void createSpace_EmptyProjectId() throws Exception {
        // Given
        CreateSpaceRequest request = new CreateSpaceRequest();
        request.setName("Space");
        request.setProjectId(null);

        // When & Then
        mockMvc.perform(post("/api/v1/wiki/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(wikiService, never()).createSpace(any(CreateSpaceRequest.class));
    }

    @Test
    void getSpaceById_Success() throws Exception {
        // Given
        when(wikiService.getSpaceById(1L)).thenReturn(testSpaceVO);

        // When & Then
        mockMvc.perform(get("/api/v1/wiki/spaces/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Space"))
                .andExpect(jsonPath("$.data.pageCount").value(5));

        verify(wikiService).getSpaceById(1L);
    }

    @Test
    void getSpaceById_NotFound() throws Exception {
        // Given
        when(wikiService.getSpaceById(999L))
                .thenThrow(new RuntimeException("Wiki space not found: 999"));

        // When & Then
        mockMvc.perform(get("/api/v1/wiki/spaces/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Wiki space not found: 999"));

        verify(wikiService).getSpaceById(999L);
    }

    @Test
    void getSpacesByProjectId_Success() throws Exception {
        // Given
        List<SpaceVO> spaces = Arrays.asList(testSpaceVO);
        when(wikiService.getSpacesByProjectId(1L)).thenReturn(spaces);

        // When & Then
        mockMvc.perform(get("/api/v1/wiki/spaces/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(wikiService).getSpacesByProjectId(1L);
    }

    @Test
    void updateSpace_Success() throws Exception {
        // Given
        CreateSpaceRequest request = new CreateSpaceRequest();
        request.setName("Updated Space");
        request.setDescription("Updated description");

        SpaceVO updated = new SpaceVO();
        updated.setId(1L);
        updated.setName("Updated Space");

        when(wikiService.updateSpace(eq(1L), any(CreateSpaceRequest.class))).thenReturn(updated);

        // When & Then
        mockMvc.perform(put("/api/v1/wiki/spaces/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("Updated Space"));

        verify(wikiService).updateSpace(eq(1L), any(CreateSpaceRequest.class));
    }

    @Test
    void deleteSpace_Success() throws Exception {
        // Given
        doNothing().when(wikiService).deleteSpace(1L);

        // When & Then
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/wiki/spaces/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(wikiService).deleteSpace(1L);
    }

    @Test
    void deleteSpace_NotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("Wiki space not found: 999"))
                .when(wikiService).deleteSpace(999L);

        // When & Then
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/wiki/spaces/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Wiki space not found: 999"));

        verify(wikiService).deleteSpace(999L);
    }

    // ==================== Page Tests ====================

    @Test
    void createPage_Success() throws Exception {
        // Given
        CreatePageRequest request = new CreatePageRequest();
        request.setTitle("New Page");
        request.setContent("New content");
        request.setSpaceId(1L);
        request.setSlug("new-page");

        when(wikiService.createPage(any(CreatePageRequest.class))).thenReturn(testPageVO);

        // When & Then
        mockMvc.perform(post("/api/v1/wiki/pages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Page"));

        verify(wikiService).createPage(any(CreatePageRequest.class));
    }

    @Test
    void createPage_EmptyTitle() throws Exception {
        // Given
        CreatePageRequest request = new CreatePageRequest();
        request.setTitle("");
        request.setSpaceId(1L);

        // When & Then
        mockMvc.perform(post("/api/v1/wiki/pages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(wikiService, never()).createPage(any(CreatePageRequest.class));
    }

    @Test
    void createPage_EmptySpaceId() throws Exception {
        // Given
        CreatePageRequest request = new CreatePageRequest();
        request.setTitle("Page");
        request.setSpaceId(null);

        // When & Then
        mockMvc.perform(post("/api/v1/wiki/pages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(wikiService, never()).createPage(any(CreatePageRequest.class));
    }

    @Test
    void getPageById_Success() throws Exception {
        // Given
        when(wikiService.getPageById(1L)).thenReturn(testPageVO);

        // When & Then
        mockMvc.perform(get("/api/v1/wiki/pages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Page"))
                .andExpect(jsonPath("$.data.content").value("# Test Content\n\nThis is test content."));

        verify(wikiService).getPageById(1L);
    }

    @Test
    void getPageById_NotFound() throws Exception {
        // Given
        when(wikiService.getPageById(999L))
                .thenThrow(new RuntimeException("Wiki page not found: 999"));

        // When & Then
        mockMvc.perform(get("/api/v1/wiki/pages/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Wiki page not found: 999"));

        verify(wikiService).getPageById(999L);
    }

    @Test
    void getPagesBySpaceId_Success() throws Exception {
        // Given
        List<PageVO> pages = Arrays.asList(testPageVO);
        when(wikiService.getPagesBySpaceId(1L)).thenReturn(pages);

        // When & Then
        mockMvc.perform(get("/api/v1/wiki/pages/space/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(wikiService).getPagesBySpaceId(1L);
    }

    @Test
    void getPagesByParentId_Success() throws Exception {
        // Given
        List<PageVO> pages = Arrays.asList(testPageVO);
        when(wikiService.getPagesByParentId(1L)).thenReturn(pages);

        // When & Then
        mockMvc.perform(get("/api/v1/wiki/pages/parent/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(wikiService).getPagesByParentId(1L);
    }

    @Test
    void updatePage_Success() throws Exception {
        // Given
        CreatePageRequest request = new CreatePageRequest();
        request.setTitle("Updated Page");
        request.setContent("Updated content");

        PageVO updated = new PageVO();
        updated.setId(1L);
        updated.setTitle("Updated Page");

        when(wikiService.updatePage(eq(1L), any(CreatePageRequest.class))).thenReturn(updated);

        // When & Then
        mockMvc.perform(put("/api/v1/wiki/pages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Updated Page"));

        verify(wikiService).updatePage(eq(1L), any(CreatePageRequest.class));
    }

    @Test
    void deletePage_Success() throws Exception {
        // Given
        doNothing().when(wikiService).deletePage(1L);

        // When & Then
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/wiki/pages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(wikiService).deletePage(1L);
    }

    @Test
    void deletePage_NotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("Wiki page not found: 999"))
                .when(wikiService).deletePage(999L);

        // When & Then
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/wiki/pages/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Wiki page not found: 999"));

        verify(wikiService).deletePage(999L);
    }
}
