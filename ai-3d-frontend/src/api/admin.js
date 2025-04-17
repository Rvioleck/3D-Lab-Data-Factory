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

// 创建用户（仅管理员可访问）
export const createUser = async (userData) => {
  try {
    const response = await apiClient.post('/user/create', userData)
    return response.data
  } catch (error) {
    console.error('创建用户失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 获取用户基本信息（仅管理员可访问）
export const getUserById = async (id) => {
  try {
    const response = await apiClient.post('/user/get', id)
    return response.data
  } catch (error) {
    console.error('获取用户信息失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 获取用户详细信息（仅管理员可访问）
export const getUserDetailById = async (id) => {
  try {
    const response = await apiClient.post('/user/detail', id)
    return response.data
  } catch (error) {
    console.error('获取用户详细信息失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 更新用户信息（仅管理员可访问）
export const updateUser = async (userData) => {
  try {
    const response = await apiClient.post('/user/update', userData)
    return response.data
  } catch (error) {
    console.error('更新用户信息失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 删除用户（仅管理员可访问）
export const deleteUser = async (id) => {
  try {
    const response = await apiClient.post('/user/delete', { id })
    return response.data
  } catch (error) {
    console.error('删除用户失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 分页获取用户列表（仅管理员可访问）
export const listUsersByPage = async (queryParams) => {
  try {
    const response = await apiClient.post('/user/list/page', queryParams)
    return response.data
  } catch (error) {
    console.error('获取用户列表失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

// 获取所有用户信息（仅管理员可访问）
export const listAllUsers = async () => {
  try {
    const response = await apiClient.post('/user/list')
    return response.data
  } catch (error) {
    console.error('获取所有用户信息失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}
