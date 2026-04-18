import http from './http'
import type {
  ChatAnswer,
  LoginResult,
  PageResult,
  SummaryData,
  TaskDetail,
  TaskEvent,
  TaskItem,
  TranscriptData,
  TranscriptSegment,
  UploadInitResult,
  UsageOverview,
  UserQuota,
  UsageStats,
  UserInfo
} from '../types'

export const userApi = {
  register: (data: { username: string; password: string; nickname: string }) =>
    http.post<any, LoginResult>('/api/user/register', data),
  login: (data: { username: string; password: string }) =>
    http.post<any, LoginResult>('/api/user/login', data),
  me: () => http.get<any, UserInfo>('/api/user/me')
}

export const uploadApi = {
  init: (data: {
    fileName: string
    fileMd5: string
    fileSize: number
    chunkSize: number
    totalChunks: number
    mimeType?: string
  }) => http.post<any, UploadInitResult>('/api/upload/init', data),
  chunks: (uploadId: string) => http.get<any, number[]>('/api/upload/chunks', { params: { uploadId } }),
  uploadChunk: (formData: FormData) =>
    http.post<any, void>('/api/upload/chunk', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  merge: (data: { uploadId: string; fileMd5: string; fileName: string }) =>
    http.post<any, { fileId: number }>('/api/upload/merge', data)
}

export const taskApi = {
  create: (data: { uploadId: string; sessionId?: string }) =>
    http.post<any, TaskItem>('/api/video-task/create', data),
  page: (params: { current?: number; pageSize?: number; status?: number; keyword?: string }) =>
    http.get<any, PageResult<TaskItem>>('/api/video-task/page', { params }),
  detail: (id: number) => http.get<any, TaskDetail>(`/api/video-task/${id}`),
  status: (id: number) => http.get<any, TaskItem>(`/api/video-task/${id}/status`),
  retry: (id: number) => http.post<any, void>(`/api/video-task/${id}/retry`),
  events: (id: number) => http.get<any, TaskEvent[]>(`/api/video-task/${id}/events`),
  remove: (id: number) => http.delete<any, void>(`/api/video-task/${id}`)
}

export const resultApi = {
  transcript: (taskId: number) => http.get<any, TranscriptData>(`/api/video-result/${taskId}/transcript`),
  summary: (taskId: number) => http.get<any, SummaryData>(`/api/video-result/${taskId}/summary`),
  segments: (taskId: number) => http.get<any, TranscriptSegment[]>(`/api/video-result/${taskId}/segments`)
}

export const chatApi = {
  ask: (data: { taskId: number; question: string; sessionId?: string }) =>
    http.post<any, ChatAnswer>('/api/chat/ask', data),
  history: (taskId: number, sessionId: string) =>
    http.get<any, PageResult<string>>('/api/chat/history', { params: { taskId, sessionId } })
}

export const agentApi = {
  ask: (data: { taskId: number; question: string; sessionId?: string }) =>
    http.post<any, ChatAnswer>('/api/agent/video/ask', data),
  debug: (taskId: number, keyword?: string) =>
    http.get<any, Record<string, unknown>>('/api/agent/video/tools/debug', { params: { taskId, keyword } })
}

export const usageApi = {
  me: () => http.get<any, UsageOverview>('/api/usage/me'),
  quota: () => http.get<any, UserQuota>('/api/usage/quota'),
  stats: (period: string = 'day') => http.get<any, UsageStats>('/api/usage/stats', { params: { period } }),
  daily: () => http.get<any, UsageStats>('/api/usage/daily'),
  monthly: () => http.get<any, UsageStats>('/api/usage/monthly')
}
