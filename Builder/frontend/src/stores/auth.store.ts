import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { User, LoginCredentials, RegisterData, AuthTokens } from '@/types/user';
import { api } from '@/lib/api/axios';
import { endpoints } from '@/lib/api/endpoints';
import type { Result } from '@/types/api';

// 设置 cookie 的辅助函数
const setAuthCookie = (token: string, expiresIn: number) => {
  if (typeof document !== 'undefined') {
    // expiresIn 是毫秒，需要转换为秒
    const maxAge = expiresIn ? Math.floor(expiresIn / 1000) : 7200; // 默认 2 小时
    // 使用 SameSite=Lax 和 Secure=false，允许在重定向时发送 cookie
    // 注意：不要设置 Secure 属性，因为开发环境是 HTTP
    // Next.js middleware 会自动解码 cookie，所以不需要 encodeURIComponent
    const cookieValue = JSON.stringify({ token, isAuthenticated: true });
    // 不设置 domain，默认使用当前域名（包括 IP 地址访问）
    document.cookie = `auth-storage=${cookieValue}; path=/; max-age=${maxAge}; SameSite=Lax`;
    console.log('[Auth Cookie] Set cookie:', {
      maxAge,
      value: cookieValue,
      hostname: window.location.hostname
    });
  }
};

// 删除 cookie 的辅助函数
const removeAuthCookie = () => {
  if (typeof document !== 'undefined') {
    document.cookie = 'auth-storage=; path=/; max-age=-1; SameSite=Lax';
  }
};

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  login: (credentials: LoginCredentials) => Promise<void>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => void;
  updateUser: (user: Partial<User>) => void;
  clearError: () => void;
  rehydrate: () => void; // 手动重新水合
}

// 手动从 localStorage 恢复状态的函数
const getStoredToken = () => {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('access_token');
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      // 手动重新水合 - 从 localStorage 恢复 token
      rehydrate: () => {
        const token = getStoredToken();
        if (token) {
          set({
            token,
            isAuthenticated: true,
          });
        }
      },

      login: async (credentials: LoginCredentials) => {
        set({ isLoading: true, error: null });

        try {
          // api.post 返回 Result<AuthTokens>，需要访问 .data 获取实际数据
          const result = await api.post<AuthTokens>(endpoints.auth.login, credentials);

          console.log('Login token data:', result);

          // 检查是否有 accessToken 来判断登录是否成功
          if (result && result.data && result.data.accessToken) {
            const { accessToken, refreshToken, expiresIn } = result.data;

            console.log('Token:', accessToken);
            console.log('ExpiresIn:', expiresIn);

            // 1. 先存储 token 到 localStorage (供 Axios 拦截器使用)
            localStorage.setItem('access_token', accessToken);
            localStorage.setItem('refresh_token', refreshToken);

            // 2. 同步到 cookie 供中间件使用
            setAuthCookie(accessToken, expiresIn);

            // 3. 获取用户信息（可选，失败不影响登录）
            let userProfile: User | undefined;
            try {
              userProfile = await api.get<User>(endpoints.user.profile).then(res => res.data);
            } catch (profileError) {
              console.warn('获取用户信息失败，但登录已完成:', profileError);
            }

            // 4. 更新 Zustand store - 这会触发 persist 中间件保存到 localStorage
            set({
              user: userProfile ?? null,
              token: accessToken,
              isAuthenticated: true,
              isLoading: false,
            });

            console.log('Auth state after login:', get());

            // 5. 手动同步到 localStorage 的 auth-storage（双重保障）
            if (typeof window !== 'undefined') {
              const state = get();
              const persistedState = {
                state: {
                  token: state.token,
                  isAuthenticated: state.isAuthenticated,
                  user: state.user,
                },
                version: 0,
              };
              localStorage.setItem('auth-storage', JSON.stringify(persistedState));
              console.log('Manually synced auth-storage to localStorage');
            }
          }
        } catch (error: unknown) {
          const message = error instanceof Error ? error.message : '登录失败';
          set({ error: message, isLoading: false });
          throw error;
        }
      },

      register: async (data: RegisterData) => {
        set({ isLoading: true, error: null });

        try {
          // api.post 返回 Result<AuthTokens>，需要访问 .data 获取实际数据
          const result = await api.post<AuthTokens>(endpoints.auth.register, {
            username: data.username,
            email: data.email,
            password: data.password,
          });

          // 检查是否有 accessToken 来判断注册是否成功
          if (result && result.data && result.data.accessToken) {
            const { accessToken, refreshToken, expiresIn } = result.data;

            // 1. 先存储 token 到 localStorage
            localStorage.setItem('access_token', accessToken);
            localStorage.setItem('refresh_token', refreshToken);

            // 2. 同步到 cookie 供中间件使用
            setAuthCookie(accessToken, expiresIn);

            const userProfile = await api.get<User>(endpoints.user.profile).then(res => res.data);

            // 3. 更新 Zustand store
            set({
              user: userProfile,
              token: accessToken,
              isAuthenticated: true,
              isLoading: false,
            });

            console.log('Auth state after register:', get());

            // 4. 手动同步到 localStorage 的 auth-storage（双重保障）
            if (typeof window !== 'undefined') {
              const state = get();
              const persistedState = {
                state: {
                  token: state.token,
                  isAuthenticated: state.isAuthenticated,
                  user: state.user,
                },
                version: 0,
              };
              localStorage.setItem('auth-storage', JSON.stringify(persistedState));
            }
          }
        } catch (error: unknown) {
          const message = error instanceof Error ? error.message : '注册失败';
          set({ error: message, isLoading: false });
          throw error;
        }
      },

      logout: () => {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('auth-storage'); // 清除 Zustand persist 存储
        removeAuthCookie(); // 删除 cookie
        set({
          user: null,
          token: null,
          isAuthenticated: false,
          error: null,
        });
      },

      updateUser: (user: Partial<User>) => {
        const currentUser = get().user;
        if (currentUser) {
          set({ user: { ...currentUser, ...user } });
        }
      },

      clearError: () => {
        set({ error: null });
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        token: state.token,
        isAuthenticated: state.isAuthenticated,
        user: state.user,
      }),
      onRehydrateStorage: () => {
        console.log('=== onRehydrateStorage called ===');
        return (state, error) => {
          if (error) {
            console.error('Failed to rehydrate auth state:', error);
          } else {
            console.log('Rehydrated auth state:', state);
            console.log('Token from storage:', state?.token);
            console.log('Is authenticated from storage:', state?.isAuthenticated);
          }
        };
      },
    }
  )
);
