<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { taskApi, usageApi } from '../api/modules'
import type { TaskItem, UsageOverview } from '../types'
import TaskStatusPill from '../components/TaskStatusPill.vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const tasks = ref<TaskItem[]>([])
const usage = ref<UsageOverview>({
  totalCalls: 0,
  totalTokens: 0,
  summaryCalls: 0,
  qaCalls: 0,
  taskCount: 0,
  quotaLimit: 0
})

const progress = computed(() => {
  if (!usage.value.quotaLimit) return 0
  return Math.min(100, Math.round((usage.value.totalTokens / usage.value.quotaLimit) * 100))
})

const loadData = async () => {
  loading.value = true
  try {
    const [taskPage, usageResult] = await Promise.all([
      taskApi.page({ current: 1, pageSize: 5 }),
      usageApi.me()
    ])
    tasks.value = taskPage.records
    usage.value = usageResult
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-shell dashboard">
    <section class="hero panel fade-in-up">
      <div class="hero-copy">
        <div class="mono hero-kicker">LONG VIDEO TO STRUCTURED KNOWLEDGE</div>
        <h1>让视频先被理解，再被提问。</h1>
        <p>
          这里聚合了分片上传、异步处理、转写摘要、RAG 召回、Agent 工具调用与成本治理，适合作为校招/实习项目的完整演示面板。
        </p>
        <div class="hero-actions">
          <el-button type="warning" @click="router.push('/upload')">上传新视频</el-button>
          <el-button plain @click="router.push('/tasks')">查看任务列表</el-button>
        </div>
      </div>
      <div class="hero-metrics">
        <div class="metric">
          <span>任务总数</span>
          <strong>{{ usage.taskCount }}</strong>
        </div>
        <div class="metric">
          <span>AI 调用次数</span>
          <strong>{{ usage.totalCalls }}</strong>
        </div>
        <div class="metric">
          <span>Token 消耗</span>
          <strong>{{ usage.totalTokens }}</strong>
        </div>
      </div>
    </section>

    <section class="grid">
      <div class="insight panel">
        <div class="section-head">
          <div>
            <h3>成本与配额</h3>
            <p>缓存命中、FAQ 复用、任务拦截会共同降低 Token 消耗。</p>
          </div>
          <span class="mono">{{ progress }}%</span>
        </div>
        <el-progress :percentage="progress" color="#f6b655" :stroke-width="10" />
        <div class="usage-list">
          <div>
            <span>Summary Calls</span>
            <strong>{{ usage.summaryCalls }}</strong>
          </div>
          <div>
            <span>QA Calls</span>
            <strong>{{ usage.qaCalls }}</strong>
          </div>
          <div>
            <span>Quota Limit</span>
            <strong>{{ usage.quotaLimit }}</strong>
          </div>
        </div>
      </div>

      <div class="pipeline panel">
        <div class="section-head">
          <div>
            <h3>处理状态机</h3>
            <p>PENDING / UPLOADED / QUEUED / PROCESSING_AUDIO / TRANSCRIBING / SUMMARIZING / INDEXING / SUCCESS</p>
          </div>
        </div>
        <div class="steps">
          <span v-for="step in ['UPLOAD', 'MQ', 'ASR', 'SUMMARY', 'RAG', 'AGENT']" :key="step">{{ step }}</span>
        </div>
      </div>
    </section>

    <section class="panel recent">
      <div class="section-head">
        <div>
          <h3>最近任务</h3>
          <p>快速查看最新视频的解析状态与处理结果。</p>
        </div>
      </div>
      <el-table v-loading="loading" :data="tasks">
        <el-table-column prop="videoTitle" label="视频名称" min-width="240" />
        <el-table-column label="状态" width="150">
          <template #default="{ row }">
            <TaskStatusPill :status-code="row.statusCode" />
          </template>
        </el-table-column>
        <el-table-column prop="currentStep" label="当前步骤" width="180" />
        <el-table-column prop="progressPercent" label="进度" width="120" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="warning" @click="router.push(`/tasks/${row.id}`)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.dashboard {
  display: grid;
  gap: 20px;
}

.hero {
  border-radius: 32px;
  padding: 30px;
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 24px;
}

.hero-kicker {
  color: var(--accent);
  letter-spacing: 0.12em;
  font-size: 12px;
}

.hero h1 {
  margin: 14px 0;
  font-size: clamp(42px, 5vw, 72px);
  letter-spacing: -0.06em;
  line-height: 1.02;
  max-width: 680px;
}

.hero p {
  max-width: 640px;
  color: var(--muted);
  line-height: 1.8;
}

.hero-actions {
  margin-top: 22px;
  display: flex;
  gap: 12px;
}

.hero-metrics {
  display: grid;
  gap: 14px;
}

.metric {
  border: 1px solid var(--line);
  border-radius: 22px;
  padding: 18px 20px;
  background: rgba(255, 255, 255, 0.04);
}

.metric span,
.usage-list span,
.section-head p {
  color: var(--muted);
  font-size: 13px;
}

.metric strong {
  display: block;
  margin-top: 10px;
  font-size: 30px;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.insight,
.pipeline,
.recent {
  border-radius: 28px;
  padding: 22px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.section-head h3 {
  margin: 0 0 8px;
  font-size: 24px;
}

.usage-list {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.usage-list strong {
  display: block;
  margin-top: 8px;
  font-size: 22px;
}

.steps {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.steps span {
  padding: 12px 16px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.03);
}

@media (max-width: 1100px) {
  .hero,
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
