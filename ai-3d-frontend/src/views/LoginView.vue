<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">
          <i class="bi bi-box-fill"></i>
        </div>
        <h1 class="auth-title">欢迎回来</h1>
        <p class="auth-subtitle">登录您的账号继续使用</p>
      </div>

      <form @submit.prevent="handleLogin">
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
          <button type="button" @click="errorMessage = ''" class="toggle-password">
            <i class="bi bi-x"></i>
          </button>
        </div>

        <div class="form-group">
          <div class="input-wrapper">
            <i class="bi bi-person"></i>
            <input
              type="text"
              class="form-input"
              v-model="loginForm.userAccount"
              placeholder="请输入账号"
              required
              :disabled="loading"
            />
          </div>
        </div>

        <div class="form-group">
          <div class="input-wrapper">
            <i class="bi bi-lock"></i>
            <input
              :type="showPassword ? 'text' : 'password'"
              class="form-input"
              v-model="loginForm.userPassword"
              placeholder="请输入密码"
              required
              :disabled="loading"
            />
            <button
              type="button"
              class="toggle-password"
              @click="showPassword = !showPassword"
            >
              <i :class="'bi ' + (showPassword ? 'bi-eye-slash' : 'bi-eye')"></i>
            </button>
          </div>
        </div>

        <button type="submit" class="auth-button" :disabled="loading">
          <span v-if="loading">
            <i class="bi bi-arrow-repeat spin me-2"></i>
            登录中...
          </span>
          <span v-else>登录</span>
        </button>

        <div class="auth-link">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import '@/assets/styles/auth.css'

const router = useRouter()
const userStore = useUserStore()

const loginForm = reactive({
  userAccount: '',
  userPassword: ''
})

const loading = ref(false)
const showPassword = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  if (!loginForm.userAccount || !loginForm.userPassword) {
    errorMessage.value = '请输入账号和密码'
    return
  }

  loading.value = true
  try {
    const response = await userStore.login(loginForm)
    if (response && response.code === 0) {
      router.push('/home')
    } else {
      errorMessage.value = (response && response.message) || '登录失败'
    }
  } catch (error) {
    console.error('登录错误:', error)
    errorMessage.value = error.message || '登录异常，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>