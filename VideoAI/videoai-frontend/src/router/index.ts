import { createRouter, createWebHistory, type RouteLocationNormalizedGeneric } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import AppLayout from '../components/AppLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      component: LoginView,
      meta: {
        public: true,
        title: '登录'
      }
    },
    {
      path: '/register',
      component: RegisterView,
      meta: {
        public: true,
        title: '注册'
      }
    },
    {
      path: '/',
      component: AppLayout,
      meta: { requiresAuth: true }
    }
  ]
})

router.beforeEach((to: RouteLocationNormalizedGeneric) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.token) {
    return '/login'
  }
  if (to.meta.public && authStore.token) {
    return '/'
  }
  return true
})

router.afterEach((to) => {
  const title = typeof to.meta.title === 'string' ? `${to.meta.title} · AutoScribe` : 'AutoScribe'
  document.title = title
})

export default router
