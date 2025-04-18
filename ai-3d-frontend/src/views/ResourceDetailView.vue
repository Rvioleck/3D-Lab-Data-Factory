<template>
  <div class="resource-detail-view">
    <div class="container mt-4">
      <!-- 返回按钮 -->
      <div class="row mb-4">
        <div class="col-12">
          <router-link to="/resources" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left"></i> 返回资源库
          </router-link>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="row">
        <div class="col-12 text-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">加载中...</span>
          </div>
          <p class="mt-2">正在加载资源详情...</p>
        </div>
      </div>

      <!-- 资源不存在 -->
      <div v-else-if="!resource" class="row">
        <div class="col-12 text-center py-5">
          <div class="empty-state">
            <i class="bi bi-exclamation-triangle" style="font-size: 3rem;"></i>
            <h4 class="mt-3">资源不存在</h4>
            <p class="text-muted">该资源可能已被删除或您没有访问权限</p>
            <router-link to="/resources" class="btn btn-primary mt-2">
              返回资源库
            </router-link>
          </div>
        </div>
      </div>

      <!-- 资源详情 -->
      <div v-else class="row">
        <!-- 资源标题和操作按钮 -->
        <div class="col-12 mb-4">
          <div class="d-flex justify-content-between align-items-center">
            <h2>{{ resource.name }}</h2>
            <div class="resource-actions">
              <button class="btn btn-outline-primary me-2" @click="downloadResource">
                <i class="bi bi-download"></i> 下载
              </button>
              <div v-if="isAdmin" class="btn-group">
                <button class="btn btn-outline-secondary" @click="showEditModal">
                  <i class="bi bi-pencil"></i> 编辑
                </button>
                <button class="btn btn-outline-danger" @click="confirmDelete">
                  <i class="bi bi-trash"></i> 删除
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 资源预览和详情 -->
        <div class="col-md-8">
          <div class="card mb-4">
            <div class="card-header">
              <h5>资源预览</h5>
            </div>
            <div class="card-body p-0">
              <!-- 图片预览 -->
              <div v-if="resource.type === 'image'" class="image-preview">
                <img :src="resource.url" class="img-fluid" alt="资源预览">
              </div>
              
              <!-- 3D模型预览 -->
              <div v-else-if="resource.type === 'model'" class="model-preview">
                <ModelViewer 
                  :objUrl="resource.objUrl"
                  :mtlUrl="resource.mtlUrl"
                  :textureUrl="resource.textureUrl"
                />
              </div>
            </div>
          </div>
        </div>

        <div class="col-md-4">
          <!-- 基本信息 -->
          <div class="card mb-4">
            <div class="card-header">
              <h5>基本信息</h5>
            </div>
            <div class="card-body">
              <table class="table table-sm">
                <tbody>
                  <tr>
                    <th scope="row" width="40%">ID</th>
                    <td>{{ resource.id }}</td>
                  </tr>
                  <tr>
                    <th scope="row">类型</th>
                    <td>
                      <span class="badge" :class="resource.type === 'image' ? 'bg-info' : 'bg-primary'">
                        {{ resource.type === 'image' ? '图片' : '3D模型' }}
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <th scope="row">创建时间</th>
                    <td>{{ formatDate(resource.createTime, true) }}</td>
                  </tr>
                  <tr>
                    <th scope="row">更新时间</th>
                    <td>{{ formatDate(resource.updateTime, true) }}</td>
                  </tr>
                  <tr v-if="resource.type === 'model'">
                    <th scope="row">来源图片</th>
                    <td>
                      <router-link 
                        v-if="resource.sourceImageId" 
                        :to="`/resources/${resource.sourceImageId}`"
                      >
                        查看来源图片
                      </router-link>
                      <span v-else class="text-muted">无来源图片</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 标签 -->
          <div class="card mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
              <h5 class="mb-0">标签</h5>
              <button 
                v-if="isAdmin" 
                class="btn btn-sm btn-outline-primary"
                @click="showTagsModal"
              >
                <i class="bi bi-pencil"></i> 编辑标签
              </button>
            </div>
            <div class="card-body">
              <div class="resource-tags">
                <span 
                  v-for="tag in resource.tags" 
                  :key="tag"
                  class="badge bg-light text-dark me-1 mb-1"
                >
                  {{ tag }}
                </span>
                <span v-if="resource.tags.length === 0" class="text-muted">
                  暂无标签
                </span>
              </div>
            </div>
          </div>

          <!-- 技术参数 -->
          <div v-if="resource.type === 'model'" class="card mb-4">
            <div class="card-header">
              <h5>技术参数</h5>
            </div>
            <div class="card-body">
              <table class="table table-sm">
                <tbody>
                  <tr>
                    <th scope="row" width="40%">顶点数</th>
                    <td>{{ resource.vertices || '未知' }}</td>
                  </tr>
                  <tr>
                    <th scope="row">面数</th>
                    <td>{{ resource.faces || '未知' }}</td>
                  </tr>
                  <tr>
                    <th scope="row">材质数</th>
                    <td>{{ resource.materials || '未知' }}</td>
                  </tr>
                  <tr>
                    <th scope="row">纹理分辨率</th>
                    <td>{{ resource.textureResolution || '未知' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 图片参数 -->
          <div v-if="resource.type === 'image'" class="card mb-4">
            <div class="card-header">
              <h5>图片参数</h5>
            </div>
            <div class="card-body">
              <table class="table table-sm">
                <tbody>
                  <tr>
                    <th scope="row" width="40%">分辨率</th>
                    <td>{{ resource.resolution || '未知' }}</td>
                  </tr>
                  <tr>
                    <th scope="row">文件大小</th>
                    <td>{{ formatFileSize(resource.fileSize) }}</td>
                  </tr>
                  <tr>
                    <th scope="row">文件格式</th>
                    <td>{{ resource.format || '未知' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑资源模态框 -->
    <div class="modal fade" id="editResourceModal" tabindex="-1" aria-hidden="true" ref="editModal">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">编辑资源</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="关闭"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveResourceChanges">
              <div class="mb-3">
                <label for="resourceName" class="form-label">资源名称</label>
                <input 
                  type="text" 
                  class="form-control" 
                  id="resourceName" 
                  v-model="editForm.name" 
                  required
                >
              </div>
              <div class="mb-3">
                <label for="resourceDescription" class="form-label">描述</label>
                <textarea 
                  class="form-control" 
                  id="resourceDescription" 
                  v-model="editForm.description" 
                  rows="3"
                ></textarea>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" @click="saveResourceChanges">保存</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑标签模态框 -->
    <div class="modal fade" id="editTagsModal" tabindex="-1" aria-hidden="true" ref="tagsModal">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">编辑标签</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="关闭"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">当前标签</label>
              <div class="current-tags mb-3">
                <span 
                  v-for="tag in editTags" 
                  :key="tag"
                  class="badge bg-primary me-1 mb-1"
                >
                  {{ tag }}
                  <i class="bi bi-x-circle ms-1" @click="removeTag(tag)"></i>
                </span>
                <span v-if="editTags.length === 0" class="text-muted">
                  暂无标签
                </span>
              </div>
              
              <label class="form-label">添加标签</label>
              <div class="input-group">
                <input 
                  type="text" 
                  class="form-control" 
                  v-model="newTag" 
                  placeholder="输入标签名称"
                  @keyup.enter="addTag"
                >
                <button class="btn btn-outline-primary" type="button" @click="addTag">
                  添加
                </button>
              </div>
              
              <div class="form-text">
                按Enter键或点击添加按钮添加标签
              </div>
            </div>
            
            <div class="mb-3">
              <label class="form-label">推荐标签</label>
              <div class="suggested-tags">
                <span 
                  v-for="tag in suggestedTags" 
                  :key="tag"
                  class="badge bg-light text-dark me-1 mb-1 clickable"
                  @click="addSuggestedTag(tag)"
                >
                  {{ tag }}
                  <i class="bi bi-plus-circle ms-1"></i>
                </span>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" @click="saveTagChanges">保存</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { Modal } from 'bootstrap'
import ModelViewer from '@/components/ModelViewer.vue'

// 导入资源API
// import { getResourceById, updateResource, deleteResource, addResourceTags, removeResourceTags, getResourceTags } from '@/api/resource'

export default {
  name: 'ResourceDetailView',
  components: {
    ModelViewer
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()
    
    // 状态变量
    const loading = ref(true)
    const resource = ref(null)
    const editModal = ref(null)
    const tagsModal = ref(null)
    const editForm = ref({
      name: '',
      description: ''
    })
    const editTags = ref([])
    const newTag = ref('')
    const suggestedTags = ref([
      '实验室', '器材', '显微镜', '试管', '培养皿', '高清', '低多边形', '高精度'
    ])
    
    // 计算属性
    const isAdmin = computed(() => store.getters['user/isAdmin'])
    const resourceId = computed(() => route.params.id)
    
    // 模拟资源数据 - 实际应用中应该从API获取
    const mockResource = {
      id: '1',
      name: '实验室显微镜',
      type: 'image',
      url: 'https://via.placeholder.com/800x600?text=显微镜',
      tags: ['实验室', '器材', '显微镜', '高清'],
      createTime: '2023-04-15T10:30:00Z',
      updateTime: '2023-04-15T10:30:00Z',
      resolution: '1920x1080',
      fileSize: 1024 * 1024 * 2.5, // 2.5MB
      format: 'JPEG'
    }
    
    // 初始化
    onMounted(async () => {
      // 初始化Bootstrap模态框
      editModal.value = new Modal(document.getElementById('editResourceModal'))
      tagsModal.value = new Modal(document.getElementById('editTagsModal'))
      
      await loadResourceData()
    })
    
    // 监听路由参数变化，重新加载资源
    watch(() => route.params.id, async () => {
      await loadResourceData()
    })
    
    // 加载资源数据
    const loadResourceData = async () => {
      loading.value = true
      try {
        // 实际应用中应该调用API
        // const response = await getResourceById(resourceId.value)
        // resource.value = response.data
        
        // 模拟API响应
        setTimeout(() => {
          resource.value = mockResource
          
          // 初始化编辑表单
          editForm.value.name = resource.value.name
          editForm.value.description = resource.value.description || ''
          
          // 初始化标签编辑
          editTags.value = [...resource.value.tags]
          
          loading.value = false
        }, 500)
      } catch (error) {
        console.error('加载资源详情失败:', error)
        loading.value = false
      }
    }
    
    // 下载资源
    const downloadResource = () => {
      if (!resource.value) return
      
      // 实际应用中应该调用API
      // downloadResource(resource.value.id)
      
      // 模拟下载
      alert(`模拟下载资源: ${resource.value.name}`)
    }
    
    // 显示编辑模态框
    const showEditModal = () => {
      if (!resource.value) return
      
      // 重置表单
      editForm.value.name = resource.value.name
      editForm.value.description = resource.value.description || ''
      
      // 显示模态框
      editModal.value.show()
    }
    
    // 保存资源修改
    const saveResourceChanges = async () => {
      if (!resource.value) return
      
      try {
        // 实际应用中应该调用API
        // await updateResource(resource.value.id, editForm.value)
        
        // 模拟API响应
        resource.value.name = editForm.value.name
        resource.value.description = editForm.value.description
        
        // 关闭模态框
        editModal.value.hide()
        
        // 显示成功消息
        alert('资源信息已更新')
      } catch (error) {
        console.error('更新资源失败:', error)
        alert('更新资源失败: ' + (error.message || '未知错误'))
      }
    }
    
    // 确认删除
    const confirmDelete = () => {
      if (!resource.value) return
      
      if (confirm(`确定要删除资源 "${resource.value.name}" 吗？此操作不可撤销。`)) {
        deleteResource()
      }
    }
    
    // 删除资源
    const deleteResource = async () => {
      if (!resource.value) return
      
      try {
        // 实际应用中应该调用API
        // await deleteResource(resource.value.id)
        
        // 模拟API响应
        alert(`资源 "${resource.value.name}" 已删除`)
        
        // 返回资源库页面
        router.push('/resources')
      } catch (error) {
        console.error('删除资源失败:', error)
        alert('删除资源失败: ' + (error.message || '未知错误'))
      }
    }
    
    // 显示标签编辑模态框
    const showTagsModal = () => {
      if (!resource.value) return
      
      // 重置标签编辑状态
      editTags.value = [...resource.value.tags]
      newTag.value = ''
      
      // 显示模态框
      tagsModal.value.show()
    }
    
    // 添加标签
    const addTag = () => {
      if (!newTag.value.trim()) return
      
      // 检查标签是否已存在
      if (!editTags.value.includes(newTag.value.trim())) {
        editTags.value.push(newTag.value.trim())
      }
      
      // 清空输入
      newTag.value = ''
    }
    
    // 添加推荐标签
    const addSuggestedTag = (tag) => {
      if (!editTags.value.includes(tag)) {
        editTags.value.push(tag)
      }
    }
    
    // 移除标签
    const removeTag = (tag) => {
      editTags.value = editTags.value.filter(t => t !== tag)
    }
    
    // 保存标签修改
    const saveTagChanges = async () => {
      if (!resource.value) return
      
      try {
        // 计算要添加和删除的标签
        const originalTags = resource.value.tags
        const tagsToAdd = editTags.value.filter(tag => !originalTags.includes(tag))
        const tagsToRemove = originalTags.filter(tag => !editTags.value.includes(tag))
        
        // 实际应用中应该调用API
        // if (tagsToAdd.length > 0) {
        //   await addResourceTags(resource.value.id, tagsToAdd)
        // }
        // if (tagsToRemove.length > 0) {
        //   await removeResourceTags(resource.value.id, tagsToRemove)
        // }
        
        // 模拟API响应
        resource.value.tags = [...editTags.value]
        
        // 关闭模态框
        tagsModal.value.hide()
        
        // 显示成功消息
        alert('标签已更新')
      } catch (error) {
        console.error('更新标签失败:', error)
        alert('更新标签失败: ' + (error.message || '未知错误'))
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
    
    // 格式化文件大小
    const formatFileSize = (bytes) => {
      if (bytes === undefined || bytes === null) return '未知'
      
      const units = ['B', 'KB', 'MB', 'GB', 'TB']
      let size = bytes
      let unitIndex = 0
      
      while (size >= 1024 && unitIndex < units.length - 1) {
        size /= 1024
        unitIndex++
      }
      
      return `${size.toFixed(2)} ${units[unitIndex]}`
    }
    
    return {
      loading,
      resource,
      isAdmin,
      editForm,
      editTags,
      newTag,
      suggestedTags,
      
      downloadResource,
      showEditModal,
      saveResourceChanges,
      confirmDelete,
      showTagsModal,
      addTag,
      addSuggestedTag,
      removeTag,
      saveTagChanges,
      formatDate,
      formatFileSize
    }
  }
}
</script>

<style scoped>
.image-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f8f9fa;
  min-height: 300px;
  max-height: 600px;
  overflow: hidden;
}

.image-preview img {
  max-width: 100%;
  max-height: 600px;
  object-fit: contain;
}

.model-preview {
  height: 500px;
  background-color: #f8f9fa;
}

.resource-tags .badge {
  font-size: 0.85rem;
  padding: 0.4rem 0.6rem;
}

.current-tags .badge {
  cursor: pointer;
}

.current-tags .badge i {
  opacity: 0.7;
}

.current-tags .badge:hover i {
  opacity: 1;
}

.suggested-tags .badge {
  cursor: pointer;
  transition: all 0.2s;
}

.suggested-tags .badge:hover {
  background-color: #e9ecef !important;
}

.clickable {
  cursor: pointer;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 3rem 0;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .resource-actions {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .model-preview {
    height: 350px;
  }
}
</style>
