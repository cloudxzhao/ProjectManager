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
  QQOutlined,
  DingtalkOutlined,
} from '@ant-design/icons';
import { useAuth } from '@/lib/hooks/useAuth';
import { BackgroundLayer } from '@/components/common/BackgroundLayer';

interface LoginFormValues {
  email: string;
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
        email: values.email,
        password: values.password,
        remember: values.remember,
      });
      message.success('登录成功');
      router.push('/dashboard');
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '登录失败，请检查邮箱和密码';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <BackgroundLayer showShapes={true}>
      <div className="min-h-screen flex">
        {/* 左侧品牌区域 - 仅大屏显示 */}
        <div className="hidden lg:flex lg:w-1/2 relative items-center justify-center p-12">
          <div className="max-w-md text-white z-10 animate-fade-in-left">
            <h1 className="font-display text-5xl font-extrabold mb-6 leading-tight">
              高效协作<br />
              <span className="text-orange-400">项目驱动</span>
            </h1>
            <p className="text-white/70 text-lg mb-8 leading-relaxed">
              一站式项目管理解决方案，让团队协作更高效，让项目交付更可控。
            </p>
            <div className="space-y-4">
              <div className="flex items-center gap-3">
                <span className="w-6 h-6 rounded-lg bg-orange-500/20 flex items-center justify-center text-orange-400">
                  <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                </span>
                <span>任务看板与进度追踪</span>
              </div>
              <div className="flex items-center gap-3">
                <span className="w-6 h-6 rounded-lg bg-orange-500/20 flex items-center justify-center text-orange-400">
                  <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                </span>
                <span>团队协作与实时沟通</span>
              </div>
              <div className="flex items-center gap-3">
                <span className="w-6 h-6 rounded-lg bg-orange-500/20 flex items-center justify-center text-orange-400">
                  <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                </span>
                <span>数据报表与智能分析</span>
              </div>
            </div>
          </div>
        </div>

        {/* 右侧登录表单区域 */}
        <div className="flex-1 flex items-center justify-center p-4 sm:p-8">
          <div className="w-full max-w-[460px]">
            {/* 登录卡片 */}
            <div className="glass-card p-8 sm:p-10 animate-fade-in-up">
              {/* Logo */}
              <Link href="/" className="flex items-center gap-3 mb-8">
                <div className="w-12 h-12 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl flex items-center justify-center glow-orange">
                  <svg className="w-7 h-7 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
                    <polyline points="9 22 9 12 15 12 15 22" />
                  </svg>
                </div>
                <div className="flex flex-col">
                  <span className="font-display text-xl font-bold text-gray-900">ProjectHub</span>
                  <span className="text-xs text-gray-500">项目管理系统</span>
                </div>
              </Link>

              {/* 欢迎文字 */}
              <div className="mb-8">
                <h2 className="font-display text-2xl font-bold text-gray-900 mb-2">欢迎回来</h2>
                <p className="text-gray-500">请登录您的账户，继续管理您的项目和团队。</p>
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
                  name="email"
                  label={<span className="text-gray-700 text-sm font-medium">邮箱地址</span>}
                  rules={[
                    { required: true, message: '请输入邮箱' },
                    { type: 'email', message: '请输入有效的邮箱地址' },
                  ]}
                >
                  <Input
                    prefix={<MailOutlined className="text-gray-400" />}
                    placeholder="name@example.com"
                    className="h-12"
                    autoComplete="email"
                  />
                </Form.Item>

                <Form.Item
                  name="password"
                  label={<span className="text-gray-700 text-sm font-medium">密码</span>}
                  rules={[
                    { required: true, message: '请输入密码' },
                    { min: 6, message: '密码长度至少为 6 位' },
                  ]}
                >
                  <Input.Password
                    prefix={<LockOutlined className="text-gray-400" />}
                    placeholder="请输入密码"
                    className="h-12"
                    autoComplete="current-password"
                    iconRender={(visible) =>
                      visible ? (
                        <EyeOutlined
                          className="text-gray-400 cursor-pointer hover:text-gray-600"
                          onClick={() => setPasswordVisible(!visible)}
                        />
                      ) : (
                        <EyeInvisibleOutlined
                          className="text-gray-400 cursor-pointer hover:text-gray-600"
                          onClick={() => setPasswordVisible(!visible)}
                        />
                      )
                    }
                  />
                </Form.Item>

                <div className="flex items-center justify-between">
                  <Form.Item name="remember" valuePropName="checked" className="!mb-0">
                    <Checkbox className="text-gray-600">记住我</Checkbox>
                  </Form.Item>
                  <Link href="/forgot-password" className="text-sm text-orange-500 hover:text-orange-600 font-medium">
                    忘记密码？
                  </Link>
                </div>

                <Form.Item>
                  <Button
                    type="primary"
                    htmlType="submit"
                    loading={loading || isLoading}
                    className="w-full h-12 bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 border-none font-semibold text-base glow-orange hover:-translate-y-0.5 transition-all"
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
                <button className="flex items-center justify-center gap-2 p-3 bg-white border border-gray-200 rounded-xl hover:border-orange-500 hover:bg-orange-50 transition-all">
                  <WechatOutlined className="text-xl text-[#07c160]" />
                  <span className="text-sm text-gray-700">微信</span>
                </button>
                <button className="flex items-center justify-center gap-2 p-3 bg-white border border-gray-200 rounded-xl hover:border-orange-500 hover:bg-orange-50 transition-all">
                  <QQOutlined className="text-xl text-[#1296db]" />
                  <span className="text-sm text-gray-700">QQ</span>
                </button>
                <button className="flex items-center justify-center gap-2 p-3 bg-white border border-gray-200 rounded-xl hover:border-orange-500 hover:bg-orange-50 transition-all">
                  <DingtalkOutlined className="text-xl text-[#0089ff]" />
                  <span className="text-sm text-gray-700">钉钉</span>
                </button>
              </div>

              {/* 注册链接 */}
              <div className="text-center mt-8 pt-6 border-t border-gray-100">
                <span className="text-gray-500">还没有账户？</span>
                <Link href="/register" className="text-orange-500 hover:text-orange-600 font-semibold ml-1">
                  立即注册
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </BackgroundLayer>
  );
}
