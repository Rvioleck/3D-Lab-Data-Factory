<template>
  <div class="login-dialog" v-if="isOpen">
    <div class="login-dialog-backdrop" @click="close"></div>
    <div class="login-dialog-content">
      <div class="login-dialog-header">
        <h3 class="login-dialog-title">登录</h3>
        <button class="login-dialog-close" @click="close">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>
      
      <div class="login-dialog-body">
        <div v-if="errorMessage" class="login-dialog-error">
          {{ errorMessage }}
          <button type="button" @click="errorMessage = ''" class="login-dialog-error-close">
            <i class="bi bi-x"></i>
          </button>
        </div>

        <form @submit.prevent="handleLogin">
          <div class="login-dialog-form-group">
            <div class="login-dialog-input-wrapper">
              <i class="bi bi-person login-dialog-input-icon"></i>
              <input
                type="text"
                class="login-dialog-input"
                v-model="loginForm.userAccount"
                placeholder="请输入账号"
                required
                :disabled="loading"
              />
            </div>
          </div>

          <div class="login-dialog-form-group">
            <div class="login-dialog-input-wrapper">
              <i class="bi bi-lock login-dialog-input-icon"></i>
              <input
                :type="showPassword ? 'text' : 'password'"
                class="login-dialog-input"
                v-model="loginForm.userPassword"
                placeholder="请输入密码"
                required
                :disabled="loading"
              />
              <button
                type="button"
                class="login-dialog-toggle-password"
                @click="showPassword = !showPassword"
              >
                <i :class="'bi ' + (showPassword ? 'bi-eye-slash' : 'bi-eye')"></i>
              </button>
            </div>
          </div>

          <button type="submit" class="login-dialog-button" :disabled="loading">
            <span v-if="loading">
              <i class="bi bi-arrow-repeat spin me-2"></i>
              登录中...
            </span>
            <span v-else>登录</span>
          </button>

          <div class="login-dialog-link">
            还没有账号？
            <router-link to="/register" @click="close">立即注册</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

export default {
  name: 'LoginDialog',
  props: {
    isOpen: {
      type: Boolean,
      default: false
    }
  },
  emits: ['close', 'login-success'],
  setup(props, { emit }) {
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
          emit('login-success')
          close()
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

    const close = () => {
      emit('close')
    }

    return {
      loginForm,
      loading,
      showPassword,
      errorMessage,
      handleLogin,
      close
    }
  }
}
</script>

<style scoped>
.login-dialog {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1100;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-dialog-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
}

.login-dialog-content {
  position: relative;
  width: 100%;
  max-width: 400px;
  background-color: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
  animation: dialog-appear 0.3s ease-out forwards;
}

@keyframes dialog-appear {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.login-dialog-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.login-dialog-close {
  background: none;
  border: none;
  color: var(--text-tertiary);
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all var(--transition-fast) ease;
}

.login-dialog-close:hover {
  background-color: var(--bg-tertiary);
  color: var(--text-primary);
}

.login-dialog-body {
  padding: 1.5rem;
}

.login-dialog-error {
  background: transparent;
  border-left: 4px solid var(--error-color);
  color: var(--error-color);
  padding: 0.75rem;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  animation: shake 0.5s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
  20%, 40%, 60%, 80% { transform: translateX(5px); }
}

.login-dialog-error-close {
  background: none;
  border: none;
  color: var(--error-color);
  cursor: pointer;
  padding: 0;
  font-size: 1rem;
}

.login-dialog-form-group {
  margin-bottom: 1.5rem;
}

.login-dialog-input-wrapper {
  position: relative;
}

.login-dialog-input-icon {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-tertiary);
  font-size: 1.25rem;
}

.login-dialog-input {
  width: 100%;
  padding: 1rem 0 0.75rem 2rem;
  background: transparent;
  border: none;
  border-bottom: 2px solid var(--border-color);
  color: var(--text-primary);
  font-size: 1rem;
  transition: all var(--transition-normal) ease;
}

.login-dialog-input:focus {
  outline: none;
  border-color: var(--primary-color);
}

.login-dialog-input::placeholder {
  color: var(--text-tertiary);
}

.login-dialog-toggle-password {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  padding: 0;
  font-size: 1.25rem;
  transition: color var(--transition-fast) ease;
}

.login-dialog-toggle-password:hover {
  color: var(--text-secondary);
}

.login-dialog-button {
  width: 100%;
  padding: 0.75rem;
  margin-top: 1rem;
  background: var(--primary-color);
  border: none;
  border-radius: var(--radius-lg);
  color: white;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: all var(--transition-normal) ease;
  position: relative;
  overflow: hidden;
  letter-spacing: 0.5px;
}

.login-dialog-button:hover {
  background: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(var(--primary-rgb), 0.3);
}

.login-dialog-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.login-dialog-link {
  text-align: center;
  margin-top: 1.5rem;
  color: var(--text-secondary);
}

.login-dialog-link a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  margin-left: 0.5rem;
  transition: color var(--transition-fast) ease;
}

.login-dialog-link a:hover {
  color: var(--primary-hover);
  text-decoration: underline;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
