package com.projecthub.module.task.entity;

import com.projecthub.common.constant.TaskStatus;
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

/** 任务实体类 */
@Entity
@Table(name = "task")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE task SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Task {

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
  private TaskStatus status = TaskStatus.TODO;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(nullable = false, length = 20)
  private Priority priority = Priority.MEDIUM;

  @Column(name = "assignee_id")
  private Long assigneeId;

  @Column(name = "creator_id", nullable = false)
  private Long creatorId;

  @Column(name = "parent_id")
  private Long parentId;

  @Column(name = "user_story_id")
  private Long userStoryId;

  @Column(name = "epic_id")
  private Long epicId;

  @Column(name = "due_date")
  private LocalDate dueDate;

  @Column(name = "story_points")
  private Integer storyPoints;

  @Column(nullable = false)
  private Integer position = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  @LastModifiedDate
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
  }
}
