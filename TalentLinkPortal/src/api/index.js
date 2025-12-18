// ... existing code ...
return processStreamResponse(response,
  (data) => {
    const store = useChatStore()
    store.updateLastAIMessage(data)
  },
  async (completeData) => {
    const store = useChatStore()
    try {
      const json = JSON.parse(completeData)
      store.finalizeAIMessage(json.message)

      // 当收到新的 chatId（新建会话时），更新并刷新历史会话列表
      if (typeof json.chatId === 'string' && /^\d+$/.test(json.chatId)) {
        store.currentChatId = json.chatId
        await store.refreshChatHistory()
      }
    } catch (e) {
      store.finalizeAIMessage(completeData)
    }

    if (typeof onAIComplete === 'function') {
      onAIComplete()
    }
  }
)
// ... existing code ...
