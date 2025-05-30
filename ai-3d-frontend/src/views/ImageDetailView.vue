<template>
  <div class="image-detail-view">
    <div class="container mt-4">
      <div class="row mb-4">
        <div class="col-12">
          <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
              <li class="breadcrumb-item"><router-link to="/images">图片库</router-link></li>
              <li class="breadcrumb-item active" aria-current="page">图片详情</li>
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
          <p class="mt-3">正在加载图片详情...</p>
        </div>
      </div>

      <!-- 图片不存在 -->
      <div v-else-if="!image" class="row">
        <div class="col-12 text-center py-5">
          <div class="alert alert-warning">
            <i class="bi bi-exclamation-triangle me-2"></i>
            找不到该图片，可能已被删除或您没有访问权限。
          </div>
          <router-link to="/images" class="btn btn-primary mt-3">
            <i class="bi bi-arrow-left me-2"></i> 返回图片库
          </router-link>
        </div>
      </div>

      <!-- 图片详情 -->
      <div v-else class="row">
        <!-- 图片信息 -->
        <div class="col-md-8">
          <div class="card mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
              <h5 class="mb-0">{{ image.name || '未命名图片' }}</h5>
              <div>
                <button class="btn btn-sm btn-outline-primary me-2" @click="downloadImage">
                  <i class="bi bi-download me-1"></i> 下载
                </button>
                <button class="btn btn-sm btn-outline-danger" @click="confirmDeleteImage" v-if="isAdmin">
                  <i class="bi bi-trash me-1"></i> 删除
                </button>
              </div>
            </div>
            <div class="card-body p-0">
              <div class="image-container">
                <img :src="image.url" class="img-fluid" alt="图片详情">
              </div>
            </div>
          </div>
        </div>

        <!-- 图片元数据和操作 -->
        <div class="col-md-4">
          <!-- 图片信息卡片 -->
          <div class="card mb-4">
            <div class="card-header">
              <h5 class="mb-0">图片信息</h5>
            </div>
            <div class="card-body">
              <ul class="list-group list-group-flush">
                <li class="list-group-item d-flex justify-content-between">
                  <span>ID</span>
                  <span class="text-muted">{{ image.id }}</span>
                </li>
                <li class="list-group-item d-flex justify-content-between">
                  <span>上传时间</span>
                  <span class="text-muted">{{ formatDate(image.createTime) }}</span>
                </li>
                <li class="list-group-item d-flex justify-content-between">
                  <span>尺寸</span>
                  <span class="text-muted">{{ image.width || '未知' }} x {{ image.height || '未知' }}</span>
                </li>
                <li class="list-group-item d-flex justify-content-between">
                  <span>3D模型状态</span>
                  <span v-if="loadingModel" class="badge bg-warning">加载中...</span>
                  <span v-else-if="hasModel" class="badge bg-success">已创建</span>
                  <span v-else class="badge bg-secondary">未创建</span>
                </li>
              </ul>
            </div>
          </div>

          <!-- 3D模型操作卡片 -->
          <div class="card mb-4">
            <div class="card-header bg-primary text-white">
              <h5 class="mb-0">
                <i class="bi bi-box me-2"></i>
                3D模型
              </h5>
            </div>
            <div class="card-body">
              <!-- 加载中 -->
              <div v-if="loadingModel" class="text-center py-3">
                <div class="spinner-border text-primary" role="status">
                  <span class="visually-hidden">加载中...</span>
                </div>
                <p class="mt-2">正在加载模型信息...</p>
              </div>

              <!-- 有关联模型 -->
              <div v-else-if="hasModel" class="text-center">
                <div class="model-info mb-3">
                  <h6 class="mb-2">{{ relatedModel.name || '未命名模型' }}</h6>
                  <p v-if="relatedModel.introduction" class="text-muted small mb-2">
                    {{ relatedModel.introduction }}
                  </p>
                  <div class="d-flex justify-content-center gap-2 mb-2">
                    <span v-if="relatedModel.category" class="badge bg-secondary">{{ relatedModel.category }}</span>
                    <span v-if="relatedModel.modelFormat" class="badge bg-info">{{ relatedModel.modelFormat }}</span>
                    <span class="badge bg-success">已完成</span>
                  </div>
                  <p class="text-muted small">
                    创建于: {{ formatDate(relatedModel.createTime) }}
                  </p>
                </div>
                <button class="btn btn-primary w-100" @click="viewModel">
                  <i class="bi bi-box me-2"></i> 查看3D模型
                </button>
              </div>

              <!-- 无关联模型 -->
              <div v-else class="text-center">
                <p class="text-muted mb-3">
                  <i class="bi bi-info-circle me-2"></i>
                  该图片尚未创建3D模型
                </p>
                <button class="btn btn-success w-100" @click="createModel" v-if="isAdmin">
                  <i class="bi bi-box me-2"></i> 创建3D模型
                </button>
                <p class="small text-muted mt-2" v-if="!isAdmin">
                  只有管理员可以创建3D模型
                </p>
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
          <div class="modal-body" v-if="image">
            <p>确定要删除图片 "{{ image.name || '未命名图片' }}" 吗？</p>
            <p class="text-danger">此操作不可撤销。如果该图片已创建3D模型，相关模型也将被删除。</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-danger" @click="deleteImage">删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useStore } from '@/utils/storeCompat'
