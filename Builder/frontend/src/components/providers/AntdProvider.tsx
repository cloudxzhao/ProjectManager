'use client';

import { createContext, useContext, useMemo, useEffect } from 'react';
import { ConfigProvider, theme, message } from 'antd';

interface AntdConfigContextValue {
  token: Record<string, unknown>;
  components: Record<string, unknown>;
}

const AntdConfigContext = createContext<AntdConfigContextValue>({
  token: {},
  components: {},
});

export function useAntdConfig() {
  return useContext(AntdConfigContext);
}

// 全局 API 错误监听组件
function ApiErrorListener() {
  const [messageApi, contextHolder] = message.useMessage();

  useEffect(() => {
    const handleErrorEvent = (event: CustomEvent<{ code: number; message: string }>) => {
      const { code, message: errorMsg } = event.detail;

      // 根据错误码显示不同的友好提示
      const errorMessages: Record<number, string> = {
        400: errorMsg || '请求参数错误，请检查后重试',
        403: errorMsg || '权限不足，请联系管理员',
        404: errorMsg || '请求的资源不存在',
        500: errorMsg || '服务器开小差了，请稍后重试',
      };

      const friendlyMessage = errorMessages[code] || `请求失败 (${code})`;

      messageApi.error({
        content: friendlyMessage,
        duration: 3,
      });
    };

    window.addEventListener('api-error', handleErrorEvent as EventListener);

    return () => {
      window.removeEventListener('api-error', handleErrorEvent as EventListener);
    };
  }, [messageApi]);

  return contextHolder;
}

interface AntdProviderProps {
  children: React.ReactNode;
}

export function AntdProvider({ children }: AntdProviderProps) {
  const config = useMemo(
    () => ({
      algorithm: theme.darkAlgorithm,
      token: {
        colorPrimary: '#f97316',
        colorBgContainer: '#1e293b',
        colorBgElevated: '#1e293b',
        colorBgLayout: '#0f172a',
        colorText: '#f8fafc',
        colorTextSecondary: '#94a3b8',
        colorBorder: 'rgba(255, 255, 255, 0.08)',
        colorBorderSecondary: 'rgba(255, 255, 255, 0.08)',
        borderRadius: 10,
        fontFamily: 'var(--font-sans)',
      },
      components: {
        Input: {
          colorBgContainer: 'rgba(255, 255, 255, 0.05)',
          colorBgHover: 'rgba(255, 255, 255, 0.1)',
          colorText: '#f8fafc',
          colorTextPlaceholder: '#64748b',
          activeBorderColor: '#f97316',
          hoverBorderColor: '#f97316',
        },
        Select: {
          colorBgContainer: 'rgba(255, 255, 255, 0.05)',
          colorBgElevated: '#1e293b',
          colorBgHover: 'rgba(255, 255, 255, 0.1)',
          colorText: '#f8fafc',
          colorTextPlaceholder: '#64748b',
          optionSelectedBg: 'rgba(249, 115, 22, 0.15)',
        },
        DatePicker: {
          colorBgContainer: 'rgba(255, 255, 255, 0.05)',
          colorBgHover: 'rgba(255, 255, 255, 0.1)',
          colorBgElevated: '#1e293b',
          colorText: '#f8fafc',
          colorTextPlaceholder: '#64748b',
        },
        Card: {
          colorBgContainer: 'rgba(255, 255, 255, 0.03)',
          colorBorderSecondary: 'rgba(255, 255, 255, 0.08)',
        },
        Table: {
          colorBgContainer: 'transparent',
          colorText: '#f8fafc',
          colorTextHeading: '#94a3b8',
          headerBg: 'rgba(255, 255, 255, 0.03)',
          rowHoverBg: 'rgba(255, 255, 255, 0.05)',
        },
        Button: {
          colorBgContainer: 'rgba(255, 255, 255, 0.05)',
          colorBgHover: 'rgba(255, 255, 255, 0.1)',
          colorText: '#f8fafc',
        },
        Modal: {
          contentBg: '#1e293b',
          headerBg: '#1e293b',
        },
        Popover: {
          colorBgElevated: '#1e293b',
        },
        Dropdown: {
          colorBgElevated: '#1e293b',
        },
        Menu: {
          colorItemBg: 'transparent',
          colorSubItemBg: 'transparent',
          colorItemBgHover: 'rgba(255, 255, 255, 0.05)',
          colorItemText: '#94a3b8',
          colorItemTextHover: '#f8fafc',
        },
        Tabs: {
          colorText: '#94a3b8',
          colorTextActive: '#f8fafc',
          inkBarColor: '#f97316',
          itemActiveColor: '#f97316',
          itemHoverColor: '#f8fafc',
        },
        Tag: {
          colorBgContainer: 'rgba(255, 255, 255, 0.05)',
          colorText: '#f8fafc',
        },
        Pagination: {
          colorBgContainer: 'transparent',
          colorPrimary: '#f97316',
          itemActiveBg: 'rgba(249, 115, 22, 0.15)',
        },
        Progress: {
          colorText: '#f8fafc',
          remainingColor: 'rgba(255, 255, 255, 0.1)',
        },
        Drawer: {
          colorBgElevated: '#1e293b',
        },
        Tooltip: {
          colorBgSpotlight: '#1e293b',
          colorTextLightSolid: '#f8fafc',
        },
      },
    }),
    []
  );

  return (
    <AntdConfigContext.Provider value={config as any}>
      <ConfigProvider theme={config}>
        <ApiErrorListener />
        {children}
      </ConfigProvider>
    </AntdConfigContext.Provider>
  );
}