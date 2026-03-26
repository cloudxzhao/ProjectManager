# 前端适配微服务架构指南

## 概述

本文档说明如何将前端应用从单体架构迁移到微服务架构，确保前端能够通过 API Gateway 正确访问所有微服务。

## 架构变更

### 变更前（单体架构）

```
┌──────────┐     ┌──────────┐
│ Frontend │────▶│ Backend  │
│ :3000    │     │ :9527    │
└──────────┘     └──────────┘
```

### 变更后（微服务架构）

```
┌──────────┐     ┌─────────────┐     ┌────────────────┐
│ Frontend │────▶│   Gateway   │────▶│  Microservices │
│ :3000    │     │   :8080     │     │  :9521-:9528   │
└──────────┘     └─────────────┘     └────────────────┘
```

## 配置变更

### 1. 环境变量更新

#### 开发环境 (`.env.development`)

```bash
# 微服务架构 - 指向 API Gateway
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
```

#### 生产环境 (`.env.production`)

```bash
# 微服务架构 - 使用相对路径，由 Nginx 反向代理
NEXT_PUBLIC_API_URL=/api/v1
```

### 2. Next.js 配置更新

`next.config.js`:

```javascript
/** @type {import('next').NextConfig} */
const nextConfig = {
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'images.unsplash.com',
      },
    ],
  },
  // 开发环境代理配置 - 指向 API Gateway
  async rewrites() {
    return [
      {
        source: '/api/v1/:path*',
        destination: 'http://localhost:8080/api/v1/:path*',
      },
    ];
  },
  // CORS 配置
  async headers() {
    return [
      {
        source: '/api/:path*',
        headers: [
          { key: 'Access-Control-Allow-Credentials', value: 'true' },
          { key: 'Access-Control-Allow-Origin', value: 'http://localhost:3000' },
          { key: 'Access-Control-Allow-Methods', value: 'GET,DELETE,PATCH,POST,PUT' },
          { key: 'Access-Control-Allow-Headers', value: 'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version, Authorization, X-User-Id, X-User-Name, X-User-Role' },
        ],
      },
    ];
  },
};

module.exports = nextConfig;
```

## API 路径映射

### Gateway 路由规则

