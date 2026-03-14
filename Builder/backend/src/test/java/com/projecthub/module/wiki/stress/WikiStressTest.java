/**
 * Wiki 模块压力测试脚本
 *
 * <p>使用方法: 1. 确保后端服务已启动 (http://localhost:9527) 2. 确保数据库和 Redis 已启动 3. 运行此测试类
 *
 * <p>测试内容: - 创建 100 条 Wiki 文档 - 按照树形结构存储 (3 层结构)
 */
package com.projecthub.module.wiki.stress;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecthub.module.project.entity.Project;
import com.projecthub.module.project.entity.ProjectMember;
import com.projecthub.module.project.entity.ProjectMember.ProjectMemberRole;
import com.projecthub.module.project.repository.ProjectMemberRepository;
import com.projecthub.module.project.repository.ProjectRepository;
import com.projecthub.module.wiki.dto.WikiVO;
import com.projecthub.module.wiki.repository.WikiRepository;
import com.projecthub.module.wiki.service.WikiService;
import com.projecthub.security.UserDetailsImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** Wiki 压力测试 - 直接向数据库写入 100 条测试数据 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional
@DisplayName("Wiki 压力测试")
public class WikiStressTest {

  @Autowired private WikiService wikiService;

  @Autowired private WikiRepository wikiRepository;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private ProjectMemberRepository projectMemberRepository;

  @Autowired private ObjectMapper objectMapper;

  private Project testProject;
  private Long testUserId;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();

