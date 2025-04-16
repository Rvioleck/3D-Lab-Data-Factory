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

// 发送消息
export const sendMessage = async (sessionId, message) => {
  try {
    const response = await apiClient.post('/chat/message', {
      sessionId,
      message
    })
    return response.data
  } catch (error) {
    console.error('发送消息失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 流式发送消息
export const streamChat = async (sessionId, message, callbacks) => {
  try {
    const { onMessage, onDone, onError } = callbacks

    const response = await fetch(`${API_URL}/chat/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        sessionId,
        message
      }),
      credentials: 'include' // 允许跨域请求携带cookie
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    while (true) {
      const { value, done } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value)
      console.log('Received chunk:', chunk) // 调试日志

      // 处理数据块
      const lines = chunk.split('\n')

      for (const line of lines) {
        if (!line.trim()) continue

        // 处理结束标记
        if (line === '[DONE]' || line === 'data: [DONE]') {
          onDone && onDone()
          return
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
    }

    onDone && onDone()
  } catch (error) {
    console.error('流式发送消息失败:', error)
    onError && onError(error)
    throw error
  }
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
