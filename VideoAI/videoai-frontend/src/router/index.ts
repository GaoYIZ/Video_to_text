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
    { path: '/login', component: LoginView, meta: { public: true } },
    { path: '/register', component: RegisterView, meta: { public: true } },
    {
      path: '/',
      component: AppLayout,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: '/dashboard', component: DashboardView },
        { path: '/upload', component: UploadView },
        { path: '/tasks', component: TaskListView },
        { path: '/tasks/:id', component: TaskDetailView, props: true },
        { path: '/usage', component: UsageView }
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

export default router
