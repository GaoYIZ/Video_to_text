import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { useAuthStore } from '../stores/auth'

const http = axios.create({
  baseURL: '/',
  timeout: 300000 // 5 minutes for large file operations
})

http.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload.code !== 0) {
      ElMessage.error(payload.message || '请求失败，请稍后重试')
      return Promise.reject(new Error(payload.message || '请求失败'))
    }
    return payload.data
  },
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      router.push('/login')
    }
    ElMessage.error(error.response?.data?.message || error.message || '网络异常，请检查服务是否启动')
    return Promise.reject(error)
  }
)

export default http
