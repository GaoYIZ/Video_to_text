<template>
  <UploadPage
    title="上传音频"
    accept=".mp3,.wav,.m4a,.aac,.flac,.wma"
    format-hint="支持 mp3/wav/m4a/aac/flac/wma 格式,单个文件不超过 100MB"
    :steps="steps"
    :tips="tips"
    :on-process="handleUpload"
  />
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import UploadPage from './UploadVideoView.vue'
import { uploadAudioFile, processWithAi } from '../api/video'

const router = useRouter()

const steps = [
  { title: '上传', desc: '上传音频文件' },
  { title: '转写', desc: 'AI 语音识别' },
  { title: '总结', desc: '生成智能摘要' },
  { title: '完成', desc: '查看结果' }
]

const tips = [
  '音频文件比视频处理快 3-5 倍',
  '建议先转换音频再上传,节省流量',
  '支持常见音频格式,自动识别编码',
  '转写完成后自动生成 AI 总结'
]

const handleUpload = async ({ file, onProgress, onStepChange, onSuccess }) => {
  onStepChange(0)
  
  // 步骤1: 上传音频
  const uploadRes = await uploadAudioFile(file, onProgress)
  if (uploadRes.code !== 200) {
    throw new Error(uploadRes.message || '上传失败')
  }
  
  const videoId = uploadRes.data.id
  onStepChange(1)

  // 步骤2: AI 处理(音频无需转换)
  await processWithAi(videoId)
  onStepChange(2)
  
  onStepChange(3)
  onSuccess(videoId)
}
</script>
