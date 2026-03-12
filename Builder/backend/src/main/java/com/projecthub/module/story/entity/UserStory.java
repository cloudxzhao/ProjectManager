package com.projecthub.module.story.entity;

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

/** 用户故事实体类 */
@Entity
@Table(name = "user_story")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE user_story SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class UserStory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "epic_id")
  private Long epicId;

  @Column(name = "project_id", nullable = false)
  private Long projectId;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "acceptance_criteria", columnDefinition = "TEXT")
  private String acceptanceCriteria;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(nullable = false, length = 20)
  private Priority priority = Priority.MEDIUM;

  @Column(name = "story_points")
  private Integer storyPoints;

  @Column(name = "assignee_id")
  private Long assigneeId;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(nullable = false, length = 20)
  private TaskStatus status = TaskStatus.TODO;

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

  /** 优先级枚举 */
  public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
  }

  /** 任务状态枚举 */
  public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    IN_REVIEW,
    DONE
  }
}
