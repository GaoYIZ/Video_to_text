<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  statusCode?: string
}>()

const statusMap: Record<string, { label: string; color: string }> = {
  PENDING: { label: '待处理', color: '#8f9db1' },
  UPLOADED: { label: '已上传', color: '#72b6ff' },
  QUEUED: { label: '排队中', color: '#6e9dff' },
  PROCESSING_AUDIO: { label: '提取音频', color: '#ffb861' },
  TRANSCRIBING: { label: '语音转写', color: '#ffb861' },
  SUMMARIZING: { label: '生成摘要', color: '#ffb861' },
  INDEXING: { label: '建立索引', color: '#b594ff' },
  SUCCESS: { label: '处理完成', color: '#4fd3a3' },
  FAILED: { label: '处理失败', color: '#ff7d7d' }
}

const current = computed(() => statusMap[props.statusCode || 'PENDING'] || statusMap.PENDING)
</script>

<template>
  <span class="pill" :style="{ borderColor: `${current.color}44`, color: current.color, background: `${current.color}14` }">
    <span class="status-dot" :style="{ color: current.color }" />
    <span>{{ current.label }}</span>
  </span>
</template>

<style scoped>
.pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1px solid;
  border-radius: 999px;
  padding: 7px 12px;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
}
</style>
