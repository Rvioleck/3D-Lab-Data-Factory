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
 * 上传图片并创建重建任务
 * @param {File} file - 图片文件
 * @param {Object} metadata - 可选的元数据（名称、分类）
 * @returns {Promise} - 包含任务ID和SSE URL的响应
 */
export const uploadImage = async (file, metadata = {}) => {
  try {
    // 先上传图片文件
    const formData = new FormData()
    formData.append('file', file)

    // 上传图片到文件服务
    const uploadResponse = await axios.post(`${API_URL}/file/upload/image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      withCredentials: true
    })

    if (uploadResponse.data.code !== 0) {
      throw new Error(uploadResponse.data.message || '上传图片失败')
    }

    // 获取上传的图片URL
    const imageUrl = uploadResponse.data.data.url

    // 创建重建任务
    const createTaskData = {
      imageUrl: imageUrl
    }

    // 添加可选的元数据
    if (metadata.name) createTaskData.name = metadata.name
    if (metadata.category) createTaskData.category = metadata.category

    // 调用创建任务接口
    const response = await apiClient.post('/reconstruction/create', createTaskData)
    return response.data
  } catch (error) {
    console.error('上传图片失败:', error)
    throw error.response?.data || { message: error.message || '网络错误，请稍后重试' }
  }
}

/**
 * 从现有图片创建重建任务
 * @param {Object} data - 任务数据（imageId, name, category）
 * @returns {Promise} - 包含任务ID和SSE URL的响应
 */
export const createReconstructionTask = async (data) => {
  try {
    const response = await apiClient.post('/reconstruction/create-from-image', data)
    return response.data
  } catch (error) {
    console.error('创建重建任务失败:', error)
    throw error.response?.data || { message: error.message || '网络错误，请稍后重试' }
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
 * 获取任务列表
 * @param {Object} params - 查询参数（status, current, pageSize）
 * @returns {Promise} - 包含任务列表的响应
 */
export const getTaskList = async (params = {}) => {
  try {
    const response = await apiClient.get('/reconstruction/tasks', { params })
    return response.data
  } catch (error) {
    console.error('获取任务列表失败:', error)
    throw error.response?.data || { message: '网络错误，请稍后重试' }
  }
}

/**
 * 获取重建结果文件URL
 * @param {string} taskId - 任务ID
 * @param {string} fileName - 文件名
 * @returns {string} - 文件URL
 */
export const getResultFileUrl = (taskId, fileName) => {
  return `${API_URL}/reconstruction/files/${taskId}/${fileName}`
}

/**
 * 创建SSE连接
 * @param {string} taskId - 任务ID
 * @param {Object} handlers - 事件处理器对象
 * @returns {EventSource} - SSE连接对象
 */
export const createSseConnection = (taskId, handlers = {}) => {
  const eventSource = new EventSource(`${API_URL}/reconstruction/events/${taskId}`)

  // 连接建立
  eventSource.addEventListener('open', (event) => {
    console.log('SSE连接已建立')
    if (handlers.onOpen) handlers.onOpen(event)
  })

  // 连接错误
  eventSource.addEventListener('error', (event) => {
    console.error('SSE连接错误:', event)
    if (handlers.onError) handlers.onError(event)
  })

  // 连接成功事件
  eventSource.addEventListener('connect', (event) => {
    console.log('SSE连接成功:', event.data)
    if (handlers.onConnect) handlers.onConnect(event.data)
  })

  // 状态更新事件
  eventSource.addEventListener('status', (event) => {
    const data = JSON.parse(event.data)
    console.log('任务状态更新:', data)
    if (handlers.onStatus) handlers.onStatus(data)
  })

  // 结果文件事件
  eventSource.addEventListener('result', (event) => {
    const data = JSON.parse(event.data)
    console.log('结果文件可用:', data)
    if (handlers.onResult) handlers.onResult(data)
  })

  // 默认消息处理
  eventSource.onmessage = (event) => {
    console.log('收到未处理的SSE消息:', event.data)
    if (handlers.onMessage) handlers.onMessage(event)
  }

  return eventSource
}

/**
 * 关闭SSE连接
 * @param {EventSource} eventSource - SSE连接对象
 */
export const closeSseConnection = (eventSource) => {
  if (eventSource && eventSource.readyState !== 2) {
    eventSource.close()
    console.log('SSE连接已关闭')
  }
}

/**
 * 获取3D模型预览所需的所有文件URL
 * @param {Object} task - 任务对象
 * @returns {Object} - 包含所有必要文件URL的对象
 */
export const getModelPreviewUrls = (task) => {
  if (!task || !task.taskId) return {}

  return {
    objUrl: task.outputZipUrl ? getResultFileUrl(task.taskId, 'model.obj') : null,
    mtlUrl: task.outputZipUrl ? getResultFileUrl(task.taskId, 'model.mtl') : null,
    textureUrl: task.pixelImagesUrl || null,
    pixelImagesUrl: task.pixelImagesUrl || null,
    xyzImagesUrl: task.xyzImagesUrl || null
  }
}


