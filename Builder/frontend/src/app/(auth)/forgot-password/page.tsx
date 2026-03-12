'use client';

import { useState } from 'react';
import Link from 'next/link';
import { Form, Input, Button, message, Result } from 'antd';
import { MailOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { api } from '@/lib/api/axios';
import { endpoints } from '@/lib/api/endpoints';

interface ForgotPasswordFormValues {
  email: string;
}

export default function ForgotPasswordPage() {
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const onFinish = async (values: ForgotPasswordFormValues) => {
    setLoading(true);
    try {
      await api.post(endpoints.auth.forgotPassword, { email: values.email });
      message.success('重置链接已发送到您的邮箱，请查收');
      setSuccess(true);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '发送失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-900 p-4">
        <div className="max-w-md w-full">
          <Result
            status="success"
            title="邮件已发送"
            subTitle={
              <div className="text-gray-400">
                <p>重置密码链接已发送到您的邮箱</p>
                <p className="mt-2 text-sm">请检查收件箱，并按照邮件中的指示重置密码</p>
              </div>
            }
            extra={[
              <Button
                key="back"
                type="primary"
                onClick={() => window.location.href = '/login'}
                className="bg-gradient-to-r from-orange-500 to-orange-600 border-none"
              >
                返回登录
              </Button>,
              <Button
                key="resend"
                onClick={() => setSuccess(false)}
                className="border-gray-600 text-gray-300"
              >
                重新发送
              </Button>,
            ]}
          />
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-900 p-4">
      <div className="max-w-md w-full">
        <div className="text-center mb-8">
          <Link href="/login" className="inline-flex items-center text-gray-400 hover:text-white mb-6">
            <ArrowLeftOutlined className="mr-2" />
            返回登录
          </Link>
          <h1 className="text-2xl font-bold text-white mb-2">忘记密码</h1>
          <p className="text-gray-400">
            请输入您的邮箱地址，我们将发送密码重置链接
          </p>
        </div>

        <Form
          name="forgotPassword"
          layout="vertical"
          requiredMark={false}
          onFinish={onFinish}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="email"
            label={
              <span className="text-gray-300 text-sm">邮箱地址</span>
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

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              className="w-full h-12 bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 border-none font-semibold text-base"
            >
              发送重置链接
            </Button>
          </Form.Item>

          <div className="text-center">
            <Link href="/login" className="text-gray-400 hover:text-white text-sm">
              想起密码了？返回登录
            </Link>
          </div>
        </Form>
      </div>
    </div>
  );
}
