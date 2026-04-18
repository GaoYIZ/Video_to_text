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
      <div class="mono">CREATE OPERATOR ACCOUNT</div>
      <h1>Join VideoAI</h1>
      <p>注册后即可演示上传、解析、摘要、Agent 问答与使用统计完整链路。</p>
    </section>

    <section class="auth-form panel">
      <div>
        <div class="mono form-meta">REGISTER</div>
        <h2>创建你的演示账号</h2>
      </div>
      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="仅支持字母数字下划线" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-button type="warning" :loading="loading" class="full-btn" @click="submit">注册并进入</el-button>
        <el-button text type="warning" class="full-btn" @click="router.push('/login')">已有账号？返回登录</el-button>
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

.auth-poster {
  padding: 56px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.auth-poster h1 {
  margin: 16px 0;
  font-size: clamp(58px, 10vw, 108px);
  letter-spacing: -0.08em;
}

.auth-poster p,
.mono {
  color: var(--muted);
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
