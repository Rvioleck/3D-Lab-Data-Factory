<template>
  <div class="model-management-view">
    <div class="container py-4">
      <div class="page-header">
        <h1 class="page-title">模型管理</h1>
        <p class="page-subtitle">管理所有用户的模型</p>
      </div>

      <div class="card mb-4">
        <div class="card-body">
          <div class="row">
            <div class="col-md-6 mb-3 mb-md-0">
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
            <div class="col-md-3 mb-3 mb-md-0">
              <select class="form-select" v-model="selectedStatus" @change="search">
                <option value="">所有状态</option>
                <option value="COMPLETED">已完成</option>
                <option value="PROCESSING">处理中</option>
                <option value="FAILED">失败</option>
                <option value="PENDING">等待中</option>
              </select>
            </div>
            <div class="col-md-3">
              <select class="form-select" v-model="selectedVisibility" @change="search">
                <option value="">所有可见性</option>
                <option value="public">公开</option>
                <option value="private">私有</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-body">
          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">加载中...</span>
            </div>
            <p class="mt-3">正在加载模型数据...</p>
          </div>

          <div v-else-if="models.length === 0" class="text-center py-5">
            <i class="bi bi-inbox display-1 text-muted"></i>
            <p class="mt-3">没有找到符合条件的模型</p>
          </div>

          <div v-else>
            <div class="table-responsive">
              <table class="table table-hover">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>缩略图</th>
                    <th>名称</th>
                    <th>所有者</th>
                    <th>状态</th>
                    <th>可见性</th>
                    <th>创建时间</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="model in models" :key="model.id">
                    <td>{{ model.id }}</td>
                    <td>
                      <div class="model-thumbnail">
                        <img :src="model.thumbnailUrl || '/placeholder-model.png'" alt="缩略图">
                      </div>
                    </td>
                    <td>
                      <div class="model-name">{{ model.name }}</div>
                      <div class="model-category" v-if="model.category">{{ model.category }}</div>
                    </td>
                    <td>
                      <div class="user-info">
                        <span class="user-name">{{ model.userName || model.userId }}</span>
                      </div>
                    </td>
                    <td>
                      <span class="status-badge" :class="getStatusClass(model.status)">
                        {{ getStatusText(model.status) }}
                      </span>
                    </td>
                    <td>
                      <span class="visibility-badge" :class="model.isPublic ? 'public' : 'private'">
                        {{ model.isPublic ? '公开' : '私有' }}
                      </span>
                    </td>
                    <td>{{ formatDate(model.createTime) }}</td>
                    <td>
                      <div class="action-buttons">
                        <button 
                          class="btn btn-sm btn-outline-primary" 
                          @click="viewModel(model.id)"
                          :disabled="model.status !== 'COMPLETED'"
                        >
                          <i class="bi bi-eye"></i>
                        </button>
                        <button 
                          class="btn btn-sm btn-outline-secondary" 
                          @click="editModel(model.id)"
                        >
                          <i class="bi bi-pencil"></i>
                        </button>
                        <button 
                          class="btn btn-sm btn-outline-danger" 
                          @click="confirmDeleteModel(model)"
                        >
                          <i class="bi bi-trash"></i>
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- 分页 -->
            <div class="d-flex justify-content-between align-items-center mt-4">
              <div class="pagination-info">
                显示 {{ models.length }} 条，共 {{ totalItems }} 条
              </div>
              <nav aria-label="Page navigation">
                <ul class="pagination">
                  <li class="page-item" :class="{ disabled: currentPage === 1 }">
                    <a class="page-link" href="#" @click.prevent="changePage(currentPage - 1)">
                      <i class="bi bi-chevron-left"></i>
                    </a>
                  </li>
                  <li 
                    v-for="page in paginationItems" 
                    :key="page" 
                    class="page-item"
                    :class="{ active: page === currentPage }"
                  >
                    <a class="page-link" href="#" @click.prevent="changePage(page)">
                      {{ page }}
                    </a>
                  </li>
                  <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                    <a class="page-link" href="#" @click.prevent="changePage(currentPage + 1)">
                      <i class="bi bi-chevron-right"></i>
                    </a>
                  </li>
                </ul>
              </nav>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import debounce from 'lodash/debounce'
import { useModelService } from '@/services/modelService'
import { formatDate } from '@/utils/dateTime'

