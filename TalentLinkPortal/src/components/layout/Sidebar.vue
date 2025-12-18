<template>
  <div class="sidebar" :class="{ active: isSidebarActive, collapsed: isCollapsed }">
    <div class="sidebar-header">
      <div class="logo">
        <div class="logo-icon">
          <i class="fas fa-brain"></i>
        </div>
        <div v-if="!isCollapsed" class="logo-text">JaneAI</div>
      </div>
      <button
        type="button"
        v-if="!isCollapsed"
        class="collapse-btn"
        @click="toggleSidebar"
        :title="isCollapsed ? '展开侧边栏' : '收起侧边栏'"
      >
        <i :class="isCollapsed ? 'fas fa-chevron-right' : 'fas fa-chevron-left'"></i>
      </button>
      <!-- 折叠时按钮单独放在logo下方 -->
      <button
        type="button"
        v-if="isCollapsed"
        class="collapse-btn collapsed-btn"
        @click="toggleSidebar"
        :title="isCollapsed ? '展开侧边栏' : '收起侧边栏'"
      >
        <i class="fas fa-chevron-right"></i>
      </button>
    </div>
    <div class="sidebar-content" v-show="!isCollapsed">
      <button type="button" class="new-chat-btn" @click="createNewChat">
        <transition name="icon-rotate" mode="out-in">
          <i class="fas fa-plus" key="plus"></i>
        </transition>
        新聊天
      </button>
      
      <div class="search-box">
        <i class="fas fa-search"></i>
        <input 
          type="text" 
          placeholder="搜索聊天记录..." 
          v-model="searchQuery"
        >
      </div>
      
      <div class="section-title">资源库</div>
      <transition-group name="resource" tag="div" class="resource-container">
        <div class="resource-btn" key="tutorial" @click="goToTutorial">
          <i class="fas fa-file-alt"></i>
          <span>使用教程</span>
        </div>
      </transition-group>
      
      <div class="section-title">聊天历史</div>
      <div class="history-list">
        <!-- 游客用户显示当前会话 -->
        <div v-if="isGuestUser && chatStore.currentChatId" 
             class="history-item simple active"
             @click="selectChat(chatStore.currentChatId)">
          <span class="history-title simple-title">当前会话</span>
        </div>
        
        <!-- 普通用户显示历史记录 -->
        <template v-else>
          <transition name="fade">
          <div 
            v-if="chatStore.chatHistory.length === 0"
            class="history-empty"
          >
            <i class="fas fa-inbox"></i>
            <span>没有历史记录</span>
          </div>
        </transition>
        
        <transition-group name="history" tag="div" class="history-container">
          <div 
            class="history-item simple" 
            v-for="chat in filteredHistory" 
            :key="chat.id"
            :class="{ active: chat.active }"
            @click="selectChat(chat.id)"
          >
            <span class="history-title simple-title">{{ chat.title }}</span>
          </div>
        </transition-group>
        </template>
      </div>
    </div>
    
    <!-- 登录提示模态框 -->
    <div v-if="showLoginModal" class="modal-overlay" @click="showLoginModal = false">
      <div class="login-modal" @click.stop>
        <div class="modal-header">
          <div class="modal-icon">
            <i class="fas fa-user-lock"></i>
          </div>
          <h3 class="modal-title">需要登录</h3>
          <button type="button" class="modal-close" @click="showLoginModal = false">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <p class="modal-message">游客模式下无法创建新会话</p>
          <p class="modal-submessage">请登录后体验完整功能，包括：</p>
          <ul class="feature-list">
            <li><i class="fas fa-check"></i> 创建多个会话</li>
            <li><i class="fas fa-check"></i> 保存聊天历史</li>
            <li><i class="fas fa-check"></i> 个性化设置</li>
            <li><i class="fas fa-check"></i> 更多高级功能</li>
          </ul>
        </div>
        <div class="modal-footer">
           <button type="button" class="btn-secondary" @click="showLoginModal = false">继续游客模式</button>
           <button type="button" class="btn-primary" @click="goToLogin">立即登录</button>
         </div>
      </div>
    </div>

    <!-- 使用教程模态框 -->
    <div v-if="showTutorialModal" class="modal-overlay" @click="showTutorialModal = false">
      <div class="tutorial-modal" @click.stop>
        <div class="modal-header">
          <div class="modal-icon">
            <i class="fas fa-graduation-cap"></i>
          </div>
          <h3 class="modal-title">使用教程</h3>
          <button type="button" class="modal-close" @click="showTutorialModal = false">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body tutorial-content">
          <div class="tutorial-section">
            <h4><i class="fas fa-rocket"></i> 快速开始</h4>
            <ul>
              <li>在聊天框中输入您的问题或需求</li>
              <li>点击发送按钮或按 Enter 键发送消息</li>
              <li>AI 会智能分析并回复您的问题</li>
              <li>可以进行多轮对话，保持上下文连贯</li>
            </ul>
          </div>
          
          <div class="tutorial-section">
            <h4><i class="fas fa-lightbulb"></i> 主要功能</h4>
            <ul>
              <li><strong>智能对话：</strong>自然语言交流，理解上下文</li>
              <li><strong>多轮对话：</strong>保持话题连贯性</li>
              <li><strong>聊天历史：</strong>保存和管理对话记录</li>
              <li><strong>主题切换：</strong>支持明暗主题切换</li>
              <li><strong>排行榜：</strong>查看用户活跃度排名</li>
            </ul>
          </div>
          
          <div class="tutorial-section">
            <h4><i class="fas fa-tips"></i> 使用技巧</h4>
            <ul>
              <li>尽量详细描述您的需求，获得更准确的回答</li>
              <li>可以要求 AI 以特定格式回复（如表格、列表等）</li>
              <li>支持代码相关问题的咨询和解答</li>
              <li>可以进行角色扮演对话</li>
              <li>支持中英文等多语言交流</li>
            </ul>
          </div>
          
          <div class="tutorial-section">
            <h4><i class="fas fa-user-friends"></i> 账户功能</h4>
            <ul>
              <li><strong>游客模式：</strong>可以体验基本对话功能</li>
              <li><strong>注册登录：</strong>享受完整功能，包括历史记录保存</li>
              <li><strong>个人设置：</strong>自定义主题、模型选择等</li>
            </ul>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn-primary" @click="showTutorialModal = false">开始使用</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chatStore'
