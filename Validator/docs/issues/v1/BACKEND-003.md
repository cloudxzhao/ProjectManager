{
  "id": "BACKEND-003",
  "title": "用户角色(role)字段在 API 响应中始终显示为 MEMBER",
  "severity": "medium",
  "category": "response",
  "description": "超级管理员账户的角色应该是 ADMIN，但 API 返回的 role 字段始终显示为 MEMBER。UserVO.fromEntity() 方法已废弃但可能仍在使用，导致 role 字段没有被正确映射。",
  "assignee": "",
  "affected_apis": [
    "GET /api/v1/user/profile"
  ],
  "status": "analysis",
  "current_state": {
    "response": {
      "id": 1,
      "username": "admin",
      "role": "MEMBER"
    }
  },
  "expected_state": {
    "response": {
      "id": 1,
      "username": "admin",
      "role": "ADMIN"
    }
  },
  "solution": {
    "type": "fix",
    "description": "修改 UserVO 添加 role 字段，使用 BeanCopyUtil 正确复制 role 属性。",
    "files_to_modify": [
      "src/main/java/com/projecthub/module/user/dto/UserVO.java"
    ]
  },
  "related_issues": [],
  "labels": ["api", "response"]
}