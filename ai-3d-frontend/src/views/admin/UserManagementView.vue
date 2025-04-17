<template>
  <div class="user-management-view">
    <div class="container py-4">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>用户管理</h2>
        <div>
          <button class="btn btn-outline-primary me-2" @click="exportUserData">
            <i class="bi bi-download me-1"></i>导出数据
          </button>
          <button class="btn btn-primary" @click="showCreateUserModal">
            <i class="bi bi-person-plus me-1"></i>创建用户
          </button>
        </div>
      </div>

      <!-- 用户统计 -->
      <UserStatistics :users="users" :total-count="totalUsers" class="mb-4" />

      <!-- 批量操作 -->
      <UserBatchActions
        v-if="selectedUsers.length > 0"
        :selected-users="selectedUsers"
        :is-admin="isAdmin"
        @clear-selection="clearSelectedUsers"
        @batch-set-role="handleBatchSetRole"
        @batch-delete="handleBatchDelete"
      />

      <!-- 搜索和筛选 -->
      <div class="card mb-4">
        <div class="card-body">
          <div class="row g-3">
            <div class="col-md-4">
              <div class="input-group">
                <span class="input-group-text">
                  <i class="bi bi-search"></i>
                </span>
                <input
                  type="text"
                  class="form-control"
                  placeholder="搜索用户账号或名称"
                  v-model="searchText"
                  @input="handleSearch"
                />
              </div>
            </div>
            <div class="col-md-2">
              <select class="form-select" v-model="roleFilter" @change="handleSearch">
                <option value="">所有角色</option>
                <option value="admin">管理员</option>
                <option value="user">普通用户</option>
                <option value="ban">已封禁</option>
              </select>
            </div>
            <div class="col-md-2">
              <select class="form-select" v-model="statusFilter" @change="handleSearch">
                <option value="">所有状态</option>
                <option value="active">活跃</option>
                <option value="inactive">非活跃</option>
              </select>
            </div>
            <div class="col-md-2">
              <div class="input-group">
                <span class="input-group-text">每页</span>
                <select class="form-select" v-model="pageSize" @change="handlePageSizeChange">
                  <option value="10">10</option>
                  <option value="20">20</option>
                  <option value="50">50</option>
                </select>
              </div>
            </div>
            <div class="col-md-2">
              <button class="btn btn-outline-secondary w-100" @click="resetFilters">
                <i class="bi bi-arrow-counterclockwise me-1"></i>重置筛选
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 用户列表 -->
      <div class="card">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>
                <div class="form-check">
                  <input
                    class="form-check-input"
                    type="checkbox"
                    id="select-all-users"
                    :checked="isAllSelected"
                    @change="toggleAllSelection"
                  >
                  <label class="form-check-label" for="select-all-users">ID</label>
                </div>
              </th>
                <th>账号</th>
                <th>用户名</th>
                <th>角色</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="6" class="text-center py-4">
                  <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">加载中...</span>
                  </div>
                </td>
              </tr>
              <tr v-else-if="users.length === 0">
                <td colspan="6" class="text-center py-4">
                  <div class="text-muted">
                    <i class="bi bi-inbox-fill me-2"></i>没有找到用户
                  </div>
                </td>
              </tr>
              <tr v-for="user in users" :key="user.id" :class="{ 'table-active': isUserSelected(user) }">
                <td>
                  <div class="form-check">
                    <input
                      class="form-check-input"
                      type="checkbox"
                      :id="`user-${user.id}`"
                      :checked="isUserSelected(user)"
                      @change="toggleUserSelection(user)"
                    >
                    <label class="form-check-label" :for="`user-${user.id}`">
                      {{ user.id }}
                    </label>
                  </div>
                </td>
                <td>
                  <div class="d-flex align-items-center">
                    <div class="user-avatar-sm me-2">
                      <i class="bi bi-person-circle"></i>
                    </div>
                    {{ user.userAccount }}
                  </div>
                </td>
                <td>{{ user.userName || '-' }}</td>
                <td>
                  <span class="badge" :class="getRoleBadgeClass(user.userRole)">
                    {{ getRoleText(user.userRole) }}
                  </span>
                </td>
                <td>{{ formatDate(user.createTime) }}</td>
                <td>
                  <div class="btn-group">
                    <button
                      class="btn btn-sm btn-outline-primary"
                      @click="viewUserDetail(user.id)"
                      title="查看详情"
                    >
                      <i class="bi bi-eye"></i>
                    </button>
                    <button
                      class="btn btn-sm btn-outline-secondary"
                      @click="editUser(user)"
                      title="编辑用户"
                    >
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button
                      class="btn btn-sm btn-outline-danger"
                      @click="confirmDeleteUser(user)"
                      title="删除用户"
                      :disabled="user.userRole === 'admin'"
                    >
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页 -->
        <div class="card-footer d-flex justify-content-between align-items-center">
          <div>
            显示 {{ (currentPage - 1) * pageSize + 1 }} -
            {{ Math.min(currentPage * pageSize, totalUsers) }}
            条，共 {{ totalUsers }} 条
          </div>
          <nav aria-label="Page navigation">
            <ul class="pagination mb-0">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <button class="page-link" @click="goToPage(currentPage - 1)" :disabled="currentPage === 1">
                  上一页
                </button>
              </li>
              <li
                v-for="page in getPageNumbers()"
                :key="page"
                class="page-item"
                :class="{ active: page === currentPage }"
              >
                <button class="page-link" @click="goToPage(page)">{{ page }}</button>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                <button class="page-link" @click="goToPage(currentPage + 1)" :disabled="currentPage === totalPages">
                  下一页
                </button>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>

    <!-- 创建用户模态框 -->
    <div class="modal fade" id="createUserModal" tabindex="-1" aria-hidden="true" ref="createUserModalRef">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">创建新用户</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="submitCreateUser">
              <div class="mb-3">
                <label for="userAccount" class="form-label">用户账号 <span class="text-danger">*</span></label>
                <input
                  type="text"
                  class="form-control"
                  id="userAccount"
                  v-model="newUser.userAccount"
                  required
                  minlength="4"
                />
                <div class="form-text">账号长度至少为4个字符</div>
              </div>
              <div class="mb-3">
                <label for="userPassword" class="form-label">用户密码 <span class="text-danger">*</span></label>
                <input
                  type="password"
                  class="form-control"
                  id="userPassword"
                  v-model="newUser.userPassword"
                  required
                  minlength="8"
                />
                <div class="form-text">密码长度至少为8个字符</div>
              </div>
              <div class="mb-3">
                <label for="checkPassword" class="form-label">确认密码 <span class="text-danger">*</span></label>
                <input
                  type="password"
                  class="form-control"
                  id="checkPassword"
                  v-model="newUser.checkPassword"
                  required
                />
              </div>
              <div class="mb-3">
                <label for="userName" class="form-label">用户名</label>
                <input
                  type="text"
                  class="form-control"
                  id="userName"
                  v-model="newUser.userName"
                />
              </div>
              <div class="mb-3">
                <label for="userRole" class="form-label">用户角色</label>
                <select class="form-select" id="userRole" v-model="newUser.userRole">
                  <option value="user">普通用户</option>
                  <option value="admin">管理员</option>
                  <option value="ban">已封禁</option>
                </select>
              </div>
              <div class="mb-3">
                <label for="userProfile" class="form-label">用户简介</label>
                <textarea
                  class="form-control"
                  id="userProfile"
                  v-model="newUser.userProfile"
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
              @click="submitCreateUser"
              :disabled="createUserLoading"
            >
              <span v-if="createUserLoading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
              创建用户
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑用户模态框 -->
    <div class="modal fade" id="editUserModal" tabindex="-1" aria-hidden="true" ref="editUserModalRef">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">编辑用户</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="submitUpdateUser">
              <div class="mb-3">
                <label for="editUserAccount" class="form-label">用户账号</label>
                <input
                  type="text"
                  class="form-control"
                  id="editUserAccount"
                  v-model="editingUser.userAccount"
                  disabled
                />
              </div>
              <div class="mb-3">
                <label for="editUserPassword" class="form-label">用户密码</label>
                <input
                  type="password"
                  class="form-control"
                  id="editUserPassword"
                  v-model="editingUser.userPassword"
                  placeholder="留空表示不修改密码"
                />
                <div class="form-text">如需修改密码，请输入新密码，长度至少为8个字符</div>
              </div>
              <div class="mb-3">
                <label for="editUserName" class="form-label">用户名</label>
                <input
                  type="text"
                  class="form-control"
                  id="editUserName"
                  v-model="editingUser.userName"
                />
              </div>
              <div class="mb-3">
                <label for="editUserRole" class="form-label">用户角色</label>
                <select class="form-select" id="editUserRole" v-model="editingUser.userRole">
                  <option value="user">普通用户</option>
                  <option value="admin">管理员</option>
                  <option value="ban">已封禁</option>
                </select>
              </div>
              <div class="mb-3">
                <label for="editUserProfile" class="form-label">用户简介</label>
                <textarea
                  class="form-control"
                  id="editUserProfile"
                  v-model="editingUser.userProfile"
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
              @click="submitUpdateUser"
              :disabled="updateUserLoading"
            >
              <span v-if="updateUserLoading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
              保存修改
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 用户详情模态框 -->
    <div class="modal fade" id="userDetailModal" tabindex="-1" aria-hidden="true" ref="userDetailModalRef">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">用户详情</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div v-if="userDetailLoading" class="text-center py-4">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">加载中...</span>
              </div>
            </div>
            <div v-else-if="userDetail">
              <div class="user-detail-header d-flex align-items-center mb-4">
                <div class="avatar me-3">
                  <i class="bi bi-person-circle" style="font-size: 3rem;"></i>
                </div>
                <div>
                  <h4>{{ userDetail.userName || userDetail.userAccount }}</h4>
                  <span class="badge" :class="getRoleBadgeClass(userDetail.userRole)">
                    {{ getRoleText(userDetail.userRole) }}
                  </span>
                </div>
              </div>
              <div class="user-detail-info">
                <div class="row mb-2">
                  <div class="col-4 text-muted">用户ID</div>
                  <div class="col-8">{{ userDetail.id }}</div>
                </div>
                <div class="row mb-2">
                  <div class="col-4 text-muted">用户账号</div>
                  <div class="col-8">{{ userDetail.userAccount }}</div>
                </div>
                <div class="row mb-2">
                  <div class="col-4 text-muted">用户名</div>
                  <div class="col-8">{{ userDetail.userName || '未设置' }}</div>
                </div>
                <div class="row mb-2">
                  <div class="col-4 text-muted">用户简介</div>
                  <div class="col-8">{{ userDetail.userProfile || '未设置' }}</div>
                </div>
                <div class="row mb-2">
                  <div class="col-4 text-muted">创建时间</div>
                  <div class="col-8">{{ formatDateTime(userDetail.createTime) }}</div>
                </div>
                <div class="row mb-2">
                  <div class="col-4 text-muted">更新时间</div>
                  <div class="col-8">{{ formatDateTime(userDetail.updateTime) }}</div>
                </div>
                <div class="row mb-2" v-if="userDetail.editTime">
                  <div class="col-4 text-muted">编辑时间</div>
                  <div class="col-8">{{ formatDateTime(userDetail.editTime) }}</div>
                </div>
                <div class="row mb-2" v-if="userDetail.lastLoginTime">
                  <div class="col-4 text-muted">最后登录</div>
                  <div class="col-8">{{ formatDateTime(userDetail.lastLoginTime) }}</div>
                </div>
                <div class="row mb-2" v-if="userDetail.userStatus">
                  <div class="col-4 text-muted">用户状态</div>
                  <div class="col-8">{{ userDetail.userStatus }}</div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
            <button
              type="button"
              class="btn btn-primary"
              @click="editUser(userDetail)"
              data-bs-dismiss="modal"
            >
              编辑用户
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
import { Modal, Toast } from 'bootstrap'
import {
  createUser,
  getUserById,
  getUserDetailById,
  updateUser,
  deleteUser,
  listUsersByPage
} from '@/api/admin'
import UserStatistics from '@/components/UserStatistics.vue'
import UserBatchActions from '@/components/UserBatchActions.vue'

