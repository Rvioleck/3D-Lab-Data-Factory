<template>
  <div class="login-container">
    <!-- 背景效果 -->
    <div class="login-bg"></div>
    <div class="login-bg-shapes">
      <div class="login-bg-shape"></div>
      <div class="login-bg-shape"></div>
      <div class="login-bg-shape"></div>
      <div class="login-bg-shape"></div>
    </div>

    <!-- 左侧内容区 -->
    <div class="login-content-left">
      <div class="login-brand">
        <div class="login-brand-logo">
          <i class="bi bi-box-fill"></i>
        </div>
        <div class="login-brand-text">3D-Lab Data Factory</div>
      </div>

      <h1 class="login-headline">3D-Lab<br />Data Factory</h1>
      <p class="login-subheadline">轻松创建、管理和分享高质量3D模型和图像</p>

      <div class="login-features">
        <div class="login-feature">
          <div class="login-feature-icon">
            <i class="bi bi-magic"></i>
          </div>
          <div class="login-feature-text">从单张图片生成高质量3D模型</div>
        </div>
        <div class="login-feature">
          <div class="login-feature-icon">
            <i class="bi bi-chat-dots"></i>
          </div>
          <div class="login-feature-text">进行数据分析和可视化</div>
        </div>
        <div class="login-feature">
          <div class="login-feature-icon">
            <i class="bi bi-collection"></i>
          </div>
          <div class="login-feature-text">管理和组织您的所有数字资产</div>
        </div>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="login-content-right">
      <div class="login-card">
        <div class="login-card-header">
          <div class="login-logo">
            <i class="bi bi-box-fill"></i>
          </div>
          <h1 class="login-title">欢迎回来</h1>
          <p class="login-subtitle">登录您的账号继续使用</p>
        </div>

        <form @submit.prevent="handleLogin">
          <div v-if="errorMessage" class="login-error">
            {{ errorMessage }}
            <button type="button" @click="errorMessage = ''" class="login-close-error">
              <i class="bi bi-x"></i>
            </button>
          </div>

          <div class="login-form-group">
            <div class="login-input-wrapper">
              <i class="bi bi-person login-input-icon"></i>
              <input
                type="text"
                class="login-input"
                v-model="loginForm.userAccount"
                placeholder="请输入账号"
                required
                :disabled="loading"
              />
            </div>
          </div>

          <div class="login-form-group">
            <div class="login-input-wrapper">
              <i class="bi bi-lock login-input-icon"></i>
              <input
                :type="showPassword ? 'text' : 'password'"
                class="login-input"
                v-model="loginForm.userPassword"
                placeholder="请输入密码"
                required
                :disabled="loading"
              />
              <button
                type="button"
                class="login-toggle-password"
                @click="showPassword = !showPassword"
              >
                <i :class="'bi ' + (showPassword ? 'bi-eye-slash' : 'bi-eye')"></i>
              </button>
            </div>
          </div>

          <button type="submit" class="login-button" :disabled="loading">
            <span v-if="loading">
              <i class="bi bi-arrow-repeat spin me-2"></i>
              登录中...
            </span>
            <span v-else>登录</span>
          </button>

          <div class="login-link">
            还没有账号？
            <router-link to="/register">立即注册</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import '@/assets/login.css'

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