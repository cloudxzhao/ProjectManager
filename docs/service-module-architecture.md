# 服务功能模块架构设计文档

**项目名称**: ProjectHub 项目管理系统
**模块名称**: Business Service (业务服务模块)
**文档版本**: v1.0
**创建日期**: 2026-03-15

---

## 1. 概述

### 1.1 背景

ProjectHub 是一款现代化项目管理系统，采用敏捷开发理念。当前系统已支持「项目 → 用户故事 → 任务」的层级结构。

随着大型项目需求的增长，需要在项目和用户故事之间增加**服务层**，用于组织和管理复杂项目中的多个业务服务模块，形成更清晰的功能分区。

### 1.2 目标

- 支持在项目中创建多个业务服务模块
- 每个服务模块下管理相关的用户故事
- 实现服务维度的故事聚合和统计
- 提供清晰的服务边界和组织结构

### 1.3 适用范围

本文档适用于：
- 后端开发工程师（数据库设计、API 开发）
- 前端开发工程师（页面开发、组件设计）
- 测试工程师（测试用例设计）
- 产品经理（功能理解）

---

## 2. 整体架构

### 2.1 层级关系

```
┌─────────────────────────────────────────────────────────────┐
│                      Project (项目)                          │
│                   电商平台开发项目                            │
└─────────────────────────────────────────────────────────────┘
                              │
            ┌─────────────────┼─────────────────┐
            │                 │                 │
            ▼                 ▼                 ▼
┌───────────────────┐ ┌───────────────────┐ ┌───────────────────┐
│  User Service     │ │  Order Service    │ │  Payment Service  │
│  (用户服务)        │ │  (订单服务)        │ │  (支付服务)        │
└───────────────────┘ └───────────────────┘ └───────────────────┘
            │                 │                 │
      ┌─────┴─────┐     ┌─────┴─────┐     ┌─────┴─────┐
      │           │     │           │     │           │
      ▼           ▼     ▼           ▼     ▼           ▼
┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
│用户注册  │ │用户登录  │ │订单创建  │ │订单查询  │ │支付接口  │
│故事     │ │故事     │ │故事     │ │故事     │ │故事     │
└─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘
```

### 2.2 数据关系

```
┌──────────────┐       ┌───────────────────┐       ┌──────────────┐
│   Project    │ 1───N │ BusinessService   │ 1───N │  UserStory   │
│   (项目)     │       │   (业务服务)       │       │  (用户故事)   │
└──────────────┘       └───────────────────┘       └──────────────┘
       │                                                │
       │                      ┌─────────────────────────┘
       │                      │
       └──────────────────────┘ (直接归属，用于跨服务查询)
```

---

## 3. 数据库设计

### 3.1 新增表：business_service

**表名**: `business_service`
**用途**: 存储业务服务模块信息

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 主键 ID |
| project_id | BIGINT | NOT NULL, FK | 所属项目 ID |
| name | VARCHAR(100) | NOT NULL | 服务名称 |
| description | TEXT | | 服务描述 |
| code | VARCHAR(50) | NOT NULL | 服务代码标识（如 USER_SERVICE） |
| owner_id | BIGINT | FK | 服务负责人 ID |
| status | VARCHAR(20) | DEFAULT 'ACTIVE' | 状态：ACTIVE/INACTIVE/ARCHIVED |
| position | INTEGER | DEFAULT 0 | 排序位置 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | | 更新时间 |
| deleted_at | TIMESTAMP | | 删除时间（软删除） |

**索引设计**:
```sql
-- 主键索引（自动创建）
PRIMARY KEY (id)

-- 项目查询索引
CREATE INDEX idx_service_project ON business_service(project_id, deleted_at);

-- 状态查询索引
CREATE INDEX idx_service_status ON business_service(status);

-- 唯一约束：同一项目下服务代码唯一
CREATE UNIQUE INDEX uk_project_code ON business_service(project_id, code, deleted_at);
```

**外键约束**:
```sql
ALTER TABLE business_service
ADD CONSTRAINT fk_service_project
FOREIGN KEY (project_id) REFERENCES project(id);

ALTER TABLE business_service
ADD CONSTRAINT fk_service_owner
FOREIGN KEY (owner_id) REFERENCES sys_user(id);
```

---

### 3.2 修改表：user_story

**表名**: `user_story`
**用途**: 用户故事表，新增服务关联字段

**新增字段**:

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| service_id | BIGINT | FK | 所属服务 ID |

**外键约束**:
```sql
ALTER TABLE user_story
ADD CONSTRAINT fk_story_service
FOREIGN KEY (service_id) REFERENCES business_service(id);
```

**索引设计**:
```sql
-- 服务查询索引
CREATE INDEX idx_story_service ON user_story(service_id, deleted_at);

-- 项目 + 服务联合查询索引
CREATE INDEX idx_story_project_service ON user_story(project_id, service_id, deleted_at);
```

---

### 3.3 数据库迁移脚本

**文件名**: `V20260315__add_business_service_module.sql`

