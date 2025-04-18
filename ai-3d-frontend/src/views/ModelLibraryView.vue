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
        <div class="col-md-8">
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
        <div class="col-md-4">
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
                :class="modelStatus === 'completed' ? 'btn-primary' : 'btn-outline-primary'" 
                @click="setModelStatus('completed')"
              >
                已完成
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 模型列表 -->
      <div class="row">
        <div v-if="loading" class="col-12 text-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">加载中...</span>
          </div>
          <p class="mt-3">正在加载模型...</p>
        </div>

        <div v-else-if="filteredModels.length === 0" class="col-12 text-center py-5">
          <div class="empty-state">
            <i class="bi bi-box" style="font-size: 3rem;"></i>
            <h4 class="mt-3">暂无模型</h4>
            <p class="text-muted">您还没有创建任何3D模型</p>
            <router-link to="/images" class="btn btn-primary mt-2">
              <i class="bi bi-image me-1"></i> 浏览图片库
            </router-link>
          </div>
        </div>

        <div v-else class="col-12">
          <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
            <!-- 模型卡片 -->
            <div 
              v-for="model in filteredModels" 
              :key="model.id"
              class="col"
            >
              <div class="card h-100 model-card">
                <div class="card-img-container">
                  <!-- 模型预览图 -->
                  <div class="model-preview" :style="model.pixelImagesUrl ? `background-image: url(${model.pixelImagesUrl})` : ''">
                    <div v-if="!model.pixelImagesUrl" class="model-icon">
                      <i class="bi bi-box"></i>
                    </div>
                    <div class="model-overlay">
                      <button class="btn btn-sm btn-light" @click="viewModel(model)">
                        <i class="bi bi-eye"></i> 预览
                      </button>
                    </div>
                  </div>
                  <!-- 状态标识 -->
                  <div class="model-status-badge">
                    <span class="badge" :class="model.status === 'completed' ? 'bg-success' : 'bg-primary'">
                      {{ model.status === 'completed' ? '已完成' : '处理中' }}
                    </span>
                  </div>
                </div>
                <div class="card-body">
                  <h5 class="card-title text-truncate">{{ model.name || '未命名模型' }}</h5>
                  <div class="d-flex justify-content-between align-items-center">
                    <p class="card-text text-muted small mb-0">
                      <i class="bi bi-clock me-1"></i> {{ formatDate(model.createTime) }}
                    </p>
                    <div class="model-actions">
                      <button class="btn btn-sm btn-outline-primary me-1" @click="viewModel(model)" title="查看模型">
                        <i class="bi bi-eye"></i>
                      </button>
                      <button class="btn btn-sm btn-outline-secondary" @click="downloadModel(model)" title="下载模型" :disabled="model.status !== 'completed'">
                        <i class="bi bi-download"></i>
                      </button>
                    </div>
                  </div>
                </div>
                <div class="card-footer bg-transparent">
                  <div class="d-flex align-items-center">
                    <small class="text-muted">源图片:</small>
                    <div class="source-image-thumbnail ms-2" v-if="model.sourceImageUrl">
                      <img :src="model.sourceImageUrl" alt="源图片" @click="viewSourceImage(model)">
                    </div>
                    <span class="text-muted ms-2" v-else>无源图片</span>
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
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import axios from 'axios'
import debounce from 'lodash/debounce'

