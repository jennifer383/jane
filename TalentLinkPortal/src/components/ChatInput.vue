<template>
  <div class="chat-input-container">
    <!-- PDF上传区域 -->
    <div v-if="uploadedFile" class="uploaded-file">
      <div class="file-info">
        <i class="fas fa-file-pdf file-icon"></i>
        <span class="file-name">{{ uploadedFile.name }}</span>
        <button type="button" @click="removeFile" class="remove-file-btn">
          <i class="fas fa-times"></i>
        </button>
      </div>
    </div>

    <!-- 主输入区域 -->
    <div class="input-wrapper" :class="{ 
      'focused': isFocused, 
      'error': hasError
    }">
      <!-- 浮动标签 -->
      <label class="floating-label" :class="{ 'active': isFocused || inputText || uploadedFile }">
        {{ 
          chatStore.isTyping ? '请稍后...' : 
          uploadedFile ? '已上传PDF文件，点击发送按钮发送' : 
          '输入您的问题或上传PDF文件...' 
        }}
      </label>
      
      <!-- 输入框 -->
      <div class="input-content">
        <textarea
          ref="textareaRef"
          v-model="inputText"
          :disabled="uploadedFile"
          :placeholder="isFocused ? '' : ''"
          @focus="handleFocus"
          @blur="handleBlur"
          @keydown="handleKeydown"
          @input="adjustHeight"
          class="message-input"
          :class="{ 'disabled-by-file': uploadedFile }"
          rows="1"
        ></textarea>
        
        <!-- 右侧按钮组 -->
        <div class="action-buttons">
          <!-- PDF上传按钮 -->
          <button 
            type="button"
            @click="triggerFileUpload" 
            class="action-btn upload-btn"
            :disabled="chatStore.isTyping || (chatStore.user?.id === 0 || chatStore.user?.id === '0')"
            :title="(chatStore.user?.id === 0 || chatStore.user?.id === '0') ? 'PDF上传功能需要登录后使用' : '上传PDF文件'"
            :class="{ 'guest-disabled': (chatStore.user?.id === 0 || chatStore.user?.id === '0') }"
          >
            <i class="fas fa-paperclip"></i>
          </button>
          
          <!-- 清除按钮 -->
          <button 
            type="button"
            v-if="inputText.trim() || uploadedFile"
            @click="clearInput" 
            class="action-btn clear-btn"
            title="清除内容"
          >
            <i class="fas fa-times"></i>
          </button>
          
          <!-- 发送按钮 -->
          <button
            type="button"
            @click="handleSend"
            :disabled="(!inputText.trim() && !uploadedFile) || chatStore.isTyping"
            class="action-btn send-btn"
            :class="{ 'active': (inputText.trim() || uploadedFile) && !chatStore.isTyping }"
            title="发送消息"
          >
            <i v-if="chatStore.isTyping" class="fas fa-spinner fa-spin"></i>
            <i v-else class="fas fa-paper-plane"></i>
          </button>
        </div>
      </div>
      
      <!-- 隐藏的文件输入 -->
      <input 
        ref="fileInputRef"
        type="file" 
        accept=".pdf"
        @change="handleFileUpload"
        style="display: none;"
      />
    </div>
    
    <!-- 错误提示 -->
    <div v-if="errorMessage" class="error-message">
      <div class="error-content">
        <i class="fas fa-exclamation-circle"></i>
        <span>{{ errorMessage }}</span>
      </div>
      <button 
        v-if="lastFailedMessage || lastFailedFile"
        type="button"
        @click="retryLastMessage" 
        class="retry-btn"
        title="重试发送"
      >
        <i class="fas fa-redo"></i>
        重试
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useChatStore } from '@/stores/chatStore'

const chatStore = useChatStore()
const inputText = ref('')
const isFocused = ref(false)
const hasError = ref(false)
const errorMessage = ref('')
const uploadedFile = ref(null)
const textareaRef = ref(null)
const fileInputRef = ref(null)
const lastFailedMessage = ref(null)
const lastFailedFile = ref(null)

// 焦点处理
const handleFocus = () => {
  isFocused.value = true
  hasError.value = false
  errorMessage.value = ''
}

const handleBlur = () => {
  isFocused.value = false
}

