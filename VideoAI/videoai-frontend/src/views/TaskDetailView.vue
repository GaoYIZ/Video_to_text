<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { agentApi, resultApi, taskApi } from '../api/modules'
import type { SummaryData, TaskDetail, TranscriptData, TranscriptSegment } from '../types'
import TaskStatusPill from '../components/TaskStatusPill.vue'
import ChatPanel from '../components/ChatPanel.vue'
import { formatDateTime, formatDuration, formatSegmentTime } from '../utils/format'

const route = useRoute()
const taskId = Number(route.params.id)

const detail = ref<TaskDetail | null>(null)
const summary = ref<SummaryData | null>(null)
const transcript = ref<TranscriptData | null>(null)
const segments = ref<TranscriptSegment[]>([])
const debugResult = ref<Record<string, unknown> | null>(null)
const loading = ref(false)
let timer: number | null = null

const shouldPoll = computed(() => !detail.value || !['SUCCESS', 'FAILED'].includes(detail.value.statusCode))
const transcriptPreview = computed(() => transcript.value?.transcriptText || '任务处理中，转写结果暂未生成。')

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
        <div class="mono hero-kicker">TASK DETAIL</div>
        <h1 class="page-title">{{ detail?.videoTitle || '任务详情' }}</h1>
        <p class="page-copy">
          这里把任务状态、结构化摘要、转写全文、片段召回和双通道问答全部放在同一页，适合完整讲述项目闭环。
        </p>
      </div>

      <div class="hero-side">
        <TaskStatusPill :status-code="detail?.statusCode" />
        <div class="hero-meta">
          <div>
            <span>任务编号</span>
            <strong>{{ detail?.taskNo || '-' }}</strong>
          </div>
          <div>
            <span>开始时间</span>
            <strong>{{ formatDateTime(detail?.startedAt) }}</strong>
          </div>
          <div>
            <span>累计耗时</span>
            <strong>{{ formatDuration(detail?.costTimeMs) }}</strong>
          </div>
          <div>
            <span>文件 MD5</span>
            <strong>{{ detail?.fileMd5 || '-' }}</strong>
          </div>
        </div>
      </div>
    </section>

    <section class="overview-grid">
      <div class="panel summary-board">
        <div class="section-header">
          <div>
            <h3 class="section-heading">{{ summary?.title || '结构化摘要' }}</h3>
            <p class="section-description">SummaryAgent 输出一句话总结、大纲、关键词、亮点和建议追问。</p>
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
              <li v-for="item in summary?.highlights || []" :key="item">{{ item }}</li>
            </ul>
          </div>
        </div>

        <div v-if="summary?.qaSuggestions?.length" class="follow-up">
          <h4>建议追问</h4>
          <div class="follow-up-list">
            <span v-for="item in summary.qaSuggestions" :key="item">{{ item }}</span>
          </div>
        </div>
      </div>

      <div class="panel timeline-board">
        <div class="section-header">
          <div>
            <h3 class="section-heading">状态时间线</h3>
            <p class="section-description">事件日志会记录状态流转，用于定位失败原因与耗时阶段。</p>
          </div>
        </div>

        <div class="timeline-list">
          <div v-for="event in detail?.events || []" :key="`${event.createTime}-${event.step}`" class="timeline-item">
            <div class="mono timeline-time">{{ formatDateTime(event.createTime) }}</div>
            <div>
              <strong>{{ event.toStatus || event.step }}</strong>
              <p>{{ event.detail || '无额外说明' }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="panel transcript-board">
      <div class="section-header">
        <div>
          <h3 class="section-heading">转写全文与片段列表</h3>
          <p class="section-description">全文用于回看原始内容，片段列表用于 RAG 召回与时间定位。</p>
        </div>
      </div>

      <div class="transcript-grid">
        <div class="transcript-full panel-muted">{{ transcriptPreview }}</div>

        <div class="segment-list">
          <div v-if="segments.length === 0" class="segment-empty panel-muted">任务处理中，片段结果暂未生成。</div>
          <div v-for="segment in segments" :key="segment.id" class="segment-item panel-muted">
            <div class="mono segment-time">
              {{ formatSegmentTime(segment.startTimeMs) }} - {{ formatSegmentTime(segment.endTimeMs) }}
            </div>
            <p>{{ segment.content }}</p>
            <span>{{ segment.keywords || '暂无关键词' }}</span>
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
          <p class="section-description">用于展示 Video Analysis Agent 能访问到的工具与辅助诊断结果。</p>
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
  grid-template-columns: 1.08fr 0.92fr;
  gap: 24px;
}

.hero-kicker {
  color: var(--accent-strong);
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
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid var(--line);
}

.hero-meta span,
.segment-item span,
.segment-time,
.timeline-time {
  color: var(--muted);
  font-size: 12px;
}

.hero-meta strong {
  display: block;
  margin-top: 8px;
  color: var(--text);
  word-break: break-word;
}

.overview-grid {
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 20px;
}

.summary-text {
  margin: 18px 0 0;
  color: var(--text-soft);
  line-height: 1.8;
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
  background: rgba(111, 168, 255, 0.12);
  border: 1px solid rgba(111, 168, 255, 0.22);
  color: var(--accent-strong);
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
}

.summary-columns ul {
  margin: 0;
  padding-left: 18px;
  color: var(--text-soft);
  line-height: 1.8;
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
  grid-template-columns: 170px 1fr;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);
}

.timeline-item:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.timeline-item p,
.segment-item p {
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
  min-height: 320px;
  white-space: pre-wrap;
  color: var(--text-soft);
  line-height: 1.85;
}

.segment-list {
  display: grid;
  gap: 12px;
  max-height: 520px;
  overflow: auto;
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
