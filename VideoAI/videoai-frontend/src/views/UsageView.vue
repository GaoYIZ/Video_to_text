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
    <section class="panel summary-board">
      <div>
        <div class="mono">COST GOVERNANCE</div>
        <h1>AI 使用统计与成本控制</h1>
        <p>这里展示配额、调用量、Token、费用估算、响应时间、缓存命中率和成功率，方便你从工程和 AI 成本两个维度讲项目。</p>
      </div>
    </section>

    <section v-if="quota" class="panel quota-section">
      <div class="section-head">
        <div>
          <h3>用户配额状态</h3>
          <p>默认提供每日 100 次、每月 3000 次 AI 调用额度，可在数据库中按用户调整。</p>
        </div>
      </div>

      <div class="quota-grid">
        <div class="quota-item">
          <span>今日已用 / 限额</span>
          <strong>{{ quota.todayUsed }} / {{ quota.dailyLimit }}</strong>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: `${percentage(quota.todayUsed, quota.dailyLimit)}%` }" />
          </div>
        </div>

        <div class="quota-item">
          <span>本月已用 / 限额</span>
          <strong>{{ quota.monthUsed }} / {{ quota.monthlyLimit }}</strong>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: `${percentage(quota.monthUsed, quota.monthlyLimit)}%` }" />
          </div>
        </div>

        <div class="quota-item">
          <span>今日剩余</span>
          <strong :class="{ warning: quota.remainingToday < 10 }">{{ quota.remainingToday }}</strong>
        </div>

        <div class="quota-item">
          <span>本月剩余</span>
          <strong :class="{ warning: quota.remainingMonth < 100 }">{{ quota.remainingMonth }}</strong>
        </div>
      </div>
    </section>

    <section class="usage-grid">
      <div class="panel usage-item">
        <span>总调用次数</span>
        <strong>{{ usage.totalCalls }}</strong>
      </div>
      <div class="panel usage-item">
        <span>总 Token</span>
        <strong>{{ usage.totalTokens }}</strong>
      </div>
      <div class="panel usage-item">
        <span>Summary Calls</span>
        <strong>{{ usage.summaryCalls }}</strong>
      </div>
      <div class="panel usage-item">
        <span>QA Calls</span>
        <strong>{{ usage.qaCalls }}</strong>
      </div>
      <div class="panel usage-item">
        <span>任务总数</span>
        <strong>{{ usage.taskCount }}</strong>
      </div>
      <div class="panel usage-item">
        <span>月度配额</span>
        <strong>{{ usage.quotaLimit }}</strong>
      </div>
    </section>

    <section v-if="currentStats" class="panel detailed-stats">
      <div class="section-head">
        <div>
          <h3>详细用量统计</h3>
          <p>支持按日和按月查看业务分布、模型分布和成本估算。</p>
        </div>

        <div class="tab-buttons">
          <button :class="{ active: activeTab === 'daily' }" @click="activeTab = 'daily'">今日</button>
          <button :class="{ active: activeTab === 'monthly' }" @click="activeTab = 'monthly'">本月</button>
        </div>
      </div>

      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-label">调用次数</div>
          <div class="stat-value">{{ currentStats.totalCalls }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">输入 Token</div>
          <div class="stat-value">{{ currentStats.totalInputTokens }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">输出 Token</div>
          <div class="stat-value">{{ currentStats.totalOutputTokens }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">预估费用</div>
          <div class="stat-value price">¥{{ currentStats.estimatedCost.toFixed(4) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">平均响应时间</div>
          <div class="stat-value">{{ currentStats.avgResponseTimeMs }} ms</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">缓存命中率</div>
          <div class="stat-value">{{ currentStats.cacheHitRate.toFixed(2) }}%</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">成功率</div>
          <div class="stat-value">{{ currentStats.successRate.toFixed(2) }}%</div>
        </div>
      </div>

      <div v-if="currentStats.callsByBizType" class="breakdown-section">
        <h4>按业务类型分布</h4>
        <div class="breakdown-list">
          <div v-for="(count, type) in currentStats.callsByBizType" :key="type" class="breakdown-item">
            <span class="type-name">{{ type }}</span>
            <span class="type-count">{{ count }} 次</span>
          </div>
        </div>
      </div>

      <div v-if="currentStats.callsByModel" class="breakdown-section">
        <h4>按模型分布</h4>
        <div class="breakdown-list">
          <div v-for="(count, model) in currentStats.callsByModel" :key="model" class="breakdown-item">
            <span class="type-name">{{ model }}</span>
            <span class="type-count">{{ count }} 次</span>
          </div>
        </div>
      </div>
    </section>

    <section class="panel governance">
      <div class="section-head">
        <div>
          <h3>已落地的治理能力</h3>
          <p>这些策略共同支撑高并发、防刷和成本控制。</p>
        </div>
      </div>
      <ul>
        <li>Redis 令牌桶限流，控制用户级别访问频率。</li>
        <li>视频摘要缓存和同问题 QA 缓存，减少重复调用。</li>
        <li>重复任务拦截，避免对同一文件反复触发昂贵处理链路。</li>
        <li>AI usage 全量记录，支持按用户、业务类型、模型统计。</li>
        <li>每日与每月双层配额控制，防止单用户过度消耗资源。</li>
        <li>基于 Token 的费用估算，便于展示成本治理能力。</li>
      </ul>
    </section>
  </div>
</template>

<style scoped>
.usage-page {
  display: grid;
  gap: 20px;
}

.summary-board,
.usage-item,
.governance,
.quota-section,
.detailed-stats {
  border-radius: 28px;
  padding: 24px;
}

.summary-board h1 {
  margin: 12px 0;
  font-size: clamp(34px, 4vw, 60px);
  letter-spacing: -0.06em;
}

.summary-board p,
.usage-item span,
.section-head p {
  color: var(--muted);
}

.usage-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.usage-item strong {
  display: block;
  margin-top: 12px;
  font-size: 36px;
}

.quota-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-top: 16px;
}

.quota-item,
.stat-card {
  padding: 18px;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 16px;
}

.quota-item span,
.stat-label {
  display: block;
  font-size: 14px;
  color: var(--muted);
  margin-bottom: 8px;
}

.quota-item strong,
.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
}

.quota-item strong.warning {
  color: #ff7d7d;
}

.progress-bar {
  width: 100%;
  height: 8px;
  margin-top: 12px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 999px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #f6b655 0%, #ef8d3c 100%);
  transition: width 0.3s ease;
}

.tab-buttons {
  display: flex;
  gap: 12px;
}

.tab-buttons button {
  padding: 8px 20px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: transparent;
  color: var(--text);
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.24s ease;
}

.tab-buttons button.active {
  background: rgba(246, 182, 85, 0.14);
  color: var(--accent);
  border-color: rgba(246, 182, 85, 0.25);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.stat-value.price {
  color: #ffd27a;
}

.breakdown-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.breakdown-section h4 {
  margin: 0 0 16px;
  font-size: 18px;
}

.breakdown-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.breakdown-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 10px;
}

.type-count {
  color: var(--muted);
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

.governance ul {
  margin: 0;
  padding-left: 18px;
  line-height: 2;
  color: #dce2ec;
}

@media (max-width: 1100px) {
  .usage-grid,
  .quota-grid,
  .section-head {
    grid-template-columns: 1fr;
  }

  .section-head {
    display: grid;
  }
}
</style>
