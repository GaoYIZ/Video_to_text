<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'
import { User, Avatar, Promotion } from '@element-plus/icons-vue'
import { taskApi, chatApi, agentApi } from '../api/modules'
import type { TaskItem } from '../types'

interface Message {
  role: 'user' | 'bot'
  content: string
}

const loading = ref(false)
const tasks = ref<TaskItem[]>([])
const selectedTaskId = ref<number>()
const chatMessages = ref<Message[]>([
  { role: 'bot', content: '你好！我是您的 AI 助理。您可以问我关于转录内容的任何问题，或者让我为您提供建议。' }
])
const userInput = ref('')
const isAiLoading = ref(false)
const chatMode = ref<'chat' | 'agent'>('chat')
const chatContainerRef = ref<HTMLElement>()

const selectedTask = computed(() => tasks.value.find((item) => item.id === selectedTaskId.value))

const loadTasks = async () => {
  loading.value = true
  try {
    const result = await taskApi.page({ current: 1, pageSize: 20 })
    tasks.value = result.records
    if (!selectedTaskId.value && result.records.length > 0) {
      selectedTaskId.value = result.records[0].id
    }
  } finally {
    loading.value = false
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatContainerRef.value) {
    chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
  }
}

const handleSendMessage = async () => {
  if (!userInput.value.trim() || !selectedTaskId.value) return
  
  const newMsg: Message = { role: 'user', content: userInput.value }
  chatMessages.value.push(newMsg)
  const question = userInput.value
  userInput.value = ''
  isAiLoading.value = true
  
  await scrollToBottom()

  try {
    let response
    if (chatMode.value === 'chat') {
      // RAG 问答
      response = await chatApi.ask({ 
        taskId: selectedTaskId.value, 
        question 
      })
    } else {
      // Agent 问答
      response = await agentApi.ask({ 
        taskId: selectedTaskId.value, 
        question 
      })
    }
    
    chatMessages.value.push({ 
      role: 'bot', 
      content: response.answer 
    })
  } catch (error: any) {
    chatMessages.value.push({ 
      role: 'bot', 
      content: error.message || '抱歉，处理您的请求时出现了错误。' 
    })
  } finally {
    isAiLoading.value = false
    await scrollToBottom()
  }
}

const handleKeyPress = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSendMessage()
  }
}

onMounted(loadTasks)
</script>

<template>
  <div class="chat-container">
    <!-- Task Selector -->
    <div v-if="!selectedTaskId" class="task-selection panel">
      <h2 class="section-title">选择任务</h2>
      <p class="section-desc">推荐选择已经完成转写和摘要的任务，以便获得更完整的问答结果。</p>
      
      <div class="task-list" v-loading="loading">
        <button
          v-for="task in tasks"
          :key="task.id"
          :class="['task-button', { active: task.id === selectedTaskId }]"
          @click="selectedTaskId = task.id"
        >
          <div class="task-top">
            <strong>{{ task.videoTitle }}</strong>
            <el-tag size="small" :type="task.statusCode === 'COMPLETED' ? 'success' : 'warning'">
              {{ task.currentStep }}
            </el-tag>
          </div>
          <p>{{ task.taskNo }}</p>
        </button>
        
        <div v-if="tasks.length === 0 && !loading" class="empty-tasks">
          暂无任务，请先上传视频
        </div>
      </div>
    </div>

    <!-- Chat Interface -->
    <div v-else class="chat-interface">
      <!-- Selected Task Info -->
      <div class="task-info-bar panel-muted">
        <div class="info-item">
          <span>当前任务</span>
          <strong>{{ selectedTask?.videoTitle }}</strong>
        </div>
        <div class="info-item">
          <span>任务编号</span>
          <strong>{{ selectedTask?.taskNo }}</strong>
        </div>
        <el-button link @click="selectedTaskId = undefined">切换任务</el-button>
      </div>

      <!-- Mode Selector -->
      <div class="mode-selector">
        <button
          :class="['mode-btn', { active: chatMode === 'chat' }]"
          @click="chatMode = 'chat'"
        >
          RAG 问答
        </button>
        <button
          :class="['mode-btn', { active: chatMode === 'agent' }]"
          @click="chatMode = 'agent'"
        >
          Agent 问答
        </button>
      </div>

      <!-- Chat Messages -->
      <div class="chat-messages" ref="chatContainerRef">
        <div
          v-for="(msg, idx) in chatMessages"
          :key="idx"
          :class="['message-row', msg.role === 'user' ? 'user-message' : 'bot-message']"
        >
          <div class="message-content">
            <div :class="['avatar', msg.role]">
              <el-icon :size="20"><component :is="msg.role === 'user' ? User : Avatar" /></el-icon>
            </div>
            <div :class="['message-bubble', msg.role]">
              {{ msg.content }}
            </div>
          </div>
        </div>
        
        <div v-if="isAiLoading" class="typing-indicator">
          AI 正在输入...
        </div>
      </div>

      <!-- Input Area -->
      <div class="chat-input-area">
        <div class="input-wrapper">
          <textarea
            v-model="userInput"
            @keydown="handleKeyPress"
            placeholder="发送消息..."
            rows="1"
            class="chat-input"
          />
          <el-button 
            type="primary" 
            :disabled="!userInput.trim()"
            @click="handleSendMessage"
            class="send-btn"
          >
            <el-icon><Promotion /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-container {
  max-width: 1200px;
  margin: 0 auto;
  height: calc(100vh - 160px);
  display: flex;
  flex-direction: column;
}

