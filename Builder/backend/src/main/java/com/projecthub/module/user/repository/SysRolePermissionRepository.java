package com.projecthub.module.user.repository;

import com.projecthub.module.user.entity.SysRolePermission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 角色权限关联 Repository 接口 */
@Repository
public interface SysRolePermissionRepository extends JpaRepository<SysRolePermission, Long> {

  /** 根据角色 ID 列表查询权限 ID 列表 */
  @Query("SELECT srp.permissionId FROM SysRolePermission srp WHERE srp.roleId IN :roleIds")
  List<Long> findPermissionIdsByRoleIds(@Param("roleIds") List<Long> roleIds);

  /** 根据角色 ID 列表查询角色权限关联列表 */
  @Query("SELECT srp FROM SysRolePermission srp WHERE srp.roleId IN :roleIds")
  List<SysRolePermission> findAllByRoleIds(@Param("roleIds") List<Long> roleIds);

  /** 根据角色 ID 删除角色权限关联 */
  void deleteByRoleId(Long roleId);
}
