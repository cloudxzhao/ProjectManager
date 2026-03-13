'use client';

import { useState, useEffect } from 'react';
import { Card, Form, Input, Button, Avatar, message, Tabs, Space, Tag, Spin } from 'antd';
import {
  UserOutlined,
  LockOutlined,
  BellOutlined,
  EyeOutlined,
  InboxOutlined,
  CheckOutlined,
  UploadOutlined,
  SaveOutlined,
} from '@ant-design/icons';
import { useAuth } from '@/lib/hooks/useAuth';
import { getUserProfile, updateProfile, uploadAvatar, changePassword } from '@/lib/api/user';
import type { User } from '@/lib/api/user';

const { Password } = Input;

// Mock 当前用户数据
const mockUser = {
  id: '1',
  username: 'zhangsan',
  email: 'zhangsan@example.com',
  phone: '138****5678',
  avatar: null,
  bio: '热爱编程，追求极致用户体验',
  company: '某某科技有限公司',
  location: '北京',
};

export default function SettingsPage() {
  const { user, updateUser } = useAuth();
  const [loading, setLoading] = useState(false);
  const [fetchingProfile, setFetchingProfile] = useState(true);
  const [avatarFile, setAvatarFile] = useState<any>(null);
  const [previewOpen, setPreviewOpen] = useState(false);
  const [userProfile, setUserProfile] = useState<User | null>(null);

  // 个人资料表单
  const [profileForm] = Form.useForm();

  // 修改密码表单
  const [passwordForm] = Form.useForm();

  // 加载用户资料
  useEffect(() => {
    const loadUserProfile = async () => {
      try {
        setFetchingProfile(true);
        const result = await getUserProfile();
        const profile = result.data;
        setUserProfile(profile);

        // 填充表单
        profileForm.setFieldsValue({
          username: profile.username,
          email: profile.email,
          phone: profile.phone,
          bio: profile.bio,
          company: profile.company,
          location: profile.location,
        });
      } catch (error) {
        console.error('加载用户资料失败:', error);
        message.error('加载用户资料失败');
      } finally {
        setFetchingProfile(false);
      }
    };

    loadUserProfile();
  }, []);

  // 头像上传处理
  const handleAvatarChange = async (info: any) => {
    if (info.file.status === 'uploading') {
      return;
    }
    if (info.file.status === 'done') {
      setAvatarFile(info.file);
      message.success('头像上传成功');
    }
  };

  // 更新个人资料
  const handleProfileUpdate = async (values: Record<string, unknown>) => {
    setLoading(true);
    try {
      await updateProfile({
        username: values.username as string,
        bio: values.bio as string,
        company: values.company as string,
        location: values.location as string,
      });

      // 更新本地用户信息
      if (userProfile) {
        const updatedProfile = { ...userProfile, ...values };
        setUserProfile(updatedProfile);
        updateUser(updatedProfile);
      }

      message.success('个人资料更新成功');
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '更新失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // 修改密码
  const handlePasswordChange = async (values: Record<string, unknown>) => {
    setLoading(true);
    try {
      // 验证两次密码是否一致
      if (values.newPassword !== values.confirmPassword) {
        message.error('两次输入的密码不一致');
        return;
      }
      await changePassword({
        currentPassword: values.currentPassword as string,
        newPassword: values.newPassword as string,
      });
      message.success('密码修改成功，请重新登录');
      passwordForm.resetFields();
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '修改失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // 个人资料 Tab
  const profileTab = {
    key: 'profile',
    label: (
      <span className="flex items-center gap-2">
        <UserOutlined />
        个人资料
      </span>
    ),
    children: fetchingProfile ? (
      <div className="flex justify-center items-center py-20">
        <Spin size="large" />
      </div>
    ) : (
      <div className="max-w-2xl">
        {/* 头像上传区 */}
        <div className="flex items-center gap-6 mb-8 p-6 bg-gray-800/30 rounded-lg">
          <div className="relative">
            <Avatar
              size={100}
              src={avatarFile?.thumbUrl || userProfile?.avatar || user?.avatar}
              icon={<UserOutlined />}
              className="bg-gradient-to-br from-orange-400 to-amber-500"
            />
            <label className="absolute bottom-0 right-0 w-10 h-10 bg-orange-500 rounded-full flex items-center justify-center cursor-pointer hover:bg-orange-600 transition-colors">
              <UploadOutlined className="text-white" />
              <input
                type="file"
                accept="image/*"
                className="hidden"
                onChange={async (e) => {
                  const file = e.target.files?.[0];
                  if (file) {
                    // 先显示预览
                    const reader = new FileReader();
                    reader.onload = (e) => {
                      setAvatarFile({
                        uid: '-1',
                        name: file.name,
                        status: 'done',
                        thumbUrl: e.target?.result as string,
                      });
                    };
                    reader.readAsDataURL(file);

                    // 上传头像
                    try {
                      const formData = new FormData();
                      formData.append('avatar', file);
                      const result = await uploadAvatar(formData);
                      if (result && result.data) {
                        message.success('头像上传成功');
                        // 更新用户信息
                        const updatedProfile = { ...userProfile, avatar: result.data.avatar } as User;
                        setUserProfile(updatedProfile);
                        updateUser(updatedProfile);
                      }
                    } catch (error) {
                      console.error('头像上传失败:', error);
                      message.error('头像上传失败');
                    }
                  }
                }}
              />
            </label>
          </div>
          <div>
            <h3 className="text-lg font-semibold text-white mb-2">头像</h3>
            <p className="text-gray-400 text-sm mb-3">支持 JPG、PNG 格式，大小不超过 2MB</p>
            <div className="flex gap-2">
              <Tag color="blue">推荐尺寸：200x200</Tag>
              <Tag color="green">已优化</Tag>
            </div>
          </div>
        </div>

        {/* 个人资料表单 */}
        <Form
          form={profileForm}
          layout="vertical"
          onFinish={handleProfileUpdate}
          size="large"
        >
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Form.Item
              name="username"
              label={<span className="text-gray-300">用户名</span>}
              rules={[
                { required: true, message: '请输入用户名' },
                { min: 3, message: '用户名至少 3 个字符' },
              ]}
            >
              <Input
                prefix={<UserOutlined className="text-gray-400" />}
                className="bg-gray-700/50 border-gray-600 text-white"
                placeholder="请输入用户名"
              />
            </Form.Item>

            <Form.Item
              name="email"
              label={<span className="text-gray-300">邮箱</span>}
              rules={[
                { required: true, message: '请输入邮箱' },
                { type: 'email', message: '请输入正确的邮箱格式' },
              ]}
            >
              <Input
                prefix={<InboxOutlined className="text-gray-400" />}
                className="bg-gray-700/50 border-gray-600 text-white"
                placeholder="请输入邮箱"
                disabled
              />
            </Form.Item>
          </div>

          <Form.Item
            name="phone"
            label={<span className="text-gray-300">手机号</span>}
          >
            <Input
              className="bg-gray-700/50 border-gray-600 text-white"
              placeholder="请输入手机号"
              disabled
            />
          </Form.Item>

          <Form.Item
            name="bio"
            label={<span className="text-gray-300">个人简介</span>}
          >
            <Input.TextArea
              rows={3}
              className="bg-gray-700/50 border-gray-600 text-white"
              placeholder="介绍一下自己吧"
              showCount
              maxLength={200}
            />
          </Form.Item>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Form.Item
              name="company"
              label={<span className="text-gray-300">公司</span>}
            >
              <Input
                className="bg-gray-700/50 border-gray-600 text-white"
                placeholder="请输入公司名称"
              />
            </Form.Item>

            <Form.Item
              name="location"
              label={<span className="text-gray-300">所在地</span>}
            >
              <Input
                className="bg-gray-700/50 border-gray-600 text-white"
                placeholder="请输入城市"
              />
            </Form.Item>
          </div>

          <Form.Item className="pt-4">
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              icon={<SaveOutlined />}
              className="bg-gradient-to-r from-orange-500 to-orange-600 border-none px-8"
            >
              保存修改
            </Button>
          </Form.Item>
        </Form>
      </div>
    ),
  };

  // 修改密码 Tab
  const passwordTab = {
    key: 'password',
    label: (
      <span className="flex items-center gap-2">
        <LockOutlined />
        修改密码
      </span>
    ),
    children: (
      <div className="max-w-xl">
        <div className="mb-6 p-4 bg-blue-900/20 border border-blue-800/30 rounded-lg">
          <div className="flex items-start gap-3">
            <EyeOutlined className="text-blue-400 text-xl mt-0.5" />
            <div className="text-sm text-blue-300">
              <p className="font-medium mb-1">密码要求</p>
              <ul className="list-disc list-inside space-y-1 text-blue-400/80">
                <li>至少 8 个字符</li>
                <li>包含大小写字母</li>
                <li>包含数字和特殊字符</li>
              </ul>
            </div>
          </div>
        </div>

        <Form
          form={passwordForm}
          layout="vertical"
          onFinish={handlePasswordChange}
          size="large"
        >
          <Form.Item
            name="currentPassword"
            label={<span className="text-gray-300">当前密码</span>}
            rules={[{ required: true, message: '请输入当前密码' }]}
          >
            <Password
              prefix={<LockOutlined className="text-gray-400" />}
              className="bg-gray-700/50 border-gray-600 text-white"
              placeholder="请输入当前密码"
            />
          </Form.Item>

          <Form.Item
            name="newPassword"
            label={<span className="text-gray-300">新密码</span>}
            rules={[
              { required: true, message: '请输入新密码' },
              { min: 8, message: '密码至少 8 个字符' },
            ]}
          >
            <Password
              prefix={<LockOutlined className="text-gray-400" />}
              className="bg-gray-700/50 border-gray-600 text-white"
              placeholder="请输入新密码"
            />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            label={<span className="text-gray-300">确认新密码</span>}
            rules={[
              { required: true, message: '请确认新密码' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('newPassword') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('两次输入的密码不一致'));
                },
              }),
            ]}
            dependencies={['newPassword']}
          >
            <Password
              prefix={<LockOutlined className="text-gray-400" />}
              className="bg-gray-700/50 border-gray-600 text-white"
              placeholder="请再次输入新密码"
            />
          </Form.Item>

          <Form.Item className="pt-4">
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              icon={<CheckOutlined />}
              className="bg-gradient-to-r from-orange-500 to-orange-600 border-none px-8"
            >
              确认修改
            </Button>
          </Form.Item>
        </Form>
      </div>
    ),
  };

  // 通知设置 Tab
  const notificationTab = {
    key: 'notifications',
    label: (
      <span className="flex items-center gap-2">
        <BellOutlined />
        通知设置
      </span>
    ),
    children: (
      <div className="max-w-xl space-y-4">
        <div className="p-4 bg-gray-800/50 border border-gray-700 rounded-lg flex items-center justify-between">
          <div>
            <h4 className="text-white font-medium">任务分配通知</h4>
            <p className="text-gray-400 text-sm mt-1">当有新任务分配给你时</p>
          </div>
          <Form.Item className="mb-0">
            <input
              type="checkbox"
              defaultChecked
              className="w-5 h-5 rounded border-gray-600 bg-gray-700 text-orange-500 focus:ring-orange-500 focus:ring-offset-0"
            />
          </Form.Item>
        </div>

        <div className="p-4 bg-gray-800/50 border border-gray-700 rounded-lg flex items-center justify-between">
          <div>
            <h4 className="text-white font-medium">评论回复通知</h4>
            <p className="text-gray-400 text-sm mt-1">当有人回复你的评论时</p>
          </div>
          <Form.Item className="mb-0">
            <input
              type="checkbox"
              defaultChecked
              className="w-5 h-5 rounded border-gray-600 bg-gray-700 text-orange-500 focus:ring-orange-500 focus:ring-offset-0"
            />
          </Form.Item>
        </div>

        <div className="p-4 bg-gray-800/50 border border-gray-700 rounded-lg flex items-center justify-between">
          <div>
            <h4 className="text-white font-medium">项目更新通知</h4>
            <p className="text-gray-400 text-sm mt-1">当你关注的项目有更新时</p>
          </div>
          <Form.Item className="mb-0">
            <input
              type="checkbox"
              className="w-5 h-5 rounded border-gray-600 bg-gray-700 text-orange-500 focus:ring-orange-500 focus:ring-offset-0"
            />
          </Form.Item>
        </div>

        <div className="p-4 bg-gray-800/50 border border-gray-700 rounded-lg flex items-center justify-between">
          <div>
            <h4 className="text-white font-medium">系统公告</h4>
            <p className="text-gray-400 text-sm mt-1">接收系统更新和公告</p>
          </div>
          <Form.Item className="mb-0">
            <input
              type="checkbox"
              defaultChecked
              className="w-5 h-5 rounded border-gray-600 bg-gray-700 text-orange-500 focus:ring-orange-500 focus:ring-offset-0"
            />
          </Form.Item>
        </div>

        <div className="pt-4">
          <Button
            type="primary"
            icon={<SaveOutlined />}
            className="bg-gradient-to-r from-orange-500 to-orange-600 border-none"
          >
            保存设置
          </Button>
        </div>
      </div>
    ),
  };

  const tabItems = [profileTab, passwordTab, notificationTab];

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      {/* 页面标题 */}
      <div>
        <h1 className="text-2xl font-bold text-white">个人设置</h1>
        <p className="text-gray-400 mt-1">管理你的个人资料和账户设置</p>
      </div>

      {/* 设置卡片 */}
      <Card className="bg-gray-800/50 border-gray-700">
        <Tabs
          defaultActiveKey="profile"
          items={tabItems}
          tabBarStyle={{ borderBottomColor: '#374151' }}
        />
      </Card>
    </div>
  );
}
