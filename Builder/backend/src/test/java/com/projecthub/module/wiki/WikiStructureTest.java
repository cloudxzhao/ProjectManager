package com.projecthub.module.wiki;

import static org.junit.jupiter.api.Assertions.*;

import com.projecthub.module.project.entity.Project;
import com.projecthub.module.project.entity.ProjectMember;
import com.projecthub.module.project.entity.ProjectMember.ProjectMemberRole;
import com.projecthub.module.project.repository.ProjectMemberRepository;
import com.projecthub.module.project.repository.ProjectRepository;
import com.projecthub.module.wiki.dto.WikiVO;
import com.projecthub.module.wiki.entity.WikiDocument;
import com.projecthub.module.wiki.repository.WikiRepository;
import com.projecthub.module.wiki.service.WikiService;
import com.projecthub.security.UserDetailsImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Wiki 模块结构文档入参测试类 严格按照结构文档的入参格式进行测试
 *
 * <p>入参结构: - CreateRequest: { parentId?, title, content? } - UpdateRequest: { title?, content? } -
 * WikiVO: { id, projectId, parentId, title, content, position, createdAt, updatedAt, children }
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Wiki 模块结构文档入参测试")
class WikiStructureTest {

  @Autowired private WikiService wikiService;

  @Autowired private WikiRepository wikiRepository;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private ProjectMemberRepository projectMemberRepository;

  private Project testProject;
  private UserDetailsImpl testUser;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();

    // 创建测试用户
    testUser =
        new UserDetailsImpl(
            1L, "testuser", "test@example.com", "password", null, true, true, true, true);

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // 创建测试项目
    testProject = new Project();
    testProject.setName("结构测试项目");
    testProject.setDescription("用于结构文档入参测试的项目");
    testProject.setOwnerId(1L);
    testProject.setStartDate(LocalDate.now());
    testProject.setEndDate(LocalDate.now().plusMonths(6));
    testProject = projectRepository.save(testProject);

