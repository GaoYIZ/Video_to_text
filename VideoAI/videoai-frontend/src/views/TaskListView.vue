<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { VideoPlay, Headset } from '@element-plus/icons-vue'
import { taskApi } from '../api/modules'
import type { TaskItem } from '../types'
import TaskStatusPill from '../components/TaskStatusPill.vue'

const loading = ref(false)
const tasks = ref<TaskItem[]>([])

const loadTasks = async () => {
  loading.value = true
  try {
    const result = await taskApi.page({ current: 1, pageSize: 50 })
    tasks.value = result.records
  } finally {
    loading.value = false
  }
}

onMounted(loadTasks)
</script>

<template>
  <div class="tasks-container">
    <div class="tasks-header">
      <h1 class="page-title">历史转录记录</h1>
    </div>

    <div class="panel">
      <el-table 
        v-loading="loading" 
        :data="tasks"
        style="width: 100%"
      >
        <el-table-column label="来源" min-width="220">
          <template #default="{ row }">
            <div class="task-source">
              <el-icon :size="16" class="text-indigo-500">
                <component :is="row.videoTitle.endsWith('.mp3') ? Headset : VideoPlay" />
              </el-icon>
              <span>{{ row.videoTitle }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="taskNo" label="任务编号" width="180" />
        
        <el-table-column label="时长" width="120">
          <template #default="{ row }">
            <span class="mono">{{ row.finishedAt ? '已完成' : '处理中' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="日期" width="140">
          <template #default="{ row }">
            {{ new Date(row.createTime).toLocaleDateString('zh-CN') }}
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <TaskStatusPill :status-code="row.statusCode" />
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary">回看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="tasks.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无任务记录" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.tasks-container {
  max-width: 1200px;
  margin: 0 auto;
}

.tasks-header {
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  color: #1e293b;
  margin: 0;
}

.panel {
  background: white;
  border-radius: 24px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.task-source {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 700;
  color: #1e293b;
}

.mono {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: #64748b;
}

.empty-state {
  padding: 60px 20px;
}

:deep(.el-table) {
  --el-table-border-color: #f1f5f9;
  --el-table-header-bg-color: #f8fafc;
}

:deep(.el-table th) {
  font-weight: 700;
  color: #94a3b8;
  text-transform: uppercase;
  font-size: 12px;
  letter-spacing: 0.05em;
}

:deep(.el-table td) {
  padding: 20px 0;
}

:deep(.el-table tr:hover) {
  background: #f8fafc;
}

@media (max-width: 768px) {
  .page-title {
    font-size: 24px;
  }
}
</style>
