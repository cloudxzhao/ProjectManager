'use client';

import { MainLayout } from '@/components/layout';

interface DashboardLayoutProps {
  children: React.ReactNode;
}

export default function DashboardLayout({ children }: DashboardLayoutProps) {
  return (
    <MainLayout>
      {children}
    </MainLayout>
  );
}
