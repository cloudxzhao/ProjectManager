import type { Metadata } from 'next';
import { Noto_Sans_SC, Outfit } from 'next/font/google';
import './globals.css';
import { AntdRegistry } from '@ant-design/nextjs-registry';
import { AntdProvider } from '@/components/providers/AntdProvider';

// 配置中文字体
const notoSansSC = Noto_Sans_SC({
  subsets: ['latin'],
  weight: ['300', '400', '500', '700'],
  variable: '--font-sans',
  display: 'swap',
});

// 配置标题字体
const outfit = Outfit({
  subsets: ['latin'],
  weight: ['400', '600', '800'],
  variable: '--font-display',
  display: 'swap',
});

export const metadata: Metadata = {
  title: 'ProjectHub - 项目管理系统',
  description: '一站式项目管理解决方案，让团队协作更高效，让项目交付更可控。',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="zh-CN" suppressHydrationWarning>
      <body className={`${notoSansSC.variable} ${outfit.variable} font-sans`}>
        <AntdRegistry>
          <AntdProvider>{children}</AntdProvider>
        </AntdRegistry>
      </body>
    </html>
  );
}