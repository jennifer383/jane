// src/api/index.js
import { useChatStore } from '@/stores/chatStore'
import { getApiUrl, API_CONFIG } from '@/config/api'

// 超时封装
async function fetchWithTimeout(url, options = {}, timeout = 60000) {
  const controller = new AbortController()
  const id = setTimeout(() => controller.abort(), timeout)

  try {
    const response = await fetch(url, {
      ...options,
      signal: controller.signal
    })
    clearTimeout(id)

    if (!response.ok) {
      const error = new Error(`HTTP error! status: ${response.status}`)
      error.status = response.status
      throw error
    }

    return response
  } catch (error) {
    if (error.name === 'AbortError') {
      const timeoutError = new Error('请求超时，请稍后再试')
      timeoutError.code = 'TIMEOUT'
      throw timeoutError
    }
    throw error
  }
}

function buildUrl(baseUrl, params) {
  const url = new URL(baseUrl)
  Object.keys(params).forEach(key => {
    if (params[key] !== undefined) {
      url.searchParams.append(key, params[key])
    }
  })
  return url.toString()
}

async function processStreamResponse(response, onData, onComplete) {
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let result = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) {
      onComplete(result)
      return result
    }

    const chunk = decoder.decode(value, { stream: true })
    result += chunk
    onData(result)
  }
}

class APIError extends Error {
  constructor(message, statusCode) {
    super(message)
    this.name = 'APIError'
    this.statusCode = statusCode
  }
}

function getAuthHeaders() {
  const userId = localStorage.getItem('userId') || '0'
  const token = localStorage.getItem('token') || ''
  return {
    'userId': userId,
    'Authorization': token
  }
}

