<template>
  <div class="app-container" :class="{ 'dark-theme': isDarkTheme }">
    <nav class="navbar navbar-expand-lg mb-4" :class="isDarkTheme ? 'navbar-dark bg-dark' : 'navbar-light bg-white'">
      <div class="container">
        <router-link class="navbar-brand" to="/home">
          <i class="bi bi-chat-dots-fill me-2"></i>
          AI 3D 平台
        </router-link>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav">
            <li class="nav-item">
              <router-link class="nav-link" to="/home">
                <i class="bi bi-house me-1"></i> 首页
              </router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" to="/chat">
                <i class="bi bi-chat me-1"></i> 聊天
              </router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" to="/health">
                <i class="bi bi-heart-pulse me-1"></i> 健康检查
              </router-link>
            </li>
          </ul>
          <ul class="navbar-nav ms-auto">
            <li class="nav-item">
              <button class="btn btn-link nav-link" @click="toggleTheme">
                <i class="bi" :class="isDarkTheme ? 'bi-sun' : 'bi-moon'"></i>
              </button>
            </li>
            <li class="nav-item" v-if="!isLoggedIn">
              <router-link class="nav-link" to="/login">
                <i class="bi bi-box-arrow-in-right me-1"></i> 登录
              </router-link>
            </li>
            <li class="nav-item" v-if="!isLoggedIn">
              <router-link class="nav-link" to="/register">
                <i class="bi bi-person-plus me-1"></i> 注册
              </router-link>
            </li>
            <li class="nav-item" v-if="isLoggedIn">
              <span class="nav-link text-primary">
                <i class="bi bi-person-circle me-1"></i>
                {{ currentUser?.userName || currentUser?.userAccount }}
              </span>
            </li>
            <li class="nav-item" v-if="isLoggedIn">
              <a class="nav-link" href="#" @click.prevent="logout">
                <i class="bi bi-box-arrow-right me-1"></i> 退出登录
              </a>
            </li>
          </ul>
        </div>
      </div>
    </nav>

    <div class="container mb-4">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script>
import { computed, ref, onMounted, watch } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'

export default {
  name: 'App',
  setup() {
    const store = useStore()
    const router = useRouter()

    // 用户状态
    const isLoggedIn = computed(() => store.getters['user/isLoggedIn'])
    const currentUser = computed(() => store.getters['user/currentUser'])

    // 主题设置
    const isDarkTheme = ref(localStorage.getItem('theme') === 'dark')

    // 切换主题
    const toggleTheme = () => {
      isDarkTheme.value = !isDarkTheme.value
      localStorage.setItem('theme', isDarkTheme.value ? 'dark' : 'light')
    }

    // 退出登录
    const logout = () => {
      store.dispatch('user/logout')
      router.push('/login')
    }

    // 组件挂载时获取用户信息
    onMounted(async () => {
      if (localStorage.getItem('token')) {
        try {
          console.log('尝试获取当前登录用户信息')
          const userData = await store.dispatch('user/fetchCurrentUser')
          console.log('获取用户信息成功:', userData)

          // 如果当前在登录页面但已经登录，跳转到聊天页面
          if (router.currentRoute.value.path === '/login' && store.getters['user/isLoggedIn']) {
            console.log('已登录状态，从登录页跳转到聊天页面')
            router.replace('/chat')
          }
        } catch (error) {
          console.error('获取用户信息失败:', error)
          // 如果获取用户信息失败，清除token
          localStorage.removeItem('token')

          // 如果当前页面需要登录权限，跳转到登录页面
          if (router.currentRoute.value.meta.requiresAuth) {
            router.replace('/login')
          }
        }
      }
    })

    return {
      isLoggedIn,
      currentUser,
      isDarkTheme,
      toggleTheme,
      logout
    }
  }
}
</script>

<style>
@import './assets/theme.css';
@import url('https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.2/font/bootstrap-icons.min.css');

.app-container {
  min-height: 100vh;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 导航栏样式 */
.navbar {
  box-shadow: var(--shadow-sm);
}

.navbar-brand {
  font-weight: 600;
  display: flex;
  align-items: center;
}

.nav-link {
  display: flex;
  align-items: center;
}
</style>