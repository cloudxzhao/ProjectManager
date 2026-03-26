package com.projecthub.common.core.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义服务信息贡献者
 *
 * 用于在 /actuator/info 端点中显示服务的详细信息
 */
@Component
public class ServiceInfoContributor implements InfoContributor {

    @Value("${spring.application.name:unknown}")
    private String serviceName;

    @Value("${spring.application.version:1.0.0}")
    private String serviceVersion;

    @Value("${server.port:0}")
    private String serverPort;

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> details = new HashMap<>();
        details.put("name", serviceName);
        details.put("version", serviceVersion);
        details.put("port", serverPort);
        details.put("javaVersion", System.getProperty("java.version"));
        details.put("osName", System.getProperty("os.name"));

        builder.withDetails(details);
    }

}
