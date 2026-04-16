import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 300000 // 5分钟超时,因为 AI 处理可能较慢
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
    return Promise.reject(error)
  }
)

// 上传视频
export function uploadVideo(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post('/video/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: progressEvent => {
      if (onProgress && progressEvent.total) {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percentCompleted)
      }
    }
  })
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
