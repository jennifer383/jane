import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import * as api from './api/api.js'
import { useChatStore } from '@/stores/chatStore'
import { useAuthStore } from '@/stores/authStore'

// 引入所有主题CSS
import './styles/themes/dark.css'
import './styles/themes/light.css'
import './styles/themes/blue.css'
import './styles/themes/green.css'

// 引入主样式
import './styles/main.css'

// 引入Font Awesome
import '@fortawesome/fontawesome-free/css/all.css'

// 创建应用
const app = createApp(App)

// 将api挂载到全局属性
app.config.globalProperties.$api = api

// 使用Pinia状态管理
const pinia = createPinia()
app.use(pinia)

// 使用路由
app.use(router)

// 初始化状态管理
const chatStore = useChatStore()
const authStore = useAuthStore()

// 同步认证状态
if (authStore.isAuthenticated && !chatStore.isAuthenticated) {
  chatStore.setAuthenticated(true)
  if (authStore.user) {
    chatStore.setUser(authStore.user)
  }
  
  // 如果是从localStorage恢复的认证状态，加载聊天历史
  // 延迟加载聊天历史，确保组件已挂载
  setTimeout(() => {
    chatStore.loadChatHistory().then(() => {
      // 如果有保存的当前聊天ID，尝试激活它
      if (chatStore.currentChatId && !chatStore.currentChatId.startsWith('temp-')) {
        chatStore.setActiveChat(chatStore.currentChatId)
      }
    })
  }, 100)
} else if (chatStore.isAuthenticated && !authStore.isAuthenticated) {
  // 如果chatStore有认证状态但authStore没有，同步到authStore
  if (chatStore.user) {
    authStore.user = chatStore.user
    authStore.isAuthenticated = true
    authStore.saveAuthState()
  }
  
  // 加载聊天历史
  setTimeout(() => {
    chatStore.loadChatHistory().then(() => {
      if (chatStore.currentChatId && !chatStore.currentChatId.startsWith('temp-')) {
        chatStore.setActiveChat(chatStore.currentChatId)
      }
    })
  }, 100)
}

// 自动同步主题主色到 :root，保证滚动条主色生效
chatStore.setTheme(chatStore.currentTheme)

// 初始化时获取后端模型信息
chatStore.loadModelInfo().catch(err => {
  console.warn('初始化模型信息失败:', err)
})

// 挂载应用
app.mount('#app')