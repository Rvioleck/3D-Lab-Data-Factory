/**
 * 模型服务
 * 提供模型相关的数据访问和权限控制
 */

import { useUserStore } from '@/stores/user'
import apiClient from '@/utils/apiClient'

export const useModelService = () => {
  const userStore = useUserStore()
  
  /**
   * 获取模型列表，根据用户权限过滤
   * @param {Object} params - 查询参数
   * @returns {Promise} - API响应
   */
  const getModels = async (params = {}) => {
    const isLoggedIn = userStore.isLoggedIn
    const isAdmin = userStore.isAdmin
    const userId = userStore.user?.id
    
    // 未登录用户只能查看公开资源
    if (!isLoggedIn) {
      params.isPublic = true
    } 
    // 非管理员只能查看自己的或公开的资源
    else if (!isAdmin && !params.all) {
      params.userId = userId
      params.includePublic = true
    }
    // 管理员可以查看所有资源
    
    return await apiClient.get('/api/models', { params })
  }
  
  /**
   * 获取单个模型详情
   * @param {string|number} modelId - 模型ID
   * @returns {Promise} - API响应
   */
  const getModelById = async (modelId) => {
    const isLoggedIn = userStore.isLoggedIn
    const isAdmin = userStore.isAdmin
    const userId = userStore.user?.id
    
    const response = await apiClient.get(`/api/models/${modelId}`)
    const model = response.data?.data
    
    // 检查权限
    if (!model) {
      throw new Error('模型不存在')
    }
    
    // 如果模型不是公开的，且用户未登录，拒绝访问
    if (!model.isPublic && !isLoggedIn) {
      throw new Error('此模型不是公开的，请登录后查看')
    }
    
    // 如果模型不是公开的，且用户不是所有者或管理员，拒绝访问
    if (!model.isPublic && !isAdmin && model.userId !== userId) {
      throw new Error('您没有权限查看此模型')
    }
    
    return response
  }
  
  /**
   * 创建新模型
   * @param {Object} data - 模型数据
   * @returns {Promise} - API响应
   */
  const createModel = async (data) => {
    const isLoggedIn = userStore.isLoggedIn
    
    // 检查权限
    if (!isLoggedIn) {
      throw new Error('请先登录')
    }
    
    return await apiClient.post('/api/models', data)
  }
  
  /**
   * 更新模型，需要权限检查
   * @param {string|number} modelId - 模型ID
   * @param {Object} data - 更新数据
   * @returns {Promise} - API响应
   */
  const updateModel = async (modelId, data) => {
    const isLoggedIn = userStore.isLoggedIn
    const isAdmin = userStore.isAdmin
    const userId = userStore.user?.id
    
    // 首先获取模型信息以检查所有权
    const modelResponse = await apiClient.get(`/api/models/${modelId}`)
    const model = modelResponse.data?.data
    
    if (!model) {
      throw new Error('模型不存在')
    }
    
    // 检查权限
    if (!isLoggedIn) {
      throw new Error('请先登录')
    }
    
    if (!isAdmin && model.userId !== userId) {
      throw new Error('您没有权限编辑此模型')
    }
    
    return await apiClient.put(`/api/models/${modelId}`, data)
  }
  
  /**
   * 删除模型，需要权限检查
   * @param {string|number} modelId - 模型ID
   * @returns {Promise} - API响应
   */
  const deleteModel = async (modelId) => {
    const isLoggedIn = userStore.isLoggedIn
    const isAdmin = userStore.isAdmin
    const userId = userStore.user?.id
    
    // 首先获取模型信息以检查所有权
    const modelResponse = await apiClient.get(`/api/models/${modelId}`)
    const model = modelResponse.data?.data
    
    if (!model) {
      throw new Error('模型不存在')
    }
    
    // 检查权限
    if (!isLoggedIn) {
      throw new Error('请先登录')
    }
    
    if (!isAdmin && model.userId !== userId) {
      throw new Error('您没有权限删除此模型')
    }
    
    return await apiClient.delete(`/api/models/${modelId}`)
  }
  
  /**
   * 设置模型公开/私有状态
   * @param {string|number} modelId - 模型ID
   * @param {boolean} isPublic - 是否公开
   * @returns {Promise} - API响应
   */
  const setModelVisibility = async (modelId, isPublic) => {
    return await updateModel(modelId, { isPublic })
  }
  
  /**
   * 下载模型
   * @param {string|number} modelId - 模型ID
   * @returns {Promise} - API响应
   */
  const downloadModel = async (modelId) => {
    const isLoggedIn = userStore.isLoggedIn
    
    // 首先获取模型信息以检查可访问性
    const modelResponse = await apiClient.get(`/api/models/${modelId}`)
    const model = modelResponse.data?.data
    
    if (!model) {
      throw new Error('模型不存在')
    }
    
    // 如果模型不是公开的，且用户未登录，拒绝访问
    if (!model.isPublic && !isLoggedIn) {
      throw new Error('此模型不是公开的，请登录后下载')
    }
    
    // 下载模型
    return await apiClient.get(`/api/models/${modelId}/download`, { 
      responseType: 'blob' 
    })
  }
  
  return {
    getModels,
    getModelById,
    createModel,
    updateModel,
    deleteModel,
    setModelVisibility,
    downloadModel
  }
}
