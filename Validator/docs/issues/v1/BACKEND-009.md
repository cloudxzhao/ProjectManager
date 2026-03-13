{
  "id": "BACKEND-009",
  "title": "UserVO 缺少 nickname 字段定义",
  "severity": "low",
  "category": "response",
  "description": "UserController.updateProfile 接受 nickname 参数，但 UserVO 中没有定义 nickname 字段，导致无法正确返回用户昵称。",
  "assignee": "",
  "affected_apis": [
    "GET /api/v1/user/profile",
    "PUT /api/v1/user/profile"
  ],
  "status": "analysis",
  "current_state": {
    "user_vo_fields": ["id", "username", "email", "avatar", "status", "role", "createdAt"],
    "missing_field": "nickname"
  },
  "expected_state": {
    "user_vo_fields": ["id", "username", "nickname", "email", "avatar", "status", "role", "createdAt"]
  },
  "solution": {
    "type": "add",
    "description": "在 UserVO 中添加 nickname 字段。",
    "files_to_modify": [
      "src/main/java/com/projecthub/module/user/dto/UserVO.java"
    ]
  },
  "related_issues": ["BACKEND-002"],
  "labels": ["api", "response"]
}