```sql
-- =====================================================
-- 业务服务模块 - 数据库迁移脚本
-- 版本：v1.0
-- 日期：2026-03-15
-- =====================================================

-- 1. 创建业务服务表
CREATE TABLE IF NOT EXISTS business_service (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    code VARCHAR(50) NOT NULL,
    owner_id BIGINT,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    position INTEGER DEFAULT 0 NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- 2. 添加注释
COMMENT ON TABLE business_service IS '业务服务表';
COMMENT ON COLUMN business_service.id IS '主键 ID';
COMMENT ON COLUMN business_service.project_id IS '所属项目 ID';
COMMENT ON COLUMN business_service.name IS '服务名称';
COMMENT ON COLUMN business_service.description IS '服务描述';
COMMENT ON COLUMN business_service.code IS '服务代码标识';
COMMENT ON COLUMN business_service.owner_id IS '服务负责人 ID';
COMMENT ON COLUMN business_service.status IS '状态：ACTIVE/INACTIVE/ARCHIVED';
COMMENT ON COLUMN business_service.position IS '排序位置';

-- 3. 创建索引
CREATE INDEX idx_service_project ON business_service(project_id, deleted_at);
CREATE INDEX idx_service_status ON business_service(status);
CREATE UNIQUE INDEX uk_project_code ON business_service(project_id, code, deleted_at);

-- 4. 添加外键约束
ALTER TABLE business_service
ADD CONSTRAINT fk_service_project
FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE;

ALTER TABLE business_service
ADD CONSTRAINT fk_service_owner
FOREIGN KEY (owner_id) REFERENCES sys_user(id) ON DELETE SET NULL;

-- 5. 修改用户故事表，新增服务关联字段
ALTER TABLE user_story
ADD COLUMN IF NOT EXISTS service_id BIGINT;

-- 6. 创建用户故事 - 服务外键约束
ALTER TABLE user_story
ADD CONSTRAINT fk_story_service
FOREIGN KEY (service_id) REFERENCES business_service(id) ON DELETE SET NULL;

-- 7. 创建用户故事 - 服务索引
CREATE INDEX IF NOT EXISTS idx_story_service ON user_story(service_id, deleted_at);
CREATE INDEX IF NOT EXISTS idx_story_project_service ON user_story(project_id, service_id, deleted_at);
```

---

## 4. 后端设计

### 4.1 模块目录结构

```
Builder/backend/src/main/java/com/projecthub/module/service/
├── controller/
│   ├── BusinessServiceController.java    # 服务管理接口
│   └── ServiceStoryController.java       # 服务故事关联接口
├── entity/
│   ├── BusinessService.java              # 服务实体
│   └── ServiceStatus.java                # 服务状态枚举
├── repository/
│   ├── BusinessServiceRepository.java    # 服务数据访问
│   └── ServiceStoryRepository.java       # 服务故事查询
├── service/
│   ├── BusinessServiceService.java       # 服务业务逻辑
│   └── ServiceStoryService.java          # 服务故事业务逻辑
└── dto/
    ├── BusinessServiceVO.java            # 服务响应对象
    ├── CreateServiceDTO.java             # 创建服务请求
    ├── UpdateServiceDTO.java             # 更新服务请求
    └── ServiceStoryVO.java               # 服务故事聚合视图
```

---

### 4.2 实体类设计

#### 4.2.1 ServiceStatus.java (服务状态枚举)

```java
package com.projecthub.module.service.entity;

/**
 * 业务服务状态枚举
 */
public enum ServiceStatus {
    /**
     * 活跃 - 正常使用的服务
     */
    ACTIVE,

    /**
     * 非活跃 - 暂时停用的服务
     */
    INACTIVE,

    /**
     * 归档 - 已完成或废弃的服务
     */
    ARCHIVED
}
```

---

#### 4.2.2 BusinessService.java (业务服务实体)

```java
package com.projecthub.module.service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 业务服务实体类
 */
@Entity
@Table(name = "business_service")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE business_service SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class BusinessService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "owner_id")
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ServiceStatus status = ServiceStatus.ACTIVE;

    @Column(nullable = false)
    private Integer position = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
```

---

### 4.3 Repository 设计

#### 4.3.1 BusinessServiceRepository.java

