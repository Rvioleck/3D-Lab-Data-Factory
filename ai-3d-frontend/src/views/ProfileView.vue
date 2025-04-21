<template>
  <div class="profile-view">
    <div class="container py-5">
      <div class="row">
        <div class="col-lg-4">
          <div class="card mb-4">
            <div class="card-body text-center">
              <div class="avatar-container mb-3">
                <div class="avatar mx-auto">
                  <i class="bi bi-person-circle"></i>
                </div>
              </div>
              <h5 class="mb-1">{{ user?.userName || user?.userAccount }}</h5>
              <p class="text-muted mb-3">
                <span class="badge" :class="getRoleBadgeClass(user?.userRole)">
                  {{ getRoleText(user?.userRole) }}
                </span>
              </p>
              <div class="d-flex justify-content-center mb-2">
                <button class="btn btn-primary" @click="showEditProfileModal">
                  <i class="bi bi-pencil-square me-2"></i>编辑资料
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="col-lg-8">
          <div class="card mb-4">
            <div class="card-body">
              <div class="row">
                <div class="col-sm-3">
                  <p class="mb-0 text-muted">用户ID</p>
                </div>
                <div class="col-sm-9">
                  <p class="mb-0">{{ user?.id }}</p>
                </div>
              </div>
              <hr>
              <div class="row">
                <div class="col-sm-3">
                  <p class="mb-0 text-muted">账号</p>
                </div>
                <div class="col-sm-9">
                  <p class="mb-0">{{ user?.userAccount }}</p>
                </div>
              </div>
              <hr>
              <div class="row">
                <div class="col-sm-3">
                  <p class="mb-0 text-muted">用户名</p>
                </div>
                <div class="col-sm-9">
                  <p class="mb-0">{{ user?.userName || '未设置' }}</p>
                </div>
              </div>
              <hr>
              <div class="row">
                <div class="col-sm-3">
                  <p class="mb-0 text-muted">用户简介</p>
                </div>
                <div class="col-sm-9">
                  <p class="mb-0">{{ user?.userProfile || '未设置' }}</p>
                </div>
              </div>
              <hr>
              <div class="row">
                <div class="col-sm-3">
                  <p class="mb-0 text-muted">创建时间</p>
                </div>
                <div class="col-sm-9">
                  <p class="mb-0">{{ formatDateTime(user?.createTime) }}</p>
                </div>
              </div>
              <hr>
              <div class="row">
                <div class="col-sm-3">
                  <p class="mb-0 text-muted">最后登录</p>
                </div>
                <div class="col-sm-9">
                  <p class="mb-0">{{ formatDateTime(user?.lastLoginTime) || '未记录' }}</p>
                </div>
              </div>
            </div>
          </div>

          <div class="row">
            <div class="col-md-6">
              <div class="card mb-4">
                <div class="card-body">
                  <h5 class="card-title">账号安全</h5>
                  <p class="card-text">修改密码和安全设置</p>
                  <button class="btn btn-outline-primary" @click="showChangePasswordModal">
                    <i class="bi bi-shield-lock me-2"></i>修改密码
                  </button>
                </div>
              </div>
            </div>
            <div class="col-md-6">
              <UserActivityLog :userId="user?.id" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑资料模态框 -->
    <div class="modal fade" id="editProfileModal" tabindex="-1" aria-hidden="true" ref="editProfileModalRef">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">编辑个人资料</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="submitUpdateProfile">
              <div class="mb-3">
                <label for="userName" class="form-label">用户名</label>
                <input
                  type="text"
                  class="form-control"
                  id="userName"
                  v-model="profileForm.userName"
                />
              </div>
              <div class="mb-3">
                <label for="userProfile" class="form-label">个人简介</label>
                <textarea
                  class="form-control"
                  id="userProfile"
                  v-model="profileForm.userProfile"
                  rows="3"
                ></textarea>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button
              type="button"
              class="btn btn-primary"
              @click="submitUpdateProfile"
              :disabled="updateProfileLoading"
            >
              <span v-if="updateProfileLoading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
              保存修改
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码模态框 -->
    <div class="modal fade" id="changePasswordModal" tabindex="-1" aria-hidden="true" ref="changePasswordModalRef">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">修改密码</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="submitChangePassword">
              <div class="mb-3">
                <label for="oldPassword" class="form-label">当前密码</label>
                <input
                  type="password"
                  class="form-control"
                  id="oldPassword"
                  v-model="passwordForm.oldPassword"
                  required
                />
              </div>
              <div class="mb-3">
                <label for="newPassword" class="form-label">新密码</label>
                <input
                  type="password"
                  class="form-control"
                  id="newPassword"
                  v-model="passwordForm.newPassword"
                  required
                  minlength="8"
                />
                <div class="form-text">密码长度至少为8个字符</div>
              </div>
              <div class="mb-3">
                <label for="confirmPassword" class="form-label">确认新密码</label>
                <input
                  type="password"
                  class="form-control"
                  id="confirmPassword"
                  v-model="passwordForm.confirmPassword"
                  required
                />
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button
              type="button"
              class="btn btn-primary"
              @click="submitChangePassword"
              :disabled="changePasswordLoading"
            >
              <span v-if="changePasswordLoading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
              修改密码
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { Modal } from 'bootstrap'
import { getLoginUser, updateUserProfile, updateUserPassword } from '@/api/user'
import UserActivityLog from '@/components/UserActivityLog.vue'

