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
 * 获取用户的所有会话列表
 * @returns {Promise<Object>} - 包含会话列表的响应
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
 * 获取指定会话的消息历史
 * @param {string} sessionId - 会话ID
 * @returns {Promise<Object>} - 包含消息列表的响应
 */
export const listMessages = async (sessionId) => {
  try {
    console.log('获取会话消息, sessionId:', sessionId)
    const response = await apiClient.get(`/chat/message/${sessionId}`)
    return response.data
  } catch (error) {
    console.error('获取消息列表失败:', error)
    if (error.response) {
      console.error('响应状态:', error.response.status)
      console.error('响应数据:', error.response.data)
    } else if (error.request) {
      console.error('请求已发送但没有收到响应')
    } else {
      console.error('请求配置错误:', error.message)
    }
    throw error
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

    // 构建完整的URL
    const url = `/api/chat/stream`

    console.debug('发送流式请求:', { url, requestBody })

    // 使用fetch API进行流式请求
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestBody),
      credentials: 'include'
    })

    if (!response.ok) {
      const errorText = await response.text()
      let errorMessage = '请求失败'
      try {
        const errorData = JSON.parse(errorText)
        errorMessage = errorData.message || errorMessage
      } catch (e) {
        errorMessage = errorText || errorMessage
      }
      throw new Error(errorMessage)
    }

    console.debug('流式请求成功，开始处理响应')

    // 使用原生 EventSource 处理 SSE
    if (window.EventSource && false) { // 暂时禁用EventSource方式，使用手动解析
      // 这里的代码保留作为参考，但不使用
      // EventSource 方式需要后端支持正确的 SSE 格式
    } else {
      // 手动解析 SSE 流
      const reader = response.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''

      // 处理流式响应
      while (true) {
        const { done, value } = await reader.read()

        // 如果数据流结束
        if (done) {
          console.debug('数据流结束')
          if (onDone) onDone()
          break
        }

        // 解码收到的数据
        const chunk = decoder.decode(value, { stream: true })
        buffer += chunk

        // 处理SSE格式的数据
        // 根据日志显示，实际数据格式是每行以data:开头
        const lines = buffer.split('\n')
        buffer = lines.pop() || '' // 保留未完成的行

        for (const line of lines) {
          if (!line.trim()) continue

          // 如果是结束标记
          if (line === 'data: [DONE]' || line === 'data:[DONE]') {
            console.debug('收到结束标记 [DONE]')
            if (onDone) onDone()
            return
          }

          // 处理所有以data:开头的行，不管是否有空格
          if (line.startsWith('data:')) {
            // 提取内容（去掉'data:'前缀）
            let content = ''

            if (line.startsWith('data: ')) {
              // 如果是'data: '格式，去掉前6个字符
              content = line.substring(6)
            } else {
              // 如果是'data:'格式（没有空格），去掉前5个字符
              content = line.substring(5)
            }

            console.debug('收到流式内容:', content)

            // 如果内容不为空，发送给回调函数
            if (content && onMessage) {
              onMessage(content)
            }
          } else {
            // 非数据行，可能是其他格式
            console.debug('收到非数据行:', line)
          }
        }
      }
    }
  } catch (error) {
    console.error('流式请求错误:', error)
    if (onError) onError(error)
    throw error
  }
}
