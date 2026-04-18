<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../api/modules'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  nickname: ''
})

const submit = async () => {
  loading.value = true
  try {
    const result = await userApi.register(form)
    authStore.setLogin(result)
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <section class="auth-poster">
      <div class="poster-inner fade-in-up">
        <div class="poster-label mono">CREATE VIDEOAI ACCOUNT</div>
        <h1>创建演示账号</h1>
        <p>注册后即可完整体验视频上传、解析任务追踪、结构化摘要、片段检索与多轮问答链路。</p>

        <div class="poster-list">
          <div>
            <strong>1. 上传视频</strong>
            <span>前端自动分片、计算 MD5，支持断点续传与秒传。</span>
          </div>
          <div>
            <strong>2. 跟踪任务</strong>
            <span>任务状态机与事件日志帮助快速定位失败节点。</span>
          </div>
          <div>
            <strong>3. 开始问答</strong>
            <span>基于 RAG 与 Agent 工具调用，面向视频内容持续追问。</span>
          </div>
        </div>
      </div>
    </section>

    <section class="auth-panel panel fade-in">
      <div class="auth-head">
        <div class="mono">注册账号</div>
        <h2>创建新的使用者身份</h2>
        <p>建议使用清晰的用户名与昵称，方便后续查看配额和任务归属。</p>
      </div>

      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="仅支持字母、数字和下划线" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="用于页面展示的名称" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="建议至少 6 位" />
        </el-form-item>

        <div class="action-group">
          <el-button type="warning" :loading="loading" class="full-btn" @click="submit">注册并进入</el-button>
          <el-button plain class="full-btn" @click="router.push('/login')">返回登录</el-button>
        </div>
      </el-form>
    </section>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
}

.auth-poster {
  padding: 42px;
}

.poster-inner {
  height: 100%;
  border-radius: 36px;
  padding: 48px;
  display: grid;
  align-content: space-between;
  background:
    radial-gradient(circle at top left, rgba(114, 182, 255, 0.18), transparent 24%),
    linear-gradient(180deg, rgba(17, 31, 52, 0.94), rgba(7, 15, 28, 0.98));
  border: 1px solid var(--line);
  box-shadow: var(--shadow-lg);
}

.poster-label {
  color: var(--muted);
  font-size: 12px;
  letter-spacing: 0.12em;
}

.poster-inner h1 {
  margin: 18px 0 0;
  font-size: clamp(48px, 7vw, 90px);
  line-height: 1;
  letter-spacing: -0.07em;
}

.poster-inner p {
  margin: 14px 0 0;
  color: var(--text-soft);
  line-height: 1.8;
  max-width: 640px;
}

.poster-list {
  display: grid;
  gap: 16px;
}

.poster-list div {
  padding: 18px 0 0;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.poster-list strong {
  display: block;
  font-size: 18px;
}

.poster-list span {
  display: block;
  margin-top: 8px;
  color: var(--muted);
  line-height: 1.7;
}

.auth-panel {
  margin: 42px 42px 42px 0;
  border-radius: 32px;
  padding: 42px;
  display: grid;
  align-content: center;
  gap: 28px;
}

.auth-head h2 {
  margin: 10px 0 0;
  font-size: 36px;
  letter-spacing: -0.05em;
}

.auth-head p {
  margin: 10px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.action-group {
  display: grid;
  gap: 12px;
  margin-top: 8px;
}

.full-btn {
  width: 100%;
}

@media (max-width: 980px) {
  .auth-page {
    grid-template-columns: 1fr;
  }

  .auth-poster {
    padding: 20px 20px 0;
  }

  .poster-inner,
  .auth-panel {
    padding: 28px;
  }

  .auth-panel {
    margin: 20px;
  }
}
</style>