export default {
  name: 'ProfileView',
  components: {
    UserActivityLog
  },
  setup() {
    const store = useStore()
    const router = useRouter()

    // 用户信息
    const user = computed(() => store.getters['user/currentUser'])

    // 如果未登录，重定向到登录页
    if (!store.getters['user/isLoggedIn']) {
      router.push('/login')
    }

    // 编辑资料模态框
    const editProfileModalRef = ref(null)
    const editProfileModal = ref(null)
    const updateProfileLoading = ref(false)
    const profileForm = ref({
      userName: '',
      userProfile: ''
    })

    // 修改密码模态框
    const changePasswordModalRef = ref(null)
    const changePasswordModal = ref(null)
    const changePasswordLoading = ref(false)
    const passwordForm = ref({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })

    // 初始化模态框
    onMounted(() => {
      editProfileModal.value = new Modal(editProfileModalRef.value)
      changePasswordModal.value = new Modal(changePasswordModalRef.value)

      // 刷新用户信息
      refreshUserInfo()
    })

    // 刷新用户信息
    const refreshUserInfo = async () => {
      try {
        const response = await getLoginUser()
        if (response.code === 0) {
          store.commit('user/SET_CURRENT_USER', response.data)
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    }

    // 显示编辑资料模态框
    const showEditProfileModal = () => {
      profileForm.value = {
        userName: user.value?.userName || '',
        userProfile: user.value?.userProfile || ''
      }
      editProfileModal.value.show()
    }

    // 提交更新资料
    const submitUpdateProfile = async () => {
      // 表单验证
      if (!profileForm.value.userName) {
        showToast('用户名不能为空', 'warning')
        return
      }

      updateProfileLoading.value = true
      try {
        // 调用更新用户资料的API
        const response = await updateUserProfile(profileForm.value)

        if (response.code === 0) {
          showToast('个人资料更新成功', 'success')
          editProfileModal.value.hide()

          // 更新本地用户状态
          store.commit('user/SET_CURRENT_USER', response.data)
        } else {
          showToast(`更新失败: ${response.message}`, 'danger')
        }
      } catch (error) {
        showToast(`更新资料失败: ${error.message || '未知错误'}`, 'danger')
      } finally {
        updateProfileLoading.value = false
      }
    }

    // 显示修改密码模态框
    const showChangePasswordModal = () => {
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      changePasswordModal.value.show()
    }

    // 提交修改密码
    const submitChangePassword = async () => {
      // 表单验证
      if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword || !passwordForm.value.confirmPassword) {
        showToast('请填写所有字段', 'warning')
        return
      }

      if (passwordForm.value.newPassword.length < 8) {
        showToast('新密码长度至少为8个字符', 'warning')
        return
      }

      if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
        showToast('两次输入的新密码不一致', 'warning')
        return
      }

      changePasswordLoading.value = true
      try {
        // 调用修改密码的API
        const response = await updateUserPassword({
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword,
          checkPassword: passwordForm.value.confirmPassword
        })

        if (response.code === 0) {
          showToast('密码修改成功', 'success')
          changePasswordModal.value.hide()

          // 清空表单
          passwordForm.value = {
            oldPassword: '',
            newPassword: '',
            confirmPassword: ''
          }
        } else {
          showToast(`修改失败: ${response.message}`, 'danger')
        }
      } catch (error) {
        showToast(`修改密码失败: ${error.message || '未知错误'}`, 'danger')
      } finally {
        changePasswordLoading.value = false
      }
    }

    // 显示提示消息
    const showToast = (message, type = 'info') => {
      // 创建toast元素
      const toastEl = document.createElement('div')
      toastEl.className = `toast align-items-center text-white bg-${type} border-0`
      toastEl.setAttribute('role', 'alert')
      toastEl.setAttribute('aria-live', 'assertive')
      toastEl.setAttribute('aria-atomic', 'true')

      // 设置内容
      toastEl.innerHTML = `
        <div class="d-flex">
          <div class="toast-body">
            ${message}
          </div>
          <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
      `

      // 添加到容器
      const toastContainer = document.getElementById('toast-container')
      if (!toastContainer) {
        const container = document.createElement('div')
        container.id = 'toast-container'
        container.className = 'toast-container position-fixed top-0 end-0 p-3'
        container.style.zIndex = '1050'
        document.body.appendChild(container)
        container.appendChild(toastEl)
      } else {
        toastContainer.appendChild(toastEl)
      }

      // 显示toast
      const toast = new bootstrap.Toast(toastEl, { delay: 3000 })
      toast.show()

      // 自动移除
      toastEl.addEventListener('hidden.bs.toast', () => {
        toastEl.remove()
      })
    }

    // 格式化日期时间
    const formatDateTime = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString()
    }

    // 获取角色文本
    const getRoleText = (role) => {
      switch (role) {
        case 'admin': return '管理员'
        case 'user': return '普通用户'
        case 'ban': return '已封禁'
        default: return '未知角色'
      }
    }

    // 获取角色徽章样式
    const getRoleBadgeClass = (role) => {
      switch (role) {
        case 'admin': return 'bg-primary'
        case 'user': return 'bg-success'
        case 'ban': return 'bg-danger'
        default: return 'bg-secondary'
      }
    }

    return {
      user,
      profileForm,
      passwordForm,
      editProfileModalRef,
      updateProfileLoading,
      changePasswordModalRef,
      changePasswordLoading,
      showEditProfileModal,
      submitUpdateProfile,
      showChangePasswordModal,
      submitChangePassword,
      formatDateTime,
      getRoleText,
      getRoleBadgeClass
    }
  }
}
</script>

<style scoped>
.profile-view {
  min-height: 100vh;
  background-color: var(--bg-secondary);
}

.avatar-container {
  position: relative;
  display: inline-block;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background-color: var(--bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
  font-size: 4rem;
  margin-bottom: 1rem;
}

.card {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: none;
}

.card-body {
  padding: 1.5rem;
}

.card-title {
  font-weight: 600;
  margin-bottom: 0.5rem;
}

@media (max-width: 768px) {
  .avatar {
    width: 100px;
    height: 100px;
    font-size: 3.5rem;
  }
}
</style>
