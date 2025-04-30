import axios from 'axios'
import { apiClient } from './apiClient'

const API_URL = '/api'

/**
 * 上传图片
 * @param {File} file - 图片文件
 * @param {Object} metadata - 图片元数据
 * @param {string} metadata.name - 图片名称（可选）
 * @param {string} metadata.category - 图片分类（可选）
 * @param {string} metadata.tags - 图片标签，逗号分隔（可选）
 * @param {string} metadata.introduction - 图片简介（可选）
 * @returns {Promise<Object>} 上传结果
 */
export const uploadPicture = async (file, metadata = {}) => {
  try {
    const formData = new FormData()
    formData.append('file', file)

    // 添加元数据
    Object.entries(metadata).forEach(([key, value]) => {
      if (value) formData.append(key, value)
    })

    const response = await apiClient.post('/picture/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    return response.data
  } catch (error) {
    console.error('上传图片失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 获取图片详情
 * @param {string|number} id - 图片ID
 * @returns {Promise<Object>} 图片详情
 */
export const getPictureById = async (id) => {
  try {
    const response = await apiClient.get(`/picture/${id}`)
    return response.data
  } catch (error) {
    console.error('获取图片详情失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 分页获取图片列表
 * @param {Object} params - 查询参数
 * @param {number} params.current - 当前页码
 * @param {number} params.pageSize - 每页大小
 * @param {string} params.name - 图片名称（可选，支持模糊查询）
 * @param {string} params.category - 图片分类（可选）
 * @param {string} params.tags - 图片标签（可选，支持模糊查询）
 * @param {number} params.userId - 用户ID（可选）
 * @returns {Promise<Object>} 分页图片列表
 */
export const listPictureByPage = async (params) => {
  try {
    const response = await apiClient.post('/picture/list/page', params)
    return response.data
  } catch (error) {
    console.error('获取图片列表失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 更新图片信息
 * @param {Object} data - 更新数据
 * @param {number} data.id - 图片ID
 * @param {string} data.name - 图片名称（可选）
 * @param {string} data.introduction - 图片简介（可选）
 * @param {string} data.category - 图片分类（可选）
 * @param {string} data.tags - 图片标签（可选）
 * @returns {Promise<Object>} 更新结果
 */
export const updatePicture = async (data) => {
  try {
    const response = await apiClient.post('/picture/update', data)
    return response.data
  } catch (error) {
    console.error('更新图片信息失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 删除图片
 * @param {number} id - 图片ID
 * @returns {Promise<Object>} 删除结果
 */
export const deletePicture = async (id) => {
  try {
    const response = await apiClient.post('/picture/delete', { id })
    return response.data
  } catch (error) {
    console.error('删除图片失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 获取图片分类列表
 * @returns {Promise<Object>} 分类列表
 */
export const getPictureCategories = async () => {
  try {
    const response = await apiClient.get('/picture/categories')
    return response.data
  } catch (error) {
    console.error('获取图片分类列表失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 获取图片标签列表
 * @returns {Promise<Object>} 标签列表
 */
export const getPictureTags = async () => {
  try {
    const response = await apiClient.get('/picture/tags')
    return response.data
  } catch (error) {
    console.error('获取图片标签列表失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 批量获取图片信息
 * @param {Array<number>} ids - 图片ID列表
 * @returns {Promise<Object>} 图片信息列表
 */
export const batchGetPictureByIds = async (ids) => {
  try {
    const response = await apiClient.post('/picture/batch', ids)
    return response.data
  } catch (error) {
    console.error('批量获取图片信息失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}
