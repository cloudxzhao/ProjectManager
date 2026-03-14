#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ProjectHub Backend API Test Suite v6
执行全量 API 接口测试、边界测试、异常场景测试
"""

import requests
import json
import time
from datetime import datetime
from typing import Dict, List, Any, Optional

# 配置
BASE_URL = "http://localhost:9527/api/v1"
OUTPUT_DIR = "D:/data/project/ClaudeStudy/ProjectManagerStudy/Validator/docs/issues/v6"

# 测试用户凭证
CREDENTIALS = {
    "usernameOrEmail": "admin",
    "password": "Admin123"
}

class APITester:
    def __init__(self):
        self.session = requests.Session()
        self.access_token = ""
        self.refresh_token = ""
        self.project_id = None
        self.task_id = None
        self.test_results = []
        self.issues = []
        self.issue_counter = 0
        self.tests_passed = 0
        self.tests_failed = 0
        self.tests_total = 0

    def log(self, message: str, level: str = "INFO"):
        """记录日志"""
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        print(f"[{timestamp}] [{level}] {message}")

    def record_issue(self, title: str, severity: str, category: str,
                     description: str, affected_apis: List[str],
                     current_state: str, expected_state: str,
                     reproduction_steps: List[str], labels: List[str]):
        """记录发现的问题"""
        self.issue_counter += 1
        issue = {
            "id": f"BACKEND-V6-{self.issue_counter:03d}",
            "title": title,
            "severity": severity,
            "category": category,
            "description": description,
            "affected_apis": affected_apis,
            "current_state": current_state,
            "expected_state": expected_state,
            "reproduction_steps": reproduction_steps,
            "labels": labels
        }
        self.issues.append(issue)
        self.log(f"发现问题：{title}", "ISSUE")

    def test_assert(self, name: str, condition: bool,
                    expected: str = "", actual: str = "",
                    severity: str = "medium", category: str = "functionality",
                    description: str = "", affected_apis: List[str] = None,
                    reproduction_steps: List[str] = None, labels: List[str] = None):
        """执行测试断言"""
        self.tests_total += 1
        if condition:
            self.tests_passed += 1
            self.log(f"[PASS] {name}", "GREEN")
            return True
        else:
            self.tests_failed += 1
            self.log(f"[FAIL] {name} - 期望：{expected}, 实际：{actual}", "RED")

            # 记录问题
            if affected_apis is None:
                affected_apis = []
            if reproduction_steps is None:
                reproduction_steps = ["步骤待补充"]
            if labels is None:
                labels = []

            self.record_issue(
                title=name,
                severity=severity,
                category=category,
                description=description or f"测试失败：{name}",
                affected_apis=affected_apis,
                current_state=actual,
                expected_state=expected,
                reproduction_steps=reproduction_steps,
                labels=labels
            )
            return False

    def make_request(self, method: str, endpoint: str,
                     data: Optional[Dict] = None,
                     headers: Optional[Dict] = None,
                     use_auth: bool = False) -> tuple:
        """发送 HTTP 请求"""
        url = f"{BASE_URL}{endpoint}"
        req_headers = {"Content-Type": "application/json"}

        if headers:
            req_headers.update(headers)

        if use_auth and self.access_token:
            req_headers["Authorization"] = f"Bearer {self.access_token}"

        try:
            if method.upper() == "GET":
                response = requests.get(url, headers=req_headers, timeout=30)
            elif method.upper() == "POST":
                response = requests.post(url, json=data, headers=req_headers, timeout=30)
            elif method.upper() == "PUT":
                response = requests.put(url, json=data, headers=req_headers, timeout=30)
            elif method.upper() == "DELETE":
                response = requests.delete(url, headers=req_headers, timeout=30)
            else:
                raise ValueError(f"不支持的方法：{method}")

            # 尝试解析 JSON
            try:
                resp_data = response.json()
            except:
                resp_data = {"raw": response.text}

            return response.status_code, resp_data

        except requests.exceptions.RequestException as e:
            return 0, {"error": str(e)}

    def login(self) -> bool:
        """登录获取 Token"""
        self.log("正在登录...", "INFO")
        status, data = self.make_request("POST", "/auth/login",
                                          data=CREDENTIALS)

        if status == 200 and data.get("code") == 200:
            self.access_token = data.get("data", {}).get("accessToken", "")
            self.refresh_token = data.get("data", {}).get("refreshToken", "")
            self.log(f"登录成功，Token: {self.access_token[:50]}...", "GREEN")
            return True
        else:
            self.log(f"登录失败：{data}", "RED")
            self.test_assert(
                "登录接口正常返回 Token",
                False,
                expected="code=200 且返回 accessToken",
                actual=f"status={status}, code={data.get('code')}",
                severity="high",
                category="functionality",
                description="登录接口无法正常获取 Token",
                affected_apis=["POST /api/v1/auth/login"],
                reproduction_steps=[
                    "1. 发送 POST /api/v1/auth/login",
                    f"2. 请求体：{json.dumps(CREDENTIALS)}",
                    "3. 观察返回结果"
                ],
                labels=["auth", "login", "token"]
            )
            return False

    def test_auth_module(self):
        """测试认证模块"""
        self.log("=== 开始测试认证模块 ===", "HEADER")

        # 1. 测试登录 - 空用户名
        status, data = self.make_request("POST", "/auth/login",
                                          data={"usernameOrEmail": "", "password": "Admin123"})
        self.test_assert(
            "登录 - 空用户名返回 400",
            data.get("code") == 400,
            expected="code=400",
            actual=f"code={data.get('code')}",
            severity="low",
            category="validation",
            description="空用户名登录的验证测试",
            affected_apis=["POST /api/v1/auth/login"],
            reproduction_steps=["1. 发送空用户名的登录请求"],
            labels=["auth", "validation"]
        )

        # 2. 测试登录 - 空密码
        status, data = self.make_request("POST", "/auth/login",
                                          data={"usernameOrEmail": "admin"})
        self.test_assert(
            "登录 - 空密码返回 400",
            data.get("code") == 400,
            expected="code=400",
            actual=f"code={data.get('code')}",
            severity="low",
            category="validation",
            description="空密码登录的验证测试",
            affected_apis=["POST /api/v1/auth/login"],
            reproduction_steps=["1. 发送空密码的登录请求"],
            labels=["auth", "validation"]
        )

        # 3. 测试登录 - 错误密码
        status, data = self.make_request("POST", "/auth/login",
                                          data={"usernameOrEmail": "admin", "password": "wrong"})
        self.test_assert(
            "登录 - 错误密码返回 401",
            data.get("code") == 2001 and status == 401,
            expected="code=2001, HTTP 401",
            actual=f"code={data.get('code')}, HTTP {status}",
            severity="low",
            category="functionality",
            description="错误密码登录的响应测试",
            affected_apis=["POST /api/v1/auth/login"],
            reproduction_steps=["1. 使用错误密码登录"],
            labels=["auth", "login"]
        )

        # 4. 测试登录 - 不存在的用户
        status, data = self.make_request("POST", "/auth/login",
                                          data={"usernameOrEmail": "nonexistent", "password": "Admin123"})
        self.test_assert(
            "登录 - 不存在的用户返回 401",
            data.get("code") == 2001 and status == 401,
            expected="code=2001, HTTP 401",
            actual=f"code={data.get('code')}, HTTP {status}",
            severity="low",
            category="functionality",
            description="不存在用户登录的响应测试",
            affected_apis=["POST /api/v1/auth/login"],
            reproduction_steps=["1. 使用不存在的用户名登录"],
            labels=["auth", "login"]
        )

        # 5. 测试刷新 Token - 空 Token
        status, data = self.make_request("POST", "/auth/refresh",
                                          data={"refreshToken": ""})
        self.test_assert(
            "刷新 Token - 空 Token 返回 400",
            data.get("code") == 400,
            expected="code=400",
            actual=f"code={data.get('code')}",
            severity="low",
            category="validation",
            description="空刷新 Token 的验证测试",
            affected_apis=["POST /api/v1/auth/refresh"],
            reproduction_steps=["1. 发送空 refreshToken 的刷新请求"],
            labels=["auth", "validation", "token"]
        )

        # 6. 测试刷新 Token - 无效 Token
        status, data = self.make_request("POST", "/auth/refresh",
                                          data={"refreshToken": "invalid_token"})
        self.test_assert(
            "刷新 Token - 无效 Token 返回 401",
            data.get("code") == 2003 or status == 401,
            expected="code=2003 或 HTTP 401",
            actual=f"code={data.get('code')}, HTTP {status}",
            severity="low",
            category="functionality",
            description="无效刷新 Token 的响应测试",
            affected_apis=["POST /api/v1/auth/refresh"],
            reproduction_steps=["1. 发送无效的 refreshToken"],
            labels=["auth", "validation", "token"]
        )

        # 7. 测试登出 - 无认证
        status, data = self.make_request("POST", "/auth/logout")
        self.test_assert(
            "登出 - 无认证返回 401",
            status == 401,
            expected="HTTP 401",
            actual=f"HTTP {status}",
            severity="low",
            category="functionality",
            description="无认证登出的响应测试",
            affected_apis=["POST /api/v1/auth/logout"],
            reproduction_steps=["1. 不带 Token 发送登出请求"],
            labels=["auth", "logout"]
        )

        self.log("=== 认证模块测试完成 ===", "HEADER")

    def test_user_module(self):
        """测试用户模块"""
        self.log("=== 开始测试用户模块 ===", "HEADER")

        # 1. 测试获取用户资料 - 无认证
        status, data = self.make_request("GET", "/user/profile")
        self.test_assert(
            "获取用户资料 - 无认证返回 401",
            status == 401,
            expected="HTTP 401",
            actual=f"HTTP {status}",
            severity="low",
            category="security",
            description="未认证访问用户资料的测试",
            affected_apis=["GET /api/v1/user/profile"],
            reproduction_steps=["1. 不带 Token 访问用户资料接口"],
            labels=["user", "security"]
        )

        if not self.access_token:
            self.log("跳过后续用户模块测试（无 Token）", "WARN")
            self.log("=== 用户模块测试完成 ===", "HEADER")
            return

        # 2. 测试获取用户资料 - 有认证
        status, data = self.make_request("GET", "/user/profile", use_auth=True)
        self.test_assert(
            "获取用户资料 - 有认证返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="medium",
            category="functionality",
            description="认证访问用户资料的测试",
            affected_apis=["GET /api/v1/user/profile"],
            reproduction_steps=["1. 登录后获取 Token", "2. 带 Token 访问用户资料接口"],
            labels=["user", "profile"]
        )

        # 3. 测试更新用户资料
        update_data = {
            "nickname": "Test Admin",
            "avatar": "https://example.com/avatar.jpg"
        }
        status, data = self.make_request("PUT", "/user/profile",
                                          data=update_data, use_auth=True)
        self.test_assert(
            "更新用户资料返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="medium",
            category="functionality",
            description="更新用户资料的测试",
            affected_apis=["PUT /api/v1/user/profile"],
            reproduction_steps=["1. 登录后获取 Token", "2. PUT 更新用户资料"],
            labels=["user", "profile", "update"]
        )

        # 4. 测试修改密码 - 旧密码错误
        status, data = self.make_request("POST", "/user/change-password",
                                          data={"oldPassword": "wrong",
                                                "newPassword": "New123!"},
                                          use_auth=True)
        self.test_assert(
            "修改密码 - 旧密码错误返回 401",
            status == 401 or data.get("code") == 3001,
            expected="HTTP 401 或 code=3001",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="low",
            category="functionality",
            description="错误旧密码修改密码的测试",
            affected_apis=["POST /api/v1/user/change-password"],
            reproduction_steps=["1. 登录后获取 Token", "2. 使用错误旧密码修改密码"],
            labels=["user", "password"]
        )

        # 5. 测试修改密码 - 空新密码
        status, data = self.make_request("POST", "/user/change-password",
                                          data={"oldPassword": "Admin123",
                                                "newPassword": ""},
                                          use_auth=True)
        self.test_assert(
            "修改密码 - 空新密码返回 400",
            data.get("code") == 400 or status == 400,
            expected="HTTP 400 或 code=400",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="low",
            category="validation",
            description="空新密码的验证测试",
            affected_apis=["POST /api/v1/user/change-password"],
            reproduction_steps=["1. 登录后获取 Token", "2. 使用空新密码修改密码"],
            labels=["user", "password", "validation"]
        )

        self.log("=== 用户模块测试完成 ===", "HEADER")

    def test_project_module(self):
        """测试项目模块"""
        self.log("=== 开始测试项目模块 ===", "HEADER")

        if not self.access_token:
            self.log("跳过项目模块测试（无 Token）", "WARN")
            self.log("=== 项目模块测试完成 ===", "HEADER")
            return

        # 1. 测试获取项目列表 - 无认证
        status, data = self.make_request("GET", "/projects")
        self.test_assert(
            "获取项目列表 - 无认证返回 401",
            status == 401,
            expected="HTTP 401",
            actual=f"HTTP {status}",
            severity="low",
            category="security",
            description="未认证访问项目列表的测试",
            affected_apis=["GET /api/v1/projects"],
            reproduction_steps=["1. 不带 Token 访问项目列表接口"],
            labels=["project", "security"]
        )

        # 2. 测试获取项目列表 - 有认证
        status, data = self.make_request("GET", "/projects", use_auth=True)
        self.test_assert(
            "获取项目列表 - 有认证返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="medium",
            category="functionality",
            description="认证访问项目列表的测试",
            affected_apis=["GET /api/v1/projects"],
            reproduction_steps=["1. 登录后获取 Token", "2. 带 Token 访问项目列表接口"],
            labels=["project", "list"]
        )

        # 3. 测试创建项目
        project_data = {
            "name": f"Test Project {int(time.time())}",
            "key": f"TP{int(time.time())}",
            "description": "API Test Project",
            "leadUserId": 1,
            "type": "SOFTWARE"
        }
        status, data = self.make_request("POST", "/projects",
                                          data=project_data, use_auth=True)
        if status == 200 and data.get("code") == 200:
            self.project_id = data.get("data", {}).get("id")
            self.log(f"创建项目成功，ID: {self.project_id}", "GREEN")

        self.test_assert(
            "创建项目返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="medium",
            category="functionality",
            description="创建项目的测试",
            affected_apis=["POST /api/v1/projects"],
            reproduction_steps=["1. 登录后获取 Token", "2. POST 创建项目"],
            labels=["project", "create"]
        )

        # 4. 测试创建项目 - 空名称
        status, data = self.make_request("POST", "/projects",
                                          data={"key": "TEST", "description": "Test"},
                                          use_auth=True)
        self.test_assert(
            "创建项目 - 空名称返回 400",
            data.get("code") == 400 or status == 400,
            expected="HTTP 400 或 code=400",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="low",
            category="validation",
            description="空项目名称的验证测试",
            affected_apis=["POST /api/v1/projects"],
            reproduction_steps=["1. 登录后获取 Token", "2. POST 创建空名称项目"],
            labels=["project", "validation"]
        )

        # 5. 测试获取项目详情 - 不存在的 ID
        status, data = self.make_request("GET", "/projects/99999", use_auth=True)
        self.test_assert(
            "获取项目详情 - 不存在的 ID 返回 404",
            status == 404 or data.get("code") == 404,
            expected="HTTP 404 或 code=404",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="low",
            category="functionality",
            description="获取不存在项目的测试",
            affected_apis=["GET /api/v1/projects/{id}"],
            reproduction_steps=["1. 登录后获取 Token", "2. GET 访问不存在的项目 ID"],
            labels=["project", "not-found"]
        )

        # 6. 测试更新项目
        if self.project_id:
            status, data = self.make_request("PUT", f"/projects/{self.project_id}",
                                              data={"name": "Updated Project Name"},
                                              use_auth=True)
            self.test_assert(
                "更新项目返回 200",
                status == 200 and data.get("code") == 200,
                expected="HTTP 200, code=200",
                actual=f"HTTP {status}, code={data.get('code')}",
                severity="medium",
                category="functionality",
                description="更新项目的测试",
                affected_apis=["PUT /api/v1/projects/{id}"],
                reproduction_steps=["1. 登录后获取 Token", "2. PUT 更新项目"],
                labels=["project", "update"]
            )

        # 7. 测试添加项目成员
        if self.project_id:
            status, data = self.make_request("POST", f"/projects/{self.project_id}/members",
                                              data={"userId": 2, "role": "MEMBER"},
                                              use_auth=True)
            self.test_assert(
                "添加项目成员返回 200",
                status == 200 and data.get("code") == 200,
                expected="HTTP 200, code=200",
                actual=f"HTTP {status}, code={data.get('code')}",
                severity="medium",
                category="functionality",
                description="添加项目成员的测试",
                affected_apis=["POST /api/v1/projects/{id}/members"],
                reproduction_steps=["1. 登录后获取 Token", "2. POST 添加项目成员"],
                labels=["project", "member"]
            )

        # 8. 测试删除项目
        if self.project_id:
            status, data = self.make_request("DELETE", f"/projects/{self.project_id}",
                                              use_auth=True)
            self.test_assert(
                "删除项目返回 200",
                status == 200 and data.get("code") == 200,
                expected="HTTP 200, code=200",
                actual=f"HTTP {status}, code={data.get('code')}",
                severity="medium",
                category="functionality",
                description="删除项目的测试",
                affected_apis=["DELETE /api/v1/projects/{id}"],
                reproduction_steps=["1. 登录后获取 Token", "2. DELETE 删除项目"],
                labels=["project", "delete"]
            )

        self.log("=== 项目模块测试完成 ===", "HEADER")

    def test_task_module(self):
        """测试任务模块"""
        self.log("=== 开始测试任务模块 ===", "HEADER")

        if not self.access_token:
            self.log("跳过任务模块测试（无 Token）", "WARN")
            self.log("=== 任务模块测试完成 ===", "HEADER")
            return

        # 先创建一个项目用于测试
        if not self.project_id:
            project_data = {
                "name": f"Task Test Project {int(time.time())}",
                "key": f"TTP{int(time.time())}",
                "description": "API Test Project for Tasks",
                "leadUserId": 1,
                "type": "SOFTWARE"
            }
            status, data = self.make_request("POST", "/projects",
                                              data=project_data, use_auth=True)
            if status == 200 and data.get("code") == 200:
                self.project_id = data.get("data", {}).get("id")
                self.log(f"创建测试项目成功，ID: {self.project_id}", "GREEN")

        if not self.project_id:
            self.log("无法创建项目，跳过任务模块测试", "WARN")
            return

        # 1. 测试创建任务
        task_data = {
            "title": "Test Task",
            "description": "API Test Task",
            "status": "TODO",
            "priority": "MEDIUM",
            "assigneeId": 1
        }
        status, data = self.make_request("POST", f"/projects/{self.project_id}/tasks",
                                          data=task_data, use_auth=True)
        if status == 200 and data.get("code") == 200:
            self.task_id = data.get("data", {}).get("id")
            self.log(f"创建任务成功，ID: {self.task_id}", "GREEN")

            # 检查 status 和 priority 是否正确返回
            resp_status = data.get("data", {}).get("status")
            resp_priority = data.get("data", {}).get("priority")
            self.test_assert(
                "创建任务 - status 和 priority 正确返回",
                resp_status == "TODO" and resp_priority == "MEDIUM",
                expected="status=TODO, priority=MEDIUM",
                actual=f"status={resp_status}, priority={resp_priority}",
                severity="high",
                category="data",
                description="创建任务时 status 和 priority 字段的返回测试",
                affected_apis=["POST /api/v1/projects/{id}/tasks"],
                reproduction_steps=["1. 登录后获取 Token", "2. POST 创建任务", "3. 检查返回的 status 和 priority"],
                labels=["task", "data-consistency"]
            )
        else:
            self.test_assert(
                "创建任务返回 200",
                False,
                expected="HTTP 200, code=200",
                actual=f"HTTP {status}, code={data.get('code')}",
                severity="high",
                category="functionality",
                description="创建任务接口测试失败",
                affected_apis=["POST /api/v1/projects/{id}/tasks"],
                reproduction_steps=["1. 登录后获取 Token", "2. POST 创建任务"],
                labels=["task", "create"]
            )

        # 2. 测试创建任务 - 空标题
        status, data = self.make_request("POST", f"/projects/{self.project_id}/tasks",
                                          data={"description": "Test"},
                                          use_auth=True)
        self.test_assert(
            "创建任务 - 空标题返回 400",
            data.get("code") == 400 or status == 400,
            expected="HTTP 400 或 code=400",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="low",
            category="validation",
            description="空任务标题的验证测试",
            affected_apis=["POST /api/v1/projects/{id}/tasks"],
            reproduction_steps=["1. 登录后获取 Token", "2. POST 创建空标题任务"],
            labels=["task", "validation"]
        )

        # 3. 测试创建任务 - 超长标题
        long_title = "A" * 500
        status, data = self.make_request("POST", f"/projects/{self.project_id}/tasks",
                                          data={"title": long_title, "description": "Test"},
                                          use_auth=True)
        self.test_assert(
            "创建任务 - 超长标题返回 400 或 422",
            data.get("code") in [400, 422] or status in [400, 422],
            expected="HTTP 400/422",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="medium",
            category="validation",
            description="超长任务标题的验证测试",
            affected_apis=["POST /api/v1/projects/{id}/tasks"],
            reproduction_steps=["1. 登录后获取 Token", "2. POST 创建超长标题任务"],
            labels=["task", "validation", "input-length"]
        )

        # 4. 测试获取任务列表
        status, data = self.make_request("GET", f"/projects/{self.project_id}/tasks",
                                          use_auth=True)
        self.test_assert(
            "获取任务列表返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="medium",
            category="functionality",
            description="获取任务列表的测试",
            affected_apis=["GET /api/v1/projects/{id}/tasks"],
            reproduction_steps=["1. 登录后获取 Token", "2. GET 获取任务列表"],
            labels=["task", "list"]
        )

        # 5. 测试获取任务详情 - 不存在的 ID
        status, data = self.make_request("GET", f"/projects/{self.project_id}/tasks/99999",
                                          use_auth=True)
        self.test_assert(
            "获取任务详情 - 不存在的 ID 返回 404",
            status == 404 or data.get("code") == 404,
            expected="HTTP 404 或 code=404",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="low",
            category="functionality",
            description="获取不存在任务的测试",
            affected_apis=["GET /api/v1/projects/{id}/tasks/{id}"],
            reproduction_steps=["1. 登录后获取 Token", "2. GET 访问不存在的任务 ID"],
            labels=["task", "not-found"]
        )

        # 6. 测试更新任务
        if self.task_id:
            status, data = self.make_request("PUT", f"/projects/{self.project_id}/tasks/{self.task_id}",
                                              data={"title": "Updated Task", "status": "IN_PROGRESS", "priority": "HIGH"},
                                              use_auth=True)
            self.test_assert(
                "更新任务返回 200",
                status == 200 and data.get("code") == 200,
                expected="HTTP 200, code=200",
                actual=f"HTTP {status}, code={data.get('code')}",
                severity="medium",
                category="functionality",
                description="更新任务的测试",
                affected_apis=["PUT /api/v1/projects/{id}/tasks/{id}"],
                reproduction_steps=["1. 登录后获取 Token", "2. PUT 更新任务"],
                labels=["task", "update"]
            )

            # 7. 测试更新后 GET 获取任务详情 - 检查数据一致性
            if status == 200 and data.get("code") == 200:
                get_status, get_data = self.make_request("GET", f"/projects/{self.project_id}/tasks/{self.task_id}",
                                                          use_auth=True)
                get_status_val = get_data.get("data", {}).get("status")
                get_priority_val = get_data.get("data", {}).get("priority")
                self.test_assert(
                    "更新任务后 GET 返回正确的 status 和 priority",
                    get_status_val == "IN_PROGRESS" and get_priority_val == "HIGH",
                    expected="status=IN_PROGRESS, priority=HIGH",
                    actual=f"status={get_status_val}, priority={get_priority_val}",
                    severity="high",
                    category="data",
                    description="更新任务后通过 GET 获取时 status 和 priority 字段的返回测试",
                    affected_apis=["GET /api/v1/projects/{id}/tasks/{id}"],
                    reproduction_steps=[
                        "1. 登录后获取 Token",
                        "2. PUT 更新任务 status=IN_PROGRESS, priority=HIGH",
                        "3. GET 获取任务详情",
                        "4. 检查 status 和 priority 字段"
                    ],
                    labels=["task", "data-consistency"]
                )

        # 8. 测试切换任务完成状态
        if self.task_id:
            status, data = self.make_request("POST", f"/projects/{self.project_id}/tasks/{self.task_id}/toggle-complete",
                                              use_auth=True)
            self.test_assert(
                "切换任务完成状态返回 200",
                status == 200 and data.get("code") == 200,
                expected="HTTP 200, code=200",
                actual=f"HTTP {status}, code={data.get('code')}",
                severity="medium",
                category="functionality",
                description="切换任务完成状态的测试",
                affected_apis=["POST /api/v1/projects/{id}/tasks/{id}/toggle-complete"],
                reproduction_steps=["1. 登录后获取 Token", "2. POST 切换任务完成状态"],
                labels=["task", "toggle"]
            )

        # 9. 测试删除任务
        if self.task_id:
            status, data = self.make_request("DELETE", f"/projects/{self.project_id}/tasks/{self.task_id}",
                                              use_auth=True)
            self.test_assert(
                "删除任务返回 200",
                status == 200 and data.get("code") == 200,
                expected="HTTP 200, code=200",
                actual=f"HTTP {status}, code={data.get('code')}",
                severity="medium",
                category="functionality",
                description="删除任务的测试",
                affected_apis=["DELETE /api/v1/projects/{id}/tasks/{id}"],
                reproduction_steps=["1. 登录后获取 Token", "2. DELETE 删除任务"],
                labels=["task", "delete"]
            )

        self.log("=== 任务模块测试完成 ===", "HEADER")

    def test_comment_module(self):
        """测试评论模块"""
        self.log("=== 开始测试评论模块 ===", "HEADER")

        if not self.access_token:
            self.log("跳过评论模块测试（无 Token）", "WARN")
            self.log("=== 评论模块测试完成 ===", "HEADER")
            return

        # 先创建项目和任务
        if not self.project_id:
            project_data = {
                "name": f"Comment Test Project {int(time.time())}",
                "key": f"CTP{int(time.time())}",
                "description": "API Test Project for Comments",
                "leadUserId": 1,
                "type": "SOFTWARE"
            }
            status, data = self.make_request("POST", "/projects",
                                              data=project_data, use_auth=True)
            if status == 200 and data.get("code") == 200:
                self.project_id = data.get("data", {}).get("id")

        if not self.task_id and self.project_id:
            task_data = {
                "title": "Comment Test Task",
                "description": "API Test Task for Comments",
                "status": "TODO",
                "priority": "MEDIUM"
            }
            status, data = self.make_request("POST", f"/projects/{self.project_id}/tasks",
                                              data=task_data, use_auth=True)
            if status == 200 and data.get("code") == 200:
                self.task_id = data.get("data", {}).get("id")

        if not self.task_id:
            self.log("无法创建任务，跳过评论模块测试", "WARN")
            return

        comment_id = None

        # 1. 测试创建评论
        status, data = self.make_request("POST", f"/tasks/{self.task_id}/comments",
                                          data={"content": "Test comment"},
                                          use_auth=True)
        if status == 200 and data.get("code") == 200:
            comment_id = data.get("data", {}).get("id")
            self.log(f"创建评论成功，ID: {comment_id}", "GREEN")

        self.test_assert(
            "创建评论返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="medium",
            category="functionality",
            description="创建评论的测试",
            affected_apis=["POST /api/v1/tasks/{id}/comments"],
            reproduction_steps=["1. 登录后获取 Token", "2. POST 创建评论"],
            labels=["comment", "create"]
        )

        # 2. 测试创建评论 - 空内容
        status, data = self.make_request("POST", f"/tasks/{self.task_id}/comments",
                                          data={},
                                          use_auth=True)
        self.test_assert(
            "创建评论 - 空内容返回 400",
            data.get("code") == 400 or status == 400,
            expected="HTTP 400 或 code=400",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="low",
            category="validation",
            description="空评论内容的验证测试",
            affected_apis=["POST /api/v1/tasks/{id}/comments"],
            reproduction_steps=["1. 登录后获取 Token", "2. POST 创建空内容评论"],
            labels=["comment", "validation"]
        )

        # 3. 测试创建评论 - 不存在的任务
        status, data = self.make_request("POST", "/tasks/99999/comments",
                                          data={"content": "Test"},
                                          use_auth=True)
        self.test_assert(
            "创建评论 - 不存在的任务返回 404",
            status == 404 or data.get("code") == 404,
            expected="HTTP 404 或 code=404",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="low",
            category="functionality",
            description="在不存在任务上创建评论的测试",
            affected_apis=["POST /api/v1/tasks/{id}/comments"],
            reproduction_steps=["1. 登录后获取 Token", "2. POST 到不存在的任务 ID 创建评论"],
            labels=["comment", "not-found"]
        )

        # 4. 测试获取评论列表
        status, data = self.make_request("GET", f"/tasks/{self.task_id}/comments",
                                          use_auth=True)
        self.test_assert(
            "获取评论列表返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="medium",
            category="functionality",
            description="获取评论列表的测试",
            affected_apis=["GET /api/v1/tasks/{id}/comments"],
            reproduction_steps=["1. 登录后获取 Token", "2. GET 获取评论列表"],
            labels=["comment", "list"]
        )

        # 5. 测试获取评论详情 - 不存在的 ID
        status, data = self.make_request("GET", f"/tasks/{self.task_id}/comments/99999",
                                          use_auth=True)
        self.test_assert(
            "获取评论详情 - 不存在的 ID 返回 404",
            status == 404 or data.get("code") == 404,
            expected="HTTP 404 或 code=404",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="low",
            category="functionality",
            description="获取不存在评论的测试",
            affected_apis=["GET /api/v1/tasks/{id}/comments/{id}"],
            reproduction_steps=["1. 登录后获取 Token", "2. GET 访问不存在的评论 ID"],
            labels=["comment", "not-found"]
        )

        # 6. 测试更新评论
        if comment_id:
            status, data = self.make_request("PUT", f"/tasks/{self.task_id}/comments/{comment_id}",
                                              data={"content": "Updated comment"},
                                              use_auth=True)
            self.test_assert(
                "更新评论返回 200",
                status == 200 and data.get("code") == 200,
                expected="HTTP 200, code=200",
                actual=f"HTTP {status}, code={data.get('code')}",
                severity="medium",
                category="functionality",
                description="更新评论的测试",
                affected_apis=["PUT /api/v1/tasks/{id}/comments/{id}"],
                reproduction_steps=["1. 登录后获取 Token", "2. PUT 更新评论"],
                labels=["comment", "update"]
            )

        # 7. 测试删除评论
        if comment_id:
            status, data = self.make_request("DELETE", f"/tasks/{self.task_id}/comments/{comment_id}",
                                              use_auth=True)
            self.test_assert(
                "删除评论返回 200",
                status == 200 and data.get("code") == 200,
                expected="HTTP 200, code=200",
                actual=f"HTTP {status}, code={data.get('code')}",
                severity="medium",
                category="functionality",
                description="删除评论的测试",
                affected_apis=["DELETE /api/v1/tasks/{id}/comments/{id}"],
                reproduction_steps=["1. 登录后获取 Token", "2. DELETE 删除评论"],
                labels=["comment", "delete"]
            )

        self.log("=== 评论模块测试完成 ===", "HEADER")

    def test_epic_module(self):
        """测试史诗模块"""
        self.log("=== 开始测试史诗模块 ===", "HEADER")

        if not self.access_token:
            self.log("跳过史诗模块测试（无 Token）", "WARN")
            self.log("=== 史诗模块测试完成 ===", "HEADER")
            return

        # 先创建项目
        if not self.project_id:
            project_data = {
                "name": f"Epic Test Project {int(time.time())}",
                "key": f"ETP{int(time.time())}",
                "description": "API Test Project for Epics",
                "leadUserId": 1,
                "type": "SOFTWARE"
            }
            status, data = self.make_request("POST", "/projects",
                                              data=project_data, use_auth=True)
            if status == 200 and data.get("code") == 200:
                self.project_id = data.get("data", {}).get("id")

        if not self.project_id:
            self.log("无法创建项目，跳过史诗模块测试", "WARN")
            return

        epic_id = None

        # 1. 测试创建史诗
        epic_data = {
            "name": "Test Epic",
            "description": "API Test Epic",
            "status": "TODO",
            "color": "#FF5733"
        }
        status, data = self.make_request("POST", f"/projects/{self.project_id}/epics",
                                          data=epic_data, use_auth=True)
        if status == 200 and data.get("code") == 200:
            epic_id = data.get("data", {}).get("id")
            self.log(f"创建史诗成功，ID: {epic_id}", "GREEN")

        self.test_assert(
            "创建史诗返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="high",
            category="functionality",
            description="创建史诗接口测试",
            affected_apis=["POST /api/v1/projects/{id}/epics"],
            reproduction_steps=["1. 登录后获取 Token", "2. POST 创建史诗"],
            labels=["epic", "create"]
        )

        # 2. 测试创建史诗 - 空名称
        status, data = self.make_request("POST", f"/projects/{self.project_id}/epics",
                                          data={"description": "Test"},
                                          use_auth=True)
        self.test_assert(
            "创建史诗 - 空名称返回 400",
            data.get("code") == 400 or status == 400,
            expected="HTTP 400 或 code=400",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="low",
            category="validation",
            description="空史诗名称的验证测试",
            affected_apis=["POST /api/v1/projects/{id}/epics"],
            reproduction_steps=["1. 登录后获取 Token", "2. POST 创建空名称史诗"],
            labels=["epic", "validation"]
        )

        # 3. 测试获取史诗列表
        status, data = self.make_request("GET", f"/projects/{self.project_id}/epics",
                                          use_auth=True)
        self.test_assert(
            "获取史诗列表返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="medium",
            category="functionality",
            description="获取史诗列表的测试",
            affected_apis=["GET /api/v1/projects/{id}/epics"],
            reproduction_steps=["1. 登录后获取 Token", "2. GET 获取史诗列表"],
            labels=["epic", "list"]
        )

        # 4. 测试获取史诗详情 - 不存在的 ID
        status, data = self.make_request("GET", f"/projects/{self.project_id}/epics/99999",
                                          use_auth=True)
        self.test_assert(
            "获取史诗详情 - 不存在的 ID 返回 404",
            status == 404 or data.get("code") == 404,
            expected="HTTP 404 或 code=404",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="low",
            category="functionality",
            description="获取不存在史诗的测试",
            affected_apis=["GET /api/v1/projects/{id}/epics/{id}"],
            reproduction_steps=["1. 登录后获取 Token", "2. GET 访问不存在的史诗 ID"],
            labels=["epic", "not-found"]
        )

        # 5. 测试更新史诗
        if epic_id:
            status, data = self.make_request("PUT", f"/projects/{self.project_id}/epics/{epic_id}",
                                              data={"name": "Updated Epic"},
                                              use_auth=True)
            self.test_assert(
                "更新史诗返回 200",
                status == 200 and data.get("code") == 200,
                expected="HTTP 200, code=200",
                actual=f"HTTP {status}, code={data.get('code')}",
                severity="medium",
                category="functionality",
                description="更新史诗的测试",
                affected_apis=["PUT /api/v1/projects/{id}/epics/{id}"],
                reproduction_steps=["1. 登录后获取 Token", "2. PUT 更新史诗"],
                labels=["epic", "update"]
            )

        # 6. 测试删除史诗
        if epic_id:
            status, data = self.make_request("DELETE", f"/projects/{self.project_id}/epics/{epic_id}",
                                              use_auth=True)
            self.test_assert(
                "删除史诗返回 200",
                status == 200 and data.get("code") == 200,
                expected="HTTP 200, code=200",
                actual=f"HTTP {status}, code={data.get('code')}",
                severity="medium",
                category="functionality",
                description="删除史诗的测试",
                affected_apis=["DELETE /api/v1/projects/{id}/epics/{id}"],
                reproduction_steps=["1. 登录后获取 Token", "2. DELETE 删除史诗"],
                labels=["epic", "delete"]
            )

        self.log("=== 史诗模块测试完成 ===", "HEADER")

    def test_userstory_module(self):
        """测试用户故事模块"""
        self.log("=== 开始测试用户故事模块 ===", "HEADER")

        if not self.access_token:
            self.log("跳过用户故事模块测试（无 Token）", "WARN")
            self.log("=== 用户故事模块测试完成 ===", "HEADER")
            return

        # 先创建项目
        if not self.project_id:
            project_data = {
                "name": f"Story Test Project {int(time.time())}",
                "key": f"STP{int(time.time())}",
                "description": "API Test Project for User Stories",
                "leadUserId": 1,
                "type": "SOFTWARE"
            }
            status, data = self.make_request("POST", "/projects",
                                              data=project_data, use_auth=True)
            if status == 200 and data.get("code") == 200:
                self.project_id = data.get("data", {}).get("id")

        if not self.project_id:
            self.log("无法创建项目，跳过用户故事模块测试", "WARN")
            return

        story_id = None

        # 1. 测试创建用户故事
        story_data = {
            "title": "Test User Story",
            "description": "API Test User Story",
            "status": "TODO",
            "priority": "MEDIUM"
        }
        status, data = self.make_request("POST", f"/projects/{self.project_id}/stories",
                                          data=story_data, use_auth=True)
        if status == 200 and data.get("code") == 200:
            story_id = data.get("data", {}).get("id")
            self.log(f"创建用户故事成功，ID: {story_id}", "GREEN")

        self.test_assert(
            "创建用户故事返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="high",
            category="functionality",
            description="创建用户故事接口测试",
            affected_apis=["POST /api/v1/projects/{id}/stories"],
            reproduction_steps=["1. 登录后获取 Token", "2. POST 创建用户故事"],
            labels=["userstory", "create"]
        )

        # 2. 测试创建用户故事 - 空标题
        status, data = self.make_request("POST", f"/projects/{self.project_id}/stories",
                                          data={"description": "Test"},
                                          use_auth=True)
        self.test_assert(
            "创建用户故事 - 空标题返回 400",
            data.get("code") == 400 or status == 400,
            expected="HTTP 400 或 code=400",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="low",
            category="validation",
            description="空用户故事标题的验证测试",
            affected_apis=["POST /api/v1/projects/{id}/stories"],
            reproduction_steps=["1. 登录后获取 Token", "2. POST 创建空标题用户故事"],
            labels=["userstory", "validation"]
        )

        # 3. 测试获取用户故事列表
        status, data = self.make_request("GET", f"/projects/{self.project_id}/stories",
                                          use_auth=True)
        self.test_assert(
            "获取用户故事列表返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="medium",
            category="functionality",
            description="获取用户故事列表的测试",
            affected_apis=["GET /api/v1/projects/{id}/stories"],
            reproduction_steps=["1. 登录后获取 Token", "2. GET 获取用户故事列表"],
            labels=["userstory", "list"]
        )

        # 4. 测试获取用户故事详情 - 不存在的 ID
        status, data = self.make_request("GET", f"/projects/{self.project_id}/stories/99999",
                                          use_auth=True)
        self.test_assert(
            "获取用户故事详情 - 不存在的 ID 返回 404",
            status == 404 or data.get("code") == 404,
            expected="HTTP 404 或 code=404",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="low",
            category="functionality",
            description="获取不存在用户故事的测试",
            affected_apis=["GET /api/v1/projects/{id}/stories/{id}"],
            reproduction_steps=["1. 登录后获取 Token", "2. GET 访问不存在的用户故事 ID"],
            labels=["userstory", "not-found"]
        )

        # 5. 测试更新用户故事
        if story_id:
            status, data = self.make_request("PUT", f"/projects/{self.project_id}/stories/{story_id}",
                                              data={"title": "Updated Story"},
                                              use_auth=True)
            self.test_assert(
                "更新用户故事返回 200",
                status == 200 and data.get("code") == 200,
                expected="HTTP 200, code=200",
                actual=f"HTTP {status}, code={data.get('code')}",
                severity="medium",
                category="functionality",
                description="更新用户故事的测试",
                affected_apis=["PUT /api/v1/projects/{id}/stories/{id}"],
                reproduction_steps=["1. 登录后获取 Token", "2. PUT 更新用户故事"],
                labels=["userstory", "update"]
            )

        # 6. 测试删除用户故事
        if story_id:
            status, data = self.make_request("DELETE", f"/projects/{self.project_id}/stories/{story_id}",
                                              use_auth=True)
            self.test_assert(
                "删除用户故事返回 200",
                status == 200 and data.get("code") == 200,
                expected="HTTP 200, code=200",
                actual=f"HTTP {status}, code={data.get('code')}",
                severity="medium",
                category="functionality",
                description="删除用户故事的测试",
                affected_apis=["DELETE /api/v1/projects/{id}/stories/{id}"],
                reproduction_steps=["1. 登录后获取 Token", "2. DELETE 删除用户故事"],
                labels=["userstory", "delete"]
            )

        self.log("=== 用户故事模块测试完成 ===", "HEADER")

    def test_issue_module(self):
        """测试 Issue 模块"""
        self.log("=== 开始测试 Issue 模块 ===", "HEADER")

        if not self.access_token:
            self.log("跳过 Issue 模块测试（无 Token）", "WARN")
            self.log("=== Issue 模块测试完成 ===", "HEADER")
            return

        # 先创建项目
        if not self.project_id:
            project_data = {
                "name": f"Issue Test Project {int(time.time())}",
                "key": f"ITP{int(time.time())}",
                "description": "API Test Project for Issues",
                "leadUserId": 1,
                "type": "SOFTWARE"
            }
            status, data = self.make_request("POST", "/projects",
                                              data=project_data, use_auth=True)
            if status == 200 and data.get("code") == 200:
                self.project_id = data.get("data", {}).get("id")

        if not self.project_id:
            self.log("无法创建项目，跳过 Issue 模块测试", "WARN")
            return

        issue_id = None

        # 1. 测试创建 Issue
        issue_data = {
            "title": "Test Issue",
            "description": "API Test Issue",
            "type": "BUG",
            "priority": "HIGH",
            "status": "OPEN"
        }
        status, data = self.make_request("POST", f"/projects/{self.project_id}/issues",
                                          data=issue_data, use_auth=True)
        if status == 200 and data.get("code") == 200:
            issue_id = data.get("data", {}).get("id")
            self.log(f"创建 Issue 成功，ID: {issue_id}", "GREEN")

        self.test_assert(
            "创建 Issue 返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="high",
            category="functionality",
            description="创建 Issue 接口测试",
            affected_apis=["POST /api/v1/projects/{id}/issues"],
            reproduction_steps=["1. 登录后获取 Token", "2. POST 创建 Issue"],
            labels=["issue", "create"]
        )

        # 2. 测试创建 Issue - 空标题
        status, data = self.make_request("POST", f"/projects/{self.project_id}/issues",
                                          data={"description": "Test"},
                                          use_auth=True)
        self.test_assert(
            "创建 Issue - 空标题返回 400",
            data.get("code") == 400 or status == 400,
            expected="HTTP 400 或 code=400",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="low",
            category="validation",
            description="空 Issue 标题的验证测试",
            affected_apis=["POST /api/v1/projects/{id}/issues"],
            reproduction_steps=["1. 登录后获取 Token", "2. POST 创建空标题 Issue"],
            labels=["issue", "validation"]
        )

        # 3. 测试获取 Issue 列表
        status, data = self.make_request("GET", f"/projects/{self.project_id}/issues",
                                          use_auth=True)
        self.test_assert(
            "获取 Issue 列表返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="medium",
            category="functionality",
            description="获取 Issue 列表的测试",
            affected_apis=["GET /api/v1/projects/{id}/issues"],
            reproduction_steps=["1. 登录后获取 Token", "2. GET 获取 Issue 列表"],
            labels=["issue", "list"]
        )

        # 4. 测试获取 Issue 详情 - 不存在的 ID
        status, data = self.make_request("GET", f"/projects/{self.project_id}/issues/99999",
                                          use_auth=True)
        self.test_assert(
            "获取 Issue 详情 - 不存在的 ID 返回 404",
            status == 404 or data.get("code") == 404,
            expected="HTTP 404 或 code=404",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="low",
            category="functionality",
            description="获取不存在 Issue 的测试",
            affected_apis=["GET /api/v1/projects/{id}/issues/{id}"],
            reproduction_steps=["1. 登录后获取 Token", "2. GET 访问不存在的 Issue ID"],
            labels=["issue", "not-found"]
        )

        # 5. 测试更新 Issue
        if issue_id:
            status, data = self.make_request("PUT", f"/projects/{self.project_id}/issues/{issue_id}",
                                              data={"title": "Updated Issue"},
                                              use_auth=True)
            self.test_assert(
                "更新 Issue 返回 200",
                status == 200 and data.get("code") == 200,
                expected="HTTP 200, code=200",
                actual=f"HTTP {status}, code={data.get('code')}",
                severity="medium",
                category="functionality",
                description="更新 Issue 的测试",
                affected_apis=["PUT /api/v1/projects/{id}/issues/{id}"],
                reproduction_steps=["1. 登录后获取 Token", "2. PUT 更新 Issue"],
                labels=["issue", "update"]
            )

        # 6. 测试删除 Issue
        if issue_id:
            status, data = self.make_request("DELETE", f"/projects/{self.project_id}/issues/{issue_id}",
                                              use_auth=True)
            self.test_assert(
                "删除 Issue 返回 200",
                status == 200 and data.get("code") == 200,
                expected="HTTP 200, code=200",
                actual=f"HTTP {status}, code={data.get('code')}",
                severity="medium",
                category="functionality",
                description="删除 Issue 的测试",
                affected_apis=["DELETE /api/v1/projects/{id}/issues/{id}"],
                reproduction_steps=["1. 登录后获取 Token", "2. DELETE 删除 Issue"],
                labels=["issue", "delete"]
            )

        self.log("=== Issue 模块测试完成 ===", "HEADER")

    def test_wiki_module(self):
        """测试 Wiki 模块"""
        self.log("=== 开始测试 Wiki 模块 ===", "HEADER")

        if not self.access_token:
            self.log("跳过 Wiki 模块测试（无 Token）", "WARN")
            self.log("=== Wiki 模块测试完成 ===", "HEADER")
            return

        # 先创建项目
        if not self.project_id:
            project_data = {
                "name": f"Wiki Test Project {int(time.time())}",
                "key": f"WTP{int(time.time())}",
                "description": "API Test Project for Wiki",
                "leadUserId": 1,
                "type": "SOFTWARE"
            }
            status, data = self.make_request("POST", "/projects",
                                              data=project_data, use_auth=True)
            if status == 200 and data.get("code") == 200:
                self.project_id = data.get("data", {}).get("id")

        if not self.project_id:
            self.log("无法创建项目，跳过 Wiki 模块测试", "WARN")
            return

        wiki_id = None

        # 1. 测试创建 Wiki
        wiki_data = {
            "title": "Test Wiki",
            "content": "API Test Wiki Content",
            "parentId": None
        }
        status, data = self.make_request("POST", f"/projects/{self.project_id}/wiki",
                                          data=wiki_data, use_auth=True)
        if status == 200 and data.get("code") == 200:
            wiki_id = data.get("data", {}).get("id")
            self.log(f"创建 Wiki 成功，ID: {wiki_id}", "GREEN")

        self.test_assert(
            "创建 Wiki 返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="high",
            category="functionality",
            description="创建 Wiki 接口测试",
            affected_apis=["POST /api/v1/projects/{id}/wiki"],
            reproduction_steps=["1. 登录后获取 Token", "2. POST 创建 Wiki"],
            labels=["wiki", "create"]
        )

        # 2. 测试创建 Wiki - 空标题
        status, data = self.make_request("POST", f"/projects/{self.project_id}/wiki",
                                          data={"content": "Test"},
                                          use_auth=True)
        self.test_assert(
            "创建 Wiki - 空标题返回 400",
            data.get("code") == 400 or status == 400,
            expected="HTTP 400 或 code=400",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="low",
            category="validation",
            description="空 Wiki 标题的验证测试",
            affected_apis=["POST /api/v1/projects/{id}/wiki"],
            reproduction_steps=["1. 登录后获取 Token", "2. POST 创建空标题 Wiki"],
            labels=["wiki", "validation"]
        )

        # 3. 测试获取 Wiki 列表
        status, data = self.make_request("GET", f"/projects/{self.project_id}/wiki",
                                          use_auth=True)
        self.test_assert(
            "获取 Wiki 列表返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="medium",
            category="functionality",
            description="获取 Wiki 列表的测试",
            affected_apis=["GET /api/v1/projects/{id}/wiki"],
            reproduction_steps=["1. 登录后获取 Token", "2. GET 获取 Wiki 列表"],
            labels=["wiki", "list"]
        )

        # 4. 测试获取 Wiki 详情 - 不存在的 ID
        status, data = self.make_request("GET", f"/projects/{self.project_id}/wiki/99999",
                                          use_auth=True)
        self.test_assert(
            "获取 Wiki 详情 - 不存在的 ID 返回 404",
            status == 404 or data.get("code") == 404,
            expected="HTTP 404 或 code=404",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="low",
            category="functionality",
            description="获取不存在 Wiki 的测试",
            affected_apis=["GET /api/v1/projects/{id}/wiki/{id}"],
            reproduction_steps=["1. 登录后获取 Token", "2. GET 访问不存在的 Wiki ID"],
            labels=["wiki", "not-found"]
        )

        # 5. 测试更新 Wiki
        if wiki_id:
            status, data = self.make_request("PUT", f"/projects/{self.project_id}/wiki/{wiki_id}",
                                              data={"title": "Updated Wiki"},
                                              use_auth=True)
            self.test_assert(
                "更新 Wiki 返回 200",
                status == 200 and data.get("code") == 200,
                expected="HTTP 200, code=200",
                actual=f"HTTP {status}, code={data.get('code')}",
                severity="medium",
                category="functionality",
                description="更新 Wiki 的测试",
                affected_apis=["PUT /api/v1/projects/{id}/wiki/{id}"],
                reproduction_steps=["1. 登录后获取 Token", "2. PUT 更新 Wiki"],
                labels=["wiki", "update"]
            )

        # 6. 测试删除 Wiki
        if wiki_id:
            status, data = self.make_request("DELETE", f"/projects/{self.project_id}/wiki/{wiki_id}",
                                              use_auth=True)
            self.test_assert(
                "删除 Wiki 返回 200",
                status == 200 and data.get("code") == 200,
                expected="HTTP 200, code=200",
                actual=f"HTTP {status}, code={data.get('code')}",
                severity="medium",
                category="functionality",
                description="删除 Wiki 的测试",
                affected_apis=["DELETE /api/v1/projects/{id}/wiki/{id}"],
                reproduction_steps=["1. 登录后获取 Token", "2. DELETE 删除 Wiki"],
                labels=["wiki", "delete"]
            )

        self.log("=== Wiki 模块测试完成 ===", "HEADER")

    def test_notification_module(self):
        """测试通知模块"""
        self.log("=== 开始测试通知模块 ===", "HEADER")

        if not self.access_token:
            self.log("跳过通知模块测试（无 Token）", "WARN")
            self.log("=== 通知模块测试完成 ===", "HEADER")
            return

        # 1. 测试获取通知列表 - 无认证
        status, data = self.make_request("GET", "/notifications")
        self.test_assert(
            "获取通知列表 - 无认证返回 401",
            status == 401,
            expected="HTTP 401",
            actual=f"HTTP {status}",
            severity="low",
            category="security",
            description="未认证访问通知列表的测试",
            affected_apis=["GET /api/v1/notifications"],
            reproduction_steps=["1. 不带 Token 访问通知列表接口"],
            labels=["notification", "security"]
        )

        # 2. 测试获取通知列表 - 有认证
        status, data = self.make_request("GET", "/notifications", use_auth=True)
        self.test_assert(
            "获取通知列表返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="medium",
            category="functionality",
            description="获取通知列表的测试",
            affected_apis=["GET /api/v1/notifications"],
            reproduction_steps=["1. 登录后获取 Token", "2. GET 获取通知列表"],
            labels=["notification", "list"]
        )

        # 3. 测试获取未读通知数量
        status, data = self.make_request("GET", "/notifications/unread-count", use_auth=True)
        self.test_assert(
            "获取未读通知数量返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="medium",
            category="functionality",
            description="获取未读通知数量的测试",
            affected_apis=["GET /api/v1/notifications/unread-count"],
            reproduction_steps=["1. 登录后获取 Token", "2. GET 获取未读通知数量"],
            labels=["notification", "unread"]
        )

        # 4. 测试全部标记已读
        status, data = self.make_request("POST", "/notifications/mark-all-read", use_auth=True)
        self.test_assert(
            "全部标记已读返回 200",
            status == 200 and data.get("code") == 200,
            expected="HTTP 200, code=200",
            actual=f"HTTP {status}, code={data.get('code')}",
            severity="medium",
            category="functionality",
            description="全部标记已读的测试",
            affected_apis=["POST /api/v1/notifications/mark-all-read"],
            reproduction_steps=["1. 登录后获取 Token", "2. POST 全部标记已读"],
            labels=["notification", "mark-read"]
        )

        self.log("=== 通知模块测试完成 ===", "HEADER")

    def save_results(self):
        """保存测试结果"""
        # 计算通过率
        pass_rate = 0
        if self.tests_total > 0:
            pass_rate = self.tests_passed * 100 / self.tests_total

        # 生成测试报告
        report = {
            "test_version": "v6",
            "test_date": datetime.now().strftime("%Y-%m-%d"),
            "tester": "API Test Engineer",
            "api_base_url": BASE_URL,
            "summary": {
                "total_tests": self.tests_total,
                "passed": self.tests_passed,
                "failed": self.tests_failed,
                "pass_rate": f"{pass_rate:.1f}%"
            },
            "issues": self.issues,
            "working_features": []
        }

        # 保存 JSON 文件
        output_file = f"{OUTPUT_DIR}/issues-v6-p01.json"
        with open(output_file, "w", encoding="utf-8") as f:
            json.dump(report, f, ensure_ascii=False, indent=2)

        self.log(f"测试结果已保存到：{output_file}", "INFO")

        # 生成测试报告文本
        report_text = f"""# ProjectHub Backend API 测试报告 v6

