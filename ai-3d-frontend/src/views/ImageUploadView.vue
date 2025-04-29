<template>
  <div class="image-upload-view">
    <div class="container py-4">
      <div class="page-header">
        <h1 class="page-title">上传图片</h1>
        <p class="page-subtitle">上传您的图片到图片库</p>
      </div>

      <div class="card">
        <div class="card-body">
          <div class="upload-container">
            <div 
              class="upload-zone"
              :class="{ 'drag-over': isDragging }"
              @dragover.prevent="onDragOver"
              @dragleave.prevent="onDragLeave"
              @drop.prevent="onDrop"
              @click="triggerFileInput"
            >
              <input 
                type="file" 
                ref="fileInput" 
                class="file-input" 
                accept="image/*" 
                multiple
                @change="onFileChange"
              >
              <div class="upload-content">
                <div class="upload-icon">
                  <i class="bi bi-cloud-arrow-up"></i>
                </div>
                <h3 class="upload-title">拖放图片到此处或点击上传</h3>
                <p class="upload-subtitle">支持 JPG, PNG, GIF 格式，单个文件最大 10MB</p>
              </div>
            </div>

            <div v-if="selectedFiles.length > 0" class="selected-files mt-4">
              <h4 class="mb-3">已选择 {{ selectedFiles.length }} 个文件</h4>
              
              <div class="file-list">
                <div v-for="(file, index) in selectedFiles" :key="index" class="file-item">
                  <div class="file-preview">
                    <img :src="file.preview" alt="预览" class="file-thumbnail">
                  </div>
                  <div class="file-info">
                    <div class="file-name">{{ file.name }}</div>
                    <div class="file-size">{{ formatFileSize(file.size) }}</div>
                  </div>
                  <button type="button" class="file-remove" @click="removeFile(index)">
                    <i class="bi bi-x"></i>
                  </button>
                </div>
              </div>

              <div class="form-group mt-4">
                <label for="visibility" class="form-label">可见性</label>
                <select id="visibility" v-model="visibility" class="form-select">
                  <option value="public">公开 - 所有人可见</option>
                  <option value="private">私有 - 仅自己可见</option>
                </select>
              </div>

              <div class="form-group mt-3">
                <label for="category" class="form-label">分类</label>
                <select id="category" v-model="category" class="form-select">
                  <option value="">选择分类</option>
                  <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
                </select>
              </div>

              <div class="form-group mt-3">
                <label for="tags" class="form-label">标签 (用逗号分隔)</label>
                <input 
                  type="text" 
                  id="tags" 
                  v-model="tags" 
                  class="form-control" 
                  placeholder="例如: 风景, 自然, 建筑"
                >
              </div>

              <div class="upload-actions mt-4">
                <button 
                  type="button" 
                  class="btn btn-primary" 
                  @click="uploadFiles" 
                  :disabled="uploading"
                >
                  <span v-if="uploading">
                    <i class="bi bi-arrow-repeat spin me-2"></i>
                    上传中...
                  </span>
                  <span v-else>
                    <i class="bi bi-cloud-upload me-2"></i>
                    开始上传
                  </span>
                </button>
                <button 
                  type="button" 
                  class="btn btn-outline-secondary" 
                  @click="cancelUpload"
                  :disabled="uploading"
                >
                  取消
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { usePictureService } from '@/services/pictureService'

