<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  House,
  Connection,
  ChatDotRound,
  List,
  Monitor,
  Setting
} from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import HomeView from '../views/HomeView.vue'
import ConvertView from '../views/ConvertView.vue'
import ChatView from '../views/ChatView.vue'
import TaskListView from '../views/TaskListView.vue'
import MonitorView from '../views/MonitorView.vue'

const router = useRouter()
const authStore = useAuthStore()
const activeTab = ref('home')

const navItems = [
  { id: 'home', label: '主页概览', icon: House },
  { id: 'convert', label: '转换工作区', icon: Connection },
  { id: 'chat', label: 'AI 聊天', icon: ChatDotRound },
  { id: 'tasks', label: '任务列表', icon: List },
  { id: 'monitor', label: '系统监控', icon: Monitor }
]

const currentLabel = computed(() => {
  const item = navItems.find(i => i.id === activeTab.value)
  return item?.label || ''
})

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

const switchTab = (tabId: string) => {
  activeTab.value = tabId
}
</script>

<template>
  <div class="app-container">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="brand-header">
        <div class="brand-icon">
          <el-icon :size="20"><ArrowRightLeft /></el-icon>
        </div>
        <span class="brand-name">AutoScribe</span>
      </div>

      <nav class="nav-menu">
        <button
          v-for="item in navItems"
          :key="item.id"
          :class="['nav-item', { active: activeTab === item.id }]"
          @click="switchTab(item.id)"
        >
          <el-icon :size="16" :class="{ 'text-white': activeTab === item.id }">
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.label }}</span>
        </button>
      </nav>

      <div class="sidebar-footer">
        <div class="user-info">
          <span class="username">{{ authStore.user?.nickname || authStore.user?.username }}</span>
          <el-button link size="small" @click="handleLogout">退出</el-button>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <header class="top-header">
        <div class="header-title">{{ currentLabel }}</div>
        <div class="header-actions">
          <el-tag type="success" effect="plain" size="small">Gemini 2.5 Flash Online</el-tag>
          <el-icon :size="20" class="settings-icon"><Setting /></el-icon>
        </div>
      </header>

      <div class="content-area">
        <HomeView v-if="activeTab === 'home'" @navigate="switchTab" />
        <ConvertView v-else-if="activeTab === 'convert'" />
        <ChatView v-else-if="activeTab === 'chat'" />
        <TaskListView v-else-if="activeTab === 'tasks'" />
        <MonitorView v-else-if="activeTab === 'monitor'" />
      </div>
    </main>
  </div>
</template>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
  background: #FBFBFE;
  color: #1e293b;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 侧边栏 */
.sidebar {
  width: 256px;
  background: white;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  z-index: 20;
}

.brand-header {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid #f1f5f9;
}

.brand-icon {
  width: 32px;
  height: 32px;
  background: #4f46e5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.15);
}

.brand-icon .el-icon {
  color: white;
}

.brand-name {
  font-weight: bold;
  font-size: 18px;
  letter-spacing: -0.02em;
  font-style: italic;
}

.nav-menu {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  width: 100%;
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 12px;
  transition: all 0.2s;
  background: transparent;
  border: none;
  cursor: pointer;
  gap: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
}

.nav-item:hover {
  background: #f8fafc;
}

.nav-item.active {
  background: #4f46e5;
  color: white;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.nav-item.active .el-icon {
  color: white;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #f1f5f9;
}

.user-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: #f8fafc;
  border-radius: 10px;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

/* 主内容区 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.top-header {
  height: 64px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  flex-shrink: 0;
}

.header-title {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.settings-icon {
  color: #cbd5e1;
  cursor: pointer;
  transition: color 0.2s;
}

.settings-icon:hover {
  color: #4f46e5;
}

.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
}

@media (max-width: 768px) {
  .sidebar {
    width: 200px;
  }
  
  .content-area {
    padding: 20px;
  }
}
</style>
