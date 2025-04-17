<template>
  <div class="home-view">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-md-8">
          <div class="card shadow-sm">
            <div class="card-body">
              <h2 class="card-title text-center mb-4">欢迎使用 AI 3D 平台</h2>

              <div class="user-info mb-4">
                <div class="d-flex align-items-center">
                  <div class="avatar me-3">
                    <i class="bi bi-person-circle" style="font-size: 3rem;"></i>
                  </div>
                  <div>
                    <h5>{{ currentUser.userName || currentUser.userAccount }}</h5>
                    <div class="badge" :class="roleClass">{{ roleText }}</div>
                  </div>
                </div>
              </div>

              <div class="features">
                <h4 class="mb-3">可用功能</h4>

                <!-- 所有用户可见功能 -->
                <div class="feature-card mb-3 p-3 border rounded">
                  <h5><i class="bi bi-person me-2"></i>用户信息</h5>
                  <p class="text-muted">查看和管理您的个人信息</p>
                  <button class="btn btn-outline-primary btn-sm">
                    进入
                  </button>
                </div>

                <!-- 仅管理员可见功能 -->
                <div v-if="isAdmin" class="feature-card mb-3 p-3 border rounded admin-feature">
                  <h5><i class="bi bi-chat-dots me-2"></i>AI 聊天</h5>
                  <p class="text-muted">使用AI助手进行对话（仅管理员可用）</p>
                  <router-link to="/chat" class="btn btn-primary btn-sm">
                    开始聊天
                  </router-link>
                </div>

                <!-- 用户管理功能 -->
                <div v-if="isAdmin" class="feature-card mb-3 p-3 border rounded admin-feature">
                  <h5><i class="bi bi-people me-2"></i>用户管理</h5>
                  <p class="text-muted">管理系统用户（仅管理员可用）</p>
                  <router-link to="/admin/users" class="btn btn-primary btn-sm">
                    管理用户
                  </router-link>
                </div>

                <!-- 非管理员提示 -->
                <div v-else class="feature-card mb-3 p-3 border rounded disabled-feature">
                  <h5><i class="bi bi-chat-dots me-2"></i>AI 聊天</h5>
                  <p class="text-muted">此功能当前仅对管理员开放</p>
                  <button class="btn btn-secondary btn-sm" disabled>
                    需要管理员权限
                  </button>
                </div>

                <div class="feature-card mb-3 p-3 border rounded">
                  <h5><i class="bi bi-heart-pulse me-2"></i>系统状态</h5>
                  <p class="text-muted">查看系统健康状态</p>
                  <router-link to="/health" class="btn btn-outline-primary btn-sm">
                    查看
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

.avatar {
  color: var(--primary-color);
}

.admin-feature {
  background-color: var(--primary-light);
  border-color: var(--primary-color) !important;
}

.disabled-feature {
  background-color: var(--bg-tertiary);
  opacity: 0.8;
}

.feature-card {
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}
</style>
