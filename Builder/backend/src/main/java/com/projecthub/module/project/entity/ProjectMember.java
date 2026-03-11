package com.projecthub.module.project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

/** 项目成员实体类 */
@Entity
@Table(
    name = "project_member",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"project_id", "user_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "project_id", nullable = false)
  private Long projectId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProjectMemberRole role = ProjectMemberRole.MEMBER;

  @Column(name = "joined_at", nullable = false)
  @CreatedDate
  private LocalDateTime joinedAt;

  public enum ProjectMemberRole {
    OWNER,
    MANAGER,
    MEMBER
  }
}
