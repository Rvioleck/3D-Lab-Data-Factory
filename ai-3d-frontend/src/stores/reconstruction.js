import { defineStore } from 'pinia'
import { reconstructionApi } from '@/api/reconstruction'

export const useReconstructionStore = defineStore('reconstruction', {
  state: () => ({
    currentTask: null,
    taskList: [],
    loading: false,
    error: null,
    sseConnection: null
  }),

  actions: {
    async uploadAndCreateTask(file) {
      this.loading = true
      this.error = null
      
      try {
        const uploadResult = await reconstructionApi.uploadImage(file)
        const taskResult = await reconstructionApi.createTask(uploadResult.imageId)
        this.currentTask = taskResult
        this.connectToSSE(taskResult.taskId)
        return taskResult
      } catch (error) {
        this.error = error.message
        throw error
      } finally {
        this.loading = false
      }
    },

    connectToSSE(taskId) {
      if (this.sseConnection) {
        this.sseConnection.close()
      }

      const sse = reconstructionApi.connectSSE(taskId)
      
      sse.onmessage = (event) => {
        const data = JSON.parse(event.data)
        this.updateTaskStatus(data)
      }

      sse.onerror = () => {
        sse.close()
        this.sseConnection = null
      }

      this.sseConnection = sse
    },

    updateTaskStatus(data) {
      if (this.currentTask?.taskId === data.taskId) {
        this.currentTask = { ...this.currentTask, ...data }
      }
    }
  }
})