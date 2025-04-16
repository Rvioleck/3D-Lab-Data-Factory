import axios from 'axios'

const API_URL = '/api'

// 创建axios实例
const apiClient = axios.create({
  baseURL: API_URL,
  withCredentials: true, // 允许跨域请求携带cookie
  headers: {
    'Content-Type': 'application/json'
  }
})

// 创建会话
export const createSession = async (sessionName) => {
  try {
    const response = await apiClient.post(`/chat/session?sessionName=${encodeURIComponent(sessionName)}`)
    return response.data
  } catch (error) {
    console.error('创建会话失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 获取会话列表
export const listSessions = async () => {
  try {
    const response = await apiClient.get('/chat/session/list')
    return response.data
  } catch (error) {
    console.error('获取会话列表失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 获取会话消息
export const listMessages = async (sessionId) => {
  try {
    const response = await apiClient.get(`/chat/message/${sessionId}`)
    return response.data
  } catch (error) {
    console.error('获取会话消息失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 发送消息（支持自动创建会话）
export const sendMessage = async (message, options = {}) => {
  try {
    const { sessionId, first = false } = options
    const response = await apiClient.post('/chat/message', {
      sessionId,
      message,
      first
    })
    return response.data
  } catch (error) {
    console.error('发送消息失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 兼容旧版本的自动创建会话并发送消息
export const sendMessageWithAutoSession = async (message) => {
  return sendMessage(message, { first: true })
}

// 流式发送消息（支持自动创建会话）
export const streamChat = async (message, options = {}, callbacks) => {
  try {
    const { sessionId, first = false } = options
    const { onMessage, onDone, onError } = callbacks

    // 使用AbortController来处理请求取消
    const controller = new AbortController()
    const signal = controller.signal

    // 设置超时处理
    const timeoutId = setTimeout(() => {
      controller.abort()
      onError && onError(new Error('请求超时'))
    }, 30000) // 30秒超时

    const response = await fetch(`${API_URL}/chat/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        sessionId,
        message,
        first
      }),
      credentials: 'include', // 允许跨域请求携带cookie
      signal // 添加信号以支持取消
    })

    // 清除超时定时器
    clearTimeout(timeoutId)

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    // 使用更高效的数据处理方式
    let buffer = ''

    const processText = (text) => {
      buffer += text
      const lines = buffer.split('\n')

      // 保留最后一行（可能不完整）
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (!line.trim()) continue

        // 处理结束标记
        if (line === '[DONE]' || line === 'data: [DONE]') {
          return true // 表示完成
        }

        // 处理SSE格式的数据
        if (line.startsWith('data:')) {
          // 提取data:后的内容，允许空格
          const data = line.substring(5).trim()

          if (data && data !== '[DONE]') {
            onMessage && onMessage(data)
          }
        } else {
          // 直接处理纯文本
          onMessage && onMessage(line)
        }
      }
      return false // 表示未完成
    }

    try {
      while (true) {
        const { value, done } = await reader.read()
        if (done) break

        const chunk = decoder.decode(value, { stream: true })
        const isComplete = processText(chunk)
        if (isComplete) break
      }

      // 处理缓冲区中的最后内容
      if (buffer.trim()) {
        processText(buffer + '\n')
      }

      onDone && onDone()
    } catch (readError) {
      console.error('读取流失败:', readError)
      onError && onError(readError)
    } finally {
      reader.releaseLock()
    }
  } catch (error) {
    console.error('流式发送消息失败:', error)
    onError && onError(error)
    throw error
  }
}

// 兼容旧版本的自动创建会话并流式发送消息
export const streamChatWithAutoSession = async (message, callbacks) => {
  return streamChat(message, { first: true }, callbacks)
}

// 删除会话
export const deleteSession = async (sessionId) => {
  try {
    const response = await apiClient.delete(`/chat/session/${sessionId}`)
    return response.data
  } catch (error) {
    console.error('删除会话失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 删除消息对（用户消息和对应的AI回复）
export const deleteMessage = async (messageId) => {
  try {
    const response = await apiClient.delete(`/chat/message/${messageId}`)
    return response.data
  } catch (error) {
    console.error('删除消息失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}
