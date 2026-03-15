package com.projecthub.module.user.repository;

import com.projecthub.module.user.entity.SysUserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 用户角色关联 Repository 接口 */
@Repository
public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Long> {

  /** 根据用户 ID 查询角色 ID 列表 */
  @Query("SELECT sur.roleId FROM SysUserRole sur WHERE sur.userId = :userId")
  List<Long> findRoleIdsByUserId(@Param("userId") Long userId);

  /** 根据用户 ID 查询用户角色关联列表 */
  @Query("SELECT sur FROM SysUserRole sur WHERE sur.userId = :userId")
  List<SysUserRole> findAllByUserId(@Param("userId") Long userId);

  /** 根据用户 ID 删除用户角色关联 */
  void deleteByUserId(Long userId);
}
