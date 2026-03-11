/**
 * 本地存储工具函数
 */

export const storage = {
  /**
   * 获取存储项
   */
  get: <T>(key: string): T | null => {
    if (typeof window === 'undefined') return null;
    try {
      const item = localStorage.getItem(key);
      return item ? JSON.parse(item) : null;
    } catch {
      return null;
    }
  },

  /**
   * 设置存储项
   */
  set: <T>(key: string, value: T): void => {
    if (typeof window === 'undefined') return;
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch (error) {
      console.error('Failed to save to localStorage:', error);
    }
  },

  /**
   * 移除存储项
   */
  remove: (key: string): void => {
    if (typeof window === 'undefined') return;
    try {
      localStorage.removeItem(key);
    } catch (error) {
      console.error('Failed to remove from localStorage:', error);
    }
  },

  /**
   * 清空所有存储
   */
  clear: (): void => {
    if (typeof window === 'undefined') return;
    try {
      localStorage.clear();
    } catch (error) {
      console.error('Failed to clear localStorage:', error);
    }
  },
};

/**
 * 带过期时间的存储
 */
export const storageWithExpiry = {
  /**
   * 设置带过期时间的存储
   */
  set: <T>(key: string, value: T, expiryMs: number): void => {
    const item = {
      value,
      expiry: Date.now() + expiryMs,
    };
    storage.set(key, item);
  },

  /**
   * 获取存储项（检查是否过期）
   */
  get: <T>(key: string): T | null => {
    const item = storage.get<{ value: T; expiry: number }>(key);
    if (!item) return null;

    if (Date.now() > item.expiry) {
      storage.remove(key);
      return null;
    }

    return item.value;
  },
};
