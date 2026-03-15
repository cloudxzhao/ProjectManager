package com.projecthub.module.user.service;

import com.projecthub.common.exception.BusinessException;
import com.projecthub.module.user.entity.User;
import com.projecthub.module.user.repository.SysPermissionRepository;
import com.projecthub.module.user.repository.SysRolePermissionRepository;
import com.projecthub.module.user.repository.SysRoleRepository;
import com.projecthub.module.user.repository.SysUserRoleRepository;
import com.projecthub.module.user.repository.UserRepository;
import com.projecthub.security.UserDetailsImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用户详情服务实现 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final SysUserRoleRepository userRoleRepository;
  private final SysRoleRepository roleRepository;
  private final SysRolePermissionRepository rolePermissionRepository;
  private final SysPermissionRepository permissionRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
    log.debug("加载用户详情：{}", usernameOrEmail);

    User user = null;

    // 先尝试通过用户名查找
    if (userRepository.findByUsername(usernameOrEmail).isPresent()) {
      user = userRepository.findByUsername(usernameOrEmail).get();
    } else {
      // 如果用户名查找失败，尝试通过邮箱查找
      if (userRepository.findByEmail(usernameOrEmail).isPresent()) {
        user = userRepository.findByEmail(usernameOrEmail).get();
      }
    }

    // 如果仍然没有找到用户，抛出异常
    if (user == null) {
      throw new UsernameNotFoundException("用户不存在：" + usernameOrEmail);
    }

    // 检查用户状态
    if (user.getStatus() == User.UserStatus.INACTIVE) {
      throw new BusinessException("用户账号未激活");
    }
    if (user.getStatus() == User.UserStatus.BANNED) {
      throw new BusinessException("用户账号已被封禁");
    }

    // 加载用户角色
    List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(user.getId());
    List<String> roleCodes =
        roleRepository.findAllByIds(roleIds).stream()
            .map(com.projecthub.module.user.entity.SysRole::getCode)
            .toList();

    // 加载角色权限
    List<Long> permissionIds = rolePermissionRepository.findPermissionIdsByRoleIds(roleIds);
    List<String> permissionCodes =
        permissionRepository.findAllByIds(permissionIds).stream()
            .map(com.projecthub.module.user.entity.SysPermission::getCode)
            .distinct()
            .toList();

    log.debug("用户 [{}] 角色：{}, 权限：{}", user.getUsername(), roleCodes, permissionCodes);

    // 创建 UserDetails（包含角色和权限）
    return UserDetailsImpl.create(user, roleCodes, permissionCodes);
  }
}
