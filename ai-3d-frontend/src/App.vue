<template>
  <div class="app-container" :class="{ 'dark-theme': isDarkTheme }">
    <!-- 使用新导航栏 -->
    <NewNavbar
      :isDarkTheme="isDarkTheme"
      @toggle-theme="toggleTheme"
    />

    <!-- 主要内容区域 -->
    <div class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" class="hardware-accelerated" />
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script>
import { computed, ref, onMounted, watch } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import NewNavbar from './components/NewNavbar.vue'

export default {
  name: 'App',
  components: {
    NewNavbar
  },
  setup() {
    const store = useStore()
    const router = useRouter()

    // 用户状态
    const isLoggedIn = computed(() => store.getters['user/isLoggedIn'])
    const currentUser = computed(() => store.getters['user/currentUser'])
    const isAdmin = computed(() => store.getters['user/isAdmin'])

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

          // 如果当前在登录页面但已经登录，跳转到首页
          if (router.currentRoute.value.path === '/login' && store.getters['user/isLoggedIn']) {
            console.log('已登录状态，从登录页跳转到首页')
            router.replace('/home')
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
      isAdmin,
      isDarkTheme,
      toggleTheme,
      logout
    }
  }
}
</script>

<style>
@import './assets/new-theme.css';
@import url('https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.2/font/bootstrap-icons.min.css');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  width: 100%;
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  padding-top: 70px; /* 为固定导航栏留出空间 */
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding-left: 1rem;
  padding-right: 1rem;
}

/* 容器 */
.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding-left: 1rem;
  padding-right: 1rem;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .container {
    max-width: 100%;
  }
}

/* 过渡动画 */
.page-enter-active,
.page-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 硬件加速 */
.hardware-accelerated {
  transform: translateZ(0);
  backface-visibility: hidden;
  perspective: 1000px;
}
</style>