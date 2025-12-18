<template>
  <div class="content-area">
    <div class="content-header">
      <div class="header-left">
        <ModelSelector 
          v-model="chatStore.currentModel" 
          @model-change="handleModelChange" 
        />
        
        <div class="leaderboard-btn" @click="toggleLeaderboard">
          <i class="fas fa-trophy"></i>
          <span>排行榜</span>
        </div>
        
        <ThemeSelector />
      </div>
      
      <div class="user-info">
        <div class="user-avatar">
          <span>{{ userInitials }}</span>
        </div>
        <span>{{ chatStore.user.name }}</span>
        <button type="button" class="logout-btn" @click="logout">
          <i class="fas fa-sign-out-alt"></i>
        </button>
      </div>
    </div>
    
    <!-- 独立的教程界面 -->
    <transition name="tutorial-fade">
      <div v-if="chatStore.showTutorial" class="tutorial-overlay">
        <div class="tutorial-content-area">
          <div class="tutorial-close-btn" @click="closeTutorial">
            <i class="fas fa-times"></i>
          </div>
        
          <div class="tutorial-main">
            <div class="tutorial-title">
              <h1>Jane AI 项目功能简介</h1>
            </div>
            
            <div class="tutorial-welcome">
              <p><strong>大家好，感谢大家的使用，欢迎交流！</strong></p>
            </div>
            
            <div class="tutorial-info">
              <div class="contact-layout">
                <div class="contact-text">
                   <p><strong>

        💬 **技术交流** | 🐛 **问题反馈** | 💡 **功能建议** 

