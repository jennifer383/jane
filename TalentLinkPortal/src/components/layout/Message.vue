<template>
  <div class="message-outer">
    <div class="message-inner">
      <div :class="['message', message.sender + '-message']">
        <template v-if="message.sender === 'user'">
          <div class="user-message-inner">
            <!-- 只有PDF文件时的简化显示 -->
            <template v-if="hasPdfFile && !displayText.trim()">
              <div class="pdf-only-container">
                <div class="pdf-only-indicator">
                  <div class="pdf-icon-container">
                    <i class="fas fa-file-pdf pdf-icon"></i>
                    <div class="loading-ring" v-if="isLoading"></div>
                  </div>
                  <div class="pdf-info">
                    <span class="pdf-name">{{ pdfFileName }}</span>
                    <span class="pdf-status" v-if="isLoading">正在处理...</span>
                    <span class="pdf-status success" v-else>已上传</span>
                  </div>
                </div>
              </div>
            </template>
            <!-- 有文字内容时的完整显示 -->
            <template v-else>
              <div class="message-content user-content">
                <!-- PDF文件指示器 -->
                <div v-if="hasPdfFile" class="pdf-indicator">
                  <div class="pdf-icon-container">
                    <i class="fas fa-file-pdf pdf-icon"></i>
                    <div class="loading-ring" v-if="isLoading"></div>
                  </div>
                  <div class="pdf-info">
                    <span class="pdf-name">{{ pdfFileName }}</span>
                    <span class="pdf-status" v-if="isLoading">正在处理...</span>
                    <span class="pdf-status success" v-else>已上传</span>
                  </div>
                </div>
                <span class="message-text" v-if="displayText.trim()">{{ displayText }}</span>
              </div>
            </template>
          </div>
        </template>
        <template v-else-if="message.sender === 'ai'">
          <div class="ai-message-inner">

            <div class="message-content ai-content">
              <div class="message-text ai-text" v-html="renderedMarkdown"></div>
            </div>
          </div>
        </template>
        <template v-else>
          <div class="message-content system-content">
            <span class="message-text">{{ message.text }}</span>
    </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, computed } from 'vue'
import { useChatStore } from '@/stores/chatStore'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})
 
const chatStore = useChatStore()

// 配置marked选项
const renderer = new marked.Renderer()
marked.use({
  renderer,
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang }).value
      } catch (err) {}
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true,
  gfm: true
})

// 检测是否包含PDF文件（通过Jane_pdf:标签或已上传PDF文件标识）
const hasPdfFile = computed(() => {
  return props.message.text && (
    props.message.text.includes('[已上传PDF文件:') || 
    props.message.text.startsWith('Jane_pdf:')
  )
})

// 提取PDF文件名
const pdfFileName = computed(() => {
  if (!hasPdfFile.value) return ''
  const match = props.message.text.match(/\[已上传PDF文件: (.+?)\]/)
  return match ? match[1] : 'PDF文件'
})

// 显示的文本（去除PDF文件信息和Jane_pdf:标签）
const displayText = computed(() => {
  if (!hasPdfFile.value) return props.message.text
  
  let text = props.message.text
  
  // 如果是Jane_pdf:标签，完全隐藏用户消息内容，只显示PDF图标
  if (text.startsWith('Jane_pdf:')) {
    return '' // 返回空字符串，只显示PDF图标
  }
  
  // 移除PDF文件信息（兼容旧格式）
  text = text.replace(/\n\n\[已上传PDF文件: .+?\]/, '').trim()
  
  return text
})

// 检测是否正在加载（AI还在输入状态且这是最后一条用户消息）
const isLoading = computed(() => {
  if (!hasPdfFile.value) return false
  const messages = chatStore.messages
  const currentIndex = messages.findIndex(m => m.id === props.message.id)
  const isLastUserMessage = currentIndex === messages.length - 2 // AI消息在最后
  return chatStore.isTyping && isLastUserMessage
})

// 渲染markdown（仅对AI消息）
const renderedMarkdown = computed(() => {
  if (props.message.sender !== 'ai') return props.message.text
  
  const text = props.message.text || ''
  if (!text.trim()) return text
  
  try {
    // 使用marked渲染markdown
    return marked(text)
  } catch (error) {
    console.error('Markdown渲染错误:', error)
    // 降级到简单的换行处理
    return text.replace(/\n/g, '<br>')
  }
})

// 格式化时间
const formatTime = (date) => {
  if (!(date instanceof Date)) {
    date = new Date(date);
  }
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}
</script>

<style scoped>
.message-outer {
  width: 100%;
  display: flex;
  justify-content: center;
}
.message-inner {
  width: 80%;
  max-width: 900px;
  min-width: 0;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
}
.message {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  margin-bottom: 16px;
  width: 100%;
  max-width: 100%;
  min-width: 0;
}
.user-message-inner {
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  justify-content: flex-end;
  width: 100%;
  margin-left: auto;
}



