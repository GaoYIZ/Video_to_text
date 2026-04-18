<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { taskApi, usageApi } from '../api/modules'
import type { TaskItem, UsageOverview } from '../types'
import TaskStatusPill from '../components/TaskStatusPill.vue'

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

const quotaUsage = computed(() => {
  if (!usage.value.quotaLimit) return 0
  return Math.min(100, Math.round((usage.value.totalCalls / usage.value.quotaLimit) * 100))
})

const processingCount = computed(
  () => tasks.value.filter((item) => !['SUCCESS', 'FAILED'].includes(item.statusCode)).length
)

const loadData = async () => {
  loading.value = true
  try {
    const [taskPage, usageResult] = await Promise.all([
      taskApi.page({ current: 1, pageSize: 6 }),
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
    <section class="overview panel fade-in-up">
      <div class="overview-copy">
        <div class="mono overview-kicker">SYSTEM OVERVIEW</div>
        <h1 class="page-title">长视频解析、理解与问答的统一工作台</h1>
        <p class="page-copy">
          从上传入库到转写、摘要、片段召回和智能问答，这里聚合了整个视频理解链路，让你可以在一个界面里追踪工程状态和 AI 结果。
        </p>

        <div class="overview-actions">
          <el-button type="warning" @click="router.push('/upload')">上传视频</el-button>
          <el-button plain @click="router.push('/tasks')">查看任务列表</el-button>
        </div>
      </div>

      <div class="overview-stats">
        <div class="stat panel-muted">
          <span>总任务数</span>
          <strong class="metric-value">{{ usage.taskCount }}</strong>
          <p>已纳入解析链路的视频任务总量</p>
        </div>
        <div class="stat panel-muted">
          <span>处理中任务</span>
          <strong class="metric-value">{{ processingCount }}</strong>
          <p>仍处于上传、转写、摘要或索引阶段</p>
        </div>
        <div class="stat panel-muted">
          <span>AI 调用次数</span>
          <strong class="metric-value">{{ usage.totalCalls }}</strong>
          <p>累计摘要与问答请求的总调用量</p>
        </div>
      </div>
    </section>

    <section class="dashboard-grid">
      <div class="panel system-board">
        <div class="section-header">
          <div>
            <h3 class="section-heading">处理链路</h3>
            <p class="section-description">当前系统围绕上传、异步编排、ASR、摘要、索引和问答展开。</p>
          </div>
        </div>

        <div class="pipeline">
          <div v-for="step in ['上传入库', '异步投递', '音频处理', '语音转写', '结构化摘要', 'RAG 问答']" :key="step">
            {{ step }}
          </div>
        </div>
      </div>

      <div class="panel quota-board">
        <div class="section-header">
          <div>
            <h3 class="section-heading">调用与成本</h3>
            <p class="section-description">配额、缓存和任务去重共同降低模型调用成本。</p>
          </div>
          <span class="quota-percent mono">{{ quotaUsage }}%</span>
        </div>

        <el-progress :percentage="quotaUsage" color="#ffb761" :stroke-width="10" />

        <div class="quota-list">
          <div>
            <span>摘要调用</span>
            <strong>{{ usage.summaryCalls }}</strong>
          </div>
          <div>
            <span>问答调用</span>
            <strong>{{ usage.qaCalls }}</strong>
          </div>
          <div>
            <span>月度额度</span>
            <strong>{{ usage.quotaLimit }}</strong>
          </div>
        </div>
      </div>
    </section>

    <section class="panel recent-board">
      <div class="section-header">
        <div>
          <h3 class="section-heading">最近任务</h3>
          <p class="section-description">优先展示最近进入系统的视频，便于继续跟踪处理和问答。</p>
        </div>
        <el-button plain @click="router.push('/tasks')">查看全部</el-button>
      </div>

      <el-table v-loading="loading" :data="tasks">
        <el-table-column prop="videoTitle" label="视频标题" min-width="220" />
        <el-table-column prop="taskNo" label="任务编号" min-width="180" />
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <TaskStatusPill :status-code="row.statusCode" />
          </template>
        </el-table-column>
        <el-table-column prop="currentStep" label="当前步骤" min-width="160" />
        <el-table-column prop="progressPercent" label="进度" width="100">
          <template #default="{ row }">{{ row.progressPercent }}%</template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button link type="warning" @click="router.push(`/tasks/${row.id}`)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.dashboard {
  gap: 20px;
}

.overview,
.system-board,
.quota-board,
.recent-board {
  border-radius: 30px;
  padding: 24px;
}

.overview {
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 24px;
}

.overview-kicker {
  color: var(--accent);
  font-size: 12px;
  letter-spacing: 0.12em;
}

.overview-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 24px;
}

.overview-stats {
  display: grid;
  gap: 14px;
}

.stat {
  border-radius: 20px;
  padding: 18px;
}

.stat span,
.quota-list span {
  color: var(--muted);
  font-size: 13px;
}

.stat p {
  margin: 10px 0 0;
  color: var(--muted);
  line-height: 1.6;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.pipeline {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 20px;
}

.pipeline div {
  padding: 12px 16px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.03);
}

.quota-percent {
  color: var(--accent);
}

.quota-list {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.quota-list strong {
  display: block;
  margin-top: 8px;
  font-size: 24px;
}

@media (max-width: 1180px) {
  .overview,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .quota-list {
    grid-template-columns: 1fr;
  }
}
</style>
