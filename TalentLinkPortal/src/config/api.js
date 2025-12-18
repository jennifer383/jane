// API配置文件
export const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081',
  ENDPOINTS: {
    LOGIN: '/jane/login',
    REGISTER: '/jane/register',
    CHAT: '/jane/chat',
    PDF_UPLOAD: '/jane/pdf/upload',
    CHAT_HISTORY: '/jane/titlehistory',
    CHAT_MESSAGES: '/jane/chathistory',
    DELETE_CHAT: '/jane/delete-chat',
    PDF_RANKING: '/jane/pdf/ranking',
    MODEL: '/jane/model'
  }
}

// 获取完整的API URL
export const getApiUrl = (endpoint) => {
  return `${API_CONFIG.BASE_URL}${endpoint}`
}