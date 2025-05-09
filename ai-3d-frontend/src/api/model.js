import { apiClient } from './apiClient'

/**
 * 获取模型详情
 * @param {number} id 模型ID
 * @returns {Promise} 包含模型详情的响应
 */
export const getModelById = async (id) => {
  try {
    const response = await apiClient.get(`/model/${id}`)
    return response.data
  } catch (error) {
    console.error('获取模型详情失败:', error)
    throw error.response?.data || { message: error.message || '网络错误，请稍后重试' }
  }
}

/**
 * 分页获取模型列表
 * @param {Object} params 查询参数
 * @param {number} params.current 当前页码
 * @param {number} params.pageSize 每页大小
 * @param {string} params.name 模型名称（可选）
 * @param {string} params.category 模型分类（可选）
 * @param {string} params.tags 模型标签（可选）
 * @param {number} params.userId 用户ID（可选）
 * @param {string} params.status 模型状态（可选）
 * @returns {Promise} 包含分页模型列表的响应
 */
export const listModelsByPage = async (params) => {
  try {
    const response = await apiClient.post('/model/list/page', params)
    return response.data
  } catch (error) {
    console.error('获取模型列表失败:', error)
    throw error.response?.data || { message: error.message || '网络错误，请稍后重试' }
  }
}

/**
 * 更新模型信息
 * @param {Object} data 更新数据
 * @param {number} data.id 模型ID
 * @param {string} data.name 模型名称（可选）
 * @param {string} data.introduction 模型简介（可选）
 * @param {string} data.category 模型分类（可选）
 * @param {string} data.tags 模型标签（可选）
 * @returns {Promise} 包含更新结果的响应
 */
export const updateModel = async (data) => {
  try {
    const response = await apiClient.post('/model/update', data)
    return response.data
  } catch (error) {
    console.error('更新模型失败:', error)
    throw error.response?.data || { message: error.message || '网络错误，请稍后重试' }
  }
}

/**
 * 删除模型
 * @param {number} id 模型ID
 * @returns {Promise} 包含删除结果的响应
 */
export const deleteModel = async (id) => {
  try {
    const response = await apiClient.post('/model/delete', { id })
    return response.data
  } catch (error) {
    console.error('删除模型失败:', error)
    throw error.response?.data || { message: error.message || '网络错误，请稍后重试' }
  }
}

/**
 * 获取模型分类列表
 * @returns {Promise} 包含分类列表的响应
 */
export const getModelCategories = async () => {
  try {
    const response = await apiClient.get('/model/categories')
    return response.data
  } catch (error) {
    console.error('获取模型分类列表失败:', error)
    throw error.response?.data || { message: error.message || '网络错误，请稍后重试' }
  }
}

/**
 * 批量获取模型信息
 * @param {Array<number>} ids 模型ID列表
 * @returns {Promise} 包含模型信息列表的响应
 */
export const batchGetModelsByIds = async (ids) => {
  try {
    const response = await apiClient.post('/model/batch', ids)
    return response.data
  } catch (error) {
    console.error('批量获取模型信息失败:', error)
    throw error.response?.data || { message: error.message || '网络错误，请稍后重试' }
  }
}

/**
 * 根据图片ID获取关联的模型
 * @param {string|number} imageId - 图片ID
 * @returns {Promise} - 包含模型信息的响应
 */
export const getModelByImageId = async (imageId) => {
  try {
    // 确保ID是字符串类型
    const idStr = String(imageId)
    const response = await apiClient.get(`/model/image/${idStr}`)
    return response.data  // 返回原始响应数据
  } catch (error) {
    console.error('获取图片关联模型失败:', error)
    throw error.response?.data || { message: error.message || '网络错误，请稍后重试' }
  }
}

export default {
  getModelById,
  listModelsByPage,
  updateModel,
  deleteModel,
  getModelCategories,
  batchGetModelsByIds,
  getModelByImageId
}
