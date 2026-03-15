package com.projecthub.security;

import com.projecthub.module.user.entity.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Security 用户详情实现 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

  private Long id;
  private String username;
  private String email;
  private String password;
  private Collection<? extends GrantedAuthority> authorities;
  private boolean accountNonExpired;
  private boolean accountNonLocked;
  private boolean credentialsNonExpired;
  private boolean enabled;

  /** 创建用户详情（向后兼容，默认 ROLE_USER 角色） */
  public static UserDetailsImpl create(User user) {
    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
    return new UserDetailsImpl(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getPassword(),
        authorities,
        true,
        true,
        true,
        "ACTIVE".equals(user.getStatus().name()));
  }

  /**
   * 创建用户详情（包含角色和权限）
   *
   * @param user 用户实体
   * @param roleCodes 角色编码列表
   * @param permissionCodes 权限编码列表
   * @return 用户详情
   */
  public static UserDetailsImpl create(
      User user, List<String> roleCodes, List<String> permissionCodes) {
    List<GrantedAuthority> authorities = new ArrayList<>();

    // 添加角色（Spring Security 要求角色以 ROLE_ 开头）
    for (String roleCode : roleCodes) {
      String authority = roleCode.startsWith("ROLE_") ? roleCode : "ROLE_" + roleCode;
      authorities.add(new SimpleGrantedAuthority(authority));
    }

    // 添加权限
    for (String permissionCode : permissionCodes) {
      authorities.add(new SimpleGrantedAuthority(permissionCode));
    }

    return new UserDetailsImpl(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getPassword(),
        authorities,
        true,
        true,
        true,
        "ACTIVE".equals(user.getStatus().name()));
  }
}
