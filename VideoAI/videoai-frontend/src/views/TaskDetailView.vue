<script setup lang="ts">
import dayjs from 'dayjs'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { agentApi, resultApi, taskApi } from '../api/modules'
import type { SummaryData, TaskDetail, TranscriptData, TranscriptSegment } from '../types'
import TaskStatusPill from '../components/TaskStatusPill.vue'
import ChatPanel from '../components/ChatPanel.vue'

const route = useRoute()
const taskId = Number(route.params.id)

const detail = ref<TaskDetail | null>(null)
const summary = ref<SummaryData | null>(null)
const transcript = ref<TranscriptData | null>(null)
const segments = ref<TranscriptSegment[]>([])
const debugResult = ref<Record<string, unknown> | null>(null)
const loading = ref(false)
let timer: number | null = null

const formatTime = (time?: string) => (time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-')
const formatDuration = (ms?: number) => (ms ? `${(ms / 1000).toFixed(1)} 秒` : '-')

const shouldPoll = computed(() => !detail.value || !['SUCCESS', 'FAILED'].includes(detail.value.statusCode))
const transcriptPreview = computed(() => transcript.value?.transcriptText || '任务处理中，转写结果暂未生成。')
const summaryHighlights = computed(() => summary.value?.highlights || [])
const summaryQuestions = computed(() => summary.value?.qaSuggestions || [])

const loadDetail = async () => {
  loading.value = true
  try {
    const [taskResult, summaryResult, transcriptResult, segmentResult, debug] = await Promise.all([
      taskApi.detail(taskId),
      resultApi.summary(taskId).catch(() => null),
      resultApi.transcript(taskId).catch(() => null),
      resultApi.segments(taskId).catch(() => []),
      agentApi.debug(taskId).catch(() => null)
    ])
    detail.value = taskResult
    summary.value = summaryResult
    transcript.value = transcriptResult
    segments.value = segmentResult
    debugResult.value = debug
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadDetail()
  timer = window.setInterval(() => {
    if (shouldPoll.value) {
      loadDetail()
    }
  }, 5000)
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<template>
  <div class="page-shell detail-page" v-loading="loading">
    <section class="detail-hero panel fade-in-up">
      <div>
        <div class="mono hero-label">TASK DETAIL</div>
        <h1 class="page-title">{{ detail?.videoTitle || '任务详情' }}</h1>
        <p class="page-copy">
          这里聚合了当前任务的状态机、转写结果、结构化摘要、片段检索与多轮问答能力，适合完整演示视频理解链路。
        </p>
      </div>

      <div class="hero-side">
        <TaskStatusPill :status-code="detail?.statusCode" />
        <div class="hero-meta">
          <div>
            <span>任务编号</span>
            <strong class="mono">{{ detail?.taskNo || '-' }}</strong>
          </div>
          <div>
            <span>开始时间</span>
            <strong>{{ formatTime(detail?.startedAt) }}</strong>
          </div>
          <div>
            <span>累计耗时</span>
            <strong>{{ formatDuration(detail?.costTimeMs) }}</strong>
          </div>
          <div>
            <span>文件 MD5</span>
            <strong class="mono">{{ detail?.fileMd5 || '-' }}</strong>
          </div>
        </div>
      </div>
    </section>

    <section class="overview-grid">
      <div class="panel summary-board">
        <div class="section-header">
          <div>
            <h3 class="section-heading">{{ summary?.title || '结构化摘要' }}</h3>
            <p class="section-description">SummaryAgent 输出摘要、大纲、关键词、亮点和追问建议。</p>
          </div>
        </div>

        <p class="summary-text">{{ summary?.summary || '任务处理中，摘要结果暂未生成。' }}</p>

        <div v-if="summary?.keywords?.length" class="keyword-group">
          <span v-for="item in summary.keywords" :key="item">{{ item }}</span>
        </div>

        <div class="summary-columns">
          <div>
            <h4>大纲</h4>
            <ul>
              <li v-for="item in summary?.outline || []" :key="item">{{ item }}</li>
            </ul>
          </div>
          <div>
            <h4>亮点</h4>
            <ul>
              <li v-for="item in summaryHighlights" :key="item">{{ item }}</li>
            </ul>
          </div>
        </div>

        <div v-if="summaryQuestions.length" class="follow-up">
          <h4>可继续追问</h4>
          <div class="follow-up-list">
            <span v-for="item in summaryQuestions" :key="item">{{ item }}</span>
          </div>
        </div>
      </div>

      <div class="panel timeline-board">
        <div class="section-header">
          <div>
            <h3 class="section-heading">状态时间线</h3>
            <p class="section-description">通过事件日志观察任务状态流转，辅助定位失败节点和耗时阶段。</p>
          </div>
        </div>

        <div class="timeline-list">
          <div v-for="event in detail?.events || []" :key="event.createTime + event.step" class="timeline-item">
            <div class="timeline-time mono">{{ formatTime(event.createTime) }}</div>
            <div>
              <strong>{{ event.toStatus || event.step }}</strong>
              <p>{{ event.detail || '无补充说明' }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="panel transcript-board">
      <div class="section-header">
        <div>
          <h3 class="section-heading">转写全文与片段检索</h3>
          <p class="section-description">转写全文用于回看原始内容，片段列表用于 RAG 召回与时间定位。</p>
        </div>
      </div>

      <div class="transcript-grid">
        <div class="transcript-full panel-muted">
          {{ transcriptPreview }}
        </div>

        <div class="segment-list">
          <div v-if="segments.length === 0" class="segment-empty panel-muted">任务处理中，片段结果暂未生成。</div>
          <div v-for="segment in segments" :key="segment.id" class="segment-item panel-muted">
            <div class="segment-time mono">
              {{ (segment.startTimeMs / 1000).toFixed(1) }}s - {{ (segment.endTimeMs / 1000).toFixed(1) }}s
            </div>
            <p>{{ segment.content }}</p>
            <span>{{ segment.keywords || '未提取关键词' }}</span>
          </div>
        </div>
      </div>
    </section>

    <section class="chat-grid">
      <ChatPanel :task-id="taskId" mode="chat" />
      <ChatPanel :task-id="taskId" mode="agent" />
    </section>

    <section v-if="debugResult" class="panel debug-board">
      <div class="section-header">
        <div>
          <h3 class="section-heading">Agent 调试输出</h3>
          <p class="section-description">用于演示 Video Analysis Agent 可调用的工具信息与辅助诊断结果。</p>
        </div>
      </div>
      <pre>{{ JSON.stringify(debugResult, null, 2) }}</pre>
    </section>
  </div>
</template>

<style scoped>
.detail-page,
.chat-grid {
  gap: 20px;
}

.detail-hero,
.summary-board,
.timeline-board,
.transcript-board,
.debug-board {
  border-radius: 30px;
  padding: 24px;
}

.detail-hero {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 24px;
}

.hero-label {
  color: var(--accent);
  font-size: 12px;
  letter-spacing: 0.12em;
}

.hero-side {
  display: grid;
  gap: 16px;
  justify-items: end;
}

.hero-meta {
  width: 100%;
  display: grid;
  gap: 14px;
}

.hero-meta div {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.035);
  border: 1px solid var(--line);
}

.hero-meta span {
  color: var(--muted);
  font-size: 13px;
}

.hero-meta strong {
  display: block;
  margin-top: 8px;
  word-break: break-word;
}

.overview-grid {
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 20px;
}

.summary-text {
  margin: 20px 0 0;
  color: var(--text-soft);
  line-height: 1.9;
  white-space: pre-wrap;
}

.keyword-group,
.follow-up-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.keyword-group {
  margin-top: 18px;
}

.keyword-group span,
.follow-up-list span {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 183, 97, 0.1);
  border: 1px solid rgba(255, 183, 97, 0.2);
  color: var(--accent);
}

.summary-columns {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-top: 22px;
}

.summary-columns h4,
.follow-up h4 {
  margin: 0 0 12px;
  font-size: 16px;
}

.summary-columns ul {
  margin: 0;
  padding-left: 18px;
  color: var(--text-soft);
  line-height: 1.9;
}

.follow-up {
  margin-top: 22px;
}

.timeline-list {
  display: grid;
  gap: 18px;
}

.timeline-item {
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 16px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--line);
}

.timeline-item:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.timeline-time {
  color: var(--muted);
  font-size: 12px;
}

.timeline-item p {
  margin: 8px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.transcript-grid,
.chat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.transcript-full,
.segment-item,
.segment-empty {
  border-radius: 18px;
  padding: 16px;
}

.transcript-full {
  white-space: pre-wrap;
  line-height: 1.9;
  color: var(--text-soft);
  min-height: 320px;
}

.segment-list {
  display: grid;
  gap: 12px;
  max-height: 520px;
  overflow: auto;
}

.segment-item p {
  margin: 10px 0 0;
  line-height: 1.8;
}

.segment-item span,
.segment-time {
  color: var(--muted);
  font-size: 12px;
}

.segment-item span {
  display: block;
  margin-top: 10px;
}

.segment-empty {
  color: var(--muted);
  display: grid;
  place-items: center;
}

.debug-board pre {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.7;
  color: var(--text-soft);
}

@media (max-width: 1180px) {
  .detail-hero,
  .overview-grid,
  .transcript-grid,
  .chat-grid,
  .summary-columns,
  .timeline-item {
    grid-template-columns: 1fr;
  }

  .hero-side {
    justify-items: start;
  }
}
</style>
