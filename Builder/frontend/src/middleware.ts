import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

// 需要认证的路由
const protectedRoutes = [
  '/dashboard',
  '/projects',
  '/tasks',
  '/messages',
  '/wiki',
  '/reports',
  '/settings',
];

// 认证相关路由（已登录用户访问这些路由时应重定向到 dashboard）
const authRoutes = ['/login', '/register', '/forgot-password'];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // 从 cookie 获取 auth-storage
  // cookie 中存储的是 JSON 字符串：{ token: string, isAuthenticated: boolean }
  const authCookie = request.cookies.get('auth-storage')?.value;

  // 解析 cookie 并检查是否已登录
  let isAuthenticated = false;
  if (authCookie) {
    try {
      // 注意：cookie 值是经过 encodeURIComponent 编码的，需要先解码
      const decodedCookie = decodeURIComponent(authCookie);
      const authData = JSON.parse(decodedCookie);
      isAuthenticated = !!authData?.token && authData?.isAuthenticated === true;
    } catch {
      // 解析失败，认为未登录
      isAuthenticated = false;
    }
  }

  // 检查是否是需要保护的路由
  const isProtectedRoute = protectedRoutes.some((route) =>
    pathname.startsWith(route)
  );

  // 检查是否是认证路由
  const isAuthRoute = authRoutes.some((route) => pathname.startsWith(route));

  // 未登录用户访问需要保护的路由，重定向到登录页
  if (isProtectedRoute && !isAuthenticated) {
    const loginUrl = new URL('/login', request.url);
    loginUrl.searchParams.set('from', pathname);
    return NextResponse.redirect(loginUrl);
  }

  // 已登录用户访问认证路由，重定向到 dashboard
  if (isAuthRoute && isAuthenticated) {
    return NextResponse.redirect(new URL('/dashboard', request.url));
  }

  return NextResponse.next();
}

// 配置中间件匹配的路由
export const config = {
  matcher: [
    /*
     * 匹配所有请求路径，除了：
     * - _next/static (静态文件)
     * - _next/image (图片优化文件)
     * - favicon.ico (网站图标)
     * - 公共文件 (public 目录)
     */
    '/((?!_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)',
  ],
};
