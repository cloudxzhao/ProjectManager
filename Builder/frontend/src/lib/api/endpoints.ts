// API 端点常量定义

export const endpoints = {
  // 认证相关
  auth: {
    login: '/auth/login',
    register: '/auth/register',
    logout: '/auth/logout',
    refreshToken: '/auth/refresh',
    passwordReset: '/auth/password/reset',
    passwordResetConfirm: '/auth/password/reset/confirm',
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
    detail: (id: string) => `/projects/${id}`,
    update: (id: string) => `/projects/${id}`,
    delete: (id: string) => `/projects/${id}`,
    members: (id: string) => `/projects/${id}/members`,
    addMember: (id: string) => `/projects/${id}/members`,
    removeMember: (projectId: string, userId: string) => `/projects/${projectId}/members/${userId}`,
  },

  // 任务相关
  task: {
    list: '/tasks',
    create: '/tasks',
    detail: (id: string) => `/tasks/${id}`,
    update: (id: string) => `/tasks/${id}`,
    delete: (id: string) => `/tasks/${id}`,
    move: (id: string) => `/tasks/${id}/move`,
    comments: (id: string) => `/tasks/${id}/comments`,
    subtasks: (id: string) => `/tasks/${id}/subtasks`,
  },

  // 用户故事相关
  story: {
    list: (projectId: string) => `/projects/${projectId}/stories`,
    create: (projectId: string) => `/projects/${projectId}/stories`,
    detail: (projectId: string, storyId: string) => `/projects/${projectId}/stories/${storyId}`,
    update: (projectId: string, storyId: string) => `/projects/${projectId}/stories/${storyId}`,
    delete: (projectId: string, storyId: string) => `/projects/${projectId}/stories/${storyId}`,
  },

  // 问题追踪相关
  issue: {
    list: (projectId: string) => `/projects/${projectId}/issues`,
    create: (projectId: string) => `/projects/${projectId}/issues`,
    detail: (projectId: string, issueId: string) => `/projects/${projectId}/issues/${issueId}`,
    update: (projectId: string, issueId: string) => `/projects/${projectId}/issues/${issueId}`,
    delete: (projectId: string, issueId: string) => `/projects/${projectId}/issues/${issueId}`,
  },

  // Wiki 相关
  wiki: {
    list: (projectId: string) => `/projects/${projectId}/wiki`,
    create: (projectId: string) => `/projects/${projectId}/wiki`,
    detail: (projectId: string, docId: string) => `/projects/${projectId}/wiki/${docId}`,
    update: (projectId: string, docId: string) => `/projects/${projectId}/wiki/${docId}`,
    delete: (projectId: string, docId: string) => `/projects/${projectId}/wiki/${docId}`,
  },

  // 报表相关
  report: {
    burndown: (projectId: string) => `/projects/${projectId}/reports/burndown`,
    cumulativeFlow: (projectId: string) => `/projects/${projectId}/reports/cumulative-flow`,
    velocity: (projectId: string) => `/projects/${projectId}/reports/velocity`,
  },

  // 通知相关
  notification: {
    list: '/notifications',
    unread: '/notifications/unread',
    markRead: (id: string) => `/notifications/${id}/read`,
    markAllRead: '/notifications/read-all',
  },
} as const;
