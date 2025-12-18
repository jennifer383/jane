<template>
  <div class="login-container">
    <div class="login-card">
      <div class="brand-section">
        <div class="logo">
          <i class="fas fa-brain"></i>
        </div>
        <h1 class="brand-title">JaneAI</h1>
        <p class="brand-subtitle">智能对话，无限可能</p>
      </div>

      <form @submit.prevent="login" class="login-form">
        <div class="form-group">
          <label for="username">账号</label>
          <input 
            type="text" 
            id="username" 
            v-model="username" 
            placeholder="请输入账号"
            :class="{ 'error': error }"
            autocomplete="username"
          >
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <input 
            type="password" 
            id="password" 
            v-model="password" 
            placeholder="请输入密码"
            :class="{ 'error': error }"
            autocomplete="current-password"
          >
        </div>
        
        <div v-if="error" class="error-message">
          <i class="fas fa-exclamation-circle"></i> {{ error }}
        </div>
        
        <button type="submit" class="login-btn" :disabled="loading">
          <span v-if="!loading">登录</span>
          <span v-else class="loading">
            <i class="fas fa-spinner fa-spin"></i> 登录中...
          </span>
        </button>
      </form>
      
      <div class="auth-options">
        <button type="button" class="auth-btn guest-btn" @click="guestLogin">
          <i class="fas fa-user-clock"></i> 游客体验
        </button>
        <button type="button" class="auth-btn register-btn" @click="showRegister = true">
          <i class="fas fa-user-plus"></i> 注册账号
        </button>
      </div>
      
      <div class="login-footer">
        <p>© 2025 JaneAI 版权所有</p>
      </div>
    </div>

    <!-- 注册弹窗 -->
    <div v-if="showRegister" class="modal-overlay">
      <div class="register-modal">
        <div class="modal-header">
          <h3>注册新账号</h3>
          <button type="button" class="close-btn" @click="showRegister = false">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>用户名</label>
            <input type="text" v-model="registerData.username" placeholder="输入用户名">
          </div>
          <div class="form-group">
            <label>密码</label>
            <input type="password" v-model="registerData.password" placeholder="输入密码">
          </div>
          <div class="form-group">
            <label>确认密码</label>
            <input type="password" v-model="registerData.confirmPassword" placeholder="再次输入密码">
          </div>
          <div v-if="registerError" class="error-message">
            <i class="fas fa-exclamation-circle"></i> {{ registerError }}
          </div>
          <div v-if="registerSuccess" class="success-message">
            <i class="fas fa-check-circle"></i> {{ registerSuccess }}
          </div>
          <button type="button" class="register-submit" @click="register">注册</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chatStore'
import { loginApi, registerApi } from '@/api/authApi'

const router = useRouter()
const chatStore = useChatStore()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)
const showRegister = ref(false)
const registerData = ref({
  username: '',
  password: '',
  confirmPassword: ''
})
const registerError = ref('')
const registerSuccess = ref('')

const login = async () => {
  error.value = ''
  if (!username.value || !password.value) {
    error.value = '请输入账号和密码'
    return
  }
  loading.value = true
  try {
    const res = await loginApi(null, username.value, password.value)
    if (res.code === 0 && res.data) {
      localStorage.setItem('userId', res.data.userId)
      localStorage.setItem('token', res.data.token)
      chatStore.setAuthenticated(true)
      chatStore.setUser({
        id: res.data.userId,
        name: username.value
      })
      console.log('登录成功，开始请求历史会话...')
      await chatStore.loadChatHistory()
      console.log('历史会话请求完成', chatStore.chatHistory)
      if (chatStore.chatHistory.length > 0) {
        await chatStore.setActiveChat(chatStore.chatHistory[0].id)
      }
      router.push('/')
    } else {
      error.value = res.message || '账号或密码错误，默认账号密码为admin'
    }
  } catch (err) {
    error.value = err?.message || '登录请求失败'
  } finally {
    loading.value = false
  }
}

const guestLogin = () => {
  localStorage.setItem('userId', '0')
  localStorage.setItem('token', '')
  chatStore.setAuthenticated(true)
  chatStore.setUser({
    id: 0,
    name: '游客用户'
  })
  router.push('/')
}