    // 使用管理员用户
    testUserId = 1L;
    UserDetailsImpl testUser =
        new UserDetailsImpl(
            testUserId, "admin", "admin@projecthub.com", "password", null, true, true, true, true);

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // 查找或创建测试项目
    testProject = projectRepository.findAll().stream().findFirst().orElse(null);
    if (testProject == null) {
      testProject = new Project();
      testProject.setName("压力测试项目");
      testProject.setDescription("用于 Wiki 压力测试的项目");
      testProject.setOwnerId(testUserId);
      testProject.setStartDate(LocalDate.now());
      testProject.setEndDate(LocalDate.now().plusMonths(6));
      testProject = projectRepository.save(testProject);

      ProjectMember member = new ProjectMember();
      member.setProjectId(testProject.getId());
      member.setUserId(testUserId);
      member.setRole(ProjectMemberRole.OWNER);
      projectMemberRepository.save(member);
    }
  }

  /** 压力测试 1: 创建 100 条扁平结构的 Wiki 文档 */
  @Test
  @DisplayName("压力测试 1 - 创建 100 条扁平文档")
  public void stressTest_Create100FlatDocuments() {
    System.out.println("========== 开始压力测试 1: 创建 100 条扁平文档 ==========");
    long startTime = System.currentTimeMillis();

    List<WikiVO> createdDocuments = new ArrayList<>();

    for (int i = 1; i <= 100; i++) {
      WikiVO.CreateRequest request =
          WikiVO.CreateRequest.builder()
              .title("测试文档-" + i)
              .content(
                  "这是第 "
                      + i
                      + " 条测试文档的内容。\n"
                      + "创建时间："
                      + LocalDateTime.now()
                      + "\n"
                      + "内容包含一些描述性文字，用于测试 Wiki 模块的批量创建功能。\n"
                      + "文档编号："
                      + i
                      + "/100")
              .build();

      try {
        WikiVO result = wikiService.createDocument(testProject.getId(), request);
        createdDocuments.add(result);

        if (i % 10 == 0) {
          System.out.println("已创建 " + i + " 条文档...");
        }
      } catch (Exception e) {
        System.err.println("创建文档 " + i + " 失败：" + e.getMessage());
      }
    }

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    // 验证结果
    List<WikiVO> allDocuments = wikiService.getDocumentTree(testProject.getId());

    System.out.println("\n========== 压力测试 1 结果 ==========");
    System.out.println("创建文档总数：" + createdDocuments.size());
    System.out.println("实际查询到的文档数：" + allDocuments.size());
    System.out.println("耗时：" + duration + "ms");
    System.out.println("平均每条文档耗时：" + (duration / 100.0) + "ms");
    System.out.println("=====================================\n");
  }

  /** 压力测试 2: 创建 100 条树形结构的 Wiki 文档 (3 层结构) 结构：5 个根节点 -> 15 个二级节点 -> 75 个三级节点 + 5 个独立文档 = 100 条 */
  @Test
  @DisplayName("压力测试 2 - 创建 100 条树形结构文档 (3 层)")
  public void stressTest_Create100TreeDocuments() {
    System.out.println("========== 开始压力测试 2: 创建 100 条树形结构文档 ==========");
    long startTime = System.currentTimeMillis();

    List<WikiVO> allDocuments = new ArrayList<>();

    // 第 1 层：创建 5 个根节点
    System.out.println("创建第 1 层：5 个根节点...");
    List<WikiVO> rootNodes = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      WikiVO.CreateRequest request =
          WikiVO.CreateRequest.builder()
              .title("根节点-" + i + " - 压力测试")
              .content(
                  "# 根节点 "
                      + i
                      + "\n\n"
                      + "这是压力测试创建的根节点文档。\n"
                      + "创建时间："
                      + LocalDateTime.now()
                      + "\n"
                      + "该文档有 3 个子节点，每个子节点又有 5 个孙节点。")
              .build();
      WikiVO root = wikiService.createDocument(testProject.getId(), request);
      rootNodes.add(root);
      allDocuments.add(root);
    }

    // 第 2 层：每个根节点创建 3 个子节点 (共 15 个)
    System.out.println("创建第 2 层：15 个二级节点...");
    List<WikiVO> level2Nodes = new ArrayList<>();
    for (WikiVO root : rootNodes) {
      for (int j = 1; j <= 3; j++) {
        WikiVO.CreateRequest request =
            WikiVO.CreateRequest.builder()
                .parentId(root.getId())
                .title("二级节点-" + root.getId() + "-" + j)
                .content(
                    "## 二级节点 "
                        + j
                        + "\n\n"
                        + "父节点 ID: "
                        + root.getId()
                        + "\n"
                        + "创建时间："
                        + LocalDateTime.now()
                        + "\n"
                        + "该节点有 5 个子节点。")
                .build();
        WikiVO level2 = wikiService.createDocument(testProject.getId(), request);
        level2Nodes.add(level2);
        allDocuments.add(level2);
      }
    }

    // 第 3 层：每个二级节点创建 5 个子节点 (共 75 个)
    System.out.println("创建第 3 层：75 个三级节点...");
    int count = 0;
    for (WikiVO level2 : level2Nodes) {
      for (int k = 1; k <= 5; k++) {
        WikiVO.CreateRequest request =
            WikiVO.CreateRequest.builder()
                .parentId(level2.getId())
                .title("三级节点-" + level2.getId() + "-" + k)
                .content(
                    "### 三级节点 "
                        + k
                        + "\n\n"
                        + "父节点 ID: "
                        + level2.getId()
                        + "\n"
                        + "创建时间："
                        + LocalDateTime.now())
                .build();
        wikiService.createDocument(testProject.getId(), request);
        count++;

        if (count % 25 == 0) {
          System.out.println("已创建 " + count + " 个三级节点...");
        }
      }
    }

    // 额外创建 5 个独立文档，达到 100 条
    System.out.println("创建 5 个独立文档...");
    for (int i = 1; i <= 5; i++) {
      WikiVO.CreateRequest request =
          WikiVO.CreateRequest.builder()
              .title("独立文档-" + i)
              .content("这是独立文档 " + i + "\n创建时间：" + LocalDateTime.now())
              .build();
      wikiService.createDocument(testProject.getId(), request);
    }

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    // 验证结果
    List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());
    long totalCount = countTreeDocuments(tree);

    System.out.println("\n========== 压力测试 2 结果 ==========");
    System.out.println("树形结构统计:");
    System.out.println("  - 根节点数：" + tree.size());
    System.out.println("  - 总文档数：" + totalCount);
    System.out.println("  - 预期文档数：100");
    System.out.println("\n性能统计:");
    System.out.println("  - 总耗时：" + duration + "ms");
    System.out.println("  - 平均每条文档耗时：" + (duration / 100.0) + "ms");
    System.out.println("=====================================\n");
  }

  /** 压力测试 3: 创建 10 层深度的链式结构 */
  @Test
  @DisplayName("压力测试 3 - 创建 10 层深度链式结构")
  public void stressTest_CreateDeepChain() {
    System.out.println("========== 开始压力测试 3: 创建 10 层深度链式结构 ==========");
    long startTime = System.currentTimeMillis();

    Long parentId = null;
    WikiVO lastDoc = null;

    for (int i = 1; i <= 10; i++) {
      WikiVO.CreateRequest request =
          WikiVO.CreateRequest.builder()
              .parentId(parentId)
              .title("第 " + i + " 层节点")
              .content(
                  "# 第 "
                      + i
                      + " 层节点\n\n"
                      + "这是深度测试的第 "
                      + i
                      + " 层节点。\n"
                      + "父节点："
                      + (parentId != null ? parentId : "无 (根节点)")
                      + "\n"
                      + "创建时间："
                      + LocalDateTime.now())
              .build();

      lastDoc = wikiService.createDocument(testProject.getId(), request);
      parentId = lastDoc.getId();

      System.out.println("已创建第 " + i + " 层节点，ID: " + lastDoc.getId());
    }

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    // 验证树形结构
    List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());

    System.out.println("\n========== 压力测试 3 结果 ==========");
    System.out.println("根节点数：" + tree.size());
    System.out.println("验证深度：");
    verifyDepth(tree.get(0), 1);
    System.out.println("\n总耗时：" + duration + "ms");
    System.out.println("=====================================\n");
  }

  /** 压力测试 4: 批量创建后验证树形结构完整性 */
  @Test
  @DisplayName("压力测试 4 - 验证树形结构完整性")
  public void stressTest_VerifyTreeStructure() {
    System.out.println("========== 开始压力测试 4: 验证树形结构完整性 ==========");

    // 先创建一批测试数据
    WikiVO root1 =
        wikiService.createDocument(
            testProject.getId(),
            WikiVO.CreateRequest.builder().title("验证测试 - 根 1").content("根节点 1").build());
    WikiVO root2 =
        wikiService.createDocument(
            testProject.getId(),
            WikiVO.CreateRequest.builder().title("验证测试 - 根 2").content("根节点 2").build());

    for (int i = 1; i <= 5; i++) {
      wikiService.createDocument(
          testProject.getId(),
          WikiVO.CreateRequest.builder()
              .parentId(root1.getId())
              .title("根 1-子节点-" + i)
              .content("内容")
              .build());
      wikiService.createDocument(
          testProject.getId(),
          WikiVO.CreateRequest.builder()
              .parentId(root2.getId())
              .title("根 2-子节点-" + i)
              .content("内容")
              .build());
    }

    // 获取树形结构
    List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());

    // 验证
    System.out.println("根节点数量：" + tree.size());
    for (WikiVO root : tree) {
      System.out.println(
          "根节点："
              + root.getTitle()
              + ", 子节点数："
              + (root.getChildren() != null ? root.getChildren().size() : 0));
    }

    System.out.println("=====================================\n");
  }

  /** 辅助方法：统计树形结构中的文档总数 */
  private long countTreeDocuments(List<WikiVO> tree) {
    long count = 0;
    for (WikiVO node : tree) {
      count++;
      if (node.getChildren() != null && !node.getChildren().isEmpty()) {
        count += countTreeDocuments(node.getChildren());
      }
    }
    return count;
  }

  /** 辅助方法：验证树的深度 */
  private void verifyDepth(WikiVO node, int currentDepth) {
    String indent = "  ".repeat(currentDepth - 1);
    System.out.println(
        indent + "└─ 第" + currentDepth + "层：" + node.getTitle() + " (ID: " + node.getId() + ")");

    if (node.getChildren() != null && !node.getChildren().isEmpty()) {
      for (WikiVO child : node.getChildren()) {
        verifyDepth(child, currentDepth + 1);
      }
    }
  }
}
