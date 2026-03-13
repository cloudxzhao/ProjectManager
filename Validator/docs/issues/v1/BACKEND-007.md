{
  "id": "BACKEND-007",
  "title": "任务列表筛选功能没有正确过滤结果",
  "severity": "medium",
  "category": "functionality",
  "description": "使用 status、priority 等参数筛选任务时，返回的结果不准确。虽然请求参数被接受，但由于 status 和 priority 没有正确存储（见问题1），筛选功能实际上没有生效。",
  "assignee": "",
  "affected_apis": [
    "GET /api/v1/projects/{projectId}/tasks"
  ],
  "status": "analysis",
  "current_state": {
    "behavior": "筛选参数被接受，但由于数据未正确存储，返回所有任务"
  },
  "expected_state": {
    "behavior": "只返回符合筛选条件的任务"
  },
  "solution": {
    "type": "fix",
    "description": "此问题依赖于问题 1 的修复。修复任务状态存储后，筛选功能应自动生效。",
    "files_to_modify": [],
    "related_issue": "BACKEND-001"
  },
  "related_issues": ["BACKEND-001"],
  "labels": ["api", "functionality"]
}