```java
package com.projecthub.module.service.repository;

import com.projecthub.module.service.entity.BusinessService;
import com.projecthub.module.service.entity.ServiceStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 业务服务数据访问层
 */
@Repository
public interface BusinessServiceRepository extends JpaRepository<BusinessService, Long> {

    /**
     * 查询项目下的所有服务（按位置排序）
     */
    @Query("SELECT s FROM BusinessService s WHERE s.projectId = :projectId " +
           "AND s.deletedAt IS NULL ORDER BY s.position ASC")
    List<BusinessService> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 查询项目下的活跃服务
     */
    @Query("SELECT s FROM BusinessService s WHERE s.projectId = :projectId " +
           "AND s.status = :status AND s.deletedAt IS NULL ORDER BY s.position ASC")
    List<BusinessService> findByProjectIdAndStatus(
        @Param("projectId") Long projectId,
        @Param("status") ServiceStatus status
    );

    /**
     * 根据服务代码查询
     */
    @Query("SELECT s FROM BusinessService s WHERE s.projectId = :projectId " +
           "AND s.code = :code AND s.deletedAt IS NULL")
    Optional<BusinessService> findByProjectIdAndCode(
        @Param("projectId") Long projectId,
        @Param("code") String code
    );

    /**
     * 查询最大位置值
     */
    @Query("SELECT COALESCE(MAX(s.position), 0) FROM BusinessService s " +
           "WHERE s.projectId = :projectId AND s.deletedAt IS NULL")
    Integer findMaxPosition(@Param("projectId") Long projectId);

    /**
     * 统计项目下的服务数量
     */
    @Query("SELECT COUNT(s) FROM BusinessService s WHERE s.projectId = :projectId " +
           "AND s.deletedAt IS NULL")
    Long countByProjectId(@Param("projectId") Long projectId);

    /**
     * 统计服务下的故事数量
     */
    @Query("SELECT COUNT(st) FROM UserStory st WHERE st.serviceId = :serviceId " +
           "AND st.deletedAt IS NULL")
    Long countStoriesByServiceId(@Param("serviceId") Long serviceId);
}
```

---

### 4.4 Service 层设计

#### 4.4.1 BusinessServiceService.java

```java
package com.projecthub.module.service.service;

import com.projecthub.common.constant.ErrorCode;
import com.projecthub.common.exception.BusinessException;
import com.projecthub.module.project.service.PermissionService;
import com.projecthub.module.service.dto.BusinessServiceVO;
import com.projecthub.module.service.entity.BusinessService;
import com.projecthub.module.service.entity.ServiceStatus;
import com.projecthub.module.service.repository.BusinessServiceRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 业务服务业务逻辑层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessServiceService {

    private final BusinessServiceRepository businessServiceRepository;
    private final PermissionService permissionService;

    /**
     * 创建服务
     */
    @Transactional
    public BusinessServiceVO createService(Long projectId, BusinessServiceVO.CreateRequest request) {
        Long userId = getCurrentUserId();

        // 权限校验
        if (!permissionService.hasPermission(userId, projectId, "SERVICE_CREATE")) {
            throw new BusinessException(ErrorCode.SERVICE_PERMISSION_DENIED, "无创建服务权限");
        }

        // 检查服务代码是否已存在
        if (businessServiceRepository.findByProjectIdAndCode(projectId, request.getCode()).isPresent()) {
            throw new BusinessException(ErrorCode.SERVICE_CODE_EXISTS, "服务代码已存在");
        }

        // 获取最大位置
        Integer maxPosition = businessServiceRepository.findMaxPosition(projectId);

        // 创建服务
        BusinessService service = BusinessService.builder()
            .projectId(projectId)
            .name(request.getName())
            .description(request.getDescription())
            .code(request.getCode())
            .ownerId(request.getOwnerId() != null ? request.getOwnerId() : userId)
            .status(ServiceStatus.ACTIVE)
            .position(maxPosition + 1)
            .build();

        businessServiceRepository.save(service);
        log.info("创建服务成功：serviceId={}, projectId={}", service.getId(), projectId);

        return toVO(service);
    }

    /**
     * 获取服务详情
     */
    @Transactional(readOnly = true)
    public BusinessServiceVO getService(Long serviceId) {
        BusinessService service = businessServiceRepository.findById(serviceId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SERVICE_NOT_FOUND, "服务不存在"));
        return toVO(service);
    }

    /**
     * 更新服务
     */
    @Transactional
    public BusinessServiceVO updateService(Long serviceId, BusinessServiceVO.UpdateRequest request) {
        BusinessService service = businessServiceRepository.findById(serviceId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SERVICE_NOT_FOUND, "服务不存在"));

        // 权限校验
        checkServicePermission(service.getProjectId(), "SERVICE_EDIT");

        // 更新字段
        if (request.getName() != null) {
            service.setName(request.getName());
        }
        if (request.getDescription() != null) {
            service.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            service.setStatus(ServiceStatus.valueOf(request.getStatus()));
        }
        if (request.getOwnerId() != null) {
            service.setOwnerId(request.getOwnerId());
        }

        businessServiceRepository.save(service);
        log.info("更新服务成功：serviceId={}", serviceId);

        return toVO(service);
    }

    /**
     * 删除服务
     */
    @Transactional
    public void deleteService(Long serviceId) {
        BusinessService service = businessServiceRepository.findById(serviceId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SERVICE_NOT_FOUND, "服务不存在"));

        // 权限校验
        checkServicePermission(service.getProjectId(), "SERVICE_DELETE");

        // 检查服务下是否还有故事
        Long storyCount = businessServiceRepository.countStoriesByServiceId(serviceId);
        if (storyCount > 0) {
            throw new BusinessException(ErrorCode.SERVICE_HAS_STORIES,
                "服务下还有 " + storyCount + " 个用户故事，无法删除");
        }

        businessServiceRepository.delete(service);
        log.info("删除服务成功：serviceId={}", serviceId);
    }

    /**
     * 获取项目下的服务列表
     */
    @Transactional(readOnly = true)
    public List<BusinessServiceVO> listServices(Long projectId) {
        List<BusinessService> services = businessServiceRepository.findByProjectId(projectId);
        return services.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 移动服务位置
     */
    @Transactional
    public BusinessServiceVO moveService(Long serviceId, Integer newPosition) {
        BusinessService service = businessServiceRepository.findById(serviceId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SERVICE_NOT_FOUND, "服务不存在"));

        checkServicePermission(service.getProjectId(), "SERVICE_EDIT");

        service.setPosition(newPosition);
        businessServiceRepository.save(service);
        log.info("移动服务位置：serviceId={}, newPosition={}", serviceId, newPosition);

        return toVO(service);
    }

    /**
     * VO 转换
     */
    private BusinessServiceVO toVO(BusinessService service) {
        BusinessServiceVO vo = BusinessServiceVO.builder()
            .id(service.getId())
            .projectId(service.getProjectId())
            .name(service.getName())
            .description(service.getDescription())
            .code(service.getCode())
            .ownerId(service.getOwnerId())
            .status(service.getStatus().name())
            .position(service.getPosition())
            .createdAt(service.getCreatedAt())
            .updatedAt(service.getUpdatedAt())
            .build();

        // 统计故事数量
        Long storyCount = businessServiceRepository.countStoriesByServiceId(service.getId());
        vo.setStoryCount(storyCount.intValue());

        return vo;
    }

    /**
     * 检查服务权限
     */
    private void checkServicePermission(Long projectId, String permissionCode) {
        Long userId = getCurrentUserId();
        if (!permissionService.hasPermission(userId, projectId, permissionCode)) {
            throw new BusinessException(ErrorCode.SERVICE_PERMISSION_DENIED, "权限不足");
        }
    }

    /**
     * 获取当前用户 ID
     */
    private Long getCurrentUserId() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication().getPrincipal();
        if (principal instanceof com.projecthub.security.UserDetailsImpl) {
            return ((com.projecthub.security.UserDetailsImpl) principal).getId();
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
    }
}
```

