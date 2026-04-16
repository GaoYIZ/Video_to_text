import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomeView.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/upload-video',
    name: 'UploadVideo',
    component: () => import('../views/UploadVideoView.vue'),
    meta: { title: '上传视频' }
  },
  {
    path: '/upload-audio',
    name: 'UploadAudio',
    component: () => import('../views/UploadAudioView.vue'),
    meta: { title: '上传音频' }
  },
  {
    path: '/parse-url',
    name: 'ParseUrl',
    component: () => import('../views/ParseUrlView.vue'),
    meta: { title: '解析链接' }
  },
  {
    path: '/result/:id',
    name: 'Result',
    component: () => import('../views/ResultView.vue'),
    meta: { title: '处理结果' },
    props: true
  },
  {
    path: '/history',
    name: 'History',
    component: () => import('../views/HistoryView.vue'),
    meta: { title: '历史记录' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 视频智能总结` : '视频智能总结'
  next()
})

export default router
