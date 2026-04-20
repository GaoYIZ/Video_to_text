<script setup lang="ts">
import SparkMD5 from 'spark-md5'
import { computed, ref } from 'vue'
import { ElMessage, type UploadFile } from 'element-plus'
import { useRouter } from 'vue-router'
import { uploadApi, workbenchApi } from '../api/modules'
import { formatFileSize } from '../utils/format'

const router = useRouter()
const file = ref<File | null>(null)
const uploading = ref(false)
const progress = ref(0)
const uploadLog = ref<string[]>([])

const fileSummary = computed(() => {
  if (!file.value) return null
  return {
    name: file.value.name,
    size: formatFileSize(file.value.size),
    type: file.value.type || '未知格式'
  }
})

const handleFileChange = (uploadFile: UploadFile) => {
  if (uploadFile.raw) {
    file.value = uploadFile.raw
    progress.value = 0
    uploadLog.value = []
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

const startUpload = async () => {
  if (!file.value) {
    ElMessage.warning('请先选择视频文件')
    return
  }

  uploading.value = true
  uploadLog.value.push('开始计算文件 MD5，用于秒传与内容级去重。')

  try {
    const currentFile = file.value
    const chunkSize = 5 * 1024 * 1024
    const totalChunks = Math.ceil(currentFile.size / chunkSize)
    const fileMd5 = await computeMd5(currentFile)
    uploadLog.value.push(`MD5 计算完成：${fileMd5}`)

    const initResult = await uploadApi.init({
      fileName: currentFile.name,
      fileMd5,
      fileSize: currentFile.size,
      chunkSize,
      totalChunks,
      mimeType: currentFile.type
    })

    if (initResult.fastUpload) {
      uploadLog.value.push('命中秒传，直接复用已有文件并创建解析任务。')
      const convertResult = await workbenchApi.convertFile({ uploadId: initResult.uploadId })
      if (convertResult.task?.id) {
        router.push(`/tasks/${convertResult.task.id}`)
      }
      return
    }

    const uploadedSet = new Set(initResult.uploadedChunks || [])
    for (let index = 0; index < totalChunks; index++) {
      if (uploadedSet.has(index)) {
        uploadLog.value.push(`分片 ${index + 1}/${totalChunks} 已存在，已自动跳过。`)
        progress.value = Math.round(((index + 1) / totalChunks) * 100)
        continue
      }

      const chunk = currentFile.slice(index * chunkSize, Math.min(currentFile.size, (index + 1) * chunkSize))
      const formData = new FormData()
      formData.append('uploadId', initResult.uploadId)
      formData.append('fileMd5', fileMd5)
      formData.append('chunkIndex', String(index))
      formData.append('chunkFile', chunk, `${currentFile.name}.part${index}`)
      await uploadApi.uploadChunk(formData)
      progress.value = Math.round(((index + 1) / totalChunks) * 100)
      uploadLog.value.push(`分片 ${index + 1}/${totalChunks} 上传完成。`)
    }

    uploadLog.value.push('所有分片上传完成，开始合并文件。')
    await uploadApi.merge({
      uploadId: initResult.uploadId,
      fileMd5,
      fileName: currentFile.name
    })
    uploadLog.value.push('文件合并成功，开始创建解析任务。')

    const convertResult = await workbenchApi.convertFile({ uploadId: initResult.uploadId })
    ElMessage.success(convertResult.message || '任务已创建')
    if (convertResult.task?.id) {
      router.push(`/tasks/${convertResult.task.id}`)
    }
  } catch (error: any) {
    uploadLog.value.push(`上传失败：${error.message || '未知错误'}`)
    ElMessage.error(error.message || '上传失败，请稍后重试')
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <div class="page-shell convert-page">
    <section class="convert-hero panel fade-in-up">
      <div>
        <div class="mono hero-kicker">UPLOAD · RESUME · FAST HIT</div>
        <h1 class="page-title">上传一条视频，直接进入真实解析链路</h1>
        <p class="page-copy">
          这页恢复了删掉前那套“先上传再解析”的工作流：前端负责切片、续传和 MD5，
          后端接着完成合并、异步处理、转写、摘要和问答准备。
        </p>
      </div>

      <div class="feature-pills">
        <span>分片上传</span>
        <span>断点续传</span>
        <span>秒传去重</span>
        <span>自动建任务</span>
      </div>
    </section>

    <section class="convert-grid">
      <div class="panel upload-panel">
        <div class="section-header">
          <div>
            <h3 class="section-heading">选择视频文件</h3>
            <p class="section-description">推荐使用 MP4 等常见视频格式，便于 FFmpeg 后续提取音频。</p>
          </div>
        </div>

        <el-upload drag :auto-upload="false" :show-file-list="true" :limit="1" @change="handleFileChange">
          <el-icon size="42"><UploadFilled /></el-icon>
          <div class="el-upload__text">拖拽视频到这里，或点击选择文件</div>
          <template #tip>
            <div class="upload-tip">本地演示环境会优先走文件上传链路，再创建解析任务。</div>
          </template>
        </el-upload>

        <div v-if="fileSummary" class="file-summary panel-muted">
          <div>
            <span>文件名称</span>
            <strong>{{ fileSummary.name }}</strong>
          </div>
          <div>
            <span>文件大小</span>
            <strong>{{ fileSummary.size }}</strong>
          </div>
          <div>
            <span>MIME 类型</span>
            <strong>{{ fileSummary.type }}</strong>
          </div>
        </div>

        <div class="progress-box">
          <div class="progress-row">
            <span>上传进度</span>
            <strong>{{ progress }}%</strong>
          </div>
          <el-progress :percentage="progress" color="#6fa8ff" :stroke-width="10" />
        </div>

        <el-button type="warning" :loading="uploading" @click="startUpload">上传并创建解析任务</el-button>
      </div>

      <div class="panel guide-panel">
        <div class="section-header">
          <div>
            <h3 class="section-heading">链路说明</h3>
            <p class="section-description">上传完成后，系统会继续走完整的视频理解处理流程。</p>
          </div>
        </div>

        <div class="guide-list">
          <div>
            <strong>1. 计算 MD5</strong>
            <p>识别重复文件并支持内容级秒传，减少重复上传。</p>
          </div>
          <div>
            <strong>2. 分片上传</strong>
            <p>服务端记录分片进度，前端支持断点续传和跳过已传分片。</p>
          </div>
          <div>
            <strong>3. 异步处理</strong>
            <p>文件合并后进入任务状态机，继续执行转写、摘要与索引。</p>
          </div>
          <div>
            <strong>4. 问答准备</strong>
            <p>落库转写片段与摘要结果，为 RAG 问答和 Agent 工具调用提供上下文。</p>
          </div>
        </div>
      </div>
    </section>

    <section class="panel log-panel">
      <div class="section-header">
        <div>
          <h3 class="section-heading">上传日志</h3>
          <p class="section-description">这里持续展示 MD5、分片和任务创建过程，便于演示秒传与断点续传能力。</p>
        </div>
      </div>

      <div class="log-list">
        <p v-if="uploadLog.length === 0" class="log-empty">选择文件后，这里会出现完整的上传过程记录。</p>
        <p v-for="(item, index) in uploadLog" :key="index">{{ item }}</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.convert-page {
  gap: 20px;
}

.convert-hero,
.upload-panel,
.guide-panel,
.log-panel {
  border-radius: 30px;
  padding: 24px;
}

.hero-kicker {
  color: var(--accent-strong);
  font-size: 12px;
  letter-spacing: 0.12em;
}

.feature-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.feature-pills span {
  padding: 10px 14px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.78);
}

.convert-grid {
  display: grid;
  grid-template-columns: 1.08fr 0.92fr;
  gap: 20px;
}

.upload-panel {
  display: grid;
  gap: 18px;
}

.upload-tip {
  color: var(--muted);
}

.file-summary {
  border-radius: 18px;
  padding: 16px;
  display: grid;
  gap: 12px;
}

.file-summary span,
.progress-row span {
  color: var(--muted);
  font-size: 13px;
}

.file-summary strong {
  display: block;
  margin-top: 6px;
  word-break: break-word;
}

.progress-box {
  display: grid;
  gap: 10px;
}

.progress-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.guide-list {
  display: grid;
  gap: 16px;
}

.guide-list div {
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);
}

.guide-list div:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.guide-list p {
  margin: 8px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.log-list {
  display: grid;
  gap: 10px;
}

.log-list p {
  margin: 0;
  padding-left: 14px;
  border-left: 2px solid rgba(111, 168, 255, 0.4);
  color: var(--text-soft);
  line-height: 1.7;
}

.log-empty {
  color: var(--muted) !important;
  border-left-style: dashed !important;
}

@media (max-width: 1100px) {
  .convert-grid {
    grid-template-columns: 1fr;
  }
}
</style>