export default {
  name: 'ImageUploadView',
  setup() {
    const router = useRouter()
    const pictureService = usePictureService()
    
    // 状态变量
    const fileInput = ref(null)
    const selectedFiles = ref([])
    const isDragging = ref(false)
    const uploading = ref(false)
    const visibility = ref('public')
    const category = ref('')
    const tags = ref('')
    const categories = ref([
      '风景', '建筑', '人物', '动物', '植物', '食物', '产品', '艺术', '其他'
    ])
    
    // 触发文件选择
    const triggerFileInput = () => {
      fileInput.value.click()
    }
    
    // 文件拖拽相关方法
    const onDragOver = () => {
      isDragging.value = true
    }
    
    const onDragLeave = () => {
      isDragging.value = false
    }
    
    const onDrop = (event) => {
      isDragging.value = false
      const files = event.dataTransfer.files
      if (files.length > 0) {
        handleFiles(files)
      }
    }
    
    // 文件选择处理
    const onFileChange = (event) => {
      const files = event.target.files
      if (files.length > 0) {
        handleFiles(files)
      }
    }
    
    // 处理选择的文件
    const handleFiles = (files) => {
      for (let i = 0; i < files.length; i++) {
        const file = files[i]
        
        // 检查文件类型
        if (!file.type.startsWith('image/')) {
          if (window.$toast) {
            window.$toast.error(`${file.name} 不是有效的图片文件`)
          }
          continue
        }
        
        // 检查文件大小 (10MB 限制)
        if (file.size > 10 * 1024 * 1024) {
          if (window.$toast) {
            window.$toast.error(`${file.name} 超过了10MB的大小限制`)
          }
          continue
        }
        
        // 创建预览
        const reader = new FileReader()
        reader.onload = (e) => {
          selectedFiles.value.push({
            file: file,
            name: file.name,
            size: file.size,
            preview: e.target.result
          })
        }
        reader.readAsDataURL(file)
      }
      
      // 清空文件输入，以便可以重复选择相同的文件
      fileInput.value.value = ''
    }
    
    // 移除文件
    const removeFile = (index) => {
      selectedFiles.value.splice(index, 1)
    }
    
    // 格式化文件大小
    const formatFileSize = (bytes) => {
      if (bytes === 0) return '0 Bytes'
      
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }
    
    // 上传文件
    const uploadFiles = async () => {
      if (selectedFiles.value.length === 0) {
        if (window.$toast) {
          window.$toast.warning('请先选择要上传的图片')
        }
        return
      }
      
      uploading.value = true
      
      try {
        // 创建上传进度计数器
        let successCount = 0
        let failCount = 0
        
        // 逐个上传文件
        for (const item of selectedFiles.value) {
          const formData = new FormData()
          formData.append('file', item.file)
          formData.append('isPublic', visibility.value === 'public')
          
          if (category.value) {
            formData.append('category', category.value)
          }
          
          if (tags.value) {
            formData.append('tags', tags.value)
          }
          
          try {
            await pictureService.uploadPicture(formData)
            successCount++
          } catch (error) {
            console.error(`上传 ${item.name} 失败:`, error)
            failCount++
          }
        }
        
        // 显示上传结果
        if (successCount > 0) {
          if (window.$toast) {
            window.$toast.success(`成功上传 ${successCount} 个图片`)
          }
        }
        
        if (failCount > 0) {
          if (window.$toast) {
            window.$toast.error(`${failCount} 个图片上传失败`)
          }
        }
        
        // 上传完成后跳转到图片库
        if (successCount > 0) {
          router.push('/images')
        }
      } catch (error) {
        console.error('上传过程中发生错误:', error)
        if (window.$toast) {
          window.$toast.error('上传过程中发生错误')
        }
      } finally {
        uploading.value = false
      }
    }
    
    // 取消上传
    const cancelUpload = () => {
      selectedFiles.value = []
      router.push('/images')
    }
    
    // 防止拖拽事件冒泡
    const preventDefaults = (e) => {
      e.preventDefault()
      e.stopPropagation()
    }
    
    // 添加和移除全局事件监听器
    onMounted(() => {
      ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        document.body.addEventListener(eventName, preventDefaults, false)
      })
    })
    
    onBeforeUnmount(() => {
      ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        document.body.removeEventListener(eventName, preventDefaults, false)
      })
    })
    
    return {
      fileInput,
      selectedFiles,
      isDragging,
      uploading,
      visibility,
      category,
      tags,
      categories,
      
      triggerFileInput,
      onDragOver,
      onDragLeave,
      onDrop,
      onFileChange,
      removeFile,
      formatFileSize,
      uploadFiles,
      cancelUpload
    }
  }
}
</script>

<style scoped>
.image-upload-view {
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

.upload-container {
  width: 100%;
}

.upload-zone {
  position: relative;
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-lg);
  padding: 3rem 2rem;
  text-align: center;
  background-color: var(--bg-tertiary);
  transition: all 0.3s ease;
  cursor: pointer;
}

.upload-zone:hover {
  border-color: var(--primary-color);
  background-color: var(--primary-light);
}

.upload-zone.drag-over {
  border-color: var(--primary-color);
  background-color: var(--primary-light);
  transform: scale(1.01);
}

.file-input {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
}

.upload-icon {
  font-size: 3rem;
  color: var(--primary-color);
  margin-bottom: 1rem;
}

.upload-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.upload-subtitle {
  color: var(--text-secondary);
}

.file-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.file-item {
  position: relative;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
  background-color: var(--bg-primary);
  transition: all 0.3s ease;
}

.file-item:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-3px);
}

.file-preview {
  width: 100%;
  height: 150px;
  overflow: hidden;
  background-color: var(--bg-tertiary);
}

.file-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-info {
  padding: 0.75rem;
}

.file-name {
  font-weight: 500;
  margin-bottom: 0.25rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size {
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

.file-remove {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.file-remove:hover {
  background-color: var(--error-color);
}

.upload-actions {
  display: flex;
  gap: 1rem;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .upload-zone {
    padding: 2rem 1rem;
  }
  
  .file-list {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
}
</style>
