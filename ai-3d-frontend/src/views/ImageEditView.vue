<template>
  <div class="image-edit-view">
    <div class="container py-4">
      <div class="page-header">
        <h1 class="page-title">编辑图片</h1>
        <p class="page-subtitle">修改图片信息</p>
      </div>

      <div class="card">
        <div class="card-body">
          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">加载中...</span>
            </div>
            <p class="mt-3">正在加载图片信息...</p>
          </div>

          <div v-else-if="error" class="alert alert-danger">
            {{ error }}
          </div>

          <div v-else class="edit-container">
            <div class="row">
              <div class="col-md-4">
                <div class="image-preview">
                  <img :src="picture.url" alt="图片预览" class="preview-img">
                </div>
              </div>
              <div class="col-md-8">
                <form @submit.prevent="savePicture">
                  <div class="form-group mb-3">
                    <label for="name" class="form-label">图片名称</label>
                    <input 
                      type="text" 
                      id="name" 
                      v-model="picture.name" 
                      class="form-control" 
                      required
                    >
                  </div>

                  <div class="form-group mb-3">
                    <label for="description" class="form-label">描述</label>
                    <textarea 
                      id="description" 
                      v-model="picture.description" 
                      class="form-control" 
                      rows="3"
                    ></textarea>
                  </div>

                  <div class="form-group mb-3">
                    <label for="visibility" class="form-label">可见性</label>
                    <select id="visibility" v-model="picture.isPublic" class="form-select">
                      <option :value="true">公开 - 所有人可见</option>
                      <option :value="false">私有 - 仅自己可见</option>
                    </select>
                  </div>

                  <div class="form-group mb-3">
                    <label for="category" class="form-label">分类</label>
                    <select id="category" v-model="picture.category" class="form-select">
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
                      placeholder="例如: 风景, 自然, 建筑"
                    >
                  </div>

                  <div class="form-group mb-3">
                    <label class="form-label d-block">图片信息</label>
                    <div class="image-info">
                      <div class="info-item">
                        <span class="info-label">上传时间:</span>
                        <span class="info-value">{{ formatDate(picture.createTime) }}</span>
                      </div>
                      <div class="info-item">
                        <span class="info-label">文件大小:</span>
                        <span class="info-value">{{ formatFileSize(picture.size) }}</span>
                      </div>
                      <div class="info-item">
                        <span class="info-label">分辨率:</span>
                        <span class="info-value">{{ picture.width }} x {{ picture.height }}</span>
                      </div>
                      <div class="info-item">
                        <span class="info-label">文件类型:</span>
                        <span class="info-value">{{ picture.fileType }}</span>
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
                      删除图片
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
import { usePictureService } from '@/services/pictureService'
import { formatDate } from '@/utils/dateTime'

export default {
  name: 'ImageEditView',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const pictureService = usePictureService()
    
    // 状态变量
    const loading = ref(true)
    const saving = ref(false)
    const error = ref(null)
    const picture = ref({
      id: null,
      name: '',
      description: '',
      url: '',
      isPublic: true,
      category: '',
      tags: [],
      size: 0,
      width: 0,
      height: 0,
      fileType: '',
      createTime: null,
      userId: null
    })
    
    const categories = ref([
      '风景', '建筑', '人物', '动物', '植物', '食物', '产品', '艺术', '其他'
    ])
    
    // 计算属性
    const tagsString = computed({
      get: () => picture.value.tags ? picture.value.tags.join(', ') : '',
      set: (val) => {
        picture.value.tags = val.split(',')
          .map(tag => tag.trim())
          .filter(tag => tag !== '')
      }
    })
    
    // 获取图片信息
    const fetchPicture = async () => {
      const pictureId = route.params.id
      if (!pictureId) {
        error.value = '未指定图片ID'
        loading.value = false
        return
      }
      
      try {
        loading.value = true
        const response = await pictureService.getPictureById(pictureId)
        picture.value = response.data.data
        
        // 确保tags是数组
        if (typeof picture.value.tags === 'string') {
          picture.value.tags = picture.value.tags.split(',').map(tag => tag.trim())
        } else if (!Array.isArray(picture.value.tags)) {
          picture.value.tags = []
        }
      } catch (err) {
        console.error('获取图片信息失败:', err)
        error.value = err.message || '获取图片信息失败'
      } finally {
        loading.value = false
      }
    }
    
    // 保存图片信息
    const savePicture = async () => {
      try {
        saving.value = true
        
        // 准备更新数据
        const updateData = {
          name: picture.value.name,
          description: picture.value.description,
          isPublic: picture.value.isPublic,
          category: picture.value.category,
          tags: Array.isArray(picture.value.tags) ? picture.value.tags.join(',') : picture.value.tags
        }
        
        await pictureService.updatePicture(picture.value.id, updateData)
        
        if (window.$toast) {
          window.$toast.success('图片信息已更新')
        }
        
        // 返回图片库
        router.push('/images')
      } catch (err) {
        console.error('更新图片信息失败:', err)
        if (window.$toast) {
          window.$toast.error(err.message || '更新图片信息失败')
        }
      } finally {
        saving.value = false
      }
    }
    
    // 取消编辑
    const cancel = () => {
      router.push('/images')
    }
    
    // 确认删除
    const confirmDelete = () => {
      if (window.confirm('确定要删除这张图片吗？此操作不可撤销。')) {
        deletePicture()
      }
    }
    
    // 删除图片
    const deletePicture = async () => {
      try {
        saving.value = true
        await pictureService.deletePicture(picture.value.id)
        
        if (window.$toast) {
          window.$toast.success('图片已删除')
        }
        
        // 返回图片库
        router.push('/images')
      } catch (err) {
        console.error('删除图片失败:', err)
        if (window.$toast) {
          window.$toast.error(err.message || '删除图片失败')
        }
        saving.value = false
      }
    }
    
    // 格式化文件大小
    const formatFileSize = (bytes) => {
      if (!bytes || bytes === 0) return '0 Bytes'
      
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }
    
    // 生命周期钩子
    onMounted(() => {
      fetchPicture()
    })
    
    return {
      loading,
      saving,
      error,
      picture,
      categories,
      tagsString,
      
      savePicture,
      cancel,
      confirmDelete,
      formatDate,
      formatFileSize
    }
  }
}
</script>

<style scoped>
.image-edit-view {
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

.image-preview {
  width: 100%;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  margin-bottom: 1.5rem;
}

.preview-img {
  width: 100%;
  height: auto;
  display: block;
}

.image-info {
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

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .image-preview {
    max-height: 300px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .preview-img {
    max-height: 100%;
    width: auto;
  }
}
</style>
