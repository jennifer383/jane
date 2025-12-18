import { createRouter, createWebHistory } from 'vue-router'
import { useChatStore } from '@/stores/chatStore'
import { useAuthStore } from '@/stores/authStore'
import LoginView from '@/views/LoginView.vue'
import TutorialView from '@/views/TutorialView.vue'
// src/api/index.js


// 延迟导入主布局组件，确保路由守卫正常工作
const loadChatLayout = () => import('@/components/layout/ChatLayout.vue')

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => loadChatLayout(),
    meta: { 
      requiresAuth: true,
      title: 'JaneAI - 智能对话平台'
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { 
      title: '登录 - JaneAI',
      hideForAuth: true
    }
  },
  {
    path: '/tutorial',
    name: 'Tutorial',
    component: TutorialView,
    meta: {
      requiresAuth: true,
      title: '使用教程 - JaneAI'
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || 'JaneAI'
  
  // 确保 store 已初始化
  const chatStore = useChatStore()
  const authStore = useAuthStore()
  
  // 同步认证状态到chatStore
  if (authStore.isAuthenticated && !chatStore.isAuthenticated) {
    chatStore.setAuthenticated(true)
    if (authStore.user) {
      chatStore.setUser(authStore.user)
    }
  }
  
  // 检查认证状态
  if (to.meta.requiresAuth) {
    if (authStore.isAuthenticated || chatStore.isAuthenticated) {
      next()
    } else {
      next('/login')
    }
  } else {
    // 移除对登录页面的重定向限制，允许已认证用户访问登录页面
    next()
  }
})

export default router