import { useRouter, useRoute } from 'vue-router'
import { Modal } from 'bootstrap'
import { cleanupModalState } from '@/utils/modalHelper'
import {
  getPictureById,
  updatePicture,
  deletePicture
} from '@/api/picture'
import { getModelByImageId } from '@/api/model'

export default {
  name: 'ImageDetailView',
  setup() {
    const store = useStore()
    const router = useRouter()
    const route = useRoute()
    const isAdmin = computed(() => store.getters['user/isAdmin'])

    // 状态变量
    const loading = ref(true)
    const image = ref(null)
    const deleteConfirmModal = ref(null)
    const relatedModel = ref(null)
    const loadingModel = ref(false)

    // 获取图片ID
    const imageId = route.params.id

    // 计算属性
    const hasModel = computed(() => !!relatedModel.value)


    // 加载图片详情
    const loadImageDetail = async () => {
      if (!imageId) {
        router.push('/images')
        return
      }

      loading.value = true
      try {
        const response = await getPictureById(imageId)

        if (response.code === 0 && response.data) {
          image.value = response.data
        } else {
          console.error('获取图片详情失败:', response.message)
          image.value = null
        }
      } catch (error) {
        console.error('加载图片详情失败:', error)
        image.value = null
      } finally {
        loading.value = false

        // 加载关联模型
        loadRelatedModel()
      }
    }

    // 加载关联模型
    const loadRelatedModel = async () => {
      if (!imageId) return

      loadingModel.value = true
      try {
        // 直接使用apiClient调用正确的API路径
        const { apiClient } = await import('@/api/apiClient')
        const response = await apiClient.get(`/model/image/${imageId}`)

        if (response.data && response.data.code === 0 && response.data.data) {
          relatedModel.value = response.data.data
          console.log('关联模型:', relatedModel.value)
        } else {
          console.log('没有找到关联模型:', response.data?.message || '未知原因')
          relatedModel.value = null
        }
      } catch (error) {
        console.error('加载关联模型失败:', error)
        relatedModel.value = null
      } finally {
        loadingModel.value = false
      }
    }

    // 下载图片
    const downloadImage = () => {
      if (!image.value || !image.value.url) return

      // 创建一个临时链接并触发下载
      const link = document.createElement('a')
      link.href = image.value.url
      link.download = image.value.name || `image-${image.value.id}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }

    // 查看3D模型
    const viewModel = () => {
      if (relatedModel.value && relatedModel.value.id) {
        router.push(`/models/${relatedModel.value.id}`)
      }
    }

    // 创建3D模型
    const createModel = () => {
      router.push({
        path: '/reconstruction',
        query: {
          imageId: imageId,
          autoCreate: 'true'
        }
      })
    }

    // 确认删除图片
    const confirmDeleteImage = () => {
      // 显示模态框，禁用背景遮罩
      deleteConfirmModal.value = new Modal(document.getElementById('deleteConfirmModal'), {
        backdrop: false,
        keyboard: true
      })
      deleteConfirmModal.value.show()
    }

    // 删除图片
    const deleteImage = async () => {
      if (!image.value) return

      try {
        const response = await deletePicture(image.value.id)

        if (response.code === 0) {
          // 关闭模态框
          if (deleteConfirmModal.value) {
            deleteConfirmModal.value.hide()
            // 清除模态框背景
            setTimeout(cleanupModals, 100)
          }

          // 显示成功消息
          alert('图片删除成功！')

          // 返回图片库
          router.push('/images')
        } else {
          alert('删除失败: ' + (response.message || '未知错误'))
        }
      } catch (error) {
        console.error('删除图片失败:', error)
        alert('删除图片失败: ' + (error.message || '未知错误'))
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
      loadImageDetail()

      // 初始化时清除模态框背景
      cleanupModalState()
    })

    return {
      loading,
      loadingModel,
      image,
      relatedModel,
      isAdmin,
      hasModel,

      downloadImage,
      viewModel,
      createModel,
      confirmDeleteImage,
      deleteImage,
      formatDate
    }
  }
}
</script>

<style scoped>
.image-container {
  max-height: 600px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-container img {
  max-width: 100%;
  max-height: 600px;
  object-fit: contain;
}

.list-group-item {
  padding: 0.75rem 1.25rem;
}
</style>
