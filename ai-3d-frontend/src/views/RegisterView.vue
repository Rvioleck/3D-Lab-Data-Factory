<template>
  <div class="register-container slide-up">
    <div class="row justify-content-center">
      <div class="col-md-6 col-lg-4">
        <div class="text-center mb-4">
          <i class="bi bi-chat-dots-fill text-primary" style="font-size: 3rem;"></i>
          <h2 class="mt-3 mb-4">AI 智能助手</h2>
        </div>

        <div class="card shadow border-0">
          <div class="card-header bg-white border-0 pt-4">
            <h4 class="text-center mb-0">用户注册</h4>
          </div>
          <div class="card-body p-4">
            <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
              {{ error }}
              <button type="button" class="btn-close" @click="error = ''"></button>
            </div>

            <div v-if="success" class="alert alert-success alert-dismissible fade show" role="alert">
              <i class="bi bi-check-circle me-2"></i>注册成功！
              <div class="mt-2">
                <router-link to="/login" class="btn btn-sm btn-success">
                  <i class="bi bi-box-arrow-in-right me-1"></i>去登录
                </router-link>
              </div>
            </div>

            <form @submit.prevent="handleSubmit" v-if="!success">
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
                <div class="form-text" v-if="accountFeedback">
                  {{ accountFeedback }}
                </div>
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
                    autocomplete="new-password"
                    :disabled="isLoading"
                    ref="passwordInput"
                    @keyup.enter="focusCheckPassword"
                  />
                  <button
                    class="btn btn-outline-secondary"
                    type="button"
                    @click="showPassword = !showPassword"
                  >
                    <i class="bi" :class="showPassword ? 'bi-eye-slash' : 'bi-eye'"></i>
                  </button>
                </div>
                <div class="form-text" v-if="passwordStrength">
                  密码强度：{{ passwordStrength }}
                </div>
              </div>

              <div class="mb-4">
                <label for="checkPassword" class="form-label">
                  <i class="bi bi-shield-lock me-2"></i>确认密码
                </label>
                <input
                  :type="showPassword ? 'text' : 'password'"
                  class="form-control form-control-lg"
                  id="checkPassword"
                  v-model="checkPassword"
                  placeholder="请再次输入密码"
                  required
                  autocomplete="new-password"
                  :disabled="isLoading"
                  ref="checkPasswordInput"
                />
                <div class="form-text text-danger" v-if="passwordMismatch">
                  两次输入的密码不一致
                </div>
              </div>

              <div class="d-grid gap-3 mt-4">
                <button
                  type="submit"
                  class="btn btn-primary btn-lg"
                  :disabled="isLoading || !isFormValid"
                >
                  <span v-if="isLoading" class="spinner-border spinner-border-sm me-2" role="status"></span>
                  {{ isLoading ? '注册中...' : '注册' }}
                </button>
                <div class="text-center">
                  <span class="text-muted">已有账号？</span>
                  <router-link to="/login" class="ms-2">去登录</router-link>
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
import { ref, computed, watch } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'

export default {
  name: 'RegisterView',
  setup() {
    const store = useStore()
    const router = useRouter()

    const userAccount = ref('')
    const userPassword = ref('')
    const checkPassword = ref('')
    const error = ref('')
    const success = ref(false)
    const isLoading = ref(false)
    const showPassword = ref(false)
    const passwordInput = ref(null)
    const checkPasswordInput = ref(null)

    // 表单验证状态
    const accountFeedback = computed(() => {
      if (!userAccount.value) return ''
      if (userAccount.value.length < 4) return '账号长度至少为4个字符'
      return ''
    })

    const passwordStrength = computed(() => {
      if (!userPassword.value) return ''
      if (userPassword.value.length < 6) return '弱'
      if (userPassword.value.length < 10) return '中'
      return '强'
    })

    const passwordMismatch = computed(() => {
      if (!checkPassword.value) return false
      return userPassword.value !== checkPassword.value
    })

    const isFormValid = computed(() => {
      return userAccount.value.length >= 4 &&
             userPassword.value.length >= 6 &&
             userPassword.value === checkPassword.value
    })

    // 聚焦到密码输入框
    const focusPassword = () => {
      if (userAccount.value && passwordInput.value) {
        passwordInput.value.focus()
      }
    }

    // 聚焦到确认密码输入框
    const focusCheckPassword = () => {
      if (userPassword.value && checkPasswordInput.value) {
        checkPasswordInput.value.focus()
      }
    }

    const handleSubmit = async () => {
      if (!isFormValid.value) {
        if (userAccount.value.length < 4) {
          error.value = '账号长度至少为4个字符'
        } else if (userPassword.value.length < 6) {
          error.value = '密码长度至少为6个字符'
        } else if (userPassword.value !== checkPassword.value) {
          error.value = '两次输入的密码不一致'
        }
        return
      }

      error.value = ''
      isLoading.value = true

      try {
        await store.dispatch('user/register', {
          userAccount: userAccount.value,
          userPassword: userPassword.value,
          checkPassword: checkPassword.value
        })
        success.value = true
      } catch (err) {
        error.value = typeof err === 'string' ? err : '注册失败，请重试'
      } finally {
        isLoading.value = false
      }
    }

    return {
      userAccount,
      userPassword,
      checkPassword,
      error,
      success,
      isLoading,
      showPassword,
      passwordInput,
      checkPasswordInput,
      accountFeedback,
      passwordStrength,
      passwordMismatch,
      isFormValid,
      focusPassword,
      focusCheckPassword,
      handleSubmit
    }
  }
}
</script>

<style scoped>
.register-container {
  padding-top: 2rem;
}

@media (min-height: 700px) {
  .register-container {
    padding-top: 3rem;
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