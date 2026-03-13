{
  "id": "BACKEND-006",
  "title": "创建用户故事接口返回服务器内部错误",
  "severity": "high",
  "category": "functionality",
  "description": "POST /api/v1/projects/{projectId}/stories 接口返回 500 服务器内部错误，无法创建用户故事。",
  "assignee": "",
  "affected_apis": [
    "POST /api/v1/projects/{projectId}/stories"
  ],
  "status": "analysis",
  "current_state": {
    "http_status": 500,
    "response": {"code": 500, "message": "服务器内部错误"}
  },
  "expected_state": {
    "http_status": 200,
    "response": {"code": 200, "data": {"id": "...", "title": "..."}}
  },
  "solution": {
    "type": "fix",
    "description": "检查 UserStoryService.createUserStory 方法，定位并修复导致 500 错误的原因。",
    "files_to_modify": [
      "src/main/java/com/projecthub/module/story/service/UserStoryService.java"
    ]
  },
  "related_issues": [],
  "labels": ["api", "functionality"]
}