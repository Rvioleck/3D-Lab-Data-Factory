<template>
  <div class="reconstruction-view">
    <div class="container mt-4">
      <div class="row mb-4">
        <div class="col-12">
          <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
              <li class="breadcrumb-item"><router-link to="/images">图片库</router-link></li>
              <li class="breadcrumb-item active" aria-current="page">3D重建</li>
            </ol>
          </nav>
        </div>
      </div>

      <div class="row">
        <div class="col-md-12 text-center mb-4">
          <h1>3D重建</h1>
          <p class="lead">从图片生成高质量3D模型</p>
          <p class="text-muted">支持单张图片重建，生成高质量3D模型</p>
        </div>
      </div>

      <div class="row">
        <!-- 上传区域 -->
        <div class="col-md-4">
          <div class="card mb-4 upload-card">
            <div class="card-header bg-primary text-white">
              <h5 class="mb-0">
                <i v-if="!sourceImageId" class="bi bi-cloud-upload me-2"></i>
                <i v-else class="bi bi-image me-2"></i>
                {{ sourceImageId ? '源图片' : '上传图片' }}
              </h5>
            </div>
            <div class="card-body">
              <!-- 加载源图片中 -->
              <div v-if="loadingSourceImage" class="text-center py-4">
                <div class="spinner-border text-primary mb-3" role="status">
                  <span class="visually-hidden">加载中...</span>
                </div>
                <p>正在加载图片...</p>
              </div>

              <!-- 源图片显示 -->
              <div v-else-if="sourceImage" class="source-image-container mb-3">
                <div class="position-relative">
                  <img :src="sourceImage.url" class="img-fluid rounded" alt="源图片">
                  <div class="source-image-info p-2">
                    <h6>{{ sourceImage.name || '未命名图片' }}</h6>
                    <p class="small text-muted mb-0">尺寸: {{ sourceImage.width || '未知' }} x {{ sourceImage.height || '未知' }}</p>
                  </div>
                  <button class="btn btn-sm btn-outline-secondary position-absolute top-0 end-0 m-2" @click="router.push('/images')">
                    <i class="bi bi-arrow-left me-1"></i> 返回选择其他图片
                  </button>
                </div>
              </div>

              <!-- 普通上传模式 -->
              <template v-else>
                <div class="upload-preview mb-3" v-if="fileSelected && previewUrl">
                  <img :src="previewUrl" class="img-fluid rounded" alt="图片预览">
                  <button class="btn btn-sm btn-outline-danger preview-remove" @click="removeSelectedFile">
                    <i class="bi bi-x-lg"></i>
                  </button>
                </div>

                <div class="upload-dropzone mb-3" v-if="!fileSelected" @click="triggerFileInput" @dragover.prevent @drop.prevent="handleFileDrop">
                  <i class="bi bi-image display-4"></i>
                  <p class="mt-2">点击或拖放图片到此处</p>
                  <p class="text-muted small">支持JPG、PNG格式的图片</p>
                </div>

                <input
                  type="file"
                  class="d-none"
                  id="fileInput"
                  ref="fileInput"
                  accept="image/*"
                  :disabled="uploading"
                  @change="handleFileChange"
                />
              </template>

              <div class="mb-3">
                <label for="modelName" class="form-label">模型名称（可选）</label>
                <input
                  type="text"
                  class="form-control"
                  id="modelName"
                  v-model="modelName"
                  placeholder="输入模型名称"
                  :disabled="uploading"
                />
              </div>

              <div class="mb-3">
                <label for="modelCategory" class="form-label">分类（可选）</label>
                <select
                  class="form-select"
                  id="modelCategory"
                  v-model="modelCategory"
                  :disabled="uploading"
                >
                  <option value="">选择分类</option>
                  <option value="furniture">家具</option>
                  <option value="toy">玩具</option>
                  <option value="decoration">装饰品</option>
                  <option value="other">其他</option>
                </select>
              </div>

              <div class="d-grid">
                <button
                  class="btn btn-primary btn-lg"
                  @click="sourceImage ? createTaskFromSourceImage() : handleUpload()"
                  :disabled="(!fileSelected && !sourceImage) || uploading"
                >
                  <span v-if="uploading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  <i v-else class="bi bi-magic me-2"></i>
                  {{ uploading ? '处理中...' : '开始3D重建' }}
                </button>
              </div>
            </div>
          </div>

          <!-- 任务列表 -->
          <div class="card mb-4 task-list-card">
            <div class="card-header d-flex justify-content-between align-items-center bg-light">
              <h5 class="mb-0"><i class="bi bi-list-check me-2"></i>重建任务</h5>
              <button class="btn btn-sm btn-outline-primary" @click="loadTaskList">
                <i class="bi bi-arrow-clockwise"></i> 刷新
              </button>
            </div>
            <div class="card-body p-0">
              <div v-if="loading" class="text-center p-4">
                <div class="spinner-border text-primary" role="status">
                  <span class="visually-hidden">加载中...</span>
                </div>
                <p class="mt-2">正在加载任务列表...</p>
              </div>
              <div v-else-if="taskList.length === 0" class="text-center p-4 empty-task-list">
                <i class="bi bi-inbox display-4 text-muted"></i>
                <p class="mt-2">暂无重建任务</p>
                <p class="text-muted small">上传图片并开始您的第一个3D重建任务</p>
              </div>
              <ul v-else class="list-group list-group-flush task-list">
                <li
                  v-for="task in taskList"
                  :key="task.taskId"
                  class="list-group-item list-group-item-action task-item"
                  :class="{ 'active': task.taskId === currentTask?.taskId }"
                  @click="selectTask(task)"
                >
                  <div class="d-flex justify-content-between align-items-center">
                    <div>
                      <div class="fw-bold d-flex align-items-center">
                        <i class="bi me-2" :class="getStatusIcon(task.status)"></i>
                        任务 #{{ task.taskId.substring(0, 8) }}
                      </div>
                      <small class="text-muted">{{ formatDate(task.createTime) }}</small>
                    </div>
                    <span class="badge rounded-pill" :class="getStatusBadgeClass(task.status)">
                      {{ getStatusText(task.status) }}
                    </span>
                  </div>
                  <div v-if="task.status === 'PROCESSING'" class="progress mt-2" style="height: 5px;">
                    <div class="progress-bar progress-bar-striped progress-bar-animated" role="progressbar" style="width: 100%"></div>
                  </div>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 结果区域 -->
        <div class="col-md-8">
          <!-- 欢迎信息（无选中任务时显示） -->
          <div v-if="!currentTask" class="card welcome-card mb-4">
            <div class="card-body text-center p-5">
              <i class="bi bi-box display-1 text-primary mb-3"></i>
              <h3>欢迎使用3D重建工具</h3>
              <p class="lead">上传一张图片，即可生成高质量的交互式3D模型</p>
              <hr class="my-4">
              <p>从左侧面板上传图片开始，或者选择一个已有的任务查看结果。</p>
            </div>
          </div>

          <!-- 任务状态 -->
          <div v-if="currentTask" class="card mb-4 task-status-card">
            <div class="card-header bg-light">
              <h5 class="mb-0 d-flex align-items-center">
                <i class="bi me-2" :class="getStatusIcon(currentTask.status)"></i>
                任务状态
              </h5>
            </div>
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <div>
                  <h6 class="mb-0">任务ID: <span class="text-primary">{{ currentTask.taskId }}</span></h6>
                  <small class="text-muted">创建时间: {{ formatDate(currentTask.createTime) }}</small>
                </div>
                <span class="badge rounded-pill" :class="getStatusBadgeClass(currentTask.status)">
                  {{ getStatusText(currentTask.status) }}
                </span>
              </div>

              <div v-if="currentTask.status === 'PROCESSING'" class="progress mb-3">
                <div class="progress-bar progress-bar-striped progress-bar-animated" role="progressbar" style="width: 100%"></div>
              </div>

              <div v-if="currentTask.errorMessage" class="alert alert-danger">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                <strong>错误:</strong> {{ currentTask.errorMessage }}
              </div>

              <div v-if="currentTask.processingTime" class="text-muted">
                <i class="bi bi-clock me-2"></i>
                处理时间: <span class="fw-bold">{{ currentTask.processingTime }}</span> 秒
              </div>

              <div v-if="currentTask.status === 'PROCESSING'" class="alert alert-info mt-3">
                <i class="bi bi-info-circle-fill me-2"></i>
                正在处理您的图片并生成3D模型，这可能需要几分钟时间。请耐心等待。
              </div>
            </div>
          </div>

          <!-- 结果文件 -->
          <div v-if="currentTask && (currentTask.status === 'COMPLETED' || currentTask.pixelImagesUrl)" class="card mb-4 result-files-card">
            <div class="card-header bg-success text-white">
              <h5 class="mb-0"><i class="bi bi-file-earmark-image me-2"></i>重建结果</h5>
            </div>
            <div class="card-body">
              <div class="row">
                <div class="col-md-6 mb-3" v-if="currentTask.pixelImagesUrl">
                  <div class="card h-100 result-image-card">
                    <div class="card-header bg-light">纹理图像</div>
                    <div class="card-body text-center p-2">
                      <img :src="currentTask.pixelImagesUrl" class="img-fluid mb-2 result-image" alt="纹理图像">
                      <a :href="currentTask.pixelImagesUrl" class="btn btn-sm btn-primary" download>
                        <i class="bi bi-download"></i> 下载纹理
                      </a>
                    </div>
                  </div>
                </div>
                <div class="col-md-6 mb-3" v-if="currentTask.xyzImagesUrl">
                  <div class="card h-100 result-image-card">
                    <div class="card-header bg-light">深度图像</div>
                    <div class="card-body text-center p-2">
                      <img :src="currentTask.xyzImagesUrl" class="img-fluid mb-2 result-image" alt="深度图像">
                      <a :href="currentTask.xyzImagesUrl" class="btn btn-sm btn-primary" download>
                        <i class="bi bi-download"></i> 下载深度图
                      </a>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="currentTask.outputZipUrl" class="mt-4 model-download-section">
                <h6 class="mb-3"><i class="bi bi-box me-2"></i>3D模型文件</h6>
                <div class="d-flex flex-wrap gap-2">
                  <a :href="currentTask.outputZipUrl" class="btn btn-primary" download>
                    <i class="bi bi-file-earmark-zip me-1"></i> 下载完整ZIP包
                  </a>
                  <a :href="getResultFileUrl(currentTask.taskId, 'model.obj')" class="btn btn-outline-primary" download>
                    <i class="bi bi-box me-1"></i> OBJ模型
                  </a>
                  <a :href="getResultFileUrl(currentTask.taskId, 'model.mtl')" class="btn btn-outline-primary" download>
                    <i class="bi bi-palette me-1"></i> MTL材质
                  </a>
                </div>
                <div class="alert alert-light mt-3 small">
                  <i class="bi bi-info-circle me-2"></i>
                  提示：您可以在Blender、Maya等软件中打开OBJ文件进行进一步编辑。
                </div>
              </div>
            </div>
          </div>

          <!-- 3D模型预览 -->
          <div v-if="currentTask && currentTask.outputZipUrl" class="card mb-4 model-viewer-card">
            <div class="card-header bg-primary text-white">
              <h5 class="mb-0"><i class="bi bi-cube me-2"></i>3D模型预览</h5>
            </div>
            <div class="card-body p-0">
              <ModelViewer
                :objUrl="modelUrls.objUrl"
                :mtlUrl="modelUrls.mtlUrl"
                :textureUrl="modelUrls.textureUrl"
              />
            </div>
            <div class="card-footer bg-light">
              <div class="small text-muted">
                <i class="bi bi-mouse me-2"></i>拖动鼠标旋转模型 | 滚轮缩放 | 右键平移
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router'
import ModelViewer from '@/components/ModelViewer.vue'
import {
  uploadImage,
  getTaskStatus,
  getTaskList,
  getResultFileUrl,
  createSseConnection,
  closeSseConnection,
  getModelPreviewUrls,
  createReconstructionTask
} from '@/api/reconstruction'
import axios from 'axios'

