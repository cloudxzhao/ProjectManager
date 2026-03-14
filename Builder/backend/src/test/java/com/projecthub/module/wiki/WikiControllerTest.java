package com.projecthub.module.wiki;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecthub.common.response.Result;
import com.projecthub.module.project.entity.Project;
import com.projecthub.module.project.entity.ProjectMember;
import com.projecthub.module.project.entity.ProjectMember.ProjectMemberRole;
import com.projecthub.module.project.repository.ProjectMemberRepository;
import com.projecthub.module.project.repository.ProjectRepository;
import com.projecthub.module.wiki.dto.WikiVO;
import com.projecthub.module.wiki.entity.WikiDocument;
import com.projecthub.module.wiki.repository.WikiRepository;
import com.projecthub.security.UserDetailsImpl;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/** Wiki 控制器集成测试类 测试 REST API 接口的正确性 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Wiki 控制器测试")
class WikiControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private WikiRepository wikiRepository;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private ProjectMemberRepository projectMemberRepository;

  private Project testProject;
  private UserDetailsImpl testUser;
  private String authToken;

  @BeforeEach
  void setUp() throws Exception {
    // 清理安全上下文
    SecurityContextHolder.clearContext();

    // 创建测试用户
    testUser =
        new UserDetailsImpl(
            1L, "testuser", "test@example.com", "password", null, true, true, true, true);

    // 设置安全上下文
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // 创建测试项目
    testProject = new Project();
    testProject.setName("Wiki 测试项目");
    testProject.setDescription("用于 Wiki 测试的项目");
    testProject.setOwnerId(1L);
    testProject.setStartDate(LocalDateTime.now().toLocalDate());
    testProject.setEndDate(LocalDateTime.now().plusMonths(6).toLocalDate());
    testProject = projectRepository.save(testProject);

    // 添加项目成员权限
    ProjectMember member = new ProjectMember();
    member.setProjectId(testProject.getId());
    member.setUserId(1L);
    member.setRole(ProjectMemberRole.OWNER);
    projectMemberRepository.save(member);
  }

  // ==================== 创建文档 API 测试 ====================

  @Test
  @DisplayName("1. 创建文档 API - 成功")
  void createDocument_Success() throws Exception {
    // Given
    WikiVO.CreateRequest request =
        WikiVO.CreateRequest.builder().title("测试文档").content("这是测试内容").build();

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/projects/{projectId}/wiki", testProject.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.title").value("测试文档"))
        .andExpect(jsonPath("$.data.content").value("这是测试内容"))
        .andExpect(jsonPath("$.data.projectId").value(testProject.getId()));
  }

  @Test
  @DisplayName("2. 创建文档 API - 创建子文档")
  void createDocument_WithParent_Success() throws Exception {
    // Given
    // 先创建父文档
    WikiDocument parentDoc = createWikiDocument("父文档", "父内容", null);

    WikiVO.CreateRequest request =
        WikiVO.CreateRequest.builder()
            .title("子文档")
            .content("子内容")
            .parentId(parentDoc.getId())
            .build();

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/projects/{projectId}/wiki", testProject.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.title").value("子文档"))
        .andExpect(jsonPath("$.data.parentId").value(parentDoc.getId()));
  }

  @Test
  @DisplayName("3. 创建文档 API - 标题为空 (失败)")
  void createDocument_EmptyTitle_Fail() throws Exception {
    // Given
    WikiVO.CreateRequest request = WikiVO.CreateRequest.builder().title("").content("内容").build();

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/projects/{projectId}/wiki", testProject.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("4. 创建文档 API - 标题过长 (失败)")
  void createDocument_TitleTooLong_Fail() throws Exception {
    // Given
    WikiVO.CreateRequest request =
        WikiVO.CreateRequest.builder().title("a".repeat(201)).content("内容").build();

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/projects/{projectId}/wiki", testProject.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  // ==================== 获取文档 API 测试 ====================

  @Test
  @DisplayName("5. 获取文档详情 API - 成功")
  void getDocument_Success() throws Exception {
    // Given
    WikiDocument doc = createWikiDocument("测试文档", "测试内容", null);

    // When & Then
    mockMvc
        .perform(get("/api/v1/projects/{projectId}/wiki/{id}", testProject.getId(), doc.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.title").value("测试文档"))
        .andExpect(jsonPath("$.data.content").value("测试内容"));
  }

  @Test
  @DisplayName("6. 获取文档详情 API - 文档不存在")
  void getDocument_NotFound() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/api/v1/projects/{projectId}/wiki/{id}", testProject.getId(), 99999L))
        .andExpect(status().isNotFound());
  }

  // ==================== 获取文档树 API 测试 ====================

  @Test
  @DisplayName("7. 获取文档树 API - 成功")
  void getDocumentTree_Success() throws Exception {
    // Given
    createWikiDocument("文档 1", "内容 1", null);
    createWikiDocument("文档 2", "内容 2", null);
    WikiDocument doc3 = createWikiDocument("文档 3", "内容 3", null);
    createWikiDocument("子文档 3-1", "子内容 3-1", doc3.getId());
    createWikiDocument("子文档 3-2", "子内容 3-2", doc3.getId());

    // When & Then
    mockMvc
        .perform(get("/api/v1/projects/{projectId}/wiki", testProject.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data", hasSize(3)))
        .andExpect(jsonPath("$.data[2].children", hasSize(2)));
  }

  @Test
  @DisplayName("8. 获取文档树 API - 空项目")
  void getDocumentTree_Empty() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/api/v1/projects/{projectId}/wiki", testProject.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data", hasSize(0)));
  }

  // ==================== 更新文档 API 测试 ====================

  @Test
  @DisplayName("9. 更新文档 API - 成功")
  void updateDocument_Success() throws Exception {
    // Given
    WikiDocument doc = createWikiDocument("原文档", "原内容", null);

    WikiVO.UpdateRequest request =
        WikiVO.UpdateRequest.builder().title("更新后的标题").content("更新后的内容").build();

    // When & Then
    mockMvc
        .perform(
            put("/api/v1/projects/{projectId}/wiki/{id}", testProject.getId(), doc.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.title").value("更新后的标题"))
        .andExpect(jsonPath("$.data.content").value("更新后的内容"));
  }

  @Test
  @DisplayName("10. 更新文档 API - 只更新标题")
  void updateDocument_TitleOnly() throws Exception {
    // Given
    WikiDocument doc = createWikiDocument("原文档", "原内容", null);

    WikiVO.UpdateRequest request = WikiVO.UpdateRequest.builder().title("新标题").build();

    // When & Then
    mockMvc
        .perform(
            put("/api/v1/projects/{projectId}/wiki/{id}", testProject.getId(), doc.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.title").value("新标题"))
        .andExpect(jsonPath("$.data.content").value("原内容"));
  }

  @Test
  @DisplayName("11. 更新文档 API - 文档不存在")
  void updateDocument_NotFound() throws Exception {
    // Given
    WikiVO.UpdateRequest request = WikiVO.UpdateRequest.builder().title("新标题").build();

    // When & Then
    mockMvc
        .perform(
            put("/api/v1/projects/{projectId}/wiki/{id}", testProject.getId(), 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  // ==================== 删除文档 API 测试 ====================

  @Test
  @DisplayName("12. 删除文档 API - 成功")
  void deleteDocument_Success() throws Exception {
    // Given
    WikiDocument doc = createWikiDocument("待删除文档", "内容", null);
    Long docId = doc.getId();

    // When & Then
    mockMvc
        .perform(delete("/api/v1/projects/{projectId}/wiki/{id}", testProject.getId(), docId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));

    // 验证文档已被删除
    mockMvc
        .perform(get("/api/v1/projects/{projectId}/wiki/{id}", testProject.getId(), docId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("13. 删除文档 API - 文档不存在")
  void deleteDocument_NotFound() throws Exception {
    // When & Then
    mockMvc
        .perform(delete("/api/v1/projects/{projectId}/wiki/{id}", testProject.getId(), 99999L))
        .andExpect(status().isNotFound());
  }

  // ==================== 历史记录 API 测试 ====================

  @Test
  @DisplayName("14. 获取文档历史记录 API - 成功")
  void getDocumentHistory_Success() throws Exception {
    // Given
    WikiDocument doc = createWikiDocument("测试文档", "初始内容", null);

    // 更新两次
    WikiVO.UpdateRequest update1 = WikiVO.UpdateRequest.builder().content("第一次更新").build();
    mockMvc.perform(
        put("/api/v1/projects/{projectId}/wiki/{id}", testProject.getId(), doc.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(update1)));

    WikiVO.UpdateRequest update2 = WikiVO.UpdateRequest.builder().content("第二次更新").build();
    mockMvc.perform(
        put("/api/v1/projects/{projectId}/wiki/{id}", testProject.getId(), doc.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(update2)));

    // When & Then
    mockMvc
        .perform(
            get("/api/v1/projects/{projectId}/wiki/{id}/history", testProject.getId(), doc.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data", hasSize(3))) // 初始 + 2 次更新
        .andExpect(jsonPath("$.data[0].content").value("第二次更新"));
  }

  // ==================== 批量创建压力测试 ====================

  @Test
  @DisplayName("15. 压力测试 - 批量创建 100 条文档")
  void stressTest_BatchCreate100Documents() throws Exception {
    // Given
    int count = 100;

    // When
    for (int i = 1; i <= count; i++) {
      WikiVO.CreateRequest request =
          WikiVO.CreateRequest.builder()
              .title("批量测试文档-" + i)
              .content("这是第" + i + "条测试文档的内容")
              .build();

      mockMvc
          .perform(
              post("/api/v1/projects/{projectId}/wiki", testProject.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200));
    }

    // Then
    MvcResult result =
        mockMvc
            .perform(get("/api/v1/projects/{projectId}/wiki", testProject.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(count)))
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    Result<List<WikiVO>> resultObj =
        objectMapper.readValue(
            responseBody,
            objectMapper.getTypeFactory().constructParametricType(Result.class, List.class));
    assertNotNull(resultObj.getData());
  }

  @Test
  @DisplayName("16. 压力测试 - 创建树形结构文档 (100 条)")
  void stressTest_CreateTreeStructure() throws Exception {
    // Given
    int rootCount = 10;
    int childrenPerRoot = 9;

    // 创建 10 个根文档
    for (int i = 1; i <= rootCount; i++) {
      WikiVO.CreateRequest request =
          WikiVO.CreateRequest.builder().title("根文档-" + i).content("根文档" + i + "的内容").build();

      MvcResult result =
          mockMvc
              .perform(
                  post("/api/v1/projects/{projectId}/wiki", testProject.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isOk())
              .andReturn();

      WikiVO rootDoc = parseResponse(result, WikiVO.class);

      // 为每个根文档创建 9 个子文档
      for (int j = 1; j <= childrenPerRoot; j++) {
        WikiVO.CreateRequest childRequest =
            WikiVO.CreateRequest.builder()
                .title("子文档-" + rootDoc.getId() + "-" + j)
                .content("子文档内容")
                .parentId(rootDoc.getId())
                .build();

        mockMvc
            .perform(
                post("/api/v1/projects/{projectId}/wiki", testProject.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(childRequest)))
            .andExpect(status().isOk());
      }
    }

    // Then
    mockMvc
        .perform(get("/api/v1/projects/{projectId}/wiki", testProject.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(rootCount)))
        .andExpect(jsonPath("$.data[*].children", hasSize(rootCount)))
        .andExpect(jsonPath("$.data[*].children[*]", hasSize(rootCount * childrenPerRoot)));
  }

  // ==================== 树形结构验证测试 ====================

  @Test
  @DisplayName("17. 树形结构测试 - 三层嵌套")
  void treeStructure_ThreeLevels() throws Exception {
    // Given
    // 创建根文档
    WikiVO.CreateRequest rootReq =
        WikiVO.CreateRequest.builder().title("根节点").content("根内容").build();
    MvcResult rootResult =
        mockMvc
            .perform(
                post("/api/v1/projects/{projectId}/wiki", testProject.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(rootReq)))
            .andExpect(status().isOk())
            .andReturn();
    WikiVO root = parseResponse(rootResult, WikiVO.class);

    // 创建二级子节点
    WikiVO.CreateRequest childReq =
        WikiVO.CreateRequest.builder().title("子节点").content("子内容").parentId(root.getId()).build();
    MvcResult childResult =
        mockMvc
            .perform(
                post("/api/v1/projects/{projectId}/wiki", testProject.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(childReq)))
            .andExpect(status().isOk())
            .andReturn();
    WikiVO child = parseResponse(childResult, WikiVO.class);

    // 创建三级子节点
    WikiVO.CreateRequest grandChildReq =
        WikiVO.CreateRequest.builder().title("孙节点").content("孙内容").parentId(child.getId()).build();
    mockMvc
        .perform(
            post("/api/v1/projects/{projectId}/wiki", testProject.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(grandChildReq)))
        .andExpect(status().isOk());

    // When & Then
    mockMvc
        .perform(get("/api/v1/projects/{projectId}/wiki", testProject.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].children", hasSize(1)))
        .andExpect(jsonPath("$.data[0].children[0].children", hasSize(1)))
        .andExpect(jsonPath("$.data[0].children[0].children[0].title").value("孙节点"));
  }

  // ==================== 辅助方法 ====================

  private WikiDocument createWikiDocument(String title, String content, Long parentId) {
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

  private <T> T parseResponse(MvcResult result, Class<T> clazz) throws Exception {
    String responseBody = result.getResponse().getContentAsString();
    @SuppressWarnings("unchecked")
    Result<T> resultObj =
        objectMapper.readValue(
            responseBody,
            objectMapper.getTypeFactory().constructParametricType(Result.class, clazz));
    return resultObj.getData();
  }
}
