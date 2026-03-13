{
  "id": "BACKEND-002",
  "title": "用户更新 profile 时 nickname 参数错误地覆盖了 username",
  "severity": "critical",
  "category": "data_integrity",
  "description": "UserController 的 updateProfile 方法接受 nickname 参数，但实际上错误地将其设置为 username 字段，导致用户无法使用原用户名登录。这是一个严重的数据完整性问题。",
  "assignee": "",
  "affected_apis": [
    "PUT /api/v1/user/profile"
  ],
  "status": "analysis",
  "current_state": {
    "profile": {
      "username": "AdminUser (被错误修改)",
      "nickname": "未存储"
    }
  },
  "expected_state": {
    "profile": {
      "username": "admin (保持不变)",
      "nickname": "AdminUser"
    }
  },
  "solution": {
    "type": "fix",
    "description": "在 UserVO 中添加 nickname 字段，修改 UserService.updateProfile 方法正确处理 nickname 参数。",
    "files_to_modify": [
      "src/main/java/com/projecthub/module/user/dto/UserVO.java",
      "src/main/java/com/projecthub/module/user/service/UserService.java",
      "src/main/java/com/projecthub/module/user/controller/UserController.java"
    ]
  },
  "related_issues": ["BACKEND-003", "BACKEND-009"],
  "labels": ["api", "data", "critical"]
}