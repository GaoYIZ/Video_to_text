<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { workbenchApi } from '../api/modules'
import type { WorkbenchMonitor } from '../types'
import TaskStatusPill from '../components/TaskStatusPill.vue'
import { formatDateTime } from '../utils/format'

const loading = ref(false)
const monitor = ref<WorkbenchMonitor | null>(null)
const activeStats = ref<'daily' | 'monthly'>('daily')

const currentStats = computed(() => (activeStats.value === 'daily' ? monitor.value?.dailyStats : monitor.value?.monthlyStats))
const distributionEntries = computed(() => Object.entries(monitor.value?.statusDistribution || {}))

const loadData = async () => {
  loading.value = true
  try {
    monitor.value = await workbenchApi.monitor()
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-shell monitor-page" v-loading="loading">
    <section class="monitor-hero panel fade-in-up">
      <div>
        <div class="mono hero-kicker">OBSERVABILITY · COST · QUOTA</div>
        <h1 class="page-title">任务监控与 AI 成本治理面板</h1>
        <p class="page-copy">
          这页恢复的是删掉前那套“可讲面试亮点”的监控视图：既看任务状态和事件，也看 Token、
          缓存命中率、成功率和配额使用。
        </p>
      </div>
    </section>

    <section class="overview-grid">
      <div class="panel overview-card">
        <span>任务总数</span>
        <strong>{{ monitor?.totalTasks || 0 }}</strong>
      </div>
      <div class="panel overview-card">
        <span>处理中</span>
        <strong>{{ monitor?.processingTasks || 0 }}</strong>
      </div>
      <div class="panel overview-card">
        <span>成功任务</span>
        <strong>{{ monitor?.successTasks || 0 }}</strong>
      </div>
      <div class="panel overview-card">
        <span>失败任务</span>
        <strong>{{ monitor?.failedTasks || 0 }}</strong>
      </div>
      <div class="panel overview-card">
        <span>今日剩余配额</span>
        <strong>{{ monitor?.quota?.remainingToday || 0 }}</strong>
      </div>
      <div class="panel overview-card">
        <span>本月剩余配额</span>
        <strong>{{ monitor?.quota?.remainingMonth || 0 }}</strong>
      </div>
    </section>

    <section class="monitor-grid">
      <div class="panel stats-panel">
        <div class="section-header">
          <div>
            <h3 class="section-heading">调用统计</h3>
            <p class="section-description">按日或按月查看 Token、费用估算、缓存命中率和成功率。</p>
          </div>

          <div class="tab-row">
            <button :class="{ active: activeStats === 'daily' }" @click="activeStats = 'daily'">今日</button>
            <button :class="{ active: activeStats === 'monthly' }" @click="activeStats = 'monthly'">本月</button>
          </div>
        </div>

        <div v-if="currentStats" class="stats-grid">
          <div class="panel-muted stat-card">
            <span>调用次数</span>
            <strong>{{ currentStats.totalCalls }}</strong>
          </div>
          <div class="panel-muted stat-card">
            <span>总 Token</span>
            <strong>{{ currentStats.totalTokens }}</strong>
          </div>
          <div class="panel-muted stat-card">
            <span>费用估算</span>
            <strong>¥{{ currentStats.estimatedCost.toFixed(4) }}</strong>
          </div>
          <div class="panel-muted stat-card">
            <span>平均耗时</span>
            <strong>{{ currentStats.avgResponseTimeMs }} ms</strong>
          </div>
          <div class="panel-muted stat-card">
            <span>缓存命中率</span>
            <strong>{{ currentStats.cacheHitRate.toFixed(2) }}%</strong>
          </div>
          <div class="panel-muted stat-card">
            <span>成功率</span>
            <strong>{{ currentStats.successRate.toFixed(2) }}%</strong>
          </div>
        </div>
      </div>

      <div class="panel distribution-panel">
        <div class="section-header">
          <div>
            <h3 class="section-heading">状态分布</h3>
            <p class="section-description">当前样本任务在不同状态节点上的分布情况。</p>
          </div>
        </div>

        <div class="distribution-list">
          <div v-for="[statusCode, count] in distributionEntries" :key="statusCode" class="distribution-item panel-muted">
            <TaskStatusPill :status-code="statusCode" />
            <strong>{{ count }}</strong>
          </div>
        </div>
      </div>
    </section>

    <section class="monitor-grid">
      <div class="panel recent-task-panel">
        <div class="section-header">
          <div>
            <h3 class="section-heading">最近任务</h3>
            <p class="section-description">快速回看最新视频任务，判断当前工作台是否处于稳定状态。</p>
          </div>
        </div>

        <div class="recent-task-list">
          <div v-for="task in monitor?.recentTasks || []" :key="task.id" class="recent-task-item panel-muted">
            <div class="recent-task-top">
              <strong>{{ task.videoTitle }}</strong>
              <TaskStatusPill :status-code="task.statusCode" />
            </div>
            <p>{{ task.taskNo }}</p>
            <span>{{ task.currentStep }} · {{ task.progressPercent }}%</span>
          </div>
        </div>
      </div>

      <div class="panel event-panel">
        <div class="section-header">
          <div>
            <h3 class="section-heading">最近事件</h3>
            <p class="section-description">用时间线回放状态流转，帮助定位失败节点与耗时瓶颈。</p>
          </div>
        </div>

        <div class="event-list">
          <div v-for="event in monitor?.recentEvents || []" :key="`${event.taskId}-${event.createTime}-${event.step}`" class="event-item">
            <div class="mono event-time">{{ formatDateTime(event.createTime) }}</div>
            <div>
              <strong>{{ event.videoTitle }}</strong>
              <p>{{ event.detail }}</p>
              <span>{{ event.taskNo }} · {{ event.step }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.monitor-page {
  gap: 20px;
}

.monitor-hero,
.overview-card,
.stats-panel,
.distribution-panel,
.recent-task-panel,
.event-panel {
  border-radius: 30px;
  padding: 24px;
}

.hero-kicker {
  color: var(--accent-strong);
  font-size: 12px;
  letter-spacing: 0.12em;
}

.overview-grid,
.monitor-grid,
.stats-grid {
  display: grid;
  gap: 20px;
}

.overview-grid {
  grid-template-columns: repeat(3, 1fr);
}

.monitor-grid {
  grid-template-columns: 1fr 1fr;
}

.overview-card span,
.stat-card span {
  color: var(--muted);
  font-size: 13px;
}

.overview-card strong,
.stat-card strong {
  display: block;
  margin-top: 8px;
  font-size: 28px;
}

.tab-row {
  display: flex;
  gap: 12px;
}

.tab-row button {
  padding: 10px 18px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.76);
  cursor: pointer;
}

.tab-row button.active {
  color: var(--accent-strong);
  border-color: rgba(111, 168, 255, 0.3);
}

.stats-grid {
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  margin-top: 18px;
}

.stat-card,
.distribution-item,
.recent-task-item {
  border-radius: 18px;
  padding: 16px;
}

.distribution-list,
.recent-task-list,
.event-list {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}

.distribution-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.recent-task-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.recent-task-item p,
.recent-task-item span,
.event-item p,
.event-item span {
  margin: 8px 0 0;
  color: var(--muted);
}

.event-item {
  display: grid;
  grid-template-columns: 170px 1fr;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);
}

.event-item:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.event-time {
  color: var(--muted);
  font-size: 12px;
}

@media (max-width: 1180px) {
  .overview-grid,
  .monitor-grid,
  .event-item {
    grid-template-columns: 1fr;
  }
}
</style>