## 测试概要

| 项目 | 值 |
|------|-----|
| 测试日期 | {datetime.now().strftime("%Y-%m-%d")} |
| 测试人员 | API Test Engineer |
| API 地址 | {BASE_URL} |
| 总测试数 | {self.tests_total} |
| 通过数 | {self.tests_passed} |
| 失败数 | {self.tests_failed} |
| 通过率 | {pass_rate:.1f}% |

## 发现的问题 ({len(self.issues)} 个)

"""
        for issue in self.issues:
            report_text += f"""
### {issue['id']}: {issue['title']}

- **严重程度**: {issue['severity']}
- **类别**: {issue['category']}
- **影响接口**: {', '.join(issue['affected_apis'])}
- **描述**: {issue['description']}
- **当前状态**: {issue['current_state']}
- **期望状态**: {issue['expected_state']}
- **复现步骤**:
{chr(10).join(['  ' + step for step in issue['reproduction_steps']])}
- **标签**: {', '.join(issue['labels'])}

"""

        report_file = f"{OUTPUT_DIR}/test-report-v6.md"
        with open(report_file, "w", encoding="utf-8") as f:
            f.write(report_text)

        self.log(f"测试报告已保存到：{report_file}", "INFO")

    def run_all_tests(self):
        """运行所有测试"""
        self.log("=========================================", "HEADER")
        self.log("ProjectHub Backend API Test Suite v6", "HEADER")
        self.log("=========================================", "HEADER")

        # 登录
        if not self.login():
            self.log("登录失败，继续测试无需认证的接口", "WARN")

        # 执行各模块测试
        self.test_auth_module()
        self.test_user_module()
        self.test_project_module()
        self.test_task_module()
        self.test_comment_module()
        self.test_epic_module()
        self.test_userstory_module()
        self.test_issue_module()
        self.test_wiki_module()
        self.test_notification_module()

        # 保存结果
        self.save_results()

        # 打印摘要
        self.log("=========================================", "HEADER")
        self.log("测试完成摘要", "HEADER")
        self.log("=========================================", "HEADER")
        self.log(f"总测试数：{self.tests_total}", "INFO")
        self.log(f"通过：{self.tests_passed}", "GREEN")
        self.log(f"失败：{self.tests_failed}", "RED")
        if self.tests_total > 0:
            self.log(f"通过率：{self.tests_passed * 100 / self.tests_total:.1f}%", "INFO")
        self.log(f"发现问题数：{len(self.issues)}", "ISSUE")


if __name__ == "__main__":
    tester = APITester()
    tester.run_all_tests()
