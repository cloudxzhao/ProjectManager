package com.projecthub.module.permission.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 权限申请实体类 */
@Entity
@Table(name = "sys_permission_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
public class PermissionRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private Long permissionId;

  @Column(name = "project_id")
  private Long projectId;

  @Column(nullable = false, length = 500)
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RequestStatus status = RequestStatus.PENDING;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  @LastModifiedDate
  private LocalDateTime updatedAt;

  public enum RequestStatus {
    PENDING,
    APPROVED,
    REJECTED
  }
}
