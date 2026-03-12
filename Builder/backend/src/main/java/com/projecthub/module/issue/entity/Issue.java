package com.projecthub.module.issue.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 问题实体类 */
@Entity
@Table(name = "issue")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE issue SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Issue {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "project_id", nullable = false)
  private Long projectId;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(nullable = false, length = 20)
  private IssueType type = IssueType.BUG;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(nullable = false, length = 20)
  private Severity severity = Severity.NORMAL;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(nullable = false, length = 20)
  private IssueStatus status = IssueStatus.NEW;

  @Column(name = "assignee_id")
  private Long assigneeId;

  @Column(name = "reporter_id", nullable = false)
  private Long reporterId;

  @Column(name = "found_date", nullable = false)
  private LocalDate foundDate;

  @Column(name = "resolved_date")
  private LocalDate resolvedDate;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  /** 问题类型枚举 */
  public enum IssueType {
    BUG,
    ISSUE,
    IMPROVEMENT,
    TECH_DEBT
  }

  /** 严重程度枚举 */
  public enum Severity {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
  }

  /** 问题状态枚举 */
  public enum IssueStatus {
    NEW,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    REOPENED
  }
}
