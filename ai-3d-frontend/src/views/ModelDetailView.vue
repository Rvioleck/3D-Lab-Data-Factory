<template>
  <div class="model-detail-view">
    <div class="container py-4">
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">加载中...</span>
        </div>
        <p class="mt-3">正在加载模型信息...</p>
      </div>

      <div v-else-if="model" class="row">
        <!-- 模型信息卡片 -->
        <div class="col-md-4 mb-4">
          <div class="card">
            <div class="card-header bg-primary text-white">
              <h5 class="mb-0">
                <i class="bi bi-info-circle me-2"></i> 模型信息
              </h5>
            </div>
            <div class="card-body">
              <h4 class="mb-3">{{ model.name || '未命名模型' }}</h4>

              <div class="model-meta mb-3">
                <div class="badges mb-2">
                  <span v-if="model.category" class="badge bg-secondary me-1">{{ model.category }}</span>
                  <span v-if="model.modelFormat" class="badge bg-info me-1">{{ model.modelFormat }}</span>
                  <span class="badge bg-success">{{ model.status }}</span>
                </div>
                <p v-if="model.introduction" class="text-muted">{{ model.introduction }}</p>
                <p class="text-muted small">
                  <i class="bi bi-calendar-event me-1"></i> 创建于: {{ formatDate(model.createTime) }}
                </p>
              </div>

              <!-- 源图片 -->
              <div v-if="model.sourceImageUrl" class="source-image-container mb-3">
                <h6>源图片:</h6>
                <div class="source-image">
                  <img :src="model.sourceImageUrl" alt="源图片" class="img-fluid rounded">
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="d-grid gap-2">
                <button class="btn btn-danger" @click="confirmDeleteModel">
                  <i class="bi bi-trash me-1"></i> 删除模型
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 模型查看器和结果文件 -->
        <div class="col-md-8">
          <!-- 3D模型查看器 -->
          <div class="card mb-4">
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
                  :obj-url="model.objFileUrl"
                  :mtl-url="model.mtlFileUrl"
                  :texture-url="model.textureImageUrl"
                />
              </div>
            </div>
          </div>

          <!-- 结果文件查看器 -->
          <div class="card mb-4">
            <div class="card-header bg-success text-white">
              <h5 class="mb-0">
                <i class="bi bi-file-earmark-richtext me-2"></i> 结果文件
              </h5>
            </div>
            <div class="card-body">
              <ResultFilesViewer
                :pixel-images-url="model.pixelImagesUrl"
                :xyz-images-url="model.xyzImagesUrl"
                :obj-file-url="model.objFileUrl"
                :mtl-file-url="model.mtlFileUrl"
                :texture-image-url="model.textureImageUrl"
                :task-id="model.taskId"
              />
            </div>
          </div>
        </div>
      </div>

      <div v-else class="alert alert-warning">
        <i class="bi bi-exclamation-triangle me-2"></i>
        模型不存在或已被删除
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

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Modal } from 'bootstrap'
import { cleanupModalState } from '@/utils/modalHelper'
import ModelViewer from '@/components/model/ModelViewer.vue'
import ResultFilesViewer from '@/components/model/ResultFilesViewer.vue'
import { getModelById } from '@/api/model'

const API_URL = import.meta.env.VITE_API_URL || ''

const loading = ref(false)
const model = ref(null)
const route = useRoute()
const router = useRouter()
const largeModelViewer = ref(false)
const deleteConfirmModal = ref(null)

const modelId = computed(() => route.params.id)

// 加载模型详情
const loadModelDetail = async () => {
  loading.value = true
  try {
    const response = await getModelById(modelId.value)
    if (response.code === 0 && response.data) {
      model.value = {
        ...response.data,
        sourceImageUrl: response.data.sourceImageId
          ? `${API_URL}/picture/${response.data.sourceImageId}`
          : null
      }
      console.log('加载模型成功:', model.value)
    } else {
      console.error('获取模型失败:', response.message)
      model.value = null
    }
  } catch (error) {
    console.error('加载模型详情失败:', error)
    model.value = null
  } finally {
    loading.value = false
  }
}

// 切换模型查看器大小
const toggleModelViewerSize = () => {
  largeModelViewer.value = !largeModelViewer.value
}

// 确认删除模型
const confirmDeleteModel = () => {
  deleteConfirmModal.value = new Modal(document.getElementById('deleteConfirmModal'), {
    backdrop: false,
    keyboard: true
  })
  deleteConfirmModal.value.show()
}

// 删除模型
const deleteModel = async () => {
  try {
    // 使用model API而不是reconstruction API
    const { apiClient } = await import('@/api/apiClient')
    const response = await apiClient.post('/model/delete', { id: modelId.value })

    if (response.data && response.data.code === 0) {
      // 关闭模态框
      if (deleteConfirmModal.value) {
        deleteConfirmModal.value.hide()
      }

      // 显示成功消息
      alert('模型删除成功！')

      // 返回模型列表页面
      router.push('/models')
    } else {
      console.error('删除模型失败:', response.data?.message || '未知错误')
      alert('删除模型失败，请稍后再试')
    }
  } catch (error) {
    console.error('删除模型失败:', error)
    alert('删除模型失败: ' + (error.message || '未知错误'))
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

onMounted(() => {
  loadModelDetail()

  // 初始化时清除模态框背景
  cleanupModalState()
})
</script>

<style scoped>
.model-detail-view {
  min-height: calc(100vh - 70px);
  background-color: #f8f9fa;
}

/* 模型查看器样式 */
.model-viewer-large {
  height: 600px;
}

/* 源图片样式 */
.source-image-container {
  border-radius: 8px;
  overflow: hidden;
}

.source-image img {
  width: 100%;
  height: auto;
  transition: transform 0.3s ease;
}

.source-image img:hover {
  transform: scale(1.05);
}

/* 移动端样式 */
@media (max-width: 767.98px) {
  .model-viewer-large {
    height: 400px;
  }
}
</style>