const register = async () => {
  registerError.value = ''
  registerSuccess.value = ''
  if (!registerData.value.username || !registerData.value.password) {
    registerError.value = '请输入用户名和密码'
    return
  }
  if (registerData.value.password !== registerData.value.confirmPassword) {
    registerError.value = '两次输入的密码不一致'
    return
  }
  loading.value = true
  try {
    const res = await registerApi(null, registerData.value.username, registerData.value.password)
    if (res.code === 0) {
      registerSuccess.value = '注册成功，请登录'
      // 注册成功，自动填充登录框
      username.value = registerData.value.username
      password.value = registerData.value.password
      setTimeout(() => {
        showRegister.value = false
        registerSuccess.value = ''
      }, 1200)
    } else {
      registerError.value = res.message || '注册失败'
    }
  } catch (err) {
    registerError.value = err?.message || '注册请求失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 美化后的登录界面样式 */

/* 主容器 - 原始深色背景 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #1e1f25 0%, #121318 100%);
  padding: 20px;
  position: relative;
}

/* 登录卡片 - 原始深色设计 */
.login-card {
  width: 100%;
  max-width: 400px;
  background: #2d2f3a;
  border-radius: 16px;
  padding: 40px 32px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
  border: 1px solid #3a3d4a;
  transition: all 0.15s ease;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 
    0 35px 60px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

/* 品牌区域 */
.brand-section {
  text-align: center;
  margin-bottom: 40px;
  animation: fadeInDown 0.5s ease-out 0.1s both;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Logo - 原始设计 */
.logo {
  width: 80px;
  height: 80px;
  margin: 0 auto 25px;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: rgba(255, 255, 255, 0.795);
  box-shadow: 0 8px 25px rgba(99, 102, 241, 0.3);
  transition: transform 0.15s ease;
}

.logo:hover {
  transform: translateY(-2px);
}

/* 品牌标题 - 原始设计 */
.brand-title {
  font-size: 32px;
  font-weight: 700;
  color: #f1f1f1;
  margin-bottom: 12px;
  letter-spacing: -0.5px;
}

.brand-subtitle {
  color: #b4b4b8;
  font-size: 16px;
  font-weight: 400;
  letter-spacing: 0.3px;
}

/* 表单样式 */
.login-form {
  margin-bottom: 35px;
  animation: fadeInUp 0.5s ease-out 0.2s both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-group {
  margin-bottom: 25px;
  position: relative;
}

label {
  display: block;
  margin-bottom: 8px;
  color: #f1f1f1;
  font-weight: 500;
  font-size: 14px;
}

/* 输入框 - 原始深色设计 */
input {
  width: 100%;
  padding: 14px 16px;
  background: #1e1f25;
  border: 1px solid #3a3d4a;
  border-radius: 8px;
  color: #f1f1f1;
  font-size: 16px;
  transition: all 0.1s ease;
}

input::placeholder {
  color: #8a8a94;
}

input:focus {
  border-color: #6366f1;
  outline: none;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
  background: #2d2f3a;
}

input.error {
  border-color: #ff6b6b;
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.2);
}

/* 错误和成功消息 */
.error-message {
  background: #fed7d7;
  color: #c53030;
  padding: 12px 16px;
  border-radius: 8px;
  margin: 15px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  border: 1px solid #feb2b2;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.success-message {
  background: #c6f6d5;
  color: #2f855a;
  padding: 12px 16px;
  border-radius: 8px;
  margin: 15px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  border: 1px solid #9ae6b4;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 登录按钮 - 原始设计 */
.login-btn {
  width: 100%;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 16px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-top: 10px;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

/* 认证选项 */
.auth-options {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.auth-btn {
  flex: 1;
  padding: 12px 16px;
  border-radius: 8px;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.guest-btn {
  background: #1e1f25;
  color: #b4b4b8;
  border: 1px solid #3a3d4a;
}

.guest-btn:hover {
  background: #2d2f3a;
  transform: translateY(-1px);
}

.register-btn {
  background: linear-gradient(135deg, #ed64a6, #f56565);
  color: white;
}

.register-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(237, 100, 166, 0.3);
}

/* 页脚 */
.login-footer {
  text-align: center;
  color: #8a8a94;
  font-size: 14px;
  padding-top: 20px;
  border-top: 1px solid #3a3d4a;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.login-footer .hint {
  margin-top: 10px;
  font-size: 12px;
  color: #6366f1;
  background: #1e1f25;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #3a3d4a;
}

/* 注册弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.15s ease-out;
}

.register-modal {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  border: 1px solid #e2e8f0;
  overflow: hidden;
  animation: modalSlideUp 0.2s ease-out;
}

@keyframes modalSlideUp {
  from {
    opacity: 0;
    transform: translateY(50px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f7fafc;
}

.modal-header h3 {
  margin: 0;
  color: #2d3748;
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  background: #edf2f7;
  border: 1px solid #e2e8f0;
  color: #718096;
  cursor: pointer;
  font-size: 16px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.1s;
}

.close-btn:hover {
  background: #e2e8f0;
  color: #4a5568;
}

.modal-body {
  padding: 24px;
}

.register-submit {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #ed64a6, #f56565);
  color: white;
  border: none;
  border-radius: 8px;
  margin-top: 20px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.register-submit:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(237, 100, 166, 0.4);
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-card {
    padding: 40px 25px;
    margin: 10px;
  }
  
  .brand-title {
    font-size: 30px;
  }
  
  .logo {
    width: 75px;
    height: 75px;
    font-size: 32px;
  }
  
  .auth-options {
    flex-direction: column;
    gap: 12px;
  }
  
  .modal-body {
    padding: 25px 20px;
  }
}


</style>
