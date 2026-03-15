package com.projecthub.module.user.repository;

import com.projecthub.module.user.entity.SysPermission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 系统权限 Repository 接口 */
@Repository
public interface SysPermissionRepository extends JpaRepository<SysPermission, Long> {

  /** 根据权限编码查询权限 */
  Optional<SysPermission> findByCode(String code);

  /** 根据权限 ID 列表查询权限 */
  @Query("SELECT p FROM SysPermission p WHERE p.id IN :ids")
  List<SysPermission> findAllByIds(@Param("ids") List<Long> ids);
}
