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
const formatMs = (ms?: number) => (ms ? `${(ms / 1000).toFixed(1)}s` : '-')

const shouldPoll = computed(() => !detail.value || !['SUCCESS', 'FAILED'].includes(detail.value.statusCode))

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
    <section class="headline panel">
      <div>
        <div class="mono">TASK DETAIL</div>
        <h1>{{ detail?.videoTitle || '任务详情' }}</h1>
        <p class="mono">{{ detail?.taskNo }}</p>
      </div>
      <div class="headline-side">
        <TaskStatusPill :status-code="detail?.statusCode" />
        <div class="meta-grid">
          <div>
            <span>开始时间</span>
            <strong>{{ formatTime(detail?.startedAt) }}</strong>
          </div>
          <div>
            <span>完成耗时</span>
            <strong>{{ formatMs(detail?.costTimeMs) }}</strong>
          </div>
          <div>
            <span>文件 MD5</span>
            <strong class="mono">{{ detail?.fileMd5 || '-' }}</strong>
          </div>
        </div>
      </div>
    </section>

    <section class="dual-grid">
      <div class="panel timeline">
        <div class="section-head">
          <div>
            <h3>状态流转时间线</h3>
            <p>事件日志用于定位失败节点和统计处理耗时。</p>
          </div>
        </div>
        <div class="timeline-list">
          <div v-for="event in detail?.events || []" :key="event.createTime + event.step" class="timeline-item">
            <span class="time mono">{{ formatTime(event.createTime) }}</span>
            <div>
              <strong>{{ event.toStatus || event.step }}</strong>
              <p>{{ event.detail }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="panel summary">
        <div class="section-head">
          <div>
            <h3>{{ summary?.title || '结构化总结' }}</h3>
            <p>SummaryAgent 输出结构化摘要、提纲、关键词和追问建议。</p>
          </div>
        </div>
        <p class="summary-text">{{ summary?.summary || '任务处理中，摘要暂未生成。' }}</p>
        <div class="tag-group">
          <span v-for="item in summary?.keywords || []" :key="item">{{ item }}</span>
        </div>
        <ul class="outline-list">
          <li v-for="item in summary?.outline || []" :key="item">{{ item }}</li>
        </ul>
      </div>
    </section>

    <section class="panel transcript-section">
      <div class="section-head">
        <div>
          <h3>转写与片段召回</h3>
          <p>长文本分段后用于 MySQL/Redis 关键词召回版 RAG。</p>
        </div>
      </div>
      <div class="transcript-grid">
        <div class="transcript-full">{{ transcript?.transcriptText || '暂无转写内容' }}</div>
        <div class="segment-list">
          <div v-for="segment in segments" :key="segment.id" class="segment-item">
            <div class="mono">{{ segment.startTimeMs / 1000 }}s - {{ segment.endTimeMs / 1000 }}s</div>
            <p>{{ segment.content }}</p>
            <span>{{ segment.keywords }}</span>
          </div>
        </div>
      </div>
    </section>

    <section class="dual-grid">
      <ChatPanel :task-id="taskId" mode="chat" />
      <ChatPanel :task-id="taskId" mode="agent" />
    </section>

    <section class="panel debug-panel" v-if="debugResult">
      <div class="section-head">
        <div>
          <h3>Agent Tools Debug</h3>
          <p>演示 Video Analysis Agent 可调用的视频信息、摘要和片段检索工具。</p>
        </div>
      </div>
      <pre>{{ JSON.stringify(debugResult, null, 2) }}</pre>
    </section>
  </div>
</template>

<style scoped>
.detail-page {
  display: grid;
  gap: 20px;
}

.headline,
.timeline,
.summary,
.transcript-section,
.debug-panel {
  border-radius: 28px;
  padding: 24px;
}

.headline {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 24px;
}

.headline h1 {
  margin: 12px 0 10px;
  font-size: clamp(36px, 4vw, 58px);
  letter-spacing: -0.06em;
}

.headline .mono,
.section-head p,
.meta-grid span {
  color: var(--muted);
}

.headline-side {
  display: grid;
  justify-items: end;
  gap: 16px;
}

.meta-grid {
  display: grid;
  gap: 12px;
}

.meta-grid strong {
  display: block;
  margin-top: 6px;
}

.dual-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.section-head {
  margin-bottom: 18px;
}

.section-head h3 {
  margin: 0 0 8px;
  font-size: 24px;
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

.timeline-item p,
.summary-text,
.segment-item p {
  margin: 8px 0 0;
  color: #d6dbe5;
  line-height: 1.8;
}

.tag-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin: 18px 0;
}

.tag-group span {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(246, 182, 85, 0.1);
  color: var(--accent);
  border: 1px solid rgba(246, 182, 85, 0.2);
}

.outline-list {
  margin: 0;
  padding-left: 18px;
  color: #d6dbe5;
  line-height: 2;
}

.transcript-grid {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 20px;
}

.transcript-full {
  white-space: pre-wrap;
  line-height: 1.9;
  color: #dfe5ec;
}

.segment-list {
  display: grid;
  gap: 12px;
  max-height: 420px;
  overflow: auto;
}

.segment-item {
  border: 1px solid var(--line);
  border-radius: 18px;
  padding: 14px;
}

.segment-item .mono,
.segment-item span {
  color: var(--muted);
  font-size: 12px;
}

.debug-panel pre {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.6;
  color: #dce2ec;
}

@media (max-width: 1100px) {
  .headline,
  .dual-grid,
  .transcript-grid,
  .timeline-item {
    grid-template-columns: 1fr;
  }

  .headline-side {
    justify-items: start;
  }
}
</style>
