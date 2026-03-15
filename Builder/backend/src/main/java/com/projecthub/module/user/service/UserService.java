package com.projecthub.module.user.service;

import com.projecthub.common.exception.BusinessException;
import com.projecthub.common.util.BeanCopyUtil;
import com.projecthub.common.util.PasswordUtil;
import com.projecthub.module.user.dto.UserVO;
import com.projecthub.module.user.entity.User;
import com.projecthub.module.user.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** 用户服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordUtil passwordUtil;

  /** 上传目录 */
  private static final String UPLOAD_DIR = "uploads/avatars/";

  /** 获取当前用户信息 */
  @Transactional(readOnly = true)
  public UserVO getCurrentUserInfo() {
    User user = getCurrentUser();
    UserVO userVO = BeanCopyUtil.copyProperties(user, UserVO.class);

    // 设置用户角色
    String roleCode = userRepository.findRoleCodeByUserId(user.getId());
    if (roleCode != null && !roleCode.isEmpty()) {
      userVO.setRole(roleCode); // 直接使用数据库中的角色 code，如 "ADMIN"
    } else {
      userVO.setRole("MEMBER"); // 默认为 MEMBER
    }

    // 设置用户状态
    if (user.getStatus() != null) {
      userVO.setStatus(user.getStatus().name());
    }

    return userVO;
  }

  /** 更新用户资料 */
  @Transactional
  public UserVO updateProfile(String nickname, String avatar) {
    User user = getCurrentUser();

    if (nickname != null) {
      user.setNickname(nickname);
    }
    if (avatar != null) {
      user.setAvatar(avatar);
    }

    userRepository.save(user);
    log.info("更新用户资料成功：{}", user.getUsername());

    UserVO userVO = BeanCopyUtil.copyProperties(user, UserVO.class);
    // 手动设置枚举字段的字符串表示
    userVO.setStatus(user.getStatus().name());
    setUserRole(userVO, user.getId());
    return userVO;
  }

  /** 上传头像 */
  @Transactional
  public UserVO uploadAvatar(MultipartFile file) {
    User user = getCurrentUser();

    try {
      // 创建上传目录
      Path uploadPath = Paths.get(UPLOAD_DIR);
      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
      }

      // 生成唯一文件名
      String originalFilename = file.getOriginalFilename();
      String extension =
          originalFilename != null && originalFilename.contains(".")
              ? originalFilename.substring(originalFilename.lastIndexOf("."))
              : "";
      String filename = UUID.randomUUID() + extension;

      // 保存文件
      Path filePath = uploadPath.resolve(filename);
      Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

      // 更新用户头像
      String avatarUrl = "/uploads/avatars/" + filename;
      user.setAvatar(avatarUrl);
      userRepository.save(user);

      log.info("上传头像成功：{}", user.getUsername());

      UserVO userVO = BeanCopyUtil.copyProperties(user, UserVO.class);
      // 手动设置枚举字段的字符串表示
      userVO.setStatus(user.getStatus().name());
      setUserRole(userVO, user.getId());
      return userVO;

    } catch (IOException e) {
      log.error("上传头像失败：{}", e.getMessage(), e);
      throw new BusinessException("上传头像失败");
    }
  }

  /** 修改密码 */
  @Transactional
  public void updatePassword(String oldPassword, String newPassword) {
    User user = getCurrentUser();

    // 验证旧密码
    if (!passwordUtil.matches(oldPassword, user.getPassword())) {
      throw new BusinessException("原密码错误");
    }

    // 更新密码
    user.setPassword(passwordUtil.encode(newPassword));
    userRepository.save(user);

    log.info("修改密码成功：{}", user.getUsername());
  }

  /** 根据 ID 获取用户 */
  @Transactional(readOnly = true)
  public UserVO getUserById(Long userId) {
    User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
    UserVO userVO = BeanCopyUtil.copyProperties(user, UserVO.class);
    // 手动设置枚举字段的字符串表示
    userVO.setStatus(user.getStatus().name());
    setUserRole(userVO, userId);
    return userVO;
  }

  /** 搜索用户（根据用户名、昵称或邮箱） */
  @Transactional(readOnly = true)
  public List<UserVO> searchUsers(String keyword) {
    List<User> users = userRepository.search(keyword);
    return users.stream()
        .map(
            user -> {
              UserVO userVO = BeanCopyUtil.copyProperties(user, UserVO.class);
              userVO.setStatus(user.getStatus().name());
              setUserRole(userVO, user.getId());
              return userVO;
            })
        .toList();
  }

  /** 设置用户角色 */
  private void setUserRole(UserVO userVO, Long userId) {
    String roleCode = userRepository.findRoleCodeByUserId(userId);
    if (roleCode != null && !roleCode.isEmpty()) {
      userVO.setRole(roleCode); // 直接使用数据库中的角色 code，如 "ADMIN"
    } else {
      userVO.setRole("MEMBER"); // 默认为 MEMBER
    }
  }

  /** 获取当前登录用户 */
  private User getCurrentUser() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new BusinessException("用户不存在"));
  }
}
