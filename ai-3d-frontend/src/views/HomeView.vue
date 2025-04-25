<template>
  <div class="home-view">
    <!-- 顶部英雄区域 -->
    <section class="hero-section">
      <div class="container">
        <div class="row align-items-center">
          <div class="col-lg-6 hero-content">
            <h1 class="hero-title">
              <span class="accent-text">AI驱动</span>的3D内容创作平台
            </h1>
            <p class="hero-subtitle">
              使用先进的人工智能技术，轻松创建、管理和分享高质量3D模型和图像
            </p>
            <div class="hero-actions">
              <button class="btn btn-primary btn-lg">开始创建</button>
              <button class="btn btn-outline-light btn-lg">了解更多</button>
            </div>
          </div>
          <div class="col-lg-6 hero-visual">
            <div class="model-preview">
              <div class="model-placeholder" v-if="loading">
                <div class="spinner"></div>
              </div>
              <div class="model-canvas" v-else>
                <!-- 3D模型展示区域 -->
                <div class="model-decoration"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="hero-decoration">
        <div class="decoration-circle circle-1"></div>
        <div class="decoration-circle circle-2"></div>
        <div class="decoration-line line-1"></div>
        <div class="decoration-line line-2"></div>
      </div>
    </section>

    <!-- 主要内容区域 -->
    <section class="dashboard-section">
      <div class="container">
        <!-- 用户欢迎区域 -->
        <div class="user-welcome">
          <div class="user-info">
            <div class="user-avatar">
              <i class="bi bi-person-circle"></i>
            </div>
            <div class="user-details">
              <h2>欢迎回来，{{ currentUser.userName || currentUser.userAccount }}</h2>
              <div class="user-role" :class="roleClass">{{ roleText }}</div>
            </div>
          </div>
          <div class="quick-actions">
            <button class="action-btn">
              <i class="bi bi-plus-lg"></i>
              <span>新建项目</span>
            </button>
            <button class="action-btn">
              <i class="bi bi-upload"></i>
              <span>上传模型</span>
            </button>
            <button class="action-btn logout-btn" @click="handleLogout">
              <i class="bi bi-box-arrow-right"></i>
              <span>注销</span>
            </button>
          </div>
        </div>

        <!-- 数据概览卡片 -->
        <div class="stats-cards">
          <div class="row g-4">
            <div class="col-md-6 col-lg-3">
              <div class="stat-card">
                <div class="stat-icon">
                  <i class="bi bi-image"></i>
                </div>
                <div class="stat-info">
                  <h3>图片库</h3>
                  <div class="stat-value">{{ imageCount || 0 }}</div>
                  <router-link to="/images" class="stat-link">查看全部</router-link>
                </div>
              </div>
            </div>

            <div class="col-md-6 col-lg-3">
              <div class="stat-card">
                <div class="stat-icon">
                  <i class="bi bi-box"></i>
                </div>
                <div class="stat-info">
                  <h3>模型库</h3>
                  <div class="stat-value">{{ modelCount || 0 }}</div>
                  <router-link to="/models" class="stat-link">查看全部</router-link>
                </div>
              </div>
            </div>

            <div class="col-md-6 col-lg-3">
              <div class="stat-card">
                <div class="stat-icon">
                  <i class="bi bi-chat-dots"></i>
                </div>
                <div class="stat-info">
                  <h3>AI对话</h3>
                  <div class="stat-value">{{ chatCount || 0 }}</div>
                  <router-link to="/chat" class="stat-link">开始对话</router-link>
                </div>
              </div>
            </div>

            <div class="col-md-6 col-lg-3">
              <div class="stat-card">
                <div class="stat-icon">
                  <i class="bi bi-heart-pulse"></i>
                </div>
                <div class="stat-info">
                  <h3>系统状态</h3>
                  <div class="stat-value">正常</div>
                  <router-link to="/health" class="stat-link">查看详情</router-link>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 最近项目 -->
        <div class="recent-projects">
          <div class="section-header">
            <h2>最近项目</h2>
            <button class="btn btn-sm btn-outline-primary">查看全部</button>
          </div>

          <div class="row g-4">
            <div v-for="(item, index) in recentItems" :key="index" class="col-md-6 col-lg-4">
              <div class="project-card">
                <div class="project-preview" :style="{ backgroundColor: item.color }">
                  <i class="bi" :class="item.icon"></i>
                </div>
                <div class="project-info">
                  <h3>{{ item.name }}</h3>
                  <p class="project-date">{{ item.date }}</p>
                  <div class="project-actions">
                    <button class="btn btn-sm btn-icon">
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-icon">
                      <i class="bi bi-eye"></i>
                    </button>
                    <button class="btn btn-sm btn-icon">
                      <i class="bi bi-three-dots"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 功能区域 -->
        <div class="features-section">
          <div class="section-header">
            <h2>功能区</h2>
          </div>

          <div class="row g-4">
            <div class="col-md-6 col-lg-4">
              <div class="feature-card">
                <div class="feature-icon">
                  <i class="bi bi-magic"></i>
                </div>
                <div class="feature-content">
                  <h3>AI生成模型</h3>
                  <p>使用人工智能技术，通过文本描述生成3D模型</p>
                  <button class="btn btn-primary">开始生成</button>
                </div>
              </div>
            </div>

            <div class="col-md-6 col-lg-4">
              <div class="feature-card">
                <div class="feature-icon">
                  <i class="bi bi-brush"></i>
                </div>
                <div class="feature-content">
                  <h3>模型编辑</h3>
                  <p>编辑和优化您的3D模型，调整细节和材质</p>
                  <button class="btn btn-primary">开始编辑</button>
                </div>
              </div>
            </div>

            <div class="col-md-6 col-lg-4">
              <div class="feature-card">
                <div class="feature-icon">
                  <i class="bi bi-share"></i>
                </div>
                <div class="feature-content">
                  <h3>分享与导出</h3>
                  <p>分享您的作品或导出为多种格式</p>
                  <button class="btn btn-primary">查看选项</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { computed, ref } from 'vue'
