<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { agentApi, chatApi } from '../api/modules'
import type { ChatAnswer, TranscriptSegment } from '../types'
import { formatSegmentTime } from '../utils/format'

const props = defineProps<{
  taskId: number
  mode: 'chat' | 'agent'
}>()

const messages = ref<{ role: 'USER' | 'ASSISTANT'; content: string; fromCache?: boolean }[]>([])
const sessionId = ref(`${props.mode}-${props.taskId}`)
const question = ref('')
const loading = ref(false)
const citedSegments = ref<TranscriptSegment[]>([])

const title = computed(() => (props.mode === 'agent' ? 'VideoQAAgent' : 'RAG 问答'))
const subtitle = computed(() =>
  props.mode === 'agent'
    ? '自动调用任务状态、摘要、转写与片段检索工具，给出更有上下文的回答。'
    : '先召回相关转写片段，再把结果交给模型生成回答。'
)
const suggestions = computed(() =>
  props.mode === 'agent'
    ? ['这个任务目前处理到哪一步？', '这条视频的核心内容是什么？', '如果任务失败，我该怎么排查？']
    : ['请概括这条视频的主要内容', '视频里提到了哪些工程亮点？', '请根据片段给我一份面试讲述稿']
)

const ask = async (preset?: string) => {
  const actualQuestion = (preset ?? question.value).trim()
  if (!actualQuestion) return

  loading.value = true
  messages.value.push({ role: 'USER', content: actualQuestion })
  try {
    const requestApi = props.mode === 'agent' ? agentApi.ask : chatApi.ask
    const result: ChatAnswer = await requestApi({
      taskId: props.taskId,
      question: actualQuestion,
      sessionId: sessionId.value
    })
    sessionId.value = result.sessionId
    citedSegments.value = result.citedSegments || []
    messages.value.push({
      role: 'ASSISTANT',
      content: result.answer,
      fromCache: result.fromCache
    })
    question.value = ''
  } catch (error) {
    ElMessage.error('问答失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="chat-panel panel">
    <div class="section-header">
      <div>
        <div class="mono panel-kicker">{{ title.toUpperCase() }}</div>
        <h3 class="section-heading">{{ title }}</h3>
        <p class="section-description">{{ subtitle }}</p>
      </div>
      <span class="session-id mono">{{ sessionId }}</span>
    </div>

    <div class="suggestion-row">
      <button v-for="item in suggestions" :key="item" type="button" @click="ask(item)">
        {{ item }}
      </button>
    </div>

    <div class="message-list">
      <div v-if="messages.length === 0" class="empty-state">
        你可以先从上方快捷问题开始，也可以直接输入围绕当前视频内容、处理状态或失败原因的问题。
      </div>

      <div v-for="(item, index) in messages" :key="index" :class="['message-bubble', item.role.toLowerCase()]">
        <div class="message-meta">
          <span class="mono">{{ item.role === 'USER' ? 'USER' : title.toUpperCase() }}</span>
          <span v-if="item.fromCache" class="cache-tag">缓存命中</span>
        </div>
        <p>{{ item.content }}</p>
      </div>
    </div>

    <div v-if="citedSegments.length" class="citation-list">
      <div v-for="segment in citedSegments.slice(0, 3)" :key="segment.id" class="citation-card">
        <div class="mono citation-time">
          {{ formatSegmentTime(segment.startTimeMs) }} - {{ formatSegmentTime(segment.endTimeMs) }}
        </div>
        <p>{{ segment.content }}</p>
      </div>
    </div>

    <div class="composer">
      <el-input
        v-model="question"
        type="textarea"
        :rows="3"
        resize="none"
        placeholder="输入你的问题，例如：请帮我总结视频中的工程亮点，并指出最值得追问的部分。"
      />
      <el-button type="warning" :loading="loading" @click="ask()">发送问题</el-button>
    </div>
  </section>
</template>

<style scoped>
.chat-panel {
  border-radius: 30px;
  padding: 24px;
  display: grid;
  gap: 16px;
}

.panel-kicker,
.session-id,
.citation-time {
  color: var(--muted);
  font-size: 12px;
}

.suggestion-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.suggestion-row button {
  padding: 10px 14px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.78);
  color: var(--text-soft);
  cursor: pointer;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.suggestion-row button:hover {
  transform: translateY(-1px);
  border-color: rgba(111, 168, 255, 0.28);
  box-shadow: 0 10px 24px rgba(111, 168, 255, 0.1);
}

.message-list {
  min-height: 260px;
  max-height: 420px;
  overflow: auto;
  display: grid;
  gap: 12px;
}

.empty-state {
  min-height: 180px;
  display: grid;
  place-items: center;
  text-align: center;
  color: var(--muted);
  line-height: 1.8;
}

.message-bubble {
  max-width: 90%;
  padding: 16px;
  border-radius: 22px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.82);
}

.message-bubble.user {
  justify-self: end;
  background: rgba(111, 168, 255, 0.14);
}

.message-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.cache-tag {
  color: var(--accent-strong);
  font-size: 12px;
}

.message-bubble p,
.citation-card p {
  margin: 10px 0 0;
  line-height: 1.75;
  white-space: pre-wrap;
}

.citation-list {
  display: grid;
  gap: 10px;
}

.citation-card {
  border-radius: 18px;
  border: 1px dashed var(--line-strong);
  background: rgba(248, 251, 255, 0.92);
  padding: 12px 14px;
}

.composer {
  display: grid;
  gap: 12px;
}

@media (max-width: 760px) {
  .message-bubble {
    max-width: 100%;
  }
}
</style>
