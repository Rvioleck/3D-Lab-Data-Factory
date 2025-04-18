<template>
  <div class="resource-library-view">
    <div class="container mt-4">
      <div class="row">
        <div class="col-md-12 text-center mb-4">
          <h1>3D模型库</h1>
          <p class="lead">浏览和管理您的重建模型</p>
          <p class="text-muted">所有通过3D重建生成的模型都会自动保存在这里</p>
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

      <!-- 资源列表 -->
      <div class="row">
        <div v-if="loading" class="col-12 text-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">加载中...</span>
          </div>
          <p class="mt-2">正在加载资源...</p>
        </div>

        <div v-else-if="filteredResources.length === 0" class="col-12 text-center py-5">
          <div class="empty-state">
            <i class="bi bi-box" style="font-size: 3rem;"></i>
            <h4 class="mt-3">未找到模型</h4>
            <p class="text-muted">您还没有创建任何模型，或者没有符合搜索条件的模型</p>
            <div class="mt-3">
              <button class="btn btn-outline-primary me-2" @click="resetFilters">
                <i class="bi bi-arrow-counterclockwise me-1"></i> 清除筛选
              </button>
              <router-link to="/reconstruction" class="btn btn-primary">
                <i class="bi bi-plus-circle me-1"></i> 创建新模型
              </router-link>
            </div>
          </div>
        </div>

        <div v-else class="col-12">
          <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
            <!-- 3D模型卡片 -->
            <div
              v-for="model in filteredResources"
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
                      <button class="btn btn-sm btn-light" @click="viewResource(model)">
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
                      <button class="btn btn-sm btn-outline-primary me-1" @click="viewResource(model)" title="查看模型">
                        <i class="bi bi-eye"></i>
                      </button>
                      <button class="btn btn-sm btn-outline-secondary" @click="downloadResource(model)" title="下载模型">
                        <i class="bi bi-download"></i>
                      </button>
                    </div>
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
          <nav aria-label="资源分页" v-if="totalPages > 1">
            <ul class="pagination justify-content-center">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <a class="page-link" href="#" @click.prevent="changePage(currentPage - 1)">上一页</a>
              </li>
              <li
                v-for="page in paginationItems"
                :key="page.value"
                class="page-item"
                :class="{ active: page.active, disabled: page.disabled }"
              >
                <a
                  v-if="!page.disabled"
                  class="page-link"
                  href="#"
                  @click.prevent="changePage(page.value)"
                >
                  {{ page.label }}
                </a>
                <span v-else class="page-link">{{ page.label }}</span>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                <a class="page-link" href="#" @click.prevent="changePage(currentPage + 1)">下一页</a>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>

    <!-- 资源详情模态框 -->
    <div class="modal fade" id="resourceDetailModal" tabindex="-1" aria-hidden="true" ref="resourceModal">
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ selectedResource?.name || '资源详情' }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="关闭"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedResource">
              <div class="resource-preview mb-4">
                <img
                  v-if="selectedResource.type === 'image'"
                  :src="selectedResource.url"
                  class="img-fluid"
                  alt="资源预览"
                >
                <div v-else class="model-viewer-container">
                  <ModelViewer
                    v-if="selectedResource.type === 'model'"
                    :objUrl="selectedResource.objUrl"
                    :mtlUrl="selectedResource.mtlUrl"
                    :textureUrl="selectedResource.textureUrl"
                  />
                </div>
              </div>

              <div class="resource-details">
                <h6>基本信息</h6>
                <table class="table table-sm">
                  <tbody>
                    <tr>
                      <th scope="row" width="30%">ID</th>
                      <td>{{ selectedResource.id }}</td>
                    </tr>
                    <tr>
                      <th scope="row">名称</th>
                      <td>{{ selectedResource.name }}</td>
                    </tr>
                    <tr>
                      <th scope="row">类型</th>
                      <td>{{ selectedResource.type === 'image' ? '图片' : '3D模型' }}</td>
                    </tr>
                    <tr>
                      <th scope="row">创建时间</th>
                      <td>{{ formatDate(selectedResource.createTime, true) }}</td>
                    </tr>
                    <tr v-if="selectedResource.type === 'model'">
                      <th scope="row">来源图片</th>
                      <td>
                        <a href="#" @click.prevent="viewSourceImage(selectedResource)">查看来源图片</a>
                      </td>
                    </tr>
                  </tbody>
                </table>

                <h6 class="mt-3">标签</h6>
                <div class="resource-tags mb-3">
                  <span
                    v-for="tag in selectedResource.tags"
                    :key="tag"
                    class="badge bg-light text-dark me-1 mb-1"
                  >
                    {{ tag }}
                  </span>
                  <span v-if="selectedResource.tags.length === 0" class="text-muted">
                    暂无标签
                  </span>
                </div>

                <div v-if="isAdmin" class="admin-actions mt-4">
                  <h6>管理操作</h6>
                  <div class="d-flex">
                    <button class="btn btn-sm btn-outline-primary me-2" @click="editResource(selectedResource)">
                      <i class="bi bi-pencil"></i> 编辑
                    </button>
                    <button class="btn btn-sm btn-outline-danger" @click="confirmDeleteResource(selectedResource)">
                      <i class="bi bi-trash"></i> 删除
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
            <button
              type="button"
              class="btn btn-primary"
              @click="downloadResource(selectedResource)"
            >
              <i class="bi bi-download"></i> 下载
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useStore } from 'vuex'
import ModelViewer from '@/components/ModelViewer.vue'
import { Modal } from 'bootstrap'

