<script setup lang="ts">
import SparkMD5 from 'spark-md5'
import { computed, ref } from 'vue'
import { ElMessage, type UploadFile } from 'element-plus'
import { useRouter } from 'vue-router'
import { taskApi, uploadApi } from '../api/modules'

const router = useRouter()
const file = ref<File | null>(null)
const uploading = ref(false)
const progress = ref(0)
const uploadLog = ref<string[]>([])

const fileSummary = computed(() => {
  if (!file.value) return null
  return {
    name: file.value.name,
    size: formatSize(file.value.size),
    type: file.value.type || '未知格式'
  }
})

const handleFileChange = (uploadFile: UploadFile, uploadFiles: UploadFile[]) => {
  console.log('handleFileChange called', uploadFile)
  if (uploadFile.raw) {
    file.value = uploadFile.raw
    progress.value = 0
    uploadLog.value = []
    console.log('File selected:', file.value.name, file.value.size)
  }
}

const formatSize = (size: number) => {
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  if (size < 1024 * 1024 * 1024) return `${(size / 1024 / 1024).toFixed(1)} MB`
  return `${(size / 1024 / 1024 / 1024).toFixed(2)} GB`
}

const computeMd5 = async (target: File) => {
  console.log('computeMd5 started, file size:', target.size)
  const spark = new SparkMD5.ArrayBuffer()
  const chunkSize = 5 * 1024 * 1024
  const totalChunks = Math.ceil(target.size / chunkSize)
  console.log('Total chunks to process:', totalChunks)

  for (let index = 0; index < totalChunks; index++) {
    console.log(`Processing chunk ${index + 1}/${totalChunks}`)
    const chunk = target.slice(index * chunkSize, Math.min(target.size, (index + 1) * chunkSize))
    const buffer = await chunk.arrayBuffer()
    spark.append(buffer)
    if ((index + 1) % 10 === 0 || index === totalChunks - 1) {
      console.log(`MD5 progress: ${index + 1}/${totalChunks}`)
    }
  }

  const md5 = spark.end()
  console.log('computeMd5 completed, md5:', md5)
  return md5
}