export const chatApi = {
  async uploadPdf(file, chatId, message = '', retries = 2) {
    let lastError
    
    for (let attempt = 0; attempt <= retries; attempt++) {
      try {
        const formData = new FormData()
        formData.append('file', file)
        if (message) {
          formData.append('message', message)
        }

        const timeout = attempt === 0 ? 90000 : 120000 // PDF上传需要更长时间
        const response = await fetchWithTimeout(getApiUrl(API_CONFIG.ENDPOINTS.PDF_UPLOAD), {
          method: 'POST',
          headers: {
            ...getAuthHeaders(),
            'chatId': chatId || ''
            // 不设置Content-Type，让浏览器自动设置multipart/form-data边界
          },
          body: formData
        }, timeout)

        return processStreamResponse(response,
          (data) => {
            const store = useChatStore()
            // 只有当前会话ID匹配时才更新消息
            if (store.currentChatId === chatId) {
              store.updateLastAIMessage(data)
            }
          },
          (completeData) => {
            const store = useChatStore()
            // 只有当前会话ID匹配时才处理完成回调
            if (store.currentChatId === chatId) {
              try {
                const json = JSON.parse(completeData)
                store.finalizeAIMessage(json.message || 'PDF上传完成')
                // 如果后端返回了模型信息，更新当前模型
                if (json.model) {
                  store.setCurrentModel(json.model)
                }
                if (store.currentChatId.startsWith('temp-') && json.chatId) {
                  const newChatId = json.chatId
                  store.refreshChatHistory().then(() => {
                    store.currentChatId = newChatId
                    store.setActiveChat(newChatId)
                  }).catch(err => {
                    console.error('刷新聊天历史记录失败:', err)
                  })
                } else {
                  store.refreshChatHistory().catch(err => {
                    console.error('刷新聊天历史记录失败:', err)
                  })
                }
              } catch {
                store.finalizeAIMessage(completeData || 'PDF上传完成')
              }
            }
          }
        )
      } catch (error) {
        console.error(`PDF upload error (attempt ${attempt + 1}):`, error)
        lastError = error
        
        // 如果不是超时错误或已经是最后一次尝试，直接抛出错误
        if (error.code !== 'TIMEOUT' || attempt === retries) {
          break
        }
        
        // 等待一段时间后重试
        await new Promise(resolve => setTimeout(resolve, 3000 * (attempt + 1)))
      }
    }
    
    throw new APIError(lastError.message, lastError.code || 'UPLOAD_FAILED', lastError.status || 500)
  },
  async simpleChat(prompt, chatId, onAIComplete, retries = 2) {
    const url = buildUrl(getApiUrl(API_CONFIG.ENDPOINTS.CHAT), { prompt, chatId })
    let lastError

    for (let attempt = 0; attempt <= retries; attempt++) {
      try {
        const timeout = attempt === 0 ? 60000 : 90000 // 第一次60秒，重试时90秒
        const response = await fetchWithTimeout(url, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            ...getAuthHeaders()
          }
        }, timeout)

        return processStreamResponse(response,
          (data) => {
            const store = useChatStore()
            // 只有当前会话ID匹配时才更新消息
            if (store.currentChatId === chatId) {
              store.updateLastAIMessage(data)
            }
          },
          (completeData) => {
            const store = useChatStore()
            // 只有当前会话ID匹配时才处理完成回调
            if (store.currentChatId === chatId) {
              console.log('[api.js] simpleChat onComplete, completeData:', completeData)
              let aiMessage = ''
              let responseChatId = ''
              // 兼容非JSON格式，尝试正则提取内容
              try {
                // 1. 优先尝试JSON解析
                const json = JSON.parse(completeData)
                aiMessage = json.message
                responseChatId = json.chatId
                // 如果后端返回了模型信息，更新当前模型
                if (json.model) {
                  store.setCurrentModel(json.model)
                }
                console.log('[api.js] simpleChat onComplete, parsed json:', json)
              } catch (e) {
                // 2. 非JSON格式，尝试正则提取AI回复内容和chatId
                // 提取AI回复内容（假设**@Jane**后到~为AI回复）
                const aiMatch = completeData.match(/\*\*@Jane\*\*([\s\S]*?)~/)
                if (aiMatch) {
                  aiMessage = aiMatch[1].trim()
                } else {
                  aiMessage = completeData
                }
                // 提取chatId（如有）
                const chatIdMatch = completeData.match(/chatId[：: ]*([\w-]+)/i)
                if (chatIdMatch) {
                  responseChatId = chatIdMatch[1]
                }
                console.warn('[api.js] simpleChat onComplete, fallback parse:', aiMessage, responseChatId)
              }
              store.finalizeAIMessage(aiMessage)
              if (store.currentChatId.startsWith('temp-') && responseChatId) {
                const newChatId = responseChatId
                store.refreshChatHistory().then(() => {
                  store.currentChatId = newChatId
                  store.chatHistory.forEach(chat => {
                    chat.active = chat.id === newChatId
                  })
                }).catch(err => {
                  console.error('刷新聊天历史记录失败:', err)
                })
              } else {
                store.refreshChatHistory().catch(err => {
                  console.error('刷新聊天历史记录失败:', err)
                })
              }
              if (typeof onAIComplete === 'function') {
                onAIComplete();
              }
            }
          }
        )
      } catch (error) {
        console.error(`Simple chat error (attempt ${attempt + 1}):`, error)
        lastError = error
        
        // 如果不是超时错误或已经是最后一次尝试，直接抛出错误
        if (error.code !== 'TIMEOUT' || attempt === retries) {
          break
        }
        
        // 等待一段时间后重试
        await new Promise(resolve => setTimeout(resolve, 2000 * (attempt + 1)))
      }
    }
    
    throw new APIError(lastError.message, lastError.code || 'CHAT_FAILED', lastError.status || 500)
  },



  async getChatHistoryList(userId) {
    const url = getApiUrl(API_CONFIG.ENDPOINTS.CHAT_HISTORY);
    try {
      const response = await fetchWithTimeout(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeaders()
        },
        body: JSON.stringify({ userId })
      });
      return await response.json();
    } catch (error) {
      throw new APIError(error.message, error.status || 500);
    }
  },

  async getChatHistoryMessages(chatId) {
    const url = `${getApiUrl(API_CONFIG.ENDPOINTS.CHAT_MESSAGES)}?chatId=${encodeURIComponent(chatId)}`;
    try {
      const response = await fetchWithTimeout(url, {
        method: 'GET',
        headers: {
          ...getAuthHeaders()
        }
      });
      const res = await response.json();
      return res.data || [];
    } catch (error) {
      throw new APIError(error.message, error.status || 500);
    }
  },

  async deleteChat(chatId) {
    if (chatId.startsWith('temp-')) return { skipped: true }

    const url = buildUrl(getApiUrl(API_CONFIG.ENDPOINTS.DELETE_CHAT), { chatId })

    try {
      const response = await fetchWithTimeout(url, {
        method: 'DELETE',
        headers: {
          ...getAuthHeaders()
        }
      })
      return await response.json()
    } catch (error) {
      throw new APIError(error.message, error.status || 500)
    }
  },

  async getPdfRanking() {
    const url = getApiUrl(API_CONFIG.ENDPOINTS.PDF_RANKING)
    try {
      const response = await fetchWithTimeout(url, {
        method: 'GET',
        headers: {
          ...getAuthHeaders()
        }
      })
      return await response.json()
    } catch (error) {
      throw new APIError(error.message, error.status || 500)
    }
  },

  async getModel() {
    const url = getApiUrl(API_CONFIG.ENDPOINTS.MODEL)
    try {
      const response = await fetchWithTimeout(url, {
        method: 'GET',
        headers: {
          ...getAuthHeaders()
        }
      })
      return await response.json()
    } catch (error) {
      throw new APIError(error.message, error.status || 500)
    }
  },

  async changeModel(model) {
    const url = buildUrl(getApiUrl(API_CONFIG.ENDPOINTS.MODEL), { model })
    try {
      const response = await fetchWithTimeout(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeaders()
        },
        body: JSON.stringify({ model })
      })
      return await response.json()
    } catch (error) {
      throw new APIError(error.message, error.status || 500)
    }
  }
}
