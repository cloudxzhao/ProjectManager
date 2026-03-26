package com.projecthub.auth.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.projecthub.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 用户 Repository
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {

    /**
     * 根据邮箱查询用户
     */
    default Optional<User> findByEmail(String email) {
        return selectList(null).stream()
                .filter(u -> email.equals(u.getEmail()))
                .findFirst();
    }

    /**
     * 根据用户名查询用户
     */
    default Optional<User> findByUsername(String username) {
        return selectList(null).stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst();
    }

    /**
     * 检查邮箱是否存在
     */
    default boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    /**
     * 检查用户名是否存在
     */
    default boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

}