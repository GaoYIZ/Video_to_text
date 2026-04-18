<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { agentApi, chatApi } from '../api/modules'
import type { ChatAnswer, TranscriptSegment } from '../types'

const props = defineProps<{
  taskId: number
  mode: 'chat' | 'agent'
}>()

const messages = ref<{ role: string; content: string; fromCache?: boolean }[]>([])
const sessionId = ref(`${props.mode}-${props.taskId}`)
const loading = ref(false)
const question = ref('')
const citedSegments = ref<TranscriptSegment[]>([])

const title = computed(() => (props.mode === 'agent' ? 'VideoQAAgent' : 'RAG 问答'))
const subtitle = computed(() =>
  props.mode === 'agent'
    ? '自动调用视频信息、任务状态、摘要和片段检索工具。'
    : '基于转写片段召回相关内容，再交给模型生成回答。'
)
const suggestions = computed(() =>
  props.mode === 'agent'
    ? ['这条视频目前处理到哪一步了？', '为什么这个任务处理失败？', '请总结视频中的核心工程亮点']
    : ['请概括这条视频的主要内容', '提到 Agent 的片段有哪些？', '帮我整理视频中的关键时间点']
)

const ask = async (customQuestion?: string) => {
  const actualQuestion = (customQuestion ?? question.value).trim()
  if (!actualQuestion) return

  loading.value = true
  messages.value.push({ role: 'USER', content: actualQuestion })

  try {
    const api = props.mode === 'agent' ? agentApi.ask : chatApi.ask
    const response: ChatAnswer = await api({
      taskId: props.taskId,
      question: actualQuestion,
      sessionId: sessionId.value
    })
    sessionId.value = response.sessionId
    citedSegments.value = response.citedSegments || []
    messages.value.push({
      role: 'ASSISTANT',
      content: response.answer,
      fromCache: response.fromCache
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
  <section class="chat panel">
    <div class="chat-head">
      <div>
        <div class="mono chat-label">{{ title }}</div>
        <h3>{{ props.mode === 'agent' ? '智能体问答' : '内容问答' }}</h3>
        <p>{{ subtitle }}</p>
      </div>
      <span class="session mono">{{ sessionId }}</span>
    </div>

    <div class="suggestions">
      <button v-for="item in suggestions" :key="item" type="button" @click="ask(item)">
        {{ item }}
      </button>
    </div>

    <div class="chat-log">
      <div v-if="messages.length === 0" class="empty-state">
        你可以直接围绕视频内容提问，也可以从上方快捷问题开始，快速体验多轮问答效果。
      </div>

      <div v-for="(item, index) in messages" :key="index" :class="['bubble', item.role.toLowerCase()]">
        <div class="bubble-meta">
          <span class="mono">{{ item.role === 'USER' ? 'USER' : title.toUpperCase() }}</span>
          <span v-if="item.fromCache" class="cache-tag">缓存命中</span>
        </div>
        <p>{{ item.content }}</p>
      </div>
    </div>

    <div v-if="citedSegments.length" class="citation-strip">
      <div v-for="segment in citedSegments.slice(0, 3)" :key="segment.id" class="citation">
        <div class="citation-time mono">
          {{ (segment.startTimeMs / 1000).toFixed(1) }}s - {{ (segment.endTimeMs / 1000).toFixed(1) }}s
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
        placeholder="输入你的问题，例如：请总结视频中的功能亮点，并指出最值得继续追问的方向。"
      />
      <el-button type="warning" :loading="loading" @click="ask()">发送问题</el-button>
    </div>
  </section>
</template>

<style scoped>
.chat {
  border-radius: 26px;
  padding: 22px;
  display: grid;
  gap: 16px;
}

.chat-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.chat-label,
.session {
  color: var(--muted);
  font-size: 12px;
}

.chat-head h3 {
  margin: 8px 0 0;
  font-size: 24px;
  letter-spacing: -0.04em;
}

.chat-head p {
  margin: 8px 0 0;
  color: var(--muted);
  line-height: 1.6;
  max-width: 520px;
}

.suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.suggestions button {
  border: 1px solid var(--line);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.03);
  color: var(--text-soft);
  padding: 10px 14px;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    background 0.2s ease;
}

.suggestions button:hover {
  transform: translateY(-1px);
  border-color: rgba(255, 183, 97, 0.3);
  background: rgba(255, 183, 97, 0.08);
}

.chat-log {
  min-height: 260px;
  max-height: 440px;
  overflow: auto;
  display: grid;
  gap: 12px;
  padding-right: 4px;
}

.empty-state {
  min-height: 180px;
  display: grid;
  place-items: center;
  text-align: center;
  color: var(--muted);
  line-height: 1.8;
}

.bubble {
  max-width: 88%;
  border-radius: 20px;
  padding: 15px 16px;
  border: 1px solid var(--line);
}

.bubble.user {
  justify-self: end;
  background: rgba(255, 183, 97, 0.09);
}

.bubble.assistant {
  background: rgba(255, 255, 255, 0.035);
}

.bubble-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.bubble-meta .mono {
  color: var(--muted);
  font-size: 11px;
}

.cache-tag {
  color: var(--accent);
  font-size: 12px;
}

.bubble p,
.citation p {
  margin: 10px 0 0;
  white-space: pre-wrap;
  line-height: 1.8;
}

.citation-strip {
  display: grid;
  gap: 10px;
}

.citation {
  border: 1px dashed var(--line);
  border-radius: 18px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.02);
}

.citation-time {
  color: var(--accent);
  font-size: 12px;
}

.composer {
  display: grid;
  gap: 12px;
}

@media (max-width: 760px) {
  .chat-head {
    display: grid;
  }

  .bubble {
    max-width: 100%;
  }
}
</style>
