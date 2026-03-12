package com.projecthub.module.user.service;

import com.projecthub.common.exception.BusinessException;
import com.projecthub.module.user.entity.User;
import com.projecthub.module.user.repository.UserRepository;
import com.projecthub.security.UserDetailsImpl;
import java.util.Optional;
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

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
    log.debug("加载用户详情：{}", usernameOrEmail);

    User user = null;

    // 先尝试通过用户名查找
    Optional<User> userByUsername = userRepository.findByUsername(usernameOrEmail);
    if (userByUsername.isPresent()) {
      user = userByUsername.get();
    } else {
      // 如果用户名查找失败，尝试通过邮箱查找
      Optional<User> userByEmail = userRepository.findByEmail(usernameOrEmail);
      if (userByEmail.isPresent()) {
        user = userByEmail.get();
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

    return UserDetailsImpl.create(user);
  }
}