---

### 4.5 DTO/VO 设计

#### 4.5.1 BusinessServiceVO.java

```java
package com.projecthub.module.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.*;

/**
 * 业务服务响应对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessServiceVO {

    private Long id;
    private Long projectId;
    private String name;
    private String description;
    private String code;
    private Long ownerId;
    private String ownerName;  // 负责人姓名（可选）
    private String status;     // ACTIVE, INACTIVE, ARCHIVED
    private Integer position;
    private Integer storyCount; // 故事数量

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 创建服务请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "服务名称不能为空")
        @Size(max = 100, message = "服务名称最多 100 字符")
        private String name;

        @Size(max = 500, message = "服务描述最多 500 字符")
        private String description;

        @NotBlank(message = "服务代码不能为空")
        @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "服务代码必须是大写字母、数字、下划线，且以字母开头")
        private String code;

        private Long ownerId;
    }

    /**
     * 更新服务请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(max = 100, message = "服务名称最多 100 字符")
        private String name;

        @Size(max = 500, message = "服务描述最多 500 字符")
        private String description;

        private String status;

        private Long ownerId;
    }
}
```

---

### 4.6 Controller 设计

#### 4.6.1 BusinessServiceController.java

```java
package com.projecthub.module.service.controller;

import com.projecthub.common.response.Result;
import com.projecthub.module.service.dto.BusinessServiceVO;
import com.projecthub.module.service.service.BusinessServiceService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 业务服务管理接口
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/services")
@RequiredArgsConstructor
@Validated
public class BusinessServiceController {

    private final BusinessServiceService businessServiceService;

    /**
     * 获取项目下的服务列表
     */
    @GetMapping
    public Result<List<BusinessServiceVO>> listServices(@PathVariable Long projectId) {
        List<BusinessServiceVO> services = businessServiceService.listServices(projectId);
        return Result.success(services);
    }

    /**
     * 创建服务
     */
    @PostMapping
    public Result<BusinessServiceVO> createService(
        @PathVariable Long projectId,
        @RequestBody @Valid BusinessServiceVO.CreateRequest request
    ) {
        BusinessServiceVO service = businessServiceService.createService(projectId, request);
        return Result.success(service);
    }

    /**
     * 获取服务详情
     */
    @GetMapping("/{serviceId}")
    public Result<BusinessServiceVO> getService(
        @PathVariable Long projectId,
        @PathVariable Long serviceId
    ) {
        BusinessServiceVO service = businessServiceService.getService(serviceId);
        return Result.success(service);
    }

    /**
     * 更新服务
     */
    @PutMapping("/{serviceId}")
    public Result<BusinessServiceVO> updateService(
        @PathVariable Long projectId,
        @PathVariable Long serviceId,
        @RequestBody @Valid BusinessServiceVO.UpdateRequest request
    ) {
        BusinessServiceVO service = businessServiceService.updateService(serviceId, request);
        return Result.success(service);
    }

    /**
     * 删除服务
     */
    @DeleteMapping("/{serviceId}")
    public Result<Void> deleteService(
        @PathVariable Long projectId,
        @PathVariable Long serviceId
    ) {
        businessServiceService.deleteService(serviceId);
        return Result.success();
    }

    /**
     * 移动服务位置
     */
    @PostMapping("/{serviceId}/move")
    public Result<BusinessServiceVO> moveService(
        @PathVariable Long projectId,
        @PathVariable Long serviceId,
        @RequestParam Integer newPosition
    ) {
        BusinessServiceVO service = businessServiceService.moveService(serviceId, newPosition);
        return Result.success(service);
    }
}
```

