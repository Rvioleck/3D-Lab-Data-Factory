import { 
  uploadImage, 
  createReconstructionTask, 
  getTaskStatus, 
  getTaskList,
  createSseConnection,
  closeSseConnection,
  getModelPreviewUrls
} from '../../api/reconstruction'

const state = {
  // 当前任务
  currentTask: null,
  // 任务列表
  taskList: [],
  // SSE连接
  sseConnection: null,
  // 任务进度
  taskProgress: 0,
  // 任务日志
  taskLogs: [],
  // 模型URL
  modelUrls: {
    objUrl: '',
    mtlUrl: '',
    textureUrl: '',
    pixelImagesUrl: '',
    xyzImagesUrl: '',
    outputZipUrl: ''
  },
  // 加载状态
  loading: false,
  // 上传状态
  uploading: false
}

const getters = {
  currentTask: state => state.currentTask,
  taskList: state => state.taskList,
  taskProgress: state => state.taskProgress,
  taskLogs: state => state.taskLogs,
  modelUrls: state => state.modelUrls,
  isLoading: state => state.loading,
  isUploading: state => state.uploading,
  hasTask: state => !!state.currentTask,
  taskStatus: state => state.currentTask ? state.currentTask.status : null,
  hasModelUrls: state => !!state.modelUrls.objUrl && (!!state.modelUrls.mtlUrl || !!state.modelUrls.textureUrl)
}

