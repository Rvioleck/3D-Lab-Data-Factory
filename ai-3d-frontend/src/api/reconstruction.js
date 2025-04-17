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
 * 上传图片进行3D重建（异步模式）
 * @param {File} file - 图片文件
 * @returns {Promise} - 包含任务ID和状态的响应
 */
export const uploadImage = async (file) => {
  try {
    const formData = new FormData()
    formData.append('file', file)

    const response = await axios.post(`${API_URL}/reconstruction/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      withCredentials: true
    })
    return response.data
  } catch (error) {
    console.error('上传图片失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 同步上传图片进行3D重建
 * @param {File} file - 图片文件
 * @returns {Promise} - 包含任务ID和文件URL的响应
 */
export const uploadImageSync = async (file) => {
  try {
    const formData = new FormData()
    formData.append('file', file)

    const response = await axios.post(`${API_URL}/reconstruction/upload/sync`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      withCredentials: true
    })
    return response.data
  } catch (error) {
    console.error('同步上传图片失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 获取任务状态
 * @param {string} taskId - 任务ID
 * @returns {Promise} - 包含任务状态的响应
 */
export const getTaskStatus = async (taskId) => {
  try {
    const response = await apiClient.get(`/reconstruction/status/${taskId}`)
    return response.data
  } catch (error) {
    console.error('获取任务状态失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 获取重建结果文件
 * @param {string} taskId - 任务ID
 * @param {string} fileName - 文件名
 * @returns {string} - 文件URL
 */
export const getResultFileUrl = (taskId, fileName) => {
  return `${API_URL}/reconstruction/files/${taskId}/${fileName}`
}

/**
 * 获取文件内容（用于图片预览）
 * @param {string} path - 文件的绝对路径
 * @returns {string} - 文件预览URL
 */
export const getFilePreviewUrl = (path) => {
  return `${API_URL}/reconstruction/preview?path=${encodeURIComponent(path)}`
}

/**
 * 检查WebSocket连接状态
 * @returns {Promise} - 包含连接状态的响应
 */
export const checkConnectionStatus = async () => {
  try {
    const response = await apiClient.get('/reconstruction/connection/status')
    return response.data
  } catch (error) {
    console.error('检查连接状态失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 获取3D模型预览所需的所有文件URL
 * @param {Object} data - 包含文件URL的数据对象
 * @returns {Object} - 包含所有必要文件URL的对象
 */
export const getModelPreviewUrls = (data) => {
  return {
    objUrl: data.objFileUrl ? getResultFileUrl(data.taskId, 'model.obj') : null,
    mtlUrl: data.mtlFileUrl ? getResultFileUrl(data.taskId, 'model.mtl') : null,
    textureUrl: data.textureImageUrl ? getResultFileUrl(data.taskId, 'pixel_images.png') : null,
    pixelImagesUrl: data.pixelImagesUrl ? getResultFileUrl(data.taskId, 'pixel_images.png') : null,
    xyzImagesUrl: data.xyzImagesUrl ? getResultFileUrl(data.taskId, 'xyz_images.png') : null
  }
}
