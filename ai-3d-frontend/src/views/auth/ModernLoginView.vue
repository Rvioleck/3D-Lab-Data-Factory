<template>
  <div class="modern-login-view">
    <div class="login-container">
      <div class="row g-0">
        <!-- 左侧信息区域 -->
        <div class="col-lg-6 d-none d-lg-block">
          <div class="info-side glass-background">
            <div class="info-content">
              <h1 class="display-4 mb-4 text-3d">
                <span class="gradient-text-primary">AI 3D</span> 平台
              </h1>
              <p class="lead mb-4">
                使用先进的人工智能技术，轻松创建、管理和分享高质量3D模型和图像
              </p>

              <!-- 3D效果展示 -->
              <div class="model-preview model-viewer-3d">
                <div class="loader-3d" v-if="loading">
                  <div class="cube">
                    <div class="face front"></div>
                    <div class="face back"></div>
                    <div class="face right"></div>
                    <div class="face left"></div>
                    <div class="face top"></div>
                    <div class="face bottom"></div>
                  </div>
                </div>
                <div
                  v-if="!loading"
                  class="model-placeholder img-fluid rounded"
                >
                  <div class="model-placeholder-content">
                    <i class="bi bi-box"></i>
                    <span>3D模型预览</span>
                  </div>
                </div>
              </div>

              <div class="features mt-5">
                <div class="feature-item">
                  <i class="bi bi-image me-2"></i>
                  <span>AI 图像生成</span>
                </div>
                <div class="feature-item">
                  <i class="bi bi-box me-2"></i>
                  <span>3D 模型创建</span>
                </div>
                <div class="feature-item">
                  <i class="bi bi-chat-dots me-2"></i>
                  <span>AI 助手对话</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧登录表单 -->
        <div class="col-lg-6">
          <div class="login-form-container">
            <div class="login-form-wrapper neu-card">
              <div class="text-center mb-4">
                <h2 class="text-3d">用户登录</h2>
                <p class="text-muted">欢迎回来，请登录您的账号</p>
              </div>

              <form @submit.prevent="handleLogin">
                <div class="form-group mb-4">
                  <label for="userAccount" class="form-label">账号</label>
                  <input
                    type="text"
                    id="userAccount"
                    v-model="loginForm.userAccount"
                    class="neu-input form-control"
                    placeholder="请输入账号"
                    required
                  />
                </div>

                <div class="form-group mb-4">
                  <div class="d-flex justify-content-between align-items-center">
                    <label for="userPassword" class="form-label">密码</label>
                    <a href="#" class="text-decoration-none small">忘记密码?</a>
                  </div>
                  <input
                    type="password"
                    id="userPassword"
                    v-model="loginForm.userPassword"
                    class="neu-input form-control"
                    placeholder="请输入密码"
                    required
                  />
                </div>

                <div class="form-group mb-4">
                  <div class="form-check">
                    <input
                      type="checkbox"
                      id="rememberMe"
                      v-model="rememberMe"
                      class="form-check-input"
                    />
                    <label for="rememberMe" class="form-check-label">记住我</label>
                  </div>
                </div>

                <button
                  type="submit"
                  class="neu-button btn-3d w-100 mb-4"
                  :disabled="loading"
                >
                  <span v-if="loading">
                    <i class="bi bi-arrow-repeat spinner"></i>
                    登录中...
                  </span>
                  <span v-else>登录</span>
                </button>

                <div class="text-center">
                  <p class="mb-0">
                    没有账号?
                    <router-link to="/register" class="text-decoration-none">
                      立即注册
                    </router-link>
                  </p>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { login } from '@/api/user'

export default {
  name: 'ModernLoginView',
  setup() {
    const store = useStore()
    const router = useRouter()

    const loading = ref(false)
    const rememberMe = ref(false)

    const loginForm = reactive({
      userAccount: '',
      userPassword: ''
    })

    const handleLogin = async () => {
      // 表单验证
      if (!loginForm.userAccount || !loginForm.userPassword) {
        alert('请输入账号和密码')
        return
      }

      loading.value = true
      try {
        const response = await login(loginForm)
        if (response.code === 0) {
          // 登录成功，保存用户信息
          store.commit('user/SET_CURRENT_USER', response.data)

          // 根据用户角色跳转到不同页面
          if (response.data.userRole === 'admin') {
            router.push('/admin/dashboard')
          } else {
            router.push('/home')
          }
        } else {
          alert(`登录失败: ${response.message}`)
        }
      } catch (error) {
        alert(`登录异常: ${error.message || '未知错误'}`)
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      // 模拟加载
      setTimeout(() => {
        loading.value = false
      }, 1500)
    })

    return {
      loginForm,
      rememberMe,
      loading,
      handleLogin
    }
  }
}
</script>

<style scoped>
.modern-login-view {
  min-height: 100vh;
  background-color: var(--neu-background, #f0f0f3);
}

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: stretch;
}

/* 左侧信息区域 */
.info-side {
  height: 100%;
  min-height: 100vh;
  background: var(--gradient-primary);
  color: white;
  position: relative;
  overflow: hidden;
}

.info-content {
  padding: 3rem;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

.model-preview {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 2rem 0;
  border-radius: 12px;
  overflow: hidden;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.feature-item {
  display: flex;
  align-items: center;
  font-size: 1.1rem;
}

/* 右侧登录表单 */
.login-form-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  min-height: 100vh;
}

.login-form-wrapper {
  width: 100%;
  max-width: 450px;
  padding: 2.5rem;
}

.form-label {
  font-weight: 500;
  margin-bottom: 0.5rem;
}

/* 加载动画 */
.spinner {
  animation: spin 1s linear infinite;
  display: inline-block;
  margin-right: 0.5rem;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 模型占位符样式 */
.model-placeholder {
  height: 250px;
  background: linear-gradient(135deg, #4f46e5 0%, #8338ec 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.model-placeholder-content {
  text-align: center;
  color: white;
}

.model-placeholder-content i {
  font-size: 3rem;
  margin-bottom: 1rem;
  display: block;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .login-form-container {
    padding: 1.5rem;
  }

  .login-form-wrapper {
    padding: 2rem;
  }

  .model-placeholder {
    height: 200px;
  }
}
</style>
