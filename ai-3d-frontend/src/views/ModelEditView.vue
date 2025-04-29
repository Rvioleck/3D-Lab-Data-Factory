<template>
  <div class="model-edit-view">
    <div class="container py-4">
      <div class="page-header">
        <h1 class="page-title">编辑模型</h1>
        <p class="page-subtitle">修改模型信息</p>
      </div>

      <div class="card">
        <div class="card-body">
          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">加载中...</span>
            </div>
            <p class="mt-3">正在加载模型信息...</p>
          </div>

          <div v-else-if="error" class="alert alert-danger">
            {{ error }}
          </div>

          <div v-else class="edit-container">
            <div class="row">
              <div class="col-md-4">
                <div class="model-preview">
                  <div class="model-thumbnail">
                    <img :src="model.thumbnailUrl || '/placeholder-model.png'" alt="模型预览" class="preview-img">
                  </div>
                  
                  <div class="model-viewer-container mt-3" v-if="model.status === 'COMPLETED'">
                    <button class="btn btn-primary w-100" @click="openModelViewer">
                      <i class="bi bi-box me-2"></i>
                      在3D查看器中打开
                    </button>
                  </div>
                </div>
              </div>
              <div class="col-md-8">
                <form @submit.prevent="saveModel">
                  <div class="form-group mb-3">
                    <label for="name" class="form-label">模型名称</label>
                    <input 
                      type="text" 
                      id="name" 
                      v-model="model.name" 
                      class="form-control" 
                      required
                    >
                  </div>

                  <div class="form-group mb-3">
                    <label for="description" class="form-label">描述</label>
                    <textarea 
                      id="description" 
                      v-model="model.description" 
                      class="form-control" 
                      rows="3"
                    ></textarea>
                  </div>

                  <div class="form-group mb-3">
                    <label for="visibility" class="form-label">可见性</label>
                    <select id="visibility" v-model="model.isPublic" class="form-select">
                      <option :value="true">公开 - 所有人可见</option>
                      <option :value="false">私有 - 仅自己可见</option>
                    </select>
                  </div>

                  <div class="form-group mb-3">
                    <label for="category" class="form-label">分类</label>
                    <select id="category" v-model="model.category" class="form-select">
                      <option value="">选择分类</option>
                      <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
                    </select>
                  </div>

                  <div class="form-group mb-3">
                    <label for="tags" class="form-label">标签 (用逗号分隔)</label>
                    <input 
                      type="text" 
                      id="tags" 
                      v-model="tagsString" 
                      class="form-control" 
                      placeholder="例如: 建筑, 家具, 角色"
                    >
                  </div>

                  <div class="form-group mb-3">
                    <label class="form-label d-block">模型信息</label>
                    <div class="model-info">
                      <div class="info-item">
                        <span class="info-label">创建时间:</span>
                        <span class="info-value">{{ formatDate(model.createTime) }}</span>
                      </div>
                      <div class="info-item">
                        <span class="info-label">状态:</span>
                        <span class="info-value" :class="getStatusClass(model.status)">
                          {{ getStatusText(model.status) }}
                        </span>
                      </div>
                      <div class="info-item">
                        <span class="info-label">文件大小:</span>
                        <span class="info-value">{{ formatFileSize(model.size) }}</span>
                      </div>
                      <div class="info-item">
                        <span class="info-label">格式:</span>
                        <span class="info-value">{{ model.format || 'GLB/GLTF' }}</span>
                      </div>
                    </div>
                  </div>

                  <div class="form-actions mt-4">
                    <button 
                      type="submit" 
                      class="btn btn-primary" 
                      :disabled="saving"
                    >
                      <span v-if="saving">
                        <i class="bi bi-arrow-repeat spin me-2"></i>
                        保存中...
                      </span>
                      <span v-else>
                        <i class="bi bi-check-lg me-2"></i>
                        保存更改
                      </span>
                    </button>
                    <button 
                      type="button" 
                      class="btn btn-outline-secondary ms-2" 
                      @click="cancel"
                      :disabled="saving"
                    >
                      取消
                    </button>
                    <button 
                      type="button" 
                      class="btn btn-outline-danger ms-2" 
                      @click="confirmDelete"
                      :disabled="saving"
                    >
                      删除模型
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useModelService } from '@/services/modelService'
import { formatDate } from '@/utils/dateTime'

