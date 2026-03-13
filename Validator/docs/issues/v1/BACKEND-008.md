{
  "id": "BACKEND-008",
  "title": "Token 刷新接口返回 500 错误",
  "severity": "high",
  "category": "authentication",
  "description": "使用 refreshToken 刷新访问令牌时返回 500 错误。用户需要频繁重新登录，影响用户体验。",
  "assignee": "",
  "affected_apis": [
    "POST /api/v1/auth/refresh"
  ],
  "status": "analysis",
  "current_state": {
    "http_status": 500,
    "response": {"code": 500, "message": "刷新 Token 失败"}
  },
  "expected_state": {
    "http_status": 200,
    "response": {"code": 200, "data": {"accessToken": "...", "refreshToken": "..."}}
  },
  "solution": {
    "type": "fix",
    "description": "检查 AuthService.refreshToken 方法，定位并修复导致刷新失败的原因。可能是 token 解析或验证逻辑的问题。",
    "files_to_modify": [
      "src/main/java/com/projecthub/module/auth/service/AuthService.java",
      "src/main/java/com/projecthub/common/util/JwtUtil.java"
    ]
  },
  "related_issues": [],
  "labels": ["api", "authentication"]
}