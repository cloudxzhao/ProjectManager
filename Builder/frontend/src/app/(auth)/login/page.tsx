'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Form, Input, Checkbox, Button, message, Divider } from 'antd';
import {
  MailOutlined,
  LockOutlined,
  EyeOutlined,
  EyeInvisibleOutlined,
  WechatOutlined,
  QqOutlined,
  DingtalkOutlined,
} from '@ant-design/icons';
import { useAuth } from '@/lib/hooks/useAuth';

interface LoginFormValues {
  username: string;
  password: string;
  remember?: boolean;
}

export default function LoginPage() {
  const router = useRouter();
  const { login, isLoading } = useAuth();
  const [loading, setLoading] = useState(false);
  const [passwordVisible, setPasswordVisible] = useState(false);

  const onFinish = async (values: LoginFormValues) => {
    setLoading(true);
    try {
      await login({
        usernameOrEmail: values.username,
        password: values.password,
        remember: values.remember,
      });
      message.success('登录成功');
      // 使用 window.location.href 替代 router.push
      // 因为 router.push 是客户端导航，不会触发 middleware
      // 而 window.location.href 会触发完整页面加载，middleware 会正确读取 cookie
      window.location.href = '/dashboard';
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '登录失败，请检查用户名和密码';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full">
      {/* Logo */}
      <Link href="/" className="flex items-center gap-3 mb-8">
        <div className="w-12 h-12 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl flex items-center justify-center">
          <svg className="w-7 h-7 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            <polyline points="9 22 9 12 15 12 15 22" />
          </svg>
        </div>
        <div className="flex flex-col">
          <span className="font-display text-xl font-bold text-white">ProjectHub</span>
          <span className="text-xs text-gray-400">项目管理系统</span>
        </div>
      </Link>

      {/* 登录卡片 */}
      <div className="glass-card p-8 sm:p-10 animate-fade-in-up">
        {/* 欢迎文字 */}
        <div className="mb-8">
          <h2 className="font-display text-2xl font-bold text-white mb-2">欢迎回来</h2>
          <p className="text-gray-400">请登录您的账户，继续管理您的项目和团队。</p>
        </div>

        {/* 登录表单 */}
        <Form
          name="login"
          layout="vertical"
          requiredMark={false}
          onFinish={onFinish}
          autoComplete="off"
          size="large"
          className="space-y-5"
        >
          <Form.Item
            name="username"
            label={<span className="text-gray-300 text-sm font-medium">用户名/邮箱</span>}
            rules={[
              { required: true, message: '请输入用户名或邮箱' },
            ]}
          >
            <Input
              prefix={<MailOutlined className="text-gray-400" />}
              placeholder="请输入用户名或邮箱"
              className="h-12 bg-white/10 border-white/10 text-white placeholder:text-gray-400"
              autoComplete="username"
            />
          </Form.Item>

          <Form.Item
            name="password"
            label={<span className="text-gray-300 text-sm font-medium">密码</span>}
            rules={[
              { required: true, message: '请输入密码' },
              { min: 6, message: '密码长度至少为 6 位' },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined className="text-gray-400" />}
              placeholder="请输入密码"
              className="h-12 bg-white/10 border-white/10 text-white placeholder:text-gray-400"
              autoComplete="current-password"
              iconRender={(visible) =>
                visible ? (
                  <EyeOutlined
                    className="text-gray-400 text-gray-300"
                    onClick={() => setPasswordVisible(!visible)}
                  />
                ) : (
                  <EyeInvisibleOutlined
                    className="text-gray-400 text-gray-300"
                    onClick={() => setPasswordVisible(!visible)}
                  />
                )
              }
            />
          </Form.Item>

          <div className="flex items-center justify-between">
            <Form.Item name="remember" valuePropName="checked" className="!mb-0">
              <Checkbox className="text-gray-300">记住我</Checkbox>
            </Form.Item>
            <Link href="/forgot-password" className="text-sm text-orange-400 font-medium">
              忘记密码？
            </Link>
          </div>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading || isLoading}
              className="w-full h-12 bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 border-none font-semibold text-base"
            >
              登录
            </Button>
          </Form.Item>
        </Form>

        {/* 分隔线 */}
        <Divider className="!my-6">
          <span className="text-gray-400 text-sm">或使用以下方式登录</span>
        </Divider>

        {/* 社交登录 */}
        <div className="grid grid-cols-3 gap-3">
          <button className="flex items-center justify-center gap-2 p-3 bg-white/10 border border-white/20 rounded-xl">
            <WechatOutlined className="text-xl text-[#07c160]" />
            <span className="text-sm text-gray-300">微信</span>
          </button>
          <button className="flex items-center justify-center gap-2 p-3 bg-white/10 border border-white/20 rounded-xl">
            <QqOutlined className="text-xl text-[#1296db]" />
            <span className="text-sm text-gray-300">QQ</span>
          </button>
          <button className="flex items-center justify-center gap-2 p-3 bg-white/10 border border-white/20 rounded-xl">
            <DingtalkOutlined className="text-xl text-[#0089ff]" />
            <span className="text-sm text-gray-300">钉钉</span>
          </button>
        </div>

        {/* 注册链接 */}
        <div className="text-center mt-8 pt-6 border-t border-white/10">
          <span className="text-gray-400">还没有账户？</span>
          <Link href="/register" className="text-orange-400 font-semibold ml-1">
            立即注册
          </Link>
        </div>
      </div>
    </div>
  );
}
