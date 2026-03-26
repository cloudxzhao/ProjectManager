package com.projecthub.wiki;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.projecthub.wiki.repository")
@EnableTransactionManagement
public class WikiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WikiServiceApplication.class, args);
    }
}