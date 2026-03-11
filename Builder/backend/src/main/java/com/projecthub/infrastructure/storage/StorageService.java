package com.projecthub.infrastructure.storage;

import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 文件存储服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

  @Value("${app.storage.local.upload-path:./uploads}")
  private String uploadPath;

  @Value("${app.storage.local.avatar-path:./uploads/avatars}")
  private String avatarPath;

  /** 上传文件 */
  public String uploadFile(MultipartFile file, String directory) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("文件不能为空");
    }

    // 生成唯一文件名
    String originalFilename = file.getOriginalFilename();
    String extension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }
    String filename = UUID.randomUUID().toString() + extension;

    try {
      // 创建目录
      java.nio.file.Path uploadDir = java.nio.file.Path.of(uploadPath, directory);
      java.nio.file.Files.createDirectories(uploadDir);

      // 保存文件
      java.nio.file.Path filePath = uploadDir.resolve(filename);
      java.nio.file.Files.copy(file.getInputStream(), filePath);

      // 返回访问路径
      String accessPath = "/" + directory + "/" + filename;
      log.info("文件上传成功：{}", accessPath);

      return accessPath;
    } catch (Exception e) {
      log.error("文件上传失败", e);
      throw new RuntimeException("文件上传失败", e);
    }
  }

  /** 上传头像 */
  public String uploadAvatar(MultipartFile file) {
    // 验证文件类型
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new IllegalArgumentException("只能上传图片文件");
    }

    // 验证文件大小（最大 2MB）
    if (file.getSize() > 2 * 1024 * 1024) {
      throw new IllegalArgumentException("图片大小不能超过 2MB");
    }

    return uploadFile(file, "avatars");
  }

  /** 删除文件 */
  public void deleteFile(String path) {
    try {
      // 移除开头的 /
      if (path.startsWith("/")) {
        path = path.substring(1);
      }
      java.nio.file.Path filePath = java.nio.file.Path.of(uploadPath, path);
      java.nio.file.Files.deleteIfExists(filePath);
      log.info("文件删除成功：{}", path);
    } catch (Exception e) {
      log.error("文件删除失败：{}", path, e);
      throw new RuntimeException("文件删除失败", e);
    }
  }

  /** 获取文件输入流 */
  public InputStream getFileInputStream(String path) {
    try {
      // 移除开头的 /
      if (path.startsWith("/")) {
        path = path.substring(1);
      }
      java.nio.file.Path filePath = java.nio.file.Path.of(uploadPath, path);
      return java.nio.file.Files.newInputStream(filePath);
    } catch (Exception e) {
      log.error("获取文件输入流失败：{}", path, e);
      throw new RuntimeException("获取文件失败", e);
    }
  }
}
