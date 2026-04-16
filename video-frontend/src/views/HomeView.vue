<template>
  <div class="home-container">
    <el-card class="upload-card">
      <template #header>
        <div class="card-header">
          <el-icon :size="24" color="#409EFF"><VideoCamera /></el-icon>
          <span>视频智能总结系统</span>
        </div>
      </template>

      <!-- 上传区域 -->
      <el-upload
        ref="uploadRef"
        class="upload-demo"
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        :limit="1"
        accept=".mp4,.avi,.mov,.wmv,.flv,.mkv"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽视频到此处或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 mp4/avi/mov/wmv/flv/mkv 格式,最大 500MB
          </div>
        </template>
      </el-upload>

      <!-- 处理按钮 -->
      <div v-if="selectedFile" class="action-area">
        <el-button 
          type="primary" 
          size="large"
          :loading="processing"
          @click="handleProcess"
        >
          {{ processing ? '处理中...' : '开始处理' }}
        </el-button>
      </div>

      <!-- 进度显示 -->
      <div v-if="processing" class="progress-area">
        <el-steps :active="currentStep" finish-status="success" align-center>
          <el-step title="上传视频" />
          <el-step title="转换音频" />
          <el-step title="AI 分析" />
          <el-step title="完成" />
        </el-steps>

        <el-progress 
          v-if="currentStep === 0"
          :percentage="uploadProgress" 
          :status="uploadProgress === 100 ? 'success' : ''"
        />
        
        <div class="step-info">
          {{ stepText }}
        </div>
      </div>

      <!-- 结果预览 -->
      <div v-if="videoInfo && !processing" class="result-preview">
        <el-alert
          title="处理完成!"
          type="success"
          :closable="false"
          show-icon
        />
        <el-button 
          type="success" 
          size="large"
          @click="viewResult"
          style="margin-top: 15px"
        >
          查看总结报告
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { uploadVideo, convertToAudio, processWithAi } from '../api/video'

const router = useRouter()
const uploadRef = ref(null)
const selectedFile = ref(null)
const processing = ref(false)
const currentStep = ref(0)
const uploadProgress = ref(0)
const videoInfo = ref(null)

const stepText = computed(() => {
  const texts = [
    '正在上传视频...',
    '正在转换音频...',
    'AI 正在分析内容...',
    '处理完成!'
  ]
  return texts[currentStep.value] || ''
})

// 文件选择
const handleFileChange = (file) => {
  selectedFile.value = file.raw
  videoInfo.value = null
  currentStep.value = 0
  uploadProgress.value = 0
}

// 处理视频
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
    console.log('开始上传视频...')
    const uploadRes = await uploadVideo(selectedFile.value, (progress) => {
      uploadProgress.value = progress
    })
    
    if (uploadRes.code !== 200) {
      throw new Error(uploadRes.message || '上传失败')
    }
    
    videoInfo.value = uploadRes.data
    currentStep.value = 1
    console.log('视频上传成功, ID:', videoInfo.value.id)

    // 步骤2: 转换音频
    console.log('开始转换音频...')
    const convertRes = await convertToAudio(videoInfo.value.id)
    if (convertRes.code !== 200) {
      throw new Error(convertRes.message || '音频转换失败')
    }
    currentStep.value = 2
    console.log('音频转换成功')

    // 步骤3: AI 处理
    console.log('开始 AI 处理...')
    const aiRes = await processWithAi(videoInfo.value.id)
    if (aiRes.code !== 200) {
      throw new Error(aiRes.message || 'AI 处理失败')
    }
    currentStep.value = 3
    console.log('AI 处理成功')

    ElMessage.success('视频处理完成!')

  } catch (error) {
    console.error('处理失败:', error)
    ElMessage.error(error.message || '处理失败,请重试')
    processing.value = false
  }
}

// 查看结果
const viewResult = () => {
  if (videoInfo.value && videoInfo.value.id) {
    router.push(`/result/${videoInfo.value.id}`)
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.upload-card {
  width: 100%;
  max-width: 700px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.upload-demo {
  margin: 20px 0;
}

.action-area {
  text-align: center;
  margin-top: 20px;
}

.progress-area {
  margin-top: 30px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.step-info {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
  color: #606266;
}

.result-preview {
  margin-top: 20px;
  text-align: center;
}
</style>
