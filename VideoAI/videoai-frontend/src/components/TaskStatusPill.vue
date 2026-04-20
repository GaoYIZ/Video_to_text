<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  statusCode?: string
}>()

const statusMap: Record<string, { label: string; color: string }> = {
  PENDING: { label: '待处理', color: '#8a99ad' },
  UPLOADED: { label: '已上传', color: '#63a4ff' },
  QUEUED: { label: '排队中', color: '#7c8eff' },
  PROCESSING_AUDIO: { label: '提取音频', color: '#eea95a' },
  TRANSCRIBING: { label: '语音转写', color: '#eea95a' },
  SUMMARIZING: { label: '生成摘要', color: '#eea95a' },
  INDEXING: { label: '建立索引', color: '#9d8fff' },
  SUCCESS: { label: '处理完成', color: '#52a171' },
  FAILED: { label: '处理失败', color: '#e27070' }
}

const current = computed(() => statusMap[props.statusCode || 'PENDING'] || statusMap.PENDING)
</script>

<template>
  <span
    class="pill"
    :style="{ borderColor: `${current.color}44`, color: current.color, background: `${current.color}16` }"
  >
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

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  display: inline-block;
  background: currentColor;
}
</style>
