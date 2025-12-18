import { defineStore } from 'pinia'
import { ref } from 'vue'
import { chatApi } from '@/api/api'

const generateTempChatId = () => {
  return Date.now().toString(); // 直接返回时间戳
}

// 创建新聊天逻辑
const createNewChat = () => {
  const newChatId = generateTempChatId();
  currentChatId.value = newChatId;
  isNewChat = true;
}

export const useChatStore = defineStore('chat', () => {
  // 从localStorage恢复状态
  const savedState = JSON.parse(localStorage.getItem('chatAppState') || '{}')
  
  const currentTheme = ref(savedState.currentTheme || 'dark')
  const currentModel = ref(savedState.currentModel || 'qwen-turbo')
  const models = ref([
    {
      value: 'qwen-turbo',
      label: 'DeepSeek-V3.2 (默认)',
      description: '高性能中文AI模型，适合各种对话场景'
    },
    {
      value: 'gpt-4-turbo',
      label: 'GPT-4 Turbo',
      description: 'OpenAI最新一代模型，强大的多模态能力'
    },
    {
      value: 'claude-3-opus',
      label: 'Claude 3 Opus',
      description: 'Anthropic最新模型，擅长处理长文本'
    },
    {
      value: 'gemini-pro',
      label: 'Gemini Pro',
      description: 'Google的AI模型，强大的推理能力'
    },
    {
      value: 'llama-3-70b',
      label: 'Llama 3 70B',
      description: 'Meta的开源大模型，适合多种应用场景'
    },
    {
      value: 'deepseek-chat',
      label: 'DeepSeek Chat',
      description: '专注于对话的AI模型'
    },
    {
      value: 'kimi-k2-thinking',
      label: 'Kimi K2 Thinking',
      description: '字节跳动的AI模型，擅长创造性思维'
    }
  ])
  const messages = ref(savedState.messages || [])
  const chatHistory = ref(savedState.chatHistory || [])
  const isTyping = ref(false)
  const isAuthenticated = ref(savedState.isAuthenticated || false)
  const currentChatId = ref(savedState.currentChatId || null)
  const showTutorial = ref(false)
  const apiStatus = ref({
    lastError: null,
    lastSuccess: null,
    isConnected: false
  })

  const user = ref(savedState.user || {
    id: 'admin',
    name: '管理员',
    avatar: null
  })

  const themeVars = {
    dark: {
      '--primary': '#6366f1',
      '--primary-dark': '#4f46e5',
      '--sidebar-bg': '#1e1f25',
      '--sidebar-hover': '#2d2f3a',
      '--sidebar-active': '#3a3d4a',
      '--content-bg': '#121318',
      '--chat-bg': '#0f1015',
      '--user-msg': '#343541',
      '--ai-msg': '#444654',
      '--text-primary': '#f1f1f1',
      '--text-secondary': '#b4b4b8',
      '--text-tertiary': '#8a8a94',
      '--border-color': '#2d2f3a',
      '--input-bg': '#2d2f3a',
      '--success': '#10b981',
      '--warning': '#f59e0b',
      '--error': '#ef4444',
      '--shadow': '0 4px 12px rgba(0, 0, 0, 0.25)'
    },
    light: {
      '--primary': '#4f46e5',
      '--primary-dark': '#4338ca',
      '--sidebar-bg': '#f9fafb',
      '--sidebar-hover': '#f3f4f6',
      '--sidebar-active': '#e5e7eb',
      '--content-bg': '#ffffff',
      '--chat-bg': '#f9fafb',
      '--user-msg': '#e0e7ff',
      '--ai-msg': '#f0f9ff',
      '--text-primary': '#1f2937',
      '--text-secondary': '#4b5563',
      '--text-tertiary': '#6b7280',
      '--border-color': '#e5e7eb',
      '--input-bg': '#ffffff',
      '--success': '#10b981',
      '--warning': '#f59e0b',
      '--error': '#ef4444',
      '--shadow': '0 4px 12px rgba(0, 0, 0, 0.08)'
    },
    blue: {
      '--primary': '#9ca3af',
      '--primary-dark': '#6b7280',
      '--sidebar-bg': '#f3f4f6',
      '--sidebar-hover': '#e5e7eb',
      '--sidebar-active': '#d1d5db',
      '--content-bg': '#f9fafb',
      '--chat-bg': '#f3f4f6',
      '--user-msg': '#e2e8f0',
      '--ai-msg': '#dbeafe',
      '--text-primary': '#1f2937',
      '--text-secondary': '#4b5563',
      '--text-tertiary': '#6b7280',
      '--border-color': '#d1d5db',
      '--input-bg': '#e5e7eb',
      '--success': '#10b981',
      '--warning': '#f59e0b',
      '--error': '#ef4444',
      '--shadow': '0 4px 12px rgba(0, 0, 0, 0.08)'
    },
    green: {
      '--primary': '#f9a8d4',
      '--primary-dark': '#f472b6',
      '--sidebar-bg': '#fdf2f8',
      '--sidebar-hover': '#fbcfe8',
      '--sidebar-active': '#f9a8d4',
      '--content-bg': '#fff1f2',
      '--chat-bg': '#fff9fb',
      '--user-msg': '#fce7f3',
      '--ai-msg': '#fbcfe8',
      '--text-primary': '#831843',
      '--text-secondary': '#9d174d',
      '--text-tertiary': '#be185d',
      '--border-color': '#f9a8d4',
      '--input-bg': '#fdf2f8',
      '--success': '#10b981',
      '--warning': '#f59e0b',
      '--error': '#ef4444',
      '--shadow': '0 4px 12px rgba(0, 0, 0, 0.08)'
    }
  }

  const setTheme = (theme) => {
    currentTheme.value = theme
    document.body.className = `theme-${theme}`
    // 同步主题变量到:root
    const vars = themeVars[theme]
    if (vars) {
      Object.keys(vars).forEach(key => {
        document.documentElement.style.setProperty(key, vars[key])
      })
    }
    saveState() // 保存状态
  }

  const setActiveChat = async (chatId) => {
    // 检查chatId是否存在于历史记录中
    if (chatId && !chatId.startsWith('temp-') && !chatHistory.value.find(chat => chat.id === chatId)) {
      console.warn('尝试切换到不存在的会话:', chatId)
      return
    }
    
    // 立即更新当前会话ID，确保后续的消息更新只影响新会话
    currentChatId.value = chatId;
    messages.value = []; // 切换会话时清空消息
    isTyping.value = false; // 重置打字状态
    
    try {
      isTyping.value = true;
      const messagesData = await chatApi.getChatHistoryMessages(chatId);
      
      // 再次检查当前会话ID，防止在加载过程中用户又切换了会话
      if (currentChatId.value === chatId) {
        messages.value = (messagesData || []).map(msg => ({
          id: msg.metadata?.timestamp || Date.now(),
          text: msg.text,
          sender: msg.messageType === 'USER' ? 'user' : 'ai',
          timestamp: msg.metadata?.timestamp ? new Date(msg.metadata.timestamp) : new Date()
        }));
        isTyping.value = false;
        // 更新历史会话激活状态 - 使用响应式方式
        chatHistory.value = chatHistory.value.map(chat => ({
          ...chat,
          active: chat.id === chatId
        }));
        apiStatus.value = {
          lastError: null,
          lastSuccess: `成功加载会话 ${chatId}`,
          isConnected: true
        };
        saveState(); // 保存状态
      }
    } catch (error) {
      console.error('加载消息失败:', error);
      // 只有当前会话ID仍然匹配时才处理错误
      if (currentChatId.value === chatId) {
        handleApiError(error, '加载消息');
        isTyping.value = false;
      }
    }
  }

  const sendMessage = async (message) => {
    if (!message.trim()) return

    let isNewChat = false;
    let tempChatId = null;
    if (!currentChatId.value) {
      tempChatId = generateTempChatId(); // 现在会生成temp-171234567890格式的ID
      currentChatId.value = tempChatId;
      isNewChat = true;
    } else if (currentChatId.value.startsWith('temp-')) {
      isNewChat = true; // 如果当前是临时会话ID，也标记为新会话
    }

    const userMessage = {
      id: `user-${Date.now()}`,
      text: message,
      sender: 'user',
      timestamp: new Date()
    }
    messages.value.push(userMessage)

    const tempAIMessage = {
      id: `ai-${Date.now()}`,
      text: '',
      sender: 'ai',
      timestamp: new Date()
    }
    messages.value.push(tempAIMessage)

    isTyping.value = true  // 开始发送，显示打字状态

    try {
      await chatApi.simpleChat(message, currentChatId.value, async () => {
        if (isNewChat) {
          await refreshChatHistory();
          // 新会话刷新后自动激活当前chatId（如果已被正式ID替换，currentChatId已变更）
          if (currentChatId.value && !currentChatId.value.startsWith('temp-')) {
            await setActiveChat(currentChatId.value);
          }
        }
      });
      apiStatus.value = {
        lastError: null,
        lastSuccess: '消息发送成功',
        isConnected: true
      }
    } catch (error) {
      console.error('API请求失败:', error)
      handleApiError(error, '发送消息')

      const errorIndex = messages.value.findIndex(m => m.id === tempAIMessage.id)
      if (errorIndex !== -1) {
        messages.value[errorIndex] = {
          ...tempAIMessage,
          text: `请求失败: ${getUserFriendlyError(error)}`,
          isError: true
        }
      }
    } finally {
      isTyping.value = false
    }
  }

  const sendPdfMessage = async (message, pdfFile) => {
    if (!message.trim() && !pdfFile) return

    let isNewChat = false;
    let tempChatId = null;
    if (!currentChatId.value) {
      tempChatId = generateTempChatId();
      currentChatId.value = tempChatId;
      isNewChat = true;
    } else if (currentChatId.value.startsWith('temp-')) {
      isNewChat = true;
    }

    // 在消息前添加特殊标签Jane_pdf:，用于标识PDF文件消息
    const messageWithTag = pdfFile ? `Jane_pdf:${message}` : message;
    
    const userMessage = {
      id: `user-${Date.now()}`,
      text: messageWithTag + (pdfFile ? `\n\n[已上传PDF文件: ${pdfFile.name}]` : ''),
      sender: 'user',
      timestamp: new Date()
    }
    messages.value.push(userMessage)

    const tempAIMessage = {
      id: `ai-${Date.now()}`,
      text: '',
      sender: 'ai',
      timestamp: new Date()
    }
    messages.value.push(tempAIMessage)

    isTyping.value = true

    try {
      // 先上传PDF文件
      let uploadResult = null
      if (pdfFile) {
        // 传递带有Jane_ai标签的消息给后端
        uploadResult = await chatApi.uploadPdf(pdfFile, currentChatId.value, messageWithTag)
        console.log('PDF上传结果:', uploadResult)
      }

      apiStatus.value = {
        lastError: null,
        lastSuccess: 'PDF消息发送成功',
        isConnected: true
      }
    } catch (error) {
      console.error('PDF API请求失败:', error)
      handleApiError(error, '发送PDF消息')

      const errorIndex = messages.value.findIndex(m => m.id === tempAIMessage.id)
      if (errorIndex !== -1) {
        messages.value[errorIndex] = {
          ...tempAIMessage,
          text: `PDF请求失败: ${getUserFriendlyError(error)}`,
          isError: true
        }
      }
    } finally {
      isTyping.value = false
    }
  }

  const updateLastAIMessage = (text) => {
    // 确保只更新当前会话的消息
    if (!currentChatId.value) return
    
    for (let i = messages.value.length - 1; i >= 0; i--) {
      if (messages.value[i].sender === 'ai') {
        messages.value[i].text = text
        return
      }
    }

    messages.value.push({
      id: `ai-${Date.now()}`,
      text: text,
      sender: 'ai',
      timestamp: new Date()
    })
  }

  const finalizeAIMessage = (text) => {
    // 确保只更新当前会话的消息
    if (!currentChatId.value) return
    updateLastAIMessage(text)
  }

  const setAuthenticated = (status) => {
    isAuthenticated.value = status
    saveState() // 保存状态
  }

  const setUser = (userData) => {
    user.value = { ...user.value, ...userData }
    saveState() // 保存状态
  }

  const setCurrentModel = (model) => {
    if (model) {
      currentModel.value = model
      saveState() // 保存状态
    }
  }

  const changeModel = async (model) => {
    try {
      // 调用后端API切换模型
      const res = await chatApi.changeModel(model)
      if (res.code === 0) {
        // 更新本地模型状态
        currentModel.value = model
        saveState() // 保存状态
        console.log('[chatStore] 模型切换成功:', model)
        return true
      } else {
        console.error('[chatStore] 模型切换失败:', res.message)
        return false
      }
    } catch (error) {
      console.error('[chatStore] 模型切换异常:', error)
      return false
    }
  }

  const loadModelInfo = async () => {
    try {
      const res = await chatApi.getModel()
      if (res.code === 0 && res.data) {
        const modelName = res.data.model
        if (modelName) {
          currentModel.value = modelName
          saveState() // 保存状态
          console.log('[chatStore] 模型信息已更新:', modelName)
        }
      }
    } catch (error) {
      console.warn('获取模型信息失败:', error)
      // 不抛出错误，使用默认值即可
    }
  }

  const loadChatHistory = async () => {
    console.log('[chatStore] loadChatHistory called, userId:', user.value.id)
    try {
      const res = await chatApi.getChatHistoryList(user.value.id)
      console.log('[chatStore] getChatHistoryList result:', res)

      // 使用响应式方式更新整个数组，过滤掉null值
      const list = (res.data || [])
        .filter(item => item && item.status) // 过滤掉null和无效项
        .sort((a, b) => Number(b.chatId) - Number(a.chatId))
        .map(item => ({
          id: item.chatId,
          title: item.title || '无标题会话',
          createdAt: Date.now(),
          active: false
        }))

      chatHistory.value = list

      // 只在没有当前会话时，自动进入最近的会话
      if (!currentChatId.value && chatHistory.value.length > 0) {
        const latest = chatHistory.value.reduce((max, chat) => Number(chat.id) > Number(max.id) ? chat : max, chatHistory.value[0])
        setActiveChat(latest.id)
      }
      apiStatus.value = {
        lastError: null,
        lastSuccess: '历史记录加载成功',
        isConnected: true
      }
    } catch (error) {
      console.error('加载历史会话失败:', error)
      handleApiError(error, '加载历史记录')
    }
  }

  // 刷新会话列表并保持当前会话的激活状态
  const refreshChatHistory = async () => {
    console.log('[chatStore] refreshChatHistory called, currentId:', currentChatId.value)
    const currentId = currentChatId.value
    await loadChatHistory()
    if (currentId && !currentId.startsWith('temp-')) {
      // 恢复当前会话的激活状态 - 使用响应式方式
      chatHistory.value = chatHistory.value.map(chat => ({
        ...chat,
        active: chat.id === currentId
      }))
    }
  }

  const deleteChat = async (chatId) => {
    try {
      await chatApi.deleteChat(chatId)

      chatHistory.value = chatHistory.value.filter(chat => chat.id !== chatId)

      if (currentChatId.value === chatId) {
        currentChatId.value = null
        messages.value = []
      }

      apiStatus.value = {
        lastError: null,
        lastSuccess: `会话 ${chatId} 已删除`,
        isConnected: true
      }
    } catch (error) {
      console.error('删除会话失败:', error)
      handleApiError(error, '删除会话')
    }
  }

  const handleApiError = (error, action) => {
    apiStatus.value = {
      lastError: {
        action,
        message: error.message,
        details: error.details || '',
        code: error.code || 'UNKNOWN',
        timestamp: new Date().toISOString()
      },
      lastSuccess: null,
      isConnected: false
    }
    console.error(`API错误 [${action}]:`, error)
  }

  const getUserFriendlyError = (error) => {
    if (error.code === 'TIMEOUT') {
      return '请求超时，系统已自动重试。如仍失败，请检查网络连接后重试'
    } else if (error.code === 'CHAT_FAILED') {
      return '聊天请求失败，请稍后再试'
    } else if (error.code === 'UPLOAD_FAILED') {
      return 'PDF上传失败，系统已自动重试。请检查文件大小和网络连接'
    } else if (error.code === 'CONNECTION_FAILED') {
      return '无法连接到服务器，请检查网络连接'
    } else if (error.details) {
      return `服务器错误: ${error.details}`
    }
    return error.message || '未知错误，请稍后重试'
  }

  // 保存状态到localStorage
  const saveState = () => {
    const stateToSave = {
      currentTheme: currentTheme.value,
      currentModel: currentModel.value,
      messages: messages.value,
      chatHistory: chatHistory.value,
      isAuthenticated: isAuthenticated.value,
      currentChatId: currentChatId.value,
      user: user.value
    }
    localStorage.setItem('chatAppState', JSON.stringify(stateToSave))
  }

  // 监听页面刷新，保存状态
  if (typeof window !== 'undefined') {
    window.addEventListener('beforeunload', saveState)
    
    // 定期保存状态（每30秒）
    setInterval(saveState, 30000)
  }

  // 清空聊天状态（用于登录时重置）
  const clearChatState = () => {
    messages.value = []
    chatHistory.value = []
    currentChatId.value = null
    isTyping.value = false
    apiStatus.value = {
      lastError: null,
      lastSuccess: null,
      isConnected: false
    }
    // 清空localStorage中的聊天状态
    localStorage.removeItem('chatAppState')
  }

  return {
    currentTheme,
    currentModel,
    models,
    messages,
    chatHistory,
    isTyping,
    isAuthenticated,
    currentChatId,
    showTutorial,
    user,
    apiStatus,

    setTheme,
    setActiveChat,
    sendMessage,
    sendPdfMessage,
    updateLastAIMessage,
    finalizeAIMessage,
    setAuthenticated,
    setUser,
    setCurrentModel,
    changeModel, // 新增导出
    loadModelInfo,
    loadChatHistory,
    refreshChatHistory, // 新增导出
    deleteChat,
    getUserFriendlyError,
    saveState,
    clearChatState // 新增导出
  }
})
