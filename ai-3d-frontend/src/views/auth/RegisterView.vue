<template>
  <div class="register-container">
    <div class="register-form">
      <h2>用户注册</h2>
      <div class="form-group">
        <label for="userAccount">账号</label>
        <input 
          type="text" 
          id="userAccount" 
          v-model="registerForm.userAccount" 
          placeholder="请输入账号（至少4个字符）"
          required
        />
        <div class="form-tip" v-if="accountError">{{ accountError }}</div>
      </div>
      <div class="form-group">
        <label for="userPassword">密码</label>
        <input 
          type="password" 
          id="userPassword" 
          v-model="registerForm.userPassword" 
          placeholder="请输入密码（至少8个字符）"
          required
        />
        <div class="form-tip" v-if="passwordError">{{ passwordError }}</div>
      </div>
      <div class="form-group">
        <label for="checkPassword">确认密码</label>
        <input 
          type="password" 
          id="checkPassword" 
          v-model="registerForm.checkPassword" 
          placeholder="请再次输入密码"
          required
        />
        <div class="form-tip" v-if="checkPasswordError">{{ checkPasswordError }}</div>
      </div>
      <button 
        class="register-button" 
        @click="handleRegister" 
        :disabled="loading"
      >
        {{ loading ? '注册中...' : '注册' }}
      </button>
      <div class="form-footer">
        <router-link to="/login">已有账号？立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/user'

const router = useRouter()
const loading = ref(false)

// 表单数据
const registerForm = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

// 表单错误信息
const accountError = ref('')
const passwordError = ref('')
const checkPasswordError = ref('')

// 表单验证
const validateForm = () => {
  let isValid = true
  
  // 重置错误信息
  accountError.value = ''
  passwordError.value = ''
  checkPasswordError.value = ''
  
  // 验证账号
  if (!registerForm.userAccount) {
    accountError.value = '请输入账号'
    isValid = false
  } else if (registerForm.userAccount.length < 4) {
    accountError.value = '账号长度至少为4个字符'
    isValid = false
  }
  
  // 验证密码
  if (!registerForm.userPassword) {
    passwordError.value = '请输入密码'
    isValid = false
  } else if (registerForm.userPassword.length < 8) {
    passwordError.value = '密码长度至少为8个字符'
    isValid = false
  }
  
  // 验证确认密码
  if (!registerForm.checkPassword) {
    checkPasswordError.value = '请确认密码'
    isValid = false
  } else if (registerForm.userPassword !== registerForm.checkPassword) {
    checkPasswordError.value = '两次输入的密码不一致'
    isValid = false
  }
  
  return isValid
}

// 注册处理
const handleRegister = async () => {
  if (!validateForm()) {
    return
  }
  
  loading.value = true
  try {
    const response = await register(registerForm)
    if (response.code === 0) {
      alert('注册成功，请登录')
      router.push('/login')
    } else {
      alert(`注册失败: ${response.message}`)
    }
  } catch (error) {
    alert(`注册异常: ${error.message || '未知错误'}`)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.register-form {
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

.form-tip {
  margin-top: 5px;
  color: #ff4d4f;
  font-size: 14px;
}

.register-button {
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

.register-button:hover {
  background-color: #40a9ff;
}

.register-button:disabled {
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