export default {
  name: 'UserManagementView',
  components: {
    UserStatistics,
    UserBatchActions
  },
  setup() {
    const store = useStore()
    const router = useRouter()

    // 检查是否是管理员
    const isAdmin = computed(() => store.getters['user/isAdmin'])

    // 如果不是管理员，重定向到首页
    if (!isAdmin.value) {
      router.push('/home')
    }

    // 用户列表状态
    const users = ref([])
    const loading = ref(false)
    const totalUsers = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalPages = computed(() => Math.ceil(totalUsers.value / pageSize.value))

    // 选中的用户
    const selectedUsers = ref([])
    const isAllSelected = computed(() => {
      return users.value.length > 0 && selectedUsers.value.length === users.value.length
    })

    // 搜索和筛选
    const searchText = ref('')
    const roleFilter = ref('')
    const statusFilter = ref('')

    // 创建用户状态
    const createUserModalRef = ref(null)
    const createUserModal = ref(null)
    const createUserLoading = ref(false)
    const newUser = ref({
      userAccount: '',
      userPassword: '',
      checkPassword: '',
      userName: '',
      userRole: 'user',
      userProfile: ''
    })

    // 编辑用户状态
    const editUserModalRef = ref(null)
    const editUserModal = ref(null)
    const updateUserLoading = ref(false)
    const editingUser = ref({
      id: null,
      userAccount: '',
      userPassword: '',
      userName: '',
      userRole: '',
      userProfile: ''
    })

    // 用户详情状态
    const userDetailModalRef = ref(null)
    const userDetailModal = ref(null)
    const userDetail = ref(null)
    const userDetailLoading = ref(false)

    // 初始化模态框
    onMounted(() => {
      createUserModal.value = new Modal(createUserModalRef.value)
      editUserModal.value = new Modal(editUserModalRef.value)
      userDetailModal.value = new Modal(userDetailModalRef.value)

      // 加载用户列表
      fetchUsers()
    })

    // 获取用户列表
    const fetchUsers = async () => {
      loading.value = true
      try {
        const queryParams = {
          current: currentPage.value,
          pageSize: pageSize.value
        }

        // 添加搜索条件
        if (searchText.value) {
          if (searchText.value.includes('@')) {
            queryParams.userAccount = searchText.value
          } else {
            queryParams.userName = searchText.value
          }
        }

        // 添加角色筛选
        if (roleFilter.value) {
          queryParams.userRole = roleFilter.value
        }

        // 添加状态筛选
        if (statusFilter.value) {
          queryParams.userStatus = statusFilter.value
        }

        const response = await listUsersByPage(queryParams)
        if (response.code === 0) {
          users.value = response.data.records
          totalUsers.value = response.data.total
        } else {
          console.error('获取用户列表失败:', response.message)
        }
      } catch (error) {
        console.error('获取用户列表异常:', error)
      } finally {
        loading.value = false
      }
    }

    // 处理搜索
    const handleSearch = () => {
      currentPage.value = 1
      fetchUsers()
    }

    // 重置筛选
    const resetFilters = () => {
      searchText.value = ''
      roleFilter.value = ''
      statusFilter.value = ''
      currentPage.value = 1
      fetchUsers()
    }

    // 处理分页大小变化
    const handlePageSizeChange = () => {
      currentPage.value = 1
      fetchUsers()
    }

    // 跳转到指定页
    const goToPage = (page) => {
      if (page < 1 || page > totalPages.value) return
      currentPage.value = page
      fetchUsers()
    }

    // 获取分页数字
    const getPageNumbers = () => {
      const pages = []
      const maxVisiblePages = 5

      if (totalPages.value <= maxVisiblePages) {
        // 如果总页数小于等于最大可见页数，显示所有页码
        for (let i = 1; i <= totalPages.value; i++) {
          pages.push(i)
        }
      } else {
        // 否则，显示当前页附近的页码
        let startPage = Math.max(1, currentPage.value - Math.floor(maxVisiblePages / 2))
        let endPage = startPage + maxVisiblePages - 1

        if (endPage > totalPages.value) {
          endPage = totalPages.value
          startPage = Math.max(1, endPage - maxVisiblePages + 1)
        }

        for (let i = startPage; i <= endPage; i++) {
          pages.push(i)
        }
      }

      return pages
    }

    // 显示创建用户模态框
    const showCreateUserModal = () => {
      // 重置表单
      newUser.value = {
        userAccount: '',
        userPassword: '',
        checkPassword: '',
        userName: '',
        userRole: 'user',
        userProfile: ''
      }
      createUserModal.value.show()
    }

    // 提交创建用户
    const submitCreateUser = async () => {
      // 表单验证
      if (!newUser.value.userAccount || !newUser.value.userPassword || !newUser.value.checkPassword) {
        showToast('请填写必填字段', 'warning')
        return
      }

      if (newUser.value.userAccount.length < 4) {
        showToast('用户账号长度至少为4个字符', 'warning')
        return
      }

      if (newUser.value.userPassword.length < 8) {
        showToast('密码长度至少为8个字符', 'warning')
        return
      }

      if (newUser.value.userPassword !== newUser.value.checkPassword) {
        showToast('两次输入的密码不一致', 'warning')
        return
      }

      createUserLoading.value = true
      try {
        const response = await createUser(newUser.value)
        if (response.code === 0) {
          showToast('创建用户成功', 'success')
          createUserModal.value.hide()
          fetchUsers()
        } else {
          showToast(`创建用户失败: ${response.message}`, 'danger')
        }
      } catch (error) {
        showToast(`创建用户异常: ${error.message || '未知错误'}`, 'danger')
      } finally {
        createUserLoading.value = false
      }
    }

    // 查看用户详情
    const viewUserDetail = async (userId) => {
      userDetail.value = null
      userDetailLoading.value = true
      userDetailModal.value.show()

      try {
        const response = await getUserDetailById(userId)
        if (response.code === 0) {
          userDetail.value = response.data
        } else {
          alert(`获取用户详情失败: ${response.message}`)
        }
      } catch (error) {
        alert(`获取用户详情异常: ${error.message || '未知错误'}`)
      } finally {
        userDetailLoading.value = false
      }
    }

    // 编辑用户
    const editUser = (user) => {
      editingUser.value = {
        id: user.id,
        userAccount: user.userAccount,
        userPassword: '', // 不回显密码
        userName: user.userName,
        userRole: user.userRole,
        userProfile: user.userProfile
      }

      // 如果用户详情模态框已打开，先关闭它
      if (userDetailModal.value._isShown) {
        userDetailModal.value.hide()
      }

      editUserModal.value.show()
    }

    // 提交更新用户
    const submitUpdateUser = async () => {
      // 表单验证
      if (editingUser.value.userPassword && editingUser.value.userPassword.length < 8) {
        showToast('密码长度至少为8个字符', 'warning')
        return
      }

      updateUserLoading.value = true
      try {
        const response = await updateUser(editingUser.value)
        if (response.code === 0) {
          showToast('更新用户成功', 'success')
          editUserModal.value.hide()
          fetchUsers()
        } else {
          showToast(`更新用户失败: ${response.message}`, 'danger')
        }
      } catch (error) {
        showToast(`更新用户异常: ${error.message || '未知错误'}`, 'danger')
      } finally {
        updateUserLoading.value = false
      }
    }

    // 确认删除用户
    const confirmDeleteUser = (user) => {
      if (user.userRole === 'admin') {
        showToast('不能删除管理员用户', 'warning')
        return
      }

      if (confirm(`确定要删除用户 "${user.userName || user.userAccount}" 吗？此操作不可恢复。`)) {
        deleteUserById(user.id)
      }
    }

    // 删除用户
    const deleteUserById = async (userId) => {
      loading.value = true
      try {
        const response = await deleteUser(userId)
        if (response.code === 0) {
          showToast('删除用户成功', 'success')
          fetchUsers()
        } else {
          showToast(`删除用户失败: ${response.message}`, 'danger')
        }
      } catch (error) {
        showToast(`删除用户异常: ${error.message || '未知错误'}`, 'danger')
      } finally {
        loading.value = false
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

    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString()
    }

    // 格式化日期时间
    const formatDateTime = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString()
    }

    // 选择用户相关方法
    const isUserSelected = (user) => {
      return selectedUsers.value.some(selectedUser => selectedUser.id === user.id)
    }

    const toggleUserSelection = (user) => {
      if (isUserSelected(user)) {
        selectedUsers.value = selectedUsers.value.filter(selectedUser => selectedUser.id !== user.id)
      } else {
        selectedUsers.value.push(user)
      }
    }

    const toggleAllSelection = () => {
      if (isAllSelected.value) {
        selectedUsers.value = []
      } else {
        selectedUsers.value = [...users.value]
      }
    }

    const clearSelectedUsers = () => {
      selectedUsers.value = []
    }

    // 批量操作方法
    const handleBatchSetRole = async ({ users: batchUsers, role }) => {
      if (!batchUsers || batchUsers.length === 0) return

      loading.value = true
      try {
        // 这里应该调用后端批量更新API
        // 但目前后端可能没有这个接口，所以模拟一个串行处理
        let successCount = 0
        let failCount = 0

        for (const user of batchUsers) {
          // 跳过管理员用户
          if (user.userRole === 'admin' && role === 'ban') {
            failCount++
            continue
          }

          try {
            const updateData = {
              id: user.id,
              userRole: role
            }
            const response = await updateUser(updateData)
            if (response.code === 0) {
              successCount++
            } else {
              failCount++
            }
          } catch (error) {
            console.error(`更新用户 ${user.id} 失败:`, error)
            failCount++
          }
        }

        if (successCount > 0) {
          showToast(`成功更新 ${successCount} 个用户的角色${failCount > 0 ? `，${failCount} 个用户失败` : ''}`, 'success')
          fetchUsers()
          clearSelectedUsers()
        } else if (failCount > 0) {
          showToast(`所有 ${failCount} 个用户更新失败`, 'danger')
        }
      } catch (error) {
        showToast(`批量操作失败: ${error.message || '未知错误'}`, 'danger')
      } finally {
        loading.value = false
      }
    }

    const handleBatchDelete = async (batchUsers) => {
      if (!batchUsers || batchUsers.length === 0) return

      loading.value = true
      try {
        // 这里应该调用后端批量删除API
        // 但目前后端可能没有这个接口，所以模拟一个串行处理
        let successCount = 0
        let failCount = 0

        for (const user of batchUsers) {
          // 跳过管理员用户
          if (user.userRole === 'admin') {
            failCount++
            continue
          }

          try {
            const response = await deleteUser(user.id)
            if (response.code === 0) {
              successCount++
            } else {
              failCount++
            }
          } catch (error) {
            console.error(`删除用户 ${user.id} 失败:`, error)
            failCount++
          }
        }

        if (successCount > 0) {
          showToast(`成功删除 ${successCount} 个用户${failCount > 0 ? `，${failCount} 个用户失败` : ''}`, 'success')
          fetchUsers()
          clearSelectedUsers()
        } else if (failCount > 0) {
          showToast(`所有 ${failCount} 个用户删除失败`, 'danger')
        }
      } catch (error) {
        showToast(`批量删除失败: ${error.message || '未知错误'}`, 'danger')
      } finally {
        loading.value = false
      }
    }

    // 导出用户数据
    const exportUserData = async () => {
      try {
        // 获取所有用户数据
        loading.value = true
        showToast('正在准备导出数据...', 'info')

        // 这里可以调用API获取所有用户，或者使用当前页面的数据
        // 为简化实现，这里使用当前页面的数据
        const userData = users.value.map(user => ({
          ID: user.id,
          账号: user.userAccount,
          用户名: user.userName || '',
          角色: getRoleText(user.userRole),
          创建时间: formatDateTime(user.createTime),
          更新时间: formatDateTime(user.updateTime)
        }))

        // 转换为CSV格式
        const headers = Object.keys(userData[0])
        const csvContent = [
          headers.join(','), // 表头
          ...userData.map(row => headers.map(header => `"${row[header] || ''}"`).join(','))
        ].join('\n')

        // 创建下载链接
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.setAttribute('href', url)
        link.setAttribute('download', `用户数据_${new Date().toISOString().slice(0, 10)}.csv`)
        link.style.visibility = 'hidden'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)

        showToast('数据导出成功', 'success')
      } catch (error) {
        console.error('导出数据失败:', error)
        showToast('导出数据失败: ' + (error.message || '未知错误'), 'danger')
      } finally {
        loading.value = false
      }
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
      users,
      loading,
      totalUsers,
      currentPage,
      pageSize,
      totalPages,
      searchText,
      roleFilter,
      createUserModalRef,
      createUserLoading,
      newUser,
      editUserModalRef,
      updateUserLoading,
      editingUser,
      userDetailModalRef,
      userDetail,
      userDetailLoading,
      statusFilter,
      handleSearch,
      resetFilters,
      handlePageSizeChange,
      goToPage,
      getPageNumbers,
      showCreateUserModal,
      submitCreateUser,
      viewUserDetail,
      editUser,
      submitUpdateUser,
      confirmDeleteUser,
      formatDate,
      formatDateTime,
      getRoleText,
      getRoleBadgeClass,
      showToast,
      exportUserData,
      selectedUsers,
      isAllSelected,
      isUserSelected,
      toggleUserSelection,
      toggleAllSelection,
      clearSelectedUsers,
      handleBatchSetRole,
      handleBatchDelete
    }
  }
}
</script>

<style scoped>
.user-management-view {
  min-height: 100vh;
  background-color: var(--bg-secondary);
}

.table th {
  font-weight: 600;
}

.btn-group .btn {
  padding: 0.25rem 0.5rem;
}

.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: var(--bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
}

.user-avatar-sm {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: var(--bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
  font-size: 1.2rem;
}

.user-detail-header {
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 1rem;
}

.user-detail-info .row {
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--border-color-light);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .table-responsive {
    font-size: 0.875rem;
  }

  .btn-group .btn {
    padding: 0.2rem 0.4rem;
  }

  .user-avatar-sm {
    width: 28px;
    height: 28px;
    font-size: 1rem;
  }
}
</style>
