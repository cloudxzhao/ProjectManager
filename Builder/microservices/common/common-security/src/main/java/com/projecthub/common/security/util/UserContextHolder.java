package com.projecthub.common.security.util;

import com.projecthub.common.security.domain.LoginUser;

/**
 * 用户上下文工具类
 */
public class UserContextHolder {

    private static final ThreadLocal<LoginUser> USER_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户
     */
    public static void setUser(LoginUser user) {
        USER_HOLDER.set(user);
    }

    /**
     * 获取当前用户
     */
    public static LoginUser getUser() {
        return USER_HOLDER.get();
    }

    /**
     * 获取当前用户 ID
     */
    public static Long getUserId() {
        LoginUser user = getUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        LoginUser user = getUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前用户角色
     */
    public static String getRole() {
        LoginUser user = getUser();
        return user != null ? user.getRole() : null;
    }

    /**
     * 清除当前用户
     */
    public static void clear() {
        USER_HOLDER.remove();
    }

}