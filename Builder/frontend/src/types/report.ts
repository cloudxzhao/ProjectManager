// 报表 (Report) 相关类型定义

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

export interface ReportQueryParams {
  startDate?: string;
  endDate?: string;
  sprintId?: number;
}
