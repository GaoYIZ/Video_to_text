<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Microphone, MagicStick, Check, DataLine, Loading } from '@element-plus/icons-vue'
import SparkMD5 from 'spark-md5'
import { uploadApi, workbenchApi } from '../api/modules'

type ConvertType = 'link' | 'video' | 'record'

const convertType = ref<ConvertType>('video')
const isConverting = ref(false)
const isRecording = ref(false)
const transcription = ref('')
const aiResult = ref('')
const activeAiTask = ref('')
const isAiLoading = ref(false)
const uploadProgress = ref(0)
const linkUrl = ref('')

// 模拟转录文本
const mockTranscript = `会议记录（00:00 - 02:30）：\n大家好，感谢参加本月的产品发布会。关键点如下：1. 我们全新的 AI 实时转录引擎识别率已达到 98% 以上。2. 下个季度的目标是增加对 50 种语言的支持。3. 开发团队需要优化 API 的 Token 消耗，以降低运营成本。`

const handleReset = () => {
  transcription.value = ''
  aiResult.value = ''
  activeAiTask.value = ''
  uploadProgress.value = 0
}

const handleStartConvert = async () => {
  if (convertType.value === 'link' && !linkUrl.value) {
    ElMessage.warning('请输入视频链接')
    return
  }

  isConverting.value = true
  
  try {
    if (convertType.value === 'link') {
      // 调用链接转换接口
      const result = await workbenchApi.convertLink({ 
        sourceUrl: linkUrl.value,
        title: '链接视频'
      })
      
      if (result.accepted && result.task) {
        transcription.value = mockTranscript
        ElMessage.success(result.message || '任务已创建')
      } else {
        ElMessage.error(result.message || '转换失败')
      }
    }
    
    isConverting.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '转换失败')
    isConverting.value = false
  }
}

const toggleRecording = () => {
  if (!isRecording.value) {
    isRecording.value = true
    transcription.value = ''
    setTimeout(() => {
      isRecording.value = false
      transcription.value = "这是实时录音转化的文本示例：我们正在讨论如何通过 Gemini API 提升系统的智能化程度。"
    }, 3000)
  } else {
    isRecording.value = false
  }
}

const runAiTask = async (taskType: string) => {
  activeAiTask.value = taskType
  isAiLoading.value = true
  
  // 注意：这里需要先有 taskId，当前 ConvertView 中没有 taskId
  // 所以这个功能暂时无法直接使用，需要从任务列表或详情中调用
  ElMessage.warning('请先在任务列表中选择一个已完成的任务')
  isAiLoading.value = false
}

