package com.projecthub.story.entity;

import com.baomidou.mybatisplus.annotation.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserStory Entity 单元测试
 */
@DisplayName("UserStory Entity 单元测试")
class UserStoryTest {

    @Test
    @DisplayName("创建 UserStory 实体")
    void createUserStory_Success() {
        // Given & When
        UserStory story = new UserStory();
        story.setId(1L);
        story.setStoryKey("STORY-1-ABC123");
        story.setTitle("测试用户故事");
        story.setDescription("这是一个测试用户故事");
        story.setAcceptanceCriteria("验收标准");
        story.setEpicId(1L);
        story.setProjectId(1L);
        story.setAssigneeId(2L);
        story.setStatus("OPEN");
        story.setPriority("MEDIUM");
        story.setStoryPoints(5);
        story.setCreatorId(1L);
        story.setCreatedAt(LocalDateTime.now());
        story.setUpdatedAt(LocalDateTime.now());
        story.setDeleted(0);

        // Then
        assertNotNull(story);
        assertEquals(1L, story.getId());
        assertEquals("STORY-1-ABC123", story.getStoryKey());
        assertEquals("测试用户故事", story.getTitle());
        assertEquals("这是一个测试用户故事", story.getDescription());
        assertEquals("验收标准", story.getAcceptanceCriteria());
        assertEquals(1L, story.getEpicId());
        assertEquals(1L, story.getProjectId());
        assertEquals(2L, story.getAssigneeId());
        assertEquals("OPEN", story.getStatus());
        assertEquals("MEDIUM", story.getPriority());
        assertEquals(5, story.getStoryPoints());
        assertEquals(1L, story.getCreatorId());
        assertEquals(0, story.getDeleted());
    }

    @Test
    @DisplayName("UserStory 实体字段注解验证")
    void userStory_EntityAnnotations() throws NoSuchFieldException {
        // Given
        Field idField = UserStory.class.getDeclaredField("id");
        Field createdAtField = UserStory.class.getDeclaredField("createdAt");
        Field updatedAtField = UserStory.class.getDeclaredField("updatedAt");
        Field deletedField = UserStory.class.getDeclaredField("deleted");

        // Then - ID field
        assertTrue(idField.isAnnotationPresent(TableId.class));
        TableId tableId = idField.getAnnotation(TableId.class);
        assertEquals(IdType.AUTO, tableId.type());

        // Then - CreatedAt field
        assertTrue(createdAtField.isAnnotationPresent(TableField.class));
        TableField createdAt = createdAtField.getAnnotation(TableField.class);
        assertEquals(FieldFill.INSERT, createdAt.fill());

        // Then - UpdatedAt field
        assertTrue(updatedAtField.isAnnotationPresent(TableField.class));
        TableField updatedAt = updatedAtField.getAnnotation(TableField.class);
        assertEquals(FieldFill.INSERT_UPDATE, updatedAt.fill());

        // Then - Deleted field
        assertTrue(deletedField.isAnnotationPresent(TableLogic.class));
    }

    @Test
    @DisplayName("UserStory 实体 TableName 注解验证")
    void userStory_TableNameAnnotation() {
        // Given
        TableName tableName = UserStory.class.getAnnotation(TableName.class);

        // Then
        assertNotNull(tableName);
        assertEquals("user_stories", tableName.value());
    }

    @Test
    @DisplayName("UserStory 实体 Lombok Data 注解验证")
    void userStory_LombokDataAnnotation() {
        // Given & When - Using setter
        UserStory story = new UserStory();
        story.setTitle("测试故事");

        // Then - Using getter (Lombok generated)
        assertEquals("测试故事", story.getTitle());
    }

    @Test
    @DisplayName("UserStory 实体默认值测试")
    void userStory_DefaultValues() {
        // Given
        UserStory story = new UserStory();

        // Then - Default values should be null for objects
        assertNull(story.getId());
        assertNull(story.getStoryKey());
        assertNull(story.getTitle());
        assertNull(story.getDescription());
        assertNull(story.getAcceptanceCriteria());
        assertNull(story.getEpicId());
        assertNull(story.getProjectId());
        assertNull(story.getAssigneeId());
        assertNull(story.getStatus());
        assertNull(story.getPriority());
        assertNull(story.getStoryPoints());
        assertNull(story.getCreatorId());
        assertNull(story.getCreatedAt());
        assertNull(story.getUpdatedAt());
        assertNull(story.getDeleted());
    }

