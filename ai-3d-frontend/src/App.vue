<template>
  <div class="app-container" :class="{ 'dark-theme': isDarkTheme }">
    <!-- Navigation bar -->
    <Navbar
      :isDarkTheme="isDarkTheme"
      @toggle-theme="toggleTheme"
    />

    <!-- Main content area -->
    <div class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" class="hardware-accelerated" />
        </transition>
      </router-view>
    </div>

    <!-- Global toast notifications -->
    <Toast position="top-right" :isGlass="true" />
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from './stores/user'
import Navbar from './components/shared/Navbar.vue'

// Router and store
const router = useRouter()
const userStore = useUserStore()

// User state
const isLoggedIn = computed(() => userStore.isLoggedIn)
const currentUser = computed(() => userStore.user)
const isAdmin = computed(() => userStore.isAdmin)

// Theme settings
const isDarkTheme = ref(localStorage.getItem('theme') === 'dark')

// Toggle theme
const toggleTheme = () => {
  isDarkTheme.value = !isDarkTheme.value
  localStorage.setItem('theme', isDarkTheme.value ? 'dark' : 'light')
}

// Logout
const logout = async () => {
  try {
    await userStore.logout()
    router.push('/login')
    window.$toast?.success('Logged out successfully')
  } catch (error) {
    window.$toast?.error('Logout failed')
  }
}

// Fetch user data on mount
onMounted(async () => {
  if (localStorage.getItem('token')) {
    try {
      await userStore.fetchCurrentUser()

      // If on login page but already logged in, redirect to home
      if (router.currentRoute.value.path === '/login' && userStore.isLoggedIn) {
        router.replace('/home')
      }
    } catch (error) {
      // If current page requires auth, redirect to login
      if (router.currentRoute.value.meta.requiresAuth) {
        router.replace('/login')
      }
    }
  }
})
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