package com.projecthub.module.wiki;

import static org.junit.jupiter.api.Assertions.*;

import com.projecthub.common.exception.BusinessException;
import com.projecthub.module.project.entity.Project;
import com.projecthub.module.project.entity.ProjectMember;
import com.projecthub.module.project.entity.ProjectMember.ProjectMemberRole;
import com.projecthub.module.project.repository.ProjectMemberRepository;
import com.projecthub.module.project.repository.ProjectRepository;
import com.projecthub.module.wiki.dto.WikiVO;
import com.projecthub.module.wiki.entity.WikiDocument;
import com.projecthub.module.wiki.entity.WikiHistory;
import com.projecthub.module.wiki.repository.WikiHistoryRepository;
import com.projecthub.module.wiki.repository.WikiRepository;
import com.projecthub.module.wiki.service.WikiService;
import com.projecthub.security.UserDetailsImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** Wiki 服务集成测试类 包含基础 CRUD 测试、树形结构测试和压力测试 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Wiki 服务测试")
class WikiServiceTest {

  @Autowired private WikiService wikiService;

  @Autowired private WikiRepository wikiRepository;

  @Autowired private WikiHistoryRepository wikiHistoryRepository;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private ProjectMemberRepository projectMemberRepository;

  private Project testProject;
  private UserDetailsImpl testUser;
  private Long testUserId;

  @BeforeEach
  void setUp() {
    // 清理安全上下文
    SecurityContextHolder.clearContext();

    // 创建测试用户
    testUserId = 1L;
    testUser =
        new UserDetailsImpl(
            testUserId, "testuser", "test@example.com", "password", null, true, true, true, true);

    // 设置安全上下文
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // 创建测试项目
    testProject = new Project();
    testProject.setName("Wiki 测试项目");
    testProject.setDescription("用于 Wiki 测试的项目");
    testProject.setOwnerId(testUserId);
    testProject.setStartDate(LocalDateTime.now().toLocalDate());
    testProject.setEndDate(LocalDateTime.now().plusMonths(6).toLocalDate());
    testProject = projectRepository.save(testProject);

    // 添加项目成员权限
    ProjectMember member = new ProjectMember();
    member.setProjectId(testProject.getId());
    member.setUserId(testUserId);
    member.setRole(ProjectMemberRole.OWNER);
    projectMemberRepository.save(member);
  }

  // ==================== 基础 CRUD 测试 ====================

  @Test
  @DisplayName("1.1 创建 Wiki 文档 - 成功")
  void createDocument_Success() {
    // Given
    WikiVO.CreateRequest request =
        WikiVO.CreateRequest.builder().title("测试文档").content("这是测试文档内容").parentId(null).build();

    // When
    WikiVO result = wikiService.createDocument(testProject.getId(), request);

    // Then
    assertNotNull(result);
    assertNotNull(result.getId());
    assertEquals("测试文档", result.getTitle());
    assertEquals("这是测试文档内容", result.getContent());
    assertEquals(testProject.getId(), result.getProjectId());
    assertNull(result.getParentId());

    // 验证数据库中存在
    assertTrue(wikiRepository.existsById(result.getId()));
  }