const props = defineProps({
  isCollapsed: Boolean
})
const emit = defineEmits(['toggle-collapse'])
const router = useRouter()
const chatStore = useChatStore()
const isSidebarActive = ref(false)
const searchQuery = ref('')
const showLoginModal = ref(false)
const showTutorialModal = ref(false)

// 跳转到登录页面
const goToLogin = () => {
  showLoginModal.value = false
  router.push('/login')
}

// 显示教程界面
const goToTutorial = () => {
  chatStore.showTutorial = true
}

const toggleSidebar = () => {
  emit('toggle-collapse')
}

// 移动端自动收起侧边栏
watch(isSidebarActive, (val) => {
  if (window.innerWidth <= 768 && !val) {
    emit('toggle-collapse') // 自动收起时也触发折叠
  }
})

// 创建新聊天
const createNewChat = () => {
  // 判断是否为游客
  let userId = chatStore.user?.id;
  if (typeof userId === 'string') userId = userId.trim();
  if (userId === 0 || userId === '0') {
    showLoginModal.value = true;
    return;
  }
  // 如果教程正在显示，先关闭教程
  if (chatStore.showTutorial) {
    chatStore.showTutorial = false
  }
  const newChatId = Date.now().toString();
  // 取消所有激活 - 使用响应式方式
  const updatedHistory = chatStore.chatHistory.map(chat => ({
    ...chat,
    active: false
  }));
  
  // 添加新聊天 - 使用响应式方式
  chatStore.chatHistory = [
    {
      id: newChatId,
      title: `新聊天 ${new Date().toLocaleTimeString()}`,
      createdAt: Date.now(),
      active: true
    },
    ...updatedHistory
  ];
  // 激活新聊天
  chatStore.setActiveChat(newChatId);
  // 清空当前消息
  chatStore.messages = [];
  chatStore.currentChatId = newChatId;
}