.user-content {
  background: var(--user-msg);
  border-top-right-radius: 4px;
  margin-right: 0;
  padding: 16px 20px;
  padding-top: 12px;
  border-radius: 12px;
  line-height: 1.5;
  font-size: 15px;
  max-width: calc(100% - 20px);
  box-shadow: var(--shadow);
  color: var(--text-primary);
}

.ai-message-inner {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: flex-start;
  width: 100%;
  margin-right: auto;
}
.ai-content {
  background: transparent;
  box-shadow: none;
  padding: 16px 0;
  color: var(--text-primary);
  max-width: 100%;
}
.ai-text {
  background: transparent;
  color: var(--text-primary);
  font-size: 15px;
  line-height: 1.7;
  padding: 0;
  border-radius: 0;
  box-shadow: none;
}
.system-content {
  background: rgba(245, 158, 11, 0.2);
  border: 1px solid rgba(245, 158, 11, 0.5);
}
.message-text {
  white-space: pre-wrap;
  word-break: break-word;
}

/* PDF指示器样式 */
.pdf-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding: 12px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 8px;
  transition: all 0.3s ease;
}

/* PDF独立容器样式 */
.pdf-only-container {
  position: relative;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  width: 100%;
  justify-content: flex-end;
}

/* PDF独立显示样式 */
.pdf-only-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  padding-top: 12px;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  max-width: calc(100% - 20px);
  margin-right: 0;
}

.pdf-only-indicator:hover {
  background: rgba(59, 130, 246, 0.15);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.pdf-icon-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
}

.pdf-icon {
  color: #ef4444;
  font-size: 20px;
  z-index: 2;
}

.pdf-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.pdf-name {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 14px;
}

.pdf-status {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 400;
}

.pdf-status.success {
  color: #10b981;
}

/* 加载动画 */
.loading-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 28px;
  height: 28px;
  border: 2px solid rgba(239, 68, 68, 0.2);
  border-top: 2px solid #ef4444;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  z-index: 1;
}

@keyframes spin {
  0% { transform: translate(-50%, -50%) rotate(0deg); }
  100% { transform: translate(-50%, -50%) rotate(360deg); }
}

/* 加载时的脉冲效果 */
.pdf-indicator:has(.loading-ring) {
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    background: rgba(239, 68, 68, 0.1);
    border-color: rgba(239, 68, 68, 0.2);
  }
  50% {
    background: rgba(239, 68, 68, 0.15);
    border-color: rgba(239, 68, 68, 0.3);
  }
}

/* Markdown样式 - 优化间距 */
.ai-text :deep(h1),
.ai-text :deep(h2),
.ai-text :deep(h3),
.ai-text :deep(h4),
.ai-text :deep(h5),
.ai-text :deep(h6) {
  margin: 0.5em 0 0.15em 0;
  font-weight: 600;
  line-height: 1.3;
  color: var(--text-primary);
}

.ai-text :deep(h1) { font-size: 2.2em; }
.ai-text :deep(h2) { font-size: 1.8em; }
.ai-text :deep(h3) { font-size: 1.5em; }

.ai-text :deep(p) {
  margin: 0.25em 0;
  line-height: 1.5;
  font-size: 1.1em;
}

.ai-text :deep(ul),
.ai-text :deep(ol) {
  margin: 0.25em 0;
  padding-left: 0.6em;
}

.ai-text :deep(li) {
  margin: 0.1em 0;
  line-height: 1.4;
  font-size: 1.05em;
}

.ai-text :deep(blockquote) {
  margin: 0.4em 0;
  padding: 0.3em 0.5em;
  border-left: 2px solid var(--primary-color, #3b82f6);
  background: transparent;
  font-style: italic;
  border-radius: 0;
}

.ai-text :deep(code) {
  background: transparent;
  padding: 0.075em 0.15em;
  border-radius: 0;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.95em;
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.ai-text :deep(pre) {
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 0;
  padding: 0.4em;
  margin: 0.3em 0;
  overflow-x: auto;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.85em;
  line-height: 1.4;
}

.ai-text :deep(pre code) {
  background: none;
  padding: 0;
  border-radius: 0;
  font-size: 1em;
}

.ai-text :deep(table) {
  border-collapse: collapse;
  margin: 0.4em 0;
  width: 100%;
  font-size: 1em;
}

.ai-text :deep(th),
.ai-text :deep(td) {
  border: 1px solid var(--border-color);
  padding: 0.2em 0.4em;
  text-align: left;
}

.ai-text :deep(th) {
  background: transparent;
  font-weight: 600;
}

.ai-text :deep(a) {
  color: var(--primary-color, #3b82f6);
  text-decoration: none;
  transition: color 0.2s ease;
}

.ai-text :deep(a:hover) {
  text-decoration: underline;
  color: var(--primary-dark, #2563eb);
}

.ai-text :deep(strong) {
  font-weight: 600;
  color: var(--text-primary);
}

.ai-text :deep(em) {
  font-style: italic;
  color: var(--text-secondary);
}

.ai-text :deep(hr) {
  border: none;
  border-top: 1px solid var(--border-color);
  margin: 0.5em 0;
  opacity: 0.6;
}


</style>