Architect 建立任务清单时需添加一个字段，叫做是否完成done？默认值是false，给出它 fewshot，例如：

```json
  {
    "id": "BE-002",
    "title": "数据库和环境配置",
    "description": "配置 PostgreSQL 数据库连接、Redis 缓存、Flyway 迁移等",
    "module": "基础设施",
    "priority": "P0",
    "estimatedDays": 1,
    "dependencies": ["BE-001"],
    "deliverables": [
      "application.yml 配置",
      "application-dev.yml 开发环境配置",
      "application-prod.yml 生产环境配置",
      "PostgreSQL 数据源配置",
      "Redis 配置",
      "Flyway 配置"
    ],
    "done": false
  }
```

后续 Builder 在完成任务后 done 改为 true，表示已完成任务

```json
  {
    "id": "BE-002",
    "title": "数据库和环境配置",
    "description": "配置 PostgreSQL 数据库连接、Redis 缓存、Flyway 迁移等",
    "module": "基础设施",
    "priority": "P0",
    "estimatedDays": 1,
    "dependencies": ["BE-001"],
    "deliverables": [
      "application.yml 配置",
      "application-dev.yml 开发环境配置",
      "application-prod.yml 生产环境配置",
      "PostgreSQL 数据源配置",
      "Redis 配置",
      "Flyway 配置"
    ],
    "done": true
  }
```




