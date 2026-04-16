<template>
  <div class="result-view" v-loading="loading">
    <el-page-header @back="goBack">
      <template #content>
        <span class="page-title">处理结果</span>
      </template>
    </el-page-header>

    <div class="content-wrapper" v-if="videoInfo">
      <!-- 基本信息 -->
      <el-card class="info-card">
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="文件名">
            {{ videoInfo.originalName }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(videoInfo.status)">
              {{ getStatusText(videoInfo.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatTime(videoInfo.createTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- AI 总结 -->
      <el-card v-if="videoInfo.summary" class="summary-card">
        <template #header>
          <div class="card-header">
            <el-icon><MagicStick /></el-icon>
            <span>AI 智能总结</span>
          </div>
        </template>
        <div class="summary-content" v-html="formatSummary(videoInfo.summary)"></div>
      </el-card>

      <!-- 转录文本 -->
      <el-card v-if="videoInfo.transcript" class="transcript-card">
        <template #header>
          <div class="card-header">
            <el-icon><Document /></el-icon>
            <span>完整转录文本</span>
            <el-button 
              size="small" 
              @click="copyTranscript"
              style="margin-left: auto"
            >
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
          </div>
        </template>
        <el-input
          type="textarea"
          :rows="15"
          :model-value="videoInfo.transcript"
          readonly
          class="transcript-box"
        />
      </el-card>

      <!-- 空状态 -->
      <el-empty 
        v-if="!videoInfo.transcript && !videoInfo.summary"
        description="暂无处理结果"
      >
        <el-button type="primary" @click="reprocess">
          重新处理
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getVideoInfo, convertToAudio, processWithAi } from '../api/video'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const videoInfo = ref(null)

const fetchVideoInfo = async () => {
  const videoId = route.params.id
  if (!videoId) {
    ElMessage.error('无效的视频 ID')
    return
  }

  loading.value = true
  try {
    const res = await getVideoInfo(videoId)
    if (res.code === 200) {
      videoInfo.value = res.data
    } else {
      ElMessage.error(res.message || '获取视频信息失败')
    }
  } catch (error) {
    console.error('获取视频信息失败:', error)
    ElMessage.error('网络错误,请重试')
  } finally {
    loading.value = false
  }
}

const reprocess = async () => {
  if (!videoInfo.value) return

  loading.value = true
  try {
    if (!videoInfo.value.audioPath) {
      await convertToAudio(videoInfo.value.id)
    }
    
    await processWithAi(videoInfo.value.id)
    
    ElMessage.success('重新处理完成')
    await fetchVideoInfo()
  } catch (error) {
    console.error('重新处理失败:', error)
    ElMessage.error(error.message || '处理失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/')
}

const copyTranscript = () => {
  if (videoInfo.value?.transcript) {
    navigator.clipboard.writeText(videoInfo.value.transcript)
    ElMessage.success('已复制到剪贴板')
  }
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

const formatSummary = (text) => {
  if (!text) return ''
  
  let formatted = text.replace(/\n/g, '<br>')
  formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  
  return formatted
}

onMounted(() => {
  fetchVideoInfo()
})
</script>

<style scoped>
.result-view {
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

.info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.summary-card {
  margin-bottom: 20px;
}

.summary-content {
  padding: 20px;
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  border-radius: 8px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

.summary-content :deep(strong) {
  color: #409EFF;
}

.transcript-card {
  margin-bottom: 20px;
}

.transcript-box {
  font-family: 'Courier New', monospace;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .content-wrapper {
    margin: 15px auto;
    padding: 0 10px;
  }

  .summary-content {
    padding: 15px;
  }
}
</style>
