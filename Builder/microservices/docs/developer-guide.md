# ProjectHub 微服务开发指南

## 目录

1. [开发环境搭建](#开发环境搭建)
2. [项目结构](#项目结构)
3. [编码规范](#编码规范)
4. [服务间调用](#服务间调用)
5. [事件驱动开发](#事件驱动开发)
6. [数据库操作](#数据库操作)
7. [测试指南](#测试指南)
8. [调试技巧](#调试技巧)

---

## 开发环境搭建

### 1. 安装必要工具

```bash
# JDK 21
sdk install java 21.0.2-tem

# Maven 3.9+
sdk install maven

# Docker Desktop
# 下载：https://www.docker.com/products/docker-desktop

# Node.js 18+
nvm install 18

# Python 3.10+ (用于迁移脚本)
pyenv install 3.10.0
```

### 2. 克隆项目

```bash
git clone https://github.com/your-org/projecthub.git
cd projecthub/Builder/microservices
```

### 3. 启动基础设施

```bash
# 启动所有基础设施
docker-compose up -d

# 验证服务
docker-compose ps

# 访问 Nacos 控制台
open http://localhost:8848/nacos
# 默认账号：nacos/nacos

# 访问 RabbitMQ 控制台
open http://localhost:15672
# 默认账号：guest/guest
```

### 4. 配置 IDE

#### IntelliJ IDEA

1. **导入项目**: File → Open → 选择 `Builder/microservices/pom.xml`
2. **配置 Lombok**: Settings → Annotation Processors → Enable
3. **代码风格**: 导入项目根目录的 `codestyle.xml`

#### VS Code

1. **安装扩展**:
   - Language Support for Java (Red Hat)
   - Lombok Annotations
   - Spring Boot Extension Pack
   - Docker

2. **配置 settings.json**:
```json
{
  "java.home": "/path/to/jdk-21",
  "java.project.referencedLibraries": ["**/*.jar"],
  "java.configuration.updateBuildConfiguration": "automatic"
}
```

---

## 项目结构

### 目录结构

```
Builder/microservices/
├── common/                    # 公共模块
│   ├── common-api/           # 公共 DTO、VO、常量
│   ├── common-core/          # 工具类、异常处理
│   ├── common-redis/         # Redis 配置
│   ├── common-feign/         # Feign 配置
│   ├── common-mq/            # RabbitMQ 配置
│   └── common-security/      # 安全认证
├── gateway/                   # API Gateway
├── services/                  # 微服务
│   ├── auth-service/         # 认证服务
│   ├── user-service/         # 用户服务
│   ├── project-service/      # 项目服务
│   ├── task-service/         # 任务服务
│   ├── story-service/        # 用户故事服务
│   ├── issue-service/        # 问题追踪服务
│   ├── wiki-service/         # Wiki 服务
│   └── notification-service/ # 通知服务
├── scripts/                   # 脚本
│   ├── migrate_data.py       # 数据迁移
│   ├── validate_data.py      # 数据校验
│   └── rollback_data.py      # 数据回滚
└── docs/                      # 文档
```

### 服务结构

```
auth-service/
├── src/main/java/com/projecthub/auth/
│   ├── AuthServiceApplication.java    # 启动类
│   ├── controller/                    # Controller 层
│   │   ├── AuthController.java
│   │   └── AuthInternalController.java
│   ├── service/                       # Service 层
│   │   ├── AuthService.java
│   │   └── impl/
│   ├── entity/                        # 实体类
│   │   ├── User.java
│   │   └── RefreshToken.java
│   ├── repository/                    # Repository 层
│   │   ├── UserRepository.java
│   │   └── RefreshTokenRepository.java
│   ├── dto/                           # DTO
│   │   ├── request/                   # 请求 DTO
│   │   │   ├── LoginRequest.java
│   │   │   └── RegisterRequest.java
│   │   └── response/                  # 响应 DTO
│   │       └── LoginResponse.java
│   ├── config/                        # 配置类
│   │   ├── SecurityConfig.java
│   │   └── MyMetaObjectHandler.java
│   └── client/                        # Feign Client
│       └── UserClient.java
├── src/main/resources/
│   ├── application.yaml              # 配置文件
│   ├── application-dev.yaml          # 开发环境
│   ├── application-prod.yaml         # 生产环境
│   └── schema.sql                    # 数据库脚本
└── src/test/java/                    # 测试代码
```

---

## 编码规范

### 命名规范

```java
// ✅ 好的命名
@Service
public class AuthService { }

@Repository
public interface UserRepository extends BaseMapper<User> { }

@Data
public class LoginRequest {
    private String email;
    private String password;
}

@Data
public class LoginResponse {
    private String accessToken;
    private Long expiresIn;
}

// ❌ 不好的命名
@Service
public class auth_service { }  // 应该用 PascalCase

@Data
public class login_request { }  // 应该用 PascalCase
```

### 注释规范

```java
/**
 * 用户登录服务
 *
 * <p>验证用户凭据，生成 JWT Token</p>
 *
 * @author 开发者姓名
 * @since 1.0.0
 */
@Service
public class AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求（包含邮箱和密码）
     * @return 登录响应（包含访问令牌和刷新令牌）
     * @throws AuthenticationException 认证失败时抛出
     */
    public LoginResponse login(LoginRequest request) {
        // ...
    }
}
```

### 异常处理

```java
// ✅ 好的做法：使用全局异常处理
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        log.error("认证失败：{}", e.getMessage());
        return Result.error(ErrorCode.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.error(e.getErrorCode(), e.getMessage());
    }
}

// ❌ 不好的做法：在每个 Controller 中捕获异常
@RestController
public class AuthController {

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            return Result.success(authService.login(request));
        } catch (Exception e) {
            // 不好的做法：捕获所有异常
            return Result.error(500, e.getMessage());
        }
    }
}
```

### 日志规范

```java
// ✅ 好的日志实践
@Slf4j
@Service
public class AuthService {

    public LoginResponse login(LoginRequest request) {
        log.info("用户登录：email={}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                log.warn("用户不存在：email={}", request.getEmail());
                return new AuthenticationException("用户不存在");
            });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("密码错误：email={}", request.getEmail());
            throw new AuthenticationException("密码错误");
        }

        log.info("登录成功：userId={}", user.getId());
        return generateToken(user);
    }
}

// ❌ 不好的日志实践
log.info("登录：" + request.toString());  // 应该使用占位符
log.debug(user.toString());  // 可能泄露敏感信息
```

---

## 服务间调用

### OpenFeign 使用

#### 1. 定义 Feign Client

```java
// common-feign 模块或服务的 client 包
@FeignClient(
    name = "user-service",
    fallbackFactory = UserClientFallbackFactory.class
)
public interface UserClient {

    /**
     * 根据 ID 获取用户信息
     */
    @GetMapping("/api/v1/users/internal/{id}")
    Result<UserVO> getUserById(@PathVariable("id") Long id);

    /**
     * 根据 ID 批量获取用户信息
     */
    @PostMapping("/api/v1/users/internal/batch")
    Result<List<UserVO>> getUsersByIds(@RequestBody List<Long> ids);
}
```

#### 2. 定义 Fallback Factory

```java
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public Result<UserVO> getUserById(Long id) {
                log.error("调用 User Service 失败：id={}", id, cause);
                return Result.error(ErrorCode.SERVICE_UNAVAILABLE, "用户服务不可用");
            }

            @Override
            public Result<List<UserVO>> getUsersByIds(List<Long> ids) {
                log.error("批量获取用户失败：ids={}", ids, cause);
                return Result.error(ErrorCode.SERVICE_UNAVAILABLE, "用户服务不可用");
            }
        };
    }
}
```

#### 3. 使用 Feign Client

```java
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final UserClient userClient;

    public ProjectVO getProjectWithOwner(Long projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new NotFoundException("项目不存在"));

        // 调用 User Service 获取 Owner 信息
        Result<UserVO> userResult = userClient.getUserById(project.getOwnerId());

        if (!userResult.isSuccess()) {
            log.warn("获取用户信息失败：projectId={}, ownerId={}", projectId, project.getOwnerId());
            // 可以返回降级数据或抛出异常
        }

        ProjectVO vo = BeanUtil.copyTo(project, ProjectVO.class);
        vo.setOwner(userResult.getData());

        return vo;
    }
}
```

### Feign 配置

```yaml
# application.yaml
feign:
  client:
    config:
      default:  # 全局配置
        connectTimeout: 5000  # 连接超时 5 秒
        readTimeout: 30000    # 读超时 30 秒
        loggerLevel: basic    # 日志级别
      user-service:  # 特定服务配置
        connectTimeout: 10000
        readTimeout: 60000
  compression:
    request:
      enabled: true  # 启用请求压缩
    response:
      enabled: true  # 启用响应压缩
```

---

## 事件驱动开发

### 定义事件

```java
// common-mq 模块
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskAssignedEvent extends EventDataBase {

    private Long taskId;
    private String taskTitle;
    private Long assigneeId;
    private String assigneeName;
    private Long projectId;
    private Long operatorId;  // 操作人 ID

}
```

### 发布事件

```java
@Service
@RequiredArgsConstructor
public class TaskService {

    private final EventPublisher eventPublisher;

    @Transactional
    public Task assignTask(Long taskId, Long assigneeId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException("任务不存在"));

        // 更新任务分配
        task.setAssigneeId(assigneeId);
        task.setStatus(TaskStatus.IN_PROGRESS.name());
        taskRepository.updateById(task);

        // 发布事件
        TaskAssignedEvent eventData = new TaskAssignedEvent();
        eventData.setTaskId(task.getId());
        eventData.setTaskTitle(task.getTitle());
        eventData.setAssigneeId(assigneeId);
        eventData.setProjectId(task.getProjectId());

        EventMessage<TaskAssignedEvent> message = EventMessage.of(
            EventTypes.TASK_ASSIGNED,
            "task-service",
            eventData
        );

        eventPublisher.publish(
            MqConstant.EXCHANGE_TOPIC,
            MqConstant.ROUTING_KEY_TASK + ".assign",
            message
        );

        return task;
    }
}
```

### 消费事件

```java
@Component
@Slf4j
public class TaskAssignedListener extends BaseEventListener<TaskAssignedEvent> {

    private final NotificationService notificationService;

    @Override
    protected boolean onMessage(EventMessage<TaskAssignedEvent> message) {
        TaskAssignedEvent event = message.getData();

        log.info("收到任务分配事件：taskId={}, assigneeId={}",
                 event.getTaskId(), event.getAssigneeId());

        try {
            // 创建通知
            CreateNotificationRequest request = new CreateNotificationRequest();
            request.setUserId(event.getAssigneeId());
            request.setTitle("新任务分配");
            request.setContent(String.format("您被分配了任务：%s", event.getTaskTitle()));
            request.setType(NotificationType.TASK_ASSIGNED);
            request.setRelatedId(event.getTaskId().toString());

            notificationService.createNotification(request);

            // 可以发送邮件、推送消息等
            // emailService.sendTaskAssignedEmail(event);

            return true;

        } catch (Exception e) {
            log.error("处理任务分配事件失败：eventId={}", message.getEventId(), e);
            return false;
        }
    }
}
```

---

## 数据库操作

### MyBatis-Plus 使用

#### Entity 定义

```java
@Data
@TableName("tasks")
public class Task {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    @TableField("project_id")
    private Long projectId;

    @TableField("assignee_id")
    private Long assigneeId;

    @TableField("status")
    private String status;

    @TableField(value = "deleted", fill = FieldFill.INSERT)
    private Boolean deleted = false;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

#### Repository 定义

```java
@Mapper
public interface TaskRepository extends BaseMapper<Task> {

    /**
     * 自定义查询：获取项目的任务列表
     */
    @Select("SELECT * FROM tasks WHERE project_id = #{projectId} AND deleted = false ORDER BY position ASC")
    List<Task> selectByProject(@Param("projectId") Long projectId);

    /**
     * 自定义查询：统计任务数
     */
    @Select("SELECT COUNT(*) FROM tasks WHERE assignee_id = #{assigneeId} AND deleted = false")
    int countByAssignee(@Param("assigneeId") Long assigneeId);
}
```

#### Service 使用

```java
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getProjectTasks(Long projectId) {
        // 使用 QueryWrapper
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_id", projectId)
                    .eq("deleted", false)
                    .orderByAsc("position");

        return taskRepository.selectList(queryWrapper);
    }

    public Page<Task> getMyTasks(Long assigneeId, int page, int size) {
        // 分页查询
        Page<Task> taskPage = new Page<>(page, size);
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("assignee_id", assigneeId)
                    .eq("deleted", false);

        return taskRepository.selectPage(taskPage, queryWrapper);
    }
}
```

---

## 测试指南

### 单元测试

```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void testLogin_Success() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(true);

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
    }

    @Test
    void testLogin_UserNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest("notfound@example.com", "password123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }
}
```

### 集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("auth_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLogin() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("admin@example.com", "admin123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").exists());
    }
}
```

---

## 调试技巧

### 本地调试微服务

1. **启动基础设施**:
   ```bash
   docker-compose up -d nacos rabbitmq postgres
   ```

2. **配置启动参数**:
   ```bash
   -Dspring.profiles.active=dev
   -Dserver.port=9521
   -Dskywalking.agent.service_name=auth-service
   ```

3. **Debug 模式启动**:
   - IDEA: 右键 Run Configuration → Debug
   - 或使用 Maven: `./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"`

### 日志调试

```yaml
# application-dev.yaml
logging:
  level:
    root: INFO
    com.projecthub: DEBUG
    org.springframework.web: DEBUG
    com.projecthub.auth: DEBUG  # 特定包的详细日志
```

### 链路追踪

1. **访问 SkyWalking UI**: http://localhost:8080
2. **搜索 Trace**: 按服务名、Trace ID 搜索
3. **查看调用链**: 点击 Trace 查看详细调用过程

### 常见问题

**Q: 服务无法注册到 Nacos**
- 检查 `spring.cloud.nacos.discovery.server-addr` 配置
- 确认 Nacos 服务可用
- 查看服务日志

**Q: Feign 调用失败**
- 检查服务是否已注册
- 检查 Feign Client 定义是否正确
- 查看 Fallback Factory 日志

**Q: 消息未消费**
- 检查队列绑定
- 查看消费者日志
- 确认消息格式正确

---

## 快速开始新服务

### 1. 创建服务模板

```bash
# 复制一个现有服务作为模板
cp -r services/auth-service services/new-service

# 重命名包名
find services/new-service -type f -name "*.java" -exec sed -i '' 's/com\.projecthub\.auth/com.projecthub.newservice/g' {} \;
```

### 2. 修改配置

```yaml
# application.yaml
spring:
  application:
    name: new-service
  datasource:
    url: jdbc:postgresql://localhost:5432/newservice_db
```

### 3. 修改 pom.xml

```xml
<artifactId>new-service</artifactId>
<name>New Service</name>
```

### 4. 启动验证

```bash
cd services/new-service
./mvnw spring-boot:run

# 检查服务注册
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=new-service
```

---

**文档维护**: 开发团队
**最后更新**: 2024-03-25
