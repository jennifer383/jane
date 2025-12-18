import { defineStore } from 'pinia'
import { loginApi } from '@/api/authApi'
import { useChatStore } from './chatStore'

export const useAuthStore = defineStore('auth', {
  state: () => {
    // 从localStorage恢复认证状态
    const savedAuth = JSON.parse(localStorage.getItem('authState') || '{}')
    return {
      user: savedAuth.user || null,
      isAuthenticated: savedAuth.isAuthenticated || false
    }
  },
  actions: {
    saveAuthState() {
      const authState = {
        user: this.user,
        isAuthenticated: this.isAuthenticated
      }
      localStorage.setItem('authState', JSON.stringify(authState))
    },
    async login(username, password) {
      try {
        const res = await loginApi(username, password)
        this.user = res.user
        this.isAuthenticated = true
        localStorage.setItem('token', res.token)
        this.saveAuthState() // 保存认证状态
        
        // 清空之前的聊天状态
        const chatStore = useChatStore()
        chatStore.clearChatState()
        
        return true
      } catch (error) {
        throw error
      }
    },
    guestLogin() {
      this.user = { id: 0 }
      this.isAuthenticated = true
      localStorage.setItem('userId', '0')
      localStorage.setItem('token', '')
      this.saveAuthState() // 保存认证状态
      
      // 清空之前的聊天状态
      const chatStore = useChatStore()
      chatStore.clearChatState()
    },
    logout() {
      this.user = null
      this.isAuthenticated = false
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('authState')
      localStorage.removeItem('chatAppState')
    }
  }
})