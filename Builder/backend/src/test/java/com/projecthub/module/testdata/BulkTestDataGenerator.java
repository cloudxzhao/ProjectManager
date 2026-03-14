package com.projecthub.module.testdata;

import com.projecthub.common.util.PasswordUtil;
import com.projecthub.module.issue.dto.IssueVO;
import com.projecthub.module.issue.service.IssueService;
import com.projecthub.module.task.dto.TaskVO;
import com.projecthub.module.task.service.TaskService;
import com.projecthub.module.user.entity.User;
import com.projecthub.module.user.repository.UserRepository;
import com.projecthub.module.wiki.dto.WikiVO;
import com.projecthub.module.wiki.service.WikiService;
import com.projecthub.security.UserDetailsImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

/**
 * 批量测试数据生成器
 *
 * <p>为 projectId=16 的项目创建： - 10 个用户 - 100 个任务 - 50 个问题追踪 - 20 篇 Wiki
 */
@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("批量测试数据生成器")
public class BulkTestDataGenerator {

  private static final Logger log = LoggerFactory.getLogger(BulkTestDataGenerator.class);

  private static final Long PROJECT_ID = 16L;
  private static final int USER_COUNT = 10;
  private static final int TASK_COUNT = 100;
  private static final int ISSUE_COUNT = 50;
  private static final int WIKI_COUNT = 20;

  @Autowired private UserRepository userRepository;
  @Autowired private TaskService taskService;
  @Autowired private IssueService issueService;
  @Autowired private WikiService wikiService;
  @Autowired private PasswordUtil passwordUtil;

  private Long currentUserId;

  @BeforeEach
  void setUp() {
    // 设置管理员用户上下文
    UserDetailsImpl userDetails =
        new UserDetailsImpl(
            1L, // admin 用户 ID
            "admin",
            "admin@projecthub.com",
            "password",
            List.of(
                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "ROLE_ADMIN")),
            true,
            true,
            true,
            true);

    var authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  /** 生成所有测试数据 */
  @Test
  @DisplayName("为项目 16 生成批量测试数据")
  public void generateAllTestData() {
    log.info("========== 开始为项目 {} 生成测试数据 ==========", PROJECT_ID);
    long startTime = System.currentTimeMillis();

    // 1. 创建 10 个用户
    log.info("第一步：创建 {} 个用户...", USER_COUNT);
    Long[] userIds = createUsers();

    // 2. 创建 100 个任务
    log.info("第二步：创建 {} 个任务...", TASK_COUNT);
    createTasks(userIds);

    // 3. 创建 50 个问题追踪
    log.info("第三步：创建 {} 个问题追踪...", ISSUE_COUNT);
    createIssues(userIds);

    // 4. 创建 20 篇 Wiki
    log.info("第四步：创建 {} 篇 Wiki...", WIKI_COUNT);
    createWikis();

    long endTime = System.currentTimeMillis();
    log.info("========== 测试数据生成完成 ==========");
    log.info("总耗时：{}ms", (endTime - startTime));
    log.info("创建用户数：{}", USER_COUNT);
    log.info("创建任务数：{}", TASK_COUNT);
    log.info("创建问题数：{}", ISSUE_COUNT);
    log.info("创建 Wiki 数：{}", WIKI_COUNT);
    log.info("==========================================");
  }

  /** 创建用户 */
  private Long[] createUsers() {
    Long[] userIds = new Long[USER_COUNT];

    String[] nicknames = {
      "John Smith",
      "Jane Johnson",
      "Mike Williams",
      "Sarah Brown",
      "David Jones",
      "Emma Garcia",
      "Chris Miller",
      "Lisa Davis",
      "Tom Martinez",
      "Amy Wilson"
    };

    for (int i = 0; i < USER_COUNT; i++) {
      try {
        String username = "testuser_" + (i + 1);
        String email = "testuser_" + (i + 1) + "@projecthub.com";
        String password = "Test123!";
        String nickname = nicknames[i];

        // 检查用户是否已存在
        var existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
          userIds[i] = existingUser.get().getId();
          log.info("  用户 {} 已存在，使用现有 ID: {}", username, userIds[i]);
          continue;
        }

        // 创建新用户
        User user =
            User.builder()
                .username(username)
                .email(email)
                .password(passwordUtil.encode(password))
                .nickname(nickname)
                .status(User.UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);
        userIds[i] = user.getId();
        log.info("  创建用户 {}: {} ({})", i + 1, username, nickname);

      } catch (Exception e) {
        log.warn("  创建用户 {} 失败：{}", i + 1, e.getMessage());
        userIds[i] = 1L; // 使用 admin 用户作为备用
      }
    }

