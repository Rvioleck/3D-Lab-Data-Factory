<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">
          <i class="bi bi-box-fill"></i>
        </div>
        <h1 class="auth-title">创建账号</h1>
        <p class="auth-subtitle">注册新账号开始使用</p>
      </div>

      <form @submit.prevent="handleRegister" v-if="!success">
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
              v-model="registerForm.userAccount"
              placeholder="请输入账号（至少4个字符）"
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
              v-model="registerForm.userPassword"
              placeholder="请输入密码（至少6个字符）"
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

        <div class="form-group">
          <div class="input-wrapper">
            <i class="bi bi-lock-fill"></i>
            <input
              :type="showConfirmPassword ? 'text' : 'password'"
              class="form-input"
              v-model="registerForm.checkPassword"
              placeholder="请确认密码"
              required
              :disabled="loading"
            />
            <button
              type="button"
              class="toggle-password"
              @click="showConfirmPassword = !showConfirmPassword"
            >
              <i :class="'bi ' + (showConfirmPassword ? 'bi-eye-slash' : 'bi-eye')"></i>
            </button>
          </div>
        </div>

        <button type="submit" class="auth-button" :disabled="loading || !isFormValid">
          <span v-if="loading">
            <i class="bi bi-arrow-repeat spin me-2"></i>
            注册中...
          </span>
          <span v-else>注册</span>
        </button>

        <div class="auth-link">
          已有账号？
          <router-link to="/login">立即登录</router-link>
        </div>
      </form>

      <div v-else class="success-message">
        <i class="bi bi-check-circle-fill success-icon"></i>
        <h2>注册成功！</h2>
        <p>您的账号已创建成功，现在可以登录了</p>
        <router-link to="/login" class="auth-button">
          前往登录
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import '@/assets/styles/auth.css'

const router = useRouter()
const userStore = useUserStore()

const registerForm = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const errorMessage = ref('')
const success = ref(false)

const isFormValid = computed(() => {
  return registerForm.userAccount.length >= 4 &&
         registerForm.userPassword.length >= 6 &&
         registerForm.userPassword === registerForm.checkPassword
})

const handleRegister = async () => {
  if (!isFormValid.value) {
    if (registerForm.userAccount.length < 4) {
      errorMessage.value = '账号长度至少为4个字符'
    } else if (registerForm.userPassword.length < 6) {
      errorMessage.value = '密码长度至少为6个字符'
    } else if (registerForm.userPassword !== registerForm.checkPassword) {
      errorMessage.value = '两次输入的密码不一致'
    }
    return
  }

  loading.value = true
  try {
    const response = await userStore.register(registerForm)
    if (response.code === 0) {
      success.value = true
    } else {
      errorMessage.value = response.message || '注册失败'
    }
  } catch (error) {
    console.error('注册错误:', error)
    errorMessage.value = error.message || '注册异常，请稍后重试'
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

.success-message {
  text-align: center;
  color: #fff;
}

.success-icon {
  font-size: 4rem;
  color: var(--success-color);
  margin-bottom: 1.5rem;
}

.success-message h2 {
  margin-bottom: 1rem;
}

.success-message p {
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 2rem;
}
</style>