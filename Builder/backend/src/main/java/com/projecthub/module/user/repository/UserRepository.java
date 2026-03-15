package com.projecthub.module.user.repository;

import com.projecthub.module.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 用户 Repository 接口 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  /** 根据用户名查询用户 */
  @Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
  Optional<User> findByUsername(@Param("username") String username);

  /** 根据邮箱查询用户 */
  @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
  Optional<User> findByEmail(@Param("email") String email);

  /** 检查用户名是否存在 */
  @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
  boolean existsByUsername(@Param("username") String username);

  /** 检查邮箱是否存在 */
  @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
  boolean existsByEmail(@Param("email") String email);

  /** 查询用户的角色（从 sys_user_role 和 sys_role 表） */
  @Query(
      value =
          "SELECT r.code FROM sys_role r "
              + "JOIN sys_user_role ur ON r.id = ur.role_id "
              + "WHERE ur.user_id = :userId LIMIT 1",
      nativeQuery = true)
  String findRoleCodeByUserId(@Param("userId") Long userId);

  /** 根据用户 ID 列表查询用户 */
  @Query("SELECT u FROM User u WHERE u.id IN :userIds AND u.deletedAt IS NULL")
  List<com.projecthub.module.user.entity.User> findByIds(@Param("userIds") List<Long> userIds);

  /** 搜索用户（根据用户名、昵称或邮箱） */
  @Query(
      "SELECT u FROM User u WHERE u.deletedAt IS NULL AND "
          + "(:keyword IS NULL OR :keyword = '' OR "
          + "u.username LIKE %:keyword% OR "
          + "u.nickname LIKE %:keyword% OR "
          + "u.email LIKE %:keyword%)")
  List<com.projecthub.module.user.entity.User> search(@Param("keyword") String keyword);
}
