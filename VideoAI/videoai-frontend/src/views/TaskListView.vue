<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { taskApi } from '../api/modules'
import type { TaskItem } from '../types'
import TaskStatusPill from '../components/TaskStatusPill.vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const tasks = ref<TaskItem[]>([])
const total = ref(0)
const filters = reactive({
  current: 1,
  pageSize: 10,
  keyword: '',
  status: undefined as number | undefined
})

const loadTasks = async () => {
  loading.value = true
  try {
    const result = await taskApi.page(filters)
    tasks.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

const removeTask = async (id: number) => {
  await ElMessageBox.confirm('删除后任务记录将不可恢复，确认继续？', '删除确认', { type: 'warning' })
  await taskApi.remove(id)
  await loadTasks()
}

onMounted(loadTasks)
</script>

<template>
  <div class="page-shell task-list">
    <section class="toolbar panel">
      <div>
        <h3>任务检索</h3>
        <p>按状态和关键词筛选视频解析任务。</p>
      </div>
      <div class="toolbar-actions">
        <el-input v-model="filters.keyword" placeholder="搜索视频标题" clearable @change="loadTasks" />
        <el-select v-model="filters.status" clearable placeholder="任务状态" @change="loadTasks">
          <el-option label="UPLOADED" :value="1" />
          <el-option label="QUEUED" :value="2" />
          <el-option label="PROCESSING_AUDIO" :value="3" />
          <el-option label="TRANSCRIBING" :value="4" />
          <el-option label="SUMMARIZING" :value="5" />
          <el-option label="INDEXING" :value="6" />
          <el-option label="SUCCESS" :value="7" />
          <el-option label="FAILED" :value="8" />
        </el-select>
      </div>
    </section>

    <section class="panel table-wrap">
      <el-table v-loading="loading" :data="tasks">
        <el-table-column prop="taskNo" label="任务编号" width="220" />
        <el-table-column prop="videoTitle" label="视频名称" min-width="260" />
        <el-table-column label="状态" width="150">
          <template #default="{ row }">
            <TaskStatusPill :status-code="row.statusCode" />
          </template>
        </el-table-column>
        <el-table-column prop="currentStep" label="当前步骤" width="180" />
        <el-table-column prop="progressPercent" label="进度" width="120" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="warning" @click="router.push(`/tasks/${row.id}`)">详情</el-button>
            <el-button link type="primary" @click="taskApi.retry(row.id).then(loadTasks)">重试</el-button>
            <el-button link type="danger" @click="removeTask(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :current-page="filters.current"
          :page-size="filters.pageSize"
          :total="total"
          @current-change="(page) => { filters.current = page; loadTasks() }"
        />
      </div>
    </section>
  </div>
</template>

<style scoped>
.task-list {
  display: grid;
  gap: 20px;
}

.toolbar,
.table-wrap {
  border-radius: 28px;
  padding: 24px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
}

.toolbar h3 {
  margin: 0 0 8px;
  font-size: 24px;
}

.toolbar p {
  margin: 0;
  color: var(--muted);
}

.toolbar-actions {
  display: grid;
  grid-template-columns: 220px 180px;
  gap: 12px;
}

.pager {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .toolbar,
  .toolbar-actions {
    grid-template-columns: 1fr;
  }

  .toolbar {
    display: grid;
  }
}
</style>
