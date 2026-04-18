<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { usageApi } from '../api/modules'
import type { UsageOverview, UserQuota, UsageStats } from '../types'

const usage = ref<UsageOverview>({
  totalCalls: 0,
  totalTokens: 0,
  summaryCalls: 0,
  qaCalls: 0,
  taskCount: 0,
  quotaLimit: 0
})

const quota = ref<UserQuota | null>(null)
const dailyStats = ref<UsageStats | null>(null)
const monthlyStats = ref<UsageStats | null>(null)
const activeTab = ref<'daily' | 'monthly'>('daily')

const currentStats = computed(() => (activeTab.value === 'daily' ? dailyStats.value : monthlyStats.value))

const loadData = async () => {
  const [overview, quotaData, daily, monthly] = await Promise.all([
    usageApi.me(),
    usageApi.quota(),
    usageApi.daily(),
    usageApi.monthly()
  ])
  usage.value = overview
  quota.value = quotaData
  dailyStats.value = daily
  monthlyStats.value = monthly
}

const percentage = (used: number, limit: number) => {
  if (!limit) return 0
  return Math.min(100, Math.round((used / limit) * 100))
}

onMounted(loadData)
</script>

<template>
  <div class="page-shell usage-page">
    <section class="usage-hero panel fade-in-up">
      <div>
        <div class="mono">COST GOVERNANCE</div>
        <h1 class="page-title">AI 调用配额、成本与缓存收益</h1>
        <p class="page-copy">
          这一页聚焦“模型调用是否可控”。你可以查看用户配额、Token 消耗、费用估算、缓存命中率与成功率，完整体现项目的成本治理能力。
        </p>
      </div>
    </section>

    <section v-if="quota" class="panel quota-section">
      <div class="section-header">
        <div>
          <h3 class="section-heading">用户配额</h3>
          <p class="section-description">默认支持每日与每月双层配额控制，避免单用户过度消耗模型资源。</p>
        </div>
      </div>

      <div class="quota-grid">
        <div class="quota-card panel-muted">
          <span>今日已用 / 限额</span>
          <strong>{{ quota.todayUsed }} / {{ quota.dailyLimit }}</strong>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: `${percentage(quota.todayUsed, quota.dailyLimit)}%` }" />
          </div>
        </div>
        <div class="quota-card panel-muted">
          <span>本月已用 / 限额</span>
          <strong>{{ quota.monthUsed }} / {{ quota.monthlyLimit }}</strong>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: `${percentage(quota.monthUsed, quota.monthlyLimit)}%` }" />
          </div>
        </div>
        <div class="quota-card panel-muted">
          <span>今日剩余</span>
          <strong>{{ quota.remainingToday }}</strong>
        </div>
        <div class="quota-card panel-muted">
          <span>本月剩余</span>
          <strong>{{ quota.remainingMonth }}</strong>
        </div>
      </div>
    </section>

    <section class="overview-grid">
      <div class="panel overview-card">
        <span>总调用次数</span>
        <strong>{{ usage.totalCalls }}</strong>
      </div>
      <div class="panel overview-card">
        <span>总 Token</span>
        <strong>{{ usage.totalTokens }}</strong>
      </div>
      <div class="panel overview-card">
        <span>摘要调用</span>
        <strong>{{ usage.summaryCalls }}</strong>
      </div>
      <div class="panel overview-card">
        <span>问答调用</span>
        <strong>{{ usage.qaCalls }}</strong>
      </div>
      <div class="panel overview-card">
        <span>任务总数</span>
        <strong>{{ usage.taskCount }}</strong>
      </div>
      <div class="panel overview-card">
        <span>月度配额</span>
        <strong>{{ usage.quotaLimit }}</strong>
      </div>
    </section>

    <section v-if="currentStats" class="panel stats-section">
      <div class="section-header">
        <div>
          <h3 class="section-heading">详细统计</h3>
          <p class="section-description">可以按日或按月查看业务调用分布、模型分布与费用估算。</p>
        </div>

        <div class="tab-buttons">
          <button :class="{ active: activeTab === 'daily' }" @click="activeTab = 'daily'">今日</button>
          <button :class="{ active: activeTab === 'monthly' }" @click="activeTab = 'monthly'">本月</button>
        </div>
      </div>

      <div class="stats-grid">
        <div class="stat-card panel-muted">
          <span>调用次数</span>
          <strong>{{ currentStats.totalCalls }}</strong>
        </div>
        <div class="stat-card panel-muted">
          <span>输入 Token</span>
          <strong>{{ currentStats.totalInputTokens }}</strong>
        </div>
        <div class="stat-card panel-muted">
          <span>输出 Token</span>
          <strong>{{ currentStats.totalOutputTokens }}</strong>
        </div>
        <div class="stat-card panel-muted">
          <span>费用估算</span>
          <strong>¥{{ currentStats.estimatedCost.toFixed(4) }}</strong>
        </div>
        <div class="stat-card panel-muted">
          <span>平均响应时间</span>
          <strong>{{ currentStats.avgResponseTimeMs }} ms</strong>
        </div>
        <div class="stat-card panel-muted">
          <span>缓存命中率</span>
          <strong>{{ currentStats.cacheHitRate.toFixed(2) }}%</strong>
        </div>
        <div class="stat-card panel-muted">
          <span>成功率</span>
          <strong>{{ currentStats.successRate.toFixed(2) }}%</strong>
        </div>
      </div>

      <div v-if="currentStats.callsByBizType" class="breakdown-grid">
        <div class="panel-muted breakdown-card">
          <h4>按业务类型分布</h4>
          <div class="breakdown-list">
            <div v-for="(count, type) in currentStats.callsByBizType" :key="type" class="breakdown-item">
              <span>{{ type }}</span>
              <strong>{{ count }}</strong>
            </div>
          </div>
        </div>

        <div v-if="currentStats.callsByModel" class="panel-muted breakdown-card">
          <h4>按模型分布</h4>
          <div class="breakdown-list">
            <div v-for="(count, model) in currentStats.callsByModel" :key="model" class="breakdown-item">
              <span>{{ model }}</span>
              <strong>{{ count }}</strong>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="panel governance-section">
      <div class="section-header">
        <div>
          <h3 class="section-heading">已落地的治理能力</h3>
          <p class="section-description">这些策略共同支持高并发、防刷和模型成本控制。</p>
        </div>
      </div>

      <div class="governance-list">
        <div class="panel-muted">
          <strong>配额控制</strong>
          <p>按用户维度限制每日和每月可用额度，防止资源被单点消耗。</p>
        </div>
        <div class="panel-muted">
          <strong>缓存复用</strong>
          <p>摘要缓存、问答缓存与常见问题复用共同降低重复调用。</p>
        </div>
        <div class="panel-muted">
          <strong>重复任务拦截</strong>
          <p>对相同文件和重复任务进行拦截，避免昂贵处理链路重复执行。</p>
        </div>
        <div class="panel-muted">
          <strong>Usage 统计</strong>
          <p>按业务类型、模型和时间维度记录 Token、耗时和成功率。</p>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.usage-page {
  gap: 20px;
}

