import Image from 'next/image';
import Link from 'next/link';

export default function Home() {
  return (
    <main className="min-h-screen">
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <h1 className="text-5xl font-bold text-gradient mb-4">
            ProjectHub
          </h1>
          <p className="text-xl text-gray-400 mb-8">
            让团队协作更高效 · 智能 · 可控
          </p>
          <div className="flex gap-4 justify-center">
            <Link
              href="/login"
              className="px-8 py-3 bg-gradient-to-r from-orange-500 to-orange-600 rounded-lg font-semibold hover:shadow-lg hover:shadow-orange-500/25 transition-all hover:-translate-y-0.5"
            >
              登录
            </Link>
            <Link
              href="/dashboard"
              className="px-8 py-3 glass rounded-lg font-semibold hover:border-orange-500/50 transition-all"
            >
              进入系统
            </Link>
          </div>
        </div>
      </div>
    </main>
  );
}
