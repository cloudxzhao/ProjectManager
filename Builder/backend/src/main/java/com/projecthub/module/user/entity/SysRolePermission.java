package com.projecthub.module.user.entity;

import jakarta.persistence.*;
import lombok.*;

/** 角色权限关联实体类 */
@Entity
@Table(name = "sys_role_permission")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysRolePermission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "role_id", nullable = false)
  private Long roleId;

  @Column(name = "permission_id", nullable = false)
  private Long permissionId;
}
