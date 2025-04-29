/**
 * 图片服务
 * 提供图片相关的数据访问和权限控制
 */

import { useUserStore } from '@/stores/user'
import apiClient from '@/utils/apiClient'

export const usePictureService = () => {
  const userStore = useUserStore()
  
  /**
   * 获取图片列表，根据用户权限过滤
   * @param {Object} params - 查询参数
   * @returns {Promise} - API响应
   */
  const getPictures = async (params = {}) => {
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
    
    return await apiClient.get('/api/pictures', { params })
  }
  
  /**
   * 获取单个图片详情
   * @param {string|number} pictureId - 图片ID
   * @returns {Promise} - API响应
   */
  const getPictureById = async (pictureId) => {
    const isLoggedIn = userStore.isLoggedIn
    const isAdmin = userStore.isAdmin
    const userId = userStore.user?.id
    
    const response = await apiClient.get(`/api/pictures/${pictureId}`)
    const picture = response.data?.data
    
    // 检查权限
    if (!picture) {
      throw new Error('图片不存在')
    }
    
    // 如果图片不是公开的，且用户未登录，拒绝访问
    if (!picture.isPublic && !isLoggedIn) {
      throw new Error('此图片不是公开的，请登录后查看')
    }
    
    // 如果图片不是公开的，且用户不是所有者或管理员，拒绝访问
    if (!picture.isPublic && !isAdmin && picture.userId !== userId) {
      throw new Error('您没有权限查看此图片')
    }
    
    return response
  }
  
  /**
   * 上传新图片
   * @param {FormData} formData - 包含图片的表单数据
   * @returns {Promise} - API响应
   */
  const uploadPicture = async (formData) => {
    const isLoggedIn = userStore.isLoggedIn
    
    // 检查权限
    if (!isLoggedIn) {
      throw new Error('请先登录')
    }
    
    return await apiClient.post('/api/pictures', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
  
  /**
   * 更新图片信息，需要权限检查
   * @param {string|number} pictureId - 图片ID
   * @param {Object} data - 更新数据
   * @returns {Promise} - API响应
   */
  const updatePicture = async (pictureId, data) => {
    const isLoggedIn = userStore.isLoggedIn
    const isAdmin = userStore.isAdmin
    const userId = userStore.user?.id
    
    // 首先获取图片信息以检查所有权
    const pictureResponse = await apiClient.get(`/api/pictures/${pictureId}`)
    const picture = pictureResponse.data?.data
    
    if (!picture) {
      throw new Error('图片不存在')
    }
    
    // 检查权限
    if (!isLoggedIn) {
      throw new Error('请先登录')
    }
    
    if (!isAdmin && picture.userId !== userId) {
      throw new Error('您没有权限编辑此图片')
    }
    
    return await apiClient.put(`/api/pictures/${pictureId}`, data)
  }
  
  /**
   * 删除图片，需要权限检查
   * @param {string|number} pictureId - 图片ID
   * @returns {Promise} - API响应
   */
  const deletePicture = async (pictureId) => {
    const isLoggedIn = userStore.isLoggedIn
    const isAdmin = userStore.isAdmin
    const userId = userStore.user?.id
    
    // 首先获取图片信息以检查所有权
    const pictureResponse = await apiClient.get(`/api/pictures/${pictureId}`)
    const picture = pictureResponse.data?.data
    
    if (!picture) {
      throw new Error('图片不存在')
    }
    
    // 检查权限
    if (!isLoggedIn) {
      throw new Error('请先登录')
    }
    
    if (!isAdmin && picture.userId !== userId) {
      throw new Error('您没有权限删除此图片')
    }
    
    return await apiClient.delete(`/api/pictures/${pictureId}`)
  }
  
  /**
   * 设置图片公开/私有状态
   * @param {string|number} pictureId - 图片ID
   * @param {boolean} isPublic - 是否公开
   * @returns {Promise} - API响应
   */
  const setPictureVisibility = async (pictureId, isPublic) => {
    return await updatePicture(pictureId, { isPublic })
  }
  
  /**
   * 下载图片
   * @param {string|number} pictureId - 图片ID
   * @returns {Promise} - API响应
   */
  const downloadPicture = async (pictureId) => {
    const isLoggedIn = userStore.isLoggedIn
    
    // 首先获取图片信息以检查可访问性
    const pictureResponse = await apiClient.get(`/api/pictures/${pictureId}`)
    const picture = pictureResponse.data?.data
    
    if (!picture) {
      throw new Error('图片不存在')
    }
    
    // 如果图片不是公开的，且用户未登录，拒绝访问
    if (!picture.isPublic && !isLoggedIn) {
      throw new Error('此图片不是公开的，请登录后下载')
    }
    
    // 下载图片
    return await apiClient.get(`/api/pictures/${pictureId}/download`, { 
      responseType: 'blob' 
    })
  }
  
  return {
    getPictures,
    getPictureById,
    uploadPicture,
    updatePicture,
    deletePicture,
    setPictureVisibility,
    downloadPicture
  }
}
