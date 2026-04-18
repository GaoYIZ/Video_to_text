export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface UserInfo {
  id: number
  userNo: string
  username: string
  nickname: string
}

export interface LoginResult {
  token: string
  user: UserInfo
}

export interface UploadInitResult {
  uploadId: string
  fastUpload: boolean
  fileId?: number
  uploadedChunks: number[]
  message: string
}

export interface TaskItem {
  id: number
  taskNo: string
  videoTitle: string
  status: number
  statusCode: string
  currentStep: string
  progressPercent: number
  failReason?: string
  fileId: number
  createTime: string
  finishedAt?: string
}

export interface TaskEvent {
  fromStatus?: string
  toStatus?: string
  step: string
  eventType: string
  success: number
  detail: string
  createTime: string
}

export interface TaskDetail extends TaskItem {
  fileMd5: string
  startedAt?: string
  costTimeMs?: number
  events: TaskEvent[]
}

export interface TranscriptSegment {
  id: number
  segmentIndex: number
  startTimeMs: number
  endTimeMs: number
  content: string
  keywords: string
}

export interface TranscriptData {
  transcriptId: number
  language: string
  durationMs: number
  wordCount: number
  transcriptText: string
  segments: TranscriptSegment[]
}

export interface SummaryData {
  title: string
  summary: string
  outline: string[]
  keywords: string[]
  highlights: string[]
  qaSuggestions: string[]
  modelName: string
  totalTokens: number
  costTimeMs: number
}

export interface ChatAnswer {
  sessionId: string
  answer: string
  citedSegments: TranscriptSegment[]
  fromCache: boolean
}

export interface UsageOverview {
  totalCalls: number
  totalTokens: number
  summaryCalls: number
  qaCalls: number
  taskCount: number
  quotaLimit: number
}

export interface UserQuota {
  userId: number
  dailyLimit: number
  monthlyLimit: number
  todayUsed: number
  monthUsed: number
  remainingToday: number
  remainingMonth: number
  quotaExceeded: boolean
}

export interface UsageStats {
  userId: number
  period: string
  totalCalls: number
  totalInputTokens: number
  totalOutputTokens: number
  totalTokens: number
  estimatedCost: number
  avgResponseTimeMs: number
  cacheHitRate: number
  successRate: number
  callsByBizType?: Record<string, number>
  callsByModel?: Record<string, number>
}

export interface PageResult<T> {
  total: number
  current: number
  pageSize: number
  records: T[]
}
