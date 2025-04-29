<template>
  <div class="user-activity-log">
    <div class="card">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h5 class="mb-0">账号活动记录</h5>
        <div>
          <button class="btn btn-sm btn-outline-secondary" @click="refreshLogs">
            <i class="bi bi-arrow-clockwise me-1"></i>刷新
          </button>
        </div>
      </div>
      <div class="card-body">
        <div v-if="loading" class="text-center py-4">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">加载中...</span>
          </div>
        </div>
        <div v-else-if="logs.length === 0" class="text-center py-4">
          <i class="bi bi-clock-history text-muted" style="font-size: 3rem;"></i>
          <p class="mt-3 text-muted">暂无活动记录</p>
        </div>
        <div v-else class="activity-list">
          <div v-for="(log, index) in logs" :key="index" class="activity-item">
            <div class="activity-icon">
              <i class="bi" :class="getActivityIcon(log.type)"></i>
            </div>
            <div class="activity-content">
              <div class="activity-title">{{ getActivityTitle(log.type) }}</div>
              <div class="activity-description">{{ log.description }}</div>
              <div class="activity-time text-muted">{{ formatDateTime(log.time) }}</div>
            </div>
          </div>
        </div>
      </div>
      <div class="card-footer text-center" v-if="logs.length > 0">
        <button class="btn btn-sm btn-outline-primary" @click="loadMore" :disabled="!hasMore || loadingMore">
          <span v-if="loadingMore" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'

export default {
  name: 'UserActivityLog',
  props: {
    userId: {
      type: [Number, String],
      required: true
    }
  },
  setup(props) {
    const logs = ref([])
    const loading = ref(true)
    const loadingMore = ref(false)
    const hasMore = ref(false)
    const page = ref(1)
    
    // 模拟活动类型
    const ACTIVITY_TYPES = {
      LOGIN: 'login',
      LOGOUT: 'logout',
      PROFILE_UPDATE: 'profile_update',
      PASSWORD_CHANGE: 'password_change',
      MODEL_CREATE: 'model_create',
      MODEL_DELETE: 'model_delete'
    }
    
    // 获取活动图标
    const getActivityIcon = (type) => {
      switch (type) {
        case ACTIVITY_TYPES.LOGIN: return 'bi-box-arrow-in-right text-success'
        case ACTIVITY_TYPES.LOGOUT: return 'bi-box-arrow-left text-danger'
        case ACTIVITY_TYPES.PROFILE_UPDATE: return 'bi-person-gear text-primary'
        case ACTIVITY_TYPES.PASSWORD_CHANGE: return 'bi-shield-lock text-warning'
        case ACTIVITY_TYPES.MODEL_CREATE: return 'bi-plus-circle text-info'
        case ACTIVITY_TYPES.MODEL_DELETE: return 'bi-trash text-danger'
        default: return 'bi-activity'
      }
    }
    
    // 获取活动标题
    const getActivityTitle = (type) => {
      switch (type) {
        case ACTIVITY_TYPES.LOGIN: return '登录系统'
        case ACTIVITY_TYPES.LOGOUT: return '退出系统'
        case ACTIVITY_TYPES.PROFILE_UPDATE: return '更新个人资料'
        case ACTIVITY_TYPES.PASSWORD_CHANGE: return '修改密码'
        case ACTIVITY_TYPES.MODEL_CREATE: return '创建3D模型'
        case ACTIVITY_TYPES.MODEL_DELETE: return '删除3D模型'
        default: return '未知活动'
      }
    }
    
    // 格式化日期时间
    const formatDateTime = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString()
    }
    
    // 加载活动日志
    const loadLogs = async (isRefresh = false) => {
      if (isRefresh) {
        loading.value = true
        page.value = 1
      } else {
        loadingMore.value = true
      }
      
      try {
        // 这里应该调用获取用户活动日志的API
        // 目前后端可能没有这个接口，所以这里只是模拟数据
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 模拟数据
        const mockLogs = [
          {
            type: ACTIVITY_TYPES.LOGIN,
            description: '从IP 192.168.1.1登录系统',
            time: new Date(Date.now() - 3600000).toISOString()
          },
          {
            type: ACTIVITY_TYPES.PROFILE_UPDATE,
            description: '更新了个人资料',
            time: new Date(Date.now() - 7200000).toISOString()
          },
          {
            type: ACTIVITY_TYPES.MODEL_CREATE,
            description: '创建了3D模型 "客厅沙发"',
            time: new Date(Date.now() - 86400000).toISOString()
          }
        ]
        
        if (isRefresh) {
          logs.value = mockLogs
        } else {
          logs.value = [...logs.value, ...mockLogs]
        }
        
        // 模拟是否有更多数据
        hasMore.value = page.value < 3
        page.value++
      } catch (error) {
        console.error('获取活动日志失败:', error)
      } finally {
        loading.value = false
        loadingMore.value = false
      }
    }
    
    // 刷新日志
    const refreshLogs = () => {
      loadLogs(true)
    }
    
    // 加载更多
    const loadMore = () => {
      if (!hasMore.value || loadingMore.value) return
      loadLogs()
    }
    
    // 组件挂载时加载日志
    onMounted(() => {
      loadLogs(true)
    })
    
    return {
      logs,
      loading,
      loadingMore,
      hasMore,
      getActivityIcon,
      getActivityTitle,
      formatDateTime,
      refreshLogs,
      loadMore
    }
  }
}
</script>

<style scoped>
.user-activity-log {
  margin-bottom: 1.5rem;
}

.activity-list {
  max-height: 400px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  padding: 1rem 0;
  border-bottom: 1px solid var(--border-color);
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
  flex-shrink: 0;
}

.activity-icon i {
  font-size: 1.25rem;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.activity-description {
  margin-bottom: 0.25rem;
  color: var(--text-color);
}

.activity-time {
  font-size: 0.875rem;
}

.card-footer {
  background-color: transparent;
  border-top: 1px solid var(--border-color);
}
</style>
