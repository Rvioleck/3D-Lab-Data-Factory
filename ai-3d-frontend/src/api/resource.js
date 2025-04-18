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

/**
 * 获取资源列表
 * @param {Object} params 查询参数
 * @param {number} params.page 页码
 * @param {number} params.pageSize 每页大小
 * @param {string} params.type 资源类型 (image/model/all)
 * @param {Array<string>} params.tags 标签列表
 * @param {string} params.query 搜索关键词
 * @returns {Promise<Object>} 资源列表和分页信息
 */
export const getResources = async (params) => {
  try {
    const response = await apiClient.get('/resource/list', { params })
    return response.data
  } catch (error) {
    console.error('获取资源列表失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 获取资源详情
 * @param {string} id 资源ID
 * @returns {Promise<Object>} 资源详情
 */
export const getResourceById = async (id) => {
  try {
    const response = await apiClient.get(`/resource/${id}`)
    return response.data
  } catch (error) {
    console.error('获取资源详情失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 下载资源
 * @param {string} id 资源ID
 * @returns {Promise<Object>} 下载链接
 */
export const downloadResource = async (id) => {
  try {
    const response = await apiClient.get(`/resource/download/${id}`)
    
    // 如果返回的是下载链接，则打开新窗口下载
    if (response.data.data && response.data.data.downloadUrl) {
      window.open(response.data.data.downloadUrl, '_blank')
    }
    
    return response.data
  } catch (error) {
    console.error('下载资源失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 更新资源信息（仅管理员）
 * @param {string} id 资源ID
 * @param {Object} data 更新数据
 * @returns {Promise<Object>} 更新结果
 */
export const updateResource = async (id, data) => {
  try {
    const response = await apiClient.put(`/resource/${id}`, data)
    return response.data
  } catch (error) {
    console.error('更新资源失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 删除资源（仅管理员）
 * @param {string} id 资源ID
 * @returns {Promise<Object>} 删除结果
 */
export const deleteResource = async (id) => {
  try {
    const response = await apiClient.delete(`/resource/${id}`)
    return response.data
  } catch (error) {
    console.error('删除资源失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 获取所有可用标签
 * @returns {Promise<Object>} 标签列表
 */
export const getResourceTags = async () => {
  try {
    const response = await apiClient.get('/resource/tags')
    return response.data
  } catch (error) {
    console.error('获取标签列表失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 为资源添加标签（仅管理员）
 * @param {string} id 资源ID
 * @param {Array<string>} tags 标签列表
 * @returns {Promise<Object>} 更新结果
 */
export const addResourceTags = async (id, tags) => {
  try {
    const response = await apiClient.post(`/resource/${id}/tags`, { tags })
    return response.data
  } catch (error) {
    console.error('添加标签失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 从资源移除标签（仅管理员）
 * @param {string} id 资源ID
 * @param {Array<string>} tags 标签列表
 * @returns {Promise<Object>} 更新结果
 */
export const removeResourceTags = async (id, tags) => {
  try {
    const response = await apiClient.delete(`/resource/${id}/tags`, { data: { tags } })
    return response.data
  } catch (error) {
    console.error('移除标签失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 获取资源统计信息（仅管理员）
 * @returns {Promise<Object>} 统计信息
 */
export const getResourceStats = async () => {
  try {
    const response = await apiClient.get('/resource/stats')
    return response.data
  } catch (error) {
    console.error('获取资源统计信息失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}
