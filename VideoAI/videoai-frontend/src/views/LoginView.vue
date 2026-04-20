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
        <div class="mono visual-kicker">AI VIDEO UNDERSTANDING</div>
        <h1>VideoAI</h1>

      </div>
    </section>

    <section class="auth-form panel fade-in">
      <div>
        <div class="mono form-kicker">SIGN IN</div>
        <h2>进入视频解析工作台</h2>
        <p>演示账号已预填，你也可以使用自己注册的新账号登录。</p>
      </div>

      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>

        <div class="form-actions">
          <el-button type="warning" class="full-width" :loading="loading" @click="submit">登录并进入</el-button>
          <el-button plain class="full-width" @click="router.push('/register')">创建新账号</el-button>
        </div>
      </el-form>

      <div class="demo-card">
        <span class="mono">DEMO ACCOUNT</span>
        <strong>demo_user / 123456</strong>
      </div>
    </section>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.08fr 0.92fr;
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
    radial-gradient(circle at top left, rgba(140, 204, 255, 0.56), transparent 28%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.9), rgba(244, 249, 255, 0.86));
  box-shadow: var(--shadow-lg);
  display: grid;
  align-content: end;
  gap: 22px;
}

.visual-kicker,
.form-kicker,
.demo-card span {
  color: var(--muted);
  font-size: 12px;
  letter-spacing: 0.12em;
}

.visual-card h1 {
  margin: 0;
  font-size: clamp(72px, 11vw, 128px);
  line-height: 0.92;
  letter-spacing: -0.08em;
}

.visual-card p {
  max-width: 680px;
  margin: 0;
  color: var(--text-soft);
  line-height: 1.8;
}

.visual-grid {
  display: grid;
  gap: 14px;
}

.visual-grid div {
  padding: 16px 0 0;
  border-top: 1px solid var(--line);
}

.visual-grid span {
  display: block;
  color: var(--muted);
  margin-bottom: 8px;
  font-size: 13px;
}

.visual-grid strong {
  font-size: 17px;
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

.demo-card {
  border-radius: 20px;
  padding: 16px;
  border: 1px solid var(--line);
  background: rgba(248, 251, 255, 0.9);
  display: grid;
  gap: 8px;
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
