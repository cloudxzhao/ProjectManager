package com.projecthub.module.user.repository;

import com.projecthub.module.user.entity.SysRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 系统角色 Repository 接口 */
@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

  /** 根据角色编码查询角色 */
  Optional<SysRole> findByCode(String code);

  /** 根据角色 ID 列表查询角色 */
  @Query("SELECT r FROM SysRole r WHERE r.id IN :ids")
  List<SysRole> findAllByIds(@Param("ids") List<Long> ids);
}
