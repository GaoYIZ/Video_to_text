<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const menus = [
  {
    label: '总览',
    path: '/dashboard',
    icon: 'DataAnalysis',
    caption: '系统状态与最新任务'
  },
  {
    label: '上传中心',
    path: '/upload',
    icon: 'UploadFilled',
    caption: '分片上传与任务投递'
  },
  {
    label: '任务列表',
    path: '/tasks',
    icon: 'Tickets',
    caption: '状态机、失败诊断、结果回看'
  },
  {
    label: '使用统计',
    path: '/usage',
    icon: 'PieChart',
    caption: '配额、成本与缓存收益'
  }
]

const currentMenu = computed(() => menus.find((item) => route.path.startsWith(item.path)))
const currentTitle = computed(() => (route.meta.title as string) || currentMenu.value?.label || 'VideoAI')
const currentDescription = computed(
  () => (route.meta.description as string) || currentMenu.value?.caption || 'AI 视频解析与问答工作台'
)
const currentUserLabel = computed(() => authStore.user?.nickname || authStore.user?.username || '未登录')

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="layout">
    <aside class="sidebar panel fade-in">
      <div class="sidebar-top">
        <div class="brand">
          <div class="brand-mark">VA</div>
          <div>
            <div class="brand-kicker mono">AI VIDEO ANALYSIS WORKBENCH</div>
            <h1>VideoAI</h1>
            <p>面向长视频解析、结构化理解与多轮问答的工程化平台。</p>
          </div>
        </div>

        <nav class="nav">
          <button
            v-for="item in menus"
            :key="item.path"
            :class="['nav-item', { active: route.path.startsWith(item.path) }]"
            @click="router.push(item.path)"
          >
            <el-icon size="18"><component :is="item.icon" /></el-icon>
            <div>
              <strong>{{ item.label }}</strong>
              <span>{{ item.caption }}</span>
            </div>
          </button>
        </nav>
      </div>

      <div class="sidebar-bottom">
        <div class="operator panel-muted">
          <div class="operator-row">
            <span class="mono">当前用户</span>
            <span class="status-live">
              <span class="status-dot" style="color: var(--success)" />
              在线
            </span>
          </div>
          <strong>{{ currentUserLabel }}</strong>
          <span>{{ authStore.user?.userNo || '等待分配用户编号' }}</span>
        </div>

        <div class="mini-board panel-muted">
          <div>
            <span class="mono">处理链路</span>
            <strong>Upload → MQ → ASR → Summary → RAG → Agent</strong>
          </div>
        </div>

        <el-button plain @click="handleLogout">退出登录</el-button>
      </div>
    </aside>

    <main class="workspace">
      <header class="topbar fade-in-up">
        <div>
          <div class="topbar-meta mono">VIDEO UNDERSTANDING · TOOL CALLING · OBSERVABILITY</div>
          <h2>{{ currentTitle }}</h2>
          <p>{{ currentDescription }}</p>
        </div>

        <div class="topbar-side">
          <div class="readiness panel-muted">
            <span class="status-dot" style="color: var(--success)" />
            <span>工作台已就绪</span>
          </div>
          <el-button type="warning" @click="router.push('/upload')">上传新视频</el-button>
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
  border-radius: var(--radius-xl);
  padding: 24px;
  display: grid;
  gap: 28px;
}

.sidebar-top,
.sidebar-bottom {
  display: grid;
  gap: 22px;
}

.brand {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 16px;
  align-items: start;
}

.brand-mark {
  width: 62px;
  height: 62px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  font-size: 24px;
  font-weight: 800;
  color: #1c1307;
  background: linear-gradient(145deg, #ffd59a 0%, #ffb761 58%, #ff9254 100%);
  box-shadow: 0 16px 40px rgba(255, 166, 81, 0.28);
}

.brand-kicker,
.operator span,
.topbar-meta {
  color: var(--muted);
  font-size: 11px;
  letter-spacing: 0.12em;
}

.brand h1 {
  margin: 0;
  font-size: 36px;
  letter-spacing: -0.06em;
}

.brand p {
  margin: 8px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.nav {
  display: grid;
  gap: 10px;
}

.nav-item {
  width: 100%;
  border: 1px solid transparent;
  border-radius: 18px;
  background: transparent;
  color: var(--text);
  padding: 14px 15px;
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 12px;
  align-items: start;
  text-align: left;
  cursor: pointer;
  transition:
    transform 0.24s ease,
    background 0.24s ease,
    border-color 0.24s ease;
}

.nav-item strong {
  display: block;
  font-size: 15px;
}

.nav-item span {
  display: block;
  margin-top: 4px;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.5;
}

.nav-item:hover,
.nav-item.active {
  transform: translateX(4px);
  background: rgba(255, 255, 255, 0.05);
  border-color: var(--line);
}

.operator,
.mini-board {
  border-radius: 18px;
  padding: 16px;
  display: grid;
  gap: 8px;
}

.operator strong,
.mini-board strong {
  font-size: 16px;
}

.operator-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.status-live {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--text-soft);
}

.workspace {
  padding: 24px 24px 24px 0;
}

.topbar {
  padding: 10px 4px 0;
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: flex-start;
}

.topbar h2 {
  margin: 10px 0 0;
  font-size: clamp(34px, 4vw, 52px);
  letter-spacing: -0.06em;
}

.topbar p {
  margin: 10px 0 0;
  max-width: 620px;
  color: var(--muted);
  line-height: 1.7;
}

.topbar-side {
  display: grid;
  gap: 12px;
  justify-items: end;
}

.readiness {
  border-radius: 999px;
  padding: 12px 16px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: var(--text-soft);
}

@media (max-width: 1180px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .sidebar {
    margin: 18px 18px 0;
  }

  .workspace {
    padding: 18px;
  }

  .topbar {
    display: grid;
  }

  .topbar-side {
    justify-items: start;
  }
}

@media (max-width: 720px) {
  .brand {
    grid-template-columns: 1fr;
  }

  .brand-mark {
    width: 56px;
    height: 56px;
  }
}
</style>
