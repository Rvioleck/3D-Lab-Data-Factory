<template>
  <div class="image-management-view">
    <div class="container py-4">
      <div class="page-header">
        <h1 class="page-title">图片管理</h1>
        <p class="page-subtitle">管理所有用户的图片</p>
      </div>

      <div class="card mb-4">
        <div class="card-body">
          <div class="row">
            <div class="col-md-6 mb-3 mb-md-0">
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
            <div class="col-md-3 mb-3 mb-md-0">
              <select class="form-select" v-model="selectedCategory" @change="search">
                <option value="">所有分类</option>
                <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
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
            <p class="mt-3">正在加载图片数据...</p>
          </div>

          <div v-else-if="pictures.length === 0" class="text-center py-5">
            <i class="bi bi-images display-1 text-muted"></i>
            <p class="mt-3">没有找到符合条件的图片</p>
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
                    <th>分类</th>
                    <th>可见性</th>
                    <th>上传时间</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="picture in pictures" :key="picture.id">
                    <td>{{ picture.id }}</td>
                    <td>
                      <div class="image-thumbnail">
                        <img :src="picture.url" alt="缩略图">
                      </div>
                    </td>
                    <td>
                      <div class="image-name">{{ picture.name || '未命名图片' }}</div>
                      <div class="image-size" v-if="picture.width && picture.height">
                        {{ picture.width }}x{{ picture.height }}
                      </div>
                    </td>
                    <td>
                      <div class="user-info">
                        <span class="user-name">{{ picture.userName || picture.userId }}</span>
                      </div>
                    </td>
                    <td>
                      <span class="category-badge" v-if="picture.category">
                        {{ picture.category }}
                      </span>
                      <span v-else class="text-muted">未分类</span>
                    </td>
                    <td>
                      <span class="visibility-badge" :class="picture.isPublic ? 'public' : 'private'">
                        {{ picture.isPublic ? '公开' : '私有' }}
                      </span>
                    </td>
                    <td>{{ formatDate(picture.createTime) }}</td>
                    <td>
                      <div class="action-buttons">
                        <button 
                          class="btn btn-sm btn-outline-primary" 
                          @click="viewPicture(picture.id)"
                        >
                          <i class="bi bi-eye"></i>
                        </button>
                        <button 
                          class="btn btn-sm btn-outline-secondary" 
                          @click="editPicture(picture.id)"
                        >
                          <i class="bi bi-pencil"></i>
                        </button>
                        <button 
                          class="btn btn-sm btn-outline-danger" 
                          @click="confirmDeletePicture(picture)"
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
                显示 {{ pictures.length }} 条，共 {{ totalItems }} 条
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
import { usePictureService } from '@/services/pictureService'
import { formatDate } from '@/utils/dateTime'

export default {
  name: 'ImageManagementView',
  setup() {
    const router = useRouter()
    const pictureService = usePictureService()
    
    // 状态变量
    const loading = ref(false)
    const pictures = ref([])
    const searchQuery = ref('')
    const selectedCategory = ref('')
    const selectedVisibility = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalItems = ref(0)
    const totalPages = ref(0)
    
    const categories = ref([
      '风景', '建筑', '人物', '动物', '植物', '食物', '产品', '艺术', '其他'
    ])
    
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
    const loadPictures = async () => {
      loading.value = true
      try {
        // 调用API获取图片列表
        const params = {
          current: currentPage.value,
          pageSize: pageSize.value,
          name: searchQuery.value || undefined,
          category: selectedCategory.value || undefined,
          all: true // 管理员查看所有图片
        }
        
        // 根据可见性筛选
        if (selectedVisibility.value === 'public') {
          params.isPublic = true
        } else if (selectedVisibility.value === 'private') {
          params.isPublic = false
        }
        
        const response = await pictureService.getPictures(params)
        pictures.value = response.data.data.records || []
        totalItems.value = response.data.data.total || 0
        totalPages.value = Math.ceil(totalItems.value / pageSize.value)
      } catch (error) {
        console.error('获取图片列表失败:', error)
        if (window.$toast) {
          window.$toast.error('获取图片列表失败')
        }
        pictures.value = []
        totalItems.value = 0
        totalPages.value = 0
      } finally {
        loading.value = false
      }
    }
    
    // 搜索
    const search = () => {
      currentPage.value = 1
      loadPictures()
    }
    
    // 防抖搜索
    const debounceSearch = debounce(() => {
      search()
    }, 500)
    
    // 切换页码
    const changePage = (page) => {
      if (page < 1 || page > totalPages.value) return
      currentPage.value = page
      loadPictures()
    }
    
    // 查看图片详情
    const viewPicture = (pictureId) => {
      router.push(`/images/${pictureId}`)
    }
    
    // 编辑图片
    const editPicture = (pictureId) => {
      router.push(`/images/edit/${pictureId}`)
    }
    
    // 确认删除图片
    const confirmDeletePicture = (picture) => {
      if (window.confirm(`确定要删除图片 "${picture.name || '未命名图片'}" 吗？此操作不可撤销。`)) {
        deletePicture(picture.id)
      }
    }
    
    // 删除图片
    const deletePicture = async (pictureId) => {
      try {
        await pictureService.deletePicture(pictureId)
        
        if (window.$toast) {
          window.$toast.success('图片已删除')
        }
        
        // 重新加载图片列表
        loadPictures()
      } catch (error) {
        console.error('删除图片失败:', error)
        if (window.$toast) {
          window.$toast.error(error.message || '删除图片失败')
        }
      }
    }
    
    // 生命周期钩子
    onMounted(() => {
      loadPictures()
    })
    
    return {
      loading,
      pictures,
      searchQuery,
      selectedCategory,
      selectedVisibility,
      currentPage,
      pageSize,
      totalItems,
      totalPages,
      paginationItems,
      categories,
      
      search,
      debounceSearch,
      changePage,
      viewPicture,
      editPicture,
      confirmDeletePicture,
      formatDate
    }
  }
}
</script>

<style scoped>
.image-management-view {
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

.image-thumbnail {
  width: 50px;
  height: 50px;
  border-radius: var(--radius-md);
  overflow: hidden;
  background-color: var(--bg-tertiary);
}

.image-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-name {
  font-weight: 500;
  color: var(--text-primary);
}

.image-size {
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

.category-badge {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 500;
  background-color: var(--bg-tertiary);
  color: var(--text-secondary);
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
