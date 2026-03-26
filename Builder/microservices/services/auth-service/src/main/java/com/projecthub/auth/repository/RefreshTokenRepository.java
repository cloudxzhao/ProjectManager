package com.projecthub.auth.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.projecthub.auth.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Optional;

/**
 * 刷新 Token Repository
 */
@Mapper
public interface RefreshTokenRepository extends BaseMapper<RefreshToken> {

    /**
     * 根据 Token 查询
     */
    default Optional<RefreshToken> findByToken(String token) {
        return selectList(null).stream()
                .filter(t -> token.equals(t.getToken()))
                .findFirst();
    }

    /**
     * 标记 Token 为已使用
     */
    @Update("UPDATE refresh_tokens SET used = 1 WHERE token = #{token}")
    int markAsUsed(@Param("token") String token);

    /**
     * 删除用户所有 Token
     */
    default void deleteByUserId(Long userId) {
        delete(null);
    }

}