import { apiClient } from './apiClient'

export const reconstructionApi = {
  uploadImage: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return apiClient.post('/reconstruction/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  createTask: (imageId) =>
    apiClient.post('/reconstruction/create', { imageId }),

  getTaskStatus: (taskId) =>
    apiClient.get(`/reconstruction/status/${taskId}`),

  getTaskList: (params) =>
    apiClient.get('/reconstruction/tasks', { params }),

  connectSSE: (taskId) =>
    new EventSource(`/api/reconstruction/events/${taskId}`),

  getResultFileUrl: (taskId, fileName) =>
    `${apiClient.defaults.baseURL}/reconstruction/files/${taskId}/${fileName}`,

  getModelById: (modelId) =>
    apiClient.get(`/model/${modelId}`),

  deleteModel: (modelId) =>
    apiClient.post(`/model/delete`, { id: modelId })
}