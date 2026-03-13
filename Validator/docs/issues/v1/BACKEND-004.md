{
  "id": "BACKEND-004",
  "title": "资源不存在时返回 500 错误而非 404",
  "severity": "medium",
  "category": "error_handling",
  "description": "当请求的资源不存在（如项目、任务等）时，后端返回 HTTP 500 状态码而不是更准确的 404 Not Found。这不符合 RESTful API 最佳实践。",
  "assignee": "",
  "affected_apis": [
    "GET /api/v1/projects/{id}",
    "PUT /api/v1/projects/{id}",
    "DELETE /api/v1/projects/{id}",
    "PUT /api/v1/projects/{projectId}/tasks/{id}",
    "DELETE /api/v1/projects/{projectId}/tasks/{id}",
    "POST /api/v1/tasks/{taskId}/comments"
  ],
  "status": "analysis",
  "current_state": {
    "http_status": 500,
    "error_message": "项目不存在/任务不存在"
  },
  "expected_state": {
    "http_status": 404,
    "error_message": "项目不存在/任务不存在"
  },
  "solution": {
    "type": "fix",
    "description": "在 Service 层捕获 NotFoundException 或类似异常时，设置 HTTP 状态码为 404。",
    "files_to_modify": [
      "src/main/java/com/projecthub/module/project/service/ProjectService.java",
      "src/main/java/com/projecthub/module/task/service/TaskService.java",
      "src/main/java/com/projecthub/common/exception/GlobalExceptionHandler.java"
    ]
  },
  "related_issues": [],
  "labels": ["api", "error-handling"]
}