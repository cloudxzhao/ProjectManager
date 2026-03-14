package com.projecthub.module.wiki;

import com.projecthub.module.project.entity.Project;
import com.projecthub.module.project.entity.ProjectMember;
import com.projecthub.module.project.entity.ProjectMember.ProjectMemberRole;
import com.projecthub.module.project.repository.ProjectMemberRepository;
import com.projecthub.module.project.repository.ProjectRepository;
import com.projecthub.module.wiki.dto.WikiVO;
import com.projecthub.module.wiki.service.WikiService;
import com.projecthub.security.UserDetailsImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Wiki 持久化测试 - 数据会保留在数据库中
 *
 * <p>使用方法：运行测试后，数据会持久化到数据库，不会回滚
 */
@SpringBootTest
@ActiveProfiles("dev")
@WithUserDetails("admin")
@DisplayName("Wiki 持久化测试（数据保留到数据库）")
@TestPropertySource(properties = "spring.transaction.default-timeout=60")
public class WikiPersistenceTest {

  private static final Logger log = LoggerFactory.getLogger(WikiPersistenceTest.class);

  @Autowired private WikiService wikiService;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private ProjectMemberRepository projectMemberRepository;

  private Project testProject;

  @BeforeEach
  void setUp() {
    // 清理安全上下文
    SecurityContextHolder.clearContext();

    // 创建测试用户
    UserDetailsImpl testUser =
        new UserDetailsImpl(
            1L, "admin", "admin@projecthub.com", "password", null, true, true, true, true);

    // 设置安全上下文
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // 查找或创建测试项目
    testProject =
        projectRepository.findAll().stream()
            .filter(p -> "Wiki 持久化测试项目".equals(p.getName()))
            .findFirst()
            .orElseGet(
                () -> {
                  Project project = new Project();
                  project.setName("Wiki 持久化测试项目");
                  project.setDescription("用于 Wiki 持久化测试的项目，数据会保留在数据库中");
                  project.setOwnerId(1L);
                  project.setStartDate(LocalDate.now());
                  project.setEndDate(LocalDate.now().plusMonths(6));
                  project = projectRepository.save(project);

                  // 添加项目成员权限
                  ProjectMember member = new ProjectMember();
                  member.setProjectId(project.getId());
                  member.setUserId(1L);
                  member.setRole(ProjectMemberRole.OWNER);
                  member.setJoinedAt(LocalDateTime.now());
                  projectMemberRepository.save(member);

                  log.info("创建测试项目：{}", project.getName());
                  return project;
                });
  }

  /** 持久化测试 1：创建 100 条扁平结构的 Wiki 文档 */
  @Test
  @DisplayName("持久化测试 1 - 创建 100 条扁平文档")
  public void persistenceTest_Create100FlatDocuments() {
    log.info("========== 开始持久化测试 1: 创建 100 条扁平文档 ==========");
    long startTime = System.currentTimeMillis();

    for (int i = 1; i <= 100; i++) {
      WikiVO.CreateRequest request =
          WikiVO.CreateRequest.builder()
              .title("持久化测试文档-" + i)
              .content(
                  "这是第 "
                      + i
                      + " 条持久化测试文档的内容。\n"
                      + "创建时间："
                      + LocalDateTime.now()
                      + "\n"
                      + "该数据会永久保存在数据库中，用于验证 Wiki 模块的创建功能。")
              .build();

      try {
        WikiVO result = wikiService.createDocument(testProject.getId(), request);
        if (i % 20 == 0) {
          log.info("已创建 {} 条文档，最后创建的文档 ID: {}", i, result.getId());
        }
      } catch (Exception e) {
        log.error("创建文档 {} 失败：{}", i, e.getMessage());
      }
    }

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    // 验证结果
    List<WikiVO> allDocuments = wikiService.getDocumentTree(testProject.getId());

    log.info("\n========== 持久化测试 1 结果 ==========");
    log.info("创建文档总数：100");
    log.info("实际查询到的文档数：{}", allDocuments.size());
    log.info("耗时：{}ms", duration);
    log.info("平均每条文档耗时：{}ms", (duration / 100.0));
    log.info("=====================================\n");
  }

