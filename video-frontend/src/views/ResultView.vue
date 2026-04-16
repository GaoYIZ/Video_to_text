<template>
  <div class="result-page">
    <!-- 顶部信息栏 -->
    <header class="result-header">
      <el-button text @click="$router.push('/')">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      
      <div class="file-info">
        <h2>{{ videoInfo?.originalName || '加载中...' }}</h2>
        <div class="meta-tags">
          <el-tag size="small" :type="getStatusType(videoInfo?.status)">
            {{ getStatusText(videoInfo?.status) }}
          </el-tag>
          <span class="meta-text">
            <el-icon><Clock /></el-icon>
            {{ formatTime(videoInfo?.createTime) }}
          </span>
        </div>
      </div>

      <div class="header-actions">
        <el-button type="primary" @click="downloadResult">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </header>

    <!-- 主内容区 - 三栏布局 -->
    <main class="result-main" v-loading="loading">
      <el-row :gutter="20">
        <!-- 左侧:转录文本 -->
        <el-col :xs="24" :lg="16">
          <el-card class="transcript-card">
            <template #header>
              <div class="card-header">
                <el-icon><Document /></el-icon>
                <span>完整转录文本</span>
                <el-button 
                  size="small" 
                  text
                  @click="copyTranscript"
                >
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-button>
              </div>
            </template>
            
            <div class="transcript-content">
              <p v-if="videoInfo?.transcript">{{ videoInfo.transcript }}</p>
              <el-empty v-else description="暂无转录文本" />
            </div>
          </el-card>
        </el-col>

        <!-- 右侧:AI 智能分析 (三个Tab) -->
        <el-col :xs="24" :lg="8">
          <el-card class="ai-analysis-card">
            <el-tabs v-model="activeTab" class="analysis-tabs">
              <!-- Tab 1: 全文摘要 -->
              <el-tab-pane label="全文摘要" name="summary">
                <div class="tab-content">
                  <div v-if="videoInfo?.summary" class="summary-content" v-html="formatSummary(videoInfo.summary)"></div>
                  <el-empty v-else description="等待 AI 生成总结..." />
                </div>
              </el-tab-pane>

              <!-- Tab 2: 思维导图 -->
              <el-tab-pane label="思维导图" name="mindmap">
                <div class="tab-content">
                  <div v-if="mindmapData" class="mindmap-container">
                    <el-tree
                      :data="mindmapData"
                      :props="treeProps"
                      default-expand-all
                      class="mindmap-tree"
                    >
                      <template #default="{ node, data }">
                        <span class="tree-node">
                          <el-icon v-if="data.icon" :color="data.color"><component :is="data.icon" /></el-icon>
                          {{ node.label }}
                        </span>
                      </template>
                    </el-tree>
                  </div>
                  <el-empty v-else description="正在生成思维导图..." />
                </div>
              </el-tab-pane>

              <!-- Tab 3: 智能问答 -->
              <el-tab-pane label="智能问答" name="chat">
                <div class="chat-container">
                  <div class="chat-messages">
                    <div 
                      v-for="(msg, index) in chatMessages" 
                      :key="index"
                      class="message"
                      :class="msg.role"
                    >
                      <div class="message-avatar">
                        <el-avatar :size="32" v-if="msg.role === 'ai'" style="background: #9333EA">AI</el-avatar>
                        <el-avatar :size="32" v-else>U</el-avatar>
                      </div>
                      <div class="message-content">{{ msg.content }}</div>
                    </div>
                    
                    <div v-if="chatLoading" class="message ai-message">
                      <div class="message-avatar">
                        <el-avatar :size="32" style="background: #9333EA">AI</el-avatar>
                      </div>
                      <div class="message-content typing">
                        <span></span><span></span><span></span>
                      </div>
                    </div>
                  </div>

                  <div class="chat-input-area">
                    <el-input
                      v-model="chatInput"
                      placeholder="针对此视频提问..."
                      @keyup.enter="sendChatMessage"
                      clearable
                    >
                      <template #append>
                        <el-button @click="sendChatMessage" :loading="chatLoading">
                          <el-icon><Promotion /></el-icon>
                        </el-button>
                      </template>
                    </el-input>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-col>
      </el-row>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getVideoInfo } from '../api/video'

const route = useRoute()
const loading = ref(false)
const videoInfo = ref(null)
const activeTab = ref('summary')
const chatInput = ref('')
const chatLoading = ref(false)
const chatMessages = ref([
  {
    role: 'ai',
    content: '你好!我可以回答关于这个视频内容的任何问题。'
  }
])