---

### 4.7 错误码设计

在 `ErrorCode.java` 中新增服务相关错误码：

```java
// 服务模块 6000-6999
SERVICE_NOT_FOUND(6001, "服务不存在"),
SERVICE_CODE_EXISTS(6002, "服务代码已存在"),
SERVICE_PERMISSION_DENIED(6003, "无服务访问权限"),
SERVICE_HAS_STORIES(6004, "服务下还有用户故事，无法删除"),
SERVICE_INVALID_STATUS(6005, "无效的服务状态"),
```

---

## 5. 前端设计

### 5.1 目录结构

```
Builder/frontend/src/
├── app/(dashboard)/projects/[id]/
│   └── services/
│       ├── page.tsx                    # 服务列表页
│       ├── create/
│       │   └── page.tsx                # 创建服务页
│       └── [serviceId]/
│           ├── page.tsx                # 服务详情页
│           └── stories/
│               └── page.tsx            # 服务下故事管理
├── components/services/
│   ├── ServiceList.tsx                 # 服务列表组件
│   ├── ServiceCard.tsx                 # 服务卡片组件
│   ├── ServiceForm.tsx                 # 服务表单组件
│   ├── ServiceSelector.tsx             # 服务选择器组件
│   ├── ServiceStats.tsx                # 服务统计组件
│   └── ServiceStatusTag.tsx            # 服务状态标签
├── lib/api/
│   └── service.ts                      # 服务 API 客户端
└── types/
    └── service.ts                      # 服务 TypeScript 类型
```

---

### 5.2 类型定义

#### 5.2.1 types/service.ts

```typescript
/**
 * 业务服务类型定义
 */

/**
 * 服务状态枚举
 */
export enum ServiceStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  ARCHIVED = 'ARCHIVED',
}

/**
 * 业务服务对象
 */
export interface BusinessService {
  id: number;
  projectId: number;
  name: string;
  description: string | null;
  code: string;
  ownerId: number | null;
  ownerName?: string | null;
  status: ServiceStatus;
  position: number;
  storyCount: number;
  createdAt: string;
  updatedAt: string | null;
}

/**
 * 创建服务请求
 */
export interface CreateServiceRequest {
  name: string;
  description?: string;
  code: string;
  ownerId?: number;
}

/**
 * 更新服务请求
 */
export interface UpdateServiceRequest {
  name?: string;
  description?: string;
  status?: ServiceStatus;
  ownerId?: number;
}

/**
 * 服务列表查询参数
 */
export interface ServiceListParams {
  projectId: number;
  status?: ServiceStatus;
}
```

---

### 5.3 API 客户端

#### 5.3.1 lib/api/service.ts

```typescript
import axios from 'axios';
import type {
  BusinessService,
  CreateServiceRequest,
  UpdateServiceRequest,
} from '@/types/service';
import type { Result } from './types';

const BASE_URL = '/api/v1';

/**
 * 获取项目下的服务列表
 */
export async function getServices(projectId: number): Promise<Result<BusinessService[]>> {
  const response = await axios.get(`${BASE_URL}/projects/${projectId}/services`);
  return response.data;
}

/**
 * 获取服务详情
 */
export async function getService(
  projectId: number,
  serviceId: number
): Promise<Result<BusinessService>> {
  const response = await axios.get(`${BASE_URL}/projects/${projectId}/services/${serviceId}`);
  return response.data;
}

/**
 * 创建服务
 */
export async function createService(
  projectId: number,
  request: CreateServiceRequest
): Promise<Result<BusinessService>> {
  const response = await axios.post(`${BASE_URL}/projects/${projectId}/services`, request);
  return response.data;
}

/**
 * 更新服务
 */
export async function updateService(
  projectId: number,
  serviceId: number,
  request: UpdateServiceRequest
): Promise<Result<BusinessService>> {
  const response = await axios.put(
    `${BASE_URL}/projects/${projectId}/services/${serviceId}`,
    request
  );
  return response.data;
}

/**
 * 删除服务
 */
export async function deleteService(
  projectId: number,
  serviceId: number
): Promise<Result<void>> {
  const response = await axios.delete(
    `${BASE_URL}/projects/${projectId}/services/${serviceId}`
  );
  return response.data;
}

/**
 * 移动服务位置
 */
export async function moveService(
  projectId: number,
  serviceId: number,
  newPosition: number
): Promise<Result<BusinessService>> {
  const response = await axios.post(
    `${BASE_URL}/projects/${projectId}/services/${serviceId}/move`,
    null,
    { params: { newPosition } }
  );
  return response.data;
}
```

