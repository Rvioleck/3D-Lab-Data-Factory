<template>
  <div class="admin-dashboard">
    <div class="container py-4">
      <div class="page-header">
        <h1 class="page-title">管理员控制面板</h1>
        <p class="page-subtitle">管理系统资源和用户</p>
      </div>

      <div class="dashboard-stats">
        <div class="row">
          <div class="col-md-3 col-sm-6 mb-4">
            <div class="stat-card">
              <div class="stat-card-body">
                <div class="stat-card-icon">
                  <i class="bi bi-people"></i>
                </div>
                <div class="stat-card-content">
                  <h3 class="stat-card-title">用户</h3>
                  <div class="stat-card-value">{{ userCount }}</div>
                  <div class="stat-card-change" :class="userChangeClass">
                    <i :class="userChangeIcon"></i> {{ userChange }}%
                  </div>
                </div>
              </div>
              <div class="stat-card-footer">
                <router-link to="/admin/users" class="stat-card-link">
                  管理用户 <i class="bi bi-arrow-right"></i>
                </router-link>
              </div>
            </div>
          </div>

          <div class="col-md-3 col-sm-6 mb-4">
            <div class="stat-card">
              <div class="stat-card-body">
                <div class="stat-card-icon">
                  <i class="bi bi-images"></i>
                </div>
                <div class="stat-card-content">
                  <h3 class="stat-card-title">图片</h3>
                  <div class="stat-card-value">{{ imageCount }}</div>
                  <div class="stat-card-change" :class="imageChangeClass">
                    <i :class="imageChangeIcon"></i> {{ imageChange }}%
                  </div>
                </div>
              </div>
              <div class="stat-card-footer">
                <router-link to="/admin/images" class="stat-card-link">
                  管理图片 <i class="bi bi-arrow-right"></i>
                </router-link>
              </div>
            </div>
          </div>

          <div class="col-md-3 col-sm-6 mb-4">
            <div class="stat-card">
              <div class="stat-card-body">
                <div class="stat-card-icon">
                  <i class="bi bi-box"></i>
                </div>
                <div class="stat-card-content">
                  <h3 class="stat-card-title">模型</h3>
                  <div class="stat-card-value">{{ modelCount }}</div>
                  <div class="stat-card-change" :class="modelChangeClass">
                    <i :class="modelChangeIcon"></i> {{ modelChange }}%
                  </div>
                </div>
              </div>
              <div class="stat-card-footer">
                <router-link to="/admin/models" class="stat-card-link">
                  管理模型 <i class="bi bi-arrow-right"></i>
                </router-link>
              </div>
            </div>
          </div>

          <div class="col-md-3 col-sm-6 mb-4">
            <div class="stat-card">
              <div class="stat-card-body">
                <div class="stat-card-icon">
                  <i class="bi bi-activity"></i>
                </div>
                <div class="stat-card-content">
                  <h3 class="stat-card-title">系统状态</h3>
                  <div class="stat-card-value">
                    <span :class="systemStatusClass">{{ systemStatus }}</span>
                  </div>
                  <div class="stat-card-info">CPU: {{ cpuUsage }}%</div>
                </div>
              </div>
              <div class="stat-card-footer">
                <router-link to="/health" class="stat-card-link">
                  查看详情 <i class="bi bi-arrow-right"></i>
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="row mt-4">
        <div class="col-md-6 mb-4">
          <div class="card">
            <div class="card-header">
              <h5 class="card-title">最近活动</h5>
            </div>
            <div class="card-body">
              <div class="activity-list">
                <div v-for="(activity, index) in recentActivities" :key="index" class="activity-item">
                  <div class="activity-icon" :class="getActivityIconClass(activity.type)">
                    <i :class="getActivityIcon(activity.type)"></i>
                  </div>
                  <div class="activity-content">
                    <div class="activity-title">{{ activity.title }}</div>
                    <div class="activity-time">{{ activity.time }}</div>
                  </div>
                </div>
              </div>
            </div>
            <div class="card-footer">
              <router-link to="/admin/logs" class="btn btn-sm btn-outline-primary">
                查看所有日志
              </router-link>
            </div>
          </div>
        </div>

        <div class="col-md-6 mb-4">
          <div class="card">
            <div class="card-header">
              <h5 class="card-title">快速操作</h5>
            </div>
            <div class="card-body">
              <div class="quick-actions">
                <router-link to="/admin/users/create" class="quick-action-btn">
                  <i class="bi bi-person-plus"></i>
                  <span>创建用户</span>
                </router-link>
                <router-link to="/reconstruction" class="quick-action-btn">
                  <i class="bi bi-box-seam"></i>
                  <span>3D重建</span>
                </router-link>
                <router-link to="/chat" class="quick-action-btn">
                  <i class="bi bi-chat-dots"></i>
                  <span>数据分析</span>
                </router-link>
                <router-link to="/admin/settings" class="quick-action-btn">
                  <i class="bi bi-gear"></i>
                  <span>系统设置</span>
                </router-link>
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
import { useRouter } from 'vue-router'
import apiClient from '@/utils/apiClient'

