import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 600000 // 10分钟超时
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('请求错误:', error)
    ElMessage.error(error.message || '网络请求失败')
    return Promise.reject(error)
  }
)

// 上传视频文件
export function uploadVideoFile(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post('/video/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: progressEvent => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    }
  })
}

// 上传音频文件
export function uploadAudioFile(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', 'audio')
  
  return request.post('/video/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: progressEvent => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    }
  })
}

// 解析视频链接
export function parseVideoUrl(url) {
  return request.post('/video/parse-url', { url })
}

// 转换音频
export function convertToAudio(videoId) {
  return request.post(`/video/convert/${videoId}`)
}

// AI 处理
export function processWithAi(videoId) {
  return request.post(`/video/process/${videoId}`)
}

// 获取视频信息
export function getVideoInfo(videoId) {
  return request.get(`/video/${videoId}`)
}

// 获取历史记录
export function getHistoryList(page = 1, pageSize = 10) {
  return request.get('/video/history', {
    params: { page, pageSize }
  })
}
