'use client';

import { AuthLayout } from '@/components/layout';

interface AuthPageLayoutProps {
  children: React.ReactNode;
}

export default function AuthPageLayout({ children }: AuthPageLayoutProps) {
  return (
    <AuthLayout title="登录" subtitle="欢迎回来，请输入您的账号信息">
      {children}
    </AuthLayout>
  );
}