---

### 5.4 组件设计

#### 5.4.1 ServiceCard.tsx (服务卡片组件)

```tsx
import React from 'react';
import { Card, Typography, Tag, Progress, Space, Button, Tooltip } from 'antd';
import {
  CodeOutlined,
  UserOutlined,
  EditOutlined,
  DeleteOutlined,
  UnorderedListOutlined
} from '@ant-design/icons';
import type { BusinessService } from '@/types/service';
import { ServiceStatus } from '@/types/service';

const { Title, Text } = Typography;

interface ServiceCardProps {
  service: BusinessService;
  onEdit?: (service: BusinessService) => void;
  onDelete?: (service: BusinessService) => void;
  onViewStories?: (service: BusinessService) => void;
}

export const ServiceCard: React.FC<ServiceCardProps> = ({
  service,
  onEdit,
  onDelete,
  onViewStories,
}) => {
  // 状态颜色映射
  const getStatusColor = (status: ServiceStatus): string => {
    switch (status) {
      case ServiceStatus.ACTIVE:
        return 'green';
      case ServiceStatus.INACTIVE:
        return 'orange';
      case ServiceStatus.ARCHIVED:
        return 'gray';
      default:
        return 'default';
    }
  };

  // 状态中文映射
  const getStatusText = (status: ServiceStatus): string => {
    switch (status) {
      case ServiceStatus.ACTIVE:
        return '活跃';
      case ServiceStatus.INACTIVE:
        return '停用';
      case ServiceStatus.ARCHIVED:
        return '归档';
    }
  };

  return (
    <Card
      hoverable
      actions={[
        <Tooltip title="查看故事" key="stories">
          <Button
            type="link"
            icon={<UnorderedListOutlined />}
            onClick={() => onViewStories?.(service)}
          />
        </Tooltip>,
        <Tooltip title="编辑" key="edit">
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => onEdit?.(service)}
          />
        </Tooltip>,
        <Tooltip title="删除" key="delete">
          <Button
            type="link"
            danger
            icon={<DeleteOutlined />}
            onClick={() => onDelete?.(service)}
          />
        </Tooltip>,
      ]}
    >
      <Card.Meta
        title={
          <Space>
            <Title level={5} style={{ marginBottom: 0 }}>
              {service.name}
            </Title>
            <Tag color={getStatusColor(service.status)}>
              {getStatusText(service.status)}
            </Tag>
          </Space>
        }
        description={
          <div>
            <Space direction="vertical" size="small" style={{ width: '100%' }}>
              <Text type="secondary" ellipsis>{service.description}</Text>

              <Space split={<span>|</span>}>
                <Space>
                  <CodeOutlined />
                  <Text code>{service.code}</Text>
                </Space>
                {service.ownerName && (
                  <Space>
                    <UserOutlined />
                    <Text>{service.ownerName}</Text>
                  </Space>
                )}
              </Space>

              <Space>
                <Text type="secondary">故事进度</Text>
                <Progress
                  percent={Math.round((service.storyCount / 20) * 100)}
                  size="small"
                  showInfo={false}
                />
                <Text type="secondary">{service.storyCount} 个故事</Text>
              </Space>
            </Space>
          </div>
        }
      />
    </Card>
  );
};
```

---

#### 5.4.2 ServiceForm.tsx (服务表单组件)

