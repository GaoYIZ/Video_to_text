import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AppLayout from '../components/AppLayout.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import DashboardView from '../views/DashboardView.vue'
import UploadView from '../views/UploadView.vue'
import TaskListView from '../views/TaskListView.vue'
import TaskDetailView from '../views/TaskDetailView.vue'
import UsageView from '../views/UsageView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      component: LoginView,
      meta: {
        public: true,
        title: '登录',
        description: '进入 VideoAI 工作台'
      }
    },
    {
      path: '/register',
      component: RegisterView,
      meta: {
        public: true,
        title: '注册',
        description: '创建新的演示账号'
      }
    },
    {
      path: '/',
      component: AppLayout,
      children: [
        {
          path: '',
          redirect: '/dashboard'
        },
        {
          path: '/dashboard',
          component: DashboardView,
          meta: {
            title: '总览',
            description: '查看系统状态、最近任务和关键指标'
          }
        },
        {
          path: '/upload',
          component: UploadView,
          meta: {
            title: '上传中心',
            description: '分片上传、断点续传、秒传与任务投递'
          }
        },
        {
          path: '/tasks',
          component: TaskListView,
          meta: {
            title: '任务列表',
            description: '追踪视频解析任务、状态机和失败重试'
          }
        },
        {
          path: '/tasks/:id',
          component: TaskDetailView,
          props: true,
          meta: {
            title: '任务详情',
            description: '查看转写、摘要、片段检索和智能问答'
          }
        },
        {
          path: '/usage',
          component: UsageView,
          meta: {
            title: '使用统计',
            description: '查看配额、调用量、成本和缓存收益'
          }
        }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (!to.meta.public && !authStore.token) {
    return '/login'
  }
  if (to.meta.public && authStore.token) {
    return '/dashboard'
  }
  return true
})

router.afterEach((to) => {
  const title = typeof to.meta.title === 'string' ? `${to.meta.title} - VideoAI` : 'VideoAI'
  document.title = title
})

export default router
