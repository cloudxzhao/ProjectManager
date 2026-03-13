{
  "id": "BACKEND-010",
  "title": "更新项目时传入的 status 字段被忽略",
  "severity": "low",
  "category": "data_persistence",
  "description": "在 UpdateProjectRequest 中传入 status 字段（如 'ACTIVE'），更新后该字段仍然为 null。",
  "assignee": "",
  "affected_apis": [
    "PUT /api/v1/projects/{id}"
  ],
  "status": "analysis",
  "current_state": {
    "project": {
      "status": null
    }
  },
  "expected_state": {
    "project": {
      "status": "ACTIVE"
    }
  },
  "solution": {
    "type": "fix",
    "description": "检查 ProjectService.updateProject 方法，确认 status 字段是否被正确更新。",
    "files_to_modify": [
      "src/main/java/com/projecthub/module/project/service/ProjectService.java"
    ]
  },
  "related_issues": [],
  "labels": ["api", "data"]
}