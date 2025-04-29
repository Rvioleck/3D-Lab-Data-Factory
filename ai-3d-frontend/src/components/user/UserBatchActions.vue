<template>
  <div class="user-batch-actions">
    <div class="card">
      <div class="card-body">
        <div class="d-flex justify-content-between align-items-center">
          <div>
            <span class="me-2">已选择 {{ selectedUsers.length }} 个用户</span>
            <button 
              class="btn btn-sm btn-outline-secondary" 
              @click="clearSelection"
              v-if="selectedUsers.length > 0"
            >
              <i class="bi bi-x-circle me-1"></i>清除选择
            </button>
          </div>
          <div class="btn-group" v-if="selectedUsers.length > 0">
            <button 
              class="btn btn-sm btn-outline-primary" 
              @click="batchSetRole('user')"
              :disabled="!hasPermission"
            >
              <i class="bi bi-person me-1"></i>设为普通用户
            </button>
            <button 
              class="btn btn-sm btn-outline-warning" 
              @click="batchSetRole('ban')"
              :disabled="!hasPermission"
            >
              <i class="bi bi-slash-circle me-1"></i>封禁用户
            </button>
            <button 
              class="btn btn-sm btn-outline-danger" 
              @click="batchDelete"
              :disabled="!hasPermission || hasAdminSelected"
            >
              <i class="bi bi-trash me-1"></i>批量删除
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'UserBatchActions',
  props: {
    selectedUsers: {
      type: Array,
      required: true
    },
    isAdmin: {
      type: Boolean,
      default: false
    }
  },
  emits: ['clear-selection', 'batch-set-role', 'batch-delete'],
  setup(props, { emit }) {
    // 检查是否有权限执行批量操作（只有管理员可以）
    const hasPermission = computed(() => props.isAdmin)
    
    // 检查是否选中了管理员用户
    const hasAdminSelected = computed(() => {
      return props.selectedUsers.some(user => user.userRole === 'admin')
    })
    
    // 清除选择
    const clearSelection = () => {
      emit('clear-selection')
    }
    
    // 批量设置角色
    const batchSetRole = (role) => {
      if (!hasPermission.value) return
      
      // 如果要设置为管理员，需要二次确认
      if (role === 'admin') {
        if (!confirm(`确定要将选中的 ${props.selectedUsers.length} 个用户设置为管理员吗？`)) {
          return
        }
      }
      
      // 如果要封禁用户，需要二次确认
      if (role === 'ban') {
        if (!confirm(`确定要封禁选中的 ${props.selectedUsers.length} 个用户吗？`)) {
          return
        }
      }
      
      emit('batch-set-role', { users: props.selectedUsers, role })
    }
    
    // 批量删除
    const batchDelete = () => {
      if (!hasPermission.value || hasAdminSelected.value) return
      
      if (confirm(`确定要删除选中的 ${props.selectedUsers.length} 个用户吗？此操作不可恢复。`)) {
        emit('batch-delete', props.selectedUsers)
      }
    }
    
    return {
      hasPermission,
      hasAdminSelected,
      clearSelection,
      batchSetRole,
      batchDelete
    }
  }
}
</script>

<style scoped>
.user-batch-actions {
  margin-bottom: 1rem;
  transition: opacity 0.3s ease;
}

.btn-group .btn {
  display: flex;
  align-items: center;
}

@media (max-width: 768px) {
  .user-batch-actions .card-body {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .user-batch-actions .btn-group {
    margin-top: 0.5rem;
    width: 100%;
  }
  
  .user-batch-actions .btn-group .btn {
    flex: 1;
  }
}
</style>
