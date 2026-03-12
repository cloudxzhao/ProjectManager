// 报表 (Report) 管理模块 API

import { api } from './axios';
import { endpoints } from './endpoints';

export interface BurndownData {
  date: string;
  remaining: number;
  ideal: number;
  completed: number;
}

export interface CumulativeFlowData {
  date: string;
  todo: number;
  inProgress: number;
  testing: number;
  done: number;
}

export interface VelocityData {
  sprint: string;
  committed: number;
  completed: number;
}

export interface TaskDistribution {
  status: string;
  count: number;
  percentage: number;
}

export interface ProjectReport {
  projectId: number;
  totalTasks: number;
  completedTasks: number;
  inProgressTasks: number;
  todoTasks: number;
  overdueTasks: number;
  totalStories: number;
  completedStories: number;
  totalPoints: number;
  completedPoints: number;
  velocity: number;
  burndownData: BurndownData[];
  cumulativeFlowData: CumulativeFlowData[];
  velocityData: VelocityData[];
  taskDistribution: TaskDistribution[];
}

export interface QueryParams {
  startDate?: string;
  endDate?: string;
  sprintId?: number;
}

/**
 * 获取燃尽图数据
 * @param projectId 项目 ID
 * @param params 查询参数
 */
export const getBurndown = (projectId: number, params?: QueryParams) =>
  api.get<BurndownData[]>(endpoints.report.burndown(projectId), { params });

/**
 * 获取累积流程图数据
 * @param projectId 项目 ID
 * @param params 查询参数
 */
export const getCumulativeFlow = (projectId: number, params?: QueryParams) =>
  api.get<CumulativeFlowData[]>(endpoints.report.cumulativeFlow(projectId), { params });

/**
 * 获取速率图数据
 * @param projectId 项目 ID
 * @param params 查询参数
 */
export const getVelocity = (projectId: number, params?: QueryParams) =>
  api.get<VelocityData[]>(endpoints.report.velocity(projectId), { params });

/**
 * 获取项目完整报表
 * @param projectId 项目 ID
 * @param params 查询参数
 */
export const getProjectReport = (projectId: number, params?: QueryParams) =>
  api.get<ProjectReport>(`/projects/${projectId}/reports`);
