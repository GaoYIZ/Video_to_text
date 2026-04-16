<template>
  <div class="parse-url-view">
    <el-page-header @back="$router.push('/')">
      <template #content>
        <span class="page-title">解析视频链接</span>
      </template>
    </el-page-header>

    <div class="content-wrapper">
      <el-card>
        <el-alert
          title="🎯 支持 B站、YouTube、抖音等平台视频链接"
          type="success"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        />

        <div class="input-section">
          <el-input
            v-model="videoUrl"
            placeholder="请输入视频链接,例如: https://www.bilibili.com/video/BV1xx411c7mD"
            size="large"
            clearable
            @keyup.enter="handleParse"
          >
            <template #prefix>
              <el-icon><Link /></el-icon>
            </template>
          </el-input>

          <el-button 
            type="primary" 
            size="large"
            :loading="processing"
            :disabled="!videoUrl"
            @click="handleParse"
            style="margin-top: 15px; width: 100%"
          >
            {{ processing ? '解析中...' : '开始解析' }}
          </el-button>
        </div>

        <!-- 支持的 platform -->
        <div class="platforms">
          <h4>支持的平台:</h4>
          <el-space wrap>
            <el-tag effect="plain">Bilibili</el-tag>
            <el-tag effect="plain">YouTube</el-tag>
            <el-tag effect="plain">抖音</el-tag>
            <el-tag effect="plain">快手</el-tag>
            <el-tag effect="plain">西瓜视频</el-tag>
          </el-space>
        </div>

        <!-- 进度显示 -->
        <div v-if="processing" class="progress-section">
          <el-steps :active="currentStep" finish-status="success" align-center>
            <el-step title="解析" description="解析视频链接" />
            <el-step title="下载" description="下载视频内容" />
            <el-step title="转换" description="转换为音频" />
            <el-step title="AI分析" description="智能总结" />
            <el-step title="完成" description="查看结果" />
          </el-steps>
          
          <div class="step-tip">{{ stepText }}</div>
        </div>

        <!-- 解析结果预览 -->
        <div v-if="parsedInfo" class="parsed-info">
          <el-divider content-position="left">视频信息</el-divider>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="标题">{{ parsedInfo.title }}</el-descriptions-item>
            <el-descriptions-item label="作者">{{ parsedInfo.author || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="时长">{{ parsedInfo.duration || '未知' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { parseVideoUrl, convertToAudio, processWithAi } from '../api/video'

const router = useRouter()
const videoUrl = ref('')
const processing = ref(false)
const currentStep = ref(0)
const videoId = ref(null)
const parsedInfo = ref(null)

const stepText = computed(() => {
  const texts = [
    '正在解析视频链接...',
    '正在下载视频内容...',
    '正在转换音频格式...',
    'AI 正在分析视频内容...',
    '处理完成,即将跳转...'
  ]
  return texts[currentStep.value] || ''
})

const handleParse = async () => {
  if (!videoUrl.value) {
    ElMessage.warning('请输入视频链接')
    return
  }

  // 简单的 URL 验证
  if (!isValidUrl(videoUrl.value)) {
    ElMessage.error('请输入有效的视频链接')
    return
  }

  processing.value = true
  currentStep.value = 0
  parsedInfo.value = null

  try {
    // 步骤1: 解析链接
    currentStep.value = 0
    const parseRes = await parseVideoUrl(videoUrl.value)
    
    if (parseRes.code !== 200) {
      throw new Error(parseRes.message || '解析失败')
    }
    
    videoId.value = parseRes.data.id
    parsedInfo.value = parseRes.data
    currentStep.value = 1

    // 步骤2: 下载视频(后端自动完成)
    currentStep.value = 2

    // 步骤3: 转换音频
    await convertToAudio(videoId.value)
    currentStep.value = 3

    // 步骤4: AI 处理
    await processWithAi(videoId.value)
    currentStep.value = 4

    ElMessage.success('视频解析完成!')
    
    setTimeout(() => {
      router.push(`/result/${videoId.value}`)
    }, 1000)

  } catch (error) {
    console.error('解析失败:', error)
    ElMessage.error(error.message || '解析失败,请检查链接是否正确')
    processing.value = false
  }
}

const isValidUrl = (url) => {
  try {
    new URL(url)
    return true
  } catch {
    return false
  }
}
</script>

<style scoped>
.parse-url-view {
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

.input-section {
  margin: 20px 0;
}

.platforms {
  margin: 20px 0;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.platforms h4 {
  margin-bottom: 10px;
  color: #606266;
  font-size: 14px;
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

.parsed-info {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .content-wrapper {
    margin: 15px auto;
    padding: 0 10px;
  }
}
</style>
