import { apiClient } from './apiClient'

// 简单健康检查
export const checkHealth = async () => {
  try {
    const response = await apiClient.get('/health')
    return response.data
  } catch (error) {
    console.error('健康检查失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 详细健康检查
export const checkHealthDetail = async () => {
  try {
    const response = await apiClient.get('/health/detail')
    return response.data
  } catch (error) {
    console.error('详细健康检查失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}