```tsx
import React, { useEffect } from 'react';
import { Form, Input, Select, Button, message } from 'antd';
import type { FormProps } from 'antd';
import type { BusinessService, CreateServiceRequest, UpdateServiceRequest } from '@/types/service';
import { ServiceStatus } from '@/types/service';

const { TextArea } = Input;

interface ServiceFormProps {
  initialValues?: BusinessService;
  onSubmit?: (values: CreateServiceRequest | UpdateServiceRequest) => void;
  onCancel?: () => void;
  loading?: boolean;
  mode?: 'create' | 'edit';
}

export const ServiceForm: React.FC<ServiceFormProps> = ({
  initialValues,
  onSubmit,
  onCancel,
  loading = false,
  mode = 'create',
}) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (initialValues) {
      form.setFieldsValue({
        name: initialValues.name,
        description: initialValues.description,
        code: initialValues.code,
        ownerId: initialValues.ownerId,
        status: initialValues.status,
      });
    }
  }, [initialValues, form]);

  const handleSubmit: FormProps['onFinish'] = async (values) => {
    try {
      await onSubmit?.(values);
      message.success(mode === 'create' ? '服务创建成功' : '服务更新成功');
      form.resetFields();
    } catch (error) {
      message.error(mode === 'create' ? '服务创建失败' : '服务更新失败');
    }
  };

  return (
    <Form
      form={form}
      layout="vertical"
      onFinish={handleSubmit}
      initialValues={{ status: ServiceStatus.ACTIVE }}
    >
      <Form.Item
        name="name"
        label="服务名称"
        rules={[
          { required: true, message: '请输入服务名称' },
          { max: 100, message: '服务名称最多 100 字符' },
        ]}
      >
        <Input placeholder="例如：用户服务" />
      </Form.Item>

      <Form.Item
        name="code"
        label="服务代码"
        rules={[
          { required: true, message: '请输入服务代码' },
          {
            pattern: /^[A-Z][A-Z0-9_]*$/,
            message: '服务代码必须是大写字母、数字、下划线，且以字母开头'
          },
        ]}
        extra="用于代码中引用，例如：USER_SERVICE"
      >
        <Input placeholder="例如：USER_SERVICE" disabled={mode === 'edit'} />
      </Form.Item>

      <Form.Item
        name="description"
        label="服务描述"
        rules={[{ max: 500, message: '服务描述最多 500 字符' }]}
      >
        <TextArea rows={4} placeholder="描述服务的功能和职责" />
      </Form.Item>

      {mode === 'edit' && (
        <Form.Item
          name="status"
          label="服务状态"
          rules={[{ required: true }]}
        >
          <Select>
            <Select.Option value={ServiceStatus.ACTIVE}>活跃</Select.Option>
            <Select.Option value={ServiceStatus.INACTIVE}>停用</Select.Option>
            <Select.Option value={ServiceStatus.ARCHIVED}>归档</Select.Option>
          </Select>
        </Form.Item>
      )}

      <Form.Item>
        <Space>
          <Button type="primary" htmlType="submit" loading={loading}>
            {mode === 'create' ? '创建' : '保存'}
          </Button>
          <Button onClick={onCancel}>
            取消
          </Button>
        </Space>
      </Form.Item>
    </Form>
  );
};
```

---

#### 5.4.3 ServiceSelector.tsx (服务选择器组件)

```tsx
import React, { useEffect, useState } from 'react';
import { Select, Spin } from 'antd';
import { getServices } from '@/lib/api/service';
import type { BusinessService } from '@/types/service';

interface ServiceSelectorProps {
  projectId: number;
  value?: number;
  onChange?: (serviceId: number | null) => void;
  placeholder?: string;
  allowClear?: boolean;
}

export const ServiceSelector: React.FC<ServiceSelectorProps> = ({
  projectId,
  value,
  onChange,
  placeholder = '请选择所属服务',
  allowClear = true,
}) => {
  const [services, setServices] = useState<BusinessService[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadServices();
  }, [projectId]);

  const loadServices = async () => {
    try {
      setLoading(true);
      const result = await getServices(projectId);
      if (result.code === 200) {
        // 只显示活跃服务
        setServices(result.data.filter(s => s.status === 'ACTIVE'));
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Select
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      allowClear={allowClear}
      loading={loading}
      style={{ width: '100%' }}
    >
      {services.map((service) => (
        <Select.Option key={service.id} value={service.id}>
          {service.name} ({service.code})
        </Select.Option>
      ))}
    </Select>
  );
};
```

---

### 5.5 页面设计

#### 5.5.1 services/page.tsx (服务列表页)

```tsx
'use client';

import React, { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import {
  PageHeader,
  Button,
  Row,
  Col,
  Modal,
  message,
  Empty,
  Space,
  Typography
} from 'antd';
import { PlusOutlined, UnorderedListOutlined } from '@ant-design/icons';
import type { BusinessService } from '@/types/service';
import { getServices, deleteService } from '@/lib/api/service';
import { ServiceCard } from '@/components/services/ServiceCard';
import { ServiceForm } from '@/components/services/ServiceForm';

const { Title } = Typography;

export default function ServicesPage() {
  const params = useParams();
  const router = useRouter();
  const projectId = Number(params.id);

  const [services, setServices] = useState<BusinessService[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingService, setEditingService] = useState<BusinessService | undefined>();
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadServices();
  }, [projectId]);

  const loadServices = async () => {
    try {
      setLoading(true);
      const result = await getServices(projectId);
      if (result.code === 200) {
        setServices(result.data);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingService(undefined);
    setModalVisible(true);
  };

  const handleEdit = (service: BusinessService) => {
    setEditingService(service);
    setModalVisible(true);
  };

  const handleDelete = async (service: BusinessService) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除服务 "${service.name}" 吗？`,
      onOk: async () => {
        try {
          const result = await deleteService(projectId, service.id);
          if (result.code === 200) {
            message.success('服务删除成功');
            loadServices();
          }
        } catch (error: any) {
          message.error(error.response?.data?.message || '删除失败');
        }
      },
    });
  };

  const handleViewStories = (service: BusinessService) => {
    router.push(`/projects/${projectId}/services/${service.id}/stories`);
  };

  const handleSubmit = async (values: any) => {
    try {
      setSubmitting(true);
      // TODO: 调用创建/更新 API
      message.success(editingService ? '服务更新成功' : '服务创建成功');
      setModalVisible(false);
      loadServices();
    } catch (error: any) {
      message.error(error.response?.data?.message || '操作失败');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <PageHeader
        title={
          <Space>
            <UnorderedListOutlined />
            <Title level={4} style={{ margin: 0 }}>服务管理</Title>
          </Space>
        }
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            新建服务
          </Button>
        }
      />

      {services.length === 0 && !loading ? (
        <Empty
          description="暂无服务，点击右上角创建第一个服务"
          image={Empty.PRESENTED_IMAGE_SIMPLE}
        />
      ) : (
        <Row gutter={[16, 16]}>
          {services.map((service) => (
            <Col key={service.id} xs={24} sm={12} lg={8} xl={6}>
              <ServiceCard
                service={service}
                onEdit={handleEdit}
                onDelete={handleDelete}
                onViewStories={handleViewStories}
              />
            </Col>
          ))}
        </Row>
      )}

      <Modal
        title={editingService ? '编辑服务' : '创建服务'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={600}
      >
        <ServiceForm
          mode={editingService ? 'edit' : 'create'}
          initialValues={editingService}
          onSubmit={handleSubmit}
          onCancel={() => setModalVisible(false)}
          loading={submitting}
        />
      </Modal>
    </div>
  );
}
```

---

## 6. 用户故事集成

### 6.1 UserStory 实体修改

在现有 `UserStory.java` 中添加 `serviceId` 字段：

```java
// 新增字段
@Column(name = "service_id")
private Long serviceId;

