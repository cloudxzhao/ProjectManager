package com.projecthub.wiki.service;

import com.projecthub.wiki.dto.*;
import com.projecthub.wiki.entity.WikiPage;
import com.projecthub.wiki.entity.WikiSpace;
import com.projecthub.wiki.repository.WikiPageRepository;
import com.projecthub.wiki.repository.WikiSpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Wiki Service Unit Tests
 */
class WikiServiceTest {

    private WikiSpaceRepository wikiSpaceRepository;
    private WikiPageRepository wikiPageRepository;
    private WikiService wikiService;

    private WikiSpace testSpace;
    private WikiPage testPage;

    @BeforeEach
    void setUp() {
        wikiSpaceRepository = mock(WikiSpaceRepository.class);
        wikiPageRepository = mock(WikiPageRepository.class);
        wikiService = new WikiService(wikiSpaceRepository, wikiPageRepository);

        // Create test Space
        testSpace = new WikiSpace();
        testSpace.setId(1L);
        testSpace.setName("Test Space");
        testSpace.setDescription("This is a test space");
        testSpace.setProjectId(1L);
        testSpace.setIcon("📚");
        testSpace.setOwnerId("1");
        testSpace.setCreatedAt(LocalDateTime.now());
        testSpace.setUpdatedAt(LocalDateTime.now());

        // Create test Page
        testPage = new WikiPage();
        testPage.setId(1L);
        testPage.setSpaceId(1L);
        testPage.setParentId(null);
        testPage.setTitle("Test Page");
        testPage.setContent("# Test Content\n\nThis is test content.");
        testPage.setSlug("test-page");
        testPage.setOrderNum(1);
        testPage.setLevel(1);
        testPage.setCreatorId(1L);
        testPage.setLastEditorId(1L);
        testPage.setCreatedAt(LocalDateTime.now());
        testPage.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== Space Tests ====================

    @Test
    void createSpace_Success() {
        // Given
        CreateSpaceRequest request = new CreateSpaceRequest();
        request.setName("New Space");
        request.setDescription("New description");
        request.setProjectId(1L);
        request.setIcon("📖");

        when(wikiSpaceRepository.insert(any(WikiSpace.class))).thenReturn(1);
        doAnswer(invocation -> {
            WikiSpace space = invocation.getArgument(0);
            space.setId(1L);
            return null;
        }).when(wikiSpaceRepository).insert(any(WikiSpace.class));

        // When
        SpaceVO result = wikiService.createSpace(request);

        // Then
        assertNotNull(result);
        assertEquals("New Space", result.getName());
        assertEquals("New description", result.getDescription());
        assertEquals(1L, result.getProjectId());
        assertEquals("📖", result.getIcon());

        verify(wikiSpaceRepository).insert(any(WikiSpace.class));
    }

    @Test
    void getSpaceById_Success() {
        // Given
        when(wikiSpaceRepository.selectById(1L)).thenReturn(testSpace);

        // When
        SpaceVO result = wikiService.getSpaceById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Space", result.getName());
        assertEquals("This is a test space", result.getDescription());
    }

    @Test
    void getSpaceById_NotFound() {
        // Given
        when(wikiSpaceRepository.selectById(999L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            wikiService.getSpaceById(999L);
        });
        assertEquals("Wiki space not found: 999", exception.getMessage());
    }

    @Test
    void getSpacesByProjectId_Success() {
        // Given
        List<WikiSpace> spaces = Arrays.asList(testSpace);
        when(wikiSpaceRepository.findByProjectId(1L)).thenReturn(spaces);

        // When
        List<SpaceVO> result = wikiService.getSpacesByProjectId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Space", result.get(0).getName());
    }

    @Test
    void getSpacesByProjectId_Empty() {
        // Given
        when(wikiSpaceRepository.findByProjectId(1L)).thenReturn(Arrays.asList());

        // When
        List<SpaceVO> result = wikiService.getSpacesByProjectId(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateSpace_Success() {
        // Given
        CreateSpaceRequest request = new CreateSpaceRequest();
        request.setName("Updated Space");
        request.setDescription("Updated description");

        when(wikiSpaceRepository.selectById(1L)).thenReturn(testSpace);
        when(wikiSpaceRepository.updateById(any(WikiSpace.class))).thenReturn(1);

        // When
        SpaceVO result = wikiService.updateSpace(1L, request);

        // Then
        assertNotNull(result);
        verify(wikiSpaceRepository).selectById(1L);
        verify(wikiSpaceRepository).updateById(any(WikiSpace.class));
    }

    @Test
    void updateSpace_NotFound() {
        // Given
        CreateSpaceRequest request = new CreateSpaceRequest();
        request.setName("Updated Space");

        when(wikiSpaceRepository.selectById(999L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            wikiService.updateSpace(999L, request);
        });
        assertEquals("Wiki space not found: 999", exception.getMessage());
    }

    @Test
    void deleteSpace_Success() {
        // Given
        doNothing().when(wikiSpaceRepository).deleteById(1L);

        // When
        wikiService.deleteSpace(1L);

        // Then
        verify(wikiSpaceRepository).deleteById(1L);
    }

    // ==================== Page Tests ====================

    @Test
    void createPage_Success() {
        // Given
        CreatePageRequest request = new CreatePageRequest();
        request.setTitle("New Page");
        request.setContent("New content");
        request.setSpaceId(1L);
        request.setSlug("new-page");

        when(wikiPageRepository.insert(any(WikiPage.class))).thenReturn(1);
        doAnswer(invocation -> {
            WikiPage page = invocation.getArgument(0);
            page.setId(1L);
            return null;
        }).when(wikiPageRepository).insert(any(WikiPage.class));

        // When
        PageVO result = wikiService.createPage(request);

        // Then
        assertNotNull(result);
        assertEquals("New Page", result.getTitle());
        assertEquals("New content", result.getContent());
        assertEquals(1L, result.getSpaceId());
        assertEquals(1, result.getLevel());

        verify(wikiPageRepository).insert(any(WikiPage.class));
    }

    @Test
    void createPage_WithParent_Success() {
        // Given
        CreatePageRequest request = new CreatePageRequest();
        request.setTitle("Child Page");
        request.setContent("Child content");
        request.setSpaceId(1L);
        request.setParentId(10L);

        when(wikiPageRepository.insert(any(WikiPage.class))).thenReturn(1);
        doAnswer(invocation -> {
            WikiPage page = invocation.getArgument(0);
            page.setId(2L);
            return null;
        }).when(wikiPageRepository).insert(any(WikiPage.class));

        // When
        PageVO result = wikiService.createPage(request);

        // Then
        assertNotNull(result);
        assertEquals(10L, result.getParentId());
        assertEquals(2, result.getLevel()); // Child page should have level 2
    }

    @Test
    void createPage_WithOrderNum_Success() {
        // Given
        CreatePageRequest request = new CreatePageRequest();
        request.setTitle("Page");
        request.setContent("Content");
        request.setSpaceId(1L);
        request.setOrderNum(5);

        when(wikiPageRepository.insert(any(WikiPage.class))).thenReturn(1);
        doAnswer(invocation -> {
            WikiPage page = invocation.getArgument(0);
            page.setId(1L);
            return null;
        }).when(wikiPageRepository).insert(any(WikiPage.class));

        // When
        PageVO result = wikiService.createPage(request);

        // Then
        assertNotNull(result);
        assertEquals(5, result.getOrderNum());
    }

    @Test
    void getPageById_Success() {
        // Given
        when(wikiPageRepository.selectById(1L)).thenReturn(testPage);

        // When
        PageVO result = wikiService.getPageById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Page", result.getTitle());
        assertEquals("# Test Content\n\nThis is test content.", result.getContent());
    }

    @Test
    void getPageById_NotFound() {
        // Given
        when(wikiPageRepository.selectById(999L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            wikiService.getPageById(999L);
        });
        assertEquals("Wiki page not found: 999", exception.getMessage());
    }

    @Test
    void getPagesBySpaceId_Success() {
        // Given
        List<WikiPage> pages = Arrays.asList(testPage);
        when(wikiPageRepository.findBySpaceId(1L)).thenReturn(pages);

        // When
        List<PageVO> result = wikiService.getPagesBySpaceId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Page", result.get(0).getTitle());
    }

    @Test
    void getPagesBySpaceId_BuildsTree() {
        // Given
        WikiPage parentPage = new WikiPage();
        parentPage.setId(1L);
        parentPage.setSpaceId(1L);
        parentPage.setParentId(null);
        parentPage.setTitle("Parent Page");

        WikiPage childPage = new WikiPage();
        childPage.setId(2L);
        childPage.setSpaceId(1L);
        childPage.setParentId(1L);
        childPage.setTitle("Child Page");

        when(wikiPageRepository.findBySpaceId(1L)).thenReturn(Arrays.asList(parentPage, childPage));

        // When
        List<PageVO> result = wikiService.getPagesBySpaceId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Only parent at root level
        assertNotNull(result.get(0).getChildren());
        assertEquals(1, result.get(0).getChildren().size()); // One child
    }

    @Test
    void getPagesByParentId_Success() {
        // Given
        List<WikiPage> pages = Arrays.asList(testPage);
        when(wikiPageRepository.findByParentId(1L)).thenReturn(pages);

        // When
        List<PageVO> result = wikiService.getPagesByParentId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updatePage_Success() {
        // Given
        CreatePageRequest request = new CreatePageRequest();
        request.setTitle("Updated Page");
        request.setContent("Updated content");

        when(wikiPageRepository.selectById(1L)).thenReturn(testPage);
        when(wikiPageRepository.updateById(any(WikiPage.class))).thenReturn(1);

        // When
        PageVO result = wikiService.updatePage(1L, request);

        // Then
        assertNotNull(result);
        verify(wikiPageRepository).selectById(1L);
        verify(wikiPageRepository).updateById(any(WikiPage.class));
    }

    @Test
    void updatePage_PartialUpdate() {
        // Given
        CreatePageRequest request = new CreatePageRequest();
        request.setTitle("Only title updated");
        // Other fields are null

        when(wikiPageRepository.selectById(1L)).thenReturn(testPage);
        when(wikiPageRepository.updateById(any(WikiPage.class))).thenReturn(1);

        // When
        PageVO result = wikiService.updatePage(1L, request);

        // Then
        assertNotNull(result);
        verify(wikiPageRepository).updateById(any(WikiPage.class));
    }

    @Test
    void updatePage_NotFound() {
        // Given
        CreatePageRequest request = new CreatePageRequest();
        request.setTitle("Updated Title");

        when(wikiPageRepository.selectById(999L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            wikiService.updatePage(999L, request);
        });
        assertEquals("Wiki page not found: 999", exception.getMessage());
    }

    @Test
    void deletePage_Success() {
        // Given
        doNothing().when(wikiPageRepository).deleteById(1L);

        // When
        wikiService.deletePage(1L);

        // Then
        verify(wikiPageRepository).deleteById(1L);
    }
}