  /** 持久化测试 2：创建 100 条树形结构的 Wiki 文档 (3 层结构) */
  @Test
  @DisplayName("持久化测试 2 - 创建 100 条树形结构文档 (3 层)")
  public void persistenceTest_Create100TreeDocuments() {
    log.info("========== 开始持久化测试 2: 创建 100 条树形结构文档 ==========");
    long startTime = System.currentTimeMillis();

    // 第 1 层：创建 5 个根节点
    log.info("创建第 1 层：5 个根节点...");
    List<WikiVO> rootNodes =
        java.util.stream.IntStream.rangeClosed(1, 5)
            .mapToObj(
                i -> {
                  WikiVO.CreateRequest request =
                      WikiVO.CreateRequest.builder()
                          .title("根节点-" + i + " - 持久化测试")
                          .content(
                              "# 根节点 "
                                  + i
                                  + "\n\n"
                                  + "这是持久化测试创建的根节点文档。\n"
                                  + "创建时间："
                                  + LocalDateTime.now()
                                  + "\n"
                                  + "该文档有 3 个子节点，每个子节点又有 5 个孙节点。")
                          .build();
                  return wikiService.createDocument(testProject.getId(), request);
                })
            .toList();

    // 第 2 层：每个根节点创建 3 个子节点 (共 15 个)
    log.info("创建第 2 层：15 个二级节点...");
    List<WikiVO> level2Nodes =
        rootNodes.stream()
            .flatMap(
                root ->
                    java.util.stream.IntStream.rangeClosed(1, 3)
                        .mapToObj(
                            j -> {
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
                              return wikiService.createDocument(testProject.getId(), request);
                            }))
            .toList();

    // 第 3 层：每个二级节点创建 5 个子节点 (共 75 个)
    log.info("创建第 3 层：75 个三级节点...");
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
          log.info("已创建 {} 个三级节点...", count);
        }
      }
    }

    // 额外创建 5 个独立文档，达到 100 条
    log.info("创建 5 个独立文档...");
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

    // 验证树形结构
    List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());
    long totalCount = countTreeDocuments(tree);

    log.info("\n========== 持久化测试 2 结果 ==========");
    log.info("树形结构统计:");
    log.info("  - 根节点数：{}", tree.size());
    log.info("  - 总文档数：{}", totalCount);
    log.info("  - 预期文档数：100");
    log.info("\n性能统计:");
    log.info("  - 总耗时：{}ms", duration);
    log.info("  - 平均每条文档耗时：{}ms", (duration / 100.0));
    log.info("=====================================\n");
  }

  /** 持久化测试 3：创建 10 层深度的链式结构 */
  @Test
  @DisplayName("持久化测试 3 - 创建 10 层深度链式结构")
  public void persistenceTest_CreateDeepChain() {
    log.info("========== 开始持久化测试 3: 创建 10 层深度链式结构 ==========");
    long startTime = System.currentTimeMillis();

    Long parentId = null;
    WikiVO lastDoc = null;

    for (int i = 1; i <= 10; i++) {
      WikiVO.CreateRequest request =
          WikiVO.CreateRequest.builder()
              .parentId(parentId)
              .title("第 " + i + " 层节点 - 持久化测试")
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

      log.info("已创建第 {} 层节点，ID: {}", i, lastDoc.getId());
    }

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    // 验证树形结构
    List<WikiVO> tree = wikiService.getDocumentTree(testProject.getId());

    log.info("\n========== 持久化测试 3 结果 ==========");
    log.info("根节点数：{}", tree.size());
    log.info("总耗时：{}ms", duration);
    log.info("=====================================\n");
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
}
