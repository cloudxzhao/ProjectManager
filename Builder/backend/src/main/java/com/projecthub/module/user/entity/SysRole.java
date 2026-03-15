package com.projecthub.module.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

/** 系统角色实体类 */
@Entity
@Table(name = "sys_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysRole {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String name;

  @Column(nullable = false, unique = true, length = 50)
  private String code;

  @Column(length = 255)
  private String description;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreatedDate
  private LocalDateTime createdAt;
}
