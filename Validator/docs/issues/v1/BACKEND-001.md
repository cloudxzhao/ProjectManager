{
  "id": "BACKEND-001",
  "title": "Task 状态(status)和优先级(priority)未正确保存和返回",
  "severity": "high",
  "category": "data_persistence",
  "description": "创建任务时传入的 status (如 'TODO') 和 priority (如 'HIGH') 没有被正确保存到数据库，返回的响应中这些字段始终为 null。更新任务和移动任务后同样无法正确保存。",
  "assignee": "",
  "affected_apis": [
    "POST /api/v1/projects/{projectId}/tasks",
    "PUT /api/v1/projects/{projectId}/tasks/{id}",
    "POST /api/v1/projects/{projectId}/tasks/{id}/move"
  ],
  "status": "analysis",
  "current_state": {
    "task": {
      "status": null,
      "priority": null
    }
  },
  "expected_state": {
    "task": {
      "status": "TODO",
      "priority": "HIGH"
    }
  },
  "solution": {
    "type": "fix",
    "description": "检查 TaskService 创建和更新任务的逻辑，确认 status 和 priority 字段是否正确保存到数据库。检查 Task 实体类的字段映射。",
    "files_to_modify": [
      "src/main/java/com/projecthub/module/task/service/TaskService.java",
      "src/main/java/com/projecthub/module/task/entity/Task.java",
      "src/main/java/com/projecthub/module/task/mapper/TaskMapper.java"
    ]
  },
  "related_issues": ["BACKEND-007"],
  "labels": ["api", "data"]
}