<template>
  <div class="login-container slide-up">
    <div class="row justify-content-center">
      <div class="col-md-6 col-lg-4">
        <div class="text-center mb-4">
          <i class="bi bi-chat-dots-fill text-primary" style="font-size: 3rem;"></i>
          <h2 class="mt-3 mb-4">AI 智能助手</h2>
        </div>

        <div class="card shadow border-0">
          <div class="card-header bg-white border-0 pt-4">
            <h4 class="text-center mb-0">用户登录</h4>
          </div>
          <div class="card-body p-4">
            <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
              {{ error }}
              <button type="button" class="btn-close" @click="error = ''"></button>
            </div>

            <form @submit.prevent="handleSubmit">
              <div class="mb-4">
                <label for="userAccount" class="form-label">
                  <i class="bi bi-person me-2"></i>账号
                </label>
                <input
                  type="text"
                  class="form-control form-control-lg"
                  id="userAccount"
                  v-model="userAccount"
                  placeholder="请输入账号"
                  required
                  autocomplete="username"
                  :disabled="isLoading"
                  @keyup.enter="focusPassword"
                />
              </div>

              <div class="mb-4">
                <label for="userPassword" class="form-label">
                  <i class="bi bi-lock me-2"></i>密码
                </label>
                <div class="input-group">
                  <input
                    :type="showPassword ? 'text' : 'password'"
                    class="form-control form-control-lg"
                    id="userPassword"
                    v-model="userPassword"
                    placeholder="请输入密码"
                    required
                    autocomplete="current-password"
                    :disabled="isLoading"
                    ref="passwordInput"
                  />
                  <button
                    class="btn btn-outline-secondary"
                    type="button"
                    @click="showPassword = !showPassword"
                  >
                    <i class="bi" :class="showPassword ? 'bi-eye-slash' : 'bi-eye'"></i>
                  </button>
                </div>
              </div>

              <div class="d-grid gap-3 mt-4">
                <button
                  type="submit"
                  class="btn btn-primary btn-lg"
                  :disabled="isLoading || !userAccount || !userPassword"
                >
                  <span v-if="isLoading" class="spinner-border spinner-border-sm me-2" role="status"></span>
                  {{ isLoading ? '登录中...' : '登录' }}
                </button>
                <div class="text-center">
                  <span class="text-muted">没有账号？</span>
                  <router-link to="/register" class="ms-2">立即注册</router-link>
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'

export default {
  name: 'LoginView',
  setup() {
    const store = useStore()
    const router = useRouter()

    const userAccount = ref('')
    const userPassword = ref('')
    const error = ref('')
    const isLoading = ref(false)
    const showPassword = ref(false)
    const passwordInput = ref(null)

    // 聚焦到密码输入框
    const focusPassword = () => {
      if (userAccount.value && passwordInput.value) {
        passwordInput.value.focus()
      }
    }

    const handleSubmit = async () => {
      if (!userAccount.value || !userPassword.value) {
        error.value = '请输入账号和密码'
        return
      }

      error.value = ''
      isLoading.value = true

      try {
        // 登录并获取用户信息
        const result = await store.dispatch('user/login', {
          userAccount: userAccount.value,
          userPassword: userPassword.value
        })

        console.log('登录成功，用户信息:', result)

        // 确保用户状态已设置
        if (store.getters['user/isLoggedIn']) {
          console.log('用户已登录，跳转到主页')
          // 使用replace而不是push，避免用户可以通过浏览器后退回到登录页面
          router.replace('/home')
        } else {
          console.error('登录成功但用户状态未设置')
          error.value = '登录成功但系统出现异常，请刷新页面重试'
        }
      } catch (err) {
        console.error('登录失败:', err)
        error.value = typeof err === 'string' ? err : '登录失败，请检查账号密码'
      } finally {
        isLoading.value = false
      }
    }

    // 组件挂载后聚焦到账号输入框
    onMounted(() => {
      // 如果已经登录，直接跳转到主页
      if (store.getters['user/isLoggedIn']) {
        router.push('/home')
        return
      }
    })

    return {
      userAccount,
      userPassword,
      error,
      isLoading,
      showPassword,
      passwordInput,
      focusPassword,
      handleSubmit
    }
  }
}
</script>

<style scoped>
.login-container {
  padding-top: 2rem;
}

@media (min-height: 700px) {
  .login-container {
    padding-top: 5rem;
  }
}

.card {
  border-radius: var(--radius-lg);
}

.form-control {
  border-radius: var(--radius-md);
}

.btn-primary {
  border-radius: var(--radius-md);
}
</style>