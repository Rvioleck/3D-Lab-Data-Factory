<template>
  <div class="reconstruction-view">
    <div class="container mt-4">
      <div class="row">
        <div class="col-md-12 text-center mb-4">
          <h1>3D重建</h1>
          <p class="lead">上传图片，生成3D模型</p>
        </div>
      </div>

      <div class="row">
        <!-- 上传区域 -->
        <div class="col-md-6">
          <div class="card mb-4">
            <div class="card-header">
              <h5>上传图片</h5>
            </div>
            <div class="card-body">
              <form @submit.prevent="handleUpload">
                <div class="mb-3">
                  <label for="imageFile" class="form-label">选择图片文件</label>
                  <input class="form-control" type="file" id="imageFile" ref="fileInput" accept="image/*">
                  <div class="form-text">支持的格式：JPG, PNG, JPEG等常见图片格式</div>
                </div>
                <div class="mb-3">
                  <div class="form-check">
                    <input class="form-check-input" type="checkbox" id="syncMode" v-model="syncMode">
                    <label class="form-check-label" for="syncMode">
                      同步模式（等待处理完成）
                    </label>
                  </div>
                </div>
                <div class="d-grid gap-2">
                  <button type="submit" class="btn btn-primary" :disabled="uploading">
                    <span v-if="uploading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                    {{ uploading ? '上传中...' : '上传并处理' }}
                  </button>
                </div>
              </form>
            </div>
          </div>

          <!-- 连接状态 -->
          <div class="card mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
              <h5 class="mb-0">WebSocket连接状态</h5>
              <button class="btn btn-sm btn-outline-primary" @click="checkConnection">
                <i class="bi bi-arrow-repeat"></i> 检查
              </button>
            </div>
            <div class="card-body">
              <div class="d-flex align-items-center">
                <div class="me-2">
                  <span class="badge" :class="connectionStatus ? 'bg-success' : 'bg-danger'">
                    {{ connectionStatus ? '已连接' : '未连接' }}
                  </span>
                </div>
                <div>
                  WebSocket服务器连接状态
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 结果区域 -->
        <div class="col-md-6">
          <div v-if="taskId" class="card mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
              <h5 class="mb-0">处理状态</h5>
              <button v-if="taskStatus === 'processing'" class="btn btn-sm btn-outline-primary" @click="checkTaskStatus">
                <i class="bi bi-arrow-repeat"></i> 刷新
              </button>
            </div>
            <div class="card-body">
              <div class="mb-3">
                <strong>任务ID:</strong> {{ taskId }}
              </div>
              <div class="mb-3">
                <strong>状态:</strong>
                <span class="badge ms-2" :class="getStatusBadgeClass">
                  {{ getStatusText }}
                </span>
              </div>
              <div v-if="taskStatus === 'success'" class="mb-3">
                <strong>文件:</strong>
                <div class="mt-2">
                  <a :href="objFileUrl" class="btn btn-sm btn-primary me-2" download>
                    <i class="bi bi-download"></i> 下载OBJ模型
                  </a>
                  <a :href="mtlFileUrl" class="btn btn-sm btn-primary me-2" download>
                    <i class="bi bi-download"></i> 下载MTL材质
                  </a>
                  <a :href="textureFileUrl" class="btn btn-sm btn-primary" download>
                    <i class="bi bi-download"></i> 下载纹理图像
                  </a>
                </div>
              </div>
            </div>
          </div>

          <!-- 3D模型预览 -->
          <div v-if="taskStatus === 'success' && objFileUrl && mtlFileUrl && textureFileUrl" class="card mb-4">
            <div class="card-header">
              <h5>3D模型预览</h5>
            </div>
            <div class="card-body p-0">
              <ModelViewer 
                :objUrl="objFileUrl" 
                :mtlUrl="mtlFileUrl" 
                :textureUrl="textureFileUrl" 
              />
            </div>
          </div>

          <!-- 图片预览 -->
          <div v-if="taskStatus === 'success' && textureFileUrl" class="card mb-4">
            <div class="card-header">
              <h5>纹理图像预览</h5>
            </div>
            <div class="card-body text-center">
              <img :src="textureFileUrl" class="img-fluid" alt="纹理图像">
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import ModelViewer from '@/components/ModelViewer.vue'
import { 
  uploadImage, 
  uploadImageSync, 
  getTaskStatus, 
  getResultFileUrl, 
  checkConnectionStatus 
} from '@/api/reconstruction'

