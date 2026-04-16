<template>
  <div class="upload-video-view">
    <!-- 导航栏 -->
    <el-page-header @back="$router.push('/')">
      <template #content>
        <span class="page-title">上传视频</span>
      </template>
    </el-page-header>

    <div class="content-wrapper">
      <el-card>
        <el-upload
          ref="uploadRef"
          class="upload-area"
          drag
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1"
          accept=".mp4,.avi,.mov,.wmv,.flv,.mkv"
        >
          <el-icon class="upload-icon"><UploadFilled /></el-icon>
          <div class="upload-text">
            <p>拖拽视频到此处或 <em>点击上传</em></p>
            <p class="upload-tip">支持 mp4/avi/mov/wmv/flv/mkv,最大 500MB</p>
          </div>
        </el-upload>

        <div v-if="selectedFile" class="file-info">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="文件名">{{ selectedFile.name }}</el-descriptions-item>
            <el-descriptions-item label="大小">{{ formatFileSize(selectedFile.size) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="action-buttons">
          <el-button 
            type="primary" 
            size="large"
            :loading="processing"
            :disabled="!selectedFile"
            @click="handleProcess"
          >
            {{ processing ? '处理中...' : '开始处理' }}
          </el-button>
        </div>

        <!-- 进度显示 -->
        <div v-if="processing" class="progress-section">
          <el-steps :active="currentStep" finish-status="success" align-center>
            <el-step title="上传" description="上传视频文件" />
            <el-step title="转换" description="转换为音频" />
            <el-step title="AI分析" description="智能总结" />
            <el-step title="完成" description="查看结果" />
          </el-steps>

          <el-progress 
            v-if="currentStep === 0"
            :percentage="uploadProgress"
            :status="uploadProgress === 100 ? 'success' : ''"
          />
          
          <div class="step-tip">{{ stepText }}</div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { uploadVideoFile, convertToAudio, processWithAi } from '../api/video'

const router = useRouter()
const uploadRef = ref(null)
const selectedFile = ref(null)
const processing = ref(false)
const currentStep = ref(0)
const uploadProgress = ref(0)
const videoId = ref(null)

const stepText = computed(() => {
  const texts = [
    '正在上传视频,请稍候...',
    '正在转换音频格式...',
    'AI 正在分析视频内容...',
    '处理完成,即将跳转...'
  ]
  return texts[currentStep.value] || ''
})

const handleFileChange = (file) => {
  selectedFile.value = file.raw
  resetState()
}

const resetState = () => {
  processing.value = false
  currentStep.value = 0
  uploadProgress.value = 0
  videoId.value = null
}

const handleProcess = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择视频文件')
    return
  }

  processing.value = true
  currentStep.value = 0
  uploadProgress.value = 0

  try {
    // 步骤1: 上传视频
    currentStep.value = 0
    const uploadRes = await uploadVideoFile(selectedFile.value, (progress) => {
      uploadProgress.value = progress
    })
    
    if (uploadRes.code !== 200) {
      throw new Error(uploadRes.message || '上传失败')
    }
    
    videoId.value = uploadRes.data.id
    currentStep.value = 1

    // 步骤2: 转换音频
    await convertToAudio(videoId.value)
    currentStep.value = 2

    // 步骤3: AI 处理
    await processWithAi(videoId.value)
    currentStep.value = 3

    ElMessage.success('视频处理完成!')
    
    setTimeout(() => {
      router.push(`/result/${videoId.value}`)
    }, 1000)

  } catch (error) {
    console.error('处理失败:', error)
    ElMessage.error(error.message || '处理失败,请重试')
    processing.value = false
  }
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}
</script>

<style scoped>
.upload-video-view {
  min-height: 100vh;
  background: #f5f7fa;
}

.content-wrapper {
  max-width: 800px;
  margin: 30px auto;
  padding: 0 20px;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
}

.upload-area {
  margin: 20px 0;
}

.upload-icon {
  font-size: 67px;
  color: #409EFF;
  margin-bottom: 16px;
}

.upload-text {
  color: #606266;
}

.upload-text p {
  margin: 8px 0;
}

.upload-text em {
  color: #409EFF;
  font-style: normal;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
}

.file-info {
  margin: 20px 0;
}

.action-buttons {
  text-align: center;
  margin: 20px 0;
}

.progress-section {
  margin-top: 30px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.step-tip {
  text-align: center;
  margin-top: 15px;
  color: #606266;
  font-size: 14px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .content-wrapper {
    margin: 15px auto;
    padding: 0 10px;
  }

  .upload-icon {
    font-size: 48px;
  }

  .upload-text p {
    font-size: 14px;
  }
}
</style>
