<template>
  <div class="image-library">
    <div class="container mt-4">
      <div class="row">
        <div class="col-md-12 text-center mb-4">
          <h1>图片库</h1>
          <p class="lead">管理您的图片资源，用于3D重建</p>
        </div>
      </div>

      <!-- 搜索和筛选 -->
      <div class="row mb-4">
        <div class="col-md-8">
          <div class="input-group">
            <input
              type="text"
              class="form-control"
              placeholder="搜索图片名称..."
              v-model="searchQuery"
              @input="debounceSearch"
            >
            <button class="btn btn-primary" type="button" @click="search">
              <i class="bi bi-search"></i> 搜索
            </button>
          </div>
        </div>
        <div class="col-md-4 text-end">
          <button class="btn btn-success" @click="showUploadModal">
            <i class="bi bi-plus-circle me-1"></i> 上传新图片
          </button>
        </div>
      </div>

      <!-- 图片列表 -->
      <div class="row">
        <div v-if="loading" class="col-12 text-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">加载中...</span>
          </div>
          <p class="mt-3">正在加载图片...</p>
        </div>

        <div v-else-if="filteredImages.length === 0" class="col-12 text-center py-5">
          <div class="empty-state">
            <i class="bi bi-image" style="font-size: 3rem;"></i>
            <h4 class="mt-3">暂无图片</h4>
            <p class="text-muted">上传一些图片开始使用</p>
            <button class="btn btn-primary mt-2" @click="showUploadModal">
              <i class="bi bi-plus-circle me-1"></i> 上传图片
            </button>
          </div>
        </div>

        <div v-else class="col-12">
          <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 row-cols-xl-4 g-4">
            <!-- 图片卡片 -->
            <div
              v-for="image in filteredImages"
              :key="image.id"
              class="col"
            >
              <div class="card h-100 image-card">
                <div class="card-img-container">
                  <img
                    :src="image.url"
                    class="card-img-top"
                    alt="图片"
                    v-lazy
                  >
                  <div class="image-overlay">
                    <div class="btn-group">
                      <button class="btn btn-sm btn-light" @click="viewImage(image)">
                        <i class="bi bi-eye"></i>
                      </button>
                      <button class="btn btn-sm btn-primary" @click="createModel(image)" v-if="isAdmin">
                        <i class="bi bi-box"></i>
                      </button>
                      <button class="btn btn-sm btn-danger" @click="confirmDeleteImage(image)" v-if="isAdmin">
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                  </div>
                  <div v-if="image.hasModel" class="model-badge">
                    <span class="badge bg-success">已有3D模型</span>
                  </div>
                </div>
                <div class="card-body">
                  <h5 class="card-title text-truncate">{{ image.name || '未命名图片' }}</h5>
                  <p class="card-text text-muted small mb-0">
                    <i class="bi bi-clock me-1"></i> {{ formatDate(image.createTime) }}
                  </p>
                </div>
                <div class="card-footer bg-transparent">
                  <div class="d-flex justify-content-between">
                    <button class="btn btn-sm btn-outline-primary" @click="viewImage(image)">
                      <i class="bi bi-eye me-1"></i> 查看
                    </button>
                    <button class="btn btn-sm btn-success" @click="createModel(image)" v-if="isAdmin && !image.hasModel">
                      <i class="bi bi-box me-1"></i> 创建3D模型
                    </button>
                    <button class="btn btn-sm btn-outline-success" @click="viewModel(image)" v-if="image.hasModel">
                      <i class="bi bi-box me-1"></i> 查看3D模型
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="row mt-4">
        <div class="col-12">
          <nav v-if="totalPages > 1">
            <ul class="pagination justify-content-center">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <a class="page-link" href="#" @click.prevent="changePage(currentPage - 1)">上一页</a>
              </li>
              <li
                v-for="page in paginationItems"
                :key="page"
                class="page-item"
                :class="{ active: page === currentPage }"
              >
                <a class="page-link" href="#" @click.prevent="changePage(page)">{{ page }}</a>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                <a class="page-link" href="#" @click.prevent="changePage(currentPage + 1)">下一页</a>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>

    <!-- 上传图片模态框 -->
    <div class="modal fade" id="uploadModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">上传新图片</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label for="imageName" class="form-label">图片名称（可选）</label>
              <input type="text" class="form-control" id="imageName" v-model="newImage.name" placeholder="输入图片名称">
            </div>

            <div class="upload-preview mb-3" v-if="newImage.file">
              <img :src="newImage.previewUrl" class="img-fluid rounded" alt="图片预览">
              <button class="btn btn-sm btn-outline-danger preview-remove" @click="removeSelectedFile">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>

            <div class="upload-dropzone mb-3" v-if="!newImage.file" @click="triggerFileInput" @dragover.prevent @drop.prevent="handleFileDrop">
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
              @change="handleFileChange"
            />
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button
              type="button"
              class="btn btn-primary"
              @click="uploadImage"
              :disabled="!newImage.file || uploading"
            >
              <span v-if="uploading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
              {{ uploading ? '上传中...' : '上传图片' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 图片详情模态框 -->
    <div class="modal fade" id="imageDetailModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content" v-if="selectedImage">
          <div class="modal-header">
            <h5 class="modal-title">{{ selectedImage.name || '未命名图片' }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="row">
              <div class="col-md-8">
                <img :src="selectedImage.url" class="img-fluid rounded" alt="图片详情">
              </div>
              <div class="col-md-4">
                <div class="image-details">
                  <p><strong>ID:</strong> {{ selectedImage.id }}</p>
                  <p><strong>上传时间:</strong> {{ formatDate(selectedImage.createTime) }}</p>
                  <p><strong>尺寸:</strong> {{ selectedImage.width || '未知' }} x {{ selectedImage.height || '未知' }}</p>
                  <p><strong>3D模型状态:</strong>
                    <span v-if="selectedImage.hasModel" class="badge bg-success">已创建</span>
                    <span v-else class="badge bg-secondary">未创建</span>
                  </p>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
            <button
              type="button"
              class="btn btn-success"
              @click="createModel(selectedImage)"
              v-if="isAdmin && !selectedImage.hasModel"
            >
              <i class="bi bi-box me-1"></i> 创建3D模型
            </button>
            <button
              type="button"
              class="btn btn-primary"
              @click="viewModel(selectedImage)"
              v-if="selectedImage.hasModel"
            >
              <i class="bi bi-box me-1"></i> 查看3D模型
            </button>
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
          <div class="modal-body" v-if="imageToDelete">
            <p>确定要删除图片 "{{ imageToDelete.name || '未命名图片' }}" 吗？</p>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useStore } from '@/utils/storeCompat'
import { useRouter } from 'vue-router'
import { Modal } from 'bootstrap'
import debounce from 'lodash/debounce'
import { cleanupModalState } from '@/utils/modalFix.js'
import {
  uploadPicture,
  listPictureByPage,
  deletePicture,
  getPictureCategories,
  getPictureTags
} from '@/api/picture'

export default {
  name: 'ImageLibraryView',
  setup() {
    const store = useStore()
    const router = useRouter()
    const isAdmin = computed(() => store.getters['user/isAdmin'])

    // 状态变量
    const loading = ref(false)
    const uploading = ref(false)
    const images = ref([])
    const searchQuery = ref('')
    const currentPage = ref(1)
    const pageSize = ref(12)
    const totalItems = ref(0)
    const selectedImage = ref(null)
    const imageToDelete = ref(null)
    const fileInput = ref(null)

    // 模态框引用
    const uploadModal = ref(null)
    const imageDetailModal = ref(null)
    const deleteConfirmModal = ref(null)

    // 新图片数据
    const newImage = ref({
      name: '',
      file: null,
      previewUrl: null
    })

    // 分类和标签列表
    const categories = ref([])
    const tags = ref([])

    // 计算属性
    const filteredImages = computed(() => {
      let result = [...images.value]

      // 根据搜索关键词筛选
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(image =>
          image.name && image.name.toLowerCase().includes(query)
        )
      }

      return result
    })

    const totalPages = computed(() => {
      return Math.ceil(totalItems.value / pageSize.value)
    })

    const paginationItems = computed(() => {
      const items = []
      const maxVisiblePages = 5

      if (totalPages.value <= maxVisiblePages) {
        // 显示所有页码
        for (let i = 1; i <= totalPages.value; i++) {
          items.push(i)
        }
      } else {
        // 显示部分页码
        let startPage = Math.max(1, currentPage.value - Math.floor(maxVisiblePages / 2))
        let endPage = Math.min(totalPages.value, startPage + maxVisiblePages - 1)

        // 调整起始页
        if (endPage - startPage + 1 < maxVisiblePages) {
          startPage = Math.max(1, endPage - maxVisiblePages + 1)
        }

        for (let i = startPage; i <= endPage; i++) {
          items.push(i)
        }
      }

      return items
    })

    // 方法
    const loadImages = async () => {
      loading.value = true
      try {
        const params = {
          current: currentPage.value,
          pageSize: pageSize.value
        }

        if (searchQuery.value) {
          params.name = searchQuery.value
        }

        const response = await listPictureByPage(params)

        if (response.code === 0 && response.data) {
          images.value = response.data.records || []
          totalItems.value = response.data.total || 0
        } else {
          console.error('加载图片失败:', response.message)
          alert('加载图片失败: ' + (response.message || '未知错误'))
          images.value = []
          totalItems.value = 0
        }
      } catch (error) {
        console.error('加载图片失败:', error)
        alert('加载图片失败: ' + (error?.response?.data?.message || error.message || '未知错误'))
        images.value = []
        totalItems.value = 0
      } finally {
        loading.value = false
      }
    }

    const search = () => {
      currentPage.value = 1
      loadImages()
    }

    const debounceSearch = debounce(() => {
      search()
    }, 300)

    const resetFilters = () => {
      searchQuery.value = ''
      currentPage.value = 1
      loadImages()
    }

    const changePage = (page) => {
      if (page < 1 || page > totalPages.value) return
      currentPage.value = page
      loadImages()
    }

    // 图片上传相关方法
    const showUploadModal = () => {
      // 重置表单
      newImage.value = {
        name: '',
        file: null,
        previewUrl: null
      }

      // 显示模态框，禁用背景遮罩
      uploadModal.value = new Modal(document.getElementById('uploadModal'), {
        backdrop: false,
        keyboard: true
      })
      uploadModal.value.show()
    }

    const triggerFileInput = () => {
      if (fileInput.value) {
        fileInput.value.click()
      }
    }

    const handleFileDrop = (event) => {
      const files = event.dataTransfer.files
      if (files && files.length > 0) {
        const file = files[0]
        if (file.type.startsWith('image/')) {
          handleFileSelection(file)
        } else {
          alert('请上传图片文件（JPG、PNG等）')
        }
      }
    }

    const handleFileChange = (event) => {
      const files = event.target.files
      if (files && files.length > 0) {
        handleFileSelection(files[0])
      }
    }

    const handleFileSelection = (file) => {
      newImage.value.file = file
      newImage.value.previewUrl = URL.createObjectURL(file)

      // 自动设置图片名称（如果未设置）
      if (!newImage.value.name) {
        // 从文件名提取名称（去除扩展名）
        const fileName = file.name.replace(/\.[^/.]+$/, "")
        newImage.value.name = fileName
      }
    }

    const removeSelectedFile = () => {
      if (newImage.value.previewUrl) {
        URL.revokeObjectURL(newImage.value.previewUrl)
      }
      newImage.value.file = null
      newImage.value.previewUrl = null
      if (fileInput.value) {
        fileInput.value.value = ''
      }
    }

    const uploadImage = async () => {
      if (!newImage.value.file) return

      uploading.value = true
      try {
        // 准备上传数据
        const metadata = {}
        if (newImage.value.name) {
          metadata.name = newImage.value.name
        }

        // 调用API上传图片
        const response = await uploadPicture(newImage.value.file, metadata)

        if (response.code === 0) {
          // 关闭模态框
          if (uploadModal.value) {
            uploadModal.value.hide()
            // 清除模态框背景
            setTimeout(cleanupModalState, 100)
          }

          // 重新加载图片列表
          loadImages()

          // 重置状态
          newImage.value = {
            name: '',
            file: null,
            previewUrl: null
          }

          // 显示成功消息
          alert('图片上传成功！')
        } else {
          alert('上传失败: ' + (response.message || '未知错误'))
        }
      } catch (error) {
        console.error('上传图片失败:', error)
        alert('上传图片失败: ' + (error.message || '未知错误'))
      } finally {
        uploading.value = false
      }
    }

    // 图片查看和操作
    const viewImage = (image) => {
      selectedImage.value = image

      // 显示模态框，禁用背景遮罩
      imageDetailModal.value = new Modal(document.getElementById('imageDetailModal'), {
        backdrop: false,
        keyboard: true
      })
      imageDetailModal.value.show()
    }

    const createModel = (image) => {
      // 跳转到重建页面，并传递图片ID和自动创建参数
      router.push({
        path: '/reconstruction',
        query: {
          imageId: image.id,
          autoCreate: 'true'
        }
      })
    }

    const viewModel = async (image) => {
      try {
        // 显示加载状态
        loading.value = true

        // 直接使用apiClient调用正确的API路径
        const { apiClient } = await import('@/api/apiClient')
        const response = await apiClient.get(`/model/image/${image.id}`)

        if (response.data && response.data.code === 0 && response.data.data) {
          // 导航到模型详情页
          router.push(`/models/${response.data.data.id}`)
        } else {
          console.error('获取模型信息失败:', response.data?.message || '未知错误')
          alert('获取模型信息失败，请稍后再试')
        }
      } catch (error) {
        console.error('查看模型失败:', error)
        alert('查看模型失败: ' + (error.message || '未知错误'))
      } finally {
        loading.value = false
      }
    }

    const confirmDeleteImage = (image) => {
      imageToDelete.value = image

      // 显示模态框，禁用背景遮罩
      deleteConfirmModal.value = new Modal(document.getElementById('deleteConfirmModal'), {
        backdrop: false,
        keyboard: true
      })
      deleteConfirmModal.value.show()
    }

    const deleteImage = async () => {
      if (!imageToDelete.value) return

      try {
        // 调用API删除图片
        const response = await deletePicture(imageToDelete.value.id)

        if (response.code === 0) {
          // 关闭模态框
          if (deleteConfirmModal.value) {
            deleteConfirmModal.value.hide()
            // 清除模态框背景
            setTimeout(cleanupModalState, 100)
          }

          // 重新加载图片列表
          loadImages()

          // 重置状态
          imageToDelete.value = null

          // 显示成功消息
          alert('图片删除成功！')
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

    // 加载分类列表
    const loadCategories = async () => {
      try {
        const response = await getPictureCategories()
        if (response.code === 0 && response.data) {
          categories.value = response.data
        }
      } catch (error) {
        console.error('加载分类失败:', error)
      }
    }

    // 加载标签列表
    const loadTags = async () => {
      try {
        const response = await getPictureTags()
        if (response.code === 0 && response.data) {
          tags.value = response.data
        }
      } catch (error) {
        console.error('加载标签失败:', error)
      }
    }

    // 生命周期钩子
    onMounted(() => {
      loadImages()
      loadCategories()
      loadTags()

      // 初始化时清除模态框背景
      cleanupModalState()
    })

    return {
      loading,
      uploading,
      images,
      filteredImages,
      searchQuery,
      currentPage,
      totalPages,
      paginationItems,
      selectedImage,
      imageToDelete,
      newImage,
      fileInput,
      isAdmin,
      categories,
      tags,

      loadImages,
      search,
      debounceSearch,
      resetFilters,
      changePage,
      showUploadModal,
      triggerFileInput,
      handleFileDrop,
      handleFileChange,
      removeSelectedFile,
      uploadImage,
      viewImage,
      createModel,
      viewModel,
      confirmDeleteImage,
      deleteImage,
      formatDate
    }
  }
}
</script>

<style scoped>
/* 图片卡片样式 */
.image-card {
  transition: transform 0.2s, box-shadow 0.2s;
  overflow: hidden;
  border-radius: 8px;
}

.image-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-img-container {
  position: relative;
  height: 180px;
  overflow: hidden;
  background-color: #f8f9fa;
}

.card-img-top {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.image-card:hover .card-img-top {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-card:hover .image-overlay {
  opacity: 1;
}

.model-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
}

/* 上传区域样式 */
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

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 3rem 0;
}

/* 图片详情样式 */
.image-details {
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 8px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .card-img-container {
    height: 150px;
  }
}
</style>
