package com.projecthub.module.log.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 操作日志实体类 */
@Entity
@Table(name = "operation_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class OperationLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "username")
  private String username;

  @Column(nullable = false, length = 50)
  private String module;

  @Column(nullable = false, length = 50)
  private String operation;

  @Column(length = 200)
  private String method;

  @Column(columnDefinition = "TEXT")
  private String params;

  @Column(columnDefinition = "TEXT")
  private String result;

  @Column(name = "ip_address", length = 50)
  private String ipAddress;

  @Column(nullable = false)
  private Long duration;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}