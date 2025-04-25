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
        <!-- 移动端切换按钮 -->
        <div class="col-12 d-md-none mb-3">
          <div class="btn-group w-100" role="group">
            <button
              class="btn"
              :class="activeTab === 'upload' ? 'btn-primary' : 'btn-outline-primary'"
              @click="activeTab = 'upload'"
            >
              <i class="bi bi-cloud-upload me-1"></i> 上传区域
            </button>
            <button
              class="btn"
              :class="activeTab === 'result' ? 'btn-primary' : 'btn-outline-primary'"
              @click="activeTab = 'result'"
            >
              <i class="bi bi-box me-1"></i> 结果区域
            </button>
          </div>
        </div>

        <!-- 上传区域 -->
        <div class="col-md-4" :class="{ 'd-none d-md-block': activeTab === 'result' }">
          <div class="card mb-4 upload-card">
            <div class="card-header bg-primary text-white">
              <h5 class="mb-0">
                <i v-if="!sourceImageId" class="bi bi-cloud-upload me-2"></i>
                <i v-else class="bi bi-image me-2"></i>
                {{ sourceImageId ? '选中的图片' : '上传图片' }}
              </h5>
            </div>
            <div class="card-body">
              <!-- 已选择图片显示 -->
              <div v-if="sourceImage" class="selected-image-container mb-3">
                <div class="selected-image">
                  <img :src="sourceImage.url" alt="选中的图片" class="img-fluid rounded" />
                  <div class="image-info mt-2">
                    <h6>{{ sourceImage.name || '未命名图片' }}</h6>
                    <p v-if="sourceImage.introduction" class="text-muted small">
                      {{ sourceImage.introduction }}
                    </p>
                    <div class="d-flex justify-content-between align-items-center">
                      <span class="badge bg-secondary">{{ sourceImage.picFormat }}</span>
                      <span class="text-muted small">
                        {{ sourceImage.picWidth }}x{{ sourceImage.picHeight }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 文件上传区域 -->
              <div v-else class="upload-area" @click="triggerFileInput" @drop.prevent="handleFileDrop" @dragover.prevent>
                <input
                  type="file"
                  ref="fileInput"
                  class="d-none"
                  accept="image/*"
                  @change="handleFileChange"
                />
                <div class="upload-placeholder">
                  <i class="bi bi-cloud-arrow-up-fill display-4"></i>
                  <p class="mt-3">点击或拖拽图片到此处上传</p>
                  <p class="text-muted small">支持JPG、PNG格式图片</p>
                </div>
              </div>

              <!-- 已选择文件信息 -->
              <div v-if="fileSelected && !sourceImageId" class="selected-file mt-3">
                <div class="d-flex align-items-center">
                  <i class="bi bi-file-earmark-image me-2 text-primary"></i>
                  <div class="flex-grow-1">
                    <div class="fw-bold">{{ selectedFile.name }}</div>
                    <div class="text-muted small">
                      {{ formatFileSize(selectedFile.size) }}
                    </div>
                  </div>
                  <button class="btn btn-sm btn-outline-danger" @click="removeSelectedFile">
                    <i class="bi bi-x"></i>
                  </button>
                </div>
              </div>

              <!-- 模型信息表单 -->
              <div class="model-form mt-4">
                <div class="mb-3">
                  <label for="modelName" class="form-label">模型名称</label>
                  <input
                    type="text"
                    class="form-control"
                    id="modelName"
                    v-model="modelName"
                    placeholder="输入模型名称"
                  />
                </div>
                <div class="mb-3">
                  <label for="modelCategory" class="form-label">模型分类</label>
                  <input
                    type="text"
                    class="form-control"
                    id="modelCategory"
                    v-model="modelCategory"
                    placeholder="输入模型分类"
                  />
                </div>
              </div>

              <!-- 上传按钮 -->
              <div class="d-grid gap-2 mt-4">
                <button
                  v-if="!sourceImageId"
                  class="btn btn-primary"
                  @click="handleUpload"
                  :disabled="!fileSelected || isUploading"
                >
                  <span v-if="isUploading">
                    <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                    上传中...
                  </span>
                  <span v-else>
                    <i class="bi bi-cloud-upload me-1"></i> 上传并创建3D模型
                  </span>
                </button>
                <button
                  v-else
                  class="btn btn-primary"
                  @click="createTaskFromSourceImage"
                  :disabled="isUploading"
                >
                  <span v-if="isUploading">
                    <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                    创建中...
                  </span>
                  <span v-else>
                    <i class="bi bi-box me-1"></i> 创建3D模型
                  </span>
                </button>
              </div>
            </div>
          </div>

          <!-- 任务列表 -->
          <div class="card task-list-card">
            <div class="card-header bg-secondary text-white">
              <h5 class="mb-0">
                <i class="bi bi-list-check me-2"></i> 任务列表
              </h5>
            </div>
            <div class="card-body p-0">
              <div class="list-group list-group-flush">
                <button
                  v-for="task in taskList"
                  :key="task.id"
                  class="list-group-item list-group-item-action"
                  :class="{ active: currentTask && currentTask.id === task.id }"
                  @click="selectTask(task)"
                >
                  <div class="d-flex w-100 justify-content-between">
                    <h6 class="mb-1">任务 #{{ task.id }}</h6>
                    <span :class="getStatusBadgeClass(task.status)">{{ getStatusText(task.status) }}</span>
                  </div>
                  <p class="mb-1 small">创建时间: {{ formatDate(task.createTime) }}</p>
                </button>
                <div v-if="taskList.length === 0" class="list-group-item text-center text-muted py-4">
                  <i class="bi bi-inbox-fill display-6"></i>
                  <p class="mt-2">暂无任务</p>
                </div>
              </div>
            </div>
            <div class="card-footer">
              <button class="btn btn-sm btn-outline-secondary w-100" @click="loadTaskList">
                <i class="bi bi-arrow-clockwise me-1"></i> 刷新列表
              </button>
            </div>
          </div>
        </div>

        <!-- 结果区域 -->
        <div class="col-md-8" :class="{ 'd-none d-md-block': activeTab === 'upload' }">
          <!-- 任务状态卡片 -->
          <div v-if="currentTask" class="card mb-4">
            <div class="card-header" :class="getStatusHeaderClass(currentTask.status)">
              <div class="d-flex justify-content-between align-items-center">
                <h5 class="mb-0 text-white">
                  <i :class="getStatusIcon(currentTask.status)" class="me-2"></i>
                  任务状态
                </h5>
                <span :class="getStatusBadgeClass(currentTask.status)">{{ getStatusText(currentTask.status) }}</span>
              </div>
            </div>
            <div class="card-body">
              <ReconstructionStatus
                :status="currentTask.status"
                :error-message="currentTask.errorMessage"
                :processing-time="currentTask.processingTime"
                :progress="taskProgress"
                :logs="taskLogs"
                @clear-logs="clearTaskLogs"
              />
            </div>
          </div>

          <!-- 结果文件查看器 -->
          <div v-if="currentTask" class="card mb-4">
            <div class="card-header bg-success text-white">
              <h5 class="mb-0">
                <i class="bi bi-file-earmark-richtext me-2"></i> 结果文件
              </h5>
            </div>
            <div class="card-body">
              <ResultFilesViewer
                :pixel-images-url="modelUrls.pixelImagesUrl"
                :xyz-images-url="modelUrls.xyzImagesUrl"
                :output-zip-url="modelUrls.outputZipUrl"
                :obj-file-url="modelUrls.objUrl"
                :mtl-file-url="modelUrls.mtlUrl"
                :texture-image-url="modelUrls.textureUrl"
                :task-id="currentTask.id"
              />
            </div>
          </div>

          <!-- 3D模型查看器 -->
          <div v-if="hasModelUrls" class="card mb-4">
            <div class="card-header bg-dark text-white">
              <div class="d-flex justify-content-between align-items-center">
                <h5 class="mb-0">
                  <i class="bi bi-box me-2"></i> 3D模型查看器
                </h5>
                <button class="btn btn-sm btn-outline-light" @click="toggleModelViewerSize">
                  <i :class="largeModelViewer ? 'bi bi-fullscreen-exit' : 'bi bi-fullscreen'"></i>
                </button>
              </div>
            </div>
            <div class="card-body p-0">
              <div :class="{ 'model-viewer-large': largeModelViewer }">
                <ModelViewer
                  :obj-url="modelUrls.objUrl"
                  :mtl-url="modelUrls.mtlUrl"
                  :texture-url="modelUrls.textureUrl"
                />
              </div>
            </div>
          </div>

          <!-- 无任务提示 -->
          <div v-if="!currentTask" class="card mb-4">
            <div class="card-body text-center py-5">
              <i class="bi bi-box-seam display-1 text-muted"></i>
              <h3 class="mt-4">暂无重建任务</h3>
              <p class="text-muted">
                请上传图片或从任务列表中选择一个任务
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useStore } from '@/utils/storeCompat'
import { useRoute, useRouter } from 'vue-router'
import ModelViewer from '@/components/model/ModelViewer.vue'
import ReconstructionStatus from '@/components/model/ReconstructionStatus.vue'
import ResultFilesViewer from '@/components/model/ResultFilesViewer.vue'
import { getPictureById } from '@/api/picture'