.panel {
  background: white;
  border-radius: 30px;
  padding: 24px;
  border: 1px solid #e2e8f0;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.section-desc {
  color: #94a3b8;
  font-size: 14px;
  margin: 0 0 24px 0;
}

/* Task Selection */
.task-list {
  display: grid;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.task-button {
  padding: 16px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  background: rgba(255, 255, 255, 0.78);
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
}

.task-button:hover,
.task-button.active {
  transform: translateY(-2px);
  border-color: rgba(99, 102, 241, 0.32);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.12);
}

.task-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 8px;
}

.task-button strong {
  color: #1e293b;
  font-size: 15px;
}

.task-button p {
  margin: 0;
  color: #94a3b8;
  font-size: 13px;
}

.empty-tasks {
  text-align: center;
  padding: 40px;
  color: #94a3b8;
}

/* Chat Interface */
.chat-interface {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 16px;
}

.task-info-bar {
  border-radius: 16px;
  padding: 16px 18px;
  display: flex;
  gap: 24px;
  align-items: center;
  background: #f8fafc;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item span {
  color: #94a3b8;
  font-size: 12px;
}

.info-item strong {
  color: #1e293b;
  font-size: 14px;
}

.mode-selector {
  display: flex;
  gap: 8px;
  background: #f1f5f9;
  padding: 4px;
  border-radius: 12px;
  width: fit-content;
}

.mode-btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.2s;
  background: transparent;
  border: none;
  cursor: pointer;
  color: #64748b;
}

.mode-btn.active {
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  color: #4f46e5;
}

/* Chat Messages */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: white;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
}

.message-row {
  display: flex;
  animation: fadeIn 0.3s ease-out;
}

.user-message {
  justify-content: flex-end;
}

.bot-message {
  justify-content: flex-start;
}

.message-content {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.user-message .message-content {
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar.user {
  background: #1e293b;
  color: white;
}

.avatar.bot {
  background: #4f46e5;
  color: white;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
}

.message-bubble.user {
  background: #4f46e5;
  color: white;
}

.message-bubble.bot {
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  color: #1e293b;
}

.typing-indicator {
  font-size: 12px;
  color: #94a3b8;
  margin-left: 48px;
}

/* Input Area */
.chat-input-area {
  padding: 16px 0;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 8px 12px;
}

.chat-input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  padding: 8px;
  font-size: 14px;
  resize: none;
  font-family: inherit;
  max-height: 120px;
}

.send-btn {
  border-radius: 12px;
  padding: 8px 16px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .chat-container {
    height: calc(100vh - 140px);
  }
  
  .task-info-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .message-content {
    max-width: 90%;
  }
}
</style>