// API URL
const API_URL = import.meta.env.VITE_API_URL || ''

export default {
  name: 'ReconstructionView',
  components: {
    ModelViewer
  },
  setup() {
    const store = useStore()
    const route = useRoute()
    const router = useRouter()
    const fileInput = ref(null)
    const fileSelected = ref(false)
    const uploading = ref(false)
    const loading = ref(false)
    const modelName = ref('')
    const modelCategory = ref('')
    const currentTask = ref(null)
    const taskList = ref([])
    const sseConnection = ref(null)
    const previewUrl = ref(null) // 图片预览URL

    // 源图片相关变量
    const sourceImageId = ref(route.params.imageId)
    const sourceImage = ref(null)
    const loadingSourceImage = ref(false)

    // 计算模型预览URL
    const modelUrls = computed(() => {
      if (!currentTask.value || !currentTask.value.outputZipUrl) return {}
      return getModelPreviewUrls(currentTask.value)
    })

    // 触发文件输入点击
    const triggerFileInput = () => {
      if (fileInput.value) {
        fileInput.value.click()
      }
    }

    // 处理文件拖放
    const handleFileDrop = (event) => {
      const files = event.dataTransfer.files
      if (files && files.length > 0) {
        const file = files[0]
        if (file.type.startsWith('image/')) {
          fileInput.value.files = files
          handleFileChange({ target: { files } })
        } else {
          alert('请上传图片文件（JPG、PNG等）')
        }
      }
    }

    // 移除选中的文件
    const removeSelectedFile = () => {
      fileInput.value.value = ''
      fileSelected.value = false
      previewUrl.value = null
    }

    // 处理文件选择
    const handleFileChange = (event) => {
      const files = event.target.files
      fileSelected.value = files && files.length > 0

      if (fileSelected.value) {
        const file = files[0]
        // 创建URL预览
        previewUrl.value = URL.createObjectURL(file)

        // 自动设置模型名称（如果未设置）
        if (!modelName.value) {
          // 从文件名提取名称（去除扩展名）
          const fileName = file.name.replace(/\.[^/.]+$/, "")
          modelName.value = fileName
        }
      } else {
        previewUrl.value = null
      }
    }

    // 处理上传
    const handleUpload = async () => {
      if (!fileInput.value.files || fileInput.value.files.length === 0) {
        alert('请选择图片文件')
        return
      }

      const file = fileInput.value.files[0]
      uploading.value = true

      try {
        // 准备元数据
        const metadata = {
          name: modelName.value,
          category: modelCategory.value
        }

        // 上传图片
        const response = await uploadImage(file, metadata)
        console.log('上传成功:', response)

        if (response.code === 0) {
          const taskId = response.data.taskId
          const sseUrl = response.data.sseUrl

          // 获取初始任务状态
          await fetchTaskStatus(taskId)

          // 建立SSE连接
          connectToSse(taskId)

          // 重置表单
          fileInput.value.value = ''
          fileSelected.value = false
          modelName.value = ''
          modelCategory.value = ''

          // 刷新任务列表
          loadTaskList()
        } else {
          alert(`上传失败: ${response.message}`)
        }
      } catch (error) {
        console.error('上传失败:', error)
        alert(`上传失败: ${error.message || '未知错误'}`)
      } finally {
        uploading.value = false
      }
    }

    // 获取任务状态
    const fetchTaskStatus = async (taskId) => {
      try {
        const response = await getTaskStatus(taskId)
        if (response.code === 0) {
          currentTask.value = response.data
          return response.data
        } else {
          console.error('获取任务状态失败:', response.message)
          return null
        }
      } catch (error) {
        console.error('获取任务状态失败:', error)
        return null
      }
    }

    // 加载任务列表
    const loadTaskList = async () => {
      loading.value = true
      try {
        const response = await getTaskList()
        if (response.code === 0) {
          taskList.value = response.data.records
        } else {
          console.error('获取任务列表失败:', response.message)
        }
      } catch (error) {
        console.error('获取任务列表失败:', error)
      } finally {
        loading.value = false
      }
    }

    // 选择任务
    const selectTask = async (task) => {
      // 关闭之前的SSE连接
      if (sseConnection.value) {
        closeSseConnection(sseConnection.value)
        sseConnection.value = null
      }

      // 设置当前任务
      currentTask.value = task

      // 如果任务正在处理中，建立SSE连接
      if (task.status === 'PROCESSING') {
        connectToSse(task.taskId)
      } else {
        // 刷新任务状态以获取最新数据
        await fetchTaskStatus(task.taskId)
      }
    }

    // 建立SSE连接
    const connectToSse = (taskId) => {
      // 关闭之前的连接
      if (sseConnection.value) {
        closeSseConnection(sseConnection.value)
      }

      // 创建新连接
      sseConnection.value = createSseConnection(taskId, {
        onStatus: (data) => {
          console.log('收到状态更新:', data)
          if (currentTask.value && currentTask.value.taskId === data.taskId) {
            // 更新任务状态
            currentTask.value.status = data.status
            if (data.error) {
              currentTask.value.errorMessage = data.error
            }

            // 如果任务完成或失败，刷新任务状态以获取完整数据
            if (data.status === 'COMPLETED' || data.status === 'FAILED') {
              fetchTaskStatus(taskId)
              // 刷新任务列表
              loadTaskList()
            }
          }
        },
        onResult: (data) => {
          console.log('收到结果文件:', data)
          if (currentTask.value && currentTask.value.taskId === data.taskId) {
            // 根据文件类型更新URL
            if (data.name.includes('pixel_images')) {
              currentTask.value.pixelImagesUrl = data.url
            } else if (data.name.includes('xyz_images')) {
              currentTask.value.xyzImagesUrl = data.url
            } else if (data.name.includes('output3d')) {
              currentTask.value.outputZipUrl = data.url
            }
          }
        },
        onError: (event) => {
          console.error('SSE连接错误:', event)
          // 尝试重新连接
          setTimeout(() => {
            if (currentTask.value && currentTask.value.status === 'PROCESSING') {
              connectToSse(taskId)
            }
          }, 5000)
        }
      })
    }

    // 获取状态图标
    const getStatusIcon = (status) => {
      switch (status) {
        case 'PENDING':
          return 'bi-hourglass'
        case 'PROCESSING':
          return 'bi-arrow-repeat'
        case 'COMPLETED':
          return 'bi-check-circle'
        case 'FAILED':
          return 'bi-exclamation-circle'
        default:
          return 'bi-question-circle'
      }
    }

    // 获取状态徽章样式
    const getStatusBadgeClass = (status) => {
      switch (status) {
        case 'PENDING':
          return 'bg-secondary'
        case 'PROCESSING':
          return 'bg-primary'
        case 'COMPLETED':
          return 'bg-success'
        case 'FAILED':
          return 'bg-danger'
        default:
          return 'bg-secondary'
      }
    }

    // 获取状态文本
    const getStatusText = (status) => {
      switch (status) {
        case 'PENDING':
          return '等待处理'
        case 'PROCESSING':
          return '处理中'
        case 'COMPLETED':
          return '已完成'
        case 'FAILED':
          return '失败'
        default:
          return '未知状态'
      }
    }

    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString()
    }

    // 加载源图片
    const loadSourceImage = async () => {
      if (!sourceImageId.value) return

      loadingSourceImage.value = true
      try {
        // 实际应用中应该调用API
        // const response = await axios.get(`${API_URL}/api/images/${sourceImageId.value}`, {
        //   withCredentials: true
        // })
        // sourceImage.value = response.data

        // 模拟API响应
        setTimeout(() => {
          // 模拟图片数据
          sourceImage.value = {
            id: sourceImageId.value,
            name: `图片${sourceImageId.value}`,
            url: `https://via.placeholder.com/800x600?text=图片${sourceImageId.value}`,
            width: 800,
            height: 600,
            createTime: new Date().toISOString()
          }

          // 预填充模型名称
          if (!modelName.value && sourceImage.value.name) {
            modelName.value = `${sourceImage.value.name}模型`
          }

          // 设置预览URL
          previewUrl.value = sourceImage.value.url
          fileSelected.value = true

          loadingSourceImage.value = false
        }, 500)
      } catch (error) {
        console.error('加载源图片失败:', error)
        loadingSourceImage.value = false
        alert('加载图片失败，请返回图片库重新选择')
        router.push('/images')
      }
    }

    // 从源图片创建重建任务
    const createTaskFromSourceImage = async () => {
      if (!sourceImage.value) return

      uploading.value = true
      try {
        // 实际应用中应该调用API
        // const data = {
        //   imageId: sourceImage.value.id,
        //   name: modelName.value || undefined,
        //   category: modelCategory.value || undefined
        // }
        // const response = await createReconstructionTask(data)
        // 处理响应...

        // 模拟创建任务成功
        setTimeout(() => {
          // 模拟任务数据
          const taskId = 'task-' + Date.now()
          const sseUrl = `${API_URL}/api/reconstruction/events/${taskId}`

          // 创建新任务
          currentTask.value = {
            taskId,
            status: 'PENDING',
            createTime: new Date().toISOString(),
            sourceImageId: sourceImage.value.id
          }

          // 添加到任务列表
          taskList.value.unshift(currentTask.value)

          // 连接SSE
          connectToTaskEvents(taskId, sseUrl)

          // 重置状态
          uploading.value = false

          // 显示成功消息
          alert('重建任务创建成功！')
        }, 1000)
      } catch (error) {
        console.error('创建重建任务失败:', error)
        alert('创建重建任务失败: ' + (error.response?.data?.message || '未知错误'))
        uploading.value = false
      }
    }

    // 组件挂载时加载任务列表
    onMounted(() => {
      loadTaskList()

      // 如果有源图片ID，加载源图片
      if (sourceImageId.value) {
        loadSourceImage()
      }
    })

    // 组件卸载前关闭SSE连接
    onBeforeUnmount(() => {
      if (sseConnection.value) {
        closeSseConnection(sseConnection.value)
        sseConnection.value = null
      }
    })

    return {
      fileInput,
      fileSelected,
      uploading,
      loading,
      modelName,
      modelCategory,
      currentTask,
      taskList,
      modelUrls,
      previewUrl,
      sourceImageId,
      sourceImage,
      loadingSourceImage,
      router,
      handleFileChange,
      handleUpload,
      loadTaskList,
      selectTask,
      getStatusBadgeClass,
      getStatusText,
      getStatusIcon,
      formatDate,
      getResultFileUrl,
      triggerFileInput,
      handleFileDrop,
      removeSelectedFile,
      createTaskFromSourceImage
    }
  }
}
</script>

