<template>
  <div class="app-layout">
    <!-- 左侧导航栏 -->
    <aside class="sidebar">
      <!-- Logo -->
      <div class="logo-area">
        <el-icon :size="32" color="#9333EA"><VideoCamera /></el-icon>
      </div>

      <!-- 导航菜单 -->
      <nav class="nav-menu">
        <div 
          class="nav-item" 
          :class="{ active: currentMenu === 'home' }"
          @click="currentMenu = 'home'"
        >
          <el-tooltip content="首页" placement="right">
            <el-icon :size="24"><HomeFilled /></el-icon>
          </el-tooltip>
        </div>

        <div 
          class="nav-item"
          :class="{ active: currentMenu === 'ai' }"
          @click="toggleAIDrawer"
        >
          <el-tooltip content="AI助手" placement="right">
            <el-icon :size="24"><ChatDotRound /></el-icon>
          </el-tooltip>
        </div>

        <div 
          class="nav-item"
          :class="{ active: currentMenu === 'folder' }"
          @click="currentMenu = 'folder'"
        >
          <el-tooltip content="文件夹" placement="right">
            <el-icon :size="24"><FolderOpened /></el-icon>
          </el-tooltip>
        </div>

        <div 
          class="nav-item"
          :class="{ active: currentMenu === 'more' }"
          @click="currentMenu = 'more'"
        >
          <el-tooltip content="更多" placement="right">
            <el-icon :size="24"><More /></el-icon>
          </el-tooltip>
        </div>
      </nav>

      <!-- 底部用户区 -->
      <div class="user-area">
        <div class="user-avatar">
          <el-avatar :size="40" style="background: #9333EA">U</el-avatar>
          <div class="upgrade-badge">权益升级</div>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- AI 助手抽屉 -->
    <el-drawer
      v-model="aiDrawerVisible"
      direction="rtl"
      size="350px"
      :with-header="false"
      class="ai-drawer"
    >
      <div class="ai-chat-container">
        <div class="chat-header">
          <h3>AI 智能助手</h3>
          <p>支持自然语言搜索和管理</p>
        </div>

        <div class="chat-messages">
          <div class="message ai-message">
            <div class="message-content">
              你好!我可以帮你:
              <ul>
                <li>🔍 搜索历史视频</li>
                <li>📝 生成内容总结</li>
                <li>❓ 回答相关问题</li>
              </ul>
            </div>
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="chatInput"
            placeholder="输入消息,如:找一下昨天的视频..."
            @keyup.enter="sendMessage"
          >
            <template #append>
              <el-button @click="sendMessage">
                <el-icon><Promotion /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const currentMenu = ref('home')
const aiDrawerVisible = ref(false)
const chatInput = ref('')

const toggleAIDrawer = () => {
  aiDrawerVisible.value = !aiDrawerVisible.value
}

const sendMessage = () => {
  if (!chatInput.value.trim()) return
  
  // TODO: 实现 AI 对话逻辑
  console.log('发送消息:', chatInput.value)
  chatInput.value = ''
}
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #F5F7FF 0%, #FFFFFF 100%);
}

/* 左侧导航栏 */
.sidebar {
  width: 72px;
  height: 100vh;
  background: #fff;
  border-right: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 1px 0 10px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 100;
}

.logo-area {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #f0f0f0;
}

.nav-menu {
  flex: 1;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  width: 48px;
  height: 48px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  color: #8c8c8c;
}

.nav-item:hover {
  background: #f5f5f5;
  color: #9333EA;
}

.nav-item.active {
  background: linear-gradient(135deg, #C084FC 0%, #9333EA 100%);
  color: #fff;
}

.user-area {
  padding: 16px 0;
  border-top: 1px solid #f0f0f0;
}

.user-avatar {
  position: relative;
  display: flex;
  justify-content: center;
}

.upgrade-badge {
  position: absolute;
  bottom: -8px;
  background: linear-gradient(135deg, #C084FC 0%, #9333EA 100%);
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(147, 51, 234, 0.3);
}

/* 主内容区 */
.main-content {
  margin-left: 72px;
  flex: 1;
  padding: 32px;
  min-height: 100vh;
}

/* AI 抽屉 */
.ai-drawer :deep(.el-drawer__body) {
  padding: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.ai-chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.chat-header h3 {
  margin: 0 0 4px;
  font-size: 18px;
  color: #262626;
}

.chat-header p {
  margin: 0;
  font-size: 13px;
  color: #8c8c8c;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message {
  margin-bottom: 16px;
}

.ai-message .message-content {
  background: #f5f5f5;
  padding: 12px 16px;
  border-radius: 12px 12px 12px 4px;
  max-width: 85%;
}

.message-content ul {
  margin: 8px 0 0;
  padding-left: 20px;
}

.message-content li {
  margin: 4px 0;
  font-size: 14px;
  color: #595959;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
}

@media (max-width: 768px) {
  .sidebar {
    width: 60px;
  }

  .main-content {
    margin-left: 60px;
    padding: 16px;
  }

  .nav-item {
    width: 40px;
    height: 40px;
  }
}
</style>
