<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../api/modules'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const form = reactive({
  username: 'demo_user',
  password: '123456'
})

const submit = async () => {
  loading.value = true
  try {
    const result = await userApi.login(form)
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
        <div class="poster-label mono">AI VIDEO ANALYSIS PLATFORM</div>
        <h1>VideoAI</h1>
        <p>
          面向长视频理解的完整工作台，覆盖上传、异步处理、语音转写、结构化摘要、片段检索和智能问答。
        </p>

        <div class="poster-grid">
          <div>
            <span>工程链路</span>
            <strong>分片上传 · MQ 编排 · 可观测状态机</strong>
          </div>
          <div>
            <span>AI 能力</span>
            <strong>ASR · SummaryAgent · VideoQAAgent</strong>
          </div>
          <div>
            <span>检索策略</span>
            <strong>关键词召回 · 语义检索 · 混合 RAG</strong>
          </div>
        </div>
      </div>
    </section>

    <section class="auth-panel panel fade-in">
      <div class="auth-head">
        <div class="mono">登录工作台</div>
        <h2>进入视频解析控制台</h2>
        <p>默认演示账号已填充，你也可以使用自己的账号登录。</p>
      </div>

      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>

        <div class="action-group">
          <el-button type="warning" :loading="loading" class="full-btn" @click="submit">登录并进入</el-button>
          <el-button plain class="full-btn" @click="router.push('/register')">创建新账号</el-button>
        </div>
      </el-form>

      <div class="demo-tip panel-muted">
        <span class="mono">演示账号</span>
        <strong>demo_user / 123456</strong>
      </div>
    </section>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
}

.auth-poster {
  padding: 42px;
  display: grid;
  align-items: stretch;
}

.poster-inner {
  border-radius: 36px;
  padding: 48px;
  display: grid;
  align-content: end;
  gap: 22px;
  background:
    radial-gradient(circle at top left, rgba(255, 183, 97, 0.22), transparent 26%),
    linear-gradient(180deg, rgba(18, 34, 57, 0.92), rgba(7, 16, 30, 0.94));
  border: 1px solid var(--line);
  box-shadow: var(--shadow-lg);
}

.poster-label,
.demo-tip span {
  color: var(--muted);
  font-size: 12px;
  letter-spacing: 0.12em;
}

.poster-inner h1 {
  margin: 0;
  font-size: clamp(72px, 12vw, 132px);
  letter-spacing: -0.09em;
  line-height: 0.92;
}

.poster-inner p {
  max-width: 640px;
  margin: 0;
  color: var(--text-soft);
  line-height: 1.8;
}

.poster-grid {
  display: grid;
  gap: 12px;
  margin-top: 10px;
}

.poster-grid div {
  padding: 16px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.poster-grid span {
  display: block;
  color: var(--muted);
  font-size: 13px;
  margin-bottom: 8px;
}

.poster-grid strong {
  font-size: 17px;
  line-height: 1.5;
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

.demo-tip {
  border-radius: 18px;
  padding: 16px;
  display: grid;
  gap: 8px;
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