| 前端路径 | Gateway 路由 | 目标服务 | 服务端口 |
|---------|-------------|---------|---------|
| /api/v1/auth/** | /api/v1/auth/** | auth-service | 9521 |
| /api/v1/users/** | /api/v1/users/** | user-service | 9522 |
| /api/v1/projects/** | /api/v1/projects/** | project-service | 9523 |
| /api/v1/tasks/** | /api/v1/tasks/** | task-service | 9524 |
| /api/v1/stories/** | /api/v1/stories/** | story-service | 9525 |
| /api/v1/issues/** | /api/v1/issues/** | issue-service | 9526 |
| /api/v1/wiki/** | /api/v1/wiki/** | wiki-service | 9527 |
| /api/v1/notifications/** | /api/v1/notifications/** | notification-service | 9528 |

### Gateway 路由配置示例

```yaml
# application.yml (Gateway)
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/v1/auth/**
          filters:
            - StripPrefix=0
            - name: JwtAuthentication
            - name: RequestLog

        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/v1/users/**
          filters:
            - StripPrefix=0
            - name: JwtAuthentication
            - name: RequestLog

        # ... 其他服务路由配置
```

## 认证流程变更

### JWT Token 处理

**无变化** - Token 处理逻辑保持不变：

```typescript
// lib/api/axios.ts - 无需修改
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### Gateway 认证流程

```
┌──────────┐     ┌─────────────┐     ┌──────────────┐
│ Frontend │────▶│   Gateway   │────▶│ Auth Service │
└──────────┘     └─────────────┘     └──────────────┘
     │                  │                    │
     │  1. 登录请求      │                    │
     ├─────────────────>│                    │
     │                  │  2. 转发请求        │
     │                  ├───────────────────>│
     │                  │                    │
     │                  │  3. 返回 Token      │
     │                  │<───────────────────│
     │  4. 返回 Token    │                    │
     │<─────────────────│                    │
     │                  │                    │
     │  5. 业务请求（携带 Token）             │
     ├─────────────────>│                    │
     │                  │  6. 验证 Token      │
     │                  │  注入用户信息到 Header│
     │                  │                    │
     │                  │  7. 转发到下游服务   │
     │                  ├───────────────────>│
     │                  │   X-User-Id: 123   │
     │                  │   X-User-Name: 张三 │
```

## 跨域问题处理

### Gateway CORS 配置

Gateway 已配置全局 CORS 策略，前端无需额外处理：

```java
// GatewayConfig.java - 已配置
@Configuration
public class GatewayConfig {
    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("https://your-production-domain.com");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        // ... 更多配置
    }
}
```

### 开发环境代理

开发时使用 Next.js 重写规则代理到 Gateway：

```javascript
// next.config.js - 已配置
async rewrites() {
  return [
    {
      source: '/api/v1/:path*',
      destination: 'http://localhost:8080/api/v1/:path*',
    },
  ];
}
```

## 验证清单

### 开发环境验证

- [ ] 启动 Gateway：`cd Builder/microservices && docker-compose up -d`
- [ ] 启动至少一个微服务：`cd services/auth-service && mvn spring-boot:run`
- [ ] 启动前端：`cd Builder/frontend && npm run dev`
- [ ] 访问 http://localhost:3000
- [ ] 验证登录功能
- [ ] 验证 API 请求通过 Gateway 转发

### 生产环境验证

- [ ] 构建前端：`npm run build`
- [ ] 配置 Nginx 反向代理
- [ ] 验证所有 API 请求正常
- [ ] 验证跨域请求无错误

## Nginx 生产环境配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    # API Gateway 反向代理
    location /api/ {
        proxy_pass http://gateway:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket 支持（如果需要）
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        # 超时配置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}
```

## 故障排查

### 问题 1: 所有请求返回 404

**可能原因**: Gateway 未正确配置路由

**解决方案**:
1. 检查 Gateway 的 `application.yml` 路由配置
2. 确认微服务已注册到 Nacos
3. 查看 Gateway 日志

### 问题 2: 跨域错误

**可能原因**: CORS 配置不正确

**解决方案**:
1. 检查 Gateway 的 CORS 配置
2. 确认前端请求的 Origin 在白名单中
3. 检查浏览器控制台错误信息

### 问题 3: 认证失败（401）

**可能原因**: Token 未正确传递

**解决方案**:
1. 检查前端 Axios 拦截器是否正确注入 Token
2. 确认 Gateway 的 JWT 验证过滤器配置
3. 查看 Gateway 日志中的请求头信息

### 问题 4: 请求超时

**可能原因**: 微服务响应慢或不可用

**解决方案**:
1. 检查微服务是否正常运行
2. 查看 Gateway 的超时配置
3. 增加 Gateway 的超时时间：
   ```yaml
   spring:
     cloud:
       gateway:
         httpclient:
           connect-timeout: 10000
           response-timeout: 60s
   ```

## 性能优化建议

### 1. 启用 Gzip 压缩

```nginx
# Nginx 配置
gzip on;
gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
gzip_min_length 1000;
```

### 2. 静态资源缓存

```nginx
# Nginx 配置
location /_next/static/ {
    alias /usr/share/nginx/html/_next/static/;
    expires 365d;
    access_log off;
}
```

### 3. API 响应缓存

对于不常变更的数据，可在 Gateway 层配置缓存：

```java
// Gateway 缓存配置
@Bean
public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("user-service", r -> r
            .path("/api/v1/users/**")
            .filters(f -> f
                .cacheControl(c -> c
                    .maxAge(Duration.ofMinutes(5))
                    .cachePrivate()
                )
            )
            .uri("lb://user-service"))
        .build();
}
```

## 回滚方案

如需回滚到单体架构：

1. **修改环境变量**:
   ```bash
   NEXT_PUBLIC_API_URL=http://localhost:9527/api/v1
   ```

2. **更新 Next.js 配置**:
   ```javascript
   async rewrites() {
     return [
       {
         source: '/api/v1/:path*',
         destination: 'http://localhost:9527/api/v1/:path*',
       },
     ];
   }
   ```

3. **重启前端服务**

## 总结

前端适配微服务架构的变更非常小，主要集中在：

1. **环境变量**: 更新 API 地址指向 Gateway
2. **代理配置**: Next.js 重写规则
3. **CORS 配置**: 由 Gateway 统一处理

前端代码**无需修改**，因为：
- API 路径保持不变（/api/v1/*）
- 认证机制保持不变（JWT Token）
- 响应格式保持一致

所有复杂性都在 Gateway 层处理，对前端透明。
