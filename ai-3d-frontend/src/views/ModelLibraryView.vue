<template>
  <div class="model-library-view">
    <div class="container mt-4">
      <div class="row">
        <div class="col-md-12 text-center mb-4">
          <h1>3D模型库</h1>
          <p class="lead">浏览和管理您的3D模型</p>
        </div>
      </div>

      <!-- 搜索和筛选 -->
      <div class="row mb-4">
        <div class="col-md-6">
          <div class="input-group">
            <input
              type="text"
              class="form-control"
              placeholder="搜索模型名称..."
              v-model="searchQuery"
              @input="debounceSearch"
            >
            <button class="btn btn-primary" type="button" @click="search">
              <i class="bi bi-search"></i> 搜索
            </button>
          </div>
        </div>
        <div class="col-md-3">
          <select class="form-select" v-model="selectedCategory" @change="search">
            <option value="">所有分类</option>
            <option v-for="category in categories" :key="category" :value="category">
              {{ category }}
            </option>
          </select>
        </div>
        <div class="col-md-3">
          <div class="d-flex justify-content-end">
            <div class="btn-group">
              <button
                class="btn"
                :class="modelStatus === 'all' ? 'btn-primary' : 'btn-outline-primary'"
                @click="setModelStatus('all')"
              >
                全部模型
              </button>
              <button
                class="btn"
                :class="modelStatus === 'COMPLETED' ? 'btn-primary' : 'btn-outline-primary'"
                @click="setModelStatus('COMPLETED')"
              >
                已完成
              </button>
              <button
                class="btn"
                :class="modelStatus === 'PROCESSING' ? 'btn-primary' : 'btn-outline-primary'"
                @click="setModelStatus('PROCESSING')"
              >
                处理中
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载中提示 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">加载中...</span>
        </div>
        <p class="mt-3">正在加载模型数据...</p>
      </div>

      <!-- 无数据提示 -->
      <div v-else-if="filteredModels.length === 0" class="text-center py-5">
        <i class="bi bi-inbox-fill display-1 text-muted"></i>
        <h3 class="mt-4">暂无模型数据</h3>
        <p class="text-muted">
          {{ searchQuery || selectedCategory ? '没有找到符合条件的模型' : '您还没有创建任何3D模型' }}
        </p>
        <router-link to="/images" class="btn btn-primary mt-3">
          <i class="bi bi-images me-2"></i> 浏览图片库
        </router-link>
      </div>

      <!-- 模型列表 -->
      <div v-else class="row">
        <div v-for="model in filteredModels" :key="model.id" class="col-md-4 col-sm-6 mb-4">
          <div class="card h-100 model-card">
            <div class="model-status-badge" :class="getStatusClass(model.status)">
              {{ getStatusText(model.status) }}
            </div>
            
            <!-- 模型预览图 -->
            <div class="model-preview" @click="viewModel(model.id)">
              <img
                v-if="model.status === 'COMPLETED' && model.pixelImagesUrl"
                :src="model.pixelImagesUrl"
                :alt="model.name"
                class="card-img-top"
              >
              <div v-else-if="model.status === 'COMPLETED'" class="placeholder-preview">
                <i class="bi bi-box display-4"></i>
                <p>3D模型已生成</p>
              </div>
              <div v-else-if="model.status === 'PROCESSING'" class="placeholder-preview processing">
                <div class="spinner-border text-primary" role="status">
                  <span class="visually-hidden">处理中...</span>
                </div>
                <p>模型生成中...</p>
              </div>
              <div v-else class="placeholder-preview">
                <i class="bi bi-hourglass-split display-4"></i>
                <p>等待处理</p>
              </div>
            </div>
            
            <div class="card-body">
              <h5 class="card-title">{{ model.name || '未命名模型' }}</h5>
              <p v-if="model.introduction" class="card-text text-muted small">
                {{ model.introduction }}
              </p>
              <div class="model-meta">
                <div class="badges mb-2">
                  <span v-if="model.category" class="badge bg-secondary me-1">{{ model.category }}</span>
                  <span v-if="model.modelFormat" class="badge bg-info me-1">{{ model.modelFormat }}</span>
                </div>
                <div class="text-muted small mb-2">
                  <i class="bi bi-calendar-event me-1"></i> {{ formatDate(model.createTime) }}
                </div>
                <div v-if="model.sourceImageUrl" class="source-image">
                  <span class="text-muted small">源图片:</span>
                  <img :src="model.sourceImageUrl" :alt="model.name + ' 源图片'" class="source-image-thumbnail">
                </div>
              </div>
            </div>
            <div class="card-footer">
              <div class="d-grid gap-2">
                <button
                  class="btn btn-primary"
                  @click="viewModel(model.id)"
                  :disabled="model.status !== 'COMPLETED'"
                >
                  <i class="bi bi-box me-1"></i> 查看模型
                </button>
                <button
                  v-if="isAdmin"
                  class="btn btn-outline-danger"
                  @click="confirmDeleteModel(model)"
                >
                  <i class="bi bi-trash me-1"></i> 删除
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="row mt-4">
        <div class="col-12">
          <nav aria-label="模型分页">
            <ul class="pagination justify-content-center">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <a class="page-link" href="#" @click.prevent="changePage(currentPage - 1)">上一页</a>
              </li>
              <li
                v-for="page in paginationItems"
                :key="page"
                class="page-item"
                :class="{ active: currentPage === page }"
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
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import debounce from 'lodash/debounce'
import { listModelsByPage, getModelCategories, deleteModel } from '@/api/model'

// 模型状态常量
const MODEL_STATUS = {
  PENDING: 'PENDING',
  PROCESSING: 'PROCESSING',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED'
}

