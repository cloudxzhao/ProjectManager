'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Form, Input, Button, DatePicker, Select, ColorPicker, message, Card } from 'antd';
import { ArrowLeftOutlined, ProjectOutlined } from '@ant-design/icons';
import { useAuth } from '@/lib/hooks/useAuth';
import { api } from '@/lib/api/axios';
import { endpoints } from '@/lib/api/endpoints';
import Link from 'next/link';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;

interface ProjectFormValues {
  name: string;
  description: string;
  startDate?: dayjs.Dayjs;
  endDate?: dayjs.Dayjs;
  color?: string;
  icon?: string;
}

const projectIcons = ['🛒', '📱', '📊', '🤝', '🌐', '🔧', '💼', '🎯', '🚀', '💡'];

export default function NewProjectPage() {
  const router = useRouter();
  const { isAuthenticated, token } = useAuth();
  const [loading, setLoading] = useState(false);
  const [selectedIcon, setSelectedIcon] = useState('🛒');
  const [selectedColor, setSelectedColor] = useState('#f97316');

  // 检查是否已登录，未登录则重定向到登录页
  useEffect(() => {
    if (!isAuthenticated) {
      // 检查 localStorage 中是否有 token（双重检查）
      const storedToken = typeof window !== 'undefined' ? localStorage.getItem('access_token') : null;
      if (!storedToken) {
        // 未登录，重定向到登录页
        window.location.href = `/login?from=${encodeURIComponent('/projects/new')}`;
        return;
      }
    }
  }, [isAuthenticated]);

  const onFinish = async (values: ProjectFormValues) => {
    setLoading(true);
    try {
      console.log('Form values:', values, 'selectedColor:', selectedColor, 'selectedIcon:', selectedIcon);
      const payload = {
        name: values.name,
        description: values.description,
        startDate: values.startDate?.format('YYYY-MM-DD'),
        endDate: values.endDate?.format('YYYY-MM-DD'),
        color: selectedColor,
        icon: selectedIcon,
      };
      console.log('Submitting payload:', payload);
      const response = await api.post(endpoints.project.create, payload);

      if (response.code === 200) {
        message.success('项目创建成功');
        const data = response.data as { id: string };
        router.push(`/projects/${data.id}`);
      }
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '创建失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      {/* 页面标题 */}
      <div className="flex items-center gap-4">
        <Link href="/projects">
          <Button type="text" icon={<ArrowLeftOutlined />} className="text-gray-400 hover:text-white">
            返回
          </Button>
        </Link>
        <div>
          <h1 className="text-2xl font-bold text-white">创建新项目</h1>
          <p className="text-gray-400 mt-1">填写以下信息创建新项目</p>
        </div>
      </div>

      {/* 创建表单 */}
      <Card className="bg-gray-800/50 border-gray-700">
        <Form
          layout="vertical"
          requiredMark={false}
          onFinish={onFinish}
          size="large"
          initialValues={{
            color: '#f97316',
            icon: '🛒',
          }}
        >
          {/* 项目名称 */}
          <Form.Item
            name="name"
            label={<span className="text-gray-300">项目名称</span>}
            rules={[
              { required: true, message: '请输入项目名称' },
              { min: 2, message: '项目名称至少 2 个字符' },
              { max: 50, message: '项目名称不能超过 50 个字符' },
            ]}
          >
            <Input
              placeholder="例如：电商平台重构"
              className="bg-gray-700/50 border-gray-600 text-white placeholder-gray-500"
              prefix={<ProjectOutlined className="text-gray-400" />}
            />
          </Form.Item>

          {/* 项目描述 */}
          <Form.Item
            name="description"
            label={<span className="text-gray-300">项目描述</span>}
            rules={[{ required: true, message: '请输入项目描述' }]}
          >
            <TextArea
              rows={4}
              placeholder="描述项目目标、范围等..."
              className="bg-gray-700/50 border-gray-600 text-white placeholder-gray-500"
              showCount
              maxLength={500}
            />
          </Form.Item>

          {/* 项目周期 */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Form.Item
              name="startDate"
              label={<span className="text-gray-300">开始日期</span>}
            >
              <DatePicker
                className="w-full bg-gray-700/50 border-gray-600 text-white"
                format="YYYY-MM-DD"
                placeholder="选择开始日期"
              />
            </Form.Item>

            <Form.Item
              name="endDate"
              label={<span className="text-gray-300">结束日期</span>}
            >
              <DatePicker
                className="w-full bg-gray-700/50 border-gray-600 text-white"
                format="YYYY-MM-DD"
                placeholder="选择结束日期"
              />
            </Form.Item>
          </div>

          {/* 项目图标选择 */}
          <Form.Item
            name="icon"
            label={<span className="text-gray-300">项目图标</span>}
          >
            <div className="flex gap-2 flex-wrap">
              {projectIcons.map((icon) => (
                <button
                  key={icon}
                  type="button"
                  onClick={() => setSelectedIcon(icon)}
                  className={`w-12 h-12 text-2xl rounded-lg flex items-center justify-center transition-all ${
                    selectedIcon === icon
                      ? 'bg-orange-500 ring-2 ring-orange-400'
                      : 'bg-gray-700/50 hover:bg-gray-600'
                  }`}
                >
                  {icon}
                </button>
              ))}
            </div>
          </Form.Item>

          {/* 项目颜色选择 */}
          <Form.Item
            name="color"
            label={<span className="text-gray-300">项目颜色</span>}
          >
            <ColorPicker
              format="hex"
              showText
              className="w-full"
              defaultValue="#f97316"
              onChange={(color) => {
                const hexColor = color.toHexString();
                console.log('Color changed to:', hexColor);
                setSelectedColor(hexColor);
              }}
            />
          </Form.Item>

          {/* 提交按钮 */}
          <Form.Item>
            <div className="flex gap-4">
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                className="bg-gradient-to-r from-orange-500 to-orange-600 border-none px-8"
              >
                创建项目
              </Button>
              <Button
                type="default"
                onClick={() => router.back()}
                className="border-gray-600 text-gray-300"
              >
                取消
              </Button>
            </div>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
