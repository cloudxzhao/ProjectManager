import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { User, LoginCredentials, RegisterData, AuthTokens } from '@/types/user';
import { api } from '@/lib/api/axios';
import { endpoints } from '@/lib/api/endpoints';

// 设置 cookie 的辅助函数
const setAuthCookie = (value: string, expiresIn: number) => {
  if (typeof document !== 'undefined') {
    const maxAge = expiresIn || 3600; // 默认 1 小时
    document.cookie = `auth-storage=${encodeURIComponent(value)}; path=/; max-age=${maxAge}; SameSite=Strict`;
  }
};

// 删除 cookie 的辅助函数
const removeAuthCookie = () => {
  if (typeof document !== 'undefined') {
    document.cookie = 'auth-storage=; path=/; max-age=0; SameSite=Strict';
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
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      login: async (credentials: LoginCredentials) => {
        set({ isLoading: true, error: null });

        try {
          const response = await api.post<AuthTokens>(endpoints.auth.login, credentials);

          if (response.code === 200) {
            const { accessToken, refreshToken, expiresIn } = response.data;

            // 存储 token 到 localStorage
            localStorage.setItem('access_token', accessToken);
            localStorage.setItem('refresh_token', refreshToken);

            // 同步到 cookie 供中间件使用
            setAuthCookie(JSON.stringify({ token: accessToken, isAuthenticated: true }), expiresIn);

            // 获取用户信息
            const userProfile = await api.get<User>(endpoints.user.profile);

            set({
              user: userProfile.data,
              token: accessToken,
              isAuthenticated: true,
              isLoading: false,
            });
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
          const response = await api.post<AuthTokens>(endpoints.auth.register, {
            username: data.username,
            email: data.email,
            password: data.password,
          });

          if (response.code === 200) {
            const { accessToken, refreshToken, expiresIn } = response.data;

            localStorage.setItem('access_token', accessToken);
            localStorage.setItem('refresh_token', refreshToken);

            // 同步到 cookie 供中间件使用
            setAuthCookie(JSON.stringify({ token: accessToken, isAuthenticated: true }), expiresIn);

            const userProfile = await api.get<User>(endpoints.user.profile);

            set({
              user: userProfile.data,
              token: accessToken,
              isAuthenticated: true,
              isLoading: false,
            });
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
    }
  )
);