// 新增关联查询
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "service_id", insertable = false, updatable = false)
private BusinessService businessService;
```

### 6.2 UserStoryVO 扩展

```java
@Data
@Builder
public class UserStoryVO {
    // ... 现有字段

    private Long serviceId;
    private String serviceName;  // 服务名称
}
```

### 6.3 用户故事表单集成

在用户故事创建/编辑表单中，集成 `ServiceSelector` 组件：

```tsx
import { ServiceSelector } from '@/components/services/ServiceSelector';

// 在表单中添加
<Form.Item name="serviceId" label="所属服务">
  <ServiceSelector
    projectId={projectId}
    allowClear
    placeholder="选择关联的服务（可选）"
  />
</Form.Item>
```

---

## 7. 权限设计

### 7.1 服务权限码

| 权限码 | 说明 |
|--------|------|
| SERVICE_CREATE | 创建服务 |
| SERVICE_EDIT | 编辑服务 |
| SERVICE_DELETE | 删除服务 |
| SERVICE_VIEW | 查看服务 |

### 7.2 权限校验逻辑

服务权限继承自项目权限体系：
- PROJECT_OWNER: 所有服务权限
- PROJECT_MANAGER: 除删除外的服务权限
- PROJECT_MEMBER: 仅查看权限

---

## 8. 接口文档

### 8.1 服务管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/projects/{projectId}/services` | 获取服务列表 |
| POST | `/api/v1/projects/{projectId}/services` | 创建服务 |
| GET | `/api/v1/projects/{projectId}/services/{id}` | 获取服务详情 |
| PUT | `/api/v1/projects/{projectId}/services/{id}` | 更新服务 |
| DELETE | `/api/v1/projects/{projectId}/services/{id}` | 删除服务 |
| POST | `/api/v1/projects/{projectId}/services/{id}/move` | 移动服务位置 |

### 8.2 请求/响应示例

#### 创建服务

**请求**:
```http
POST /api/v1/projects/1/services
Content-Type: application/json

{
  "name": "用户服务",
  "description": "负责用户管理相关功能",
  "code": "USER_SERVICE",
  "ownerId": 1
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "projectId": 1,
    "name": "用户服务",
    "description": "负责用户管理相关功能",
    "code": "USER_SERVICE",
    "ownerId": 1,
    "status": "ACTIVE",
    "position": 1,
    "storyCount": 0,
    "createdAt": "2026-03-15 10:00:00"
  }
}
```

---

## 9. 测试计划

### 9.1 后端测试

| 测试项 | 说明 |
|--------|------|
| 单元测试 | Service 层业务逻辑测试 |
| 集成测试 | Controller 层 API 测试 |
| 数据库测试 | Flyway 迁移脚本测试 |

### 9.2 前端测试

| 测试项 | 说明 |
|--------|------|
| 组件测试 | ServiceCard, ServiceForm 组件测试 |
| E2E 测试 | 创建、编辑、删除服务完整流程 |

---

## 10. 部署计划

### 10.1 依赖检查

- [ ] PostgreSQL 15+
- [ ] Flyway 迁移
- [ ] 后端编译通过
- [ ] 前端编译通过

### 10.2 上线步骤

1. 执行数据库迁移脚本
2. 部署后端服务
3. 部署前端应用
4. 验证 API 可用性
5. 验证前端功能

---

## 附录

### A. 术语表

| 术语 | 说明 |
|------|------|
| Business Service | 业务服务，项目下的功能模块 |
| User Story | 用户故事，敏捷开发中的需求单元 |
| Project | 项目，最高层级的组织单元 |

### B. 参考文档

- [项目后端开发规范](./CLAUDE.md)
- [项目前端开发规范](../frontend/README.md)
- [数据库设计规范](./docs/database.md)
