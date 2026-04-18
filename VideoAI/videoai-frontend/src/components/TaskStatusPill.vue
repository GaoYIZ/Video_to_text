<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  statusCode?: string
}>()

const theme = computed(() => {
  switch (props.statusCode) {
    case 'SUCCESS':
      return '#46c29d'
    case 'FAILED':
      return '#f56c6c'
    case 'SUMMARIZING':
    case 'TRANSCRIBING':
    case 'INDEXING':
      return '#f6b655'
    default:
      return '#7b8aa0'
  }
})
</script>

<template>
  <span class="pill" :style="{ borderColor: `${theme}55`, color: theme, background: `${theme}12` }">
    {{ statusCode || 'PENDING' }}
  </span>
</template>

<style scoped>
.pill {
  border: 1px solid;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 700;
}
</style>
