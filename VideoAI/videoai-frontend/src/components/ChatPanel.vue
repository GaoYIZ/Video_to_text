<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { agentApi, chatApi } from '../api/modules'
import type { ChatAnswer, TranscriptSegment } from '../types'

const props = defineProps<{
  taskId: number
  mode: 'chat' | 'agent'
}>()

const messages = ref<{ role: string; content: string }[]>([])
const sessionId = ref(`${props.mode}-${props.taskId}`)
const loading = ref(false)
const question = ref('')
const citedSegments = ref<TranscriptSegment[]>([])

const title = computed(() => (props.mode === 'agent' ? 'VideoQAAgent' : 'RAG 问答'))

const ask = async () => {
  if (!question.value.trim()) return
  loading.value = true
  messages.value.push({ role: 'USER', content: question.value })
  try {
    const api = props.mode === 'agent' ? agentApi.ask : chatApi.ask
    const response: ChatAnswer = await api({
      taskId: props.taskId,
      question: question.value,
      sessionId: sessionId.value
    })
    sessionId.value = response.sessionId
    citedSegments.value = response.citedSegments
    messages.value.push({ role: 'ASSISTANT', content: response.answer })
    question.value = ''
  } catch (error) {
    ElMessage.error('问答失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="chat panel">
    <div class="chat-head">
      <div>
        <div class="mono chat-label">{{ title }}</div>
        <h3>{{ mode === 'agent' ? '工具增强问答' : '内容检索问答' }}</h3>
      </div>
      <span class="session mono">{{ sessionId }}</span>
    </div>

    <div class="chat-log">
      <div v-if="messages.length === 0" class="empty-state">
        可以直接提问：这个视频讲了什么、任务为什么失败、哪些片段提到了 Agent？
      </div>
      <div v-for="(item, index) in messages" :key="index" :class="['bubble', item.role.toLowerCase()]">
        <span class="mono">{{ item.role }}</span>
        <p>{{ item.content }}</p>
      </div>
    </div>

    <div v-if="citedSegments.length" class="citation-strip">
      <div v-for="segment in citedSegments.slice(0, 2)" :key="segment.id" class="citation">
        <div class="mono">{{ segment.startTimeMs / 1000 }}s - {{ segment.endTimeMs / 1000 }}s</div>
        <p>{{ segment.content }}</p>
      </div>
    </div>

    <div class="composer">
      <el-input
        v-model="question"
        type="textarea"
        :rows="3"
        resize="none"
        placeholder="输入问题，例如：请总结视频里的工程亮点"
      />
      <el-button type="warning" :loading="loading" @click="ask">发送</el-button>
    </div>
  </section>
</template>

<style scoped>
.chat {
  border-radius: 24px;
  padding: 22px;
  display: grid;
  gap: 18px;
}

.chat-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.chat-label,
.session {
  color: var(--muted);
  font-size: 12px;
}

.chat-head h3 {
  margin: 6px 0 0;
  font-size: 22px;
}

.chat-log {
  min-height: 220px;
  max-height: 420px;
  overflow: auto;
  display: grid;
  gap: 12px;
}

.empty-state {
  color: var(--muted);
  padding: 28px 0;
}

.bubble {
  max-width: 85%;
  border-radius: 20px;
  padding: 14px 16px;
  border: 1px solid var(--line);
}

.bubble.user {
  justify-self: end;
  background: rgba(246, 182, 85, 0.08);
}

.bubble.assistant {
  background: rgba(255, 255, 255, 0.04);
}

.bubble span {
  display: block;
  color: var(--muted);
  margin-bottom: 8px;
  font-size: 11px;
}

.bubble p,
.citation p {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.7;
}

.citation-strip {
  display: grid;
  gap: 10px;
}

.citation {
  border: 1px dashed var(--line);
  border-radius: 18px;
  padding: 12px 14px;
}

.citation .mono {
  color: var(--accent);
  font-size: 12px;
  margin-bottom: 8px;
}

.composer {
  display: grid;
  gap: 12px;
}
</style>
