<template>
  <div class="home-view">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-md-10">
          <div class="welcome-card text-center p-5 mb-4">
            <h1 class="display-4 mb-3">欢迎使用 AI 3D 平台</h1>
            <p class="lead mb-4">一站式3D模型生成与管理平台</p>
            <div class="user-greeting mb-4">
              <div class="avatar mb-3">
                <i class="bi bi-person-circle"></i>
              </div>
              <h4>{{ currentUser.userName || currentUser.userAccount }}</h4>
              <div class="badge mb-2" :class="roleClass">{{ roleText }}</div>
            </div>
          </div>

          <div class="row feature-grid">
            <!-- 图片库 -->
            <div class="col-md-6 mb-4">
              <div class="feature-card h-100">
                <div class="feature-icon">
                  <i class="bi bi-images"></i>
                </div>
                <div class="feature-content">
                  <h3>图片库</h3>
                  <p>浏览和管理图片资源</p>
                  <router-link to="/images" class="btn btn-primary">
                    浏览图片
                  </router-link>
                </div>
              </div>
            </div>

            <!-- 3D模型库 -->
            <div class="col-md-6 mb-4">
              <div class="feature-card h-100">
                <div class="feature-icon">
                  <i class="bi bi-box"></i>
                </div>
                <div class="feature-content">
                  <h3>3D模型库</h3>
                  <p>浏览和管理三维模型</p>
                  <router-link to="/models" class="btn btn-primary">
                    浏览模型
                  </router-link>
                </div>
              </div>
            </div>

            <!-- 3D重建 (仅管理员) -->
            <div class="col-md-6 mb-4" v-if="isAdmin">
              <div class="feature-card h-100 admin-feature">
                <div class="feature-icon">
                  <i class="bi bi-tools"></i>
                </div>
                <div class="feature-content">
                  <h3>3D重建</h3>
                  <p>创建和管理三维重建任务</p>
                  <router-link to="/reconstruction" class="btn btn-primary">
                    开始重建
                  </router-link>
                </div>
              </div>
            </div>

            <!-- AI聊天 (仅管理员) -->
            <div class="col-md-6 mb-4" v-if="isAdmin">
              <div class="feature-card h-100 admin-feature">
                <div class="feature-icon">
                  <i class="bi bi-chat-dots"></i>
                </div>
                <div class="feature-content">
                  <h3>AI 聊天</h3>
                  <p>使用AI助手进行对话</p>
                  <router-link to="/chat" class="btn btn-primary">
                    开始聊天
                  </router-link>
                </div>
              </div>
            </div>

            <!-- 用户管理 (仅管理员) -->
            <div class="col-md-6 mb-4" v-if="isAdmin">
              <div class="feature-card h-100 admin-feature">
                <div class="feature-icon">
                  <i class="bi bi-people"></i>
                </div>
                <div class="feature-content">
                  <h3>用户管理</h3>
                  <p>管理系统用户和权限</p>
                  <router-link to="/admin/users" class="btn btn-primary">
                    管理用户
                  </router-link>
                </div>
              </div>
            </div>

            <!-- 系统状态 -->
            <div class="col-md-6 mb-4">
              <div class="feature-card h-100">
                <div class="feature-icon">
                  <i class="bi bi-heart-pulse"></i>
                </div>
                <div class="feature-content">
                  <h3>系统状态</h3>
                  <p>查看系统健康状态</p>
                  <router-link to="/health" class="btn btn-primary">
                    查看状态
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
import { computed } from 'vue'
import { useStore } from 'vuex'

export default {
  name: 'HomeView',
  setup() {
    const store = useStore()

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

    return {
      currentUser,
      isAdmin,
      roleText,
      roleClass
    }
  }
}
</script>

<style scoped>
.home-view {
  padding: 2rem 0;
}

.welcome-card {
  background-color: var(--bg-secondary);
  border-radius: 1rem;
  box-shadow: var(--shadow-md);
  margin-top: 1rem;
}

.avatar {
  font-size: 4rem;
  color: var(--primary-color);
  margin-bottom: 1rem;
}

.user-greeting {
  padding: 1rem;
}

.feature-grid {
  margin-top: 1rem;
}

.feature-card {
  background-color: var(--bg-secondary);
  border-radius: 1rem;
  padding: 2rem;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-md);
}

.feature-icon {
  font-size: 2.5rem;
  color: var(--primary-color);
  margin-bottom: 1.5rem;
}

.feature-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.feature-content h3 {
  margin-bottom: 0.75rem;
  font-weight: 600;
}

.feature-content p {
  margin-bottom: 1.5rem;
  color: var(--text-secondary);
  flex: 1;
}

.admin-feature {
  background-color: var(--primary-light);
  border-left: 4px solid var(--primary-color);
}

.admin-feature .feature-icon {
  color: var(--primary-color);
}

@media (max-width: 768px) {
  .welcome-card {
    padding: 1.5rem !important;
  }

  .feature-card {
    padding: 1.5rem;
  }

  .feature-icon {
    font-size: 2rem;
    margin-bottom: 1rem;
  }
}
</style>
