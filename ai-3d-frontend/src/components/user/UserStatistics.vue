<template>
  <div class="user-statistics">
    <div class="row g-3">
      <div class="col-md-3 col-sm-6">
        <div class="stat-card bg-primary text-white">
          <div class="stat-icon">
            <i class="bi bi-people-fill"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ totalUsers }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </div>
      </div>
      <div class="col-md-3 col-sm-6">
        <div class="stat-card bg-success text-white">
          <div class="stat-icon">
            <i class="bi bi-person-check-fill"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ activeUsers }}</div>
            <div class="stat-label">活跃用户</div>
          </div>
        </div>
      </div>
      <div class="col-md-3 col-sm-6">
        <div class="stat-card bg-info text-white">
          <div class="stat-icon">
            <i class="bi bi-person-badge"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ adminUsers }}</div>
            <div class="stat-label">管理员</div>
          </div>
        </div>
      </div>
      <div class="col-md-3 col-sm-6">
        <div class="stat-card bg-danger text-white">
          <div class="stat-icon">
            <i class="bi bi-person-x-fill"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ bannedUsers }}</div>
            <div class="stat-label">已封禁用户</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'UserStatistics',
  props: {
    users: {
      type: Array,
      required: true
    },
    totalCount: {
      type: Number,
      default: 0
    }
  },
  setup(props) {
    // 计算管理员用户数量
    const adminUsers = computed(() => {
      return props.users.filter(user => user.userRole === 'admin').length
    })
    
    // 计算普通用户数量
    const normalUsers = computed(() => {
      return props.users.filter(user => user.userRole === 'user').length
    })
    
    // 计算被封禁用户数量
    const bannedUsers = computed(() => {
      return props.users.filter(user => user.userRole === 'ban').length
    })
    
    // 计算活跃用户数量（这里简单使用普通用户数量，实际可能需要根据登录时间等判断）
    const activeUsers = computed(() => {
      return normalUsers.value
    })
    
    // 总用户数（如果传入了totalCount则使用，否则使用当前页面用户数量）
    const totalUsers = computed(() => {
      return props.totalCount || props.users.length
    })
    
    return {
      adminUsers,
      normalUsers,
      bannedUsers,
      activeUsers,
      totalUsers
    }
  }
}
</script>

<style scoped>
.user-statistics {
  margin-bottom: 1.5rem;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 1.25rem;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  height: 100%;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

.stat-icon {
  font-size: 2.5rem;
  margin-right: 1rem;
  opacity: 0.8;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 600;
  line-height: 1.2;
}

.stat-label {
  font-size: 0.875rem;
  opacity: 0.9;
}

@media (max-width: 768px) {
  .stat-card {
    padding: 1rem;
  }
  
  .stat-icon {
    font-size: 2rem;
  }
  
  .stat-value {
    font-size: 1.5rem;
  }
}
</style>
