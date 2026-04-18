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
      <div class="mono">AI VIDEO UNDERSTANDING PLATFORM</div>
      <h1 class="fade-in-up">VideoAI</h1>
      <p>
        长视频解析、转写摘要、Agent 问答、高并发治理，一套能真正写进简历并讲出深度的完整项目。
      </p>
      <ul>
        <li>Chunk Upload + 秒传 + 断点续传</li>
        <li>RocketMQ 异步处理链路</li>
        <li>RAG + Tool Calling VideoQAAgent</li>
      </ul>
    </section>

    <section class="auth-form panel">
      <div>
        <div class="mono form-meta">SIGN IN</div>
        <h2>进入视频理解工作台</h2>
      </div>
      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="warning" :loading="loading" class="full-btn" @click="submit">登录</el-button>
        <el-button text type="warning" class="full-btn" @click="router.push('/register')">没有账号？去注册</el-button>
      </el-form>
    </section>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
}

.auth-poster {
  padding: 54px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.auth-poster .mono,
.form-meta {
  color: var(--muted);
  font-size: 12px;
  letter-spacing: 0.12em;
}

.auth-poster h1 {
  margin: 18px 0 14px;
  font-size: clamp(64px, 12vw, 132px);
  letter-spacing: -0.08em;
}

.auth-poster p {
  max-width: 620px;
  color: var(--muted);
  line-height: 1.8;
}

.auth-poster ul {
  margin: 26px 0 0;
  padding-left: 18px;
  color: var(--text);
  line-height: 2;
}

.auth-form {
  margin: 28px;
  border-radius: 32px;
  padding: 40px;
  display: grid;
  align-content: center;
  gap: 28px;
}

.auth-form h2 {
  margin: 8px 0 0;
  font-size: 34px;
  letter-spacing: -0.05em;
}

.full-btn {
  width: 100%;
}

@media (max-width: 900px) {
  .auth-page {
    grid-template-columns: 1fr;
  }

  .auth-poster {
    padding: 28px 22px 0;
  }
}
</style>