export default {
  name: 'ReconstructionView',
  components: {
    ModelViewer
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const fileInput = ref(null)
    const syncMode = ref(false)
    const uploading = ref(false)
    const connectionStatus = ref(false)
    const taskId = ref('')
    const taskStatus = ref('')
    const objFileUrl = ref('')
    const mtlFileUrl = ref('')
    const textureFileUrl = ref('')

    // 计算属性
    const getStatusBadgeClass = computed(() => {
      switch (taskStatus.value) {
        case 'processing':
          return 'bg-warning'
        case 'success':
          return 'bg-success'
        case 'failed':
          return 'bg-danger'
        default:
          return 'bg-secondary'
      }
    })

    const getStatusText = computed(() => {
      switch (taskStatus.value) {
        case 'processing':
          return '处理中'
        case 'success':
          return '成功'
        case 'failed':
          return '失败'
        default:
          return '未知'
      }
    })

    // 检查用户是否已登录
    const checkLogin = () => {
      const user = store.state.user.currentUser
      if (!user) {
        router.push('/login')
        return false
      }
      
      // 检查用户是否为管理员
      if (user.userRole !== 'admin') {
        alert('只有管理员可以使用此功能')
        router.push('/')
        return false
      }
      
      return true
    }

    // 检查WebSocket连接状态
    const checkConnection = async () => {
      try {
        const response = await checkConnectionStatus()
        connectionStatus.value = response.data
      } catch (error) {
        console.error('检查连接状态失败:', error)
        connectionStatus.value = false
      }
    }

    // 上传图片
    const handleUpload = async () => {
      if (!checkLogin()) return
      
      const file = fileInput.value.files[0]
      if (!file) {
        alert('请选择一个图片文件')
        return
      }

      if (!file.type.startsWith('image/')) {
        alert('请选择有效的图片文件')
        return
      }

      uploading.value = true
      
      try {
        let response
        
        if (syncMode.value) {
          // 同步模式
          response = await uploadImageSync(file)
        } else {
          // 异步模式
          response = await uploadImage(file)
        }
        
        if (response.code === 0) {
          taskId.value = response.data.taskId
          taskStatus.value = response.data.status
          
          // 如果状态是成功，设置文件URL
          if (response.data.status === 'success') {
            setFileUrls(response.data.taskId)
          }
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

    // 检查任务状态
    const checkTaskStatus = async () => {
      if (!taskId.value) return
      
      try {
        const response = await getTaskStatus(taskId.value)
        
        if (response.code === 0) {
          taskStatus.value = response.data.status
          
          // 如果状态是成功，设置文件URL
          if (response.data.status === 'success') {
            setFileUrls(taskId.value)
          }
        } else {
          console.error('获取任务状态失败:', response.message)
        }
      } catch (error) {
        console.error('获取任务状态失败:', error)
      }
    }

    // 设置文件URL
    const setFileUrls = (id) => {
      objFileUrl.value = getResultFileUrl(id, 'model.obj')
      mtlFileUrl.value = getResultFileUrl(id, 'model.mtl')
      textureFileUrl.value = getResultFileUrl(id, 'pixel_images.png')
    }

    // 组件挂载时检查连接状态
    onMounted(() => {
      checkConnection()
      checkLogin()
    })

    return {
      fileInput,
      syncMode,
      uploading,
      connectionStatus,
      taskId,
      taskStatus,
      objFileUrl,
      mtlFileUrl,
      textureFileUrl,
      getStatusBadgeClass,
      getStatusText,
      handleUpload,
      checkConnection,
      checkTaskStatus
    }
  }
}
</script>

<style scoped>
.card {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.card-header {
  background-color: #f8f9fa;
  border-bottom: 1px solid rgba(0, 0, 0, 0.125);
}

.card-body img {
  max-height: 300px;
  object-fit: contain;
}
</style>
