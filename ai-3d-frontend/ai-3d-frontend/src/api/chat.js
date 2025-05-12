import { apiClient } from './apiClient'

/**
 * 聊天API服务
 * 提供与后端聊天相关API的交互功能
 */

/**
 * 创建新的对话会话
 * @param {string} sessionName - 会话名称
 * @returns {Promise<Object>} - 创建的会话对象
 */
export const createSession = async (sessionName) => {
  try {
    const response = await apiClient.post(`/chat/session?sessionName=${encodeURIComponent(sessionName)}`)
    return response.data
  } catch (error) {
    console.error('创建会话失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 获取会话列表
 * @returns {Promise<Object>} - 会话列表
 */
export const listSessions = async () => {
  try {
    const response = await apiClient.get('/chat/session/list')
    return response.data
  } catch (error) {
    console.error('获取会话列表失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 获取指定会话的消息列表
 * @param {string} sessionId - 会话ID
 * @returns {Promise<Object>} - 消息列表
 */
export const listMessages = async (sessionId) => {
  try {
    const response = await apiClient.get(`/chat/message/${sessionId}`)
    return response.data
  } catch (error) {
    console.error('获取消息列表失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 删除指定的会话
 * @param {string} sessionId - 要删除的会话ID
 * @returns {Promise<Object>} - 删除结果
 */
export const deleteSession = async (sessionId) => {
  try {
    const response = await apiClient.delete(`/chat/session/${sessionId}`)
    return response.data
  } catch (error) {
    console.error('删除会话失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 删除指定的消息
 * @param {string} messageId - 要删除的消息ID
 * @returns {Promise<Object>} - 删除结果
 */
export const deleteMessage = async (messageId) => {
  try {
    const response = await apiClient.delete(`/chat/message/${messageId}`)
    return response.data
  } catch (error) {
    console.error('删除消息失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 编辑消息内容
 * @param {string} messageId - 要编辑的消息ID
 * @param {string} content - 新的消息内容
 * @returns {Promise<Object>} - 编辑结果
 */
export const editMessage = async (messageId, content) => {
  try {
    const response = await apiClient.put(`/chat/message/${messageId}`, { content })
    return response.data
  } catch (error) {
    console.error('编辑消息失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 发送消息并获取AI回复
 * @param {string} content - 消息内容
 * @param {Object} options - 选项
 * @param {string} [options.sessionId] - 会话ID，如果不提供则自动创建新会话
 * @param {boolean} [options.first=false] - 是否是首次消息
 * @returns {Promise<Object>} - AI回复
 */
export const sendMessage = async (content, options = {}) => {
  try {
    const { sessionId, first = false } = options

    // 准备请求体
    const requestBody = {
      message: content,
      first: first
    }

    // 如果有sessionId则添加
    if (sessionId) {
      requestBody.sessionId = sessionId
    }

    const response = await apiClient.post('/chat/message', requestBody)
    return response.data
  } catch (error) {
    console.error('发送消息失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 重命名会话
 * @param {string} sessionId - 会话ID
 * @param {string} newName - 新的会话名称
 * @returns {Promise<Object>} - 重命名结果
 */
export const renameSession = async (sessionId, newName) => {
  try {
    const response = await apiClient.put(`/chat/session/${sessionId}`, { sessionName: newName })
    return response.data
  } catch (error) {
    console.error('重命名会话失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 流式发送消息并获取AI实时回复
 * @param {string} content - 消息内容
 * @param {Object} options - 选项
 * @param {string} [options.sessionId] - 会话ID，如果不提供则自动创建新会话
 * @param {boolean} [options.first=false] - 是否是首次消息
 * @param {Object} callbacks - 回调函数
 * @param {Function} callbacks.onMessage - 收到消息块时的回调
 * @param {Function} callbacks.onDone - 流式响应完成时的回调
 * @param {Function} callbacks.onError - 发生错误时的回调
 */
export const streamChat = async (content, options = {}, callbacks = {}) => {
  try {
    const { sessionId, first = false } = options
    const { onMessage, onDone, onError } = callbacks

    // 准备请求体
    const requestBody = {
      message: content,
      first: first
    }

    // 如果有sessionId则添加
    if (sessionId) {
      requestBody.sessionId = sessionId
    }

    // 使用fetch API进行流式请求
    const response = await fetch('/api/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify(requestBody)
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    // 获取响应的可读流
    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    // 读取流
    while (true) {
      const { done, value } = await reader.read()
      
      if (done) {
        console.debug('流读取完成')
        break
      }

      // 解码二进制数据为文本
      const chunk = decoder.decode(value, { stream: true })
      buffer += chunk

      // 处理接收到的数据块
      const lines = buffer.split('\n\n')
      buffer = lines.pop() || '' // 保留最后一个可能不完整的块

      for (const line of lines) {
        if (line.trim() === '') continue

        // 解析SSE格式的数据
        const dataMatch = line.match(/^data: (.+)$/m)
        if (!dataMatch) continue

        const data = dataMatch[1].trim()
        
        // 检查是否是结束标记
        if (data === '[DONE]') {
          if (onDone) onDone()
          return
        }

        // 处理消息内容
        if (onMessage) onMessage(data)
      }
    }

    // 处理可能剩余的数据
    if (buffer.trim() !== '') {
      const dataMatch = buffer.match(/^data: (.+)$/m)
      if (dataMatch) {
        const data = dataMatch[1].trim()
        if (data !== '[DONE]' && onMessage) {
          onMessage(data)
        }
      }
    }

    // 完成回调
    if (onDone) onDone()
  } catch (error) {
    console.error('流式请求失败:', error)
    if (callbacks.onError) {
      callbacks.onError(error)
    }
    throw error
  }
}