    @Test
    @DisplayName("UserStory 实体 Story Points 边界值测试")
    void userStory_StoryPointsBoundaryValues() {
        // Given
        UserStory story = new UserStory();

        // When & Then - Zero story points
        story.setStoryPoints(0);
        assertEquals(0, story.getStoryPoints());

        // When & Then - Large story points
        story.setStoryPoints(100);
        assertEquals(100, story.getStoryPoints());

        // When & Then - Negative story points (technically allowed by entity)
        story.setStoryPoints(-1);
        assertEquals(-1, story.getStoryPoints());
    }

    @Test
    @DisplayName("UserStory 实体状态值测试")
    void userStory_StatusValues() {
        // Given
        UserStory story = new UserStory();

        // When & Then - Various status values
        story.setStatus("OPEN");
        assertEquals("OPEN", story.getStatus());

        story.setStatus("IN_PROGRESS");
        assertEquals("IN_PROGRESS", story.getStatus());

        story.setStatus("DONE");
        assertEquals("DONE", story.getStatus());

        story.setStatus("COMPLETED");
        assertEquals("COMPLETED", story.getStatus());
    }

    @Test
    @DisplayName("UserStory 实体优先级值测试")
    void userStory_PriorityValues() {
        // Given
        UserStory story = new UserStory();

        // When & Then - Various priority values
        story.setPriority("LOW");
        assertEquals("LOW", story.getPriority());

        story.setPriority("MEDIUM");
        assertEquals("MEDIUM", story.getPriority());

        story.setPriority("HIGH");
        assertEquals("HIGH", story.getPriority());

        story.setPriority("CRITICAL");
        assertEquals("CRITICAL", story.getPriority());
    }

    @Test
    @DisplayName("UserStory 实体 equals 和 hashCode 测试")
    void userStory_EqualsAndHashCode() {
        // Given
        UserStory story1 = new UserStory();
        story1.setId(1L);
        story1.setTitle("故事 1");

        UserStory story2 = new UserStory();
        story2.setId(1L);
        story2.setTitle("故事 1");

        UserStory story3 = new UserStory();
        story3.setId(2L);
        story3.setTitle("故事 2");

        // Then
        assertEquals(story1.hashCode(), story2.hashCode());
        assertNotEquals(story1.hashCode(), story3.hashCode());
    }

    @Test
    @DisplayName("UserStory 实体 toString 测试")
    void userStory_ToString() {
        // Given
        UserStory story = new UserStory();
        story.setId(1L);
        story.setTitle("测试用户故事");
        story.setStoryKey("STORY-1-ABC123");

        // When
        String result = story.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("1"));
        assertTrue(result.contains("测试用户故事"));
        assertTrue(result.contains("STORY-1-ABC123"));
    }

    @Test
    @DisplayName("UserStory 实体逻辑删除字段测试")
    void userStory_LogicDelete() {
        // Given
        UserStory activeStory = new UserStory();
        activeStory.setId(1L);
        activeStory.setDeleted(0); // Not deleted

        UserStory deletedStory = new UserStory();
        deletedStory.setId(2L);
        deletedStory.setDeleted(1); // Deleted

        // Then
        assertEquals(0, activeStory.getDeleted());
        assertEquals(1, deletedStory.getDeleted());
    }

    @Test
    @DisplayName("UserStory 实体 StoryKey 格式测试")
    void userStory_StoryKeyFormat() {
        // Given
        UserStory story = new UserStory();
        story.setStoryKey("STORY-1-ABC123");

        // Then - Verify format: STORY-{projectId}-{uuid}
        String storyKey = story.getStoryKey();
        assertNotNull(storyKey);
        assertTrue(storyKey.startsWith("STORY-"));
        String[] parts = storyKey.split("-");
        assertEquals(3, parts.length);
    }
}
