<template>
  <div class="history-view">
    <el-page-header @back="$router.push('/')">
      <template #content>
        <span class="page-title">历史记录</span>
      </template>
    </el-page-header>

    <div class="content-wrapper">
      <el-card>
        <div v-if="loading" class="loading-box">
          <el-skeleton :rows="5" animated />
        </div>

        <el-empty v-else-if="historyList.length === 0" description="暂无历史记录">
          <el-button type="primary" @click="$router.push('/')">
            去处理视频
          </el-button>
        </el-empty>

        <div v-else class="history-list">
          <el-card 
            v-for="item in historyList" 
            :key="item.id"
            class="history-item"
            shadow="hover"
            @click="viewDetail(item.id)"
          >
            <div class="item-header">
              <h4 class="item-title">{{ item.originalName }}</h4>
              <el-tag :type="getStatusType(item.status)" size="small">
                {{ getStatusText(item.status) }}
              </el-tag>
            </div>
            
            <div class="item-info">
              <span class="info-text">
                <el-icon><Clock /></el-icon>
                {{ formatTime(item.createTime) }}
              </span>
              <span v-if="item.summary" class="info-text">
                <el-icon><Document /></el-icon>
                已生成总结
              </span>
            </div>

            <div class="item-actions">
              <el-button size="small" type="primary" text>
                查看详情
              </el-button>
            </div>
          </el-card>

          <!-- 分页 -->
          <div class="pagination-box">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHistoryList } from '../api/video'

const router = useRouter()
const loading = ref(false)
const historyList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const fetchHistory = async () => {
  loading.value = true
  try {
    const res = await getHistoryList(currentPage.value, pageSize.value)
    if (res.code === 200) {
      historyList.value = res.data.list || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取历史记录失败')
    }
  } catch (error) {
    console.error('获取历史记录失败:', error)
    ElMessage.error('网络错误,请重试')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchHistory()
}

const viewDetail = (id) => {
  router.push(`/result/${id}`)
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const getStatusType = (status) => {
  const types = {
    'UPLOADED': 'info',
    'CONVERTED': 'warning',
    'TRANSCRIBED': 'primary',
    'SUMMARIZED': 'success'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'UPLOADED': '已上传',
    'CONVERTED': '已转换',
    'TRANSCRIBED': '已转录',
    'SUMMARIZED': '已完成'
  }
  return texts[status] || status
}

onMounted(() => {
  fetchHistory()
})
</script>

<style scoped>
.history-view {
  min-height: 100vh;
  background: #f5f7fa;
}

.content-wrapper {
  max-width: 1000px;
  margin: 30px auto;
  padding: 0 20px;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
}

.loading-box {
  padding: 20px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.history-item {
  cursor: pointer;
  transition: all 0.3s ease;
}

.history-item:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.item-title {
  margin: 0;
  font-size: 16px;
  color: #303133;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 10px;
}

.item-info {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
}

.info-text {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #909399;
  font-size: 13px;
}

.item-actions {
  text-align: right;
}

.pagination-box {
  margin-top: 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .content-wrapper {
    margin: 15px auto;
    padding: 0 10px;
  }

  .item-title {
    font-size: 14px;
  }
}
</style>
