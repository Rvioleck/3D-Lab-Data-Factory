<template>
  <div class="modern-home-view">
    <!-- 顶部英雄区域 -->
    <div class="hero-section glass-background">
      <div class="container py-5">
        <div class="row align-items-center">
          <div class="col-lg-6">
            <h1 class="display-4 fw-bold text-3d mb-4">
              <span class="gradient-text-primary">AI 驱动的</span>
              <br />3D 内容创作平台
            </h1>
            <p class="lead mb-4">
              使用先进的人工智能技术，轻松创建、管理和分享高质量3D模型和图像
            </p>
            <div class="d-flex gap-3 mb-5">
              <button class="btn-3d glass-button gradient-bg-primary">
                开始创建
              </button>
              <button class="btn-3d glass-button">
                了解更多
              </button>
            </div>
          </div>
          <div class="col-lg-6">
            <div class="hero-image-container">
              <!-- 3D模型展示区 -->
              <div class="model-viewer-3d">
                <div class="loader-3d" v-if="loading">
                  <div class="cube">
                    <div class="face front"></div>
                    <div class="face back"></div>
                    <div class="face right"></div>
                    <div class="face left"></div>
                    <div class="face top"></div>
                    <div class="face bottom"></div>
                  </div>
                </div>
                <!-- 这里可以放置3D模型查看器组件 -->
                <div
                  v-if="!loading"
                  class="model-placeholder img-fluid rounded"
                >
                  <div class="model-placeholder-content">
                    <i class="bi bi-cube"></i>
                    <span>3D模型预览</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 功能特点区域 -->
    <div class="features-section py-5">
      <div class="container">
        <h2 class="text-center mb-5 text-3d">平台特点</h2>
        <div class="row g-4">
          <!-- 特点卡片 -->
          <div class="col-md-4">
            <div class="neu-card card-3d h-100">
              <div class="card-content">
                <div class="feature-icon icon-3d mb-4">
                  <i class="bi bi-image gradient-text-primary"></i>
                </div>
                <h3>AI 图像生成</h3>
                <p>通过简单的文本描述，利用AI技术生成高质量的图像资源</p>
              </div>
            </div>
          </div>

          <div class="col-md-4">
            <div class="neu-card card-3d h-100">
              <div class="card-content">
                <div class="feature-icon icon-3d mb-4">
                  <i class="bi bi-box gradient-text-secondary"></i>
                </div>
                <h3>3D 模型创建</h3>
                <p>从2D图像自动生成精确的3D模型，无需专业建模技能</p>
              </div>
            </div>
          </div>

          <div class="col-md-4">
            <div class="neu-card card-3d h-100">
              <div class="card-content">
                <div class="feature-icon icon-3d mb-4">
                  <i class="bi bi-chat-dots gradient-text-accent"></i>
                </div>
                <h3>AI 助手对话</h3>
                <p>与智能AI助手交流，获取创作建议和技术支持</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 用户仪表盘区域 -->
    <div class="dashboard-section py-5 bg-secondary">
      <div class="container">
        <div class="glass-card">
          <h2 class="mb-4">欢迎回来，{{ currentUser.userName || currentUser.userAccount }}</h2>

          <div class="divider-3d mb-4"></div>

          <div class="row g-4">
            <!-- 快速访问卡片 -->
            <div class="col-md-6 col-lg-3">
              <div class="glass-card h-100">
                <div class="d-flex align-items-center">
                  <div class="dashboard-icon me-3">
                    <i class="bi bi-image"></i>
                  </div>
                  <div>
                    <h4 class="mb-1">图片库</h4>
                    <p class="mb-0">{{ imageCount || 0 }} 个图片</p>
                  </div>
                </div>
                <router-link to="/images" class="stretched-link"></router-link>
              </div>
            </div>

            <div class="col-md-6 col-lg-3">
              <div class="glass-card h-100">
                <div class="d-flex align-items-center">
                  <div class="dashboard-icon me-3">
                    <i class="bi bi-box"></i>
                  </div>
                  <div>
                    <h4 class="mb-1">模型库</h4>
                    <p class="mb-0">{{ modelCount || 0 }} 个模型</p>
                  </div>
                </div>
                <router-link to="/models" class="stretched-link"></router-link>
              </div>
            </div>

            <div class="col-md-6 col-lg-3">
              <div class="glass-card h-100">
                <div class="d-flex align-items-center">
                  <div class="dashboard-icon me-3">
                    <i class="bi bi-chat-dots"></i>
                  </div>
                  <div>
                    <h4 class="mb-1">AI 聊天</h4>
                    <p class="mb-0">{{ chatCount || 0 }} 个会话</p>
                  </div>
                </div>
                <router-link to="/chat" class="stretched-link"></router-link>
              </div>
            </div>

            <div class="col-md-6 col-lg-3">
              <div class="glass-card h-100">
                <div class="d-flex align-items-center">
                  <div class="dashboard-icon me-3">
                    <i class="bi bi-gear"></i>
                  </div>
                  <div>
                    <h4 class="mb-1">设置</h4>
                    <p class="mb-0">个人资料与偏好</p>
                  </div>
                </div>
                <router-link to="/settings" class="stretched-link"></router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近活动区域 -->
    <div class="recent-activity-section py-5">
      <div class="container">
        <h2 class="mb-4 text-3d">最近活动</h2>

        <div class="row">
          <div class="col-lg-8">
            <!-- 最近创建的内容 -->
            <div class="neu-card mb-4">
              <h3 class="mb-3">最近创建</h3>
              <div class="row g-3">
                <div class="col-md-4" v-for="(item, index) in recentItems" :key="index">
                  <div class="recent-item card-3d">
                    <div class="recent-item-preview" :style="{ backgroundColor: item.color }">
                      <div class="recent-item-icon">
                        <i class="bi" :class="item.icon"></i>
                      </div>
                    </div>
                    <div class="recent-item-info mt-2">
                      <h5 class="mb-1">{{ item.name }}</h5>
                      <p class="small text-muted mb-0">{{ item.date }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="col-lg-4">
            <!-- 系统状态 -->
            <div class="glass-card mb-4">
              <h3 class="mb-3">系统状态</h3>
              <div class="system-status">
                <div class="status-item d-flex justify-content-between align-items-center mb-2">
                  <span>API 服务</span>
                  <span class="badge bg-success">正常</span>
                </div>
                <div class="status-item d-flex justify-content-between align-items-center mb-2">
                  <span>AI 模型</span>
                  <span class="badge bg-success">正常</span>
                </div>
                <div class="status-item d-flex justify-content-between align-items-center mb-2">
                  <span>存储服务</span>
                  <span class="badge bg-success">正常</span>
                </div>
                <div class="glass-divider my-3"></div>
                <div class="text-center">
                  <router-link to="/health" class="btn btn-sm glass-button">
                    查看详情
                  </router-link>
                </div>
              </div>
            </div>

            <!-- 用户角色信息 -->
            <div class="glass-card">
              <div class="user-role-info text-center">
                <div class="avatar mb-3">
                  <i class="bi bi-person-circle"></i>
                </div>
                <h4>{{ currentUser.userName || currentUser.userAccount }}</h4>
                <div class="badge mb-3" :class="roleClass">{{ roleText }}</div>
                <div class="glass-divider my-3"></div>
                <div v-if="isAdmin" class="mt-3">
                  <router-link to="/admin/dashboard" class="btn glass-button">
                    管理控制台
                  </router-link>
                </div>
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
import { useStore } from 'vuex'

export default {
  name: 'ModernHomeView',
  setup() {
    const store = useStore()
    const loading = ref(true)

    // 模拟数据
    const imageCount = ref(12)
    const modelCount = ref(5)
    const chatCount = ref(3)

    const recentItems = ref([
      {
        name: '实验室场景',
        previewUrl: null,
        color: '#4f46e5',
        icon: 'bi-flask',
        date: '2023-06-15'
      },
      {
        name: '医疗设备',
        previewUrl: null,
        color: '#8338ec',
        icon: 'bi-heart-pulse',
        date: '2023-06-14'
      },
      {
        name: '显微镜',
        previewUrl: null,
        color: '#ff006e',
        icon: 'bi-microscope',
        date: '2023-06-12'
      }
    ])

    const currentUser = computed(() => store.getters['user/currentUser'] || {})
    const isAdmin = computed(() => store.getters['user/isAdmin'])

    const roleText = computed(() => {
      if (!currentUser.value) return '未登录'

      switch (currentUser.value.userRole) {
        case 'admin': return '管理员'
        case 'user': return '普通用户'
        case 'ban': return '已封禁'
        default: return '未知角色'
      }
    })

    const roleClass = computed(() => {
      if (!currentUser.value) return 'bg-secondary'

      switch (currentUser.value.userRole) {
        case 'admin': return 'bg-primary'
        case 'user': return 'bg-success'
        case 'ban': return 'bg-danger'
        default: return 'bg-secondary'
      }
    })

    onMounted(() => {
      // 模拟加载
      setTimeout(() => {
        loading.value = false
      }, 1500)
    })

    return {
      currentUser,
      isAdmin,
      roleText,
      roleClass,
      loading,
      imageCount,
      modelCount,
      chatCount,
      recentItems
    }
  }
}
</script>

<style scoped>
.modern-home-view {
  min-height: 100vh;
}

/* 英雄区域样式 */
.hero-section {
  position: relative;
  padding: 80px 0;
  background: var(--gradient-primary);
  color: white;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: radial-gradient(circle at 10px 10px, rgba(255, 255, 255, 0.1) 2px, transparent 0);
  background-size: 20px 20px;
  opacity: 0.2;
  z-index: 0;
}

.hero-section .container {
  position: relative;
  z-index: 1;
}

.hero-image-container {
  position: relative;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 模型占位符样式 */
.model-placeholder {
  height: 300px;
  width: 100%;
  background: linear-gradient(135deg, #4f46e5 0%, #8338ec 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.model-placeholder-content {
  text-align: center;
  color: white;
}

.model-placeholder-content i {
  font-size: 4rem;
  margin-bottom: 1rem;
  display: block;
}

.model-placeholder-content span {
  font-size: 1.2rem;
  font-weight: 500;
}

/* 特点区域样式 */
.features-section {
  background-color: var(--bg-secondary);
  padding: 80px 0;
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1.5rem;
  display: inline-block;
}

/* 仪表盘区域样式 */
.dashboard-section {
  background: var(--gradient-dark, var(--gradient-primary));
  padding: 60px 0;
}

.dashboard-icon {
  font-size: 2rem;
  color: var(--primary-color);
}

/* 最近活动区域样式 */
.recent-activity-section {
  background-color: var(--bg-primary);
  padding: 60px 0;
}

.recent-item {
  transition: transform 0.3s ease;
  cursor: pointer;
}

.recent-item:hover {
  transform: translateY(-5px);
}

.recent-item-preview {
  height: 150px;
  overflow: hidden;
  border-radius: 8px;
  background-color: var(--bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.recent-item-icon {
  font-size: 3rem;
  color: white;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: var(--bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  font-size: 2.5rem;
  color: var(--primary-color);
}

/* 响应式调整 */
@media (max-width: 992px) {
  .hero-section {
    padding: 60px 0;
  }

  .hero-image-container {
    height: 300px;
    margin-top: 2rem;
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 40px 0;
  }

  .hero-image-container {
    height: 250px;
  }
}
</style>