// API URL
const API_URL = import.meta.env.VITE_API_URL || ''

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
    
    // 模拟3D模型数据 - 实际应用中应该从API获取
    const mockModels = [
      {
        id: 'm1',
        name: '显微镜模型',
        status: 'completed',
        pixelImagesUrl: 'https://via.placeholder.com/300x200?text=显微镜纹理',
        xyzImagesUrl: 'https://via.placeholder.com/300x200?text=显微镜深度',
        objUrl: 'https://example.com/models/microscope.obj',
        mtlUrl: 'https://example.com/models/microscope.mtl',
        textureUrl: 'https://example.com/models/microscope.png',
        outputZipUrl: 'https://example.com/models/microscope.zip',
        sourceImageId: '1',
        sourceImageUrl: 'https://via.placeholder.com/100x100?text=显微镜',
        taskId: 'abc123def456',
        createTime: '2023-04-16T14:20:00Z'
      },
      {
        id: 'm2',
        name: '试管架模型',
        status: 'completed',
        pixelImagesUrl: 'https://via.placeholder.com/300x200?text=试管架纹理',
        xyzImagesUrl: 'https://via.placeholder.com/300x200?text=试管架深度',
        objUrl: 'https://example.com/models/test-tube-rack.obj',
        mtlUrl: 'https://example.com/models/test-tube-rack.mtl',
        textureUrl: 'https://example.com/models/test-tube-rack.png',
        outputZipUrl: 'https://example.com/models/test-tube-rack.zip',
        sourceImageId: '2',
        sourceImageUrl: 'https://via.placeholder.com/100x100?text=试管架',
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
        sourceImageUrl: 'https://via.placeholder.com/100x100?text=培养皿',
        taskId: 'xyz789abc012',
        createTime: '2023-04-18T11:45:00Z'
      }
    ]
    
    // 计算属性
    const filteredModels = computed(() => {
      let result = [...models.value]
      
      // 根据状态筛选
      if (modelStatus.value !== 'all') {
        result = result.filter(model => model.status === modelStatus.value)
      }
      
      // 根据搜索关键词筛选
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(model => 
          model.name && model.name.toLowerCase().includes(query)
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
    const loadModels = async () => {
      loading.value = true
      try {
        // 实际应用中应该调用API
        // const response = await axios.get(`${API_URL}/api/models`, {
        //   params: {
        //     status: modelStatus.value !== 'all' ? modelStatus.value : undefined,
        //     page: currentPage.value,
        //     pageSize: pageSize.value,
        //     query: searchQuery.value || undefined
        //   },
        //   withCredentials: true
        // })
        // models.value = response.data.records
        // totalItems.value = response.data.total
        
        // 模拟API响应
        setTimeout(() => {
          models.value = mockModels
          totalItems.value = mockModels.length
          loading.value = false
        }, 500)
      } catch (error) {
        console.error('加载模型失败:', error)
        loading.value = false
      }
    }
    
    const search = () => {
      currentPage.value = 1
      loadModels()
    }
    
    const debounceSearch = debounce(() => {
      search()
    }, 300)
    
    const setModelStatus = (status) => {
      modelStatus.value = status
      currentPage.value = 1
      loadModels()
    }
    
    const changePage = (page) => {
      if (page < 1 || page > totalPages.value) return
      currentPage.value = page
      loadModels()
    }
    
    const viewModel = (model) => {
      router.push(`/models/${model.id}`)
    }
    
    const viewSourceImage = (model) => {
      if (model.sourceImageId) {
        router.push(`/images/${model.sourceImageId}`)
      }
    }
    
    const downloadModel = (model) => {
      if (!model || model.status !== 'completed' || !model.outputZipUrl) {
        alert('模型还未完成生成，无法下载')
        return
      }
      
      // 实际应用中应该使用正确的URL
      window.open(model.outputZipUrl, '_blank')
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
      loadModels()
    })
    
    return {
      loading,
      models,
      filteredModels,
      searchQuery,
      modelStatus,
      currentPage,
      totalPages,
      paginationItems,
      isAdmin,
      
      loadModels,
      search,
      debounceSearch,
      setModelStatus,
      changePage,
      viewModel,
      viewSourceImage,
      downloadModel,
      formatDate
    }
  }
}
</script>

<style scoped>
/* 模型卡片样式 */
.model-card {
  transition: transform 0.2s, box-shadow 0.2s;
  overflow: hidden;
  border-radius: 8px;
}

.model-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-img-container {
  position: relative;
  height: 180px;
  overflow: hidden;
  background-color: #f8f9fa;
}

/* 模型预览区域 */
.model-preview {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

.model-icon {
  font-size: 3rem;
  color: #6c757d;
}

.model-overlay {
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

.model-card:hover .model-overlay {
  opacity: 1;
}

.model-status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
}

/* 源图片缩略图 */
.source-image-thumbnail {
  width: 30px;
  height: 30px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #dee2e6;
}

.source-image-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 3rem 0;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .card-img-container {
    height: 150px;
  }
}
</style>
