<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const menus = [
  { label: '控制台', path: '/dashboard', icon: 'DataAnalysis' },
  { label: '视频上传', path: '/upload', icon: 'UploadFilled' },
  { label: '任务列表', path: '/tasks', icon: 'Tickets' },
  { label: '使用统计', path: '/usage', icon: 'PieChart' }
]

const currentTitle = computed(() => menus.find((item) => route.path.startsWith(item.path))?.label || 'VideoAI')

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="layout">
    <aside class="sidebar panel">
      <div class="brand fade-in-up">
        <div class="brand-mark">VA</div>
        <div>
          <div class="mono brand-sub">VIDEO UNDERSTANDING OPS</div>
          <h1>VideoAI</h1>
        </div>
      </div>

      <nav class="nav">
        <button
          v-for="item in menus"
          :key="item.path"
          :class="['nav-item', { active: route.path.startsWith(item.path) }]"
          @click="router.push(item.path)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </button>
      </nav>

      <div class="sidebar-foot">
        <div class="operator panel">
          <div class="mono">OPERATOR</div>
          <strong>{{ authStore.user?.nickname || authStore.user?.username }}</strong>
          <span>{{ authStore.user?.userNo }}</span>
        </div>
        <el-button type="warning" plain @click="handleLogout">退出登录</el-button>
      </div>
    </aside>

    <main class="workspace">
      <header class="topbar">
        <div>
          <div class="mono topbar-meta">AI VIDEO PARSING + AGENT ORCHESTRATION</div>
          <h2>{{ currentTitle }}</h2>
        </div>
        <div class="topbar-badge panel">
          <span class="dot" />
          <span>Pipeline Ready</span>
        </div>
      </header>

      <router-view />
    </main>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 320px 1fr;
}

.sidebar {
  margin: 18px;
  border-radius: 28px;
  padding: 28px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.brand {
  display: flex;
  gap: 18px;
  align-items: center;
}

.brand-mark {
  width: 68px;
  height: 68px;
  border-radius: 22px;
  display: grid;
  place-items: center;
  font-size: 26px;
  font-weight: 800;
  color: #101010;
  background: linear-gradient(135deg, #f7d28f, #f6b655);
}

.brand-sub {
  color: var(--muted);
  font-size: 12px;
  margin-bottom: 6px;
}

.brand h1 {
  margin: 0;
  font-size: 38px;
  letter-spacing: -0.05em;
}

.nav {
  display: grid;
  gap: 12px;
  margin: 42px 0 auto;
}

.nav-item {
  border: 1px solid transparent;
  border-radius: 18px;
  background: transparent;
  color: var(--text);
  padding: 16px 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  font-size: 15px;
  transition: all 0.24s ease;
}

.nav-item:hover,
.nav-item.active {
  border-color: var(--line);
  background: rgba(255, 255, 255, 0.06);
  transform: translateX(4px);
}

.sidebar-foot {
  display: grid;
  gap: 14px;
}

.operator {
  border-radius: 20px;
  padding: 16px;
  display: grid;
  gap: 6px;
}

.operator .mono,
.operator span {
  color: var(--muted);
  font-size: 12px;
}

.workspace {
  padding: 26px 24px 24px 0;
}

.topbar {
  padding: 12px 6px 26px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.topbar-meta {
  color: var(--muted);
  font-size: 12px;
  margin-bottom: 8px;
}

.topbar h2 {
  margin: 0;
  font-size: 44px;
  letter-spacing: -0.05em;
}

.topbar-badge {
  border-radius: 999px;
  padding: 12px 16px;
  display: flex;
  gap: 10px;
  align-items: center;
  color: var(--muted);
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: var(--success);
  box-shadow: 0 0 18px rgba(70, 194, 157, 0.6);
}

@media (max-width: 1100px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .sidebar {
    margin: 18px 18px 0;
  }

  .workspace {
    padding: 18px;
  }
}
</style>