const startUpload = async () => {
  console.log('startUpload called, file:', file.value)
  if (!file.value) {
    ElMessage.warning('请先选择视频文件')
    return
  }

  uploading.value = true
  uploadLog.value.push('🚀 开始上传流程...')

  try {
    const currentFile = file.value
    const chunkSize = 5 * 1024 * 1024
    const totalChunks = Math.ceil(currentFile.size / chunkSize)

    uploadLog.value.push('开始计算文件 MD5，用于秒传与内容级去重。')
    console.log('Starting MD5 calculation...')
    const fileMd5 = await computeMd5(currentFile)
    console.log('MD5 calculation completed:', fileMd5)
    uploadLog.value.push(`MD5 计算完成：${fileMd5}`)

    console.log('Calling uploadApi.init...')
    const initResult = await uploadApi.init({
      fileName: currentFile.name,
      fileMd5,
      fileSize: currentFile.size,
      chunkSize,
      totalChunks,
      mimeType: currentFile.type
    })
    console.log('uploadApi.init result:', initResult)

    if (initResult.fastUpload) {
      uploadLog.value.push('命中秒传，直接复用已上传文件。')
      const task = await taskApi.create({ uploadId: initResult.uploadId })
      router.push(`/tasks/${task.id}`)
      return
    }

    const uploadedSet = new Set(initResult.uploadedChunks || [])

    for (let index = 0; index < totalChunks; index++) {
      console.log(`Processing chunk ${index + 1}/${totalChunks}`)
      if (uploadedSet.has(index)) {
        uploadLog.value.push(`分片 ${index + 1}/${totalChunks} 已存在，自动跳过。`)
        progress.value = Math.round(((index + 1) / totalChunks) * 100)
        continue
      }

      const chunk = currentFile.slice(index * chunkSize, Math.min(currentFile.size, (index + 1) * chunkSize))
      const formData = new FormData()
      formData.append('uploadId', initResult.uploadId)
      formData.append('fileMd5', fileMd5)
      formData.append('chunkIndex', String(index))
      formData.append('chunkFile', chunk, `${currentFile.name}.part${index}`)

      console.log(`Uploading chunk ${index + 1}/${totalChunks}...`)
      await uploadApi.uploadChunk(formData)
      console.log(`Chunk ${index + 1}/${totalChunks} uploaded successfully`)
      progress.value = Math.round(((index + 1) / totalChunks) * 100)
      uploadLog.value.push(`分片 ${index + 1}/${totalChunks} 上传完成。`)
    }

    uploadLog.value.push('所有分片上传完成，开始合并文件。')
    console.log('Calling uploadApi.merge...')
    await uploadApi.merge({
      uploadId: initResult.uploadId,
      fileMd5,
      fileName: currentFile.name
    })
    console.log('uploadApi.merge completed')
    uploadLog.value.push('文件合并成功，创建解析任务。')
    const task = await taskApi.create({ uploadId: initResult.uploadId })
    ElMessage.success('上传成功，解析任务已创建')
    router.push(`/tasks/${task.id}`)
  } catch (error: any) {
    console.error('Upload failed:', error)
    uploadLog.value.push(`❌ 上传失败：${error.message || '未知错误'}`)
    ElMessage.error(error.message || '上传失败，请检查网络和登录状态')
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <div class="page-shell upload-page">
    <section class="upload-header panel fade-in-up">
      <div>
        <div class="mono upload-kicker">CHUNK UPLOAD · RESUME · FAST HIT</div>
        <h1 class="page-title">上传一条视频，启动完整解析链路</h1>
        <p class="page-copy">
          前端会自动切片、计算 MD5、识别已上传分片，后端继续接管文件合并、异步投递和后续 AI 处理流程。
        </p>
      </div>

      <div class="upload-features">
        <span>分片上传</span>
        <span>断点续传</span>
        <span>秒传去重</span>
        <span>自动建任务</span>
      </div>
    </section>

    <section class="upload-grid">
      <div class="panel upload-stage">
        <div class="section-header">
          <div>
            <h3 class="section-heading">文件投递</h3>
            <p class="section-description">支持本地大文件上传，适合长视频解析场景演示。</p>
          </div>
        </div>

        <el-upload drag :auto-upload="false" :show-file-list="true" :limit="1" @change="handleFileChange">
          <el-icon size="42"><UploadFilled /></el-icon>
          <div class="el-upload__text">拖拽视频到这里，或点击选择文件</div>
          <template #tip>
            <div class="upload-tip">建议使用 MP4 等常见视频格式，便于后续音频提取与转写处理。</div>
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

        <div class="progress-block">
          <div class="progress-label">
            <span>上传进度</span>
            <strong>{{ progress }}%</strong>
          </div>
          <el-progress :percentage="progress" color="#ffb761" :stroke-width="10" />
        </div>

        <el-button type="warning" :loading="uploading" @click="startUpload">开始上传并创建任务</el-button>
      </div>

      <div class="panel process-board">
        <div class="section-header">
          <div>
            <h3 class="section-heading">链路说明</h3>
            <p class="section-description">上传只是第一步，文件完成后会进入完整的视频理解流程。</p>
          </div>
        </div>

        <div class="process-list">
          <div>
            <strong>1. 计算 MD5</strong>
            <p>识别重复文件，支持内容级去重与秒传。</p>
          </div>
          <div>
            <strong>2. 分片上传</strong>
            <p>前端切片上传，服务端记录进度，支持断点续传。</p>
          </div>
          <div>
            <strong>3. 合并文件</strong>
            <p>后端完成合并后，落 MinIO 并创建视频任务。</p>
          </div>
          <div>
            <strong>4. 异步处理</strong>
            <p>任务进入转写、摘要、索引与问答准备阶段。</p>
          </div>
        </div>
      </div>
    </section>

    <section class="panel upload-log">
      <div class="section-header">
        <div>
          <h3 class="section-heading">上传日志</h3>
          <p class="section-description">这里会持续显示 MD5、分片和任务创建过程，方便演示断点续传与秒传能力。</p>
        </div>
      </div>

      <div class="logs">
        <p v-if="uploadLog.length === 0" class="log-empty">选择文件后，这里会出现完整的上传过程记录。</p>
        <p v-for="(item, index) in uploadLog" :key="index">{{ item }}</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.upload-page {
  gap: 20px;
}

.upload-header,
.upload-stage,
.process-board,
.upload-log {
  border-radius: 30px;
  padding: 24px;
}

.upload-kicker {
  color: var(--accent);
  font-size: 12px;
  letter-spacing: 0.12em;
}

.upload-features {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.upload-features span {
  padding: 12px 16px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.03);
}

.upload-grid {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 20px;
}

.upload-stage {
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
  gap: 14px;
}

.file-summary span,
.progress-label span {
  color: var(--muted);
  font-size: 13px;
}

.file-summary strong {
  display: block;
  margin-top: 6px;
  word-break: break-word;
}

.progress-block {
  display: grid;
  gap: 10px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.process-list {
  display: grid;
  gap: 18px;
}

.process-list div {
  padding-bottom: 18px;
  border-bottom: 1px solid var(--line);
}

.process-list div:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.process-list strong {
  font-size: 16px;
}

.process-list p {
  margin: 8px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.logs {
  display: grid;
  gap: 10px;
}

.logs p {
  margin: 0;
  padding-left: 14px;
  border-left: 2px solid rgba(255, 183, 97, 0.6);
  line-height: 1.7;
  color: var(--text-soft);
}

.log-empty {
  color: var(--muted) !important;
  border-left-style: dashed !important;
}

@media (max-width: 1100px) {
  .upload-grid {
    grid-template-columns: 1fr;
  }
}
</style>
