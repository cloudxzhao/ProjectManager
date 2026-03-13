{
  "id": "BACKEND-005",
  "title": "未认证用户访问受保护接口返回 HTTP 403",
  "severity": "medium",
  "category": "security",
  "description": "当用户未提供 token 或 token 无效时访问受保护接口，应返回 HTTP 401 Unauthorized 而不是 HTTP 403 Forbidden。",
  "assignee": "",
  "affected_apis": ["所有需要认证的接口"],
  "status": "analysis",
  "current_state": {
    "http_status": 403,
    "description": "无token或无效token访问"
  },
  "expected_state": {
    "http_status": 401,
    "description": "未授权访问"
  },
  "solution": {
    "type": "fix",
    "description": "在 Spring Security 配置中，正确区分 AuthenticationException (401) 和 AccessDeniedException (403)。",
    "files_to_modify": [
      "src/main/java/com/projecthub/security/*.java",
      "src/main/java/com/projecthub/common/config/SecurityConfig.java"
    ]
  },
  "related_issues": [],
  "labels": ["api", "security"]
}