<style scoped>
/* 卡片基本样式 */
.card {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}

.card:hover {
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.card-header {
  border-bottom: 1px solid rgba(0, 0, 0, 0.125);
  padding: 0.75rem 1.25rem;
}

/* 上传区域样式 */
.upload-card {
  border: none;
}

.upload-dropzone {
  border: 2px dashed #dee2e6;
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-dropzone:hover {
  border-color: #0d6efd;
  background-color: rgba(13, 110, 253, 0.05);
}

.upload-preview {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  max-height: 200px;
}

.upload-preview img {
  width: 100%;
  object-fit: cover;
}

.preview-remove {
  position: absolute;
  top: 8px;
  right: 8px;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.8);
}

/* 源图片样式 */
.source-image-container {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.source-image-container img {
  width: 100%;
  object-fit: contain;
}

.source-image-info {
  background-color: rgba(0, 0, 0, 0.05);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

/* 任务列表样式 */
.task-list-card {
  max-height: 400px;
}

.task-list {
  max-height: 350px;
  overflow-y: auto;
}

.task-item {
  cursor: pointer;
  transition: background-color 0.2s;
  border-left: 3px solid transparent;
}

.task-item.active {
  background-color: #f0f8ff;
  color: #000;
  border-left-color: #0d6efd;
}

.task-item:hover:not(.active) {
  background-color: #f8f9fa;
}

.empty-task-list {
  color: #6c757d;
}

/* 结果区域样式 */
.welcome-card {
  background-color: #f8f9fa;
  border: none;
}

.result-image-card {
  transition: transform 0.2s;
}

.result-image-card:hover {
  transform: translateY(-5px);
}

.result-image {
  max-height: 200px;
  object-fit: contain;
}

.model-download-section {
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 8px;
}

.model-viewer-card {
  overflow: hidden;
}
</style>