// 自动调整textarea高度
const adjustHeight = () => {
  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
      const scrollHeight = textareaRef.value.scrollHeight
      const maxHeight = 120 // 最大高度
      textareaRef.value.style.height = Math.min(scrollHeight, maxHeight) + 'px'
    }
  })
}

// 键盘事件处理
const handleKeydown = (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    handleSend()
  }
}

// 文件上传处理
const triggerFileUpload = () => {
  // 检查是否为游客用户
  if (chatStore.user?.id === 0 || chatStore.user?.id === '0') {
    showError('PDF文件上传功能需要登录后使用，请先登录')
    return
  }
  
  fileInputRef.value?.click()
}

const handleFileUpload = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 验证文件类型
  if (file.type !== 'application/pdf') {
    showError('请选择PDF文件')
    return
  }
  
  // 验证文件大小 (10MB)
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    showError('文件大小不能超过10MB')
    return
  }
  
  uploadedFile.value = file
  hasError.value = false
  errorMessage.value = ''
  
  // 清空文件输入
  event.target.value = ''
}

// 移除文件
const removeFile = () => {
  uploadedFile.value = null
}

// 清除输入
const clearInput = () => {
  inputText.value = ''
  uploadedFile.value = null
  hasError.value = false
  errorMessage.value = ''
  adjustHeight()
}

// 显示错误
const showError = (message) => {
  hasError.value = true
  errorMessage.value = message
  setTimeout(() => {
    hasError.value = false
    errorMessage.value = ''
  }, 3000)
}

// 发送消息
const handleSend = async () => {
  if ((!inputText.value.trim() && !uploadedFile.value) || chatStore.isTyping) {
    return
  }
  
  // 检查游客用户是否尝试上传PDF
  if (uploadedFile.value && (chatStore.user?.id === 0 || chatStore.user?.id === '0')) {
    showError('PDF文件上传功能需要登录后使用，请先登录')
    return
  }
  
  try {
    let messageContent = inputText.value.trim()
    const fileToUpload = uploadedFile.value
    
    // 清除之前的失败记录
    lastFailedMessage.value = null
    lastFailedFile.value = null
    
    // 立即清空输入框
    inputText.value = ''
    uploadedFile.value = null
    adjustHeight()
    
    // 如果有上传的PDF文件，调用PDF专用方法
    if (fileToUpload) {
      await chatStore.sendPdfMessage(messageContent, fileToUpload)
    } else if (messageContent) {
      // 普通文本消息
      await chatStore.sendMessage(messageContent)
    }
  } catch (error) {
    // 保存失败的消息以便重试
    lastFailedMessage.value = messageContent
    lastFailedFile.value = fileToUpload
    
    showError('发送失败，请重试')
  }
}

// 重试最后失败的消息
const retryLastMessage = async () => {
  if (chatStore.isTyping) return
  
  try {
    const messageContent = lastFailedMessage.value
    const fileToRetry = lastFailedFile.value
    
    // 清除失败记录
    lastFailedMessage.value = null
    lastFailedFile.value = null
    hasError.value = false
    errorMessage.value = ''
    
    // 重新发送
    if (fileToRetry) {
      await chatStore.sendPdfMessage(messageContent, fileToRetry)
    } else if (messageContent) {
      await chatStore.sendMessage(messageContent)
    }
  } catch (error) {
    showError('重试失败，请检查网络连接')
  }
}
</script>

<style scoped>
.chat-input-container {
  padding: 16px;
  background: transparent;
  width: 65%;
  margin: 0 auto;
}

/* 上传文件显示区域 */
.uploaded-file {
  margin-bottom: 12px;
  padding: 12px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  animation: slideIn 0.15s ease-out;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-icon {
  color: var(--error);
  font-size: 16px;
}

.file-name {
  flex: 1;
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
}

.remove-file-btn {
  background: none;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.1s ease;
}

.remove-file-btn:hover {
  color: var(--error);
  background: transparent;
}

/* 主输入包装器 */
.input-wrapper {
  position: relative;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 32px;
  transition: all 0.1s ease;
  overflow: hidden;
  box-shadow: none;
}

.input-wrapper:hover {
  border-color: var(--primary);
  box-shadow: none;
}

.input-wrapper.focused {
  border-color: var(--primary);
  box-shadow: none;
  background: var(--input-bg);
}

.input-wrapper.error {
  border-color: var(--error);
  box-shadow: none;
  animation: shake 0.25s ease-in-out;
}

.input-wrapper.disabled {
  opacity: 0.6;
  cursor: not-allowed;
  border-color: var(--border-color);
}

/* 浮动标签 */
.floating-label {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-tertiary);
  font-size: 15px;
  font-weight: 400;
  pointer-events: none;
  transition: all 0.2s ease;
  background: transparent;
  padding: 0;
  z-index: 1;
}

