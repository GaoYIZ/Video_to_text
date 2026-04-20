<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Link, VideoPlay, Microphone } from '@element-plus/icons-vue'
import { workbenchApi } from '../api/modules'
import type { TaskItem, WorkbenchOverview } from '../types'
import TaskStatusPill from '../components/TaskStatusPill.vue'

const emit = defineEmits<{
  navigate: [tabId: string]
}>()

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
    console.log('开始加载首页数据...')
    overview.value = await workbenchApi.overview()
    console.log('首页数据加载成功:', overview.value)
  } catch (error) {
    console.error('首页数据加载失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

const handleNavigate = (type: string) => {
  emit('navigate', 'convert')
}
</script>

<template>
  <div class="home-container">
    <!-- Hero Section -->
    <section class="hero-section fade-in-up">
      <div class="hero-content">
        <h1 class="hero-title">欢迎回来!</h1>
        <p class="hero-subtitle">选择一种模式开始高效处理您的媒体资源。</p>
        
        <div class="card-grid">
          <div 
            class="feature-card" 
            @click="handleNavigate('link')"
          >
            <div class="card-icon blue">
              <el-icon :size="40"><Link /></el-icon>
            </div>
            <h3 class="card-title">链接转文字</h3>
          </div>

          <div 
            class="feature-card"
            @click="handleNavigate('video')"
          >
            <div class="card-icon indigo">
              <el-icon :size="40"><VideoPlay /></el-icon>
            </div>
            <h3 class="card-title">视频转文字</h3>
          </div>

          <div 
            class="feature-card"
            @click="handleNavigate('record')"
          >
            <div class="card-icon red">
              <el-icon :size="40"><Microphone /></el-icon>
            </div>
            <h3 class="card-title">实时录音</h3>
          </div>
        </div>
      </div>
    </section>

    <!-- Recent Tasks Section -->
    <section v-if="recentTasks.length > 0" class="recent-section panel">
      <div class="section-header">
        <h2 class="section-title">最近任务</h2>
        <el-button link @click="emit('navigate', 'tasks')">查看全部</el-button>
      </div>

      <el-table v-loading="loading" :data="recentTasks.slice(0, 5)" style="width: 100%">
        <el-table-column prop="videoTitle" label="视频标题" min-width="200" />
        <el-table-column prop="taskNo" label="任务编号" width="180" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <TaskStatusPill :status-code="row.statusCode" />
          </template>
        </el-table-column>
        <el-table-column prop="progressPercent" label="进度" width="100">
          <template #default="{ row }">{{ row.progressPercent }}%</template>
        </el-table-column>
      </el-table>
    </section>

    <!-- Quota Section -->
    <section v-if="quota" class="quota-section panel">
      <div class="quota-header">
        <h2 class="section-title">今日配额</h2>
        <span class="quota-percent">{{ todayPercent }}%</span>
      </div>
      
      <el-progress 
        :percentage="todayPercent" 
        color="#6366f1" 
        :stroke-width="10"
      />

      <div class="quota-stats">
        <div class="stat-item">
          <span>今日已用</span>
          <strong>{{ quota.todayUsed }}</strong>
        </div>
        <div class="stat-item">
          <span>今日剩余</span>
          <strong>{{ quota.remainingToday }}</strong>
        </div>
        <div class="stat-item">
          <span>月度限额</span>
          <strong>{{ quota.monthlyLimit }}</strong>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.hero-section {
  animation: fadeInUp 0.7s ease-out;
}

.hero-title {
  font-size: 36px;
  font-weight: 900;
  color: #0f172a;
  margin: 0 0 8px 0;
}

.hero-subtitle {
  font-size: 18px;
  color: #94a3b8;
  margin: 0 0 40px 0;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 32px;
}

.feature-card {
  background: white;
  padding: 40px;
  border-radius: 40px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.card-icon {
  width: 80px;
  height: 80px;
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  transition: transform 0.3s ease;
}

.feature-card:hover .card-icon {
  transform: rotate(6deg);
}

.card-icon.blue {
  background: #eff6ff;
  color: #2563eb;
}

.card-icon.indigo {
  background: #eef2ff;
  color: #4f46e5;
}

.card-icon.red {
  background: #fef2f2;
  color: #dc2626;
}

.card-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.panel {
  background: white;
  padding: 24px;
  border-radius: 30px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.quota-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.quota-percent {
  color: #6366f1;
  font-weight: 700;
  font-size: 14px;
}

.quota-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-item span {
  color: #94a3b8;
  font-size: 13px;
}

.stat-item strong {
  font-size: 24px;
  color: #1e293b;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-in-up {
  animation: fadeInUp 0.7s ease-out;
}

@media (max-width: 768px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
  
  .quota-stats {
    grid-template-columns: 1fr;
  }
  
  .hero-title {
    font-size: 28px;
  }
}
</style>