  @Test
  @DisplayName("1.2 创建 Wiki 文档 - 父文档不存在")
  void createDocument_ParentNotFound() {
    // Given
    WikiVO.CreateRequest request =
        WikiVO.CreateRequest.builder()
            .title("子文档")
            .content("子文档内容")
            .parentId(99999L) // 不存在的父文档 ID
            .build();

    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              wikiService.createDocument(testProject.getId(), request);
            });
    assertEquals("父文档不存在", exception.getMessage());
  }

  @Test
  @DisplayName("1.3 创建 Wiki 文档 - 项目不存在")
  void createDocument_ProjectNotFound() {
    // Given
    WikiVO.CreateRequest request =
        WikiVO.CreateRequest.builder().title("测试文档").content("内容").build();

    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              wikiService.createDocument(99999L, request);
            });
    assertEquals("项目不存在", exception.getMessage());
  }

  @Test
  @DisplayName("1.4 获取 Wiki 文档详情 - 成功")
  void getDocument_Success() {
    // Given
    WikiDocument document = createTestDocument("测试文档", "内容", null);

    // When
    WikiVO result = wikiService.getDocument(document.getId());

    // Then
    assertNotNull(result);
    assertEquals(document.getId(), result.getId());
    assertEquals("测试文档", result.getTitle());
    assertEquals("内容", result.getContent());
  }

  @Test
  @DisplayName("1.5 获取 Wiki 文档详情 - 文档不存在")
  void getDocument_NotFound() {
    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              wikiService.getDocument(99999L);
            });
    assertEquals("文档不存在", exception.getMessage());
  }

  @Test
  @DisplayName("1.6 更新 Wiki 文档 - 成功")
  void updateDocument_Success() {
    // Given
    WikiDocument document = createTestDocument("原文档", "原文内容", null);

    WikiVO.UpdateRequest request =
        WikiVO.UpdateRequest.builder().title("更新后的标题").content("更新后的内容").build();

    // When
    WikiVO result = wikiService.updateDocument(document.getId(), request);

    // Then
    assertEquals("更新后的标题", result.getTitle());
    assertEquals("更新后的内容", result.getContent());

    // 验证历史记录被保存
    List<WikiHistory> history =
        wikiHistoryRepository.findByDocumentIdOrderByCreatedAtDesc(document.getId());
    assertFalse(history.isEmpty());
    assertEquals("原文内容", history.get(0).getContent());
  }

  @Test
  @DisplayName("1.7 更新 Wiki 文档 - 只更新标题")
  void updateDocument_TitleOnly() {
    // Given
    WikiDocument document = createTestDocument("原文档", "原文内容", null);

    WikiVO.UpdateRequest request = WikiVO.UpdateRequest.builder().title("新标题").build();

    // When
    WikiVO result = wikiService.updateDocument(document.getId(), request);

    // Then
    assertEquals("新标题", result.getTitle());
    assertEquals("原文内容", result.getContent());
  }

  @Test
  @DisplayName("1.8 更新 Wiki 文档 - 只更新内容")
  void updateDocument_ContentOnly() {
    // Given
    WikiDocument document = createTestDocument("原文档", "原文内容", null);

    WikiVO.UpdateRequest request = WikiVO.UpdateRequest.builder().content("新内容").build();

    // When
    WikiVO result = wikiService.updateDocument(document.getId(), request);

    // Then
    assertEquals("原文档", result.getTitle());
    assertEquals("新内容", result.getContent());
  }

  @Test
  @DisplayName("1.9 更新 Wiki 文档 - 文档不存在")
  void updateDocument_NotFound() {
    // Given
    WikiVO.UpdateRequest request = WikiVO.UpdateRequest.builder().title("新标题").build();

    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              wikiService.updateDocument(99999L, request);
            });
    assertEquals("文档不存在", exception.getMessage());
  }

  @Test
  @DisplayName("1.10 删除 Wiki 文档 - 成功")
  void deleteDocument_Success() {
    // Given
    WikiDocument document = createTestDocument("待删除文档", "内容", null);
    Long documentId = document.getId();

    // When
    wikiService.deleteDocument(documentId);

    // Then
    // 验证文档被软删除（不存在于查询结果中）
    assertFalse(wikiRepository.findById(documentId).isPresent());
  }

  @Test
  @DisplayName("1.11 删除 Wiki 文档 - 文档不存在")
  void deleteDocument_NotFound() {
    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              wikiService.deleteDocument(99999L);
            });
    assertEquals("文档不存在", exception.getMessage());
  }

  @Test
  @DisplayName("1.12 获取文档树 - 成功")
  void getDocumentTree_Success() {
    // Given
    // 创建根文档
    WikiDocument root1 = createTestDocument("根文档 1", "内容 1", null);
    WikiDocument root2 = createTestDocument("根文档 2", "内容 2", null);

    // 创建子文档
    createTestDocument("子文档 1-1", "内容 1-1", root1.getId());
    createTestDocument("子文档 1-2", "内容 1-2", root1.getId());
    createTestDocument("子文档 2-1", "内容 2-1", root2.getId());

    // When
    List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());

    // Then
    assertEquals(2, tree.size());

    // 验证根文档 1
    WikiVO root1Vo = tree.get(0);
    assertEquals("根文档 1", root1Vo.getTitle());
    assertNotNull(root1Vo.getChildren());
    assertEquals(2, root1Vo.getChildren().size());

    // 验证根文档 2
    WikiVO root2Vo = tree.get(1);
    assertEquals("根文档 2", root2Vo.getTitle());
    assertNotNull(root2Vo.getChildren());
    assertEquals(1, root2Vo.getChildren().size());
  }

  @Test
  @DisplayName("1.13 获取文档树 - 空项目")
  void getDocumentTree_Empty() {
    // When
    List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());

    // Then
    assertTrue(tree.isEmpty());
  }

  @Test
  @DisplayName("1.14 获取文档历史记录 - 成功")
  void getDocumentHistory_Success() {
    // Given
    WikiDocument document = createTestDocument("测试文档", "初始内容", null);

    // 更新两次以创建历史记录
    wikiService.updateDocument(
        document.getId(), WikiVO.UpdateRequest.builder().content("第一次更新").build());
    wikiService.updateDocument(
        document.getId(), WikiVO.UpdateRequest.builder().content("第二次更新").build());

    // When
    List<WikiHistory> history = wikiService.getDocumentHistory(document.getId());

    // Then
    assertFalse(history.isEmpty());
    assertEquals(3, history.size()); // 初始创建 + 2 次更新

    // 验证历史记录顺序（最新的在前）
    assertEquals("第二次更新", history.get(0).getContent());
    assertEquals("第一次更新", history.get(1).getContent());
  }

  @Test
  @DisplayName("1.15 创建带父子关系的文档 - 成功")
  void createDocument_WithParent_Success() {
    // Given
    WikiVO.CreateRequest rootRequest =
        WikiVO.CreateRequest.builder().title("根文档").content("根内容").build();
    WikiVO rootDoc = wikiService.createDocument(testProject.getId(), rootRequest);

    // When
    WikiVO.CreateRequest childRequest =
        WikiVO.CreateRequest.builder()
            .title("子文档")
            .content("子内容")
            .parentId(rootDoc.getId())
            .build();
    WikiVO childDoc = wikiService.createDocument(testProject.getId(), childRequest);

    // Then
    assertNotNull(childDoc);
    assertEquals(rootDoc.getId(), childDoc.getParentId());

    // 验证树形结构
    List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());
    assertEquals(1, tree.size());
    assertEquals(1, tree.get(0).getChildren().size());
    assertEquals("子文档", tree.get(0).getChildren().get(0).getTitle());
  }

  // ==================== 树形结构测试 ====================

  @Test
  @DisplayName("2.1 三层树形结构测试")
  void treeStructure_ThreeLevels() {
    // Given
    // 第一层 - 根文档
    WikiVO root =
        wikiService.createDocument(
            testProject.getId(),
            WikiVO.CreateRequest.builder().title("根节点").content("根内容").build());

    // 第二层 - 两个子节点
    WikiVO child1 =
        wikiService.createDocument(
            testProject.getId(),
            WikiVO.CreateRequest.builder()
                .title("子节点 1")
                .content("内容 1")
                .parentId(root.getId())
                .build());
    WikiVO child2 =
        wikiService.createDocument(
            testProject.getId(),
            WikiVO.CreateRequest.builder()
                .title("子节点 2")
                .content("内容 2")
                .parentId(root.getId())
                .build());

    // 第三层 - 孙节点
    wikiService.createDocument(
        testProject.getId(),
        WikiVO.CreateRequest.builder()
            .title("孙节点 1-1")
            .content("内容 1-1")
            .parentId(child1.getId())
            .build());
    wikiService.createDocument(
        testProject.getId(),
        WikiVO.CreateRequest.builder()
            .title("孙节点 1-2")
            .content("内容 1-2")
            .parentId(child1.getId())
            .build());
    wikiService.createDocument(
        testProject.getId(),
        WikiVO.CreateRequest.builder()
            .title("孙节点 2-1")
            .content("内容 2-1")
            .parentId(child2.getId())
            .build());

    // When
    List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());

    // Then
    assertEquals(1, tree.size());
    WikiVO rootVo = tree.get(0);
    assertEquals("根节点", rootVo.getTitle());
    assertEquals(2, rootVo.getChildren().size());

    // 验证第一层子节点
    WikiVO child1Vo = rootVo.getChildren().get(0);
    assertEquals("子节点 1", child1Vo.getTitle());
    assertEquals(2, child1Vo.getChildren().size());

    WikiVO child2Vo = rootVo.getChildren().get(1);
    assertEquals("子节点 2", child2Vo.getTitle());
    assertEquals(1, child2Vo.getChildren().size());
  }

  @Test
  @DisplayName("2.2 树形结构 - 深度测试 (5 层)")
  void treeStructure_Depth_FiveLevels() {
    // Given
    Long parentId = null;
    for (int i = 1; i <= 5; i++) {
      final Long currentParentId = parentId;
      WikiVO.CreateRequest request =
          WikiVO.CreateRequest.builder()
              .title("第" + i + "层节点")
              .content("第" + i + "层内容")
              .parentId(currentParentId)
              .build();
      WikiVO doc = wikiService.createDocument(testProject.getId(), request);
      parentId = doc.getId();
    }

    // When
    List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());

    // Then
    assertEquals(1, tree.size());
    assertEquals("第 1 层节点", tree.get(0).getTitle());
    assertEquals(1, tree.get(0).getChildren().size());
    assertEquals("第 2 层节点", tree.get(0).getChildren().get(0).getTitle());
    assertEquals(1, tree.get(0).getChildren().get(0).getChildren().size());
  }

  @Test
  @DisplayName("2.3 树形结构 - 宽度测试 (10 个子节点)")
  void treeStructure_Width_TenChildren() {
    // Given
    WikiVO root =
        wikiService.createDocument(
            testProject.getId(),
            WikiVO.CreateRequest.builder().title("根节点").content("根内容").build());

    for (int i = 1; i <= 10; i++) {
      final int index = i;
      wikiService.createDocument(
          testProject.getId(),
          WikiVO.CreateRequest.builder()
              .title("子节点" + index)
              .content("内容" + index)
              .parentId(root.getId())
              .build());
    }

    // When
    List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());

    // Then
    assertEquals(1, tree.size());
    assertEquals(10, tree.get(0).getChildren().size());
  }

  // ==================== 100 条压力测试 ====================

  @Test
  @DisplayName("3.1 压力测试 - 创建 100 条扁平文档")
  void stressTest_Create100FlatDocuments() {
    // Given
    int documentCount = 100;

    // When
    List<WikiVO> documents =
        IntStream.rangeClosed(1, documentCount)
            .mapToObj(
                i -> {
                  WikiVO.CreateRequest request =
                      WikiVO.CreateRequest.builder()
                          .title("测试文档-" + i)
                          .content("这是第" + i + "条测试文档的内容。包含一些描述性文字用于测试。")
                          .build();
                  return wikiService.createDocument(testProject.getId(), request);
                })
            .collect(Collectors.toList());

    // Then
    assertEquals(documentCount, documents.size());

    // 验证所有文档都已保存
    List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());
    assertEquals(documentCount, tree.size());

    // 验证文档 ID 都不相同
    List<Long> ids = documents.stream().map(WikiVO::getId).collect(Collectors.toList());
    assertEquals(documentCount, ids.stream().distinct().count());
  }

  @Test
  @DisplayName("3.2 压力测试 - 创建 100 条树形结构文档")
  void stressTest_Create100TreeDocuments() {
    // Given
    int totalDocuments = 100;
    int rootCount = 10;
    int childrenPerRoot = (totalDocuments - rootCount) / rootCount; // 9 个子节点

    List<WikiVO> rootDocuments = new ArrayList<>();

    // 创建 10 个根文档
    for (int i = 1; i <= rootCount; i++) {
      WikiVO root =
          wikiService.createDocument(
              testProject.getId(),
              WikiVO.CreateRequest.builder().title("根文档-" + i).content("根文档" + i + "的内容").build());
      rootDocuments.add(root);
    }

    // 为每个根文档创建 9 个子文档
    List<WikiVO> allDocuments = new ArrayList<>(rootDocuments);
    for (WikiVO root : rootDocuments) {
      for (int j = 1; j <= childrenPerRoot; j++) {
        WikiVO child =
            wikiService.createDocument(
                testProject.getId(),
                WikiVO.CreateRequest.builder()
                    .title("子文档-" + root.getId() + "-" + j)
                    .content("子文档" + root.getId() + "-" + j + "的内容")
                    .parentId(root.getId())
                    .build());
        allDocuments.add(child);
      }
    }

    // When
    List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());

    // Then
    assertEquals(rootCount, tree.size()); // 10 个根节点

    // 验证每个根节点都有 9 个子节点
    for (WikiVO rootVo : tree) {
      assertEquals(childrenPerRoot, rootVo.getChildren().size());
    }

    // 验证总文档数
    long totalCount = tree.stream().mapToLong(root -> 1 + root.getChildren().size()).sum();
    assertEquals(totalDocuments, totalCount);
  }

  @Test
  @DisplayName("3.3 压力测试 - 复杂树形结构 (100 条，多层)")
  void stressTest_ComplexTreeStructure() {
    // Given
    // 创建 5 个根节点
    List<WikiVO> roots = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      WikiVO root =
          wikiService.createDocument(
              testProject.getId(),
              WikiVO.CreateRequest.builder().title("根节点-" + i).content("根节点" + i + "的内容").build());
      roots.add(root);
    }

    // 为每个根节点创建 3 个二级子节点 (5 * 3 = 15)
    List<WikiVO> level2Nodes = new ArrayList<>();
    for (WikiVO root : roots) {
      for (int j = 1; j <= 3; j++) {
        WikiVO level2 =
            wikiService.createDocument(
                testProject.getId(),
                WikiVO.CreateRequest.builder()
                    .title("L2-" + root.getId() + "-" + j)
                    .content("二级节点内容")
                    .parentId(root.getId())
                    .build());
        level2Nodes.add(level2);
      }
    }

    // 为每个二级节点创建 5 个三级子节点 (15 * 5 = 75)
    // 总计：5 + 15 + 75 = 95 条
    List<WikiVO> level3Nodes = new ArrayList<>();
    for (WikiVO level2 : level2Nodes) {
      for (int k = 1; k <= 5; k++) {
        WikiVO level3 =
            wikiService.createDocument(
                testProject.getId(),
                WikiVO.CreateRequest.builder()
                    .title("L3-" + level2.getId() + "-" + k)
                    .content("三级节点内容")
                    .parentId(level2.getId())
                    .build());
        level3Nodes.add(level3);
      }
    }

    // 再添加 5 个独立文档，达到 100 条
    for (int i = 1; i <= 5; i++) {
      wikiService.createDocument(
          testProject.getId(),
          WikiVO.CreateRequest.builder().title("独立文档-" + i).content("独立文档内容").build());
    }

    // When
    List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());

    // Then
    assertEquals(10, tree.size()); // 5 个根节点 + 5 个独立文档

    // 验证树结构深度
    for (WikiVO rootVo : tree.subList(0, 5)) {
      assertEquals(3, rootVo.getChildren().size()); // 每个根节点有 3 个二级子节点
      for (WikiVO level2Vo : rootVo.getChildren()) {
        assertEquals(5, level2Vo.getChildren().size()); // 每个二级节点有 5 个三级子节点
      }
    }
  }

  @Test
  @DisplayName("3.4 压力测试 - 批量创建性能测试")
  void stressTest_BatchCreatePerformance() {
    // Given
    int documentCount = 100;
    long startTime = System.currentTimeMillis();

    // When
    for (int i = 1; i <= documentCount; i++) {
      WikiVO.CreateRequest request =
          WikiVO.CreateRequest.builder()
              .title("性能测试文档-" + i)
              .content("内容".repeat(100)) // 每条文档 100 个字符
              .build();
      wikiService.createDocument(testProject.getId(), request);
    }

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    // Then
    List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());
    assertEquals(documentCount, tree.size());

    // 输出性能日志（100 条文档应该在合理时间内完成）
    System.out.println("创建 100 条文档耗时：" + duration + "ms");
    assertTrue(duration < 30000, "创建 100 条文档应该在 30 秒内完成");
  }

  // ==================== 边界条件测试 ====================

  @Test
  @DisplayName("4.1 边界测试 - 标题为空")
  void boundaryTest_EmptyTitle() {
    // Given
    WikiVO.CreateRequest request = WikiVO.CreateRequest.builder().title("").content("内容").build();

    // When & Then - 应该被 @NotBlank 验证拦截
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              wikiService.createDocument(testProject.getId(), request);
            });
    assertNotNull(exception.getMessage());
  }

  @Test
  @DisplayName("4.2 边界测试 - 标题过长 (超过 200 字符)")
  void boundaryTest_TitleTooLong() {
    // Given
    String longTitle = "a".repeat(201);
    WikiVO.CreateRequest request =
        WikiVO.CreateRequest.builder().title(longTitle).content("内容").build();

    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              wikiService.createDocument(testProject.getId(), request);
            });
    assertNotNull(exception.getMessage());
  }

  @Test
  @DisplayName("4.3 边界测试 - 内容为空 (允许)")
  void boundaryTest_EmptyContent() {
    // Given
    WikiVO.CreateRequest request = WikiVO.CreateRequest.builder().title("测试文档").content("").build();

    // When
    WikiVO result = wikiService.createDocument(testProject.getId(), request);

    // Then
    assertNotNull(result);
    assertEquals("测试文档", result.getTitle());
  }

  @Test
  @DisplayName("4.4 边界测试 - 内容为 null (允许)")
  void boundaryTest_NullContent() {
    // Given
    WikiVO.CreateRequest request = WikiVO.CreateRequest.builder().title("测试文档").build();

    // When
    WikiVO result = wikiService.createDocument(testProject.getId(), request);

    // Then
    assertNotNull(result);
    assertEquals("测试文档", result.getTitle());
    assertNull(result.getContent());
  }

  // ==================== 辅助方法 ====================

  /** 创建测试文档的辅助方法 */
  private WikiDocument createTestDocument(String title, String content, Long parentId) {
    WikiDocument document =
        WikiDocument.builder()
            .projectId(testProject.getId())
            .parentId(parentId)
            .title(title)
            .content(content)
            .position(0)
            .build();
    return wikiRepository.save(document);
  }
}