扫描二维码添加微信，备注“Jane AI交流”
我们将邀请您加入技术讨论群，与开发者直接沟通！</strong></p>
                 </div>
                
                <div class="qr-code-section">
                  <img src="/qr-code.png" alt="微信二维码" class="qr-code-image" />
                </div>
              </div>
            </div>
            
            <div class="features-list">
               <div class="feature-section">
                 <h3>🤖 智能对话功能</h3>
                 <p>多AI模型支持：集成DeepSeek、OpenAI、阿里云通义千问等多种AI模型</p>
                 <p>智能问答：可以进行自然语言对话，回答各种问题</p>
                 <p>对话记忆：支持多轮对话，系统会记住之前的聊天内容</p>
                 <p>RAG增强：结合 Pinecone 向量数据库，能基于上传的文档内容进行更准确的回答</p>
               </div>
               
               <div class="feature-section">
                 <h3>📄 PDF简历分析</h3>
                 <p>简历上传：支持上传PDF格式的简历文件</p>
                 <p>智能评分：AI自动分析简历内容并给出评分，需要用户提供对应的名称与密码，指令：打分上传</p>
                 <p>排行榜：查看所有简历的评分排名</p>
                 <p>求职建议：基于简历内容提供个性化的求职建议</p>
               </div>
               
               <div class="feature-section">
                 <h3>👤 用户系统</h3>
                 <p>用户注册/登录：安全的用户账号管理</p>
                 <p>权限控制：不同用户有不同的访问权限</p>
                 <p>会话管理：每个用户可以创建多个对话会话</p>
               </div>
             </div>
          </div>
        </div>
      </div>
    </transition>

    <div v-if="!chatStore.showTutorial" class="chat-container" ref="chatContainer" @scroll="onScroll">
      <WelcomeSection v-if="chatStore.messages.length === 0" />
      
      <div class="messages-container" @click="handleMessagesClick">
        <Message 
          v-for="message in chatStore.messages" 
          :key="message.id"
          :message="message"
        />
      </div>
      
      <!-- TypingIndicator 加载动画 -->
      <transition name="typing-fade">
        <TypingIndicator v-if="chatStore.isTyping" />
      </transition>
    </div>
    
    <div v-if="!chatStore.showTutorial" class="input-container" ref="inputContainer">
      <ChatInput />
      <!-- 回到底部按钮，居中圆形 -->
      <transition name="bounce">
        <button
          type="button"
          v-if="showScrollToBottom"
          class="scroll-to-bottom-btn"
          @click="scrollToBottom"
        >
          <i class="fas fa-arrow-down"></i>
        </button>
      </transition>
      <div class="disclaimer">
        JaneAI 可能会生成不准确的信息，不代表真实情况。
        <a href="#">使用条款</a> | <a href="#">隐私政策</a>
      </div>
    </div>

    <!-- 排行榜侧边栏 -->
    <div class="leaderboard-modal" :class="{ show: showLeaderboard }">
         <div class="leaderboard-content">
           <div class="leaderboard-header">
             <div class="header-title">
               <h3>用户排行榜</h3>
               <div class="update-countdown">
                 <i class="fas fa-clock"></i>
                 <span>{{ updateCountdown }}</span>
               </div>
             </div>
             <button type="button" class="close-btn" @click="showLeaderboard = false">
               <i class="fas fa-times"></i>
             </button>
           </div>
           <div class="leaderboard-list">
             <!-- 加载状态 -->
             <div v-if="leaderboardLoading" class="leaderboard-loading">
               <i class="fas fa-spinner fa-spin"></i>
               <span>加载中...</span>
             </div>
             
             <!-- 错误状态 -->
             <div v-else-if="leaderboardError" class="leaderboard-error">
               <i class="fas fa-exclamation-triangle"></i>
               <span>{{ leaderboardError }}</span>
               <button type="button" class="retry-btn" @click="fetchLeaderboardData">
                 <i class="fas fa-redo"></i>
                 重试
               </button>
             </div>
             
             <!-- 数据列表 -->
             <div v-else>
               <!-- 表头 -->
                <div class="leaderboard-item header">
                  <div>排名</div>
                  <div>用户名</div>
                  <div>得分</div>
                  <div>修改次数</div>
                  <div>最近修改时间</div>
                </div>
                
                <!-- 数据行，最多显示20条 -->
                <div v-for="(user, index) in leaderboardData.slice(0, 20)" :key="user.id" class="leaderboard-item">
                  <div class="rank-cell">{{ index + 1 }}</div>
                  <div class="user-cell">{{ user.userName || '未知用户' }}</div>
                  <div class="score-cell">{{ user.pdfScore || 0 }}</div>
                  <div class="count-cell">{{ user.pdfUpdateCount || 0 }}</div>
                  <div class="time-cell">{{ formatTime(user.updateTime) }}</div>
                </div>
               
               <!-- 空数据提示 -->
               <div v-if="leaderboardData.length === 0" class="leaderboard-empty">
                 <i class="fas fa-trophy"></i>
                 <span>暂无排行榜数据</span>
               </div>
             </div>
           </div>
         </div>
       </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, computed, onUnmounted } from 'vue'
import { useChatStore } from '@/stores/chatStore'
import { useAuthStore } from '@/stores/authStore'
import { useRouter } from 'vue-router'
import { chatApi } from '@/api/api'
import Message from './Message.vue'
import TypingIndicator from '../ui/TypingIndicator.vue'
import ThemeSelector from '../ui/ThemeSelector.vue'
import ModelSelector from '../ui/ModelSelector.vue'
import WelcomeSection from '../WelcomeSection.vue'
import ChatInput from '../ChatInput.vue'

const chatStore = useChatStore()
const authStore = useAuthStore()
const router = useRouter()
const chatContainer = ref(null)
const autoScroll = ref(true)
const showLeaderboard = ref(false)
const showScrollToBottom = ref(false)
const inputContainer = ref(null)
const leaderboardData = ref([])
const leaderboardLoading = ref(false)
const leaderboardError = ref('')
const updateCountdown = ref('')

const userInitials = computed(() => {
  return chatStore.user.name
    .split(' ')
    .map(n => n[0])
    .join('')
    .toUpperCase()
    .substring(0, 2)
})

const logout = () => {
  authStore.logout()
  chatStore.setAuthenticated(false)
  router.push('/login')
}

const closeTutorial = () => {
  chatStore.showTutorial = false
}

