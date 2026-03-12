'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Form, Input, Checkbox, Button, message } from 'antd';
import { MailOutlined, LockOutlined, UserOutlined, EyeOutlined, EyeInvisibleOutlined } from '@ant-design/icons';
import { useAuth } from '@/lib/hooks/useAuth';

interface RegisterFormValues {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  agree?: boolean;
}

export default function RegisterPage() {
  const router = useRouter();
  const { register, isLoading } = useAuth();
  const [loading, setLoading] = useState(false);
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [confirmPasswordVisible, setConfirmPasswordVisible] = useState(false);

  const onFinish = async (values: RegisterFormValues) => {
    if (values.password !== values.confirmPassword) {
      message.error('两次输入的密码不一致');
      return;
    }

    setLoading(true);
    try {
      await register({
        username: values.username,
        email: values.email,
        password: values.password,
        confirmPassword: values.confirmPassword,
      });
      message.success('注册成功');
      router.push('/dashboard');
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '注册失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // 密码强度检查
  const validatePassword = (_: unknown, value: string) => {
    if (!value) {
      return Promise.reject('请输入密码');
    }
    if (value.length < 6) {
      return Promise.reject('密码长度至少为 6 位');
    }
    if (!/[A-Za-z]/.test(value) || !/[0-9]/.test(value)) {
      return Promise.reject('密码必须包含字母和数字');
    }
    return Promise.resolve();
  };

  return (
    <Form
      name="register"
      layout="vertical"
      requiredMark={false}
      onFinish={onFinish}
      autoComplete="off"
      size="large"
      className="space-y-4"
    >
      <Form.Item
        name="username"
        label={
          <span className="text-gray-300 text-sm">用户名</span>
        }
        rules={[
          { required: true, message: '请输入用户名' },
          { min: 2, message: '用户名长度至少为 2 位' },
          { max: 20, message: '用户名长度不能超过 20 位' },
        ]}
      >
        <Input
          prefix={<UserOutlined className="text-gray-400" />}
          placeholder="请输入用户名"
          className="dark-input"
          autoComplete="username"
        />
      </Form.Item>

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
          className="dark-input"
          autoComplete="email"
        />
      </Form.Item>

      <Form.Item
        name="password"
        label={
          <span className="text-gray-300 text-sm">密码</span>
        }
        rules={[{ validator: validatePassword }]}
        extra="密码需包含字母和数字，长度至少 6 位"
      >
        <Input.Password
          prefix={<LockOutlined className="text-gray-400" />}
          placeholder="请输入密码"
          className="dark-input"
          autoComplete="new-password"
          iconRender={(visible) =>
            visible ? (
              <EyeOutlined
                className="text-gray-400 cursor-pointer"
                onClick={() => setPasswordVisible(!passwordVisible)}
              />
            ) : (
              <EyeInvisibleOutlined
                className="text-gray-400 cursor-pointer"
                onClick={() => setPasswordVisible(!passwordVisible)}
              />
            )
          }
        />
      </Form.Item>

      <Form.Item
        name="confirmPassword"
        label={
          <span className="text-gray-300 text-sm">确认密码</span>
        }
        dependencies={['password']}
        rules={[
          { required: true, message: '请再次输入密码' },
          ({ getFieldValue }) => ({
            validator(_: unknown, value: string) {
              if (!value || getFieldValue('password') === value) {
                return Promise.resolve();
              }
              return Promise.reject(new Error('两次输入的密码不一致'));
            },
          }),
        ]}
      >
        <Input.Password
          prefix={<LockOutlined className="text-gray-400" />}
          placeholder="请再次输入密码"
          className="dark-input"
          autoComplete="new-password"
          iconRender={(visible) =>
            visible ? (
              <EyeOutlined
                className="text-gray-400 cursor-pointer"
                onClick={() => setConfirmPasswordVisible(!confirmPasswordVisible)}
              />
            ) : (
              <EyeInvisibleOutlined
                className="text-gray-400 cursor-pointer"
                onClick={() => setConfirmPasswordVisible(!confirmPasswordVisible)}
              />
            )
          }
        />
      </Form.Item>

      <Form.Item
        name="agree"
        valuePropName="checked"
        rules={[{ validator: (_, value) => (value ? Promise.resolve() : Promise.reject('请同意用户协议')) }]}
      >
        <Checkbox className="text-gray-400">
          我已阅读并同意{' '}
          <Link href="/terms" target="_blank" className="text-orange-500 hover:text-orange-400">
            用户协议
          </Link>{' '}
          和{' '}
          <Link href="/privacy" target="_blank" className="text-orange-500 hover:text-orange-400">
            隐私政策
          </Link>
        </Checkbox>
      </Form.Item>

      <Form.Item>
        <Button
          type="primary"
          htmlType="submit"
          loading={loading || isLoading}
          className="w-full h-12 bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 border-none font-semibold text-base"
        >
          注册
        </Button>
      </Form.Item>

      <div className="text-center">
        <span className="text-gray-400">已有账号？</span>
        <Link href="/login" className="text-orange-500 hover:text-orange-400 font-medium ml-1">
          立即登录
        </Link>
      </div>
    </Form>
  );
}
