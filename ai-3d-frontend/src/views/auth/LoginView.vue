<template>
  <div class="login-container">
    <div class="login-form">
      <h2>用户登录</h2>
      <div class="form-group">
        <label for="userAccount">账号</label>
        <input 
          type="text" 
          id="userAccount" 
          v-model="loginForm.userAccount" 
          placeholder="请输入账号"
          required
        />
      </div>
      <div class="form-group">
        <label for="userPassword">密码</label>
        <input 
          type="password" 
          id="userPassword" 
          v-model="loginForm.userPassword" 
          placeholder="请输入密码"
          required
        />
      </div>
      <div class="form-group remember-me">
        <input type="checkbox" id="rememberMe" v-model="rememberMe" />
        <label for="rememberMe">记住我</label>
      </div>
      <button 
        class="login-button" 
        @click="handleLogin" 
        :disabled="loading"
      >
        {{ loading ? '登录中...' : '登录' }}
      </button>
      <div class="form-footer">
        <router-link to="/register">没有账号？立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
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
      userStore.setUser(response.data)
      
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
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.login-form {
  width: 400px;
  padding: 30px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

input[type="text"],
input[type="password"] {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.remember-me {
  display: flex;
  align-items: center;
}

.remember-me input {
  margin-right: 8px;
}

.login-button {
  width: 100%;
  padding: 12px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-button:hover {
  background-color: #40a9ff;
}

.login-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.form-footer {
  margin-top: 16px;
  text-align: center;
}

.form-footer a {
  color: #1890ff;
  text-decoration: none;
}
</style>
