'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Form, Input, Checkbox, Button, message } from 'antd';
import { MailOutlined, LockOutlined, UserOutlined, EyeOutlined, EyeInvisibleOutlined } from '@ant-design/icons';
import { useAuth } from '@/lib/hooks/useAuth';

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
    <Form
      name="login"
      layout="vertical"
      requiredMark={false}
      onFinish={onFinish}
      autoComplete="off"
      size="large"
      className="space-y-6"
    >
      <Form.Item
        name="email"
        label={
          <span className="text-gray-300 text-sm">邮箱</span>
        }
        rules={[
          { required: true, message: '请输入邮箱' },
          { type: 'email', message: '请输入有效的邮箱地址' },
        ]}
      >
        <Input
          prefix={<MailOutlined className="text-gray-400" />}
          placeholder="name@example.com"
          className="bg-gray-800 border-gray-700 text-white placeholder-gray-500 h-12"
          autoComplete="email"
        />
      </Form.Item>

      <Form.Item
        name="password"
        label={
          <span className="text-gray-300 text-sm">密码</span>
        }
        rules={[
          { required: true, message: '请输入密码' },
          { min: 6, message: '密码长度至少为 6 位' },
        ]}
      >
        <Input.Password
          prefix={<LockOutlined className="text-gray-400" />}
          type={passwordVisible ? 'text' : 'password'}
          placeholder="请输入密码"
          className="bg-gray-800 border-gray-700 text-white placeholder-gray-500 h-12"
          autoComplete="current-password"
          iconRender={(visible) =>
            visible ? (
              <EyeOutlined className="text-gray-400" onClick={() => setPasswordVisible(!visible)} />
            ) : (
              <EyeInvisibleOutlined className="text-gray-400" onClick={() => setPasswordVisible(!visible)} />
            )
          }
        />
      </Form.Item>

      <div className="flex items-center justify-between">
        <Form.Item name="remember" valuePropName="checked" className="!mb-0">
          <Checkbox className="text-gray-400">记住我</Checkbox>
        </Form.Item>
        <Link href="/forgot-password" className="text-sm text-orange-500 hover:text-orange-400">
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

      <div className="text-center">
        <span className="text-gray-400">还没有账号？</span>
        <Link href="/register" className="text-orange-500 hover:text-orange-400 font-medium ml-1">
          立即注册
        </Link>
      </div>
    </Form>
  );
}