// 导入样式
import '@/assets/css/reconstruction.css'

export default {
  name: 'ReconstructionView',
  components: {
    ModelViewer,
    ReconstructionStatus,
    ResultFilesViewer
  },
  setup() {
    const store = useStore()
    const route = useRoute()
    const router = useRouter()
    const fileInput = ref(null)
    const selectedFile = ref(null)
    const fileSelected = ref(false)
    const modelName = ref('')
    const modelCategory = ref('')
    const sourceImageId = ref(route.params.imageId || route.query.imageId)
    const sourceImage = ref(null)
    const loadingSourceImage = ref(false)
    const largeModelViewer = ref(false)
    const activeTab = ref('upload')

    // 从Vuex获取状态
    const currentTask = computed(() => store.getters['reconstruction/currentTask'])
    const taskList = computed(() => store.getters['reconstruction/taskList'])
    const taskProgress = computed(() => store.getters['reconstruction/taskProgress'])
    const taskLogs = computed(() => store.getters['reconstruction/taskLogs'])
    const modelUrls = computed(() => store.getters['reconstruction/modelUrls'])
    const isLoading = computed(() => store.getters['reconstruction/isLoading'])
    const isUploading = computed(() => store.getters['reconstruction/isUploading'])
    const hasModelUrls = computed(() => store.getters['reconstruction/hasModelUrls'])

    // 加载源图片信息
    const loadSourceImage = async () => {
      if (!sourceImageId.value) return

      loadingSourceImage.value = true
      try {
        // 调用API获取图片信息
        const response = await getPictureById(sourceImageId.value)

        if (response.code === 0 && response.data) {
          // 设置源图片数据
          sourceImage.value = response.data

          // 预填充模型名称
          if (!modelName.value && sourceImage.value.name) {
            modelName.value = `${sourceImage.value.name}模型`
          }

          console.log('成功加载源图片:', sourceImage.value)

          // 如果是从图片库跳转过来的，自动创建重建任务
          if (route.query.autoCreate === 'true') {
            // 等待图片加载完成后自动创建任务
            setTimeout(() => {
              createTaskFromSourceImage()
            }, 500)
          }
        } else {
          throw new Error(response.message || '获取图片信息失败')
        }
      } catch (error) {
        console.error('加载源图片失败:', error)
        alert('加载图片信息失败: ' + error.message)
      } finally {
        loadingSourceImage.value = false
      }
    }

    // 触发文件选择
    const triggerFileInput = () => {
      fileInput.value.click()
    }

    // 处理文件选择
    const handleFileChange = (event) => {
      const files = event.target.files
      if (files.length > 0) {
        selectedFile.value = files[0]
        fileSelected.value = true

        // 预填充模型名称
        const fileName = selectedFile.value.name.split('.')[0]
        modelName.value = `${fileName}模型`
      }
    }

    // 处理文件拖放
    const handleFileDrop = (event) => {
      const files = event.dataTransfer.files
      if (files.length > 0) {
        selectedFile.value = files[0]
        fileSelected.value = true

        // 预填充模型名称
        const fileName = selectedFile.value.name.split('.')[0]
        modelName.value = `${fileName}模型`
      }
    }

    // 移除已选择的文件
    const removeSelectedFile = () => {
      selectedFile.value = null
      fileSelected.value = false
      fileInput.value.value = ''
      modelName.value = ''
    }

    // 上传图片并创建任务
    const handleUpload = async () => {
      if (!fileSelected.value || !selectedFile.value) {
        alert('请先选择图片')
        return
      }

      try {
        // 准备元数据
        const metadata = {
          name: selectedFile.value.name.split('.')[0],
          category: modelCategory.value || undefined
        }

        // 调用Vuex action上传图片并创建任务
        await store.dispatch('reconstruction/uploadAndCreateTask', {
          file: selectedFile.value,
          metadata: {
            ...metadata,
            name: modelName.value || metadata.name
          }
        })

        // 清空选择的文件
        removeSelectedFile()

        // 切换到结果标签页（移动端）
        activeTab.value = 'result'

        // 显示成功消息
        alert('重建任务创建成功！')
      } catch (error) {
        console.error('上传图片失败:', error)
        alert('上传图片失败: ' + error.message)
      }
    }

    // 从现有图片创建重建任务
    const createTaskFromSourceImage = async () => {
      if (!sourceImage.value) {
        alert('请先选择图片')
        return
      }

      try {
        // 准备数据
        const data = {
          imageId: sourceImage.value.id,
          name: modelName.value || undefined
        }

        // 调用Vuex action创建任务
        await store.dispatch('reconstruction/createTaskFromImage', data)

        // 切换到结果标签页（移动端）
        activeTab.value = 'result'

        // 显示成功消息
        alert('重建任务创建成功！')
      } catch (error) {
        console.error('创建重建任务失败:', error)
        alert('创建重建任务失败: ' + error.message)
      }
    }

    // 加载任务列表
    const loadTaskList = async () => {
      try {
        await store.dispatch('reconstruction/fetchTaskList')
      } catch (error) {
        console.error('加载任务列表失败:', error)
        alert('加载任务列表失败: ' + error.message)
      }
    }

    // 选择任务
    const selectTask = (task) => {
      store.dispatch('reconstruction/selectTask', task)

      // 切换到结果标签页（移动端）
      activeTab.value = 'result'
    }

    // 清空任务日志
    const clearTaskLogs = () => {
      store.dispatch('reconstruction/clearTaskLogs')
    }

    // 切换模型查看器大小
    const toggleModelViewerSize = () => {
      largeModelViewer.value = !largeModelViewer.value
    }

    // 格式化文件大小
    const formatFileSize = (bytes) => {
      if (bytes < 1024) return bytes + ' B'
      else if (bytes < 1048576) return (bytes / 1024).toFixed(2) + ' KB'
      else return (bytes / 1048576).toFixed(2) + ' MB'
    }

    // 格式化日期
    const formatDate = (dateStr) => {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return date.toLocaleString()
    }

    // 获取状态徽章样式
    const getStatusBadgeClass = (status) => {
      switch (status) {
        case 'PENDING': return 'badge bg-warning text-dark'
        case 'PROCESSING': return 'badge bg-primary'
        case 'COMPLETED': return 'badge bg-success'
        case 'FAILED': return 'badge bg-danger'
        default: return 'badge bg-secondary'
      }
    }

    // 获取状态头部样式
    const getStatusHeaderClass = (status) => {
      switch (status) {
        case 'PENDING': return 'bg-warning text-white'
        case 'PROCESSING': return 'bg-primary text-white'
        case 'COMPLETED': return 'bg-success text-white'
        case 'FAILED': return 'bg-danger text-white'
        default: return 'bg-secondary text-white'
      }
    }

    // 获取状态文本
    const getStatusText = (status) => {
      switch (status) {
        case 'PENDING': return '等待中'
        case 'PROCESSING': return '处理中'
        case 'COMPLETED': return '已完成'
        case 'FAILED': return '失败'
        default: return '未知'
      }
    }

    // 获取状态图标
    const getStatusIcon = (status) => {
      switch (status) {
        case 'PENDING': return 'bi bi-hourglass-split'
        case 'PROCESSING': return 'bi bi-arrow-repeat'
        case 'COMPLETED': return 'bi bi-check-circle'
        case 'FAILED': return 'bi bi-exclamation-triangle'
        default: return 'bi bi-question-circle'
      }
    }

    // 组件挂载时
    onMounted(() => {
      // 加载源图片信息
      if (sourceImageId.value) {
        loadSourceImage()
      }

      // 加载任务列表
      loadTaskList()
    })

    // 组件卸载前
    onBeforeUnmount(() => {
      // 断开SSE连接
      store.dispatch('reconstruction/disconnectSse')
    })

    // 监听路由变化
    watch(
      () => route.params.imageId || route.query.imageId,
      (newId) => {
        if (newId && newId !== sourceImageId.value) {
          sourceImageId.value = newId
          loadSourceImage()
        }
      }
    )

    return {
      fileInput,
      selectedFile,
      fileSelected,
      modelName,
      modelCategory,
      sourceImageId,
      sourceImage,
      currentTask,
      taskList,
      taskProgress,
      taskLogs,
      modelUrls,
      largeModelViewer,
      activeTab,
      isLoading,
      isUploading,
      hasModelUrls,
      triggerFileInput,
      handleFileChange,
      handleFileDrop,
      removeSelectedFile,
      handleUpload,
      createTaskFromSourceImage,
      loadTaskList,
      selectTask,
      clearTaskLogs,
      toggleModelViewerSize,
      formatFileSize,
      formatDate,
      getStatusBadgeClass,
      getStatusHeaderClass,
      getStatusText,
      getStatusIcon
    }
  }
}
</script>

<style scoped>
.reconstruction-view {
  min-height: calc(100vh - 70px);
}

.upload-card {
  transition: all 0.3s ease;
}

.upload-area {
  border: 2px dashed #ccc;
  border-radius: 8px;
  padding: 30px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-area:hover {
  border-color: #007bff;
  background-color: rgba(0, 123, 255, 0.05);
}

.upload-placeholder {
  color: #6c757d;
}

.selected-image-container {
  text-align: center;
}

.selected-image img {
  max-height: 200px;
  object-fit: contain;
}

.task-list-card {
  max-height: 400px;
  overflow-y: auto;
}

.model-viewer-large {
  height: 600px;
}

/* 移动端样式 */
@media (max-width: 767.98px) {
  .task-list-card {
    max-height: 300px;
  }

  .model-viewer-large {
    height: 400px;
  }
}
</style>