export default {
  name: 'ModelManagementView',
  setup() {
    const router = useRouter()
    const modelService = useModelService()
    
    // 状态变量
    const loading = ref(false)
    const models = ref([])
    const searchQuery = ref('')
    const selectedStatus = ref('')
    const selectedVisibility = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalItems = ref(0)
    const totalPages = ref(0)
    
    // 计算属性
    const paginationItems = computed(() => {
      const items = []
      const maxVisiblePages = 5
      
      if (totalPages.value <= maxVisiblePages) {
        // 如果总页数小于等于最大可见页数，显示所有页码
        for (let i = 1; i <= totalPages.value; i++) {
          items.push(i)
        }
      } else {
        // 否则，显示当前页附近的页码
        let startPage = Math.max(currentPage.value - Math.floor(maxVisiblePages / 2), 1)
        let endPage = startPage + maxVisiblePages - 1
        
        if (endPage > totalPages.value) {
          endPage = totalPages.value
          startPage = Math.max(endPage - maxVisiblePages + 1, 1)
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
          status: selectedStatus.value || undefined,
          all: true // 管理员查看所有模型
        }
        
        // 根据可见性筛选
        if (selectedVisibility.value === 'public') {
          params.isPublic = true
        } else if (selectedVisibility.value === 'private') {
          params.isPublic = false
        }
        
        const response = await modelService.getModels(params)
        models.value = response.data.data.records || []
        totalItems.value = response.data.data.total || 0
        totalPages.value = Math.ceil(totalItems.value / pageSize.value)
      } catch (error) {
        console.error('获取模型列表失败:', error)
        if (window.$toast) {
          window.$toast.error('获取模型列表失败')
        }
        models.value = []
        totalItems.value = 0
        totalPages.value = 0
      } finally {
        loading.value = false
      }
    }
    
    // 搜索
    const search = () => {
      currentPage.value = 1
      loadModels()
    }
    
    // 防抖搜索
    const debounceSearch = debounce(() => {
      search()
    }, 500)
    
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
    
    // 编辑模型
    const editModel = (modelId) => {
      router.push(`/models/edit/${modelId}`)
    }
    
    // 确认删除模型
    const confirmDeleteModel = (model) => {
      if (window.confirm(`确定要删除模型 "${model.name}" 吗？此操作不可撤销。`)) {
        deleteModel(model.id)
      }
    }
    
    // 删除模型
    const deleteModel = async (modelId) => {
      try {
        await modelService.deleteModel(modelId)
        
        if (window.$toast) {
          window.$toast.success('模型已删除')
        }
        
        // 重新加载模型列表
        loadModels()
      } catch (error) {
        console.error('删除模型失败:', error)
        if (window.$toast) {
          window.$toast.error(error.message || '删除模型失败')
        }
      }
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
        case 'COMPLETED': return 'success'
        case 'PROCESSING': return 'primary'
        case 'FAILED': return 'danger'
        case 'PENDING': return 'warning'
        default: return ''
      }
    }
    
    // 生命周期钩子
    onMounted(() => {
      loadModels()
    })
    
    return {
      loading,
      models,
      searchQuery,
      selectedStatus,
      selectedVisibility,
      currentPage,
      pageSize,
      totalItems,
      totalPages,
      paginationItems,
      
      search,
      debounceSearch,
      changePage,
      viewModel,
      editModel,
      confirmDeleteModel,
      formatDate,
      getStatusText,
      getStatusClass
    }
  }
}
</script>

<style scoped>
.model-management-view {
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

.model-thumbnail {
  width: 50px;
  height: 50px;
  border-radius: var(--radius-md);
  overflow: hidden;
  background-color: var(--bg-tertiary);
}

.model-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.model-name {
  font-weight: 500;
  color: var(--text-primary);
}

.model-category {
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

.user-info {
  display: flex;
  align-items: center;
}

.user-name {
  font-weight: 500;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 500;
}

.status-badge.success {
  background-color: var(--success-light);
  color: var(--success-color);
}

.status-badge.primary {
  background-color: var(--primary-light);
  color: var(--primary-color);
}

.status-badge.danger {
  background-color: var(--error-light);
  color: var(--error-color);
}

.status-badge.warning {
  background-color: var(--warning-light);
  color: var(--warning-color);
}

.visibility-badge {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 500;
}

.visibility-badge.public {
  background-color: var(--info-light);
  color: var(--info-color);
}

.visibility-badge.private {
  background-color: var(--neutral-200);
  color: var(--neutral-700);
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.pagination-info {
  color: var(--text-secondary);
  font-size: 0.875rem;
}
</style>
