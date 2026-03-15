package com.projecthub.module.permission.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 权限审批记录实体类 */
@Entity
@Table(name = "sys_permission_approval")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
public class PermissionApproval {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long requestId;

  @Column(nullable = false)
  private Long approverId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ApprovalAction action;

  @Column(length = 500)
  private String comment;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreatedDate
  private LocalDateTime createdAt;

  public enum ApprovalAction {
    APPROVE,
    REJECT
  }
}
