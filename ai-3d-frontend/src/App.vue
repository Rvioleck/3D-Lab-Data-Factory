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
@import './assets/main.css';
@import url('https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.2/font/bootstrap-icons.min.css');
</style>