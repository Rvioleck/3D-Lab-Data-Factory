import axios from 'axios'

// API基础URL，可以通过环境变量配置
const API_URL = import.meta.env.VITE_API_URL || '/api'

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

    // 添加可选的元数据到表单
    if (metadata.name) formData.append('name', metadata.name)
    if (metadata.category) formData.append('category', metadata.category)
    if (metadata.tags) formData.append('tags', metadata.tags)
    if (metadata.introduction) formData.append('introduction', metadata.introduction)

    // 上传图片到图片服务
    const uploadResponse = await axios.post(`${API_URL}/picture/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      withCredentials: true
    })

    if (uploadResponse.data.code !== 0) {
      throw new Error(uploadResponse.data.message || '上传图片失败')
    }

    // 获取上传的图片ID
    const imageId = uploadResponse.data.data.id

    // 创建重建任务
    const params = new URLSearchParams()
    params.append('imageId', imageId)
    if (metadata.name) params.append('name', metadata.name)

    // 调用创建任务接口
    const response = await apiClient.post('/reconstruction/create', params, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })
    return response.data
  } catch (error) {
    console.error('上传图片失败:', error)
    // 提供更详细的错误信息
    const errorMessage = error.response?.data?.message || error.message || '网络错误，请稍后重试'
    const errorCode = error.response?.data?.code || -1
    throw { message: errorMessage, code: errorCode, originalError: error }
  }
}

/**
 * 从现有图片创建重建任务
 * @param {Object} data - 任务数据（imageId, name, category）
 * @returns {Promise} - 包含任务ID和SSE URL的响应
 */
export const createReconstructionTask = async (data) => {
  try {
    // 创建参数
    const params = new URLSearchParams()
    params.append('imageId', data.imageId)
    if (data.name) params.append('name', data.name)

    // 调用创建任务接口
    const response = await apiClient.post('/reconstruction/create', params, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })
    return response.data
  } catch (error) {
    console.error('创建重建任务失败:', error)
    // 提供更详细的错误信息
    const errorMessage = error.response?.data?.message || error.message || '网络错误，请稍后重试'
    const errorCode = error.response?.data?.code || -1
    throw { message: errorMessage, code: errorCode, originalError: error }
  }
}

/**
 * 获取任务状态
 * @param {string|number} taskId - 任务ID
 * @returns {Promise} - 包含任务状态的响应
 */
export const getTaskStatus = async (taskId) => {
  try {
    // 确保 taskId 是字符串类型
    const taskIdStr = String(taskId)
    const response = await apiClient.get(`/reconstruction/status/${taskIdStr}`)
    return response.data
  } catch (error) {
    console.error('获取任务状态失败:', error)
    // 提供更详细的错误信息
    const errorMessage = error.response?.data?.message || error.message || '网络错误，请稍后重试'
    const errorCode = error.response?.data?.code || -1
    throw { message: errorMessage, code: errorCode, originalError: error }
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
    // 提供更详细的错误信息
    const errorMessage = error.response?.data?.message || error.message || '网络错误，请稍后重试'
    const errorCode = error.response?.data?.code || -1
    throw { message: errorMessage, code: errorCode, originalError: error }
  }
}

/**
 * 获取重建结果文件URL
 * @param {string|number} taskId - 任务ID
 * @param {string} fileName - 文件名
 * @returns {string} - 文件URL
 */
export const getResultFileUrl = (taskId, fileName) => {
  // 确保 taskId 是字符串类型
  const taskIdStr = String(taskId)
  return `${API_URL}/reconstruction/files/${taskIdStr}/${fileName}`
}

/**
 * 创建SSE连接
 * @param {string|number} taskId - 任务ID
 * @param {Object} handlers - 事件处理器对象
 * @returns {EventSource} - SSE连接对象
 */
export const createSseConnection = (taskId, handlers = {}) => {
  // 确保 taskId 是字符串类型
  const taskIdStr = String(taskId)
  // 确保使用正确的SSE URL
  const eventSource = new EventSource(`${API_URL}/reconstruction/events/${taskIdStr}`)

  // 连接计数和重试逻辑
  let retryCount = 0
  const maxRetries = 5
  const retryDelay = 3000 // 3秒

  // 连接建立
  eventSource.addEventListener('open', (event) => {
    console.log('SSE连接已建立')
    // 重置重试计数
    retryCount = 0
    if (handlers.onOpen) handlers.onOpen(event)
  })

  // 连接错误
  eventSource.addEventListener('error', (event) => {
    console.error('SSE连接错误:', event)

    // 如果连接关闭且需要重试
    if (eventSource.readyState === EventSource.CLOSED && retryCount < maxRetries) {
      retryCount++
      console.log(`SSE连接断开，${retryDelay/1000}秒后尝试重新连接 (${retryCount}/${maxRetries})...`)

      // 通知错误处理器
      if (handlers.onError) {
        handlers.onError({
          ...event,
          retryCount,
          maxRetries,
          willRetry: true,
          retryDelay
        })
      }

      // 延迟后重试
      setTimeout(() => {
        if (handlers.onRetry) handlers.onRetry(retryCount)
        // 这里不需要重新创建连接，EventSource会自动重连
      }, retryDelay)
    } else if (retryCount >= maxRetries) {
      // 达到最大重试次数
      console.error(`SSE连接失败，已达到最大重试次数(${maxRetries})，不再重试`)
      if (handlers.onError) {
        handlers.onError({
          ...event,
          retryCount,
          maxRetries,
          willRetry: false,
          fatal: true
        })
      }
    } else {
      // 其他错误
      if (handlers.onError) handlers.onError(event)
    }
  })

  // 连接成功事件
  eventSource.addEventListener('connect', (event) => {
    console.log('SSE连接成功:', event.data)
    if (handlers.onConnect) handlers.onConnect(event.data)
  })

  // 状态更新事件
  eventSource.addEventListener('status', (event) => {
    try {
      console.log('收到原始状态事件数据:', event.data)
      const data = JSON.parse(event.data)
      console.log('解析后的任务状态更新:', data)
      console.log('状态类型:', typeof data.status, '状态值:', data.status)
      console.log('taskId类型:', typeof data.taskId, 'taskId值:', data.taskId)
      if (handlers.onStatus) handlers.onStatus(data)
    } catch (error) {
      console.error('解析状态事件数据失败:', error, event.data)
    }
  })

  // 结果文件事件
  eventSource.addEventListener('result', (event) => {
    try {
      console.log('收到原始结果事件数据:', event.data)
      const data = JSON.parse(event.data)
      console.log('解析后的结果文件数据:', data)
      console.log('文件名类型:', typeof data.name, '文件名:', data.name)
      console.log('文件URL类型:', typeof data.url, '文件URL:', data.url)
      console.log('taskId类型:', typeof data.taskId, 'taskId值:', data.taskId)
      if (handlers.onResult) handlers.onResult(data)
    } catch (error) {
      console.error('解析结果事件数据失败:', error, event.data)
    }
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
  if (!task || (!task.id && !task.taskId)) {
    console.warn('getModelPreviewUrls: 无效的任务对象');
    return {};
  }

  // 使用id或taskId，确保兼容性
  const taskId = task.id || task.taskId;
  console.log('getModelPreviewUrls 输入任务:', task, '使用taskId:', taskId);

  // 构建标准文件URL
  const standardObjUrl = getResultFileUrl(taskId, 'model.obj');
  const standardMtlUrl = getResultFileUrl(taskId, 'model.mtl');
  const standardTextureUrl = getResultFileUrl(taskId, 'texture.png');

  // 检查任务是否有关联的模型
  if (task.resultModelId) {
    // 如果有关联的模型，尝试使用模型的URL
    const urls = {
      // 模型文件 - 优先使用模型中的URL，如果没有则使用标准URL
      objUrl: task.objFileUrl || (task.outputZipUrl ? standardObjUrl : null),
      mtlUrl: task.mtlFileUrl || (task.outputZipUrl ? standardMtlUrl : null),
      // 纹理文件 - 优先使用模型中的纹理URL，如果没有则使用标准URL，最后尝试使用像素图像
      textureUrl: task.textureImageUrl || (task.outputZipUrl ? standardTextureUrl : task.pixelImagesUrl || null),
      // 原始图像和深度图像
      pixelImagesUrl: task.pixelImagesUrl || null,
      xyzImagesUrl: task.xyzImagesUrl || null
    };

    console.log('getModelPreviewUrls 返回结果 (有模型ID):', urls);
    return urls;
  } else {
    // 如果没有关联的模型，使用标准URL
    const urls = {
      // 模型文件 - 如果有outputZipUrl，则使用标准URL
      objUrl: task.outputZipUrl ? standardObjUrl : null,
      mtlUrl: task.outputZipUrl ? standardMtlUrl : null,
      // 纹理文件 - 优先使用标准纹理URL，如果没有则使用像素图像
      textureUrl: task.outputZipUrl ? standardTextureUrl : task.pixelImagesUrl || null,
      // 原始图像和深度图像
      pixelImagesUrl: task.pixelImagesUrl || null,
      xyzImagesUrl: task.xyzImagesUrl || null
    };

    console.log('getModelPreviewUrls 返回结果 (无模型ID):', urls);
    return urls;
  }
}


