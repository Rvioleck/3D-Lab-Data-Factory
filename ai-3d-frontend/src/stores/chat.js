import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  createSession,
  listSessions,
  listMessages,
  deleteSession,
  deleteMessage,
  sendMessage as apiSendMessage,
  streamChat
} from '../api/chat'

export const useChatStore = defineStore('chat', () => {
  // State
  const sessions = ref([])
  const currentSessionId = ref(null)
  const messages = ref({
    'temp-session': [] // 初始化临时会话消息数组
  }) // Map of sessionId -> messages array
  const streaming = ref(false)
  const streamingMessage = ref('')
  const loading = ref(false)
  const error = ref(null)

  // Computed
  const sortedSessions = computed(() => {
    return [...sessions.value].sort((a, b) => {
      return new Date(b.createTime) - new Date(a.createTime)
    })
  })

  const sortedMessages = computed(() => {
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
   * Create a new chat session
   * @param {string} sessionName - Session name
   * @returns {Promise} Session data
   */
  async function createSession(sessionName) {
    loading.value = true
    error.value = null

    try {
      const response = await createSession(sessionName)

      if (response.code === 0) {
        const newSession = response.data
        sessions.value.push(newSession)
        currentSessionId.value = newSession.id
        messages.value[newSession.id] = []
        return newSession
      } else {
        throw { message: response.message || 'Failed to create session' }
      }
    } catch (err) {
      error.value = err.message || 'Failed to create session'
      console.error('Create session error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Load all chat sessions
   * @returns {Promise} Sessions data
   */
  async function loadSessions() {
    loading.value = true
    error.value = null

    try {
      const response = await listSessions()

      if (response.code === 0) {
        sessions.value = response.data || []
        return response.data
      } else {
        throw { message: response.message || 'Failed to load sessions' }
      }
    } catch (err) {
      error.value = err.message || 'Failed to load sessions'
      console.error('Load sessions error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Load messages for a session
   * @param {string} sessionId - Session ID
   * @returns {Promise} Messages data
   */
  async function loadMessages(sessionId) {
    if (!sessionId) return []

    loading.value = true
    error.value = null

    try {
      const response = await listMessages(sessionId)

      if (response.code === 0) {
        messages.value[sessionId] = response.data || []
        return response.data
      } else {
        throw { message: response.message || 'Failed to load messages' }
      }
    } catch (err) {
      error.value = err.message || 'Failed to load messages'
      console.error('Load messages error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Delete a chat session
   * @param {string} sessionId - Session ID
   * @returns {Promise} Delete result
   */
  async function deleteSession(sessionId) {
    loading.value = true
    error.value = null

    try {
      const response = await deleteSession(sessionId)

      if (response.code === 0) {
        // Remove session from state
        sessions.value = sessions.value.filter(s => s.id !== sessionId)

        // Clear messages for this session
        delete messages.value[sessionId]

        // If current session was deleted, set to null
        if (currentSessionId.value === sessionId) {
          currentSessionId.value = null
        }

        return response
      } else {
        throw { message: response.message || 'Failed to delete session' }
      }
    } catch (err) {
      error.value = err.message || 'Failed to delete session'
      console.error('Delete session error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Delete a message
   * @param {string} messageId - Message ID
   * @returns {Promise} Delete result
   */
  async function deleteMessage(messageId) {
    loading.value = true
    error.value = null

    try {
      const response = await deleteMessage(messageId)

      if (response.code === 0) {
        // Find and remove message from all sessions
        Object.keys(messages.value).forEach(sessionId => {
          messages.value[sessionId] = messages.value[sessionId].filter(m => m.id !== messageId)
        })

        return response
      } else {
        throw { message: response.message || 'Failed to delete message' }
      }
    } catch (err) {
      error.value = err.message || 'Failed to delete message'
      console.error('Delete message error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Send a message
   * @param {Object} params - Message parameters
   * @param {string} params.content - Message content
   * @param {string} [params.sessionId] - Session ID
   * @param {boolean} [params.first=false] - Is first message
   * @returns {Promise} Send result
   */
  async function sendMessage(params) {
    loading.value = true
    error.value = null

    try {
      const { content, sessionId = currentSessionId.value, first = false } = params

      const response = await apiSendMessage(content, { sessionId, first })

      if (response.code === 0) {
        // If new session was created
        if (response.data.sessionId && !sessions.value.find(s => s.id === response.data.sessionId)) {
          const newSession = {
            id: response.data.sessionId,
            name: response.data.sessionName || 'New Chat',
            createTime: new Date().toISOString()
          }
          sessions.value.push(newSession)
          currentSessionId.value = newSession.id

          if (!messages.value[newSession.id]) {
            messages.value[newSession.id] = []
          }
        }

        // Add user message
        if (response.data.userMessage) {
          const sessionId = response.data.sessionId || currentSessionId.value

          if (!messages.value[sessionId]) {
            messages.value[sessionId] = []
          }

          messages.value[sessionId].push(response.data.userMessage)
        }

        // Add AI response
        if (response.data.aiMessage) {
          const sessionId = response.data.sessionId || currentSessionId.value

          if (!messages.value[sessionId]) {
            messages.value[sessionId] = []
          }

          messages.value[sessionId].push(response.data.aiMessage)
        }

        return response.data
      } else {
        throw { message: response.message || 'Failed to send message' }
      }
    } catch (err) {
      error.value = err.message || 'Failed to send message'
      console.error('Send message error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Stream a message
   * @param {Object} params - Message parameters
   * @param {string} params.content - Message content
   * @param {string} [params.sessionId] - Session ID
   * @param {boolean} [params.first=false] - Is first message
   * @param {Function} [params.onMessage] - Message callback
   * @param {Function} [params.onDone] - Done callback
   * @returns {Promise} Stream result
   */
  async function streamMessage(params) {
    const { content, sessionId = currentSessionId.value, first = false, onMessage, onDone } = params

    streaming.value = true
    error.value = null

    try {
      // Add user message immediately
      const userMessage = {
        id: `temp-${Date.now()}`,
        content,
        role: 'user',
        createTime: new Date().toISOString()
      }

      if (!messages.value[sessionId]) {
        messages.value[sessionId] = []
      }

      messages.value[sessionId].push(userMessage)

      // Stream AI response
      let aiMessageContent = ''

      await streamChat(
        content,
        { sessionId, first },
        {
          onMessage: (chunk) => {
            aiMessageContent += chunk
            if (onMessage) onMessage(chunk)
          },
          onDone: () => {
            if (onDone) onDone(aiMessageContent)
          },
          onError: (err) => {
            error.value = err.message || 'Streaming failed'
            console.error('Stream error:', err)
          }
        }
      )

      // Add complete AI message
      const aiMessage = {
        id: `temp-ai-${Date.now()}`,
        content: aiMessageContent,
        role: 'assistant',
        createTime: new Date().toISOString()
      }

      messages.value[sessionId].push(aiMessage)

      return { userMessage, aiMessage }
    } catch (err) {
      error.value = err.message || 'Streaming failed'
      console.error('Stream message error:', err)
      throw err
    } finally {
      streaming.value = false
    }
  }

  /**
   * Set current session
   * @param {string} sessionId - Session ID
   * @param {Object} options - Options
   * @param {boolean} options.keepMessages - Whether to keep temporary messages
   */
  function setCurrentSession(sessionId, options = {}) {
    currentSessionId.value = sessionId

    // Load messages if not already loaded
    if (sessionId && !messages.value[sessionId]) {
      loadMessages(sessionId)
    }
  }

  /**
   * Start streaming mode
   */
  function startStreaming() {
    streaming.value = true
    streamingMessage.value = ''
  }

  /**
   * Finish streaming mode
   * @param {Object} options - Options
   * @param {boolean} options.keepContent - Whether to keep streaming content
   */
  function finishStreaming(options = {}) {
    streaming.value = false
    if (!options.keepContent) {
      streamingMessage.value = ''
    }
  }

  /**
   * Append content to streaming message
   * @param {string} content - Content to append
   */
  function appendStreamingContent(content) {
    streamingMessage.value += content
  }

  /**
   * Add user message
   * @param {Object} params - Message parameters
   * @param {string} params.sessionId - Session ID
   * @param {string} params.content - Message content
   */
  function addUserMessage(params) {
    const { sessionId, content } = params

    if (!messages.value[sessionId]) {
      messages.value[sessionId] = []
    }

    messages.value[sessionId].push({
      id: `temp-user-${Date.now()}`,
      sessionId,
      role: 'user',
      content,
      createTime: new Date().toISOString()
    })
  }

  return {
    // State
    sessions,
    currentSessionId,
    messages,
    streaming,
    streamingMessage,
    loading,
    error,

    // Computed
    sortedSessions,
    sortedMessages,
    currentSession,

    // Actions
    createSession,
    loadSessions,
    loadMessages,
    deleteSession,
    deleteMessage,
    sendMessage,
    streamMessage,
    setCurrentSession,
    startStreaming,
    finishStreaming,
    appendStreamingContent,
    addUserMessage
  }
})
