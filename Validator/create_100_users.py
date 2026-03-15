#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量创建 100 个用户脚本
用于 ProjectHub 系统测试数据生成
"""

import requests
import json
from datetime import datetime
from typing import List, Dict, Any

# 配置
API_BASE_URL = "http://localhost:9527/api/v1"
ADMIN_USERNAME = "admin"
ADMIN_PASSWORD = "Admin123"

# 用户数据模板
FIRST_NAMES = [
    "James", "Mary", "John", "Patricia", "Robert", "Jennifer", "Michael", "Linda",
    "William", "Elizabeth", "David", "Barbara", "Richard", "Susan", "Joseph", "Jessica",
    "Thomas", "Sarah", "Charles", "Karen", "Christopher", "Nancy", "Daniel", "Lisa",
    "Matthew", "Betty", "Anthony", "Margaret", "Mark", "Sandra", "Donald", "Ashley",
    "Steven", "Kimberly", "Paul", "Emily", "Andrew", "Donna", "Joshua", "Michelle",
    "Kenneth", "Dorothy", "Kevin", "Carol", "Brian", "Amanda", "George", "Melissa",
    "Edward", "Deborah", "Ronald", "Stephanie", "Timothy", "Rebecca", "Jason", "Sharon",
    "Jeffrey", "Laura", "Ryan", "Cynthia", "Jacob", "Kathleen", "Gary", "Amy",
    "Nicholas", "Angela", "Eric", "Shirley", "Jonathan", "Anna", "Stephen", "Brenda"
]

LAST_NAMES = [
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
    "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
    "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
    "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker",
    "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
    "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell",
    "Carter", "Roberts", "Gomez", "Phillips", "Evans", "Turner", "Diaz", "Parker",
    "Cruz", "Edwards", "Collins", "Reyes", "Stewart", "Morris", "Morales", "Murphy",
    "Cook", "Rogers", "Gutierrez", "Ortiz", "Morgan", "Cooper", "Peterson", "Bailey"
]


class UserCreator:
    def __init__(self):
        self.session = requests.Session()
        self.access_token = ""
        self.successful_users = []
        self.failed_users = []
        self.skipped_users = []

    def log(self, message: str, level: str = "INFO"):
        """记录日志"""
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        print(f"[{timestamp}] [{level}] {message}")

    def login(self) -> bool:
        """使用管理员账号登录获取 Token"""
        self.log("正在登录...")

        login_url = f"{API_BASE_URL}/auth/login"
        payload = {
            "usernameOrEmail": ADMIN_USERNAME,
            "password": ADMIN_PASSWORD
        }

        try:
            response = self.session.post(login_url, json=payload)
            if response.status_code == 200:
                data = response.json()
                if data.get("success") or "accessToken" in str(data):
                    # 兼容不同的响应格式
                    if "data" in data and "accessToken" in data["data"]:
                        self.access_token = data["data"]["accessToken"]
                    elif "accessToken" in data:
                        self.access_token = data["accessToken"]

                    self.log(f"登录成功！Token: {self.access_token[:20]}...")
                    return True
                else:
                    self.log(f"登录失败：{response.text}", "ERROR")
                    return False
            else:
                self.log(f"登录请求失败，状态码：{response.status_code}", "ERROR")
                return False
        except Exception as e:
            self.log(f"登录异常：{e}", "ERROR")
            return False

    def create_user(self, username: str, email: str, password: str) -> Dict[str, Any]:
        """创建单个用户"""
        url = f"{API_BASE_URL}/auth/register"
        payload = {
            "username": username,
            "email": email,
            "password": password
        }

        headers = {"Authorization": f"Bearer {self.access_token}"} if self.access_token else {}

        try:
            response = self.session.post(url, json=payload, headers=headers)
            result = {
                "username": username,
                "email": email,
                "status_code": response.status_code,
                "response": response.json() if response.text else None,
                "success": response.status_code == 200
            }

            if response.status_code == 200:
                self.successful_users.append(result)
                return {"success": True, "data": result}
            elif response.status_code == 400:
                # 用户已存在
                self.skipped_users.append(result)
                return {"success": False, "reason": "exists", "data": result}
            else:
                self.failed_users.append(result)
                return {"success": False, "reason": "error", "data": result}

        except Exception as e:
            self.log(f"创建用户 {username} 异常：{e}", "ERROR")
            result = {
                "username": username,
                "email": email,
                "error": str(e),
                "success": False
            }
            self.failed_users.append(result)
            return {"success": False, "reason": "exception", "data": result}

    def generate_users(self, count: int = 100) -> List[Dict[str, str]]:
        """生成用户数据"""
        users = []
        for i in range(count):
            first_name = FIRST_NAMES[i % len(FIRST_NAMES)]
            last_name = LAST_NAMES[i % len(LAST_NAMES)]

            username = f"user_{i + 1:03d}"  # user_001, user_002, ...
            email = f"user_{i + 1:03d}@projecthub.com"
            password = "User123!"  # 统一密码

            # 生成昵称
            nickname = f"{first_name} {last_name}"

            users.append({
                "username": username,
                "email": email,
                "password": password,
                "nickname": nickname
            })

        return users

    def run(self, count: int = 100):
        """执行批量创建"""
        self.log("=" * 60)
        self.log(f"开始批量创建 {count} 个用户")
        self.log(f"API 地址：{API_BASE_URL}")
        self.log("=" * 60)

        # 1. 登录获取 token
        if not self.login():
            self.log("无法登录，请检查凭证或服务器状态", "ERROR")
            return False

        # 2. 生成用户数据
        users = self.generate_users(count)
        self.log(f"已生成 {len(users)} 个用户数据")

        # 3. 批量创建用户
        self.log("开始创建用户...")
        start_time = datetime.now()

        for i, user in enumerate(users, 1):
            result = self.create_user(
                username=user["username"],
                email=user["email"],
                password=user["password"]
            )

            if result["success"]:
                self.log(f"[{i}/{count}] 创建成功：{user['username']} ({user['nickname']})", "SUCCESS")
            elif result.get("reason") == "exists":
                self.log(f"[{i}/{count}] 用户已存在：{user['username']}", "WARN")
            else:
                self.log(f"[{i}/{count}] 创建失败：{user['username']}", "ERROR")

        end_time = datetime.now()
        duration = (end_time - start_time).total_seconds()

        # 4. 输出统计
        self.log("")
        self.log("=" * 60)
        self.log("批量创建完成！")
        self.log(f"总耗时：{duration:.2f} 秒")
        self.log(f"成功：{len(self.successful_users)} 个")
        self.log(f"跳过（已存在）: {len(self.skipped_users)} 个")
        self.log(f"失败：{len(self.failed_users)} 个")
        self.log("=" * 60)

        # 5. 保存结果
        self.save_report()

        return True

    def save_report(self):
        """保存执行报告"""
        report = {
            "timestamp": datetime.now().isoformat(),
            "summary": {
                "total": len(self.successful_users) + len(self.skipped_users) + len(self.failed_users),
                "successful": len(self.successful_users),
                "skipped": len(self.skipped_users),
                "failed": len(self.failed_users)
            },
            "successful_users": self.successful_users,
            "skipped_users": self.skipped_users,
            "failed_users": self.failed_users
        }

        report_file = "user_creation_report.json"
        with open(report_file, "w", encoding="utf-8") as f:
            json.dump(report, f, indent=2, ensure_ascii=False)

        self.log(f"报告已保存至：{report_file}")


if __name__ == "__main__":
    creator = UserCreator()
    creator.run(count=100)
