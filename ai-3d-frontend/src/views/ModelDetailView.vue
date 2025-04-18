<template>
  <div class="model-detail-view">
    <div class="container mt-4">
      <div class="row mb-4">
        <div class="col-12">
          <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
              <li class="breadcrumb-item"><router-link to="/models">3D模型库</router-link></li>
              <li class="breadcrumb-item active" aria-current="page">模型详情</li>
            </ol>
          </nav>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="row">
        <div class="col-12 text-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">加载中...</span>
          </div>
          <p class="mt-3">正在加载模型详情...</p>
        </div>
      </div>

      <!-- 模型不存在 -->
      <div v-else-if="!model" class="row">
        <div class="col-12 text-center py-5">
          <div class="alert alert-warning">
            <i class="bi bi-exclamation-triangle me-2"></i>
            找不到该模型，可能已被删除或您没有访问权限。
          </div>
          <router-link to="/models" class="btn btn-primary mt-3">
            <i class="bi bi-arrow-left me-2"></i> 返回模型库
          </router-link>
        </div>
      </div>

      <!-- 模型详情 -->
      <div v-else class="row">
        <!-- 模型标题 -->
        <div class="col-12 mb-4">
          <div class="d-flex justify-content-between align-items-center">
            <h2>{{ model.name || '未命名模型' }}</h2>
            <div>
              <button class="btn btn-outline-primary me-2" @click="downloadModel" :disabled="model.status !== 'completed'">
                <i class="bi bi-download me-1"></i> 下载模型
              </button>
              <button class="btn btn-outline-danger" @click="confirmDeleteModel" v-if="isAdmin">
                <i class="bi bi-trash me-1"></i> 删除
              </button>
            </div>
          </div>
          <div class="model-status mt-2">
            <span class="badge" :class="model.status === 'completed' ? 'bg-success' : 'bg-primary'">
              {{ model.status === 'completed' ? '已完成' : '处理中' }}
            </span>
            <span class="text-muted ms-2">创建于 {{ formatDate(model.createTime) }}</span>
          </div>
        </div>

        <!-- 3D模型预览 -->
        <div class="col-md-8 mb-4">
          <div class="card">
            <div class="card-header bg-primary text-white">
              <h5 class="mb-0"><i class="bi bi-cube me-2"></i>3D模型预览</h5>
            </div>
            <div class="card-body p-0">
              <div v-if="model.status === 'completed'" class="model-viewer-container">
                <ModelViewer
                  :objUrl="model.objUrl"
                  :mtlUrl="model.mtlUrl"
                  :textureUrl="model.textureUrl"
                />
              </div>
              <div v-else class="model-processing-placeholder">
                <div class="text-center py-5">
                  <div class="spinner-border text-primary mb-3" role="status">
                    <span class="visually-hidden">处理中...</span>
                  </div>
                  <h5>模型正在处理中</h5>
                  <p class="text-muted">请稍后再查看，处理可能需要几分钟时间</p>
                </div>
              </div>
            </div>
            <div class="card-footer bg-light" v-if="model.status === 'completed'">
              <div class="small text-muted">
                <i class="bi bi-mouse me-2"></i>拖动鼠标旋转模型 | 滚轮缩放 | 右键平移
              </div>
            </div>
          </div>
        </div>

        <!-- 模型信息和源图片 -->
        <div class="col-md-4">
          <!-- 模型信息卡片 -->
          <div class="card mb-4">
            <div class="card-header">
              <h5 class="mb-0">模型信息</h5>
            </div>
            <div class="card-body">
              <ul class="list-group list-group-flush">
                <li class="list-group-item d-flex justify-content-between">
                  <span>ID</span>
                  <span class="text-muted">{{ model.id }}</span>
                </li>
                <li class="list-group-item d-flex justify-content-between">
                  <span>任务ID</span>
                  <span class="text-muted">{{ model.taskId }}</span>
                </li>
                <li class="list-group-item d-flex justify-content-between">
                  <span>创建时间</span>
                  <span class="text-muted">{{ formatDate(model.createTime) }}</span>
                </li>
                <li class="list-group-item d-flex justify-content-between">
                  <span>状态</span>
                  <span class="badge" :class="model.status === 'completed' ? 'bg-success' : 'bg-primary'">
                    {{ model.status === 'completed' ? '已完成' : '处理中' }}
                  </span>
                </li>
              </ul>
            </div>
          </div>

          <!-- 源图片卡片 -->
          <div class="card mb-4" v-if="model.sourceImageId">
            <div class="card-header">
              <h5 class="mb-0">源图片</h5>
            </div>
            <div class="card-body p-0">
              <div class="source-image-container">
                <img :src="model.sourceImageUrl" class="img-fluid" alt="源图片">
              </div>
              <div class="p-3">
                <button class="btn btn-outline-primary w-100" @click="viewSourceImage">
                  <i class="bi bi-image me-1"></i> 查看源图片详情
                </button>
              </div>
            </div>
          </div>

          <!-- 下载文件卡片 -->
          <div class="card mb-4" v-if="model.status === 'completed'">
            <div class="card-header">
              <h5 class="mb-0">下载文件</h5>
            </div>
            <div class="card-body">
              <div class="d-grid gap-2">
                <a :href="model.outputZipUrl" class="btn btn-primary" download>
                  <i class="bi bi-file-earmark-zip me-1"></i> 下载完整ZIP包
                </a>
                <a :href="model.objUrl" class="btn btn-outline-primary" download>
                  <i class="bi bi-box me-1"></i> OBJ模型
                </a>
                <a :href="model.mtlUrl" class="btn btn-outline-primary" download>
                  <i class="bi bi-palette me-1"></i> MTL材质
                </a>
                <a :href="model.textureUrl" class="btn btn-outline-primary" download>
                  <i class="bi bi-image me-1"></i> 纹理图像
                </a>
              </div>
              <div class="alert alert-light mt-3 small">
                <i class="bi bi-info-circle me-2"></i>
                提示：您可以在Blender、Maya等软件中打开OBJ文件进行进一步编辑。
              </div>
            </div>
          </div>
        </div>

        <!-- 纹理和深度图像 -->
        <div class="col-12 mb-4" v-if="model.status === 'completed'">
          <div class="card">
            <div class="card-header">
              <h5 class="mb-0">生成的图像</h5>
            </div>
            <div class="card-body">
              <div class="row">
                <div class="col-md-6 mb-3" v-if="model.pixelImagesUrl">
                  <div class="card h-100">
                    <div class="card-header bg-light">纹理图像</div>
                    <div class="card-body text-center p-2">
                      <img :src="model.pixelImagesUrl" class="img-fluid mb-2 result-image" alt="纹理图像">
                      <a :href="model.pixelImagesUrl" class="btn btn-sm btn-primary" download>
                        <i class="bi bi-download"></i> 下载纹理
                      </a>
                    </div>
                  </div>
                </div>
                <div class="col-md-6 mb-3" v-if="model.xyzImagesUrl">
                  <div class="card h-100">
                    <div class="card-header bg-light">深度图像</div>
                    <div class="card-body text-center p-2">
                      <img :src="model.xyzImagesUrl" class="img-fluid mb-2 result-image" alt="深度图像">
                      <a :href="model.xyzImagesUrl" class="btn btn-sm btn-primary" download>
                        <i class="bi bi-download"></i> 下载深度图
                      </a>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 删除确认模态框 -->
    <div class="modal fade" id="deleteConfirmModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">确认删除</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body" v-if="model">
            <p>确定要删除模型 "{{ model.name || '未命名模型' }}" 吗？</p>
            <p class="text-danger">此操作不可撤销。</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-danger" @click="deleteModel">删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'
