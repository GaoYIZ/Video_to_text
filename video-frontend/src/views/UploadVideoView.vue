<template>
  <div class="upload-page">
    <!-- 头部 -->
    <header class="page-header">
      <div class="header-content">
        <el-button text @click="$router.push('/')">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </el-button>
        <h2>{{ pageTitle }}</h2>
      </div>
    </header>

    <!-- 主内容 -->
    <main class="page-main">
      <div class="upload-container">
        <el-card class="upload-card">
          <!-- 上传区域 -->
          <el-upload
            ref="uploadRef"
            class="upload-area"
            drag
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            :accept="acceptFormats"
          >
            <el-icon class="upload-icon"><UploadFilled /></el-icon>
            <div class="upload-text">
              <p class="upload-title">将文件拖到此处,或<em>点击上传</em></p>
              <p class="upload-hint">{{ formatHint }}</p>
            </div>
          </el-upload>

          <!-- 文件信息 -->
          <div v-if="selectedFile" class="file-info">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="文件名">
                <el-text truncated style="max-width: 400px">{{ selectedFile.name }}</el-text>
              </el-descriptions-item>
              <el-descriptions-item label="大小">{{ formatFileSize(selectedFile.size) }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 操作按钮 -->
          <div class="action-area">
            <el-button 
              type="primary" 
              size="large"
              :loading="processing"
              :disabled="!selectedFile"
              @click="handleProcess"
              style="min-width: 200px"
            >
              {{ processing ? '处理中...' : '开始转写' }}
            </el-button>
          </div>

          <!-- 进度显示 -->
          <div v-if="processing" class="progress-area">
            <el-steps :active="currentStep" finish-status="success" align-center>
              <el-step 
                v-for="(step, index) in steps" 
                :key="index"
                :title="step.title"
                :description="step.desc"
              />
            </el-steps>

            <el-progress 
              v-if="currentStep === 0"
              :percentage="uploadProgress"
              :status="uploadProgress === 100 ? 'success' : ''"
              style="margin-top: 20px"
            />
            
            <div class="step-tip">{{ currentStepText }}</div>
          </div>
        </el-card>

        <!-- 说明卡片 -->
        <el-card class="tips-card" shadow="never">
          <template #header>
            <div class="tips-header">
              <el-icon><InfoFilled /></el-icon>
              <span>温馨提示</span>
            </div>
          </template>
          <ul class="tips-list">
            <li v-for="(tip, index) in tips" :key="index">
              <el-icon color="#1890ff"><CircleCheck /></el-icon>
              <span>{{ tip }}</span>
            </li>
          </ul>
        </el-card>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { uploadVideoFile, convertToAudio, processWithAi } from '../api/video'

const props = defineProps({
  title: {
    type: String,
    default: '上传视频'
  },
  acceptFormats: {
    type: String,
    default: '.mp4,.avi,.mov,.wmv,.flv,.mkv'
  },
  formatHint: {
    type: String,
    default: '支持 mp4/avi/mov/wmv/flv/mkv 格式,单个文件不超过 500MB'
  },
  steps: {
    type: Array,
    default: () => [
      { title: '上传', desc: '上传视频文件' },
      { title: '转换', desc: '提取音频' },
      { title: '转写', desc: 'AI 语音识别' },
      { title: '总结', desc: '生成智能摘要' },
      { title: '完成', desc: '查看结果' }
    ]
  },
  tips: {
    type: Array,
    default: () => [
      '视频会自动转换为音频进行转写',
      '处理时间取决于视频长度,请耐心等待',
      '建议上传清晰的音频以获得更好效果',
      '转写完成后自动生成 AI 总结'
    ]
  },
  onProcess: {
    type: Function,
    default: null
  }
})

const router = useRouter()
const uploadRef = ref(null)
const selectedFile = ref(null)
const processing = ref(false)
const currentStep = ref(0)
const uploadProgress = ref(0)

const pageTitle = computed(() => props.title)

const currentStepText = computed(() => {
  if (currentStep.value < props.steps.length) {
    return props.steps[currentStep.value].desc
  }
  return '处理完成'
})

const handleFileChange = (file) => {
  selectedFile.value = file.raw
  resetState()
}

const resetState = () => {
  processing.value = false
  currentStep.value = 0
  uploadProgress.value = 0
}

const handleProcess = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  processing.value = true
  currentStep.value = 0
  uploadProgress.value = 0

  try {
    // 如果传入了自定义处理函数,使用它
    if (props.onProcess) {
      await props.onProcess({
        file: selectedFile.value,
        onProgress: (progress) => {
          uploadProgress.value = progress
        },
        onStepChange: (step) => {
          currentStep.value = step
        },
        onSuccess: (videoId) => {
          ElMessage.success('转写完成!')
          setTimeout(() => {
            router.push(`/result/${videoId}`)
          }, 1000)
        }
      })
      return
    }

    // 默认处理逻辑
    // 步骤1: 上传视频
    currentStep.value = 0
    const uploadRes = await uploadVideoFile(selectedFile.value, (progress) => {
      uploadProgress.value = progress
    })
    
    if (uploadRes.code !== 200) {
      throw new Error(uploadRes.message || '上传失败')
    }
    
    const videoId = uploadRes.data.id
    currentStep.value = 1

    // 步骤2: 转换音频
    await convertToAudio(videoId)
    currentStep.value = 2

    // 步骤3: AI 处理
    await processWithAi(videoId)
    currentStep.value = 3
    
    currentStep.value = 4
    ElMessage.success('转写完成!')
    
    setTimeout(() => {
      router.push(`/result/${videoId}`)
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
.upload-page {
  min-height: 100vh;
  background: #f0f2f5;
}

.page-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 16px 24px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-content h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.page-main {
  max-width: 900px;
  margin: 32px auto;
  padding: 0 24px;
}

.upload-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.upload-card {
  border-radius: 8px;
}

.upload-area {
  margin: 20px 0;
}

.upload-area :deep(.el-upload-dragger) {
  padding: 40px 20px;
}

.upload-icon {
  font-size: 72px;
  color: #1890ff;
  margin-bottom: 16px;
}

.upload-text {
  color: #595959;
}

.upload-title {
  font-size: 16px;
  margin: 8px 0;
}

.upload-title em {
  color: #1890ff;
  font-style: normal;
}

.upload-hint {
  font-size: 13px;
  color: #8c8c8c;
  margin: 4px 0 0;
}

.file-info {
  margin: 20px 0;
}

.action-area {
  text-align: center;
  margin: 24px 0;
}

.progress-area {
  margin-top: 32px;
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;
}

.step-tip {
  text-align: center;
  margin-top: 16px;
  color: #595959;
  font-size: 14px;
}

.tips-card {
  border-radius: 8px;
  background: #fff;
}

.tips-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #262626;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tips-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 0;
  color: #595959;
  font-size: 14px;
}

@media (max-width: 768px) {
  .page-main {
    margin: 16px auto;
    padding: 0 16px;
  }

  .upload-icon {
    font-size: 56px;
  }

  .upload-title {
    font-size: 14px;
  }
}
</style>