import { useStore } from '@/utils/storeCompat'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

export default {
  name: 'HomeView',
  setup() {
    const store = useStore()
    const userStore = useUserStore()
    const router = useRouter()
    const loading = ref(false)

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
      if (!currentUser.value) return 'role-unknown'

      switch (currentUser.value.userRole) {
        case 'admin': return 'role-admin'
        case 'user': return 'role-user'
        case 'ban': return 'role-banned'
        default: return 'role-unknown'
      }
    })

    // 处理注销
    const handleLogout = async () => {
      try {
        await userStore.logout()
        router.push('/login')
        if (window.$toast) {
          window.$toast.success('注销成功')
        }
      } catch (error) {
        console.error('注销失败:', error)
        if (window.$toast) {
          window.$toast.error('注销失败')
        }
      }
    }

    return {
      currentUser,
      isAdmin,
      roleText,
      roleClass,
      loading,
      imageCount,
      modelCount,
      chatCount,
      recentItems,
      handleLogout
    }
  }
}
</script>

<style scoped>
/* 主页整体样式 */
.home-view {
  --section-spacing: 4rem;
  --card-border-radius: 12px;
  --transition-speed: 0.3s;
  --shadow-sm: 0 2px 4px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);

  background-color: var(--bg-primary, #f8f9fa);
  color: var(--text-primary, #333);
}

/* 英雄区域样式 */
.hero-section {
  position: relative;
  padding: 6rem 0;
  background: linear-gradient(135deg, #4f46e5 0%, #8338ec 100%);
  color: white;
  overflow: hidden;
}

.hero-title {
  font-size: 3rem;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 1.5rem;
}

.accent-text {
  position: relative;
  display: inline-block;
}

.accent-text::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 6px;
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
}

.hero-subtitle {
  font-size: 1.25rem;
  margin-bottom: 2rem;
  opacity: 0.9;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.hero-visual {
  position: relative;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.model-preview {
  width: 100%;
  height: 100%;
  position: relative;
  border-radius: var(--card-border-radius);
  overflow: hidden;
  background-color: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  box-shadow: var(--shadow-lg);
}

.model-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.model-canvas {
  width: 100%;
  height: 100%;
  position: relative;
}

.model-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(circle at 10% 10%, rgba(255, 255, 255, 0.1) 1px, transparent 1px);
  background-size: 20px 20px;
  opacity: 0.5;
}

.hero-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 500px;
  height: 500px;
  bottom: -200px;
  left: -200px;
}

.decoration-line {
  position: absolute;
  background: rgba(255, 255, 255, 0.05);
}

.line-1 {
  width: 100%;
  height: 1px;
  top: 30%;
  transform: rotate(-5deg);
}

.line-2 {
  width: 1px;
  height: 100%;
  left: 20%;
  transform: rotate(5deg);
}

/* 主要内容区域样式 */
.dashboard-section {
  padding: var(--section-spacing) 0;
}

/* 用户欢迎区域 */
.user-welcome {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: white;
  border-radius: var(--card-border-radius);
  box-shadow: var(--shadow-sm);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: var(--primary-light, #e0e7ff);
  color: var(--primary-color, #4f46e5);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.75rem;
}

.user-details h2 {
  font-size: 1.5rem;
  margin-bottom: 0.25rem;
}

.user-role {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 50px;
  font-size: 0.875rem;
  font-weight: 500;
}

.role-admin {
  background-color: var(--primary-light, #e0e7ff);
  color: var(--primary-color, #4f46e5);
}

.role-user {
  background-color: var(--success-light, #d1fae5);
  color: var(--success-color, #10b981);
}

.role-banned {
  background-color: var(--error-light, #fee2e2);
  color: var(--error-color, #ef4444);
}

.role-unknown {
  background-color: var(--neutral-200, #e5e7eb);
  color: var(--neutral-600, #4b5563);
}

.quick-actions {
  display: flex;
  gap: 1rem;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  border-radius: var(--card-border-radius);
  background-color: white;
  border: 1px solid var(--neutral-200, #e5e7eb);
  color: var(--text-primary, #333);
  font-weight: 500;
  transition: all var(--transition-speed) ease;
}

.action-btn:hover {
  background-color: var(--primary-color, #4f46e5);
  border-color: var(--primary-color, #4f46e5);
  color: white;
}

.logout-btn {
  background-color: rgba(239, 68, 68, 0.1);
  border-color: rgba(239, 68, 68, 0.3);
  color: var(--error-color, #ef4444);
}

.logout-btn:hover {
  background-color: var(--error-color, #ef4444);
  border-color: var(--error-color, #ef4444);
  color: white;
}

/* 数据概览卡片 */
.stats-cards {
  margin-bottom: var(--section-spacing);
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 1.5rem;
  background-color: white;
  border-radius: var(--card-border-radius);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-speed) ease;
  height: 100%;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  background-color: var(--primary-light, #e0e7ff);
  color: var(--primary-color, #4f46e5);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  margin-right: 1rem;
}

.stat-info h3 {
  font-size: 1rem;
  margin-bottom: 0.25rem;
  color: var(--neutral-600, #4b5563);
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 0.25rem;
}

.stat-link {
  font-size: 0.875rem;
  color: var(--primary-color, #4f46e5);
  text-decoration: none;
}

.stat-link:hover {
  text-decoration: underline;
}

/* 最近项目 */
.recent-projects {
  margin-bottom: var(--section-spacing);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-header h2 {
  font-size: 1.5rem;
  margin-bottom: 0;
}

.project-card {
  display: flex;
  flex-direction: column;
  background-color: white;
  border-radius: var(--card-border-radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-speed) ease;
  height: 100%;
}

.project-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-md);
}

.project-preview {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
  color: white;
}

.project-info {
  padding: 1.5rem;
}

.project-info h3 {
  font-size: 1.25rem;
  margin-bottom: 0.5rem;
}

.project-date {
  font-size: 0.875rem;
  color: var(--neutral-500, #6b7280);
  margin-bottom: 1rem;
}

.project-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background-color: var(--neutral-100, #f3f4f6);
  color: var(--neutral-700, #374151);
  border: none;
  transition: all var(--transition-speed) ease;
}

.btn-icon:hover {
  background-color: var(--primary-color, #4f46e5);
  color: white;
}

/* 功能区域 */
.features-section {
  margin-bottom: var(--section-spacing);
}

.feature-card {
  display: flex;
  flex-direction: column;
  background-color: white;
  border-radius: var(--card-border-radius);
  padding: 2rem;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-speed) ease;
  height: 100%;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-md);
}

.feature-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: var(--primary-light, #e0e7ff);
  color: var(--primary-color, #4f46e5);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.75rem;
  margin-bottom: 1.5rem;
}

.feature-content h3 {
  font-size: 1.25rem;
  margin-bottom: 0.75rem;
}

.feature-content p {
  color: var(--neutral-600, #4b5563);
  margin-bottom: 1.5rem;
}

/* 响应式调整 */
@media (max-width: 991.98px) {
  .hero-title {
    font-size: 2.5rem;
  }

  .hero-visual {
    height: 300px;
    margin-top: 2rem;
  }

  .user-welcome {
    flex-direction: column;
    align-items: flex-start;
    gap: 1.5rem;
  }

  .quick-actions {
    width: 100%;
  }

  .action-btn {
    flex: 1;
    justify-content: center;
  }
}

@media (max-width: 767.98px) {
  .hero-title {
    font-size: 2rem;
  }

  .hero-subtitle {
    font-size: 1.125rem;
  }

  .hero-actions {
    flex-direction: column;
  }

  .hero-actions .btn {
    width: 100%;
  }
}
</style>
