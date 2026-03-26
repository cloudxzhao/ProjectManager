package com.projecthub.story.entity;

import com.baomidou.mybatisplus.annotation.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Epic Entity 单元测试
 */
@DisplayName("Epic Entity 单元测试")
class EpicTest {

    @Test
    @DisplayName("创建 Epic 实体")
    void createEpic_Success() {
        // Given & When
        Epic epic = new Epic();
        epic.setId(1L);
        epic.setName("测试史诗");
        epic.setDescription("这是一个测试史诗");
        epic.setProjectId(1L);
        epic.setStatus("OPEN");
        epic.setPriority(1);
        epic.setStartDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        epic.setEndDate(LocalDateTime.of(2024, 12, 31, 23, 59));
        epic.setCreatorId(1L);
        epic.setCreatedAt(LocalDateTime.now());
        epic.setUpdatedAt(LocalDateTime.now());
        epic.setDeleted(0);

        // Then
        assertNotNull(epic);
        assertEquals(1L, epic.getId());
        assertEquals("测试史诗", epic.getName());
        assertEquals("这是一个测试史诗", epic.getDescription());
        assertEquals(1L, epic.getProjectId());
        assertEquals("OPEN", epic.getStatus());
        assertEquals(1, epic.getPriority());
        assertEquals(1L, epic.getCreatorId());
        assertEquals(0, epic.getDeleted());
    }

    @Test
    @DisplayName("Epic 实体字段注解验证")
    void epic_EntityAnnotations() throws NoSuchFieldException {
        // Given
        Field idField = Epic.class.getDeclaredField("id");
        Field createdAtField = Epic.class.getDeclaredField("createdAt");
        Field updatedAtField = Epic.class.getDeclaredField("updatedAt");
        Field deletedField = Epic.class.getDeclaredField("deleted");

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
    @DisplayName("Epic 实体 TableName 注解验证")
    void epic_TableNameAnnotation() {
        // Given
        TableName tableName = Epic.class.getAnnotation(TableName.class);

        // Then
        assertNotNull(tableName);
        assertEquals("epics", tableName.value());
    }

    @Test
    @DisplayName("Epic 实体 Lombok Data 注解验证")
    void epic_LombokDataAnnotation() {
        // Given & When - Using setter
        Epic epic = new Epic();
        epic.setName("测试");

        // Then - Using getter (Lombok generated)
        assertEquals("测试", epic.getName());
    }

    @Test
    @DisplayName("Epic 实体 equals 和 hashCode 测试")
    void epic_EqualsAndHashCode() {
        // Given
        Epic epic1 = new Epic();
        epic1.setId(1L);
        epic1.setName("史诗 1");

        Epic epic2 = new Epic();
        epic2.setId(1L);
        epic2.setName("史诗 1");

        Epic epic3 = new Epic();
        epic3.setId(2L);
        epic3.setName("史诗 2");

        // Then
        assertEquals(epic1.hashCode(), epic2.hashCode());
        assertNotEquals(epic1.hashCode(), epic3.hashCode());
    }

    @Test
    @DisplayName("Epic 实体 toString 测试")
    void epic_ToString() {
        // Given
        Epic epic = new Epic();
        epic.setId(1L);
        epic.setName("测试史诗");

        // When
        String result = epic.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("1"));
        assertTrue(result.contains("测试史诗"));
    }
}