const handleModelChange = async (model) => {
  try {
    // 调用store中的changeModel方法切换模型
    const success = await chatStore.changeModel(model)
    if (success) {
      console.log('模型切换成功:', model)
      // 可以添加成功提示
    } else {
      console.error('模型切换失败')
      // 可以添加失败提示
    }
  } catch (error) {
    console.error('模型切换异常:', error)
    // 可以添加异常提示
  }
}

const handleMessagesClick = () => {
  if (chatStore.showTutorial) {
    chatStore.showTutorial = false
  }
}

const startNewChat = () => {
  chatStore.showTutorial = false
  // 触发新建聊天逻辑，可以通过事件或直接调用相关方法
}

const fetchLeaderboardData = async () => {
  leaderboardLoading.value = true
  leaderboardError.value = ''
  try {
    const response = await chatApi.getPdfRanking()
    if (response.code === 0 && response.data) {
      leaderboardData.value = response.data
    } else {
      leaderboardError.value = response.message || '获取排行榜数据失败'
    }
  } catch (error) {
    console.error('获取排行榜数据失败:', error)
    leaderboardError.value = '网络请求失败，请稍后重试'
  } finally {
    leaderboardLoading.value = false
  }
}

const toggleLeaderboard = () => {
  showLeaderboard.value = !showLeaderboard.value
  if (showLeaderboard.value) {
    fetchLeaderboardData()
    startCountdownTimer()
  } else {
    stopCountdownTimer()
  }
}

const formatTime = (timeString) => {
  if (!timeString) return '--'
  try {
    const date = new Date(timeString)
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return '--'
  }
}

