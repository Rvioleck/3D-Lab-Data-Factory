import axios from 'axios'
import { apiClient } from './apiClient'

// API URL已经在apiClient中设置为'/api'，这里不需要重复定义
const API_URL = ''

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

// 获取会话消息列表
export const listMessages = async (sessionId) => {
  try {
    console.log('发送获取消息列表请求, sessionId:', sessionId)
    const response = await apiClient.get(`/chat/message/${sessionId}`)
    console.log('获取消息列表响应:', response.data)
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

// 删除消息
export const deleteMessage = async (messageId) => {
  try {
    const response = await apiClient.delete(`/chat/message/${messageId}`)
    return response.data
  } catch (error) {
    console.error('删除消息失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 发送消息
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

// 流式发送消息
export const streamChat = async (content, options = {}, callbacks = {}) => {
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

    const url = `/api/chat/stream`

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
      const errorData = await response.json()
      throw new Error(errorData.message || '请求失败')
    }

    // 获取响应流
    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')

    // 处理流式响应
    while (true) {
      const { done, value } = await reader.read()

      if (done) {
        callbacks.onDone && callbacks.onDone()
        break
      }

      // 解码二进制数据
      const chunk = decoder.decode(value, { stream: true })
      console.log('收到原始数据块:', chunk)

      // 处理SSE格式的数据
      if (chunk) {
        try {
          // 将数据块按行分割
          const lines = chunk.split('\n')

          for (const line of lines) {
            // 处理非空行
            if (line.trim() !== '') {
              console.log('处理行:', line)

              // 检查是否是SSE数据行
              if (line.startsWith('data:')) {
                // 提取data:后面的内容，允许data:后面有空格
                let content = line.substring(5).trim()

                // 检查是否是JSON格式
                try {
                  const jsonData = JSON.parse(content)
                  if (jsonData.content) {
                    content = jsonData.content
                  }
                } catch (e) {
                  // 不是JSON格式，直接使用原始内容
                  console.log('非JSON格式内容:', content)
                }

                // 检查是否是结束标记
                if (content === '[DONE]') {
                  console.log('收到流式响应结束标记')
                  // 不将结束标记发送给回调
                } else {
                  console.log('发送内容给回调:', content)
                  // 将内容发送给回调
                  callbacks.onMessage && callbacks.onMessage(content)
                }
              } else {
                // 非SSE格式，可能是直接的文本内容
                console.log('非SSE格式数据:', line)
                callbacks.onMessage && callbacks.onMessage(line)
              }
            }
          }
        } catch (parseError) {
          console.error('解析流式数据错误:', parseError)
          // 如果解析出错，尝试直接发送原始数据
          callbacks.onMessage && callbacks.onMessage(chunk)
        }
      }
    }
  } catch (error) {
    console.error('流式聊天请求失败:', error)
    callbacks.onError && callbacks.onError(error)
    throw error
  }
}
