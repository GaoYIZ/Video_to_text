<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Lightning, TrendCharts, Cpu } from '@element-plus/icons-vue'
import { workbenchApi } from '../api/modules'
import type { WorkbenchMonitor } from '../types'

const loading = ref(false)
const monitor = ref<WorkbenchMonitor | null>(null)

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
  <div class="monitor-container">
    <!-- Stats Cards -->
    <div class="stats-grid">
      <!-- Token Usage Card -->
      <div class="stat-card dark-card">
        <div class="card-glow"></div>
        <p class="card-label">Chat AI Token 消耗 (今日)</p>
        <div class="card-metrics">
          <h3 class="metric-value">{{ monitor?.dailyStats?.totalTokens.toLocaleString() || '0' }}</h3>
          <div class="metric-badge success">
            <el-icon :size="16"><Lightning /></el-icon>
            <span>99%</span>
          </div>
        </div>
        <div class="card-details">
          <div>提示词: {{ monitor?.dailyStats?.totalInputTokens.toLocaleString() || '0' }}</div>
          <div>回复词: {{ monitor?.dailyStats?.totalOutputTokens.toLocaleString() || '0' }}</div>
          <div>峰值: 12k/s</div>
        </div>
      </div>

      <!-- Conversion Stats Card -->
      <div class="stat-card light-card">
        <p class="card-label">模型转换调用 (Requests)</p>
        <h3 class="metric-value dark">{{ monitor?.totalTasks.toLocaleString() || '0' }}</h3>
        
        <div class="success-rate">
          <div class="rate-header">
            <span>ASR 识别成功率</span>
            <span class="rate-value">99.9%</span>
          </div>
          <el-progress 
            :percentage="99.9" 
            color="#6366f1" 
            :stroke-width="8"
            :show-text="false"
          />
        </div>
      </div>
    </div>

    <!-- Task Status Distribution -->
    <div v-if="monitor?.statusDistribution" class="panel status-panel">
      <h2 class="section-title">任务状态分布</h2>
      <div class="status-grid">
        <div 
          v-for="(count, status) in monitor.statusDistribution" 
          :key="status"
          class="status-item"
        >
          <span class="status-label">{{ status }}</span>
          <strong class="status-count">{{ count }}</strong>
        </div>
      </div>
    </div>

    <!-- Recent Events Log -->
    <div class="panel log-panel">
      <h2 class="section-title log-title">System Log Activity</h2>
      <div class="log-entries">
        <div 
          v-for="(event, idx) in monitor?.recentEvents?.slice(0, 10)" 
          :key="idx"
          class="log-entry"
        >
          <span class="log-time">[{{ new Date(event.createTime).toLocaleTimeString('zh-CN') }}]</span>
          <span :class="['log-type', event.success ? 'success' : 'error']">
            {{ event.success ? 'SUCCESS' : 'ERROR' }}
          </span>
          <span class="log-detail">{{ event.detail }}</span>
        </div>
        
        <div v-if="!monitor?.recentEvents || monitor.recentEvents.length === 0" class="empty-log">
          暂无日志记录
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.monitor-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 32px;
  animation: fadeIn 0.7s ease-out;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 32px;
}

.stat-card {
  border-radius: 40px;
  padding: 40px;
  position: relative;
  overflow: hidden;
}

.dark-card {
  background: #0f172a;
  color: white;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.3);
}

.card-glow {
  position: absolute;
  top: 0;
  right: 0;
  width: 128px;
  height: 128px;
  background: rgba(99, 102, 241, 0.2);
  filter: blur(60px);
  pointer-events: none;
}

.light-card {
  background: white;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.card-label {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  margin: 0 0 16px 0;
  opacity: 0.7;
}

.dark-card .card-label {
  color: #a5b4fc;
}

.light-card .card-label {
  color: #94a3b8;
}

.card-metrics {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 24px;
}

.metric-value {
  font-size: 48px;
  font-weight: 900;
  letter-spacing: -0.05em;
  margin: 0;
}

.metric-value.dark {
  color: #0f172a;
}

.metric-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.metric-badge.success {
  background: rgba(34, 197, 94, 0.2);
  color: #22c55e;
}

.card-details {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  font-size: 11px;
  font-family: 'Courier New', monospace;
  opacity: 0.6;
}

.success-rate {
  margin-top: 32px;
}

.rate-header {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #64748b;
}

.rate-value {
  color: #4f46e5;
}

/* Status Panel */
.panel {
  background: white;
  border-radius: 30px;
  padding: 32px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 24px 0;
}

.log-title {
  color: #818cf8;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  font-size: 14px;
  margin-bottom: 16px;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.status-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
}

.status-label {
  font-size: 13px;
  color: #64748b;
}

.status-count {
  font-size: 28px;
  color: #1e293b;
}

/* Log Panel */
.log-panel {
  background: #0f172a;
  color: rgba(255, 255, 255, 0.5);
  font-family: 'Courier New', monospace;
  font-size: 11px;
}

.log-entries {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.log-entry {
  display: flex;
  gap: 12px;
  align-items: center;
}

.log-time {
  color: #64748b;
  flex-shrink: 0;
}

.log-type {
  font-weight: 700;
  flex-shrink: 0;
}

.log-type.success {
  color: #22c55e;
}

.log-type.error {
  color: #ef4444;
}

.log-detail {
  color: rgba(255, 255, 255, 0.7);
}

.empty-log {
  text-align: center;
  padding: 40px;
  color: rgba(255, 255, 255, 0.3);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .metric-value {
    font-size: 36px;
  }
  
  .card-details {
    grid-template-columns: 1fr;
  }
}
</style>