// 选择聊天
const selectChat = (chatId) => {
  // 如果教程正在显示，先关闭教程
  if (chatStore.showTutorial) {
    chatStore.showTutorial = false
  }
  chatStore.setActiveChat(chatId)
}

// 删除聊天
const deleteChat = (chatId) => {
  if (confirm('确定要删除此会话吗？删除后将无法恢复。')) {
    chatStore.deleteChat(chatId)
  }
}

// 判断是否为游客用户
const isGuestUser = computed(() => {
  const userId = chatStore.user?.id
  return userId === 0 || userId === '0'
})

// 过滤历史记录
const filteredHistory = computed(() => {
  if (!searchQuery.value) return chatStore.chatHistory
  
  return chatStore.chatHistory.filter(chat => 
    chat.title.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// 格式化日期
const formatDate = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleDateString([], { month: 'numeric', day: 'numeric' })
}
</script>

<style scoped>
/* 历史记录项样式 */
.history-item {
  position: relative;
  padding: 12px 16px 12px 40px;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: all 0.1s;
}

.history-item:hover {
  background: var(--sidebar-hover);
}

.history-item.active {
  background: var(--sidebar-active);
}

.history-title {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-date {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 4px;
}

.history-item i.fa-comment {
  position: absolute;
  left: 16px;
  top: 16px;
  color: var(--text-tertiary);
}

.history-item.active i.fa-comment {
  color: var(--primary);
}

.history-empty {
  padding: 20px;
  text-align: center;
  color: var(--text-tertiary);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.history-empty i {
  font-size: 24px;
}

/* 删除按钮样式 */
.delete-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.1s;
}

.history-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  color: var(--error);
}

/* 其他样式保持不变 */

.history-item {
  position: relative;
  padding-right: 40px; /* 为删除按钮留空间 */
}

.delete-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}

.history-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  color: var(--error);
}

  .sidebar {
    width: 280px;
    min-width: 66px;
    max-width: 280px;
    background: var(--sidebar-bg);
    display: flex;
    flex-direction: column;
    border-right: 1px solid var(--border-color);
    transform: translateX(0);
    transition: 
      width 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94),
      transform 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94),
      min-width 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94),
      max-width 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    z-index: 100;
    position: relative;
  }
  .sidebar.collapsed {
    width: 66px;
    min-width: 66px;
    max-width: 66px;
    overflow: hidden;
    box-shadow: none;
  }
  .app-container {
    transition: margin-left 0.15s cubic-bezier(.4,0,.2,1);
  }
  
  .sidebar-header {
    padding: 20px 16px 12px 16px;
    border-bottom: 1px solid var(--border-color);
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    position: relative;
  }
  
  .logo {
    display: flex;
    align-items: center;
    gap: 12px;
    justify-content: flex-start;
    position: relative;
  }
  
  .logo-icon {
    background: linear-gradient(135deg, var(--primary), #8b5cf6);
    width: 36px;
    height: 36px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    color: white;
  }
  
  .logo-text {
    font-size: 18px;
    font-weight: 700;
    background: linear-gradient(90deg, var(--primary), #a78bfa);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    opacity: 1;
    transform: translateX(0);
    transition: 
      opacity 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94),
      transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  }
  
  .sidebar.collapsed .logo-text {
    opacity: 0;
    transform: translateX(-10px);
  }
  
  .sidebar-content {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    opacity: 1;
    transition: 
      opacity 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94),
      transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    transform: translateX(0);
  }
  .sidebar.collapsed .sidebar-content {
    opacity: 0;
    pointer-events: none;
    transform: translateX(-10px);
  }
  
  .new-chat-btn {
    background: linear-gradient(135deg, var(--primary), var(--primary-dark));
    color: white;
    border: none;
    border-radius: 8px;
    padding: 12px 16px;
    display: flex;
    align-items: center;
    gap: 10px;
    width: 100%;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    margin-bottom: 20px;
  }
  
  .new-chat-btn:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow);
  }
  
  .search-box {
    position: relative;
    margin-bottom: 20px;
  }
  
  .search-box input {
    width: 100%;
    background: var(--input-bg);
    border: 1px solid var(--border-color);
    border-radius: 8px;
    padding: 10px 16px 10px 40px;
    color: var(--text-primary);
    font-size: 14px;
    outline: none;
  }
  
  .search-box i {
    position: absolute;
    left: 16px;
    top: 50%;
    transform: translateY(-50%);
    color: var(--text-tertiary);
  }
  
  .section-title {
    font-size: 13px;
    text-transform: uppercase;
    color: var(--text-tertiary);
    margin: 24px 0 12px;
    font-weight: 600;
    letter-spacing: 0.5px;
  }
  
  .resource-btn {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 16px;
    border-radius: 8px;
    color: var(--text-secondary);
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;
  }
  
  .resource-btn:hover {
    background: var(--sidebar-hover);
    color: var(--text-primary);
  }
  
  .history-list {
    margin-top: 12px;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .history-item {
    display: flex;
    align-items: center;
    gap: 10px;
    background: var(--sidebar-bg);
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(60,0,120,0.04);
    padding: 12px 16px;
    cursor: pointer;
    transition: background 0.1s, box-shadow 0.1s, color 0.1s;
    border: 1.5px solid transparent;
    position: relative;
    min-height: 48px;
  }
  
  .history-item:hover {
    background: var(--sidebar-hover);
    box-shadow: 0 4px 16px rgba(60,0,120,0.08);
    color: var(--primary);
  }
  
  .history-item.active {
    background: var(--sidebar-active);
    border-color: var(--primary);
    color: var(--primary);
    box-shadow: 0 4px 16px rgba(99,102,241,0.10);
  }
  
  .history-item i {
    color: var(--text-tertiary);
  }

/* 折叠按钮样式 */
.collapse-btn {
  background: var(--sidebar-bg);
  border: none;
  color: var(--text-primary);
  font-size: 18px;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: 
    background 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94),
    color 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94),
    transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94),
    opacity 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  z-index: 101;
  margin-left: auto;
  opacity: 1;
}
.collapse-btn:hover {
  background: var(--sidebar-hover);
  color: var(--primary);
  transform: scale(1.1);
}
.collapsed-btn {
  position: absolute;
  left: 50%;
  top: 70px;
  transform: translateX(-50%);
  margin-top: 8px;
}
.logo-row {
  display: flex;
  align-items: center;
  width: 100%;
  justify-content: center;
  position: relative;
  left: 0px;
}

