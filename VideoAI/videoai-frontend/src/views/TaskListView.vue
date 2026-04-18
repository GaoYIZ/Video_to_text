<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { taskApi } from '../api/modules'
import type { TaskItem } from '../types'
import TaskStatusPill from '../components/TaskStatusPill.vue'

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

const statusOptions = [
  { label: '已上传', value: 1 },
  { label: '排队中', value: 2 },
  { label: '提取音频', value: 3 },
  { label: '语音转写', value: 4 },
  { label: '生成摘要', value: 5 },
  { label: '建立索引', value: 6 },
  { label: '处理完成', value: 7 },
  { label: '处理失败', value: 8 }
]

const failedCount = computed(() => tasks.value.filter((item) => item.statusCode === 'FAILED').length)
const successCount = computed(() => tasks.value.filter((item) => item.statusCode === 'SUCCESS').length)

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
  await ElMessageBox.confirm('删除后该任务的前端展示记录将无法恢复，确认继续吗？', '删除确认', {
    type: 'warning'
  })
  await taskApi.remove(id)
  await loadTasks()
}

onMounted(loadTasks)
</script>

<template>
  <div class="page-shell task-list">
    <section class="list-header panel fade-in-up">
      <div>
        <h1 class="page-title">视频解析任务列表</h1>
        <p class="page-copy">集中查看任务状态、失败重试和结果回看，适合演示完整的视频处理状态机。</p>
      </div>

      <div class="header-metrics">
        <div class="panel-muted">
          <span>当前页完成数</span>
          <strong>{{ successCount }}</strong>
        </div>
        <div class="panel-muted">
          <span>当前页失败数</span>
          <strong>{{ failedCount }}</strong>
        </div>
      </div>
    </section>

    <section class="toolbar panel">
      <div class="section-header">
        <div>
          <h3 class="section-heading">筛选条件</h3>
          <p class="section-description">支持按视频标题和任务状态进行筛选。</p>
        </div>
      </div>

      <div class="toolbar-actions">
        <el-input v-model="filters.keyword" placeholder="搜索视频标题" clearable @change="loadTasks" />
        <el-select v-model="filters.status" clearable placeholder="选择任务状态" @change="loadTasks">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="warning" @click="loadTasks">刷新列表</el-button>
      </div>
    </section>

    <section class="panel table-wrap">
      <el-table v-loading="loading" :data="tasks">
        <el-table-column prop="taskNo" label="任务编号" min-width="180" />
        <el-table-column prop="videoTitle" label="视频标题" min-width="220" />
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <TaskStatusPill :status-code="row.statusCode" />
          </template>
        </el-table-column>
        <el-table-column prop="currentStep" label="当前步骤" min-width="160" />
        <el-table-column prop="progressPercent" label="进度" width="100">
          <template #default="{ row }">{{ row.progressPercent }}%</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="220" fixed="right">
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
  gap: 20px;
}

.list-header,
.toolbar,
.table-wrap {
  border-radius: 30px;
  padding: 24px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: stretch;
}

.header-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(140px, 1fr));
  gap: 12px;
}

.header-metrics > div {
  border-radius: 18px;
  padding: 16px;
}

.header-metrics span {
  color: var(--muted);
  font-size: 13px;
}

.header-metrics strong {
  display: block;
  margin-top: 8px;
  font-size: 28px;
}

.toolbar-actions {
  display: grid;
  grid-template-columns: 1fr 220px auto;
  gap: 12px;
  margin-top: 20px;
}

.pager {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1100px) {
  .list-header,
  .toolbar-actions {
    grid-template-columns: 1fr;
  }

  .list-header {
    display: grid;
  }
}

@media (max-width: 760px) {
  .header-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
