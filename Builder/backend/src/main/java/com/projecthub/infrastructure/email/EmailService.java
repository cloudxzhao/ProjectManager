package com.projecthub.infrastructure.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** 邮件发送服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Value("${app.name:ProjectHub}")
  private String appName;

  /** 发送密码重置邮件 */
  public void sendPasswordResetEmail(String to, String resetToken, String username) {
    String subject = String.format("[%s] 密码重置", appName);

    // 构建重置链接（实际部署时替换为真实域名）
    String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;

    String content =
        String.format(
            """
                尊敬的 %s：

                您收到了一个密码重置请求。

                请点击以下链接重置您的密码：
                %s

                该链接将在 1 小时后过期。

                如果您没有请求重置密码，请忽略此邮件，您的密码将不会改变。

                ---
                %s 团队
                """,
            username, resetLink, appName);

    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(to);
      message.setSubject(subject);
      message.setText(content);

      mailSender.send(message);
      log.info("密码重置邮件已发送到：{}", to);
    } catch (Exception e) {
      log.error("发送密码重置邮件失败，邮箱：{}", to, e);
      // 在生产环境中，这里应该抛出异常或进行重试
      // 开发环境下，我们只记录日志
    }
  }

  /** 发送欢迎邮件 */
  public void sendWelcomeEmail(String to, String username) {
    String subject = String.format("欢迎加入 %s！", appName);

    String content =
        String.format(
            """
                尊敬的 %s：

                欢迎加入 %s！

                您现在可以开始使用我们的服务了。

                ---
                %s 团队
                """,
            username, appName, appName);

    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(to);
      message.setSubject(subject);
      message.setText(content);

      mailSender.send(message);
      log.info("欢迎邮件已发送到：{}", to);
    } catch (Exception e) {
      log.error("发送欢迎邮件失败，邮箱：{}", to, e);
    }
  }
}