/* 新增：确保删除按钮在折叠状态下不可见 */
.sidebar.collapsed .delete-btn {
  display: none;
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .sidebar {
    position: absolute;
    height: 100%;
    transform: translateX(-100%);
    transition: transform 0.2s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  }
  
  .sidebar.active {
    transform: translateX(0);
  }
}

.history-item.simple {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
  cursor: pointer;
  transition: background 0.09s, color 0.09s;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}
.history-item.simple.active {
  background: var(--sidebar-active);
  color: var(--primary);
}
.history-item.simple:hover {
  background: var(--sidebar-hover);
  color: var(--primary);
}
.simple-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  padding-left: 0;
  text-align: left;
  max-width: 100%;
}

/* 登录模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.15s ease-out;
}

.login-modal {
  background: var(--content-bg);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  max-width: 420px;
  width: 90%;
  max-height: 90vh;
  overflow: hidden;
  animation: slideUp 0.15s ease-out;
  border: 1px solid var(--border-color);
}

.modal-header {
  padding: 24px 24px 16px;
  text-align: center;
  position: relative;
  border-bottom: 1px solid var(--border-color);
}

.modal-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, var(--primary), #8b5cf6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  font-size: 24px;
  color: white;
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.3);
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.modal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  background: transparent;
  border: none;
  color: var(--text-tertiary);
  font-size: 18px;
  cursor: pointer;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.modal-close:hover {
  background: var(--sidebar-hover);
  color: var(--text-primary);
}

.modal-body {
  padding: 24px;
}

.modal-message {
  font-size: 16px;
  color: var(--text-primary);
  margin: 0 0 12px;
  text-align: center;
}

.modal-submessage {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 16px;
  text-align: center;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.feature-list li i {
  color: var(--success);
  font-size: 12px;
  width: 16px;
}

.modal-footer {
  padding: 16px 24px 24px;
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn-primary, .btn-secondary {
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  min-width: 120px;
}

.btn-primary {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.3);
}

.btn-secondary {
  background: var(--input-bg);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
}

.btn-secondary:hover {
  background: var(--sidebar-hover);
  color: var(--text-primary);
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 新增动画样式 */
/* 图标旋转动画 */
.icon-rotate-enter-active,
.icon-rotate-leave-active {
  transition: all 0.15s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.icon-rotate-enter-from,
.icon-rotate-leave-to {
  opacity: 0;
  transform: rotate(180deg) scale(0.8);
}

/* 资源按钮动画 */
.resource-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.resource-enter-active {
  transition: all 0.2s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.resource-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.resource-move {
  transition: transform 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

/* 历史记录动画 */
.history-container {
  display: flex;
  flex-direction: column;
}

.history-enter-active {
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.history-enter-from {
  opacity: 0;
  transform: translateX(-30px) scale(0.95);
}

.history-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.history-leave-to {
  opacity: 0;
  transform: translateX(30px) scale(0.95);
}

.history-move {
  transition: transform 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

/* 增强按钮悬停效果 */
.new-chat-btn {
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.new-chat-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.2);
}

.new-chat-btn:active {
  transform: translateY(0);
}

.resource-btn {
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.resource-btn:hover {
  transform: translateX(5px);
  background: var(--sidebar-hover);
  border-left: 3px solid var(--primary);
}

.collapse-btn {
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.collapse-btn:hover {
  transform: scale(1.1);
  background: var(--sidebar-hover);
}

.collapse-btn:active {
  transform: scale(0.95);
}

/* 搜索框动画 */
.search-box {
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.search-box:focus-within {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.1);
}

/* 教程弹窗样式 */
.tutorial-modal {
  background: var(--content-bg);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-width: 600px;
  width: 90vw;
  max-height: 80vh;
  overflow: hidden;
  animation: slideUp 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.tutorial-content {
  max-height: 60vh;
  overflow-y: auto;
  padding: 0 24px;
}

.tutorial-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}

.tutorial-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.tutorial-section h4 {
  color: var(--primary);
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tutorial-section h4 i {
  font-size: 14px;
}

.tutorial-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tutorial-section li {
  padding: 6px 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.5;
  position: relative;
  padding-left: 16px;
}

.tutorial-section li::before {
  content: '•';
  color: var(--primary);
  position: absolute;
  left: 0;
  top: 6px;
}

.tutorial-section strong {
  color: var(--text-primary);
  font-weight: 600;
}

/* 滚动条样式 */
.tutorial-content::-webkit-scrollbar {
  width: 6px;
}

.tutorial-content::-webkit-scrollbar-track {
  background: var(--input-bg);
  border-radius: 3px;
}

.tutorial-content::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}

.tutorial-content::-webkit-scrollbar-thumb:hover {
  background: var(--text-tertiary);
}

</style>