export default {
  name: 'ModelLibraryView',
  setup() {
    const store = useStore()
    const router = useRouter()
    const isAdmin = computed(() => store.getters['user/isAdmin'])

    // 状态变量
    const loading = ref(false)
    const models = ref([])
    const searchQuery = ref('')
    const modelStatus = ref('all')
    const currentPage = ref(1)
    const pageSize = ref(12)
    const totalItems = ref(0)
    const categories = ref([])
    const selectedCategory = ref('')

    // 计算属性
    const filteredModels = computed(() => {
      return models.value
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
    const loadModels = async () => {
      loading.value = true
      try {
        // 调用API获取模型列表
        const params = {
          current: currentPage.value,
          pageSize: pageSize.value,
          name: searchQuery.value || undefined,
          category: selectedCategory.value || undefined,
          status: modelStatus.value !== 'all' ? modelStatus.value : undefined
        }

        const response = await listModelsByPage(params)
        if (response.code === 0) {
          models.value = response.data.records || []
          totalItems.value = response.data.total || 0
          console.log('加载模型成功:', models.value)
        } else {
          console.error('获取模型列表失败:', response.message)
          models.value = []
          totalItems.value = 0
        }
      } catch (error) {
        console.error('加载模型失败:', error)
        models.value = []
        totalItems.value = 0
      } finally {
        loading.value = false
      }
    }

    // 加载模型分类列表
    const loadCategories = async () => {
      try {
        const response = await getModelCategories()
        if (response.code === 0 && response.data) {
          categories.value = response.data
        }
      } catch (error) {
        console.error('加载分类失败:', error)
      }
    }

    // 搜索方法
    const search = () => {
      currentPage.value = 1
      loadModels()
    }

    // 防抖搜索
    const debounceSearch = debounce(() => {
      search()
    }, 500)

    // 设置模型状态筛选
    const setModelStatus = (status) => {
      modelStatus.value = status
      search()
    }

    // 切换页码
    const changePage = (page) => {
      if (page < 1 || page > totalPages.value) return
      currentPage.value = page
      loadModels()
    }

    // 查看模型详情
    const viewModel = (modelId) => {
      router.push(`/models/${modelId}`)
    }

    // 确认删除模型
    const confirmDeleteModel = (model) => {
      if (confirm(`确定要删除模型 "${model.name || '未命名模型'}" 吗？此操作不可恢复。`)) {
        deleteModelById(model.id)
      }
    }

    // 删除模型
    const deleteModelById = async (modelId) => {
      try {
        const response = await deleteModel(modelId)
        if (response.code === 0) {
          alert('删除成功')
          loadModels() // 重新加载模型列表
        } else {
          alert(`删除失败: ${response.message}`)
        }
      } catch (error) {
        console.error('删除模型失败:', error)
        alert(`删除失败: ${error.message || '未知错误'}`)
      }
    }

    // 格式化日期
    const formatDate = (dateStr) => {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return date.toLocaleString()
    }

    // 获取状态文本
    const getStatusText = (status) => {
      switch (status) {
        case MODEL_STATUS.PENDING: return '等待中'
        case MODEL_STATUS.PROCESSING: return '处理中'
        case MODEL_STATUS.COMPLETED: return '已完成'
        case MODEL_STATUS.FAILED: return '失败'
        case 'completed': return '已完成' // 兼容旧数据
        case 'processing': return '处理中' // 兼容旧数据
        case 'pending': return '等待中' // 兼容旧数据
        case 'failed': return '失败' // 兼容旧数据
        default: return '未知'
      }
    }

    // 获取状态样式类
    const getStatusClass = (status) => {
      switch (status) {
        case MODEL_STATUS.PENDING: return 'status-pending'
        case MODEL_STATUS.PROCESSING: return 'status-processing'
        case MODEL_STATUS.COMPLETED: return 'status-completed'
        case MODEL_STATUS.FAILED: return 'status-failed'
        case 'completed': return 'status-completed' // 兼容旧数据
        case 'processing': return 'status-processing' // 兼容旧数据
        case 'pending': return 'status-pending' // 兼容旧数据
        case 'failed': return 'status-failed' // 兼容旧数据
        default: return ''
      }
    }

    // 组件挂载时加载数据
    onMounted(() => {
      loadModels()
      loadCategories()
    })

    return {
      loading,
      models,
      filteredModels,
      searchQuery,
      modelStatus,
      currentPage,
      pageSize,
      totalItems,
      totalPages,
      paginationItems,
      categories,
      selectedCategory,
      isAdmin,

      search,
      debounceSearch,
      setModelStatus,
      changePage,
      viewModel,
      confirmDeleteModel,
      formatDate,
      getStatusText,
      getStatusClass
    }
  }
}
</script>

<style scoped>
.model-library-view {
  min-height: calc(100vh - 70px);
}

.model-card {
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.model-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.model-status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: bold;
  z-index: 10;
  color: white;
}

.status-pending {
  background-color: #ffc107;
  color: #212529;
}

.status-processing {
  background-color: #0d6efd;
}

.status-completed {
  background-color: #198754;
}

.status-failed {
  background-color: #dc3545;
}

.model-preview {
  height: 200px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  cursor: pointer;
}

.model-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.model-preview:hover img {
  transform: scale(1.05);
}

.placeholder-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;
  color: #6c757d;
  padding: 20px;
  text-align: center;
}

.placeholder-preview.processing {
  background-color: rgba(13, 110, 253, 0.05);
}

.model-meta {
  margin-top: 15px;
}

.source-image {
  display: flex;
  align-items: center;
  margin-top: 10px;
}

.source-image-thumbnail {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
  margin-left: 10px;
  border: 1px solid #dee2e6;
}

@media (max-width: 767.98px) {
  .model-preview {
    height: 150px;
  }
}
</style>
