import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  createSession as apiCreateSession,
  listSessions,
  listMessages,
  deleteSession as apiDeleteSession,
  deleteMessage as apiDeleteMessage,
  sendMessage as apiSendMessage,
  streamChat
} from '../api/chat'

/**
 * Chat Store
 *
 * Centralized state management for the chat functionality.
 * Handles sessions, messages, and streaming responses.
 */
export const useChatStore = defineStore('chat', () => {
  // State
  const sessions = ref([])
  const currentSessionId = ref(null)
  const messages = ref({
    'temp-session': [] // Initialize temporary session messages array
  })
  const isStreaming = ref(false)
  const streamingContent = ref('')
  const isLoading = ref(false)
  const error = ref(null)
  const isNewChat = ref(false)

  // Computed properties
  const sortedSessions = computed(() => {
    return [...sessions.value].sort((a, b) => {
      return new Date(b.createTime) - new Date(a.createTime)
    })
  })

  const currentMessages = computed(() => {
    if (isNewChat.value) {
      return messages.value['temp-session'] || []
    }

    if (!currentSessionId.value || !messages.value[currentSessionId.value]) {
      return []
    }

    return [...messages.value[currentSessionId.value]].sort((a, b) => {
      return new Date(a.createTime) - new Date(b.createTime)
    })
  })

  const currentSession = computed(() => {
    return sessions.value.find(session => session.id === currentSessionId.value) || null
  })

  // Actions
  /**
   * Fetch all chat sessions
   */
  async function fetchSessions() {
    isLoading.value = true
    error.value = null

    try {
      const response = await listSessions()

      if (response.code === 0) {
        sessions.value = response.data || []
        return response.data
      } else {
        throw new Error(response.message || 'Failed to load sessions')
      }
    } catch (err) {
      error.value = err.message || 'Failed to load sessions'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Fetch messages for a specific session
   */
  async function fetchMessages(sessionId) {
    if (!sessionId) return []

    isLoading.value = true
    error.value = null

    try {
      const response = await listMessages(sessionId)

      if (response.code === 0) {
        // Ensure messages array exists
        if (!messages.value[sessionId]) {
          messages.value[sessionId] = []
        }

        // Set messages
        messages.value[sessionId] = response.data || []
        return response.data
      } else {
        throw new Error(response.message || 'Failed to load messages')
      }
    } catch (err) {
      error.value = err.message || 'Failed to load messages'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Set the current active session
   */
  async function setCurrentSession(sessionId, options = {}) {
    // End streaming state
    isStreaming.value = false
    isNewChat.value = false

    // If switching to new chat mode (sessionId is null)
    if (sessionId === null) {
      streamingContent.value = ''
      isNewChat.value = true

      // Clear temporary messages (unless specified to keep)
      if (!options.keepMessages && messages.value['temp-session']) {
        messages.value['temp-session'] = []
      }
    }

    // Set current session ID
    currentSessionId.value = sessionId

    // If valid session ID, load messages
    if (sessionId) {
      await fetchMessages(sessionId)
    }
  }

  /**
   * Start a new chat
   */
  function startNewChat() {
    isNewChat.value = true
    currentSessionId.value = null
    streamingContent.value = ''
    messages.value['temp-session'] = []
  }

  /**
   * Send a message in the current context
   */
  async function sendMessage(content, options = {}) {
    if (!content.trim()) return

    const { isNewChat: isNewChatOption = isNewChat.value } = options
    const sessionId = isNewChatOption ? null : currentSessionId.value

    // Add user message to UI immediately
    const userMessage = {
      id: `temp-${Date.now()}`,
      content,
      role: 'user',
      createTime: new Date().toISOString()
    }

    const targetSessionId = sessionId || 'temp-session'

    if (!messages.value[targetSessionId]) {
      messages.value[targetSessionId] = []
    }

    messages.value[targetSessionId].push(userMessage)

    // Start streaming
    isStreaming.value = true
    streamingContent.value = ''

    try {
      console.debug('开始流式请求，会话ID:', sessionId, '是否新会话:', isNewChatOption)

      await streamChat(
        content,
        { sessionId, first: isNewChatOption },
        {
          onMessage: (chunk) => {
            // 确保 chunk 是字符串
            const chunkStr = String(chunk || '')
            console.debug('收到流式内容块，长度:', chunkStr.length)

            // 累加到流式内容中
            streamingContent.value += chunkStr
          },
          onDone: async () => {
            console.debug('流式响应完成，总长度:', streamingContent.value.length)

            // 创建AI消息
            const aiMessage = {
              id: `temp-ai-${Date.now()}`,
              content: streamingContent.value,
              role: 'assistant',
              createTime: new Date().toISOString()
            }

            if (isNewChatOption) {
              // 添加到临时会话
              messages.value['temp-session'].push(aiMessage)
              console.debug('消息已添加到临时会话')

              // 刷新会话列表以获取新会话
              await fetchSessions()

              // 将当前会话设置为第一个（最新的）
              if (sessions.value.length > 0) {
                const newSessionId = sessions.value[0].id
                currentSessionId.value = newSessionId
                isNewChat.value = false
                console.debug('新会话已创建，ID:', newSessionId)

                // 将消息从临时会话移动到新会话
                messages.value[newSessionId] = [...messages.value['temp-session']]
                messages.value['temp-session'] = []
              }
            } else {
              // 添加到当前会话
              messages.value[sessionId].push(aiMessage)
              console.debug('消息已添加到现有会话, ID:', sessionId)
            }

            // 结束流式状态
            isStreaming.value = false
          },
          onError: (err) => {
            console.error('流式请求错误:', err)
            error.value = err.message || '流式请求失败'
            isStreaming.value = false
          }
        }
      )
    } catch (err) {
      error.value = err.message || 'Failed to send message'
      isStreaming.value = false
      throw err
    }
  }

  /**
   * Delete a chat session
   */
  async function deleteSession(sessionId) {
    isLoading.value = true
    error.value = null

    try {
      const response = await apiDeleteSession(sessionId)

      if (response.code === 0) {
        // Remove session from state
        sessions.value = sessions.value.filter(s => s.id !== sessionId)

        // Clear messages for this session
        delete messages.value[sessionId]

        // If current session was deleted, set to null
        if (currentSessionId.value === sessionId) {
          startNewChat()
        }

        return true
      } else {
        throw new Error(response.message || 'Failed to delete session')
      }
    } catch (err) {
      error.value = err.message || 'Failed to delete session'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Delete a message
   */
  async function deleteMessage(messageId, sessionId = currentSessionId.value) {
    isLoading.value = true
    error.value = null

    try {
      const response = await apiDeleteMessage(messageId)

      if (response.code === 0) {
        // Remove message from session
        if (messages.value[sessionId]) {
          messages.value[sessionId] = messages.value[sessionId].filter(m => m.id !== messageId)
        }

        return true
      } else {
        throw new Error(response.message || 'Failed to delete message')
      }
    } catch (err) {
      error.value = err.message || 'Failed to delete message'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Rename a session
   */
  async function renameSession(sessionId, newName) {
    // This would need a backend API endpoint to be implemented
    // For now, just update the local state
    const session = sessions.value.find(s => s.id === sessionId)
    if (session) {
      session.sessionName = newName
    }
    return true
  }

  return {
    // State
    sessions,
    currentSessionId,
    messages,
    isStreaming,
    streamingContent,
    isLoading,
    error,
    isNewChat,

    // Computed
    sortedSessions,
    currentMessages,
    currentSession,

    // Actions
    fetchSessions,
    fetchMessages,
    setCurrentSession,
    startNewChat,
    sendMessage,
    deleteSession,
    deleteMessage,
    renameSession
  }
})
