package com.projecthub.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.projecthub.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 用户 Repository
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {

    /**
     * 根据邮箱查询
     */
    @Select("SELECT * FROM users WHERE email = #{email} AND deleted = 0")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * 根据用户名查询
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND deleted = 0")
    Optional<User> findByUsername(@Param("username") String username);

    /**
     * 根据角色查询用户列表
     */
    @Select("SELECT * FROM users WHERE role = #{role} AND deleted = 0")
    List<User> findByRole(@Param("role") String role);

    /**
     * 根据用户 ID 列表查询
     */
    @Select("<script>" +
            "SELECT * FROM users WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND deleted = 0" +
            "</script>")
    List<User> findByIds(@Param("ids") List<Long> ids);

}