const handleFileUpload = async (file: File) => {
  isConverting.value = true
  uploadProgress.value = 0
  
  try {
    const chunkSize = 5 * 1024 * 1024
    const totalChunks = Math.ceil(file.size / chunkSize)
    const fileMd5 = await computeMd5(file)
    
    // 初始化上传
    const initResult = await uploadApi.init({
      fileName: file.name,
      fileMd5,
      fileSize: file.size,
      chunkSize,
      totalChunks,
      mimeType: file.type
    })

    // 秒传处理
    if (initResult.fastUpload) {
      ElMessage.info('文件已存在，使用秒传')
      const convertResult = await workbenchApi.convertFile({ 
        uploadId: initResult.uploadId 
      })
      
      if (convertResult.accepted && convertResult.task) {
        transcription.value = mockTranscript
        ElMessage.success(convertResult.message || '任务已创建')
      }
      isConverting.value = false
      return
    }

    // 分片上传
    const uploadedSet = new Set(initResult.uploadedChunks || [])
    for (let index = 0; index < totalChunks; index++) {
      if (uploadedSet.has(index)) {
        uploadProgress.value = Math.round(((index + 1) / totalChunks) * 100)
        continue
      }

      const chunk = file.slice(index * chunkSize, Math.min(file.size, (index + 1) * chunkSize))
      const formData = new FormData()
      formData.append('uploadId', initResult.uploadId)
      formData.append('fileMd5', fileMd5)
      formData.append('chunkIndex', String(index))
      formData.append('chunkFile', chunk, `${file.name}.part${index}`)
      await uploadApi.uploadChunk(formData)
      uploadProgress.value = Math.round(((index + 1) / totalChunks) * 100)
    }

    // 合并文件
    await uploadApi.merge({
      uploadId: initResult.uploadId,
      fileMd5,
      fileName: file.name
    })

    // 创建转换任务
    const convertResult = await workbenchApi.convertFile({ 
      uploadId: initResult.uploadId 
    })
    
    if (convertResult.accepted && convertResult.task) {
      transcription.value = mockTranscript
      ElMessage.success(convertResult.message || '任务已创建')
    } else {
      ElMessage.error(convertResult.message || '创建任务失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '上传失败')
  } finally {
    isConverting.value = false
  }
}

const computeMd5 = async (target: File) => {
  const spark = new SparkMD5.ArrayBuffer()
  const chunkSize = 5 * 1024 * 1024
  const totalChunks = Math.ceil(target.size / chunkSize)
  for (let index = 0; index < totalChunks; index++) {
    const chunk = target.slice(index * chunkSize, Math.min(target.size, (index + 1) * chunkSize))
    spark.append(await chunk.arrayBuffer())
  }
  return spark.end()
}

const beforeUpload = (file: File) => {
  handleFileUpload(file)
  return false // 阻止自动上传
}
</script>

<template>
  <div class="convert-container">
    <div class="convert-panel panel">
      <!-- Type Selector -->
      <div class="type-selector">
        <button
          v-for="type in ['link', 'video', 'record']"
          :key="type"
          :class="['type-btn', { active: convertType === type }]"
          @click="() => { convertType = type as ConvertType; handleReset() }"
        >
          {{ type === 'link' ? 'URL' : type === 'video' ? '文件' : '实时' }}
        </button>
      </div>

      <!-- Input Area -->
      <div v-if="!transcription && !isConverting && !isRecording" class="input-area">
        <!-- Link Input -->
        <div v-if="convertType === 'link'" class="space-y-6">
          <el-input
            v-model="linkUrl"
            placeholder="粘贴视频链接..."
            size="large"
            class="url-input"
          />
          <el-button 
            type="primary" 
            size="large" 
            @click="handleStartConvert"
            class="start-btn"
          >
            开始转录
          </el-button>
        </div>

        <!-- Video Upload -->
        <div v-else-if="convertType === 'video'">
          <el-upload
            drag
            :auto-upload="false"
            :show-file-list="false"
            :before-upload="beforeUpload"
            accept="video/*,audio/*"
            class="upload-area"
          >
            <el-icon :size="48" class="upload-icon"><UploadFilled /></el-icon>
            <div class="upload-text">点击上传音视频文件</div>
          </el-upload>
          
          <div v-if="uploadProgress > 0" class="progress-bar">
            <el-progress :percentage="uploadProgress" color="#6366f1" :stroke-width="10" />
          </div>
        </div>

        <!-- Record -->
        <div v-else class="record-area">
          <div class="record-icon-wrapper">
            <el-icon :size="32"><Microphone /></el-icon>
          </div>
          <el-button 
            type="danger" 
            size="large" 
            @click="toggleRecording"
            class="record-btn"
          >
            开始录音
          </el-button>
        </div>
      </div>

      <!-- Recording State -->
      <div v-if="isRecording" class="recording-state">
        <div class="waveform">
          <div 
            v-for="i in 12" 
            :key="i" 
            class="wave-bar"
            :style="{ animationDelay: `${i * 0.05}s` }"
          ></div>
        </div>
        <el-button @click="toggleRecording" size="small">停止并处理</el-button>
      </div>

      <!-- Converting State -->
      <div v-if="isConverting" class="converting-state">
        <el-icon :size="48" class="spinner"><Loading /></el-icon>
        <p class="loading-text">模型解析中...</p>
      </div>

      <!-- Result Area -->
      <div v-if="transcription && !isConverting && !isRecording" class="result-area fade-in">
        <div class="transcription-box">
          {{ transcription }}
        </div>

        <div class="ai-actions">
          <button class="action-btn" @click="runAiTask('summary')">
            <el-icon :size="20" class="text-indigo-500"><MagicStick /></el-icon>
            <span>摘要</span>
          </button>
          <button class="action-btn" @click="runAiTask('todo')">
            <el-icon :size="20" class="text-amber-500"><Check /></el-icon>
            <span>待办</span>
          </button>
          <button class="action-btn" @click="runAiTask('data')">
            <el-icon :size="20" class="text-emerald-500"><DataLine /></el-icon>
            <span>数据</span>
          </button>
        </div>

        <div v-if="isAiLoading" class="ai-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
        </div>

        <div v-if="aiResult" class="ai-result">
          {{ aiResult }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.convert-container {
  max-width: 900px;
  margin: 0 auto;
}

.panel {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 40px;
  padding: 40px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.type-selector {
  display: flex;
  background: #f1f5f9;
  padding: 6px;
  border-radius: 16px;
  width: fit-content;
  margin-bottom: 40px;
}

.type-btn {
  padding: 8px 24px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 700;
  transition: all 0.2s;
  background: transparent;
  border: none;
  cursor: pointer;
  color: #64748b;
}

.type-btn.active {
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  color: #4f46e5;
}

.url-input {
  margin-bottom: 24px;
}

.url-input :deep(.el-input__wrapper) {
  border-radius: 16px;
  padding: 16px 20px;
}

.start-btn {
  width: 100%;
  border-radius: 16px;
  padding: 20px;
  font-size: 16px;
  font-weight: 700;
}

.upload-area {
  border-radius: 32px;
  padding: 80px 40px;
  background: rgba(248, 250, 252, 0.5);
  cursor: pointer;
  transition: all 0.3s;
}

.upload-area:hover {
  background: white;
}

.upload-icon {
  color: #818cf8;
  margin-bottom: 16px;
}

.upload-text {
  font-weight: 700;
  color: #1e293b;
}

.progress-bar {
  margin-top: 24px;
}

.record-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
  gap: 24px;
}

.record-icon-wrapper {
  width: 80px;
  height: 80px;
  background: #fef2f2;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #fecaca;
  color: #ef4444;
}

.record-btn {
  padding: 16px 40px;
  border-radius: 16px;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.recording-state,
.converting-state {
  padding: 80px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.waveform {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 40px;
  margin-bottom: 32px;
}

.wave-bar {
  width: 6px;
  background: #ef4444;
  border-radius: 999px;
  animation: wave 1s ease-in-out infinite;
}

@keyframes wave {
  0%, 100% { height: 20%; }
  50% { height: 100%; }
}

.spinner {
  color: #4f46e5;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-text {
  font-weight: 700;
  color: #1e293b;
}

.result-area {
  animation: fadeIn 0.5s ease-out;
}

.transcription-box {
  background: #f8fafc;
  padding: 24px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  font-size: 14px;
  line-height: 1.7;
  margin-bottom: 24px;
  font-style: italic;
  color: #475569;
}

.ai-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  gap: 4px;
}

.action-btn:hover {
  border-color: #6366f1;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.1);
}

.action-btn span {
  font-size: 13px;
  font-weight: 500;
  color: #1e293b;
}

.ai-loading {
  display: flex;
  justify-content: center;
  padding: 24px;
}

.ai-result {
  padding: 32px;
  background: #eef2ff;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.7;
  border: 1px solid #e0e7ff;
  color: #1e293b;
}

.fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@media (max-width: 768px) {
  .panel {
    padding: 24px;
    border-radius: 24px;
  }
  
  .ai-actions {
    grid-template-columns: 1fr;
  }
}
</style>
