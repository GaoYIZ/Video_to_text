<template>
  <div class="history-page">
    <div class="page-header">
      <h2>历史记录</h2>
      <p>共 {{ total }} 个文件</p>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索文件名..."
        clearable
        style="width: 300px"
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select 
        v-model="statusFilter" 
        placeholder="状态筛选" 
        clearable
        style="width: 150px"
        @change="handleFilter"
      >
        <el-option label="全部" value="" />
        <el-option label="已完成" value="SUMMARIZED" />
        <el-option label="处理中" value="PROCESSING" />
        <el-option label="已上传" value="UPLOADED" />
      </el-select>

      <el-button type="danger" text @click="clearAll" :disabled="historyList.length === 0">
        <el-icon><Delete /></el-icon>
        清空全部
      </el-button>
    </div>

    <div v-loading="loading" class="history-list">
      <el-empty v-if="!loading && historyList.length === 0" description="暂无历史记录">
        <el-button type="primary" @click="$router.push('/')">
          去处理视频
        </el-button>
      </el-empty>

      <div v-else class="file-grid">
        <el-card 
          v-for="file in historyList" 
          :key="file.id"
          class="history-item"
          shadow="hover"
          @click="viewDetail(file.id)"
        >
          <div class="item-status" :class="file.status?.toLowerCase()">
            <div class="status-dot"></div>
          </div>

          <div class="item-content">
            <h4 class="item-title">{{ file.originalName }}</h4>
            
            <p class="item-summary">
              {{ file.summary ? file.summary.substring(0, 80) + '...' : '等待 AI 处理...' }}
            </p>

            <div class="item-meta">
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ formatTime(file.createTime) }}
              </span>
              <span class="meta-item" v-if="file.summary">
                <el-icon><Check /></el-icon>
                已完成
              </span>
            </div>
          </div>

          <div class="item-actions">
            <el-dropdown trigger="click" @click.stop>
              <el-button text size="small">
                <el-icon><More /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="viewDetail(file.id)">
                    <el-icon><View /></el-icon>
                    查看详情
                  </el-dropdown-item>
                  <el-dropdown-item @click="downloadFile(file)">
                    <el-icon><Download /></el-icon>
                    下载结果
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="deleteFile(file.id)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next, jumper"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getHistoryList } from '../api/video'

const router = useRouter()
const loading = ref(false)
const historyList = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const searchKeyword = ref('')
const statusFilter = ref('')

// 获取历史列表
const fetchHistory = async () => {
  loading.value = true
  try {
    const res = await getHistoryList(currentPage.value, pageSize.value)
    if (res.code === 200) {
      historyList.value = res.data.list || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取失败')
    }
  } catch (error) {
    console.error('获取失败:', error)
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = (id) => {
  router.push(`/result/${id}`)
}

// 下载文件
const downloadFile = (file) => {
  if (!file.transcript && !file.summary) {
    ElMessage.warning('暂无可下载内容')
    return
  }

  const content = `
标题: ${file.originalName}
时间: ${formatTime(file.createTime)}
状态: ${getStatusText(file.status)}

【转录文本】
${file.transcript || '无'}

【AI 总结】
${file.summary || '无'}
  `.trim()

  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${file.originalName}_转写结果.txt`
  a.click()
  URL.revokeObjectURL(url)

  ElMessage.success('下载成功')
}

// 删除文件
const deleteFile = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个文件吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // TODO: 调用删除接口
    ElMessage.success('删除成功')
    await fetchHistory()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 清空全部
const clearAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有历史记录吗?此操作不可恢复!', '警告', {
      confirmButtonText: '确定清空',
      cancelButtonText: '取消',
      type: 'error'
    })

    // TODO: 调用批量删除接口
    ElMessage.success('已清空')
    await fetchHistory()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空失败:', error)
    }
  }
}

// 搜索
const handleSearch = () => {
  // TODO: 实现搜索逻辑
  console.log('搜索:', searchKeyword.value)
}

// 筛选
const handleFilter = () => {
  // TODO: 实现筛选逻辑
  console.log('筛选:', statusFilter.value)
  fetchHistory()
}

// 分页
const handlePageChange = (page) => {
  currentPage.value = page
  fetchHistory()
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString('zh-CN')
}

const getStatusText = (status) => {
  const texts = {
    'UPLOADED': '已上传',
    'CONVERTED': '已转换',
    'SUMMARIZED': '已完成'
  }
  return texts[status] || status
}

onMounted(() => {
  fetchHistory()
})
</script>

<style scoped>
.history-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #262626;
  margin: 0 0 4px;
}

.page-header p {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  align-items: center;
}

.history-list {
  min-height: 400px;
}

.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.history-item {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 12px;
  position: relative;
  overflow: hidden;
}

.history-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}

.item-status {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 1;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #52c41a;
  box-shadow: 0 0 12px rgba(82, 196, 26, 0.6);
  animation: pulse 2s infinite;
}

.item-status.processing .status-dot {
  background: #ff4d4f;
  box-shadow: 0 0 12px rgba(255, 77, 79, 0.6);
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.2); }
}

.item-content {
  padding: 8px 0;
}

.item-title {
  font-size: 15px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-summary {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0 0 12px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 42px;
}

.item-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #bfbfbf;
}

.item-actions {
  position: absolute;
  top: 8px;
  right: 8px;
}

.pagination-wrapper {
  margin-top: 32px;
  text-align: center;
}

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-bar .el-input,
  .filter-bar .el-select {
    width: 100% !important;
  }

  .file-grid {
    grid-template-columns: 1fr;
  }
}
</style>