export default {
  name: 'AdminDashboardView',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    
    // 统计数据
    const userCount = ref(0)
    const imageCount = ref(0)
    const modelCount = ref(0)
    const userChange = ref(5.2)
    const imageChange = ref(12.7)
    const modelChange = ref(-3.4)
    const systemStatus = ref('正常')
    const cpuUsage = ref(32)
    
    // 计算属性
    const userChangeClass = computed(() => userChange.value >= 0 ? 'positive' : 'negative')
    const imageChangeClass = computed(() => imageChange.value >= 0 ? 'positive' : 'negative')
    const modelChangeClass = computed(() => modelChange.value >= 0 ? 'positive' : 'negative')
    const userChangeIcon = computed(() => userChange.value >= 0 ? 'bi bi-arrow-up' : 'bi bi-arrow-down')
    const imageChangeIcon = computed(() => imageChange.value >= 0 ? 'bi bi-arrow-up' : 'bi bi-arrow-down')
    const modelChangeIcon = computed(() => modelChange.value >= 0 ? 'bi bi-arrow-up' : 'bi bi-arrow-down')
    const systemStatusClass = computed(() => {
      if (systemStatus.value === '正常') return 'text-success'
      if (systemStatus.value === '警告') return 'text-warning'
      if (systemStatus.value === '错误') return 'text-danger'
      return ''
    })
    
    // 最近活动
    const recentActivities = ref([
      { type: 'user', title: '新用户注册: 张三', time: '10分钟前' },
      { type: 'image', title: '上传了新图片: 产品展示图', time: '25分钟前' },
      { type: 'model', title: '创建了新模型: 建筑模型A', time: '1小时前' },
      { type: 'system', title: '系统更新完成', time: '2小时前' },
      { type: 'user', title: '用户李四更新了个人资料', time: '3小时前' }
    ])
    
    // 获取活动图标
    const getActivityIcon = (type) => {
      switch (type) {
        case 'user': return 'bi bi-person'
        case 'image': return 'bi bi-image'
        case 'model': return 'bi bi-box'
        case 'system': return 'bi bi-gear'
        default: return 'bi bi-activity'
      }
    }
    
    // 获取活动图标类名
    const getActivityIconClass = (type) => {
      switch (type) {
        case 'user': return 'bg-primary'
        case 'image': return 'bg-success'
        case 'model': return 'bg-warning'
        case 'system': return 'bg-info'
        default: return 'bg-secondary'
      }
    }
    
    // 获取统计数据
    const fetchStats = async () => {
      loading.value = true
      try {
        // 这里应该调用实际的API获取统计数据
        // 示例数据
        userCount.value = 128
        imageCount.value = 543
        modelCount.value = 217
      } catch (error) {
        console.error('获取统计数据失败:', error)
        if (window.$toast) {
          window.$toast.error('获取统计数据失败')
        }
      } finally {
        loading.value = false
      }
    }
    
    onMounted(() => {
      fetchStats()
    })
    
    return {
      router,
      loading,
      userCount,
      imageCount,
      modelCount,
      userChange,
      imageChange,
      modelChange,
      systemStatus,
      cpuUsage,
      userChangeClass,
      imageChangeClass,
      modelChangeClass,
      userChangeIcon,
      imageChangeIcon,
      modelChangeIcon,
      systemStatusClass,
      recentActivities,
      getActivityIcon,
      getActivityIconClass
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  background-color: var(--bg-secondary);
  min-height: calc(100vh - 70px);
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

/* 统计卡片 */
.stat-card {
  background-color: white;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-md);
}

.stat-card-body {
  padding: 1.5rem;
  display: flex;
  align-items: center;
  flex: 1;
}

.stat-card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background-color: var(--primary-light);
  color: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  margin-right: 1rem;
}

.stat-card-content {
  flex: 1;
}

.stat-card-title {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.stat-card-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.stat-card-change {
  font-size: 0.875rem;
  font-weight: 500;
}

.stat-card-change.positive {
  color: var(--success-color);
}

.stat-card-change.negative {
  color: var(--error-color);
}

.stat-card-info {
  font-size: 0.875rem;
  color: var(--text-tertiary);
}

.stat-card-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--border-color);
  background-color: var(--bg-secondary);
}

.stat-card-link {
  color: var(--primary-color);
  font-weight: 500;
  text-decoration: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-card-link:hover {
  text-decoration: underline;
}

/* 活动列表 */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.activity-item {
  display: flex;
  align-items: center;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.activity-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.activity-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
  color: white;
}

.activity-icon.bg-primary {
  background-color: var(--primary-color);
}

.activity-icon.bg-success {
  background-color: var(--success-color);
}

.activity-icon.bg-warning {
  background-color: var(--warning-color);
}

.activity-icon.bg-info {
  background-color: var(--info-color);
}

.activity-icon.bg-secondary {
  background-color: var(--secondary-color);
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-weight: 500;
  margin-bottom: 0.25rem;
  color: var(--text-primary);
}

.activity-time {
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

/* 快速操作 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.quick-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
  background-color: var(--bg-secondary);
  border-radius: var(--radius-lg);
  text-decoration: none;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.quick-action-btn:hover {
  background-color: var(--primary-light);
  color: var(--primary-color);
  transform: translateY(-3px);
}

.quick-action-btn i {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .quick-actions {
    grid-template-columns: 1fr;
  }
}
</style>
