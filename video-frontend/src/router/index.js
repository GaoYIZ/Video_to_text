import { createRouter, createWebHistory } from 'vue-router'
import LayoutView from '../views/LayoutView.vue'

const routes = [
  {
    path: '/',
    component: LayoutView,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/HomeView.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'result/:id',
        name: 'Result',
        component: () => import('../views/ResultView.vue'),
        meta: { title: '处理结果' },
        props: true
      },
      {
        path: 'history',
        name: 'History',
        component: () => import('../views/HistoryView.vue'),
        meta: { title: '历史记录' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - AI视频助手` : 'AI视频助手'
  next()
})

export default router