// 导入重建相关的API
import { getTaskList, getResultFileUrl } from '@/api/reconstruction'

export default {
  name: 'ResourceLibraryView',
  components: {
    ModelViewer
  },
  setup() {
    const store = useStore()
    const isAdmin = computed(() => store.getters['user/isAdmin'])

    // 状态变量
    const loading = ref(false)
    const resources = ref([])
    const searchQuery = ref('')
    const modelStatus = ref('all') // 模型状态筛选：全部/已完成
    const currentPage = ref(1)
    const pageSize = ref(12)
    const totalItems = ref(0)
    const selectedResource = ref(null)
    const resourceModal = ref(null)

    // 模拟3D模型数据 - 实际应用中应该从 API 获取
    const mockModels = [
      {
        id: '1',
        name: '试管架模型',
        status: 'completed',
        pixelImagesUrl: 'https://via.placeholder.com/300x200?text=试管架纹理',
        xyzImagesUrl: 'https://via.placeholder.com/300x200?text=试管架深度',
        objUrl: 'https://example.com/models/test-tube-rack.obj',
        mtlUrl: 'https://example.com/models/test-tube-rack.mtl',
        textureUrl: 'https://example.com/models/test-tube-rack.png',
        outputZipUrl: 'https://example.com/models/test-tube-rack.zip',
        taskId: 'abc123def456',
        createTime: '2023-04-16T14:20:00Z'
      },
      {
        id: '2',
        name: '显微镜模型',
        status: 'processing',
        pixelImagesUrl: null,
        xyzImagesUrl: null,
        objUrl: null,
        mtlUrl: null,
        textureUrl: null,
        outputZipUrl: null,
        taskId: 'xyz789abc012',
        createTime: '2023-04-17T09:15:00Z'
      },
      {
        id: '3',
        name: '培养皿3D模型',
        status: 'completed',
        pixelImagesUrl: 'https://via.placeholder.com/300x200?text=培养皿纹理',
        xyzImagesUrl: 'https://via.placeholder.com/300x200?text=培养皿深度',
        objUrl: 'https://example.com/models/petri-dish.obj',
        mtlUrl: 'https://example.com/models/petri-dish.mtl',
        textureUrl: 'https://example.com/models/petri-dish.png',
        outputZipUrl: 'https://example.com/models/petri-dish.zip',
        taskId: 'def456ghi789',
        createTime: '2023-04-18T11:45:00Z'
      }
    ]

    // 初始化数据
    onMounted(() => {
      loadResources()

      // 初始化Bootstrap模态框
      resourceModal.value = new Modal(document.getElementById('resourceDetailModal'))
    })

    // 加载模型数据
    const loadResources = async () => {
      loading.value = true
      try {
        // 实际应用中应该调用API
        // const response = await getTaskList({
        //   status: modelStatus.value !== 'all' ? modelStatus.value : undefined,
        //   current: currentPage.value,
        //   pageSize: pageSize.value,
        //   query: searchQuery.value || undefined
        // })
        // resources.value = response.data.records
        // totalItems.value = response.data.total

        // 模拟API响应
        setTimeout(() => {
          resources.value = mockModels
          totalItems.value = mockModels.length
          loading.value = false
        }, 500)
      } catch (error) {
        console.error('加载模型失败:', error)
        loading.value = false
      }
    }

    // 筛选模型
    const filteredResources = computed(() => {
      let result = [...resources.value]

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

    // 计算总页数
    const totalPages = computed(() => {
      return Math.ceil(totalItems.value / pageSize.value)
    })

    // 生成分页项
    const paginationItems = computed(() => {
      const items = []
      const maxVisiblePages = 5

      if (totalPages.value <= maxVisiblePages) {
        // 显示所有页码
        for (let i = 1; i <= totalPages.value; i++) {
          items.push({
            value: i,
            label: i.toString(),
            active: i === currentPage.value,
            disabled: false
          })
        }
      } else {
        // 始终显示第一页
        items.push({
          value: 1,
          label: '1',
          active: currentPage.value === 1,
          disabled: false
        })

        // 计算中间页码的起始和结束
        let startPage = Math.max(2, currentPage.value - 1)
        let endPage = Math.min(totalPages.value - 1, startPage + 2)

        // 调整以确保显示3个中间页码
        if (endPage - startPage < 2) {
          if (startPage === 2) {
            endPage = Math.min(totalPages.value - 1, startPage + 2)
          } else {
            startPage = Math.max(2, endPage - 2)
          }
        }

        // 添加省略号
        if (startPage > 2) {
          items.push({
            value: null,
            label: '...',
            active: false,
            disabled: true
          })
        }

        // 添加中间页码
        for (let i = startPage; i <= endPage; i++) {
          items.push({
            value: i,
            label: i.toString(),
            active: i === currentPage.value,
            disabled: false
          })
        }

        // 添加省略号
        if (endPage < totalPages.value - 1) {
          items.push({
            value: null,
            label: '...',
            active: false,
            disabled: true
          })
        }

        // 始终显示最后一页
        items.push({
          value: totalPages.value,
          label: totalPages.value.toString(),
          active: currentPage.value === totalPages.value,
          disabled: false
        })
      }

      return items
    })

    // 设置模型状态筛选
    const setModelStatus = (status) => {
      modelStatus.value = status
      currentPage.value = 1
      loadResources()
    }

    // 搜索防抖
    let searchTimeout = null
    const debounceSearch = () => {
      if (searchTimeout) {
        clearTimeout(searchTimeout)
      }
      searchTimeout = setTimeout(() => {
        search()
      }, 500)
    }

    // 执行搜索
    const search = () => {
      currentPage.value = 1
      loadResources()
    }

    // 重置筛选条件
    const resetFilters = () => {
      searchQuery.value = ''
      resourceType.value = 'all'
      selectedTags.value = []
      currentPage.value = 1
      loadResources()
    }

    // 切换页码
    const changePage = (page) => {
      if (page < 1 || page > totalPages.value) return
      currentPage.value = page
      loadResources()
    }

    // 查看资源详情
    const viewResource = (resource) => {
      selectedResource.value = resource
      resourceModal.value.show()
    }

    // 查看来源图片
    const viewSourceImage = (modelResource) => {
      const sourceImage = resources.value.find(r => r.id === modelResource.sourceImageId)
      if (sourceImage) {
        selectedResource.value = sourceImage
      }
    }

    // 下载模型
    const downloadResource = (model) => {
      if (!model) return

      // 如果模型还没有完成
      if (model.status !== 'completed' || !model.outputZipUrl) {
        alert('模型还未完成生成，无法下载')
        return
      }

      // 实际应用中应该使用正确的URL
      window.open(model.outputZipUrl, '_blank')
    }

    // 编辑资源（仅管理员）
    const editResource = (resource) => {
      if (!resource) return
      alert(`模拟编辑资源: ${resource.name}`)
    }

    // 确认删除资源（仅管理员）
    const confirmDeleteResource = (resource) => {
      if (!resource) return
      if (confirm(`确定要删除资源 "${resource.name}" 吗？此操作不可撤销。`)) {
        alert(`模拟删除资源: ${resource.name}`)
        resourceModal.value.hide()
        loadResources()
      }
    }

    // 格式化日期
    const formatDate = (dateString, showTime = false) => {
      if (!dateString) return ''

      const date = new Date(dateString)
      const options = {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      }

      if (showTime) {
        options.hour = '2-digit'
        options.minute = '2-digit'
      }

      return date.toLocaleDateString('zh-CN', options)
    }

    return {
      loading,
      resources,
      filteredResources,
      searchQuery,
      modelStatus,
      currentPage,
      totalPages,
      paginationItems,
      selectedResource,
      isAdmin,

      setModelStatus,
      debounceSearch,
      search,
      resetFilters,
      changePage,
      viewResource,
      viewSourceImage,
      downloadResource,
      editResource,
      confirmDeleteResource,
      formatDate,
      getResultFileUrl
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

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 3rem 0;
}

/* 3D模型预览容器 */
.model-viewer-container {
  height: 400px;
  background-color: #f8f9fa;
  border-radius: 0.25rem;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .card-img-container {
    height: 150px;
  }

  .model-viewer-container {
    height: 300px;
  }
}
</style>