const actions = {
  /**
   * 上传图片并创建重建任务
   */
  async uploadAndCreateTask({ commit, dispatch }, { file, metadata }) {
    commit('SET_UPLOADING', true)
    try {
      const response = await uploadImage(file, metadata)
      if (response.code === 0) {
        const taskId = response.data.taskId
        await dispatch('fetchTaskStatus', taskId)
        return taskId
      } else {
        throw new Error(response.message || '创建任务失败')
      }
    } catch (error) {
      console.error('上传图片并创建任务失败:', error)
      throw error
    } finally {
      commit('SET_UPLOADING', false)
    }
  },

  /**
   * 从现有图片创建重建任务
   */
  async createTaskFromImage({ commit, dispatch }, data) {
    commit('SET_UPLOADING', true)
    try {
      const response = await createReconstructionTask(data)
      if (response.code === 0) {
        const taskId = response.data.taskId
        await dispatch('fetchTaskStatus', taskId)
        return taskId
      } else {
        throw new Error(response.message || '创建任务失败')
      }
    } catch (error) {
      console.error('创建重建任务失败:', error)
      throw error
    } finally {
      commit('SET_UPLOADING', false)
    }
  },

  /**
   * 获取任务状态
   */
  async fetchTaskStatus({ commit }, taskId) {
    commit('SET_LOADING', true)
    try {
      const response = await getTaskStatus(taskId)
      if (response.code === 0) {
        commit('SET_CURRENT_TASK', response.data)
        
        // 如果任务已完成，获取模型URL
        if (response.data.status === 'COMPLETED') {
          const urls = getModelPreviewUrls(response.data)
          commit('SET_MODEL_URLS', urls)
        }
        
        return response.data
      } else {
        throw new Error(response.message || '获取任务状态失败')
      }
    } catch (error) {
      console.error('获取任务状态失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  /**
   * 获取任务列表
   */
  async fetchTaskList({ commit }, params = {}) {
    commit('SET_LOADING', true)
    try {
      const response = await getTaskList(params)
      if (response.code === 0) {
        commit('SET_TASK_LIST', response.data.records || [])
        return response.data
      } else {
        throw new Error(response.message || '获取任务列表失败')
      }
    } catch (error) {
      console.error('获取任务列表失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  /**
   * 连接到SSE事件流
   */
  connectToSse({ commit, state, dispatch }, taskId) {
    // 关闭之前的连接
    if (state.sseConnection) {
      closeSseConnection(state.sseConnection)
    }

    // 添加任务日志
    commit('ADD_TASK_LOG', { type: 'info', message: '建立实时连接...' })

    // 创建新连接
    const sseConnection = createSseConnection(taskId, {
      // 连接成功事件
      onConnect: (data) => {
        console.log(`SSE连接成功，任务ID: ${taskId}`, data)
        commit('ADD_TASK_LOG', { type: 'success', message: '实时连接已建立' })
      },
      // 状态更新事件
      onStatus: (data) => {
        console.log('收到任务状态更新:', data)
        
        // 更新任务状态
        dispatch('fetchTaskStatus', taskId)
        
        // 添加任务日志
        let logType = 'info'
        let statusText = '处理中'
        
        switch (data.status) {
          case 'PENDING':
            statusText = '等待处理'
            break
          case 'PROCESSING':
            statusText = '正在处理'
            break
          case 'COMPLETED':
            statusText = '处理完成'
            logType = 'success'
            break
          case 'FAILED':
            statusText = '处理失败'
            logType = 'error'
            break
        }
        
        commit('ADD_TASK_LOG', { 
          type: logType, 
          message: `任务状态: ${statusText}${data.error ? ' - ' + data.error : ''}` 
        })
        
        // 如果任务完成或失败，关闭连接
        if (data.status === 'COMPLETED' || data.status === 'FAILED') {
          dispatch('disconnectSse')
        }
      },
      // 结果文件事件
      onResult: (data) => {
        console.log('收到结果文件:', data)
        
        // 更新模型URL
        const updatedUrls = { ...state.modelUrls }
        
        switch (data.name) {
          case 'pixel_images.png':
            updatedUrls.pixelImagesUrl = data.url
            commit('ADD_TASK_LOG', { type: 'info', message: '收到像素图像文件' })
            break
          case 'xyz_images.png':
            updatedUrls.xyzImagesUrl = data.url
            commit('ADD_TASK_LOG', { type: 'info', message: '收到深度图像文件' })
            break
          case 'output3d.zip':
            updatedUrls.outputZipUrl = data.url
            commit('ADD_TASK_LOG', { type: 'success', message: '收到3D模型文件包' })
            
            // 设置标准模型文件URL
            updatedUrls.objUrl = `${data.url.substring(0, data.url.lastIndexOf('/'))}/model.obj`
            updatedUrls.mtlUrl = `${data.url.substring(0, data.url.lastIndexOf('/'))}/model.mtl`
            updatedUrls.textureUrl = `${data.url.substring(0, data.url.lastIndexOf('/'))}/texture.png`
            break
        }
        
        commit('SET_MODEL_URLS', updatedUrls)
      },
      // 错误处理
      onError: (error) => {
        console.error('SSE连接错误:', error)
        commit('ADD_TASK_LOG', { type: 'error', message: '连接错误，尝试重新连接...' })
      }
    })
    
    commit('SET_SSE_CONNECTION', sseConnection)
  },

  /**
   * 断开SSE连接
   */
  disconnectSse({ commit, state }) {
    if (state.sseConnection) {
      closeSseConnection(state.sseConnection)
      commit('SET_SSE_CONNECTION', null)
      commit('ADD_TASK_LOG', { type: 'info', message: '已断开实时连接' })
    }
  },

  /**
   * 清空任务日志
   */
  clearTaskLogs({ commit }) {
    commit('CLEAR_TASK_LOGS')
  },

  /**
   * 选择任务
   */
  selectTask({ commit, dispatch }, task) {
    commit('SET_CURRENT_TASK', task)
    
    // 如果任务已完成，获取模型URL
    if (task.status === 'COMPLETED') {
      const urls = getModelPreviewUrls(task)
      commit('SET_MODEL_URLS', urls)
    }
    
    // 如果任务正在处理中，连接SSE
    if (task.status === 'PENDING' || task.status === 'PROCESSING') {
      dispatch('connectToSse', task.id)
    }
  }
}

const mutations = {
  SET_CURRENT_TASK(state, task) {
    state.currentTask = task
  },
  SET_TASK_LIST(state, taskList) {
    state.taskList = taskList
  },
  SET_SSE_CONNECTION(state, connection) {
    state.sseConnection = connection
  },
  SET_TASK_PROGRESS(state, progress) {
    state.taskProgress = progress
  },
  ADD_TASK_LOG(state, log) {
    // 添加时间戳
    const now = new Date()
    const timeStr = now.toLocaleTimeString()
    
    state.taskLogs.push({
      time: timeStr,
      type: log.type,
      message: log.message
    })
    
    // 限制日志数量
    if (state.taskLogs.length > 100) {
      state.taskLogs = state.taskLogs.slice(-100)
    }
  },
  CLEAR_TASK_LOGS(state) {
    state.taskLogs = []
  },
  SET_MODEL_URLS(state, urls) {
    state.modelUrls = { ...urls }
  },
  SET_LOADING(state, loading) {
    state.loading = loading
  },
  SET_UPLOADING(state, uploading) {
    state.uploading = uploading
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}
