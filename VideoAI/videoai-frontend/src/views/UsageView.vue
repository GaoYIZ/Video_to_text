<script setup lang="ts">
import { onMounted, ref } from 'vue'
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

onMounted(async () => {
  usage.value = await usageApi.me()
  quota.value = await usageApi.quota()
  dailyStats.value = await usageApi.daily()
  monthlyStats.value = await usageApi.monthly()
})

const currentStats = () => activeTab.value === 'daily' ? dailyStats.value : monthlyStats.value
</script>

<template>
  <div class="page-shell usage-page">
    <section class="panel summary-board">
      <div>
        <div class="mono">COST GOVERNANCE</div>
        <h1>AI 使用统计与成本控制</h1>
        <p>这里展示 Token 总量、调用次数、任务规模以及用户配额，是“可观测 + 可治理”的一部分。</p>
      </div>
    </section>

    <!-- 配额信息 -->
    <section v-if="quota" class="panel quota-section">
      <div class="section-head">
        <h3>📊 用户配额状态</h3>
      </div>
      <div class="quota-grid">
        <div class="quota-item">
          <span>今日已用 / 限制</span>
          <strong>{{ quota.todayUsed }} / {{ quota.dailyLimit }}</strong>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: (quota.todayUsed / quota.dailyLimit * 100) + '%' }"></div>
          </div>
        </div>
        <div class="quota-item">
          <span>本月已用 / 限制</span>
          <strong>{{ quota.monthUsed }} / {{ quota.monthlyLimit }}</strong>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: (quota.monthUsed / quota.monthlyLimit * 100) + '%' }"></div>
          </div>
        </div>
        <div class="quota-item">
          <span>今日剩余</span>
          <strong :class="{ 'warning': quota.remainingToday < 10 }">{{ quota.remainingToday }}</strong>
        </div>
        <div class="quota-item">
          <span>本月剩余</span>
          <strong :class="{ 'warning': quota.remainingMonth < 100 }">{{ quota.remainingMonth }}</strong>
        </div>
      </div>
    </section>

    <!-- 基础统计 -->
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
        <span>任务数</span>
        <strong>{{ usage.taskCount }}</strong>
      </div>
      <div class="panel usage-item">
        <span>用户配额</span>
        <strong>{{ usage.quotaLimit }}</strong>
      </div>
    </section>

    <!-- 详细统计 -->
    <section v-if="currentStats()" class="panel detailed-stats">
      <div class="section-head">
        <h3>📈 详细使用统计</h3>
        <div class="tab-buttons">
          <button 
            :class="{ active: activeTab === 'daily' }" 
            @click="activeTab = 'daily'"
          >
            今日
          </button>
          <button 
            :class="{ active: activeTab === 'monthly' }" 
            @click="activeTab = 'monthly'"
          >
            本月
          </button>
        </div>
      </div>
      
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-label">调用次数</div>
          <div class="stat-value">{{ currentStats()?.totalCalls || 0 }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">输入 Token</div>
          <div class="stat-value">{{ currentStats()?.totalInputTokens || 0 }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">输出 Token</div>
          <div class="stat-value">{{ currentStats()?.totalOutputTokens || 0 }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">预估费用</div>
          <div class="stat-value price">¥{{ currentStats()?.estimatedCost?.toFixed(4) || '0.0000' }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">平均响应时间</div>
          <div class="stat-value">{{ currentStats()?.avgResponseTimeMs || 0 }} ms</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">缓存命中率</div>
          <div class="stat-value">{{ currentStats()?.cacheHitRate?.toFixed(2) || 0 }}%</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">成功率</div>
          <div class="stat-value">{{ currentStats()?.successRate?.toFixed(2) || 0 }}%</div>
        </div>
      </div>

      <!-- 按业务类型统计 -->
      <div v-if="currentStats()?.callsByBizType" class="breakdown-section">
        <h4>按业务类型分布</h4>
        <div class="breakdown-list">
          <div v-for="(count, type) in currentStats()?.callsByBizType" :key="type" class="breakdown-item">
            <span class="type-name">{{ type }}</span>
            <span class="type-count">{{ count }} 次</span>
          </div>
        </div>
      </div>

      <!-- 按模型统计 -->
      <div v-if="currentStats()?.callsByModel" class="breakdown-section">
        <h4>按模型分布</h4>
        <div class="breakdown-list">
          <div v-for="(count, model) in currentStats()?.callsByModel" :key="model" class="breakdown-item">
            <span class="type-name">{{ model }}</span>
            <span class="type-count">{{ count }} 次</span>
          </div>
        </div>
      </div>
    </section>

    <section class="panel governance">
      <div class="section-head">
        <div>
          <h3>✅ 已落地的成本治理能力</h3>
          <p>这些策略会在 AI 使用和高并发场景下共同发挥作用。</p>
        </div>
      </div>
      <ul>
        <li>✅ Redis 令牌桶限流，控制用户级别访问频率</li>
        <li>✅ 视频摘要缓存和同问题 QA 缓存，减少重复调用</li>
        <li>✅ 重复任务拦截，避免对同一文件反复触发昂贵处理链路</li>
        <li>✅ Token usage 全量记录，支持用户维度配额统计</li>
        <li>✨ 新增：每日/每月配额管理，防止过度使用</li>
        <li>✨ 新增：多维度使用统计(按天/月/业务类型/模型)</li>
        <li>✨ 新增：费用估算功能，实时掌握 AI 成本</li>
        <li>✨ 新增：缓存命中率监控，优化缓存策略</li>
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

/* 配额区域 */
.quota-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-top: 16px;
}

.quota-item {
  padding: 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
}

.quota-item span {
  display: block;
  font-size: 14px;
  color: var(--muted);
  margin-bottom: 8px;
}

.quota-item strong {
  display: block;
  font-size: 24px;
  margin-bottom: 8px;
}

.quota-item strong.warning {
  color: #ff6b6b;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s ease;
}

/* 详细统计 */
.tab-buttons {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.tab-buttons button {
  padding: 8px 20px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.tab-buttons button.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.stat-card {
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: var(--muted);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
}

.stat-value.price {
  color: #ffd700;
}

.breakdown-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
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
  border-radius: 8px;
}

.type-name {
  font-weight: 500;
}

.type-count {
  color: var(--muted);
  font-size: 14px;
}

.section-head {
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
  .usage-grid {
    grid-template-columns: 1fr;
  }
  
  .quota-grid {
    grid-template-columns: 1fr;
  }
}
</style>
