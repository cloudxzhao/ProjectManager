import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface UIState {
  // Sidebar
  sidebarCollapsed: boolean;
  sidebarOpen: boolean;

  // Theme
  isDarkMode: boolean;

  // Modals
  modals: Record<string, boolean>;

  // Actions
  toggleSidebar: () => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  setSidebarOpen: (open: boolean) => void;
  toggleDarkMode: () => void;
  setDarkMode: (isDark: boolean) => void;
  openModal: (name: string) => void;
  closeModal: (name: string) => void;
}

export const useUIStore = create<UIState>()(
  persist(
    (set) => ({
      sidebarCollapsed: false,
      sidebarOpen: false,
      isDarkMode: true,
      modals: {},

      toggleSidebar: () => {
        set((state) => ({
          sidebarOpen: !state.sidebarOpen,
        }));
      },

      setSidebarCollapsed: (collapsed: boolean) => {
        set({ sidebarCollapsed: collapsed });
      },

      setSidebarOpen: (open: boolean) => {
        set({ sidebarOpen: open });
      },

      toggleDarkMode: () => {
        set((state) => ({ isDarkMode: !state.isDarkMode }));
      },

      setDarkMode: (isDark: boolean) => {
        set({ isDarkMode: isDark });
      },

      openModal: (name: string) => {
        set((state) => ({
          modals: { ...state.modals, [name]: true },
        }));
      },

      closeModal: (name: string) => {
        set((state) => ({
          modals: { ...state.modals, [name]: false },
        }));
      },
    }),
    {
      name: 'ui-storage',
      partialize: (state) => ({
        sidebarCollapsed: state.sidebarCollapsed,
        isDarkMode: state.isDarkMode,
      }),
    }
  )
);
