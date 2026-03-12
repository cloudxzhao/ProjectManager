// API 端点常量定义

export const endpoints = {
  // 认证相关
  auth: {
    login: '/auth/login',
    register: '/auth/register',
    logout: '/auth/logout',
    refreshToken: '/auth/refresh',
    forgotPassword: '/auth/forgot-password',
    resetPassword: '/auth/reset-password',
  },

  // 用户相关
  user: {
    profile: '/user/profile',
    update: '/user/profile',
    avatar: '/user/avatar',
  },

  // 项目相关
  project: {
    list: '/projects',
    create: '/projects',
    detail: (id: number) => `/projects/${id}`,
    update: (id: number) => `/projects/${id}`,
    delete: (id: number) => `/projects/${id}`,
    members: (id: number) => `/projects/${id}/members`,
    addMember: (id: number) => `/projects/${id}/members`,
    removeMember: (projectId: number, userId: number) => `/projects/${projectId}/members/${userId}`,
  },

  // 任务相关
  task: {
    list: (projectId: number) => `/projects/${projectId}/tasks`,
    create: (projectId: number) => `/projects/${projectId}/tasks`,
    detail: (projectId: number, id: number) => `/projects/${projectId}/tasks/${id}`,
    update: (projectId: number, id: number) => `/projects/${projectId}/tasks/${id}`,
    delete: (projectId: number, id: number) => `/projects/${projectId}/tasks/${id}`,
    move: (projectId: number, id: number) => `/projects/${projectId}/tasks/${id}/move`,
    comments: (projectId: number, id: number) => `/projects/${projectId}/tasks/${id}/comments`,
    subtasks: (projectId: number, id: number) => `/projects/${projectId}/tasks/${id}/subtasks`,
    toggleComplete: (projectId: number, id: number) => `/projects/${projectId}/tasks/${id}/toggle-complete`,
  },

  // 用户故事相关
  story: {
    list: (projectId: number) => `/projects/${projectId}/stories`,
    create: (projectId: number) => `/projects/${projectId}/stories`,
    detail: (projectId: number, storyId: number) => `/projects/${projectId}/stories/${storyId}`,
    update: (projectId: number, storyId: number) => `/projects/${projectId}/stories/${storyId}`,
    delete: (projectId: number, storyId: number) => `/projects/${projectId}/stories/${storyId}`,
  },

  // 问题追踪相关
  issue: {
    list: (projectId: number) => `/projects/${projectId}/issues`,
    create: (projectId: number) => `/projects/${projectId}/issues`,
    detail: (projectId: number, issueId: number) => `/projects/${projectId}/issues/${issueId}`,
    update: (projectId: number, issueId: number) => `/projects/${projectId}/issues/${issueId}`,
    delete: (projectId: number, issueId: number) => `/projects/${projectId}/issues/${issueId}`,
  },

  // Wiki 相关
  wiki: {
    list: (projectId: number) => `/projects/${projectId}/wiki`,
    create: (projectId: number) => `/projects/${projectId}/wiki`,
    detail: (projectId: number, docId: number) => `/projects/${projectId}/wiki/${docId}`,
    update: (projectId: number, docId: number) => `/projects/${projectId}/wiki/${docId}`,
    delete: (projectId: number, docId: number) => `/projects/${projectId}/wiki/${docId}`,
  },

  // 报表相关
  report: {
    burndown: (projectId: number) => `/projects/${projectId}/reports/burndown`,
    cumulativeFlow: (projectId: number) => `/projects/${projectId}/reports/cumulative-flow`,
    velocity: (projectId: number) => `/projects/${projectId}/reports/velocity`,
  },

  // 通知相关
  notification: {
    list: '/notifications',
    unread: '/notifications/unread',
    markRead: (id: number) => `/notifications/${id}/read`,
    markAllRead: '/notifications/read-all',
  },
} as const;
