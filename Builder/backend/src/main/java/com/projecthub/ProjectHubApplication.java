package com.projecthub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** ProjectHub 项目管理系统的 Spring Boot 启动类 */
@SpringBootApplication
@EnableJpaAuditing
public class ProjectHubApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProjectHubApplication.class, args);
  }
}