.usage-hero,
.quota-section,
.stats-section,
.governance-section,
.overview-card {
  border-radius: 30px;
  padding: 24px;
}

.quota-grid,
.overview-grid,
.stats-grid,
.breakdown-grid,
.governance-list {
  display: grid;
  gap: 20px;
}

.quota-grid {
  grid-template-columns: repeat(2, 1fr);
  margin-top: 18px;
}

.quota-card,
.stat-card,
.breakdown-card,
.governance-list > div {
  border-radius: 18px;
  padding: 18px;
}

.quota-card span,
.overview-card span,
.stat-card span,
.breakdown-item span {
  color: var(--muted);
  font-size: 13px;
}

.quota-card strong,
.overview-card strong,
.stat-card strong {
  display: block;
  margin-top: 8px;
  font-size: 28px;
}

.overview-grid {
  grid-template-columns: repeat(3, 1fr);
}

.progress-bar {
  width: 100%;
  height: 8px;
  margin-top: 14px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 999px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ffb761, #ff8f57);
  transition: width 0.3s ease;
}

.tab-buttons {
  display: flex;
  gap: 12px;
}

.tab-buttons button {
  border: 1px solid var(--line);
  border-radius: 999px;
  background: transparent;
  color: var(--text-soft);
  padding: 10px 18px;
  cursor: pointer;
  transition:
    background 0.24s ease,
    border-color 0.24s ease,
    color 0.24s ease;
}

.tab-buttons button.active {
  background: rgba(255, 183, 97, 0.12);
  border-color: rgba(255, 183, 97, 0.28);
  color: var(--accent);
}

.stats-grid {
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  margin-top: 20px;
}

.breakdown-grid,
.governance-list {
  grid-template-columns: 1fr 1fr;
  margin-top: 20px;
}

.breakdown-card h4 {
  margin: 0 0 16px;
  font-size: 18px;
}

.breakdown-list {
  display: grid;
  gap: 12px;
}

.breakdown-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.governance-list strong {
  font-size: 17px;
}

.governance-list p {
  margin: 10px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

@media (max-width: 1100px) {
  .quota-grid,
  .overview-grid,
  .breakdown-grid,
  .governance-list {
    grid-template-columns: 1fr;
  }
}
</style>