.floating-label.active {
  top: -16px;
  left: 16px;
  transform: translateY(0);
  font-size: 12px;
  font-weight: 500;
  color: var(--primary);
  background: var(--input-bg);
  padding: 0 8px;
  border-radius: 4px;
}

/* 输入内容区域 */
.input-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 12px;
  min-height: 65px;
}

/* 消息输入框 */
.message-input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: var(--text-primary);
  font-size: 15px;
  transition: all 0.2s ease;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  line-height: 1.4;
  resize: none;
  min-height: 30px;
  max-height: 120px;
  overflow-y: auto;
  padding: 8px 0;
  margin: 0;
  display: flex;
  align-items: center;
}

.message-input.disabled-by-file {
  opacity: 0.5;
  cursor: not-allowed;
  color: var(--text-tertiary);
}

.message-input.disabled-by-file::placeholder {
  color: var(--text-tertiary);
  opacity: 0.7;
}

.message-input::placeholder {
  color: var(--text-tertiary);
  opacity: 1;
}

.message-input:disabled {
  cursor: not-allowed;
}

/* 按钮组 */
.action-buttons {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: 8px;
}

/* 通用按钮样式 */
.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 16px;
  position: relative;
  overflow: hidden;
}

.action-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.action-btn:hover:not(:disabled) {
  transform: scale(1.1);
}

/* 上传按钮 */
.upload-btn {
  background: transparent;
  color: var(--text-tertiary);
}

.upload-btn:hover:not(:disabled) {
  background: transparent;
  color: var(--text-secondary);
}

/* 游客用户禁用状态 */
.upload-btn.guest-disabled {
  opacity: 0.3;
  cursor: not-allowed;
  color: var(--text-tertiary);
}

.upload-btn.guest-disabled:hover {
  transform: none;
  color: var(--text-tertiary);
  background: transparent;
}

/* 清除按钮 */
.clear-btn {
  background: transparent;
  color: var(--text-tertiary);
}

.clear-btn:hover {
  background: transparent;
  color: var(--error);
}

/* 发送按钮 */
.send-btn {
  background: transparent;
  color: var(--text-tertiary);
}

.send-btn.active {
  background: var(--primary);
  color: white;
}

.send-btn.active:hover:not(:disabled) {
  background: var(--primary-dark);
}

.send-btn:disabled {
  background: transparent;
  color: var(--text-tertiary);
}

/* 错误消息 */
.error-message {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 8px;
  padding: 8px 12px;
  background: transparent;
  border: 1px solid var(--error);
  border-radius: 8px;
  color: var(--error);
  font-size: 14px;
  animation: slideIn 0.15s ease-out;
}

.error-content {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.retry-btn {
  background: var(--error);
  color: white;
  border: none;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.retry-btn:hover {
  background: var(--error-hover, #dc2626);
  transform: translateY(-1px);
}

.retry-btn:active {
  transform: translateY(0);
}

/* 动画 */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-4px); }
  75% { transform: translateX(4px); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-input-container {
    padding: 12px;
  }
  
  .input-wrapper {
    border-radius: 12px;
  }
  
  .input-content {
    padding: 12px;
  }
  
  .floating-label {
    font-size: 14px;
  }
  
  .floating-label.active {
    font-size: 11px;
  }
  
  .message-input {
    font-size: 14px;
  }
  
  .action-btn {
    width: 32px;
    height: 32px;
    font-size: 12px;
  }
}

/* 深色主题适配 */
@media (prefers-color-scheme: dark) {
  .input-wrapper {
    box-shadow: none;
  }
  
  .input-wrapper:hover {
    box-shadow: none;
  }
  
  .input-wrapper.focused {
    box-shadow: none;
  }
}
</style>
