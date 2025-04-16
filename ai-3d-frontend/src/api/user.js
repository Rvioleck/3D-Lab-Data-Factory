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

// 用户登录
export const login = async (credentials) => {
  try {
    const response = await apiClient.post('/user/login', credentials)
    return response.data
  } catch (error) {
    console.error('登录失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 用户注册
export const register = async (userData) => {
  try {
    const response = await apiClient.post('/user/register', userData)
    return response.data
  } catch (error) {
    console.error('注册失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 获取当前登录用户信息
export const getLoginUser = async () => {
  try {
    const response = await apiClient.post('/user/get/login')
    return response.data
  } catch (error) {
    console.error('获取用户信息失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}
