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
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <section class="auth-visual">
      <div class="visual-card fade-in-up">
        <div class="mono visual-kicker">CREATE YOUR WORKSPACE</div>
        <h1>注册账号</h1>
        <p>
          创建新账号后，你可以完整体验视频上传、任务状态机、结构化摘要、片段检索和多轮问答整条链路。
        </p>

        <div class="visual-steps">
          <div>
            <strong>1. 上传视频</strong>
            <span>前端自动分片、计算 MD5，并支持断点续传和秒传。</span>
          </div>
          <div>
            <strong>2. 查看任务</strong>
            <span>任务进入异步处理链路，状态机和事件日志持续可见。</span>
          </div>
          <div>
            <strong>3. 发起问答</strong>
            <span>使用 RAG 检索与 Agent 工具调用围绕单个视频持续追问。</span>
          </div>
        </div>
      </div>
    </section>

    <section class="auth-form panel fade-in">
      <div>
        <div class="mono form-kicker">CREATE ACCOUNT</div>
        <h2>创建新的 VideoAI 用户</h2>
        <p>建议使用易识别的用户名和昵称，方便后续查看任务和配额归属。</p>
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

        <div class="form-actions">
          <el-button type="warning" class="full-width" :loading="loading" @click="submit">注册并进入</el-button>
          <el-button plain class="full-width" @click="router.push('/login')">返回登录</el-button>
        </div>
      </el-form>
    </section>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
}

.auth-visual {
  padding: 28px;
}

.visual-card {
  height: 100%;
  border-radius: 36px;
  padding: 42px;
  border: 1px solid var(--line);
  background:
    radial-gradient(circle at top left, rgba(173, 229, 210, 0.52), transparent 28%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(242, 249, 246, 0.88));
  box-shadow: var(--shadow-lg);
  display: grid;
  gap: 22px;
  align-content: space-between;
}

.visual-kicker,
.form-kicker {
  color: var(--muted);
  font-size: 12px;
  letter-spacing: 0.12em;
}

.visual-card h1 {
  margin: 0;
  font-size: clamp(56px, 8vw, 92px);
  line-height: 0.98;
  letter-spacing: -0.08em;
}

.visual-card p {
  margin: 0;
  color: var(--text-soft);
  line-height: 1.8;
}

.visual-steps {
  display: grid;
  gap: 16px;
}

.visual-steps div {
  padding-top: 16px;
  border-top: 1px solid var(--line);
}

.visual-steps strong {
  display: block;
}

.visual-steps span {
  display: block;
  margin-top: 8px;
  color: var(--muted);
  line-height: 1.7;
}

.auth-form {
  margin: 28px 28px 28px 0;
  border-radius: 32px;
  padding: 40px;
  display: grid;
  align-content: center;
  gap: 28px;
}

.auth-form h2 {
  margin: 10px 0 0;
  font-size: 38px;
  letter-spacing: -0.05em;
}

.auth-form p {
  margin: 10px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.form-actions {
  display: grid;
  gap: 12px;
}

.full-width {
  width: 100%;
}

@media (max-width: 980px) {
  .auth-page {
    grid-template-columns: 1fr;
  }

  .auth-visual {
    padding: 20px 20px 0;
  }

  .visual-card,
  .auth-form {
    padding: 28px;
  }

  .auth-form {
    margin: 20px;
  }
}
</style>