import { Modal } from 'bootstrap'
import ModelViewer from '@/components/ModelViewer.vue'
import axios from 'axios'

// API URL
const API_URL = import.meta.env.VITE_API_URL || ''

export default {
  name: 'ModelDetailView',
  components: {
    ModelViewer
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const route = useRoute()
    const isAdmin = computed(() => store.getters['user/isAdmin'])
    
    // 状态变量
    const loading = ref(true)
    const model = ref(null)
    const deleteConfirmModal = ref(null)
    
    // 获取模型ID
    const modelId = route.params.id
    
    // 模拟模型数据 - 实际应用中应该从API获取
    const mockModels = [
      {
        id: 'm1',
        name: '显微镜模型',
        status: 'completed',
        pixelImagesUrl: 'https://via.placeholder.com/800x600?text=显微镜纹理',
        xyzImagesUrl: 'https://via.placeholder.com/800x600?text=显微镜深度',
        objUrl: 'https://example.com/models/microscope.obj',
        mtlUrl: 'https://example.com/models/microscope.mtl',
        textureUrl: 'https://example.com/models/microscope.png',
        outputZipUrl: 'https://example.com/models/microscope.zip',
        sourceImageId: '1',
        sourceImageUrl: 'https://via.placeholder.com/800x600?text=显微镜',
        taskId: 'abc123def456',
        createTime: '2023-04-16T14:20:00Z'
      },
      {
        id: 'm2',
        name: '试管架模型',
        status: 'completed',
        pixelImagesUrl: 'https://via.placeholder.com/800x600?text=试管架纹理',
        xyzImagesUrl: 'https://via.placeholder.com/800x600?text=试管架深度',
        objUrl: 'https://example.com/models/test-tube-rack.obj',
        mtlUrl: 'https://example.com/models/test-tube-rack.mtl',
        textureUrl: 'https://example.com/models/test-tube-rack.png',
        outputZipUrl: 'https://example.com/models/test-tube-rack.zip',
        sourceImageId: '2',
        sourceImageUrl: 'https://via.placeholder.com/800x600?text=试管架',
        taskId: 'def456ghi789',
        createTime: '2023-04-17T09:15:00Z'
      },
      {
        id: 'm3',
        name: '培养皿模型',
        status: 'processing',
        pixelImagesUrl: null,
        xyzImagesUrl: null,
        objUrl: null,
        mtlUrl: null,
        textureUrl: null,
        outputZipUrl: null,
        sourceImageId: '3',
        sourceImageUrl: 'https://via.placeholder.com/800x600?text=培养皿',
        taskId: 'xyz789abc012',
        createTime: '2023-04-18T11:45:00Z'
      }
    ]
    
    // 加载模型详情
    const loadModelDetail = async () => {
      loading.value = true
      try {
        // 实际应用中应该调用API
        // const response = await axios.get(`${API_URL}/api/models/${modelId}`, {
        //   withCredentials: true
        // })
        // model.value = response.data
        
        // 模拟API响应
        setTimeout(() => {
          model.value = mockModels.find(m => m.id === modelId) || null
          loading.value = false
        }, 500)
      } catch (error) {
        console.error('加载模型详情失败:', error)
        loading.value = false
      }
    }
    
    // 下载模型
    const downloadModel = () => {
      if (!model.value || model.value.status !== 'completed' || !model.value.outputZipUrl) {
        alert('模型还未完成生成，无法下载')
        return
      }
      
      // 实际应用中应该使用正确的URL
      window.open(model.value.outputZipUrl, '_blank')
    }
    
    // 查看源图片
    const viewSourceImage = () => {
      if (model.value && model.value.sourceImageId) {
        router.push(`/images/${model.value.sourceImageId}`)
      }
    }
    
    // 确认删除模型
    const confirmDeleteModel = () => {
      if (!deleteConfirmModal.value) {
        deleteConfirmModal.value = new Modal(document.getElementById('deleteConfirmModal'))
      }
      deleteConfirmModal.value.show()
    }
    
    // 删除模型
    const deleteModel = async () => {
      try {
        // 实际应用中应该调用API
        // await axios.delete(`${API_URL}/api/models/${modelId}`, {
        //   withCredentials: true
        // })
        
        // 模拟删除成功
        setTimeout(() => {
          // 关闭模态框
          if (deleteConfirmModal.value) {
            deleteConfirmModal.value.hide()
          }
          
          // 显示成功消息
          alert('模型删除成功！')
          
          // 返回模型库
          router.push('/models')
        }, 500)
      } catch (error) {
        console.error('删除模型失败:', error)
        alert('删除模型失败: ' + (error.response?.data?.message || '未知错误'))
      }
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return '未知时间'
      
      const date = new Date(dateString)
      const options = { 
        year: 'numeric', 
        month: 'short', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      }
      
      return date.toLocaleDateString('zh-CN', options)
    }
    
    // 生命周期钩子
    onMounted(() => {
      loadModelDetail()
    })
    
    return {
      loading,
      model,
      isAdmin,
      
      downloadModel,
      viewSourceImage,
      confirmDeleteModel,
      deleteModel,
      formatDate
    }
  }
}
</script>

<style scoped>
.model-viewer-container {
  height: 500px;
  background-color: #f8f9fa;
}

.model-processing-placeholder {
  height: 300px;
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.source-image-container {
  max-height: 300px;
  overflow: hidden;
}

.source-image-container img {
  width: 100%;
  object-fit: cover;
}

.result-image {
  max-height: 300px;
  object-fit: contain;
}

.list-group-item {
  padding: 0.75rem 1.25rem;
}

@media (max-width: 768px) {
  .model-viewer-container {
    height: 300px;
  }
}
</style>