// 模拟思维导图数据
const mindmapData = computed(() => {
  if (!videoInfo.value?.summary) return null
  
  // TODO: 从后端获取真实的思维导图数据
  return [
    {
      label: '核心主题',
      icon: 'Star',
      color: '#9333EA',
      children: [
        { label: '要点一:主要内容概述' },
        { label: '要点二:关键观点' },
        { label: '要点三:结论与建议' }
      ]
    }
  ]
})

const treeProps = {
  children: 'children',
  label: 'label'
}

// 获取视频信息
const fetchVideoInfo = async () => {
  const videoId = route.params.id
  if (!videoId) {
    ElMessage.error('无效的视频 ID')
    return
  }

  loading.value = true
  try {
    const res = await getVideoInfo(videoId)
    if (res.code === 200) {
      videoInfo.value = res.data
    } else {
      ElMessage.error(res.message || '获取失败')
    }
  } catch (error) {
    console.error('获取失败:', error)
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

// 复制转录文本
const copyTranscript = () => {
  if (videoInfo.value?.transcript) {
    navigator.clipboard.writeText(videoInfo.value.transcript)
    ElMessage.success('已复制到剪贴板')
  }
}

// 下载结果
const downloadResult = () => {
  if (!videoInfo.value) return
  
  const content = `
标题: ${videoInfo.value.originalName}
时间: ${formatTime(videoInfo.value.createTime)}

【转录文本】
${videoInfo.value.transcript || '无'}

【AI 总结】
${videoInfo.value.summary || '无'}
  `.trim()
  
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${videoInfo.value.originalName}_转写结果.txt`
  a.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('下载成功')
}

// 发送聊天消息
const sendChatMessage = async () => {
  if (!chatInput.value.trim()) return
  
  const userMessage = chatInput.value
  chatMessages.value.push({
    role: 'user',
    content: userMessage
  })
  
  chatInput.value = ''
  chatLoading.value = true
  
  // TODO: 调用后端 AI 问答接口
  setTimeout(() => {
    chatMessages.value.push({
      role: 'ai',
      content: '这是一个模拟回答。实际使用时需要连接后端的 AI 问答接口,基于视频内容进行智能回复。'
    })
    chatLoading.value = false
  }, 1500)
}

// 格式化总结
const formatSummary = (text) => {
  if (!text) return ''
  let formatted = text.replace(/\n/g, '<br>')
  formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  return formatted
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const getStatusType = (status) => {
  const types = {
    'UPLOADED': 'info',
    'CONVERTED': 'warning',
    'SUMMARIZED': 'success'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'UPLOADED': '已上传',
    'CONVERTED': '已转换',
    'SUMMARIZED': '已完成'
  }
  return texts[status] || status
}

onMounted(() => {
  fetchVideoInfo()
})
</script>

<style scoped>
.result-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #F5F7FF 0%, #FFFFFF 100%);
}

.result-header {
  background: #fff;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 16px;
  position: sticky;
  top: 0;
  z-index: 10;
}

.file-info {
  flex: 1;
}

.file-info h2 {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.meta-tags {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta-text {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #8c8c8c;
}

.result-main {
  padding: 24px;
  max-width: 1600px;
  margin: 0 auto;
}

.transcript-card,
.ai-analysis-card {
  border-radius: 12px;
  height: calc(100vh - 140px);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #262626;
}

.transcript-content {
  height: calc(100% - 60px);
  overflow-y: auto;
  padding: 16px;
  line-height: 1.8;
  color: #595959;
  white-space: pre-wrap;
}

.analysis-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.analysis-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: hidden;
}

.tab-content {
  height: 100%;
  overflow-y: auto;
  padding: 16px;
}

.summary-content {
  line-height: 1.8;
  color: #595959;
}

.summary-content :deep(strong) {
  color: #9333EA;
}

/* 思维导图 */
.mindmap-container {
  padding: 8px;
}

.mindmap-tree {
  background: transparent;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

/* 聊天界面 */
.chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 75%;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
}

.ai-message .message-content {
  background: #f5f5f5;
  color: #262626;
  border-radius: 12px 12px 12px 4px;
}

.user-message .message-content {
  background: linear-gradient(135deg, #C084FC 0%, #9333EA 100%);
  color: #fff;
  border-radius: 12px 12px 4px 12px;
}

.typing span {
  display: inline-block;
  width: 6px;
  height: 6px;
  margin: 0 2px;
  background: #9333EA;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing span:nth-child(2) { animation-delay: 0.2s; }
.typing span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-8px); }
}

.chat-input-area {
  padding: 12px;
  border-top: 1px solid #f0f0f0;
}

@media (max-width: 992px) {
  .transcript-card,
  .ai-analysis-card {
    height: auto;
    margin-bottom: 16px;
  }
  
  .transcript-content {
    max-height: 400px;
  }
}
</style>
