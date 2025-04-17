import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')
  
  // 计算属性
  const isLoggedIn = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.userRole === 'admin')
  
  // 方法
  function setUser(userData) {
    user.value = userData
    // 如果后端实现了token认证，可以在这里保存token
    // localStorage.setItem('token', userData.token)
  }
  
  function clearUser() {
    user.value = null
    localStorage.removeItem('token')
  }
  
  return {
    user,
    token,
    isLoggedIn,
    isAdmin,
    setUser,
    clearUser
  }
})
