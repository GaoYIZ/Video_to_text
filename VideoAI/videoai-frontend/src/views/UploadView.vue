<script setup lang="ts">
import SparkMD5 from 'spark-md5'
import { ref } from 'vue'
import { ElMessage, type UploadFile } from 'element-plus'
import { taskApi, uploadApi } from '../api/modules'
import { useRouter } from 'vue-router'

const router = useRouter()
const file = ref<File | null>(null)
const uploading = ref(false)
const progress = ref(0)
const uploadLog = ref<string[]>([])

const beforeSelect = (uploadFile: UploadFile) => {
  file.value = uploadFile.raw || null
  progress.value = 0
  uploadLog.value = []
  return false
}

const computeMd5 = async (target: File) => {
  const spark = new SparkMD5.ArrayBuffer()
  const chunkSize = 5 * 1024 * 1024
  const totalChunks = Math.ceil(target.size / chunkSize)
  for (let index = 0; index < totalChunks; index++) {
    const chunk = target.slice(index * chunkSize, Math.min(target.size, (index + 1) * chunkSize))
    const buffer = await chunk.arrayBuffer()
    spark.append(buffer)
  }
  return spark.end()
}

const startUpload = async () => {
  if (!file.value) {
    ElMessage.warning('请先选择视频文件')
    return
  }
  uploading.value = true
  try {
    const currentFile = file.value
    const chunkSize = 5 * 1024 * 1024
    const totalChunks = Math.ceil(currentFile.size / chunkSize)
    uploadLog.value.push('正在计算文件 MD5...')
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
      uploadLog.value.push('命中秒传，直接创建解析任务')
      const task = await taskApi.create({ uploadId: initResult.uploadId })
      router.push(`/tasks/${task.id}`)
      return
    }

    const uploadedSet = new Set(initResult.uploadedChunks || [])
    for (let index = 0; index < totalChunks; index++) {
      if (uploadedSet.has(index)) continue
      const chunk = currentFile.slice(index * chunkSize, Math.min(currentFile.size, (index + 1) * chunkSize))
      const formData = new FormData()
      formData.append('uploadId', initResult.uploadId)
      formData.append('fileMd5', fileMd5)
      formData.append('chunkIndex', String(index))
      formData.append('chunkFile', chunk, `${currentFile.name}.part${index}`)
      await uploadApi.uploadChunk(formData)
      progress.value = Math.round(((index + 1) / totalChunks) * 100)
      uploadLog.value.push(`分片 ${index + 1}/${totalChunks} 上传完成`)
    }

    uploadLog.value.push('开始合并分片...')
    await uploadApi.merge({
      uploadId: initResult.uploadId,
      fileMd5,
      fileName: currentFile.name
    })
    uploadLog.value.push('分片合并成功，创建解析任务...')
    const task = await taskApi.create({ uploadId: initResult.uploadId })
    ElMessage.success('上传并创建任务成功')
    router.push(`/tasks/${task.id}`)
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <div class="page-shell upload-page">
    <section class="upload-hero panel">
      <div>
        <div class="mono">UPLOAD + BREAKPOINT RESUME + MD5 DEDUP</div>
        <h1>上传一段视频，让整条 AI 管线开始运转。</h1>
        <p>前端会自动切片、计算 MD5、断点续传，后端再接 MinIO、RocketMQ、ASR、Summary、RAG 与 Agent。</p>
      </div>
      <div class="hero-steps">
        <span>Slice</span>
        <span>Resume</span>
        <span>Fast Hit</span>
        <span>Dispatch</span>
      </div>
    </section>

    <section class="upload-stage panel">
      <el-upload drag :auto-upload="false" :show-file-list="true" :limit="1" :before-upload="beforeSelect">
        <el-icon size="40"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽视频到这里，或点击选择文件</div>
      </el-upload>

      <el-progress :percentage="progress" color="#f6b655" :stroke-width="10" />
      <el-button type="warning" :loading="uploading" @click="startUpload">开始上传并创建任务</el-button>
    </section>

    <section class="upload-log panel">
      <div class="section-head">
        <div>
          <h3>上传日志</h3>
          <p>这里可以观察 MD5、分片上传、合并与任务创建过程。</p>
        </div>
      </div>
      <div class="logs">
        <p v-for="(item, index) in uploadLog" :key="index">{{ item }}</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.upload-page {
  display: grid;
  gap: 20px;
}

.upload-hero,
.upload-stage,
.upload-log {
  border-radius: 28px;
  padding: 24px;
}

.upload-hero {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 20px;
}

.upload-hero h1 {
  margin: 14px 0;
  font-size: clamp(34px, 4vw, 58px);
  letter-spacing: -0.06em;
  max-width: 760px;
}

.upload-hero p,
.section-head p {
  color: var(--muted);
  line-height: 1.8;
}

.hero-steps {
  display: grid;
  gap: 10px;
}

.hero-steps span {
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 12px 18px;
  text-align: center;
}

.upload-stage {
  display: grid;
  gap: 18px;
}

.logs {
  display: grid;
  gap: 10px;
}

.logs p {
  margin: 0;
  border-left: 2px solid var(--accent);
  padding-left: 12px;
  color: #d8dde5;
}

@media (max-width: 900px) {
  .upload-hero {
    grid-template-columns: 1fr;
  }
}
</style>
