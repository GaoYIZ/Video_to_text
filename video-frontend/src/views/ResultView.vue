<template>
  <div class="result-container">
    <el-card class="result-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-button @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <span>视频总结报告</span>
        </div>
      </template>

      <div v-if="videoInfo" class="content">
        <!-- 基本信息 -->
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

        <!-- 转录文本 -->
        <div v-if="videoInfo.transcript" class="section">
          <h3>
            <el-icon><Document /></el-icon>
            完整转录文本
          </h3>
          <el-input
            type="textarea"
            :rows="10"
            :model-value="videoInfo.transcript"
            readonly
            class="transcript-box"
          />
        </div>

        <!-- AI 总结 -->
        <div v-if="videoInfo.summary" class="section">
          <h3>
            <el-icon><MagicStick /></el-icon>
            AI 智能总结
          </h3>
          <div class="summary-content" v-html="formatSummary(videoInfo.summary)"></div>
        </div>

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
    </el-card>
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

// 获取视频信息
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

// 重新处理
const reprocess = async () => {
  if (!videoInfo.value) return

  loading.value = true
  try {
    // 如果没有音频,先转换
    if (!videoInfo.value.audioPath) {
      await convertToAudio(videoInfo.value.id)
    }
    
    // AI 处理
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

// 返回
const goBack = () => {
  router.push('/')
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    'UPLOADED': 'info',
    'CONVERTED': 'warning',
    'TRANSCRIBED': 'primary',
    'SUMMARIZED': 'success'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    'UPLOADED': '已上传',
    'CONVERTED': '已转换',
    'TRANSCRIBED': '已转录',
    'SUMMARIZED': '已完成'
  }
  return texts[status] || status
}

// 格式化总结内容(简单处理 Markdown)
const formatSummary = (text) => {
  if (!text) return ''
  
  // 将换行转换为 <br>
  let formatted = text.replace(/\n/g, '<br>')
  
  // 加粗文本 **text** -> <strong>text</strong>
  formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  
  return formatted
}

onMounted(() => {
  fetchVideoInfo()
})
</script>

<style scoped>
.result-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.result-card {
  max-width: 1000px;
  margin: 0 auto;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 18px;
  font-weight: bold;
}

.content {
  padding: 10px;
}

.section {
  margin-top: 30px;
}

.section h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
  margin-bottom: 15px;
  font-size: 16px;
}

.transcript-box {
  font-family: 'Courier New', monospace;
  line-height: 1.6;
}

.summary-content {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

.summary-content :deep(strong) {
  color: #409EFF;
}
</style>
