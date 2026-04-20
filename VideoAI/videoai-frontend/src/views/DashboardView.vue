<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { workbenchApi } from '../api/modules'
import type { TaskItem, WorkbenchOverview } from '../types'
import TaskStatusPill from '../components/TaskStatusPill.vue'

const router = useRouter()
const loading = ref(false)
const overview = ref<WorkbenchOverview | null>(null)

const recentTasks = computed<TaskItem[]>(() => overview.value?.recentTasks || [])
const usage = computed(() => overview.value?.usageOverview)
const quota = computed(() => overview.value?.quota)
const todayPercent = computed(() => {
  if (!quota.value?.dailyLimit) return 0
  return Math.min(100, Math.round((quota.value.todayUsed / quota.value.dailyLimit) * 100))
})

const loadData = async () => {
  loading.value = true
  try {
    overview.value = await workbenchApi.overview()
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-shell home-page">
    <section class="hero panel fade-in-up">
      <div>
        <div class="mono hero-kicker">VIDEO ANALYSIS OVERVIEW</div>
        <h1 class="page-title">围绕单个视频，完成从上传到问答的完整理解闭环</h1>
        <p class="page-copy">
          这里是删掉前那套工作台的首页形态：它更强调“当前可操作的链路”，把上传入口、近期任务、
          AI 调用量和配额使用集中到一屏里。
        </p>

        <div class="hero-actions">
          <el-button type="warning" @click="router.push('/convert')">开始解析视频</el-button>
          <el-button plain @click="router.push('/chat')">进入智能问答</el-button>
        </div>
      </div>

      <div class="hero-metrics">
        <div class="metric-card panel-muted">
          <span>累计任务数</span>
          <strong class="metric-value">{{ usage?.taskCount || 0 }}</strong>
          <p>已经创建并纳入解析链路的视频任务总量。</p>
        </div>
        <div class="metric-card panel-muted">
          <span>处理中任务</span>
          <strong class="metric-value">{{ overview?.processingTasks || 0 }}</strong>
          <p>仍在上传、转写、摘要或索引阶段中的任务。</p>
        </div>
        <div class="metric-card panel-muted">
          <span>AI 总调用次数</span>
          <strong class="metric-value">{{ usage?.totalCalls || 0 }}</strong>
          <p>累计摘要和问答请求次数，用于体现成本治理与限流收益。</p>
        </div>
      </div>
    </section>

    <section class="content-grid">
      <div class="panel pipeline-panel">
        <div class="section-header">
          <div>
            <h3 class="section-heading">当前处理链路</h3>
            <p class="section-description">把上传、处理、检索和问答收束成一条可演示、可讲解的完整路径。</p>
          </div>
        </div>

        <div class="step-list">
          <span>分片上传</span>
          <span>断点续传</span>
          <span>FFmpeg 提音</span>
          <span>ASR 转写</span>
          <span>SummaryAgent</span>
          <span>RAG 检索</span>
          <span>VideoQAAgent</span>
        </div>
      </div>

      <div class="panel quota-panel">
        <div class="section-header">
          <div>
            <h3 class="section-heading">今日配额</h3>
            <p class="section-description">展示用量和限额，方便讲解 AI 成本治理与安全防刷策略。</p>
          </div>
          <strong class="quota-percent">{{ todayPercent }}%</strong>
        </div>

        <el-progress :percentage="todayPercent" color="#6fa8ff" :stroke-width="10" />

        <div class="quota-grid">
          <div>
            <span>今日已用</span>
            <strong>{{ quota?.todayUsed || 0 }}</strong>
          </div>
          <div>
            <span>今日剩余</span>
            <strong>{{ quota?.remainingToday || 0 }}</strong>
          </div>
          <div>
            <span>月度限额</span>
            <strong>{{ quota?.monthlyLimit || 0 }}</strong>
          </div>
        </div>
      </div>
    </section>

    <section class="panel recent-panel">
      <div class="section-header">
        <div>
          <h3 class="section-heading">最近任务</h3>
          <p class="section-description">快速继续昨天删掉前那套体验：点进去就能看转写、摘要、Agent 问答。</p>
        </div>
        <el-button plain @click="router.push('/tasks')">查看全部任务</el-button>
      </div>

      <el-table v-loading="loading" :data="recentTasks">
        <el-table-column prop="videoTitle" label="视频标题" min-width="220" />
        <el-table-column prop="taskNo" label="任务编号" min-width="180" />
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <TaskStatusPill :status-code="row.statusCode" />
          </template>
        </el-table-column>
        <el-table-column prop="currentStep" label="当前步骤" min-width="160" />
        <el-table-column prop="progressPercent" label="进度" width="110">
          <template #default="{ row }">{{ row.progressPercent }}%</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/tasks/${row.id}`)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.home-page {
  gap: 20px;
}

.hero,
.pipeline-panel,
.quota-panel,
.recent-panel {
  border-radius: 30px;
  padding: 24px;
}

.hero {
  display: grid;
  grid-template-columns: 1.08fr 0.92fr;
  gap: 22px;
}

.hero-kicker {
  color: var(--accent-strong);
  font-size: 12px;
  letter-spacing: 0.12em;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 22px;
}

.hero-metrics {
  display: grid;
  gap: 14px;
}

.metric-card {
  border-radius: 22px;
  padding: 18px;
}

.metric-card span,
.quota-grid span {
  color: var(--muted);
  font-size: 13px;
}

.metric-card p {
  margin: 10px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.step-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.step-list span {
  padding: 10px 14px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.76);
}

.quota-percent {
  color: var(--accent-strong);
}

.quota-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.quota-grid strong {
  display: block;
  margin-top: 8px;
  font-size: 24px;
  color: var(--text);
}

@media (max-width: 1180px) {
  .hero,
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .quota-grid {
    grid-template-columns: 1fr;
  }
}
</style>