    // 添加项目成员权限
    ProjectMember member = new ProjectMember();
    member.setProjectId(testProject.getId());
    member.setUserId(1L);
    member.setRole(ProjectMemberRole.OWNER);
    projectMemberRepository.save(member);
  }

  // ==================== CreateRequest 入参测试 ====================

  @Nested
  @DisplayName("CreateRequest 入参测试")
  class CreateRequestTest {

    @Test
    @DisplayName("1. 最小入参 - 只有标题")
    void createRequest_MinimalParams() {
      // Given - 只有必填参数 title
      WikiVO.CreateRequest request = WikiVO.CreateRequest.builder().title("只有标题").build();

      // When
      WikiVO result = wikiService.createDocument(testProject.getId(), request);

      // Then
      assertNotNull(result);
      assertNotNull(result.getId());
      assertEquals("只有标题", result.getTitle());
      assertNull(result.getContent());
      assertNull(result.getParentId());
    }

    @Test
    @DisplayName("2. 完整入参 - 标题 + 内容 + 父 ID")
    void createRequest_FullParams() {
      // Given - 创建父文档
      WikiVO parent =
          wikiService.createDocument(
              testProject.getId(),
              WikiVO.CreateRequest.builder().title("父文档").content("父内容").build());

      // 完整参数的子文档请求
      WikiVO.CreateRequest request =
          WikiVO.CreateRequest.builder()
              .parentId(parent.getId())
              .title("完整参数文档")
              .content("这是完整的内容，包含多种字符：中文、English、12345、!@#$%")
              .build();

      // When
      WikiVO result = wikiService.createDocument(testProject.getId(), request);

      // Then
      assertNotNull(result);
      assertEquals("完整参数文档", result.getTitle());
      assertEquals("这是完整的内容，包含多种字符：中文、English、12345、!@#$%", result.getContent());
      assertEquals(parent.getId(), result.getParentId());
    }

    @Test
    @DisplayName("3. 标题边界值 - 1 字符")
    void createRequest_Title_MinLength() {
      // Given
      WikiVO.CreateRequest request = WikiVO.CreateRequest.builder().title("一").build();

      // When
      WikiVO result = wikiService.createDocument(testProject.getId(), request);

      // Then
      assertEquals("一", result.getTitle());
    }

    @Test
    @DisplayName("4. 标题边界值 - 200 字符")
    void createRequest_Title_MaxLength() {
      // Given
      String maxTitle = "a".repeat(200);
      WikiVO.CreateRequest request = WikiVO.CreateRequest.builder().title(maxTitle).build();

      // When
      WikiVO result = wikiService.createDocument(testProject.getId(), request);

      // Then
      assertEquals(200, result.getTitle().length());
    }

    @Test
    @DisplayName("5. 标题边界值 - 201 字符 (应失败)")
    void createRequest_Title_ExceedMaxLength() {
      // Given
      String tooLongTitle = "a".repeat(201);
      WikiVO.CreateRequest request = WikiVO.CreateRequest.builder().title(tooLongTitle).build();

      // When & Then
      assertThrows(
          Exception.class,
          () -> {
            wikiService.createDocument(testProject.getId(), request);
          });
    }

    @Test
    @DisplayName("6. 内容为空字符串")
    void createRequest_EmptyContent() {
      // Given
      WikiVO.CreateRequest request =
          WikiVO.CreateRequest.builder().title("测试文档").content("").build();

      // When
      WikiVO result = wikiService.createDocument(testProject.getId(), request);

      // Then
      assertEquals("", result.getContent());
    }

    @Test
    @DisplayName("7. 内容为长文本 (10000 字符)")
    void createRequest_LongContent() {
      // Given
      String longContent = "内容行\n".repeat(1250); // 约 10000 字符
      WikiVO.CreateRequest request =
          WikiVO.CreateRequest.builder().title("长文档测试").content(longContent).build();

      // When
      WikiVO result = wikiService.createDocument(testProject.getId(), request);

      // Then
      assertNotNull(result);
      assertEquals(longContent, result.getContent());
    }

    @Test
    @DisplayName("8. 内容包含特殊字符和 HTML")
    void createRequest_SpecialCharacters() {
      // Given
      String specialContent =
          "<p>HTML 内容</p>\n<script>alert('test')</script>\n"
              + "特殊字符：& < > \" '\n"
              + "Emoji: 😀😁😂\n"
              + "多语言：Hello 世界こんにちは";

      WikiVO.CreateRequest request =
          WikiVO.CreateRequest.builder().title("特殊字符测试").content(specialContent).build();

      // When
      WikiVO result = wikiService.createDocument(testProject.getId(), request);

      // Then
      assertEquals(specialContent, result.getContent());
    }

    @Test
    @DisplayName("9. 父文档 ID 为 null (创建根文档)")
    void createRequest_NullParentId() {
      // Given
      WikiVO.CreateRequest request =
          WikiVO.CreateRequest.builder()
              .parentId(null) // 显式设置为 null
              .title("根文档")
              .content("根内容")
              .build();

      // When
      WikiVO result = wikiService.createDocument(testProject.getId(), request);

      // Then
      assertNull(result.getParentId());

      // 验证是根文档
      List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());
      assertTrue(tree.stream().anyMatch(d -> d.getId().equals(result.getId())));
    }

    @Test
    @DisplayName("10. 创建多层级子文档")
    void createRequest_NestedChildren() {
      // Given - 创建曾祖父文档
      WikiVO greatGrandparent =
          wikiService.createDocument(
              testProject.getId(), WikiVO.CreateRequest.builder().title("曾祖父").build());

      // 创建祖父文档
      WikiVO grandparent =
          wikiService.createDocument(
              testProject.getId(),
              WikiVO.CreateRequest.builder()
                  .title("祖父")
                  .parentId(greatGrandparent.getId())
                  .build());

      // 创建父亲文档
      WikiVO parent =
          wikiService.createDocument(
              testProject.getId(),
              WikiVO.CreateRequest.builder().title("父亲").parentId(grandparent.getId()).build());

      // When - 创建儿子文档
      WikiVO.CreateRequest request =
          WikiVO.CreateRequest.builder().title("儿子").parentId(parent.getId()).build();
      WikiVO child = wikiService.createDocument(testProject.getId(), request);

      // Then - 验证层级关系
      assertNotNull(child);
      assertEquals(parent.getId(), child.getParentId());

      // 验证树形结构
      List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());
      assertEquals(1, tree.size()); // 只有曾祖父是根节点
      assertEquals("曾祖父", tree.get(0).getTitle());
    }
  }

  // ==================== UpdateRequest 入参测试 ====================

  @Nested
  @DisplayName("UpdateRequest 入参测试")
  class UpdateRequestTest {

    @Test
    @DisplayName("11. 只更新标题")
    void updateRequest_TitleOnly() {
      // Given
      WikiDocument doc = createTestDocument("原标题", "原内容", null);
      WikiVO.UpdateRequest request = WikiVO.UpdateRequest.builder().title("新标题").build();

      // When
      WikiVO result = wikiService.updateDocument(doc.getId(), request);

      // Then
      assertEquals("新标题", result.getTitle());
      assertEquals("原内容", result.getContent());
    }

    @Test
    @DisplayName("12. 只更新内容")
    void updateRequest_ContentOnly() {
      // Given
      WikiDocument doc = createTestDocument("原标题", "原内容", null);
      WikiVO.UpdateRequest request = WikiVO.UpdateRequest.builder().content("新内容").build();

      // When
      WikiVO result = wikiService.updateDocument(doc.getId(), request);

      // Then
      assertEquals("原标题", result.getTitle());
      assertEquals("新内容", result.getContent());
    }

    @Test
    @DisplayName("13. 同时更新标题和内容")
    void updateRequest_BothTitleAndContent() {
      // Given
      WikiDocument doc = createTestDocument("原标题", "原内容", null);
      WikiVO.UpdateRequest request =
          WikiVO.UpdateRequest.builder().title("新标题").content("新内容").build();

      // When
      WikiVO result = wikiService.updateDocument(doc.getId(), request);

      // Then
      assertEquals("新标题", result.getTitle());
      assertEquals("新内容", result.getContent());
    }

    @Test
    @DisplayName("14. 更新为空标题 (应失败)")
    void updateRequest_EmptyTitle() {
      // Given
      WikiDocument doc = createTestDocument("原标题", "原内容", null);
      WikiVO.UpdateRequest request = WikiVO.UpdateRequest.builder().title("").build();

      // When & Then
      assertThrows(
          Exception.class,
          () -> {
            wikiService.updateDocument(doc.getId(), request);
          });
    }

    @Test
    @DisplayName("15. 更新为过长标题 (应失败)")
    void updateRequest_TooLongTitle() {
      // Given
      WikiDocument doc = createTestDocument("原标题", "原内容", null);
      WikiVO.UpdateRequest request = WikiVO.UpdateRequest.builder().title("a".repeat(201)).build();

      // When & Then
      assertThrows(
          Exception.class,
          () -> {
            wikiService.updateDocument(doc.getId(), request);
          });
    }

    @Test
    @DisplayName("16. 更新内容为空字符串")
    void updateRequest_EmptyContent() {
      // Given
      WikiDocument doc = createTestDocument("原标题", "原内容", null);
      WikiVO.UpdateRequest request = WikiVO.UpdateRequest.builder().content("").build();

      // When
      WikiVO result = wikiService.updateDocument(doc.getId(), request);

      // Then
      assertEquals("", result.getContent());
    }

    @Test
    @DisplayName("17. 更新内容为 null")
    void updateRequest_NullContent() {
      // Given
      WikiDocument doc = createTestDocument("原标题", "原内容", null);
      WikiVO.UpdateRequest request = WikiVO.UpdateRequest.builder().content(null).build();

      // When
      WikiVO result = wikiService.updateDocument(doc.getId(), request);

      // Then
      // 内容应该不变或者变为 null (取决于实现)
      assertEquals("原标题", result.getTitle());
    }

    @Test
    @DisplayName("18. 空更新请求 (不更新任何字段)")
    void updateRequest_EmptyRequest() {
      // Given
      WikiDocument doc = createTestDocument("原标题", "原内容", null);
      WikiVO.UpdateRequest request = new WikiVO.UpdateRequest();

      // When
      WikiVO result = wikiService.updateDocument(doc.getId(), request);

      // Then
      assertEquals("原标题", result.getTitle());
      assertEquals("原内容", result.getContent());
    }

    @Test
    @DisplayName("19. 更新不存在的文档 (应失败)")
    void updateRequest_NonExistentDocument() {
      // Given
      WikiVO.UpdateRequest request = WikiVO.UpdateRequest.builder().title("新标题").build();

      // When & Then
      assertThrows(
          Exception.class,
          () -> {
            wikiService.updateDocument(99999L, request);
          });
    }
  }

  // ==================== WikiVO 返回结构测试 ====================

  @Nested
  @DisplayName("WikiVO 返回结构测试")
  class WikiVOResponseTest {

    @Test
    @DisplayName("20. 验证返回的 WikiVO 包含所有必需字段")
    void wikiVO_AllRequiredFields() {
      // Given
      WikiVO.CreateRequest request =
          WikiVO.CreateRequest.builder().title("测试文档").content("测试内容").build();

      // When
      WikiVO result = wikiService.createDocument(testProject.getId(), request);

      // Then
      assertNotNull(result.getId(), "ID 不应为 null");
      assertNotNull(result.getProjectId(), "projectId 不应为 null");
      assertNotNull(result.getTitle(), "title 不应为 null");
      assertNotNull(result.getCreatedAt(), "createdAt 不应为 null");
      assertEquals("测试文档", result.getTitle());
      assertEquals("测试内容", result.getContent());
      assertEquals(testProject.getId(), result.getProjectId());
    }

    @Test
    @DisplayName("21. 验证返回的 WikiVO 中可选字段")
    void wikiVO_OptionalFields() {
      // Given
      WikiVO.CreateRequest request = WikiVO.CreateRequest.builder().title("测试文档").build();

      // When
      WikiVO result = wikiService.createDocument(testProject.getId(), request);

      // Then - 可选字段可以为 null
      assertNull(result.getParentId());
      assertNull(result.getContent());
      assertNull(result.getUpdatedAt()); // 初次创建时 updatedAt 可能为 null
      assertNull(result.getChildren()); // 根节点的 children 可能为 null 或空列表
    }

    @Test
    @DisplayName("22. 验证树形结构返回的 children")
    void wikiVO_ChildrenInTree() {
      // Given
      WikiVO root =
          wikiService.createDocument(
              testProject.getId(), WikiVO.CreateRequest.builder().title("根").build());
      wikiService.createDocument(
          testProject.getId(),
          WikiVO.CreateRequest.builder().title("子 1").parentId(root.getId()).build());
      wikiService.createDocument(
          testProject.getId(),
          WikiVO.CreateRequest.builder().title("子 2").parentId(root.getId()).build());

      // When
      List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());

      // Then
      assertEquals(1, tree.size());
      assertNotNull(tree.get(0).getChildren());
      assertEquals(2, tree.get(0).getChildren().size());
    }

    @Test
    @DisplayName("23. 验证 position 字段")
    void wikiVO_PositionField() {
      // Given
      WikiVO.CreateRequest request = WikiVO.CreateRequest.builder().title("测试文档").build();

      // When
      WikiVO result = wikiService.createDocument(testProject.getId(), request);

      // Then
      assertNotNull(result.getPosition());
      assertEquals(0, result.getPosition()); // 默认 position 为 0
    }

    @Test
    @DisplayName("24. 验证 timestamps")
    void wikiVO_Timestamps() {
      // Given
      LocalDateTime beforeCreate = LocalDateTime.now();

      WikiVO.CreateRequest request = WikiVO.CreateRequest.builder().title("测试文档").build();
      WikiVO result = wikiService.createDocument(testProject.getId(), request);

      LocalDateTime afterCreate = LocalDateTime.now();

      // Then
      assertNotNull(result.getCreatedAt());
      assertTrue(result.getCreatedAt().isAfter(beforeCreate.minusSeconds(5)));
      assertTrue(result.getCreatedAt().isBefore(afterCreate.plusSeconds(5)));
    }

    @Test
    @DisplayName("25. 验证更新后 updatedAt 变化")
    void wikiVO_UpdatedAtChanges() {
      // Given
      WikiVO.CreateRequest createRequest = WikiVO.CreateRequest.builder().title("原文档").build();
      WikiVO created = wikiService.createDocument(testProject.getId(), createRequest);
      LocalDateTime createdAt = created.getCreatedAt();
      LocalDateTime updatedAtBefore = created.getUpdatedAt();

      // Wait a little to ensure time difference
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      // When
      WikiVO.UpdateRequest updateRequest = WikiVO.UpdateRequest.builder().title("新标题").build();
      WikiVO updated = wikiService.updateDocument(created.getId(), updateRequest);

      // Then
      assertEquals(createdAt, updated.getCreatedAt()); // createdAt 不变
      assertNotNull(updated.getUpdatedAt()); // updatedAt 被设置
      if (updatedAtBefore != null) {
        assertTrue(updated.getUpdatedAt().isAfter(updatedAtBefore));
      }
    }
  }

  // ==================== 复杂树形结构入参测试 ====================

  @Nested
  @DisplayName("复杂树形结构入参测试")
  class ComplexTreeStructureTest {

    @Test
    @DisplayName("26. 创建 100 条文档 - 单层结构")
    void complexTree_Flat100Documents() {
      // Given & When
      for (int i = 1; i <= 100; i++) {
        final int index = i;
        WikiVO.CreateRequest request =
            WikiVO.CreateRequest.builder().title("文档-" + index).content("内容" + index).build();
        wikiService.createDocument(testProject.getId(), request);
      }

      // Then
      List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());
      assertEquals(100, tree.size());
    }

    @Test
    @DisplayName("27. 创建 100 条文档 - 两层树形结构")
    void complexTree_TwoLevel100Documents() {
      // Given & When - 创建 10 个根节点，每个根节点 9 个子节点
      for (int i = 1; i <= 10; i++) {
        WikiVO root =
            wikiService.createDocument(
                testProject.getId(),
                WikiVO.CreateRequest.builder().title("根节点-" + i).content("根内容" + i).build());

        for (int j = 1; j <= 9; j++) {
          wikiService.createDocument(
              testProject.getId(),
              WikiVO.CreateRequest.builder()
                  .title("子节点-" + i + "-" + j)
                  .content("子内容" + i + "-" + j)
                  .parentId(root.getId())
                  .build());
        }
      }

      // Then
      List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());
      assertEquals(10, tree.size());
      for (WikiVO root : tree) {
        assertEquals(9, root.getChildren().size());
      }
    }

    @Test
    @DisplayName("28. 创建 100 条文档 - 三层树形结构")
    void complexTree_ThreeLevel100Documents() {
      // Given & When
      // 5 个根节点
      for (int i = 1; i <= 5; i++) {
        WikiVO root =
            wikiService.createDocument(
                testProject.getId(), WikiVO.CreateRequest.builder().title("L1-" + i).build());

        // 每个根节点 3 个二级子节点 (5 * 3 = 15)
        for (int j = 1; j <= 3; j++) {
          WikiVO level2 =
              wikiService.createDocument(
                  testProject.getId(),
                  WikiVO.CreateRequest.builder()
                      .title("L2-" + i + "-" + j)
                      .parentId(root.getId())
                      .build());

          // 每个二级节点 5 个三级子节点 (15 * 5 = 75)
          for (int k = 1; k <= 5; k++) {
            wikiService.createDocument(
                testProject.getId(),
                WikiVO.CreateRequest.builder()
                    .title("L3-" + i + "-" + j + "-" + k)
                    .parentId(level2.getId())
                    .build());
          }
        }
      }

      // 再加 5 个独立文档 (5 + 15 + 75 + 5 = 100)
      for (int i = 1; i <= 5; i++) {
        wikiService.createDocument(
            testProject.getId(), WikiVO.CreateRequest.builder().title("独立-" + i).build());
      }

      // Then
      List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());
      assertEquals(10, tree.size()); // 5 个根节点 + 5 个独立文档
    }

    @Test
    @DisplayName("29. 树形结构 - 深度优先创建")
    void complexTree_DepthFirstCreation() {
      // Given & When - 创建一条深度为 10 的链
      Long parentId = null;
      for (int i = 1; i <= 10; i++) {
        final Long currentParentId = parentId;
        WikiVO.CreateRequest request =
            WikiVO.CreateRequest.builder()
                .title("第" + i + "层")
                .content("第" + i + "层内容")
                .parentId(currentParentId)
                .build();
        WikiVO doc = wikiService.createDocument(testProject.getId(), request);
        parentId = doc.getId();
      }

      // Then
      List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());
      assertEquals(1, tree.size());

      // 验证深度
      WikiVO current = tree.get(0);
      for (int i = 2; i <= 10; i++) {
        assertNotNull(current.getChildren());
        assertEquals(1, current.getChildren().size());
        current = current.getChildren().get(0);
        assertEquals("第" + i + "层", current.getTitle());
      }
    }

    @Test
    @DisplayName("30. 树形结构 - 广度优先创建")
    void complexTree_BreadthFirstCreation() {
      // Given - 第一层 2 个节点
      WikiVO root1 =
          wikiService.createDocument(
              testProject.getId(), WikiVO.CreateRequest.builder().title("根 1").build());
      WikiVO root2 =
          wikiService.createDocument(
              testProject.getId(), WikiVO.CreateRequest.builder().title("根 2").build());

      // 第二层 4 个节点 (每个根节点 2 个子节点)
      WikiVO level2_1 =
          wikiService.createDocument(
              testProject.getId(),
              WikiVO.CreateRequest.builder().title("L2-1").parentId(root1.getId()).build());
      wikiService.createDocument(
          testProject.getId(),
          WikiVO.CreateRequest.builder().title("L2-2").parentId(root1.getId()).build());
      WikiVO level2_3 =
          wikiService.createDocument(
              testProject.getId(),
              WikiVO.CreateRequest.builder().title("L2-3").parentId(root2.getId()).build());
      wikiService.createDocument(
          testProject.getId(),
          WikiVO.CreateRequest.builder().title("L2-4").parentId(root2.getId()).build());

      // 第三层 8 个节点 (每个二级节点 2 个子节点)
      for (int i = 1; i <= 2; i++) {
        final Long pid = level2_1.getId();
        wikiService.createDocument(
            testProject.getId(),
            WikiVO.CreateRequest.builder().title("L3-1-" + i).parentId(pid).build());
      }
      for (int i = 1; i <= 2; i++) {
        final Long pid = level2_3.getId();
        wikiService.createDocument(
            testProject.getId(),
            WikiVO.CreateRequest.builder().title("L3-3-" + i).parentId(pid).build());
      }

      // Then
      List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());
      assertEquals(2, tree.size());
      assertEquals(2, tree.get(0).getChildren().size());
      assertEquals(2, tree.get(1).getChildren().size());
    }
  }

  // ==================== 辅助方法 ====================

  private WikiDocument createTestDocument(String title, String content, Long parentId) {
    WikiDocument doc =
        WikiDocument.builder()
            .projectId(testProject.getId())
            .parentId(parentId)
            .title(title)
            .content(content)
            .position(0)
            .build();
    return wikiRepository.save(doc);
  }
}