    return userIds;
  }

  /** 创建任务 */
  private void createTasks(Long[] userIds) {
    String[] taskPrefixes = {"实现", "修复", "优化", "更新", "添加", "删除", "重构", "测试", "文档", "部署"};
    String[] taskSubjects = {
      "用户登录功能", "数据导出模块", "API 接口", "数据库查询", "前端页面",
      "缓存机制", "消息通知", "权限控制", "日志系统", "配置管理",
      "文件上传", "报表生成", "搜索功能", "数据验证", "错误处理"
    };
    String[] statuses = {"TODO", "IN_PROGRESS", "IN_REVIEW", "DONE"};
    String[] priorities = {"LOW", "MEDIUM", "HIGH", "URGENT"};

    Random random = new Random();

    for (int i = 0; i < TASK_COUNT; i++) {
      try {
        String prefix = taskPrefixes[random.nextInt(taskPrefixes.length)];
        String subject = taskSubjects[random.nextInt(taskSubjects.length)];
        String title = prefix + " - " + subject + " #" + (i + 1);

        String description =
            "这是第 "
                + (i + 1)
                + " 个测试任务。\n\n"
                + "## 任务描述\n"
                + "该任务用于测试 ProjectHub 系统的任务管理功能。\n\n"
                + "## 验收标准\n"
                + "- [ ] 代码实现完成\n"
                + "- [ ] 单元测试通过\n"
                + "- [ ] 代码审查通过";

        String status = statuses[random.nextInt(statuses.length)];
        String priority = priorities[random.nextInt(priorities.length)];
        Long assigneeId = userIds[random.nextInt(userIds.length)];
        Integer storyPoints = random.nextInt(5) + 1; // 1-5 点
        LocalDate dueDate = LocalDate.now().plusDays(random.nextInt(30)); // 30 天内

        TaskVO.CreateRequest request =
            TaskVO.CreateRequest.builder()
                .title(title)
                .description(description)
                .status(status)
                .priority(priority)
                .assigneeId(assigneeId)
                .storyPoints(storyPoints)
                .dueDate(dueDate)
                .build();

        TaskVO createdTask = taskService.createTask(PROJECT_ID, request);

        if ((i + 1) % 20 == 0) {
          log.info("  已创建 {} 个任务，最后创建的任务 ID: {}", i + 1, createdTask.getId());
        }

      } catch (Exception e) {
        log.error("  创建任务 {} 失败：{}", i + 1, e.getMessage());
      }
    }

    log.info("  任务创建完成，共 {} 个", TASK_COUNT);
  }

  /** 创建问题追踪 */
  private void createIssues(Long[] userIds) {
    String[] issueTypes = {"BUG", "ISSUE", "IMPROVEMENT", "TECH_DEBT"};
    String[] severities = {"LOW", "NORMAL", "HIGH", "CRITICAL"};
    String[] issuePrefixes = {"发现", "需要", "建议", "请求", "报告"};
    String[] issueSubjects = {
      "界面显示异常", "性能问题", "新功能需求", "用户体验优化", "兼容性支持",
      "安全问题", "文档缺失", "测试覆盖率", "代码规范", "构建脚本"
    };

    Random random = new Random();

    for (int i = 0; i < ISSUE_COUNT; i++) {
      try {
        String prefix = issuePrefixes[random.nextInt(issuePrefixes.length)];
        String subject = issueSubjects[random.nextInt(issueSubjects.length)];
        String title = prefix + " - " + subject + " #" + (i + 1);

        String description =
            "这是第 "
                + (i + 1)
                + " 个测试问题。\n\n"
                + "## 问题描述\n"
                + "详细描述这个问题的具体情况。\n\n"
                + "## 复现步骤\n"
                + "1. 第一步\n"
                + "2. 第二步\n"
                + "3. 第三步\n\n"
                + "## 期望结果\n"
                + "描述期望的正确行为。";

        String type = issueTypes[random.nextInt(issueTypes.length)];
        String severity = severities[random.nextInt(severities.length)];
        Long assigneeId = userIds[random.nextInt(userIds.length)];

        // IssueVO.CreateRequest 只包含 title, description, type, severity, assigneeId, foundDate
        IssueVO.CreateRequest request =
            IssueVO.CreateRequest.builder()
                .title(title)
                .description(description)
                .type(type)
                .severity(severity)
                .assigneeId(assigneeId)
                .foundDate(LocalDate.now())
                .build();

        IssueVO createdIssue = issueService.createIssue(PROJECT_ID, request);

        if ((i + 1) % 10 == 0) {
          log.info("  已创建 {} 个问题，最后创建的问题 ID: {}", i + 1, createdIssue.getId());
        }

      } catch (Exception e) {
        log.error("  创建问题 {} 失败：{}", i + 1, e.getMessage());
      }
    }

    log.info("  问题创建完成，共 {} 个", ISSUE_COUNT);
  }

  /** 创建 Wiki */
  private void createWikis() {
    String[] wikiCategories = {
      "开发指南", "API 文档", "架构设计", "用户手册", "部署文档",
      "最佳实践", "故障排查", "版本说明", "培训资料", "规范文档"
    };
    String[] wikiSubjects = {
      "入门教程", "快速开始", "核心概念", "模块说明", "接口定义",
      "数据模型", "流程图", "配置项", "环境变量", "依赖关系"
    };

    Random random = new Random();

    // 创建 5 个根节点
    WikiVO[] rootNodes = new WikiVO[5];
    for (int i = 0; i < 5; i++) {
      try {
        String title = wikiCategories[i] + " - 项目 16 文档";
        String content =
            "# "
                + title
                + "\n\n"
                + "这是项目 16 的"
                + wikiCategories[i]
                + "根节点文档。\n\n"
                + "## 目录\n"
                + "- [概述](#概述)\n"
                + "- [内容](#内容)\n"
                + "- [相关链接](#相关链接)\n\n"
                + "## 概述\n"
                + "这里是"
                + wikiCategories[i]
                + "的概述内容。\n\n"
                + "## 内容\n"
                + "详细内容请查看子页面。";

        WikiVO.CreateRequest request =
            WikiVO.CreateRequest.builder().title(title).content(content).build();

        rootNodes[i] = wikiService.createDocument(PROJECT_ID, request);
        log.info("  创建根节点 Wiki {}: {}", i + 1, title);

      } catch (Exception e) {
        log.error("  创建根节点 Wiki {} 失败：{}", i + 1, e.getMessage());
      }
    }

    // 创建 15 个子节点（每个根节点下 3 个）
    int subNodeCount = 0;
    for (int i = 0; i < 5 && rootNodes[i] != null; i++) {
      for (int j = 0; j < 3; j++) {
        try {
          String title = wikiSubjects[j] + " - " + wikiCategories[i];
          String content =
              "## "
                  + title
                  + "\n\n"
                  + "这是"
                  + wikiCategories[i]
                  + "下的"
                  + wikiSubjects[j]
                  + "子页面。\n\n"
                  + "### 详细说明\n"
                  + "这里是详细说明内容。\n\n"
                  + "### 示例\n"
                  + "```java\n"
                  + "// 示例代码\n"
                  + "public class Example {\n"
                  + "    public static void main(String[] args) {\n"
                  + "        System.out.println(\"Hello, ProjectHub!\");\n"
                  + "    }\n"
                  + "}\n"
                  + "```";

          WikiVO.CreateRequest request =
              WikiVO.CreateRequest.builder()
                  .parentId(rootNodes[i].getId())
                  .title(title)
                  .content(content)
                  .build();

          wikiService.createDocument(PROJECT_ID, request);
          subNodeCount++;

        } catch (Exception e) {
          log.error("  创建子节点 Wiki 失败：{}", e.getMessage());
        }
      }
    }

    log.info("  Wiki 创建完成，共 {} 个根节点，{} 个子节点", 5, subNodeCount);
  }
}