const updateCountdownTimer = () => {
  const now = new Date()
  const tomorrow = new Date(now)
  tomorrow.setDate(tomorrow.getDate() + 1)
  tomorrow.setHours(0, 0, 0, 0)
  
  const diff = tomorrow.getTime() - now.getTime()
  
  if (diff <= 0) {
    updateCountdown.value = '正在更新...'
    return
  }
  
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)
  
  updateCountdown.value = `距离更新还剩 ${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
}

// 启动倒计时定时器
let countdownInterval = null
const startCountdownTimer = () => {
  updateCountdownTimer()
  countdownInterval = setInterval(updateCountdownTimer, 1000)
}

const stopCountdownTimer = () => {
  if (countdownInterval) {
    clearInterval(countdownInterval)
    countdownInterval = null
  }
}

const onScroll = () => {
  if (!chatContainer.value) return
  const el = chatContainer.value
  // 判断是否在底部（允许2px误差）
  showScrollToBottom.value = el.scrollHeight - el.scrollTop - el.clientHeight > 2
}

const scrollToBottom = () => {
  if (!chatContainer.value) return
  chatContainer.value.scrollTo({
    top: chatContainer.value.scrollHeight,
    behavior: 'smooth'
  })
}

// 保持原有autoScroll逻辑，或在消息变化后自动滚动到底部
watch(
  () => chatStore.messages.length,
  () => {
    nextTick(() => {
      scrollToBottom()
    })
  }
)

// 组件卸载时清理定时器
onUnmounted(() => {
  stopCountdownTimer()
})

// 其他原有方法保持不变...
</script>

<style scoped>
/* 原有样式保持不变 */

/* 新增样式 */
.leaderboard-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--input-bg);
  padding: 8px 12px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.1s;
}

.leaderboard-btn:hover {
  background: var(--sidebar-hover);
}



.leaderboard-header {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.leaderboard-header h3 {
  margin: 0;
  font-size: 16px;
}

.close-btn {
  background: none;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
}

.leaderboard-content {
  max-height: 400px;
  overflow-y: auto;
}

.leaderboard-item {
  display: grid;
  grid-template-columns: 60px 1fr 100px 100px 140px;
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
  transition: all 0.3s ease;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.leaderboard-item:hover {
  background: var(--sidebar-hover);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.leaderboard-item.header {
  font-weight: 600;
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: white;
  border-radius: 8px 8px 0 0;
  margin: 0 -20px;
  padding: 16px 40px;
  font-size: 14px;
}

.leaderboard-item.current-user {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15), rgba(139, 92, 246, 0.1));
  border-left: 4px solid var(--primary);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.2);
}

.leaderboard-item.top-three {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.1), rgba(255, 193, 7, 0.05));
}

/* 简洁表格样式 */
.rank-cell {
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: 500;
}

.user-cell {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  font-weight: 500;
}

.score-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
}

/* 修改次数和时间样式 */
.count-cell,
.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
  font-size: 14px;
}

.count-cell i,
.time-cell i {
  color: var(--primary);
  font-size: 12px;
}

/* 动画效果 */


.leaderboard-loading,
.leaderboard-error,
.leaderboard-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--text-secondary);
  gap: 12px;
}

.leaderboard-loading i {
  font-size: 24px;
  color: var(--primary);
}

.leaderboard-error {
  color: var(--error);
}

.leaderboard-error i {
  font-size: 24px;
}

.retry-btn {
  background: var(--primary);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: background 0.2s;
}

.retry-btn:hover {
  background: var(--primary-dark);
}

.leaderboard-empty i {
  font-size: 32px;
  color: var(--text-tertiary);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
}

.logout-btn {
  background: none;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  margin-left: 8px;
}

.logout-btn:hover {
  color: var(--error);
}

.input-box {
  position: relative;
  padding-right: 50px;
}


/* （省略样式，与之前相同） */
.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--chat-bg);
  position: relative;
  height: 100vh;
  overflow: hidden;
}

.content-header {
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--content-bg);
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.model-info {
  display: flex;
  align-items: center;
  gap: 10px;
  background: var(--input-bg);
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
}

.model-tag {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: white;
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  color: white;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  will-change: scroll-position;
  transform: translateZ(0);
}

.input-container {
  position: relative;
  background: transparent;
}

.input-tools {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
  padding-left: 16px;
  padding-right: 16px;
}

.tool-btn {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 16px;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.tool-btn:hover {
  background: var(--sidebar-hover);
  color: var(--text-primary);
}

.send-btn {
  background: var(--primary);
  color: white;
}

.send-btn:hover {
  background: var(--primary-dark);
  transform: translateY(-2px);
}

.disclaimer {
  text-align: center;
  font-size: 12px;
  color: var(--text-tertiary);
  padding: 8px 24px 0;
}

.disclaimer a {
  color: var(--primary);
  text-decoration: none;
}

.disclaimer a:hover {
  text-decoration: underline;
}

.scroll-to-bottom-btn {
  position: absolute;
  left: 50%;
  bottom: 100px; /* 再向上一些 */
  transform: translateX(-50%);
  z-index: 1001;
  width: 36px;
  height: 36px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.10);
  cursor: pointer;
  transition: background 0.1s, box-shadow 0.1s, opacity 0.1s;
  opacity: 0.6;
}
.scroll-to-bottom-btn:hover {
  background: var(--primary-dark);
  box-shadow: 0 4px 16px rgba(0,0,0,0.16);
  opacity: 0.85;
}

/* 消息动画 - 优化性能 */
.messages-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  will-change: transform;
  transform: translateZ(0);
}

.message-enter-active {
  transition: opacity 0.2s ease-out, transform 0.2s ease-out;
  will-change: opacity, transform;
}

.message-enter-from {
  opacity: 0;
  transform: translateY(15px);
}

.message-leave-active {
  transition: opacity 0.15s ease-out, transform 0.15s ease-out;
  will-change: opacity, transform;
}

.message-leave-to {
  opacity: 0;
  transform: translateY(-15px);
}

/* 打字指示器动画 - 优化性能 */
.typing-fade-enter-active,
.typing-fade-leave-active {
  transition: opacity 0.2s ease-out, transform 0.2s ease-out;
  will-change: opacity, transform;
}

.typing-fade-enter-from,
.typing-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* 图标切换动画 */
.icon-spin-enter-active,
.icon-spin-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.icon-spin-enter-from,
.icon-spin-leave-to {
  opacity: 0;
  transform: rotate(180deg) scale(0.8);
}

/* 弹跳动画 */
.bounce-enter-active {
  animation: bounce-in 0.25s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.bounce-leave-active {
  animation: bounce-out 0.15s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

@keyframes bounce-in {
  0% {
    opacity: 0;
    transform: translateX(-50%) translateY(15px);
  }
  100% {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

@keyframes bounce-out {
  0% {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
  100% {
    opacity: 0;
    transform: translateX(-50%) translateY(15px);
  }
}

/* 模态框动画 */
.modal-enter-active {
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.modal-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .leaderboard-content,
.modal-leave-to .leaderboard-content {
  transform: scale(0.9) translateY(-20px);
}

.modal-enter-active .leaderboard-content,
.modal-leave-active .leaderboard-content {
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}



/* 排行榜样式 */
.leaderboard-modal {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 700px;
  max-height: 75vh;
  background: linear-gradient(135deg, var(--content-bg) 0%, rgba(255, 255, 255, 0.05) 100%);
  backdrop-filter: blur(20px);
  z-index: 1000;
  opacity: 0;
  visibility: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25), 0 0 0 1px rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  overflow: hidden;
}

.leaderboard-modal.show {
  opacity: 1;
  visibility: visible;
  transform: translate(-50%, -50%) scale(1);
}

.leaderboard-modal:not(.show) {
  transform: translate(-50%, -50%) scale(0.95);
}

@keyframes modalFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.leaderboard-content {
  display: flex;
  flex-direction: column;
  max-height: 70vh;
  overflow: hidden;
  position: relative;
  border-radius: 20px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%);
  box-shadow: 
    0 25px 80px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(30px);
}

@keyframes modalSlideIn {
  from { 
    opacity: 0;
    transform: translateY(-50px) scale(0.9);
  }
  to { 
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.leaderboard-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 20%, rgba(99, 102, 241, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(168, 85, 247, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 60%, rgba(236, 72, 153, 0.05) 0%, transparent 50%);
  border-radius: 20px;
  pointer-events: none;
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.leaderboard-header {
  padding: 24px 28px;
  background: rgba(255, 255, 255, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  flex-shrink: 0;
  z-index: 2;
}

.leaderboard-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #6366f1, #8b5cf6, #ec4899, #f59e0b);
  background-size: 300% 100%;
  animation: gradientFlow 3s ease-in-out infinite;
}

@keyframes gradientFlow {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.header-title {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.leaderboard-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 12px;
}

.update-countdown {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
  opacity: 0.8;
}

.update-countdown i {
  color: var(--primary);
  font-size: 12px;
  animation: tick 1s ease-in-out infinite;
}

@keyframes tick {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.leaderboard-header h3::before {
  content: '🏆';
  font-size: 28px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
  animation: trophy-glow 2s ease-in-out infinite;
}

@keyframes trophy-glow {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.close-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 16px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.close-btn:hover {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.4);
  color: #ef4444;
  transform: rotate(90deg);
}

.leaderboard-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
  max-height: calc(70vh - 80px);
  scrollbar-width: thin;
  scrollbar-color: rgba(99, 102, 241, 0.3) transparent;
  position: relative;
  z-index: 1;
}

.leaderboard-list::-webkit-scrollbar {
  width: 6px;
}

.leaderboard-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.leaderboard-list::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.6) 0%, rgba(168, 85, 247, 0.6) 100%);
  border-radius: 3px;
  transition: background 0.3s ease;
}

.leaderboard-list::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.8) 0%, rgba(168, 85, 247, 0.8) 100%);
}

.leaderboard-item {
  display: grid;
  grid-template-columns: 60px 1fr 100px 100px 140px;
  gap: 16px;
  padding: 16px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  align-items: center;
  position: relative;
}

.leaderboard-item:last-child {
  border-bottom: none;
}

.leaderboard-item.header {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15) 0%, rgba(168, 85, 247, 0.15) 100%);
  font-weight: 700;
  color: var(--text-primary);
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid rgba(99, 102, 241, 0.3);
  backdrop-filter: blur(10px);
}

.leaderboard-item:not(.header) {
  background: rgba(255, 255, 255, 0.02);
}

.leaderboard-item:not(.header):hover {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08) 0%, rgba(168, 85, 247, 0.08) 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.leaderboard-item:not(.header):nth-child(2) {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.15) 0%, rgba(255, 193, 7, 0.15) 100%);
  border-left: 3px solid #ffd700;
  animation: goldShimmer 3s ease-in-out infinite;
}

.leaderboard-item:not(.header):nth-child(2) .rank-cell:not(.header .rank-cell) {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  color: #1a1a1a;
  box-shadow: 0 6px 20px rgba(255, 215, 0, 0.4);
}

.leaderboard-item:not(.header):nth-child(3) {
  background: linear-gradient(135deg, rgba(192, 192, 192, 0.15) 0%, rgba(169, 169, 169, 0.15) 100%);
  border-left: 3px solid #c0c0c0;
  animation: silverShimmer 3s ease-in-out infinite 0.5s;
}

.leaderboard-item:not(.header):nth-child(3) .rank-cell:not(.header .rank-cell) {
  background: linear-gradient(135deg, #c0c0c0 0%, #e5e5e5 100%);
  color: #1a1a1a;
  box-shadow: 0 6px 20px rgba(192, 192, 192, 0.4);
}

.leaderboard-item:not(.header):nth-child(4) {
  background: linear-gradient(135deg, rgba(205, 127, 50, 0.15) 0%, rgba(184, 115, 51, 0.15) 100%);
  border-left: 3px solid #cd7f32;
  animation: bronzeShimmer 3s ease-in-out infinite 1s;
}

.leaderboard-item:not(.header):nth-child(4) .rank-cell:not(.header .rank-cell) {
  background: linear-gradient(135deg, #cd7f32 0%, #e6a85c 100%);
  color: #1a1a1a;
  box-shadow: 0 6px 20px rgba(205, 127, 50, 0.4);
}

@keyframes goldShimmer {
  0%, 100% { box-shadow: 0 0 0 rgba(255, 215, 0, 0); }
  50% { box-shadow: 0 0 20px rgba(255, 215, 0, 0.3); }
}

@keyframes silverShimmer {
  0%, 100% { box-shadow: 0 0 0 rgba(192, 192, 192, 0); }
  50% { box-shadow: 0 0 20px rgba(192, 192, 192, 0.3); }
}

@keyframes bronzeShimmer {
  0%, 100% { box-shadow: 0 0 0 rgba(205, 127, 50, 0); }
  50% { box-shadow: 0 0 20px rgba(205, 127, 50, 0.3); }
}

.rank-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.rank-cell:not(.header .rank-cell) {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary) 0%, #8b5cf6 100%);
  color: white;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  position: relative;
  overflow: hidden;
}

.rank-cell:not(.header .rank-cell)::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, transparent 30%, rgba(255, 255, 255, 0.3) 50%, transparent 70%);
  transform: translateX(-100%);
  transition: transform 0.6s;
}

.leaderboard-item:hover .rank-cell::before {
  transform: translateX(100%);
}

.user-cell, .score-cell, .count-cell, .time-cell {
  display: flex;
  align-items: center;
  font-weight: 500;
}

.user-cell {
  justify-content: flex-start;
  color: var(--text-primary);
  font-weight: 600;
}

.score-cell {
  justify-content: center;
  color: var(--primary);
  font-weight: 700;
  font-size: 16px;
}

.count-cell, .time-cell {
  justify-content: center;
  font-size: 13px;
  color: var(--text-secondary);
  opacity: 0.8;
}

.count-cell::before {
  content: '✏️';
  margin-right: 6px;
  font-size: 12px;
}

.time-cell::before {
  content: '🕒';
  margin-right: 6px;
  font-size: 12px;
}



/* 教程覆盖层样式 */
.tutorial-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: var(--content-bg);
  z-index: 1000;
  overflow-y: auto;
}

/* 教程内容区域样式 */
.tutorial-content-area {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, rgba(var(--primary-rgb), 0.05) 0%, rgba(var(--primary-rgb), 0.1) 100%);
  padding: 20px;
  box-sizing: border-box;
  position: relative;
}

/* 为不同主题定义RGB值 */
:root {
  --primary-rgb: 99, 102, 241; /* 默认主题 */
}

.theme-light {
  --primary-rgb: 79, 70, 229;
}

.theme-dark {
  --primary-rgb: 99, 102, 241;
}

.theme-blue {
  --primary-rgb: 156, 163, 175;
}

.theme-green {
  --primary-rgb: 249, 168, 212;
}

.tutorial-close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  background: var(--content-bg);
  border: 1px solid #ef4444;
  color: #ef4444;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  transition: all 0.3s ease;
  box-shadow: var(--shadow);
  z-index: 1001;
}

.tutorial-close-btn:hover {
  background: #ef4444;
  color: white;
  transform: scale(1.1);
  border-color: #dc2626;
}

.tutorial-main {
  max-width: 1000px;
  margin: 0 auto;
  padding: 50px 20px 40px;
}

.tutorial-title {
  text-align: center;
  margin-bottom: 30px;
}

.tutorial-title h1 {
  color: var(--text-primary);
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.tutorial-welcome {
  text-align: center;
  margin-bottom: 40px;
}

.tutorial-welcome p {
  color: var(--text-secondary);
  font-size: 1.2rem;
  margin: 0;
  font-weight: 500;
}

.tutorial-info {
  margin-bottom: 40px;
  padding: 20px;
  background: rgba(var(--primary-rgb), 0.05);
  border-radius: 10px;
  border: 1px solid rgba(var(--primary-rgb), 0.1);
}

.contact-layout {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 30px;
}

.contact-text {
  flex: 1;
}

.contact-text p {
  color: var(--text-secondary);
  font-size: 1rem;
  line-height: 1.6;
  margin: 8px 0;
}

.qr-code-section {
  flex-shrink: 0;
}

.qr-code-image {
  width: 120px;
  height: 120px;
  border: 2px solid var(--border-color);
  border-radius: 8px;
  background: white;
  box-shadow: var(--shadow);
  object-fit: contain;
}

.project-link {
  font-weight: bold;
  color: var(--primary);
  text-decoration: none;
  transition: color 0.3s ease;
}

.project-link:hover {
  color: var(--primary-dark);
  text-decoration: underline;
}

.tutorial-description {
  text-align: center;
  margin-bottom: 50px;
}

.tutorial-description p {
  color: var(--text-primary);
  font-size: 1.3rem;
  font-weight: 600;
  margin: 0;
}

.features-list {
  margin-bottom: 40px;
}

.feature-section {
  margin-bottom: 30px;
}

.feature-section h3 {
  color: var(--text-primary);
  font-size: 1.3rem;
  font-weight: 600;
  margin: 0 0 15px 0;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 8px;
}

.feature-section p {
  color: var(--text-secondary);
  font-size: 1rem;
  line-height: 1.6;
  margin: 8px 0;
  padding-left: 0;
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

@media (max-width: 768px) {
  .feature-section {
    margin-bottom: 25px;
  }
  
  .feature-section h3 {
    font-size: 1.2rem;
  }
  
  .feature-section p {
    font-size: 0.9rem;
  }
  
  .leaderboard-content {
    width: 95%;
    margin: 20px;
  }
  
  .leaderboard-item {
    grid-template-columns: 50px 1fr 60px 60px;
    gap: 12px;
    padding: 12px 16px;
  }
  
  .tutorial-content-area {
    padding: 15px;
  }
  
  .tutorial-main {
    padding: 40px 10px 30px;
  }
  
  .tutorial-title h1 {
    font-size: 2rem;
  }
  
  .tutorial-info {
    padding: 15px;
  }
  
  .contact-layout {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .contact-text {
    text-align: left;
  }
  
  .tutorial-close-btn {
    width: 35px;
    height: 35px;
    font-size: 1rem;
  }
}

/* 教程界面过渡动画 - 优化性能 */
.tutorial-fade-enter-active,
.tutorial-fade-leave-active {
  transition: opacity 0.3s ease-out, transform 0.3s ease-out;
  will-change: opacity, transform;
}

.tutorial-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.tutorial-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.tutorial-fade-enter-to,
.tutorial-fade-leave-from {
  opacity: 1;
  transform: translateY(0);
}
</style>