export default {
  name: 'ModelEditView',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const modelService = useModelService()
    
    // 状态变量
    const loading = ref(true)
    const saving = ref(false)
    const error = ref(null)
    const model = ref({
      id: null,
      name: '',
      description: '',
      thumbnailUrl: '',
      isPublic: true,
      category: '',
      tags: [],
      status: '',
      size: 0,
      format: '',
      createTime: null,
      userId: null
    })
    
    const categories = ref([
      '建筑', '家具', '角色', '动物', '植物', '交通工具', '电子产品', '艺术品', '其他'
    ])
    
    // 计算属性
    const tagsString = computed({
      get: () => model.value.tags ? model.value.tags.join(', ') : '',
      set: (val) => {
        model.value.tags = val.split(',')
          .map(tag => tag.trim())
          .filter(tag => tag !== '')
      }
    })
    
    // 获取模型信息
    const fetchModel = async () => {
      const modelId = route.params.id
      if (!modelId) {
        error.value = '未指定模型ID'
        loading.value = false
        return
      }
      
      try {
        loading.value = true
        const response = await modelService.getModelById(modelId)
        model.value = response.data.data
        
        // 确保tags是数组
        if (typeof model.value.tags === 'string') {
          model.value.tags = model.value.tags.split(',').map(tag => tag.trim())
        } else if (!Array.isArray(model.value.tags)) {
          model.value.tags = []
        }
      } catch (err) {
        console.error('获取模型信息失败:', err)
        error.value = err.message || '获取模型信息失败'
      } finally {
        loading.value = false
      }
    }
    
    // 保存模型信息
    const saveModel = async () => {
      try {
        saving.value = true
        
        // 准备更新数据
        const updateData = {
          name: model.value.name,
          description: model.value.description,
          isPublic: model.value.isPublic,
          category: model.value.category,
          tags: Array.isArray(model.value.tags) ? model.value.tags.join(',') : model.value.tags
        }
        
        await modelService.updateModel(model.value.id, updateData)
        
        if (window.$toast) {
          window.$toast.success('模型信息已更新')
        }
        
        // 返回模型库
        router.push('/models')
      } catch (err) {
        console.error('更新模型信息失败:', err)
        if (window.$toast) {
          window.$toast.error(err.message || '更新模型信息失败')
        }
      } finally {
        saving.value = false
      }
    }
    
    // 取消编辑
    const cancel = () => {
      router.push('/models')
    }
    
    // 确认删除
    const confirmDelete = () => {
      if (window.confirm('确定要删除这个模型吗？此操作不可撤销。')) {
        deleteModel()
      }
    }
    
    // 删除模型
    const deleteModel = async () => {
      try {
        saving.value = true
        await modelService.deleteModel(model.value.id)
        
        if (window.$toast) {
          window.$toast.success('模型已删除')
        }
        
        // 返回模型库
        router.push('/models')
      } catch (err) {
        console.error('删除模型失败:', err)
        if (window.$toast) {
          window.$toast.error(err.message || '删除模型失败')
        }
        saving.value = false
      }
    }
    
    // 打开模型查看器
    const openModelViewer = () => {
      router.push(`/models/${model.value.id}`)
    }
    
    // 格式化文件大小
    const formatFileSize = (bytes) => {
      if (!bytes || bytes === 0) return '0 Bytes'
      
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }
    
    // 获取状态文本
    const getStatusText = (status) => {
      switch (status) {
        case 'COMPLETED': return '已完成'
        case 'PROCESSING': return '处理中'
        case 'FAILED': return '失败'
        case 'PENDING': return '等待中'
        default: return status
      }
    }
    
    // 获取状态类名
    const getStatusClass = (status) => {
      switch (status) {
        case 'COMPLETED': return 'text-success'
        case 'PROCESSING': return 'text-primary'
        case 'FAILED': return 'text-danger'
        case 'PENDING': return 'text-warning'
        default: return ''
      }
    }
    
    // 生命周期钩子
    onMounted(() => {
      fetchModel()
    })
    
    return {
      loading,
      saving,
      error,
      model,
      categories,
      tagsString,
      
      saveModel,
      cancel,
      confirmDelete,
      openModelViewer,
      formatDate,
      formatFileSize,
      getStatusText,
      getStatusClass
    }
  }
}
</script>

<style scoped>
.model-edit-view {
  min-height: calc(100vh - 70px);
  background-color: var(--bg-secondary);
}

.page-header {
  margin-bottom: 2rem;
}

.page-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.page-subtitle {
  color: var(--text-secondary);
}

.model-preview {
  width: 100%;
  margin-bottom: 1.5rem;
}

.model-thumbnail {
  width: 100%;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  background-color: var(--bg-tertiary);
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.model-info {
  background-color: var(--bg-tertiary);
  border-radius: var(--radius-md);
  padding: 1rem;
}

.info-item {
  margin-bottom: 0.5rem;
  display: flex;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  font-weight: 500;
  width: 100px;
  color: var(--text-secondary);
}

.info-value {
  color: var(--text-primary);
}

.form-actions {
  display: flex;
}

.text-success {
  color: var(--success-color);
}

.text-primary {
  color: var(--primary-color);
}

.text-danger {
  color: var(--error-color);
}

.text-warning {
  color: var(--warning-color);
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .form-actions {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .form-actions button {
    width: 100%;
    margin-left: 0 !important;
  }
}
</style>
